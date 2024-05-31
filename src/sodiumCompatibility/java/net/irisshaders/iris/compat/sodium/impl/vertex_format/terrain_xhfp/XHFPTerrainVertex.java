package net.irisshaders.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.irisshaders.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static net.irisshaders.iris.compat.sodium.impl.vertex_format.terrain_xhfp.XHFPModelVertexType.STRIDE;

public class XHFPTerrainVertex implements ChunkVertexEncoder, ContextAwareVertexWriter {
	private final QuadViewTerrain.QuadViewTerrainUnsafe quad = new QuadViewTerrain.QuadViewTerrainUnsafe();
	private final Vector3f normal = new Vector3f();

	private BlockContextHolder contextHolder;

	private int vertexCount;
	private float uSum;
	private float vSum;
	private boolean flipUpcomingNormal;

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		this.contextHolder = holder;
	}

	@Override
	public void flipUpcomingQuadNormal() {
		flipUpcomingNormal = true;
	}

	@Override
	public long write(long ptr,
					  Material material, Vertex vertex, int chunkId) {
		uSum += vertex.u;
		vSum += vertex.v;
		vertexCount++;

		MemoryUtil.memPutShort(ptr, XHFPModelVertexType.encodePosition(vertex.x));
		MemoryUtil.memPutShort(ptr + 2L, XHFPModelVertexType.encodePosition(vertex.y));
		MemoryUtil.memPutShort(ptr + 4L, XHFPModelVertexType.encodePosition(vertex.z));
		MemoryUtil.memPutByte(ptr + 6L, (byte) material.bits());
		MemoryUtil.memPutByte(ptr + 7L, (byte) chunkId);

		MemoryUtil.memPutInt(ptr + 8, vertex.color);

		MemoryUtil.memPutInt(ptr + 12, XHFPModelVertexType.encodeTexture(vertex.u, vertex.v));

		MemoryUtil.memPutInt(ptr + 16, vertex.light);

		MemoryUtil.memPutShort(ptr + 32, contextHolder.blockId);
		MemoryUtil.memPutShort(ptr + 34, contextHolder.renderType);
		MemoryUtil.memPutInt(ptr + 36, contextHolder.ignoreMidBlock ? 0 : ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.localPosX, contextHolder.localPosY, contextHolder.localPosZ));
		MemoryUtil.memPutByte(ptr + 39, contextHolder.lightValue);

		if (vertexCount == 4) {
			vertexCount = 0;

			uSum *= 0.25f;
			vSum *= 0.25f;

			int midUV = XHFPModelVertexType.encodeTexture(uSum, vSum);

			MemoryUtil.memPutInt(ptr + 20, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE * 2, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE * 3, midUV);

			uSum = 0;
			vSum = 0;

			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			quad.setup(ptr, STRIDE);
			if (flipUpcomingNormal) {
				NormalHelper.computeFaceNormalFlipped(normal, quad);
				flipUpcomingNormal = false;
			} else {
				NormalHelper.computeFaceNormal(normal, quad);
			}

			int packedNormal = NormI8.pack(normal);

			MemoryUtil.memPutInt(ptr + 28, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 3, packedNormal);

			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			MemoryUtil.memPutInt(ptr + 24, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE * 3, tangent);
		}

		return ptr + STRIDE;
	}
}
