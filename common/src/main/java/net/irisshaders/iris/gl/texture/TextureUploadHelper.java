package net.irisshaders.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20C;

public class TextureUploadHelper {
	private TextureUploadHelper() {
		// no construction
	}

	public static void resetTextureUploadState() {
		// Ensure that the pixel storage mode is in a sane state, otherwise the uploaded texture data will be quite
		// incorrect.
		//
		// It is likely that this also avoids the crashes on AMD that I previously experienced with texture creation.
		//
		// This code is from Canvas: https://github.com/grondag/canvas/commit/f0ab652d7a8b7cc9febf0209bee15cffce9eac83
		GlStateManager._pixelStore(GL20C.GL_UNPACK_ROW_LENGTH, 0);
		GlStateManager._pixelStore(GL20C.GL_UNPACK_SKIP_ROWS, 0);
		GlStateManager._pixelStore(GL20C.GL_UNPACK_SKIP_PIXELS, 0);
		GlStateManager._pixelStore(GL20C.GL_UNPACK_ALIGNMENT, 4);
	}
}
