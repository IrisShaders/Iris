package net.irisshaders.iris.uniforms;

import net.minecraft.client.Minecraft;

public class EndFlashStorage {
	private float lastEndFlash;
	private float currentEndFlash;

	public void tick() {
		lastEndFlash = currentEndFlash;
		currentEndFlash = Minecraft.getInstance().level.endFlashState() == null ? 0 : Minecraft.getInstance().level.endFlashState().getIntensity(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	public float getLastEndFlash() {
		return lastEndFlash;
	}

	public float getCurrentEndFlash() {
		return currentEndFlash;
	}
}
