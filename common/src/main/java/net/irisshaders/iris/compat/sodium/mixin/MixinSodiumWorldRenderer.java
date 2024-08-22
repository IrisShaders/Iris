package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
	@Redirect(method = "setupTerrain", remap = false,
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;needsUpdate()Z",
			remap = false))
	private boolean iris$forceChunkGraphRebuildInShadowPass(RenderSectionManager instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Detect when the sun/moon isn't moving
			return true;
		} else {
			return instance.needsUpdate();
		}
	}

	@Unique
	private static boolean renderLightsOnly = false;
	@Unique
	private static int beList = 0;

	static {
		ShadowRenderingState.setBlockEntityRenderFunction((shadowRenderer, bufferSource, modelView, camera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, lightsOnly) -> {
			renderLightsOnly = lightsOnly;

			SodiumWorldRenderer.instance().renderBlockEntities(modelView, bufferSource, ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getDestructionProgress(), camera, tickDelta);

			int finalBeList = beList;

			beList = 0;

			return finalBeList;
		});
	}

	@Inject(method = "renderBlockEntity", at = @At("HEAD"), cancellable = true)
	private static void checkRenderShadow(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (renderLightsOnly && entity.getBlockState().getLightEmission() == 0) {
				ci.cancel();
			}
			beList++;
		}
	}
}
