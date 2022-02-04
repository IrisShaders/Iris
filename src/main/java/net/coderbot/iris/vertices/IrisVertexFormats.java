package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);

		ImmutableMap.Builder<String, VertexFormatElement> terrainElements = ImmutableMap.builder();
		ImmutableMap.Builder<String, VertexFormatElement> entityElements = ImmutableMap.builder();

		terrainElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
		terrainElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
		terrainElements.put("UV0", DefaultVertexFormat.ELEMENT_UV);
		terrainElements.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
		terrainElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
		terrainElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
		terrainElements.put("mc_Entity", ENTITY_ELEMENT);
		terrainElements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
		terrainElements.put("at_tangent", TANGENT_ELEMENT);

		entityElements.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
		entityElements.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
		entityElements.put("UV0", DefaultVertexFormat.ELEMENT_UV);
		entityElements.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
		entityElements.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
		entityElements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
		entityElements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
		entityElements.put("mc_Entity", ENTITY_ELEMENT);
		entityElements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
		entityElements.put("at_tangent", TANGENT_ELEMENT);

		TERRAIN = new VertexFormat(terrainElements.build());
		ENTITY = new VertexFormat(entityElements.build());
	}
}
