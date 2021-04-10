package net.coderbot.iris.rendertarget;

import java.nio.ByteBuffer;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.texture.TextureUploadHelper;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;

/**
 * An extremely simple noise texture. Each color channel contains a uniform random value from 0 to 255. Essentially just
 * dumps an array of random bytes into a texture and calls it a day, literally could not be any simpler than that.
 */
public class NoiseTexture extends GlResource {
	int width;
	int height;

	public NoiseTexture(int width, int height) {
		super(GL11C.glGenTextures());
		GlStateManager.bindTexture(getGlId());

		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_REPEAT);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_REPEAT);

		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAX_LEVEL, 0);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_LOD, 0);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAX_LOD,0);
		GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_LOD_BIAS, 0.0F);
		resize(width, height);

		GlStateManager.bindTexture(0);
	}

	void resize(int width, int height) {
		this.width = width;
		this.height = height;

		GlStateManager.bindTexture(getGlId());

		ByteBuffer pixels = generateNoise();

		TextureUploadHelper.resetTextureUploadState();

		// Since we're using tightly-packed RGB data, we must use an alignment of 1 byte instead of the usual 4 bytes.
		GlStateManager.pixelStore(GL20C.GL_UNPACK_ALIGNMENT, 1);
		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGB, width, height, 0, GL11C.GL_RGB, GL11C.GL_UNSIGNED_BYTE, pixels);

		GlStateManager.bindTexture(0);
	}

	private ByteBuffer generateNoise() {
		byte[] pixels = new byte[3 * width * height];

		Random random = new Random(0);
		random.nextBytes(pixels);

		ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
		buffer.put(pixels);
		buffer.flip();

		return buffer;
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GL11C.glDeleteTextures(getGlId());
	}
}
