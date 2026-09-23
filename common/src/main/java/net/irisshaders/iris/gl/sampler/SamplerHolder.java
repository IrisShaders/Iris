package net.irisshaders.iris.gl.sampler;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.samplers.SamplerHeapLayout;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface SamplerHolder {
	default boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		for (String name : names) {
			if (this.hasSampler(name)) {
				throw new UnsupportedOperationException("native texture registration is unavailable: " + name);
			}
		}

		return false;
	}

	default boolean addDrawTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		return this.addTextureSampler(binding, defaultSampler, names);
	}

	void addExternalSampler(int textureUnit, String... names);

	boolean hasSampler(String name);

	/**
	 * Like addDynamicSampler, but also ensures that any unrecognized / unbound samplers sample from this
	 * sampler.
	 * <p>
	 * Throws an exception if texture unit 0 is already allocated or reserved in some way. Do not call this
	 * function after calls to addDynamicSampler, it must be called before any calls to addDynamicSampler.
	 */
	default boolean addDefaultSampler(IntSupplier sampler, String... names) {
		return addDefaultSampler(TextureType.TEXTURE_2D, sampler, null, null, names);
	}

	boolean addDefaultSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names);

	default boolean addDynamicSampler(IntSupplier texture, GlSampler sampler, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, texture, () -> sampler, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier texture, Supplier<GlSampler> sampler, String... names);

	default boolean addDynamicSampler(IntSupplier texture, GlSampler sampler, ValueUpdateNotifier notifier, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, texture, notifier, () -> sampler, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names);

	record TextureBinding(Object key, Supplier<GpuTextureView> texture, Supplier<GlSampler> sampler, Supplier<GpuSampler> nativeSampler, Supplier<IrisImage> image) {
		public TextureBinding(Object key, Supplier<GpuTextureView> texture, Supplier<GlSampler> sampler,
			Supplier<GpuSampler> nativeSampler) {
			this(key, texture, sampler, nativeSampler, null);
		}

		public TextureBinding(Object key, Supplier<GpuTextureView> texture, Supplier<GlSampler> sampler) {
			this(key, texture, sampler, null);
		}

		public TextureBinding {
			Objects.requireNonNull(key);

			if (image == null) {
				Objects.requireNonNull(texture);
			}

			Objects.requireNonNull(sampler);
		}

		public Resolved resolve(SamplerHeapLayout.Type type) {
			boolean comparison = type.glsl.endsWith("Shadow");
			var state = this.sampler.get().description();
			var description = new GlSampler.Description(state.linear(), state.linearMips(), state.mipmapped(), comparison, state.clamp());
			var custom = this.image == null ? null : Objects.requireNonNull(this.image.get(), "missing sampler image: " + this.key);
			var view = this.image == null ? this.texture.get() : null;

			if (this.image == null) {
				if (view == null || view.isClosed() || view.texture().isClosed() || (view.texture()
					.usage() & GpuTexture.USAGE_TEXTURE_BINDING) == 0) {
					throw new IllegalStateException("missing or closed sampler texture: " + this.key);
				}

				if (comparison && !view.texture().getFormat().hasDepthAspect()) {
					throw new IllegalArgumentException("comparison sampler requires depth: " + this.key);
				}
			}

			var external = this.nativeSampler == null ? null : this.nativeSampler.get();

			if (external != null && external.isClosed()) {
				throw new IllegalStateException("closed sampler: " + this.key);
			}

			if (external != null && comparison) {
				throw new IllegalArgumentException("external comparison sampler requires an Iris sampler description");
			}

			return new Resolved(view, custom, description, external);
		}

		public record Resolved(GpuTextureView texture, IrisImage image, GlSampler.Description sampler, GpuSampler nativeSampler) {
		}
	}
}
