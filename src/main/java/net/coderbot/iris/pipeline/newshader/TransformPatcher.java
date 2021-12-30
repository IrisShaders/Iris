package net.coderbot.iris.pipeline.newshader;

import java.util.Optional;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VersionStatementContext;
import io.github.douira.glsl_transformer.transform.RunPhase;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.Transformations;

/**
 * The transform patcher uses glsl-transformer to do shader transformation. It
 * delegates the things it doesn't do itself to TriforcePatcher by either not
 * overwriting methods or calling them itself.
 */
public class TransformPatcher extends TriforcePatcher {
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

        replaceNode(versionStatement, "#version " + version + " " + profile, GLSLParser::versionStatement);
      }
    });

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
    System.out.println(input);
    input = commonManager.transform(input);
    System.out.println(input);

    return super.patchCommon(input, type);
  }

  @Override
  void fixVersion(Transformations transformations) {
    // do nothing because this patcher does itself
  }
}
