package net.irisshaders.iris.compat.sodium.impl.vertex_format;

import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class ModelToEntityVertexSerializer implements VertexSerializer {
	@Override
	public void serialize(long src, long dst, int vertexCount) {
		// Only accept quads, to be safe
		int quadCount = vertexCount / 4;
		for (int i = 0; i < quadCount; i++) {
			int normal = MemoryUtil.memGetInt(src + 32);
			int tangent = NormalHelper.computeTangent(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal), MemoryUtil.memGetFloat(src), MemoryUtil.memGetFloat(src + 4), MemoryUtil.memGetFloat(src + 8), MemoryUtil.memGetFloat(src + 16), MemoryUtil.memGetFloat(src + 20),
				MemoryUtil.memGetFloat(src + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 4 + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 8 + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 16 + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 20 + ModelVertex.STRIDE),
				MemoryUtil.memGetFloat(src + ModelVertex.STRIDE + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 4 + ModelVertex.STRIDE + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 8 + ModelVertex.STRIDE + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 16 + ModelVertex.STRIDE + ModelVertex.STRIDE), MemoryUtil.memGetFloat(src + 20 + ModelVertex.STRIDE + ModelVertex.STRIDE));
			float midU = 0, midV = 0;
			for (int vertex = 0; vertex < 4; vertex++) {
				midU += MemoryUtil.memGetFloat(src + 16 + (ModelVertex.STRIDE * vertex));
				midV += MemoryUtil.memGetFloat(src + 20 + (ModelVertex.STRIDE * vertex));
			}

			midU /= 4;
			midV /= 4;

			for (int j = 0; j < 4; j++) {
				MemoryUtil.memCopy(src, dst, 36);
				MemoryUtil.memPutShort(dst + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
				MemoryUtil.memPutShort(dst + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
				MemoryUtil.memPutShort(dst + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
				MemoryUtil.memPutFloat(dst + 42, midU);
				MemoryUtil.memPutFloat(dst + 46, midV);
				MemoryUtil.memPutInt(dst + 50, tangent);

				src += ModelVertex.STRIDE;
				dst += IrisVertexFormats.ENTITY.getVertexSize();
			}
		}
	}
}
