package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.jellysquid.mods.sodium.client.render.vertex.VertexElementType;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link VertexElementType} corresponding to our
 * extended vertex attributes.
 *
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(VertexElementType.class)
public class MixinVertexElementType {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static VertexElementType[] $VALUES;

	@Mutable
	@Shadow
	@Final
	public static int COUNT;

	static {
		int baseOrdinal = $VALUES.length;

		net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.TANGENT
				= VertexElementTypeAccessor.createVertexElementType("TANGENT", baseOrdinal, IrisVertexFormats.TANGENT_ELEMENT);
		net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.MID_TEX_COORD
				= VertexElementTypeAccessor.createVertexElementType("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.BLOCK_ID
				= VertexElementTypeAccessor.createVertexElementType("BLOCK_ID", baseOrdinal + 2, IrisVertexFormats.ENTITY_ELEMENT);
		net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.MID_BLOCK
				= VertexElementTypeAccessor.createVertexElementType("MID_BLOCK", baseOrdinal + 3, IrisVertexFormats.MID_BLOCK_ELEMENT);

		$VALUES = ArrayUtils.addAll($VALUES,
				net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.TANGENT,
				net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.MID_TEX_COORD,
				net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.BLOCK_ID,
				net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes.MID_BLOCK);

		COUNT = $VALUES.length;
	}
}
