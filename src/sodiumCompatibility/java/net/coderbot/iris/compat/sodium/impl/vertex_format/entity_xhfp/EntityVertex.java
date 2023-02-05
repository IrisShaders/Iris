package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.RenderGlobal;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatRegistry;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.common.util.MatrixHelper;
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
	private static final int OFFSET_TANGENT = 44;

	private static Vector3f lastNormal = new Vector3f();

	public static void write(long ptr,
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int light, int overlay, int normal, int tangent) {
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 0, x);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);

		MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);

		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 0, u);
		MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 4, v);

		MemoryUtil.memPutInt(ptr + OFFSET_LIGHT, light);

		MemoryUtil.memPutInt(ptr + OFFSET_OVERLAY, overlay);

		MemoryUtil.memPutInt(ptr + OFFSET_NORMAL, normal);
		MemoryUtil.memPutInt(ptr + OFFSET_TANGENT, tangent);

		MemoryUtil.memPutFloat(ptr + OFFSET_MID_TEXTURE, midU);
		MemoryUtil.memPutFloat(ptr + OFFSET_MID_TEXTURE + 4, midV);
	}

	public static void write2(long ptr,
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int light, int overlay, int normal) {
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
			float nxt = MatrixHelper.transformNormalX(matNormal, nx, ny, nz);
			float nyt = MatrixHelper.transformNormalY(matNormal, nx, ny, nz);
			float nzt = MatrixHelper.transformNormalZ(matNormal, nx, ny, nz);

			// The packed transformed normal vector
			var nt = Norm3b.pack(nxt, nyt, nzt);

			for (int i = 0; i < 4; i++) {
				// The position vector
				float x = quad.getX(i);
				float y = quad.getY(i);
				float z = quad.getZ(i);

				// The transformed position vector
				float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
				float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
				float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

				write2(ptr, xt, yt, zt, color, quad.getTexU(i), quad.getTexV(i), midU, midV, light, overlay, nt);
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
}
