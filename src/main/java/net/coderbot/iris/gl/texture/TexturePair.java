package net.coderbot.iris.gl.texture;

import java.util.function.IntSupplier;

public class TexturePair {
	private final TextureType type;
	private final IntSupplier id;

	public TexturePair(TextureType type, IntSupplier id) {
		this.type = type;
		this.id = id;
	}

	public TextureType getType() {
		return type;
	}

	public IntSupplier getId() {
		return id;
	}
}
