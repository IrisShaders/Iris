package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Enables usage of the extended vertex format needed by Iris.
 */
@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
    @ModifyArg(method = "initRenderer()V", remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "me/jellysquid/mods/sodium/client/render/SodiumWorldRenderer.createChunkRenderBackend (" +
                                "Lme/jellysquid/mods/sodium/client/gl/device/RenderDevice;" +
                                "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions;" +
                                "Lme/jellysquid/mods/sodium/client/model/vertex/type/ChunkVertexType;" +
                            ")Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderBackend;"))
    private ChunkVertexType iris$overrideVertexType(ChunkVertexType vertexType) {
        return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
    }
}
