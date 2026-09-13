package net.irisshaders.iris.gl.buffer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
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

public final class ShaderStorageBuffer implements AutoCloseable {
	public static final int USAGE_STORAGE = 1 << 20;
	private final VulkanDevice device;
	private final int index;
	private final BuiltShaderStorageInfo info;
	private StorageAllocation buffer;
	private boolean closed;

	public ShaderStorageBuffer(VulkanDevice device, int index, BuiltShaderStorageInfo info, int width, int height) {
		this.device = device;
		this.index = index;
		this.info = info;
		resize(width, height);
	}

	public int getIndex() {
		return index;
	}

	public long getSize() {
		return buffer().size();
	}

	public GpuBufferSlice slice() {
		return buffer().slice();
	}

	public VulkanGpuBuffer buffer() {
		if (closed) {
			throw new IllegalStateException("SSBO " + index + " is closed");
		}

		return buffer;
	}

	private void resize(int width, int height) {
		long size = info.size();

		if (info.relative()) {
			if (width < 1 || height < 1 || !Float.isFinite(info.scaleX()) || !Float.isFinite(info.scaleY()) || info.scaleX() <= 0 ||
				info.scaleY() <= 0) {
				throw new IllegalArgumentException("Invalid relative SSBO dimensions: " + index);
			}

			long scaledWidth = Math.max(1, (long) (width * info.scaleX()));
			long scaledHeight = Math.max(1, (long) (height * info.scaleY()));
			size = Math.multiplyExact(size, Math.multiplyExact(scaledWidth, scaledHeight));
		}

		var replacement = new StorageAllocation(device, "Iris SSBO " + index, size, info.content());

		if (buffer != null) {
			buffer.close();
		}

		buffer = replacement;
	}

	public void resizeIfRelative(int width, int height) {
		buffer();

		if (info.relative()) {
			resize(width, height);
		}
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;

		if (buffer != null) {
			buffer.close();
		}
	}

	private static final class StorageAllocation extends VulkanGpuBuffer {
		private final VulkanDevice device;
		private final long allocation;
		private boolean closed;

		private StorageAllocation(VulkanDevice device, String name, long size, byte[] content) {
			this(device, allocate(device, name, size, content), size);

			try (var stack = MemoryStack.stackPush()) {
				var encoder = device.createCommandEncoder();
				var command = encoder.allocateAndBeginTransientCommandBuffer();
				VulkanCommandEncoder.memoryBarrier(command, stack);
				VK10.vkCmdFillBuffer(command, vkBuffer(), 0, VK10.VK_WHOLE_SIZE, 0);
				VulkanCommandEncoder.memoryBarrier(command, stack);

				if (content != null && content.length > 0) {
					var data = ByteBuffer.allocateDirect(Math.toIntExact(alignedSize(content.length)));
					data.put(content).position(0);

					try (var staging = device.createBuffer(() -> name + " upload", GpuBuffer.USAGE_COPY_SRC, data)) {
						var copy = VkBufferCopy.calloc(1, stack).size(data.remaining());
						VK10.vkCmdCopyBuffer(command, ((VulkanGpuBuffer) staging).vkBuffer(), vkBuffer(), copy);
						VulkanCommandEncoder.memoryBarrier(command, stack);
						check(VK10.vkEndCommandBuffer(command), "initialize " + name);
						encoder.execute(command);
					}
				} else {
					check(VK10.vkEndCommandBuffer(command), "initialize " + name);
					encoder.execute(command);
				}
			} catch (RuntimeException | Error failure) {
				close();

				throw failure;
			}
		}

		private StorageAllocation(VulkanDevice device, Allocation allocation, long size) {
			super(allocation.buffer(), USAGE_STORAGE | USAGE_COPY_SRC | USAGE_COPY_DST | USAGE_INDIRECT_PARAMETERS, size);
			this.device = device;
			this.allocation = allocation.memory();
		}

		private static long alignedSize(long size) {
			return Math.addExact(size, 3) & ~3L;
		}

		private static Allocation allocate(VulkanDevice device, String name, long size, byte[] content) {
			if (size <= 0 || content != null && content.length > size) {
				throw new IllegalArgumentException("Invalid SSBO size or initial content: " + name);
			}

			try (var stack = MemoryStack.stackPush()) {
				var properties = VkPhysicalDeviceProperties.calloc(stack);
				VK10.vkGetPhysicalDeviceProperties(device.vkDevice().getPhysicalDevice(), properties);

				long maximum = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange());

				if (size > maximum) {
					throw new IllegalArgumentException(name + " needs " + size + " bytes; maxStorageBufferRange is " + maximum);
				}

				var info = VkBufferCreateInfo.calloc(stack).sType$Default().size(alignedSize(size)).usage(
						VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT |
							VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT)
					.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				var buffer = stack.mallocLong(1);
				var memory = stack.mallocPointer(1);
				check(
					Vma.vmaCreateBuffer(device.vma(), info, VmaAllocationCreateInfo.calloc(stack).usage(Vma.VMA_MEMORY_USAGE_AUTO), buffer,
						memory, null), "allocate " + name);
				device.instance().debug().setObjectName(device.vkDevice(), VK10.VK_OBJECT_TYPE_BUFFER, buffer.get(0), name);

				return new Allocation(buffer.get(0), memory.get(0));
			}
		}

		@Override
		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() {
			if (closed) {
				return;
			}

			closed = true;
			device.createCommandEncoder().queueForDestroy(this);
		}

		@Override
		public void destroy() {
			Vma.vmaDestroyBuffer(device.vma(), vkBuffer(), allocation);
		}

		@Override
		public MappedView map(long offset, long length, boolean read, boolean write) {
			throw new UnsupportedOperationException("Storage buffers use staging uploads and readback buffers");
		}

		private static void check(int result, String action) {
			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException(action + ": Vulkan error " + result);
			}
		}

		private record Allocation(long buffer, long memory) {
		}
	}

}
