package net.irisshaders.iris.gl.sampler;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.vulkan.VulkanImage;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface SamplerHolder {
	record TextureBinding(Object key, Supplier<GpuTextureView> texture, Supplier<GlSampler> sampler, Supplier<GpuSampler> nativeSampler,
		Supplier<VulkanImage> image) {
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
	}

	default boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		for (String name : names) {
			if (hasSampler(name)) {
				throw new UnsupportedOperationException("Native texture registration requires a Vulkan program: " + name);
			}
		}

		return false;
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
}
