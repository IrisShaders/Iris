package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;

import java.util.Locale;
import java.util.Optional;

public enum InternalTextureFormat {
	// Default
	// TODO: This technically shouldn't be exposed to shaders since it's not in the specification, it's the default anyways
	RGBA(GL11C.GL_RGBA, GlVersion.GL_11, PixelFormat.RGBA),
	// 8-bit normalized
	R8(GL30C.GL_R8, GlVersion.GL_30, PixelFormat.RED),
	RG8(GL30C.GL_RG8, GlVersion.GL_30, PixelFormat.RG),
	RGB8(GL11C.GL_RGB8, GlVersion.GL_11, PixelFormat.RGB),
	RGBA8(GL11C.GL_RGBA8, GlVersion.GL_11, PixelFormat.RGBA),
	// 8-bit signed normalized
	R8_SNORM(GL31C.GL_R8_SNORM, GlVersion.GL_31, PixelFormat.RED),
	RG8_SNORM(GL31C.GL_RG8_SNORM, GlVersion.GL_31, PixelFormat.RG),
	RGB8_SNORM(GL31C.GL_RGB8_SNORM, GlVersion.GL_31, PixelFormat.RGB),
	RGBA8_SNORM(GL31C.GL_RGBA8_SNORM, GlVersion.GL_31, PixelFormat.RGBA),
	// 16-bit normalized
	R16(GL30C.GL_R16, GlVersion.GL_30, PixelFormat.RED),
	RG16(GL30C.GL_RG16, GlVersion.GL_30, PixelFormat.RG),
	RGB16(GL11C.GL_RGB16, GlVersion.GL_11, PixelFormat.RGB),
	RGBA16(GL11C.GL_RGBA16, GlVersion.GL_11, PixelFormat.RGBA),
	// 16-bit signed normalized
	R16_SNORM(GL31C.GL_R16_SNORM, GlVersion.GL_31, PixelFormat.RED),
	RG16_SNORM(GL31C.GL_RG16_SNORM, GlVersion.GL_31, PixelFormat.RG),
	RGB16_SNORM(GL31C.GL_RGB16_SNORM, GlVersion.GL_31, PixelFormat.RGB),
	RGBA16_SNORM(GL31C.GL_RGBA16_SNORM, GlVersion.GL_31, PixelFormat.RGBA),
	// 16-bit float
	R16F(GL30C.GL_R16F, GlVersion.GL_30, PixelFormat.RED),
	RG16F(GL30C.GL_RG16F, GlVersion.GL_30, PixelFormat.RG),
	RGB16F(GL30C.GL_RGB16F, GlVersion.GL_30, PixelFormat.RGB),
	RGBA16F(GL30C.GL_RGBA16F, GlVersion.GL_30, PixelFormat.RGBA),
	// 32-bit float
	R32F(GL30C.GL_R32F, GlVersion.GL_30, PixelFormat.RED),
	RG32F(GL30C.GL_RG32F, GlVersion.GL_30, PixelFormat.RG),
	RGB32F(GL30C.GL_RGB32F, GlVersion.GL_30, PixelFormat.RGB),
	RGBA32F(GL30C.GL_RGBA32F, GlVersion.GL_30, PixelFormat.RGBA),
	// 8-bit integer
	R8I(GL30C.GL_R8I, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG8I(GL30C.GL_RG8I, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB8I(GL30C.GL_RGB8I, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA8I(GL30C.GL_RGBA8I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// 8-bit unsigned integer
	R8UI(GL30C.GL_R8UI, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG8UI(GL30C.GL_RG8UI, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB8UI(GL30C.GL_RGB8UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA8UI(GL30C.GL_RGBA8UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// 16-bit integer
	R16I(GL30C.GL_R16I, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG16I(GL30C.GL_RG16I, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB16I(GL30C.GL_RGB16I, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA16I(GL30C.GL_RGBA16I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// 16-bit unsigned integer
	R16UI(GL30C.GL_R16UI, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG16UI(GL30C.GL_RG16UI, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB16UI(GL30C.GL_RGB16UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA16UI(GL30C.GL_RGBA16UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// 32-bit integer
	R32I(GL30C.GL_R32I, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG32I(GL30C.GL_RG32I, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB32I(GL30C.GL_RGB32I, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA32I(GL30C.GL_RGBA32I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// 32-bit unsigned integer
	R32UI(GL30C.GL_R32UI, GlVersion.GL_30, PixelFormat.RED_INTEGER),
	RG32UI(GL30C.GL_RG32UI, GlVersion.GL_30, PixelFormat.RG_INTEGER),
	RGB32UI(GL30C.GL_RGB32UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER),
	RGBA32UI(GL30C.GL_RGBA32UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER),
	// Mixed
	R3_G3_B2(GL11C.GL_R3_G3_B2, GlVersion.GL_11, PixelFormat.RGB),
	RGB5_A1(GL11C.GL_RGB5_A1, GlVersion.GL_11, PixelFormat.RGBA),
	RGB10_A2(GL11C.GL_RGB10_A2, GlVersion.GL_11, PixelFormat.RGBA),
	R11F_G11F_B10F(GL30C.GL_R11F_G11F_B10F, GlVersion.GL_30, PixelFormat.RGB),
	RGB9_E5(GL30C.GL_RGB9_E5, GlVersion.GL_30, PixelFormat.RGB);

	private final int glFormat;
	private final GlVersion minimumGlVersion;
	private final PixelFormat expectedPixelFormat;

	InternalTextureFormat(int glFormat, GlVersion minimumGlVersion, PixelFormat expectedPixelFormat) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
		this.expectedPixelFormat = expectedPixelFormat;
	}

	public static Optional<InternalTextureFormat> fromString(String name) {
		try {
			return Optional.of(InternalTextureFormat.valueOf(name.toUpperCase(Locale.US)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public int getGlFormat() {
		return glFormat;
	}

	public PixelFormat getPixelFormat() {
		return expectedPixelFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}
}
