package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Matrix4f;
import net.coderbot.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.GameRendererAccessor;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private boolean ACTIVE;
	private boolean renderingSolid;
	private FullyBufferedMultiBufferSource bufferSource = new FullyBufferedMultiBufferSource();

	private void setupGlState(GameRenderer gameRenderer, Camera camera, PoseStack poseStack, float tickDelta) {
        final PoseStack.Pose pose = poseStack.last();

		// We need to scale the matrix by 0.125 so the hand doesn't clip through blocks.
		gameRenderer.resetProjectionMatrix(getHandProjectionMatrix(gameRenderer, camera, tickDelta));

		pose.pose().setIdentity();
        pose.normal().setIdentity();

		((GameRendererAccessor) gameRenderer).invokeBobHurt(poseStack, tickDelta);

		if(Minecraft.getInstance().options.bobView) {
			((GameRendererAccessor) gameRenderer).invokeBobView(poseStack, tickDelta);
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
		Item item = Minecraft.getInstance().player.getItemBySlot(hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).getItem();

		if (item instanceof BlockItem) {
			return ItemBlockRenderTypes.getChunkRenderType(((BlockItem) item).getBlock().defaultBlockState()) == RenderType.translucent();
		}

		return false;
	}

	public boolean isAnyHandTranslucent() {
		return isHandTranslucent(InteractionHand.MAIN_HAND) || isHandTranslucent(InteractionHand.OFF_HAND);
	}

	public Matrix4f getHandProjectionMatrix(GameRenderer gameRenderer, Camera camera, float tickDelta) {
		PoseStack poseStack = new PoseStack();
		poseStack.last().pose().setIdentity();

		poseStack.scale(1F, 1F, 0.125F);
		poseStack.last().pose().multiply(Matrix4f.perspective(((GameRendererAccessor) gameRenderer).invokeGetFov(camera, tickDelta, false), (float) Minecraft.getInstance().getWindow().getWidth() / (float) Minecraft.getInstance().getWindow().getHeight(), 0.05F, Math.max(43.25F, gameRenderer.getRenderDistance() * 2.0F)));
		return poseStack.last().pose();
	}

	public void renderSolid(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if(!canRender(camera, gameRenderer) || pipeline instanceof FixedFunctionWorldRenderingPipeline) {
			return;
		}

		ACTIVE = true;

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand");

		setupGlState(gameRenderer, camera, poseStack, tickDelta);

		pipeline.pushProgram(GbufferProgram.HAND);

		RenderSystem.disableBlend();

		renderingSolid = true;

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, bufferSource, Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		renderingSolid = false;

		pipeline.popProgram(GbufferProgram.HAND);

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		poseStack.popPose();

		bufferSource.endBatch();

		ACTIVE = false;
	}

	public void renderTranslucent(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if(!canRender(camera, gameRenderer) || !isAnyHandTranslucent() || pipeline instanceof FixedFunctionWorldRenderingPipeline) {
			return;
		}

		ACTIVE = true;

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand_translucent");

		setupGlState(gameRenderer, camera, poseStack, tickDelta);

		RenderSystem.enableBlend();

		pipeline.pushProgram(GbufferProgram.HAND_TRANSLUCENT);

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, bufferSource, Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		pipeline.popProgram(GbufferProgram.HAND_TRANSLUCENT);

		poseStack.popPose();

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		bufferSource.endBatch();

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}

	public boolean isRenderingSolid() {
		return renderingSolid;
	}

	public FullyBufferedMultiBufferSource getBufferSource() {
		return bufferSource;
	}
}
