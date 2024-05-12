package net.irisshaders.iris.gl.texture;

import java.util.function.IntSupplier;

public interface TextureAccess {
	TextureType getType();

	IntSupplier getTextureId();
}
