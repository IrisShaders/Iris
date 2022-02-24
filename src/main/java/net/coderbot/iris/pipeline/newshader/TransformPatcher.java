package net.coderbot.iris.pipeline.newshader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.ExternalDeclarationContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VersionStatementContext;
import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.SearchTerminalsDynamic;
import io.github.douira.glsl_transformer.core.SearchTerminalsImpl;
import io.github.douira.glsl_transformer.core.WrapIdentifier;
import io.github.douira.glsl_transformer.core.WrapIdentifierExternalDeclaration;
import io.github.douira.glsl_transformer.core.target.HandlerTarget;
import io.github.douira.glsl_transformer.core.target.HandlerTargetImpl;
import io.github.douira.glsl_transformer.core.target.ParsedReplaceTarget;
import io.github.douira.glsl_transformer.core.target.ParsedReplaceTargetImpl;
import io.github.douira.glsl_transformer.core.target.TerminalReplaceTargetImpl;
import io.github.douira.glsl_transformer.core.target.ThrowTargetImpl;
import io.github.douira.glsl_transformer.core.target.WrapThrowTargetImpl;
import io.github.douira.glsl_transformer.print.filter.ChannelFilter;
import io.github.douira.glsl_transformer.print.filter.TokenChannel;
import io.github.douira.glsl_transformer.print.filter.TokenFilter;
import io.github.douira.glsl_transformer.transform.RunPhase;
import io.github.douira.glsl_transformer.transform.SemanticException;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import io.github.douira.glsl_transformer.transform.TransformationPhase.InjectionPoint;
import io.github.douira.glsl_transformer.transform.WalkPhase;
import io.github.douira.glsl_transformer.tree.ExtendedContext;
import io.github.douira.glsl_transformer.tree.TreeMember;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.AttributeShaderTransformer;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

/**
 * The transform patcher (triforce 2) uses glsl-transformer to do shader
 * transformation.
 * 
 * A separate TransformationManager is created for each ShaderType.
 * That makes each of them more efficient as they don't need to run unnecessary
 * transformation phases.
 * 
 * NOTE: This patcher expects the string to not contain any (!) preprocessor
 * directives. The only allowed ones are #extension and #pragma as they are
 * considered "parsed" directives. If any other directive appears in the string,
 * it will throw.
 * 
 * TODO: JCPP has to be configured to remove preprocessor directives entirely
 * 
 * TODO: good examples for more complex transformation in triforce patcher?
 * ideas: BuiltinUniformReplacementTransformer, defines/replacements with loops,
 * replacements that account for whitespace like the one for gl_TextureMatrix
 * 
 * TODO: use new glsl-transformer features on RunPhase to make all the RunPhase
 * subclasses much more comapct (just one method call:
 * withInjectExternalDeclarations)
 */
public class TransformPatcher implements Patcher {
  private static final Logger LOGGER = LogManager.getLogger(TransformPatcher.class);

  private static enum Patch {
    VANILLA, SODIUM, COMPOSITE
  }

  private static class Parameters {
    public final Patch patch;
    public final ShaderType type;

    public Parameters(Patch patch, ShaderType type) {
      this.patch = patch;
      this.type = type;
    }

    public AlphaTest getAlphaTest() {
      return null;
    }
  }

  private static class VanillaParameters extends Parameters {
    public final AlphaTest alpha;
    public final boolean hasChunkOffset;
    public final ShaderAttributeInputs inputs;

    public VanillaParameters(Patch patch, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
        ShaderAttributeInputs inputs) {
      super(patch, type);
      this.alpha = alpha;
      this.hasChunkOffset = hasChunkOffset;
      this.inputs = inputs;
    }

    @Override
    public AlphaTest getAlphaTest() {
      return alpha;
    }
  }

  private static class SodiumParameters extends Parameters {
    public final AlphaTest alpha;
    public final ShaderAttributeInputs inputs;
    public final float positionScale;
    public final float positionOffset;
    public final float textureScale;

    public SodiumParameters(Patch patch, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
        float positionScale, float positionOffset, float textureScale) {
      super(patch, type);
      this.alpha = alpha;
      this.inputs = inputs;
      this.positionScale = positionScale;
      this.positionOffset = positionOffset;
      this.textureScale = textureScale;
    }

