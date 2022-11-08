package net.coderbot.iris.gl.sampler;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20C;

public class SamplerLimits {
	private final int maxTextureUnits;
	private final int maxDrawBuffers;
	private static SamplerLimits instance;

	private SamplerLimits() {
		this.maxTextureUnits = GlStateManager._getInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
		this.maxDrawBuffers = GlStateManager._getInteger(GL20C.GL_MAX_DRAW_BUFFERS);
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public int getMaxDrawBuffers() {
		return maxDrawBuffers;
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}
}
