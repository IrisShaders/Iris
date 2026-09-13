package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBindingFlagsCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutSupport;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceDescriptorIndexingProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class VulkanSamplerHeap implements AutoCloseable {
	private final VkDevice device;
	private final Map<GlSampler.Description, Long> samplers = new HashMap<>();
	private final SamplerHeapLayout layout;
	private final int maxSnapshots;
	private long setLayout;
	private long pool;
	private int liveSnapshots;
	private int allocations;
	private int updates;

	public int allocations() {
		return allocations;
	}

	public int updates() {
		return updates;
	}

	public VulkanSamplerHeap(VkDevice device, SamplerHeapLayout layout, int maxSnapshots) {
		this.device = Objects.requireNonNull(device);
		this.layout = Objects.requireNonNull(layout);

		if (maxSnapshots < 1) {
			throw new IllegalArgumentException("Heap must allow at least one descriptor generation");
		}

		this.maxSnapshots = maxSnapshots;

		try (var stack = MemoryStack.stackPush()) {
			validateLimits(stack);

			var bindings = VkDescriptorSetLayoutBinding.calloc(layout.heaps().size(), stack);
			var flags = stack.mallocInt(layout.heaps().size());
			int index = 0;

			for (var entry : layout.heaps().entrySet()) {
				bindings.get(index).binding(entry.getKey().binding()).descriptorType(descriptorType(entry.getKey()))
					.descriptorCount(entry.getValue().size()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
				flags.put(index++, VK12.VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT | VK12.VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT |
					VK12.VK_DESCRIPTOR_BINDING_UPDATE_UNUSED_WHILE_PENDING_BIT);
			}

			var bindingFlags = VkDescriptorSetLayoutBindingFlagsCreateInfo.calloc(stack).sType$Default().pBindingFlags(flags);
			var info = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pNext(bindingFlags)
				.flags(VK12.VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT).pBindings(bindings);
			var support = VkDescriptorSetLayoutSupport.calloc(stack).sType$Default();
			VK11.vkGetDescriptorSetLayoutSupport(device, info, support);

			if (!support.supported()) {
				throw new IllegalArgumentException("The device cannot support this sampler heap layout");
			}

			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorSetLayout(device, info, null, handle), "create sampler heap layout");
			setLayout = handle.get(0);

			var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(maxSnapshots)
				.flags(VK12.VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT | VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);

			if (layout.descriptorCount() > 0) {
				Map<Integer, Integer> counts = new TreeMap<>();
				layout.heaps().forEach((type, slots) -> counts.merge(descriptorType(type), slots.size(), Math::addExact));

				var sizes = VkDescriptorPoolSize.calloc(counts.size(), stack);
				int poolIndex = 0;

				for (var entry : counts.entrySet()) {
					sizes.get(poolIndex++).type(entry.getKey()).descriptorCount(Math.multiplyExact(entry.getValue(), maxSnapshots));
				}

				poolInfo.pPoolSizes(sizes);
			}

			check(VK10.vkCreateDescriptorPool(device, poolInfo, null, handle), "create sampler heap pool");
			pool = handle.get(0);
		} catch (RuntimeException | Error exception) {
			if (setLayout != 0) {
				VK10.vkDestroyDescriptorSetLayout(device, setLayout, null);
			}

			throw exception;
		}
	}

	private static void check(int result, String operation) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Failed to " + operation + ": VkResult " + result);
		}
	}

	private static int descriptorType(SamplerHeapLayout.Type type) {
		return type.texelBuffer() ? VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER : VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
	}

	private void validateLimits(MemoryStack stack) {
		var indexing = VkPhysicalDeviceDescriptorIndexingProperties.calloc(stack).sType$Default();
		VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(),
			VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(indexing));

		int count = layout.descriptorCount();
		int samplers = layout.heaps().entrySet().stream().filter(entry -> !entry.getKey().texelBuffer())
			.mapToInt(entry -> entry.getValue().size()).sum();

		if (samplers > Integer.toUnsignedLong(indexing.maxPerStageDescriptorUpdateAfterBindSamplers()) ||
			count > Integer.toUnsignedLong(indexing.maxPerStageDescriptorUpdateAfterBindSampledImages()) ||
			samplers > Integer.toUnsignedLong(indexing.maxDescriptorSetUpdateAfterBindSamplers()) ||
			count > Integer.toUnsignedLong(indexing.maxDescriptorSetUpdateAfterBindSampledImages()) ||
			count > Integer.toUnsignedLong(indexing.maxPerStageUpdateAfterBindResources()) ||
			(long) count * maxSnapshots > Integer.toUnsignedLong(indexing.maxUpdateAfterBindDescriptorsInAllPools())) {
			throw new IllegalArgumentException(
				"Sampler heap exceeds device descriptor indexing limits: " + count + " descriptors, " + maxSnapshots + " generations");
		}
	}

	public synchronized long setLayout() {
		requireOpen();

		return setLayout;
	}

	public SamplerHeapLayout layout() {
		return layout;
	}

	public synchronized Snapshot snapshot(Map<SamplerHeapLayout.Slot, ? extends Entry> images) {
		requireOpen();

		if (liveSnapshots == maxSnapshots) {
			throw new IllegalStateException("Retire completed sampler heap generations before allocating another");
		}

		Map<SamplerHeapLayout.Slot, Entry> copy = Map.copyOf(images);

		for (var entry : copy.entrySet()) {
			layout.requireSlot(entry.getKey());

			if (entry.getKey().type().texelBuffer() != (entry.getValue() instanceof TexelBuffer)) {
				throw new IllegalArgumentException("Descriptor does not match heap type: " + entry.getKey());
			}
		}

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(device,
					VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(setLayout)),
					handle),
				"allocate sampler heap generation");

			long set = handle.get(0);

			try {
				if (!copy.isEmpty()) {
					var writes = VkWriteDescriptorSet.calloc(copy.size(), stack);
					int index = 0;

					for (var entry : copy.entrySet()) {
						var write = writes.get(index++).sType$Default().dstSet(set).dstBinding(entry.getKey().type().binding())
							.dstArrayElement(entry.getKey().index()).descriptorType(descriptorType(entry.getKey().type()))
							.descriptorCount(1);

						if (entry.getValue() instanceof ImageSampler(long imageView, long sampler, int imageLayout)) {
							write.pImageInfo(
								VkDescriptorImageInfo.calloc(1, stack).imageView(imageView).sampler(sampler).imageLayout(imageLayout));
						} else if (entry.getValue() instanceof TexelBuffer(long bufferView)) {
							write.pTexelBufferView(stack.longs(bufferView));
						}
					}

					VK10.vkUpdateDescriptorSets(device, writes, null);
				}

				liveSnapshots++;
				allocations++;

				return new Snapshot(set, new HashMap<>(copy));
			} catch (RuntimeException | Error exception) {
				VK10.vkFreeDescriptorSets(device, pool, set);

				throw exception;
			}
		}
	}

	@Override
	public synchronized void close() {
		if (pool == 0) {
			return;
		}

		if (liveSnapshots != 0) {
			throw new IllegalStateException("Sampler heap still has live descriptor generations");
		}

		VK10.vkDestroyDescriptorPool(device, pool, null);
		VK10.vkDestroyDescriptorSetLayout(device, setLayout, null);
		pool = setLayout = 0;
		samplers.values().forEach(sampler -> VK10.vkDestroySampler(device, sampler, null));
		samplers.clear();
	}

	private void requireOpen() {
		if (pool == 0) {
			throw new IllegalStateException("Sampler heap is closed");
		}
	}

	public sealed interface Entry permits ImageSampler, TexelBuffer {
	}

	public record TexelBuffer(long bufferView) implements Entry {
		public TexelBuffer {
			if (bufferView == 0) {
				throw new IllegalArgumentException("Texel heap entries require a buffer view");
			}
		}
	}

	public record ImageSampler(long imageView, long sampler, int imageLayout) implements Entry {
		public ImageSampler {
			if (imageView == 0 || sampler == 0) {
				throw new IllegalArgumentException("Sampler heap entries require an image view and sampler");
			}

			if (imageLayout != VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL &&
				imageLayout != VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL && imageLayout != VK10.VK_IMAGE_LAYOUT_GENERAL) {
				throw new IllegalArgumentException("Unsupported sampled image layout: " + imageLayout);
			}
		}
	}

	public final class Snapshot implements AutoCloseable {
		private final Map<SamplerHeapLayout.Slot, Entry> images;
		private long set;
		private int references = 1;
		private final Map<SamplerHeapLayout.Slot, Entry> pending = new LinkedHashMap<>();

		private Snapshot(long set, Map<SamplerHeapLayout.Slot, Entry> images) {
			this.set = set;
			this.images = images;
		}

		public Snapshot retain() {
			synchronized (VulkanSamplerHeap.this) {
				if (set == 0) {
					throw new IllegalStateException("Retired frame heap");
				}

				references++;

				return this;
			}
		}

		public long handle() {
			return set;
		}

		public void append(SamplerHeapLayout.Slot slot, Entry entry) {
			synchronized (VulkanSamplerHeap.this) {
				if (set == 0) {
					throw new IllegalStateException("Retired frame heap");
				}

				layout.requireSlot(slot);

				var previous = images.putIfAbsent(slot, entry);

				if (previous != null && !previous.equals(entry)) {
					throw new IllegalStateException("Frame heap entries are immutable");
				}

				if (previous == null) {
					pending.put(slot, entry);
				}
			}
		}

		public void flush() {
			synchronized (VulkanSamplerHeap.this) {
				if (pending.isEmpty()) {
					return;
				}

				try (var stack = MemoryStack.stackPush()) {
					var writes = VkWriteDescriptorSet.calloc(pending.size(), stack);
					int i = 0;

					for (var entry : pending.entrySet()) {
						var write = writes.get(i++).sType$Default().dstSet(set).dstBinding(entry.getKey().type().binding())
							.dstArrayElement(entry.getKey().index()).descriptorType(descriptorType(entry.getKey().type()))
							.descriptorCount(1);

						if (entry.getValue() instanceof ImageSampler image) {
							write.pImageInfo(VkDescriptorImageInfo.calloc(1, stack).imageView(image.imageView()).sampler(image.sampler())
								.imageLayout(image.imageLayout()));
						} else if (entry.getValue() instanceof TexelBuffer texel) {
							write.pTexelBufferView(stack.longs(texel.bufferView()));
						}
					}

					VK10.vkUpdateDescriptorSets(device, writes, null);
					updates++;
					pending.clear();
				}
			}
		}

		public SamplerHeapLayout layout() {
			return layout;
		}

		public void requireSlots(Collection<SamplerHeapLayout.Slot> slots) {
			for (var slot : slots) {
				if (!images.containsKey(slot)) {
					throw new IllegalArgumentException("Pass accesses an unpopulated sampler heap slot: " + slot);
				}
			}
		}

		public void bind(VkCommandBuffer commandBuffer, long pipelineLayout, int bindPoint) {
			synchronized (VulkanSamplerHeap.this) {
				if (set == 0) {
					throw new IllegalStateException("Sampler heap generation is retired");
				}

				if (commandBuffer.getDevice() != device) {
					throw new IllegalArgumentException("Command buffer belongs to another device");
				}

				try (var stack = MemoryStack.stackPush()) {
					VK10.vkCmdBindDescriptorSets(commandBuffer, bindPoint, pipelineLayout, SamplerHeapLayout.DESCRIPTOR_SET,
						stack.longs(set), null);
				}
			}
		}

		@Override
		public void close() {
			synchronized (VulkanSamplerHeap.this) {
				if (set == 0) {
					return;
				}

				if (--references != 0) {
					return;
				}

				check(VK10.vkFreeDescriptorSets(device, pool, set), "retire sampler heap generation");
				set = 0;
				liveSnapshots--;
			}
		}
	}

	public long sampler(GlSampler sampler) {
		return sampler(sampler.description());
	}

	public long sampler(GlSampler.Description description) {
		requireOpen();

		return samplers.computeIfAbsent(description, this::createSampler);
	}

	private long createSampler(GlSampler.Description state) {
		try (var stack = MemoryStack.stackPush()) {
			int address = state.clamp() ? VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
			int filter = state.linear() ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST;
			var info = VkSamplerCreateInfo.calloc(stack).sType$Default().minFilter(filter).magFilter(filter)
				.mipmapMode(state.linearMips() ? VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR : VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(address).addressModeV(address).addressModeW(address).maxLod(state.mipmapped() ? VK10.VK_LOD_CLAMP_NONE : 0)
				.compareEnable(state.hardwareShadow()).compareOp(VK10.VK_COMPARE_OP_LESS_OR_EQUAL);
			var handle = stack.mallocLong(1);
			int result = VK10.vkCreateSampler(device, info, null, handle);

			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException("Could not create Iris sampler: " + result);
			}

			return handle.get(0);
		}
	}

}
