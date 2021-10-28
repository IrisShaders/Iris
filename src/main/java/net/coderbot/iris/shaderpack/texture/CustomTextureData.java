package net.coderbot.iris.shaderpack.texture;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;

public abstract class CustomTextureData {
	private final TextureFilteringData filteringData;

	private CustomTextureData(TextureFilteringData filteringData) {
		this.filteringData = filteringData;
	}

	public final TextureFilteringData getFilteringData() {
		return filteringData;
	}

	public static final class PngData extends CustomTextureData {
		private final byte[] content;

		public PngData(TextureFilteringData filteringData, byte[] content) {
			super(filteringData);

			this.content = content;
		}

		public byte[] getContent() {
			return content;
		}
	}

	public static final class ResourceData extends CustomTextureData {
		private final String namespace;
		private final String location;

		public ResourceData(TextureFilteringData filteringData, String namespace, String location) {
			super(filteringData);

			this.namespace = namespace;
			this.location = location;
		}

		public String getNamespace() {
			return namespace;
		}

		public String getLocation() {
			return location;
		}
	}

	public abstract static class RawData extends CustomTextureData {
		private final byte[] content;
		private final InternalTextureFormat internalFormat;
		private final PixelFormat pixelFormat;
		private final PixelType pixelType;

		private RawData(TextureFilteringData filteringData, byte[] content, InternalTextureFormat internalFormat,
						PixelFormat pixelFormat, PixelType pixelType) {
			super(filteringData);

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

		private RawData1D(TextureFilteringData filteringData, byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX) {
			super(filteringData, content, internalFormat, pixelFormat, pixelType);

			this.sizeX = sizeX;
		}

		public int getSizeX() {
			return sizeX;
		}
	}

	public static final class RawData2D extends RawData {
		int sizeX;
		int sizeY;

		private RawData2D(TextureFilteringData filteringData, byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX, int sizeY) {
			super(filteringData, content, internalFormat, pixelFormat, pixelType);

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

		private RawData3D(TextureFilteringData filteringData, byte[] content, InternalTextureFormat internalFormat,
						  PixelFormat pixelFormat, PixelType pixelType, int sizeX, int sizeY, int sizeZ) {
			super(filteringData, content, internalFormat, pixelFormat, pixelType);

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
