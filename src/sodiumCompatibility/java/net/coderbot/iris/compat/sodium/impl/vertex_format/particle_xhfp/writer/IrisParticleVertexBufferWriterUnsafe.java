package net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.ParticleVertexSink;
import net.coderbot.iris.block_rendering.ParticleIdMapper;
import net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.QuadViewParticle;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class IrisParticleVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements ParticleVertexSink {
	private final QuadViewParticle.QuadViewParticleUnsafe quad = new QuadViewParticle.QuadViewParticleUnsafe();
	private static final int STRIDE = IrisVertexFormats.PARTICLE.getVertexSize();
	private float midU = 0;
	private float midV = 0;
	private int vertexCount;

	public IrisParticleVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
		super(backingBuffer, VanillaVertexTypes.PARTICLES);
	}

	@Override
	public void writeParticle(float x, float y, float z, float u, float v, int color, int light) {
		long i = this.writePointer;

		MemoryUtil.memPutFloat(i, x);
		MemoryUtil.memPutFloat(i + 4, y);
		MemoryUtil.memPutFloat(i + 8, z);
		MemoryUtil.memPutFloat(i + 12, u);
		MemoryUtil.memPutFloat(i + 16, v);
		MemoryUtil.memPutInt(i + 20, color);
		MemoryUtil.memPutInt(i + 24, light);
		MemoryUtil.memPutShort(i + 28, (short) ParticleIdMapper.getInstance().currentParticle);
		MemoryUtil.memPutShort(i + 30, (short) ParticleIdMapper.getInstance().currentBlockParticle);

		this.advance();

		if (vertexCount == 4) {
			this.endQuad(vertexCount);
		}
	}

	public void endQuad(int length) {
		this.vertexCount = 0;
		long i = this.writePointer;

		quad.setup(i, STRIDE);

		midU *= 0.25;
		midV *= 0.25;

		for (long vertex = 0; vertex < length; vertex++) {
			MemoryUtil.memPutFloat(i - 8 - STRIDE * vertex, midU);
			MemoryUtil.memPutFloat(i - 4 - STRIDE * vertex, midV);
		}

		midU = 0;
		midV = 0;
		vertexCount = 0;
	}
}
