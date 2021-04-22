package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;

public class FixedFunctionWorldRenderingPipeline implements WorldRenderingPipeline {
	@Override
	public void beginWorldRendering() {
		// Use the default Minecraft framebuffer and ensure that no programs are in use
		MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
		GlStateManager.useProgram(0);
	}

	@Override
	public void beginShadowRender() {
		// stub: nothing to do here
	}

	@Override
	public void endShadowRender() {
		// stub: nothing to do here
	}

	@Override
	public void beginTranslucents() {
		// stub: nothing to do here
	}

	@Override
	public void pushProgram(GbufferProgram program) {
		// stub: nothing to do here
	}

	@Override
	public void popProgram(GbufferProgram program) {
		// stub: nothing to do here
	}

	@Override
	public void finalizeWorldRendering() {
		// stub: nothing to do here
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		return false;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return false;
	}
}
