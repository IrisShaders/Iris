package net.coderbot.iris.postprocess.target;

import java.nio.ByteBuffer;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import org.lwjgl.opengl.GL11C;

public class CompositeRenderTarget {
	private final int mainTexture;
	private final int altTexture;

	private CompositeRenderTarget(int mainTexture, int altTexture) {
		this.mainTexture = mainTexture;
		this.altTexture = altTexture;
	}

	public int getMainTexture() {
		return mainTexture;
	}

	public int getAltTexture() {
		return altTexture;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private InternalTextureFormat internalFormat = InternalTextureFormat.RGBA;
		private int width = 0;
		private int height = 0;
		private PixelFormat format = PixelFormat.BGRA;
		private PixelType type = PixelType.UNSIGNED_INT_8_8_8_8_REV;

		private static final ByteBuffer NULL_BUFFER = null;

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

		public CompositeRenderTarget build() {
			return new CompositeRenderTarget(createTexture(), createTexture());
		}

		private int createTexture() {
			int texture = GL11C.glGenTextures();

			GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, internalFormat.getGlFormat(), width, height, 0, format.getGlFormat(), type.getGlFormat(), NULL_BUFFER);

			return texture;
		}
	}
}
