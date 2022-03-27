package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import net.caffeinemc.sodium.interop.vanilla.vertex.VanillaVertexFormats;
import net.caffeinemc.sodium.interop.vanilla.vertex.formats.quad.QuadVertexSink;
import net.caffeinemc.sodium.render.vertex.buffer.VertexBufferView;
import net.caffeinemc.sodium.render.vertex.buffer.VertexBufferWriterNio;
import net.caffeinemc.sodium.util.packed.Normal3b;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.QuadViewEntity;
import net.coderbot.iris.vertices.IrisVertexFormats;

import java.nio.ByteBuffer;

public class EntityVertexBufferWriterNio extends VertexBufferWriterNio implements QuadVertexSink {
	private final QuadViewEntity.QuadViewEntityNio quad = new QuadViewEntity.QuadViewEntityNio();
	private static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();
	private float midU = 0;
	private float midV = 0;
	private int vertexCount;

	public EntityVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, VanillaVertexFormats.QUADS);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		int i = this.writeOffset;
		ByteBuffer buffer = this.byteBuffer;

		vertexCount++;
		midU += u;
		midV += v;

		buffer.putFloat(i, x);
		buffer.putFloat(i + 4, y);
		buffer.putFloat(i + 8, z);
		buffer.putInt(i + 12, color);
		buffer.putFloat(i + 16, u);
		buffer.putFloat(i + 20, v);
		buffer.putInt(i + 24, overlay);
		buffer.putInt(i + 28, light);
		buffer.putInt(i + 32, normal);
		buffer.putShort(i + 36, (short) -1);
		buffer.putShort(i + 38, (short) -1);


		this.advance();

		if (vertexCount == 4) {
			this.endQuad(vertexCount, Normal3b.unpackX(normal), Normal3b.unpackY(normal), Normal3b.unpackZ(normal));
		}
	}

	public void endQuad(int length, float normalX, float normalY, float normalZ) {
		this.vertexCount = 0;
		ByteBuffer buffer = this.byteBuffer;
		int i = this.writeOffset;

		quad.setup(buffer, i, STRIDE);

		int tangent = quad.computeTangent(normalX, normalY, normalZ);

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putInt(i - 4 - STRIDE * vertex, tangent);
		}

		midU *= 0.25;
		midV *= 0.25;

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putFloat(i - 12 - STRIDE * vertex, midU);
			buffer.putFloat(i - 8 - STRIDE * vertex, midV);
		}

		midU = 0;
		midV = 0;
		vertexCount = 0;
	}
}
