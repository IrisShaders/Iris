package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.format.ChunkMeshAttribute;
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

		IrisChunkMeshAttributes.NORMAL
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("NORMAL", baseOrdinal);
		IrisChunkMeshAttributes.TANGENT
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("TANGENT", baseOrdinal + 1);
		IrisChunkMeshAttributes.MID_TEX_COORD
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("MID_TEX_COORD", baseOrdinal + 2);
		IrisChunkMeshAttributes.BLOCK_ID
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("BLOCK_ID", baseOrdinal + 3);
		IrisChunkMeshAttributes.MID_BLOCK
				= ChunkMeshAttributeAccessor.createChunkMeshAttribute("MID_BLOCK", baseOrdinal + 4);

		$VALUES = ArrayUtils.addAll($VALUES,
				IrisChunkMeshAttributes.NORMAL,
				IrisChunkMeshAttributes.TANGENT,
				IrisChunkMeshAttributes.MID_TEX_COORD,
				IrisChunkMeshAttributes.BLOCK_ID,
				IrisChunkMeshAttributes.MID_BLOCK);
	}
}
