package net.irisshaders.iris.gl.sampler;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.samplers.SamplerHeapLayout;

import java.util.Objects;
import java.util.function.Supplier;

public interface SamplerHolder {
	boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names);

	default boolean addDrawTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		return this.addTextureSampler(binding, defaultSampler, names);
	}

	void addExternalSampler(int textureUnit, String... names);

	boolean hasSampler(String name);

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
