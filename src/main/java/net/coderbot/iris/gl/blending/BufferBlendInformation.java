package net.coderbot.iris.gl.blending;

public class BufferBlendInformation {
	private final int index;
	private final BlendMode blendMode;

	public BufferBlendInformation(int index, BlendMode blendMode) {
		this.index = index;
		this.blendMode = blendMode;
	}

	public BlendMode getBlendMode() {
		return blendMode;
	}

	public int getIndex() {
		return index;
	}
}
