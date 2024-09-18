package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
	@Inject(method = "initRenderer", at = @At("RETURN"), remap = false)
	private static void iris$onRendererInit(int debugVerbosity, boolean alwaysFalse, CallbackInfo ci) {
		Iris.duringRenderSystemInit();
		GLDebug.reloadDebugState();
		IrisRenderSystem.initRenderer();
		IrisSamplers.initRenderer();
		Iris.onRenderSystemInit();
	}

	@Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/AbstractTexture;getId()I", shift = At.Shift.AFTER))
	private static void _setShaderTexture(int unit, ResourceLocation resourceLocation, CallbackInfo ci, @Local AbstractTexture tex) {
		TextureTracker.INSTANCE.onSetShaderTexture(unit, tex.getId());
	}

	@Inject(method = "_setShaderTexture(II)V", at = @At("RETURN"), remap = false)
	private static void _setShaderTexture(int unit, int glId, CallbackInfo ci) {
		TextureTracker.INSTANCE.onSetShaderTexture(unit, glId);
	}
}
