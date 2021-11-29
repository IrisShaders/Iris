package net.coderbot.iris.shaderpack.texture;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.minecraft.resources.ResourceLocation;

public abstract class CustomTextureData {
	private CustomTextureData() {

	}

	public static final class PngData extends CustomTextureData {
		private final TextureFilteringData filteringData;
		private final byte[] content;

		public PngData(TextureFilteringData filteringData, byte[] content) {
			this.filteringData = filteringData;
			this.content = content;
		}

		public TextureFilteringData getFilteringData() {
			return filteringData;
		}

		public byte[] getContent() {
			return content;
		}
	}

	public static final class ResourceData extends CustomTextureData {
		private final ResourceLocation resourceLocation;

		public ResourceData(ResourceLocation resourceLocation) {
			this.resourceLocation = resourceLocation;
		}

		public ResourceLocation getResourceLocation() {
			return resourceLocation;
		}
	}

	public abstract static class RawData extends CustomTextureData {
		private final byte[] content;
		private final InternalTextureFormat internalFormat;
		private final PixelFormat pixelFormat;
		private final PixelType pixelType;

		private RawData(byte[] content, InternalTextureFormat internalFormat,
						PixelFormat pixelFormat, PixelType pixelType) {
			this.content = content;
			this.internalFormat = internalFormat;
			this.pixelFormat = pixelFormat;
			this.pixelType = pixelType;
		}

		public final byte[] getContent() {
			return content;
		}

		public final InternalTextureFormat getInternalFormat() {
			return internalFormat;
		}

		public final PixelFormat getPixelFormat() {
			return pixelFormat;
		}

		public final PixelType getPixelType() {
			return pixelType;
		}
	}

	public static final class RawData1D extends RawData {
		private final int sizeX;

		private RawData1D(byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX) {
			super(content, internalFormat, pixelFormat, pixelType);

			this.sizeX = sizeX;
		}

		public int getSizeX() {
			return sizeX;
		}
	}

	public static final class RawData2D extends RawData {
		int sizeX;
		int sizeY;

		private RawData2D(byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX, int sizeY) {
			super(content, internalFormat, pixelFormat, pixelType);

			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}

		public int getSizeX() {
			return sizeX;
		}

		public int getSizeY() {
			return sizeY;
		}
	}

	public static final class RawData3D extends RawData {
		int sizeX;
		int sizeY;
		int sizeZ;

		private RawData3D(byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX, int sizeY, int sizeZ) {
			super(content, internalFormat, pixelFormat, pixelType);

			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;
		}

		public int getSizeX() {
			return sizeX;
		}

		public int getSizeY() {
			return sizeY;
		}

		public int getSizeZ() {
			return sizeZ;
		}
	}
}
