package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.mixin.VulkanCommandEncoderAccessor;
import net.irisshaders.iris.pipeline.transform.VulkanPushConstants;
import net.irisshaders.iris.samplers.SamplerBindingPlan.DescriptorIndices;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.samplers.VulkanHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;

public class VulkanSamplerHeap implements AutoCloseable {
	private final VkDevice device;
	private final Object2LongMap<GlSampler.Description> samplers = new Object2LongOpenHashMap<>();
	private final VulkanHeapLayout layout;
	private int imageCount;
	private final Long2IntMap samplerIndices = new Long2IntOpenHashMap();
	private final List<Supplier<GpuBufferSlice>> buffers = new ArrayList<>();
	private VulkanStorageBuffer mipmapCounter;
	private int mipmapCounterIndex;
	private final Frame[] frames = new Frame[VulkanHeapLayout.FRAMES_IN_FLIGHT];
	private long setLayout;
	private long graphicsLayout;
	private long computeLayout;
	private final Object2LongMap<List<VulkanShaderBindings.LayoutBinding>> drawLayouts = new Object2LongOpenHashMap<>();
	private volatile long pool;
	private boolean closed;

	public VulkanSamplerHeap(VulkanDevice device) {
		this.device = device.vkDevice();
		try (var stack = MemoryStack.stackPush()) {
			this.layout = selectLayout(this.device, stack);
			Iris.logger.info("Using Vulkan heap: {} samplers, {} images, {} buffers", this.layout.samplers(), this.layout.images(), this.layout.buffers());
			int[] types = {VK10.VK_DESCRIPTOR_TYPE_SAMPLER, VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE,
				VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER};
			int[] counts = {this.layout.samplers(), this.layout.images(), this.layout.buffers(), 15};
			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorSetLayout(this.device, layoutInfo(this.layout, stack), null, handle), "create global heap layout");
			this.setLayout = handle.get(0);
			this.graphicsLayout = this.createPipelineLayout(VK10.VK_SHADER_STAGE_ALL_GRAPHICS, stack);
			this.computeLayout = this.createPipelineLayout(VK10.VK_SHADER_STAGE_COMPUTE_BIT, stack);
			var sizes = VkDescriptorPoolSize.calloc(types.length, stack);
			for (int i = 0; i < types.length; i++) {
				sizes.get(i).type(types[i]).descriptorCount(counts[i] * frames.length);
			}
			check(VK10.vkCreateDescriptorPool(this.device, VkDescriptorPoolCreateInfo.calloc(stack).sType$Default()
				.flags(VK12.VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT).maxSets(frames.length).pPoolSizes(sizes), null, handle),
				"create global heap pool");
			this.pool = handle.get(0);
			var sets = stack.mallocLong(frames.length);
			var layouts = stack.mallocLong(frames.length);
			for (int i = 0; i < frames.length; i++) layouts.put(i, this.setLayout);
			check(VK10.vkAllocateDescriptorSets(this.device, VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
				.descriptorPool(this.pool).pSetLayouts(layouts), sets), "allocate frame heaps");
			for (int i = 0; i < frames.length; i++) this.frames[i] = new Frame(sets.get(i));
			this.mipmapCounter = new VulkanStorageBuffer(device, "Iris mipmap counter", 4, null);
			this.mipmapCounterIndex = this.addBuffer(this.mipmapCounter::slice);
		} catch (RuntimeException | Error failure) {
			this.close();
			throw failure;
		}
	}

	public VulkanHeapLayout layout() {
		return this.layout;
	}

	public VulkanStorageBuffer mipmapCounter() {
		return this.mipmapCounter;
	}

	public int mipmapCounterIndex() {
		return this.mipmapCounterIndex;
	}

	public static VkDescriptorSetLayoutCreateInfo layoutInfo(VulkanHeapLayout layout, MemoryStack stack) {
		var bindings = VkDescriptorSetLayoutBinding.calloc(4, stack);
		bindings.get(0).binding(VulkanHeapLayout.SAMPLERS_BINDING).descriptorType(VK10.VK_DESCRIPTOR_TYPE_SAMPLER)
			.descriptorCount(layout.samplers()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
		bindings.get(1).binding(VulkanHeapLayout.IMAGES_BINDING).descriptorType(VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE)
			.descriptorCount(layout.images()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
		bindings.get(2).binding(VulkanHeapLayout.BUFFERS_BINDING).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
			.descriptorCount(layout.buffers()).stageFlags(VK10.VK_SHADER_STAGE_ALL);
		// our frame UBO! I actually only need 1, but I intentionally bumped it to 15 here just in case a few mods want to eat some, and it stays under the GTX limit
		bindings.get(3).binding(VulkanHeapLayout.FRAME_UNIFORMS_BINDING).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(15).stageFlags(VK10.VK_SHADER_STAGE_ALL);
		int flags = VK12.VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT | VK12.VK_DESCRIPTOR_BINDING_UPDATE_UNUSED_WHILE_PENDING_BIT
			| VK12.VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT;
		return VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings)
			.flags(VK12.VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT)
			.pNext(VkDescriptorSetLayoutBindingFlagsCreateInfo.calloc(stack).sType$Default().pBindingFlags(stack.ints(flags, flags, flags,
				VK12.VK_DESCRIPTOR_BINDING_UPDATE_UNUSED_WHILE_PENDING_BIT | VK12.VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT)));
	}

	public static VulkanHeapLayout selectLayout(VkDevice device, MemoryStack stack) {
		var push = VkPhysicalDevicePushDescriptorPropertiesKHR.calloc(stack).sType$Default();
		var indexing = VkPhysicalDeviceDescriptorIndexingProperties.calloc(stack).sType$Default().pNext(push.address());
		var properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(indexing);
		VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(), properties);
		var preferred = maximumLayout(indexing, push.maxPushDescriptors());
		if (supportsLayout(device, preferred, stack)) return preferred;

		VulkanHeapLayout selected = null;
		int low = 1;
		int high = preferred.images() - 1;
		while (low <= high) {
			int images = (low + high) / 2;
			var candidate = new VulkanHeapLayout(preferred.samplers(), images, preferred.buffers());
			if (supportsLayout(device, candidate, stack)) {
				selected = candidate;
				low = images + 1;
			} else {
				high = images - 1;
			}
		}
		if (selected == null) throw new IllegalArgumentException("device cannot support Iris's frame descriptor heap layout");
		return selected;
	}

	public static VulkanHeapLayout maximumLayout(VkPhysicalDeviceDescriptorIndexingProperties indexing, int pushDescriptors) {
		long draw = Integer.toUnsignedLong(pushDescriptors);
		int samplers = VulkanHeapLayout.SAMPLER_COUNT;
		long samplerLimit = Math.min(Integer.toUnsignedLong(indexing.maxPerStageDescriptorUpdateAfterBindSamplers()),
			Integer.toUnsignedLong(indexing.maxDescriptorSetUpdateAfterBindSamplers()));
		int buffers = (int) Math.min(VulkanHeapLayout.BUFFER_COUNT,
			Math.min(Integer.toUnsignedLong(indexing.maxPerStageDescriptorUpdateAfterBindStorageBuffers()),
				Integer.toUnsignedLong(indexing.maxDescriptorSetUpdateAfterBindStorageBuffers())));
		long resources = Math.min(Integer.toUnsignedLong(indexing.maxPerStageUpdateAfterBindResources()) - draw,
			Integer.toUnsignedLong(indexing.maxUpdateAfterBindDescriptorsInAllPools()) / VulkanHeapLayout.FRAMES_IN_FLIGHT);
		long sampledImages = Math.min(Integer.toUnsignedLong(indexing.maxPerStageDescriptorUpdateAfterBindSampledImages()),
			Integer.toUnsignedLong(indexing.maxDescriptorSetUpdateAfterBindSampledImages())) - draw;
		// WHY DOES THIS LIMIT EXIST???
		int images = (int) Math.min(VulkanHeapLayout.IMAGE_COUNT, Math.min(sampledImages, resources - samplers - buffers - 1));
		if (samplerLimit < samplers + draw || buffers < 128 || images < 1) {
			throw new IllegalArgumentException("device cannot support Iris's frame descriptor heap");
		}
		return new VulkanHeapLayout(samplers, images, buffers);
	}

	private static boolean supportsLayout(VkDevice device, VulkanHeapLayout layout, MemoryStack stack) {
		var support = VkDescriptorSetLayoutSupport.calloc(stack).sType$Default();
		VK11.vkGetDescriptorSetLayoutSupport(device, layoutInfo(layout, stack), support);
		return support.supported();
	}

	public LongBuffer pipelineSetLayouts(MemoryStack stack, long draw) {
		return draw == 0 ? stack.longs(this.setLayout) : stack.longs(this.setLayout, draw);
	}

	private long createPipelineLayout(int stages, MemoryStack stack) {
		var handle = stack.mallocLong(1);
		check(VK10.vkCreatePipelineLayout(this.device, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
			.pSetLayouts(stack.longs(this.setLayout))
			.pPushConstantRanges(VkPushConstantRange.calloc(1, stack).stageFlags(stages).offset(0).size(VulkanPushConstants.MAX_SIZE)),
			null, handle), "create heap pipeline layout");
		return handle.get(0);
	}

	public void beginFrame(GpuBufferSlice uniforms) {
		var frame = this.frame();
		if (uniforms != null) {
			frame.write(new DescriptorWrite(0, VulkanHeapLayout.FRAME_UNIFORMS_BINDING, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
				0, 0, 0, uniforms));
		}
		frame.flushDescriptors();
	}

	public void bind(VkCommandBuffer command) {
		var frame = this.frame();
		frame.flushDescriptors();
		try (var stack = MemoryStack.stackPush()) {
			var sets = stack.longs(frame.set);
			VK10.vkCmdBindDescriptorSets(command, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, this.graphicsLayout,
				VulkanHeapLayout.DESCRIPTOR_SET, sets, null);
			VK10.vkCmdBindDescriptorSets(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.computeLayout,
				VulkanHeapLayout.DESCRIPTOR_SET, sets, null);
		}
	}

	public Frame frame() {
		long submission = ((VulkanCommandEncoderAccessor) VulkanCommandBridge.encoder()).iris$getCurrentSubmitIndex();
		var frame = this.frames[Math.floorMod(submission, this.frames.length)];
		frame.begin(submission);
		return frame;
	}

	public long drawLayout(List<net.irisshaders.iris.pipeline.transform.ShaderResourceLayout.Descriptor> descriptors) {
		var key = descriptors.stream().filter(d -> d.set() == SamplerHeapLayout.DRAW_DESCRIPTOR_SET)
			.map(d -> new VulkanShaderBindings.LayoutBinding(d.binding(), VulkanShaderBindings.descriptorType(d), d.count(),
				VulkanShaderBindings.stageFlags(d)))
			.sorted(java.util.Comparator.comparingInt(VulkanShaderBindings.LayoutBinding::binding)).toList();
		if (key.isEmpty()) return 0;
		return this.drawLayouts.computeLongIfAbsent(key, unused -> {
			try (var stack = MemoryStack.stackPush()) {
				var push = VkPhysicalDevicePushDescriptorPropertiesKHR.calloc(stack).sType$Default();
				VK11.vkGetPhysicalDeviceProperties2(this.device.getPhysicalDevice(), VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(push));
				if (key.stream().mapToInt(VulkanShaderBindings.LayoutBinding::count).sum() > push.maxPushDescriptors()) {
					throw new IllegalArgumentException("draw descriptors exceed maxPushDescriptors: " + key);
				}
				var bindings = VkDescriptorSetLayoutBinding.calloc(key.size(), stack);
				for (int i = 0; i < key.size(); i++) {
					var binding = key.get(i);
					bindings.get(i).binding(binding.binding()).descriptorType(binding.type()).descriptorCount(binding.count()).stageFlags(binding.stages());
				}
				var handle = stack.mallocLong(1);
				check(VK10.vkCreateDescriptorSetLayout(this.device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default()
					.flags(KHRPushDescriptor.VK_DESCRIPTOR_SET_LAYOUT_CREATE_PUSH_DESCRIPTOR_BIT_KHR).pBindings(bindings), null, handle), "create draw push-descriptor layout");
				return handle.get(0);
			}
		});
	}

	public List<VulkanShaderBindings.LayoutBinding> drawLayoutKey(long layout) {
		return this.drawLayouts.object2LongEntrySet().stream().filter(entry -> entry.getLongValue() == layout)
			.map(Map.Entry::getKey).findFirst().orElse(List.of());
	}

	/** Indices stay assigned until this dimension's heap is destroyed. */
	public int addImage(ImageSampler image) {
		if (this.imageCount == this.layout.images()) {
			throw new IllegalArgumentException("shader pack exceeds Vulkan image heap capacity: " + this.layout.images());
		}
		int index = this.imageCount++;
		this.frame().writeImage(index, image);
		return index;
	}

	public int addSampler(long sampler) {
		int index = this.samplerIndices.getOrDefault(sampler, -1);
		if (index >= 0) return index;
		index = this.samplerIndices.size();
		if (index == this.layout.samplers()) {
			throw new IllegalArgumentException("shader pack exceeds Vulkan sampler heap capacity: " + this.layout.samplers());
		}
		this.samplerIndices.put(sampler, index);
		this.frame().writeSampler(index, sampler);
		return index;
	}

	public int addBuffer(ShaderStorageBuffer buffer) {
		return this.addBuffer(buffer::slice);
	}

	public int addBuffer(Supplier<GpuBufferSlice> buffer) {
		int index = this.buffers.size();
		if (index == this.layout.buffers()) {
			throw new IllegalArgumentException("shader pack exceeds Vulkan buffer heap capacity: " + this.layout.buffers());
		}
		this.buffers.add(buffer);
		this.frame().writeBuffer(index, buffer.get());
		return index;
	}

	public void update(DescriptorIndices indices, ImageSampler sampled) {
		var frame = this.frame();
		frame.writeImage(indices.image(), sampled);
		frame.writeSampler(indices.sampler(), sampled.sampler());
	}

	@Override
	public void close() {
		if (this.closed) return;
		this.closed = true;
		if (this.mipmapCounter != null) this.mipmapCounter.close();
		if (this.pool != 0) {
			VK10.vkDestroyDescriptorPool(this.device, this.pool, null);
			this.pool = 0;
		}
		this.samplers.values().forEach(sampler -> VK10.vkDestroySampler(this.device, sampler, null));
		this.samplers.clear();
		VK10.vkDestroyPipelineLayout(this.device, this.graphicsLayout, null);
		VK10.vkDestroyPipelineLayout(this.device, this.computeLayout, null);
		this.graphicsLayout = this.computeLayout = 0;
		VK10.vkDestroyDescriptorSetLayout(this.device, this.setLayout, null);
		this.drawLayouts.values().forEach(layout -> VK10.vkDestroyDescriptorSetLayout(this.device, layout, null));
		this.drawLayouts.clear();
		this.setLayout = 0;
	}

	public long sampler(GlSampler sampler) {
		return this.sampler(sampler.description());
	}

	public long sampler(GlSampler.Description description) {
		return this.samplers.computeLongIfAbsent(description, this::createSampler);
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
			check(VK10.vkCreateSampler(this.device, info, null, handle), "create Iris sampler");

			return handle.get(0);
		}
	}

	public sealed interface Entry permits ImageSampler, TexelBuffer {
	}

	public record TexelBuffer(long bufferView) implements Entry {
	}

	public record ImageSampler(long imageView, long sampler, int imageLayout) implements Entry {
	}


	public record DescriptorWrite(int index, int binding, int type, long view, long sampler, int layout, GpuBufferSlice buffer) { }

	public class Frame {
		private final long set;
		private final Long2ObjectMap<DescriptorWrite> pending = new Long2ObjectLinkedOpenHashMap<>();
		private final Long2ObjectMap<DescriptorWrite> written = new Long2ObjectLinkedOpenHashMap<>();
		private long submission = -1;

		private Frame(long set) {
			this.set = set;
		}

		private void begin(long submission) {
			if (this.submission == submission) return;
			this.submission = submission;
			this.pending.clear();
			for (int i = 0; i < buffers.size(); i++) {
				this.writeBuffer(i, buffers.get(i).get());
			}
		}

		public void writeBuffer(int index, GpuBufferSlice slice) {
			this.write(new DescriptorWrite(index, VulkanHeapLayout.BUFFERS_BINDING, VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
				0, 0, 0, slice));
		}

		private void writeImage(int index, ImageSampler sampled) {
			this.write(new DescriptorWrite(index, VulkanHeapLayout.IMAGES_BINDING, VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE,
				sampled.imageView(), 0, sampled.imageLayout(), null));
		}

		private void writeSampler(int index, long sampler) {
			this.write(new DescriptorWrite(index, VulkanHeapLayout.SAMPLERS_BINDING, VK10.VK_DESCRIPTOR_TYPE_SAMPLER,
				0, sampler, 0, null));
		}

		private void write(DescriptorWrite write) {
			long slot = (long) write.binding() << 32 | Integer.toUnsignedLong(write.index());
			if (write.equals(this.written.get(slot))) {
				this.pending.remove(slot);
			} else {
				this.pending.put(slot, write);
			}
		}

		public void flushDescriptors() {
			if (this.pending.isEmpty()) return;
			int count = this.pending.size();
			int bufferCount = 0;
			for (var entry : this.pending.values()) {
				if (entry.buffer() != null) bufferCount++;
			}
			int imageCount = count - bufferCount;
			int writeBytes = count * VkWriteDescriptorSet.SIZEOF;
			int imageBytes = imageCount * VkDescriptorImageInfo.SIZEOF;
			var allocation = MemoryUtil.memCalloc(writeBytes + imageBytes + bufferCount * VkDescriptorBufferInfo.SIZEOF);
			try {
				long address = MemoryUtil.memAddress(allocation);
				var writes = VkWriteDescriptorSet.create(address, count);
				var images = VkDescriptorImageInfo.create(address + writeBytes, imageCount);
				var buffers = VkDescriptorBufferInfo.create(address + writeBytes + imageBytes, bufferCount);
				int i = 0;
				int imageIndex = 0;
				int bufferIndex = 0;
				for (var entry : this.pending.values()) {
					var write = writes.get(i).sType$Default().dstSet(this.set).dstBinding(entry.binding())
						.dstArrayElement(entry.index()).descriptorType(entry.type()).descriptorCount(1);
					if (entry.buffer() != null) {
						var slice = entry.buffer();
						buffers.get(bufferIndex).buffer(((VulkanGpuBuffer) slice.buffer()).vkBuffer()).offset(slice.offset()).range(slice.length());
						buffers.position(bufferIndex).limit(bufferIndex + 1);
						write.pBufferInfo(buffers);
						buffers.position(0).limit(buffers.capacity());
						bufferIndex++;
					} else {
						images.get(imageIndex).imageView(entry.view()).sampler(entry.sampler()).imageLayout(entry.layout());
						images.position(imageIndex).limit(imageIndex + 1);
						write.pImageInfo(images);
						images.position(0).limit(images.capacity());
						imageIndex++;
					}
					i++;
				}
				VK10.vkUpdateDescriptorSets(device, writes, null);
				this.written.putAll(this.pending);
				this.pending.clear();
			} finally {
				MemoryUtil.memFree(allocation);
			}
		}

	}
}
