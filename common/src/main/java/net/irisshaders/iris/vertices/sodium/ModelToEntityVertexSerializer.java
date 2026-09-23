package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class ModelToEntityVertexSerializer implements VertexSerializer {

	private static final int MIDCOORD = IrisVertexFormats.ENTITY.getElement("mc_midTexCoord").offset();
	private static final int TANGENT = IrisVertexFormats.ENTITY.getElement("at_tangent").offset();

	private static final int SRC_STRIDE = EntityVertex.STRIDE;
	private static final int DST_STRIDE = IrisVertexFormats.ENTITY.getVertexSize();

	@Override
	public void serialize(long srcBase, long dstBase, int vertexCount) {
		final int quadCount = vertexCount >> 2; // divide by 4

		final short entity = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		final short blockEntity = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
		final short item = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem();

		long src = srcBase;
		long dst = dstBase;

		for (int q = 0; q < quadCount; q++) {
			final long v0 = src;
			final long v1 = src + SRC_STRIDE;
			final long v2 = v1 + SRC_STRIDE;
			final long v3 = v2 + SRC_STRIDE;

			final int packedNormal = MemoryUtil.memGetInt(v0 + 32);

			final float nx = NormI8.unpackX(packedNormal);
			final float ny = NormI8.unpackY(packedNormal);
			final float nz = NormI8.unpackZ(packedNormal);

			final float v0x = MemoryUtil.memGetFloat(v0);
			final float v0y = MemoryUtil.memGetFloat(v0 + 4);
			final float v0z = MemoryUtil.memGetFloat(v0 + 8);
			final float v0u = MemoryUtil.memGetFloat(v0 + 16);
			final float v0v = MemoryUtil.memGetFloat(v0 + 20);

			final float v1x = MemoryUtil.memGetFloat(v1);
			final float v1y = MemoryUtil.memGetFloat(v1 + 4);
			final float v1z = MemoryUtil.memGetFloat(v1 + 8);
			final float v1u = MemoryUtil.memGetFloat(v1 + 16);
			final float v1v = MemoryUtil.memGetFloat(v1 + 20);

			final float v2x = MemoryUtil.memGetFloat(v2);
			final float v2y = MemoryUtil.memGetFloat(v2 + 4);
			final float v2z = MemoryUtil.memGetFloat(v2 + 8);
			final float v2u = MemoryUtil.memGetFloat(v2 + 16);
			final float v2v = MemoryUtil.memGetFloat(v2 + 20);

			final int tangent = NormalHelper.computeTangent(null, nx, ny, nz, v0x, v0y, v0z, v0u, v0v, v1x, v1y, v1z, v1u, v1v, v2x, v2y,
				v2z, v2u, v2v);

			final float midU = (v0u + v1u + v2u + MemoryUtil.memGetFloat(v3 + 16)) * 0.25f;
			final float midV = (v0v + v1v + v2v + MemoryUtil.memGetFloat(v3 + 20)) * 0.25f;

			long writeSrc = v0;
			long writeDst = dst;

			for (int i = 0; i < 4; i++) {
				MemoryIntrinsics.copyMemory(writeSrc, writeDst, 36);

				MemoryUtil.memPutShort(writeDst + 36, entity);
				MemoryUtil.memPutShort(writeDst + 38, blockEntity);
				MemoryUtil.memPutShort(writeDst + 40, item);

				MemoryUtil.memPutFloat(writeDst + MIDCOORD, midU);
				MemoryUtil.memPutFloat(writeDst + MIDCOORD + 4, midV);

				MemoryUtil.memPutInt(writeDst + TANGENT, tangent);

				writeSrc += SRC_STRIDE;
				writeDst += DST_STRIDE;
			}

			src += SRC_STRIDE * 4;
			dst += DST_STRIDE * 4L;
		}
	}
}
