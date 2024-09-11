package net.irisshaders.iris.vertices.sodium.terrain;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class XHFPTerrainVertex implements ChunkVertexEncoder, VertexEncoderInterface {
	private static final int POSITION_MAX_VALUE = 1 << 20;
	private static final int TEXTURE_MAX_VALUE = 1 << 15;
	private static final float MODEL_ORIGIN = 8.0f;
	private static final float MODEL_RANGE = 32.0f;
	private final Vector3f normal = new Vector3f();
	private final int blockIdOffset;
	private final int normalOffset;
	private final int tangentOffset;
	private final int midBlockOffset;
	private final int midUvOffset;
	private final int stride;
	private BlockContextHolder contextHolder;

	public XHFPTerrainVertex(int blockIdOffset, int normalOffset, int tangentOffset, int midUvOffset, int midBlockOffset, int stride) {
		this.blockIdOffset = blockIdOffset;
		this.normalOffset = normalOffset;
		this.tangentOffset = tangentOffset;
		this.midUvOffset = midUvOffset;
		this.midBlockOffset = midBlockOffset;
		this.stride = stride;
	}

	private static int packPositionHi(int x, int y, int z) {
		return (x >>> 10 & 1023) << 0 | (y >>> 10 & 1023) << 10 | (z >>> 10 & 1023) << 20;
	}

	private static int packPositionLo(int x, int y, int z) {
		return (x & 1023) << 0 | (y & 1023) << 10 | (z & 1023) << 20;
	}

	private static int quantizePosition(float position) {
		return ((int) (normalizePosition(position) * POSITION_MAX_VALUE)) & 0xFFFFF;
	}

	private static float normalizePosition(float v) {
		return (MODEL_ORIGIN + v) / MODEL_RANGE;
	}

	private static int packTexture(int u, int v) {
		return ((u & 0xFFFF) << 0) | ((v & 0xFFFF) << 16);
	}

	private static int encodeTexture(float center, float x) {
		// Shrink the texture coordinates (towards the center of the mapped texture region) by the minimum
		// addressable unit (after quantization.) Then, encode the sign of the bias that was used, and apply
		// the inverse transformation on the GPU with a small epsilon.
		//
		// This makes it possible to use much smaller epsilons for avoiding texture bleed, since the epsilon is no
		// longer encoded into the vertex data (instead, we only store the sign.)
		int bias = (x < center) ? 1 : -1;
		int quantized = floorInt(x * TEXTURE_MAX_VALUE) + bias;

		return (quantized & 0x7FFF) | (sign(bias) << 15);
	}

	private static int encodeLight(int light) {
		int sky = Mth.clamp(light >>> 16 & 255, 8, 248);
		int block = Mth.clamp(light >>> 0 & 255, 8, 248);
		return block << 0 | sky << 8;
	}

	private static int sign(int x) {
		// Shift the sign-bit to the least significant bit's position
		// (0) if positive, (1) if negative
		return (x >>> 31);
	}

	private static int packLightAndData(int light, int material, int section) {
		return (light & '\uffff') << 0 | (material & 255) << 16 | (section & 255) << 24;
	}

	private static int floorInt(float x) {
		return (int) Math.floor(x);
	}

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		this.contextHolder = holder;
	}

	@Override
	public long write(long ptr,
					  int material, Vertex[] vertices, int section) {
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
		int packedNormal = 0;
		if (normalOffset != 0 || tangentOffset != 0) {
			NormalHelper.computeFaceNormalManual(normal, vertices[0].x, vertices[0].y, vertices[0].z,
				vertices[1].x, vertices[1].y, vertices[1].z,
				vertices[2].x, vertices[2].y, vertices[2].z,
				vertices[3].x, vertices[3].y, vertices[3].z);
			packedNormal = NormI8.pack(normal);
		}
		int tangent = 0;

		if (tangentOffset != 0) {
			tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z,
				vertices[0].x, vertices[0].y, vertices[0].z, vertices[0].u, vertices[0].v,
				vertices[1].x, vertices[1].y, vertices[1].z, vertices[1].u, vertices[1].v,
				vertices[2].x, vertices[2].y, vertices[2].z, vertices[2].u, vertices[2].v);

			if (tangent == -1) {
				// Try calculating the second triangle
				tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z,
					vertices[2].x, vertices[2].y, vertices[2].z, vertices[2].u, vertices[2].v,
					vertices[3].x, vertices[3].y, vertices[3].z, vertices[3].u, vertices[3].v,
					vertices[0].x, vertices[0].y, vertices[0].z, vertices[0].u, vertices[0].v);
			}
		}

		for (int i = 0; i < 4; i++) {
			var vertex = vertices[i];

			int x = quantizePosition(vertex.x);
			int y = quantizePosition(vertex.y);
			int z = quantizePosition(vertex.z);

			int u = encodeTexture(texCentroidU, vertex.u);
			int v = encodeTexture(texCentroidV, vertex.v);

			int light = encodeLight(vertex.light);

			MemoryUtil.memPutInt(ptr, packPositionHi(x, y, z));
			MemoryUtil.memPutInt(ptr + 4L, packPositionLo(x, y, z));
			MemoryUtil.memPutInt(ptr + 8L, WorldRenderingSettings.INSTANCE.shouldUseSeparateAo() ? ColorABGR.withAlpha(vertex.color, vertex.ao) : ColorHelper.multiplyRGB(vertex.color, vertex.ao));
			MemoryUtil.memPutInt(ptr + 12L, packTexture(u, v));
			MemoryUtil.memPutInt(ptr + 16L, packLightAndData(light, material, section));

			if (blockIdOffset != 0) {
				MemoryUtil.memPutInt(ptr + blockIdOffset, packBlockId(contextHolder));
			}

			if (midBlockOffset != 0) {
				MemoryUtil.memPutInt(ptr + midBlockOffset, contextHolder.ignoreMidBlock() ? 0 : ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.getLocalPosX(), contextHolder.getLocalPosY(), contextHolder.getLocalPosZ()));
				MemoryUtil.memPutByte(ptr + midBlockOffset + 3, contextHolder.getBlockEmission());
			}

			if (midUvOffset != 0) {
				MemoryUtil.memPutInt(ptr + midUvOffset, midUV);
			}

			if (normalOffset != 0) {
				MemoryUtil.memPutInt(ptr + normalOffset, packedNormal);
			}
			if (tangentOffset != 0) {
				MemoryUtil.memPutInt(ptr + tangentOffset, tangent);
			}

			ptr += stride;
		}

		return ptr;
	}

	private int packBlockId(BlockContextHolder contextHolder) {
		return ((contextHolder.getBlockId() + 1) << 1) | (contextHolder.getRenderType() & 1);
	}
}
