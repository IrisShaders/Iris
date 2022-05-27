package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.BufferVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.format.ChunkModelVertexFormats;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegion.RenderRegionArenas.class)
public class MixinRenderRegionArenas {
	@Redirect(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/gl/attribute/BufferVertexFormat.getStride ()I",
					remap = false))
	private int iris$useExtendedStride(BufferVertexFormat format) {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP.getBufferVertexFormat().getStride() : ChunkModelVertexFormats.DEFAULT.getBufferVertexFormat().getStride();
	}
}
