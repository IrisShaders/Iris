package net.coderbot.iris.gl.blending;

public class BlendModeOverride {
	public static final BlendModeOverride OFF = new BlendModeOverride(null);

	private final BlendMode blendMode;

	public BlendModeOverride(BlendMode blendMode) {
		this.blendMode = blendMode;
	}

	public void apply() {
		BlendModeStorage.overrideBlend(this.blendMode);
	}

	public static void restore() {
		BlendModeStorage.restoreBlend();
	}
}
