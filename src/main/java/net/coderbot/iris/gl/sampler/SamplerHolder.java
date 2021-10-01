package net.coderbot.iris.gl.sampler;

import java.util.OptionalInt;
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
	boolean addDefaultSampler(IntSupplier sampler, OptionalInt internalFormat, Runnable postBind, String... names);
	boolean addDynamicSampler(IntSupplier sampler, OptionalInt internalFormat, Runnable postBind, String... names);

	default boolean addDefaultSampler(IntSupplier sampler, OptionalInt internalFormat, String... names) {
		return addDefaultSampler(sampler, internalFormat, () -> {}, names);
	}

	default boolean addDynamicSampler(IntSupplier sampler, OptionalInt internalFormat, String... names) {
		return addDynamicSampler(sampler, internalFormat, () -> {}, names);
	}
}
