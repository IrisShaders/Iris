package net.irisshaders.iris.vertices;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.Iris;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement ENTITY_ID_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;

	static {
		int LAST_UV = 0;

		for (int i = 0; i < VertexFormatElement.MAX_COUNT; i++) {
			VertexFormatElement element = VertexFormatElement.byId(i);
			if (element != null && element.usage() == VertexFormatElement.Usage.UV) {
				LAST_UV = Math.max(LAST_UV, element.index());
			}
		}

		ENTITY_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 2);
		ENTITY_ID_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), LAST_UV + 1, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.UV, 3);
		MID_TEXTURE_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 4);
		MID_BLOCK_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 3);

		TERRAIN = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("UV2", VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.add("mc_Entity", ENTITY_ELEMENT)
			.add("mc_midTexCoord", MID_TEXTURE_ELEMENT)
			.add("at_tangent", TANGENT_ELEMENT)
			.add("at_midBlock", MID_BLOCK_ELEMENT)
			.padding(1)
			.build();

		ENTITY = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("UV1", VertexFormatElement.UV1)
			.add("UV2", VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.add("iris_Entity", ENTITY_ID_ELEMENT)
			.add("mc_midTexCoord", MID_TEXTURE_ELEMENT)
			.add("at_tangent", TANGENT_ELEMENT)
			.build();

		GLYPH = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("UV2", VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.add("iris_Entity", ENTITY_ID_ELEMENT)
			.add("mc_midTexCoord", MID_TEXTURE_ELEMENT)
			.add("at_tangent", TANGENT_ELEMENT)
			.padding(1)
			.build();

		CLOUDS = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.build();
	}

	private static void debug(VertexFormat format) {
		Iris.logger.info("Vertex format: " + format + " with byte size " + format.getVertexSize());
		int byteIndex = 0;
		for (VertexFormatElement element : format.getElements()) {
			Iris.logger.info(element + " @ " + byteIndex + " is " + element.type() + " " + element.usage());
			byteIndex += element.byteSize();
		}
	}

	private static int getNextVertexFormatElementId() {
		int id = 0;
		while (VertexFormatElement.byId(id) != null) {
			if (++id >= VertexFormatElement.MAX_COUNT) {
				throw new RuntimeException("Too many mods registering VertexFormatElements");
			}
		}
		return id;
	}
}
