package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
	private static Runnable atlasTextureListener;

	@Inject(method = "initRenderer", at = @At("RETURN"), remap = false)
	private static void iris$onRendererInit(int debugVerbosity, boolean alwaysFalse, CallbackInfo ci) {
		Iris.onRenderSystemInit();
	}

	@Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At("RETURN"))
	private static void _setShaderTexture(int unit, ResourceLocation resourceLocation, CallbackInfo ci) {
		if (unit == 0 && atlasTextureListener != null) {
			atlasTextureListener.run();
		}
	}

	@Inject(method = "_setShaderTexture(II)V", at = @At("RETURN"), remap = false)
	private static void _setShaderTexture(int unit, int glId, CallbackInfo ci) {
		if (unit == 0 && atlasTextureListener != null) {
			atlasTextureListener.run();
		}
	}

	static {
		StateUpdateNotifiers.atlasTextureNotifier = listener -> atlasTextureListener = listener;
	}
}
