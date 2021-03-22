package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.shader.GlShader;
import net.coderbot.iris.gl.shader.ProgramCreator;
import net.coderbot.iris.gl.shader.ShaderConstants;
import net.coderbot.iris.gl.shader.ShaderType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
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

	public static ProgramBuilder begin(String name, @Nullable String vertexSource, @Nullable String fragmentSource) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader vertex;
		GlShader fragment;

		try {
			vertex = new GlShader(ShaderType.VERTEX, name + ".vsh", vertexSource, EMPTY_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile vertex shader for program " + name, e);
		}
	if(geometrySource != null) {
		try {
<<<<<<< Updated upstream
=======
			geometry = new GlShader(ShaderType.GEOMETRY, name + ".gsh", geometrySource, EMPTY_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile geometry shader for program " + name, e);
		}
	}	else {
		geometry = null;
	}

		try {
>>>>>>> Stashed changes
			fragment = new GlShader(ShaderType.FRAGMENT, name + ".fsh", fragmentSource, EMPTY_CONSTANTS);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to compile fragment shader for program " + name, e);
		}

		int programId = ProgramCreator.create(name, vertex, fragment);

		vertex.destroy();
<<<<<<< Updated upstream
=======
		if (geometry != null) geometry.destroy();
>>>>>>> Stashed changes
		fragment.destroy();

		return new ProgramBuilder(name, programId);
	}

	public Program build() {
		return new Program(program, super.buildUniforms());
	}
}
