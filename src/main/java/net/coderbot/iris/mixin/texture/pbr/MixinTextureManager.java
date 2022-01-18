package net.coderbot.iris.mixin.texture.pbr;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Inject(method = "lambda$reload$4(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Ljava/lang/Void;)V", at = @At("TAIL"))
	private void onTailReloadLambda(ResourceManager resourceManager, Executor applyExecutor, Void void1, CallbackInfo ci) {
		// Clear all PBR textures
	}
}
