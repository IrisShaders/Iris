package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.mixin.VulkanDeviceAccessor;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.KHRPushDescriptor;
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
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDevicePushDescriptorPropertiesKHR;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.LongBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class VulkanSamplerHeap implements AutoCloseable {
	private final VkDevice device;
	private final Map<SamplerHeapLayout.Type, VulkanDummySampler> dummies = new EnumMap<>(SamplerHeapLayout.Type.class);
	private final Map<GlSampler.Description, Long> samplers = new HashMap<>();
	private final SamplerHeapLayout layout;
	private final int poolCapacity;
	private final boolean updateAfterBind;
	private final Map<Long, Integer> poolSizes = new LinkedHashMap<>();
	private final AtomicInteger layoutReferences = new AtomicInteger(1);
	private long setLayout;
	private long drawSetLayout;
	private long emptySetLayout;
	private volatile long pool;
	private int liveSnapshots;

	public VulkanSamplerHeap(VulkanDevice device, SamplerHeapLayout layout, int poolCapacity) {
		this.device = Objects.requireNonNull(device).vkDevice();
		this.layout = Objects.requireNonNull(layout);
		this.updateAfterBind = Platform.get() == Platform.MACOSX && device instanceof VulkanDeviceAccessor access && access.iris$getEnabledFeatures()
			.contains(VulkanShaderFeatures.SAMPLER_UPDATE_AFTER_BIND);

		if (poolCapacity < 1) {
			throw new IllegalArgumentException("heap must allow at least one descriptor generation");
		}

		this.poolCapacity = poolCapacity;

		try (var stack = MemoryStack.stackPush()) {
			this.validateLimits(stack);

			var bindings = VkDescriptorSetLayoutBinding.calloc(layout.heaps().size(), stack);
			int index = 0;

			for (var entry : layout.heaps().entrySet()) {
				bindings.get(index++).binding(entry.getKey().binding()).descriptorType(descriptorType(entry.getKey()))
					.descriptorCount(entry.getValue().size()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
			}

			var info = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
			if (this.updateAfterBind) {
				var flags = stack.mallocInt(bindings.capacity());
				for (int i = 0; i < flags.capacity(); i++) {
					flags.put(i, VK12.VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT);
				}
				info.flags(VK12.VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT)
					.pNext(VkDescriptorSetLayoutBindingFlagsCreateInfo.calloc(stack).sType$Default().pBindingFlags(flags));
			}
			var support = VkDescriptorSetLayoutSupport.calloc(stack).sType$Default();
			VK11.vkGetDescriptorSetLayoutSupport(this.device, info, support);

			if (!support.supported()) {
				throw new IllegalArgumentException("the device cannot support this sampler heap layout");
			}

			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorSetLayout(this.device, info, null, handle), "create sampler heap layout");
			this.setLayout = handle.get(0);

			if (!layout.drawHeaps().isEmpty()) {
				var properties = VkPhysicalDevicePushDescriptorPropertiesKHR.calloc(stack).sType$Default();
				VK11.vkGetPhysicalDeviceProperties2(this.device.getPhysicalDevice(),
					VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(properties));
				int count = layout.drawHeaps().values().stream().mapToInt(List::size).sum();

				if (count > properties.maxPushDescriptors()) {
					throw new IllegalArgumentException("too many per-draw samplers: " + count);
				}

				var draws = VkDescriptorSetLayoutBinding.calloc(layout.drawHeaps().size(), stack);
				int binding = 0;

				for (var entry : layout.drawHeaps().entrySet()) {
					draws.get(binding++).binding(entry.getKey().binding()).descriptorType(descriptorType(entry.getKey()))
						.descriptorCount(entry.getValue().size()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
				}
				check(VK10.vkCreateDescriptorSetLayout(this.device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default()
						.flags(KHRPushDescriptor.VK_DESCRIPTOR_SET_LAYOUT_CREATE_PUSH_DESCRIPTOR_BIT_KHR).pBindings(draws), null, handle),
					"create per-draw sampler layout");
				this.drawSetLayout = handle.get(0);
				check(VK10.vkCreateDescriptorSetLayout(this.device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default(), null,
					handle), "create empty sampler layout");
				this.emptySetLayout = handle.get(0);
			}

			this.pool = this.createPool(stack);
			Stream.concat(layout.heaps().keySet().stream(), layout.drawHeaps().keySet().stream()).distinct().forEach(type -> {
				long sampler = type.texelBuffer() ? 0 : this.sampler(
					new GlSampler.Description(false, false, false, type.glsl.endsWith("Shadow"), true));
				this.dummies.put(type, new VulkanDummySampler(device, type, sampler));
			});
		} catch (RuntimeException | Error exception) {
			this.dummies.values().forEach(VulkanDummySampler::close);
			this.samplers.values().forEach(sampler -> VK10.vkDestroySampler(this.device, sampler, null));
			if (this.pool != 0) {
				VK10.vkDestroyDescriptorPool(this.device, this.pool, null);
			}
			if (this.drawSetLayout != 0) {
				VK10.vkDestroyDescriptorSetLayout(this.device, this.drawSetLayout, null);
			}
			if (this.emptySetLayout != 0) {
				VK10.vkDestroyDescriptorSetLayout(this.device, this.emptySetLayout, null);
			}
			if (this.setLayout != 0) {
				VK10.vkDestroyDescriptorSetLayout(this.device, this.setLayout, null);
			}

			throw exception;
		}
	}

	private static void check(int result, String operation) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("failed to " + operation + ": VkResult " + result);
		}
	}

	private static int descriptorType(SamplerHeapLayout.Type type) {
		return type.texelBuffer() ? VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER : VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
	}

	private long createPool(MemoryStack stack) {
		var handle = stack.mallocLong(1);
		var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(this.poolCapacity)
			.flags(VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);

		if (this.updateAfterBind) {
			poolInfo.flags(poolInfo.flags() | VK12.VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT);
		}

		if (this.layout.descriptorCount() > 0) {
			Map<Integer, Integer> counts = new TreeMap<>();
			this.layout.heaps().forEach((type, slots) -> counts.merge(descriptorType(type), slots.size(), Math::addExact));

			var sizes = VkDescriptorPoolSize.calloc(counts.size(), stack);
			int poolIndex = 0;

			for (var entry : counts.entrySet()) {
				sizes.get(poolIndex++).type(entry.getKey()).descriptorCount(Math.multiplyExact(entry.getValue(), this.poolCapacity));
			}

			poolInfo.pPoolSizes(sizes);
		}

		check(VK10.vkCreateDescriptorPool(this.device, poolInfo, null, handle), "create sampler heap pool");
		long result = handle.get(0);
		this.poolSizes.put(result, 0);

		return result;
	}

	private void validateLimits(MemoryStack stack) {
		var properties = VkPhysicalDeviceProperties.calloc(stack);
		VK10.vkGetPhysicalDeviceProperties(this.device.getPhysicalDevice(), properties);
		var limits = properties.limits();

		int count = Stream.concat(this.layout.heaps().values().stream(), this.layout.drawHeaps().values().stream()).mapToInt(List::size)
			.sum();
		int samplers = Stream.concat(this.layout.heaps().entrySet().stream(), this.layout.drawHeaps().entrySet().stream())
			.filter(entry -> !entry.getKey().texelBuffer()).mapToInt(entry -> entry.getValue().size()).sum();

		int regularCount = this.updateAfterBind ? this.layout.drawHeaps().values().stream().mapToInt(List::size).sum() : count;
		int regularSamplers = this.updateAfterBind ? this.layout.drawHeaps().entrySet().stream()
			.filter(entry -> !entry.getKey().texelBuffer()).mapToInt(entry -> entry.getValue().size()).sum() : samplers;

		if (regularSamplers > Integer.toUnsignedLong(limits.maxPerStageDescriptorSamplers()) || regularCount > Integer.toUnsignedLong(
			limits.maxPerStageDescriptorSampledImages()) || regularSamplers > Integer.toUnsignedLong(
			limits.maxDescriptorSetSamplers()) || regularCount > Integer.toUnsignedLong(
			limits.maxDescriptorSetSampledImages()) || regularCount > Integer.toUnsignedLong(limits.maxPerStageResources())) {
			throw new IllegalArgumentException("sampler heap exceeds device descriptor limits: " + regularCount + " descriptors");
		}

		if (this.updateAfterBind) {
			var indexing = VkPhysicalDeviceDescriptorIndexingProperties.calloc(stack).sType$Default();
			VK11.vkGetPhysicalDeviceProperties2(this.device.getPhysicalDevice(),
				VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(indexing));
			if (samplers > Integer.toUnsignedLong(
				indexing.maxPerStageDescriptorUpdateAfterBindSamplers()) || count > Integer.toUnsignedLong(
				indexing.maxPerStageDescriptorUpdateAfterBindSampledImages()) || samplers > Integer.toUnsignedLong(
				indexing.maxDescriptorSetUpdateAfterBindSamplers()) || count > Integer.toUnsignedLong(
				indexing.maxDescriptorSetUpdateAfterBindSampledImages()) || count > Integer.toUnsignedLong(
				indexing.maxPerStageUpdateAfterBindResources()) || (long) this.layout.descriptorCount() * this.poolCapacity > Integer.toUnsignedLong(
				indexing.maxUpdateAfterBindDescriptorsInAllPools())) {
				throw new IllegalArgumentException(
					"sampler heap exceeds device descriptor indexing limits: " + count + " descriptors, " + this.poolCapacity + " generations");
			}
		}
	}

	public void retainLayouts() {
		this.requireOpen();
		int references;

		do {
			references = this.layoutReferences.get();

			if (references == 0) {
				throw new IllegalStateException("sampler heap is closed");
			}
		} while (!this.layoutReferences.compareAndSet(references, references + 1));
	}

	public void releaseLayouts() {
		if (this.layoutReferences.getAndUpdate(references -> Math.max(0, references - 1)) != 1) {
			return;
		}

		VK10.vkDestroyDescriptorSetLayout(this.device, this.setLayout, null);
		if (this.drawSetLayout != 0) {
			VK10.vkDestroyDescriptorSetLayout(this.device, this.drawSetLayout, null);
		}
		if (this.emptySetLayout != 0) {
			VK10.vkDestroyDescriptorSetLayout(this.device, this.emptySetLayout, null);
		}
		this.setLayout = this.drawSetLayout = this.emptySetLayout = 0;
	}

	public LongBuffer pipelineSetLayouts(MemoryStack stack, long uniforms, long frame) {
		if (this.drawSetLayout != 0) {
			return stack.longs(uniforms, this.setLayout, frame == 0 ? this.emptySetLayout : frame, this.drawSetLayout);
		}

		return frame == 0 ? stack.longs(uniforms, this.setLayout) : stack.longs(uniforms, this.setLayout, frame);
	}

	public SamplerHeapLayout layout() {
		return this.layout;
	}

	public Snapshot snapshot(Map<SamplerHeapLayout.Slot, ? extends Entry> images) {
		this.requireOpen();

		Map<SamplerHeapLayout.Slot, Entry> copy = Map.copyOf(images);

		for (var entry : copy.entrySet()) {
			this.layout.requireSlot(entry.getKey());

			if (this.layout.isDraw(entry.getKey()) || entry.getKey().type().texelBuffer() != (entry.getValue() instanceof TexelBuffer)) {
				throw new IllegalArgumentException("descriptor does not match heap type: " + entry.getKey());
			}
		}

		try (var stack = MemoryStack.stackPush()) {
			if (this.poolSizes.get(this.pool) == this.poolCapacity) {
				this.pool = this.poolSizes.entrySet().stream().filter(entry -> entry.getValue() < this.poolCapacity)
					.mapToLong(Map.Entry::getKey).findFirst().orElseGet(() -> this.createPool(stack));
			}

			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(this.device,
				VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(this.pool)
					.pSetLayouts(stack.longs(this.setLayout)), handle), "allocate sampler heap generation");

			long set = handle.get(0);

			try {
				if (!this.layout.heaps().isEmpty()) {
					var writes = this.writeDescriptors(stack, this.layout.heaps(), copy);
					writes.forEach(write -> write.dstSet(set));
					VK10.vkUpdateDescriptorSets(this.device, writes, null);
				}

				this.liveSnapshots++;
				this.poolSizes.merge(this.pool, 1, Integer::sum);

				return new Snapshot(this.pool, set);
			} catch (RuntimeException | Error exception) {
				VK10.vkFreeDescriptorSets(this.device, this.pool, set);

				throw exception;
			}
		}
	}

	private VkWriteDescriptorSet.Buffer writeDescriptors(MemoryStack stack, Map<SamplerHeapLayout.Type, List<SamplerHeapLayout.Slot>> heaps,
		Map<SamplerHeapLayout.Slot, Entry> entries) {
		var writes = VkWriteDescriptorSet.calloc(heaps.size(), stack);
		int index = 0;

		for (var entry : heaps.entrySet()) {
			var slots = entry.getValue();
			var fallback = this.dummies.get(entry.getKey()).entry();
			var write = writes.get(index++).sType$Default().dstBinding(entry.getKey().binding())
				.descriptorType(descriptorType(entry.getKey())).descriptorCount(slots.size());

			if (entry.getKey().texelBuffer()) {
				var views = stack.mallocLong(slots.size());
				for (int i = 0; i < slots.size(); i++) {
					views.put(i, ((TexelBuffer) entries.getOrDefault(slots.get(i), fallback)).bufferView());
				}
				write.pTexelBufferView(views);
			} else {
				var images = VkDescriptorImageInfo.calloc(slots.size(), stack);
				for (int i = 0; i < slots.size(); i++) {
					var image = (ImageSampler) entries.getOrDefault(slots.get(i), fallback);
					images.get(i).imageView(image.imageView()).sampler(image.sampler()).imageLayout(image.imageLayout());
				}
				write.pImageInfo(images);
			}
		}

		return writes;
	}

	@Override
	public void close() {
		if (this.pool == 0) {
			return;
		}

		if (this.liveSnapshots != 0) {
			throw new IllegalStateException("sampler heap still has live descriptor generations");
		}

		this.poolSizes.keySet().forEach(handle -> VK10.vkDestroyDescriptorPool(this.device, handle, null));
		this.poolSizes.clear();
		this.pool = 0;
		this.releaseLayouts();
		this.samplers.values().forEach(sampler -> VK10.vkDestroySampler(this.device, sampler, null));
		this.samplers.clear();
		this.dummies.values().forEach(VulkanDummySampler::close);
		this.dummies.clear();
	}

	private void requireOpen() {
		if (this.pool == 0) {
			throw new IllegalStateException("sampler heap is closed");
		}
	}

	public long sampler(GlSampler sampler) {
		return this.sampler(sampler.description());
	}

	public long sampler(GlSampler.Description description) {
		this.requireOpen();

		return this.samplers.computeIfAbsent(description, this::createSampler);
	}

	private long createSampler(GlSampler.Description state) {
		try (var stack = MemoryStack.stackPush()) {
			int address = state.clamp() ? VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
			int filter = state.linear() ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST;
			var info = VkSamplerCreateInfo.calloc(stack).sType$Default().minFilter(filter).magFilter(filter).mipmapMode(
					state.mipmapped() && state.linearMips() ? VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR : VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(address).addressModeV(address).addressModeW(address).maxLod(state.mipmapped() ? VK10.VK_LOD_CLAMP_NONE : 0)
				.compareEnable(state.hardwareShadow()).compareOp(VK10.VK_COMPARE_OP_LESS_OR_EQUAL);
			var handle = stack.mallocLong(1);
			int result = VK10.vkCreateSampler(this.device, info, null, handle);

			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException("could not create Iris sampler: " + result);
			}

			return handle.get(0);
		}
	}

	public sealed interface Entry permits ImageSampler, TexelBuffer {
	}

	public record TexelBuffer(long bufferView) implements Entry {
		public TexelBuffer {
			if (bufferView == 0) {
				throw new IllegalArgumentException("texel heap entries require a buffer view");
			}
		}
	}

	public record ImageSampler(long imageView, long sampler, int imageLayout) implements Entry {
		public ImageSampler {
			if (imageView == 0 || sampler == 0) {
				throw new IllegalArgumentException("sampler heap entries require an image view and sampler");
			}

			if (imageLayout != VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL && imageLayout != VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL && imageLayout != VK10.VK_IMAGE_LAYOUT_GENERAL) {
				throw new IllegalArgumentException("unsupported sampled image layout: " + imageLayout);
			}
		}
	}

	public class Snapshot implements AutoCloseable {
		private final Snapshot parent;
		private final Map<SamplerHeapLayout.Slot, Entry> draws;
		private final long descriptorPool;
		private long set;
		private int references = 1;

		private Snapshot(long descriptorPool, long set) {
			this.descriptorPool = descriptorPool;
			this.set = set;
			this.parent = null;
			this.draws = Map.of();
		}

		private Snapshot(Snapshot parent, Map<SamplerHeapLayout.Slot, Entry> draws) {
			this.parent = parent.retain();
			this.descriptorPool = parent.descriptorPool;
			this.set = parent.set;
			this.draws = Map.copyOf(draws);
		}

		public Snapshot withDraws(Map<SamplerHeapLayout.Slot, Entry> draws) {
			for (var entry : draws.entrySet()) {
				if (!VulkanSamplerHeap.this.layout.isDraw(entry.getKey()) || entry.getKey().type()
					.texelBuffer() != (entry.getValue() instanceof TexelBuffer)) {
					throw new IllegalArgumentException("invalid per-draw sampler: " + entry.getKey());
				}
			}

			return draws.isEmpty() ? this.retain() : new Snapshot(this, draws);
		}

		public Snapshot retain() {
			if (this.set == 0) {
				throw new IllegalStateException("retired frame heap");
			}

			this.references++;

			return this;
		}

		public SamplerHeapLayout layout() {
			return VulkanSamplerHeap.this.layout;
		}

		public void bind(VkCommandBuffer commandBuffer, long pipelineLayout, int bindPoint) {
			if (this.set == 0) {
				throw new IllegalStateException("sampler heap generation is retired");
			}

			if (commandBuffer.getDevice() != VulkanSamplerHeap.this.device) {
				throw new IllegalArgumentException("command buffer belongs to another device");
			}

			try (var stack = MemoryStack.stackPush()) {
				VK10.vkCmdBindDescriptorSets(commandBuffer, bindPoint, pipelineLayout, SamplerHeapLayout.DESCRIPTOR_SET,
					stack.longs(this.set), null);
				if (!VulkanSamplerHeap.this.layout.drawHeaps().isEmpty()) {
					var writes = VulkanSamplerHeap.this.writeDescriptors(stack, VulkanSamplerHeap.this.layout.drawHeaps(), this.draws);
					KHRPushDescriptor.vkCmdPushDescriptorSetKHR(commandBuffer, bindPoint, pipelineLayout,
						SamplerHeapLayout.DRAW_DESCRIPTOR_SET, writes);
				}
			}
		}

		@Override
		public void close() {
			if (this.set == 0) {
				return;
			}

			if (--this.references != 0) {
				return;
			}

			if (this.parent != null) {
				this.parent.close();
				this.set = 0;
				return;
			}

			check(VK10.vkFreeDescriptorSets(VulkanSamplerHeap.this.device, this.descriptorPool, this.set),
				"retire sampler heap generation");
			VulkanSamplerHeap.this.poolSizes.merge(this.descriptorPool, -1, Integer::sum);
			this.set = 0;
			VulkanSamplerHeap.this.liveSnapshots--;
		}
	}
}
