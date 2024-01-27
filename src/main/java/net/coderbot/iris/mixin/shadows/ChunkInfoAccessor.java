package net.coderbot.iris.mixin.shadows;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.RenderChunkInfo.class)
public interface ChunkInfoAccessor {
	@Accessor("chunk")
	ChunkRenderDispatcher.RenderChunk getChunk();
}
