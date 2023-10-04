package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL43C;

public final class ComputeProgram extends GlResource {
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private Vector3i absoluteWorkGroups;
	private Vector2f relativeWorkGroups;
	private int[] localSize;
	private float cachedWidth;
	private float cachedHeight;
	private Vector3i cachedWorkGroups;

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
		if (cachedWidth != width || cachedHeight != height || cachedWorkGroups == null) {
			this.cachedWidth = width;
			this.cachedHeight = height;
			if (this.absoluteWorkGroups != null) {
				this.cachedWorkGroups = this.absoluteWorkGroups;
			} else if (relativeWorkGroups != null) {
				// Do not use actual localSize here, apparently that's not what we want.
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(Math.ceil((width * relativeWorkGroups.x)) / localSize[0]), (int) Math.ceil(Math.ceil((height * relativeWorkGroups.y)) / localSize[1]), 1);
			} else {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(width / localSize[0]), (int) Math.ceil(height / localSize[1]), 1);
			}
		}

		return cachedWorkGroups;
	}

	public void use() {
		ProgramManager.glUseProgram(getGlId());

		uniforms.update();
		samplers.update();
		images.update();
	}

	public void dispatch(float width, float height) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::allowConcurrentCompute).orElse(false)) {
			IrisRenderSystem.memoryBarrier(GL43C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43C.GL_TEXTURE_FETCH_BARRIER_BIT | GL43C.GL_SHADER_STORAGE_BARRIER_BIT);
		}

		IrisRenderSystem.dispatchCompute(getWorkGroups(width, height));
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
