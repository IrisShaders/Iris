package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.caffeinemc.mods.sodium.client.util.GameRendererStorage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.MojLambdas;
import net.irisshaders.iris.NeoLambdas;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.layer.IsOutlineRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.IrisTimeUniforms;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";


	@Unique
	private WorldRenderingPipeline pipeline;

	@Shadow
	private RenderBuffers renderBuffers;

	@Shadow
	@Final
	private LevelTargetBundle targets;
	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@Shadow
	@Final
	private CloudRenderer cloudRenderer;

	@Unique
	private boolean warned;

	@Unique
	private boolean disableFrustumCulling;

	@Unique
	private Matrix4f iris$modelMatrix = new Matrix4f();

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "render", at = @At("HEAD"))
	private void iris$setupPipeline(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
		DHCompat.checkFrame();

		iris$modelMatrix.set(cameraState.viewRotationMatrix);

		IrisTimeUniforms.updateTime();
		CapturedRenderingState.INSTANCE.setGbufferModelView(cameraState.viewRotationMatrix);
		CapturedRenderingState.INSTANCE.setGbufferProjection(new Matrix4f(((GameRendererStorage) Minecraft.getInstance().gameRenderer).sodium$getProjectionMatrix()));
		float fakeTickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		CapturedRenderingState.INSTANCE.setTickDelta(fakeTickDelta);
		if (((CloudRendererAccessor) this.cloudRenderer).getTexture() != null) {
			CapturedRenderingState.INSTANCE.setCloudTime((this.levelRenderState.gameTime % (((CloudRendererAccessor) this.cloudRenderer).getTexture().width() * 400) + fakeTickDelta) * 0.03F);
		} else {
			CapturedRenderingState.INSTANCE.setCloudTime(0);
		}

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

		this.disableFrustumCulling = pipeline.shouldDisableFrustumCulling();

    	pipeline.beginLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.NONE);
		IrisRenderSystem.backupAndDisableCullingState(pipeline.shouldDisableOcclusionCulling());

		if (Iris.shouldActivateWireframe() && Minecraft.getInstance().isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_LINE);
		}
	}

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V", ordinal = 0, shift = At.Shift.AFTER))
	private void iris$beginLevelRender(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder, @Local(ordinal = 0) FramePass clearPass) {
		FramePass framePass = frameGraphBuilder.addPass("iris_setup");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		framePass.requires(clearPass);
		framePass.executes(() -> {
			GpuBufferSlice params = RenderSystem.getShaderFog();
			pipeline.onBeginClear();
			RenderSystem.setShaderFog(params);
		});
	}


	@WrapWithCondition(method = "executeSolid", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/textures/GpuSampler;Z)V"))
	private boolean skipRenderChunks(ChunkSectionsToRender instance, ChunkSectionLayerGroup chunkSectionLayerGroup, RenderPass renderPass, GpuSampler gpuSampler, boolean b) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline) {
			return !pipeline.skipAllRendering();
		} else {
			return true;
		}
	}

	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
	private void iris$endLevelRender(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
		HandRenderer.INSTANCE.renderTranslucent(cameraState.viewRotationMatrix, cameraState.cameraEntityPartialTicks, Minecraft.getInstance().gameRenderer.mainCamera(), levelRenderState.cameraRenderState, Minecraft.getInstance().gameRenderer, pipeline);
		Profiler.get().popPush("iris_final");

		if (Iris.shouldActivateWireframe() && Minecraft.getInstance().isLocalServer()) {
			IrisRenderSystem.setPolygonMode(GL43C.GL_FILL);
		}
		pipeline.finalizeLevelRendering();
		pipeline = null;

		if (!warned) {
			warned = true;
			Iris.getUpdateChecker().getBetaInfo().ifPresent(info ->
				Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal("A new beta is out for Iris " + info.betaTag + ". Please redownload it.").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
		}

		IrisRenderSystem.restoreCullingState();

	}

	// Do this before main pass submission so shadow maps are ready before terrain draws.
	@Group(name = "that", min = 1, max = 1)
	@Inject(require = 0, method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Z)V"))
	private void iris$renderTerrainShadows(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			pipeline.renderShadows((LevelRendererAccessor) this, Minecraft.getInstance().gameRenderer.mainCamera(), this.levelRenderState.cameraRenderState);
		}
	}

	// TODO IMS 1.21.2
	//@ModifyVariable(method = "renderSky", at = @At(value = "HEAD"), index = 5, argsOnly = true)
	private boolean iris$alwaysRenderSky(boolean value) {
		return false;
	}

	@Inject(method = { MojLambdas.RENDER_SKY, NeoLambdas.NEO_RENDER_SKY }, require = 1, at = @At(value = "HEAD"))
	private static void iris$beginSky(CallbackInfo ci) {
		// Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setPhase(WorldRenderingPhase.CUSTOM_SKY));

		// We've changed the phase, but vanilla doesn't update the shader program at this point before rendering stuff,
		// so we need to manually refresh the shader program so that the correct shader override gets applied.
		// TODO: Move the injection instead
	}

	@Inject(method = { MojLambdas.RENDER_SKY, NeoLambdas.NEO_RENDER_SKY }, require = 1, at = @At(value = "RETURN"))
	private static void iris$endSky(CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setPhase(WorldRenderingPhase.NONE));
	}


	@WrapOperation(method = "executeSolid", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/textures/GpuSampler;Z)V"))
	private void iris$beginTerrainLayer(ChunkSectionsToRender instance, ChunkSectionLayerGroup chunkSectionLayerGroup, RenderPass renderPass, GpuSampler gpuSampler, boolean b, Operation<Void> original) {
		pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(chunkSectionLayerGroup));
		original.call(instance, chunkSectionLayerGroup, renderPass, gpuSampler, b);
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "executeClassicTransparency", require = 1, at = @At(value = "HEAD"))
	private void iris$beginWeather(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
	}


	@Inject(method = "executeClassicTransparency", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;render(Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;Lcom/mojang/renderpearl/api/commands/RenderPass;Lnet/minecraft/world/phys/Vec3;D)V"))
	private void iris$beginWorldBorder(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = "executeClassicTransparency", require = 1, at = @At(value = "RETURN"))
	private void iris$endWeather(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

//	@Inject(method = { MojLambdas.RENDER_MAIN_PASS, NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "deb"))
	private void iris$setDebugRenderStage(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.DEBUG);
	}

//	@Inject(method = { MojLambdas.RENDER_MAIN_PASS, NeoLambdas.NEO_RENDER_MAIN_PASS }, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V", shift = At.Shift.AFTER))
	private void iris$resetDebugRenderStage(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@ModifyArg(method = "submitBlockOutline", index = 2,at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;submitHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IFZ)V"))
	private RenderType iris$beginBlockOutline(RenderType type) {
		return new OuterWrappedRenderType("iris:is_outline", type, IsOutlineRenderStateShard.INSTANCE);
	}

	// TODO this needs to be more consistent.
	@WrapOperation(method = MojLambdas.RENDER_MAIN_PASS, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;executeClassicTransparency(Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
	private void iris$beginTranslucents(LevelRenderer instance, ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame preparedFrame, RenderPass renderPass, Operation<Void> original, @Local(name = "renderPass") LocalRef<RenderPass> pass) {
		pass.get().close();

		pipeline.beginHand();
		HandRenderer.INSTANCE.renderSolid(this.iris$modelMatrix, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), Minecraft.getInstance().gameRenderer.mainCamera(), levelRenderState.cameraRenderState, Minecraft.getInstance().gameRenderer, pipeline);
		Profiler.get().popPush("iris_pre_translucent");
		pipeline.beginTranslucents();

		var newPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Terrain", Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView(), Optional.empty(), Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty());

		original.call(instance, chunkSectionsToRender, preparedFrame, newPass);
		newPass.close();
	}
}
