package net.irisshaders.iris.samplers;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class ProgramSamplers implements SamplerHolder, ImageHolder {
	private final PackSamplers pack;
	private final ShaderResourceLayout layout;
	private final Map<String, ShaderResourceLayout.Descriptor> declarations = new LinkedHashMap<>();
	private final Map<String, SamplerBinding> samplers = new LinkedHashMap<>();
	private final Map<String, ImageBinding> images = new LinkedHashMap<>();
	private TextureBinding fallback;
	private boolean drawFallback;
	private IntFunction<TextureBinding> externalBindings;

	public ProgramSamplers(PackSamplers pack, ShaderResourceLayout layout) {
		this.pack = pack;
		this.layout = layout;
		for (var descriptor : layout.descriptors()) {
			this.declarations.put(descriptor.name(), descriptor);
		}
	}

	public PackSamplers getPackSamplers() {
		return this.pack;
	}

	public ShaderResourceLayout getLayout() {
		return this.layout;
	}

	public Map<String, SamplerBinding> getSamplers() {
		return Collections.unmodifiableMap(this.samplers);
	}

	public Map<String, ImageBinding> getImages() {
		return Collections.unmodifiableMap(this.images);
	}

	public SamplerBindingPlan getBindingPlan() {
		var bindings = new LinkedHashMap<String, List<SamplerHeapLayout.Slot>>();
		this.samplers.forEach((name, binding) -> bindings.put(name, List.of(this.pack.getSlot(binding.reference()))));
		return new SamplerBindingPlan(this.pack.getLayout(), bindings);
	}

	@Override
	public boolean hasSampler(String name) {
		var declaration = this.declarations.get(name);
		return declaration != null && declaration.kind() == ShaderResourceLayout.Kind.SAMPLER;
	}

	private PackSamplers.Reference register(String name, Object key, boolean draw, boolean texel) {
		var declaration = this.declarations.get(name);
		if (!this.hasSampler(name)) return null;
		var type = SamplerHeapLayout.Type.valueOf(declaration.type());
		if (declaration.count() != 1 || type.texelBuffer() != texel) {
			throw new UnsupportedOperationException("incompatible sampler registration: " + name);
		}
		return this.pack.register(type, key, draw);
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
			var reference = this.register(name, binding.key(), draw, false);
			if (reference != null) {
				this.samplers.put(name, new SamplerBinding(reference, binding, null));
				used = true;
			}
		}
		return used;
	}

	public void addTexelSampler(String name, Object key, LongSupplier view) {
		var reference = this.register(name, key, true, true);
		if (reference != null) this.samplers.put(name, new SamplerBinding(reference, null, view));
	}

	public void finish() {
		for (var declaration : this.declarations.values()) {
			String name = declaration.name();
			if (this.hasImage(name) && !this.images.containsKey(name)) {
				throw new IllegalArgumentException("no image registered for " + name);
			}
			if (this.hasSampler(name) && !this.samplers.containsKey(name)) {
				if (this.fallback == null || !declaration.type().equals("SAMPLER2D")) {
					throw new IllegalArgumentException("no sampler registered for " + name);
				}
				this.addTextureSampler(this.fallback, false, this.drawFallback, name);
			}
		}
		this.pack.getBufferBindings(this.layout);
		this.pack.getResources().register(this);
	}

	public void setExternalBindings(IntFunction<TextureBinding> bindings) {
		this.externalBindings = bindings;
	}

	@Override
	public void addExternalSampler(int unit, String... names) {
		if (this.externalBindings == null) {
			for (String name : names) {
				if (this.hasSampler(name)) throw new IllegalArgumentException("no external texture binding for " + name);
			}
		} else {
			this.addDrawTextureSampler(this.externalBindings.apply(unit), unit == 0, names);
		}
	}

	@Override
	public boolean hasImage(String name) {
		var declaration = this.declarations.get(name);
		return declaration != null && declaration.kind() == ShaderResourceLayout.Kind.IMAGE;
	}

	@Override
	public void addTextureImage(Supplier<GpuTextureView> texture, InternalTextureFormat format, String name) {
		this.addImageBinding(new ImageBinding(texture, null, format), name);
	}

	@Override
	public void addImage(Supplier<IrisImage> image, InternalTextureFormat format, String name) {
		this.addImageBinding(new ImageBinding(null, image, format), name);
	}

	private void addImageBinding(ImageBinding binding, String name) {
		if (!this.hasImage(name)) return;
		if (this.declarations.get(name).count() != 1) {
			throw new UnsupportedOperationException("image arrays require element bindings: " + name);
		}
		this.images.put(name, binding);
	}

	public record SamplerBinding(PackSamplers.Reference reference, TextureBinding texture, LongSupplier texelBuffer) {
	}

	public record ImageBinding(Supplier<GpuTextureView> texture, Supplier<IrisImage> image, InternalTextureFormat format) {
	}
}
