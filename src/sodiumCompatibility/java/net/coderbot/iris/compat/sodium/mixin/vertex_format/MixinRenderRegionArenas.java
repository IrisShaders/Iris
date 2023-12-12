package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ModelQuadFormat;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegionManager.class)
public class MixinRenderRegionArenas {
	@Redirect(method = "uploadMeshes(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegion;Ljava/util/Collection;)V", remap = false,
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkMeshFormats;DEFAULT:Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ModelQuadFormat;",
					remap = false))
	private ModelQuadFormat iris$useExtendedStride() {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : ChunkMeshFormats.DEFAULT;
	}
}
