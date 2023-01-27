package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(DirectoryLister.class)
public class MixinDirectoryLister {
	@Unique
	private static final ThreadLocal<ResourceManager> LOCAL_RESOURCE_MANAGER = new ThreadLocal<>();

	@Inject(method = "run(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;)V", at = @At("HEAD"))
	private void iris$onHeadRun(ResourceManager resourceManager, SpriteSource.Output output, CallbackInfo ci) {
		LOCAL_RESOURCE_MANAGER.set(resourceManager);
	}

	@ModifyArg(method = "run(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", remap = false, ordinal = 0), index = 0)
	private BiConsumer<? super ResourceLocation, ? super Resource> iris$modifyForEachAction(BiConsumer<? super ResourceLocation, ? super Resource> action) {
		ResourceManager resourceManager = LOCAL_RESOURCE_MANAGER.get();
		LOCAL_RESOURCE_MANAGER.set(null);
		if (resourceManager == null) {
			return action;
		}
		return (location, resource) -> {
			ResourceLocation baseLocation = PBRType.removeSuffix(location);
			if (baseLocation != null && resourceManager.getResource(baseLocation).isPresent()) {
				return;
			}
			action.accept(location, resource);
		};
	}
}
