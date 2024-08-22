package net.irisshaders.iris.fantastic;

import net.minecraft.client.GraphicsStatus;

public enum SupportedGraphicsMode {
	FAST,
	FANCY;

	public static SupportedGraphicsMode fromVanilla(GraphicsStatus status) {
		return switch (status) {
			case FAST -> FAST;
			case FANCY -> FANCY;
			case FABULOUS -> throw new IllegalStateException("Fabulous graphics mode is not supported by Iris");
		};
	}

	public GraphicsStatus toVanilla() {
		return switch (this) {
			case FAST -> GraphicsStatus.FAST;
			case FANCY -> GraphicsStatus.FANCY;
		};
	}
}
