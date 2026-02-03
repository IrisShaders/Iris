package net.irisshaders.iris.mixin.texture;

import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Shadow
	@Final
	private ResourceManager resourceManager;

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = {
		"method_65880",
		"lambda$reload$3"
	}, at = @At("TAIL"), require = 1)
	private void iris$onTailReloadLambda(List list, Void void_, CallbackInfo ci) {
		TextureFormatLoader.reload(this.resourceManager);
		PBRTextureManager.INSTANCE.clear();
	}

	@Inject(method = "dumpAllSheets(Ljava/nio/file/Path;)V", at = @At("RETURN"))
	private void iris$onInnerDumpTextures(Path path, CallbackInfo ci) {
		PBRTextureManager.INSTANCE.dumpTextures(path);
	}

	@Inject(method = "close()V", at = @At("TAIL"), remap = false)
	private void iris$onTailClose(CallbackInfo ci) {
		PBRTextureManager.INSTANCE.close();
	}
}
