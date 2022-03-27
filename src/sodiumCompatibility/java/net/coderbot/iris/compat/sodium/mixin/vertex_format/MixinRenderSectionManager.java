package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.sodium.render.chunk.RenderSectionManager;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lnet/caffeinemc/sodium/render/chunk/draw/DefaultChunkRenderer;<init>(Lnet/caffeinemc/gfx/api/device/RenderDevice;Lnet/caffeinemc/sodium/render/terrain/format/TerrainVertexType;)V"))
	private TerrainVertexType iris$useExtendedVertexFormat$1(TerrainVertexType vertexType) {
		return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}

	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lnet/caffeinemc/sodium/render/chunk/compile/ChunkBuilder;<init>(Lnet/caffeinemc/sodium/render/terrain/format/TerrainVertexType;)V"))
	private TerrainVertexType iris$useExtendedVertexFormat$2(TerrainVertexType vertexType) {
		return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}
}
