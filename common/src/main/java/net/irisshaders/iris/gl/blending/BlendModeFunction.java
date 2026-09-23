package net.irisshaders.iris.gl.blending;

import net.irisshaders.iris.Iris;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

public enum BlendModeFunction {
	ZERO(GL11.GL_ZERO),
	ONE(GL11.GL_ONE),
	SRC_COLOR(GL11.GL_SRC_COLOR),
	ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
	DST_COLOR(GL11.GL_DST_COLOR),
	ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
	SRC_ALPHA(GL11.GL_SRC_ALPHA),
	ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
	DST_ALPHA(GL11.GL_DST_ALPHA),
	ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
	SRC_ALPHA_SATURATE(GL11.GL_SRC_ALPHA_SATURATE);

	private final int glId;

	BlendModeFunction(int glFormat) {
		this.glId = glFormat;
	}

	public static Optional<BlendModeFunction> fromString(String name) {
		try {
			return Optional.of(BlendModeFunction.valueOf(name));
		} catch (IllegalArgumentException e) {
			Iris.logger.warn("Invalid blend mode! " + name);
			return Optional.empty();
		}
	}

	public static int toVk(int glFactor) {
		return switch (glFactor) {
			case 0 -> 0;
			case 1 -> 1;
			case 0x300 -> 2;
			case 0x301 -> 3;
			case 0x306 -> 4;
			case 0x307 -> 5;
			case 0x302 -> 6;
			case 0x303 -> 7;
			case 0x304 -> 8;
			case 0x305 -> 9;
			case 0x8001 -> 10;
			case 0x8002 -> 11;
			case 0x8003 -> 12;
			case 0x8004 -> 13;
			case 0x308 -> 14;
			default -> throw new IllegalArgumentException("unsupported blend factor: " + glFactor);
		};
	}

	public int getGlId() {
		return glId;
	}
}
