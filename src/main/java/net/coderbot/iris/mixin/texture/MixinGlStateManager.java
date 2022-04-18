package net.coderbot.iris.mixin.texture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;

import net.coderbot.iris.texture.TextureTracker;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Inject(method = "_bindTexture(I)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V", shift = Shift.AFTER, remap = false))
	private static void iris$onBindTexture(int id, CallbackInfo ci) {
		TextureTracker.INSTANCE.onBindTexture(id);
	}

	@Inject(method = "_deleteTexture(I)V", at = @At("TAIL"))
	private static void iris$onDeleteTexture(int id, CallbackInfo ci) {
		TextureTracker.INSTANCE.onDeleteTexture(id);
	}

	@Inject(method = "_deleteTextures([I)V", at = @At("TAIL"))
	private static void iris$onDeleteTextures(int[] ids, CallbackInfo ci) {
		for (int id : ids) {
			TextureTracker.INSTANCE.onDeleteTexture(id);
		}
	}
}
