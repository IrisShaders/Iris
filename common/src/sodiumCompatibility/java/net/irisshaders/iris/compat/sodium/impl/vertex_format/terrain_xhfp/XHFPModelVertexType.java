package net.irisshaders.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;

/**
 * Like HFPModelVertexType, but extended to support Iris. The extensions aren't particularly efficient right now.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	public static final int STRIDE = 40;

	public static final GlVertexFormat<ChunkMeshAttribute> VERTEX_FORMAT = GlVertexFormat.builder(ChunkMeshAttribute.class, STRIDE)
		.addElement(ChunkMeshAttribute.POSITION_HI, 0, GlVertexAttributeFormat.UNSIGNED_2_10_10_10_REV, 4, false, false)
		.addElement(ChunkMeshAttribute.POSITION_LO, 4, GlVertexAttributeFormat.UNSIGNED_2_10_10_10_REV, 4, false, false)
		.addElement(ChunkMeshAttribute.COLOR, 8, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, true, false)
		.addElement(ChunkMeshAttribute.TEXTURE, 12, GlVertexAttributeFormat.SHORT, 2, false, false)
		.addElement(ChunkMeshAttribute.LIGHT_MATERIAL_INDEX, 16, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, false, true)
		.addElement(IrisChunkMeshAttributes.MID_TEX_COORD, 20, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, false)
		.addElement(IrisChunkMeshAttributes.TANGENT, 24, GlVertexAttributeFormat.BYTE, 4, true, false)
		.addElement(IrisChunkMeshAttributes.NORMAL, 28, GlVertexAttributeFormat.BYTE, 3, true, false)
		.addElement(IrisChunkMeshAttributes.BLOCK_ID, 32, GlVertexAttributeFormat.SHORT, 2, false, false)
		.addElement(IrisChunkMeshAttributes.MID_BLOCK, 36, GlVertexAttributeFormat.BYTE, 4, false, false)
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

	static float decodeBlockTexture(short raw) {
		return (raw & 0xFFFF) * TEXTURE_SCALE;
	}

	static short encodePosition(float v) {
		return (short) ((MODEL_ORIGIN + v) * MODEL_SCALE_INV);
	}

	static float decodePosition(short raw) {
		return (raw & 0xFFFF) * MODEL_SCALE - MODEL_ORIGIN;
	}

	@Override
	public GlVertexFormat<ChunkMeshAttribute> getVertexFormat() {
		return VERTEX_FORMAT;
	}

	@Override
	public ChunkVertexEncoder getEncoder() {
		return new XHFPTerrainVertex();
	}
}
