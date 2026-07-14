package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.textures.GpuTexture;

public interface GpuTextureInterface {
	default int iris$getGlId() {
		throw new AssertionError("Not accessible.");
	}

    default void iris$markMipmapNonLinear() {
		throw new AssertionError("Not accessible.");
	}
}
