package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MemoryAccess;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class EntityToTerrainVertexSerializer implements VertexSerializer {
	private static final int MIDCOORD = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int TANGENT = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.TANGENT_ELEMENT);

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		// Only accept quads, to be safe
		int quadCount = vertexCount / 4;
		for (int i = 0; i < quadCount; i++) {
			int normal = MemoryAccess.getInt(src + 32);
			int tangent = NormalHelper.computeTangent(null, NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal), MemoryAccess.getFloat(src), MemoryAccess.getFloat(src + 4), MemoryAccess.getFloat(src + 8), MemoryAccess.getFloat(src + 16), MemoryAccess.getFloat(src + 20),
				MemoryAccess.getFloat(src + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 4 + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 8 + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 16 + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 20 + EntityVertex.STRIDE),
				MemoryAccess.getFloat(src + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 4 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 8 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 16 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryAccess.getFloat(src + 20 + EntityVertex.STRIDE + EntityVertex.STRIDE));
			float midU = 0, midV = 0;
			for (int vertex = 0; vertex < 4; vertex++) {
				midU += MemoryAccess.getFloat(src + 16 + (EntityVertex.STRIDE * vertex));
				midV += MemoryAccess.getFloat(src + 20 + (EntityVertex.STRIDE * vertex));
			}

			midU /= 4;
			midV /= 4;

			for (int j = 0; j < 4; j++) {
				MemoryIntrinsics.copyMemory(src, dst, 24);
				MemoryAccess.setInt(dst + 24, MemoryAccess.getInt(src + 28L));
				MemoryAccess.setInt(dst + 28, normal);
				MemoryAccess.setShort(dst + 32, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
				MemoryAccess.setShort(dst + 34, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
				MemoryAccess.setFloat(dst + MIDCOORD, midU);
				MemoryAccess.setFloat(dst + MIDCOORD + 4, midV);
				MemoryAccess.setInt(dst + TANGENT, tangent);
				MemoryAccess.setInt(dst + 48, 0);

				src += EntityVertex.STRIDE;
				dst += IrisVertexFormats.TERRAIN.getVertexSize();
			}
		}
	}
}
