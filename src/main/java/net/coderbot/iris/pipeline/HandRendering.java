package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class HandRendering {
	private static final Minecraft minecraft = Minecraft.getInstance();

	private static GameRenderer gameRenderer;
	private static RenderBuffers renderBuffers;
	private static PoseStack poseStack;
	private static float tickDelta;
	private static Camera camera;

	private static boolean rendering;

	private static boolean canRender;

	public static void prepareForRendering(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer) {
		canRender = !(camera.isDetached() || !(camera.getEntity() instanceof Player) || minecraft.options.hideGui || (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR);

		HandRendering.gameRenderer = gameRenderer;
		HandRendering.renderBuffers = renderBuffers;
		HandRendering.poseStack = poseStack;
		HandRendering.tickDelta = tickDelta;
		HandRendering.camera = camera;
	}

	public static void render() {
		if(!canRender) return;

		rendering = true;

		poseStack.pushPose();

        final PoseStack.Pose pose = poseStack.last();

		pose.pose().multiply(Matrix4f.perspective(minecraft.options.fov, minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight(), .05f, gameRenderer.getRenderDistance() * 4f));

        pose.pose().setIdentity();
        pose.normal().setIdentity();

		if(minecraft.options.bobView) {
			final float g = minecraft.player.walkDist - minecraft.player.walkDistO;
			final float h = -(minecraft.player.walkDist + g * tickDelta);
			final float i = Mth.lerp(tickDelta, minecraft.player.oBob, minecraft.player.bob);

			poseStack.translate((double)(Mth.sin(h * 3.1415927f) * i * 0.5f), (double)(-Math.abs(Mth.cos(h * 3.1415927f) * i)), 0.0);

			poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(h * 3.1415927f) * i * 3.0f));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(h * 3.1415927f - 0.2f) * i) * 5.0f));
		}

		minecraft.getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), minecraft.player, minecraft.getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));


		poseStack.popPose();

		rendering = false;
	}

	public static boolean isRendering() {
		return rendering;
	}
}
