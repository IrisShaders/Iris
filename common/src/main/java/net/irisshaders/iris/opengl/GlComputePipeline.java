package net.irisshaders.iris.opengl;

import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

public class GlComputePipeline implements ComputePipeline {
	private final GlShaderBindings shader;
	private final ProgramSamplers resources;
	private final DrawUniforms uniforms;
	private Vector3i localSize;
	private final int[] limits = new int[3];
	private boolean closed;

	public GlComputePipeline(GlShaderBindings shader, ProgramSamplers resources, DrawUniforms uniforms) {
		this.shader = shader.retain();
		this.resources = resources;
		this.uniforms = uniforms;
		try {
			uniforms.resolveLayout(this.shader.source.layout());

			for (int i = 0; i < 3; i++) {
				this.limits[i] = GL30C.glGetIntegeri(GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT, i);
			}
		} catch (RuntimeException | Error e) {
			this.shader.close();
			throw e;
		}
	}

	@Override
	public Vector3i localSize() {
		if (this.localSize == null) {
			this.shader.finishCompilation();
			try (var stack = MemoryStack.stackPush()) {
				var size = stack.mallocInt(3);
				GL20C.glGetProgramiv(this.shader.program, GL43C.GL_COMPUTE_WORK_GROUP_SIZE, size);
				this.localSize = new Vector3i(size.get(0), size.get(1), size.get(2));
			}
		}
		return new Vector3i(this.localSize);
	}

	@Override
	public void dispatch(Vector3i groups, FilledIndirectPointer indirect) {
		for (int i = 0; i < 3; i++) {
			if (groups.get(i) < 0 || groups.get(i) > this.limits[i]) {
				throw new IllegalArgumentException("invalid compute dispatch: " + groups);
			}
		}

		this.shader.bind(this.resources, this.uniforms);
		int barriers = 0;
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::allowConcurrentCompute).orElse(false)) {
			barriers |= GL43C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43C.GL_TEXTURE_FETCH_BARRIER_BIT | GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
		}
		if (indirect != null) {
			barriers |= GL43C.GL_COMMAND_BARRIER_BIT;
		}
		if (barriers != 0) {
			IrisRenderSystem.memoryBarrier(barriers);
		}

		if (indirect == null) {
			IrisRenderSystem.dispatchCompute(groups.x, groups.y, groups.z);
		} else {
			GlStateManager._glBindBuffer(GL43C.GL_DISPATCH_INDIRECT_BUFFER, ((GlBuffer) indirect.buffer()).handle());
			IrisRenderSystem.dispatchComputeIndirect(indirect.offset());
		}
	}

	@Override
	public void close() {
		if (!this.closed) {
			this.closed = true;
			this.shader.close();
		}
	}
}
