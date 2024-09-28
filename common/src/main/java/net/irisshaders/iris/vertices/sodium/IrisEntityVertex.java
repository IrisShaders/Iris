package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.*;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public final class IrisEntityVertex {
	public static final VertexFormat FORMAT = IrisVertexFormats.ENTITY;

	public static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = IrisVertexFormats.ENTITY.getOffset(VertexFormatElement.COLOR);
	private static final int OFFSET_TEXTURE = IrisVertexFormats.ENTITY.getOffset(VertexFormatElement.UV0);
	private static final int OFFSET_OVERLAY = IrisVertexFormats.ENTITY.getOffset(VertexFormatElement.UV1);
	private static final int OFFSET_LIGHT = IrisVertexFormats.ENTITY.getOffset(VertexFormatElement.UV2);
	private static final int OFFSET_NORMAL = IrisVertexFormats.ENTITY.getOffset(VertexFormatElement.NORMAL);
	private static final int OFFSET_TANGENT = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
	private static final int OFFSET_MID_COORD = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int OFFSET_ENTITY_ID = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.ENTITY_ID_ELEMENT);
	private static final int OFFSET_VELOCITY = IrisVertexFormats.ENTITY.getOffset(IrisVertexFormats.VELOCITY_ELEMENT);

	public static void write(long ptr,
							 float x, float y, float z, float velocityX, float velocityY, float velocityZ, int color, float u, float v, int overlay, int light, int normal, int tangent,
							 float midU, float midV) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		OverlayAttribute.set(ptr + OFFSET_OVERLAY, overlay);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		NormalAttribute.set(ptr + OFFSET_NORMAL, normal);

		MemoryUtil.memPutInt(ptr + OFFSET_TANGENT, tangent);
		MemoryUtil.memPutFloat(ptr + OFFSET_MID_COORD, midU);
		MemoryUtil.memPutFloat(ptr + OFFSET_MID_COORD + 4, midV);
		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

		MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY, velocityX);
		MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY + 4, velocityY);
		MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY + 8, velocityZ);
	}
}
