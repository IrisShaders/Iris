package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.texture.TextureType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageFormatProperties;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.ByteBuffer;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;
import static org.lwjgl.vulkan.KHRSynchronization2.*;

public class VulkanImage implements IrisImage {
	private final VulkanDevice device;
	private final TextureType type;
	private final int width;
	private final int height;
	private final int depth;
	private long image;
	private long allocation;
	private long view;

	public VulkanImage(VulkanDevice device, String name, TextureType type, GpuFormat format, int width, int height, int depth,
		boolean storage) {
		this.device = device;
		this.type = type;
		this.width = width;
		this.height = Math.max(1, height);
		this.depth = Math.max(1, depth);

		int dimension = switch (type) {
			case TEXTURE_1D -> VK10.VK_IMAGE_TYPE_1D;
			case TEXTURE_2D, TEXTURE_RECTANGLE -> VK10.VK_IMAGE_TYPE_2D;
			case TEXTURE_3D -> VK10.VK_IMAGE_TYPE_3D;
		};

		try (var stack = MemoryStack.stackPush()) {
			int usage = (storage ? VK10.VK_IMAGE_USAGE_STORAGE_BIT : 0) | VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
			var properties = VkImageFormatProperties.calloc(stack);
			int result = VK10.vkGetPhysicalDeviceImageFormatProperties(device.vkDevice().getPhysicalDevice(), VulkanConst.toVk(format),
				dimension, VK10.VK_IMAGE_TILING_OPTIMAL, usage, 0, properties);

			if (result != VK10.VK_SUCCESS || width > properties.maxExtent().width() || this.height > properties.maxExtent()
				.height() || this.depth > properties.maxExtent().depth()) {
				throw new IllegalArgumentException(
					"unsupported custom image " + name + ": " + type + " " + format + " " + width + "x" + this.height + "x" + this.depth);
			}

			var info = VkImageCreateInfo.calloc(stack).sType$Default().imageType(dimension).format(VulkanConst.toVk(format)).mipLevels(1)
				.arrayLayers(1).samples(VK10.VK_SAMPLE_COUNT_1_BIT).tiling(VK10.VK_IMAGE_TILING_OPTIMAL).usage(usage)
				.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			info.extent().set(width, this.height, this.depth);

			var handle = stack.mallocLong(1);
			var memory = stack.mallocPointer(1);
			check(Vma.vmaCreateImage(device.vma(), info, VmaAllocationCreateInfo.calloc(stack).usage(Vma.VMA_MEMORY_USAGE_AUTO), handle,
				memory, null), "allocate " + name);
			this.image = handle.get(0);
			this.allocation = memory.get(0);

			var viewInfo = VkImageViewCreateInfo.calloc(stack).sType$Default().image(this.image).format(VulkanConst.toVk(format)).viewType(
				dimension == VK10.VK_IMAGE_TYPE_1D ? VK10.VK_IMAGE_VIEW_TYPE_1D : dimension == VK10.VK_IMAGE_TYPE_3D ? VK10.VK_IMAGE_VIEW_TYPE_3D : VK10.VK_IMAGE_VIEW_TYPE_2D);
			viewInfo.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
			check(VK10.vkCreateImageView(device.vkDevice(), viewInfo, null, handle), "create view for " + name);
			this.view = handle.get(0);
			device.instance().debug().setObjectName(device.vkDevice(), VK10.VK_OBJECT_TYPE_IMAGE, this.image, name);
			device.instance().debug().setObjectName(device.vkDevice(), VK10.VK_OBJECT_TYPE_IMAGE_VIEW, this.view, name);

			var command = VulkanCommandBridge.commandBuffer();
			var barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default().oldLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				.newLayout(VK10.VK_IMAGE_LAYOUT_GENERAL).dstAccessMask(VK10.VK_ACCESS_MEMORY_READ_BIT | VK10.VK_ACCESS_MEMORY_WRITE_BIT)
				.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).image(this.image);
			barrier.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
			VK10.vkCmdPipelineBarrier(command, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, null,
				null, barrier);

			if (storage) {
				this.clear();
			}
		} catch (RuntimeException | Error failure) {
			this.close();

			throw failure;
		}
	}

	@Override
	public TextureType getType() {
		return this.type;
	}

	@Override
	public long getView() {
		return this.view;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getDepth() {
		return this.depth;
	}

	@Override
	public void upload(ByteBuffer data) {
		try (var stack = MemoryStack.stackPush()) {
			var staging = this.device.createBuffer(() -> "Iris image upload",
				GpuBuffer.USAGE_COPY_SRC, data);
			var command = VulkanCommandBridge.commandBuffer();
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COPY_BIT_KHR, VK_ACCESS_2_TRANSFER_READ_BIT_KHR,
				VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);

			var region = VkBufferImageCopy.calloc(1, stack);
			region.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).layerCount(1);
			region.imageExtent().set(this.width, this.height, this.depth);
			VK10.vkCmdCopyBufferToImage(command, ((VulkanGpuBuffer) staging).vkBuffer(), this.image, VK10.VK_IMAGE_LAYOUT_GENERAL, region);
			staging.close();
		}
	}

	@Override
	public void clear() {
		var command = VulkanCommandBridge.commandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_CLEAR_BIT_KHR, 0, VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);

			var range = VkImageSubresourceRange.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
			VK10.vkCmdClearColorImage(command, this.image, VK10.VK_IMAGE_LAYOUT_GENERAL, VkClearColorValue.calloc(stack), range);
		}
	}

	@Override
	public void close() {
		long oldView = this.view;
		long oldImage = this.image;
		long oldAllocation = this.allocation;
		this.view = this.image = this.allocation = 0;

		if (oldImage == 0) {
			return;
		}

		this.device.createCommandEncoder().queueForDestroy(() -> {
			if (oldView != 0) {
				VK10.vkDestroyImageView(this.device.vkDevice(), oldView, null);
			}

			Vma.vmaDestroyImage(this.device.vma(), oldImage, oldAllocation);
		});
	}
}
