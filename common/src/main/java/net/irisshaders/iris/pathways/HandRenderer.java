package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.caffeinemc.mods.sodium.client.util.GameRendererStorage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.GameRendererAccessor;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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

	public HandRenderer() {
		submitNodeCollector = new SubmitNodeStorage();
		featureRenderDispatcher = new FeatureRenderDispatcher(submitNodeCollector, Minecraft.getInstance().getModelManager(), bufferSource.bufferSource(), Minecraft.getInstance().getAtlasManager(), bufferSource.outlineBufferSource(), bufferSource.crumblingBufferSource(), Minecraft.getInstance().font, Minecraft.getInstance().gameRenderer.getGameRenderState());
	}

	private PoseStack setupGlState(GameRenderer gameRenderer, CameraRenderState camera, Matrix4fc modelMatrix, float tickDelta) {
		final PoseStack poseStack = new PoseStack();

		// We need to scale the matrix by 0.125 so the hand doesn't clip through blocks.
		Matrix4f scaleMatrix = new Matrix4f().scale(1F, 1F, DEPTH);
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
			|| Minecraft.getInstance().options.hideGui
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
		if (!canRender(camera, gameRenderer) || !gameRenderer.itemInHandRenderer.iris$isAnyHandSolid() || !Iris.isPackInUseQuick()) {
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

		gameRenderer.itemInHandRenderer.iris$renderHandsWithCustomRenderer(this, tickDelta, new PoseStack(), this.submitNodeCollector, Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.entity(), tickDelta));

		Profiler.get().pop();

		RenderSystem.restoreProjectionMatrix();

		poseStack.popPose();
		RenderSystem.getModelViewStack().popMatrix();

		renderingSolid = false;

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public void renderTranslucent(Matrix4fc modelMatrix, float tickDelta, Camera camera, CameraRenderState cameraState, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(camera, gameRenderer) || !gameRenderer.itemInHandRenderer.iris$isAnyHandTranslucent() || !Iris.isPackInUseQuick()) {
			submitNodeCollector.endFrame();
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

		gameRenderer.itemInHandRenderer.iris$renderHandsWithCustomRenderer(this, tickDelta, new PoseStack(), submitNodeCollector, Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.entity(), tickDelta));

		poseStack.popPose();

		Profiler.get().pop();
		submitNodeCollector.endFrame();

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
		featureRenderDispatcher.renderAllFeatures();
		bufferSource.bufferSource().endBatch();
	}
}
