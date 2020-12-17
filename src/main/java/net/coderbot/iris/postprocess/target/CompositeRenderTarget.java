package net.coderbot.iris.postprocess.target;

import java.nio.ByteBuffer;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import org.lwjgl.opengl.GL11C;

public class CompositeRenderTarget {
	private final int mainTexture;
	private final int altTexture;

	public CompositeRenderTarget(CreationInfo creationInfo) {
		this.mainTexture = creationInfo.createTexture();
		this.altTexture = creationInfo.createTexture();
	}

	public int getMainTexture() {
		return mainTexture;
	}

	public int getAltTexture() {
		return altTexture;
	}

	public static class CreationInfo {
		public InternalTextureFormat internalFormat = InternalTextureFormat.RGBA;
		public int width = 0;
		public int height = 0;
		public PixelFormat format = PixelFormat.BGRA;
		public PixelType type = PixelType.UNSIGNED_INT_8_8_8_8_REV;

		private static final ByteBuffer NULL_BUFFER = null;

		private int createTexture() {
			int texture = GL11C.glGenTextures();

			GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, internalFormat.getGlFormat(), width, height, 0, format.getGlFormat(), type.getGlFormat(), NULL_BUFFER);

			return texture;
		}
	}
}
