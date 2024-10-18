package net.irisshaders.iris.mixin.texture;

import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
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
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = {
		"method_18167",
		"lambda$reload$4"
	}, at = @At("TAIL"), require = 1)
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
