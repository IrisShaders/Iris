package net.coderbot.iris.gl.sampler;

import org.lwjgl.opengl.*;

public class SamplerLimits {
	private final int maxTextureUnits;
	private final int maxImageUnits;
	private static SamplerLimits instance;

	private SamplerLimits() {
		this.maxTextureUnits = GL20C.glGetInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);

		if (GL.getCapabilities().OpenGL42) {
			this.maxImageUnits = GL42C.glGetInteger(GL42C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			this.maxImageUnits = EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT;
		} else {
			this.maxImageUnits = 0;
		}
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
