package net.coderbot.iris.shadows;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.coderbot.iris.pipeline.ShadowRenderer;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	private static BlockEntityRenderFunction function = (ShadowRenderer::renderBlockEntities);

	public static void setBlockEntityRenderFunction(BlockEntityRenderFunction function) {
		ShadowRenderingState.function = function;
	}

	public static int renderBlockEntities(ShadowRenderer shadowRenderer, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, Camera camera, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum) {
		return function.renderBlockEntities(shadowRenderer, bufferSource, modelView, camera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum);
	}

    public static int getRenderDistance() {
		return ShadowRenderer.renderDistance;
    }

    public interface BlockEntityRenderFunction {
		int renderBlockEntities(ShadowRenderer shadowRenderer, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, Camera camera, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum);
	}
}
