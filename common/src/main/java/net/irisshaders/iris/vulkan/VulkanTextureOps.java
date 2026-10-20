package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import net.irisshaders.iris.Iris;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import java.util.List;

import static org.lwjgl.vulkan.KHRSynchronization2.*;

public class VulkanTextureOps {
	public static void clearColors(List<GpuTextureView> views, Vector4fc color) {
		if (views.isEmpty()) {
			return;
		}

		var command = VulkanCommandBridge.commandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			var value = VkClearColorValue.calloc(stack);
			var range = VkImageSubresourceRange.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);

			for (var view : views) {
				Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_CLEAR_BIT_KHR, 0, VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
				var texture = (VulkanGpuTexture) view.texture();

				clearValue(value, texture.getFormat(), color);

				range.baseMipLevel(view.baseMipLevel()).levelCount(view.mipLevels()).baseArrayLayer(0)
					.layerCount(texture.getDepthOrLayers());
				VK10.vkCmdClearColorImage(command, texture.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL, value, range);
			}

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

	public static void generateMipmaps(GpuTexture texture, boolean linear) {
		var image = (VulkanGpuTexture) texture;
		if (texture.getMipLevels() == 1) {
			return;
		}


		var command = VulkanCommandBridge.commandBuffer();

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
				Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_BLIT_BIT_KHR, VK_ACCESS_2_TRANSFER_READ_BIT_KHR,
					VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
				region.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level - 1)
					.layerCount(texture.getDepthOrLayers());
				region.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).layerCount(texture.getDepthOrLayers());
				region.srcOffsets(1).set(Math.max(1, texture.getWidth(level - 1)), Math.max(1, texture.getHeight(level - 1)), 1);
				region.dstOffsets(1).set(Math.max(1, texture.getWidth(level)), Math.max(1, texture.getHeight(level)), 1);
				VK10.vkCmdBlitImage(command, image.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL, image.vkImage(), VK10.VK_IMAGE_LAYOUT_GENERAL,
					region, linear ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST);
			}

		}
	}
}
