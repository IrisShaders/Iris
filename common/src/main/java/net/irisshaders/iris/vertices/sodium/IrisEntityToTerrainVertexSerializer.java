package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MemoryAccess;
import org.lwjgl.system.MemoryUtil;

public class IrisEntityToTerrainVertexSerializer implements VertexSerializer {
	@Override
	public void serialize(long src, long dst, int vertexCount) {
		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
			MemoryAccess.setFloat(dst, MemoryAccess.getFloat(src));
			MemoryAccess.setFloat(dst + 4, MemoryAccess.getFloat(src + 4L));
			MemoryAccess.setFloat(dst + 8, MemoryAccess.getFloat(src + 8L));
			MemoryAccess.setInt(dst + 12, MemoryAccess.getInt(src + 12L));
			MemoryAccess.setFloat(dst + 16, MemoryAccess.getFloat(src + 16L));
			MemoryAccess.setFloat(dst + 20, MemoryAccess.getFloat(src + 20L));
			MemoryAccess.setInt(dst + 24, MemoryAccess.getInt(src + 28L));
			MemoryAccess.setInt(dst + 28, MemoryAccess.getInt(src + 32L));
			MemoryAccess.setInt(dst + 32, 0);
			MemoryAccess.setInt(dst + 36, MemoryAccess.getInt(src + 36L));
			MemoryAccess.setInt(dst + 40, MemoryAccess.getInt(src + 40L));
			MemoryAccess.setInt(dst + 44, MemoryAccess.getInt(src + 44L));

			src += IrisVertexFormats.ENTITY.getVertexSize();
			dst += IrisVertexFormats.TERRAIN.getVertexSize();
		}

	}
}
