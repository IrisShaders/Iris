package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ChunkMeshFormats.class)
public class MixinChunkMeshFormats {
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public static ChunkVertexType getCurrent() {
		return WorldRenderingSettings.INSTANCE.getVertexFormat();
	}
}
