package net.coderbot.iris.shadows.frustum.advanced;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.SodiumFrustumExt;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class AdvancedShadowCullingFrustum extends Frustum implements SodiumFrustumExt {
	// conservative estimate for the maximum number of clipping planes:
	// 6 base planes, and 5 possible planes added for each base plane.
	private static final int MAX_CLIPPING_PLANES = 6 * 5;

	/**
	 * We store each plane equation as a Vector4f.
	 *
	 * <p>We can represent a plane equation of the form <code>ax + by + cz = d</code> as a 4-dimensional vector
	 * of the form <code>(a, b, c, -d)</code>. In the special case of a plane that intersects the origin, we get
	 * the 4-dimensional vector <code>(a, b, c, 0)</code>. (a, b, c) is the normal vector of the plane, and d is the
	 * distance of the plane from the origin along that normal vector.</p>
	 *
	 * <p>Then, to test a given point (x, y, z) against the plane, we simply extend that point to a 4-component
	 * homogenous vector (x, y, z, 1), and then compute the dot product. Computing the dot product gives us
	 * ax + by + cz - d = 0, or, rearranged, our original plane equation of ax = by + cz = d.</p>
	 *
	 * <p>Note that, for the purposes of frustum culling, we usually aren't interested in computing whether a point
	 * lies exactly on a plane. Rather, we are interested in determining which side of the plane the point exists on
	 * - the side closer to the origin, or the side farther away from the origin. Fortunately, doing this with the
	 * dot product is still simple. If the dot product is negative, then the point lies closer to the origin than the
	 * plane, and if the dot product is positive, then the point lies further from the origin than the plane.</p>
	 *
	 * <p>In this case, if the point is closer to the origin than the plane, it is outside of the area enclosed by the
	 * plane, and if the point is farther from the origin than the plane, it is inside the area enclosed by the plane.</p>
	 *
	 * <p>So:
	 * <ul>
	 *     <li>dot(plane, point) > 0 implies the point is inside</li>
	 *     <li>dot(plane, point) < 0 implies that the point is outside</li>
	 * </ul>
	 * </p>
	 */
	private final Vector4f[] planes = new Vector4f[MAX_CLIPPING_PLANES];
	private int planeCount = 0;

	// The center coordinates of this frustum.
	private double x;
	private double y;
	private double z;

	private final Vec3f shadowLightVectorFromOrigin;
	private final BoxCuller boxCuller;

	public AdvancedShadowCullingFrustum(Matrix4f playerView, Matrix4f playerProjection, Vec3f shadowLightVectorFromOrigin,
										BoxCuller boxCuller) {
		// We're overriding all of the methods, don't pass any matrices down.
		super(new Matrix4f(), new Matrix4f());

		this.shadowLightVectorFromOrigin = shadowLightVectorFromOrigin;
		BaseClippingPlanes baseClippingPlanes = new BaseClippingPlanes(playerView, playerProjection);

		boolean[] isBack = addBackPlanes(baseClippingPlanes);
		addEdgePlanes(baseClippingPlanes, isBack);

		this.boxCuller = boxCuller;
	}

	private void addPlane(Vector4f plane) {
		planes[planeCount] = plane;
		planeCount += 1;
	}

	/**
	 * Adds the back planes of the player's view frustum from the perspective of the shadow light.
	 * This can eliminate many chunks, especially if the player is staring at the shadow light
	 * (sun / moon).
	 */
	private boolean[] addBackPlanes(BaseClippingPlanes baseClippingPlanes) {
		Vector4f[] planes = baseClippingPlanes.getPlanes();
		boolean[] isBack = new boolean[planes.length];

		for (int planeIndex = 0; planeIndex < planes.length; planeIndex++) {
			Vector4f plane = planes[planeIndex];
			Vec3f planeNormal = truncate(plane);

			// Find back planes by looking for planes with a normal vector that points
			// in the same general direction as the vector pointing from the origin to the shadow light
			//
			// That is, the angle between those two vectors is less than or equal to 90 degrees,
			// meaning that the dot product is positive or zero.

			float dot = planeNormal.dot(shadowLightVectorFromOrigin);

			boolean back = dot > 0.0;
			boolean edge = dot == 0.0;

			// TODO: audit behavior when the dot product is zero
			isBack[planeIndex] = back;

			if (back || edge) {
				addPlane(plane);
			}
		}

		return isBack;
	}

	private void addEdgePlanes(BaseClippingPlanes baseClippingPlanes, boolean[] isBack) {
		Vector4f[] planes = baseClippingPlanes.getPlanes();

		for (int planeIndex = 0; planeIndex < planes.length; planeIndex++) {
			if (!isBack[planeIndex]) {
				continue;
			}

			Vector4f plane = planes[planeIndex];

			NeighboringPlaneSet neighbors = NeighboringPlaneSet.forPlane(planeIndex);

			if (!isBack[neighbors.getPlane0()]) {
				addEdgePlane(plane, planes[neighbors.getPlane0()]);
			}

			if (!isBack[neighbors.getPlane1()]) {
				addEdgePlane(plane, planes[neighbors.getPlane1()]);
			}

			if (!isBack[neighbors.getPlane2()]) {
				addEdgePlane(plane, planes[neighbors.getPlane2()]);
			}

			if (!isBack[neighbors.getPlane3()]) {
				addEdgePlane(plane, planes[neighbors.getPlane3()]);
			}
		}
	}

	private Vec3f truncate(Vector4f base) {
		return new Vec3f(base.getX(), base.getY(), base.getZ());
	}

	private Vector4f extend(Vec3f base, float w) {
		return new Vector4f(base.getX(), base.getY(), base.getZ(), w);
	}

	private float lengthSquared(Vec3f v) {
		float x = v.getX();
		float y = v.getY();
		float z = v.getZ();

		return x * x + y * y + z * z;
	}

	private Vec3f cross(Vec3f first, Vec3f second) {
		Vec3f result = new Vec3f(first.getX(), first.getY(), first.getZ());
		result.cross(second);

		return result;
	}

	private void addEdgePlane(Vector4f backPlane4, Vector4f frontPlane4) {
		Vec3f backPlaneNormal = truncate(backPlane4);
		Vec3f frontPlaneNormal = truncate(frontPlane4);

		// vector along the intersection of the two planes
		Vec3f intersection = cross(backPlaneNormal, frontPlaneNormal);

		// compute edge plane normal, we want the normal vector of the edge plane
		// to always be perpendicular to the shadow light vector (since that's
		// what makes it an edge plane!)
		Vec3f edgePlaneNormal = cross(intersection, shadowLightVectorFromOrigin);

		// At this point, we have a normal vector for our new edge plane, but we don't
		// have a value for distance (d). We can solve for it with a little algebra,
		// given that we want all 3 planes to intersect at a line.

		// Given the following system of equations:
		// a₁x + b₁y + c₁z = d₁
		// a₂x + b₂y + c₂z = d₂
		// a₃x + b₃y + c₃z = d₃
		//
		// Solve for -d₃, if a₁, b₁, c₁, -d₁, a₂, b₂, c₂, -d₂, a₃, b₃, and c₃ are all known, such that
		// the 3 planes formed by the corresponding 3 plane equations intersect at a line.

		// First, we need to pick a point along the intersection line between our planes.
		// Unfortunately, we don't have a complete line - only its vector.
		//
		// Fortunately, we can compute that point. If we create a plane passing through the origin
		// with a normal vector parallel to the intersection line, then the intersection
		// of all 3 planes will be a point on the line of intersection between the two planes we care about.
		Vec3f point;

		{
			// "Line of intersection between two planes"
			// https://stackoverflow.com/a/32410473 by ideasman42, CC BY-SA 3.0
			// (a modified version of "Intersection of 2-planes" from Graphics Gems 1, page 305

			// NB: We can assume that the intersection vector has a non-zero length.
			Vec3f ixb = cross(intersection, backPlaneNormal);
			Vec3f fxi = cross(frontPlaneNormal, intersection);

			ixb.scale(-frontPlane4.getW());
			fxi.scale(-backPlane4.getW());

			ixb.add(fxi);

			point = ixb;
			point.scale(1.0F / lengthSquared(intersection));
		}

		// Now that we have a point and a normal vector, we can make a plane.

		Vector4f plane;

		{
			// dot(normal, (x, y, z) - point) = 0
			// a(x - point.x) + b(y - point.y) + c(z - point.z) = 0
			// d = a * point.x + b * point.y + c * point.z = dot(normal, point)
			// w = -d

			float d = edgePlaneNormal.dot(point);
			float w = -d;

			plane = extend(edgePlaneNormal, w);
		}

		// Check and make sure our point is actually on all 3 planes.
		// This can be removed in production but it's good to check for now while we're still testing.
		/*{
			float dp0 = plane.dotProduct(extend(point, 1.0F));
			float dp1 = frontPlane4.dotProduct(extend(point, 1.0F));
			float dp2 = backPlane4.dotProduct(extend(point, 1.0F));

			if (Math.abs(dp0) > 0.0005) {
				throw new IllegalStateException("dp0 should be zero, but was " + dp0);
			}

			if (Math.abs(dp1) > 0.0005) {
				throw new IllegalStateException("dp1 should be zero, but was " + dp1);
			}

			if (Math.abs(dp2) > 0.0005) {
				throw new IllegalStateException("dp2 should be zero, but was " + dp2);
			}
		}*/

		addPlane(plane);
	}

	// Note: These functions are copied & modified from the vanilla Frustum class.
	@Override
	public void setPosition(double cameraX, double cameraY, double cameraZ) {
		if (this.boxCuller != null) {
			boxCuller.setPosition(cameraX, cameraY, cameraZ);
		}

		this.x = cameraX;
		this.y = cameraY;
		this.z = cameraZ;
	}

	public boolean isVisible(Box box) {
		if (boxCuller != null && boxCuller.isCulled(box)) {
			return false;
		}

		return this.isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	// For Sodium
	public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (boxCuller != null && boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		}

		return isVisible(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		float f = (float)(minX - this.x);
		float g = (float)(minY - this.y);
		float h = (float)(minZ - this.z);
		float i = (float)(maxX - this.x);
		float j = (float)(maxY - this.y);
		float k = (float)(maxZ - this.z);
		return this.isAnyCornerVisible(f, g, h, i, j, k);
	}

	private boolean isAnyCornerVisible(float x1, float y1, float z1, float x2, float y2, float z2) {
		for(int i = 0; i < planeCount; ++i) {
			Vector4f plane = this.planes[i];

			// dot(plane, point) > 0.0F implies inside
			// if no points are inside, then this box lies entirely outside of the frustum.
			// this avoids false negative - a single point being inside causes the box to pass
			// this plane test

			if (       !(plane.dotProduct(new Vector4f(x1, y1, z1, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x2, y1, z1, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x1, y2, z1, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x2, y2, z1, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x1, y1, z2, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x2, y1, z2, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x1, y2, z2, 1.0F)) > 0.0F)
					&& !(plane.dotProduct(new Vector4f(x2, y2, z2, 1.0F)) > 0.0F)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// TODO: FULLY_VISIBLE
		return fastAabbTest(minX, minY, minZ, maxX, maxY, maxZ) ? RenderRegionVisibility.VISIBLE : RenderRegionVisibility.CULLED;
	}
}
