package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformType;

public class ExternallyManagedUniforms {
	private ExternallyManagedUniforms() {
		// no construction allowed
	}

	public static void addExternallyManagedUniforms(UniformHolder uniformHolder) {
		addMat4(uniformHolder, "u_ModelViewMatrix");
		addMat4(uniformHolder, "u_ModelViewProjectionMatrix");
		addMat4(uniformHolder, "u_NormalMatrix");
	}

	public static void addExternallyManagedUniforms116(UniformHolder uniformHolder) {
		addExternallyManagedUniforms(uniformHolder);

		uniformHolder.externallyManagedUniform("u_ModelScale", UniformType.VEC3);
		uniformHolder.externallyManagedUniform("u_TextureScale", UniformType.VEC2);
	}

	public static void addExternallyManagedUniforms117(UniformHolder uniformHolder) {
		addExternallyManagedUniforms(uniformHolder);

		// Sodium
		addFloat(uniformHolder, "u_FogStart");
		addFloat(uniformHolder, "u_FogEnd");
		addVec4(uniformHolder, "u_FogColor");
		addMat4(uniformHolder, "u_ProjectionMatrix");
		addFloat(uniformHolder, "u_TextureScale");
		addFloat(uniformHolder, "u_ModelScale");
		addFloat(uniformHolder, "u_ModelOffset");
		uniformHolder.externallyManagedUniform("u_CameraTranslation", UniformType.VEC3);

		// Vanilla
		uniformHolder.externallyManagedUniform("iris_TextureMat", UniformType.MAT4);
		uniformHolder.externallyManagedUniform("iris_ModelViewMat", UniformType.MAT4);
		uniformHolder.externallyManagedUniform("iris_ProjMat", UniformType.MAT4);
		uniformHolder.externallyManagedUniform("iris_ChunkOffset", UniformType.VEC3);
		uniformHolder.externallyManagedUniform("iris_ColorModulator", UniformType.VEC4);
		uniformHolder.externallyManagedUniform("iris_FogStart", UniformType.FLOAT);
		uniformHolder.externallyManagedUniform("iris_FogEnd", UniformType.FLOAT);
		uniformHolder.externallyManagedUniform("iris_FogDensity", UniformType.FLOAT);
		uniformHolder.externallyManagedUniform("iris_LineWidth", UniformType.FLOAT);
		uniformHolder.externallyManagedUniform("iris_ScreenSize", UniformType.VEC2);
		uniformHolder.externallyManagedUniform("iris_FogColor", UniformType.VEC4);
	}

	private static void addMat4(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.MAT4);
	}

	private static void addVec4(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.VEC4);
	}

	private static void addFloat(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.FLOAT);
	}
}
