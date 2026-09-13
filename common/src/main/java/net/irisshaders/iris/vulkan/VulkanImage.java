package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
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

public final class VulkanImage implements AutoCloseable {
	private final VulkanDevice device;
	private final GpuFormat format;
	private final int width;
	private final int height;
	private final int depth;
	private long image;
	private long allocation;
	private long view;

	public VulkanImage(VulkanDevice device, String name,
		TextureType type, GpuFormat format, int width,
		int height, int depth) {
		this(device, name, type, format, width, height, depth, true);
	}

	public VulkanImage(VulkanDevice device, String name,
		TextureType type, GpuFormat format, int width,
		int height, int depth, boolean storage) {
		this.device = device;
		this.format = format;
		this.width = width;
		this.height = Math.max(1, height);
		this.depth = Math.max(1, depth);

		if (width < 1 || height < 0 || depth < 0) {
			throw new IllegalArgumentException(
				"Invalid custom image extent: " + name);
		}

		int dimension = switch (type) {
			case TEXTURE_1D -> VK10.VK_IMAGE_TYPE_1D;
			case TEXTURE_2D, TEXTURE_RECTANGLE -> VK10.VK_IMAGE_TYPE_2D;
			case TEXTURE_3D -> VK10.VK_IMAGE_TYPE_3D;
		};

		try (var stack = MemoryStack.stackPush()) {
			int usage =
				(storage ? VK10.VK_IMAGE_USAGE_STORAGE_BIT : 0) | VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT |
					VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
			var properties = VkImageFormatProperties.calloc(stack);
			int result = VK10.vkGetPhysicalDeviceImageFormatProperties(
				device.vkDevice().getPhysicalDevice(),
				VulkanConst.toVk(format), dimension,
				VK10.VK_IMAGE_TILING_OPTIMAL, usage, 0, properties);

			if (result != VK10.VK_SUCCESS || width > properties.maxExtent()
				.width() || this.height > properties.maxExtent()
				.height() || this.depth > properties.maxExtent()
				.depth()) {
				throw new IllegalArgumentException(
					"Unsupported custom image " + name + ": " + type + " " + format + " " + width + "x" + this.height + "x" + this.depth);
			}

			var info = VkImageCreateInfo.calloc(stack)
				.sType$Default()
				.imageType(dimension)
				.format(
					VulkanConst.toVk(format))
				.mipLevels(1)
				.arrayLayers(1)
				.samples(
					VK10.VK_SAMPLE_COUNT_1_BIT)
				.tiling(
					VK10.VK_IMAGE_TILING_OPTIMAL)
				.usage(usage)
				.sharingMode(
					VK10.VK_SHARING_MODE_EXCLUSIVE);
			info.extent().set(width, this.height, this.depth);

			var handle = stack.mallocLong(1);
			var memory = stack.mallocPointer(1);
			check(Vma.vmaCreateImage(device.vma(), info,
					VmaAllocationCreateInfo.calloc(
							stack)
						.usage(
							Vma.VMA_MEMORY_USAGE_AUTO),
					handle, memory, null),
				"allocate " + name);
			image = handle.get(0);
			allocation = memory.get(0);

			var viewInfo = VkImageViewCreateInfo.calloc(stack)
				.sType$Default()
				.image(image)
				.format(
					VulkanConst.toVk(
						format))
				.viewType(
					dimension == VK10.VK_IMAGE_TYPE_1D ? VK10.VK_IMAGE_VIEW_TYPE_1D :
						dimension == VK10.VK_IMAGE_TYPE_3D ? VK10.VK_IMAGE_VIEW_TYPE_3D : VK10.VK_IMAGE_VIEW_TYPE_2D);
			viewInfo.subresourceRange()
				.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
				.levelCount(1)
				.layerCount(1);
			check(VK10.vkCreateImageView(device.vkDevice(), viewInfo, null,
					handle),
				"create view for " + name);
			view = handle.get(0);
			device.instance()
				.debug()
				.setObjectName(device.vkDevice(),
					VK10.VK_OBJECT_TYPE_IMAGE, image, name);
			device.instance()
				.debug()
				.setObjectName(device.vkDevice(),
					VK10.VK_OBJECT_TYPE_IMAGE_VIEW, view,
					name);

			var encoder = device.createCommandEncoder();
			var command = encoder.allocateAndBeginTransientCommandBuffer();
			var barrier = VkImageMemoryBarrier.calloc(1, stack)
				.sType$Default()
				.oldLayout(
					VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				.newLayout(
					VK10.VK_IMAGE_LAYOUT_GENERAL)
				.dstAccessMask(
					VK10.VK_ACCESS_MEMORY_READ_BIT | VK10.VK_ACCESS_MEMORY_WRITE_BIT)
				.srcQueueFamilyIndex(
					VK10.VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(
					VK10.VK_QUEUE_FAMILY_IGNORED)
				.image(image);
			barrier.subresourceRange()
				.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
				.levelCount(1)
				.layerCount(1);
			VK10.vkCmdPipelineBarrier(command,
				VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
				VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
				0, null, null, barrier);
			check(VK10.vkEndCommandBuffer(command), "initialize " + name);
			encoder.execute(command);

			if (storage) {
				clear();
			}
		} catch (RuntimeException | Error failure) {
			close();

			throw failure;
		}
	}

	private static void check(int status, String action) {
		if (status != VK10.VK_SUCCESS) {
			throw new IllegalStateException(
				action + ": Vulkan error " + status);
		}
	}

	public long view() {
		if (view == 0) {
			throw new IllegalStateException("Image is closed");
		}

		return view;
	}

	public long image() {
		view();

		return image;
	}

	public GpuFormat format() {
		return format;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public int depth() {
		return depth;
	}

	public void upload(ByteBuffer data) {
		view();

		try (var stack = MemoryStack.stackPush(); var staging = device.createBuffer(
			() -> "Iris image upload", GpuBuffer.USAGE_COPY_SRC,
			data)) {
			var encoder = device.createCommandEncoder();
			var command = encoder.allocateAndBeginTransientCommandBuffer();
			VulkanCommandEncoder.memoryBarrier(command, stack);

			var region = VkBufferImageCopy.calloc(1, stack);
			region.imageSubresource()
				.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
				.layerCount(1);
			region.imageExtent().set(width, height, depth);
			VK10.vkCmdCopyBufferToImage(command,
				((VulkanGpuBuffer) staging).vkBuffer(),
				image, VK10.VK_IMAGE_LAYOUT_GENERAL,
				region);
			VulkanCommandEncoder.memoryBarrier(command, stack);
			check(VK10.vkEndCommandBuffer(command), "upload image");
			encoder.execute(command);
		}
	}

	public void clear() {
		view();

		var encoder = device.createCommandEncoder();
		var command = encoder.allocateAndBeginTransientCommandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			VulkanCommandEncoder.memoryBarrier(command, stack);

			var range = VkImageSubresourceRange.calloc(1, stack)
				.aspectMask(
					VK10.VK_IMAGE_ASPECT_COLOR_BIT)
				.levelCount(1)
				.layerCount(1);
			VK10.vkCmdClearColorImage(command, image,
				VK10.VK_IMAGE_LAYOUT_GENERAL,
				VkClearColorValue.calloc(stack),
				range);
			VulkanCommandEncoder.memoryBarrier(command, stack);
			check(VK10.vkEndCommandBuffer(command), "clear custom image");
		}

		encoder.execute(command);
	}

	@Override
	public void close() {
		long oldView = view;
		long oldImage = image;
		long oldAllocation = allocation;
		view = image = allocation = 0;

		if (oldImage == 0) {
			return;
		}

		device.createCommandEncoder().queueForDestroy(() -> {
			if (oldView != 0) {
				VK10.vkDestroyImageView(device.vkDevice(), oldView, null);
			}

			Vma.vmaDestroyImage(device.vma(), oldImage,
				oldAllocation);
		});
	}
}
