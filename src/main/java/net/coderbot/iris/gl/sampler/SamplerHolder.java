package net.coderbot.iris.gl.sampler;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;

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
	boolean addDynamicSampler(IntSupplier sampler, String... names);
	boolean addDynamicSampler(IntSupplier sampler, ValueUpdateNotifier notifier, String... names);
}
