package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Inject(method = "lambda$reload$5(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/lang/Void;)V", at = @At("TAIL"))
	private void iris$onTailReloadLambda(ResourceManager resourceManager, Executor applyExecutor, CompletableFuture<?> future, Void void1, CallbackInfo ci) {
		TextureFormatLoader.reload(resourceManager);
		PBRTextureManager.INSTANCE.clear();
	}

	@Inject(method = "_dumpAllSheets(Ljava/nio/file/Path;)V", at = @At("RETURN"))
	private void iris$onInnerDumpTextures(Path path, CallbackInfo ci) {
		PBRTextureManager.INSTANCE.dumpTextures(path);
	}

	@Inject(method = "close()V", at = @At("TAIL"), remap = false)
	private void iris$onTailClose(CallbackInfo ci) {
		PBRTextureManager.INSTANCE.close();
	}
}
