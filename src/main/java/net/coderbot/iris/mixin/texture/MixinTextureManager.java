package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.coderbot.iris.texture.pbr.loader.AtlasPBRLoader;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoaderRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Inject(method = "reload", at = @At("TAIL"))
	private void iris$onTailReloadLambda(PreparableReloadListener.PreparationBarrier arg, ResourceManager arg2, ProfilerFiller arg3, ProfilerFiller arg4, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().thenRun(() -> {
			TextureFormatLoader.reload(arg2);
			PBRTextureManager.INSTANCE.clear();
		});
	}

	@Inject(method = "_dumpAllSheets", at = @At("TAIL"))
	private void iris$dumpPBR(Path path, CallbackInfo ci) {
		((AtlasPBRLoader) PBRTextureLoaderRegistry.INSTANCE.getLoader(TextureAtlas.class)).dumpTextures(path);
	}

	@Inject(method = "close()V", at = @At("TAIL"), remap = false)
	private void iris$onTailClose(CallbackInfo ci) {
		PBRTextureManager.INSTANCE.close();
	}
}
