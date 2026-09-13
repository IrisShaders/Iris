package net.irisshaders.iris.gl.texture;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.sampler.GlSampler;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextureWrapper implements TextureAccess {
	private final Supplier<GpuTextureView> texture;
	private final TextureType type;

	public TextureWrapper(Supplier<GpuTextureView> texture, TextureType type) {
		this.texture = texture;
		this.type = type;
	}

	@Override
	public TextureType getType() {
		return this.type;
	}

	@Override
	public IntSupplier getTextureId() {
		throw new UnsupportedOperationException("TextureWrapper uses Vulkan views");
	}

	@Override
	public GpuTextureView getTextureView() {
		return texture.get();
	}

	@Override
	public GlSampler getSampling() {
		return GlSampler.MIPPED_NEAREST_REPEAT; // TODO?
	}
}
