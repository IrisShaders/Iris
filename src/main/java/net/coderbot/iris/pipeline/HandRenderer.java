package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.opengl.GL11C;

import net.coderbot.iris.mixin.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private final Minecraft minecraft = Minecraft.getInstance();

	private GameRenderer gameRenderer;
	private RenderBuffers renderBuffers;
	private PoseStack poseStack;
	private float tickDelta;
	private Camera camera;

	public static boolean ACTIVE;

	private boolean canRender;

	public void prepareForRendering(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer) {
		this.canRender = !(camera.isDetached() || !(camera.getEntity() instanceof Player) || ((GameRendererAccessor)gameRenderer).getPanoramicMode() | minecraft.options.hideGui || (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR);

		this.gameRenderer = gameRenderer;
		this.renderBuffers = renderBuffers;
		this.poseStack = poseStack;
		this.tickDelta = tickDelta;
		this.camera = camera;
	}

	private void setupGlState() {
		RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, false);

        final PoseStack.Pose pose = poseStack.last();

		gameRenderer.resetProjectionMatrix(gameRenderer.getProjectionMatrix(camera, tickDelta, false));

        pose.pose().setIdentity();
        pose.normal().setIdentity();

		if(minecraft.options.bobView) {
			((GameRendererAccessor)gameRenderer).invokeBobView(poseStack, tickDelta);
		}
	}

	public void render() {
		if(!canRender) return;

		ACTIVE = true;

		poseStack.pushPose();

		setupGlState();

		minecraft.getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), minecraft.player, minecraft.getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		poseStack.popPose();

		ACTIVE = false;
	}
}
