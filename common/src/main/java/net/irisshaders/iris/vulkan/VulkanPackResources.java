package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.PackSamplers.Reference;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.samplers.SamplerBindingPlan.DescriptorIndices;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.vulkan.VK10;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class VulkanPackResources implements PackResources {
	private final PackSamplers pack;
	private VulkanSamplerHeap heap;
	private boolean closed;
	private final Map<Reference, TextureBinding> textures = new LinkedHashMap<>();
	private final Map<Integer, ShaderStorageBuffer> storageBuffers = new TreeMap<>();
	private final Map<SamplerHeapLayout.Slot, DescriptorIndices> descriptors = new LinkedHashMap<>();
	private final Object2IntMap<ShaderStorageBuffer> buffers = new Object2IntLinkedOpenHashMap<>();

	public VulkanPackResources(PackSamplers pack) {
		this.pack = pack;
	}

	@Override
	public void beginFrame(GpuBufferSlice frameUniforms) {
		var heap = this.getSamplerHeap();
		((VulkanRenderBackend) Iris.getRenderBackend()).activeHeap = heap;
		heap.beginFrame(frameUniforms);
	}

	@Override
	public void register(ProgramSamplers resources) {
		for (var sampler : resources.getSamplers().values()) {
			if (sampler.texture() != null) this.textures.put(sampler.reference(), sampler.texture());
		}
		for (var descriptor : resources.getLayout().descriptors()) {
			if (descriptor.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER) continue;
			int first = descriptor.originalBinding() == null ? 0 : descriptor.originalBinding();
			for (int i = 0; i < descriptor.count(); i++) {
				int binding = Math.addExact(first, i);
				this.storageBuffers.put(binding, this.pack.getStorageBuffer(binding));
			}
		}
	}

	public SamplerHeapLayout getLayout() {
		return this.pack.getLayout();
	}

	public VulkanSamplerHeap getSamplerHeap() {
		if (this.heap == null) {
			this.heap = new VulkanSamplerHeap(VulkanCommandBridge.getDevice());
			var bindings = new LinkedHashMap<SamplerHeapLayout.Slot, TextureBinding>();
			this.textures.forEach((reference, binding) -> {
				var slot = this.pack.getSlot(reference);
				if (!this.getLayout().isDraw(slot)) bindings.put(slot, binding);
			});
			// Allocate the complete pack before compiling any program. Program preparation order must not affect its indices.
			var ordered = bindings.entrySet().stream().sorted(
				Comparator.<Map.Entry<SamplerHeapLayout.Slot, TextureBinding>>comparingInt(entry -> samplerOrder(entry.getKey(), entry.getValue()))
					.thenComparingInt(entry -> entry.getKey().type().ordinal()).thenComparingInt(entry -> entry.getKey().index())).toList();
			for (var entry : ordered) {
				var sampled = this.resolve(entry.getValue().resolve(entry.getKey().type()));
				this.descriptors.put(entry.getKey(), new DescriptorIndices(this.heap.addImage(sampled), this.heap.addSampler(sampled.sampler())));
			}
			this.storageBuffers.values().forEach(buffer -> this.buffers.put(buffer, this.heap.addBuffer(buffer)));
			this.heap.frame().flushDescriptors();
		}
		return this.heap;
	}

	private static GlSampler.Description description(SamplerHeapLayout.Slot slot, TextureBinding binding) {
		var state = binding.sampler().get().description();
		return new GlSampler.Description(state.linear(), state.linearMips(), state.mipmapped(), slot.type().glsl.endsWith("Shadow"), state.clamp());
	}

	private static int samplerOrder(SamplerHeapLayout.Slot slot, TextureBinding binding) {
		var state = description(slot, binding);
		return (state.linear() ? 1 : 0) | (state.linearMips() ? 2 : 0) | (state.mipmapped() ? 4 : 0)
			| (state.hardwareShadow() ? 8 : 0) | (state.clamp() ? 16 : 0);
	}

	public SamplerBindingPlan assign(ProgramSamplers resources) {
		var plan = resources.getBindingPlan();
		var heap = this.getSamplerHeap();
		var descriptors = new LinkedHashMap<SamplerHeapLayout.Slot, DescriptorIndices>();
		for (var slot : plan.slots()) {
			if (plan.heap().isDraw(slot)) continue;
			descriptors.put(slot, this.descriptors.get(slot));
		}
		return plan.withDescriptors(descriptors, heap.layout());
	}

	public Int2IntMap assignStorageBuffers(ProgramSamplers resources) {
		this.getSamplerHeap();
		var indices = new Int2IntLinkedOpenHashMap();
		for (var descriptor : resources.getLayout().descriptors()) {
			if (descriptor.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER) continue;
			int first = descriptor.originalBinding() == null ? 0 : descriptor.originalBinding();
			for (int i = 0; i < descriptor.count(); i++) {
				int binding = Math.addExact(first, i);
				var buffer = this.pack.getStorageBuffer(binding);
				indices.put(binding, this.buffers.getInt(buffer));
			}
		}
		return Int2IntMaps.unmodifiable(indices);
	}

	public void update(Map<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry> entries) {
		for (var entry : entries.entrySet()) {
			if (this.getLayout().isDraw(entry.getKey())) continue;
			this.heap.update(this.descriptors.get(entry.getKey()), (VulkanSamplerHeap.ImageSampler) entry.getValue());
		}
	}

	public Map<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry> resolve(ProgramSamplers samplers) {
		this.getSamplerHeap();
		var entries = new LinkedHashMap<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry>();
		for (var sampler : samplers.getSamplers().values()) {
			var reference = sampler.reference();
			entries.computeIfAbsent(this.pack.getSlot(reference), slot -> sampler.texture() == null
				? new VulkanSamplerHeap.TexelBuffer(sampler.texelBuffer().getAsLong())
				: this.resolve(sampler.texture().resolve(reference.type())));
		}
		return entries;
	}

	private VulkanSamplerHeap.ImageSampler resolve(TextureBinding.Resolved binding) {
		long view = binding.image() == null ? ((VulkanGpuTextureView) binding.texture()).vkImageView() : binding.image().getView();
		long sampler = binding.nativeSampler() == null ? this.heap.sampler(
			binding.sampler()) : ((VulkanGpuSampler) binding.nativeSampler()).vkSampler();
		return new VulkanSamplerHeap.ImageSampler(view, sampler, VK10.VK_IMAGE_LAYOUT_GENERAL);
	}

	@Override
	public void close() {
		if (this.closed) return;
		this.closed = true;
		this.descriptors.clear();
		this.buffers.clear();
		this.textures.clear();
		this.storageBuffers.clear();
		if (this.heap != null) {
			var backend = (VulkanRenderBackend) Iris.getRenderBackend();
			if (backend.activeHeap == this.heap) backend.activeHeap = null;
			this.heap.mipmapCounter().close();
			backend.retire(this.heap::close);
		}
	}
}
