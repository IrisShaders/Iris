package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteSource.Output.class)
public interface MixinSpriteSourceOutput {
	@Inject(method = "add(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/server/packs/resources/Resource;)V", at = @At(value = "HEAD"), cancellable = true)
	private void editMap(ResourceLocation location, Resource arg2, CallbackInfo ci) {
		if (location.getPath().endsWith("_n") || location.getPath().endsWith("_s") || location.getPath().endsWith("_n.png") || location.getPath().endsWith("_s.png")) {
			ci.cancel();
		}
	}
}
