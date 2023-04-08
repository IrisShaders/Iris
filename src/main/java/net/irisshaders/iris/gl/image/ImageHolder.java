package net.irisshaders.iris.gl.image;

import net.irisshaders.iris.gl.texture.InternalTextureFormat;

import java.util.function.IntSupplier;

public interface ImageHolder {
	boolean hasImage(String name);

	void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name);
}
