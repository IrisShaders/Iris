package net.irisshaders.iris.gl.sampler;

import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.TextureType;

import java.util.function.IntSupplier;

public interface SamplerHolder {
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

	boolean addDefaultSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, GlSampler sampler, String... names);


	default boolean addDynamicSampler(IntSupplier texture, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, texture, null, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier texture, GlSampler sampler, String... names);

	default boolean addDynamicSampler(IntSupplier texture, ValueUpdateNotifier notifier, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, texture, notifier, null, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, GlSampler sampler, String... names);
}
