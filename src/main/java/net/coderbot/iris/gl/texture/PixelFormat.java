package net.coderbot.iris.gl.texture;

import net.coderbot.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import java.util.Optional;

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

	public int getGlFormat() {
		return glFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}
}
