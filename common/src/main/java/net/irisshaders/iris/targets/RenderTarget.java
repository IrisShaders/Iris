package net.irisshaders.iris.targets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import org.joml.Vector2i;

public class RenderTarget {

	public static final int USAGE_STORAGE = 1 << 20;
	private final InternalTextureFormat internalFormat;
	private final PixelFormat format;
	private final PixelType type;
	private final String name;
	private final boolean allowsLinear;
	private boolean storage;
	private GpuTexture mainGpuTexture, altGpuTexture;
	private GpuTextureView mainView, altView;
	private GpuTextureView mainMipView, altMipView;
	private int width;
	private int height;
	private boolean isValid;
	private boolean mipmapsOnAlt;
	private boolean mipmapsOnMain;

	public RenderTarget(Builder builder) {
		this.isValid = true;

		this.name = builder.name;
		this.internalFormat = builder.internalFormat;
		this.format = builder.format;
		this.type = builder.type;

		this.width = builder.width;
		this.height = builder.height;

		this.allowsLinear = !this.internalFormat.getPixelFormat().isInteger();
		this.resizeGpuTextures(this.width, this.height);
	}

	public static Builder builder() {
		return new Builder();
	}

	public void enableStorage() {
		this.requireValid();

		if (this.storage) {
			return;
		}

		this.storage = true;
		if ((this.mainGpuTexture.usage() & USAGE_STORAGE) != 0) {
			return;
		}

		try {
			this.resizeGpuTextures(this.width, this.height);
		} catch (RuntimeException | Error failure) {
			this.storage = false;

			throw failure;
		}
	}

	private void resizeGpuTextures(int width, int height) {
		int levels = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
		int usage = (this.storage ? USAGE_STORAGE : 0) | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST;

		if (levels > 1) {
			usage |= Iris.getRenderBackend().getMipmapUsage(this.internalFormat.toGpuFormat());
		}

		GpuTexture newMain = null, newAlt = null;
		GpuTextureView newMainView = null, newAltView = null, newMainMips = null, newAltMips = null;

		try {
			newMain = RenderSystem.getDevice()
				.createTexture(this.name + " main", usage, this.internalFormat.toGpuFormat(), width, height, 1, levels);
			newAlt = RenderSystem.getDevice()
				.createTexture(this.name + " alt", usage, this.internalFormat.toGpuFormat(), width, height, 1, levels);

			newMainView = RenderSystem.getDevice().createTextureView(newMain, 0, 1);
			newAltView = RenderSystem.getDevice().createTextureView(newAlt, 0, 1);
			newMainMips = RenderSystem.getDevice().createTextureView(newMain);
			newAltMips = RenderSystem.getDevice().createTextureView(newAlt);
		} catch (RuntimeException | Error exception) {
			if (newAltMips != null) {
				newAltMips.close();
			}

			if (newMainMips != null) {
				newMainMips.close();
			}

			if (newAltView != null) {
				newAltView.close();
			}

			if (newMainView != null) {
				newMainView.close();
			}

			if (newAlt != null) {
				newAlt.close();
			}

			if (newMain != null) {
				newMain.close();
			}

			throw new IllegalStateException(
				"could not allocate Iris target " + this.name + " (" + this.internalFormat + " as " + this.internalFormat.toGpuFormat() + ", " + width + "x" + height + ", " + levels + " mips)",
				exception);
		}

		this.closeGpuTextures();
		this.mainGpuTexture = newMain;
		this.altGpuTexture = newAlt;
		this.mainView = newMainView;
		this.altView = newAltView;
		this.mainMipView = newMainMips;
		this.altMipView = newAltMips;
		this.mipmapsOnMain = this.mipmapsOnAlt = false;
	}

	private void closeGpuTextures() {
		if (this.mainView == null) {
			return;
		}

		this.mainMipView.close();
		this.altMipView.close();
		this.mainView.close();
		this.altView.close();
		this.mainGpuTexture.close();
		this.altGpuTexture.close();
		this.mainMipView = this.altMipView = this.mainView = this.altView = null;
		this.mainGpuTexture = this.altGpuTexture = null;
	}

