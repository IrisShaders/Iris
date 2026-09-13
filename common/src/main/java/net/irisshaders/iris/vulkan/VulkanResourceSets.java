package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VulkanResourceSets implements AutoCloseable {
	private final VkDevice device;
	private final int size;
	private final int capacity;
	private final int setIndex;
	private final Map<Resources, SharedSet> cached = new HashMap<>();
	private int allocations;
	private final long alignment;
	private final long maxRange;
	private final long storageAlignment;
	private final long maxStorageRange;
	private final Map<String, VulkanResourceLayout.Descriptor> blocks, images;
	private long layout;
	private long pool;
	private int live;

	public VulkanResourceSets(VkDevice device, VulkanResourceLayout resources, int capacity) {
		this(device, resources, capacity, 0);
	}

	public VulkanResourceSets(VkDevice device, VulkanResourceLayout resources, int capacity, int setIndex) {
		this.setIndex = setIndex;
		this.device = device;
		this.size = resources.uniformBufferSize();
		this.capacity = capacity;

		if (capacity < 1) {
			throw new IllegalArgumentException("Uniform sets need positive capacity");
		}

		var descriptors = resources.descriptors().stream().filter(d -> d.set() == setIndex).toList();

		if (descriptors.isEmpty()) {
			throw new IllegalArgumentException("No resources in descriptor set " + setIndex);
		}

		Map<String, VulkanResourceLayout.Descriptor> blocks = new LinkedHashMap<>(), images = new LinkedHashMap<>();
		Set<Integer> bindings = new HashSet<>();

		for (var descriptor : descriptors) {
			if ((descriptor.kind() != VulkanResourceLayout.Kind.UNIFORM_BUFFER &&
				descriptor.kind() != VulkanResourceLayout.Kind.STORAGE_BUFFER && descriptor.kind() != VulkanResourceLayout.Kind.IMAGE) ||
				descriptor.count() < 1 || descriptor.binding() < 0 ||
				(descriptor.kind() == VulkanResourceLayout.Kind.IMAGE ? images : blocks).putIfAbsent(descriptor.name(), descriptor) !=
					null ||
				!bindings.add(descriptor.binding())) {
				throw new IllegalArgumentException("Invalid program descriptor: " + descriptor);
			}
		}

		if (blocks.containsKey(VulkanResourceLayout.UNIFORM_BLOCK) && size <= 0) {
			throw new IllegalArgumentException("Empty generated uniform block");
		}

		this.blocks = Collections.unmodifiableMap(blocks);
		this.images = Collections.unmodifiableMap(images);

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(device.getPhysicalDevice(), properties);
			alignment = properties.limits().minUniformBufferOffsetAlignment();
			storageAlignment = properties.limits().minStorageBufferOffsetAlignment();
			maxStorageRange = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange());
			maxRange = Integer.toUnsignedLong(properties.limits().maxUniformBufferRange());

			if (size > maxRange) {
				throw new IllegalArgumentException("Uniform block exceeds maxUniformBufferRange");
			}

			var binding = VkDescriptorSetLayoutBinding.calloc(descriptors.size(), stack);
			int index = 0;
			var counts = new LinkedHashMap<Integer, Integer>();

			for (var descriptor : descriptors) {
				int type = descriptorType(descriptor);
				counts.merge(type, descriptor.count(), Math::addExact);
				binding.get(index++)
					.binding(descriptor.binding())
					.descriptorType(type)
					.descriptorCount(descriptor.count())
					.stageFlags(stageFlags(descriptor));
			}

			var sizes = VkDescriptorPoolSize.calloc(counts.size(), stack);

			for (var entry : counts.entrySet()) {
				sizes.get().type(entry.getKey()).descriptorCount(Math.multiplyExact(entry.getValue(), capacity));
			}

			sizes.flip();

			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorSetLayout(device,
					VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(binding),
					null, handle),
				"create uniform set layout");
			layout = handle.get(0);
			check(VK10.vkCreateDescriptorPool(device,
					VkDescriptorPoolCreateInfo.calloc(stack)
						.sType$Default()
						.maxSets(capacity)
						.flags(VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
						.pPoolSizes(sizes),
					null, handle),
				"create uniform descriptor pool");
			pool = handle.get(0);
		} catch (RuntimeException | Error exception) {
			if (layout != 0) {
				VK10.vkDestroyDescriptorSetLayout(device, layout, null);
			}

			throw exception;
		}
	}

	private static int descriptorType(VulkanResourceLayout.Descriptor descriptor) {
		return switch (descriptor.kind()) {
			case UNIFORM_BUFFER ->
				descriptor.set() == 0 ? VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC : VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
			case STORAGE_BUFFER -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
			case IMAGE -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
			default -> throw new IllegalArgumentException("Not a set 0 resource: " + descriptor);
		};
	}

	private static int stageFlags(VulkanResourceLayout.Descriptor descriptor) {
		int flags = 0;

		for (var stage : descriptor.stages()) {
			flags |= switch (stage) {
				case VERTEX -> VK10.VK_SHADER_STAGE_VERTEX_BIT;
				case TESS_CONTROL -> VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
				case TESS_EVAL -> VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
				case GEOMETRY -> VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
				case FRAGMENT -> VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
				case COMPUTE -> VK10.VK_SHADER_STAGE_COMPUTE_BIT;
			};
		}

		if (flags == 0) {
			throw new IllegalArgumentException("Uniform buffer has no shader stages: " + descriptor.name());
		}

		return flags;
	}

	private static void check(int result, String operation) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Failed to " + operation + ": VkResult " + result);
		}
	}

	public synchronized long setLayout() {
		requireOpen();

		return layout;
	}

	public synchronized Snapshot snapshot(long buffer, long offset, long availableBytes) {
		if (blocks.size() != 1 || !blocks.containsKey(VulkanResourceLayout.UNIFORM_BLOCK)) {
			throw new IllegalArgumentException("Supply every named UBO for this shader");
		}

		if (availableBytes < size) {
			throw new IllegalArgumentException("Uniform buffer slice is too small");
		}

		return snapshot(Map.of(VulkanResourceLayout.UNIFORM_BLOCK, List.of(new BufferSlice(buffer, offset, size))));
	}

	public synchronized Snapshot snapshot(Map<String, List<BufferSlice>> buffers) {
		return snapshot(buffers, Map.of());
	}

	public synchronized Snapshot snapshot(Map<String, List<BufferSlice>> buffers, Map<String, List<Long>> views) {
		requireOpen();

		if (!buffers.keySet().equals(blocks.keySet())) {
			throw new IllegalArgumentException("Buffer bindings must match shader blocks: " + blocks.keySet());
		}

		if (!views.keySet().equals(images.keySet())) {
			throw new IllegalArgumentException("Image bindings must match shader images: " + images.keySet());
		}

		var imageCopy = new LinkedHashMap<String, List<Long>>();

		for (var entry : images.entrySet()) {
			var list = List.copyOf(views.get(entry.getKey()));

			if (list.size() != entry.getValue().count() || list.stream().anyMatch(v -> v == 0)) {
				throw new IllegalArgumentException("Invalid image views: " + entry.getKey());
			}

			imageCopy.put(entry.getKey(), list);
		}

		Map<String, List<BufferSlice>> copy = new LinkedHashMap<>();

		for (var entry : blocks.entrySet()) {
			var slices = List.copyOf(buffers.get(entry.getKey()));

			if (slices.size() != entry.getValue().count()) {
				throw new IllegalArgumentException("Wrong buffer array size: " + entry.getKey());
			}

			boolean storage = entry.getValue().kind() == VulkanResourceLayout.Kind.STORAGE_BUFFER;

			for (var slice : slices) {
				if (slice.offset() % (storage ? storageAlignment : alignment) != 0 ||
					slice.length() > (storage ? maxStorageRange : maxRange) ||
					entry.getKey().equals(VulkanResourceLayout.UNIFORM_BLOCK) && slice.length() < size) {
					throw new IllegalArgumentException("Invalid or unaligned buffer slice: " + entry.getKey());
				}
			}

			copy.put(entry.getKey(), slices);
		}

		var dynamicOffsets = new ArrayList<Integer>();
		var descriptorBuffers = new LinkedHashMap<String, List<BufferSlice>>();
		blocks.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().binding())).forEach(entry -> {
			var slices = copy.get(entry.getKey());

			if (descriptorType(entry.getValue()) == VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC) {
				var normalized = new ArrayList<BufferSlice>();

				for (var slice : slices) {
					if (slice.offset() > 0xffffffffL) {
						throw new IllegalArgumentException("Dynamic uniform offset exceeds uint32");
					}

					dynamicOffsets.add((int) slice.offset());
					normalized.add(new BufferSlice(slice.buffer(), 0, slice.length()));
				}

				descriptorBuffers.put(entry.getKey(), List.copyOf(normalized));
			} else {
				descriptorBuffers.put(entry.getKey(), slices);
			}
		});

		var key = new Resources(Map.copyOf(descriptorBuffers), Map.copyOf(imageCopy));
		int[] offsets = dynamicOffsets.stream().mapToInt(Integer::intValue).toArray();
		var previous = cached.get(key);

		if (previous != null) {
			previous.references++;

			return new Snapshot(previous, offsets);
		}

		if (live == capacity) {
			throw new IllegalStateException("Retire completed uniform sets before allocating another");
		}

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(
					device,
					VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(layout)),
					handle),
				"allocate uniform set");

			long set = handle.get(0);

			try {
				var writes = VkWriteDescriptorSet.calloc(blocks.size() + images.size(), stack);
				int index = 0;

				for (var entry : blocks.entrySet()) {
					var slices = descriptorBuffers.get(entry.getKey());
					var info = VkDescriptorBufferInfo.calloc(slices.size(), stack);

					for (int i = 0; i < slices.size(); i++) {
						var slice = slices.get(i);
						info.get(i).buffer(slice.buffer()).offset(slice.offset()).range(slice.length());
					}

					writes.get(index++)
						.sType$Default()
						.dstSet(set)
						.dstBinding(entry.getValue().binding())
						.descriptorType(descriptorType(entry.getValue()))
						.descriptorCount(slices.size())
						.pBufferInfo(info);
				}

				for (var entry : images.entrySet()) {
					var list = imageCopy.get(entry.getKey());
					var info = VkDescriptorImageInfo.calloc(list.size(), stack);

					for (int i = 0; i < list.size(); i++) {
						info.get(i).imageView(list.get(i)).imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
					}

					writes.get(index++)
						.sType$Default()
						.dstSet(set)
						.dstBinding(entry.getValue().binding())
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
						.descriptorCount(list.size())
						.pImageInfo(info);
				}

				VK10.vkUpdateDescriptorSets(device, writes, null);

				live++;
				allocations++;

				var shared = new SharedSet(set, key);
				cached.put(key, shared);

				return new Snapshot(shared, offsets);
			} catch (RuntimeException | Error exception) {
				VK10.vkFreeDescriptorSets(device, pool, set);

				throw exception;
			}
		}
	}

	public int allocations() {
		return allocations;
	}

	private void requireOpen() {
		if (pool == 0) {
			throw new IllegalStateException("Resource descriptor pool is closed");
		}
	}

	@Override
	public synchronized void close() {
		if (pool == 0) {
			return;
		}

		if (live != 0) {
			throw new IllegalStateException("Resource descriptor pool still has live sets");
		}

		VK10.vkDestroyDescriptorPool(device, pool, null);
		VK10.vkDestroyDescriptorSetLayout(device, layout, null);
		pool = layout = 0;
	}

	public record BufferSlice(long buffer, long offset, long length) {
		public BufferSlice {
			if (buffer == 0 || offset < 0 || length <= 0 || offset > Long.MAX_VALUE - length) {
				throw new IllegalArgumentException("Invalid buffer slice");
			}
		}
	}

	private record Resources(Map<String, List<BufferSlice>> buffers, Map<String, List<Long>> images) {
	}

	private static final class SharedSet {
		final long handle;
		final Resources resources;
		int references = 1;

		SharedSet(long handle, Resources resources) {
			this.handle = handle;
			this.resources = resources;
		}
	}

	public final class Snapshot implements AutoCloseable {
		private SharedSet shared;
		private final int[] offsets;

		private Snapshot(SharedSet shared, int[] offsets) {
			this.shared = shared;
			this.offsets = offsets;
		}

		public VulkanResourceSets owner() {
			return VulkanResourceSets.this;
		}

		public void bind(VkCommandBuffer command, long pipelineLayout) {
			bind(command, pipelineLayout, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
		}

		public void bind(VkCommandBuffer command, long pipelineLayout, int bindPoint) {
			synchronized (VulkanResourceSets.this) {
				if (shared == null) {
					throw new IllegalStateException("Uniform set is retired");
				}

				if (command.getDevice() != device) {
					throw new IllegalArgumentException("Command buffer belongs to another device");
				}

				try (var stack = MemoryStack.stackPush()) {
					VK10.vkCmdBindDescriptorSets(command, bindPoint, pipelineLayout, setIndex, stack.longs(shared.handle),
						offsets.length == 0 ? null : stack.ints(offsets));
				}
			}
		}

		@Override
		public void close() {
			synchronized (VulkanResourceSets.this) {
				if (shared == null) {
					return;
				}

				if (--shared.references == 0) {
					check(VK10.vkFreeDescriptorSets(device, pool, shared.handle), "retire uniform set");
					cached.remove(shared.resources);
					live--;
				}

				shared = null;
			}
		}
	}
}
