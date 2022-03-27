package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.sodium.render.terrain.format.TerrainMeshAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TerrainMeshAttribute.class)
public interface ChunkMeshAttributeAccessor {
    @Invoker(value = "<init>")
    static TerrainMeshAttribute createChunkMeshAttribute(String name, int ordinal) {
        throw new AssertionError();
    }
}
