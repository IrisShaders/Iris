package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
	@Shadow
	private RenderSectionManager renderSectionManager;
	@Unique
	private static boolean renderLightsOnly = false;
	@Unique
	private static int beList = 0;

	static {
		ShadowRenderingState.setBlockEntityRenderFunction((shadowRenderer, bufferSource, modelView, camera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, lightsOnly) -> {
			renderLightsOnly = lightsOnly;

			SodiumWorldRenderer.instance().renderBlockEntities(modelView, bufferSource, ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getDestructionProgress(), camera, tickDelta, null);

			int finalBeList = beList;

			beList = 0;

			return finalBeList;
		});
	}

	@Unique
	private float lastSunAngle;

	@Inject(method = "renderBlockEntity", at = @At("HEAD"), cancellable = true)
	private static void checkRenderShadow(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, LocalPlayer player, LocalBooleanRef isGlowing, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (renderLightsOnly && entity.getBlockState().getLightEmission() == 0) {
				ci.cancel();
			}
			beList++;
		}
	}

	@Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
	private void setupShadowTerrain(Camera camera, Viewport viewport, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			float sunAngle = Minecraft.getInstance().level.getSunAngle(CapturedRenderingState.INSTANCE.getTickDelta());
			if (this.lastSunAngle != sunAngle) {
				this.lastSunAngle = sunAngle;
				this.renderSectionManager.notifyChangedCamera();
			}

			this.renderSectionManager.updateRenderLists(camera, viewport, spectator, updateChunksImmediately);

			ci.cancel();
		}
	}
}
