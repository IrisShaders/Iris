package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.PackSamplers.Reference;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.vulkan.VK10;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VulkanPackResources implements PackResources {
	private final PackSamplers pack;
	private final Map<Map<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry>, VulkanSamplerHeap.Snapshot> snapshots = new HashMap<>();
	private VulkanSamplerHeap heap;
	private boolean closed;

	public VulkanPackResources(PackSamplers pack) {
		this.pack = pack;
	}

	@Override
	public void beginFrame() {
		this.retireSnapshots();
	}

	private void retireSnapshots() {
		if (!this.snapshots.isEmpty()) {
			var retired = List.copyOf(this.snapshots.values());
			VulkanCommandBridge.retire(RenderSystem.getDevice().createCommandEncoder(),
				() -> retired.forEach(VulkanSamplerHeap.Snapshot::close));
			this.snapshots.clear();
		}
	}

	public SamplerHeapLayout getLayout() {
		return this.pack.getLayout();
	}

	public VulkanSamplerHeap getSamplerHeap() {
		if (this.closed) {
			throw new IllegalStateException("pack samplers are closed");
		}

		if (this.heap == null) {
			var backend = VulkanCommandBridge.getDevice();
			this.heap = new VulkanSamplerHeap(backend, this.getLayout(), 64);
		}

		return this.heap;
	}

	public VulkanSamplerHeap.Snapshot createSnapshot(Map<Reference, TextureBinding.Resolved> bindings, Map<Reference, Long> texels) {
		var entries = this.resolve(bindings, texels);
		var draws = new LinkedHashMap<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry>();
		entries.entrySet().removeIf(entry -> {
			if (this.getLayout().isDraw(entry.getKey())) {
				draws.put(entry.getKey(), entry.getValue());
				return true;
			}
			return false;
		});
		var snapshot = this.snapshots.get(entries);

		if (snapshot == null) {
			var key = Map.copyOf(entries);
			snapshot = this.getSamplerHeap().snapshot(key);
			this.snapshots.put(key, snapshot);
		}
		return snapshot.withDraws(draws);
	}

	private Map<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry> resolve(Map<Reference, TextureBinding.Resolved> bindings,
		Map<Reference, Long> texels) {
		this.getSamplerHeap();

		var entries = new LinkedHashMap<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry>();
		texels.forEach((reference, view) -> entries.put(this.pack.getSlot(reference), new VulkanSamplerHeap.TexelBuffer(view)));

		for (var entry : bindings.entrySet()) {
			entries.put(this.pack.getSlot(entry.getKey()), this.resolve(entry.getValue()));
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
		if (this.closed) {
			return;
		}

		this.retireSnapshots();

		this.closed = true;

		if (this.heap != null) {
			var encoder = RenderSystem.getDevice().createCommandEncoder();
			VulkanCommandBridge.retire(encoder, this.heap::close);
		}
	}
}
