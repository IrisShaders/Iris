package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import java.util.List;

public final class VulkanTextureOps {
	private VulkanTextureOps() {
	}

	public static void clearColors(GpuDevice device,
		List<GpuTextureView> views,
		Vector4f color) {
		if (views.isEmpty()) {
			return;
		}

		for (var view : views) {
			if (view.isClosed() || !(view.texture() instanceof VulkanGpuTexture texture) || texture.isClosed() || !texture.getFormat()
				.hasColorAspect() || (texture.usage() & GpuTexture.USAGE_COPY_DST) == 0) {
				throw new IllegalArgumentException(
					"Expected writable Vulkan color attachments");
			}
		}

		var encoder = VulkanCommandBridge.encoder(
			device.createCommandEncoder());
		var command = encoder.allocateAndBeginTransientCommandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			VulkanCommandEncoder.memoryBarrier(command, stack);

			var value = VkClearColorValue.calloc(stack);
			var range = VkImageSubresourceRange.calloc(1, stack)
				.aspectMask(
					VK10.VK_IMAGE_ASPECT_COLOR_BIT);

			for (var view : views) {
				var texture = (VulkanGpuTexture) view.texture();

				for (int channel = 0; channel < 4; channel++) {
					float component = color.get(channel);

					switch (texture.getFormat().componentType()) {
						case SINT_8, SINT_16, SINT_32 -> value.int32(channel, (int) component);
						case UINT_8, UINT_16, UINT_32 -> value.uint32(channel,
							(int) (long) Math.clamp(
								component, 0,
								4294967295.0));
						default -> {
							if (texture.getFormat() == GpuFormat.RGB10A2_UINT) {
								value.uint32(channel,
									(int) (long) Math.clamp(
										component,
										0, 4294967295.0));
							} else {
								value.float32(channel, component);
							}
						}
					}
				}

				range.baseMipLevel(view.baseMipLevel())
					.levelCount(view.mipLevels())
					.baseArrayLayer(0)
					.layerCount(texture.getDepthOrLayers());
				VK10.vkCmdClearColorImage(command, texture.vkImage(),
					VK10.VK_IMAGE_LAYOUT_GENERAL, value,
					range);
			}

			VulkanCommandEncoder.memoryBarrier(command, stack);

			int result = VK10.vkEndCommandBuffer(command);

			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException(
					"Could not finish color clears: " + result);
			}
		}

		encoder.execute(command);
	}

	public static void generateMipmaps(GpuDevice device,
		GpuTexture texture,
		boolean linear) {
		if (!(texture instanceof VulkanGpuTexture image) || image.isClosed()) {
			throw new IllegalArgumentException(
				"Expected a live Vulkan texture");
		}

		if (!texture.getFormat().hasColorAspect()) {
			throw new IllegalArgumentException(
				"Color mipmap generation requires a color texture");
		}

		if (texture.getMipLevels() == 1) {
			return;
		}

		if (!(device.createCommandEncoder() instanceof FrontendCommandEncoder frontend) ||
			!(frontend.backend() instanceof VulkanCommandEncoder encoder)) {
			throw new IllegalArgumentException(
				"Expected the Vulkan encoder");
		}

		if (frontend.isInRenderPass()) {
			throw new IllegalStateException(
				"Generate mipmaps outside the render pass");
		}

		var command = encoder.allocateAndBeginTransientCommandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkFormatProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceFormatProperties(
				command.getDevice().getPhysicalDevice(),
				VulkanConst.toVk(texture.getFormat()), properties);

			int required = VK10.VK_FORMAT_FEATURE_BLIT_SRC_BIT | VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT |
				(linear ? VK10.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT : 0);

			if ((properties.optimalTilingFeatures() & required) != required) {
				throw new UnsupportedOperationException(
					"Cannot generate mipmaps for " + texture.getFormat());
			}

			var region = VkImageBlit.calloc(1, stack);

			for (int level = 1; level < texture.getMipLevels(); level++) {
				CompositeSynchronization.mip(command, stack, texture);
				region.srcSubresource()
					.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
					.mipLevel(level - 1)
					.layerCount(texture.getDepthOrLayers());
				region.dstSubresource()
					.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
					.mipLevel(level)
					.layerCount(texture.getDepthOrLayers());
				region.srcOffsets(1)
					.set(Math.max(1, texture.getWidth(level - 1)),
						Math.max(1, texture.getHeight(level - 1)), 1);
				region.dstOffsets(1)
					.set(Math.max(1, texture.getWidth(level)),
						Math.max(1, texture.getHeight(level)), 1);
				VK10.vkCmdBlitImage(command, image.vkImage(),
					VK10.VK_IMAGE_LAYOUT_GENERAL,
					image.vkImage(),
					VK10.VK_IMAGE_LAYOUT_GENERAL, region,
					linear ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST);
			}

			CompositeSynchronization.after(command, stack);

			int result = VK10.vkEndCommandBuffer(command);

			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException(
					"Could not finish mipmap generation: " + result);
			}
		}

		encoder.execute(command);
	}
}
