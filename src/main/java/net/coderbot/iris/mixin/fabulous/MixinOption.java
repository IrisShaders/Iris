package net.coderbot.iris.mixin.fabulous;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.options.Option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(Option.class)
public class MixinOption {
	@Redirect(method = "method_18554", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;supportsGl30()Z"))
	private static boolean iris$onAttemptedToSelectFabulousGraphics() {
		// Returning false here will cause Minecraft to cycle between Fancy and Fast, disabling Fabulous graphics
		return false;
	}
}
