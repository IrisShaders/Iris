package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public class MixinDefaultChunkRenderer {
	@Shadow
	@Final
	private boolean[] shouldDraw;
	@Unique
	private final boolean[] shouldDrawShadow = new boolean[DefaultTerrainRenderPasses.ALL.length];

	@Redirect(method = { "prepare", "render" }, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/DefaultChunkRenderer;shouldDraw:[Z"))
	private boolean[] change(DefaultChunkRenderer instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? this.shouldDrawShadow : this.shouldDraw;
	}

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/gui/SodiumOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
	private boolean iris$disableBlockFaceCullingInShadowPass(SodiumOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
		return instance.useBlockFaceCulling;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;begin(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Lnet/caffeinemc/mods/sodium/client/util/FogParameters;Lcom/mojang/renderpearl/api/textures/GpuSampler;Lnet/minecraft/client/renderer/oit/OitStage;)V"))
	private void iris$forceNearest(DefaultChunkRenderer instance,
	                               TerrainRenderPass terrainRenderPass,
	                               FogParameters fogParameters,
	                               GpuSampler gpuSampler,
	                               OitStage oitStage,
	                               Operation<Void> original) {
		original.call(instance, terrainRenderPass, fogParameters, Iris.isPackInUseQuick() ? RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true) : gpuSampler, oitStage);
	}
}
