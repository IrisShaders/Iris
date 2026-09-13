package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.caffeinemc.mods.sodium.client.util.GameRendererStorage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.GameRendererAccessor;
import net.irisshaders.iris.pbr.texture.PBRAtlasTexture;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

import java.util.Optional;
import java.util.OptionalDouble;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();
	public static final float DEPTH = 0.125F;
	private final RenderBuffers bufferSource = new RenderBuffers(Runtime.getRuntime().availableProcessors());
	private final ProjectionMatrixBuffer cachedProjectionMatrixBuffer = new ProjectionMatrixBuffer("hand (Iris)");
	private boolean ACTIVE;
	private boolean renderingSolid;
	private SubmitNodeStorage submitNodeCollector;
	private FeatureRenderDispatcher featureRenderDispatcher;
	private Projection projection = new Projection();
	private FeatureRenderDispatcher.PreparedFrame frame;

	public HandRenderer() {
		submitNodeCollector = new SubmitNodeStorage();
		featureRenderDispatcher = new FeatureRenderDispatcher(bufferSource, Minecraft.getInstance().getModelManager(), Minecraft.getInstance().getAtlasManager(), Minecraft.getInstance().font, Minecraft.getInstance().gameRenderer.gameRenderState());
	}

	private PoseStack setupGlState(GameRenderer gameRenderer, CameraRenderState camera, Matrix4fc modelMatrix, float tickDelta) {
		final PoseStack poseStack = new PoseStack();

		// We need to scale the matrix by 0.125 so the hand doesn't clip through blocks.
		Matrix4f scaleMatrix = new Matrix4f()
			.m22(DEPTH)
			.m32((1.0F - DEPTH) * 0.5F);
		this.projection.setupPerspective(0.05F, camera.depthFar, camera.hudFov, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
		scaleMatrix.mul(this.projection.getMatrix(new Matrix4f()));

		poseStack.pushPose();
		((GameRendererAccessor) gameRenderer).invokeBobHurt(camera, poseStack);
		if (Minecraft.getInstance().options.bobView().get()) {
			((GameRendererAccessor) gameRenderer).invokeBobView(camera, poseStack);
		}
		scaleMatrix.mul(poseStack.last().pose());
		RenderSystem.setProjectionMatrix(cachedProjectionMatrixBuffer.getBuffer(scaleMatrix), ProjectionType.PERSPECTIVE);

		return new PoseStack();
	}

	private boolean canRender(Camera camera, GameRenderer gameRenderer) {
		return !(camera.isDetached()
			|| !(camera.entity() instanceof Player)
			|| (camera).isPanoramicMode()
			|| Minecraft.getInstance().gui.hud.isHidden()
			|| (camera.entity() instanceof LivingEntity && ((LivingEntity) camera.entity()).isSleeping())
			|| Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR);
	}

	public boolean isHandTranslucent(ItemStack itemStack) {
		Item item = itemStack.getItem();

		if (item instanceof BlockItem) {
			return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(((BlockItem) item).getBlock().defaultBlockState()).hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT);
		}

		return false;
	}

	public void renderSolid(Matrix4fc modelMatrix, float tickDelta, Camera camera, CameraRenderState cameraState, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		var state = gameRenderer.gameRenderState().levelRenderState.playerRenderState.firstPersonHandsAndItems;
		if (!canRender(camera, gameRenderer) || !iris$isAnyHandSolid(state) || !Iris.isPackInUseQuick()) {
			return;
		}

		RenderSystem.backupProjectionMatrix();

		ACTIVE = true;

		PoseStack poseStack = setupGlState(gameRenderer, cameraState, modelMatrix, tickDelta);

		pipeline.setPhase(WorldRenderingPhase.HAND_SOLID);

		poseStack.pushPose();

		Profiler.get().push("iris_hand");

		renderingSolid = true;

		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().set(poseStack.last().pose());

		gameRenderer.firstPersonHandsAndItemsRenderer.submitHandsWithItems(tickDelta, new PoseStack(), submitNodeCollector, gameRenderer.gameRenderState().levelRenderState.playerRenderState, state);
		var frame = featureRenderDispatcher.prepareFrame(submitNodeCollector);
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Terrain", Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView(), Optional.empty(), Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			featureRenderDispatcher.renderAllFeatures(renderPass, frame);
		}
		Profiler.get().pop();
		frame.close();
		RenderSystem.restoreProjectionMatrix();

		poseStack.popPose();
		RenderSystem.getModelViewStack().popMatrix();
		renderingSolid = false;

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	private boolean iris$isAnyHandSolid(FirstPersonHandsAndItemsRenderState state) {
		return !isHandTranslucent(state.mainHandItem) || !isHandTranslucent(state.offHandItem);
	}

	public void renderTranslucent(Matrix4fc modelMatrix, float tickDelta, Camera camera, CameraRenderState cameraState, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		var state = gameRenderer.gameRenderState().levelRenderState.playerRenderState.firstPersonHandsAndItems;

		if (!canRender(camera, gameRenderer) || !Iris.isPackInUseQuick()) {
			bufferSource.endFrame();
			return;
		}

		RenderSystem.backupProjectionMatrix();

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_TRANSLUCENT);

		PoseStack poseStack = setupGlState(gameRenderer, cameraState, modelMatrix, tickDelta);

		poseStack.pushPose();

		Profiler.get().push("iris_hand_translucent");

		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().set(poseStack.last().pose());

		gameRenderer.firstPersonHandsAndItemsRenderer.submitHandsWithItems(tickDelta, new PoseStack(), submitNodeCollector, gameRenderer.gameRenderState().levelRenderState.playerRenderState, state);
		var frame = featureRenderDispatcher.prepareFrame(submitNodeCollector);
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Terrain", Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView(), Optional.empty(), Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			featureRenderDispatcher.renderAllFeatures(renderPass, frame);
		}
		frame.close();
		poseStack.popPose();

		Profiler.get().pop();
		bufferSource.endFrame();

		RenderSystem.restoreProjectionMatrix();

		RenderSystem.getModelViewStack().popMatrix();

		pipeline.setPhase(WorldRenderingPhase.NONE);


		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}

	public boolean isRenderingSolid() {
		return renderingSolid;
	}

	public void destroy() {

	}

	public void endRender() {
	}
}
