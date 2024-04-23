package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import java.util.Locale;
import java.util.Optional;

public enum PixelFormat {
	RED(GL11C.GL_RED, GlVersion.GL_11, false),
	RG(GL30C.GL_RG, GlVersion.GL_30, false),
	RGB(GL11C.GL_RGB, GlVersion.GL_11, false),
	BGR(GL12C.GL_BGR, GlVersion.GL_12, false),
	RGBA(GL11C.GL_RGBA, GlVersion.GL_11, false),
	BGRA(GL12C.GL_BGRA, GlVersion.GL_12, false),
	RED_INTEGER(GL30C.GL_RED_INTEGER, GlVersion.GL_30, true),
	RG_INTEGER(GL30C.GL_RG_INTEGER, GlVersion.GL_30, true),
	RGB_INTEGER(GL30C.GL_RGB_INTEGER, GlVersion.GL_30, true),
	BGR_INTEGER(GL30C.GL_BGR_INTEGER, GlVersion.GL_30, true),
	RGBA_INTEGER(GL30C.GL_RGBA_INTEGER, GlVersion.GL_30, true),
	BGRA_INTEGER(GL30C.GL_BGRA_INTEGER, GlVersion.GL_30, true);

	private final int glFormat;
	private final GlVersion minimumGlVersion;
	private final boolean isInteger;

	PixelFormat(int glFormat, GlVersion minimumGlVersion, boolean isInteger) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
		this.isInteger = isInteger;
	}

	public static Optional<PixelFormat> fromString(String name) {
		try {
			return Optional.of(PixelFormat.valueOf(name.toUpperCase(Locale.US)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public int getGlFormat() {
		return glFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}

	public boolean isInteger() {
		return isInteger;
	}
}
