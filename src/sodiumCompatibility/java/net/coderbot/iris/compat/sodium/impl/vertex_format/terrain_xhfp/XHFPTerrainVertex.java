package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import org.joml.Vector3f;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.coderbot.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

import static net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp.XHFPModelVertexType.STRIDE;

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
	public long write(long ptr, Material material,
					  Vertex vertex, int chunkId) {
		uSum += vertex.u;
		vSum += vertex.v;
		vertexCount++;

		MemoryUtil.memPutShort(ptr + 0, XHFPModelVertexType.encodePosition(vertex.x));
		MemoryUtil.memPutShort(ptr + 2, XHFPModelVertexType.encodePosition(vertex.y));
		MemoryUtil.memPutShort(ptr + 4, XHFPModelVertexType.encodePosition(vertex.z));
		MemoryUtil.memPutByte(ptr + 6, material.bits());
		MemoryUtil.memPutByte(ptr + 7, (byte) chunkId);

		MemoryUtil.memPutInt(ptr + 8, vertex.color);

		MemoryUtil.memPutShort(ptr + 12, XHFPModelVertexType.encodeBlockTexture(vertex.u));
		MemoryUtil.memPutShort(ptr + 14, XHFPModelVertexType.encodeBlockTexture(vertex.v));

		MemoryUtil.memPutInt(ptr + 16, vertex.light);

		MemoryUtil.memPutShort(ptr + 36, contextHolder.blockId);
		MemoryUtil.memPutShort(ptr + 38, contextHolder.renderType);
		MemoryUtil.memPutInt(ptr + 40, ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.localPosX, contextHolder.localPosY, contextHolder.localPosZ));

		if (vertexCount == 4) {
			vertexCount = 0;

			// FIXME
			// The following logic is incorrect because OpenGL denormalizes shorts by dividing by 65535. The atlas is
			// based on power-of-two values and so a normalization factor that is not a power of two causes the values
			// used in the shader to be off by enough to cause visual errors. These are most noticeable on 1.18 with POM
			// on block edges.
			//
			// The only reliable way that this can be fixed is to apply the same shader transformations to midTexCoord
			// as Sodium does to the regular texture coordinates - dividing them by the correct power-of-two value inside
			// of the shader instead of letting OpenGL value normalization do the division. However, this requires
			// fragile patching that is not yet possible.
			//
			// As a temporary solution, the normalized shorts have been replaced with regular floats, but this takes up
			// an extra 4 bytes per vertex.

			// NB: Be careful with the math here! A previous bug was caused by midU going negative as a short, which
			// was sign-extended into midTexCoord, causing midV to have garbage (likely NaN data). If you're touching
			// this code, be aware of that, and don't introduce those kinds of bugs!
			//
			// Also note that OpenGL takes shorts in the range of [0, 65535] and transforms them linearly to [0.0, 1.0],
			// so multiply by 65535, not 65536.
			//
			// TODO: Does this introduce precision issues? Do we need to fall back to floats here? This might break
			// with high resolution texture packs.
//			int midU = (int)(65535.0F * Math.min(uSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midV = (int)(65535.0F * Math.min(vSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midTexCoord = (midV << 16) | midU;

			uSum *= 0.25f;
			vSum *= 0.25f;

			MemoryUtil.memPutFloat(ptr + 20, uSum);
			MemoryUtil.memPutFloat(ptr + 20 - STRIDE, uSum);
			MemoryUtil.memPutFloat(ptr + 20 - STRIDE * 2, uSum);
			MemoryUtil.memPutFloat(ptr + 20 - STRIDE * 3, uSum);

			MemoryUtil.memPutFloat(ptr + 24, vSum);
			MemoryUtil.memPutFloat(ptr + 24 - STRIDE, vSum);
			MemoryUtil.memPutFloat(ptr + 24 - STRIDE * 2, vSum);
			MemoryUtil.memPutFloat(ptr + 24 - STRIDE * 3, vSum);

			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			quad.setup(ptr, STRIDE);
			if (flipUpcomingNormal) {
				NormalHelper.computeFaceNormalFlipped(normal, quad);
				flipUpcomingNormal = false;
			} else {
				NormalHelper.computeFaceNormal(normal, quad);
			}
			int packedNormal = NormalHelper.packNormal(normal, 0.0f);



			MemoryUtil.memPutInt(ptr + 32, packedNormal);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE * 3, packedNormal);

			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			MemoryUtil.memPutInt(ptr + 28, tangent);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE, tangent);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 3, tangent);
		}

		return ptr + STRIDE;
	}
}
