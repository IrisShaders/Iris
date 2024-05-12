package net.irisshaders.iris.compat.sodium.impl.vertex_format;

import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class IrisEntityToTerrainVertexSerializer implements VertexSerializer {
	@Override
	public void serialize(long src, long dst, int vertexCount) {
		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
			MemoryUtil.memPutFloat(dst, MemoryUtil.memGetFloat(src));
			MemoryUtil.memPutFloat(dst + 4, MemoryUtil.memGetFloat(src + 4L));
			MemoryUtil.memPutFloat(dst + 8, MemoryUtil.memGetFloat(src + 8L));
			MemoryUtil.memPutInt(dst + 12, MemoryUtil.memGetInt(src + 12L));
			MemoryUtil.memPutFloat(dst + 16, MemoryUtil.memGetFloat(src + 16L));
			MemoryUtil.memPutFloat(dst + 20, MemoryUtil.memGetFloat(src + 20L));
			MemoryUtil.memPutInt(dst + 24, MemoryUtil.memGetInt(src + 28L));
			MemoryUtil.memPutInt(dst + 28, MemoryUtil.memGetInt(src + 32L));
			MemoryUtil.memPutInt(dst + 32, 0);
			MemoryUtil.memPutInt(dst + 36, MemoryUtil.memGetInt(src + 36L));
			MemoryUtil.memPutInt(dst + 40, MemoryUtil.memGetInt(src + 40L));
			MemoryUtil.memPutInt(dst + 44, MemoryUtil.memGetInt(src + 44L));

			src += EntityVertex.STRIDE;
			dst += IrisVertexFormats.TERRAIN.getVertexSize();
		}

	}
}
