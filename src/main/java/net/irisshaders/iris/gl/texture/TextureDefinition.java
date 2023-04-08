package net.irisshaders.iris.gl.texture;

public class TextureDefinition {
	protected String name;

	public String getName() {
		return name;
	}

	public static class PNGDefinition extends TextureDefinition {
		public PNGDefinition(String name) {
			this.name = name;
		}
	}

	public static class RawDefinition extends TextureDefinition {
		private final TextureType target;
		private final int sizeX;
		private final int sizeY;
		private final int sizeZ;
		private final InternalTextureFormat internalFormat;
		private final PixelFormat format;
		private final PixelType pixelType;

		public RawDefinition(String path, TextureType target, InternalTextureFormat internalFormat, int sizeX, int sizeY, int sizeZ, PixelFormat format, PixelType pixelType) {
			this.name = path;
			this.target = target;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;
			this.internalFormat = internalFormat;
			this.format = format;
			this.pixelType = pixelType;
		}

		public TextureType getTarget() {
			return target;
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

		public PixelFormat getFormat() {
			return format;
		}

		public InternalTextureFormat getInternalFormat() {
			return internalFormat;
		}

		public PixelType getPixelType() {
			return pixelType;
		}
	}
}
