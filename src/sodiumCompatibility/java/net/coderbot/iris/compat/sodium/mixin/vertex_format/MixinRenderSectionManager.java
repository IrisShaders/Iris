package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
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
					target = "me/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer.<init> (" +
								"Lme/jellysquid/mods/sodium/client/gl/device/RenderDevice;" +
								"Lme/jellysquid/mods/sodium/client/model/vertex/type/ChunkVertexType;" +
							")V"))
	private ChunkVertexType iris$useExtendedVertexFormat$1(ChunkVertexType vertexType) {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}

	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.<init> (" +
								"Lme/jellysquid/mods/sodium/client/model/vertex/type/ChunkVertexType;" +
							")V"))
	private ChunkVertexType iris$useExtendedVertexFormat$2(ChunkVertexType vertexType) {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}
}
