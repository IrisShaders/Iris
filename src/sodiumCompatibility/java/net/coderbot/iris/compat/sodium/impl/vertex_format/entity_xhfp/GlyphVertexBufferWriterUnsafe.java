package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.model.vertex.formats.glyph.GlyphVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.system.MemoryUtil;

public class GlyphVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements QuadVertexSink, GlyphVertexSink {
	private static final int STRIDE = IrisVertexFormats.TERRAIN.getVertexSize();

	private final QuadViewEntity.QuadViewEntityUnsafe quad = new QuadViewEntity.QuadViewEntityUnsafe();
	private final Vector3f saveNormal = new Vector3f();

	private int vertexCount;
	private float uSum;
	private float vSum;

	public GlyphVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
		super(backingBuffer, ExtendedGlyphVertexType.INSTANCE);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		long i = this.writePointer;

		vertexCount++;
		uSum += u;
		vSum += v;

		MemoryUtil.memPutFloat(i, x);
		MemoryUtil.memPutFloat(i + 4, y);
		MemoryUtil.memPutFloat(i + 8, z);
		MemoryUtil.memPutInt(i + 12, color);
		MemoryUtil.memPutFloat(i + 16, u);
		MemoryUtil.memPutFloat(i + 20, v);
		MemoryUtil.memPutInt(i + 24, light);

		if (vertexCount == 4) {
			this.endQuad(normal);
		}

		this.advance();
	}

	@Override
	public void writeGlyph(float x, float y, float z, int color, float u, float v, int light) {
		writeQuad(x, y, z, color, u, v, light, OverlayTexture.NO_OVERLAY, 0);
	}

	private void endQuad(int normal) {
		this.vertexCount = 0;

		long i = this.writePointer;

		uSum *= 0.25;
		vSum *= 0.25;

		quad.setup(writePointer, STRIDE);

		float normalX, normalY, normalZ;

		if (normal == 0) {
			NormalHelper.computeFaceNormal(saveNormal, quad);
			normalX = saveNormal.x;
			normalY = saveNormal.y;
			normalZ = saveNormal.z;
			normal = NormalHelper.packNormal(saveNormal, 0.0F);
		} else {
			normalX = Norm3b.unpackX(normal);
			normalY = Norm3b.unpackY(normal);
			normalZ = Norm3b.unpackZ(normal);
		}

		int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

		for (long vertex = 0; vertex < 4; vertex++) {
			MemoryUtil.memPutFloat(i + 36 - STRIDE * vertex, uSum);
			MemoryUtil.memPutFloat(i + 40 - STRIDE * vertex, vSum);
			MemoryUtil.memPutInt(i + 28 - STRIDE * vertex, normal);
			MemoryUtil.memPutInt(i + 44 - STRIDE * vertex, tangent);
		}

		uSum = 0;
		vSum = 0;
	}
}
