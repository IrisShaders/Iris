package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.coderbot.iris.vertices.IrisVertexFormats;

public final class IrisParticleVertex {
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.instance()
		.get(IrisVertexFormats.PARTICLES);

	public static final int STRIDE = IrisVertexFormats.PARTICLES.getVertexSize();

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_TEXTURE = 12;
	private static final int OFFSET_COLOR = 20;
	private static final int OFFSET_LIGHT = 24;
	private static final int OFFSET_VELOCITY = 28;

	public static void put(long ptr,
						   float x, float y, float z, float prevX, float prevY, float prevZ, float u, float v, int color, int light) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		PositionAttribute.put(ptr + OFFSET_VELOCITY, x - prevX, y - prevY, z - prevZ);
	}
}
