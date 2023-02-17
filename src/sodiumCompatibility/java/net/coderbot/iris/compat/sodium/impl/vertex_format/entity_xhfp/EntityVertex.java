package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.render.immediate.RenderImmediate;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.OverlayAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
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
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.instance().get(IrisVertexFormats.ENTITY);
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
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int overlay, int light, int normal, int tangent) {
		PositionAttribute.put(ptr, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		OverlayAttribute.set(ptr + OFFSET_OVERLAY, overlay);
		NormalAttribute.set(ptr + OFFSET_NORMAL, normal);
		NormalAttribute.set(ptr + OFFSET_TANGENT, tangent);
		TextureAttribute.put(ptr + OFFSET_MID_TEXTURE, midU, midV);
	}

	public static void write2(long ptr,
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int light, int overlay, int normal) {
		PositionAttribute.put(ptr, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		OverlayAttribute.set(ptr + OFFSET_OVERLAY, overlay);
		NormalAttribute.set(ptr + OFFSET_NORMAL, normal);
		TextureAttribute.put(ptr + OFFSET_MID_TEXTURE, midU, midV);
	}

	public static void writeQuadVertices(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int light, int overlay, int color) {
		Matrix3f matNormal = matrices.normal();
		Matrix4f matPosition = matrices.pose();

		try (MemoryStack stack = RenderImmediate.VERTEX_DATA.push()) {
			long buffer = stack.nmalloc(4 * STRIDE);
			long ptr = buffer;

			// The packed normal vector
			var n = quad.getNormal();

			// The normal vector
			float nx = NormI8.unpackX(n);
			float ny = NormI8.unpackY(n);
			float nz = NormI8.unpackZ(n);

			float midU = ((quad.getTexU(0) + quad.getTexU(1) + quad.getTexU(2) + quad.getTexU(3)) * 0.25f);
			float midV = ((quad.getTexV(0) + quad.getTexV(1) + quad.getTexV(2) + quad.getTexV(3)) * 0.25f);

			// The transformed normal vector
			float nxt = MatrixHelper.transformNormalX(matNormal, nx, ny, nz);
			float nyt = MatrixHelper.transformNormalY(matNormal, nx, ny, nz);
			float nzt = MatrixHelper.transformNormalZ(matNormal, nx, ny, nz);

			// The packed transformed normal vector
			var nt = NormI8.pack(nxt, nyt, nzt);

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
			NormalAttribute.set(ptr + 44 - STRIDE * vertex, tangent);
		}
	}
}
