package net.coderbot.iris.mixin.fabulous;

import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LevelRenderer;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public class MixinDisableFabulousGraphics {
	@Inject(method = "onResourceManagerReload", at = @At("HEAD"))
	private void iris$disableFabulousGraphics(CallbackInfo ci) {
		Options options = Minecraft.getInstance().options;

		if (options.graphicsMode == GraphicsStatus.FABULOUS && Iris.getIrisConfig().areShadersEnabled()) {
			options.graphicsMode = GraphicsStatus.FANCY;
		}
	}
}
