package net.irisshaders.iris.vertices;

public final class ExtendedDataHelper {
	// TODO: Resolve render types for normal blocks?
	public static final short BLOCK_RENDER_TYPE = -1;
	/**
	 * All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
	 */
	public static final short FLUID_RENDER_TYPE = 1;

	public static int packMidBlock(float x, float y, float z) {
		return ((int) (x * 64) & 0xFF) | (((int) (y * 64) & 0xFF) << 8) | (((int) (z * 64) & 0xFF) << 16);
	}

	public static int computeMidBlock(float x, float y, float z, int localPosX, int localPosY, int localPosZ) {
		return packMidBlock(
			localPosX + 0.5f - x,
			localPosY + 0.5f - y,
			localPosZ + 0.5f - z
		);
	}
}
