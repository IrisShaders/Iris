package net.irisshaders.iris.targets;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureType;

public class CustomImage implements AutoCloseable {
	protected final String name;
	protected final String samplerName;
	protected final TextureType target;
	protected final PixelFormat format;
	protected final InternalTextureFormat internalTextureFormat;
	protected final PixelType pixelType;
	private final boolean clear;
	private boolean closed;
	private IrisImage image;

	public CustomImage(String name, String samplerName, TextureType target, PixelFormat format, InternalTextureFormat internalFormat,
		PixelType pixelType, boolean clear, int width, int height, int depth) {
		this.name = name;
		this.samplerName = samplerName;
		this.target = target;
		this.format = format;
		this.internalTextureFormat = internalFormat;
		this.pixelType = pixelType;
		this.clear = clear;
		this.resize(width, height, depth);
	}

	public void destroy() {
		this.close();
	}

	protected void resize(int width, int height, int depth) {
		if (this.closed) {
			throw new IllegalStateException("custom image is closed");
		}

		if (this.image != null && this.image.getWidth() == width && this.image.getHeight() == Math.max(1,
			height) && this.image.getDepth() == Math.max(1, depth)) {
			return;
		}

		var next = Iris.getRenderBackend()
			.createImage(this.name, this.target, this.internalTextureFormat.toGpuFormat(), width, height, depth, true);

		if (this.image != null) {
			this.image.close();
		}

		this.image = next;
	}

	public IrisImage image() {
		if (this.closed) {
			throw new IllegalStateException("custom image is closed");
		}

		return this.image;
	}

	public void clear() {
		this.image().clear();
	}

	public String getName() {
		return this.name;
	}

	public String getSamplerName() {
		return this.samplerName;
	}

	public TextureType getTarget() {
		return this.target;
	}

	public boolean shouldClear() {
		return this.clear;
	}

	public void updateNewSize(int width, int height) {
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;

		if (this.image != null) {
			this.image.close();
		}
	}

	public InternalTextureFormat getInternalFormat() {
		return this.internalTextureFormat;
	}

	public PixelFormat getFormat() {
		return this.format;
	}

	public PixelType getPixelType() {
		return this.pixelType;
	}

	public static class Relative extends CustomImage {
		private final float relativeWidth;
		private final float relativeHeight;

		public Relative(String name, String samplerName, PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType,
			boolean clear, float relativeWidth, float relativeHeight, int width, int height) {
			super(name, samplerName, TextureType.TEXTURE_2D, format, internalFormat, pixelType, clear,
				Math.max(1, (int) (width * relativeWidth)), Math.max(1, (int) (height * relativeHeight)), 0);
			this.relativeWidth = relativeWidth;
			this.relativeHeight = relativeHeight;
		}

		@Override
		public void updateNewSize(int width, int height) {
			this.resize(Math.max(1, (int) (width * this.relativeWidth)), Math.max(1, (int) (height * this.relativeHeight)), 0);
		}
	}
}
