package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(value = SpriteCoordinateExpander.class, priority = 1010, remap = false)
public class MixinSpriteCoordinateExpander {
	/**
	 * @author IMS
	 * @reason Rewrite
	 */
	@Overwrite(remap = false)
	private static void transform(long ptr, int count, VertexFormatDescription format,
								  float minU, float minV, float maxU, float maxV) {
		long stride = format.stride();
		long offsetUV = format.getElementOffset(CommonVertexAttribute.TEXTURE);

		// The width/height of the sprite
		float w = maxU - minU;
		float h = maxV - minV;

		boolean hasMidTexCoord = format.containsElement(IrisVertexElementTypes.MID_TEX_COORD);
		long offsetMidUV = 0;
		if (hasMidTexCoord) {
			offsetMidUV = format.getElementOffset(IrisVertexElementTypes.MID_TEX_COORD);
		}

		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			// The texture coordinates relative to the sprite bounds
			float u = TextureAttribute.getU(ptr + offsetUV);
			float v = TextureAttribute.getV(ptr + offsetUV);

			// The texture coordinates in absolute space on the sprite sheet
			float ut = minU + (w * u);
			float vt = minV + (h * v);

			TextureAttribute.put(ptr + offsetUV, ut, vt);

			if (hasMidTexCoord) {
				// The mid texture coordinates relative to the sprite bounds
				float midU = TextureAttribute.getU(ptr + offsetMidUV);
				float midV = TextureAttribute.getV(ptr + offsetMidUV);

				// The mid texture coordinates in absolute space on the sprite sheet
				float midUt = minU + (w * midU);
				float midVt = minV + (h * midV);

				TextureAttribute.put(ptr + offsetMidUV, midUt, midVt);
			}

			ptr += stride;
		}
	}
}
