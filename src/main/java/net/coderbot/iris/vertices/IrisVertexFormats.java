package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;

	public static final VertexFormat TERRAIN;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(10, VertexFormatElement.DataType.FLOAT, VertexFormatElement.Type.GENERIC, 4);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(11, VertexFormatElement.DataType.FLOAT, VertexFormatElement.Type.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(12, VertexFormatElement.DataType.FLOAT, VertexFormatElement.Type.GENERIC, 4);

		ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();

		// TODO(21w10a): Audit this
		elements.put("Position", VertexFormats.POSITION_ELEMENT);
		elements.put("Color", VertexFormats.COLOR_ELEMENT);
		elements.put("UV0", VertexFormats.TEXTURE_ELEMENT);
		elements.put("UV2", VertexFormats.LIGHT_ELEMENT);
		elements.put("Normal", VertexFormats.NORMAL_ELEMENT);
		elements.put("Padding", VertexFormats.PADDING_ELEMENT);
		elements.put("mc_Entity", ENTITY_ELEMENT);
		elements.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
		elements.put("at_tangent", TANGENT_ELEMENT);

		TERRAIN = new VertexFormat(elements.build());
	}
}
