package net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.ParticleVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.block_rendering.ParticleIdMapper;
import net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.QuadViewParticle;
import net.coderbot.iris.vertices.IrisVertexFormats;

import java.nio.ByteBuffer;

public class IrisParticleVertexBufferWriterNio extends VertexBufferWriterNio implements ParticleVertexSink {
	private final QuadViewParticle.QuadViewParticleNio quad = new QuadViewParticle.QuadViewParticleNio();
	private static final int STRIDE = IrisVertexFormats.PARTICLE.getVertexSize();
	private float midU = 0;
	private float midV = 0;
	private int vertexCount;

	public IrisParticleVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, VanillaVertexTypes.PARTICLES);
	}

	@Override
	public void writeParticle(float x, float y, float z, float u, float v, int color, int light) {
		int i = this.writeOffset;

		ByteBuffer buffer = this.byteBuffer;
		buffer.putFloat(i, x);
		buffer.putFloat(i + 4, y);
		buffer.putFloat(i + 8, z);
		buffer.putFloat(i + 12, u);
		buffer.putFloat(i + 16, v);
		buffer.putInt(i + 20, color);
		buffer.putInt(i + 24, light);
		buffer.putShort(i + 28, (short) ParticleIdMapper.instance.currentParticle);
		buffer.putShort(i + 30, (short) ParticleIdMapper.instance.currentBlockParticle);


		this.advance();

		if (vertexCount == 4) {
			this.endQuad(vertexCount);
		}
	}

	public void endQuad(int length) {
		this.vertexCount = 0;
		ByteBuffer buffer = this.byteBuffer;
		int i = this.writeOffset;

		quad.setup(buffer, i, STRIDE);

		midU *= 0.25;
		midV *= 0.25;

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putFloat(i - 8 - STRIDE * vertex, midU);
			buffer.putFloat(i - 4 - STRIDE * vertex, midV);
		}

		midU = 0;
		midV = 0;
		vertexCount = 0;
	}
}
