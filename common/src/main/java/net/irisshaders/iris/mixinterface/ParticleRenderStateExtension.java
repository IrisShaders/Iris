package net.irisshaders.iris.mixinterface;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface ParticleRenderStateExtension {
	void submitWithoutItems(SubmitNodeStorage submitNodeStorage, CameraRenderState cameraRenderState);
}
