package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import java.util.List;

public class VulkanTextureOps {
	private VulkanTextureOps() {
	}

	public static void clearColors(List<GpuTextureView> views, Vector4f color) {
		if (views.isEmpty()) {
			return;
		}

		var encoder = VulkanCommandBridge.encoder(RenderSystem.getDevice().createCommandEncoder());
		var command = VulkanCommandBridge.commandBuffer(encoder);

		try (var stack = MemoryStack.stackPush()) {
			VulkanSynchronization.before(command, stack, views.stream().map(
				view -> new VulkanSynchronization.Access(VulkanSynchronization.image(view.texture()), VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
					VK10.VK_ACCESS_TRANSFER_WRITE_BIT, true)).toList());

			var value = VkClearColorValue.calloc(stack);
			var range = VkImageSubresourceRange.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);

			for (var view : views) {
				var texture = (VulkanGpuTexture) view.texture();

				clearValue(value, texture.getFormat(), color);

				range.baseMipLevel(view.baseMipLevel()).levelCount(view.mipLevels()).baseArrayLayer(0)
					.layerCount(texture.getDepthOrLayers());
				VK10.vkCmdClearColorImage(command, texture.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL, value, range);
			}

			VulkanSynchronization.after(command, stack);
		}
	}

	public static void clearValue(VkClearColorValue value, GpuFormat format, Vector4fc color) {
		for (int channel = 0; channel < 4; channel++) {
			float component = color.get(channel);

			switch (format.componentType()) {
				case SINT_8, SINT_16, SINT_32 -> value.int32(channel, (int) component);
				case UINT_8, UINT_16, UINT_32 -> value.uint32(channel, (int) (long) Math.clamp(component, 0, 4294967295.0));
				default -> {
					if (format == GpuFormat.RGB10A2_UINT) {
						value.uint32(channel, (int) (long) Math.clamp(component, 0, 4294967295.0));
					} else {
						value.float32(channel, component);
					}
				}
			}
		}
	}

	public static void copyTexture(GpuTexture source, GpuTexture destination, int width, int height) {
		var frontend = RenderSystem.getDevice().createCommandEncoder();
		var command = VulkanCommandBridge.commandBuffer(VulkanCommandBridge.encoder(frontend));

		try (var stack = MemoryStack.stackPush()) {
			VulkanSynchronization.before(command, stack, List.of(
				new VulkanSynchronization.Access(VulkanSynchronization.image(source), VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
					VK10.VK_ACCESS_TRANSFER_READ_BIT, false),
				new VulkanSynchronization.Access(VulkanSynchronization.image(destination), VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
					VK10.VK_ACCESS_TRANSFER_WRITE_BIT, true)));
			frontend.copyTextureToTexture(source, destination, 0, 0, 0, 0, 0, width, height);
		}
	}

	public static void generateMipmaps(GpuTexture texture, boolean linear) {
		if (!(texture instanceof VulkanGpuTexture image) || image.isClosed()) {
			throw new IllegalArgumentException("expected a live Vulkan texture");
		}

		if (!texture.getFormat().hasColorAspect()) {
			throw new IllegalArgumentException("color mipmap generation requires a color texture");
		}

		if (texture.getMipLevels() == 1) {
			return;
		}

		var encoder = VulkanCommandBridge.encoder(RenderSystem.getDevice().createCommandEncoder());

		var command = VulkanCommandBridge.commandBuffer(encoder);

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkFormatProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceFormatProperties(command.getDevice().getPhysicalDevice(), VulkanConst.toVk(texture.getFormat()),
				properties);

			int required = VK10.VK_FORMAT_FEATURE_BLIT_SRC_BIT | VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT | (linear ? VK10.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT : 0);

			if ((properties.optimalTilingFeatures() & required) != required) {
				throw new UnsupportedOperationException("cannot generate mipmaps for " + texture.getFormat());
			}

			var region = VkImageBlit.calloc(1, stack);

			for (int level = 1; level < texture.getMipLevels(); level++) {
				VulkanSynchronization.mip(command, stack, texture);
				region.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level - 1)
					.layerCount(texture.getDepthOrLayers());
				region.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).layerCount(texture.getDepthOrLayers());
				region.srcOffsets(1).set(Math.max(1, texture.getWidth(level - 1)), Math.max(1, texture.getHeight(level - 1)), 1);
				region.dstOffsets(1).set(Math.max(1, texture.getWidth(level)), Math.max(1, texture.getHeight(level)), 1);
				VK10.vkCmdBlitImage(command, image.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL, image.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL,
					region, linear ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST);
			}

			VulkanSynchronization.after(command, stack);
		}
	}
}
