package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice.MappedView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import java.nio.ByteBuffer;

import static net.irisshaders.iris.gl.buffer.ShaderStorageBuffer.USAGE_STORAGE;

public class VulkanStorageBuffer extends VulkanGpuBuffer {
	private final VulkanDevice device;
	private final long allocation;
	private boolean closed;

	public VulkanStorageBuffer(VulkanDevice device, String name, long size, byte[] content) {
		this(device, allocate(device, name, size, content), size);

		try (var stack = MemoryStack.stackPush()) {
			var encoder = device.createCommandEncoder();
			var command = VulkanCommandBridge.commandBuffer(encoder);
			VulkanCommandEncoder.memoryBarrier(command, stack);
			VK10.vkCmdFillBuffer(command, this.vkBuffer(), 0, VK10.VK_WHOLE_SIZE, 0);
			VulkanCommandEncoder.memoryBarrier(command, stack);

			if (content != null && content.length > 0) {
				var data = ByteBuffer.allocateDirect(Math.toIntExact(alignedSize(content.length)));
				data.put(content).position(0);

				try (var staging = device.createBuffer(() -> name + " upload", GpuBuffer.USAGE_COPY_SRC, data)) {
					var copy = VkBufferCopy.calloc(1, stack).size(data.remaining());
					VK10.vkCmdCopyBuffer(command, ((VulkanGpuBuffer) staging).vkBuffer(), this.vkBuffer(), copy);
					VulkanCommandEncoder.memoryBarrier(command, stack);
				}
			}
		} catch (RuntimeException | Error failure) {
			this.close();

			throw failure;
		}
	}

	private VulkanStorageBuffer(VulkanDevice device, Allocation allocation, long size) {
		super(allocation.buffer(), USAGE_STORAGE | USAGE_COPY_SRC | USAGE_COPY_DST | USAGE_INDIRECT_PARAMETERS, size);
		this.device = device;
		this.allocation = allocation.memory();
	}

	private static long alignedSize(long size) {
		return Math.addExact(size, 3) & ~3L;
	}

	private static Allocation allocate(VulkanDevice device, String name, long size, byte[] content) {
		if (size <= 0 || content != null && content.length > size) {
			throw new IllegalArgumentException("invalid storage buffer size or content: " + name);
		}

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(device.vkDevice().getPhysicalDevice(), properties);

			long maximum = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange());

			if (size > maximum) {
				throw new IllegalArgumentException("storage buffer exceeds device limit: " + name + " (" + size + " > " + maximum + ")");
			}

			var info = VkBufferCreateInfo.calloc(stack).sType$Default().size(alignedSize(size)).usage(
					VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT)
				.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			var buffer = stack.mallocLong(1);
			var memory = stack.mallocPointer(1);
			check(Vma.vmaCreateBuffer(device.vma(), info, VmaAllocationCreateInfo.calloc(stack).usage(Vma.VMA_MEMORY_USAGE_AUTO), buffer,
				memory, null), "allocate " + name);
			device.instance().debug().setObjectName(device.vkDevice(), VK10.VK_OBJECT_TYPE_BUFFER, buffer.get(0), name);

			return new Allocation(buffer.get(0), memory.get(0));
		}
	}

	private static void check(int result, String action) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("failed to " + action + ": " + result);
		}
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.device.createCommandEncoder().queueForDestroy(this);
	}

	@Override
	public void destroy() {
		Vma.vmaDestroyBuffer(this.device.vma(), this.vkBuffer(), this.allocation);
	}

	@Override
	public MappedView map(long offset, long length, boolean read, boolean write) {
		throw new UnsupportedOperationException("storage buffers use staging uploads and readback buffers");
	}

	public record Allocation(long buffer, long memory) {
	}
}
