package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.Iris;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResourceLocation.class, priority = 1010)
public class MixinResourceLocation {
	@Inject(method = "isValidPath", at = @At("HEAD"), cancellable = true)
	private static void iris$allowInvalidPaths(String path, CallbackInfoReturnable<Boolean> cir) {
		if (path.equals("DUMMY")) {
			// This is here to solve a weird case in DFU that expects minecraft:DUMMY to be invalid.
			cir.setReturnValue(false);
			return;
		}

		for (int i = 0; i < path.length(); ++i) {
			if (ResourceLocation.validPathChar(path.charAt(i))) {
				continue;
			}

			Iris.logger.warn("Detected invalid path '" + path + "'. Iris allows this path to be used, but the resource pack developer should fix it!");
			break;
		}

		cir.setReturnValue(true);
	}
}
