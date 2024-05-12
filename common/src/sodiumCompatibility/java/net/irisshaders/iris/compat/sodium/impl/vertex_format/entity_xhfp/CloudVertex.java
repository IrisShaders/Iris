package net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public final class CloudVertex {
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.instance().get(IrisVertexFormats.CLOUDS);

	public static final int STRIDE = IrisVertexFormats.CLOUDS.getVertexSize();

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;

	private static final QuadViewClouds.QuadViewCloudsUnsafe quad = new QuadViewClouds.QuadViewCloudsUnsafe();
	private static final Vector3f saveNormal = new Vector3f();

	private static int vertexCount;

	public static void write(long ptr, Matrix4f matrix, float x, float y, float z, int color) {
		vertexCount++;
		float x2 = MatrixHelper.transformPositionX(matrix, x, y, z);
		float y2 = MatrixHelper.transformPositionY(matrix, x, y, z);
		float z2 = MatrixHelper.transformPositionZ(matrix, x, y, z);

		write(ptr, x2, y2, z2, color);

		if (vertexCount == 4) {
			vertexCount = 0;
			quad.setup(ptr, STRIDE);

			NormalHelper.computeFaceNormal(saveNormal, quad);
			int normal = NormI8.pack(saveNormal);

			for (long vertex = 0; vertex < 4; vertex++) {
				MemoryUtil.memPutInt(ptr + 16L - STRIDE * vertex, normal);
			}
		}
	}

	public static void write(long ptr, float x, float y, float z, int color) {
		vertexCount++;
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION, x);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);

		MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);

		if (vertexCount == 4) {
			vertexCount = 0;
			quad.setup(ptr, STRIDE);

			NormalHelper.computeFaceNormal(saveNormal, quad);
			int normal = NormI8.pack(saveNormal);

			for (long vertex = 0; vertex < 4; vertex++) {
				MemoryUtil.memPutInt(ptr + 16L - STRIDE * vertex, normal);
			}
		}
	}

}
