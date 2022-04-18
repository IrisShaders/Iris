package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class MixinOptions_Entrypoint {
	@Unique
	private static boolean iris$initialized;

	@Inject(method = "load()V", at = @At("HEAD"))
	private void iris$beforeLoadOptions(CallbackInfo ci) {
		if (iris$initialized) {
			return;
		}

		iris$initialized = true;
		new Iris().onEarlyInitialize();
	}
}
