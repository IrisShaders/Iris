package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.vertex.VertexElementSerializer;
import me.jellysquid.mods.sodium.client.render.vertex.VertexElementType;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.VertexTransformers;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisVertexElementTypes;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static me.jellysquid.mods.sodium.client.render.vertex.VertexElementSerializer.getTextureU;
import static me.jellysquid.mods.sodium.client.render.vertex.VertexElementSerializer.getTextureV;
import static me.jellysquid.mods.sodium.client.render.vertex.VertexElementSerializer.setTextureUV;

@Mixin(VertexTransformers.class)
public class MixinVertexTransform {
	/**
	 * @author IMS
	 * @reason Rewrite to edit midTexCoord too
	 */
	@Overwrite(remap = false)
	public static void transformSprite(long ptr, int count, VertexFormatDescription format,
									   float minU, float minV, float maxU, float maxV) {
		long stride = format.stride;
		long offsetUV = format.getElementOffset(VertexElementType.TEXTURE);

		// The width/height of the sprite
		float w = maxU - minU;
		float h = maxV - minV;

		boolean hasMidTexCoord = format.hasElement(IrisVertexElementTypes.MID_TEX_COORD);
		long offsetMidUV = 0;
		if (hasMidTexCoord) {
			offsetMidUV = format.getElementOffset(IrisVertexElementTypes.MID_TEX_COORD);
		}

		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			// The texture coordinates relative to the sprite bounds
			float u = getTextureU(ptr + offsetUV);
			float v = getTextureV(ptr + offsetUV);

			// The texture coordinates in absolute space on the sprite sheet
			float ut = minU + (w * u);
			float vt = minV + (h * v);

			setTextureUV(ptr + offsetUV, ut, vt);

			if (hasMidTexCoord) {
				// The mid texture coordinates relative to the sprite bounds
				float midU = getTextureU(ptr + offsetMidUV);
				float midV = getTextureV(ptr + offsetMidUV);

				// The mid texture coordinates in absolute space on the sprite sheet
				float midUt = minU + (w * midU);
				float midVt = minV + (h * midV);

				setTextureUV(ptr + offsetMidUV, midUt, midVt);
			}

			ptr += stride;
		}
	}
}
