package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link CommonVertexAttribute} corresponding to our
 * extended vertex attributes.
 *
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(CommonVertexAttribute.class)
public class MixinVertexElementType {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static CommonVertexAttribute[] $VALUES;

	@Mutable
	@Shadow
	@Final
	public static int COUNT;

	static {
		int baseOrdinal = $VALUES.length;

		IrisVertexElementTypes.BLOCK_ID
				= VertexElementTypeAccessor.createVertexElementType("BLOCK_ID", baseOrdinal, IrisVertexFormats.ENTITY_ELEMENT);
		IrisVertexElementTypes.MID_TEX_COORD
				= VertexElementTypeAccessor.createVertexElementType("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		IrisVertexElementTypes.TANGENT
				= VertexElementTypeAccessor.createVertexElementType("TANGENT", baseOrdinal + 2, IrisVertexFormats.TANGENT_ELEMENT);
		IrisVertexElementTypes.MID_BLOCK
				= VertexElementTypeAccessor.createVertexElementType("MID_BLOCK", baseOrdinal + 3, IrisVertexFormats.MID_BLOCK_ELEMENT);

		$VALUES = ArrayUtils.addAll($VALUES,
				IrisVertexElementTypes.BLOCK_ID,
				IrisVertexElementTypes.MID_TEX_COORD,
				IrisVertexElementTypes.TANGENT,
				IrisVertexElementTypes.MID_BLOCK);

		COUNT = $VALUES.length;
	}
}
