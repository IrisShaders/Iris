package net.coderbot.iris.gl.image;

import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL42C;

import java.util.function.IntSupplier;

public class ImageBinding {
	private final int imageUnit;
	private final int internalFormat;
	private final IntSupplier textureID;
	private final boolean useExt;

	public ImageBinding(int imageUnit, int internalFormat, IntSupplier textureID) {
		this.textureID = textureID;
		this.imageUnit = imageUnit;
		this.internalFormat = internalFormat;

		// We can assume that image bindings are supported here, as otherwise ImageLimits
		// would report that zero image units are supported.
		this.useExt = !GL.getCapabilities().OpenGL42;
	}

	public void update() {
		IrisRenderSystem.bindImageTexture(useExt, imageUnit, textureID.getAsInt(), 0, false, 0, GL42C.GL_READ_WRITE, internalFormat);
	}
}
