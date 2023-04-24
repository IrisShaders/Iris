package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisCommonVertexElements;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Uses some rather hacky shenanigans to add a few new enum values to {@link CommonVertexAttribute} corresponding to our
 * extended vertex attributes.
 *
 * Credit goes to Nuclearfarts for the trick.
 */
@Mixin(CommonVertexAttribute.class)
public class MixinCommonVertexElements {
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

		IrisCommonVertexElements.TANGENT
				= CommonVertexElementAccessor.createCommonVertexElement("TANGENT", baseOrdinal, IrisVertexFormats.TANGENT_ELEMENT);
		IrisCommonVertexElements.MID_TEX_COORD
				= CommonVertexElementAccessor.createCommonVertexElement("MID_TEX_COORD", baseOrdinal + 1, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		IrisCommonVertexElements.BLOCK_ID
				= CommonVertexElementAccessor.createCommonVertexElement("BLOCK_ID", baseOrdinal + 2, IrisVertexFormats.ENTITY_ID_ELEMENT);
		IrisCommonVertexElements.MID_BLOCK
				= CommonVertexElementAccessor.createCommonVertexElement("MID_BLOCK", baseOrdinal + 3, IrisVertexFormats.MID_BLOCK_ELEMENT);

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
	@Inject(method = "getCommonType", at = @At("TAIL"), cancellable = true)
	private static void getCommonType2(VertexFormatElement element, CallbackInfoReturnable<CommonVertexAttribute> cir) {
		if (cir.getReturnValue() == null) {
			if (element == IrisVertexFormats.ENTITY_ELEMENT) cir.setReturnValue(IrisCommonVertexElements.BLOCK_ID);
		}
	}
}
