package net.irisshaders.iris.mixinterface;

import net.irisshaders.iris.backend.IrisRenderPass;

public interface RenderPassInterface {
	default IrisRenderPass iris$getIrisPass() {
		throw new UnsupportedOperationException();
	}

	default void iris$setCustomPass(CustomPass pass) {
		throw new UnsupportedOperationException();
	}

	default CustomPass iris$getCustomPass() {
		throw new UnsupportedOperationException();
	}
}
