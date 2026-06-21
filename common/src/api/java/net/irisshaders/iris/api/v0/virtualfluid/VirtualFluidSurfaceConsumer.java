package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * Receives world-space quads for a virtual fluid surface.
 */
@FunctionalInterface
public interface VirtualFluidSurfaceConsumer {
	void acceptQuad(
		double x0, double y0, double z0,
		double x1, double y1, double z1,
		double x2, double y2, double z2,
		double x3, double y3, double z3,
		float red, float green, float blue, float alpha
	);
}
