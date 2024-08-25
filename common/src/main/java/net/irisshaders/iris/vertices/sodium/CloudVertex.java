package net.irisshaders.iris.vertices.sodium;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import org.joml.Matrix4f;

public final class CloudVertex {
	public static final int STRIDE = 20;

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;
	private static final int OFFSET_NORMAL = 16;

	public static void put(long ptr, Matrix4f matrix, float x, float y, float z, int color, int normal) {
		float xt = MatrixHelper.transformPositionX(matrix, x, y, z);
		float yt = MatrixHelper.transformPositionY(matrix, x, y, z);
		float zt = MatrixHelper.transformPositionZ(matrix, x, y, z);

		put(ptr, xt, yt, zt, color, normal);
	}

	public static void put(long ptr, float x, float y, float z, int color, int normal) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		NormalAttribute.set(ptr + OFFSET_NORMAL, normal);
	}
}
