package net.irisshaders.iris.shadows.frustum.advanced;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.management.ManagementFactory;

/**
 * A Frustum implementation that derives a tightly-fitted shadow pass frustum based on the player's camera frustum and
 * an assumption that the shadow map will only be sampled for the purposes of direct shadow casting, volumetric lighting,
 * and similar effects, but notably not sun-bounce GI or similar effects.
 *
 * <p>The key idea of this algorithm is that if you are looking at the sun, something behind you cannot directly cast
 * a shadow on things visible to you. It's clear why this wouldn't work for sun-bounce GI, since with sun-bounce GI an
 * object behind you could cause light to bounce on to things visible to you.</p>
 *
 * <p>Derived from L. Spiro's clever algorithm & helpful diagrams described in a two-part blog tutorial:</p>
 *
 * <ul>
 * <li><a href="http://lspiroengine.com/?p=153">Tutorial: Tightly Culling Shadow Casters for Directional Lights (Part 1)</a></li>
 * <li><a href="http://lspiroengine.com/?p=187">Tutorial: Tightly Culling Shadow Casters for Directional Lights (Part 2)</a></li>
 * </ul>
 *
 * <p>Notable changes include switching out some of the sub-algorithms for computing the "extruded" edge planes to ones that
 * are not sensitive to the specific internal ordering of planes and corners, in order to avoid potential bugs at the
 * cost of slightly more computations.</p>
 */
public class AdvancedShadowCullingFrustum extends Frustum implements net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum, ViewportProvider {
	private static final int MAX_CLIPPING_PLANES = 13;
	protected final @Nullable BoxCuller boxCuller;
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
	private final float[][] planes = new float[MAX_CLIPPING_PLANES][4];
	private final Vector3f shadowLightVectorFromOrigin;
	private final Vector3d position = new Vector3d();
	// The center coordinates of this frustum.
	public double x;
	public double y;
	public double z;
	private int planeCount = 0;

	public AdvancedShadowCullingFrustum(Matrix4fc modelViewProjection, Matrix4fc shadowProjection, Vector3f shadowLightVectorFromOrigin,
										@Nullable BoxCuller boxCuller) {
		// We're overriding all of the methods, don't pass any matrices down.
		super(new org.joml.Matrix4f(), new org.joml.Matrix4f());


		/*
			testing code, please ignore
			System.out.println(shadowProjection.toString(NumberFormat.getNumberInstance()));
			System.out.println(modelViewProjection.toString(NumberFormat.getNumberInstance()));
		 */

		this.shadowLightVectorFromOrigin = shadowLightVectorFromOrigin;
		BaseClippingPlanes baseClippingPlanes = new BaseClippingPlanes(modelViewProjection);

		boolean[] isBack = addBackPlanes(baseClippingPlanes);
		addEdgePlanes(baseClippingPlanes, isBack);

		this.boxCuller = boxCuller;
	}

