package net.coderbot.iris.gl.program;

import net.coderbot.iris.gl.GlResource;

import net.minecraft.client.gl.GlProgramManager;
import org.lwjgl.opengl.GL20C;

public final class Program extends GlResource {
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;

	Program(int program, ProgramUniforms uniforms, ProgramSamplers samplers) {
		super(program);

		this.uniforms = uniforms;
		this.samplers = samplers;
	}

	public void use() {
		GlProgramManager.useProgram(getGlId());

		uniforms.update();
		samplers.update();
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();
		GlProgramManager.useProgram(0);
	}

	public void destroyInternal() {
		GL20C.glDeleteProgram(getGlId());
	}

	/**
	 * @return the OpenGL ID of this program.
	 * @deprecated this should be encapsulated eventually
	 */
	@Deprecated
	public int getProgramId() {
		return getGlId();
	}
}
