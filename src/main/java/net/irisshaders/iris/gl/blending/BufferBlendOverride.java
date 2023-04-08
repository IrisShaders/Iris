package net.irisshaders.iris.gl.blending;

public class BufferBlendOverride {
	private final int drawBuffer;
	private final BlendMode blendMode;

	public BufferBlendOverride(int drawBuffer, BlendMode blendMode) {
		this.drawBuffer = drawBuffer;
		this.blendMode = blendMode;
	}

	public void apply() {
		BlendModeStorage.overrideBufferBlend(this.drawBuffer, this.blendMode);
	}
}
