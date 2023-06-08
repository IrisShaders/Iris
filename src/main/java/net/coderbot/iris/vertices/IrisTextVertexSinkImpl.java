package net.coderbot.iris.vertices;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import org.joml.Vector3f;
import net.irisshaders.iris.api.v0.IrisTextVertexSink;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public class IrisTextVertexSinkImpl implements IrisTextVertexSink {
	static VertexFormat format = IrisVertexFormats.ENTITY;
	private final ByteBuffer buffer;
	private final TextQuadView quad = new TextQuadView();
	private final Vector3f saveNormal = new Vector3f();
	private static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();
	private int vertexCount;
	private long elementOffset;
	private float uSum;
	private float vSum;

	public IrisTextVertexSinkImpl(int maxQuadCount, IntFunction<ByteBuffer> buffer) {
		this.buffer = buffer.apply(format.getVertexSize() * 4 * maxQuadCount);
		this.elementOffset = MemoryUtil.memAddress(this.buffer);
	}

	@Override
	public VertexFormat getUnderlyingVertexFormat() {
		return format;
	}

	@Override
	public ByteBuffer getUnderlyingByteBuffer() {
		return buffer;
	}
	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;
	private static final int OFFSET_TEXTURE = 16;
	private static final int OFFSET_MID_TEXTURE = 42;
	private static final int OFFSET_OVERLAY = 24;
	private static final int OFFSET_LIGHT = 28;
	private static final int OFFSET_NORMAL = 32;
	private static final int OFFSET_TANGENT = 50;
	@Override
	public void quad(float minX, float minY, float maxX, float maxY, float z, int color, float minU, float minV, float maxU, float maxV, int light) {
		vertex(minX, minY, z, color, minU, minV, light);
		vertex(minX, maxY, z, color, minU, maxV, light);
		vertex(maxX, maxY, z, color, maxU, maxV, light);
		vertex(maxX, minY, z, color, maxU, minV, light);
	}

	private void vertex(float x, float y, float z, int color, float u, float v, int light) {
		vertexCount++;
		uSum += u;
		vSum += v;

		long ptr = elementOffset;

		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 0, x);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);

		MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);

		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 0, u);
		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 4, v);

		MemoryUtil.memPutInt(ptr + OFFSET_LIGHT, light);

		MemoryUtil.memPutInt(ptr + OFFSET_OVERLAY, OverlayTexture.NO_OVERLAY);

		if (vertexCount == 4) {
			// TODO: compute this at the head of quad()
			vertexCount = 0;
			uSum *= 0.25;
			vSum *= 0.25;
			quad.setup(elementOffset, IrisVertexFormats.ENTITY.getVertexSize());

			NormalHelper.computeFaceNormal(saveNormal, quad);
			float normalX = saveNormal.x;
			float normalY = saveNormal.y;
			float normalZ = saveNormal.z;
			int normal = NormalHelper.packNormal(saveNormal, 0.0F);

			int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

			for (long vertex = 0; vertex < 4; vertex++) {
				MemoryUtil.memPutFloat(ptr + OFFSET_MID_TEXTURE - STRIDE * vertex, uSum);
				MemoryUtil.memPutFloat(ptr + (OFFSET_MID_TEXTURE + 4) - STRIDE * vertex, vSum);
				MemoryUtil.memPutInt(ptr + OFFSET_NORMAL - STRIDE * vertex, normal);
				MemoryUtil.memPutInt(ptr + OFFSET_TANGENT - STRIDE * vertex, tangent);
			}

			uSum = 0;
			vSum = 0;
		}

		buffer.position(buffer.position() + STRIDE);
		elementOffset += STRIDE;
	}

	static class TextQuadView implements QuadView {
		long writePointer;
		int stride;

		public TextQuadView() {

		}

		public void setup(long writePointer, int stride) {
			this.writePointer = writePointer;
			this.stride = stride;
		}

		public float x(int index) {
			return MemoryUtil.memGetFloat(writePointer - stride * (3L - index));
		}

		public float y(int index) {
			return MemoryUtil.memGetFloat(writePointer + 4 - stride * (3L - index));
		}

		public float z(int index) {
			return MemoryUtil.memGetFloat(writePointer + 8 - stride * (3L - index));
		}

		public float u(int index) {
			return MemoryUtil.memGetFloat(writePointer + 16 - stride * (3L - index));
		}

		public float v(int index) {
			return MemoryUtil.memGetFloat(writePointer + 20 - stride * (3L - index));
		}
	}
}
