package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkRenderShaderBackend;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MultidrawChunkRenderBackend.class)
public abstract class MixinMultidrawChunkRenderBackend extends ChunkRenderShaderBackend<MultidrawGraphicsState> {
    public MixinMultidrawChunkRenderBackend(ChunkVertexType vertexType) {
        // make compiler happy
        super(vertexType);
    }

    @ModifyArg(method = "createRegionTessellation", remap = false,
            at = @At(value = "INVOKE",
                    target = "me/jellysquid/mods/sodium/client/gl/tessellation/TessellationBinding.<init> (" +
                                "Lme/jellysquid/mods/sodium/client/gl/buffer/GlBuffer;" +
                                "[Lme/jellysquid/mods/sodium/client/gl/attribute/GlVertexAttributeBinding;" +
                                "Z" +
                            ")V",
                    remap = false,
                    ordinal = 0))
    private GlVertexAttributeBinding[] iris$addAdditionalBindings(GlVertexAttributeBinding[] base) {
        return Iris.isPackActive() ? ArrayUtils.addAll(base,
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
                        vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
                        vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
                        vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
                        vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))
        ) : base;
    }
}
