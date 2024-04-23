package net.irisshaders.iris.gl.texture;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.util.Objects;

public enum DepthBufferFormat {
	DEPTH(false),
	DEPTH16(false),
	DEPTH24(false),
	DEPTH32(false),
	DEPTH32F(false),
	DEPTH_STENCIL(true),
	DEPTH24_STENCIL8(true),
	DEPTH32F_STENCIL8(true);

	private final boolean combinedStencil;

	DepthBufferFormat(boolean combinedStencil) {
		this.combinedStencil = combinedStencil;
	}

	@Nullable
	public static DepthBufferFormat fromGlEnum(int glenum) {
		return switch (glenum) {
			case GL30C.GL_DEPTH_COMPONENT -> DepthBufferFormat.DEPTH;
			case GL30C.GL_DEPTH_COMPONENT16 -> DepthBufferFormat.DEPTH16;
			case GL30C.GL_DEPTH_COMPONENT24 -> DepthBufferFormat.DEPTH24;
			case GL30C.GL_DEPTH_COMPONENT32 -> DepthBufferFormat.DEPTH32;
			case GL30C.GL_DEPTH_COMPONENT32F -> DepthBufferFormat.DEPTH32F;
			case GL30C.GL_DEPTH_STENCIL -> DepthBufferFormat.DEPTH_STENCIL;
			case GL30C.GL_DEPTH24_STENCIL8 -> DepthBufferFormat.DEPTH24_STENCIL8;
			case GL30C.GL_DEPTH32F_STENCIL8 -> DepthBufferFormat.DEPTH32F_STENCIL8;
			default -> null;
		};
	}

	public static DepthBufferFormat fromGlEnumOrDefault(int glenum) {
		DepthBufferFormat format = fromGlEnum(glenum);
		// yolo, just assume it's GL_DEPTH_COMPONENT
		return Objects.requireNonNullElse(format, DepthBufferFormat.DEPTH);
	}

	public int getGlInternalFormat() {
		return switch (this) {
			case DEPTH -> GL30C.GL_DEPTH_COMPONENT;
			case DEPTH16 -> GL30C.GL_DEPTH_COMPONENT16;
			case DEPTH24 -> GL30C.GL_DEPTH_COMPONENT24;
			case DEPTH32 -> GL30C.GL_DEPTH_COMPONENT32;
			case DEPTH32F -> GL30C.GL_DEPTH_COMPONENT32F;
			case DEPTH_STENCIL -> GL30C.GL_DEPTH_STENCIL;
			case DEPTH24_STENCIL8 -> GL30C.GL_DEPTH24_STENCIL8;
			case DEPTH32F_STENCIL8 -> GL30C.GL_DEPTH32F_STENCIL8;
		};

	}

	public int getGlType() {
		return isCombinedStencil() ? GL30C.GL_DEPTH_STENCIL : GL30C.GL_DEPTH_COMPONENT;
	}

	public int getGlFormat() {
		return switch (this) {
			case DEPTH, DEPTH16 -> GL43C.GL_UNSIGNED_SHORT;
			case DEPTH24, DEPTH32 -> GL43C.GL_UNSIGNED_INT;
			case DEPTH32F -> GL30C.GL_FLOAT;
			case DEPTH_STENCIL, DEPTH24_STENCIL8 -> GL30C.GL_UNSIGNED_INT_24_8;
			case DEPTH32F_STENCIL8 -> GL30C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
		};

	}

	public boolean isCombinedStencil() {
		return combinedStencil;
	}
}
