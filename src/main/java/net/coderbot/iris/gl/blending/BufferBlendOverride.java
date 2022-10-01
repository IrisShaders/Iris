package net.coderbot.iris.gl.blending;

public class BufferBlendOverride {
	private final int index;
	private final BlendMode blendMode;

	public BufferBlendOverride(int index, BlendMode blendMode) {
		this.index = index;
		this.blendMode = blendMode;
	}

	public void apply() {
		BlendModeStorage.overrideBufferBlend(this.index, this.blendMode);
	}
}
