package net.irisshaders.iris.targets.backed;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.shaderpack.texture.TextureFilteringData;

import java.util.function.IntSupplier;

public class RawTexture implements TextureAccess, AutoCloseable {
	private final TextureType target;
	private final GlSampler sampler;
	private final IrisImage image;
	private boolean closed;

	public RawTexture(TextureType target, int width, int height, int depth, InternalTextureFormat internal, PixelFormat format,
		PixelType type, byte[] pixels, TextureFilteringData filtering) {
		this.target = target;

		boolean blur = filtering.shouldBlur() && !internal.getPixelFormat().isInteger();
		this.sampler = filtering.shouldClamp() ? (blur ? GlSampler.LINEAR : GlSampler.NEAREST) : (blur ? GlSampler.LINEAR_REPEAT : GlSampler.NEAREST_REPEAT);

		if (width < 1 || height < 0 || depth < 0) {
			throw new IllegalArgumentException("invalid raw texture dimensions");
		}

		var data = RawTextureData.convert(pixels, Math.multiplyExact(width, Math.multiplyExact(Math.max(1, height), Math.max(1, depth))),
			internal, format, type);
		this.image = Iris.getRenderBackend()
			.createImage("Iris raw custom texture", target, internal.toGpuFormat(), width, height, depth, false);

		try {
			this.image.upload(data);
		} catch (RuntimeException | Error failure) {
			this.image.close();

			throw failure;
		}
	}

	public void destroy() {
		this.close();
	}

	public TextureType getTarget() {
		return this.target;
	}

	@Override
	public TextureType getType() {
		return this.target;
	}

	@Override
	public IntSupplier getTextureId() {
		throw new UnsupportedOperationException("raw textures use Vulkan views");
	}

	@Override
	public GlSampler getSampling() {
		return this.sampler;
	}

	@Override
	public SamplerHolder.TextureBinding samplerBinding() {
		return new SamplerHolder.TextureBinding(this, null, this::getSampling, null, this::image);
	}

	public IrisImage image() {
		if (this.closed) {
			throw new IllegalStateException("raw texture is closed");
		}

		return this.image;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.image.close();
	}
}
