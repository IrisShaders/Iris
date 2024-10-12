package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import java.util.Locale;
import java.util.Optional;

public enum PixelType {
	BYTE(1, GL11C.GL_BYTE, GlVersion.GL_11),
	SHORT(2, GL11C.GL_SHORT, GlVersion.GL_11),
	INT(4, GL11C.GL_INT, GlVersion.GL_11),
	HALF_FLOAT(2, GL30C.GL_HALF_FLOAT, GlVersion.GL_30),
	FLOAT(4, GL11C.GL_FLOAT, GlVersion.GL_11),
	UNSIGNED_BYTE(1, GL11C.GL_UNSIGNED_BYTE, GlVersion.GL_11),
	UNSIGNED_BYTE_3_3_2(1, GL12C.GL_UNSIGNED_BYTE_3_3_2, GlVersion.GL_12),
	UNSIGNED_BYTE_2_3_3_REV(1, GL12C.GL_UNSIGNED_BYTE_2_3_3_REV, GlVersion.GL_12),
	UNSIGNED_SHORT(2, GL11C.GL_UNSIGNED_SHORT, GlVersion.GL_11),
	UNSIGNED_SHORT_5_6_5(2, GL12C.GL_UNSIGNED_SHORT_5_6_5, GlVersion.GL_12),
	UNSIGNED_SHORT_5_6_5_REV(2, GL12C.GL_UNSIGNED_SHORT_5_6_5_REV, GlVersion.GL_12),
	UNSIGNED_SHORT_4_4_4_4(2, GL12C.GL_UNSIGNED_SHORT_4_4_4_4, GlVersion.GL_12),
	UNSIGNED_SHORT_4_4_4_4_REV(2, GL12C.GL_UNSIGNED_SHORT_4_4_4_4_REV, GlVersion.GL_12),
	UNSIGNED_SHORT_5_5_5_1(2, GL12C.GL_UNSIGNED_SHORT_5_5_5_1, GlVersion.GL_12),
	UNSIGNED_SHORT_1_5_5_5_REV(2, GL12C.GL_UNSIGNED_SHORT_1_5_5_5_REV, GlVersion.GL_12),
	UNSIGNED_INT(4, GL11C.GL_UNSIGNED_INT, GlVersion.GL_11),
	UNSIGNED_INT_8_8_8_8(4, GL12C.GL_UNSIGNED_INT_8_8_8_8, GlVersion.GL_12),
	UNSIGNED_INT_8_8_8_8_REV(4, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, GlVersion.GL_12),
	UNSIGNED_INT_10_10_10_2(4, GL12C.GL_UNSIGNED_INT_10_10_10_2, GlVersion.GL_12),
	UNSIGNED_INT_2_10_10_10_REV(4, GL12C.GL_UNSIGNED_INT_2_10_10_10_REV, GlVersion.GL_12),
	UNSIGNED_INT_10F_11F_11F_REV(4, GL30C.GL_UNSIGNED_INT_10F_11F_11F_REV, GlVersion.GL_30),
	UNSIGNED_INT_5_9_9_9_REV(4, GL30C.GL_UNSIGNED_INT_5_9_9_9_REV, GlVersion.GL_30);

	private final int byteSize;
	private final int glFormat;
	private final GlVersion minimumGlVersion;

	PixelType(int byteSize, int glFormat, GlVersion minimumGlVersion) {
		this.byteSize = byteSize;
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
	}

	public static Optional<PixelType> fromString(String name) {
		try {
			return Optional.of(PixelType.valueOf(name.toUpperCase(Locale.US)));
		} catch (IllegalArgumentException e) {
			Iris.logger.error("Failed to find pixel type " + name.toUpperCase(Locale.ROOT));
			return Optional.empty();
		}
	}

	public int getByteSize() {
		return byteSize;
	}

	public int getGlFormat() {
		return glFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}
}
