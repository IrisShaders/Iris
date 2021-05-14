package net.coderbot.iris.gl.program;

import net.coderbot.iris.gl.GlResource;

import org.lwjgl.opengl.GL20C;

public final class Program extends GlResource {
	private final ProgramUniforms uniforms;

	Program(int program, ProgramUniforms uniforms) {
		super(program);

		this.uniforms = uniforms;
	}

	public void use() {
		GL20C.glUseProgram(getGlId());

		uniforms.update();
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
