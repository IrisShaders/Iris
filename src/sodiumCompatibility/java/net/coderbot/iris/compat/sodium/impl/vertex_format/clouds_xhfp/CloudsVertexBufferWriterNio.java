package net.coderbot.iris.compat.sodium.impl.vertex_format.clouds_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.screen_quad.BasicScreenQuadVertexSink;
import org.joml.Vector3f;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;

import java.nio.ByteBuffer;

public class CloudsVertexBufferWriterNio extends VertexBufferWriterNio implements BasicScreenQuadVertexSink {
	private static final int STRIDE = IrisVertexFormats.CLOUDS.getVertexSize();

	private final QuadViewClouds.QuadViewCloudsNio quad = new QuadViewClouds.QuadViewCloudsNio();
	private final Vector3f saveNormal = new Vector3f();

	private int vertexCount;

	public CloudsVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, ExtendedBasicScreenQuadVertexType.INSTANCE);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color) {
		ByteBuffer buffer = this.byteBuffer;
		int i = this.writeOffset;

		vertexCount++;

		buffer.putFloat(i, x);
		buffer.putFloat(i + 4, y);
		buffer.putFloat(i + 8, z);
		buffer.putInt(i + 12, color);

		if (vertexCount == 4) {
			this.endQuad();
		}

		this.advance();
	}

	private void endQuad() {
		this.vertexCount = 0;

		int i = this.writeOffset;

		quad.setup(byteBuffer, writeOffset, STRIDE);

		NormalHelper.computeFaceNormal(saveNormal, quad);
		int normal = NormalHelper.packNormal(saveNormal, 0.0F);

		for (int vertex = 0; vertex < 4; vertex++) {
			byteBuffer.putInt(i + 16 - STRIDE * vertex, normal);
		}
	}
}
