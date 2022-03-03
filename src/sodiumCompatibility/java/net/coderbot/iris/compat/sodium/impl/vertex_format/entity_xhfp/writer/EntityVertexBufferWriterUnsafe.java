package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer;

import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.NormalHelper;
import net.coderbot.iris.compat.sodium.impl.vertex_format.QuadViewEntity;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.vendored.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class EntityVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements EntityVertexSink {
	int vertexCount = 0;
	float uSum;
	float vSum;
	private QuadViewEntity currentQuad = new QuadViewEntity(IrisModelVertexFormats.ENTITIES.getVertexFormat().getVertexSize(), true);
	private Vector3f normal = new Vector3f();
	int STRIDE;

    public EntityVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
        super(backingBuffer, IrisModelVertexFormats.ENTITIES);
		STRIDE = IrisModelVertexFormats.ENTITIES.getVertexFormat().getVertexSize();
    }

    @Override
    public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		uSum += u;
		vSum += v;

		this.writeQuadInternal(x, y, z, color, u, v, light, overlay, normal);
	}

	public void writeQuadInternal(float x, float y, float z, int color, float u, float v, int light, int overlay, int unneededNormal) {
        long i = this.writePointer;

        MemoryUtil.memPutFloat(i, x);
        MemoryUtil.memPutFloat(i + 4, y);
        MemoryUtil.memPutFloat(i + 8, z);
        MemoryUtil.memPutInt(i + 12, color);
        MemoryUtil.memPutFloat(i + 16, u);
        MemoryUtil.memPutFloat(i + 20, v);
        MemoryUtil.memPutInt(i + 24, overlay);
        MemoryUtil.memPutInt(i + 28, light);
        MemoryUtil.memPutShort(i + 34, (short) -1);
        MemoryUtil.memPutShort(i + 36, (short) -1);
		MemoryUtil.memPutInt(i + 32, unneededNormal);

        this.advance();

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

			MemoryUtil.memPutInt(i + 38, midTexCoord);
			MemoryUtil.memPutInt(i + 38 - STRIDE, midTexCoord);
			MemoryUtil.memPutInt(i + 38 - STRIDE * 2, midTexCoord);
			MemoryUtil.memPutInt(i + 38 - STRIDE * 3, midTexCoord);

			vertexCount = 0;
			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			currentQuad.writePointer = this.writePointer;
			NormalHelper.computeFaceNormal(normal, currentQuad);
			int packedNormal = NormalHelper.packNormal(normal, 0.0f);

			//MemoryUtil.memPutInt(i + 32 - STRIDE, packedNormal);
			//MemoryUtil.memPutInt(i + 32 - STRIDE * 2, packedNormal);
			//MemoryUtil.memPutInt(i + 32 - STRIDE * 3, packedNormal);

			// Capture all of the relevant vertex positions
			float x0 = MemoryUtil.memGetFloat(i - STRIDE * 3);
			float y0 = MemoryUtil.memGetFloat(i + 4 - STRIDE * 3);
			float z0 = MemoryUtil.memGetFloat(i + 8 - STRIDE * 3);

			float x1 = MemoryUtil.memGetFloat(i - STRIDE * 2);
			float y1 = MemoryUtil.memGetFloat(i + 4 - STRIDE * 2);
			float z1 = MemoryUtil.memGetFloat(i + 8 - STRIDE * 2);

			float x2 = MemoryUtil.memGetFloat(i - STRIDE);
			float y2 = MemoryUtil.memGetFloat(i + 4 - STRIDE);
			float z2 = MemoryUtil.memGetFloat(i + 8 - STRIDE);

			float edge1x = x1 - x0;
			float edge1y = y1 - y0;
			float edge1z = z1 - z0;

			float edge2x = x2 - x0;
			float edge2y = y2 - y0;
			float edge2z = z2 - z0;

			float u0 = MemoryUtil.memGetFloat(i + 16 - STRIDE * 3);
			float v0 = MemoryUtil.memGetFloat(i + 20 - STRIDE * 3);

			float u1 = MemoryUtil.memGetFloat(i + 16 - STRIDE * 2);
			float v1 = MemoryUtil.memGetFloat(i + 20 - STRIDE * 2);

			float u2 = MemoryUtil.memGetFloat(i + 16 - STRIDE);
			float v2 = MemoryUtil.memGetFloat(i + 20 - STRIDE);

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

			MemoryUtil.memPutInt(i + 42, tangent);
			MemoryUtil.memPutInt(i + 42 - STRIDE, tangent);
			MemoryUtil.memPutInt(i + 42 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(i + 42 - STRIDE * 3, tangent);
		}
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
