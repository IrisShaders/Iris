package net.coderbot.iris.gl.image;

import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42C;

public class ImageLimits {
	private final int maxImageUnits;
	private static ImageLimits instance;

	private ImageLimits() {
		if (GL.getCapabilities().OpenGL42) {
			this.maxImageUnits = GL42C.glGetInteger(GL42C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			this.maxImageUnits = EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT;
		} else {
			this.maxImageUnits = 0;
		}
	}

	public int getMaxImageUnits() {
		return maxImageUnits;
	}

	public static ImageLimits get() {
		if (instance == null) {
			instance = new ImageLimits();
		}

		return instance;
	}
}
