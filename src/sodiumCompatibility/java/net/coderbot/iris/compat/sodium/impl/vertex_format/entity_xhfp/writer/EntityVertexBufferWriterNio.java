package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.QuadViewEntity;

import java.nio.ByteBuffer;

public class EntityVertexBufferWriterNio extends VertexBufferWriterNio implements EntityVertexSink {
	private final QuadViewEntity quad = new QuadViewEntity();
	private final int STRIDE;

	public EntityVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, IrisModelVertexFormats.ENTITIES);
		STRIDE = IrisModelVertexFormats.ENTITIES.getVertexFormat().getVertexSize();
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		int i = this.writeOffset;

		ByteBuffer buffer = this.byteBuffer;
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
	}

	@Override
	public void endQuad(int length, float normalX, float normalY, float normalZ) {
		ByteBuffer buffer = this.byteBuffer;
		int i = this.writeOffset;

		quad.setup(buffer, i, STRIDE);

		int tangent = quad.computeTangent(normalX, normalY, normalZ);

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putInt(i - 4 - STRIDE * vertex, tangent);
		}

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < length; vertex++) {
			midU += quad.u(vertex);
			midV += quad.v(vertex);
		}

		midU *= 0.25;
		midV *= 0.25;

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putFloat(i - 12 - STRIDE * vertex, midU);
			buffer.putFloat(i - 8 - STRIDE * vertex, midV);
		}
	}
}
