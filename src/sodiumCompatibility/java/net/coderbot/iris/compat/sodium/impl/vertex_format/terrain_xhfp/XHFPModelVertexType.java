package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorU8;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisGlVertexAttributeFormat;

/**
 * Like HFPModelVertexType, but extended to support Iris. The extensions aren't particularly efficient right now.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	public static final int STRIDE = 36;

	public static final GlVertexFormat<ChunkMeshAttribute> VERTEX_FORMAT = GlVertexFormat.builder(ChunkMeshAttribute.class, STRIDE)
		.addElement(ChunkMeshAttribute.VERTEX_DATA, 0, GlVertexAttributeFormat.UNSIGNED_INT, 4, false, true)
		.addElement(IrisChunkMeshAttributes.MID_TEX_COORD, 16, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, false)
		.addElement(IrisChunkMeshAttributes.TANGENT, 20, IrisGlVertexAttributeFormat.BYTE, 4, true, false)
		.addElement(IrisChunkMeshAttributes.NORMAL, 24, IrisGlVertexAttributeFormat.BYTE, 3, true, false)
		.addElement(IrisChunkMeshAttributes.BLOCK_ID, 28, IrisGlVertexAttributeFormat.SHORT, 2, false, false)
		.addElement(IrisChunkMeshAttributes.MID_BLOCK, 32, IrisGlVertexAttributeFormat.BYTE, 4, false, false)
		.build();

	private static final int POSITION_MAX_VALUE = 65536;
	private static final int TEXTURE_MAX_VALUE = 65536;

	private static final float MODEL_ORIGIN = 8.0f;
	private static final float MODEL_RANGE = 32.0f;
	private static final float MODEL_SCALE = 32.0f;

	private static final float MODEL_SCALE_INV = POSITION_MAX_VALUE / MODEL_RANGE;

	private static final float TEXTURE_SCALE = (1.0f / TEXTURE_MAX_VALUE);

	@Override
	public GlVertexFormat<ChunkMeshAttribute> getVertexFormat() {
		return VERTEX_FORMAT;
	}

	@Override
	public ChunkVertexEncoder getEncoder() {
		return new XHFPTerrainVertex();
	}

	public static int encodePosition(float value) {
		return (int) ((MODEL_ORIGIN + value) * (POSITION_MAX_VALUE / MODEL_SCALE));
	}

	protected static int encodeDrawParameters(Material material, int sectionIndex) {
		return (((sectionIndex & 0xFF) << 8) | ((material.bits() & 0xFF) << 0));
	}

	protected static int encodeColor(int color) {
		var brightness = BlockRenderingSettings.INSTANCE.shouldUseSeparateAo() ? 1 : ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(color));

		int r = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackRed(color)) * brightness);
		int g = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackGreen(color)) * brightness);
		int b = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackBlue(color)) * brightness);

		return ColorABGR.pack(r, g, b, 0x00);
	}

	protected static int encodeLight(int light) {
		int block = (light >> 4) & 0xF;
		int sky = (light >> 20) & 0xF;

		return ((block << 0) | (sky << 4));
	}

	static short encodeTexture(float value) {
		return (short) (Math.min(0.99999997F, value) * TEXTURE_MAX_VALUE);
	}

	static float decodeBlockTexture(short raw) {
		return (raw & 0xFFFF) * TEXTURE_SCALE;
	}
}
