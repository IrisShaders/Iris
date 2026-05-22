package net.irisshaders.iris.mixinterface;

public interface RenderTargetInterface {
	default void iris$bindFramebuffer() {
		throw new AssertionError("Impossible to access.");
	}
}
