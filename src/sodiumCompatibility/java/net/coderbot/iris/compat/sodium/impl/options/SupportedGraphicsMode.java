package net.coderbot.iris.compat.sodium.impl.options;

import net.minecraft.client.GraphicsStatus;

public enum SupportedGraphicsMode {
	FAST, FANCY;

	public static SupportedGraphicsMode fromVanilla(GraphicsStatus vanilla) {
		if (vanilla == GraphicsStatus.FAST) {
			return FAST;
		} else {
			return FANCY;
		}
	}

	public GraphicsStatus toVanilla() {
		if (this == FAST) {
			return GraphicsStatus.FAST;
		} else {
			return GraphicsStatus.FANCY;
		}
	}
}
