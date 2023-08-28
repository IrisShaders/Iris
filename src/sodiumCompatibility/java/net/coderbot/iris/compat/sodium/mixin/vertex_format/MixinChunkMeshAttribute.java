package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link ChunkMeshAttribute} corresponding to our
 * extended vertex attributes.
 *
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(ChunkMeshAttribute.class)
public class MixinChunkMeshAttribute {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static ChunkMeshAttribute[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisChunkMeshAttributes.POSITION_MATERIAL_MESH
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("POSITION_MATERIAL_MESH", baseOrdinal);
		IrisChunkMeshAttributes.COLOR_SHADE
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("COLOR_SHADE", baseOrdinal + 1);
		IrisChunkMeshAttributes.BLOCK_TEXTURE
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("BLOCK_TEXTURE", baseOrdinal + 2);
		IrisChunkMeshAttributes.LIGHT_TEXTURE
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("LIGHT_TEXTURE", baseOrdinal + 3);
		IrisChunkMeshAttributes.NORMAL
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("NORMAL", baseOrdinal + 4);
		IrisChunkMeshAttributes.TANGENT
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("TANGENT", baseOrdinal + 5);
		IrisChunkMeshAttributes.MID_TEX_COORD
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("MID_TEX_COORD", baseOrdinal + 6);
		IrisChunkMeshAttributes.BLOCK_ID
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("BLOCK_ID", baseOrdinal + 7);
		IrisChunkMeshAttributes.MID_BLOCK
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("MID_BLOCK", baseOrdinal + 8);

		$VALUES = ArrayUtils.addAll($VALUES,
				IrisChunkMeshAttributes.POSITION_MATERIAL_MESH,
				IrisChunkMeshAttributes.COLOR_SHADE,
				IrisChunkMeshAttributes.BLOCK_TEXTURE,
				IrisChunkMeshAttributes.LIGHT_TEXTURE,
				IrisChunkMeshAttributes.NORMAL,
				IrisChunkMeshAttributes.TANGENT,
				IrisChunkMeshAttributes.MID_TEX_COORD,
				IrisChunkMeshAttributes.BLOCK_ID,
				IrisChunkMeshAttributes.MID_BLOCK);
	}
}