	private void addPlane(float[] plane) {
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
			Vector3f planeNormal = truncate(plane);

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
				addPlane(new float[] { plane.x, plane.y, plane.z, plane.w });
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

			if (!isBack[neighbors.plane0()]) {
				addEdgePlane(plane, planes[neighbors.plane0()]);
			}

			if (!isBack[neighbors.plane1()]) {
				addEdgePlane(plane, planes[neighbors.plane1()]);
			}

			if (!isBack[neighbors.plane2()]) {
				addEdgePlane(plane, planes[neighbors.plane2()]);
			}

			if (!isBack[neighbors.plane3()]) {
				addEdgePlane(plane, planes[neighbors.plane3()]);
			}
		}
	}

	private Vector3f truncate(Vector4f base) {
		return new Vector3f(base.x(), base.y(), base.z());
	}

	private Vector4f extend(Vector3f base, float w) {
		return new Vector4f(base.x(), base.y(), base.z(), w);
	}

	private float lengthSquared(Vector3f v) {
		float x = v.x();
		float y = v.y();
		float z = v.z();

		return x * x + y * y + z * z;
	}

	private Vector3f cross(Vector3f first, Vector3f second) {
		Vector3f result = new Vector3f(first.x(), first.y(), first.z());
		result.cross(second);

		return result;
	}

	private void addEdgePlane(Vector4f backPlane4, Vector4f frontPlane4) {
		Vector3f backPlaneNormal = truncate(backPlane4);
		Vector3f frontPlaneNormal = truncate(frontPlane4);

		// vector along the intersection of the two planes
		Vector3f intersection = cross(backPlaneNormal, frontPlaneNormal);

		// compute edge plane normal, we want the normal vector of the edge plane
		// to always be perpendicular to the shadow light vector (since that's
		// what makes it an edge plane!)
		Vector3f edgePlaneNormal = cross(intersection, shadowLightVectorFromOrigin);

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
		Vector3f point;

		{
			// "Line of intersection between two planes"
			// https://stackoverflow.com/a/32410473 by ideasman42, CC BY-SA 3.0
			// (a modified version of "Intersection of 2-planes" from Graphics Gems 1, page 305

			// NB: We can assume that the intersection vector has a non-zero length.
			Vector3f ixb = cross(intersection, backPlaneNormal);
			Vector3f fxi = cross(frontPlaneNormal, intersection);

			ixb.mul(-frontPlane4.w());
			fxi.mul(-backPlane4.w());

			ixb.add(fxi);

			point = ixb;
			point.mul(1.0F / lengthSquared(intersection));
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

		addPlane(new float[] { plane.x, plane.y, plane.z, plane.w });
	}

	// Note: These functions are copied & modified from the vanilla Frustum class.
	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
		if (this.boxCuller != null) {
			boxCuller.setPosition(cameraX, cameraY, cameraZ);
		}

		this.x = cameraX;
		this.y = cameraY;
		this.z = cameraZ;
	}

	@Override
	public Viewport sodium$createViewport() {
		return new Viewport(this, this.position.set(this.x, this.y, this.z));
	}

	private static final boolean FMA_SUPPORT;

	static {
		// Automatically enable `joml.useMathFma` system property if JVM UseFMA is enabled.
		// The JVM will automatically enable the UseFMA vm option if the cpu supports it.
		// Thanks to kunzite for making me aware of this.
		final HotSpotDiagnosticMXBean hotSpotDiagnostic = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
		if (hotSpotDiagnostic == null) {
			FMA_SUPPORT = false;
		} else {
			final VMOption useFMAVMOption = hotSpotDiagnostic.getVMOption("UseFMA");
			FMA_SUPPORT = Boolean.parseBoolean(useFMAVMOption.getValue());
		}
	}

	private static float safeFMA(float a, float b, float c) {
		return a * b + c;
	}

	/**
	 * Checks corner visibility.
	 *
	 * @param minX Minimum X value of the AABB.
	 * @param minY Minimum Y value of the AABB.
	 * @param minZ Minimum Z value of the AABB.
	 * @param maxX Maximum X value of the AABB.
	 * @param maxY Maximum Y value of the AABB.
	 * @param maxZ Maximum Z value of the AABB.
	 * @return OUTSIDE if nothing is visible, INSIDE if everything is visible, INTERSECT if only some corners are visible.
	 */
	protected int checkCornerVisibility(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		boolean inside = true;

		for (int i = 0; i < this.planeCount; ++i) {
			float[] plane = this.planes[i];

			// Check if plane is inside or intersecting.
			// This is ported from JOML's FrustumIntersection.

			float outsideBoundX = (plane[0] < 0) ? minX : maxX;
			float outsideBoundY = (plane[1] < 0) ? minY : maxY;
			float outsideBoundZ = (plane[2] < 0) ? minZ : maxZ;

			if (FMA_SUPPORT) {
				if (Math.fma(plane[0], outsideBoundX, Math.fma(plane[1], outsideBoundY, plane[2] * outsideBoundZ)) >= -plane[3]) {
					inside &= Math.fma(plane[0], (plane[0] < 0 ? maxX : minX),
						Math.fma(plane[1], (plane[1] < 0 ? maxY : minY),
							Math.fma(plane[2], (plane[2] < 0 ? maxZ : minZ), plane[3]))) >= 0;
				} else {
					return FrustumIntersection.OUTSIDE;
				}
			} else {
				if (safeFMA(plane[0], outsideBoundX, safeFMA(plane[1], outsideBoundY, plane[2] * outsideBoundZ)) >= -plane[3]) {
					inside &= safeFMA(plane[0], (plane[0] < 0 ? maxX : minX),
						safeFMA(plane[1], (plane[1] < 0 ? maxY : minY),
							safeFMA(plane[2], (plane[2] < 0 ? maxZ : minZ), plane[3]))) >= 0;
				} else {
					return FrustumIntersection.OUTSIDE;
				}
			}
		}

		return inside ? FrustumIntersection.INSIDE : FrustumIntersection.INTERSECT;
	}

	/**
	 * Checks corner visibility.
	 *
	 * @param minX Minimum X value of the AABB.
	 * @param minY Minimum Y value of the AABB.
	 * @param minZ Minimum Z value of the AABB.
	 * @param maxX Maximum X value of the AABB.
	 * @param maxY Maximum Y value of the AABB.
	 * @param maxZ Maximum Z value of the AABB.
	 * @return true if visible, false if not.
	 */
	public boolean checkCornerVisibilityBool(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		for (int i = 0; i < this.planeCount; ++i) {
			float[] plane = this.planes[i];

			float outsideBoundX = (plane[0] < 0) ? minX : maxX;
			float outsideBoundY = (plane[1] < 0) ? minY : maxY;
			float outsideBoundZ = (plane[2] < 0) ? minZ : maxZ;

			if (FMA_SUPPORT) {
				if (Math.fma(plane[0], outsideBoundX, Math.fma(plane[1], outsideBoundY, plane[2] * outsideBoundZ)) < -plane[3]) {
					return false;
				}
			} else {
				if (safeFMA(plane[0], outsideBoundX, safeFMA(plane[1], outsideBoundY, plane[2] * outsideBoundZ)) < -plane[3]) {
					return false;
				}
			}
		}

		return true;
	}

	// For Immersive Portals
	// TODO: Figure out if IP culling can somehow be compatible with Iris culling.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	protected boolean isVisibleBool(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return this.checkCornerVisibilityBool(
			(float) (minX - this.x),
			(float) (minY - this.y),
			(float) (minZ - this.z),
			(float) (maxX - this.x),
			(float) (maxY - this.y),
			(float) (maxZ - this.z));
	}

	@Override
	public boolean isVisible(AABB aabb) {
		if (this.boxCuller != null && this.boxCuller.isCulled(aabb)) {
			return false;
		}

		return this.isVisibleBool(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return (this.boxCuller == null || !this.boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) &&
			this.checkCornerVisibilityBool(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (this.boxCuller == null) {
			return this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ);
		}

		var distanceResult = this.boxCuller.intersectAab(minX, minY, minZ, maxX, maxY, maxZ);
		if (distanceResult == FrustumIntersection.OUTSIDE) {
			return FrustumIntersection.OUTSIDE;
		}
		if (distanceResult == FrustumIntersection.INSIDE) {
			return this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ);
		}

		// distanceResult == FrustumIntersection.INTERSECT, so the final result is either OUTSIDE or INTERSECT.
		// critically, it's not INSIDE, because the frustum test can't determine that alone.
		var frustumResult = this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ);
		return frustumResult == FrustumIntersection.INSIDE ? FrustumIntersection.INTERSECT : frustumResult;
	}
}
