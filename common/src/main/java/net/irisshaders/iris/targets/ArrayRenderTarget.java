package net.irisshaders.iris.targets;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArrayRenderTarget {
	private static final ByteBuffer NULL_BUFFER = null;
	private final InternalTextureFormat internalFormat;
	private final PixelFormat format;
	private final PixelType type;
	private final int mainTexture;
	private final int altTexture;
	private int width;
	private int height;
	private int layers;
	private boolean isValid;

	public ArrayRenderTarget(Builder builder) {
		this.isValid = true;

		this.internalFormat = builder.internalFormat;
		this.format = builder.format;
		this.type = builder.type;

		this.width = builder.width;
		this.height = builder.height;
		this.layers = builder.layers;

		int[] textures = new int[2];
		IrisRenderSystem.createTextures(GL32C.GL_TEXTURE_2D_ARRAY, textures);

		this.mainTexture = textures[0];
		this.altTexture = textures[1];

		boolean isPixelFormatInteger = builder.internalFormat.getPixelFormat().isInteger();
		setupTexture(mainTexture, builder.width, builder.height, builder.layers, !isPixelFormatInteger);
		setupTexture(altTexture, builder.width, builder.height, builder.layers, !isPixelFormatInteger);

		if (builder.name != null) {
			GLDebug.nameObject(GL43C.GL_TEXTURE, mainTexture, builder.name + " main");
			GLDebug.nameObject(GL43C.GL_TEXTURE, mainTexture, builder.name + " alt");
		}

		// Clean up after ourselves
		// This is strictly defensive to ensure that other buggy code doesn't tamper with our textures
		GlStateManager._bindTexture(0);
	}

	public static Builder builder() {
		return new Builder();
	}

	private void setupTexture(int texture, int width, int height, int layers, boolean allowsLinear) {
		resizeTexture(texture, width, height, layers);

		IrisRenderSystem.texParameteri(texture, GL32C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_MIN_FILTER, allowsLinear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL32C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_MAG_FILTER, allowsLinear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL32C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		IrisRenderSystem.texParameteri(texture, GL32C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
	}

	private void resizeTexture(int texture, int width, int height, int layers) {
		IrisRenderSystem.texImage3D(texture, GL32C.GL_TEXTURE_2D_ARRAY, 0, internalFormat.getGlFormat(), width, height, layers, 0, format.getGlFormat(), type.getGlFormat(), NULL_BUFFER);
	}

	void resize(Vector2i textureScaleOverride) {
		this.resize(textureScaleOverride.x, textureScaleOverride.y, layers);
	}

	// Package private, call CompositeRenderTargets#resizeIfNeeded instead.
	void resize(int width, int height, int layers) {
		requireValid();

		this.width = width;
		this.height = height;

		resizeTexture(mainTexture, width, height, layers);

		resizeTexture(altTexture, width, height, layers);
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

	public static class Builder {
		private InternalTextureFormat internalFormat = InternalTextureFormat.RGBA8;
		private int width = 0;
		private int height = 0;
		private int layers = 0;
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

		public Builder setDimensions(int width, int height, int layers) {
			if (width <= 0) {
				throw new IllegalArgumentException("Width must be greater than zero");
			}

			if (height <= 0) {
				throw new IllegalArgumentException("Height must be greater than zero");
			}

			if (layers <= 0) {
				throw new IllegalArgumentException("Layer count must be greater than zero");
			}

			this.width = width;
			this.height = height;
			this.layers = layers;

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

		public ArrayRenderTarget build() {
			return new ArrayRenderTarget(this);
		}
	}
}
