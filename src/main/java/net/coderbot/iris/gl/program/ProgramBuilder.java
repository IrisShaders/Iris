package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.shader.GlShader;
import net.coderbot.iris.gl.shader.ProgramCreator;
import net.coderbot.iris.gl.shader.ShaderConstants;
import net.coderbot.iris.gl.shader.ShaderType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL21C;

public class ProgramBuilder extends ProgramUniforms.Builder {
	private static final ShaderConstants EMPTY_CONSTANTS = ShaderConstants.builder().build();

	private final int program;

	private ProgramBuilder(String name, int program) {
		super(name, program);

		this.program = program;
	}

	public void bindAttributeLocation(int index, String name) {
		GL21C.glBindAttribLocation(program, index, name);
	}

	public static ProgramBuilder begin(String name, @Nullable computeSource, @Nullable String vertexSource, @Nullable String geometrySource, @Nullable String fragmentSource) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader compute;
		GlShader vertex;
		GlShader geometry;
		GlShader fragment;

		if (computeSource != null) {
			try {
				compute = new GlShader(ShaderType.COMPUTE, name + ".csh", computeSource, EMPTY_CONSTANTS);
			} catch (RuntimeException e) {
				throw new RuntimeException("Failed to compile geometry shader for program " + name, e);
			}
		} else {
			compute = null;
		}

		try {
			vertex = new GlShader(ShaderType.VERTEX, name + ".vsh", vertexSource, EMPTY_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile vertex shader for program " + name, e);
		}

		if (geometrySource != null) {
			try {
				geometry = new GlShader(ShaderType.GEOMETRY, name + ".gsh", geometrySource, EMPTY_CONSTANTS);
			} catch (RuntimeException e) {
				throw new RuntimeException("Failed to compile geometry shader for program " + name, e);
			}
		} else {
			geometry = null;
		}

		try {
			fragment = new GlShader(ShaderType.FRAGMENT, name + ".fsh", fragmentSource, EMPTY_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile fragment shader for program " + name, e);
		}

		int programId;
		
		if (compute != null) {
			programId = ProgramCreator.create(name, compute, vertex, fragment);
		} else if (geometry != null) {
			programId = ProgramCreator.create(name, vertex, geometry, fragment);
		} else if (compute != null && geometry != null) {
			programId = ProgramCreator.create(name, compute, vertex, geometry, fragment);
		} else {
			programId = ProgramCreator.create(name, vertex, fragment);
		}

		if (compute != null) {
			compute.destroy();
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
}
