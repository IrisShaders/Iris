package net.irisshaders.iris.opengl;

import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

final class GlComputePipeline implements ComputePipeline {
	private final GlShaderBindings shader;
	private final ProgramResources resources;
	private final BufferUniformHolder uniforms;
	private final Vector3i localSize;
	private final int[] limits = new int[3];
	private boolean closed;

	GlComputePipeline(GlShaderBindings shader, ProgramResources resources, BufferUniformHolder uniforms) {
		this.shader = shader.retain();
		this.resources = resources;
		this.uniforms = uniforms;
		try {
			uniforms.resolveLayout(this.shader.source.layout());

			try (var stack = MemoryStack.stackPush()) {
				var size = stack.mallocInt(3);
				GL20C.glGetProgramiv(this.shader.program, GL43C.GL_COMPUTE_WORK_GROUP_SIZE, size);
				this.localSize = new Vector3i(size.get(0), size.get(1), size.get(2));
			}

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
		return new Vector3i(this.localSize);
	}

	public void dispatch(Vector3i groups, FilledIndirectPointer indirect) {
		for (int i = 0; i < 3; i++) {
			if (groups.get(i) < 0 || groups.get(i) > this.limits[i]) {
				throw new IllegalArgumentException("invalid compute dispatch: " + groups);
			}
		}

		IrisRenderSystem.memoryBarrier(GL42C.GL_ALL_BARRIER_BITS);
		var resolved = this.resources.resolve();
		resolved.buffers().putAll(this.shader.uploadUniforms(this.uniforms));
		this.shader.bind(this.resources, this.uniforms, resolved);

		if (indirect == null) {
			IrisRenderSystem.dispatchCompute(groups.x, groups.y, groups.z);
		} else {
			GlStateManager._glBindBuffer(GL43C.GL_DISPATCH_INDIRECT_BUFFER, ((GlBuffer) indirect.buffer()).handle());
			IrisRenderSystem.dispatchComputeIndirect(indirect.offset());
		}

		IrisRenderSystem.memoryBarrier(GL42C.GL_ALL_BARRIER_BITS);
	}

	@Override
	public void close() {
		if (!this.closed) {
			this.closed = true;
			this.shader.close();
		}
	}
}
