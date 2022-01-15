package net.coderbot.iris.pipeline.newshader;

import java.util.Optional;

import com.google.common.collect.ImmutableSet;

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
import io.github.douira.glsl_transformer.transform.WalkPhase;
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
public class TransformPatcher extends TriforcePatcher {
  private static final Logger LOGGER = LogManager.getLogger(TransformPatcher.class);

  /*
   * if glsl-transformer is used for everything, then there is no common, just
   * three managers that share some but not all transformations
   * private static TransformationManager vanillaManager = new
   * TransformationManager();
   * private static TransformationManager sodiumManager = new
   * TransformationManager();
   * private static TransformationManager compositeManager = new
   * TransformationManager();
   */
  private static TransformationManager commonManager = new TransformationManager();

  static {
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

    

    commonManager.registerTransformation(detectReserved);
    commonManager.registerTransformation(fixVersion);
  }

  @Override
  public String patchVanilla(
      String source, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
      ShaderAttributeInputs inputs) {
    return super.patchVanilla(source, type, alpha, hasChunkOffset, inputs);
  }

  @Override
  public String patchSodium(
      String source, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
      float positionScale, float positionOffset, float textureScale) {
    return super.patchSodium(
        source, type, alpha, inputs, positionScale, positionOffset, textureScale);
  }

  @Override
  public String patchComposite(String source, ShaderType type) {
    return super.patchComposite(source, type);
  }

  @Override
  String patchCommon(String input, ShaderType type) {
    // int index = input.indexOf("#version");
    // LOGGER.debug(input.substring(index, index + 100).trim());
    input = commonManager.transform(input);
    // index = input.indexOf("#version");
    // LOGGER.debug(input.substring(index, index + 100).trim());

    return super.patchCommon(input, type);
  }

  @Override
  void fixVersion(Transformations transformations) {
    // do nothing because this patcher does itself
    // super.fixVersion(transformations);
  }
}
