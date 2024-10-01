package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.layer.IsOutlineRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.IrisTimeUniforms;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private static WorldRenderingPipeline pipeline;

	@Shadow
	private RenderBuffers renderBuffers;

	@Shadow
	private int ticks;

	@Shadow
	private Frustum cullingFrustum;

	private boolean warned;

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$setupPipeline(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {
		DHCompat.checkFrame();

		IrisTimeUniforms.updateTime();
		CapturedRenderingState.INSTANCE.setGbufferModelView(modelView);
		CapturedRenderingState.INSTANCE.setGbufferProjection(projection);
		float fakeTickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
		CapturedRenderingState.INSTANCE.setTickDelta(fakeTickDelta);
		CapturedRenderingState.INSTANCE.setCloudTime((ticks + fakeTickDelta) * 0.03F);

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

		if (pipeline.shouldDisableFrustumCulling()) {
			this.cullingFrustum = new NonCullingFrustum();
			this.cullingFrustum.prepare(camera.getPosition().x(), camera.getPosition().y(), camera.getPosition().z());
		}
		pipeline.beginLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.NONE);
		Minecraft.getInstance().smartCull = !pipeline.shouldDisableOcclusionCulling();

		if (Iris.shouldActivateWireframe() && this.minecraft.isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_LINE);
		}
	}

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "method_62218", at = @At(value = "TAIL"))
	private static void iris$beginLevelRender(Vector4f vector4f, CallbackInfo ci) {

	}


	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lnet/minecraft/client/renderer/FogParameters;)V"))
	private void iris$endLevelRender(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f modelMatrix, Matrix4f matrix4f2, CallbackInfo ci) {
		HandRenderer.INSTANCE.renderTranslucent(modelMatrix, deltaTracker.getGameTimeDeltaPartialTick(true), camera, gameRenderer, pipeline);
		Profiler.get().popPush("iris_final");
		pipeline.finalizeLevelRendering();
		pipeline = null;

		if (!warned) {
			warned = true;
			Iris.getUpdateChecker().getBetaInfo().ifPresent(info ->
				Minecraft.getInstance().gui.getChat().addMessage(Component.literal("A new beta is out for Iris " + info.betaTag + ". Please redownload it.").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
		}

		if (Iris.shouldActivateWireframe() && this.minecraft.isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_FILL);
		}
	}

	// Setup shadow terrain & render shadows before the main terrain setup. We need to do things in this order to
	// avoid breaking other mods such as Light Overlay: https://github.com/IrisShaders/Iris/issues/1356

	// Do this before sky rendering so it's ready before the sky render starts.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;collectVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;)Z", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera);

	}

	// TODO IMS 1.21.2
	//@ModifyVariable(method = "renderSky", at = @At(value = "HEAD"), index = 5, argsOnly = true)
	private boolean iris$alwaysRenderSky(boolean value) {
		return false;
	}

	@Inject(method = "method_62215", at = @At(value = "HEAD"))
	private void iris$beginSky(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float f, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
		// Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
		pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);

		// We've changed the phase, but vanilla doesn't update the shader program at this point before rendering stuff,
		// so we need to manually refresh the shader program so that the correct shader override gets applied.
		// TODO: Move the injection instead
		RenderSystem.setShader(CoreShaders.POSITION);
	}

	@Inject(method = "method_62215", at = @At(value = "RETURN"))
	private void iris$endSky(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float f, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "method_62205", at = @At(value = "HEAD"))
	private void iris$beginClouds(ResourceHandle<?> resourceHandle, int i, CloudStatus cloudStatus, float f, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, float g, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.CLOUDS);
	}

	@Inject(method = "method_62205", at = @At("RETURN"))
	private void iris$endClouds(ResourceHandle<?> resourceHandle, int i, CloudStatus cloudStatus, float f, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, float g, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}


	@Inject(method = "renderSectionLayer", at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderType renderType, double d, double e, double f, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(renderType));
	}

	@Inject(method = "renderSectionLayer", at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderType renderType, double d, double e, double f, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "method_62216", at = @At(value = "HEAD"))
	private void iris$beginWeather(FogParameters fogParameters, LightTexture lightTexture, float f, Vec3 vec3, int i, float g, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
	}


	@Inject(method = "method_62216", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;render(Lnet/minecraft/world/level/border/WorldBorder;Lnet/minecraft/world/phys/Vec3;DD)V"))
	private void iris$beginWorldBorder(FogParameters fogParameters, LightTexture lightTexture, float f, Vec3 vec3, int i, float g, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = "method_62216", at = @At(value = "RETURN"))
	private void iris$endWeather(FogParameters fogParameters, LightTexture lightTexture, float f, Vec3 vec3, int i, float g, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"))
	private void iris$setDebugRenderStage(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f matrix4f, Matrix4f matrix4f2, ResourceHandle<?> resourceHandle, ResourceHandle<?> resourceHandle2, ResourceHandle<?> resourceHandle3, ResourceHandle<?> resourceHandle4, boolean bl, Frustum frustum, ResourceHandle<?> resourceHandle5, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.DEBUG);
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V", shift = At.Shift.AFTER))
	private void iris$resetDebugRenderStage(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f matrix4f, Matrix4f matrix4f2, ResourceHandle<?> resourceHandle, ResourceHandle<?> resourceHandle2, ResourceHandle<?> resourceHandle3, ResourceHandle<?> resourceHandle4, boolean bl, Frustum frustum, ResourceHandle<?> resourceHandle5, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@ModifyArg(method = "renderBlockOutline",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private RenderType iris$beginBlockOutline(RenderType type) {
		return new OuterWrappedRenderType("iris:is_outline", type, IsOutlineRenderStateShard.INSTANCE);
	}

	@Inject(method = "method_62214", at = @At(value = "CONSTANT", args = "stringValue=translucent"))
	private void iris$beginTranslucents(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f modelMatrix, Matrix4f matrix4f2, ResourceHandle<?> resourceHandle, ResourceHandle<?> resourceHandle2, ResourceHandle<?> resourceHandle3, ResourceHandle<?> resourceHandle4, boolean bl, Frustum frustum, ResourceHandle<?> resourceHandle5, CallbackInfo ci) {
		pipeline.beginHand();
		HandRenderer.INSTANCE.renderSolid(modelMatrix, deltaTracker.getGameTimeDeltaPartialTick(true), camera, Minecraft.getInstance().gameRenderer, pipeline);
		Profiler.get().popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}
}
