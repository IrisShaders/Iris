package net.coderbot.iris.gl.sampler;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL42C;

public class SamplerLimits {
	private final int maxTextureUnits;
	private final int maxImageUnits;
	private static SamplerLimits instance;

	private SamplerLimits() {
		this.maxTextureUnits = GL20C.glGetInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
		this.maxImageUnits = GL42C.glGetInteger(GL42C.GL_MAX_IMAGE_UNITS);
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public int getMaxImageUnits() {
		return maxImageUnits;
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}
}
