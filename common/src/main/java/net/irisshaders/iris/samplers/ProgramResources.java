package net.irisshaders.iris.samplers;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import net.irisshaders.iris.vulkan.CompositeSynchronization;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanResourceSets;
import net.irisshaders.iris.vulkan.VulkanSamplerHeap;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class ProgramResources implements SamplerHolder, ImageHolder {
	private final PackSamplers pack;
	private final List<VulkanResourceLayout.Descriptor> storageDeclarations;
	private ShaderStorageBufferHolder storageBuffers;
	private final Map<PackSamplers.Reference, LongSupplier> texels = new LinkedHashMap<>();
	private final Map<String, VulkanResourceLayout.Descriptor> declarations = new LinkedHashMap<>();
	private final Map<String, PackSamplers.Reference> references = new LinkedHashMap<>();
	private final Map<PackSamplers.Reference, TextureBinding> textures = new LinkedHashMap<>();
	private final Map<String, VulkanResourceLayout.Descriptor> imageDeclarations = new LinkedHashMap<>();
	private final Map<String, LongSupplier> images = new LinkedHashMap<>();

	public Map<String, List<Long>> imageViews() {
		var result = new LinkedHashMap<String, List<Long>>();
		images.forEach((name, view) -> result.put(name, List.of(view.getAsLong())));

		return result;
	}

	private TextureBinding fallback;
	private IntFunction<TextureBinding> externalBindings;
	private ByteBuffer indices;
	private SamplerBindingPlan plan;
	private final Map<String, LongSupplier> imageResources = new LinkedHashMap<>();
	private SamplerResource[] samplerResources;

	public ProgramResources(PackSamplers pack, VulkanResourceLayout layout) {
		this.pack = pack;
		storageDeclarations = layout.descriptors().stream().filter(d -> d.kind() == VulkanResourceLayout.Kind.STORAGE_BUFFER).toList();

		for (var descriptor : layout.descriptors()) {
			if (descriptor.kind() == VulkanResourceLayout.Kind.IMAGE) {
				imageDeclarations.put(descriptor.name(), descriptor);
			}
		}

		for (var descriptor : layout.descriptors()) {
			if (descriptor.kind() == VulkanResourceLayout.Kind.SAMPLER) {
				declarations.put(descriptor.name(), descriptor);
			}
		}
	}

	public void addStorageBuffers(ShaderStorageBufferHolder buffers) {
		storageBuffers = buffers;
		bufferBindings();
	}

	public Map<String, List<GpuBufferSlice>> bufferBindings() {
		var result = new LinkedHashMap<String, List<GpuBufferSlice>>();

		for (var descriptor : storageDeclarations) {
			if (storageBuffers == null) {
				throw new IllegalArgumentException("No buffer objects for storage block " + descriptor.name());
			}

			int first = descriptor.originalBinding() == null ? 0 : descriptor.originalBinding();
			var slices = new ArrayList<GpuBufferSlice>();

			for (int i = 0; i < descriptor.count(); i++) {
				slices.add(storageBuffers.getBuffer(Math.addExact(first, i)).slice());
			}

			result.put(descriptor.name(), List.copyOf(slices));
		}

		return result;
	}

	@Override
	public boolean hasSampler(String name) {
		return declarations.containsKey(name);
	}

	@Override
	public boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		if (defaultSampler) {
			fallback = binding;
		}

		boolean used = false;

		for (String name : names) {
			var declaration = declarations.get(name);

			if (declaration == null) {
				continue;
			}

			if (declaration.count() != 1) {
				throw new UnsupportedOperationException("Sampler array registration is not implemented: " + name);
			}

			var type = SamplerHeapLayout.Type.valueOf(declaration.type());

			if (type.texelBuffer()) {
				throw new UnsupportedOperationException("Texel samplers require a buffer view: " + name);
			}

			var reference = pack.register(type, binding.key());
			references.put(name, reference);
			textures.put(reference, binding);
			used = true;
		}

		return used;
	}

	public void addTexelSampler(String name, Object key, LongSupplier view) {
		var declaration = declarations.get(name);

		if (declaration == null) {
			return;
		}

		var type = SamplerHeapLayout.Type.valueOf(declaration.type());

		if (!type.texelBuffer() || declaration.count() != 1) {
			throw new IllegalArgumentException("Expected scalar texel sampler: " + name);
		}

		var reference = pack.register(type, key);
		references.put(name, reference);
		texels.put(reference, view);
	}

	public void finish() {
		bufferBindings();

		if (!images.keySet().equals(imageDeclarations.keySet())) {
			throw new IllegalArgumentException("Missing storage image bindings: " + imageDeclarations.keySet());
		}

		for (String name : declarations.keySet()) {
			if (!references.containsKey(name)) {
				if (fallback == null || !declarations.get(name).type().equals("SAMPLER2D")) {
					throw new IllegalArgumentException("No Vulkan sampler registered for " + name);
				}

				addTextureSampler(fallback, false, name);
			}
		}

		textures.keySet().retainAll(new HashSet<>(references.values()));
	}

	public SamplerBindingPlan plan() {
		if (plan != null) {
			return plan;
		}

		var builder = new SamplerBindingPlan.Builder(pack.layout());
		references.forEach((name, reference) -> builder.bind(name, pack.slot(reference)));

		var built = builder.build();
		var resources = new HashMap<SamplerHeapLayout.Slot, SamplerResource>();
		textures.forEach((reference, binding) -> resources.put(pack.slot(reference), new SamplerResource(reference.type(), binding,
			null)));
		texels.forEach((reference, view) -> resources.put(pack.slot(reference), new SamplerResource(reference.type(), null, view)));

		var slots = built.indexSlots();
		samplerResources = new SamplerResource[slots.size()];

		for (int i = 0; i < slots.size(); i++) {
			samplerResources[i] = Objects.requireNonNull(resources.get(slots.get(i)), "Missing sampler resource");
		}

		indices = ByteBuffer.allocateDirect(((slots.size() + 3) / 4) * 16).order(ByteOrder.nativeOrder());

		return plan = new SamplerBindingPlan(built.heap(), built.bindings(), true);
	}

	public VulkanSamplerHeap.Snapshot snapshot() {
		if (plan == null) {
			plan();
		}

		for (int i = 0; i < samplerResources.length; i++) {
			var resource = samplerResources[i];
			var entry = resource.texture() == null ? new VulkanSamplerHeap.TexelBuffer(resource.texel().getAsLong())
				: pack.resolve(resource.type(), resource.texture());
			indices.putInt(i * 4, pack.frameIndex(resource.type(), entry));
		}

		return pack.frame().retain();
	}

	private record SamplerResource(SamplerHeapLayout.Type type, TextureBinding texture, LongSupplier texel) {
	}

	public void addIndexBinding(VulkanCommandEncoder encoder, Map<String, List<VulkanResourceSets.BufferSlice>> bindings) {
		var slice = uploadIndices(encoder);

		if (slice != null) {
			bindings.put(SamplerBindingPlan.INDEX_BLOCK, List.of(VulkanCommandBridge.bufferSlice(slice)));
		}
	}

	public GpuBufferSlice uploadIndices(VulkanCommandEncoder encoder) {
		return indices == null || !indices.hasRemaining()
			? null
			: encoder.transientMemory().uploadGpu(indices, pack.uniformAlignment(), GpuBuffer.USAGE_UNIFORM);
	}

	public List<CompositeSynchronization.Access> accesses() {
		var accesses = new ArrayList<CompositeSynchronization.Access>();

		for (var entry : references.entrySet()) {
			var descriptor = declarations.get(entry.getKey());
			var binding = textures.get(entry.getValue());

			if (binding == null) {
				accesses.add(new CompositeSynchronization.Access(new CompositeSynchronization.Resource(false, 0),
					stages(descriptor.stages()), KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR, false));
			} else {
				long image = binding.image() == null ? CompositeSynchronization.image(binding.texture().get().texture()).handle()
					: binding.image().get().image();
				accesses.add(CompositeSynchronization.shaderImage(image, stages(descriptor.stages()), false));
			}
		}

		for (var descriptor : imageDeclarations.values()) {
			var image = imageResources.get(descriptor.name());
			accesses.add(CompositeSynchronization.shaderImage(image == null ? 0 : image.getAsLong(), stages(descriptor.stages()), true));
		}

		var buffers = bufferBindings();

		for (var descriptor : storageDeclarations) {
			for (var slice : buffers.get(descriptor.name())) {
				accesses.add(new CompositeSynchronization.Access(
					new CompositeSynchronization.Resource(false, ((VulkanGpuBuffer) slice.buffer()).vkBuffer()),
					stages(descriptor.stages()),
					KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_SHADER_WRITE_BIT_KHR, true));
			}
		}

		return accesses;
	}

	private static long stages(Set<PatchShaderType> stages) {
		long mask = 0;

		for (var stage : stages) {
			mask |= switch (stage) {
				case VERTEX -> VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT;
				case TESS_CONTROL -> VK10.VK_PIPELINE_STAGE_TESSELLATION_CONTROL_SHADER_BIT;
				case TESS_EVAL -> VK10.VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT;
				case GEOMETRY -> VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT;
				case FRAGMENT -> VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
				case COMPUTE -> VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
			};
		}

		return mask;
	}

	private boolean unsupported(String... names) {
		for (String name : names) {
			if (hasSampler(name)) {
				throw new UnsupportedOperationException("Sampler still uses a GL texture name: " + name);
			}
		}

		return false;
	}

	public void externalBindings(IntFunction<TextureBinding> bindings) {
		externalBindings = bindings;
	}

	@Override
	public void addExternalSampler(int unit, String... names) {
		if (externalBindings == null) {
			unsupported(names);
		} else {
			addTextureSampler(externalBindings.apply(unit), unit == 0, names);
		}
	}

	@Override
	public boolean hasImage(String name) {
		return imageDeclarations.containsKey(name);
	}

	@Override
	public void addTextureImage(LongSupplier view, LongSupplier image, InternalTextureFormat format, String name) {
		addTextureImage(view, format, name);

		if (hasImage(name)) {
			imageResources.put(name, image);
		}
	}

	@Override
	public void addTextureImage(LongSupplier view, InternalTextureFormat format, String name) {
		if (!hasImage(name)) {
			return;
		}

		if (imageDeclarations.get(name).count() != 1) {
			throw new UnsupportedOperationException("Image arrays require element bindings: " + name);
		}

		images.put(name, view);
	}

	@Override
	public void addTextureImage(IntSupplier texture, InternalTextureFormat format, String name) {
		if (hasImage(name)) {
			throw new UnsupportedOperationException("Image binding requires a Vulkan view: " + name);
		}
	}

	@Override
	public boolean addDefaultSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		return unsupported(names);
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, Supplier<GlSampler> sampler, String... names) {
		return unsupported(names);
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		return unsupported(names);
	}
}
