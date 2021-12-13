package net.coderbot.iris.gl.image;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42C;

public class ImageLimits {
	private final int maxImageUnits;
	private static ImageLimits instance;

	private ImageLimits() {
		this.maxImageUnits = IrisRenderSystem.getMaxImageUnits();
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
