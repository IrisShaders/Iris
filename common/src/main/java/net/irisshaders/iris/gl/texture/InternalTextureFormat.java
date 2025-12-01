package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL41C;

import java.util.Locale;
import java.util.Optional;

public enum InternalTextureFormat {
	// Default
	// TODO: This technically shouldn't be exposed to shaders since it's not in the specification, it's the default anyways
	RGBA(GL11C.GL_RGBA, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 8-bit normalized
	R8(GL30C.GL_R8, GlVersion.GL_30, PixelFormat.RED, ShaderDataType.FLOAT),
	RG8(GL30C.GL_RG8, GlVersion.GL_30, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB8(GL11C.GL_RGB8, GlVersion.GL_11, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA8(GL11C.GL_RGBA8, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 8-bit signed normalized
	R8_SNORM(GL31C.GL_R8_SNORM, GlVersion.GL_31, PixelFormat.RED, ShaderDataType.FLOAT),
	RG8_SNORM(GL31C.GL_RG8_SNORM, GlVersion.GL_31, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB8_SNORM(GL31C.GL_RGB8_SNORM, GlVersion.GL_31, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA8_SNORM(GL31C.GL_RGBA8_SNORM, GlVersion.GL_31, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 16-bit normalized
	R16(GL30C.GL_R16, GlVersion.GL_30, PixelFormat.RED, ShaderDataType.FLOAT),
	RG16(GL30C.GL_RG16, GlVersion.GL_30, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB16(GL11C.GL_RGB16, GlVersion.GL_11, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA16(GL11C.GL_RGBA16, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 16-bit signed normalized
	R16_SNORM(GL31C.GL_R16_SNORM, GlVersion.GL_31, PixelFormat.RED, ShaderDataType.FLOAT),
	RG16_SNORM(GL31C.GL_RG16_SNORM, GlVersion.GL_31, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB16_SNORM(GL31C.GL_RGB16_SNORM, GlVersion.GL_31, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA16_SNORM(GL31C.GL_RGBA16_SNORM, GlVersion.GL_31, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 16-bit float
	R16F(GL30C.GL_R16F, GlVersion.GL_30, PixelFormat.RED, ShaderDataType.FLOAT),
	RG16F(GL30C.GL_RG16F, GlVersion.GL_30, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB16F(GL30C.GL_RGB16F, GlVersion.GL_30, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA16F(GL30C.GL_RGBA16F, GlVersion.GL_30, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 32-bit float
	R32F(GL30C.GL_R32F, GlVersion.GL_30, PixelFormat.RED, ShaderDataType.FLOAT),
	RG32F(GL30C.GL_RG32F, GlVersion.GL_30, PixelFormat.RG, ShaderDataType.FLOAT),
	RGB32F(GL30C.GL_RGB32F, GlVersion.GL_30, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGBA32F(GL30C.GL_RGBA32F, GlVersion.GL_30, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 8-bit integer
	R8I(GL30C.GL_R8I, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.INT),
	RG8I(GL30C.GL_RG8I, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.INT),
	RGB8I(GL30C.GL_RGB8I, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.INT),
	RGBA8I(GL30C.GL_RGBA8I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.INT),
	// 8-bit unsigned integer
	R8UI(GL30C.GL_R8UI, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.UINT),
	RG8UI(GL30C.GL_RG8UI, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.UINT),
	RGB8UI(GL30C.GL_RGB8UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.UINT),
	RGBA8UI(GL30C.GL_RGBA8UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.UINT),
	// 16-bit integer
	R16I(GL30C.GL_R16I, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.INT),
	RG16I(GL30C.GL_RG16I, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.INT),
	RGB16I(GL30C.GL_RGB16I, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.INT),
	RGBA16I(GL30C.GL_RGBA16I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.INT),
	// 16-bit unsigned integer
	R16UI(GL30C.GL_R16UI, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.UINT),
	RG16UI(GL30C.GL_RG16UI, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.UINT),
	RGB16UI(GL30C.GL_RGB16UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.UINT),
	RGBA16UI(GL30C.GL_RGBA16UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.UINT),
	// 32-bit integer
	R32I(GL30C.GL_R32I, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.INT),
	RG32I(GL30C.GL_RG32I, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.INT),
	RGB32I(GL30C.GL_RGB32I, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.INT),
	RGBA32I(GL30C.GL_RGBA32I, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.INT),
	// 32-bit unsigned integer
	R32UI(GL30C.GL_R32UI, GlVersion.GL_30, PixelFormat.RED_INTEGER, ShaderDataType.UINT),
	RG32UI(GL30C.GL_RG32UI, GlVersion.GL_30, PixelFormat.RG_INTEGER, ShaderDataType.UINT),
	RGB32UI(GL30C.GL_RGB32UI, GlVersion.GL_30, PixelFormat.RGB_INTEGER, ShaderDataType.UINT),
	RGBA32UI(GL30C.GL_RGBA32UI, GlVersion.GL_30, PixelFormat.RGBA_INTEGER, ShaderDataType.UINT),
	// 2-bit normalized
	RGBA2(GL11C.GL_RGBA2, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// 4-bit normalized
	RGBA4(GL11C.GL_RGBA4, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	// Mixed
	R3_G3_B2(GL11C.GL_R3_G3_B2, GlVersion.GL_11, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGB5_A1(GL11C.GL_RGB5_A1, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	RGB565(GL41C.GL_RGB565, GlVersion.GL_41, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGB10_A2(GL11C.GL_RGB10_A2, GlVersion.GL_11, PixelFormat.RGBA, ShaderDataType.FLOAT),
	RGB10_A2UI(GL33C.GL_RGB10_A2UI, GlVersion.GL_33, PixelFormat.RGBA_INTEGER, ShaderDataType.UINT),
	R11F_G11F_B10F(GL30C.GL_R11F_G11F_B10F, GlVersion.GL_30, PixelFormat.RGB, ShaderDataType.FLOAT),
	RGB9_E5(GL30C.GL_RGB9_E5, GlVersion.GL_30, PixelFormat.RGB, ShaderDataType.FLOAT);

	private final int glFormat;
	private final GlVersion minimumGlVersion;
	private final PixelFormat expectedPixelFormat;
	private final ShaderDataType shaderDataType;

	InternalTextureFormat(int glFormat, GlVersion minimumGlVersion, PixelFormat expectedPixelFormat, ShaderDataType shaderDataType) {
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
		this.expectedPixelFormat = expectedPixelFormat;
		this.shaderDataType = shaderDataType;
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

	public ShaderDataType getShaderDataType() {
		return shaderDataType;
	}
}
