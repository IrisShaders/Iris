package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexEncoder;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkVertexBufferBuilder.class)
public class MixinChunkVertexBufferBuilder implements ContextAwareVertexWriter {
	@Shadow
	@Final
	private ChunkVertexEncoder encoder;

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).iris$setContextHolder(holder);
		}
	}

	@Override
	public void copyQuadAndFlipNormal() {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).copyQuadAndFlipNormal();
		}
	}
}
