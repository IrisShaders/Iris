package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.Iris;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class MixinResourceLocation {
	@Inject(method = "isValidPath", at = @At("HEAD"), cancellable = true)
	private static void iris$allowBrokenPaths(String string, CallbackInfoReturnable<Boolean> cir) {
		if (string.equals("DUMMY")) {
			// This is here to solve a weird case in DFU that expects minecraft:DUMMY to be invalid.
			cir.setReturnValue(false);
			return;
		}

		for (int i = 0; i < string.length(); ++i) {
			if (ResourceLocation.validPathChar(string.charAt(i))) continue;
			Iris.logger.warn("Path " + string + " is invalid. Iris allows this behavior, however the pack developer should fix this!");
			break;
		}

		cir.setReturnValue(true);
	}
}
