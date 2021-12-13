package net.coderbot.iris.gl.image;

import net.coderbot.iris.gl.texture.InternalTextureFormat;

import java.util.function.IntSupplier;

public interface ImageHolder {
	void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name);
}
