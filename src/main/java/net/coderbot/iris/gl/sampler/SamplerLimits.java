package net.coderbot.iris.gl.sampler;

import org.lwjgl.opengl.GL20C;

public class SamplerLimits {
	private static SamplerLimits instance;
	private final int maxTextureUnits;

	private SamplerLimits() {
		this.maxTextureUnits = GL20C.glGetInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}
}