    @Override
    public AlphaTest getAlphaTest() {
      return alpha;
    }
  }

  private enum FragColorOutput {
    COLOR, DATA, CUSTOM
  }

  /**
   * A transformation manager is kept for each patch type and shader type for
   * better performance since it allows the phase collector to compact the whole
   * run better. The object parameter can be filled with one of the parameter
   * classes and the transformation phases have to do their own casts.
   */
  private Table<Patch, ShaderType, TransformationManager<Parameters>> managers = HashBasedTable.create(
      Patch.values().length,
      ShaderType.values().length);

  public TransformPatcher() {
    /**
     * PREV TODO: Only do the NewLines patches if the source code isn't from
     * gbuffers_lines
     */

    TokenFilter<Parameters> parseTokenFilter = new ChannelFilter<Parameters>(TokenChannel.PREPROCESSOR) {
      @Override
      public boolean isTokenAllowed(Token token) {
        if (!super.isTokenAllowed(token)) {
          throw new SemanticException("Unparsed preprocessor directives such as '" + token.getText()
              + "' may not be present at this stage of shader processing!");
        }
        return true;
      }
    };

    // setup the transformations and even loose phases if necessary
    Transformation<Parameters> detectReserved = new Transformation<Parameters>(
        new SearchTerminalsImpl<Parameters>(SearchTerminals.IDENTIFIER,
            ImmutableSet.of(
                new ThrowTargetImpl<Parameters>(
                    "moj_import", "Iris shader programs may not use moj_import directives."),
                new ThrowTargetImpl<Parameters>(
                    "iris_",
                    "Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))));

    Transformation<Parameters> fixVersion = new Transformation<Parameters>(new RunPhase<Parameters>() {
      /**
       * This largely replicates the behavior of
       * {@link net.coderbot.iris.pipeline.newshader.TriforcePatcher#fixVersion(Transformations)}
       */
      @Override
      protected void run(TranslationUnitContext ctx) {
        VersionStatementContext versionStatement = ctx.versionStatement();
        if (versionStatement == null) {
          throw new IllegalStateException("Missing the version statement!");
        }

        String profile = Optional.ofNullable(versionStatement.NR_IDENTIFIER())
            .map(terminal -> terminal.getText())
            .orElse("");
        int version = Integer.parseInt(versionStatement.NR_INTCONSTANT().getText());

        if (profile.equals("core")) {
          throw new IllegalStateException("Transforming a shader that is already built against the core profile???");
        }

        if (version >= 200) {
          if (!profile.equals("compatibility")) {
            throw new IllegalStateException(
                "Expected \"compatibility\" after the GLSL version: #version " + version + " " + profile);
          }
        } else {
          version = 150;
        }
        profile = "core";

        replaceNode(versionStatement, "#version " + version + " " + profile + "\n", GLSLParser::versionStatement);
      }
    });

    /**
     * PREV NOTE:
     * This must be defined and valid in all shader passes, including composite
     * passes. A shader that relies on this behavior is SEUS v11 - it reads
     * gl_Fog.color and breaks if it is not properly defined.
     */
    Transformation<Parameters> wrapFogSetup = WrapIdentifier.fromTerminal(
        "gl_Fog", "iris_Fog",
        new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclarations(
                InjectionPoint.BEFORE_DECLARATIONS,
                "uniform float iris_FogDensity;",
                "uniform float iris_FogStart;",
                "uniform float iris_FogEnd;",
                "uniform vec4 iris_FogColor;",
                "struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };",
                "iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));");
          }
        });

    // PREV TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
    Transformation<Parameters> wrapFogFragCoord = WrapIdentifier.fromTerminal(
        "gl_FogFragCoord", "iris_FogFragCoord",
        new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            // PREV TODO: This doesn't handle geometry shaders... How do we do that?
            if (getJobParameters().type == ShaderType.VERTEX) {
              injectExternalDeclaration(
                  InjectionPoint.BEFORE_DECLARATIONS, "out float iris_FogFragCoord;");
            } else if (getJobParameters().type == ShaderType.FRAGMENT) {
              injectExternalDeclaration(
                  InjectionPoint.BEFORE_DECLARATIONS, "in float iris_FogFragCoord;");
            }
          }
        });

    /**
     * PREV TODO: This is incorrect and is just the bare minimum needed for SEUS v11
     * & Renewed to compile. It works because they don't actually use gl_FrontColor
     * even though they write to it.
     */
    Transformation<Parameters> wrapFrontColor = WrapIdentifier.fromTerminal(
        "gl_FrontColor", "iris_FrontColor",
        new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(
                InjectionPoint.BEFORE_DECLARATIONS, "vec4 iris_FrontColor;");
          };
        });

    // TOOD: this is not implemented yet for now as it's quite involved
    // fixes locations of outs in fragment shaders (to be called fixFragLayouts)
    // 1. find all the outs and determine their location
    // 2. check if there is a single non-located out that could receive location
    // 3. add location 0 to that declaration

    Transformation<Parameters> replaceStorageQualifierVertex = new Transformation<Parameters>(
        new SearchTerminalsImpl<Parameters>(SearchTerminals.ANY_TYPE) {
          {
            addReplacementTerminal("attribute", "in");
            addReplacementTerminal("varying", "out");
          }
        });
    Transformation<Parameters> replaceStorageQualifierFragment = new Transformation<Parameters>(
        new SearchTerminalsImpl<Parameters>(SearchTerminals.ANY_TYPE) {
          {
            addReplacementTerminal("varying", "in");
          }
        });

    // PREV TODO: Add similar functions for all legacy texture sampling functions
    Transformation<Parameters> injectTextureFunctions = new Transformation<Parameters>(new RunPhase<Parameters>() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclarations(
            InjectionPoint.BEFORE_DECLARATIONS,
            "vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }",
            "vec4 texture3D(sampler3D sampler, vec3 coord) { return texture(sampler, coord); }",
            "vec4 texture2DLod(sampler2D sampler, vec2 coord, float lod) { return textureLod(sampler, coord, lod); }",
            "vec4 texture3DLod(sampler3D sampler, vec3 coord, float lod) { return textureLod(sampler, coord, lod); }",
            "vec4 shadow2D(sampler2DShadow sampler, vec3 coord) { return vec4(texture(sampler, coord)); }",
            "vec4 shadow2DLod(sampler2DShadow sampler, vec3 coord, float lod) { return vec4(textureLod(sampler, coord, lod)); }",
            "vec4 texture2DGrad(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
            "vec4 texture2DGradARB(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
            "vec4 texture3DGrad(sampler3D sampler, vec3 coord, vec3 dPdx, vec3 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
            "vec4 texelFetch2D(sampler2D sampler, ivec2 coord, int lod) { return texelFetch(sampler, coord, lod); }",
            "vec4 texelFetch3D(sampler3D sampler, ivec3 coord, int lod) { return texelFetch(sampler, coord, lod); }");
      }
    });

    /**
     * PREV NOTE:
     * GLSL 1.50 Specification, Section 8.7:
     * In all functions below, the bias parameter is optional for fragment shaders.
     * The bias parameter is not accepted in a vertex or geometry shader.
     */
    Transformation<Parameters> injectTextureFunctionsFragment = new Transformation<Parameters>(
        new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclarations(
                InjectionPoint.BEFORE_DECLARATIONS,
                "vec4 texture2D(sampler2D sampler, vec2 coord, float bias) { return texture(sampler, coord, bias); }",
                "vec4 texture3D(sampler3D sampler, vec3 coord, float bias) { return texture(sampler, coord, bias); }");
          }
        });

    Transformation<Parameters> wrapFragColorOutput = new Transformation<Parameters>() {
      private FragColorOutput type;
      private boolean usesFragColor;
      private boolean usesFragData;
      private boolean usesCustomPossible;
      private boolean usesCustom;
      private String fragColorWrapResult;
      private String fragColorWrapTarget;

      // a list of the declared custom names
      private Collection<String> declaredCustomNames;

      // the single custom name that has been used in the code
      private String usedCustomName;

      @Override
      protected void resetState() {
        type = null;
        usesFragColor = false;
        usesFragData = false;
        usesCustomPossible = true;
        usesCustom = false;
      }

      {
        /**
         * 1. detect use of gl_FragColor, gl_FragData or custom color outputs
         * custom: find which color outputs write to location 0
         * syntax: out vec4 <IDENTIFIER>; combined with actual use
         * throw if there is more than one at the same location being used
         * (multiple declaration at the same location without multiple use is fine)
         * 1a. throw if more than one of these options is being used
         */

        // detect use of gl_FragColor
        addConcurrentPhase(new SearchTerminalsImpl<Parameters>(
            new HandlerTargetImpl<Parameters>("gl_FragColor") {
              @Override
              public void handleResult(TreeMember node, String match) {
                usesFragColor = true;
              }
            }));

        // detect use of gl_FragData
        addConcurrentPhase(new SearchTerminalsImpl<Parameters>(
            new HandlerTargetImpl<Parameters>("gl_FragData") {
              @Override
              public void handleResult(TreeMember node, String match) {
                usesFragData = true;
              }
            }) {

          // throw if there more than one of the two integrated methods is being used
          @Override
          protected void afterWalk(TranslationUnitContext ctx) {
            if (usesFragColor && usesFragData) {
              throw new SemanticException("gl_FragColor and gl_FragData can't be used at the same time!");
            }
          }
        });

        // detect use of custom color outputs like
        // "layout (location = 0) out vec4 <IDENTIFIER>"
        addPhase(new WalkPhase<Parameters>() {
          ParseTreePattern customColorOutPattern;
          ParseTreePattern illegalOutPattern;

          @Override
          protected void init() {
            customColorOutPattern = compilePattern(
                "layout (location = 0) out vec4 <name:IDENTIFIER>;",
                GLSLParser.RULE_externalDeclaration);
            illegalOutPattern = compilePattern(
                "out vec4 <name:IDENTIFIER>;",
                GLSLParser.RULE_externalDeclaration);
          }

          // if it hasn't thrown yet, using custom is still possible.
          // init the datastructures for custom name detection
          @Override
          protected void beforeWalk(TranslationUnitContext ctx) {
            declaredCustomNames = new ArrayList<>();
            usedCustomName = null;
          }

          // always run the before walk to update the possible variable
          @Override
          protected boolean isActiveBeforeWalk() {
            return true;
          }

          // only run the walk and after walk if using custom is possible at all
          @Override
          protected boolean isActive() {
            return usesCustomPossible;
          }

          // using custom names is not possible anymore if none are declared
          @Override
          protected void afterWalk(TranslationUnitContext ctx) {
            usesCustomPossible = !declaredCustomNames.isEmpty();
          }

          @Override
          public void enterExternalDeclaration(ExternalDeclarationContext ctx) {
            ParseTreeMatch illegalMatch = illegalOutPattern.match(ctx);
            if (illegalMatch.succeeded()) {
              throw new SemanticException("The declaration with the name '" + illegalMatch.get("name").getText()
                  + "' is missing a location specifier!");
            }

            ParseTreeMatch match = customColorOutPattern.match(ctx);
            if (match.succeeded()) {
              declaredCustomNames.add(match.get("name").getText());
            }
          }
        });

        // if we are doing custom, check if any of the declared names are being used
        // (and if they are being used multiple times)
        addPhase(new SearchTerminalsDynamic<Parameters>() {
          @Override
          protected Collection<HandlerTarget<Parameters>> getTargetsDynamic() {
            return declaredCustomNames.stream()
                .map(name -> new HandlerTargetImpl<Parameters>(name) {
                  @Override
                  public void handleResult(TreeMember node, String match) {
                    // name is being used, make sure it's the only one
                    if (usedCustomName == null) {
                      usedCustomName = name;
                    } else if (usedCustomName != name) {
                      // a second name is being used, this is illegal
                      throw new SemanticException(
                          "More than two custom color output names can't be used as the same time! '" + name + "' and '"
                              + usedCustomName + "' were used at the same time.");
                    }
                  }
                })
                .collect(Collectors.toList());
          }

          @Override
          protected boolean isActiveBeforeWalk() {
            return true;
          };

          // only run the walk if there are any names to find but run the after walk for
          // determining the final frag color type
          @Override
          protected boolean isActiveAtWalk() {
            return usesCustomPossible;
          }

          @Override
          protected boolean isActiveAfterWalk() {
            return true;
          }

          @Override
          protected void afterWalk(TranslationUnitContext ctx) {
            // check if any declared custom names were actually used
            usesCustom = usedCustomName != null;
            usesCustomPossible = usesCustom;

            // throw if it's being used together with one of the other two methods
            // we know that only one of the two can be true at this point
            if (usesCustom && (usesFragColor || usesFragData)) {
              throw new SemanticException("Custom color outputs can't be used at the same time as "
                  + (usesFragColor ? "gl_FragColor" : "gl_FragData") + "!");
            }

            // finally it's clear which one method is being used
            type = usesFragColor
                ? FragColorOutput.COLOR
                : usesFragData
                    ? FragColorOutput.DATA
                    : FragColorOutput.CUSTOM;

            fragColorWrapResult = type == FragColorOutput.COLOR ? "iris_FragColor" : "iris_FragData";
            fragColorWrapTarget = type == FragColorOutput.COLOR ? "gl_FragColor" : "gl_FragData";
          }
        });

        /**
         * 2. wrap their use: create a new output like
         * "out vec4 iris_FragColor/iris_FragData[8];"
         * (throw if the replacement target is present already)
         * 3. redirect gl_Frag* to the newly created output (replace identifiers)
         */
        append(new WrapIdentifierExternalDeclaration<Parameters>() {
          @Override
          protected String getInjectionContent() {
            return "out vec4 " + (type == FragColorOutput.COLOR ? "iris_FragColor" : "iris_FragData[8]") + ";";
          }

          @Override
          protected String getWrapResultDynamic() {
            return fragColorWrapResult;
          }

          @Override
          protected String getWrapTargetDynamic() {
            return fragColorWrapTarget;
          }

          @Override
          protected boolean isActiveDynamic() {
            return type == FragColorOutput.COLOR || type == FragColorOutput.DATA;
          }
        });

        /**
         * 4. if alpha test is given, apply it with iris_FragColor/iris_FragData[0].
         **/
        append(new WrapIdentifierExternalDeclaration<Parameters>() {
          @Override
          protected String getInjectionContent() {
            // inserts the alpha test, it is not null because it shouldn't be
            return "void main() { irisMain(); "
                + getJobParameters().getAlphaTest().toExpression(
                    type == FragColorOutput.COLOR ? "gl_FragColor.a" : "gl_FragData[0].a", "")
                + "}";
          }

          @Override
          protected InjectionPoint getInjectionLocation() {
            return InjectionPoint.BEFORE_EOF;
          }

          @Override
          protected String getWrapResultDynamic() {
            return "irisMain";
          }

          @Override
          protected String getWrapTargetDynamic() {
            return "main";
          }

          @Override
          protected boolean isActiveDynamic() {
            Patch patch = getJobParameters().patch;
            return (patch == Patch.VANILLA || patch == Patch.SODIUM) && getJobParameters().getAlphaTest() != null;
          }
        });
      }
    };

    Transformation<Parameters> wrapProjMatrixVanilla = WrapIdentifier.<Parameters>withExternalDeclaration(
        "gl_ProjectionMatrix", "iris_ProjMat", "iris_ProjMat",
        InjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMat;");

    Transformation<Parameters> replaceExcessMultiTexCoord = new Transformation<Parameters>(
        new SearchTerminalsImpl<Parameters>(
            new TerminalReplaceTargetImpl<>("gl_MultiTexCoord", "vec4(0.0, 0.0, 0.0, 1.0)")) {
          {
            allowInexactMatches();
          }
        });

    Transformation<Parameters> wrapAttributeInputsVanillaVertex = new Transformation<Parameters>() {
      boolean hasTex;
      boolean hasLight;
      boolean hasNormalAndIsNotNewLines;

      @Override
      protected void resetState() {
        ShaderAttributeInputs attributeInputs = ((VanillaParameters) getJobParameters()).inputs;
        hasTex = attributeInputs.hasTex();
        hasLight = attributeInputs.hasLight();
        hasNormalAndIsNotNewLines = attributeInputs.hasNormal() && !attributeInputs.isNewLines();
      }

      {
        addConcurrentPhase(new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_UV0")) {
          @Override
          protected boolean isActive() {
            return hasTex;
          }
        });
        addConcurrentPhase(new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_UV2")) {
          @Override
          protected boolean isActive() {
            return hasLight;
          }
        });

        addPhase(new SearchTerminalsImpl<Parameters>() {
          {
            addTarget(new ParsedReplaceTarget<Parameters>("gl_MultiTexCoord0") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                return hasTex ? "vec4(iris_UV0, 0.0, 1.0)" : "vec4(0.5, 0.5, 0.0, 1.0)";
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            });
            addTarget(new ParsedReplaceTarget<Parameters>("gl_MultiTexCoord1") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                return hasLight ? "vec4(iris_UV2, 0.0, 1.0)" : "vec4(240.0, 240.0, 0.0, 1.0)";
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            });
            addTarget(new ParsedReplaceTarget<Parameters>("gl_Normal") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                // the source is a little confusing
                return hasNormalAndIsNotNewLines ? "vec3(0.0, 0.0, 1.0)" : "iris_Normal";
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            });
          }
        });

        addConcurrentPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "in vec2 iris_UV0;");
          }

          @Override
          protected boolean isActive() {
            return hasTex;
          }
        });
        addConcurrentPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "in ivec2 iris_UV2;");
          }

          @Override
          protected boolean isActive() {
            return hasLight;
          }
        });
        addConcurrentPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Normal;");
          }

          @Override
          protected boolean isActive() {
            return hasNormalAndIsNotNewLines;
          }
        });

        append(replaceExcessMultiTexCoord);
      }
    };

    // TODO: in triforce this is confusing because iris_Color is used even when
    // !hasColor in which case it's not defined anywhere
    Transformation<Parameters> wrapColorVanilla = new Transformation<Parameters>() {
      boolean hasColor;

      @Override
      protected void resetState() {
        ShaderAttributeInputs attributeInputs = ((VanillaParameters) getJobParameters()).inputs;
        hasColor = attributeInputs.hasColor();
      }

      {
        addConcurrentPhase(
            new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_ColorModulator")));
        addConcurrentPhase(new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_Color")) {
          @Override
          protected boolean isActive() {
            return hasColor;
          }
        });

        addPhase(new SearchTerminalsImpl<Parameters>(new ParsedReplaceTarget<Parameters>("gl_Color") {
          @Override
          protected String getNewContent(TreeMember node, String match) {
            return hasColor ? "(iris_Color * iris_ColorModulator)" : "iris_ColorModulator";
          }

          @Override
          protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
            return GLSLParser::expression;
          }
        }));

        addConcurrentPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "uniform vec4 iris_ColorModulator;");
          }
        });
      }
    };

    // PREV TODO: More solid way to handle texture matrices
    // TODO: test if this actually works
    Transformation<Parameters> wrapTextureMatricesVanilla = new Transformation<Parameters>() {
      boolean replacementHappened;

      @Override
      protected void resetState() {
        replacementHappened = false;
      }

      {
        addConcurrentPhase(
            new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_TextureMat")));
        addConcurrentPhase(
            new SearchTerminalsImpl<Parameters>(new WrapThrowTargetImpl<Parameters>("iris_LightmapTextureMatrix")));

        addPhase(new WalkPhase<Parameters>() {
          ParseTreePattern pattern;

          @Override
          protected void init() {
            pattern = compilePattern("gl_TextureMatrix[<index:expression>]", GLSLParser.RULE_expression);
          }

          @Override
          public void enterExpression(ExpressionContext ctx) {
            ParseTreeMatch match = pattern.match(ctx);
            if (match.succeeded()) {
              int index = Integer.parseInt(match.get("index").getText());
              String replacement = null;
              if (index == 0) {
                replacement = "iris_TextureMat";
              }
              if (index == 1) {
                replacement = "iris_LightmapTextureMatrix";
              }
              if (replacement != null) {
                replaceNode(ctx, replacement, GLSLParser::expression);
                replacementHappened = true;
              }
            }
          }
        });

        addPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_TextureMat;");
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_LightmapTextureMatrix;");
          }

          @Override
          protected boolean isActive() {
            return replacementHappened;
          }
        });
      }
    };

    // PREV TODO: Should probably add the normal matrix as a proper uniform that's
    // computed on the CPU-side of things
    Transformation<Parameters> wrapModelViewMatrixVanilla = new Transformation<Parameters>() {
      String modelViewMatrixResult;
      ModelViewMatrixType type;

      enum ModelViewMatrixType {
        CHUNK_OFFSET, NEW_LINES, DEFAULT
      }

      {
        addPhase(new SearchTerminalsDynamic<Parameters>() {
          // determine the model view matrix result and what the mode is
          @Override
          protected void beforeWalk(TranslationUnitContext ctx) {
            VanillaParameters vanillaParameters = (VanillaParameters) getJobParameters();
            if (vanillaParameters.hasChunkOffset) {
              modelViewMatrixResult = "(iris_ModelViewMat * _iris_internal_translate(iris_ChunkOffset))";
              type = ModelViewMatrixType.CHUNK_OFFSET;
            } else if (vanillaParameters.inputs.isNewLines()) {
              modelViewMatrixResult = "(iris_VIEW_SCALE * iris_ModelViewMat)";
              type = ModelViewMatrixType.NEW_LINES;
            } else {
              modelViewMatrixResult = "iris_ModelViewMat";
              type = ModelViewMatrixType.DEFAULT;
            }
          }

          // detect pre-existing injection results
          @Override
          protected Collection<HandlerTarget<Parameters>> getTargetsDynamic() {
            Collection<HandlerTarget<Parameters>> targets = new ArrayList<>();
            targets.add(new WrapThrowTargetImpl<Parameters>("iris_ModelViewMat"));

            if (type == ModelViewMatrixType.CHUNK_OFFSET) {
              targets.add(new WrapThrowTargetImpl<Parameters>("iris_ChunkOffset"));
              targets.add(new WrapThrowTargetImpl<Parameters>("_iris_internal_translate"));
            } else if (type == ModelViewMatrixType.NEW_LINES) {
              targets.add(new WrapThrowTargetImpl<Parameters>("iris_VIEW_SCALE"));
              targets.add(new WrapThrowTargetImpl<Parameters>("iris_VIEW_SHRINK"));
            }

            return targets;
          }
        });

        // do variable replacements
        addPhase(new SearchTerminalsImpl<>(List.of(
            new ParsedReplaceTarget<>("gl_ModelViewMatrix") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                return modelViewMatrixResult;
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            },
            new ParsedReplaceTarget<>("gl_NormalMatrix") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                return "mat3(transpose(inverse(" + modelViewMatrixResult + ")))";
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            },
            new ParsedReplaceTarget<>("gl_ModelViewProjectionMatrix") {
              @Override
              protected String getNewContent(TreeMember node, String match) {
                return "(gl_ProjectionMatrix * " + modelViewMatrixResult + ")";
              }

              @Override
              protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
                return GLSLParser::expression;
              }
            })));

        // do fixed and conditional injections
        addPhase(new RunPhase<Parameters>() {
          @Override
          protected void run(TranslationUnitContext ctx) {
            injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMat;");

            if (type == ModelViewMatrixType.CHUNK_OFFSET) {
              injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "uniform vec3 iris_ChunkOffset;");
              injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS,
                  "mat4 _iris_internal_translate(vec3 offset) {\n" +
                      "    // NB: Column-major order\n" +
                      "    return mat4(1.0, 0.0, 0.0, 0.0,\n" +
                      "                0.0, 1.0, 0.0, 0.0,\n" +
                      "                0.0, 0.0, 1.0, 0.0,\n" +
                      "                offset.x, offset.y, offset.z, 1.0);\n" +
                      "}");
            } else if (type == ModelViewMatrixType.NEW_LINES) {
              injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS,
                  "const float iris_VIEW_SHRINK = 1.0 - (1.0 / 256.0);");
              injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS,
                  "const mat4 iris_VIEW_SCALE = mat4(\n" +
                      "    iris_VIEW_SHRINK, 0.0, 0.0, 0.0,\n" +
                      "    0.0, iris_VIEW_SHRINK, 0.0, 0.0,\n" +
                      "    0.0, 0.0, iris_VIEW_SHRINK, 0.0,\n" +
                      "    0.0, 0.0, 0.0, 1.0\n" +
                      ");");
            }
          }
        });
      }
    };

    Transformation<Parameters> replaceModelViewProjectionVanilla = new Transformation<Parameters>(
        SearchTerminalsImpl.withReplacementExpression(
            "gl_ModelViewProjectionMatrix",
            "(gl_ProjectionMatrix * gl_ModelViewMatrix)"));

    // TODO: continue with vanilla's vertex-specific block (the last one)

    // compose the transformations and phases into the managers
    for (Patch patch : Patch.values()) {
      for (ShaderType type : ShaderType.values()) {
        TransformationManager<Parameters> manager = new TransformationManager<Parameters>();
        managers.put(patch, type, manager);

        manager.setParseTokenFilter(parseTokenFilter);

        // patchCommon and alpha test
        manager.registerTransformation(detectReserved);
        manager.registerTransformation(fixVersion);
        manager.registerTransformation(wrapFogSetup);

        if (type == ShaderType.VERTEX || type == ShaderType.FRAGMENT) {
          manager.registerTransformation(wrapFogFragCoord);
        }

        if (type == ShaderType.VERTEX) {
          manager.registerTransformation(wrapFrontColor);
          manager.registerTransformation(replaceStorageQualifierVertex);
        } else if (type == ShaderType.FRAGMENT) {
          // manager.registerTransformation(fixFragLayouts);
          manager.registerTransformation(replaceStorageQualifierFragment);
          manager.registerTransformation(injectTextureFunctionsFragment);
        }

        manager.registerTransformation(injectTextureFunctions);

        // is part of patchCommon but also does the alpha test now (addAlphaTest)
        if (type == ShaderType.FRAGMENT) {
          manager.registerTransformation(wrapFragColorOutput);
        }

        // patchVanilla
        if (patch == Patch.VANILLA) {
          manager.registerTransformation(wrapProjMatrixVanilla);
          if (type == ShaderType.VERTEX) {
            manager.registerTransformation(wrapAttributeInputsVanillaVertex);
          }
          manager.registerTransformation(wrapColorVanilla);
          manager.registerTransformation(wrapTextureMatricesVanilla);
          manager.registerTransformation(wrapModelViewMatrixVanilla);
          manager.registerTransformation(replaceModelViewProjectionVanilla);
        }

        // patchSodium
        if (patch == Patch.SODIUM) {

        }

        // patchComposite
        if (patch == Patch.COMPOSITE) {

        }
      }
    }
  }

  private String transform(String source, Parameters parameters) {
    return managers.get(parameters.patch, parameters.type).transform(source, parameters);
  }

  @Override
  public String patchVanilla(
      String source, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
      ShaderAttributeInputs inputs) {
    // TODO: get rid of this by merging transformations from
    // AttributeShaderTransformer into the vanilla managers
    if (inputs.hasOverlay()) {
      StringTransformations preTransform = new StringTransformations(source);
      // PREV TODO: Change this once we implement 1.17 geometry shader support!
      AttributeShaderTransformer.patch(preTransform, type, false);
      source = source.toString();
    }

    return transform(source,
        new VanillaParameters(Patch.VANILLA, type, alpha, hasChunkOffset, inputs));
  }

  @Override
  public String patchSodium(
      String source, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
      float positionScale, float positionOffset, float textureScale) {
    return transform(source,
        new SodiumParameters(Patch.VANILLA, type, alpha, inputs, positionScale, positionOffset, textureScale));
  }

  @Override
  public String patchComposite(String source, ShaderType type) {
    return transform(source, new Parameters(Patch.COMPOSITE, type));
  }
}
