package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import java.util.Locale;
import java.util.Optional;

public enum PixelType {
	BYTE(GL11C.GL_BYTE, GlVersion.GL_11),
	SHORT(GL11C.GL_SHORT, GlVersion.GL_11),
	INT(GL11C.GL_INT, GlVersion.GL_11),
	HALF_FLOAT(GL30C.GL_HALF_FLOAT, GlVersion.GL_30),
	FLOAT(GL11C.GL_FLOAT, GlVersion.GL_11),
	UNSIGNED_BYTE(GL11C.GL_UNSIGNED_BYTE, GlVersion.GL_11),
	UNSIGNED_BYTE_3_3_2(GL12C.GL_UNSIGNED_BYTE_3_3_2, GlVersion.GL_12),
	UNSIGNED_BYTE_2_3_3_REV(GL12C.GL_UNSIGNED_BYTE_2_3_3_REV, GlVersion.GL_12),
	UNSIGNED_SHORT(GL11C.GL_UNSIGNED_SHORT, GlVersion.GL_11),
	UNSIGNED_SHORT_5_6_5(GL12C.GL_UNSIGNED_SHORT_5_6_5, GlVersion.GL_12),
	UNSIGNED_SHORT_5_6_5_REV(GL12C.GL_UNSIGNED_SHORT_5_6_5_REV, GlVersion.GL_12),
	UNSIGNED_SHORT_4_4_4_4(GL12C.GL_UNSIGNED_SHORT_4_4_4_4, GlVersion.GL_12),
	UNSIGNED_SHORT_4_4_4_4_REV(GL12C.GL_UNSIGNED_SHORT_4_4_4_4_REV, GlVersion.GL_12),
	UNSIGNED_SHORT_5_5_5_1(GL12C.GL_UNSIGNED_SHORT_5_5_5_1, GlVersion.GL_12),
	UNSIGNED_SHORT_1_5_5_5_REV(GL12C.GL_UNSIGNED_SHORT_1_5_5_5_REV, GlVersion.GL_12),
	UNSIGNED_INT(GL11C.GL_UNSIGNED_INT, GlVersion.GL_11),
	UNSIGNED_INT_8_8_8_8(GL12C.GL_UNSIGNED_INT_8_8_8_8, GlVersion.GL_12),
	UNSIGNED_INT_8_8_8_8_REV(GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, GlVersion.GL_12),
	UNSIGNED_INT_10_10_10_2(GL12C.GL_UNSIGNED_INT_10_10_10_2, GlVersion.GL_12),
	UNSIGNED_INT_2_10_10_10_REV(GL12C.GL_UNSIGNED_INT_2_10_10_10_REV, GlVersion.GL_12);

	private final int glFormat;
	private final GlVersion minimumGlVersion;

	PixelType(int glFormat, GlVersion minimumGlVersion) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
	}

	public static Optional<PixelType> fromString(String name) {
		try {
			return Optional.of(PixelType.valueOf(name.toUpperCase(Locale.US)));
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
