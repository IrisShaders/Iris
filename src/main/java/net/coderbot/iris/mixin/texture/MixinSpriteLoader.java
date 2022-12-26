package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteLoader.class)
public class MixinSpriteLoader {
	@Inject(method = "loadSprite", at = @At(value = "HEAD"), cancellable = true)
	private static void editMap(ResourceLocation location, Resource arg2, CallbackInfoReturnable<SpriteContents> ci) {
		// TODO: sga_x is the enchanting table particles, we REALLY need a better way to do this.
		if (!(arg2.sourcePackId().equals("vanilla") && arg2.isBuiltin()) && !location.getPath().startsWith("sga") && (location.getPath().endsWith("_n") || location.getPath().endsWith("_s") || location.getPath().endsWith("_n.png") || location.getPath().endsWith("_s.png"))) {
			ci.setReturnValue(null);
		}
	}
}
