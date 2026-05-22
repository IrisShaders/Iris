package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.gl.sampler.GlSampler;

import java.util.function.IntSupplier;

public interface TextureAccess {
	TextureType getType();

	IntSupplier getTextureId();

    GlSampler getSampling();
}
