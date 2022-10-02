package net.coderbot.iris.gl.sampler;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.gl.texture.TextureType;

import java.util.function.IntSupplier;

public interface SamplerHolder {
	void addExternalSampler(int textureUnit, String... names);
	boolean hasSampler(String name);

	/**
	 * Like addDynamicSampler, but also ensures that any unrecognized / unbound samplers sample from this
	 * sampler.
	 *
	 * Throws an exception if texture unit 0 is already allocated or reserved in some way. Do not call this
	 * function after calls to addDynamicSampler, it must be called before any calls to addDynamicSampler.
	 */
	boolean addDefaultSampler(IntSupplier sampler, String... names);

	default boolean addDynamicSampler(IntSupplier sampler, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, sampler, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier sampler, String... names);

	default boolean addDynamicSampler(IntSupplier sampler, ValueUpdateNotifier notifier, String... names) {
		return addDynamicSampler(TextureType.TEXTURE_2D, sampler, notifier, names);
	}

	boolean addDynamicSampler(TextureType type, IntSupplier sampler, ValueUpdateNotifier notifier, String... names);
}
