package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.WrappingVertexConsumerProvider;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IsBlockEntityRenderState;
import net.coderbot.iris.layer.IsEntityRenderState;
import net.coderbot.iris.layer.OuterWrappedRenderType;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinLevelRenderer {
	private static final String RENDER = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;F)V";
	private static final String RENDER_LAYER = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDD)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";
	private static final String RENDER_WORLD_BORDER = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBounds(Lnet/minecraft/client/Camera;)V";
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V";

	@Unique
	private boolean skyTextureEnabled;

	@Unique
	private WorldRenderingPipeline pipeline;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.last().pose());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);

		Program.unbind();

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

		pipeline.beginWorldRendering();
	}

	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = RENDER, at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void iris$endWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Minecraft.getInstance().getProfiler().popPush("iris_final");
		pipeline.finalizeWorldRendering();
		pipeline = null;
		Program.unbind();
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;compileChunksUntil(J)V", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR))
	private void iris$beforeClear(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
	private void iris$afterClear(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY))
	private void iris$beginSky(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.SKY_TEXTURED);
		skyTextureEnabled = true;
	}

	@Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;renderDistance:I"),
	          slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V")))
	private int iris$alwaysRenderSky(Options options) {
		return Math.max(options.renderDistance, 4);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V"))
	private void iris$renderSky$disableTexture(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		if (skyTextureEnabled) {
			skyTextureEnabled = false;
			pipeline.pushProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"))
	private void iris$renderSky$drawHorizon(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		RenderSystem.depthMask(false);

		Vec3 fogColor = CapturedRenderingState.INSTANCE.getFogColor();
		RenderSystem.color3f((float) fogColor.x, (float) fogColor.y, (float) fogColor.z);

		new HorizonRenderer().renderHorizon(matrices);

		RenderSystem.depthMask(true);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSunAngle(F)F"),
		slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;y:D")))
	private void iris$renderSky$tiltSun(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		matrices.mulPose(Vector3f.ZP.rotationDegrees(pipeline.getSunPathRotation()));
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V"))
	private void iris$renderSky$enableTexture(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		if (!skyTextureEnabled) {
			skyTextureEnabled = true;
			pipeline.popProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.SKY_TEXTURED);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void iris$maybeRemoveClouds(PoseStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (!pipeline.shouldRenderClouds()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = RENDER_LAYER, at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderType renderLayer, PoseStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderLayer == RenderType.solid() || renderLayer == RenderType.cutout() || renderLayer == RenderType.cutoutMipped()) {
			pipeline.pushProgram(GbufferProgram.TERRAIN);
		} else if (renderLayer == RenderType.translucent() || renderLayer == RenderType.tripwire()) {
			pipeline.pushProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderLayer);
		}
	}

	@Inject(method = RENDER_LAYER, at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderType renderLayer, PoseStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderLayer == RenderType.solid() || renderLayer == RenderType.cutout() || renderLayer == RenderType.cutoutMipped()) {
			pipeline.popProgram(GbufferProgram.TERRAIN);
		} else if (renderLayer == RenderType.translucent() || renderLayer == RenderType.tripwire()) {
			pipeline.popProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderLayer);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER))
	private void iris$beginWorldBorder(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER, shift = At.Shift.AFTER))
	private void iris$endWorldBorder(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultAlphaFunc()V", shift = At.Shift.AFTER))
	private void iris$applyWeatherOverrides(LightTexture manager, float f, double d, double e, double g, CallbackInfo ci) {
		// TODO: This is a temporary workaround for https://github.com/IrisShaders/Iris/issues/219
		pipeline.pushProgram(GbufferProgram.WEATHER);
		pipeline.popProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities", shift = At.Shift.AFTER))
	private void iris$beginEntities(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		MultiBufferSource provider = renderBuffers.bufferSource();

		if (provider instanceof WrappingVertexConsumerProvider) {
			((WrappingVertexConsumerProvider) provider).setWrappingFunction(layer ->
				new OuterWrappedRenderType("iris:is_entity", layer, IsEntityRenderState.INSTANCE));
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=blockentities", shift = At.Shift.AFTER))
	private void iris$beginBlockEntities(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		MultiBufferSource provider = renderBuffers.bufferSource();

		if (provider instanceof WrappingVertexConsumerProvider) {
			((WrappingVertexConsumerProvider) provider).setWrappingFunction(layer ->
					new OuterWrappedRenderType("iris:is_block_entity", layer, IsBlockEntityRenderState.INSTANCE));
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=destroyProgress"))
	private void iris$endBlockEntities(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		MultiBufferSource provider = renderBuffers.bufferSource();

		if (provider instanceof WrappingVertexConsumerProvider) {
			((WrappingVertexConsumerProvider) provider).setWrappingFunction(null);
		}
	}

	// TODO: Need to figure out how to properly track these values (https://github.com/IrisShaders/Iris/issues/19)
	/*@Inject(method = "renderEntity", at = @At("HEAD"))
	private void iris$beginEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(entity);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;getPos()Lnet/minecraft/util/math/BlockPos;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$getCurrentBlockEntity(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3, VertexConsumerProvider.Immediate immediate, Set var39, Iterator var40, BlockEntity blockEntity2){
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(blockEntity2);
	}*/

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$beginTranslucents(PoseStack matrices, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightTexture lightmapTextureManager, Matrix4f matrix4f,
										CallbackInfo ci, ProfilerFiller profiler, Vec3 vec3d, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										MultiBufferSource.BufferSource immediate) {
		profiler.popPush("iris_entity_draws");
		immediate.endBatch();

		profiler.popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
	private Iterable<Entity> iris$sortEntityList(ClientLevel level) {
		// Sort the entity list first in order to allow vanilla's entity batching code to work better.
		Iterable<Entity> entityIterable = level.entitiesForRendering();

		Map<EntityType<?>, List<Entity>> sortedEntities = new HashMap<>();

		List<Entity> entities = new ArrayList<>();
		entityIterable.forEach(entity -> {
			sortedEntities.computeIfAbsent(entity.getType(), entityType -> new ArrayList<>(32)).add(entity);
		});

		sortedEntities.values().forEach(entities::addAll);

		return entities;
	}
}
