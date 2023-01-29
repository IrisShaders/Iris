package net.coderbot.iris.gl.texture;

import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL30C;

import java.nio.ByteBuffer;

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

	public void apply(int texture, int sizeX, int sizeY, int sizeZ, int internalFormat, int format, int pixelType, ByteBuffer pixels) {
		switch (this) {
			case TEXTURE_1D:
				IrisRenderSystem.texImage1D(texture, getGlType(), 0, internalFormat, sizeX, 0, format, pixelType, pixels);
				break;
			case TEXTURE_2D, TEXTURE_RECTANGLE:
				IrisRenderSystem.texImage2D(texture, getGlType(), 0, internalFormat, sizeX, sizeY, 0, format, pixelType, pixels);
				break;
			case TEXTURE_3D:
				IrisRenderSystem.texImage3D(texture, getGlType(), 0, internalFormat, sizeX, sizeY, sizeZ, 0, format, pixelType, pixels);
				break;
		}
	}
}
