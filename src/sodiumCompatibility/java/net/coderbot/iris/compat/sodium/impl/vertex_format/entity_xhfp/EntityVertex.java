package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.RenderGlobal;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatRegistry;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.coderbot.iris.vertices.QuadView;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class EntityVertex {
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.get(IrisVertexFormats.ENTITY);
	public static final int STRIDE = IrisVertexFormats.ENTITY.getVertexSize();

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;
	private static final int OFFSET_TEXTURE = 16;
	private static final int OFFSET_MID_TEXTURE = 36;
	private static final int OFFSET_OVERLAY = 24;
	private static final int OFFSET_LIGHT = 28;
	private static final int OFFSET_NORMAL = 32;

	private static Vector3f lastNormal = new Vector3f();

	private static int vertexCount;

	public static void write(long ptr,
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int light, int overlay, int normal) {
		vertexCount++;
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 0, x);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);

		MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);

		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 0, u);
		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 4, v);

		MemoryUtil.memPutInt(ptr + OFFSET_LIGHT, light);

		MemoryUtil.memPutInt(ptr + OFFSET_OVERLAY, overlay);

		MemoryUtil.memPutInt(ptr + OFFSET_NORMAL, normal);

		MemoryUtil.memPutFloat(ptr + OFFSET_MID_TEXTURE, midU);
		MemoryUtil.memPutFloat(ptr + OFFSET_MID_TEXTURE + 4, midV);

		if (vertexCount == 4) {
			vertexCount = 0;
			endQuad(ptr, Norm3b.unpackX(normal), Norm3b.unpackY(normal), Norm3b.unpackZ(normal));
		}
	}

	public static void writeQuadVertices(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int light, int overlay, int color) {
		Matrix3f matNormal = matrices.normal();
		Matrix4f matPosition = matrices.pose();

		try (MemoryStack stack = RenderGlobal.VERTEX_DATA.push()) {
			long buffer = stack.nmalloc(4 * STRIDE);
			long ptr = buffer;

			// The packed normal vector
			var n = quad.getNormal();

			// The normal vector
			float nx = Norm3b.unpackX(n);
			float ny = Norm3b.unpackY(n);
			float nz = Norm3b.unpackZ(n);

			float midU = ((quad.getTexU(0) + quad.getTexU(1) + quad.getTexU(2) + quad.getTexU(3)) * 0.25f);
			float midV = ((quad.getTexV(0) + quad.getTexV(1) + quad.getTexV(2) + quad.getTexV(3)) * 0.25f);

			// The transformed normal vector
			float nxt = (matNormal.m00() * nx) + (matNormal.m10() * ny) + (matNormal.m20() * nz);
			float nyt = (matNormal.m01() * nx) + (matNormal.m11() * ny) + (matNormal.m21() * nz);
			float nzt = (matNormal.m02() * nx) + (matNormal.m12() * ny) + (matNormal.m22() * nz);

			// The packed transformed normal vector
			var nt = Norm3b.pack(nxt, nyt, nzt);

			for (int i = 0; i < 4; i++) {
				// The position vector
				float x = quad.getX(i);
				float y = quad.getY(i);
				float z = quad.getZ(i);

				// The transformed position vector
				float xt = (matPosition.m00() * x) + (matPosition.m10() * y) + (matPosition.m20() * z) + matPosition.m30();
				float yt = (matPosition.m01() * x) + (matPosition.m11() * y) + (matPosition.m21() * z) + matPosition.m31();
				float zt = (matPosition.m02() * x) + (matPosition.m12() * y) + (matPosition.m22() * z) + matPosition.m32();

				write(ptr, xt, yt, zt, color, quad.getTexU(i), quad.getTexV(i), midU, midV, light, overlay, nt);
				ptr += STRIDE;
			}

			endQuad(ptr - STRIDE, nxt, nyt, nzt);

			writer.push(stack, buffer, 4, FORMAT);
		}
	}
	private static QuadViewEntity.QuadViewEntityUnsafe quadView = new QuadViewEntity.QuadViewEntityUnsafe();

	private static void endQuad(long ptr, float normalX, float normalY, float normalZ) {
		quadView.setup(ptr, STRIDE);

		int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quadView);

		for (long vertex = 0; vertex < 4; vertex++) {
			MemoryUtil.memPutInt(ptr + 44 - STRIDE * vertex, tangent);
		}
	}

	public static void computeFaceNormal(Vector3f saveTo, ModelQuadView q) {
//		final Direction nominalFace = q.nominalFace();
//
//		if (GeometryHelper.isQuadParallelToFace(nominalFace, q)) {
//			Vec3i vec = nominalFace.getVector();
//			saveTo.set(vec.getX(), vec.getY(), vec.getZ());
//			return;
//		}

		final float x0 = q.getX(0);
		final float y0 = q.getY(0);
		final float z0 = q.getZ(0);
		final float x1 = q.getX(1);
		final float y1 = q.getY(1);
		final float z1 = q.getZ(1);
		final float x2 = q.getX(2);
		final float y2 = q.getY(2);
		final float z2 = q.getZ(2);
		final float x3 = q.getX(3);
		final float y3 = q.getY(3);
		final float z3 = q.getZ(3);

		final float dx0 = x2 - x0;
		final float dy0 = y2 - y0;
		final float dz0 = z2 - z0;
		final float dx1 = x3 - x1;
		final float dy1 = y3 - y1;
		final float dz1 = z3 - z1;

		float normX = dy0 * dz1 - dz0 * dy1;
		float normY = dz0 * dx1 - dx0 * dz1;
		float normZ = dx0 * dy1 - dy0 * dx1;

		float l = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);

		if (l != 0) {
			normX /= l;
			normY /= l;
			normZ /= l;
		}

		saveTo.set(normX, normY, normZ);
	}
}
