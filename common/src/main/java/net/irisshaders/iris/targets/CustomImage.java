package net.irisshaders.iris.targets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanImage;

public class CustomImage implements AutoCloseable {
	private boolean closed;

	public void destroy() {
		close();
	}

	protected final String name;
	protected final String samplerName;
	protected final TextureType target;
	protected final PixelFormat format;
	protected final InternalTextureFormat internalTextureFormat;
	protected final PixelType pixelType;
	private final boolean clear;
	private final VulkanDevice device;
	private VulkanImage image;

	public CustomImage(String name, String samplerName, TextureType target, PixelFormat format, InternalTextureFormat internalFormat,
		PixelType pixelType, boolean clear, int width, int height, int depth) {
		this(VulkanCommandBridge.device(RenderSystem.getDevice()), name, samplerName, target, format, internalFormat, pixelType, clear,
			width, height, depth);
	}

	public CustomImage(VulkanDevice device, String name, String samplerName, TextureType target, PixelFormat format,
		InternalTextureFormat internalFormat, PixelType pixelType, boolean clear, int width, int height, int depth) {
		this.device = device;
		this.name = name;
		this.samplerName = samplerName;
		this.target = target;
		this.format = format;
		this.internalTextureFormat = internalFormat;
		this.pixelType = pixelType;
		this.clear = clear;
		resize(width, height, depth);
	}

	protected void resize(int width, int height, int depth) {
		if (closed) {
			throw new IllegalStateException("CustomImage is closed");
		}

		if (image != null && image.width() == width && image.height() == Math.max(1, height) && image.depth() == Math.max(1, depth)) {
			return;
		}

		var next = new VulkanImage(device, name, target, internalTextureFormat.toGpuFormat(), width, height, depth);

		if (image != null) {
			image.close();
		}

		image = next;
	}

	public VulkanImage image() {
		if (closed) {
			throw new IllegalStateException("CustomImage is closed");
		}

		return image;
	}

	public void clear() {
		image().clear();
	}

	public String getName() {
		return name;
	}

	public String getSamplerName() {
		return samplerName;
	}

	public TextureType getTarget() {
		return target;
	}

	public boolean shouldClear() {
		return clear;
	}

	public void updateNewSize(int width, int height) {
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;

		if (image != null) {
			image.close();
		}
	}

	public InternalTextureFormat getInternalFormat() {
		return internalTextureFormat;
	}

	public PixelFormat getFormat() {
		return format;
	}

	public PixelType getPixelType() {
		return pixelType;
	}

	public static class Relative extends CustomImage {
		private final float relativeWidth;
		private final float relativeHeight;

		public Relative(String name, String samplerName, PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType,
			boolean clear, float relativeWidth, float relativeHeight, int width, int height) {
			this(VulkanCommandBridge.device(RenderSystem.getDevice()), name, samplerName, format, internalFormat, pixelType, clear,
				relativeWidth, relativeHeight, width, height);
		}

		public Relative(VulkanDevice device, String name, String samplerName, PixelFormat format, InternalTextureFormat internalFormat,
			PixelType pixelType, boolean clear, float relativeWidth, float relativeHeight, int width, int height) {
			super(device, name, samplerName, TextureType.TEXTURE_2D, format, internalFormat, pixelType, clear,
				Math.max(1, (int) (width * relativeWidth)), Math.max(1, (int) (height * relativeHeight)), 0);
			this.relativeWidth = relativeWidth;
			this.relativeHeight = relativeHeight;
		}

		@Override
		public void updateNewSize(int width, int height) {
			resize(Math.max(1, (int) (width * relativeWidth)), Math.max(1, (int) (height * relativeHeight)), 0);
		}
	}
}
