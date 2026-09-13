package net.irisshaders.iris.samplers;

import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanSamplerHeap;
import org.lwjgl.vulkan.VK10;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class PackSamplers implements AutoCloseable {
	private final GpuDevice device;
	private final Supplier<VulkanDevice> backend;
	private final Map<Key, Reference> references = new LinkedHashMap<>();
	private final Map<Reference, SamplerHeapLayout.Slot> slots = new HashMap<>();
	private SamplerHeapLayout layout;
	private VulkanSamplerHeap heap;
	private boolean closed;
	private static final Set<PackSamplers> active = Collections.newSetFromMap(new IdentityHashMap<>());
	private VulkanSamplerHeap.Snapshot frame;
	private final Map<SamplerHeapLayout.Type, Map<VulkanSamplerHeap.Entry, Integer>> frameEntries = new EnumMap<>(
		SamplerHeapLayout.Type.class);
	private int frames;

	public int frames() {
		return frames;
	}

	public static void flushAll() {
		for (var pack : List.copyOf(active)) {
			pack.flush();
		}
	}

	public void flush() {
		if (frame != null) {
			frame.flush();
		}
	}

	public void beginFrame() {
		flush();

		if (frame != null) {
			var previous = frame;
			VulkanCommandBridge.retire(device.createCommandEncoder(), previous::close);
			frame = null;
		}

		frameEntries.clear();
	}

	public VulkanSamplerHeap.Snapshot frame() {
		if (frame == null) {
			frame = heap().snapshot(Map.of());
			frames++;
			active.add(this);
		}

		return frame;
	}

	public int frameIndex(SamplerHeapLayout.Type type, VulkanSamplerHeap.Entry entry) {
		var entries = frameEntries.computeIfAbsent(type, ignored -> new HashMap<>());
		var previous = entries.get(entry);

		if (previous != null) {
			return previous;
		}

		int index = entries.size();
		var slots = layout().heaps().get(type);

		if (index >= slots.size()) {
			throw new IllegalStateException("Frame sampler heap capacity exceeded for " + type);
		}

		frame().append(slots.get(index), entry);
		entries.put(entry, index);

		return index;
	}

	public PackSamplers(GpuDevice device) {
		this.device = Objects.requireNonNull(device);
		this.backend = () -> VulkanCommandBridge.device(device);
	}

	public PackSamplers(GpuDevice device, VulkanDevice backend) {
		this.device = Objects.requireNonNull(device);
		this.backend = () -> backend;
	}

	public VulkanDevice backend() {
		return backend.get();
	}

	public long uniformAlignment() {
		return device.getDeviceInfo().limits().minUniformOffsetAlignment();
	}

	public Reference register(SamplerHeapLayout.Type type, Object resource) {
		if (closed) {
			throw new IllegalStateException("Pack samplers are closed");
		}

		var key = new Key(type, resource);
		var existing = references.get(key);

		if (existing != null) {
			return existing;
		}

		if (layout != null) {
			throw new IllegalStateException("Sampler discovery finished before resource registration: " + resource);
		}

		var reference = new Reference(type, "resource/" + references.size());
		references.put(key, reference);

		return reference;
	}

	public SamplerHeapLayout layout() {
		if (closed) {
			throw new IllegalStateException("Pack samplers are closed");
		}

		if (layout == null) {
			var builder = new SamplerHeapLayout.Builder();

			for (var ref : references.values()) {
				builder.add(ref.type(), ref.resource(), "iris");
			}

			var types = EnumSet.noneOf(SamplerHeapLayout.Type.class);
			references.values().forEach(ref -> types.add(ref.type()));

			for (var type : types) {
				int capacity = type == SamplerHeapLayout.Type.SAMPLER2D || type.texelBuffer() ? 4096 : 256;

				for (int i = 0; i < capacity; i++) {
					builder.add(type, "frame/" + i, "iris");
				}
			}

			layout = builder.build();
		}

		return layout;
	}

	public VulkanSamplerHeap heap() {
		if (closed) {
			throw new IllegalStateException("Pack samplers are closed");
		}

		if (heap == null) {
			var backend = backend();
			heap = new VulkanSamplerHeap(backend.vkDevice(), layout(), 16);

		}

		return heap;
	}

	public SamplerHeapLayout.Slot slot(Reference reference) {
		var layout = layout();

		return slots.computeIfAbsent(reference, ref -> layout.slot(ref.type(), ref.resource(), "iris"));
	}

	public VulkanSamplerHeap.Snapshot snapshot(Map<Reference, TextureBinding> bindings) {
		return snapshot(bindings, Map.of());
	}

	public VulkanSamplerHeap.Snapshot snapshot(Map<Reference, TextureBinding> bindings, Map<Reference, Long> texels) {
		return heap().snapshot(resolve(bindings, texels));
	}

	public Map<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry> resolve(Map<Reference, TextureBinding> bindings,
		Map<Reference, Long> texels) {
		heap();

		var entries = new LinkedHashMap<SamplerHeapLayout.Slot, VulkanSamplerHeap.Entry>();
		texels.forEach((reference, view) -> entries.put(slot(reference), new VulkanSamplerHeap.TexelBuffer(view)));

		for (var entry : bindings.entrySet()) {
			entries.put(slot(entry.getKey()), resolve(entry.getKey().type(), entry.getValue()));
		}

		return entries;
	}

	public VulkanSamplerHeap.ImageSampler resolve(SamplerHeapLayout.Type type, TextureBinding binding) {
		heap();

		if (binding.image() != null) {
			var image = binding.image().get();

			return new VulkanSamplerHeap.ImageSampler(image.view(), heap.sampler(binding.sampler().get()), VK10.VK_IMAGE_LAYOUT_GENERAL);
		}

		var view = binding.texture().get();

		if (!(view instanceof VulkanGpuTextureView nativeView) || view.isClosed() || view.texture().isClosed() ||
			(view.texture().usage() & GpuTexture.USAGE_TEXTURE_BINDING) == 0) {
			throw new IllegalStateException("Missing or closed Vulkan sampler texture: " + binding.key());
		}

		var state = binding.sampler().get().description();
		boolean comparison = type.glsl.endsWith("Shadow");

		if (comparison && !view.texture().getFormat().hasDepthAspect()) {
			throw new IllegalArgumentException("Comparison sampler requires depth: " + binding.key());
		}

		var typedState = new GlSampler.Description(state.linear(), state.linearMips(), state.mipmapped(), comparison, state.clamp());
		long sampler;

		if (binding.nativeSampler() != null) {
			if (comparison) {
				throw new IllegalArgumentException("External comparison sampler requires an Iris sampler description");
			}

			var borrowed = binding.nativeSampler().get();

			if (borrowed == null) {
				sampler = heap.sampler(typedState);
			} else if (borrowed instanceof VulkanGpuSampler nativeSampler && !borrowed.isClosed()) {
				sampler = nativeSampler.vkSampler();
			} else {
				throw new IllegalStateException("Missing or closed Vulkan sampler: " + binding.key());
			}
		} else {
			sampler = heap.sampler(typedState);
		}

		return new VulkanSamplerHeap.ImageSampler(RenderTarget.sampledView(backend.get(), nativeView), sampler,
			VK10.VK_IMAGE_LAYOUT_GENERAL);
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		flush();

		if (frame != null) {
			var previous = frame;
			VulkanCommandBridge.retire(device.createCommandEncoder(), previous::close);
			frame = null;
		}

		active.remove(this);
		closed = true;

		if (heap != null) {
			var encoder = device.createCommandEncoder();
			VulkanCommandBridge.retire(encoder, heap::close);
		}
	}

	private record Key(SamplerHeapLayout.Type type, Object resource) {
	}

	public record Reference(SamplerHeapLayout.Type type, String resource) {
	}
}
