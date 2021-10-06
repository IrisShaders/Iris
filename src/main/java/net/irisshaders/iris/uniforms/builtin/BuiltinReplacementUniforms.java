package net.irisshaders.iris.uniforms.builtin;

import com.mojang.math.Matrix4f;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BuiltinReplacementUniforms {
	private static final Matrix4f lightmapTextureMatrix;

	static {
		// This mimics the transformations done in LightmapTextureManager to the GL_TEXTURE matrix.
		lightmapTextureMatrix = new Matrix4f();
		lightmapTextureMatrix.setIdentity();
		lightmapTextureMatrix.multiply(0.00390625f);
		lightmapTextureMatrix.multiply(Matrix4f.createTranslateMatrix(8.0f, 8.0f, 8.0f));
	}

	public static void addBuiltinReplacementUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.ONCE, "iris_LightmapTextureMatrix", () -> {
			Iris.logger.warn("A shader appears to require the lightmap texture matrix even after transformations have occurred");
			Iris.logger.warn("Iris handles this correctly but it indicates that the shader is doing weird things with lightmap coordinates");

			return lightmapTextureMatrix;
		});
	}
}
