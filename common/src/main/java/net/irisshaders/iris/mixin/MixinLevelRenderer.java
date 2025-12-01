package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.NeoLambdas;
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
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
	private WorldRenderingPipeline pipeline;

	@Shadow
	private RenderBuffers renderBuffers;

	@Shadow
	private int ticks;

	@Shadow
	@Final
	private LevelTargetBundle targets;
	@Shadow
	@Final
	private LevelRenderState levelRenderState;
	private boolean warned;

	@Unique
	private boolean disableFrustumCulling;

	@Inject(method = "prepareCullFrustum", at = @At("HEAD"), cancellable = true)
	private void iris$disableFrustum(Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, CallbackInfoReturnable<Frustum> cir) {
		if (this.disableFrustumCulling) {
			NonCullingFrustum f = new NonCullingFrustum();
			f.prepare(vec3.x, vec3.y, vec3.z);

			cir.setReturnValue(f);
		}
	}
	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$setupPipeline(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f modelView, Matrix4f projection, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
		DHCompat.checkFrame();

		IrisTimeUniforms.updateTime();
		CapturedRenderingState.INSTANCE.setGbufferModelView(modelView);
		CapturedRenderingState.INSTANCE.setGbufferProjection(projection);
		float fakeTickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
		CapturedRenderingState.INSTANCE.setTickDelta(fakeTickDelta);
		CapturedRenderingState.INSTANCE.setCloudTime((ticks + fakeTickDelta) * 0.03F);

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

		this.disableFrustumCulling = pipeline.shouldDisableFrustumCulling();

    	pipeline.beginLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.NONE);
		IrisRenderSystem.backupAndDisableCullingState(pipeline.shouldDisableOcclusionCulling());

		if (Iris.shouldActivateWireframe() && this.minecraft.isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_LINE);
		}
	}

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V", ordinal = 0, shift = At.Shift.AFTER))
	private void iris$beginLevelRender(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder, @Local(ordinal = 0) FramePass clearPass) {
		FramePass framePass = frameGraphBuilder.addPass("iris_setup");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		framePass.requires(clearPass);
		framePass.executes(() -> {
			GpuBufferSlice params = RenderSystem.getShaderFog();
			pipeline.onBeginClear();
			RenderSystem.setShaderFog(params);
		});
	}


	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
	private void iris$endLevelRender(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f modelMatrix, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
		HandRenderer.INSTANCE.renderTranslucent(modelMatrix, deltaTracker.getGameTimeDeltaPartialTick(true), camera, this.minecraft.gameRenderer, pipeline);
		Profiler.get().popPush("iris_final");

		if (Iris.shouldActivateWireframe() && this.minecraft.isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_FILL);
		}
		pipeline.finalizeLevelRendering();
		pipeline = null;

		if (!warned) {
			warned = true;
			Iris.getUpdateChecker().getBetaInfo().ifPresent(info ->
				Minecraft.getInstance().gui.getChat().addMessage(Component.literal("A new beta is out for Iris " + info.betaTag + ". Please redownload it.").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
		}

		IrisRenderSystem.restoreCullingState();

	}

	// Setup shadow terrain & render shadows before the main terrain setup. We need to do things in this order to
	// avoid breaking other mods such as Light Overlay: https://github.com/IrisShaders/Iris/issues/1356

	// Do this before sky rendering so it's ready before the sky render starts.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/renderer/culling/Frustum;", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera, this.levelRenderState.cameraRenderState);

	}

	// TODO IMS 1.21.2
	//@ModifyVariable(method = "renderSky", at = @At(value = "HEAD"), index = 5, argsOnly = true)
	private boolean iris$alwaysRenderSky(boolean value) {
		return false;
	}

	@Inject(method = { "method_62215", NeoLambdas.NEO_RENDER_SKY }, require = 1, at = @At(value = "HEAD"))
	private void iris$beginSky(CallbackInfo ci) {
		// Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
		pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);

		// We've changed the phase, but vanilla doesn't update the shader program at this point before rendering stuff,
		// so we need to manually refresh the shader program so that the correct shader override gets applied.
		// TODO: Move the injection instead
	}

	@Inject(method = { "method_62215", NeoLambdas.NEO_RENDER_SKY }, require = 1, at = @At(value = "RETURN"))
	private void iris$endSky(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = { "method_62205", NeoLambdas.NEO_RENDER_CLOUDS }, require = 1, at = @At(value = "HEAD"))
	private void iris$beginClouds(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.CLOUDS);
	}

	@Inject(method = { "method_62205", NeoLambdas.NEO_RENDER_CLOUDS }, require = 1, at = @At("RETURN"))
	private void iris$endClouds(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}


	@WrapOperation(method = { "method_62214", NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;)V"))
	private void iris$beginTerrainLayer(ChunkSectionsToRender instance, ChunkSectionLayerGroup chunkSectionLayerGroup, Operation<Void> original) {
		pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(chunkSectionLayerGroup));
		original.call(instance, chunkSectionLayerGroup);
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = { "method_62216", NeoLambdas.NEO_RENDER_WEATHER }, require = 1, at = @At(value = "HEAD"))
	private void iris$beginWeather(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
	}


	@Inject(method = { "method_62216", NeoLambdas.NEO_RENDER_WEATHER }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;render(Lnet/minecraft/client/renderer/state/WorldBorderRenderState;Lnet/minecraft/world/phys/Vec3;DD)V"))
	private void iris$beginWorldBorder(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = { "method_62216", NeoLambdas.NEO_RENDER_WEATHER }, require = 1, at = @At(value = "RETURN"))
	private void iris$endWeather(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = { "method_62214", NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V"))
	private void iris$setDebugRenderStage(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.DEBUG);
	}

	@Inject(method = { "method_62214", NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V", shift = At.Shift.AFTER))
	private void iris$resetDebugRenderStage(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@ModifyArg(method = "renderBlockOutline",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private RenderType iris$beginBlockOutline(RenderType type) {
		return new OuterWrappedRenderType("iris:is_outline", type, IsOutlineRenderStateShard.INSTANCE);
	}

	// TODO this needs to be more consistent.
	@Inject(method = { "method_62214", NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 1))
	private void iris$beginTranslucents(CallbackInfo ci,  @Local(ordinal = 0, argsOnly = true) Matrix4f modelMatrix) {
		pipeline.beginHand();
		HandRenderer.INSTANCE.renderSolid(modelMatrix, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), Minecraft.getInstance().gameRenderer.getMainCamera(), Minecraft.getInstance().gameRenderer, pipeline);
		Profiler.get().popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}
}
