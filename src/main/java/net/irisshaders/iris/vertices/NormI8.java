package net.irisshaders.iris.vertices;

import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Provides some utilities for working with packed normal vectors. Each normal component provides 8 bits of
 * precision in the range of [-1.0,1.0].
 * Copied from Sodium, licensed under the LGPLv3. Modified to support a W component.
 * <p>
 * | 32        | 24        | 16        | 8          |
 * | 0000 0000 | 0110 1100 | 0110 1100 | 0110 1100  |
 * | W         | X         | Y         | Z          |
 */
public class NormI8 {
	private static final int X_COMPONENT_OFFSET = 0;
	private static final int Y_COMPONENT_OFFSET = 8;
	private static final int Z_COMPONENT_OFFSET = 16;
	private static final int W_COMPONENT_OFFSET = 24;

	/**
	 * The maximum value of a normal's vector component.
	 */
	private static final float COMPONENT_RANGE = 127.0f;

	/**
	 * Constant value which can be multiplied with a floating-point vector component to get the normalized value. The
	 * multiplication is slightly faster than a floating point division, and this code is a hot path which justifies it.
	 */
	private static final float NORM = 1.0f / COMPONENT_RANGE;

	public static int pack(Vector3f normal) {
		return pack(normal.x(), normal.y(), normal.z(), 0);
	}

	/**
	 * Packs the specified vector components into a 32-bit integer in XYZ ordering with the 8 bits of padding at the
	 * end.
	 *
	 * @param x The x component of the normal's vector
	 * @param y The y component of the normal's vector
	 * @param z The z component of the normal's vector
	 */
	public static int pack(float x, float y, float z, float w) {
		return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16) | (((int) (w * 127) & 0xFF) << 24);
	}

	/**
	 * Packs the specified vector components into a 32-bit integer in XYZ ordering with the 8 bits of padding at the
	 * end.
	 *
	 * @param x The x component of the normal's vector
	 * @param y The y component of the normal's vector
	 * @param z The z component of the normal's vector
	 */
	public static int packColor(float x, float y, float z, float w) {
		return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16) | (((int) w & 0xFF) << 24);
	}

	/**
	 * Encodes a float in the range of -1.0..1.0 to a normalized unsigned integer in the range of 0..255 which can then
	 * be passed to graphics memory.
	 */
	private static int encode(float comp) {
		// TODO: is the clamp necessary here? our inputs should always be normalized vector components
		return ((int) (Mth.clamp(comp, -1.0F, 1.0F) * COMPONENT_RANGE) & 255);
	}

	/**
	 * Unpacks the x-component of the packed normal, denormalizing it to a float in the range of -1.0..1.0.
	 *
	 * @param norm The packed normal
	 */
	public static float unpackX(int norm) {
		return ((byte) ((norm >> X_COMPONENT_OFFSET) & 0xFF)) * NORM;
	}

	/**
	 * Unpacks the y-component of the packed normal, denormalizing it to a float in the range of -1.0..1.0.
	 *
	 * @param norm The packed normal
	 */
	public static float unpackY(int norm) {
		return ((byte) ((norm >> Y_COMPONENT_OFFSET) & 0xFF)) * NORM;
	}

	/**
	 * Unpacks the z-component of the packed normal, denormalizing it to a float in the range of -1.0..1.0.
	 *
	 * @param norm The packed normal
	 */
	public static float unpackZ(int norm) {
		return ((byte) ((norm >> Z_COMPONENT_OFFSET) & 0xFF)) * NORM;
	}

	/**
	 * Unpacks the w-component of the packed normal, denormalizing it to a float in the range of -1.0..1.0.
	 *
	 * @param norm The packed normal
	 */
	public static float unpackW(int norm) {
		return ((byte) ((norm >> W_COMPONENT_OFFSET) & 0xFF)) * NORM;
	}
}
