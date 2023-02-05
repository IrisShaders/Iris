package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.jellysquid.mods.sodium.client.render.chunk.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.vertex.transform.CommonVertexElement;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisCommonVertexElements;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisCommonVertexElements;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link ChunkMeshAttribute} corresponding to our
 * extended vertex attributes.
 *
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(CommonVertexElement.class)
public class MixinCommonVertexElements {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static CommonVertexElement[] $VALUES;

	@Mutable
	@Shadow
	@Final
	public static int COUNT;

	static {
		int baseOrdinal = $VALUES.length;

		IrisCommonVertexElements.TANGENT
				= CommonVertexElementAccessor.createCommonVertexElement("TANGENT", baseOrdinal);
		IrisCommonVertexElements.MID_TEX_COORD
				= CommonVertexElementAccessor.createCommonVertexElement("MID_TEX_COORD", baseOrdinal + 1);
		IrisCommonVertexElements.BLOCK_ID
				= CommonVertexElementAccessor.createCommonVertexElement("BLOCK_ID", baseOrdinal + 2);
		IrisCommonVertexElements.MID_BLOCK
				= CommonVertexElementAccessor.createCommonVertexElement("MID_BLOCK", baseOrdinal + 3);

		$VALUES = ArrayUtils.addAll($VALUES,
				IrisCommonVertexElements.TANGENT,
				IrisCommonVertexElements.MID_TEX_COORD,
				IrisCommonVertexElements.BLOCK_ID,
				IrisCommonVertexElements.MID_BLOCK);

		COUNT = $VALUES.length;
	}

	/**
	 * @author IMS
	 * @reason Add more elements
	 */
	@Overwrite(remap = false)
	public static CommonVertexElement getCommonType(VertexFormatElement element) {
		if (element == DefaultVertexFormat.ELEMENT_POSITION) {
			return CommonVertexElement.POSITION;
		} else if (element == DefaultVertexFormat.ELEMENT_COLOR) {
			return CommonVertexElement.COLOR;
		} else if (element == DefaultVertexFormat.ELEMENT_UV0) {
			return CommonVertexElement.TEXTURE;
		} else if (element == DefaultVertexFormat.ELEMENT_UV1) {
			return CommonVertexElement.OVERLAY;
		} else if (element == DefaultVertexFormat.ELEMENT_UV2) {
			return CommonVertexElement.LIGHT;
		} else if (element == IrisVertexFormats.ENTITY_ELEMENT) {
			return IrisCommonVertexElements.BLOCK_ID;
		} else if (element == IrisVertexFormats.MID_BLOCK_ELEMENT) {
			return IrisCommonVertexElements.MID_BLOCK;
		} else if (element == IrisVertexFormats.TANGENT_ELEMENT) {
			return IrisCommonVertexElements.TANGENT;
		} else if (element == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
			return IrisCommonVertexElements.MID_TEX_COORD;
		} else {
			return element == DefaultVertexFormat.ELEMENT_NORMAL ? CommonVertexElement.NORMAL : null;
		}
	}
}
