package net.coderbot.iris.rendertarget;

public interface Blaze3dRenderTargetExt {
	boolean iris$isDepthBufferDirty();
	void iris$clearDepthBufferDirtyFlag();

	boolean iris$isColorBufferDirty();
	void iris$clearColorBufferDirtyFlag();
}
