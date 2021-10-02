package net.coderbot.iris.gl.texture;

import java.util.Optional;

import net.coderbot.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

public enum PixelFormat {
	RED(GL11C.GL_RED, GlVersion.GL_11),
	RG(GL30C.GL_RG, GlVersion.GL_30),
	RGB(GL11C.GL_RGB, GlVersion.GL_11),
	BGR(GL12C.GL_BGR, GlVersion.GL_12),
	RGBA(GL11C.GL_RGBA, GlVersion.GL_11),
	BGRA(GL12C.GL_BGRA, GlVersion.GL_12),
	RED_INTEGER(GL30C.GL_RED_INTEGER, GlVersion.GL_30),
	RG_INTEGER(GL30C.GL_RG_INTEGER, GlVersion.GL_30),
	RGB_INTEGER(GL30C.GL_RGB_INTEGER, GlVersion.GL_30),
	BGR_INTEGER(GL30C.GL_BGR_INTEGER, GlVersion.GL_30),
	RGBA_INTEGER(GL30C.GL_RGBA_INTEGER, GlVersion.GL_30),
	BGRA_INTEGER(GL30C.GL_BGRA_INTEGER, GlVersion.GL_30);

	private final int glFormat;
	private final GlVersion minimumGlVersion;

	PixelFormat(int glFormat, GlVersion minimumGlVersion) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
	}

	public static Optional<PixelFormat> fromString(String name) {
		try {
			return Optional.of(PixelFormat.valueOf(name));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public static Optional<PixelFormat> fromShaderpackString(String name) {
		switch(name) {
			case "R8":
			case "R8_SNORM":
			case "R16_SNORM":
			case "R32F":
				return Optional.of(PixelFormat.RED);

			case "RG8":
			case "RG8_SNORM":
			case "RG16_SNORM":
			case "RG32F":
				return Optional.of(PixelFormat.RG);

			case "RGB8":
			case "RGB8_SNORM":
			case "RGB16_SNORM":
			case "RGB32F":
			case "R3_G3_B2":
			case "R11F_G11F_B10F":
			case "RGB9_E5":
				return Optional.of(PixelFormat.RGB);

			case "RGBA8":
			case "RGBA8_SNORM":
			case "RGBA16_SNORM":
			case "RGBA32F":
			case "RGB5_A1":
			case "RGB10_A2":
				return Optional.of(PixelFormat.RGBA);

			case "R8I":
			case "R8UI":
			case "R16I":
			case "R16UI":
			case "R32I":
			case "R32UI":
				return Optional.of(PixelFormat.RED_INTEGER);

			case "RG8I":
			case "RG8UI":
			case "RG16I":
			case "RG16UI":
			case "RG32I":
			case "RG32UI":
				return Optional.of(PixelFormat.RG_INTEGER);

			case "RGB8I":
			case "RGB8UI":
			case "RGB16I":
			case "RGB16UI":
			case "RGB32I":
			case "RGB32UI":
				return Optional.of(PixelFormat.RGB_INTEGER);

			case "RGBA8I":
			case "RGBA8UI":
			case "RGBA16I":
			case "RGBA16UI":
			case "RGBA32I":
			case "RGBA32UI":
				return Optional.of(PixelFormat.RGBA_INTEGER);
		}

		return Optional.empty();
	}

	public int getGlFormat() {
		return glFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}
}
