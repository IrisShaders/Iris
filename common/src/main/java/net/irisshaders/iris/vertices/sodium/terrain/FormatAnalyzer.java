package net.irisshaders.iris.vertices.sodium.terrain;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.DefaultChunkMeshAttributes;

public class FormatAnalyzer {
	private static final Byte2ObjectMap<ChunkVertexType> classMap = new Byte2ObjectOpenHashMap<>();

	static {
		classMap.put((byte) 0, ChunkMeshFormats.COMPACT);
	}

	public static ChunkVertexType createFormat(boolean blockId, boolean normal, boolean midUV, boolean tangent, boolean midBlock) {
		byte key = 0;
		if (blockId) {
			key |= 1;
		}
		if (normal) {
			key |= 2;
		}
		if (midUV) {
			key |= 4;
		}
		if (tangent) {
			key |= 8;
		}
		if (midBlock) {
			key |= 16;
		}

		if (classMap.containsKey(key)) {
			return classMap.get(key);
		}

		int offset = 20; // Normal Sodium stuff

		int blockIdOffset, normalOffset, tangentOffset, midUvOffset, midBlockOffset;

		if (blockId) {
			blockIdOffset = offset;
			offset += 4;
		} else {
			blockIdOffset = 0;
		}

		if (normal) {
			normalOffset = offset;
			offset += 4;
		} else {
			normalOffset = 0;
		}

		if (midUV) {
			midUvOffset = offset;
			offset += 4;
		} else {
			midUvOffset = 0;
		}

		if (tangent) {
			tangentOffset = offset;
			offset += 4;
		} else {
			tangentOffset = 0;
		}

		if (midBlock) {
			midBlockOffset = offset;
			offset += 4;
		} else {
			midBlockOffset = 0;
		}

		GlVertexFormat.Builder VERTEX_FORMAT = GlVertexFormat.builder(offset)
			.addElement(DefaultChunkMeshAttributes.POSITION, ChunkShaderBindingPoints.ATTRIBUTE_POSITION, 0)
			.addElement(DefaultChunkMeshAttributes.COLOR, ChunkShaderBindingPoints.ATTRIBUTE_COLOR, 8)
			.addElement(DefaultChunkMeshAttributes.TEXTURE, ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE, 12)
			.addElement(DefaultChunkMeshAttributes.LIGHT_MATERIAL_INDEX, ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX, 16);

		if (blockId) {
			VERTEX_FORMAT.addElement(IrisChunkMeshAttributes.BLOCK_ID, 11, blockIdOffset);
		}

		if (normal) {
			VERTEX_FORMAT.addElement(IrisChunkMeshAttributes.NORMAL, 10, normalOffset);
		}

		if (midUV) {
			VERTEX_FORMAT.addElement(IrisChunkMeshAttributes.MID_TEX_COORD, 12, midUvOffset);
		}

		if (tangent) {
			VERTEX_FORMAT.addElement(IrisChunkMeshAttributes.TANGENT, 13, tangentOffset);
		}

		if (midBlock) {
			VERTEX_FORMAT.addElement(IrisChunkMeshAttributes.MID_BLOCK, 14, midBlockOffset);
		}


		return classMap.computeIfAbsent(key, k -> new XHFPModelVertexType(VERTEX_FORMAT.build(), blockIdOffset, normalOffset, tangentOffset, midUvOffset, midBlockOffset));
	}
}
