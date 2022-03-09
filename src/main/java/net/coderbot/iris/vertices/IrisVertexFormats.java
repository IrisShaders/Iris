package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableList;
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
		ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 2);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 4);

		ImmutableList.Builder<VertexFormatElement> terrainElements = ImmutableList.builder();
		ImmutableList.Builder<VertexFormatElement> entityElements = ImmutableList.builder();

		terrainElements.add(DefaultVertexFormat.ELEMENT_POSITION);
		terrainElements.add(DefaultVertexFormat.ELEMENT_COLOR);
		terrainElements.add(DefaultVertexFormat.ELEMENT_UV0);
		terrainElements.add(DefaultVertexFormat.ELEMENT_UV2);
		terrainElements.add(DefaultVertexFormat.ELEMENT_NORMAL);
		terrainElements.add(DefaultVertexFormat.ELEMENT_PADDING);
		terrainElements.add(ENTITY_ELEMENT);
		terrainElements.add(MID_TEXTURE_ELEMENT);
		terrainElements.add(TANGENT_ELEMENT);

		entityElements.add(DefaultVertexFormat.ELEMENT_POSITION);
		entityElements.add(DefaultVertexFormat.ELEMENT_COLOR);
		entityElements.add(DefaultVertexFormat.ELEMENT_UV0);
		entityElements.add(DefaultVertexFormat.ELEMENT_UV1);
		entityElements.add(DefaultVertexFormat.ELEMENT_UV2);
		entityElements.add(DefaultVertexFormat.ELEMENT_NORMAL);
		entityElements.add(DefaultVertexFormat.ELEMENT_PADDING);
		entityElements.add(ENTITY_ELEMENT);
		entityElements.add(MID_TEXTURE_ELEMENT);
		entityElements.add(TANGENT_ELEMENT);

		TERRAIN = new VertexFormat(terrainElements.build());
		ENTITY = new VertexFormat(entityElements.build());
	}
}
