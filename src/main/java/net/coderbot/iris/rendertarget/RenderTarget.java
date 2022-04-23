package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.nio.IntBuffer;

public class RenderTarget {
	private final InternalTextureFormat internalFormat;
	private final PixelFormat format;
	private final PixelType type;

	private boolean isValid;
	private final int mainTexture;
	private final int altTexture;

	private static final IntBuffer NULL_BUFFER = null;

	private RenderTarget(Builder builder) {
		this.isValid = true;

		this.internalFormat = builder.internalFormat;
		this.format = builder.format;
		this.type = builder.type;

		int[] textures = new int[2];
		GlStateManager._genTextures(textures);

		this.mainTexture = textures[0];
		this.altTexture = textures[1];

		GlStateManager._bindTexture(mainTexture);
		setupCurrentlyBoundTexture(builder.width, builder.height);

		GlStateManager._bindTexture(altTexture);
		setupCurrentlyBoundTexture(builder.width, builder.height);

		// Clean up after ourselves
		// This is strictly defensive to ensure that other buggy code doesn't tamper with our textures
		GlStateManager._bindTexture(0);
	}

	private void setupCurrentlyBoundTexture(int width, int height) {
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);

		resizeCurrentlyBoundTexture(width, height);
	}

	private void resizeCurrentlyBoundTexture(int width, int height) {
		GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, internalFormat.getGlFormat(), width, height, 0, format.getGlFormat(), type.getGlFormat(), NULL_BUFFER);
	}

	// Package private, call CompositeRenderTargets#resizeIfNeeded instead.
	void resize(int width, int height) {
		requireValid();

		GlStateManager._bindTexture(mainTexture);
		resizeCurrentlyBoundTexture(width, height);

		GlStateManager._bindTexture(altTexture);
		resizeCurrentlyBoundTexture(width, height);
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
