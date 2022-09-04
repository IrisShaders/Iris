package net.coderbot.iris.texture.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class TextureManipulationUtil {
	private static int colorFillFBO = -1;

	public static void fillWithColor(int textureId, int maxLevel, int rgba) {
		if (colorFillFBO == -1) {
			colorFillFBO = GlStateManager.glGenFramebuffers();
		}

		int previousFramebufferId = GlStateManager._getInteger(GL30.GL_FRAMEBUFFER_BINDING);
		float[] previousClearColor = new float[4];
		IrisRenderSystem.getFloatv(GL11.GL_COLOR_CLEAR_VALUE, previousClearColor);
		int previousTextureId = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
		int[] previousViewport = new int[4];
		IrisRenderSystem.getIntegerv(GL11.GL_VIEWPORT, previousViewport);

		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, colorFillFBO);
		GlStateManager._clearColor(
				(rgba >> 24 & 0xFF) / 255.0f,
				(rgba >> 16 & 0xFF) / 255.0f,
				(rgba >> 8 & 0xFF) / 255.0f,
				(rgba & 0xFF) / 255.0f
		);
		GlStateManager._bindTexture(textureId);
		for (int level = 0; level <= maxLevel; ++level) {
			int width = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_WIDTH);
			int height = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_HEIGHT);
			GlStateManager._viewport(0, 0, width, height);
			GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureId, level);
			GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
			GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, level);
		}

		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebufferId);
		GlStateManager._clearColor(previousClearColor[0], previousClearColor[1], previousClearColor[2], previousClearColor[3]);
		GlStateManager._bindTexture(previousTextureId);
		GlStateManager._viewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
	}
}
