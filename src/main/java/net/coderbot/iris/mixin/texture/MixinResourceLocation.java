package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResourceLocation.class, priority = 1010)
public class MixinResourceLocation {
	@Inject(method = "isValidPath", at = @At("HEAD"), cancellable = true)
	private static void iris$blockDUMMY(String string, CallbackInfoReturnable<Boolean> cir) {
		if (string.equals("DUMMY")) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "validPathChar", at = @At("HEAD"), cancellable = true)
	private static void iris$allowInvalidPaths(char c, CallbackInfoReturnable<Boolean> cir) {
		if (c >= 'A' && c <= 'Z') {
			cir.setReturnValue(true);
		}
	}
}
