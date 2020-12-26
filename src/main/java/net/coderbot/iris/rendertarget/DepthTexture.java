package net.coderbot.iris.rendertarget;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL30C;

public class DepthTexture {
	private final int textureId;

	public DepthTexture(int width, int height) {
		textureId = GL30C.glGenTextures();
		GlStateManager.bindTexture(textureId);

		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE);
		resize(width, height);

		GlStateManager.bindTexture(0);
	}

	void resize(int width, int height) {
		GlStateManager.bindTexture(textureId);

		GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, GL30C.GL_DEPTH_COMPONENT, width, height, 0, GL30C.GL_DEPTH_COMPONENT, GL30C.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		GlStateManager.bindTexture(0);
	}

	public int getTextureId() {
		return textureId;
	}
}
