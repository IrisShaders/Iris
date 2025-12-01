package net.irisshaders.iris.platform;

import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.spongepowered.asm.mixin.Unique;

public class LayerSet {
	public static final ChunkSectionLayer[] LAYER_SET_VANILLA;

	static {
		LAYER_SET_VANILLA = new ChunkSectionLayer[BlockRenderType.values().length];
		for (int i = 0; i < BlockRenderType.values().length; i++) {
			LAYER_SET_VANILLA[i] = BlockMaterialMapping.convertBlockToRenderType(BlockRenderType.values()[i]);
		}
	}
}
