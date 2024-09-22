package net.irisshaders.iris.vertices.sodium.terrain;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexFormatAttribute;

public class IrisChunkMeshAttributes {
	public static final VertexFormatAttribute MID_TEX_COORD = new VertexFormatAttribute("midTexCoord", GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, false);
	public static final VertexFormatAttribute TANGENT = new VertexFormatAttribute("TANGENT", GlVertexAttributeFormat.BYTE, 4, true, false);
	public static final VertexFormatAttribute NORMAL = new VertexFormatAttribute("NORMAL", GlVertexAttributeFormat.BYTE, 4, true, false);
	public static final VertexFormatAttribute BLOCK_ID = new VertexFormatAttribute("BLOCK_ID", GlVertexAttributeFormat.UNSIGNED_INT, 1, false, true);
	public static final VertexFormatAttribute MID_BLOCK = new VertexFormatAttribute("MID_BLOCK", GlVertexAttributeFormat.BYTE, 4, false, false);
}
