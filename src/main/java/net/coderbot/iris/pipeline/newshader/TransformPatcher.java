package net.coderbot.iris.pipeline.newshader;

import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VersionStatementContext;
import io.github.douira.glsl_transformer.core.ReplaceTerminals;
import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.target.ThrowTarget;
import io.github.douira.glsl_transformer.transform.RunPhase;
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
 * The only directives that glsl-transformer will see are #defines which are ok.
 * 
 * TODO: good examples for more complex transformation in triforce patcher?
 * ideas: BuiltinUniformReplacementTransformer, defines/replacements with loops,
 * replacements that account for whitespace like the one for gl_TextureMatrix
 */
public class TransformPatcher implements Patcher {
  private static final Logger LOGGER = LogManager.getLogger(TransformPatcher.class);

  private Table<Patch, ShaderType, TransformationManager> managers = HashBasedTable.create(
      Patch.values().length,
      ShaderType.values().length);

  private static enum Patch {
    VANILLA, SODIUM, COMPOSITE
  }

  public TransformPatcher() {
    // PREV TODO: Only do the NewLines patches if the source code isn't from
    // gbuffers_lines

    // setup the transformations and even loose phases if necessary
    Transformation detectReserved = new Transformation(
        new SearchTerminals(SearchTerminals.IDENTIFIER,
            ImmutableSet.of(
                ThrowTarget.fromMessage(
                    "moj_import", "Iris shader programs may not use moj_import directives."),
                ThrowTarget.fromMessage(
                    "iris_",
                    "Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))));

    Transformation fixVersion = new Transformation(new RunPhase() {
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
    TransformationPhase sharedFogSetup = new RunPhase() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            "uniform float iris_FogDensity;", InjectionPoint.BEFORE_DECLARATIONS);
        injectExternalDeclaration(
            "uniform float iris_FogStart;", InjectionPoint.BEFORE_DECLARATIONS);
        injectExternalDeclaration(
            "uniform float iris_FogEnd;", InjectionPoint.BEFORE_DECLARATIONS);
        injectExternalDeclaration(
            "uniform vec4 iris_FogColor;", InjectionPoint.BEFORE_DECLARATIONS);
        injectExternalDeclaration(
            "struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };",
            InjectionPoint.BEFORE_DECLARATIONS);
        injectExternalDeclaration(
            "iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));",
            InjectionPoint.BEFORE_DECLARATIONS);
      }
    };

    // PREV TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
    // TODO: update to use new glsl-transformer features to make this compact
    ReplaceTerminals wrapFogFragCoord = new ReplaceTerminals(ReplaceTerminals.IDENTIFIER);
    wrapFogFragCoord.addReplacementTerminal("gl_FogFragCoord", "iris_FogFragCoord");

    // PREV TODO: This doesn't handle geometry shaders... How do we do that?
    TransformationPhase injectOutFogFragCoord = new RunPhase() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            "out float iris_FogFragCoord;", InjectionPoint.BEFORE_DECLARATIONS);
      };
    };

    TransformationPhase injectInFogFragCoord = new RunPhase() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            "in float iris_FogFragCoord;", InjectionPoint.BEFORE_DECLARATIONS);
      };
    };

    /**
     * PREV TODO: This is incorrect and is just the bare minimum needed for SEUS v11
     * & Renewed to compile. It works because they don't actually use gl_FrontColor
     * even though they write to it.
     */
    // TODO: use glsl-transformer core wrapping transformation for this
    TransformationPhase frontColorInjection = new RunPhase() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            "vec4 iris_FrontColor;", InjectionPoint.BEFORE_DECLARATIONS);
      };
    };
    ReplaceTerminals wrapFrontColor = new ReplaceTerminals(ReplaceTerminals.IDENTIFIER);
    wrapFogFragCoord.addReplacementTerminal("gl_FrontColor", "iris_FrontColor");

    // compose the transformations and phases into the managers
    for (Patch patch : Patch.values()) {
      for (ShaderType type : ShaderType.values()) {
        TransformationManager manager = new TransformationManager();
        managers.put(patch, type, manager);

        manager.registerTransformation(detectReserved);
        manager.registerTransformation(fixVersion);

        Transformation commonInjections = new Transformation();
        manager.registerTransformation(commonInjections);

        //TODO: use addConcurrentPhase to make this more compact
        commonInjections.addPhase(0, sharedFogSetup);
        commonInjections.addPhase(0, wrapFogFragCoord);
        if (type == ShaderType.VERTEX) {
          commonInjections.addPhase(0, injectInFogFragCoord);
        } else if (type == ShaderType.FRAGMENT) {
          commonInjections.addPhase(0, injectOutFogFragCoord);
        }
        if (type == ShaderType.VERTEX) {
          commonInjections.addPhase(0, frontColorInjection);
          commonInjections.addPhase(0, wrapFrontColor);
        }
        
        //TODO: the rest of the patchCommon things
      }
    }
  }

  @Override
  public String patchVanilla(
      String source, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
      ShaderAttributeInputs inputs) {
    return managers.get(Patch.VANILLA, type).transform(source);
  }

  @Override
  public String patchSodium(
      String source, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
      float positionScale, float positionOffset, float textureScale) {
    return null;
  }

  @Override
  public String patchComposite(String source, ShaderType type) {
    return null;
  }
}
