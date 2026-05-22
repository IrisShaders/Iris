package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MemoryAccess;
import net.irisshaders.iris.vertices.NormalHelper;

public class ModelToEntityVertexSerializer implements VertexSerializer {

	private static final int MIDCOORD = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int TANGENT = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.TANGENT_ELEMENT);

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

			final int packedNormal = MemoryAccess.getInt(v0 + 32);

			final float nx = NormI8.unpackX(packedNormal);
			final float ny = NormI8.unpackY(packedNormal);
			final float nz = NormI8.unpackZ(packedNormal);

			final float v0x = MemoryAccess.getFloat(v0);
			final float v0y = MemoryAccess.getFloat(v0 + 4);
			final float v0z = MemoryAccess.getFloat(v0 + 8);
			final float v0u = MemoryAccess.getFloat(v0 + 16);
			final float v0v = MemoryAccess.getFloat(v0 + 20);

			final float v1x = MemoryAccess.getFloat(v1);
			final float v1y = MemoryAccess.getFloat(v1 + 4);
			final float v1z = MemoryAccess.getFloat(v1 + 8);
			final float v1u = MemoryAccess.getFloat(v1 + 16);
			final float v1v = MemoryAccess.getFloat(v1 + 20);

			final float v2x = MemoryAccess.getFloat(v2);
			final float v2y = MemoryAccess.getFloat(v2 + 4);
			final float v2z = MemoryAccess.getFloat(v2 + 8);
			final float v2u = MemoryAccess.getFloat(v2 + 16);
			final float v2v = MemoryAccess.getFloat(v2 + 20);

			final int tangent = NormalHelper.computeTangent(null, nx, ny, nz,
				v0x, v0y, v0z, v0u, v0v,
				v1x, v1y, v1z, v1u, v1v,
				v2x, v2y, v2z, v2u, v2v
			);

			final float midU = (v0u + v1u + v2u + MemoryAccess.getFloat(v3 + 16)) * 0.25f;
			final float midV = (v0v + v1v + v2v + MemoryAccess.getFloat(v3 + 20)) * 0.25f;

			long writeSrc = v0;
			long writeDst = dst;

			for (int i = 0; i < 4; i++) {
				MemoryIntrinsics.copyMemory(writeSrc, writeDst, 36);

				MemoryAccess.setShort(writeDst + 36, entity);
				MemoryAccess.setShort(writeDst + 38, blockEntity);
				MemoryAccess.setShort(writeDst + 40, item);

				MemoryAccess.setFloat(writeDst + MIDCOORD, midU);
				MemoryAccess.setFloat(writeDst + MIDCOORD + 4, midV);

				MemoryAccess.setInt(writeDst + TANGENT, tangent);

				writeSrc += SRC_STRIDE;
				writeDst += DST_STRIDE;
			}

			src += SRC_STRIDE * 4;
			dst += DST_STRIDE * 4;
		}
	}
}
