package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VulkanRenderPassCache {
	private final VkDevice device;
	private final Map<Key, Long> renderPasses = new ConcurrentHashMap<>();
	private final VulkanFramebufferCache framebuffers;
	private volatile boolean closed;

	public VulkanRenderPassCache(VkDevice device) {
		this.device = device;
		this.framebuffers = new VulkanFramebufferCache(device);
	}

	private static void describe(VkAttachmentDescription target, Attachment attachment) {
		target.format(attachment.format()).samples(VK10.VK_SAMPLE_COUNT_1_BIT).loadOp(attachment.load())
			.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE).stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE).initialLayout(attachment.layout()).finalLayout(attachment.layout());
	}

	public long get(int[] colors, int depth) {
		return this.get(colors, depth, new int[colors.length], VK10.VK_ATTACHMENT_LOAD_OP_LOAD, VK10.VK_IMAGE_LAYOUT_GENERAL);
	}

	private long get(int[] colors, int depth, int[] loads, int depthLoad, int colorLayout) {
		if (this.closed) {
			throw new IllegalStateException("render pass cache is closed");
		}

		var attachments = new ArrayList<Attachment>();
		for (int i = 0; i < colors.length; i++) {
			attachments.add(new Attachment(colors[i], loads[i], colorLayout));
		}

		var key = new Key(List.copyOf(attachments),
			depth == VK10.VK_FORMAT_UNDEFINED ? null : new Attachment(depth, depthLoad, VK10.VK_IMAGE_LAYOUT_GENERAL));
		return this.renderPasses.computeIfAbsent(key, this::create);
	}

	private long create(Key key) {
		try (var stack = MemoryStack.stackPush()) {
			int colorCount = key.colors().size();
			int count = (int) key.colors().stream().filter(color -> color.format() != VK10.VK_FORMAT_UNDEFINED)
				.count() + (key.depth() == null ? 0 : 1);
			var attachments = VkAttachmentDescription.calloc(count, stack);
			var colors = VkAttachmentReference.calloc(colorCount, stack);
			int index = 0;

			for (int i = 0; i < colorCount; i++) {
				var color = key.colors().get(i);
				if (color.format() == VK10.VK_FORMAT_UNDEFINED) {
					colors.get(i).attachment(VK10.VK_ATTACHMENT_UNUSED).layout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
				} else {
					describe(attachments.get(index), color);
					colors.get(i).attachment(index++).layout(color.layout());
				}
			}

			var subpass = VkSubpassDescription.calloc(1, stack).pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
				.colorAttachmentCount(colorCount).pColorAttachments(colors);
			if (key.depth() != null) {
				describe(attachments.get(index), key.depth());
				subpass.pDepthStencilAttachment(VkAttachmentReference.calloc(stack).attachment(index).layout(key.depth().layout()));
			}

			var result = stack.mallocLong(1);
			int status = VK10.vkCreateRenderPass(this.device,
				VkRenderPassCreateInfo.calloc(stack).sType$Default().pAttachments(attachments).pSubpasses(subpass), null, result);

			if (status != VK10.VK_SUCCESS) {
				throw new IllegalStateException("cannot create render pass: " + status);
			}

			return result.get(0);
		}
	}

	public void begin(VkCommandBuffer command, MemoryStack stack, RenderPassDescriptor descriptor) {
		int[] formats = new int[descriptor.colorAttachments().size()];
		int[] loads = new int[formats.length];
		var views = new ArrayList<Long>();
		var clearValues = VkClearValue.calloc(formats.length + (descriptor.depthAttachment() == null ? 0 : 1), stack);
		int width = Integer.MAX_VALUE;
		int height = Integer.MAX_VALUE;

		for (int i = 0; i < formats.length; i++) {
			var attachment = descriptor.colorAttachments().get(i);
			if (attachment == null) {
				continue;
			}

			var view = (VulkanGpuTextureView) attachment.textureView();
			formats[i] = VulkanConst.toVk(view.texture().getFormat());
			loads[i] = attachment.clearValue().isPresent() ? VK10.VK_ATTACHMENT_LOAD_OP_CLEAR : VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
			attachment.clearValue()
				.ifPresent(color -> VulkanTextureOps.clearValue(clearValues.get(views.size()).color(), view.texture().getFormat(), color));
			views.add(view.vkImageView());
			width = Math.min(width, Math.max(1, view.getWidth(0)));
			height = Math.min(height, Math.max(1, view.getHeight(0)));
		}

		int depthFormat = VK10.VK_FORMAT_UNDEFINED;
		int depthLoad = VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
		if (descriptor.depthAttachment() != null) {
			var attachment = descriptor.depthAttachment();
			var depth = (VulkanGpuTextureView) attachment.textureView();
			depthFormat = VulkanConst.toVk(depth.texture().getFormat());
			depthLoad = attachment.clearValue().isPresent() ? VK10.VK_ATTACHMENT_LOAD_OP_CLEAR : VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
			if (attachment.clearValue().isPresent()) {
				clearValues.get(views.size()).depthStencil().depth((float) attachment.clearValue().getAsDouble());
			}
			views.add(depth.vkImageView());
			width = Math.min(width, Math.max(1, depth.getWidth(0)));
			height = Math.min(height, Math.max(1, depth.getHeight(0)));
		}

		clearValues.limit(views.size());
		this.begin(command, stack, this.get(formats, depthFormat, loads, depthLoad, VK10.VK_IMAGE_LAYOUT_GENERAL),
			this.get(formats, depthFormat), views.stream().mapToLong(Long::longValue).toArray(), width, height, descriptor.renderArea(),
			clearValues);
	}

	public void begin(VkCommandBuffer command, MemoryStack stack, List<GpuTextureView> colors, GpuTextureView depth, RenderArea area) {
		int[] formats = colors.stream().mapToInt(view -> VulkanConst.toVk(view.texture().getFormat())).toArray();
		var views = new long[colors.size() + (depth == null ? 0 : 1)];
		int width = depth == null ? Integer.MAX_VALUE : Math.max(1, depth.getWidth(0));
		int height = depth == null ? Integer.MAX_VALUE : Math.max(1, depth.getHeight(0));

		for (int i = 0; i < colors.size(); i++) {
			var view = colors.get(i);
			views[i] = ((VulkanGpuTextureView) view).vkImageView();
			width = Math.min(width, Math.max(1, view.getWidth(0)));
			height = Math.min(height, Math.max(1, view.getHeight(0)));
		}

		int depthFormat = depth == null ? VK10.VK_FORMAT_UNDEFINED : VulkanConst.toVk(depth.texture().getFormat());
		if (depth != null) {
			views[colors.size()] = ((VulkanGpuTextureView) depth).vkImageView();
		}

		long renderPass = this.get(formats, depthFormat);
		this.begin(command, stack, renderPass, renderPass, views, width, height, area, null);
	}

	public void begin(VkCommandBuffer command, MemoryStack stack, int[] colors, int depth, long[] views, int width, int height,
		int colorLayout, int depthLoad) {
		long renderPass = this.get(colors, depth, new int[colors.length], depthLoad, colorLayout);
		this.begin(command, stack, renderPass, this.get(colors, depth), views, width, height, new RenderArea(0, 0, width, height), null);
	}

	private void begin(VkCommandBuffer command, MemoryStack stack, long renderPass, long compatiblePass, long[] views, int width,
		int height, RenderArea area, VkClearValue.Buffer clearValues) {
		if (views.length == 0 || area.x() < 0 || area.y() < 0 || area.width() <= 0 || area.height() <= 0 || (long) area.x() + area.width() > width || (long) area.y() + area.height() > height) {
			throw new IllegalArgumentException("invalid render area");
		}

		var info = VkRenderPassBeginInfo.calloc(stack).sType$Default().renderPass(renderPass)
			.framebuffer(this.framebuffers.get(compatiblePass, views, width, height)).pClearValues(clearValues);
		info.renderArea().offset().set(area.x(), area.y());
		info.renderArea().extent().set(area.width(), area.height());
		VK10.vkCmdBeginRenderPass(command, info, VK10.VK_SUBPASS_CONTENTS_INLINE);
	}

	public void release(long view, VulkanCommandEncoder encoder) {
		this.framebuffers.release(view, encoder);
	}

	public void close(VulkanCommandEncoder encoder) {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.framebuffers.close(encoder);
		for (long renderPass : this.renderPasses.values()) {
			encoder.queueForDestroy(() -> VK10.vkDestroyRenderPass(this.device, renderPass, null));
		}
		this.renderPasses.clear();
	}

	public record Attachment(int format, int load, int layout) {
	}

	public record Key(List<Attachment> colors, Attachment depth) {
	}
}
