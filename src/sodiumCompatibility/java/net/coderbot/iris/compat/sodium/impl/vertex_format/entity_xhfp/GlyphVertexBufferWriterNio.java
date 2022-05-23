package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.glyph.GlyphVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.nio.ByteBuffer;

public class GlyphVertexBufferWriterNio extends VertexBufferWriterNio implements QuadVertexSink, GlyphVertexSink {
	private static final int STRIDE = IrisVertexFormats.TERRAIN.getVertexSize();

	private final QuadViewEntity.QuadViewEntityNio quad = new QuadViewEntity.QuadViewEntityNio();
	private final Vector3f saveNormal = new Vector3f();

	private int vertexCount;
	private float uSum;
	private float vSum;

	public GlyphVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, ExtendedGlyphVertexType.INSTANCE);
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		int i = this.writeOffset;
		ByteBuffer buffer = this.byteBuffer;

		vertexCount++;
		uSum += u;
		vSum += v;

		buffer.putFloat(i, x);
		buffer.putFloat(i + 4, y);
		buffer.putFloat(i + 8, z);
		buffer.putInt(i + 12, color);
		buffer.putFloat(i + 16, u);
		buffer.putFloat(i + 20, v);
		buffer.putInt(i + 24, light);

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

		int i = this.writeOffset;
		ByteBuffer buffer = this.byteBuffer;

		uSum *= 0.25;
		vSum *= 0.25;

		quad.setup(byteBuffer, writeOffset, STRIDE);

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

		for (int vertex = 0; vertex < 4; vertex++) {
			buffer.putFloat(i + 36 - STRIDE * vertex, uSum);
			buffer.putFloat(i + 40 - STRIDE * vertex, vSum);
			buffer.putInt(i + 28 - STRIDE * vertex, normal);
			buffer.putInt(i + 44 - STRIDE * vertex, tangent);
		}

		uSum = 0;
		vSum = 0;
	}
}
