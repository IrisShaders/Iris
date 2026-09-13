package net.irisshaders.iris.targets.backed;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.shaderpack.texture.TextureFilteringData;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanImage;

import java.util.function.IntSupplier;

public class RawTexture implements TextureAccess, AutoCloseable {
	private boolean closed;

	public void destroy() {
		close();
	}

	private final TextureType target;
	private final GlSampler sampler;
	private final VulkanImage image;

	public RawTexture(TextureType target, int width, int height, int depth, InternalTextureFormat internal, PixelFormat format,
		PixelType type, byte[] pixels, TextureFilteringData filtering) {
		this(VulkanCommandBridge.device(RenderSystem.getDevice()), target, width, height, depth, internal, format, type, pixels,
			filtering);
	}

	public RawTexture(VulkanDevice device, TextureType target, int width, int height, int depth, InternalTextureFormat internal,
		PixelFormat format, PixelType type, byte[] pixels, TextureFilteringData filtering) {
		this.target = target;

		boolean blur = filtering.shouldBlur() && !internal.getPixelFormat().isInteger();
		sampler = filtering.shouldClamp() ? (blur ? GlSampler.LINEAR : GlSampler.NEAREST)
			: (blur ? GlSampler.LINEAR_REPEAT : GlSampler.NEAREST_REPEAT);

		if (width < 1 || height < 0 || depth < 0) {
			throw new IllegalArgumentException("Invalid raw texture dimensions");
		}

		var data = RawTextureData.convert(pixels, Math.multiplyExact(width, Math.multiplyExact(Math.max(1, height), Math.max(1, depth))),
			internal, format, type);
		image = new VulkanImage(device, "Iris raw custom texture", target, internal.toGpuFormat(), width, height, depth, false);

		try {
			image.upload(data);
		} catch (RuntimeException | Error failure) {
			image.close();

			throw failure;
		}
	}

	public TextureType getTarget() {
		return target;
	}

	@Override
	public TextureType getType() {
		return target;
	}

	@Override
	public IntSupplier getTextureId() {
		throw new UnsupportedOperationException("Raw textures use Vulkan views");
	}

	@Override
	public GlSampler getSampling() {
		return sampler;
	}

	@Override
	public SamplerHolder.TextureBinding samplerBinding() {
		return new SamplerHolder.TextureBinding(this, null, this::getSampling, null, this::image);
	}

	public VulkanImage image() {
		if (closed) {
			throw new IllegalStateException("RawTexture is closed");
		}

		return image;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;
		image.close();
	}
}
