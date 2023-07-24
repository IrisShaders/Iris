package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegion.DeviceResources.class)
public class MixinRenderRegionArenas {
	@Redirect(method = "<init>", remap = false,
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkMeshFormats;COMPACT:Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;",
					remap = false))
	private ChunkVertexType iris$useExtendedStride() {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : ChunkMeshFormats.COMPACT;
	}
}
