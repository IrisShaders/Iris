package net.irisshaders.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.irisshaders.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.minecraft.util.Mth;
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

	// TODO: FIX

	/*@Override
	public void copyQuadAndFlipNormal() {
		ensureCapacity(4);

		MemoryUtil.memCopy(this.writePointer - STRIDE * 4, this.writePointer, STRIDE * 4);

		// Now flip vertex normals
		int packedNormal = MemoryUtil.memGetInt(this.writePointer + 32);
		int inverted = NormalHelper.invertPackedNormal(packedNormal);

		MemoryUtil.memPutInt(this.writePointer + 32, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 2, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 3, inverted);

		// We just wrote 4 vertices, advance by 4
		for (int i = 0; i < 4; i++) {
			this.advance();
		}

		// Ensure vertices are flushed
		this.flush();
	}*/

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
					  Material material, Vertex[] vertices, int section) {
		// Calculate the center point of the texture region which is mapped to the quad
		float texCentroidU = 0.0f;
		float texCentroidV = 0.0f;

		for (var vertex : vertices) {
			texCentroidU += vertex.u;
			texCentroidV += vertex.v;
		}

		texCentroidU *= (1.0f / 4.0f);
		texCentroidV *= (1.0f / 4.0f);
		int midUV = XHFPModelVertexType.encodeOld(texCentroidU, texCentroidV);
		NormalHelper.computeFaceNormalManual(normal, vertices[0].x, vertices[0].y, vertices[0].z,
			vertices[1].x, vertices[1].y, vertices[1].z,
			vertices[2].x, vertices[2].y, vertices[2].z,
			vertices[3].x, vertices[3].y, vertices[3].z);
		int packedNormal = NormI8.pack(normal);
		int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z,
			vertices[0].x, vertices[0].y, vertices[0].z, vertices[0].u, vertices[0].v,
			vertices[1].x, vertices[1].y, vertices[1].z, vertices[1].u, vertices[1].v,
			vertices[2].x, vertices[2].y, vertices[2].z, vertices[2].u, vertices[2].v);

		for (int i = 0; i < 4; i++) {
			var vertex = vertices[i];

			int x = quantizePosition(vertex.x);
			int y = quantizePosition(vertex.y);
			int z = quantizePosition(vertex.z);

			int u = encodeTexture(texCentroidU, vertex.u);
			int v = encodeTexture(texCentroidV, vertex.v);

			int light = encodeLight(vertex.light);

			MemoryUtil.memPutInt(ptr +  0L, packPositionHi(x, y, z));
			MemoryUtil.memPutInt(ptr +  4L, packPositionLo(x, y, z));
			MemoryUtil.memPutInt(ptr +  8L, vertex.color);
			MemoryUtil.memPutInt(ptr + 12L, packTexture(u, v));
			MemoryUtil.memPutInt(ptr + 16L, packLightAndData(light, material.bits(), section));

			MemoryUtil.memPutShort(ptr + 32, contextHolder.blockId);
			MemoryUtil.memPutShort(ptr + 34, contextHolder.renderType);
			MemoryUtil.memPutInt(ptr + 36, contextHolder.ignoreMidBlock ? 0 : ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.localPosX, contextHolder.localPosY, contextHolder.localPosZ));
			MemoryUtil.memPutByte(ptr + 39, contextHolder.lightValue);

			MemoryUtil.memPutInt(ptr + 20, midUV);
			MemoryUtil.memPutInt(ptr + 28, packedNormal);
			MemoryUtil.memPutInt(ptr + 24, tangent);

			ptr += STRIDE;
		}

		return ptr;
	}

	private static int packPositionHi(int x, int y, int z) {
		return (x >>> 10 & 1023) << 0 | (y >>> 10 & 1023) << 10 | (z >>> 10 & 1023) << 20;
	}

	private static int packPositionLo(int x, int y, int z) {
		return (x & 1023) << 0 | (y & 1023) << 10 | (z & 1023) << 20;
	}

	private static int quantizePosition(float position) {
		return (int)(normalizePosition(position) * 1048576.0F) & 1048575;
	}

	private static float normalizePosition(float v) {
		return (8.0F + v) / 32.0F;
	}

	private static int packTexture(int u, int v) {
		return (u & '\uffff') << 0 | (v & '\uffff') << 16;
	}

	private static int encodeTexture(float center, float x) {
		int bias = x < center ? 1 : -1;
		int quantized = floorInt(x * 32768.0F) + bias & 32767;
		if (bias < 0) {
			quantized = -quantized;
		}

		return quantized;
	}

	private static int encodeLight(int light) {
		int sky = Mth.clamp(light >>> 16 & 255, 8, 248);
		int block = Mth.clamp(light >>> 0 & 255, 8, 248);
		return block << 0 | sky << 8;
	}

	private static int packLightAndData(int light, int material, int section) {
		return (light & '\uffff') << 0 | (material & 255) << 16 | (section & 255) << 24;
	}

	private static int floorInt(float x) {
		return (int)Math.floor((double)x);
	}
}
