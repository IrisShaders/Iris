package net.coderbot.iris.pipeline.newshader;

import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VersionStatementContext;
import io.github.douira.glsl_transformer.core.ReplaceTerminals;
import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.WrapIdentifier;
import io.github.douira.glsl_transformer.core.target.ThrowTarget;
import io.github.douira.glsl_transformer.print.filter.ChannelFilter;
import io.github.douira.glsl_transformer.print.filter.TokenFilter;
import io.github.douira.glsl_transformer.print.filter.TokenFilter.TokenChannel;
import io.github.douira.glsl_transformer.transform.RunPhase;
import io.github.douira.glsl_transformer.transform.SemanticException;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import io.github.douira.glsl_transformer.transform.TransformationPhase;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.Transformations;

/**
 * The transform patcher uses glsl-transformer to do shader transformation. It
 * delegates the things it doesn't do itself to TriforcePatcher by either not
 * overwriting methods or calling them itself.
 * 
 * NOTE: A separate TransformationManager should be created for each ShaderType.
 * That makes each of them more efficient as they don't need to run unnecessary
 * transformation phases.
 * 
 * TODO: good examples for more complex transformation in triforce patcher?
 * ideas: BuiltinUniformReplacementTransformer, defines/replacements with loops,
 * replacements that account for whitespace like the one for gl_TextureMatrix
 * 
 * TODO: how are defines handled? glsl-transformer can't deal with code that is
 * not valid GLSL code. Directives like #if will mess it up.
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

    TokenFilter parseTokenFilter = new ChannelFilter(TokenChannel.PREPROCESSOR) {
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
        new SearchTerminals<Parameters>(SearchTerminals.IDENTIFIER,
            ImmutableSet.of(
                ThrowTarget.fromMessage(
                    "moj_import", "Iris shader programs may not use moj_import directives."),
                ThrowTarget.fromMessage(
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

    // TODO: fragColor/fragData patching, see discord messages

    // compose the transformations and phases into the managers
    for (Patch patch : Patch.values()) {
      for (ShaderType type : ShaderType.values()) {
        TransformationManager<Parameters> manager = new TransformationManager<Parameters>();
        managers.put(patch, type, manager);

        manager.setParseTokenFilter(parseTokenFilter);

        manager.registerTransformation(detectReserved);
        manager.registerTransformation(fixVersion);
        manager.registerTransformation(wrapFogSetup);
        manager.registerTransformation(wrapFogFragCoord);

        if (type == ShaderType.VERTEX || type == ShaderType.FRAGMENT) {
          manager.registerTransformation(wrapFogFragCoord);
        }

        if (type == ShaderType.VERTEX) {
          manager.registerTransformation(wrapFrontColor);
        }

        manager.registerTransformation(injectTextureFunctions);
        if (type == ShaderType.FRAGMENT) {
          manager.registerTransformation(injectTextureFunctionsFragment);
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
