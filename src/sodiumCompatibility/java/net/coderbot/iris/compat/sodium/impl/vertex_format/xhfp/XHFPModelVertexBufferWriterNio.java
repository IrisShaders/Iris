package net.coderbot.iris.compat.sodium.impl.vertex_format.xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;

import net.coderbot.iris.compat.sodium.impl.block_id.MaterialIdAwareVertexWriter;
import net.coderbot.iris.block_rendering.MaterialIdHolder;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.vendored.joml.Vector3f;

import java.nio.ByteBuffer;

// TODO: Implement an Unsafe variant of this class.
public class XHFPModelVertexBufferWriterNio extends VertexBufferWriterNio implements ModelVertexSink, MaterialIdAwareVertexWriter {
	private MaterialIdHolder idHolder;

	public XHFPModelVertexBufferWriterNio(VertexBufferView backingBuffer) {
		super(backingBuffer, IrisModelVertexFormats.MODEL_VERTEX_XHFP);
	}

	private static final int STRIDE = XHFPModelVertexType.STRIDE;

	int vertexCount = 0;
	float uSum;
	float vSum;

	private QuadView currentQuad = new QuadView();
	private Vector3f normal = new Vector3f();

	@Override
	public void writeVertex(float posX, float posY, float posZ, int color, float u, float v, int light, int chunkId) {
		uSum += u;
		vSum += v;

		short materialId = idHolder.id;
		short renderType = idHolder.renderType;

		this.writeQuadInternal(posX, posY, posZ, color, u, v, light, materialId, renderType, chunkId);
	}

	private void writeQuadInternal(float posX, float posY, float posZ, int color,
								   float u, float v, int light, short materialId, short renderType, int chunkId) {
		int i = this.writeOffset;

		vertexCount++;
		// NB: uSum and vSum must already be incremented outside of this function.

		ByteBuffer buffer = this.byteBuffer;

		buffer.putShort(i + 0, XHFPModelVertexType.encodePosition(posX));
		buffer.putShort(i + 2, XHFPModelVertexType.encodePosition(posY));
		buffer.putShort(i + 4, XHFPModelVertexType.encodePosition(posZ));
		buffer.putShort(i + 6, (short) chunkId);

		buffer.putInt(i + 8, color);

		buffer.putShort(i + 12, XHFPModelVertexType.encodeBlockTexture(u));
		buffer.putShort(i + 14, XHFPModelVertexType.encodeBlockTexture(v));

		buffer.putInt(i + 16, XHFPModelVertexType.encodeLightMapTexCoord(light));

		// NB: We don't set midTexCoord, normal, and tangent here, they will be filled in later.
		// block ID
		buffer.putShort(i + 32, materialId);
		buffer.putShort(i + 34, renderType);
		buffer.putShort(i + 36, (short) 0);
		buffer.putShort(i + 38, (short) 0);

		if (vertexCount == 4) {
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
			int midU = (int)(65535.0F * Math.min(uSum * 0.25f, 1.0f)) & 0xFFFF;
			int midV = (int)(65535.0F * Math.min(vSum * 0.25f, 1.0f)) & 0xFFFF;
			int midTexCoord = (midV << 16) | midU;

			buffer.putInt(i + 20, midTexCoord);
			buffer.putInt(i + 20 - STRIDE, midTexCoord);
			buffer.putInt(i + 20 - STRIDE * 2, midTexCoord);
			buffer.putInt(i + 20 - STRIDE * 3, midTexCoord);

			vertexCount = 0;
			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			currentQuad.buffer = this.byteBuffer;
			currentQuad.writeOffset = this.writeOffset;
			NormalHelper.computeFaceNormal(normal, currentQuad);
			int packedNormal = NormalHelper.packNormal(normal, 0.0f);

			buffer.putInt(i + 28, packedNormal);
			buffer.putInt(i + 28 - STRIDE, packedNormal);
			buffer.putInt(i + 28 - STRIDE * 2, packedNormal);
			buffer.putInt(i + 28 - STRIDE * 3, packedNormal);

			// Capture all of the relevant vertex positions
			float x0 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 0 - STRIDE * 3));
			float y0 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 2 - STRIDE * 3));
			float z0 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 4 - STRIDE * 3));

			float x1 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 0 - STRIDE * 2));
			float y1 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 2 - STRIDE * 2));
			float z1 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 4 - STRIDE * 2));

			float x2 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 0 - STRIDE));
			float y2 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 2 - STRIDE));
			float z2 = XHFPModelVertexType.decodePosition(buffer.getShort(i + 4 - STRIDE));

			float edge1x = x1 - x0;
			float edge1y = y1 - y0;
			float edge1z = z1 - z0;

			float edge2x = x2 - x0;
			float edge2y = y2 - y0;
			float edge2z = z2 - z0;

			float u0 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 12 - STRIDE * 3));
			float v0 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 14 - STRIDE * 3));

			float u1 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 12 - STRIDE * 2));
			float v1 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 14 - STRIDE * 2));

			float u2 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 12 - STRIDE));
			float v2 = XHFPModelVertexType.decodeBlockTexture(buffer.getShort(i + 14 - STRIDE));

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

			float pbitangentx =   tangenty * normal.z() - tangentz * normal.y();
			float pbitangenty = -(tangentx * normal.z() - tangentz * normal.x());
			float pbitangentz =   tangentx * normal.x() - tangenty * normal.y();

			float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
			byte tangentW;

			if (dot < 0) {
				tangentW = -127;
			} else {
				tangentW = 127;
			}

			int tangent = Norm3b.pack(tangentx, tangenty, tangentz);
			tangent |= (tangentW << 24);

			buffer.putInt(i + 24, tangent);
			buffer.putInt(i + 24 - STRIDE, tangent);
			buffer.putInt(i + 24 - STRIDE * 2, tangent);
			buffer.putInt(i + 24 - STRIDE * 3, tangent);
		}

		this.advance();
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

	@Override
	public void iris$setIdHolder(MaterialIdHolder holder) {
		this.idHolder = holder;
	}
}
