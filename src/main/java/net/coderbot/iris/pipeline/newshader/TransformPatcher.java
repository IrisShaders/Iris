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

  private static enum Patch {
    VANILLA, SODIUM, COMPOSITE
  }

  private static class VanillaParameters {
    public final AlphaTest alpha;
    public final boolean hasChunkOffset;
    public final ShaderAttributeInputs inputs;

    public VanillaParameters(AlphaTest alpha, boolean hasChunkOffset, ShaderAttributeInputs inputs) {
      this.alpha = alpha;
      this.hasChunkOffset = hasChunkOffset;
      this.inputs = inputs;
    }
  }

  private static class SodiumParameters {
    public final AlphaTest alpha;
    public final ShaderAttributeInputs inputs;
    public final float positionScale;
    public final float positionOffset;
    public final float textureScale;

    public SodiumParameters(AlphaTest alpha, ShaderAttributeInputs inputs, float positionScale,
        float positionOffset, float textureScale) {
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
  private Table<Patch, ShaderType, TransformationManager<Object>> managers = HashBasedTable.create(
      Patch.values().length,
      ShaderType.values().length);

  public TransformPatcher() {
    // PREV TODO: Only do the NewLines patches if the source code isn't from
    // gbuffers_lines

    // setup the transformations and even loose phases if necessary
    Transformation<Object> detectReserved = new Transformation<>(
        new SearchTerminals<>(SearchTerminals.IDENTIFIER,
            ImmutableSet.of(
                ThrowTarget.fromMessage(
                    "moj_import", "Iris shader programs may not use moj_import directives."),
                ThrowTarget.fromMessage(
                    "iris_",
                    "Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))));

    Transformation<Object> fixVersion = new Transformation<>(new RunPhase<Object>() {
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
    TransformationPhase<Object> sharedFogSetup = new RunPhase<Object>() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "uniform float iris_FogDensity;");
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "uniform float iris_FogStart;");
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "uniform float iris_FogEnd;");
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "uniform vec4 iris_FogColor;");
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS,
            "struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };");
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS,
            "iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));");
      }
    };

    // PREV TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
    // TODO: update to use new glsl-transformer features to make this compact
    SearchTerminals<Object> wrapFogFragCoord = new SearchTerminals<Object>() {
      {
        addReplacementTerminal("gl_FogFragCoord", "iris_FogFragCoord");
      }
    };

    // PREV TODO: This doesn't handle geometry shaders... How do we do that?
    TransformationPhase<Object> injectOutFogFragCoord = new RunPhase<Object>() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "out float iris_FogFragCoord;");
      };
    };

    TransformationPhase<Object> injectInFogFragCoord = new RunPhase<Object>() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "in float iris_FogFragCoord;");
      };
    };

    /**
     * PREV TODO: This is incorrect and is just the bare minimum needed for SEUS v11
     * & Renewed to compile. It works because they don't actually use gl_FrontColor
     * even though they write to it.
     */
    // TODO: use glsl-transformer core wrapping transformation for this
    TransformationPhase<Object> frontColorInjection = new RunPhase<Object>() {
      @Override
      protected void run(TranslationUnitContext ctx) {
        injectExternalDeclaration(
            InjectionPoint.BEFORE_DECLARATIONS, "vec4 iris_FrontColor;");
      };
    };

    // TODO use shorthand method
    SearchTerminals<Object> wrapFrontColor = new SearchTerminals<Object>() {
      {
        addReplacementTerminal("gl_FrontColor", "iris_FrontColor");
      }
    };

    // compose the transformations and phases into the managers
    for (Patch patch : Patch.values()) {
      for (ShaderType type : ShaderType.values()) {
        TransformationManager<Object> manager = new TransformationManager<>();
        managers.put(patch, type, manager);

        manager.registerTransformation(detectReserved);
        manager.registerTransformation(fixVersion);

        Transformation<Object> commonInjections = new Transformation<>();
        manager.registerTransformation(commonInjections);

        // TODO: use addConcurrentPhase to make this more compact
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

        // TODO: the rest of the patchCommon things
      }
    }
  }

  @Override
  public String patchVanilla(
      String source, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
      ShaderAttributeInputs inputs) {
    return managers.get(Patch.VANILLA, type)
        .transform(source, new VanillaParameters(alpha, hasChunkOffset, inputs));
  }

  @Override
  public String patchSodium(
      String source, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
      float positionScale, float positionOffset, float textureScale) {
    return managers.get(Patch.VANILLA, type)
        .transform(source, new SodiumParameters(alpha, inputs, positionScale, positionOffset, textureScale));
  }

  @Override
  public String patchComposite(String source, ShaderType type) {
    return managers.get(Patch.VANILLA, type).transform(source, null);
  }
}
