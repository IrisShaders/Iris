package net.coderbot.iris.compat.sodium.impl.shadow_map;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.core.SectionPos;

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
 * One way to look at ChopChop is as a rasterization/voxelization of the frustum as a way of solving
 * the frustum culling problem. But, it is potentially different from typical rasterizers and voxelizers
 * in that it seeks to compute the inequality {@code <=} of each voxel compared to the volume rather
 * than the approximate equality ({@code =}) of voxels that lie on the planes of the frustum.
 *
 * @author coderbot16 Original work
 */
public class ChopChopFrustumCulling {
	public static void addVisibleChunksToRenderList(ChunkRenderList renderList,
													AdvancedShadowCullingFrustum frustum,
													RenderRegionManager regions,
													Long2ReferenceMap<RenderSection> sections) {
		// Note: this makes a copy, so we can safely mutate the array.
		Vector4f[] planes = frustum.getPlanes();

		for (Vector4f plane : planes) {
			// Adjust distances from blocks to chunks.
			plane.w *= (1.0 / 16.0);
		}

		double frustumCenterX = frustum.getX() / 16.0;
		double frustumCenterY = frustum.getY() / 16.0;
		double frustumCenterZ = frustum.getZ() / 16.0;

		for (RenderRegion region : regions.getLoadedRegions()) {
			if (region.getVisibility() != Frustum.Visibility.OUTSIDE) {
				// Get the region origin measured in chunk sections (16x16x16 blocks)
				int originX = region.getOriginX() / 16;
				int originY = region.getOriginY() / 16;
				int originZ = region.getOriginZ() / 16;

				float offsetX = (float) (originX - frustumCenterX);
				float offsetY = (float) (originY - frustumCenterY);
				float offsetZ = (float) (originZ - frustumCenterZ);

				BitSet visibleChunks = ChopChopFrustumCulling.chop2(planes, offsetX, offsetY, offsetZ);

				for (int i = visibleChunks.nextSetBit(0);
					 i >= 0;
					 i = visibleChunks.nextSetBit(i+1)) {
					// Get the chunk section coordinates (measured in units of chunk sections)
					int chunkX = originX + ChopChopFrustumCulling.indexX(i);
					int chunkY = originY + ChopChopFrustumCulling.indexY(i);
					int chunkZ = originZ + ChopChopFrustumCulling.indexZ(i);

					long sectionPos = SectionPos.asLong(chunkX, chunkY, chunkZ);
					RenderSection section = sections.get(sectionPos);

					// note: the original culling result from chop2 didn't take into account whether the chunk
					// was loaded before treating it as visible, so we have to check to see if the chunk is
					// actually there.
					if (section != null) {
						renderList.add(section);
					}
				}
			}
		}
	}

