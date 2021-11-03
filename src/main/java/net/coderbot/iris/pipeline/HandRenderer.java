package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.opengl.GL11C;

import net.coderbot.iris.mixin.GameRendererAccessor;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	public static boolean ACTIVE;

	private void setupGlState(GameRenderer gameRenderer, PoseStack poseStack, float tickDelta, Camera camera) {
        final PoseStack.Pose pose = poseStack.last();

		gameRenderer.resetProjectionMatrix(gameRenderer.getProjectionMatrix(camera, tickDelta, false));

        pose.pose().setIdentity();
        pose.normal().setIdentity();

		if(Minecraft.getInstance().options.bobView) {
			((GameRendererAccessor)gameRenderer).invokeBobView(poseStack, tickDelta);
		}
	}

	private boolean canRender(Camera camera, GameRenderer gameRenderer) {
		return !(camera.isDetached() 
			|| !(camera.getEntity() instanceof Player) 
				|| ((GameRendererAccessor)gameRenderer).getPanoramicMode() 
					|| Minecraft.getInstance().options.hideGui 
						|| (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) 
							|| Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR);
	}

	public void render(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer) {
		if(!canRender(camera, gameRenderer)) {
			return;
		}

		ACTIVE = true;

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand");

		setupGlState(gameRenderer, poseStack, tickDelta, camera);

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		poseStack.popPose();

		ACTIVE = false;
	}
}
