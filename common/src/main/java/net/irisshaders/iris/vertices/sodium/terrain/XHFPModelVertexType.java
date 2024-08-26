package net.irisshaders.iris.vertices.sodium.terrain;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.DefaultChunkMeshAttributes;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Like HFPModelVertexType, but extended to support Iris. The extensions aren't particularly efficient right now.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	private final GlVertexFormat format;
	private final int normalOffset;
	private final int blockIdOffset;
	private final int tangentOffset;
	private final int midBlockOffset;
	private final int midUvOffset;

	public XHFPModelVertexType(GlVertexFormat format, int blockIdOffset, int normalOffset, int tangentOffset, int midUvOffset, int midBlockOffset) {
		this.format = format;
		this.blockIdOffset = blockIdOffset;
		this.normalOffset = normalOffset;
		this.tangentOffset = tangentOffset;
		this.midUvOffset = midUvOffset;
		this.midBlockOffset = midBlockOffset;
	}

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
		return format;
	}

	@Override
	public ChunkVertexEncoder getEncoder() {
		return new XHFPTerrainVertex(blockIdOffset, normalOffset, tangentOffset, midUvOffset, midBlockOffset, format.getStride());
	}
}
