package net.irisshaders.iris.gl.texture;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;

import java.util.function.IntSupplier;

public interface TextureAccess {
	TextureType getType();

	default GpuTextureView getTextureView() {
		throw new UnsupportedOperationException("Texture has not been ported to Vulkan: " + getClass().getSimpleName());
	}

	default SamplerHolder.TextureBinding samplerBinding() {
		return new SamplerHolder.TextureBinding(this, this::getTextureView, this::getSampling);
	}

	IntSupplier getTextureId();

	GlSampler getSampling();
}
