package net.irisshaders.iris.shaderpack.texture;

public final class TextureFilteringData {
	private final boolean blur;
	private final boolean clamp;

	public TextureFilteringData(boolean blur, boolean clamp) {
		this.blur = blur;
		this.clamp = clamp;
	}

	public boolean shouldBlur() {
		return blur;
	}

	public boolean shouldClamp() {
		return clamp;
	}
}
