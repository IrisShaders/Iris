package net.irisshaders.iris.vertices.sodium.terrain;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;

/**
 * Like HFPModelVertexType, but extended to support Iris. The extensions aren't particularly efficient right now.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	private static final int POSITION_MAX_VALUE = 65536;
	private static final int TEXTURE_MAX_VALUE = 32768;
	private static final float MODEL_ORIGIN = 8.0f;
	private static final float MODEL_RANGE = 32.0f;
	private static final float MODEL_SCALE = MODEL_RANGE / POSITION_MAX_VALUE;
	private static final float MODEL_SCALE_INV = POSITION_MAX_VALUE / MODEL_RANGE;
	private static final float TEXTURE_SCALE = (1.0f / TEXTURE_MAX_VALUE);
	private final GlVertexFormat format;
	private final int normalOffset;
	private final int blockIdOffset;
	private final int midBlockOffset;
	private final int midUvOffset;

	public XHFPModelVertexType(GlVertexFormat format, int blockIdOffset, int normalOffset, int midUvOffset, int midBlockOffset) {
		this.format = format;
		this.blockIdOffset = blockIdOffset;
		this.normalOffset = normalOffset;
		this.midUvOffset = midUvOffset;
		this.midBlockOffset = midBlockOffset;
	}

	public static int encodeOld(float u, float v) {
		return ((Math.round(u * TEXTURE_MAX_VALUE) & 0xFFFF) << 0) |
			((Math.round(v * TEXTURE_MAX_VALUE) & 0xFFFF) << 16);
	}

	@Override
	public GlVertexFormat getVertexFormat() {
		return format;
	}

	@Override
	public ChunkVertexEncoder getEncoder() {
		return new XHFPTerrainVertex(blockIdOffset, normalOffset, midUvOffset, midBlockOffset, format.getStride());
	}
}
