package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.chunk.RenderSectionManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexFormats;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRenderer;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static ChunkRenderer createChunkRenderer(RenderDevice device, TerrainVertexType vertexType) {
		return IrisApi.getInstance().isShaderPackInUse() ? new IrisChunkRenderer(device, IrisModelVertexFormats.MODEL_VERTEX_XHFP) : new DefaultChunkRenderer(device, vertexType);
	}

	/**
	 * @author
	 */
	@Overwrite(remap = false)
	private static TerrainVertexType createVertexType() {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : (SodiumClientMod.options().performance.useCompactVertexFormat ? TerrainVertexFormats.COMPACT : TerrainVertexFormats.STANDARD);
	}
}
