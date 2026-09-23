package net.irisshaders.iris.mixinterface;

public interface GpuTextureInterface {
	default int iris$getGlId() {
		throw new AssertionError("Not accessible.");
	}

	default void iris$markMipmapNonLinear() {
		throw new AssertionError("Not accessible.");
	}
}
