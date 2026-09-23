package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.ForwardingRenderPass;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.mixinterface.VulkanWorldPassAccess;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTMultiDraw;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkDrawIndirectCommand;
import org.lwjgl.vulkan.VkMultiDrawIndexedInfoEXT;
import org.lwjgl.vulkan.VkMultiDrawInfoEXT;
import org.lwjgl.vulkan.VkViewport;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// using VK render passes because 26.4 will
public class VulkanWorldCommands extends ForwardingRenderPass implements WorldPassCommands {
	private final VulkanCommandEncoder encoder;
	private final boolean owned;
	private final VkCommandBuffer command;
	private final Map<TexelBufferKey, Long> texels = new HashMap<>();
	private boolean closed;
	private VulkanGraphicsShader.Pipeline pipeline;
	private int indexSize = 4;

	public VulkanWorldCommands(VulkanCommandEncoder encoder, RenderPassDescriptor descriptor) {
		this(encoder, ((VulkanWorldPassAccess) encoder).iris$createRenderPass(descriptor), true);
	}

	public VulkanWorldCommands(VulkanCommandEncoder encoder, RenderPassBackend pass, boolean owned) {
		super(pass);
		this.encoder = encoder;
		this.owned = owned;
		this.command = encoder.commandBuffer();
		((VulkanWorldPassAccess) encoder).iris$setWorldPass(this);
	}

	public boolean isOwned() {
		return this.owned;
	}

	public boolean owns(RenderPassBackend pass) {
		return this.delegate == pass;
	}

	public void finish(MemoryStack stack) {
		VulkanSynchronization.afterWorldPass(this.command, stack);
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
		try (var stack = MemoryStack.stackPush()) {
			VK10.vkCmdSetViewport(this.command, 0,
				VkViewport.calloc(1, stack).x(x).y(y).width(width).height(height).minDepth(0).maxDepth(1));
		}
	}

	@Override
	public void pushConstants(ByteBuffer data) {
		if (this.pipeline == null) {
			throw new IllegalStateException("no graphics pipeline bound");
		}

		this.pipeline.pushConstants(this.command, data);
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
	public void setIndexBuffer(GpuBuffer buffer, IndexType type) {
		this.indexSize = type == IndexType.INT ? 4 : 2;
		super.setIndexBuffer(buffer, type);
	}

	@Override
	public void setUniform(int index, Object value) {
	}

	@Override
	public void bindPipeline(GraphicsPipeline pipeline) {
		this.pipeline = (VulkanGraphicsShader.Pipeline) pipeline;
		this.pipeline.bind(this.encoder, this.command, !this.owned);
	}

	@Override
	public void setAttachments(List<GpuTextureView> colors, GpuTextureView depth, RenderArea area) {

		if (!this.owned) {
			throw new IllegalStateException("cannot change vanilla render pass attachments");
		}

		try (var stack = MemoryStack.stackPush()) {
			VK10.vkCmdEndRenderPass(this.command);
			this.finish(stack);
			((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache().begin(this.command, stack, colors, depth, area);
		}
	}

	@Override
	public long getTexelBufferView(GpuBufferSlice slice, GpuFormat format) {
		var key = new TexelBufferKey(((VulkanGpuBuffer) slice.buffer()).vkBuffer(), slice.offset(), slice.length(), format);

		return this.texels.computeIfAbsent(key, ignored -> {
			var device = this.command.getDevice();

			try (var stack = MemoryStack.stackPush()) {
				var result = stack.mallocLong(1);
				int status = VK10.vkCreateBufferView(device,
					VkBufferViewCreateInfo.calloc(stack).sType$Default().buffer(key.buffer()).offset(key.offset()).range(key.length())
						.format(VulkanConst.toVk(format)), null, result);

				if (status != VK10.VK_SUCCESS) {
					throw new IllegalStateException("cannot create world texel buffer view: " + status);
				}

				long view = result.get(0);
				this.encoder.queueForDestroy(() -> VK10.vkDestroyBufferView(device, view, null));

				return view;
			}
		});
	}

	@Override
	public void end() {
		this.texels.clear();
	}

	@Override
	public void draw(int count, int instances, int first, int firstInstance) {
		VK10.vkCmdDraw(this.command, count, instances, first, firstInstance);
	}

	@Override
	public void drawIndexed(int count, int instances, int first, int offset, int firstInstance) {
		VK10.vkCmdDrawIndexed(this.command, count, instances, first, offset, firstInstance);
	}

	@Override
	public void drawIndirect(GpuBufferSlice commands, int count) {
		VK10.vkCmdDrawIndirect(this.command, ((VulkanGpuBuffer) commands.buffer()).vkBuffer(), commands.offset(), count,
			VkDrawIndirectCommand.SIZEOF);
	}

	@Override
	public void drawIndexedIndirect(GpuBufferSlice commands, int count) {
		VK10.vkCmdDrawIndexedIndirect(this.command, ((VulkanGpuBuffer) commands.buffer()).vkBuffer(), commands.offset(), count,
			VkDrawIndexedIndirectCommand.SIZEOF);
	}

	@Override
	public void multiDraw(IntBuffer parameters, int instances, int firstInstance, int count) {
		EXTMultiDraw.nvkCmdDrawMultiEXT(this.command, count, MemoryUtil.memAddress(parameters), instances, firstInstance,
			VkMultiDrawInfoEXT.SIZEOF);
	}

	@Override
	public void multiDrawIndexed(IntBuffer parameters, int instances, int firstInstance, int count) {
		EXTMultiDraw.nvkCmdDrawMultiIndexedEXT(this.command, count, MemoryUtil.memAddress(parameters), instances, firstInstance,
			VkMultiDrawIndexedInfoEXT.SIZEOF, 0);
	}

	@Override
	public void multiDraw(IntBuffer first, IntBuffer counts, int count) {
		for (int i = 0; i < count; i++) {
			this.draw(counts.get(counts.position() + i), 1, first.get(first.position() + i), 0);
		}
	}

	@Override
	public void multiDrawIndexed(PointerBuffer offsets, IntBuffer counts, IntBuffer bases, int count) {
		for (int i = 0; i < count; i++) {
			this.drawIndexed(counts.get(counts.position() + i), 1, Math.toIntExact(offsets.get(offsets.position() + i) / this.indexSize),
				bases == null ? 0 : bases.get(bases.position() + i), 0);
		}
	}

	public record TexelBufferKey(long buffer, long offset, long length, GpuFormat format) {
	}
}
