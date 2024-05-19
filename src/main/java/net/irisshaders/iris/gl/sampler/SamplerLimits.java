package net.irisshaders.iris.gl.sampler;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;

public class SamplerLimits {
	private static SamplerLimits instance;
	private final int maxTextureUnits;
	private final int maxDrawBuffers;
	private final int maxShaderStorageUnits;
	private final int uboOffsetAlignment;

	private SamplerLimits() {
		this.maxTextureUnits = GlStateManager._getInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
		this.maxDrawBuffers = GlStateManager._getInteger(GL20C.GL_MAX_DRAW_BUFFERS);
		this.uboOffsetAlignment = GlStateManager._getInteger(GL32C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
		this.maxShaderStorageUnits = IrisRenderSystem.supportsSSBO() ? GlStateManager._getInteger(GL45C.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) : 0;
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

	public int getMaxDrawBuffers() {
		return maxDrawBuffers;
	}

	public int getUboOffsetAlignment() {
		return uboOffsetAlignment;
	}

	public int getMaxShaderStorageUnits() {
		return maxShaderStorageUnits;
	}
}
