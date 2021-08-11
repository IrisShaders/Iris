package net.coderbot.iris.shaderpack;

public class CustomTexture {
	private final byte[] content;
	private final boolean blur;
	private final boolean clamp;

	CustomTexture(byte[] content, boolean blur, boolean clamp) {
		this.content = content;
		this.blur = blur;
		this.clamp = clamp;
	}

	public byte[] getContent() {
		return content;
	}

	public boolean shouldBlur() {
		return blur;
	}

	public boolean shouldClamp() {
		return clamp;
	}
}
