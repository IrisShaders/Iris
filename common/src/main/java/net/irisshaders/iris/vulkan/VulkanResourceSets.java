package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
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

public class VulkanResourceSets implements AutoCloseable {
	private final VkDevice device;
	private final int size;
	private final int capacity;
	private final int setIndex;
	private final Map<Resources, SharedSet> cached = new HashMap<>();
	private final long alignment;
	private final long maxRange;
	private final long storageAlignment;
	private final long maxStorageRange;
	private final Map<String, ShaderResourceLayout.Descriptor> blocks;
	private final Map<String, ShaderResourceLayout.Descriptor> images;
	private final List<ShaderResourceLayout.Descriptor> blockOrder;
	private final int dynamicOffsetCount;
	private SharedSet previous;
	private long layout;
	private long pool;
	private int live;

	public VulkanResourceSets(VkDevice device, ShaderResourceLayout resources, int capacity) {
		this(device, resources, capacity, 0);
	}

	public VulkanResourceSets(VkDevice device, ShaderResourceLayout resources, int capacity, int setIndex) {
		this.setIndex = setIndex;
		this.device = device;
		this.size = resources.uniformBufferSize();
		this.capacity = capacity;

		if (capacity < 1) {
			throw new IllegalArgumentException("uniform sets need positive capacity");
		}

		var descriptors = resources.descriptors().stream().filter(d -> d.set() == setIndex).toList();

		if (descriptors.isEmpty()) {
			throw new IllegalArgumentException("no resources in descriptor set " + setIndex);
		}

		Map<String, ShaderResourceLayout.Descriptor> blocks = new LinkedHashMap<>();
		Map<String, ShaderResourceLayout.Descriptor> images = new LinkedHashMap<>();
		Set<Integer> bindings = new HashSet<>();

		for (var descriptor : descriptors) {
			if ((descriptor.kind() != ShaderResourceLayout.Kind.UNIFORM_BUFFER && descriptor.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER && descriptor.kind() != ShaderResourceLayout.Kind.IMAGE) || descriptor.count() < 1 || descriptor.binding() < 0 || (descriptor.kind() == ShaderResourceLayout.Kind.IMAGE ? images : blocks).putIfAbsent(
				descriptor.name(), descriptor) != null || !bindings.add(descriptor.binding())) {
				throw new IllegalArgumentException("invalid program descriptor: " + descriptor);
			}
		}

		if (blocks.containsKey(ShaderResourceLayout.UNIFORM_BLOCK) && this.size <= 0) {
			throw new IllegalArgumentException("empty generated uniform block");
		}

		this.blocks = Collections.unmodifiableMap(blocks);
		this.images = Collections.unmodifiableMap(images);
		this.blockOrder = blocks.values().stream().sorted(Comparator.comparingInt(ShaderResourceLayout.Descriptor::binding)).toList();
		this.dynamicOffsetCount = this.blockOrder.stream().filter(d -> descriptorType(d) == VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
			.mapToInt(ShaderResourceLayout.Descriptor::count).reduce(0, Math::addExact);

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(device.getPhysicalDevice(), properties);
			this.alignment = properties.limits().minUniformBufferOffsetAlignment();
			this.storageAlignment = properties.limits().minStorageBufferOffsetAlignment();
			this.maxStorageRange = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange());
			this.maxRange = Integer.toUnsignedLong(properties.limits().maxUniformBufferRange());

			if (this.size > this.maxRange) {
				throw new IllegalArgumentException("uniform block exceeds maxUniformBufferRange");
			}

			var binding = VkDescriptorSetLayoutBinding.calloc(descriptors.size(), stack);
			int index = 0;
			var counts = new LinkedHashMap<Integer, Integer>();

			for (var descriptor : descriptors) {
				int type = descriptorType(descriptor);
				counts.merge(type, descriptor.count(), Math::addExact);
				binding.get(index++).binding(descriptor.binding()).descriptorType(type).descriptorCount(descriptor.count())
					.stageFlags(stageFlags(descriptor));
			}

			var sizes = VkDescriptorPoolSize.calloc(counts.size(), stack);

			for (var entry : counts.entrySet()) {
				sizes.get().type(entry.getKey()).descriptorCount(Math.multiplyExact(entry.getValue(), capacity));
			}

			sizes.flip();

			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(binding),
				null, handle), "create uniform set layout");
			this.layout = handle.get(0);
			check(VK10.vkCreateDescriptorPool(device, VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(capacity)
					.flags(VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT).pPoolSizes(sizes), null, handle),
				"create uniform descriptor pool");
			this.pool = handle.get(0);
		} catch (RuntimeException | Error exception) {
			if (this.layout != 0) {
				VK10.vkDestroyDescriptorSetLayout(device, this.layout, null);
			}

			throw exception;
		}
	}

	private static int descriptorType(ShaderResourceLayout.Descriptor descriptor) {
		return switch (descriptor.kind()) {
			case UNIFORM_BUFFER ->
				descriptor.set() == 0 ? VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC : VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
			case STORAGE_BUFFER -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
			case IMAGE -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
			default -> throw new IllegalArgumentException("not a set 0 resource: " + descriptor);
		};
	}

	private static int stageFlags(ShaderResourceLayout.Descriptor descriptor) {
		int flags = 0;

		for (var stage : descriptor.stages()) {
			flags |= VulkanShaderCompiler.stageFlag(stage);
		}

		if (flags == 0) {
			throw new IllegalArgumentException("uniform buffer has no shader stages: " + descriptor.name());
		}

		return flags;
	}

	private static void check(int result, String operation) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("failed to " + operation + ": VkResult " + result);
		}
	}

	public long setLayout() {
		this.requireOpen();

		return this.layout;
	}

	public List<LayoutBinding> getLayoutBindings() {
		var descriptors = new ArrayList<>(this.blocks.values());
		descriptors.addAll(this.images.values());

		return descriptors.stream().sorted(Comparator.comparingInt(ShaderResourceLayout.Descriptor::binding)).map(
				descriptor -> new LayoutBinding(descriptor.binding(), descriptorType(descriptor), descriptor.count(), stageFlags(descriptor)))
			.toList();
	}

	public Snapshot snapshot(Map<String, List<BufferSlice>> buffers) {
		return this.snapshot(buffers, Map.of());
	}

	public Snapshot snapshot(Map<String, List<BufferSlice>> buffers, Map<String, List<Long>> views) {
		this.requireOpen();

		if (!buffers.keySet().equals(this.blocks.keySet())) {
			throw new IllegalArgumentException("buffer bindings must match shader blocks: " + this.blocks.keySet());
		}

		if (!views.keySet().equals(this.images.keySet())) {
			throw new IllegalArgumentException("image bindings must match shader images: " + this.images.keySet());
		}

		var reused = this.reuseSnapshot(buffers, views);

		if (reused != null) {
			return reused;
		}

		var imageCopy = new LinkedHashMap<String, List<Long>>();

		for (var entry : this.images.entrySet()) {
			var list = List.copyOf(views.get(entry.getKey()));

			if (list.size() != entry.getValue().count() || list.stream().anyMatch(v -> v == 0)) {
				throw new IllegalArgumentException("invalid image views: " + entry.getKey());
			}

			imageCopy.put(entry.getKey(), list);
		}

		Map<String, List<BufferSlice>> copy = new LinkedHashMap<>();

		for (var entry : this.blocks.entrySet()) {
			var slices = List.copyOf(buffers.get(entry.getKey()));

			if (slices.size() != entry.getValue().count()) {
				throw new IllegalArgumentException("wrong buffer array size: " + entry.getKey());
			}

			boolean storage = entry.getValue().kind() == ShaderResourceLayout.Kind.STORAGE_BUFFER;

			for (var slice : slices) {
				if (slice.offset() % (storage ? this.storageAlignment : this.alignment) != 0 || slice.length() > (storage ? this.maxStorageRange : this.maxRange) || entry.getKey()
					.equals(ShaderResourceLayout.UNIFORM_BLOCK) && slice.length() < this.size) {
					throw new IllegalArgumentException("invalid or unaligned buffer slice: " + entry.getKey());
				}
			}

			copy.put(entry.getKey(), slices);
		}

		var dynamicOffsets = new ArrayList<Integer>();
		var descriptorBuffers = new LinkedHashMap<String, List<BufferSlice>>();
		for (var descriptor : this.blockOrder) {
			var slices = copy.get(descriptor.name());

			if (descriptorType(descriptor) == VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC) {
				var normalized = new ArrayList<BufferSlice>();

				for (var slice : slices) {
					if (slice.offset() > 0xffffffffL) {
						throw new IllegalArgumentException("dynamic uniform offset exceeds uint32");
					}

					dynamicOffsets.add((int) slice.offset());
					normalized.add(new BufferSlice(slice.buffer(), 0, slice.length()));
				}

				descriptorBuffers.put(descriptor.name(), List.copyOf(normalized));
			} else {
				descriptorBuffers.put(descriptor.name(), slices);
			}
		}

		var key = new Resources(Map.copyOf(descriptorBuffers), Map.copyOf(imageCopy));
		int[] offsets = dynamicOffsets.stream().mapToInt(Integer::intValue).toArray();
		var previous = this.cached.get(key);

		if (previous != null) {
			previous.references++;
			this.previous = previous;

			return new Snapshot(previous, offsets);
		}

		if (this.live == this.capacity) {
			throw new IllegalStateException("retire completed uniform sets before allocating another");
		}

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(this.device,
				VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(this.pool).pSetLayouts(stack.longs(this.layout)),
				handle), "allocate uniform set");

			long set = handle.get(0);

			try {
				var writes = VkWriteDescriptorSet.calloc(this.blocks.size() + this.images.size(), stack);
				int index = 0;

				for (var entry : this.blocks.entrySet()) {
					var slices = descriptorBuffers.get(entry.getKey());
					var info = VkDescriptorBufferInfo.calloc(slices.size(), stack);

					for (int i = 0; i < slices.size(); i++) {
						var slice = slices.get(i);
						info.get(i).buffer(slice.buffer()).offset(slice.offset()).range(slice.length());
					}

					writes.get(index++).sType$Default().dstSet(set).dstBinding(entry.getValue().binding())
						.descriptorType(descriptorType(entry.getValue())).descriptorCount(slices.size()).pBufferInfo(info);
				}

				for (var entry : this.images.entrySet()) {
					var list = imageCopy.get(entry.getKey());
					var info = VkDescriptorImageInfo.calloc(list.size(), stack);

					for (int i = 0; i < list.size(); i++) {
						info.get(i).imageView(list.get(i)).imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
					}

					writes.get(index++).sType$Default().dstSet(set).dstBinding(entry.getValue().binding())
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(list.size()).pImageInfo(info);
				}

				VK10.vkUpdateDescriptorSets(this.device, writes, null);

				this.live++;

				var shared = new SharedSet(set, key);
				this.cached.put(key, shared);
				this.previous = shared;

				return new Snapshot(shared, offsets);
			} catch (RuntimeException | Error exception) {
				VK10.vkFreeDescriptorSets(this.device, this.pool, set);

				throw exception;
			}
		}
	}

	private Snapshot reuseSnapshot(Map<String, List<BufferSlice>> buffers, Map<String, List<Long>> views) {
		var shared = this.previous;

		if (shared == null || !shared.resources.images().equals(views)) {
			return null;
		}

		int[] offsets = new int[this.dynamicOffsetCount];
		int index = 0;

		for (var descriptor : this.blockOrder) {
			var slices = buffers.get(descriptor.name());
			var bound = shared.resources.buffers().get(descriptor.name());

			if (slices.size() != bound.size()) {
				return null;
			}

			boolean dynamic = descriptorType(descriptor) == VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;

			for (int i = 0; i < slices.size(); i++) {
				var slice = slices.get(i);
				var previous = bound.get(i);

				if (slice.buffer() != previous.buffer() || slice.length() != previous.length()) {
					return null;
				}

				if (dynamic) {
					if (slice.offset() % this.alignment != 0 || slice.offset() > 0xffffffffL) {
						return null;
					}

					offsets[index++] = (int) slice.offset();
				} else if (slice.offset() != previous.offset()) {
					return null;
				}
			}
		}

		shared.references++;

		return new Snapshot(shared, offsets);
	}

	private void requireOpen() {
		if (this.pool == 0) {
			throw new IllegalStateException("resource descriptor pool is closed");
		}
	}

	@Override
	public void close() {
		if (this.pool == 0) {
			return;
		}

		if (this.live != 0) {
			throw new IllegalStateException("resource descriptor pool still has live sets");
		}

		VK10.vkDestroyDescriptorPool(this.device, this.pool, null);
		VK10.vkDestroyDescriptorSetLayout(this.device, this.layout, null);
		this.pool = this.layout = 0;
	}

	public record LayoutBinding(int binding, int type, int count, int stages) {
	}

	public record BufferSlice(long buffer, long offset, long length) {
		public BufferSlice {
			if (buffer == 0 || offset < 0 || length <= 0 || offset > Long.MAX_VALUE - length) {
				throw new IllegalArgumentException("invalid buffer slice");
			}
		}
	}

	public record Resources(Map<String, List<BufferSlice>> buffers, Map<String, List<Long>> images) {
	}

	public static class SharedSet {
		private final long handle;
		private final Resources resources;
		private int references = 1;

		private SharedSet(long handle, Resources resources) {
			this.handle = handle;
			this.resources = resources;
		}
	}

	public class Snapshot implements AutoCloseable {
		private final int[] offsets;
		private SharedSet shared;

		private Snapshot(SharedSet shared, int[] offsets) {
			this.shared = shared;
			this.offsets = offsets;
		}

		public VulkanResourceSets owner() {
			return VulkanResourceSets.this;
		}

		public void bind(VkCommandBuffer command, long pipelineLayout) {
			this.bind(command, pipelineLayout, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
		}

		public void bind(VkCommandBuffer command, long pipelineLayout, int bindPoint) {
			if (this.shared == null) {
				throw new IllegalStateException("uniform set is retired");
			}

			if (command.getDevice() != VulkanResourceSets.this.device) {
				throw new IllegalArgumentException("command buffer belongs to another device");
			}

			try (var stack = MemoryStack.stackPush()) {
				VK10.vkCmdBindDescriptorSets(command, bindPoint, pipelineLayout, VulkanResourceSets.this.setIndex,
					stack.longs(this.shared.handle), this.offsets.length == 0 ? null : stack.ints(this.offsets));
			}
		}

		@Override
		public void close() {
			if (this.shared == null) {
				return;
			}

			if (--this.shared.references == 0) {
				check(VK10.vkFreeDescriptorSets(VulkanResourceSets.this.device, VulkanResourceSets.this.pool, this.shared.handle),
					"retire uniform set");
				VulkanResourceSets.this.cached.remove(this.shared.resources);

				if (VulkanResourceSets.this.previous == this.shared) {
					VulkanResourceSets.this.previous = null;
				}
				VulkanResourceSets.this.live--;
			}

			this.shared = null;
		}
	}
}
