package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL46C;

import java.nio.ByteBuffer;

public class DepthTexture extends GlResource {
	public DepthTexture(int width, int height, DepthBufferFormat format) {
		super(0);
		resize(width, height, format);

		int texture = getGlId();

		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);

		GlStateManager._bindTexture(0);
	}

	void resize(int width, int height, DepthBufferFormat format) {
		if (getGlId() != 0) {
			GlStateManager._deleteTexture(getGlId());
		}
		setNewId(IrisRenderSystem.createTexture(GL46C.GL_TEXTURE_2D));
		GL46C.glTextureStorage2D(getTextureId(), 1, format.getGlInternalFormat(), width, height);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
