package net.irisshaders.iris.vertices;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import net.irisshaders.iris.Iris;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IrisVertexFormats {
	public static final String ENTITY_ATTRIBUTE = "mc_Entity";
	public static final String ENTITY_ID_ATTRIBUTE = "iris_Entity";
	public static final String MID_TEXTURE_ATTRIBUTE = "mc_midTexCoord";
	public static final String TANGENT_ATTRIBUTE = "at_tangent";
	public static final String MID_BLOCK_ATTRIBUTE = "at_midBlock";
	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;
	private static final Map<VertexFormat, Map<String, Integer>> OFFSET_CACHE = new ConcurrentHashMap<>();

	static {
		//ENTITY_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, GpuFormat.RG16_SINT);
		//ENTITY_ID_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 3, GpuFormat.RGBA16_UINT);
		//MID_TEXTURE_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, GpuFormat.RG32_FLOAT);
		//TANGENT_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, GpuFormat.RGBA8_SNORM);
		//MID_BLOCK_ELEMENT = VertexFormatElement.register(getNextVertexFormatElementId(), 0, GpuFormat.RGB8_SINT);

		TERRAIN = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.addAttribute(ENTITY_ATTRIBUTE, GpuFormat.RG16_SINT)
			.addAttribute(MID_TEXTURE_ATTRIBUTE, GpuFormat.RG32_FLOAT)
			.addAttribute(TANGENT_ATTRIBUTE, GpuFormat.RGBA8_SNORM)
			.addAttribute(MID_BLOCK_ATTRIBUTE, GpuFormat.RGBA8_SNORM)
			.build();

		ENTITY = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV1", GpuFormat.RG16_SINT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.addAttribute(ENTITY_ID_ATTRIBUTE, GpuFormat.RGBA16_UINT)
			.addAttribute(MID_TEXTURE_ATTRIBUTE, GpuFormat.RG32_FLOAT)
			.addAttribute(TANGENT_ATTRIBUTE, GpuFormat.RGBA8_SNORM)
			.build();

		GLYPH = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.addAttribute(ENTITY_ID_ATTRIBUTE, GpuFormat.RGBA16_UINT)
			.addAttribute(MID_TEXTURE_ATTRIBUTE, GpuFormat.RG32_FLOAT)
			.addAttribute(TANGENT_ATTRIBUTE, GpuFormat.RGBA8_SNORM)
			.build();

		CLOUDS = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.build();
	}

	private static void debug(VertexFormat format) {
		Iris.logger.info("Vertex format: " + format + " with byte size " + format.getVertexSize());
		int byteIndex = 0;
		for (VertexFormatElement element : format.getElements()) {
			Iris.logger.info(element.name() + " @ " + byteIndex + " is " + element.format());
			byteIndex += element.format().blockSize();
		}
	}

	public static int getOffset(VertexFormat format, String attributeName) {
		Integer offset = OFFSET_CACHE.computeIfAbsent(format, IrisVertexFormats::createOffsetMap).get(attributeName);
		return offset != null ? offset : -1;
	}

	private static Map<String, Integer> createOffsetMap(VertexFormat format) {
		Map<String, Integer> offsets = new HashMap<>();
		for (VertexFormatElement element : format.getElements()) {
			offsets.put(element.name(), element.offset());
		}

		return Map.copyOf(offsets);
	}
}
