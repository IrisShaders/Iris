package net.coderbot.iris.compat.sodium.mixin.block_id;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.block_rendering.MaterialIdHolder;
import net.coderbot.iris.compat.sodium.impl.block_id.ChunkBuildBuffersExt;
import net.coderbot.iris.compat.sodium.impl.block_id.MaterialIdAwareVertexWriter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Associates the material ID holder with the chunk build buffers, allowing {@link MixinChunkRenderRebuildTask} to pass
 * data to {@link MaterialIdAwareVertexWriter}.
 */
@Mixin(ChunkBuildBuffers.class)
public class MixinChunkBuildBuffers implements ChunkBuildBuffersExt {
    @Unique
    private MaterialIdHolder idHolder;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onConstruct(ChunkVertexType vertexType, BlockRenderPassManager renderPassManager, CallbackInfo ci) {
        Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

        if (blockStateIds != null) {
            this.idHolder = new MaterialIdHolder(blockStateIds);
        } else {
            this.idHolder = new MaterialIdHolder();
        }
    }

    @Redirect(method = "init", remap = false, at = @At(value = "INVOKE",
            target = "me/jellysquid/mods/sodium/client/model/vertex/type/ChunkVertexType.createBufferWriter (" +
                        "Lme/jellysquid/mods/sodium/client/model/vertex/buffer/VertexBufferView;" +
                    ")Lme/jellysquid/mods/sodium/client/model/vertex/VertexSink;", remap = false))
    private VertexSink iris$redirectWriterCreation(ChunkVertexType vertexType, VertexBufferView buffer) {
        VertexSink sink = vertexType.createBufferWriter(buffer);

        if (sink instanceof MaterialIdAwareVertexWriter) {
            ((MaterialIdAwareVertexWriter) sink).iris$setIdHolder(idHolder);
        }

        return sink;
    }

    @Override
    public void iris$setMaterialId(BlockState state, short renderType) {
        this.idHolder.set(state, renderType);
    }

    @Override
    public void iris$resetMaterialId() {
        this.idHolder.reset();
    }
}
