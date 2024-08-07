package net.irisshaders.iris.vertices.sodium.terrain;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.DefaultChunkMeshAttributes;

/**
 * Like HFPModelVertexType, but extended to support Iris. The extensions aren't particularly efficient right now.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	public static final int STRIDE = 40;

	public static final GlVertexFormat VERTEX_FORMAT = GlVertexFormat.builder(STRIDE)
		.addElement(DefaultChunkMeshAttributes.POSITION_HI, ChunkShaderBindingPoints.ATTRIBUTE_POSITION_HI, 0)
		.addElement(DefaultChunkMeshAttributes.POSITION_LO, ChunkShaderBindingPoints.ATTRIBUTE_POSITION_LO, 4)
		.addElement(DefaultChunkMeshAttributes.COLOR, ChunkShaderBindingPoints.ATTRIBUTE_COLOR, 8)
		.addElement(DefaultChunkMeshAttributes.TEXTURE, ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE, 12)
		.addElement(DefaultChunkMeshAttributes.LIGHT_MATERIAL_INDEX, ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX, 16)
		.addElement(IrisChunkMeshAttributes.MID_TEX_COORD, 12, 20)
		.addElement(IrisChunkMeshAttributes.TANGENT, 13, 24)
		.addElement(IrisChunkMeshAttributes.NORMAL, 10, 28)
		.addElement(IrisChunkMeshAttributes.BLOCK_ID, 11, 32)
		.addElement(IrisChunkMeshAttributes.MID_BLOCK, 14, 36)
		.build();

	private static final int POSITION_MAX_VALUE = 65536;
	private static final int TEXTURE_MAX_VALUE = 32768;

	private static final float MODEL_ORIGIN = 8.0f;
	private static final float MODEL_RANGE = 32.0f;
	private static final float MODEL_SCALE = MODEL_RANGE / POSITION_MAX_VALUE;

	private static final float MODEL_SCALE_INV = POSITION_MAX_VALUE / MODEL_RANGE;

	private static final float TEXTURE_SCALE = (1.0f / TEXTURE_MAX_VALUE);

	public static int encodeOld(float u, float v) {
		return ((Math.round(u * TEXTURE_MAX_VALUE) & 0xFFFF) << 0) |
			((Math.round(v * TEXTURE_MAX_VALUE) & 0xFFFF) << 16);
	}

	@Override
	public GlVertexFormat getVertexFormat() {
		return VERTEX_FORMAT;
	}

	@Override
	public ChunkVertexEncoder getEncoder() {
		return new XHFPTerrainVertex();
	}
}
