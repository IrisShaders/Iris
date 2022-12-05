package net.coderbot.iris.compat.sodium.impl.shadow_map;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.BitSet;

/**
 * chop² (ChopChop): A high-performance frustum culling algorithm / implementation for voxel grids
 *
 * ChopChop is a (seemingly) novel algorithm that exploits properties of voxel grids
 * to efficiently determine the set of voxels that are contained within a given
 * frustum.
 *
 * ChopChop gets its name from the fact that it identifies the set of visible voxels by iteratively
 * "chopping" off voxels not contained within the planes of a frustum. This analytical approach
 * is far more efficient than the previous brute-force approach hacked on to Sodium's existing
 * chunk culling that iterates through chunks one-at-a-time due to a requirement of the
 * Advanced Cave Culling algorithm, which we have to disable in the shadow pass anyways.
 *
 * @author coderbot16 Original work
 */
public class ChopChopFrustumCulling {
	/**
	 * Executes the ChopChop algorithm on a given render region.
	 *
	 * @param planes A list of frustum planes. The distance component (w) is measured
	 *               in units of cubic chunks, unlike in AdvancedShadowCullingFrustum
	 *               where it is measured in units of blocks. Make sure to convert
	 *               this properly by dividing w by 16 before passing to this function!
	 * @param originX The X origin of the render region, relative to the camera position.
	 *                Measured in units of cubic chunks. (ie, originX = 1.0 means that
	 *                the render region is 16 blocks away from the origin on the x axis)
	 * @param originY The Y origin of the render region, relative to the camera position.
	 *                Measured in units of cubic chunks.
	 * @param originZ The Z origin of the render region, relative to the camera position.
	 *                Measured in units of cubic chunks.
	 * @return A BitSet containing the list of visible chunks in this render region. Use
	 *         {@link #getChunkIndex(int, int, int)} to index the BitSet.
	 */
	public static BitSet chop2(Vector4f[] planes, float originX, float originY, float originZ) {
		BitSet result = new BitSet(RenderRegion.REGION_SIZE);
		for (int i = 0; i < RenderRegion.REGION_SIZE; i++) {
			result.set(i);
		}

		BitSet current = new BitSet(RenderRegion.REGION_SIZE);
		Vector4f abs = new Vector4f();

		for (Vector4f plane : planes) {
			current.clear();
			plane.absolute(abs);

			// We have a vector (a, b, c, -d) which is expressing the plane equation:
			// ax + by + cz - d = 0
			//
			// The x, y, and z components of our plane are the plane normal vector
			// We want to figure out which axis has the greatest absolute value on our normal vector
			// That determines the major axis of the plane, and therefore the axis we'll
			// iterate over last to chop the plane.
			float maxAxis = Math.max(Math.max(abs.x, abs.y), abs.z);

			if (abs.x == maxAxis) {
				// Major axis is the x axis.
				// iteration order tuned for cache locality (see getChunkIndex)
				for (int ry = 0; ry < RenderRegion.REGION_HEIGHT; ry++) {
					for (int rz = 0; rz < RenderRegion.REGION_LENGTH; rz++) {
						// Let's assume that given a y and z value of a point on the plane,
						// we'd like to solve for the x value. We can rearrange the equation
						// like so:
						// ax = -(by + cz - d)
						// x = -(by + cz - d) / a
						//
						// Using that, we can calculate the X boundary for each corner of the
						// voxel.
						float xmid00 = -(plane.y * (ry    ) + plane.z * (rz    ) + plane.w) / plane.x;
						float xmid10 = -(plane.y * (ry + 1) + plane.z * (rz    ) + plane.w) / plane.x;
						float xmid01 = -(plane.y * (ry    ) + plane.z * (rz + 1) + plane.w) / plane.x;
						float xmid11 = -(plane.y * (ry + 1) + plane.z * (rz + 1) + plane.w) / plane.x;

						// We've found the boundary, but which side of that boundary is inside the plane?
						// Remember, given this plane equation, that a point is on the inside side of
						// a plane if dot(plane, point) < 0.
						//
						// Let x' be an arbitrary x value such that (x', y, z) is inside the plane.
						// Clearly, ax' must be less than ax for the plane equation to be less than zero:
						// ax' < ax
						//
						// Let's assume that x' < x. It must be the case that a > 0 because if a < 0,
						// then ax' < ax would only hold if x' > x. Therefore, for a given value of a,
						// two possibilities exist:
						//
						// * a > 0: All values less than the boundary x are in the plane
						// * a < 0: All values greater than the boundary x are in the plane

						int xmin = 0;
						int xmax = RenderRegion.REGION_WIDTH;

						// Note: This rounding is sensitive, it's been tuned to make sure that our
						// integral value lies on either one side or the other
						if (plane.x > 0) {
							xmax = (int) Math.ceil(Math.max(Math.max(xmid00, xmid10), Math.max(xmid01, xmid11)));
							xmax = Math.min(xmax, RenderRegion.REGION_WIDTH);
						} else {
							xmin = (int) Math.floor(Math.min(Math.min(xmid00, xmid10), Math.min(xmid01, xmid11)));
							xmin = Math.max(xmin, 0);
						}

						if (xmax <= 0 || xmin >= RenderRegion.REGION_WIDTH) {
							// Plane is not inside the region.
							continue;
						}

						// Now that we've found our bounds, we can just fill in all the voxels along this
						// axis-aligned line that are inside the plane.

						for (int rx = xmin; rx < xmax; rx++) {
							current.set(getChunkIndex(rx, ry, rz));
						}
					}
				}
			} else if (abs.y == maxAxis) {
				// Same as previous case, but with Y being the major axis.
				// TODO
			} else if (abs.z == maxAxis) {
				// Same as previous case, but with Z being the major axis.
				// TODO
			} else {
				throw new AssertionError("sanity check failure: The result of " +
					"Math.max(Math.max(x, y), z) was not equal to one of x, y, or z, this is impossible!");
			}

			// The set of visible voxels is the logical intersection of the set of voxels that
			// have passed the previous visibility checks (initially, all voxels) with the set
			// of voxels that have passed the most recent visibility check.
			result.and(current);
		}

		return result;
	}

	/**
	 * @author jellysquid3, originally sourced from Sodium's RenderRegion.java
	 */
	public static int getChunkIndex(int x, int y, int z) {
		return (x * RenderRegion.REGION_LENGTH * RenderRegion.REGION_HEIGHT) + (y * RenderRegion.REGION_LENGTH) + z;
	}
}
