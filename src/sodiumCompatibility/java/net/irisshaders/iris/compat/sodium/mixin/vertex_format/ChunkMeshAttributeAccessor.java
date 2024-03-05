package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMeshAttribute.class)
public interface ChunkMeshAttributeAccessor {
	@Invoker(value = "<init>")
	static ChunkMeshAttribute createChunkMeshAttribute(String name, int ordinal) {
		throw new AssertionError();
	}
}
