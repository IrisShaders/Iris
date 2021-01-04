package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;

public final class Program extends GlResource {
	private final ProgramUniforms uniforms;

	Program(GlProgram glProgram, ProgramUniforms uniforms) {
		super(glProgram.getProgramRef());

		this.uniforms = uniforms;
	}

	public void use() {
		GlProgramManager.useProgram(getGlId());

		uniforms.update();
	}

	public void destroyInternal() {
		GlStateManager.deleteProgram(getGlId());
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
