package net.coderbot.iris.gl.texture;

import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;

import java.nio.ByteBuffer;
import java.util.Optional;

public enum TextureType {
	TEXTURE_1D(GL30C.GL_TEXTURE_1D),
	TEXTURE_2D(GL30C.GL_TEXTURE_2D),
	TEXTURE_3D(GL30C.GL_TEXTURE_3D),
	TEXTURE_RECTANGLE(GL30C.GL_TEXTURE_3D);

	private final int glType;

	TextureType(int glType) {
		this.glType = glType;
	}

	public int getGlType() {
		return glType;
	}

	public void apply(int texture, int sizeX, int sizeY, int sizeZ, int internalFormat, int format, int pixelType, long pixels) {
		switch (this) {
			case TEXTURE_1D:
				GL46C.glTextureStorage1D(texture, 1, internalFormat, sizeX);
				if (pixels != 0L) {
					GL46C.glTextureSubImage1D(texture, 0, 0, sizeX, format, pixelType, pixels);
				}
				break;
			case TEXTURE_2D, TEXTURE_RECTANGLE:
				GL46C.glTextureStorage2D(texture, 1, internalFormat, sizeX, sizeY);
				if (pixels != 0L) {
					GL46C.glTextureSubImage2D(texture, 0, 0, 0, sizeX, sizeY, format, pixelType, pixels);
				}
				break;
			case TEXTURE_3D:
				GL46C.glTextureStorage3D(texture, 1, internalFormat, sizeX, sizeY, sizeZ);
				if (pixels != 0L) {
					GL46C.glTextureSubImage3D(texture, 0, 0, 0, 0, sizeX, sizeY, sizeZ, format, pixelType, pixels);
				}
				break;
		}
	}

	public static Optional<TextureType> fromString(String name) {
		try {
			return Optional.of(TextureType.valueOf(name));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
