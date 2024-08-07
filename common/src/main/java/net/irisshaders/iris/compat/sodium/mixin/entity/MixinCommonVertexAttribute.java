package net.irisshaders.iris.compat.sodium.mixin.entity;

import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.sodium.IrisCommonVertexAttributes;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommonVertexAttribute.class)
public class MixinCommonVertexAttribute {
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
			= CommonVertexAttributeInterface.createAttribute("TANGENT", baseOrdinal, IrisVertexFormats.TANGENT_ELEMENT);
		IrisCommonVertexAttributes.MID_TEX_COORD
			= CommonVertexAttributeInterface.createAttribute("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		IrisCommonVertexAttributes.BLOCK_ID
			= CommonVertexAttributeInterface.createAttribute("BLOCK_ID", baseOrdinal + 2, IrisVertexFormats.ENTITY_ELEMENT);
		IrisCommonVertexAttributes.ENTITY_ID
			= CommonVertexAttributeInterface.createAttribute("ENTITY_ID", baseOrdinal + 3, IrisVertexFormats.ENTITY_ID_ELEMENT);
		IrisCommonVertexAttributes.MID_BLOCK
			= CommonVertexAttributeInterface.createAttribute("MID_BLOCK", baseOrdinal + 4, IrisVertexFormats.MID_BLOCK_ELEMENT);

		$VALUES = ArrayUtils.addAll($VALUES,
			IrisCommonVertexAttributes.TANGENT,
			IrisCommonVertexAttributes.MID_TEX_COORD,
			IrisCommonVertexAttributes.BLOCK_ID,
			IrisCommonVertexAttributes.ENTITY_ID,
			IrisCommonVertexAttributes.MID_BLOCK);

		COUNT = $VALUES.length;
	}
}
