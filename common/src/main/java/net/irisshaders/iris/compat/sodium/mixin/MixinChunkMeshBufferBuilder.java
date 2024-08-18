package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.vertices.sodium.terrain.BlockContextHolder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMeshBufferBuilder.class)
public class MixinChunkMeshBufferBuilder implements VertexEncoderInterface {
	@Shadow(remap = false)
	@Final
	private ChunkVertexEncoder encoder;

	@Override
	public void iris$setContextHolder(BlockContextHolder contextHolder) {
		if (encoder instanceof VertexEncoderInterface) {
			((VertexEncoderInterface) encoder).iris$setContextHolder(contextHolder);
		}
	}
}
