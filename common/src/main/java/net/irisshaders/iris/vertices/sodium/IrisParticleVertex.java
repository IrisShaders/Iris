package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.irisshaders.iris.vertices.IrisVertexFormats;

public final class IrisParticleVertex {
	public static final VertexFormat FORMAT = IrisVertexFormats.PARTICLE;

	public static final int STRIDE = IrisVertexFormats.PARTICLE.getVertexSize();

	private static final int OFFSET_POSITION = IrisVertexFormats.PARTICLE.getOffset(VertexFormatElement.POSITION);
	private static final int OFFSET_TEXTURE = IrisVertexFormats.PARTICLE.getOffset(VertexFormatElement.UV0);
	private static final int OFFSET_COLOR = IrisVertexFormats.PARTICLE.getOffset(VertexFormatElement.COLOR);
	private static final int OFFSET_LIGHT = IrisVertexFormats.PARTICLE.getOffset(VertexFormatElement.UV2);
	private static final int OFFSET_VELOCITY = IrisVertexFormats.PARTICLE.getOffset(IrisVertexFormats.VELOCITY_ELEMENT);

	public static void put(long ptr,
						   float x, float y, float z, float velX, float velY, float velZ, float u, float v, int color, int light) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		PositionAttribute.put(ptr + OFFSET_VELOCITY, velX, velY, velZ);
	}
}
