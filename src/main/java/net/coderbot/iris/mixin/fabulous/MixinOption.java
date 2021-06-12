package net.coderbot.iris.mixin.fabulous;

import net.coderbot.iris.Iris;
import net.minecraft.client.resource.VideoWarningManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.Option;

@Environment(EnvType.CLIENT)
@Mixin(Option.class)
public class MixinOption {
	@Redirect(method = "method_32563", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/VideoWarningManager;hasCancelledAfterWarning()Z"))
	private static boolean iris$onAttemptedToSelectFabulousGraphics(VideoWarningManager manager) {
		// Returning true here will cause Minecraft to cycle between Fancy and Fast, disabling Fabulous graphics
		return Iris.getIrisConfig().areShadersEnabled();
	}
}
