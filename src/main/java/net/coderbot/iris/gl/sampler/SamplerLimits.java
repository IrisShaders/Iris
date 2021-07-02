package net.coderbot.iris.gl.sampler;

import org.lwjgl.opengl.GL20C;

public class SamplerLimits {
	private final int maxTextureUnits;
	private static SamplerLimits instance;

	private SamplerLimits() {
		this.maxTextureUnits = GL20C.glGetInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}
}
