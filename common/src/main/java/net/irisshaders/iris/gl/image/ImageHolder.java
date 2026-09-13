package net.irisshaders.iris.gl.image;

import net.irisshaders.iris.gl.texture.InternalTextureFormat;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public interface ImageHolder {
	boolean hasImage(String name);

	default void addTextureImage(LongSupplier view, LongSupplier image, InternalTextureFormat format, String name) {
		addTextureImage(view, format, name);
	}

	default void addTextureImage(LongSupplier view, InternalTextureFormat format, String name) {
		if (hasImage(name)) {
			throw new UnsupportedOperationException("Native storage image binding is unavailable: " + name);
		}
	}

	void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name);
}
