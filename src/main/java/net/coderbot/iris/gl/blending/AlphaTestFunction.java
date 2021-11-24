package net.coderbot.iris.gl.blending;

import java.util.Optional;

import org.lwjgl.opengl.GL11;

public enum AlphaTestFunction {
	NEVER(GL11.GL_NEVER),
	LESS(GL11.GL_LESS),
	EQUAL(GL11.GL_EQUAL),
	LEQUAL(GL11.GL_LEQUAL),
	GREATER(GL11.GL_GREATER),
	NOTEQUAL(GL11.GL_NOTEQUAL),
	GEQUAL(GL11.GL_GEQUAL),
	ALWAYS(GL11.GL_ALWAYS);

	private final int glId;

	AlphaTestFunction(int glFormat) {
		this.glId = glFormat;
	}

	public static Optional<AlphaTestFunction> fromString(String name) {
		if ("GL_ALWAYS".equals(name)) {
			// shaders.properties states that GL_ALWAYS is the name to use, but I haven't verified that this actually
			// matches the implementation... All of the other names do not have the GL_ prefix.
			//
			// We'll support it here just to be safe, even though just a plain ALWAYS seems more likely to be what it
			// parses.
			return Optional.of(AlphaTestFunction.ALWAYS);
		}

		try {
			return Optional.of(AlphaTestFunction.valueOf(name));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public int getGlId() {
		return glId;
	}
}
