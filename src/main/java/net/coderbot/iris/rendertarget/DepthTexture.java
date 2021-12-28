package net.coderbot.iris.rendertarget;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.GlObject;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

public class DepthTexture extends GlObject {
	public DepthTexture(int width, int height) {
		int texture = GlStateManager._genTexture();

		GlStateManager._bindTexture(texture);

		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		resize(width, height);

		GlStateManager._bindTexture(0);

		this.setHandle(texture);
	}

	void resize(int width, int height) {
		GlStateManager._bindTexture(this.handle());

		GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_DEPTH_COMPONENT, width, height, 0, GL11C.GL_DEPTH_COMPONENT, GL11C.GL_UNSIGNED_BYTE, null);

		GlStateManager._bindTexture(0);
	}

	public int getTextureId() {
		return this.handle();
	}

	public void delete() {
		GlStateManager._deleteTexture(this.handle());

		this.invalidateHandle();
	}
}
