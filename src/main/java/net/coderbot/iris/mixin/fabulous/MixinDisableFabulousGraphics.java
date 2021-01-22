package net.coderbot.iris.mixin.fabulous;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.GraphicsMode;
import net.minecraft.client.render.WorldRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class MixinDisableFabulousGraphics {
	@Inject(method = "reload()V", at = @At("HEAD"))
	private void iris$disableFabulousGraphics(CallbackInfo ci) {
		GameOptions options = MinecraftClient.getInstance().options;

		if (options.graphicsMode == GraphicsMode.FABULOUS) {
			options.graphicsMode = GraphicsMode.FANCY;
		}
	}
}
