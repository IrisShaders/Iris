package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 2);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 4);
		MID_BLOCK_ELEMENT = new VertexFormatElement(14, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 3);

		ImmutableList.Builder<VertexFormatElement> terrainElements = ImmutableList.builder();
		ImmutableList.Builder<VertexFormatElement> entityElements = ImmutableList.builder();

		terrainElements.add(DefaultVertexFormat.ELEMENT_POSITION); // 12
		terrainElements.add(DefaultVertexFormat.ELEMENT_COLOR); // 16
		terrainElements.add(DefaultVertexFormat.ELEMENT_UV0); // 24
		terrainElements.add(DefaultVertexFormat.ELEMENT_UV2); // 28
		terrainElements.add(DefaultVertexFormat.ELEMENT_NORMAL); // 31
		terrainElements.add(DefaultVertexFormat.ELEMENT_PADDING); // 32
		terrainElements.add(ENTITY_ELEMENT); // 36
		terrainElements.add(MID_TEXTURE_ELEMENT); // 44
		terrainElements.add(TANGENT_ELEMENT); // 48
		terrainElements.add(MID_BLOCK_ELEMENT); // 51
		terrainElements.add(DefaultVertexFormat.ELEMENT_PADDING); // 52

		entityElements.add(DefaultVertexFormat.ELEMENT_POSITION); // 12
		entityElements.add(DefaultVertexFormat.ELEMENT_COLOR); // 16
		entityElements.add(DefaultVertexFormat.ELEMENT_UV0); // 24
		entityElements.add(DefaultVertexFormat.ELEMENT_UV1); // 28
		entityElements.add(DefaultVertexFormat.ELEMENT_UV2); // 32
		entityElements.add(DefaultVertexFormat.ELEMENT_NORMAL); // 35
		entityElements.add(DefaultVertexFormat.ELEMENT_PADDING); // 36
		entityElements.add(MID_TEXTURE_ELEMENT); // 44
		entityElements.add(TANGENT_ELEMENT); // 48

		TERRAIN = new VertexFormat(terrainElements.build());
		ENTITY = new VertexFormat(entityElements.build());
	}
}
