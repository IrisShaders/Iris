package net.coderbot.iris.compat.sodium.impl.vertex_format.clouds_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.model.vertex.formats.screen_quad.BasicScreenQuadVertexSink;
import org.joml.Vector3f;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class CloudsVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements BasicScreenQuadVertexSink {
	private static final int STRIDE = IrisVertexFormats.CLOUDS.getVertexSize();

	private final QuadViewClouds.QuadViewCloudsUnsafe quad = new QuadViewClouds.QuadViewCloudsUnsafe();
	private final Vector3f saveNormal = new Vector3f();

	private int vertexCount;

	public CloudsVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
		super(backingBuffer, ExtendedBasicScreenQuadVertexType.INSTANCE);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color) {
		long i = this.writePointer;

		vertexCount++;

		MemoryUtil.memPutFloat(i, x);
		MemoryUtil.memPutFloat(i + 4L, y);
		MemoryUtil.memPutFloat(i + 8L, z);
		MemoryUtil.memPutInt(i + 12L, color);

		if (vertexCount == 4) {
			this.endQuad();
		}

		this.advance();
	}

	private void endQuad() {
		this.vertexCount = 0;

		long i = this.writePointer;

		quad.setup(writePointer, STRIDE);

		NormalHelper.computeFaceNormal(saveNormal, quad);
		int normal = NormalHelper.packNormal(saveNormal, 0.0F);

		for (long vertex = 0; vertex < 4; vertex++) {
			MemoryUtil.memPutInt(i + 16L - STRIDE * vertex, normal);
		}
	}
}
