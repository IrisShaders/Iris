package net.coderbot.iris.uniforms.builtin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BuiltinReplacementUniforms {
	private static final Matrix4f lightmapTextureMatrix;

	static {
		// This mimics the transformations done in LightmapTextureManager to the GL_TEXTURE matrix.
		lightmapTextureMatrix = new Matrix4f();
		lightmapTextureMatrix.loadIdentity();
		lightmapTextureMatrix.multiply(0.00390625f);
		lightmapTextureMatrix.multiply(Matrix4f.translate(8.0f, 8.0f, 8.0f));
	}

	public static void addBuiltinReplacementUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.ONCE, "iris_LightmapTextureMatrix", () -> {
			Iris.logger.warn("A shader appears to require the lightmap texture matrix even after transformations have occurred");
			Iris.logger.warn("Iris handles this correctly but it indicates that the shader is doing weird things with lightmap coordinates");

			return lightmapTextureMatrix;
		});
	}
}
