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

	static {
		ENTITY_ELEMENT = new VertexFormatElement(10, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);

		ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();

		elements.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
		elements.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
		elements.put("UV0", DefaultVertexFormat.ELEMENT_UV);
		elements.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
		elements.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
		elements.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
		elements.put("mc_Entity", ENTITY_ELEMENT);
		elements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
		elements.put("at_tangent", TANGENT_ELEMENT);

		TERRAIN = new VertexFormat(elements.build());
	}
}
