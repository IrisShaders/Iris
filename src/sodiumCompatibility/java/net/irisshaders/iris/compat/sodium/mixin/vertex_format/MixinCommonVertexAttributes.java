package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.IrisCommonVertexAttributes;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link CommonVertexAttribute} corresponding to our
 * extended vertex attributes.
 * <p>
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(CommonVertexAttribute.class)
public class MixinCommonVertexAttributes {
	@Mutable
	@Shadow
	@Final
	public static int COUNT;
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static CommonVertexAttribute[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisCommonVertexAttributes.TANGENT
			= CommonVertexAttributeAccessor.createCommonVertexElement("TANGENT", baseOrdinal, IrisVertexFormats.TANGENT_ELEMENT);
		IrisCommonVertexAttributes.MID_TEX_COORD
			= CommonVertexAttributeAccessor.createCommonVertexElement("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		IrisCommonVertexAttributes.BLOCK_ID
			= CommonVertexAttributeAccessor.createCommonVertexElement("BLOCK_ID", baseOrdinal + 2, IrisVertexFormats.ENTITY_ELEMENT);
		IrisCommonVertexAttributes.ENTITY_ID
			= CommonVertexAttributeAccessor.createCommonVertexElement("ENTITY_ID", baseOrdinal + 3, IrisVertexFormats.ENTITY_ID_ELEMENT);
		IrisCommonVertexAttributes.MID_BLOCK
			= CommonVertexAttributeAccessor.createCommonVertexElement("MID_BLOCK", baseOrdinal + 4, IrisVertexFormats.MID_BLOCK_ELEMENT);

		$VALUES = ArrayUtils.addAll($VALUES,
			IrisCommonVertexAttributes.TANGENT,
			IrisCommonVertexAttributes.MID_TEX_COORD,
			IrisCommonVertexAttributes.BLOCK_ID,
			IrisCommonVertexAttributes.ENTITY_ID,
			IrisCommonVertexAttributes.MID_BLOCK);

		COUNT = $VALUES.length;
	}
}
