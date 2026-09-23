package net.irisshaders.iris.samplers;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class ProgramResources implements SamplerHolder, ImageHolder {
	private final PackSamplers pack;
	private final List<ShaderResourceLayout.Descriptor> storageDeclarations;
	private final Map<PackSamplers.Reference, LongSupplier> texels = new LinkedHashMap<>();
	private final Map<String, ShaderResourceLayout.Descriptor> declarations = new LinkedHashMap<>();
	private final Map<String, PackSamplers.Reference> references = new LinkedHashMap<>();
	private final Map<PackSamplers.Reference, TextureBinding> textures = new LinkedHashMap<>();
	private final Map<String, ShaderResourceLayout.Descriptor> imageDeclarations = new LinkedHashMap<>();
	private final Map<String, InternalTextureFormat> imageFormats = new LinkedHashMap<>();
	private final Map<String, Supplier<ImageBinding>> images = new LinkedHashMap<>();
	private ShaderStorageBufferHolder storageBuffers;
	private Supplier<Map<String, List<GpuBufferSlice>>> uniformBuffers = Map::of;
	private TextureBinding fallback;
	private boolean drawFallback;
	private IntFunction<TextureBinding> externalBindings;
	private SamplerBindingPlan plan;

	public ProgramResources(PackSamplers pack, ShaderResourceLayout layout) {
		this.pack = pack;
		this.storageDeclarations = layout.descriptors().stream().filter(d -> d.kind() == ShaderResourceLayout.Kind.STORAGE_BUFFER).toList();

		for (var descriptor : layout.descriptors()) {
			if (descriptor.kind() == ShaderResourceLayout.Kind.IMAGE) {
				this.imageDeclarations.put(descriptor.name(), descriptor);
			} else if (descriptor.kind() == ShaderResourceLayout.Kind.SAMPLER) {
				this.declarations.put(descriptor.name(), descriptor);
			}
		}
	}

	public void addStorageBuffers(ShaderStorageBufferHolder buffers) {
		this.storageBuffers = buffers;
		this.getBufferBindings();
	}

	public void setUniformBuffers(Supplier<Map<String, List<GpuBufferSlice>>> buffers) {
		this.uniformBuffers = buffers;
	}

	public Map<String, List<GpuBufferSlice>> getBufferBindings() {
		var result = new LinkedHashMap<String, List<GpuBufferSlice>>();

		for (var descriptor : this.storageDeclarations) {
			if (this.storageBuffers == null) {
				throw new IllegalArgumentException("no buffer objects for storage block " + descriptor.name());
			}

			int first = descriptor.originalBinding() == null ? 0 : descriptor.originalBinding();
			var slices = new ArrayList<GpuBufferSlice>();

			for (int i = 0; i < descriptor.count(); i++) {
				slices.add(this.storageBuffers.getBuffer(Math.addExact(first, i)).slice());
			}

			result.put(descriptor.name(), List.copyOf(slices));
		}

		result.putAll(this.uniformBuffers.get());

		return result;
	}

	public Bindings resolve() {
		return this.resolve(Map.of());
	}

	public Bindings resolve(Map<String, List<GpuBufferSlice>> supplied) {
		var resolvedTextures = new LinkedHashMap<PackSamplers.Reference, TextureBinding.Resolved>();
		this.textures.forEach((reference, binding) -> resolvedTextures.put(reference, binding.resolve(reference.type())));
		var resolvedTexels = new LinkedHashMap<PackSamplers.Reference, Long>();
		this.texels.forEach((reference, view) -> resolvedTexels.put(reference, view.getAsLong()));
		var buffers = this.getBufferBindings();
		buffers.putAll(supplied);
		return new Bindings(resolvedTextures, resolvedTexels, buffers, this.getImages());
	}

	@Override
	public boolean hasSampler(String name) {
		return this.declarations.containsKey(name);
	}

	@Override
	public boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		return this.addTextureSampler(binding, defaultSampler, false, names);
	}

	@Override
	public boolean addDrawTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		return this.addTextureSampler(binding, defaultSampler, true, names);
	}

	private boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, boolean draw, String... names) {
		if (defaultSampler) {
			this.fallback = binding;
			this.drawFallback = draw;
		}

		boolean used = false;

		for (String name : names) {
			var declaration = this.declarations.get(name);

			if (declaration == null) {
				continue;
			}

			if (declaration.count() != 1) {
				throw new UnsupportedOperationException("sampler array registration is not implemented: " + name);
			}

			var type = SamplerHeapLayout.Type.valueOf(declaration.type());

			if (type.texelBuffer()) {
				throw new UnsupportedOperationException("texel samplers require a buffer view: " + name);
			}

			var reference = this.pack.register(type, binding.key(), draw);
			this.references.put(name, reference);
			this.textures.put(reference, binding);
			used = true;
		}

		return used;
	}

	public void addTexelSampler(String name, Object key, LongSupplier view) {
		var declaration = this.declarations.get(name);

		if (declaration == null) {
			return;
		}

		var type = SamplerHeapLayout.Type.valueOf(declaration.type());

		if (!type.texelBuffer() || declaration.count() != 1) {
			throw new IllegalArgumentException("expected scalar texel sampler: " + name);
		}

		var reference = this.pack.register(type, key, true);
		this.references.put(name, reference);
		this.texels.put(reference, view);
	}

	public void finish() {
		this.getBufferBindings();

		if (!this.images.keySet().equals(this.imageDeclarations.keySet())) {
			throw new IllegalArgumentException("missing storage image bindings: " + this.imageDeclarations.keySet());
		}

		for (String name : this.declarations.keySet()) {
			if (!this.references.containsKey(name)) {
				if (this.fallback == null || !this.declarations.get(name).type().equals("SAMPLER2D")) {
					throw new IllegalArgumentException("no sampler registered for " + name);
				}

				this.addTextureSampler(this.fallback, false, this.drawFallback, name);
			}
		}

		this.textures.keySet().retainAll(new HashSet<>(this.references.values()));
	}

	public PackSamplers getPackSamplers() {
		return this.pack;
	}

	public Map<String, PackSamplers.Reference> getSamplerReferences() {
		return Collections.unmodifiableMap(this.references);
	}

	public Map<PackSamplers.Reference, TextureBinding> getTextureBindings() {
		return Collections.unmodifiableMap(this.textures);
	}

	public Map<PackSamplers.Reference, LongSupplier> getTexelBufferBindings() {
		return Collections.unmodifiableMap(this.texels);
	}

	public Map<String, ShaderResourceLayout.Descriptor> getSamplerDeclarations() {
		return Collections.unmodifiableMap(this.declarations);
	}

	public Map<String, ShaderResourceLayout.Descriptor> getImageDeclarations() {
		return Collections.unmodifiableMap(this.imageDeclarations);
	}

	public Map<String, InternalTextureFormat> getImageFormats() {
		return Collections.unmodifiableMap(this.imageFormats);
	}

	private Map<String, List<ImageBinding>> getImages() {
		var result = new LinkedHashMap<String, List<ImageBinding>>();
		this.images.forEach((name, image) -> result.put(name, List.of(image.get())));

		return result;
	}

	public List<ShaderResourceLayout.Descriptor> getStorageDeclarations() {
		return this.storageDeclarations;
	}

	public SamplerBindingPlan getBindingPlan() {
		if (this.plan == null) {
			var builder = new SamplerBindingPlan.Builder(this.pack.getLayout());
			this.references.forEach((name, reference) -> builder.bind(name, this.pack.getSlot(reference)));
			this.plan = builder.build();
		}

		return this.plan;
	}

	private boolean unsupported(String... names) {
		for (String name : names) {
			if (this.hasSampler(name)) {
				throw new UnsupportedOperationException("sampler still uses a GL texture name: " + name);
			}
		}

		return false;
	}

	public void setExternalBindings(IntFunction<TextureBinding> bindings) {
		this.externalBindings = bindings;
	}

	@Override
	public void addExternalSampler(int unit, String... names) {
		if (this.externalBindings == null) {
			this.unsupported(names);
		} else {
			this.addDrawTextureSampler(this.externalBindings.apply(unit), unit == 0, names);
		}
	}

	@Override
	public boolean hasImage(String name) {
		return this.imageDeclarations.containsKey(name);
	}

	@Override
	public void addTextureImage(Supplier<GpuTextureView> texture, InternalTextureFormat format, String name) {
		this.addImageBinding(() -> new ImageBinding(texture.get(), null), format, name);
	}

	@Override
	public void addImage(Supplier<IrisImage> image, InternalTextureFormat format, String name) {
		this.addImageBinding(() -> new ImageBinding(null, image.get()), format, name);
	}

	private void addImageBinding(Supplier<ImageBinding> image, InternalTextureFormat format, String name) {
		if (!this.hasImage(name)) {
			return;
		}

		if (this.imageDeclarations.get(name).count() != 1) {
			throw new UnsupportedOperationException("image arrays require element bindings: " + name);
		}

		this.images.put(name, image);
		this.imageFormats.put(name, format);
	}

	@Override
	public boolean addDefaultSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		return this.unsupported(names);
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, Supplier<GlSampler> sampler, String... names) {
		return this.unsupported(names);
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		return this.unsupported(names);
	}

	public record Bindings(Map<PackSamplers.Reference, TextureBinding.Resolved> textures, Map<PackSamplers.Reference, Long> texels, Map<String, List<GpuBufferSlice>> buffers, Map<String, List<ImageBinding>> images) {
	}

	public record ImageBinding(GpuTextureView texture, IrisImage image) {
	}
}
