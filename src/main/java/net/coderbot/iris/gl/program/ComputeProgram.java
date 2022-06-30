package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector3i;
import org.lwjgl.opengl.GL43C;

public final class ComputeProgram extends GlResource {
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private Vector3i absoluteWorkGroups;
	private Vector2f relativeWorkGroups;
	private int[] localSize;

	ComputeProgram(int program, ProgramUniforms uniforms, ProgramSamplers samplers, ProgramImages images) {
		super(program);

		localSize = new int[3];
		IrisRenderSystem.getProgramiv(program, GL43C.GL_COMPUTE_WORK_GROUP_SIZE, localSize);
		this.uniforms = uniforms;
		this.samplers = samplers;
		this.images = images;
	}

	public void setWorkGroupInfo(Vector2f relativeWorkGroups, Vector3i absoluteWorkGroups) {
		this.relativeWorkGroups = relativeWorkGroups;
		this.absoluteWorkGroups = absoluteWorkGroups;
	}

	public Vector3i getWorkGroups(float width, float height) {
		if (this.absoluteWorkGroups != null) {
			return this.absoluteWorkGroups;
		} else if (relativeWorkGroups != null) {
			return new Vector3i((int) (width * relativeWorkGroups.x) / localSize[0], (int) (height * relativeWorkGroups.y) / localSize[1], localSize[2]);
		} else {
			return new Vector3i((int) width / localSize[0], (int) height / localSize[1], localSize[2]);
		}
	}

	public void dispatch(float width, float height) {
		ProgramManager.glUseProgram(getGlId());
		uniforms.update();
		samplers.update();
		images.update();

		IrisRenderSystem.memoryBarrier(40);
		IrisRenderSystem.dispatchCompute(getWorkGroups(width, height));
		IrisRenderSystem.memoryBarrier(40);
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();
		ProgramManager.glUseProgram(0);
	}

	public void destroyInternal() {
		GlStateManager.glDeleteProgram(getGlId());
	}

	/**
	 * @return the OpenGL ID of this program.
	 * @deprecated this should be encapsulated eventually
	 */
	@Deprecated
	public int getProgramId() {
		return getGlId();
	}

	public int getActiveImages() {
		return images.getActiveImages();
	}
}
