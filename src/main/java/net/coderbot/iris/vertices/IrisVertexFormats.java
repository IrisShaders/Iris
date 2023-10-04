package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.coderbot.iris.Iris;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement ENTITY_ID_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;
	public static final VertexFormatElement PADDING_SHORT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 2);
		ENTITY_ID_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.UV, 3);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 4);
		MID_BLOCK_ELEMENT = new VertexFormatElement(14, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 3);
		PADDING_SHORT = new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 1);

		ImmutableMap.Builder<String, VertexFormatElement> terrainElements = ImmutableMap.builder();
		ImmutableMap.Builder<String, VertexFormatElement> entityElements = ImmutableMap.builder();
		ImmutableMap.Builder<String, VertexFormatElement> glyphElements = ImmutableMap.builder();
		ImmutableMap.Builder<String, VertexFormatElement> cloudsElements = ImmutableMap.builder();

		terrainElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION); // 12
		terrainElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR); // 16
		terrainElements.put("UV0", DefaultVertexFormat.ELEMENT_UV0); // 24
		terrainElements.put("UV2", DefaultVertexFormat.ELEMENT_UV2); // 28
		terrainElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL); // 31
		terrainElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING); // 32
		terrainElements.put("mc_Entity", ENTITY_ELEMENT); // 36
		terrainElements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT); // 44
		terrainElements.put("at_tangent", TANGENT_ELEMENT); // 48
		terrainElements.put("at_midBlock", MID_BLOCK_ELEMENT); // 51
		terrainElements.put("Padding2", DefaultVertexFormat.ELEMENT_PADDING); // 52

		entityElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION); // 12
		entityElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR); // 16
		entityElements.put("UV0", DefaultVertexFormat.ELEMENT_UV0); // 24
		entityElements.put("UV1", DefaultVertexFormat.ELEMENT_UV1); // 28
		entityElements.put("UV2", DefaultVertexFormat.ELEMENT_UV2); // 32
		entityElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL); // 35
		entityElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING); // 36
		entityElements.put("iris_Entity", ENTITY_ID_ELEMENT); // 40
		entityElements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT); // 48
		entityElements.put("at_tangent", TANGENT_ELEMENT); // 52
		entityElements.put("Padding2", PADDING_SHORT); // 52

		glyphElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION); // 12
		glyphElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR); // 16
		glyphElements.put("UV0", DefaultVertexFormat.ELEMENT_UV0); // 24
		glyphElements.put("UV2", DefaultVertexFormat.ELEMENT_UV2); // 28
		glyphElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL); // 31
		glyphElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING); // 32
		glyphElements.put("iris_Entity", ENTITY_ID_ELEMENT); // 38
		glyphElements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT); // 46
		glyphElements.put("at_tangent", TANGENT_ELEMENT); // 50
		glyphElements.put("Padding2", PADDING_SHORT); // 52

		cloudsElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION); // 12
		cloudsElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR); // 16
		cloudsElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL); // 31
		cloudsElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING); // 32

		TERRAIN = new VertexFormat(terrainElements.build());
		ENTITY = new VertexFormat(entityElements.build());
		GLYPH = new VertexFormat(glyphElements.build());
		CLOUDS = new VertexFormat(cloudsElements.build());
	}

	private static void debug(VertexFormat format) {
		Iris.logger.info("Vertex format: " + format + " with byte size " + format.getVertexSize());
		int byteIndex = 0;
		for (VertexFormatElement element : format.getElements()) {
			Iris.logger.info(element + " @ " + byteIndex + " is " + element.getType() + " " + element.getUsage());
			byteIndex += element.getByteSize();
		}
	}
}
