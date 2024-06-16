package net.irisshaders.iris.mixin.texture.pbr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(DirectoryLister.class)
public class MixinDirectoryLister {
	@WrapOperation(method = "run(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", remap = false, ordinal = 0))
	private void iris$modifyForEachAction(Map<ResourceLocation, Resource> instance, BiConsumer<? super ResourceLocation, ? super Resource> action, Operation<Void> original, ResourceManager resourceManager) {
		original.call(instance, (BiConsumer<? super ResourceLocation, ? super Resource>) (location, resource) -> {
			String basePath = PBRType.removeSuffix(location.getPath());
			if (basePath != null) {
				ResourceLocation baseLocation = location.withPath(basePath);
				if (resourceManager.getResource(baseLocation).isPresent()) {
					return;
				}
			}
			action.accept(location, resource);
		});
	}
}
