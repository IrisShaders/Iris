package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.QuadViewEntity;
import net.coderbot.iris.vertices.IrisVertexFormats;

import java.nio.ByteBuffer;

public class EntityVertexBufferWriterNio extends VertexBufferWriterNio implements EntityVertexSink {
	private final QuadViewEntity quad = new QuadViewEntity();
	private static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();
	private float midU = 0;
	private float midV = 0;
	private int vertexCount = 0;

	public EntityVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, VanillaVertexTypes.QUADS);
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

		if (vertexCount == 4) {
			int tangent = quad.computeTangent(Norm3b.unpackX(normal), Norm3b.unpackY(normal), Norm3b.unpackZ(normal));

			quad.setup(buffer, i, STRIDE);

			for (int vertex = 0; vertex < 4; vertex++) {
				buffer.putInt(i - 4 - STRIDE * vertex, tangent);
			}

			for (int vertex = 0; vertex < 4; vertex++) {
				buffer.putFloat(i - 12 - STRIDE * vertex, midU / 4F);
				buffer.putFloat(i - 8 - STRIDE * vertex, midV / 4F);
			}

			midU = 0;
			midV = 0;
			vertexCount = 0;
		}

		this.advance();
	}
}
