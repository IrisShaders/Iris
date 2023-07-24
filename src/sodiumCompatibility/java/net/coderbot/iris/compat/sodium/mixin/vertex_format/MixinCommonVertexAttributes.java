package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisCommonVertexAttributes;
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
public class MixinCommonVertexAttributes {
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

		IrisCommonVertexAttributes.TANGENT
				= CommonVertexAttributeAccessor.createCommonVertexElement("TANGENT", baseOrdinal, IrisVertexFormats.TANGENT_ELEMENT);
		IrisCommonVertexAttributes.MID_TEX_COORD
				= CommonVertexAttributeAccessor.createCommonVertexElement("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		IrisCommonVertexAttributes.BLOCK_ID
				= CommonVertexAttributeAccessor.createCommonVertexElement("BLOCK_ID", baseOrdinal + 2, IrisVertexFormats.ENTITY_ELEMENT);
		IrisCommonVertexAttributes.MID_BLOCK
				= CommonVertexAttributeAccessor.createCommonVertexElement("MID_BLOCK", baseOrdinal + 3, IrisVertexFormats.MID_BLOCK_ELEMENT);

		$VALUES = ArrayUtils.addAll($VALUES,
				IrisCommonVertexAttributes.TANGENT,
				IrisCommonVertexAttributes.MID_TEX_COORD,
				IrisCommonVertexAttributes.BLOCK_ID,
				IrisCommonVertexAttributes.MID_BLOCK);

		COUNT = $VALUES.length;
	}

	/**
	 * @author IMS
	 * @reason Add more elements
	 */
	@Overwrite(remap = false)
	public static CommonVertexAttribute getCommonType(VertexFormatElement element) {
		if (element == DefaultVertexFormat.ELEMENT_POSITION) {
			return CommonVertexAttribute.POSITION;
		} else if (element == DefaultVertexFormat.ELEMENT_COLOR) {
			return CommonVertexAttribute.COLOR;
		} else if (element == DefaultVertexFormat.ELEMENT_UV0) {
			return CommonVertexAttribute.TEXTURE;
		} else if (element == DefaultVertexFormat.ELEMENT_UV1) {
			return CommonVertexAttribute.OVERLAY;
		} else if (element == DefaultVertexFormat.ELEMENT_UV2) {
			return CommonVertexAttribute.LIGHT;
		} else if (element == IrisVertexFormats.ENTITY_ELEMENT || element == IrisVertexFormats.ENTITY_ID_ELEMENT) {
			return IrisCommonVertexAttributes.BLOCK_ID;
		} else if (element == IrisVertexFormats.MID_BLOCK_ELEMENT) {
			return IrisCommonVertexAttributes.MID_BLOCK;
		} else if (element == IrisVertexFormats.TANGENT_ELEMENT) {
			return IrisCommonVertexAttributes.TANGENT;
		} else if (element == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
			return IrisCommonVertexAttributes.MID_TEX_COORD;
		} else {
			return element == DefaultVertexFormat.ELEMENT_NORMAL ? CommonVertexAttribute.NORMAL : null;
		}
	}
}
