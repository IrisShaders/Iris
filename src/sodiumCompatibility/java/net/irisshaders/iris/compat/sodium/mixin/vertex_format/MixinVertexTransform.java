package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.transform.CommonVertexElement;
import me.jellysquid.mods.sodium.client.render.vertex.transform.VertexTransform;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.IrisCommonVertexElements;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VertexTransform.class)
public class MixinVertexTransform {
	/**
	 * @author IMS
	 * @reason Rewrite to edit midTexCoord too
	 */
	@Overwrite(remap = false)
	public static void transformSprite(long ptr, int count, VertexFormatDescription format,
									   float minU, float minV, float maxU, float maxV) {
		long stride = format.stride;
		long offsetUV = format.getOffset(CommonVertexElement.TEXTURE);

		boolean hasMidTexCoord = false;
		long offsetMidTexCoord = 0;

		if (format.getElements().contains(IrisVertexFormats.MID_TEXTURE_ELEMENT)) {
			hasMidTexCoord = true;
			offsetMidTexCoord = format.getOffset(IrisCommonVertexElements.MID_TEX_COORD);
		}
		// The width/height of the sprite
		float w = maxU - minU;
		float h = maxV - minV;

		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			// The coordinate relative to the sprite bounds
			float u = MemoryUtil.memGetFloat(ptr + offsetUV);
			float v = MemoryUtil.memGetFloat(ptr + offsetUV + 4);

			// The coordinate absolute to the sprite sheet
			float ut = minU + (w * u);
			float vt = minV + (h * v);

			MemoryUtil.memPutFloat(ptr + offsetUV, ut);
			MemoryUtil.memPutFloat(ptr + offsetUV + 4, vt);

			if (hasMidTexCoord) {
				float midU = MemoryUtil.memGetFloat(ptr + offsetMidTexCoord);
				float midV = MemoryUtil.memGetFloat(ptr + offsetMidTexCoord + 4);

				// The coordinate absolute to the sprite sheet
				float midut = minU + (w * midU);
				float midvt = minV + (h * midV);

				MemoryUtil.memPutFloat(ptr + offsetMidTexCoord, midut);
				MemoryUtil.memPutFloat(ptr + offsetMidTexCoord + 4, midvt);
			}

			ptr += stride;
		}


	}
}
