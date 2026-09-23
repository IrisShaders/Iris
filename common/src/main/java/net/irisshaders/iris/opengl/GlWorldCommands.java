package net.irisshaders.iris.opengl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.backend.ForwardingRenderPass;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.ARBBaseInstance;
import org.lwjgl.opengl.ARBTextureBufferRange;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GlWorldCommands extends ForwardingRenderPass implements WorldPassCommands {
	private final CommandEncoderBackend encoder;
	private final boolean owned;
	private final GpuBufferSlice[] vertices = new GpuBufferSlice[16];
	private final Map<TexelBufferKey, TexelBufferView> texels = new HashMap<>();
	private final RenderArea area;
	private boolean closed;
	private GpuBuffer indices;
	private IndexType indexType = IndexType.INT;
	private GlGraphicsShader.Pipeline pipeline;
	private boolean storageWrites;

	public GlWorldCommands(CommandEncoderBackend encoder, RenderPassDescriptor descriptor) {
		this(encoder, encoder.createRenderPass(descriptor), descriptor.renderArea(), true);
	}

	public GlWorldCommands(CommandEncoderBackend encoder, RenderPassBackend pass, RenderArea area, boolean owned) {
		super(pass);
		this.encoder = encoder;
		this.owned = owned;
		this.area = area;
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
		GlStateManager._viewport(x, y, width, height);
	}

	@Override
	public void pushConstants(ByteBuffer data) {
		if (this.pipeline == null) {
			throw new IllegalStateException("no graphics pipeline bound");
		}

		this.pipeline.pushConstants(this.upload(data, RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment()));
	}

	@Override
	public void close() {
		if (this.owned && !this.closed) {
			this.closed = true;
			this.encoder.submitRenderPass();
			this.end();
		}
	}

	@Override
	public void setPipeline(BackendRenderPipeline pipeline) {
	}

	@Override
	public void setUniform(int index, Object value) {
	}

	@Override
	public void setVertexBuffer(int slot, GpuBufferSlice slice) {
		this.vertices[slot] = slice;
	}

	@Override
	public void setIndexBuffer(GpuBuffer buffer, IndexType type) {
		this.indices = buffer;
		this.indexType = type;
	}

	@Override
	public void enableScissor(int x, int y, int w, int h) {
		GlStateManager._enableScissorTest();
		GlStateManager._scissorBox(x, y, w, h);
	}

	@Override
	public void disableScissor() {
		this.enableScissor(this.area.x(), this.area.y(), this.area.width(), this.area.height());
	}

	private GpuBufferSlice upload(ByteBuffer data, int alignment) {
		return this.encoder.transientMemory().uploadGpu(data, alignment, GpuBuffer.USAGE_UNIFORM);
	}

	@Override
	public void bindPipeline(GraphicsPipeline pipeline) {
		this.pipeline = (GlGraphicsShader.Pipeline) pipeline;
		this.storageWrites = this.pipeline.hasStorageWrites();
		this.pipeline.bind();
	}

	@Override
	public void setAttachments(List<GpuTextureView> colors, GpuTextureView depth, RenderArea area) {
		int framebuffer = GlRenderBackend.getFramebuffer(colors, depth);
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
		int[] buffers = new int[colors.size()];

		for (int i = 0; i < buffers.length; i++) {
			buffers[i] = GL30C.GL_COLOR_ATTACHMENT0 + i;
		}

		IrisRenderSystem.drawBuffers(framebuffer, buffers);
	}

	@Override
	public long getTexelBufferView(GpuBufferSlice slice, GpuFormat format) {
		TexelBufferKey key = new TexelBufferKey(((GlBuffer) slice.buffer()).handle(), slice.offset(), slice.length(), format);

		return this.texels.computeIfAbsent(key, ignored -> {
			int texture = GlStateManager._genTexture();
			GL11C.glBindTexture(GL31C.GL_TEXTURE_BUFFER, texture);
			GpuBuffer copy = null;
			int internalFormat = GlConst.toGlInternalId(format);

			if (GL.getCapabilities().GL_ARB_texture_buffer_range) {
				ARBTextureBufferRange.glTexBufferRange(GL31C.GL_TEXTURE_BUFFER, internalFormat, key.buffer(), slice.offset(),
					slice.length());
			} else if (GL.getCapabilities().OpenGL43) {
				GL43C.glTexBufferRange(GL31C.GL_TEXTURE_BUFFER, internalFormat, key.buffer(), slice.offset(), slice.length());
			} else if (slice.offset() == 0 && slice.length() == slice.buffer().size()) {
				GL31C.glTexBuffer(GL31C.GL_TEXTURE_BUFFER, internalFormat, key.buffer());
			} else {
				copy = RenderSystem.getDevice()
					.createBuffer(() -> "Iris buffer texture slice", GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_COPY_DST,
						slice.length());
				this.encoder.copyToBuffer(slice, copy.slice());
				GL31C.glTexBuffer(GL31C.GL_TEXTURE_BUFFER, internalFormat, ((GlBuffer) copy).handle());
			}

			return new TexelBufferView(texture, copy);
		}).texture();
	}

	@Override
	public void end() {
		if (!this.texels.isEmpty()) {
			var textures = List.copyOf(this.texels.values());
			RenderSystem.queueFencedTask(() -> textures.forEach(view -> {
				GlStateManager._deleteTexture(view.texture());

				if (view.buffer() != null) {
					view.buffer().close();
				}
			}));
			this.texels.clear();
		}
	}

	private void prepareDraw() {
		this.pipeline.bindVertices(this.vertices);

		if (this.indices != null) {
			GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ((GlBuffer) this.indices).handle());
		}
	}

	private int getGlIndexType() {
		return this.indexType == IndexType.INT ? GL11C.GL_UNSIGNED_INT : GL11C.GL_UNSIGNED_SHORT;
	}

	private int getIndexSize() {
		return this.indexType == IndexType.INT ? 4 : 2;
	}

	private void applyStorageBarrier() {
		if (this.storageWrites) {
			IrisRenderSystem.memoryBarrier(GL42C.GL_ALL_BARRIER_BITS);
		}
	}

	@Override
	public void draw(int count, int instances, int first, int firstInstance) {
		this.prepareDraw();
		this.drawArrays(count, instances, first, firstInstance);
	}

	private void drawArrays(int count, int instances, int first, int firstInstance) {
		if (firstInstance == 0) {
			GL31C.glDrawArraysInstanced(this.pipeline.getTopology(), first, count, instances);
		} else {
			ARBBaseInstance.glDrawArraysInstancedBaseInstance(this.pipeline.getTopology(), first, count, instances, firstInstance);
		}

		this.applyStorageBarrier();
	}

	@Override
	public void drawIndexed(int count, int instances, int first, int base, int firstInstance) {
		this.prepareDraw();
		this.drawElements(count, instances, first, base, firstInstance);
	}

	private void drawElements(int count, int instances, int first, int base, int firstInstance) {
		if (firstInstance == 0) {
			GL32C.glDrawElementsInstancedBaseVertex(this.pipeline.getTopology(), count, this.getGlIndexType(),
				(long) first * this.getIndexSize(), instances, base);
		} else {
			ARBBaseInstance.glDrawElementsInstancedBaseVertexBaseInstance(this.pipeline.getTopology(), count, this.getGlIndexType(),
				(long) first * this.getIndexSize(), instances, base, firstInstance);
		}

		this.applyStorageBarrier();
	}

	@Override
	public void drawIndirect(GpuBufferSlice commands, int count) {
		this.prepareDraw();

		GlStateManager._glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, ((GlBuffer) commands.buffer()).handle());
		GL43C.glMultiDrawArraysIndirect(this.pipeline.getTopology(), commands.offset(), count, 16);
		this.applyStorageBarrier();
	}

	@Override
	public void drawIndexedIndirect(GpuBufferSlice commands, int count) {
		this.prepareDraw();

		GlStateManager._glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, ((GlBuffer) commands.buffer()).handle());
		GL43C.glMultiDrawElementsIndirect(this.pipeline.getTopology(), this.getGlIndexType(), commands.offset(), count, 20);
		this.applyStorageBarrier();
	}

	@Override
	public void multiDraw(IntBuffer first, IntBuffer counts, int count) {
		this.prepareDraw();

		GL14C.nglMultiDrawArrays(this.pipeline.getTopology(), MemoryUtil.memAddress(first), MemoryUtil.memAddress(counts), count);
		this.applyStorageBarrier();
	}

	@Override
	public void multiDrawIndexed(PointerBuffer offsets, IntBuffer counts, IntBuffer bases, int count) {
		this.prepareDraw();

		if (bases == null) {
			GL14C.nglMultiDrawElements(this.pipeline.getTopology(), MemoryUtil.memAddress(counts), this.getGlIndexType(),
				MemoryUtil.memAddress(offsets), count);
		} else {
			GL32C.nglMultiDrawElementsBaseVertex(this.pipeline.getTopology(), MemoryUtil.memAddress(counts), this.getGlIndexType(),
				MemoryUtil.memAddress(offsets), count, MemoryUtil.memAddress(bases));
		}
		this.applyStorageBarrier();
	}

	@Override
	public void multiDraw(IntBuffer commands, int instances, int firstInstance, int count) {
		this.prepareDraw();

		for (int i = 0; i < count; i++) {
			int offset = commands.position() + i * 2;
			this.drawArrays(commands.get(offset + 1), instances, commands.get(offset), firstInstance);
		}
	}

	@Override
	public void multiDrawIndexed(IntBuffer commands, int instances, int firstInstance, int count) {
		this.prepareDraw();

		for (int i = 0; i < count; i++) {
			int offset = commands.position() + i * 3;
			this.drawElements(commands.get(offset + 1), instances, commands.get(offset), commands.get(offset + 2), firstInstance);
		}
	}

	private record TexelBufferKey(int buffer, long offset, long length, GpuFormat format) {
	}

	private record TexelBufferView(int texture, GpuBuffer buffer) {
	}

}
