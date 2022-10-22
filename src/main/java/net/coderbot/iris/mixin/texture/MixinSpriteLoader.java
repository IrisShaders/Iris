package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.Iris;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(SpriteLoader.class)
public class MixinSpriteLoader {
	@ModifyArg(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteLoader;loadSpriteContents(Ljava/util/Map;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private Map<ResourceLocation, Resource> editMap(Map<ResourceLocation, Resource> map) {
		map = new ConcurrentHashMap<>(map);
		Iterator<ResourceLocation> it = map.keySet().iterator();

		while (it.hasNext()) {
			ResourceLocation location = it.next();
			if (location.getPath().endsWith("_n") || location.getPath().endsWith("_s") || location.getPath().endsWith("_n.png") || location.getPath().endsWith("_s.png")) {
				Iris.logger.warn("REMOVING " + location.getPath());
				map.remove(location);
			}
		}

		return map;
	}
}