	/**
	 * Executes the ChopChop algorithm on a given render region.
	 *
	 * @param planes A list of frustum planes. The distance component (w) is measured
	 *               in units of cubic chunks, unlike in AdvancedShadowCullingFrustum
	 *               where it is measured in units of blocks. Make sure to convert
	 *               this properly by dividing w by 16 before passing to this function!
	 * @param originX The X origin of the render region, relative to the camera position.
	 *                Measured in units of cubic chunks. (ie, originX = 1 means that
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
				for (int iy = 0; iy < RenderRegion.REGION_HEIGHT; iy++) {
					float ry = originY + iy;
					for (int iz = 0; iz < RenderRegion.REGION_LENGTH; iz++) {
						float rz = originZ + iz;

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
						// a plane if dot(plane, point) > 0.
						//
						// Let x' be an arbitrary x value such that (x', y, z) is inside the plane.
						// Clearly, ax' must be less than ax for the plane equation to be less than zero:
						// ax' > ax
						//
						// Let's assume that x' > x. It must be the case that a < 0 because if a > 0,
						// then ax' > ax would only hold if x' < x. Therefore, for a given value of a,
						// two possibilities exist:
						//
						// * a < 0: All values less than the boundary x are in the plane
						// * a > 0: All values greater than the boundary x are in the plane

						int ixmin = 0;
						int ixmax = RenderRegion.REGION_WIDTH;

						// Note: This rounding is sensitive, it's been tuned to make sure that our
						// integral value lies on either one side or the other
						if (plane.x < 0) {
							float xmax = Math.max(Math.max(xmid00, xmid10), Math.max(xmid01, xmid11));
							ixmax = (int) Math.ceil(xmax - originX);
							ixmax = Math.min(ixmax, RenderRegion.REGION_WIDTH);
						} else {
							float xmin = Math.min(Math.min(xmid00, xmid10), Math.min(xmid01, xmid11));
							ixmin = (int) Math.floor(xmin - originX);
							ixmin = Math.max(ixmin, 0);
						}

						if (ixmax <= 0 || ixmin >= RenderRegion.REGION_WIDTH) {
							// Plane is not inside the region.
							continue;
						}

						// Now that we've found our bounds, we can just fill in all the voxels along this
						// axis-aligned line that are inside the plane.

						for (int ix = ixmin; ix < ixmax; ix++) {
							current.set(getChunkIndex(ix, iy, iz));
						}
					}
				}
			} else if (abs.y == maxAxis) {
				// Same as previous case, but with Y being the major axis.
				for (int iz = 0; iz < RenderRegion.REGION_LENGTH; iz++) {
					float rz = originZ + iz;
					for (int ix = 0; ix < RenderRegion.REGION_WIDTH; ix++) {
						float rx = originX + ix;

						float ymid00 = -(plane.x * (rx    ) + plane.z * (rz    ) + plane.w) / plane.y;
						float ymid10 = -(plane.x * (rx + 1) + plane.z * (rz    ) + plane.w) / plane.y;
						float ymid01 = -(plane.x * (rx    ) + plane.z * (rz + 1) + plane.w) / plane.y;
						float ymid11 = -(plane.x * (rx + 1) + plane.z * (rz + 1) + plane.w) / plane.y;

						int iymin = 0;
						int iymax = RenderRegion.REGION_HEIGHT;

						if (plane.y < 0) {
							float ymax = Math.max(Math.max(ymid00, ymid10), Math.max(ymid01, ymid11));
							iymax = (int) Math.ceil(ymax - originY);
							iymax = Math.min(iymax, RenderRegion.REGION_HEIGHT);
						} else {
							float ymin = Math.min(Math.min(ymid00, ymid10), Math.min(ymid01, ymid11));
							iymin = (int) Math.floor(ymin - originY);
							iymin = Math.max(iymin, 0);
						}

						if (iymax <= 0 || iymin >= RenderRegion.REGION_HEIGHT) {
							// Plane is not inside the region.
							continue;
						}

						// Now that we've found our bounds, we can just fill in all the voxels along this
						// axis-aligned line that are inside the plane.

						for (int iy = iymin; iy < iymax; iy++) {
							current.set(getChunkIndex(ix, iy, iz));
						}
					}
				}
			} else if (abs.z == maxAxis) {
				// Same as previous case, but with Z being the major axis.
				for (int iy = 0; iy < RenderRegion.REGION_HEIGHT; iy++) {
					float ry = originY + iy;
					for (int ix = 0; ix < RenderRegion.REGION_WIDTH; ix++) {
						float rx = originX + ix;
						float zmid00 = -(plane.x * (rx    ) + plane.y * (ry    ) + plane.w) / plane.z;
						float zmid10 = -(plane.x * (rx + 1) + plane.y * (ry    ) + plane.w) / plane.z;
						float zmid01 = -(plane.x * (rx    ) + plane.y * (ry + 1) + plane.w) / plane.z;
						float zmid11 = -(plane.x * (rx + 1) + plane.y * (ry + 1) + plane.w) / plane.z;

						int izmin = 0;
						int izmax = RenderRegion.REGION_LENGTH;

						if (plane.z < 0) {
							float zmax = Math.max(Math.max(zmid00, zmid10), Math.max(zmid01, zmid11));
							izmax = (int) Math.ceil(zmax - originZ);
							izmax = Math.min(izmax, RenderRegion.REGION_LENGTH);
						} else {
							float zmin = Math.min(Math.min(zmid00, zmid10), Math.min(zmid01, zmid11));
							izmin = (int) Math.floor(zmin - originZ);
							izmin = Math.max(izmin, 0);
						}

						if (izmax <= 0 || izmin >= RenderRegion.REGION_LENGTH) {
							// Plane is not inside the region.
							continue;
						}

						// Now that we've found our bounds, we can just fill in all the voxels along this
						// axis-aligned line that are inside the plane.

						for (int iz = izmin; iz < izmax; iz++) {
							current.set(getChunkIndex(ix, iy, iz));
						}
					}
				}
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

	public static int indexX(int index) {
		return index / (RenderRegion.REGION_LENGTH * RenderRegion.REGION_HEIGHT);
	}

	public static int indexY(int index) {
		return (index / RenderRegion.REGION_LENGTH) % RenderRegion.REGION_HEIGHT;
	}

	public static int indexZ(int index) {
		return index % RenderRegion.REGION_LENGTH;
	}
}
