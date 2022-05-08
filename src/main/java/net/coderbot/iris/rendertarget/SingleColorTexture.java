package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.TextureUploadHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.nio.ByteBuffer;

public class SingleColorTexture extends GlResource {
	public SingleColorTexture(int red, int green, int blue, int alpha) {
		super(GlStateManager._genTexture());
		GlStateManager._bindTexture(getGlId());

		ByteBuffer pixel = BufferUtils.createByteBuffer(4);
		pixel.put((byte) red);
		pixel.put((byte) green);
		pixel.put((byte) blue);
		pixel.put((byte) alpha);
		pixel.position(0);

		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_REPEAT);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_REPEAT);

		TextureUploadHelper.resetTextureUploadState();
		IrisRenderSystem.texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA, 1, 1, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixel);

		GlStateManager._bindTexture(0);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
