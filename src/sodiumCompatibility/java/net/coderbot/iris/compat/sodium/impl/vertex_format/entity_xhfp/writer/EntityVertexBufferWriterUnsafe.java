package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import net.caffeinemc.sodium.interop.vanilla.vertex.VanillaVertexFormats;
import net.caffeinemc.sodium.interop.vanilla.vertex.formats.quad.QuadVertexSink;
import net.caffeinemc.sodium.render.vertex.buffer.VertexBufferView;
import net.caffeinemc.sodium.render.vertex.buffer.VertexBufferWriterUnsafe;
import net.caffeinemc.sodium.util.packed.Normal3b;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.QuadViewEntity;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class EntityVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements QuadVertexSink {
	private final QuadViewEntity.QuadViewEntityUnsafe quad = new QuadViewEntity.QuadViewEntityUnsafe();
	private static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();
	float midU = 0;
	float midV = 0;
	private int vertexCount;

	public EntityVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
		super(backingBuffer, VanillaVertexFormats.QUADS);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		long i = this.writePointer;

		vertexCount++;
		midU += u;
		midV += v;

		MemoryUtil.memPutFloat(i, x);
		MemoryUtil.memPutFloat(i + 4, y);
		MemoryUtil.memPutFloat(i + 8, z);
		MemoryUtil.memPutInt(i + 12, color);
		MemoryUtil.memPutFloat(i + 16, u);
		MemoryUtil.memPutFloat(i + 20, v);
		MemoryUtil.memPutInt(i + 24, overlay);
		MemoryUtil.memPutInt(i + 28, light);
		MemoryUtil.memPutInt(i + 32, normal);
		MemoryUtil.memPutShort(i + 36, (short) -1);
		MemoryUtil.memPutShort(i + 38, (short) -1);

		this.advance();

		if (vertexCount == 4) {
			this.endQuad(vertexCount, Normal3b.unpackX(normal), Normal3b.unpackY(normal), Normal3b.unpackZ(normal));
		}
	}

	public void endQuad(int length, float normalX, float normalY, float normalZ) {
		long i = this.writePointer;

		quad.setup(writePointer, STRIDE);

		int tangent = quad.computeTangent(normalX, normalY, normalZ);

		for (long vertex = 0; vertex < length; vertex++) {
			MemoryUtil.memPutInt(i - 4 - STRIDE * vertex, tangent);
		}

		midU *= 0.25;
		midV *= 0.25;

		for (long vertex = 0; vertex < length; vertex++) {
			MemoryUtil.memPutFloat(i - 12 - STRIDE * vertex, midU);
			MemoryUtil.memPutFloat(i - 8 - STRIDE * vertex, midV);
		}

		midU = 0;
		midV = 0;
		vertexCount = 0;
	}
}
