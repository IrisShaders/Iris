package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.gl.buffer.VertexData;
import me.jellysquid.mods.sodium.client.render.chunk.backends.oneshot.ChunkOneshotGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.format.ChunkMeshAttribute;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkOneshotGraphicsState.class)
public abstract class MixinChunkOneshotGraphicsState {
    @Unique
    private GlVertexFormat<ChunkMeshAttribute> vertexFormat;

    @ModifyArg(method = "upload", remap = false,
            at = @At(value = "INVOKE",
                    target = "me/jellysquid/mods/sodium/client/gl/device/CommandList.uploadData (" +
                                "Lme/jellysquid/mods/sodium/client/gl/buffer/GlMutableBuffer;" +
                                "Lme/jellysquid/mods/sodium/client/gl/buffer/VertexData;" +
                            ")V",
                    remap = false))
    @SuppressWarnings("unchecked")
    private VertexData iris$captureVertexFormat(VertexData vertexData) {
        vertexFormat = (GlVertexFormat<ChunkMeshAttribute>) vertexData.format;

        return vertexData;
    }

    @ModifyArg(method = "upload", remap = false,
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
