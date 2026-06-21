package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * A client-side fluid volume that should be treated by Iris as fluid-like
 * without requiring a real chunk {@code FluidState}.
 */
public interface VirtualFluidVolume {
	/**
	 * Stable identifier for debugging and shader/mod attribution.
	 */
	String id();

	VirtualFluidType type();

	VirtualFluidVisualProperties visualProperties();

	double minX();

	double minY();

	double minZ();

	double maxX();

	double maxY();

	double maxZ();

	/**
	 * Returns whether this world-space point is inside the virtual fluid.
	 */
	boolean contains(double worldX, double worldY, double worldZ);

	/**
	 * Returns the water/lava surface height at a world-space X/Z column.
	 */
	double surfaceHeightAt(double worldX, double worldZ);

	/**
	 * Returns the fluid depth at a world-space X/Z column in blocks.
	 */
	double depthAt(double worldX, double worldZ);

	/**
	 * Emits the visible surface quads for this virtual fluid volume.
	 *
	 * <p>The first prototype consumes only simple world-space quads. This keeps
	 * the public API independent from Minecraft renderer classes while still
	 * allowing Iris to draw the surface in its own terrain/translucent pipeline.</p>
	 */
	void forEachSurfaceQuad(VirtualFluidSurfaceConsumer consumer);
}
