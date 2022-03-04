package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.QuadViewEntity;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.NormalHelper;
import net.coderbot.iris.vertices.QuadView;

import java.nio.ByteBuffer;

public class EntityVertexBufferWriterNio extends VertexBufferWriterNio implements EntityVertexSink {
	int vertexCount = 0;
	float uSum;
	float vSum;
	private QuadView quad = new QuadView();
	private Vector3f normal = new Vector3f();
	int STRIDE;

	public EntityVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, IrisModelVertexFormats.ENTITIES);
		STRIDE = IrisModelVertexFormats.ENTITIES.getVertexFormat().getVertexSize();
	}

	@Override
	public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		uSum += u;
		vSum += v;

		this.writeQuadInternal(x, y, z, color, u, v, light, overlay, normal);
	}

	@Override
	public void endQuad(int length) {
		ByteBuffer buffer = this.byteBuffer;
		int i = this.writeOffset;
		// TODO: Consider applying similar vertex coordinate transformations as the normal HFP texture coordinates

		// NB: Be careful with the math here! A previous bug was caused by midU going negative as a short, which
		// was sign-extended into midTexCoord, causing midV to have garbage (likely NaN data). If you're touching
		// this code, be aware of that, and don't introduce those kinds of bugs!
		//
		// Also note that OpenGL takes shorts in the range of [0, 65535] and transforms them linearly to [0.0, 1.0],
		// so multiply by 65535, not 65536.
		//
		// TODO: Does this introduce precision issues? Do we need to fall back to floats here? This might break
		// with high resolution texture packs.

		vertexCount = 0;
		uSum = 0;
		vSum = 0;

		// TODO: Keep this in sync with the extensions
		int extendedDataLength = (2 * 2) + (1 * 4) + (1 * 4);

		int nextElementByte = i;

		quad.setup(buffer, nextElementByte, STRIDE);


		computeTangents(buffer, i, length);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < length; vertex++) {
			midU += quad.u(vertex);
			midV += quad.v(vertex);
		}

		midU *= 0.25;
		midV *= 0.25;

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putFloat(nextElementByte - 12 - STRIDE * vertex, midU);
			buffer.putFloat(nextElementByte - 8 - STRIDE * vertex, midV);
		}
	}

	public void writeQuadInternal(float x, float y, float z, int color, float u, float v, int light, int overlay, int unneededNormal) {
		this.normal.set(Norm3b.unpackX(unneededNormal), Norm3b.unpackY(unneededNormal), Norm3b.unpackZ(unneededNormal));
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
		buffer.putInt(i + 32, unneededNormal);
		buffer.putShort(i + 36, (short) -1);
		buffer.putShort(i + 38, (short) -1);

		this.advance();
	}

	private void computeTangents(ByteBuffer buffer, int i, int length) {
		// Capture all of the relevant vertex positions
		float x0 = quad.x(0);
		float y0 = quad.y(0);
		float z0 = quad.z(0);

		float x1 = quad.x(1);
		float y1 = quad.y(1);
		float z1 = quad.z(1);

		float x2 = quad.x(2);
		float y2 = quad.y(2);
		float z2 = quad.z(2);

		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float u0 = quad.u(0);
		float v0 = quad.v(0);

		float u1 = quad.u(1);
		float v1 = quad.v(1);

		float u2 = quad.u(2);
		float v2 = quad.v(2);

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		float pbitangentx =   tangenty * normal.z - tangentz * normal.y;
		float pbitangenty = -(tangentx * normal.z - tangentz * normal.x);
		float pbitangentz =   tangentx * normal.x - tangenty * normal.y;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		int tangent = net.coderbot.iris.vertices.NormalHelper.packNormal(tangentx, tangenty, tangentz, tangentW);

		// TODO: Use packed tangents in the vertex format
		if (tangent == 0) {
			throw new RuntimeException("tf????");
		}

		for (int vertex = 0; vertex < length; vertex++) {
			buffer.putInt(i - 4 - STRIDE * vertex, tangent);
		}
/*
		for (int vertex = 0; vertex < 4; vertex++) {
			buffer.putFloat(this.nextElementByte - 16 - stride * vertex, tangentx);
			buffer.putFloat(this.nextElementByte - 12 - stride * vertex, tangenty);
			buffer.putFloat(this.nextElementByte - 8 - stride * vertex, tangentz);
			buffer.putFloat(this.nextElementByte - 4 - stride * vertex, 1.0F);
		}*/
	}

	// TODO: Verify that this works with the new changes to the CVF
	private static float normalizeVertexPositionShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 65535.0f);
	}

	// TODO: Verify that this is correct
	private static float normalizeVertexTextureShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 32768.0f);
	}

	private static float rsqrt(float value) {
		if (value == 0.0f) {
			// You heard it here first, folks: 1 divided by 0 equals 1
			// In actuality, this is a workaround for normalizing a zero length vector (leaving it as zero length)
			return 1.0f;
		} else {
			return (float) (1.0 / Math.sqrt(value));
		}
	}
}
