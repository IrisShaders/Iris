package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.VertexSerializerRegistryImpl;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.generated.VertexSerializerFactory;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
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

@Mixin(value = VertexSerializerRegistryImpl.class, remap = false)
public abstract class MixinVertexSerializerCache {
	@Shadow
	@Final
	private Long2ReferenceMap<VertexSerializer> cache;

	@Shadow
	private static long createKey(VertexFormatDescription a, VertexFormatDescription b) {
		return 0;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void putSerializerIris(CallbackInfo ci) {
		cache.put(createKey(VertexFormatRegistry.instance().get(IrisVertexFormats.ENTITY), VertexFormatRegistry.instance().get(IrisVertexFormats.TERRAIN)), new EntityToTerrainVertexSerializer());
	}
}
