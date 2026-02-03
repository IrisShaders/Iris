package net.irisshaders.iris.mixinterface;

public interface RenderPassInterface {
	default void iris$setCustomPass(CustomPass pass) {
		throw new UnsupportedOperationException();
	}

	default CustomPass iris$getCustomPass() {
		throw new UnsupportedOperationException();
	}
}
