package net.irisshaders.iris.gl.texture;

import java.util.function.IntSupplier;

public class TextureWrapper implements TextureAccess {
	private final IntSupplier texture;
	private final TextureType type;

	public TextureWrapper(IntSupplier texture, TextureType type) {
		this.texture = texture;
		this.type = type;
	}

	@Override
	public TextureType getType() {
		return this.type;
	}

	@Override
	public IntSupplier getTextureId() {
		return this.texture;
	}
}
