package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GLDebug;
import net.coderbot.iris.texture.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
	@Inject(method = "initRenderer", at = @At("RETURN"), remap = false)
	private static void iris$onRendererInit(int debugVerbosity, boolean alwaysFalse, CallbackInfo ci) {
		GLDebug.initRenderer();
		IrisRenderSystem.initRenderer();
		Iris.onRenderSystemInit();
	}

	@Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/AbstractTexture;getId()I", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void _setShaderTexture(int unit, ResourceLocation resourceLocation, CallbackInfo ci, TextureManager lv, AbstractTexture tex) {
		TextureTracker.INSTANCE.onSetShaderTexture(unit, tex.getId());
	}

	@Inject(method = "_setShaderTexture(II)V", at = @At("RETURN"), remap = false)
	private static void _setShaderTexture(int unit, int glId, CallbackInfo ci) {
		TextureTracker.INSTANCE.onSetShaderTexture(unit, glId);
	}
}
