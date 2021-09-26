package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.shaders.ProgramManager;
import net.coderbot.iris.gl.GlResource;
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
		ProgramManager.glUseProgram(getGlId());

		uniforms.update();
		samplers.update();
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();
		ProgramManager.glUseProgram(0);
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
