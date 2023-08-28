package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;<init>(Lme/jellysquid/mods/sodium/client/gl/device/RenderDevice;Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$1(ChunkVertexType vertexType) {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}

	@ModifyArg(method = "<init>",
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/executor/ChunkBuilder;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$2(ChunkVertexType vertexType) {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}
}
