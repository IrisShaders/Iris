package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectoryLister.class)
public class MixinDirectoryLister {
	@Inject(method = "lambda$run$3(Lnet/minecraft/resources/FileToIdConverter;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/server/packs/resources/Resource;)V", at = @At("HEAD"), cancellable = true)
	private void iris$beforeAddSprite(FileToIdConverter converter, SpriteSource.Output output, ResourceLocation location, Resource resource, CallbackInfo ci) {
		if (!resource.isBuiltin()
				&& !(location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
						&& location.getPath().startsWith("textures/particle/sga_"))
				&& PBRType.isPBRTexturePath(location.getPath())) {
			ci.cancel();
		}
	}
}
