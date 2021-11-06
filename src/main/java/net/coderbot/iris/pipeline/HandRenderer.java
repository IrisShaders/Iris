package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.GameRendererAccessor;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GameType;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private static boolean ACTIVE;
	public static boolean renderingSolid;

	private void setupGlState(GameRenderer gameRenderer, PoseStack poseStack, float tickDelta, Camera camera) {
        final PoseStack.Pose pose = poseStack.last();

		// We have a inject in getProjectionMatrix to scale the matrix so the hand doesn't clip through blocks.
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

	public boolean isHandTranslucent(InteractionHand hand) {
		if (Minecraft.getInstance().player.getItemBySlot(hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).getItem() instanceof BlockItem) {
			return ItemBlockRenderTypes.getChunkRenderType(((BlockItem) Minecraft.getInstance().player.getItemBySlot(hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).getItem()).getBlock().defaultBlockState()) == RenderType.translucent();
		}
		return false;
	}

	public void render(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if(!canRender(camera, gameRenderer)) {
			return;
		}

		ACTIVE = true;

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand");

		setupGlState(gameRenderer, poseStack, tickDelta, camera);

		pipeline.pushProgram(GbufferProgram.HAND);

		RenderSystem.disableBlend();

		renderingSolid = true;

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		renderingSolid = false;

		pipeline.popProgram(GbufferProgram.HAND);

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		poseStack.popPose();

		ACTIVE = false;
	}

	public void renderTranslucent(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if(!canRender(camera, gameRenderer)) {
			return;
		}

		ACTIVE = true;

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand_translucent");

		setupGlState(gameRenderer, poseStack, tickDelta, camera);

		pipeline.pushProgram(GbufferProgram.HAND_TRANSLUCENT);

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		pipeline.popProgram(GbufferProgram.HAND_TRANSLUCENT);

		poseStack.popPose();

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		ACTIVE = false;
	}

	public static boolean isActive() {
		return ACTIVE;
	}
}
