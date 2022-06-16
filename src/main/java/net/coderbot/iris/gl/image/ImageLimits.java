package net.coderbot.iris.gl.image;

import net.coderbot.iris.gl.IrisRenderSystem;

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
