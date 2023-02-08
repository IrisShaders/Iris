package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatRegistry;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.VertexSerializer;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.VertexSerializerCache;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.generated.VertexSerializerFactory;
import net.coderbot.iris.compat.sodium.impl.vertex_format.EntityToTerrainVertexSerializer;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = VertexSerializerCache.class, remap = false)
public abstract class MixinVertexSerializerCache {
	@Shadow
	@Final
	private static Long2ReferenceMap<VertexSerializer> CACHE;

	@Shadow
	private static long createKey(VertexFormatDescription a, VertexFormatDescription b) {
		return 0;
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void putSerializerIris(CallbackInfo ci) {
		CACHE.put(createKey(VertexFormatRegistry.get(IrisVertexFormats.ENTITY), VertexFormatRegistry.get(IrisVertexFormats.TERRAIN)), new EntityToTerrainVertexSerializer());
	}
}
