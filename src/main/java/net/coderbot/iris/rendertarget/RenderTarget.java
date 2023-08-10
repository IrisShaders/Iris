package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL46C;

import java.nio.ByteBuffer;

public class RenderTarget {
	private InternalTextureFormat internalFormat;
	private final PixelFormat format;
	private final PixelType type;
	private int width;
	private int height;

	private boolean isValid;
	private int mainTexture;
	private int altTexture;
	private int mainTextureMipView;
	private int altTextureMipView;

	private static final ByteBuffer NULL_BUFFER = null;
	private boolean mipmapping;

	public RenderTarget(Builder builder) {
		this.isValid = true;

		this.internalFormat = builder.internalFormat;

		if (internalFormat == InternalTextureFormat.RGBA) internalFormat = InternalTextureFormat.RGBA8;

		this.format = builder.format;
		this.type = builder.type;

		this.width = builder.width;
		this.height = builder.height;

		boolean isPixelFormatInteger = builder.internalFormat.getPixelFormat().isInteger();
		setupTexture(false, builder.width, builder.height, !isPixelFormatInteger);
		setupTexture(true, builder.width, builder.height, !isPixelFormatInteger);

		// Clean up after ourselves
		// This is strictly defensive to ensure that other buggy code doesn't tamper with our textures
		GlStateManager._bindTexture(0);
	}

	private void setupTexture(boolean alt, int width, int height, boolean allowsLinear) {
		resizeTexture(alt, width, height);

		int texture = (alt ? altTexture : mainTexture);

		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MIN_FILTER, allowsLinear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MAG_FILTER, allowsLinear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
	}

	public void setMipmapping(boolean enabled) {
		this.mipmapping = enabled;
	}

	private void resizeTexture(boolean alt, int width, int height) {
		if (this.mainTexture != 0) GlStateManager._deleteTexture(mainTexture);
		if (this.altTexture != 0) GlStateManager._deleteTexture(altTexture);

		if (alt) {
			this.altTexture = IrisRenderSystem.createTexture(GL46C.GL_TEXTURE_2D);
		} else {
			this.mainTexture = IrisRenderSystem.createTexture(GL46C.GL_TEXTURE_2D);
		}

		GL46C.glTextureStorage2D(alt ? altTexture : mainTexture, 4, internalFormat.getGlFormat(), width, height);
	}

	void resize(Vector2i textureScaleOverride) {
		this.resize(textureScaleOverride.x, textureScaleOverride.y);
	}

	// Package private, call CompositeRenderTargets#resizeIfNeeded instead.
	void resize(int width, int height) {
		requireValid();

		this.width = width;
		this.height = height;

		setupTexture(false, width, height, !internalFormat.getPixelFormat().isInteger());

		setupTexture(true, width, height, !internalFormat.getPixelFormat().isInteger());
	}

	public InternalTextureFormat getInternalFormat() {
		return internalFormat;
	}

	public int getMainTexture() {
		requireValid();

		return mainTexture;
	}

	public int getAltTexture() {
		requireValid();

		return altTexture;
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

		GlStateManager._deleteTextures(new int[]{mainTexture, altTexture});
	}

	private void requireValid() {
		if (!isValid) {
			throw new IllegalStateException("Attempted to use a deleted composite render target");
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private InternalTextureFormat internalFormat = InternalTextureFormat.RGBA8;
		private int width = 0;
		private int height = 0;
		private PixelFormat format = PixelFormat.RGBA;
		private PixelType type = PixelType.UNSIGNED_BYTE;

		private Builder() {
			// No-op
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
