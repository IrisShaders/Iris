package net.irisshaders.iris.compat.sodium.impl.vertex_format;

import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.irisshaders.iris.vertices.views.QuadView;

public class SodiumBufferBuilderPolygonView implements QuadView {
	private long ptr;
	private int attributeOffsetPosition;
	private int attributeOffsetTexture;
	private int stride;
	private int vertexAmount;

	public void setup(long ptr, int attributeOffsetPosition, int attributeOffsetTexture, int stride, int vertexAmount) {
		this.ptr = ptr;
		this.attributeOffsetPosition = attributeOffsetPosition;
		this.attributeOffsetTexture = attributeOffsetTexture;
		this.stride = stride;
		this.vertexAmount = vertexAmount;
	}

	@Override
	public float x(int index) {
		return PositionAttribute.getX(ptr + attributeOffsetPosition - (long) stride * (vertexAmount - index - 1));
	}

	@Override
	public float y(int index) {
		return PositionAttribute.getY(ptr + attributeOffsetPosition - (long) stride * (vertexAmount - index - 1));
	}

	@Override
	public float z(int index) {
		return PositionAttribute.getZ(ptr + attributeOffsetPosition - (long) stride * (vertexAmount - index - 1));
	}

	@Override
	public float u(int index) {
		return TextureAttribute.getU(ptr + attributeOffsetTexture - (long) stride * (vertexAmount - index - 1));
	}

	@Override
	public float v(int index) {
		return TextureAttribute.getV(ptr + attributeOffsetTexture - (long) stride * (vertexAmount - index - 1));
	}
}

