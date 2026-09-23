package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageFormatProperties;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class VulkanDummySampler implements AutoCloseable {
	private final VulkanDevice device;
	private final boolean buffer;
	private final VulkanSamplerHeap.Entry entry;
	private long resource;
	private long allocation;
	private long view;

	public VulkanDummySampler(VulkanDevice device, SamplerHeapLayout.Type type, long sampler) {
		this.device = device;
		this.buffer = type.texelBuffer();

		try (var stack = MemoryStack.stackPush()) {
			if (this.buffer) {
				this.createBuffer(stack, type);
				this.entry = new VulkanSamplerHeap.TexelBuffer(this.view);
			} else {
				this.createImage(stack, type);
				this.entry = new VulkanSamplerHeap.ImageSampler(this.view, sampler, VK10.VK_IMAGE_LAYOUT_GENERAL);
			}
		} catch (RuntimeException | Error failure) {
			this.close();

			throw failure;
		}
	}

	private static void check(int result) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("failed to create dummy sampler: VkResult " + result);
		}
	}

	private void createBuffer(MemoryStack stack, SamplerHeapLayout.Type type) {
		int format = type.glsl.startsWith("i") ? VK10.VK_FORMAT_R32_SINT : type.glsl.startsWith(
			"u") ? VK10.VK_FORMAT_R32_UINT : VK10.VK_FORMAT_R32_SFLOAT;
		var handle = stack.mallocLong(1);
		var memory = stack.mallocPointer(1);
		var info = VkBufferCreateInfo.calloc(stack).sType$Default().size(16)
			.usage(VK10.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
			.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		check(Vma.vmaCreateBuffer(this.device.vma(), info, VmaAllocationCreateInfo.calloc(stack).usage(Vma.VMA_MEMORY_USAGE_AUTO), handle,
			memory, null));
		this.resource = handle.get(0);
		this.allocation = memory.get(0);
		check(VK10.vkCreateBufferView(this.device.vkDevice(),
			VkBufferViewCreateInfo.calloc(stack).sType$Default().buffer(this.resource).format(format).range(16), null, handle));
		this.view = handle.get(0);

		var command = VulkanCommandBridge.commandBuffer(this.device.createCommandEncoder());
		VK10.vkCmdFillBuffer(command, this.resource, 0, 16, 0);
		VulkanCommandEncoder.memoryBarrier(command, stack);
	}

	private void createImage(MemoryStack stack, SamplerHeapLayout.Type type) {
		boolean shadow = type.glsl.endsWith("Shadow");
		boolean cube = type.glsl.contains("Cube");
		boolean array = type.glsl.contains("Array");
		boolean multisampled = type.glsl.contains("MS");
		int dimension = type.glsl.contains("1D") ? VK10.VK_IMAGE_TYPE_1D : type.glsl.contains(
			"3D") ? VK10.VK_IMAGE_TYPE_3D : VK10.VK_IMAGE_TYPE_2D;
		int viewType = cube ? (array ? VK10.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY : VK10.VK_IMAGE_VIEW_TYPE_CUBE) : dimension == VK10.VK_IMAGE_TYPE_1D ? (array ? VK10.VK_IMAGE_VIEW_TYPE_1D_ARRAY : VK10.VK_IMAGE_VIEW_TYPE_1D) : dimension == VK10.VK_IMAGE_TYPE_3D ? VK10.VK_IMAGE_VIEW_TYPE_3D : (array ? VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY : VK10.VK_IMAGE_VIEW_TYPE_2D);
		int format = shadow ? VK10.VK_FORMAT_D16_UNORM : type.glsl.startsWith("i") ? VK10.VK_FORMAT_R8G8B8A8_SINT : type.glsl.startsWith(
			"u") ? VK10.VK_FORMAT_R8G8B8A8_UINT : VK10.VK_FORMAT_R8G8B8A8_UNORM;
		int aspect = shadow ? VK10.VK_IMAGE_ASPECT_DEPTH_BIT : VK10.VK_IMAGE_ASPECT_COLOR_BIT;
		int layers = cube ? 6 : 1;
		int flags = cube ? VK10.VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT : 0;
		int usage = VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
		var properties = VkImageFormatProperties.calloc(stack);
		check(VK10.vkGetPhysicalDeviceImageFormatProperties(this.device.vkDevice().getPhysicalDevice(), format, dimension,
			VK10.VK_IMAGE_TILING_OPTIMAL, usage, flags, properties));
		int samples = multisampled ? Integer.lowestOneBit(
			properties.sampleCounts() & ~VK10.VK_SAMPLE_COUNT_1_BIT) : VK10.VK_SAMPLE_COUNT_1_BIT;

		if (samples == 0) {
			throw new IllegalArgumentException("unsupported dummy sampler type: " + type);
		}

		var handle = stack.mallocLong(1);
		var memory = stack.mallocPointer(1);
		var info = VkImageCreateInfo.calloc(stack).sType$Default().flags(flags).imageType(dimension).format(format).mipLevels(1)
			.arrayLayers(layers).samples(samples).tiling(VK10.VK_IMAGE_TILING_OPTIMAL).usage(usage)
			.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		info.extent().set(1, 1, 1);
		check(Vma.vmaCreateImage(this.device.vma(), info, VmaAllocationCreateInfo.calloc(stack).usage(Vma.VMA_MEMORY_USAGE_AUTO), handle,
			memory, null));
		this.resource = handle.get(0);
		this.allocation = memory.get(0);
		var viewInfo = VkImageViewCreateInfo.calloc(stack).sType$Default().image(this.resource).format(format).viewType(viewType);
		viewInfo.subresourceRange().aspectMask(aspect).levelCount(1).layerCount(layers);
		check(VK10.vkCreateImageView(this.device.vkDevice(), viewInfo, null, handle));
		this.view = handle.get(0);

		var command = VulkanCommandBridge.commandBuffer(this.device.createCommandEncoder());
		var barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default().oldLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
			.newLayout(VK10.VK_IMAGE_LAYOUT_GENERAL).dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT)
			.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).image(this.resource);
		barrier.subresourceRange().aspectMask(aspect).levelCount(1).layerCount(layers);
		VK10.vkCmdPipelineBarrier(command, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null,
			barrier);

		if (shadow) {
			VK10.vkCmdClearDepthStencilImage(command, this.resource, VK10.VK_IMAGE_LAYOUT_GENERAL,
				VkClearDepthStencilValue.calloc(stack).depth(1.0f), barrier.get(0).subresourceRange());
		} else {
			VK10.vkCmdClearColorImage(command, this.resource, VK10.VK_IMAGE_LAYOUT_GENERAL, VkClearColorValue.calloc(stack),
				barrier.get(0).subresourceRange());
		}

		VulkanCommandEncoder.memoryBarrier(command, stack);
	}

	public VulkanSamplerHeap.Entry entry() {
		return this.entry;
	}

	@Override
	public void close() {
		long oldView = this.view;
		long oldResource = this.resource;
		long oldAllocation = this.allocation;
		this.view = this.resource = this.allocation = 0;

		if (oldResource == 0) {
			return;
		}

		this.device.createCommandEncoder().queueForDestroy(() -> {
			if (this.buffer) {
				if (oldView != 0) {
					VK10.vkDestroyBufferView(this.device.vkDevice(), oldView, null);
				}
				Vma.vmaDestroyBuffer(this.device.vma(), oldResource, oldAllocation);
			} else {
				if (oldView != 0) {
					VK10.vkDestroyImageView(this.device.vkDevice(), oldView, null);
				}
				Vma.vmaDestroyImage(this.device.vma(), oldResource, oldAllocation);
			}
		});
	}
}
