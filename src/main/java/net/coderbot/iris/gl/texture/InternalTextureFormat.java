package net.coderbot.iris.gl.texture;

import java.util.Optional;

import net.coderbot.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;

public enum InternalTextureFormat {
	// Default
	// TODO: This technically shouldn't be exposed to shaders since it's not in the specification, it's the default anyways
	RGBA(GL11C.GL_RGBA, GlVersion.GL_11),
	// 8-bit normalized
	R8(GL30C.GL_R8, GlVersion.GL_30),
	RG8(GL30C.GL_RG8, GlVersion.GL_30),
	RGB8(GL11C.GL_RGB8, GlVersion.GL_11),
	RGBA8(GL11C.GL_RGBA8, GlVersion.GL_11),
	// 8-bit signed normalized
	R8_SNORM(GL31C.GL_R8_SNORM, GlVersion.GL_31),
	RG8_SNORM(GL31C.GL_RG8_SNORM, GlVersion.GL_31),
	RGB8_SNORM(GL31C.GL_RGB8_SNORM, GlVersion.GL_31),
	RGBA8_SNORM(GL31C.GL_RGBA8_SNORM, GlVersion.GL_31),
	// 16-bit normalized
	R16(GL30C.GL_R16, GlVersion.GL_30),
	RG16(GL30C.GL_RG16, GlVersion.GL_30),
	RGB16(GL11C.GL_RGB16, GlVersion.GL_11),
	RGBA16(GL11C.GL_RGBA16, GlVersion.GL_11),
	// 16-bit signed normalized
	R16_SNORM(GL31C.GL_R16_SNORM, GlVersion.GL_31),
	RG16_SNORM(GL31C.GL_RG16_SNORM, GlVersion.GL_31),
	RGB16_SNORM(GL31C.GL_RGB16_SNORM, GlVersion.GL_31),
	RGBA16_SNORM(GL31C.GL_RGBA16_SNORM, GlVersion.GL_31),
	// 16-bit float
	R16F(GL30C.GL_R16F, GlVersion.GL_30),
	RG16F(GL30C.GL_RG16F, GlVersion.GL_30),
	RGB16F(GL30C.GL_RGB16F, GlVersion.GL_30),
	RGBA16F(GL30C.GL_RGBA16F, GlVersion.GL_30),
	// 32-bit float
	R32F(GL30C.GL_R32F, GlVersion.GL_30),
	RG32F(GL30C.GL_RG32F, GlVersion.GL_30),
	RGB32F(GL30C.GL_RGB32F, GlVersion.GL_30),
	RGBA32F(GL30C.GL_RGBA32F, GlVersion.GL_30),
	// 32-bit integer
	R32I(GL30C.GL_R32I, GlVersion.GL_30),
	RG32I(GL30C.GL_RG32I, GlVersion.GL_30),
	RGB32I(GL30C.GL_RGB32I, GlVersion.GL_30),
	RGBA32I(GL30C.GL_RGBA32I, GlVersion.GL_30),
	// 32-bit unsigned integer
	R32UI(GL30C.GL_R32UI, GlVersion.GL_30),
	RG32UI(GL30C.GL_RG32UI, GlVersion.GL_30),
	RGB32UI(GL30C.GL_RGB32UI, GlVersion.GL_30),
	RGBA32UI(GL30C.GL_RGBA32UI, GlVersion.GL_30),
	// Mixed
	R3_G3_B2(GL11C.GL_R3_G3_B2, GlVersion.GL_11),
	RGB5_A1(GL11C.GL_RGB5_A1, GlVersion.GL_11),
	RGB10_A2(GL11C.GL_RGB10_A2, GlVersion.GL_11),
	R11F_G11F_B10F(GL30C.GL_R11F_G11F_B10F, GlVersion.GL_30),
	RGB9_E5(GL30C.GL_RGB9_E5, GlVersion.GL_30);

	private final int glFormat;
	private final GlVersion minimumGlVersion;

	InternalTextureFormat(int glFormat, GlVersion minimumGlVersion) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
	}

	public static Optional<InternalTextureFormat> fromString(String name) {
		try {
			return Optional.of(InternalTextureFormat.valueOf(name));
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
