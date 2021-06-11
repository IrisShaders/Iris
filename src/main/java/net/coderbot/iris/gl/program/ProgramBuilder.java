package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.shader.GlShader;
import net.coderbot.iris.gl.shader.ProgramCreator;
import net.coderbot.iris.gl.shader.ShaderConstants;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.gl.shader.StandardMacros;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;

public class ProgramBuilder extends ProgramUniforms.Builder {
	private static final ShaderConstants EMPTY_CONSTANTS = ShaderConstants.builder().build();

	public static final ShaderConstants MACRO_CONSTANTS = ShaderConstants.builder()
		.define(StandardMacros.getOsString())
		.define("MC_VERSION", StandardMacros.getMcVersion())
		.define("MC_GL_VERSION", StandardMacros.getGlVersion(GL20C.GL_VERSION))
		.define("MC_GLSL_VERSION", StandardMacros.getGlVersion(GL20C.GL_SHADING_LANGUAGE_VERSION))
		.define(StandardMacros.getRenderer())
		.define(StandardMacros.getVendor())
		.defineAll(StandardMacros.getGlExtensions())
		.build();



	private final int program;

	private ProgramBuilder(String name, int program) {
		super(name, program);

		this.program = program;
	}

	public void bindAttributeLocation(int index, String name) {
		GL21C.glBindAttribLocation(program, index, name);
	}

	public static ProgramBuilder begin(String name, @Nullable String vertexSource, @Nullable String geometrySource, @Nullable String fragmentSource) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader vertex;
		GlShader geometry;
		GlShader fragment;

		vertex = buildShader(ShaderType.VERTEX, name + ".vsh", vertexSource);

		if (geometrySource != null) {
			geometry = buildShader(ShaderType.GEOMETRY, name + ".gsh", geometrySource);
		} else {
			geometry = null;
		}

		fragment = buildShader(ShaderType.FRAGMENT, name + ".fsh", fragmentSource);

		int programId;
		
		if (geometry != null) {
			programId = ProgramCreator.create(name, vertex, geometry, fragment);
		} else {
			programId = ProgramCreator.create(name, vertex, fragment);
		}

		vertex.destroy();

		if (geometry != null) {
			geometry.destroy();
		}

		fragment.destroy();

		return new ProgramBuilder(name, programId);
	}

	public Program build() {
		return new Program(program, super.buildUniforms());
	}

	private static GlShader buildShader(ShaderType shaderType, String name, @Nullable String source) {
		try {
			return new GlShader(shaderType, name, source, MACRO_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile " + shaderType + " shader for program " + name, e);
		}
	}
}
