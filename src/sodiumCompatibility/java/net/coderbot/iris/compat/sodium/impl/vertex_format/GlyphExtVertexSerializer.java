package net.coderbot.iris.compat.sodium.impl.vertex_format;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.QuadViewEntity;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormI8;
import net.coderbot.iris.vertices.NormalHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class GlyphExtVertexSerializer implements VertexSerializer {
	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;
	private static final int OFFSET_TEXTURE = 16;
	private static final int OFFSET_MID_TEXTURE = 38;
	private static final int OFFSET_LIGHT = 24;
	private static final int OFFSET_NORMAL = 28;
	private static final int OFFSET_TANGENT = 46;
	private static final QuadViewEntity.QuadViewEntityUnsafe quad = new QuadViewEntity.QuadViewEntityUnsafe();
	private static final Vector3f saveNormal = new Vector3f();

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		float uSum = 0.0f, vSum = 0.0f;

		for (int i = 0; i < vertexCount; i++) {
			float u = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE);
			float v = MemoryUtil.memGetFloat(src + OFFSET_TEXTURE + 4);

			uSum += u;
			vSum += v;

			MemoryUtil.memCopy(src, dst, 28);

			MemoryUtil.memPutShort(dst + 32, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
			MemoryUtil.memPutShort(dst + 34, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
			MemoryUtil.memPutShort(dst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

			src += DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize();
			dst += IrisVertexFormats.GLYPH.getVertexSize();
		}

		endQuad(uSum, vSum, src, dst);
	}

	private static int STRIDE = IrisVertexFormats.GLYPH.getVertexSize();

	private static void endQuad(float uSum, float vSum, long src, long dst) {
		uSum *= 0.25f;
		vSum *= 0.25f;

		quad.setup(src, IrisVertexFormats.GLYPH.getVertexSize());

		float normalX, normalY, normalZ;

		NormalHelper.computeFaceNormal(saveNormal, quad);
		normalX = saveNormal.x;
		normalY = saveNormal.y;
		normalZ = saveNormal.z;
		int normal = NormI8.pack(saveNormal);

		int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

		for (long vertex = 0; vertex < 4; vertex++) {
			MemoryUtil.memPutFloat(dst + OFFSET_MID_TEXTURE - STRIDE * vertex, uSum);
			MemoryUtil.memPutFloat(dst + (OFFSET_MID_TEXTURE + 4) - STRIDE * vertex, vSum);
			MemoryUtil.memPutInt(dst + OFFSET_NORMAL - STRIDE * vertex, normal);
			MemoryUtil.memPutInt(dst + OFFSET_TANGENT - STRIDE * vertex, tangent);
		}
	}
}
