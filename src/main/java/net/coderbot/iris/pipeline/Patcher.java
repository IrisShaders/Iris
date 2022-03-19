package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public interface Patcher {
  // static Patcher INSTANCE = new TriforcePatcher();
  static Patcher INSTANCE = new TransformPatcher();

  public static Patcher getInstance() {
    // create an instance only when needed or statically like this?
    return INSTANCE;
  }

  public String patchAttributes(String source, ShaderType type, boolean hasGeometry);

  public String patchVanilla(
      String source, ShaderType type, AlphaTest alpha,
      boolean hasChunkOffset, ShaderAttributeInputs inputs);

  public String patchSodium(String source, ShaderType type, AlphaTest alpha,
      ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale);

  public String patchComposite(String source, ShaderType type);
}
