package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.OitPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
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
	private Object2BooleanMap<TerrainRenderPass> shouldDrawPass;
	@Unique
	private final Object2BooleanMap<TerrainRenderPass> shouldDrawPassShadow = new Object2BooleanArrayMap<>();

	@Redirect(method = { "prepare", "render" }, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/DefaultChunkRenderer;shouldDrawPass:Lit/unimi/dsi/fastutil/objects/Object2BooleanMap;"))
	private Object2BooleanMap<TerrainRenderPass> change(DefaultChunkRenderer instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? this.shouldDrawPassShadow : this.shouldDrawPass;
	}

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/gui/SodiumOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
	private boolean iris$disableBlockFaceCullingInShadowPass(SodiumOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
		return instance.useBlockFaceCulling;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;begin(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Lnet/caffeinemc/mods/sodium/client/util/FogParameters;Lcom/mojang/renderpearl/api/textures/GpuSampler;Lnet/caffeinemc/mods/sodium/client/util/OitPass;)V"))
	private void iris$forceNearest(DefaultChunkRenderer instance, TerrainRenderPass renderPass, FogParameters fogParameters, GpuSampler gpuSampler, OitPass oitPass, Operation<Void> original) {
		original.call(instance, renderPass, fogParameters, Iris.isPackInUseQuick() ? RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true) : gpuSampler, oitPass);
	}
}
