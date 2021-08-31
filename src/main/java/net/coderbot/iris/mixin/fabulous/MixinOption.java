package net.coderbot.iris.mixin.fabulous;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;

@Environment(EnvType.CLIENT)
@Mixin(Option.class)
public class MixinOption {
	@Redirect(method = "lambda$static$65", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;supportsFramebufferBlit()Z"))
	private static boolean iris$onAttemptedToSelectFabulousGraphics() {
		// Returning false here will cause Minecraft to cycle between Fancy and Fast, disabling Fabulous graphics
		if(!Iris.getIrisConfig().areShadersEnabled()) {
			return GlStateManager.supportsFramebufferBlit();
		}
		return false;
	}
}
