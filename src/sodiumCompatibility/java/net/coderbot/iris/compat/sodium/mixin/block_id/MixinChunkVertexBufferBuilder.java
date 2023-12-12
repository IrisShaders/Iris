package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ModelQuadEncoder;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMeshBufferBuilder.class)
public class MixinChunkVertexBufferBuilder implements ContextAwareVertexWriter {
	@Shadow
	@Final
	private ModelQuadEncoder encoder;

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).iris$setContextHolder(holder);
		}
	}

	@Override
	public void flipUpcomingQuadNormal() {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).flipUpcomingQuadNormal();
		}
	}
}
