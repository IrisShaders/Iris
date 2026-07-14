package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderChunkRenderer.class)
public class MixinShaderChunkRenderer {
    @Redirect(method = "createShader", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;vertexFormat:Lcom/mojang/renderpearl/api/vertex/VertexFormat;"))
    private VertexFormat iris$forceSoWeCanLookUpLater(ShaderChunkRenderer instance) {
        return ChunkMeshFormats.COMPACT.getVertexFormat();
    }
}