	public GpuTextureView getTextureView(boolean alt, boolean mipmapped) {
		this.requireValid();

		return alt ? (mipmapped ? this.altMipView : this.altView) : (mipmapped ? this.mainMipView : this.mainView);
	}

	public boolean allowsLinearFiltering() {
		return this.allowsLinear;
	}

	public boolean hasMipmaps(boolean alt) {
		return alt ? this.mipmapsOnAlt : this.mipmapsOnMain;
	}

	void resize(Vector2i textureScaleOverride) {
		this.resize(textureScaleOverride.x, textureScaleOverride.y);
	}

	// Package private, call CompositeRenderTargets#resizeIfNeeded instead.
	void resize(int width, int height) {
		requireValid();

		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("invalid render target dimensions");
		}

		this.resizeGpuTextures(width, height);
		this.width = width;
		this.height = height;
	}

	public InternalTextureFormat getInternalFormat() {
		return internalFormat;
	}

	public int getMainTexture() {
		requireValid();

		throw new IllegalStateException("Vulkan render targets do not have OpenGL texture names");
	}

	public int getAltTexture() {
		requireValid();

		throw new IllegalStateException("Vulkan render targets do not have OpenGL texture names");
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void destroy() {
		requireValid();
		isValid = false;

		this.closeGpuTextures();
	}

	private void requireValid() {
		if (!isValid) {
			throw new IllegalStateException("Attempted to use a deleted composite render target");
		}
	}

	public GlSampler getAltSampler() {
		if (mipmapsOnAlt) {
			return allowsLinear ? GlSampler.MIPPED_LINEAR : GlSampler.MIPPED_NEAREST;
		}
		return allowsLinear ? GlSampler.LINEAR : GlSampler.NEAREST;
	}

	public GlSampler getMainSampler() {
		if (mipmapsOnMain) {
			return allowsLinear ? GlSampler.MIPPED_LINEAR : GlSampler.MIPPED_NEAREST;
		}
		return allowsLinear ? GlSampler.LINEAR : GlSampler.NEAREST;
	}

	public void generateMipmaps(boolean alt) {
		this.requireValid();
		Iris.getRenderBackend().commands().generateMipmaps(alt ? this.altGpuTexture : this.mainGpuTexture, this.allowsLinear);
		this.turnOnMips(alt);
	}

	public void turnOnMips(boolean alt) {
		if (alt) {
			this.mipmapsOnAlt = true;
		} else {
			this.mipmapsOnMain = true;
		}
	}

	public void turnOffMips(boolean alt) {
		if (alt) {
			this.mipmapsOnAlt = false;
		} else {
			this.mipmapsOnMain = false;
		}
	}

	public static class Builder {
		private InternalTextureFormat internalFormat = InternalTextureFormat.RGBA8;
		private int width = 0;
		private int height = 0;
		private PixelFormat format = PixelFormat.RGBA;
		private PixelType type = PixelType.UNSIGNED_BYTE;
		private String name = null;

		private Builder() {
			// No-op
		}

		public Builder setName(String name) {
			this.name = name;

			return this;
		}

		public Builder setInternalFormat(InternalTextureFormat format) {
			this.internalFormat = format;

			return this;
		}

		public Builder setDimensions(int width, int height) {
			if (width <= 0) {
				throw new IllegalArgumentException("Width must be greater than zero");
			}

			if (height <= 0) {
				throw new IllegalArgumentException("Height must be greater than zero");
			}

			this.width = width;
			this.height = height;

			return this;
		}

		public Builder setPixelFormat(PixelFormat pixelFormat) {
			this.format = pixelFormat;

			return this;
		}

		public Builder setPixelType(PixelType pixelType) {
			this.type = pixelType;

			return this;
		}

		public RenderTarget build() {
			return new RenderTarget(this);
		}
	}
}
