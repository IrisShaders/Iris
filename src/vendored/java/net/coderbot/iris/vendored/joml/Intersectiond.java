/*
 * The MIT License
 *
 * Copyright (c) 2015-2021 Kai Burjack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderbot.iris.vendored.joml;

/**
 * Contains intersection and distance tests for some 2D and 3D geometric primitives.
 * 
 * @author Kai Burjack
 */
public class Intersectiond {

    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point is the first vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_VERTEX_0 = 1;
    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point is the second vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_VERTEX_1 = 2;
    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point is the third vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_VERTEX_2 = 3;

    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point lies on the edge between the first and second vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_EDGE_01 = 4;
    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point lies on the edge between the second and third vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_EDGE_12 = 5;
    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point lies on the edge between the third and first vertex of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_EDGE_20 = 6;

    /**
     * Return value of
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)},
     * {@link #findClosestPointOnTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3d)},
     * {@link #findClosestPointOnTriangle(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #findClosestPointOnTriangle(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} or 
     * {@link #intersectSweptSphereTriangle}
     * to signal that the closest point lies on the face of the triangle.
     */
    public static final int POINT_ON_TRIANGLE_FACE = 7;

    /**
     * Return value of {@link #intersectRayAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)}
     * to indicate that the ray intersects the side of the axis-aligned rectangle with the minimum x coordinate.
     */
    public static final int AAR_SIDE_MINX = 0;
    /**
     * Return value of {@link #intersectRayAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)}
     * to indicate that the ray intersects the side of the axis-aligned rectangle with the minimum y coordinate.
     */
    public static final int AAR_SIDE_MINY = 1;
    /**
     * Return value of {@link #intersectRayAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)}
     * to indicate that the ray intersects the side of the axis-aligned rectangle with the maximum x coordinate.
     */
    public static final int AAR_SIDE_MAXX = 2;
    /**
     * Return value of {@link #intersectRayAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)}
     * to indicate that the ray intersects the side of the axis-aligned rectangle with the maximum y coordinate.
     */
    public static final int AAR_SIDE_MAXY = 3;

    /**
     * Return value of {@link #intersectLineSegmentAab(double, double, double, double, double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)} to indicate that the line segment does not intersect the axis-aligned box;
     * or return value of {@link #intersectLineSegmentAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} to indicate that the line segment does not intersect the axis-aligned rectangle.
     */
    public static final int OUTSIDE = -1;
    /**
     * Return value of {@link #intersectLineSegmentAab(double, double, double, double, double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)} to indicate that one end point of the line segment lies inside of the axis-aligned box;
     * or return value of {@link #intersectLineSegmentAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} to indicate that one end point of the line segment lies inside of the axis-aligned rectangle.
     */
    public static final int ONE_INTERSECTION = 1;
    /**
     * Return value of {@link #intersectLineSegmentAab(double, double, double, double, double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)} to indicate that the line segment intersects two sides of the axis-aligned box
     * or lies on an edge or a side of the box;
     * or return value of {@link #intersectLineSegmentAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} to indicate that the line segment intersects two edges of the axis-aligned rectangle
     * or lies on an edge of the rectangle.
     */
    public static final int TWO_INTERSECTION = 2;
    /**
     * Return value of {@link #intersectLineSegmentAab(double, double, double, double, double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)} to indicate that the line segment lies completely inside of the axis-aligned box;
     * or return value of {@link #intersectLineSegmentAar(double, double, double, double, double, double, double, double, Vector2d)} and
     * {@link #intersectLineSegmentAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)} to indicate that the line segment lies completely inside of the axis-aligned rectangle.
     */
    public static final int INSIDE = 3;

    /**
     * Test whether the plane with the general plane equation <i>a*x + b*y + c*z + d = 0</i> intersects the sphere with center
     * <code>(centerX, centerY, centerZ)</code> and <code>radius</code>.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/943383/determine-circle-of-intersection-of-plane-and-sphere">http://math.stackexchange.com</a>
     *
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @param centerX
     *          the x coordinate of the sphere's center
     * @param centerY
     *          the y coordinate of the sphere's center
     * @param centerZ
     *          the z coordinate of the sphere's center
     * @param radius
     *          the radius of the sphere
     * @return <code>true</code> iff the plane intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testPlaneSphere(
            double a, double b, double c, double d,
            double centerX, double centerY, double centerZ, double radius) {
        double denom = Math.sqrt(a * a + b * b + c * c);
        double dist = (a * centerX + b * centerY + c * centerZ + d) / denom;
        return -radius <= dist && dist <= radius;
    }

    /**
     * Test whether the plane with the general plane equation <i>a*x + b*y + c*z + d = 0</i> intersects the sphere with center
     * <code>(centerX, centerY, centerZ)</code> and <code>radius</code>, and store the center of the circle of
     * intersection in the <code>(x, y, z)</code> components of the supplied vector and the radius of that circle in the w component.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/943383/determine-circle-of-intersection-of-plane-and-sphere">http://math.stackexchange.com</a>
     *
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @param centerX
     *          the x coordinate of the sphere's center
     * @param centerY
     *          the y coordinate of the sphere's center
     * @param centerZ
     *          the z coordinate of the sphere's center
     * @param radius
     *          the radius of the sphere
     * @param intersectionCenterAndRadius
     *          will hold the center of the circle of intersection in the <code>(x, y, z)</code> components and the radius in the w component
     * @return <code>true</code> iff the plane intersects the sphere; <code>false</code> otherwise
     */
    public static boolean intersectPlaneSphere(
            double a, double b, double c, double d,
            double centerX, double centerY, double centerZ, double radius,
            Vector4d intersectionCenterAndRadius) {
        double invDenom = Math.invsqrt(a * a + b * b + c * c);
        double dist = (a * centerX + b * centerY + c * centerZ + d) * invDenom;
        if (-radius <= dist && dist <= radius) {
            intersectionCenterAndRadius.x = centerX + dist * a * invDenom;
            intersectionCenterAndRadius.y = centerY + dist * b * invDenom;
            intersectionCenterAndRadius.z = centerZ + dist * c * invDenom;
            intersectionCenterAndRadius.w = Math.sqrt(radius * radius - dist * dist);
            return true;
        }
        return false;
    }

    /**
     * Test whether the plane with the general plane equation <i>a*x + b*y + c*z + d = 0</i> intersects the moving sphere with center
     * <code>(cX, cY, cZ)</code>, <code>radius</code> and velocity <code>(vX, vY, vZ)</code>, and store the point of intersection
     * in the <code>(x, y, z)</code> components of the supplied vector and the time of intersection in the w component.
     * <p>
     * The normal vector <code>(a, b, c)</code> of the plane equation needs to be normalized.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.5.3 "Intersecting Moving Sphere Against Plane"
     * 
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @param cX
     *          the x coordinate of the center position of the sphere at t=0
     * @param cY
     *          the y coordinate of the center position of the sphere at t=0
     * @param cZ
     *          the z coordinate of the center position of the sphere at t=0
     * @param radius
     *          the sphere's radius
     * @param vX
     *          the x component of the velocity of the sphere
     * @param vY
     *          the y component of the velocity of the sphere
     * @param vZ
     *          the z component of the velocity of the sphere
     * @param pointAndTime
     *          will hold the point and time of intersection (if any)
     * @return <code>true</code> iff the sphere intersects the plane; <code>false</code> otherwise
     */
    public static boolean intersectPlaneSweptSphere(
            double a, double b, double c, double d,
            double cX, double cY, double cZ, double radius,
            double vX, double vY, double vZ,
            Vector4d pointAndTime) {
        // Compute distance of sphere center to plane
        double dist = a * cX + b * cY + c * cZ - d;
        if (Math.abs(dist) <= radius) {
            // The sphere is already overlapping the plane. Set time of
            // intersection to zero and q to sphere center
            pointAndTime.set(cX, cY, cZ, 0.0);
            return true;
        }
        double denom = a * vX + b * vY + c * vZ;
        if (denom * dist >= 0.0) {
            // No intersection as sphere moving parallel to or away from plane
            return false;
        }
        // Sphere is moving towards the plane
        // Use +r in computations if sphere in front of plane, else -r
        double r = dist > 0.0 ? radius : -radius;
        double t = (r - dist) / denom;
        pointAndTime.set(
                cX + t * vX - r * a,
                cY + t * vY - r * b,
                cZ + t * vZ - r * c,
                t);
        return true;
    }

    /**
     * Test whether the plane with the general plane equation <i>a*x + b*y + c*z + d = 0</i> intersects the sphere moving from center
     * position <code>(t0X, t0Y, t0Z)</code> to <code>(t1X, t1Y, t1Z)</code> and having the given <code>radius</code>.
     * <p>
     * The normal vector <code>(a, b, c)</code> of the plane equation needs to be normalized.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.5.3 "Intersecting Moving Sphere Against Plane"
     * 
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @param t0X
     *          the x coordinate of the start position of the sphere
     * @param t0Y
     *          the y coordinate of the start position of the sphere
     * @param t0Z
     *          the z coordinate of the start position of the sphere
     * @param r
     *          the sphere's radius
     * @param t1X
     *          the x coordinate of the end position of the sphere
     * @param t1Y
     *          the y coordinate of the end position of the sphere
     * @param t1Z
     *          the z coordinate of the end position of the sphere
     * @return <code>true</code> if the sphere intersects the plane; <code>false</code> otherwise
     */
    public static boolean testPlaneSweptSphere(
            double a, double b, double c, double d,
            double t0X, double t0Y, double t0Z, double r,
            double t1X, double t1Y, double t1Z) {
        // Get the distance for both a and b from plane p
        double adist = t0X * a + t0Y * b + t0Z * c - d;
        double bdist = t1X * a + t1Y * b + t1Z * c - d;
        // Intersects if on different sides of plane (distances have different signs)
        if (adist * bdist < 0.0) return true;
        // Intersects if start or end position within radius from plane
        if (Math.abs(adist) <= r || Math.abs(bdist) <= r) return true;
        // No intersection
        return false;
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>(minX, minY, minZ)</code> and maximum corner <code>(maxX, maxY, maxZ)</code>
     * intersects the plane with the general equation <i>a*x + b*y + c*z + d = 0</i>.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     * 
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned box
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned box
     * @param minZ
     *          the z coordinate of the minimum corner of the axis-aligned box
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned box
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned box
     * @param maxZ
     *          the z coordinate of the maximum corner of the axis-aligned box
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return <code>true</code> iff the axis-aligned box intersects the plane; <code>false</code> otherwise
     */
    public static boolean testAabPlane(
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ,
            double a, double b, double c, double d) {
        double pX, pY, pZ, nX, nY, nZ;
        if (a > 0.0) {
            pX = maxX;
            nX = minX;
        } else {
            pX = minX;
            nX = maxX;
        }
        if (b > 0.0) {
            pY = maxY;
            nY = minY;
        } else {
            pY = minY;
            nY = maxY;
        }
        if (c > 0.0) {
            pZ = maxZ;
            nZ = minZ;
        } else {
            pZ = minZ;
            nZ = maxZ;
        }
        double distN = d + a * nX + b * nY + c * nZ;
        double distP = d + a * pX + b * pY + c * pZ;
        return distN <= 0.0 && distP >= 0.0;
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>min</code> and maximum corner <code>max</code>
     * intersects the plane with the general equation <i>a*x + b*y + c*z + d = 0</i>.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     * 
     * @param min
     *          the minimum corner of the axis-aligned box
     * @param max
     *          the maximum corner of the axis-aligned box
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return <code>true</code> iff the axis-aligned box intersects the plane; <code>false</code> otherwise
     */
    public static boolean testAabPlane(Vector3dc min, Vector3dc max, double a, double b, double c, double d) {
        return testAabPlane(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), a, b, c, d);
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>(minXA, minYA, minZA)</code> and maximum corner <code>(maxXA, maxYA, maxZA)</code>
     * intersects the axis-aligned box with minimum corner <code>(minXB, minYB, minZB)</code> and maximum corner <code>(maxXB, maxYB, maxZB)</code>.
     * 
     * @param minXA
     *              the x coordinate of the minimum corner of the first axis-aligned box
     * @param minYA
     *              the y coordinate of the minimum corner of the first axis-aligned box
     * @param minZA
     *              the z coordinate of the minimum corner of the first axis-aligned box
     * @param maxXA
     *              the x coordinate of the maximum corner of the first axis-aligned box
     * @param maxYA
     *              the y coordinate of the maximum corner of the first axis-aligned box
     * @param maxZA
     *              the z coordinate of the maximum corner of the first axis-aligned box
     * @param minXB
     *              the x coordinate of the minimum corner of the second axis-aligned box
     * @param minYB
     *              the y coordinate of the minimum corner of the second axis-aligned box
     * @param minZB
     *              the z coordinate of the minimum corner of the second axis-aligned box
     * @param maxXB
     *              the x coordinate of the maximum corner of the second axis-aligned box
     * @param maxYB
     *              the y coordinate of the maximum corner of the second axis-aligned box
     * @param maxZB
     *              the z coordinate of the maximum corner of the second axis-aligned box
     * @return <code>true</code> iff both axis-aligned boxes intersect; <code>false</code> otherwise
     */
    public static boolean testAabAab(
            double minXA, double minYA, double minZA,
            double maxXA, double maxYA, double maxZA,
            double minXB, double minYB, double minZB,
            double maxXB, double maxYB, double maxZB) {
        return maxXA >= minXB && maxYA >= minYB && maxZA >= minZB && 
               minXA <= maxXB && minYA <= maxYB && minZA <= maxZB;
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>minA</code> and maximum corner <code>maxA</code>
     * intersects the axis-aligned box with minimum corner <code>minB</code> and maximum corner <code>maxB</code>.
     * 
     * @param minA
     *              the minimum corner of the first axis-aligned box
     * @param maxA
     *              the maximum corner of the first axis-aligned box
     * @param minB
     *              the minimum corner of the second axis-aligned box
     * @param maxB
     *              the maximum corner of the second axis-aligned box
     * @return <code>true</code> iff both axis-aligned boxes intersect; <code>false</code> otherwise
     */
    public static boolean testAabAab(Vector3dc minA, Vector3dc maxA, Vector3dc minB, Vector3dc maxB) {
        return testAabAab(minA.x(), minA.y(), minA.z(), maxA.x(), maxA.y(), maxA.z(), minB.x(), minB.y(), minB.z(), maxB.x(), maxB.y(), maxB.z());
    }

    /**
     * Test whether two oriented boxes given via their center position, orientation and half-size, intersect.
     * <p>
     * The orientation of a box is given as three unit vectors spanning the local orthonormal basis of the box.
     * <p>
     * The size is given as the half-size along each of the unit vectors defining the orthonormal basis.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 4.4.1 "OBB-OBB Intersection"
     * 
     * @param b0c
     *          the center of the first box
     * @param b0uX
     *          the local X unit vector of the first box
     * @param b0uY
     *          the local Y unit vector of the first box
     * @param b0uZ
     *          the local Z unit vector of the first box
     * @param b0hs
     *          the half-size of the first box
     * @param b1c
     *          the center of the second box
     * @param b1uX
     *          the local X unit vector of the second box
     * @param b1uY
     *          the local Y unit vector of the second box
     * @param b1uZ
     *          the local Z unit vector of the second box
     * @param b1hs
     *          the half-size of the second box
     * @return <code>true</code> if both boxes intersect; <code>false</code> otherwise
     */
    public static boolean testObOb(
            Vector3d b0c, Vector3d b0uX, Vector3d b0uY, Vector3d b0uZ, Vector3d b0hs,
            Vector3d b1c, Vector3d b1uX, Vector3d b1uY, Vector3d b1uZ, Vector3d b1hs) {
        return testObOb(
                b0c.x, b0c.y, b0c.z, b0uX.x, b0uX.y, b0uX.z, b0uY.x, b0uY.y, b0uY.z, b0uZ.x, b0uZ.y, b0uZ.z, b0hs.x, b0hs.y, b0hs.z,
                b1c.x, b1c.y, b1c.z, b1uX.x, b1uX.y, b1uX.z, b1uY.x, b1uY.y, b1uY.z, b1uZ.x, b1uZ.y, b1uZ.z, b1hs.x, b1hs.y, b1hs.z);
    }

    /**
     * Test whether two oriented boxes given via their center position, orientation and half-size, intersect.
     * <p>
     * The orientation of a box is given as three unit vectors spanning the local orthonormal basis of the box.
     * <p>
     * The size is given as the half-size along each of the unit vectors defining the orthonormal basis.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 4.4.1 "OBB-OBB Intersection"
     * 
     * @param b0cX
     *          the x coordinate of the center of the first box
     * @param b0cY
     *          the y coordinate of the center of the first box
     * @param b0cZ
     *          the z coordinate of the center of the first box
     * @param b0uXx
     *          the x coordinate of the local X unit vector of the first box
     * @param b0uXy
     *          the y coordinate of the local X unit vector of the first box
     * @param b0uXz
     *          the z coordinate of the local X unit vector of the first box
     * @param b0uYx
     *          the x coordinate of the local Y unit vector of the first box
     * @param b0uYy
     *          the y coordinate of the local Y unit vector of the first box
     * @param b0uYz
     *          the z coordinate of the local Y unit vector of the first box
     * @param b0uZx
     *          the x coordinate of the local Z unit vector of the first box
     * @param b0uZy
     *          the y coordinate of the local Z unit vector of the first box
     * @param b0uZz
     *          the z coordinate of the local Z unit vector of the first box
     * @param b0hsX
     *          the half-size of the first box along its local X axis
     * @param b0hsY
     *          the half-size of the first box along its local Y axis
     * @param b0hsZ
     *          the half-size of the first box along its local Z axis
     * @param b1cX
     *          the x coordinate of the center of the second box
     * @param b1cY
     *          the y coordinate of the center of the second box
     * @param b1cZ
     *          the z coordinate of the center of the second box
     * @param b1uXx
     *          the x coordinate of the local X unit vector of the second box
     * @param b1uXy
     *          the y coordinate of the local X unit vector of the second box
     * @param b1uXz
     *          the z coordinate of the local X unit vector of the second box
     * @param b1uYx
     *          the x coordinate of the local Y unit vector of the second box
     * @param b1uYy
     *          the y coordinate of the local Y unit vector of the second box
     * @param b1uYz
     *          the z coordinate of the local Y unit vector of the second box
     * @param b1uZx
     *          the x coordinate of the local Z unit vector of the second box
     * @param b1uZy
     *          the y coordinate of the local Z unit vector of the second box
     * @param b1uZz
     *          the z coordinate of the local Z unit vector of the second box
     * @param b1hsX
     *          the half-size of the second box along its local X axis
     * @param b1hsY
     *          the half-size of the second box along its local Y axis
     * @param b1hsZ
     *          the half-size of the second box along its local Z axis
     * @return <code>true</code> if both boxes intersect; <code>false</code> otherwise
     */
    public static boolean testObOb(
            double b0cX, double b0cY, double b0cZ, double b0uXx, double b0uXy, double b0uXz, double b0uYx, double b0uYy, double b0uYz, double b0uZx, double b0uZy, double b0uZz, double b0hsX, double b0hsY, double b0hsZ,
            double b1cX, double b1cY, double b1cZ, double b1uXx, double b1uXy, double b1uXz, double b1uYx, double b1uYy, double b1uYz, double b1uZx, double b1uZy, double b1uZz, double b1hsX, double b1hsY, double b1hsZ) {
        double ra, rb;
        // Compute rotation matrix expressing b in a's coordinate frame
        double rm00 = b0uXx * b1uXx + b0uYx * b1uYx + b0uZx * b1uZx;
        double rm10 = b0uXx * b1uXy + b0uYx * b1uYy + b0uZx * b1uZy;
        double rm20 = b0uXx * b1uXz + b0uYx * b1uYz + b0uZx * b1uZz;
        double rm01 = b0uXy * b1uXx + b0uYy * b1uYx + b0uZy * b1uZx;
        double rm11 = b0uXy * b1uXy + b0uYy * b1uYy + b0uZy * b1uZy;
        double rm21 = b0uXy * b1uXz + b0uYy * b1uYz + b0uZy * b1uZz;
        double rm02 = b0uXz * b1uXx + b0uYz * b1uYx + b0uZz * b1uZx;
        double rm12 = b0uXz * b1uXy + b0uYz * b1uYy + b0uZz * b1uZy;
        double rm22 = b0uXz * b1uXz + b0uYz * b1uYz + b0uZz * b1uZz;
        // Compute common subexpressions. Add in an epsilon term to
        // counteract arithmetic errors when two edges are parallel and
        // their cross product is (near) null (see text for details)
        double EPSILON = 1E-8;
        double arm00 = Math.abs(rm00) + EPSILON;
        double arm01 = Math.abs(rm01) + EPSILON;
        double arm02 = Math.abs(rm02) + EPSILON;
        double arm10 = Math.abs(rm10) + EPSILON;
        double arm11 = Math.abs(rm11) + EPSILON;
        double arm12 = Math.abs(rm12) + EPSILON;
        double arm20 = Math.abs(rm20) + EPSILON;
        double arm21 = Math.abs(rm21) + EPSILON;
        double arm22 = Math.abs(rm22) + EPSILON;
        // Compute translation vector t
        double tx = b1cX - b0cX, ty = b1cY - b0cY, tz = b1cZ - b0cZ;
        // Bring translation into a's coordinate frame
        double tax = tx * b0uXx + ty * b0uXy + tz * b0uXz;
        double tay = tx * b0uYx + ty * b0uYy + tz * b0uYz;
        double taz = tx * b0uZx + ty * b0uZy + tz * b0uZz;
        // Test axes L = A0, L = A1, L = A2
        ra = b0hsX;
        rb = b1hsX * arm00 + b1hsY * arm01 + b1hsZ * arm02;
        if (Math.abs(tax) > ra + rb) return false;
        ra = b0hsY;
        rb = b1hsX * arm10 + b1hsY * arm11 + b1hsZ * arm12;
        if (Math.abs(tay) > ra + rb) return false;
        ra = b0hsZ;
        rb = b1hsX * arm20 + b1hsY * arm21 + b1hsZ * arm22;
        if (Math.abs(taz) > ra + rb) return false;
        // Test axes L = B0, L = B1, L = B2
        ra = b0hsX * arm00 + b0hsY * arm10 + b0hsZ * arm20;
        rb = b1hsX;
        if (Math.abs(tax * rm00 + tay * rm10 + taz * rm20) > ra + rb) return false;
        ra = b0hsX * arm01 + b0hsY * arm11 + b0hsZ * arm21;
        rb = b1hsY;
        if (Math.abs(tax * rm01 + tay * rm11 + taz * rm21) > ra + rb) return false;
        ra = b0hsX * arm02 + b0hsY * arm12 + b0hsZ * arm22;
        rb = b1hsZ;
        if (Math.abs(tax * rm02 + tay * rm12 + taz * rm22) > ra + rb) return false;
        // Test axis L = A0 x B0
        ra = b0hsY * arm20 + b0hsZ * arm10;
        rb = b1hsY * arm02 + b1hsZ * arm01;
        if (Math.abs(taz * rm10 - tay * rm20) > ra + rb) return false;
        // Test axis L = A0 x B1
        ra = b0hsY * arm21 + b0hsZ * arm11;
        rb = b1hsX * arm02 + b1hsZ * arm00;
        if (Math.abs(taz * rm11 - tay * rm21) > ra + rb) return false;
        // Test axis L = A0 x B2
        ra = b0hsY * arm22 + b0hsZ * arm12;
        rb = b1hsX * arm01 + b1hsY * arm00;
        if (Math.abs(taz * rm12 - tay * rm22) > ra + rb) return false;
        // Test axis L = A1 x B0
        ra = b0hsX * arm20 + b0hsZ * arm00;
        rb = b1hsY * arm12 + b1hsZ * arm11;
        if (Math.abs(tax * rm20 - taz * rm00) > ra + rb) return false;
        // Test axis L = A1 x B1
        ra = b0hsX * arm21 + b0hsZ * arm01;
        rb = b1hsX * arm12 + b1hsZ * arm10;
        if (Math.abs(tax * rm21 - taz * rm01) > ra + rb) return false;
        // Test axis L = A1 x B2
        ra = b0hsX * arm22 + b0hsZ * arm02;
        rb = b1hsX * arm11 + b1hsY * arm10;
        if (Math.abs(tax * rm22 - taz * rm02) > ra + rb) return false;
        // Test axis L = A2 x B0
        ra = b0hsX * arm10 + b0hsY * arm00;
        rb = b1hsY * arm22 + b1hsZ * arm21;
        if (Math.abs(tay * rm00 - tax * rm10) > ra + rb) return false;
        // Test axis L = A2 x B1
        ra = b0hsX * arm11 + b0hsY * arm01;
        rb = b1hsX * arm22 + b1hsZ * arm20;
        if (Math.abs(tay * rm01 - tax * rm11) > ra + rb) return false;
        // Test axis L = A2 x B2
        ra = b0hsX * arm12 + b0hsY * arm02;
        rb = b1hsX * arm21 + b1hsY * arm20;
        if (Math.abs(tay * rm02 - tax * rm12) > ra + rb) return false;
        // Since no separating axis is found, the OBBs must be intersecting
        return true;
    }

    /**
     * Test whether the one sphere with center <code>(aX, aY, aZ)</code> and square radius <code>radiusSquaredA</code> intersects the other
     * sphere with center <code>(bX, bY, bZ)</code> and square radius <code>radiusSquaredB</code>, and store the center of the circle of
     * intersection in the <code>(x, y, z)</code> components of the supplied vector and the radius of that circle in the w component.
     * <p>
     * The normal vector of the circle of intersection can simply be obtained by subtracting the center of either sphere from the other.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param aX
     *              the x coordinate of the first sphere's center
     * @param aY
     *              the y coordinate of the first sphere's center
     * @param aZ
     *              the z coordinate of the first sphere's center
     * @param radiusSquaredA
     *              the square of the first sphere's radius
     * @param bX
     *              the x coordinate of the second sphere's center
     * @param bY
     *              the y coordinate of the second sphere's center
     * @param bZ
     *              the z coordinate of the second sphere's center
     * @param radiusSquaredB
     *              the square of the second sphere's radius
     * @param centerAndRadiusOfIntersectionCircle
     *              will hold the center of the circle of intersection in the <code>(x, y, z)</code> components and the radius in the w component
     * @return <code>true</code> iff both spheres intersect; <code>false</code> otherwise
     */
    public static boolean intersectSphereSphere(
            double aX, double aY, double aZ, double radiusSquaredA,
            double bX, double bY, double bZ, double radiusSquaredB,
            Vector4d centerAndRadiusOfIntersectionCircle) {
        double dX = bX - aX, dY = bY - aY, dZ = bZ - aZ;
        double distSquared = dX * dX + dY * dY + dZ * dZ;
        double h = 0.5 + (radiusSquaredA - radiusSquaredB) / distSquared;
        double r_i = radiusSquaredA - h * h * distSquared;
        if (r_i >= 0.0) {
            centerAndRadiusOfIntersectionCircle.x = aX + h * dX;
            centerAndRadiusOfIntersectionCircle.y = aY + h * dY;
            centerAndRadiusOfIntersectionCircle.z = aZ + h * dZ;
            centerAndRadiusOfIntersectionCircle.w = Math.sqrt(r_i);
            return true;
        }
        return false;
    }

    /**
     * Test whether the one sphere with center <code>centerA</code> and square radius <code>radiusSquaredA</code> intersects the other
     * sphere with center <code>centerB</code> and square radius <code>radiusSquaredB</code>, and store the center of the circle of
     * intersection in the <code>(x, y, z)</code> components of the supplied vector and the radius of that circle in the w component.
     * <p>
     * The normal vector of the circle of intersection can simply be obtained by subtracting the center of either sphere from the other.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param centerA
     *              the first sphere's center
     * @param radiusSquaredA
     *              the square of the first sphere's radius
     * @param centerB
     *              the second sphere's center
     * @param radiusSquaredB
     *              the square of the second sphere's radius
     * @param centerAndRadiusOfIntersectionCircle
     *              will hold the center of the circle of intersection in the <code>(x, y, z)</code> components and the radius in the w component
     * @return <code>true</code> iff both spheres intersect; <code>false</code> otherwise
     */
    public static boolean intersectSphereSphere(Vector3dc centerA, double radiusSquaredA, Vector3dc centerB, double radiusSquaredB, Vector4d centerAndRadiusOfIntersectionCircle) {
        return intersectSphereSphere(centerA.x(), centerA.y(), centerA.z(), radiusSquaredA, centerB.x(), centerB.y(), centerB.z(), radiusSquaredB, centerAndRadiusOfIntersectionCircle);
    }

    /**
     * Test whether the given sphere with center <code>(sX, sY, sZ)</code> intersects the triangle given by its three vertices, and if they intersect
     * store the point of intersection into <code>result</code>.
     * <p>
     * This method also returns whether the point of intersection is on one of the triangle's vertices, edges or on the face.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.2.7 "Testing Sphere Against Triangle"
     * 
     * @param sX
     *          the x coordinate of the sphere's center
     * @param sY
     *          the y coordinate of the sphere's center
     * @param sZ
     *          the z coordinate of the sphere's center
     * @param sR
     *          the sphere's radius
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v0Z
     *          the z coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v1Z
     *          the z coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @param v2Z
     *          the z coordinate of the third vertex of the triangle
     * @param result
     *          will hold the point of intersection
     * @return one of {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2},
     *                {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20} or
     *                {@link #POINT_ON_TRIANGLE_FACE} or <code>0</code>
     */
    public static int intersectSphereTriangle(
            double sX, double sY, double sZ, double sR,
            double v0X, double v0Y, double v0Z,
            double v1X, double v1Y, double v1Z,
            double v2X, double v2Y, double v2Z,
            Vector3d result) {
        int closest = findClosestPointOnTriangle(v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, sX, sY, sZ, result);
        double vX = result.x - sX, vY = result.y - sY, vZ = result.z - sZ;
        double dot = vX * vX + vY * vY + vZ * vZ;
        if (dot <= sR * sR) {
            return closest;
        }
        return 0;
    }

    /**
     * Test whether the one sphere with center <code>(aX, aY, aZ)</code> and square radius <code>radiusSquaredA</code> intersects the other
     * sphere with center <code>(bX, bY, bZ)</code> and square radius <code>radiusSquaredB</code>.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param aX
     *              the x coordinate of the first sphere's center
     * @param aY
     *              the y coordinate of the first sphere's center
     * @param aZ
     *              the z coordinate of the first sphere's center
     * @param radiusSquaredA
     *              the square of the first sphere's radius
     * @param bX
     *              the x coordinate of the second sphere's center
     * @param bY
     *              the y coordinate of the second sphere's center
     * @param bZ
     *              the z coordinate of the second sphere's center
     * @param radiusSquaredB
     *              the square of the second sphere's radius
     * @return <code>true</code> iff both spheres intersect; <code>false</code> otherwise
     */
    public static boolean testSphereSphere(
            double aX, double aY, double aZ, double radiusSquaredA,
            double bX, double bY, double bZ, double radiusSquaredB) {
        double dX = bX - aX, dY = bY - aY, dZ = bZ - aZ;
        double distSquared = dX * dX + dY * dY + dZ * dZ;
        double h = 0.5 + (radiusSquaredA - radiusSquaredB) / distSquared;
        double r_i = radiusSquaredA - h * h * distSquared;
        return r_i >= 0.0;
    }

    /**
     * Test whether the one sphere with center <code>centerA</code> and square radius <code>radiusSquaredA</code> intersects the other
     * sphere with center <code>centerB</code> and square radius <code>radiusSquaredB</code>.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param centerA
     *              the first sphere's center
     * @param radiusSquaredA
     *              the square of the first sphere's radius
     * @param centerB
     *              the second sphere's center
     * @param radiusSquaredB
     *              the square of the second sphere's radius
     * @return <code>true</code> iff both spheres intersect; <code>false</code> otherwise
     */
    public static boolean testSphereSphere(Vector3dc centerA, double radiusSquaredA, Vector3dc centerB, double radiusSquaredB) {
        return testSphereSphere(centerA.x(), centerA.y(), centerA.z(), radiusSquaredA, centerB.x(), centerB.y(), centerB.z(), radiusSquaredB);
    }

    /**
     * Determine the signed distance of the given point <code>(pointX, pointY, pointZ)</code> to the plane specified via its general plane equation
     * <i>a*x + b*y + c*z + d = 0</i>.
     * 
     * @param pointX
     *              the x coordinate of the point
     * @param pointY
     *              the y coordinate of the point
     * @param pointZ
     *              the z coordinate of the point
     * @param a
     *              the x factor in the plane equation
     * @param b
     *              the y factor in the plane equation
     * @param c
     *              the z factor in the plane equation
     * @param d
     *              the constant in the plane equation
     * @return the distance between the point and the plane
     */
    public static double distancePointPlane(double pointX, double pointY, double pointZ, double a, double b, double c, double d) {
        double denom = Math.sqrt(a * a + b * b + c * c);
        return (a * pointX + b * pointY + c * pointZ + d) / denom;
    }

    /**
     * Determine the signed distance of the given point <code>(pointX, pointY, pointZ)</code> to the plane of the triangle specified by its three points
     * <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>.
     * <p>
     * If the point lies on the front-facing side of the triangle's plane, that is, if the triangle has counter-clockwise winding order
     * as seen from the point, then this method returns a positive number.
     * 
     * @param pointX
     *          the x coordinate of the point
     * @param pointY
     *          the y coordinate of the point
     * @param pointZ
     *          the z coordinate of the point
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v0Z
     *          the z coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v1Z
     *          the z coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @param v2Z
     *          the z coordinate of the third vertex of the triangle
     * @return the signed distance between the point and the plane of the triangle
     */
    public static double distancePointPlane(double pointX, double pointY, double pointZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z) {
        double v1Y0Y = v1Y - v0Y;
        double v2Z0Z = v2Z - v0Z;
        double v2Y0Y = v2Y - v0Y;
        double v1Z0Z = v1Z - v0Z;
        double v2X0X = v2X - v0X;
        double v1X0X = v1X - v0X;
        double a = v1Y0Y * v2Z0Z - v2Y0Y * v1Z0Z;
        double b = v1Z0Z * v2X0X - v2Z0Z * v1X0X;
        double c = v1X0X * v2Y0Y - v2X0X * v1Y0Y;
        double d = -(a * v0X + b * v0Y + c * v0Z);
        return distancePointPlane(pointX, pointY, pointZ, a, b, c, d);
    }

    /**
     * Test whether the ray with given origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code> intersects the plane
     * containing the given point <code>(pointX, pointY, pointZ)</code> and having the normal <code>(normalX, normalY, normalZ)</code>, and return the
     * value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the plane, because it is either parallel to the plane or its direction points
     * away from the plane or the ray's origin is on the <i>negative</i> side of the plane (i.e. the plane's normal points away from the ray's origin).
     * <p>
     * Reference: <a href="https://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm">https://www.siggraph.org/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param pointX
     *              the x coordinate of a point on the plane
     * @param pointY
     *              the y coordinate of a point on the plane
     * @param pointZ
     *              the z coordinate of a point on the plane
     * @param normalX
     *              the x coordinate of the plane's normal
     * @param normalY
     *              the y coordinate of the plane's normal
     * @param normalZ
     *              the z coordinate of the plane's normal
     * @param epsilon
     *              some small epsilon for when the ray is parallel to the plane
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the plane; <code>-1.0</code> otherwise
     */
    public static double intersectRayPlane(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double pointX, double pointY, double pointZ, double normalX, double normalY, double normalZ, double epsilon) {
        double denom = normalX * dirX + normalY * dirY + normalZ * dirZ;
        if (denom < epsilon) {
            double t = ((pointX - originX) * normalX + (pointY - originY) * normalY + (pointZ - originZ) * normalZ) / denom;
            if (t >= 0.0)
                return t;
        }
        return -1.0;
    }

    /**
     * Test whether the ray with given <code>origin</code> and direction <code>dir</code> intersects the plane
     * containing the given <code>point</code> and having the given <code>normal</code>, and return the
     * value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the plane, because it is either parallel to the plane or its direction points
     * away from the plane or the ray's origin is on the <i>negative</i> side of the plane (i.e. the plane's normal points away from the ray's origin).
     * <p>
     * Reference: <a href="https://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm">https://www.siggraph.org/</a>
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param point
     *              a point on the plane
     * @param normal
     *              the plane's normal
     * @param epsilon
     *              some small epsilon for when the ray is parallel to the plane
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the plane; <code>-1.0</code> otherwise
     */
    public static double intersectRayPlane(Vector3dc origin, Vector3dc dir, Vector3dc point, Vector3dc normal, double epsilon) {
        return intersectRayPlane(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), point.x(), point.y(), point.z(), normal.x(), normal.y(), normal.z(), epsilon);
    }

    /**
     * Test whether the ray with given origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code> intersects the plane
     * given as the general plane equation <i>a*x + b*y + c*z + d = 0</i>, and return the
     * value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the plane, because it is either parallel to the plane or its direction points
     * away from the plane or the ray's origin is on the <i>negative</i> side of the plane (i.e. the plane's normal points away from the ray's origin).
     * <p>
     * Reference: <a href="https://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm">https://www.siggraph.org/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param a
     *              the x factor in the plane equation
     * @param b
     *              the y factor in the plane equation
     * @param c
     *              the z factor in the plane equation
     * @param d
     *              the constant in the plane equation
     * @param epsilon
     *              some small epsilon for when the ray is parallel to the plane
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the plane; <code>-1.0</code> otherwise
     */
    public static double intersectRayPlane(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double a, double b, double c, double d, double epsilon) {
        double denom = a * dirX + b * dirY + c * dirZ;
        if (denom < 0.0) {
            double t = -(a * originX + b * originY + c * originZ + d) / denom;
            if (t >= 0.0)
                return t;
        }
        return -1.0;
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>(minX, minY, minZ)</code> and maximum corner <code>(maxX, maxY, maxZ)</code>
     * intersects the sphere with the given center <code>(centerX, centerY, centerZ)</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     * 
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned box
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned box
     * @param minZ
     *          the z coordinate of the minimum corner of the axis-aligned box
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned box
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned box
     * @param maxZ
     *          the z coordinate of the maximum corner of the axis-aligned box
     * @param centerX
     *          the x coordinate of the sphere's center
     * @param centerY
     *          the y coordinate of the sphere's center
     * @param centerZ
     *          the z coordinate of the sphere's center
     * @param radiusSquared
     *          the square of the sphere's radius
     * @return <code>true</code> iff the axis-aligned box intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testAabSphere(
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ,
            double centerX, double centerY, double centerZ, double radiusSquared) {
        double radius2 = radiusSquared;
        if (centerX < minX) {
            double d = (centerX - minX);
            radius2 -= d * d;
        } else if (centerX > maxX) {
            double d = (centerX - maxX);
            radius2 -= d * d;
        }
        if (centerY < minY) {
            double d = (centerY - minY);
            radius2 -= d * d;
        } else if (centerY > maxY) {
            double d = (centerY - maxY);
            radius2 -= d * d;
        }
        if (centerZ < minZ) {
            double d = (centerZ - minZ);
            radius2 -= d * d;
        } else if (centerZ > maxZ) {
            double d = (centerZ - maxZ);
            radius2 -= d * d;
        }
        return radius2 >= 0.0;
    }

    /**
     * Test whether the axis-aligned box with minimum corner <code>min</code> and maximum corner <code>max</code>
     * intersects the sphere with the given <code>center</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     * 
     * @param min
     *          the minimum corner of the axis-aligned box
     * @param max
     *          the maximum corner of the axis-aligned box
     * @param center
     *          the sphere's center
     * @param radiusSquared
     *          the squared of the sphere's radius
     * @return <code>true</code> iff the axis-aligned box intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testAabSphere(Vector3dc min, Vector3dc max, Vector3dc center, double radiusSquared) {
        return testAabSphere(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), center.x(), center.y(), center.z(), radiusSquared);
    }

    /**
     * Find the point on the given plane which is closest to the specified point <code>(pX, pY, pZ)</code> and store the result in <code>result</code>.
     * 
     * @param aX
     *          the x coordinate of one point on the plane
     * @param aY
     *          the y coordinate of one point on the plane
     * @param aZ
     *          the z coordinate of one point on the plane
     * @param nX
     *          the x coordinate of the unit normal of the plane
     * @param nY
     *          the y coordinate of the unit normal of the plane
     * @param nZ
     *          the z coordinate of the unit normal of the plane
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param pZ
     *          the z coordinate of the point
     * @param result
     *          will hold the result
     * @return result
     */
    public static Vector3d findClosestPointOnPlane(double aX, double aY, double aZ, double nX, double nY, double nZ, double pX, double pY, double pZ, Vector3d result) {
        double d = -(nX * aX + nY * aY + nZ * aZ);
        double t = nX * pX + nY * pY + nZ * pZ - d;
        result.x = pX - t * nX;
        result.y = pY - t * nY;
        result.z = pZ - t * nZ;
        return result;
    }

    /**
     * Find the point on the given line segment which is closest to the specified point <code>(pX, pY, pZ)</code>, and store the result in <code>result</code>.
     * 
     * @param aX
     *          the x coordinate of the first end point of the line segment
     * @param aY
     *          the y coordinate of the first end point of the line segment
     * @param aZ
     *          the z coordinate of the first end point of the line segment
     * @param bX
     *          the x coordinate of the second end point of the line segment
     * @param bY
     *          the y coordinate of the second end point of the line segment
     * @param bZ
     *          the z coordinate of the second end point of the line segment
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param pZ
     *          the z coordinate of the point
     * @param result
     *          will hold the result
     * @return result
     */
    public static Vector3d findClosestPointOnLineSegment(double aX, double aY, double aZ, double bX, double bY, double bZ, double pX, double pY, double pZ, Vector3d result) {
        double abX = bX - aX, abY = bY - aY, abZ = bZ - aZ;
        double t = ((pX - aX) * abX + (pY - aY) * abY + (pZ - aZ) * abZ) / (abX * abX + abY * abY + abZ * abZ);
        if (t < 0.0) t = 0.0;
        if (t > 1.0) t = 1.0;
        result.x = aX + t * abX;
        result.y = aY + t * abY;
        result.z = aZ + t * abZ;
        return result;
    }

    /**
     * Find the closest points on the two line segments, store the point on the first line segment in <code>resultA</code> and 
     * the point on the second line segment in <code>resultB</code>, and return the square distance between both points.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.9 "Closest Points of Two Line Segments"
     * 
     * @param a0X
     *          the x coordinate of the first line segment's first end point
     * @param a0Y
     *          the y coordinate of the first line segment's first end point
     * @param a0Z
     *          the z coordinate of the first line segment's first end point
     * @param a1X
     *          the x coordinate of the first line segment's second end point
     * @param a1Y
     *          the y coordinate of the first line segment's second end point
     * @param a1Z
     *          the z coordinate of the first line segment's second end point
     * @param b0X
     *          the x coordinate of the second line segment's first end point
     * @param b0Y
     *          the y coordinate of the second line segment's first end point
     * @param b0Z
     *          the z coordinate of the second line segment's first end point
     * @param b1X
     *          the x coordinate of the second line segment's second end point
     * @param b1Y
     *          the y coordinate of the second line segment's second end point
     * @param b1Z
     *          the z coordinate of the second line segment's second end point
     * @param resultA
     *          will hold the point on the first line segment
     * @param resultB
     *          will hold the point on the second line segment
     * @return the square distance between the two closest points
     */
    public static double findClosestPointsLineSegments(
            double a0X, double a0Y, double a0Z, double a1X, double a1Y, double a1Z,
            double b0X, double b0Y, double b0Z, double b1X, double b1Y, double b1Z,
            Vector3d resultA, Vector3d resultB) {
        double d1x = a1X - a0X, d1y = a1Y - a0Y, d1z = a1Z - a0Z;
        double d2x = b1X - b0X, d2y = b1Y - b0Y, d2z = b1Z - b0Z;
        double rX = a0X - b0X, rY = a0Y - b0Y, rZ = a0Z - b0Z;
        double a = d1x * d1x + d1y * d1y + d1z * d1z;
        double e = d2x * d2x + d2y * d2y + d2z * d2z;
        double f = d2x * rX + d2y * rY + d2z * rZ;
        double EPSILON = 1E-8;
        double s, t;
        if (a <= EPSILON && e <= EPSILON) {
            // Both segments degenerate into points
            resultA.set(a0X, a0Y, a0Z);
            resultB.set(b0X, b0Y, b0Z);
            return resultA.dot(resultB);
        }
        if (a <= EPSILON) {
            // First segment degenerates into a point
            s = 0.0;
            t = f / e;
            t = Math.min(Math.max(t, 0.0), 1.0);
        } else {
            double c = d1x * rX + d1y * rY + d1z * rZ;
            if (e <= EPSILON) {
                // Second segment degenerates into a point
                t = 0.0;
                s = Math.min(Math.max(-c / a, 0.0), 1.0);
            } else {
                // The general nondegenerate case starts here
                double b = d1x * d2x + d1y * d2y + d1z * d2z;
                double denom = a * e - b * b;
                // If segments not parallel, compute closest point on L1 to L2 and
                // clamp to segment S1. Else pick arbitrary s (here 0)
                if (denom != 0.0)
                    s = Math.min(Math.max((b*f - c*e) / denom, 0.0), 1.0);
                else
                    s = 0.0;
                // Compute point on L2 closest to S1(s) using
                // t = Dot((P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e
                t = (b * s + f) / e;
                // If t in [0,1] done. Else clamp t, recompute s for the new value
                // of t using s = Dot((P2 + D2*t) - P1,D1) / Dot(D1,D1)= (t*b - c) / a
                // and clamp s to [0, 1]
                if (t < 0.0) {
                    t = 0.0;
                    s = Math.min(Math.max(-c / a, 0.0), 1.0);
                } else if (t > 1.0) {
                    t = 1.0;
                    s = Math.min(Math.max((b - c) / a, 0.0), 1.0);
                }
            }
        }
        resultA.set(a0X + d1x * s, a0Y + d1y * s, a0Z + d1z * s);
        resultB.set(b0X + d2x * t, b0Y + d2y * t, b0Z + d2z * t);
        double dX = resultA.x - resultB.x, dY = resultA.y - resultB.y, dZ = resultA.z - resultB.z;
        return dX*dX + dY*dY + dZ*dZ;
    }

    /**
     * Find the closest points on a line segment and a triangle.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.10 "Closest Points of a Line Segment and a Triangle"
     * 
     * @param aX
     *          the x coordinate of the line segment's first end point
     * @param aY
     *          the y coordinate of the line segment's first end point
     * @param aZ
     *          the z coordinate of the line segment's first end point
     * @param bX
     *          the x coordinate of the line segment's second end point
     * @param bY
     *          the y coordinate of the line segment's second end point
     * @param bZ
     *          the z coordinate of the line segment's second end point
     * @param v0X
     *          the x coordinate of the triangle's first vertex
     * @param v0Y
     *          the y coordinate of the triangle's first vertex
     * @param v0Z
     *          the z coordinate of the triangle's first vertex
     * @param v1X
     *          the x coordinate of the triangle's second vertex
     * @param v1Y
     *          the y coordinate of the triangle's second vertex
     * @param v1Z
     *          the z coordinate of the triangle's second vertex
     * @param v2X
     *          the x coordinate of the triangle's third vertex
     * @param v2Y
     *          the y coordinate of the triangle's third vertex
     * @param v2Z
     *          the z coordinate of the triangle's third vertex
     * @param lineSegmentResult
     *          will hold the closest point on the line segment
     * @param triangleResult
     *          will hold the closest point on the triangle
     * @return the square distance of the closest points
     */
    public static double findClosestPointsLineSegmentTriangle(
            double aX, double aY, double aZ, double bX, double bY, double bZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            Vector3d lineSegmentResult, Vector3d triangleResult) {
        double min, d;
        double minlsX, minlsY, minlsZ, mintX, mintY, mintZ;
        // AB -> V0V1
        d = findClosestPointsLineSegments(aX, aY, aZ, bX, bY, bZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, lineSegmentResult, triangleResult);
        min = d;
        minlsX = lineSegmentResult.x; minlsY = lineSegmentResult.y; minlsZ = lineSegmentResult.z;
        mintX = triangleResult.x; mintY = triangleResult.y; mintZ = triangleResult.z;
        // AB -> V1V2
        d = findClosestPointsLineSegments(aX, aY, aZ, bX, bY, bZ, v1X, v1Y, v1Z, v2X, v2Y, v2Z, lineSegmentResult, triangleResult);
        if (d < min) {
            min = d;
            minlsX = lineSegmentResult.x; minlsY = lineSegmentResult.y; minlsZ = lineSegmentResult.z;
            mintX = triangleResult.x; mintY = triangleResult.y; mintZ = triangleResult.z;
        }
        // AB -> V2V0
        d = findClosestPointsLineSegments(aX, aY, aZ, bX, bY, bZ, v2X, v2Y, v2Z, v0X, v0Y, v0Z, lineSegmentResult, triangleResult);
        if (d < min) {
            min = d;
            minlsX = lineSegmentResult.x; minlsY = lineSegmentResult.y; minlsZ = lineSegmentResult.z;
            mintX = triangleResult.x; mintY = triangleResult.y; mintZ = triangleResult.z;
        }
        // segment endpoint A and plane of triangle (when A projects inside V0V1V2)
        boolean computed = false;
        double a = Double.NaN, b = Double.NaN, c = Double.NaN, nd = Double.NaN;
        if (testPointInTriangle(aX, aY, aZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z)) {
            double v1Y0Y = v1Y - v0Y;
            double v2Z0Z = v2Z - v0Z;
            double v2Y0Y = v2Y - v0Y;
            double v1Z0Z = v1Z - v0Z;
            double v2X0X = v2X - v0X;
            double v1X0X = v1X - v0X;
            a = v1Y0Y * v2Z0Z - v2Y0Y * v1Z0Z;
            b = v1Z0Z * v2X0X - v2Z0Z * v1X0X;
            c = v1X0X * v2Y0Y - v2X0X * v1Y0Y;
            computed = true;
            double invLen = Math.invsqrt(a*a + b*b + c*c);
            a *= invLen; b *= invLen; c *= invLen;
            nd = -(a * v0X + b * v0Y + c * v0Z);
            d = (a * aX + b * aY + c * aZ + nd);
            double l = d;
            d *= d;
            if (d < min) {
                min = d;
                minlsX = aX; minlsY = aY; minlsZ = aZ;
                mintX = aX - a*l; mintY = aY - b*l; mintZ = aZ - c*l;
            }
        }
        // segment endpoint B and plane of triangle (when B projects inside V0V1V2)
        if (testPointInTriangle(bX, bY, bZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z)) {
            if (!computed) {
                double v1Y0Y = v1Y - v0Y;
                double v2Z0Z = v2Z - v0Z;
                double v2Y0Y = v2Y - v0Y;
                double v1Z0Z = v1Z - v0Z;
                double v2X0X = v2X - v0X;
                double v1X0X = v1X - v0X;
                a = v1Y0Y * v2Z0Z - v2Y0Y * v1Z0Z;
                b = v1Z0Z * v2X0X - v2Z0Z * v1X0X;
                c = v1X0X * v2Y0Y - v2X0X * v1Y0Y;
                double invLen = Math.invsqrt(a*a + b*b + c*c);
                a *= invLen; b *= invLen; c *= invLen;
                nd = -(a * v0X + b * v0Y + c * v0Z);
            }
            d = (a * bX + b * bY + c * bZ + nd);
            double l = d;
            d *= d;
            if (d < min) {
                min = d;
                minlsX = bX; minlsY = bY; minlsZ = bZ;
                mintX = bX - a*l; mintY = bY - b*l; mintZ = bZ - c*l;
            }
        }
        lineSegmentResult.set(minlsX, minlsY, minlsZ);
        triangleResult.set(mintX, mintY, mintZ);
        return min;
    }

    /**
     * Determine the closest point on the triangle with the given vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code>, <code>(v2X, v2Y, v2Z)</code>
     * between that triangle and the given point <code>(pX, pY, pZ)</code> and store that point into the given <code>result</code>.
     * <p>
     * Additionally, this method returns whether the closest point is a vertex ({@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2})
     * of the triangle, lies on an edge ({@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20})
     * or on the {@link #POINT_ON_TRIANGLE_FACE face} of the triangle.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.5 "Closest Point on Triangle to Point"
     * 
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v0Z
     *          the z coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v1Z
     *          the z coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @param v2Z
     *          the z coordinate of the third vertex of the triangle
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param pZ
     *          the y coordinate of the point
     * @param result
     *          will hold the closest point
     * @return one of {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2},
     *                {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20} or
     *                {@link #POINT_ON_TRIANGLE_FACE}
     */
    public static int findClosestPointOnTriangle(
            double v0X, double v0Y, double v0Z,
            double v1X, double v1Y, double v1Z,
            double v2X, double v2Y, double v2Z,
            double pX, double pY, double pZ,
            Vector3d result) {
        double abX = v1X - v0X, abY = v1Y - v0Y, abZ = v1Z - v0Z;
        double acX = v2X - v0X, acY = v2Y - v0Y, acZ = v2Z - v0Z;
        double apX = pX - v0X, apY = pY - v0Y, apZ = pZ - v0Z;
        double d1 = abX * apX + abY * apY + abZ * apZ;
        double d2 = acX * apX + acY * apY + acZ * apZ;
        if (d1 <= 0.0 && d2 <= 0.0) {
            result.x = v0X;
            result.y = v0Y;
            result.z = v0Z;
            return POINT_ON_TRIANGLE_VERTEX_0;
        }
        double bpX = pX - v1X, bpY = pY - v1Y, bpZ = pZ - v1Z;
        double d3 = abX * bpX + abY * bpY + abZ * bpZ;
        double d4 = acX * bpX + acY * bpY + acZ * bpZ;
        if (d3 >= 0.0 && d4 <= d3) {
            result.x = v1X;
            result.y = v1Y;
            result.z = v1Z;
            return POINT_ON_TRIANGLE_VERTEX_1;
        }
        double vc = d1 * d4 - d3 * d2;
        if (vc <= 0.0 && d1 >= 0.0 && d3 <= 0.0) {
            double v = d1 / (d1 - d3);
            result.x = v0X + v * abX;
            result.y = v0Y + v * abY;
            result.z = v0Z + v * abZ;
            return POINT_ON_TRIANGLE_EDGE_01;
        }
        double cpX = pX - v2X, cpY = pY - v2Y, cpZ = pZ - v2Z;
        double d5 = abX * cpX + abY * cpY + abZ * cpZ;
        double d6 = acX * cpX + acY * cpY + acZ * cpZ;
        if (d6 >= 0.0 && d5 <= d6) {
            result.x = v2X;
            result.y = v2Y;
            result.z = v2Z;
            return POINT_ON_TRIANGLE_VERTEX_2;
        }
        double vb = d5 * d2 - d1 * d6;
        if (vb <= 0.0 && d2 >= 0.0 && d6 <= 0.0) {
            double w = d2 / (d2 - d6);
            result.x = v0X + w * acX;
            result.y = v0Y + w * acY;
            result.z = v0Z + w * acZ;
            return POINT_ON_TRIANGLE_EDGE_20;
        }
        double va = d3 * d6 - d5 * d4;
        if (va <= 0.0 && d4 - d3 >= 0.0 && d5 - d6 >= 0.0) {
            double w = (d4 - d3) / (d4 - d3 + d5 - d6);
            result.x = v1X + w * (v2X - v1X);
            result.y = v1Y + w * (v2Y - v1Y);
            result.z = v1Z + w * (v2Z - v1Z);
            return POINT_ON_TRIANGLE_EDGE_12;
        }
        double denom = 1.0 / (va + vb + vc);
        double v = vb * denom;
        double w = vc * denom;
        result.x = v0X + abX * v + acX * w;
        result.y = v0Y + abY * v + acY * w;
        result.z = v0Z + abZ * v + acZ * w;
        return POINT_ON_TRIANGLE_FACE;
    }

    /**
     * Determine the closest point on the triangle with the vertices <code>v0</code>, <code>v1</code>, <code>v2</code>
     * between that triangle and the given point <code>p</code> and store that point into the given <code>result</code>.
     * <p>
     * Additionally, this method returns whether the closest point is a vertex ({@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2})
     * of the triangle, lies on an edge ({@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20})
     * or on the {@link #POINT_ON_TRIANGLE_FACE face} of the triangle.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.5 "Closest Point on Triangle to Point"
     * 
     * @param v0
     *          the first vertex of the triangle
     * @param v1
     *          the second vertex of the triangle
     * @param v2
     *          the third vertex of the triangle
     * @param p
     *          the point
     * @param result
     *          will hold the closest point
     * @return one of {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2},
     *                {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20} or
     *                {@link #POINT_ON_TRIANGLE_FACE}
     */
    public static int findClosestPointOnTriangle(Vector3dc v0, Vector3dc v1, Vector3dc v2, Vector3dc p, Vector3d result) {
        return findClosestPointOnTriangle(v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), p.x(), p.y(), p.z(), result);
    }

    /**
     * Find the point on a given rectangle, specified via three of its corners, which is closest to the specified point
     * <code>(pX, pY, pZ)</code> and store the result into <code>res</code>.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.4.2 "Closest Point on 3D Rectangle to Point"
     * 
     * @param aX
     *          the x coordinate of the first corner point of the rectangle
     * @param aY
     *          the y coordinate of the first corner point of the rectangle
     * @param aZ
     *          the z coordinate of the first corner point of the rectangle
     * @param bX
     *          the x coordinate of the second corner point of the rectangle 
     * @param bY
     *          the y coordinate of the second corner point of the rectangle
     * @param bZ
     *          the z coordinate of the second corner point of the rectangle
     * @param cX
     *          the x coordinate of the third corner point of the rectangle
     * @param cY
     *          the y coordinate of the third corner point of the rectangle
     * @param cZ
     *          the z coordinate of the third corner point of the rectangle
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param pZ
     *          the z coordinate of the point
     * @param res
     *          will hold the result
     * @return res
     */
    public static Vector3d findClosestPointOnRectangle(
            double aX, double aY, double aZ,
            double bX, double bY, double bZ,
            double cX, double cY, double cZ,
            double pX, double pY, double pZ, Vector3d res) {
        double abX = bX - aX, abY = bY - aY, abZ = bZ - aZ;
        double acX = cX - aX, acY = cY - aY, acZ = cZ - aZ;
        double dX = pX - aX, dY = pY - aY, dZ = pZ - aZ;
        double qX = aX, qY = aY, qZ = aZ;
        double dist = dX * abX + dY * abY + dZ * abZ;
        double maxdist = abX * abX + abY * abY + abZ * abZ;
        if (dist >= maxdist) {
            qX += abX;
            qY += abY;
            qZ += abZ;
        } else if (dist > 0.0) {
            qX += (dist / maxdist) * abX;
            qY += (dist / maxdist) * abY;
            qZ += (dist / maxdist) * abZ;
        }
        dist = dX * acX + dY * acY + dZ * acZ;
        maxdist = acX * acX + acY * acY + acZ * acZ;
        if (dist >= maxdist) {
            qX += acX;
            qY += acY;
            qZ += acZ;
        } else if (dist > 0.0) {
            qX += (dist / maxdist) * acX;
            qY += (dist / maxdist) * acY;
            qZ += (dist / maxdist) * acZ;
        }
        res.x = qX;
        res.y = qY;
        res.z = qZ;
        return res;
    }

    /**
     * Determine the point of intersection between a sphere with the given center <code>(centerX, centerY, centerZ)</code> and <code>radius</code> moving
     * with the given velocity <code>(velX, velY, velZ)</code> and the triangle specified via its three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code>, <code>(v2X, v2Y, v2Z)</code>.
     * <p>
     * The vertices of the triangle must be specified in counter-clockwise winding order.
     * <p>
     * An intersection is only considered if the time of intersection is smaller than the given <code>maxT</code> value.
     * <p>
     * Reference: <a href="http://www.peroxide.dk/papers/collision/collision.pdf">Improved Collision detection and Response</a>
     * 
     * @param centerX
     *              the x coordinate of the sphere's center
     * @param centerY
     *              the y coordinate of the sphere's center
     * @param centerZ
     *              the z coordinate of the sphere's center
     * @param radius
     *              the radius of the sphere
     * @param velX
     *              the x component of the velocity of the sphere
     * @param velY
     *              the y component of the velocity of the sphere
     * @param velZ
     *              the z component of the velocity of the sphere
     * @param v0X
     *              the x coordinate of the first triangle vertex
     * @param v0Y
     *              the y coordinate of the first triangle vertex
     * @param v0Z
     *              the z coordinate of the first triangle vertex
     * @param v1X
     *              the x coordinate of the second triangle vertex
     * @param v1Y
     *              the y coordinate of the second triangle vertex
     * @param v1Z
     *              the z coordinate of the second triangle vertex
     * @param v2X
     *              the x coordinate of the third triangle vertex
     * @param v2Y
     *              the y coordinate of the third triangle vertex
     * @param v2Z
     *              the z coordinate of the third triangle vertex
     * @param epsilon
     *              a small epsilon when testing spheres that move almost parallel to the triangle
     * @param maxT
     *              the maximum intersection time
     * @param pointAndTime
     *              iff the moving sphere and the triangle intersect, this will hold the point of intersection in the <code>(x, y, z)</code> components
     *              and the time of intersection in the <code>w</code> component
     * @return {@link #POINT_ON_TRIANGLE_FACE} if the intersection point lies on the triangle's face,
     *         or {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1} or {@link #POINT_ON_TRIANGLE_VERTEX_2} if the intersection point is a vertex,
     *         or {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12} or {@link #POINT_ON_TRIANGLE_EDGE_20} if the intersection point lies on an edge;
     *         or <code>0</code> if no intersection
     */
    public static int intersectSweptSphereTriangle(
            double centerX, double centerY, double centerZ, double radius, double velX, double velY, double velZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon, double maxT, Vector4d pointAndTime) {
        double v10X = v1X - v0X;
        double v10Y = v1Y - v0Y;
        double v10Z = v1Z - v0Z;
        double v20X = v2X - v0X;
        double v20Y = v2Y - v0Y;
        double v20Z = v2Z - v0Z;
        // build triangle plane
        double a = v10Y * v20Z - v20Y * v10Z;
        double b = v10Z * v20X - v20Z * v10X;
        double c = v10X * v20Y - v20X * v10Y;
        double d = -(a * v0X + b * v0Y + c * v0Z);
        double invLen = Math.invsqrt(a * a + b * b + c * c);
        double signedDist = (a * centerX + b * centerY + c * centerZ + d) * invLen;
        double dot = (a * velX + b * velY + c * velZ) * invLen;
        if (dot < epsilon && dot > -epsilon)
            return 0;
        double pt0 = (radius - signedDist) / dot;
        if (pt0 > maxT)
            return 0;
        double pt1 = (-radius - signedDist) / dot;
        double p0X = centerX - radius * a * invLen + velX * pt0;
        double p0Y = centerY - radius * b * invLen + velY * pt0;
        double p0Z = centerZ - radius * c * invLen + velZ * pt0;
        boolean insideTriangle = testPointInTriangle(p0X, p0Y, p0Z, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z);
        if (insideTriangle) {
            pointAndTime.x = p0X;
            pointAndTime.y = p0Y;
            pointAndTime.z = p0Z;
            pointAndTime.w = pt0;
            return POINT_ON_TRIANGLE_FACE;
        }
        int isect = 0;
        double t0 = maxT;
        double A = velX * velX + velY * velY + velZ * velZ;
        double radius2 = radius * radius;
        // test against v0
        double centerV0X = centerX - v0X;
        double centerV0Y = centerY - v0Y;
        double centerV0Z = centerZ - v0Z;
        double B0 = 2.0 * (velX * centerV0X + velY * centerV0Y + velZ * centerV0Z);
        double C0 = centerV0X * centerV0X + centerV0Y * centerV0Y + centerV0Z * centerV0Z - radius2;
        double root0 = computeLowestRoot(A, B0, C0, t0);
        if (root0 < t0) {
            pointAndTime.x = v0X;
            pointAndTime.y = v0Y;
            pointAndTime.z = v0Z;
            pointAndTime.w = root0;
            t0 = root0;
            isect = POINT_ON_TRIANGLE_VERTEX_0;
        }
        // test against v1
        double centerV1X = centerX - v1X;
        double centerV1Y = centerY - v1Y;
        double centerV1Z = centerZ - v1Z;
        double centerV1Len = centerV1X * centerV1X + centerV1Y * centerV1Y + centerV1Z * centerV1Z;
        double B1 = 2.0 * (velX * centerV1X + velY * centerV1Y + velZ * centerV1Z);
        double C1 = centerV1Len - radius2;
        double root1 = computeLowestRoot(A, B1, C1, t0);
        if (root1 < t0) {
            pointAndTime.x = v1X;
            pointAndTime.y = v1Y;
            pointAndTime.z = v1Z;
            pointAndTime.w = root1;
            t0 = root1;
            isect = POINT_ON_TRIANGLE_VERTEX_1;
        }
        // test against v2
        double centerV2X = centerX - v2X;
        double centerV2Y = centerY - v2Y;
        double centerV2Z = centerZ - v2Z;
        double B2 = 2.0 * (velX * centerV2X + velY * centerV2Y + velZ * centerV2Z);
        double C2 = centerV2X * centerV2X + centerV2Y * centerV2Y + centerV2Z * centerV2Z - radius2;
        double root2 = computeLowestRoot(A, B2, C2, t0);
        if (root2 < t0) {
            pointAndTime.x = v2X;
            pointAndTime.y = v2Y;
            pointAndTime.z = v2Z;
            pointAndTime.w = root2;
            t0 = root2;
            isect = POINT_ON_TRIANGLE_VERTEX_2;
        }
        double velLen = velX * velX + velY * velY + velZ * velZ;
        // test against edge10
        double len10 = v10X * v10X + v10Y * v10Y + v10Z * v10Z;
        double baseTo0Len = centerV0X * centerV0X + centerV0Y * centerV0Y + centerV0Z * centerV0Z;
        double v10Vel = (v10X * velX + v10Y * velY + v10Z * velZ);
        double A10 = len10 * -velLen + v10Vel * v10Vel;
        double v10BaseTo0 = v10X * -centerV0X + v10Y * -centerV0Y + v10Z * -centerV0Z;
        double velBaseTo0 = velX * -centerV0X + velY * -centerV0Y + velZ * -centerV0Z;
        double B10 = len10 * 2 * velBaseTo0 - 2 * v10Vel * v10BaseTo0;
        double C10 = len10 * (radius2 - baseTo0Len) + v10BaseTo0 * v10BaseTo0;
        double root10 = computeLowestRoot(A10, B10, C10, t0);
        double f10 = (v10Vel * root10 - v10BaseTo0) / len10;
        if (f10 >= 0.0 && f10 <= 1.0 && root10 < t0) {
            pointAndTime.x = v0X + f10 * v10X;
            pointAndTime.y = v0Y + f10 * v10Y;
            pointAndTime.z = v0Z + f10 * v10Z;
            pointAndTime.w = root10;
            t0 = root10;
            isect = POINT_ON_TRIANGLE_EDGE_01;
        }
        // test against edge20
        double len20 = v20X * v20X + v20Y * v20Y + v20Z * v20Z;
        double v20Vel = (v20X * velX + v20Y * velY + v20Z * velZ);
        double A20 = len20 * -velLen + v20Vel * v20Vel;
        double v20BaseTo0 = v20X * -centerV0X + v20Y * -centerV0Y + v20Z * -centerV0Z;
        double B20 = len20 * 2 * velBaseTo0 - 2 * v20Vel * v20BaseTo0;
        double C20 = len20 * (radius2 - baseTo0Len) + v20BaseTo0 * v20BaseTo0;
        double root20 = computeLowestRoot(A20, B20, C20, t0);
        double f20 = (v20Vel * root20 - v20BaseTo0) / len20;
        if (f20 >= 0.0 && f20 <= 1.0 && root20 < pt1) {
            pointAndTime.x = v0X + f20 * v20X;
            pointAndTime.y = v0Y + f20 * v20Y;
            pointAndTime.z = v0Z + f20 * v20Z;
            pointAndTime.w = root20;
            t0 = root20;
            isect = POINT_ON_TRIANGLE_EDGE_20;
        }
        // test against edge21
        double v21X = v2X - v1X;
        double v21Y = v2Y - v1Y;
        double v21Z = v2Z - v1Z;
        double len21 = v21X * v21X + v21Y * v21Y + v21Z * v21Z;
        double baseTo1Len = centerV1Len;
        double v21Vel = (v21X * velX + v21Y * velY + v21Z * velZ);
        double A21 = len21 * -velLen + v21Vel * v21Vel;
        double v21BaseTo1 = v21X * -centerV1X + v21Y * -centerV1Y + v21Z * -centerV1Z;
        double velBaseTo1 = velX * -centerV1X + velY * -centerV1Y + velZ * -centerV1Z;
        double B21 = len21 * 2 * velBaseTo1 - 2 * v21Vel * v21BaseTo1;
        double C21 = len21 * (radius2 - baseTo1Len) + v21BaseTo1 * v21BaseTo1;
        double root21 = computeLowestRoot(A21, B21, C21, t0);
        double f21 = (v21Vel * root21 - v21BaseTo1) / len21;
        if (f21 >= 0.0 && f21 <= 1.0 && root21 < t0) {
            pointAndTime.x = v1X + f21 * v21X;
            pointAndTime.y = v1Y + f21 * v21Y;
            pointAndTime.z = v1Z + f21 * v21Z;
            pointAndTime.w = root21;
            t0 = root21;
            isect = POINT_ON_TRIANGLE_EDGE_12;
        }
        return isect;
    }

    /**
     * Compute the lowest root for <code>t</code> in the quadratic equation <code>a*t*t + b*t + c = 0</code>.
     * <p>
     * This is a helper method for {@link #intersectSweptSphereTriangle}
     * 
     * @param a
     *              the quadratic factor
     * @param b
     *              the linear factor
     * @param c
     *              the constant
     * @param maxR
     *              the maximum expected root
     * @return the lowest of the two roots of the quadratic equation; or {@link Double#POSITIVE_INFINITY}
     */
    private static double computeLowestRoot(double a, double b, double c, double maxR) {
        double determinant = b * b - 4.0 * a * c;
        if (determinant < 0.0)
            return Double.POSITIVE_INFINITY;
        double sqrtD = Math.sqrt(determinant);
        double r1 = (-b - sqrtD) / (2.0 * a);
        double r2 = (-b + sqrtD) / (2.0 * a);
        if (r1 > r2) {
            double temp = r2;
            r2 = r1;
            r1 = temp;
        }
        if (r1 > 0.0 && r1 < maxR) {
            return r1;
        }
        if (r2 > 0.0 && r2 < maxR) {
            return r2;
        }
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Test whether the projection of the given point <code>(pX, pY, pZ)</code> lies inside of the triangle defined by the three vertices
     * <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>.
     * <p>
     * Reference: <a href="http://www.peroxide.dk/papers/collision/collision.pdf">Improved Collision detection and Response</a>
     * 
     * @param pX
     *              the x coordinate of the point to test
     * @param pY
     *              the y coordinate of the point to test
     * @param pZ
     *              the z coordinate of the point to test
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @return <code>true</code> if the projection of the given point lies inside of the given triangle; <code>false</code> otherwise
     */
    public static boolean testPointInTriangle(double pX, double pY, double pZ, double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z) {
        double e10X = v1X - v0X;
        double e10Y = v1Y - v0Y;
        double e10Z = v1Z - v0Z;
        double e20X = v2X - v0X;
        double e20Y = v2Y - v0Y;
        double e20Z = v2Z - v0Z;
        double a = e10X * e10X + e10Y * e10Y + e10Z * e10Z;
        double b = e10X * e20X + e10Y * e20Y + e10Z * e20Z;
        double c = e20X * e20X + e20Y * e20Y + e20Z * e20Z;
        double ac_bb = a * c - b * b;
        double vpX = pX - v0X;
        double vpY = pY - v0Y;
        double vpZ = pZ - v0Z;
        double d = vpX * e10X + vpY * e10Y + vpZ * e10Z;
        double e = vpX * e20X + vpY * e20Y + vpZ * e20Z;
        double x = d * c - e * b;
        double y = e * a - d * b;
        double z = x + y - ac_bb;
        return ((Runtime.doubleToLongBits(z) & ~(Runtime.doubleToLongBits(x) | Runtime.doubleToLongBits(y))) & 0x8000000000000000L) != 0L;
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and normalized direction <code>(dirX, dirY, dirZ)</code>
     * intersects the given sphere with center <code>(centerX, centerY, centerZ)</code> and square radius <code>radiusSquared</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> for both points (near
     * and far) of intersections into the given <code>result</code> vector.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the sphere.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's normalized direction
     * @param dirY
     *              the y coordinate of the ray's normalized direction
     * @param dirZ
     *              the z coordinate of the ray's normalized direction
     * @param centerX
     *              the x coordinate of the sphere's center
     * @param centerY
     *              the y coordinate of the sphere's center
     * @param centerZ
     *              the z coordinate of the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @param result
     *              a vector that will contain the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> for both points (near, far) of intersections with the sphere
     * @return <code>true</code> if the ray intersects the sphere; <code>false</code> otherwise
     */
    public static boolean intersectRaySphere(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double centerX, double centerY, double centerZ, double radiusSquared, Vector2d result) {
        double Lx = centerX - originX;
        double Ly = centerY - originY;
        double Lz = centerZ - originZ;
        double tca = Lx * dirX + Ly * dirY + Lz * dirZ;
        double d2 = Lx * Lx + Ly * Ly + Lz * Lz - tca * tca;
        if (d2 > radiusSquared)
            return false;
        double thc = Math.sqrt(radiusSquared - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;
        if (t0 < t1 && t1 >= 0.0) {
            result.x = t0;
            result.y = t1;
            return true;
        }
        return false;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and normalized direction <code>dir</code>
     * intersects the sphere with the given <code>center</code> and square radius <code>radiusSquared</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> for both points (near
     * and far) of intersections into the given <code>result</code> vector.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the sphere.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's normalized direction
     * @param center
     *              the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @param result
     *              a vector that will contain the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> for both points (near, far) of intersections with the sphere
     * @return <code>true</code> if the ray intersects the sphere; <code>false</code> otherwise
     */
    public static boolean intersectRaySphere(Vector3dc origin, Vector3dc dir, Vector3dc center, double radiusSquared, Vector2d result) {
        return intersectRaySphere(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), center.x(), center.y(), center.z(), radiusSquared, result);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and normalized direction <code>(dirX, dirY, dirZ)</code>
     * intersects the given sphere with center <code>(centerX, centerY, centerZ)</code> and square radius <code>radiusSquared</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the sphere.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's normalized direction
     * @param dirY
     *              the y coordinate of the ray's normalized direction
     * @param dirZ
     *              the z coordinate of the ray's normalized direction
     * @param centerX
     *              the x coordinate of the sphere's center
     * @param centerY
     *              the y coordinate of the sphere's center
     * @param centerZ
     *              the z coordinate of the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @return <code>true</code> if the ray intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testRaySphere(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double centerX, double centerY, double centerZ, double radiusSquared) {
        double Lx = centerX - originX;
        double Ly = centerY - originY;
        double Lz = centerZ - originZ;
        double tca = Lx * dirX + Ly * dirY + Lz * dirZ;
        double d2 = Lx * Lx + Ly * Ly + Lz * Lz - tca * tca;
        if (d2 > radiusSquared)
            return false;
        double thc = Math.sqrt(radiusSquared - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;
        return t0 < t1 && t1 >= 0.0;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and normalized direction <code>dir</code>
     * intersects the sphere with the given <code>center</code> and square radius.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the sphere.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's normalized direction
     * @param center
     *              the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @return <code>true</code> if the ray intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testRaySphere(Vector3dc origin, Vector3dc dir, Vector3dc center, double radiusSquared) {
        return testRaySphere(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), center.x(), center.y(), center.z(), radiusSquared);
    }

    /**
     * Test whether the line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects the given sphere with center <code>(centerX, centerY, centerZ)</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://paulbourke.net/geometry/circlesphere/index.html#linesphere">http://paulbourke.net/</a>
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p0Z
     *              the z coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param p1Z
     *              the z coordinate of the line segment's second end point
     * @param centerX
     *              the x coordinate of the sphere's center
     * @param centerY
     *              the y coordinate of the sphere's center
     * @param centerZ
     *              the z coordinate of the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @return <code>true</code> if the line segment intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testLineSegmentSphere(double p0X, double p0Y, double p0Z, double p1X, double p1Y, double p1Z,
            double centerX, double centerY, double centerZ, double radiusSquared) {
        double dX = p1X - p0X;
        double dY = p1Y - p0Y;
        double dZ = p1Z - p0Z;
        double nom = (centerX - p0X) * dX + (centerY - p0Y) * dY + (centerZ - p0Z) * dZ;
        double den = dX * dX + dY * dY + dZ * dZ;
        double u = nom / den;
        if (u < 0.0) {
            dX = p0X - centerX;
            dY = p0Y - centerY;
            dZ = p0Z - centerZ;
        } else if (u > 1.0) {
            dX = p1X - centerX;
            dY = p1Y - centerY;
            dZ = p1Z - centerZ;
        } else { // has to be >= 0 and <= 1
            double pX = p0X + u * dX;
            double pY = p0Y + u * dY;
            double pZ = p0Z + u * dZ;
            dX = pX - centerX;
            dY = pY - centerY;
            dZ = pZ - centerZ;
        }
        double dist = dX * dX + dY * dY + dZ * dZ;
        return dist <= radiusSquared;
    }

    /**
     * Test whether the line segment with the end points <code>p0</code> and <code>p1</code>
     * intersects the given sphere with center <code>center</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://paulbourke.net/geometry/circlesphere/index.html#linesphere">http://paulbourke.net/</a>
     * 
     * @param p0
     *              the line segment's first end point
     * @param p1
     *              the line segment's second end point
     * @param center
     *              the sphere's center
     * @param radiusSquared
     *              the sphere radius squared
     * @return <code>true</code> if the line segment intersects the sphere; <code>false</code> otherwise
     */
    public static boolean testLineSegmentSphere(Vector3dc p0, Vector3dc p1, Vector3dc center, double radiusSquared) {
        return testLineSegmentSphere(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), center.x(), center.y(), center.z(), radiusSquared);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the axis-aligned box given as its minimum corner <code>(minX, minY, minZ)</code> and maximum corner <code>(maxX, maxY, maxZ)</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned box.
     * <p>
     * If many boxes need to be tested against the same ray, then the {@link RayAabIntersection} class is likely more efficient.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectRayAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)
     * @see RayAabIntersection
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param minX
     *              the x coordinate of the minimum corner of the axis-aligned box
     * @param minY
     *              the y coordinate of the minimum corner of the axis-aligned box
     * @param minZ
     *              the z coordinate of the minimum corner of the axis-aligned box
     * @param maxX
     *              the x coordinate of the maximum corner of the axis-aligned box
     * @param maxY
     *              the y coordinate of the maximum corner of the axis-aligned box
     * @param maxZ
     *              the y coordinate of the maximum corner of the axis-aligned box
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     *              iff the ray intersects the axis-aligned box
     * @return <code>true</code> if the given ray intersects the axis-aligned box; <code>false</code> otherwise
     */
    public static boolean intersectRayAab(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vector2d result) {
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY, invDirZ = 1.0 / dirZ;
        double tNear, tFar, tymin, tymax, tzmin, tzmax;
        if (invDirX >= 0.0) {
            tNear = (minX - originX) * invDirX;
            tFar = (maxX - originX) * invDirX;
        } else {
            tNear = (maxX - originX) * invDirX;
            tFar = (minX - originX) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - originY) * invDirY;
            tymax = (maxY - originY) * invDirY;
        } else {
            tymin = (maxY - originY) * invDirY;
            tymax = (minY - originY) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return false;
        if (invDirZ >= 0.0) {
            tzmin = (minZ - originZ) * invDirZ;
            tzmax = (maxZ - originZ) * invDirZ;
        } else {
            tzmin = (maxZ - originZ) * invDirZ;
            tzmax = (minZ - originZ) * invDirZ;
        }
        if (tNear > tzmax || tzmin > tFar)
            return false;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        tNear = tzmin > tNear ? tzmin : tNear;
        tFar = tzmax < tFar ? tzmax : tFar;
        if (tNear < tFar && tFar >= 0.0) {
            result.x = tNear;
            result.y = tFar;
            return true;
        }
        return false;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the axis-aligned box specified as its minimum corner <code>min</code> and maximum corner <code>max</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection..
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned box.
     * <p>
     * If many boxes need to be tested against the same ray, then the {@link RayAabIntersection} class is likely more efficient.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectRayAab(double, double, double, double, double, double, double, double, double, double, double, double, Vector2d)
     * @see RayAabIntersection
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param min
     *              the minimum corner of the axis-aligned box
     * @param max
     *              the maximum corner of the axis-aligned box
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     *              iff the ray intersects the axis-aligned box
     * @return <code>true</code> if the given ray intersects the axis-aligned box; <code>false</code> otherwise
     */
    public static boolean intersectRayAab(Vector3dc origin, Vector3dc dir, Vector3dc min, Vector3dc max, Vector2d result) {
        return intersectRayAab(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), result);
    }

    /**
     * Determine whether the undirected line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects the axis-aligned box given as its minimum corner <code>(minX, minY, minZ)</code> and maximum corner <code>(maxX, maxY, maxZ)</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a line segment whose either end point lies inside the axis-aligned box.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p0Z
     *              the z coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param p1Z
     *              the z coordinate of the line segment's second end point
     * @param minX
     *              the x coordinate of one corner of the axis-aligned box
     * @param minY
     *              the y coordinate of one corner of the axis-aligned box
     * @param minZ
     *              the z coordinate of one corner of the axis-aligned box
     * @param maxX
     *              the x coordinate of the opposite corner of the axis-aligned box
     * @param maxY
     *              the y coordinate of the opposite corner of the axis-aligned box
     * @param maxZ
     *              the y coordinate of the opposite corner of the axis-aligned box
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *              iff the line segment intersects the axis-aligned box
     * @return {@link #INSIDE} if the line segment lies completely inside of the axis-aligned box; or
     *         {@link #OUTSIDE} if the line segment lies completely outside of the axis-aligned box; or
     *         {@link #ONE_INTERSECTION} if one of the end points of the line segment lies inside of the axis-aligned box; or
     *         {@link #TWO_INTERSECTION} if the line segment intersects two sides of the axis-aligned box
     *         or lies on an edge or a side of the box
     */
    public static int intersectLineSegmentAab(double p0X, double p0Y, double p0Z, double p1X, double p1Y, double p1Z,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vector2d result) {
        double dirX = p1X - p0X, dirY = p1Y - p0Y, dirZ = p1Z - p0Z;
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY, invDirZ = 1.0 / dirZ;
        double tNear, tFar, tymin, tymax, tzmin, tzmax;
        if (invDirX >= 0.0) {
            tNear = (minX - p0X) * invDirX;
            tFar = (maxX - p0X) * invDirX;
        } else {
            tNear = (maxX - p0X) * invDirX;
            tFar = (minX - p0X) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - p0Y) * invDirY;
            tymax = (maxY - p0Y) * invDirY;
        } else {
            tymin = (maxY - p0Y) * invDirY;
            tymax = (minY - p0Y) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return OUTSIDE;
        if (invDirZ >= 0.0) {
            tzmin = (minZ - p0Z) * invDirZ;
            tzmax = (maxZ - p0Z) * invDirZ;
        } else {
            tzmin = (maxZ - p0Z) * invDirZ;
            tzmax = (minZ - p0Z) * invDirZ;
        }
        if (tNear > tzmax || tzmin > tFar)
            return OUTSIDE;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        tNear = tzmin > tNear ? tzmin : tNear;
        tFar = tzmax < tFar ? tzmax : tFar;
        int type = OUTSIDE;
        if (tNear <= tFar && tNear <= 1.0f && tFar >= 0.0f) {
            if (tNear >= 0.0f && tFar > 1.0f) {
                tFar = tNear;
                type = ONE_INTERSECTION;
            } else if (tNear < 0.0f && tFar <= 1.0f) {
                tNear = tFar;
                type = ONE_INTERSECTION;
            } else if (tNear < 0.0f && tFar > 1.0f) {
                type = INSIDE;
            } else {
                type = TWO_INTERSECTION;
            }
            result.x = tNear;
            result.y = tFar;
        }
        return type;
    }

    /**
     * Determine whether the undirected line segment with the end points <code>p0</code> and <code>p1</code>
     * intersects the axis-aligned box given as its minimum corner <code>min</code> and maximum corner <code>max</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a line segment whose either end point lies inside the axis-aligned box.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectLineSegmentAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector2d)
     * 
     * @param p0
     *              the line segment's first end point
     * @param p1
     *              the line segment's second end point
     * @param min
     *              the minimum corner of the axis-aligned box
     * @param max
     *              the maximum corner of the axis-aligned box
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *              iff the line segment intersects the axis-aligned box
     * @return {@link #INSIDE} if the line segment lies completely inside of the axis-aligned box; or
     *         {@link #OUTSIDE} if the line segment lies completely outside of the axis-aligned box; or
     *         {@link #ONE_INTERSECTION} if one of the end points of the line segment lies inside of the axis-aligned box; or
     *         {@link #TWO_INTERSECTION} if the line segment intersects two sides of the axis-aligned box
     *         or lies on an edge or a side of the box
     */
    public static int intersectLineSegmentAab(Vector3dc p0, Vector3dc p1, Vector3dc min, Vector3dc max, Vector2d result) {
        return intersectLineSegmentAab(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), result);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the axis-aligned box given as its minimum corner <code>(minX, minY, minZ)</code> and maximum corner <code>(maxX, maxY, maxZ)</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned box.
     * <p>
     * If many boxes need to be tested against the same ray, then the {@link RayAabIntersection} class is likely more efficient.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #testRayAab(Vector3dc, Vector3dc, Vector3dc, Vector3dc)
     * @see RayAabIntersection
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param minX
     *              the x coordinate of the minimum corner of the axis-aligned box
     * @param minY
     *              the y coordinate of the minimum corner of the axis-aligned box
     * @param minZ
     *              the z coordinate of the minimum corner of the axis-aligned box
     * @param maxX
     *              the x coordinate of the maximum corner of the axis-aligned box
     * @param maxY
     *              the y coordinate of the maximum corner of the axis-aligned box
     * @param maxZ
     *              the y coordinate of the maximum corner of the axis-aligned box
     * @return <code>true</code> if the given ray intersects the axis-aligned box; <code>false</code> otherwise
     */
    public static boolean testRayAab(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY, invDirZ = 1.0 / dirZ;
        double tNear, tFar, tymin, tymax, tzmin, tzmax;
        if (invDirX >= 0.0) {
            tNear = (minX - originX) * invDirX;
            tFar = (maxX - originX) * invDirX;
        } else {
            tNear = (maxX - originX) * invDirX;
            tFar = (minX - originX) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - originY) * invDirY;
            tymax = (maxY - originY) * invDirY;
        } else {
            tymin = (maxY - originY) * invDirY;
            tymax = (minY - originY) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return false;
        if (invDirZ >= 0.0) {
            tzmin = (minZ - originZ) * invDirZ;
            tzmax = (maxZ - originZ) * invDirZ;
        } else {
            tzmin = (maxZ - originZ) * invDirZ;
            tzmax = (minZ - originZ) * invDirZ;
        }
        if (tNear > tzmax || tzmin > tFar)
            return false;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        tNear = tzmin > tNear ? tzmin : tNear;
        tFar = tzmax < tFar ? tzmax : tFar;
        return tNear < tFar && tFar >= 0.0;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the axis-aligned box specified as its minimum corner <code>min</code> and maximum corner <code>max</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned box.
     * <p>
     * If many boxes need to be tested against the same ray, then the {@link RayAabIntersection} class is likely more efficient.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *  
     * @see #testRayAab(double, double, double, double, double, double, double, double, double, double, double, double)
     * @see RayAabIntersection
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param min
     *              the minimum corner of the axis-aligned box
     * @param max
     *              the maximum corner of the axis-aligned box
     * @return <code>true</code> if the given ray intersects the axis-aligned box; <code>false</code> otherwise
     */
    public static boolean testRayAab(Vector3dc origin, Vector3dc dir, Vector3dc min, Vector3dc max) {
        return testRayAab(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the frontface of the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test implements backface culling, that is, it will return <code>false</code> when the triangle is in clockwise
     * winding order assuming a <i>right-handed</i> coordinate system when seen along the ray's direction, even if the ray intersects the triangle.
     * This is in compliance with how OpenGL handles backface culling with default frontface/backface settings.
     * 
     * @see #testRayTriangleFront(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return <code>true</code> if the given ray intersects the frontface of the triangle; <code>false</code> otherwise
     */
    public static boolean testRayTriangleFront(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon) {
        double edge1X = v1X - v0X;
        double edge1Y = v1Y - v0Y;
        double edge1Z = v1Z - v0Z;
        double edge2X = v2X - v0X;
        double edge2Y = v2Y - v0Y;
        double edge2Z = v2Z - v0Z;
        double pvecX = dirY * edge2Z - dirZ * edge2Y;
        double pvecY = dirZ * edge2X - dirX * edge2Z;
        double pvecZ = dirX * edge2Y - dirY * edge2X;
        double det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ;
        if (det < epsilon)
            return false;
        double tvecX = originX - v0X;
        double tvecY = originY - v0Y;
        double tvecZ = originZ - v0Z;
        double u = (tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ);
        if (u < 0.0 || u > det)
            return false;
        double qvecX = tvecY * edge1Z - tvecZ * edge1Y;
        double qvecY = tvecZ * edge1X - tvecX * edge1Z;
        double qvecZ = tvecX * edge1Y - tvecY * edge1X;
        double v = (dirX * qvecX + dirY * qvecY + dirZ * qvecZ);
        if (v < 0.0 || u + v > det)
            return false;
        double invDet = 1.0 / det;
        double t = (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet;
        return t >= epsilon;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and the given <code>dir</code> intersects the frontface of the triangle consisting of the three vertices
     * <code>v0</code>, <code>v1</code> and <code>v2</code>.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test implements backface culling, that is, it will return <code>false</code> when the triangle is in clockwise
     * winding order assuming a <i>right-handed</i> coordinate system when seen along the ray's direction, even if the ray intersects the triangle.
     * This is in compliance with how OpenGL handles backface culling with default frontface/backface settings.
     * 
     * @see #testRayTriangleFront(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return <code>true</code> if the given ray intersects the frontface of the triangle; <code>false</code> otherwise
     */
    public static boolean testRayTriangleFront(Vector3dc origin, Vector3dc dir, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon) {
        return testRayTriangleFront(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test does not take into account the winding order of the triangle, so a ray will intersect a front-facing triangle as well as a back-facing triangle.
     * 
     * @see #testRayTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return <code>true</code> if the given ray intersects the frontface of the triangle; <code>false</code> otherwise
     */
    public static boolean testRayTriangle(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon) {
        double edge1X = v1X - v0X;
        double edge1Y = v1Y - v0Y;
        double edge1Z = v1Z - v0Z;
        double edge2X = v2X - v0X;
        double edge2Y = v2Y - v0Y;
        double edge2Z = v2Z - v0Z;
        double pvecX = dirY * edge2Z - dirZ * edge2Y;
        double pvecY = dirZ * edge2X - dirX * edge2Z;
        double pvecZ = dirX * edge2Y - dirY * edge2X;
        double det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ;
        if (det > -epsilon && det < epsilon)
            return false;
        double tvecX = originX - v0X;
        double tvecY = originY - v0Y;
        double tvecZ = originZ - v0Z;
        double invDet = 1.0 / det;
        double u = (tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ) * invDet;
        if (u < 0.0 || u > 1.0)
            return false;
        double qvecX = tvecY * edge1Z - tvecZ * edge1Y;
        double qvecY = tvecZ * edge1X - tvecX * edge1Z;
        double qvecZ = tvecX * edge1Y - tvecY * edge1X;
        double v = (dirX * qvecX + dirY * qvecY + dirZ * qvecZ) * invDet;
        if (v < 0.0 || u + v > 1.0)
            return false;
        double t = (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet;
        return t >= epsilon;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and the given <code>dir</code> intersects the frontface of the triangle consisting of the three vertices
     * <code>v0</code>, <code>v1</code> and <code>v2</code>.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test does not take into account the winding order of the triangle, so a ray will intersect a front-facing triangle as well as a back-facing triangle.
     * 
     * @see #testRayTriangle(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return <code>true</code> if the given ray intersects the frontface of the triangle; <code>false</code> otherwise
     */
    public static boolean testRayTriangle(Vector3dc origin, Vector3dc dir, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon) {
        return testRayTriangle(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon);
    }

    /**
     * Determine whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the frontface of the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>
     * and return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test implements backface culling, that is, it will return <code>false</code> when the triangle is in clockwise
     * winding order assuming a <i>right-handed</i> coordinate system when seen along the ray's direction, even if the ray intersects the triangle.
     * This is in compliance with how OpenGL handles backface culling with default frontface/backface settings.
     * 
     * @see #testRayTriangleFront(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection
     *         if the ray intersects the frontface of the triangle; <code>-1.0</code> otherwise
     */
    public static double intersectRayTriangleFront(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon) {
        double edge1X = v1X - v0X;
        double edge1Y = v1Y - v0Y;
        double edge1Z = v1Z - v0Z;
        double edge2X = v2X - v0X;
        double edge2Y = v2Y - v0Y;
        double edge2Z = v2Z - v0Z;
        double pvecX = dirY * edge2Z - dirZ * edge2Y;
        double pvecY = dirZ * edge2X - dirX * edge2Z;
        double pvecZ = dirX * edge2Y - dirY * edge2X;
        double det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ;
        if (det <= epsilon)
            return -1.0;
        double tvecX = originX - v0X;
        double tvecY = originY - v0Y;
        double tvecZ = originZ - v0Z;
        double u = tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ;
        if (u < 0.0 || u > det)
            return -1.0;
        double qvecX = tvecY * edge1Z - tvecZ * edge1Y;
        double qvecY = tvecZ * edge1X - tvecX * edge1Z;
        double qvecZ = tvecX * edge1Y - tvecY * edge1X;
        double v = dirX * qvecX + dirY * qvecY + dirZ * qvecZ;
        if (v < 0.0 || u + v > det)
            return -1.0;
        double invDet = 1.0 / det;
        double t = (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet;
        return t;
    }

    /**
     * Determine whether the ray with the given <code>origin</code> and the given <code>dir</code> intersects the frontface of the triangle consisting of the three vertices
     * <code>v0</code>, <code>v1</code> and <code>v2</code> and return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test implements backface culling, that is, it will return <code>false</code> when the triangle is in clockwise
     * winding order assuming a <i>right-handed</i> coordinate system when seen along the ray's direction, even if the ray intersects the triangle.
     * This is in compliance with how OpenGL handles backface culling with default frontface/backface settings.
     * 
     * @see #intersectRayTriangleFront(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection
     *         if the ray intersects the frontface of the triangle; <code>-1.0</code> otherwise
     */
    public static double intersectRayTriangleFront(Vector3dc origin, Vector3dc dir, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon) {
        return intersectRayTriangleFront(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon);
    }

    /**
     * Determine whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>
     * and return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test does not take into account the winding order of the triangle, so a ray will intersect a front-facing triangle as well as a back-facing triangle.
     * 
     * @see #testRayTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param originZ
     *              the z coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param dirZ
     *              the z coordinate of the ray's direction
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection
     *         if the ray intersects the triangle; <code>-1.0</code> otherwise
     */
    public static double intersectRayTriangle(double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon) {
        double edge1X = v1X - v0X;
        double edge1Y = v1Y - v0Y;
        double edge1Z = v1Z - v0Z;
        double edge2X = v2X - v0X;
        double edge2Y = v2Y - v0Y;
        double edge2Z = v2Z - v0Z;
        double pvecX = dirY * edge2Z - dirZ * edge2Y;
        double pvecY = dirZ * edge2X - dirX * edge2Z;
        double pvecZ = dirX * edge2Y - dirY * edge2X;
        double det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ;
        if (det > -epsilon && det < epsilon)
            return -1.0;
        double tvecX = originX - v0X;
        double tvecY = originY - v0Y;
        double tvecZ = originZ - v0Z;
        double invDet = 1.0 / det;
        double u = (tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ) * invDet;
        if (u < 0.0 || u > 1.0)
            return -1.0;
        double qvecX = tvecY * edge1Z - tvecZ * edge1Y;
        double qvecY = tvecZ * edge1X - tvecX * edge1Z;
        double qvecZ = tvecX * edge1Y - tvecY * edge1X;
        double v = (dirX * qvecX + dirY * qvecY + dirZ * qvecZ) * invDet;
        if (v < 0.0 || u + v > 1.0)
            return -1.0;
        double t = (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet;
        return t;
    }

    /**
     * Determine whether the ray with the given <code>origin</code> and the given <code>dir</code> intersects the triangle consisting of the three vertices
     * <code>v0</code>, <code>v1</code> and <code>v2</code> and return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection.
     * <p>
     * This is an implementation of the <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a> method.
     * <p>
     * This test does not take into account the winding order of the triangle, so a ray will intersect a front-facing triangle as well as a back-facing triangle.
     * 
     * @see #intersectRayTriangle(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing rays that are almost parallel to the triangle
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the point of intersection
     *         if the ray intersects the triangle; <code>-1.0</code> otherwise
     */
    public static double intersectRayTriangle(Vector3dc origin, Vector3dc dir, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon) {
        return intersectRayTriangle(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon);
    }

    /**
     * Test whether the line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>,
     * regardless of the winding order of the triangle or the direction of the line segment between its two end points.
     * <p>
     * Reference: <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a>
     * 
     * @see #testLineSegmentTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double)
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p0Z
     *              the z coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param p1Z
     *              the z coordinate of the line segment's second end point
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing line segments that are almost parallel to the triangle
     * @return <code>true</code> if the given line segment intersects the triangle; <code>false</code> otherwise
     */
    public static boolean testLineSegmentTriangle(double p0X, double p0Y, double p0Z, double p1X, double p1Y, double p1Z,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon) {
        double dirX = p1X - p0X;
        double dirY = p1Y - p0Y;
        double dirZ = p1Z - p0Z;
        double t = intersectRayTriangle(p0X, p0Y, p0Z, dirX, dirY, dirZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, epsilon);
        return t >= 0.0 && t <= 1.0;
    }

    /**
     * Test whether the line segment with the end points <code>p0</code> and <code>p1</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>,
     * regardless of the winding order of the triangle or the direction of the line segment between its two end points.
     * <p>
     * Reference: <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a>
     * 
     * @see #testLineSegmentTriangle(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)
     * 
     * @param p0
     *              the line segment's first end point
     * @param p1
     *              the line segment's second end point
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing line segments that are almost parallel to the triangle
     * @return <code>true</code> if the given line segment intersects the triangle; <code>false</code> otherwise
     */
    public static boolean testLineSegmentTriangle(Vector3dc p0, Vector3dc p1, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon) {
        return testLineSegmentTriangle(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon);
    }

    /**
     * Determine whether the line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>,
     * regardless of the winding order of the triangle or the direction of the line segment between its two end points,
     * and return the point of intersection.
     * <p>
     * Reference: <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a>
     * 
     * @see #intersectLineSegmentTriangle(Vector3dc, Vector3dc, Vector3dc, Vector3dc, Vector3dc, double, Vector3d)
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p0Z
     *              the z coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param p1Z
     *              the z coordinate of the line segment's second end point
     * @param v0X
     *              the x coordinate of the first vertex
     * @param v0Y
     *              the y coordinate of the first vertex
     * @param v0Z
     *              the z coordinate of the first vertex
     * @param v1X
     *              the x coordinate of the second vertex
     * @param v1Y
     *              the y coordinate of the second vertex
     * @param v1Z
     *              the z coordinate of the second vertex
     * @param v2X
     *              the x coordinate of the third vertex
     * @param v2Y
     *              the y coordinate of the third vertex
     * @param v2Z
     *              the z coordinate of the third vertex
     * @param epsilon
     *              a small epsilon when testing line segments that are almost parallel to the triangle
     * @param intersectionPoint
     *              the point of intersection
     * @return <code>true</code> if the given line segment intersects the triangle; <code>false</code> otherwise
     */
    public static boolean intersectLineSegmentTriangle(double p0X, double p0Y, double p0Z, double p1X, double p1Y, double p1Z,
            double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
            double epsilon, Vector3d intersectionPoint) {
        double dirX = p1X - p0X;
        double dirY = p1Y - p0Y;
        double dirZ = p1Z - p0Z;
        double t = intersectRayTriangle(p0X, p0Y, p0Z, dirX, dirY, dirZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, epsilon);
        if (t >= 0.0 && t <= 1.0) {
            intersectionPoint.x = p0X + dirX * t;
            intersectionPoint.y = p0Y + dirY * t;
            intersectionPoint.z = p0Z + dirZ * t;
            return true;
        }
        return false;
    }

    /**
     * Determine whether the line segment with the end points <code>p0</code> and <code>p1</code>
     * intersects the triangle consisting of the three vertices <code>(v0X, v0Y, v0Z)</code>, <code>(v1X, v1Y, v1Z)</code> and <code>(v2X, v2Y, v2Z)</code>,
     * regardless of the winding order of the triangle or the direction of the line segment between its two end points,
     * and return the point of intersection.
     * <p>
     * Reference: <a href="http://www.graphics.cornell.edu/pubs/1997/MT97.pdf">
     * Fast, Minimum Storage Ray/Triangle Intersection</a>
     * 
     * @see #intersectLineSegmentTriangle(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, Vector3d)
     * 
     * @param p0
     *              the line segment's first end point
     * @param p1
     *              the line segment's second end point
     * @param v0
     *              the position of the first vertex
     * @param v1
     *              the position of the second vertex
     * @param v2
     *              the position of the third vertex
     * @param epsilon
     *              a small epsilon when testing line segments that are almost parallel to the triangle
     * @param intersectionPoint
     *              the point of intersection
     * @return <code>true</code> if the given line segment intersects the triangle; <code>false</code> otherwise
     */
    public static boolean intersectLineSegmentTriangle(Vector3dc p0, Vector3dc p1, Vector3dc v0, Vector3dc v1, Vector3dc v2, double epsilon, Vector3d intersectionPoint) {
        return intersectLineSegmentTriangle(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), epsilon, intersectionPoint);
    }

    /**
     * Determine whether the line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects the plane given as the general plane equation <i>a*x + b*y + c*z + d = 0</i>,
     * and return the point of intersection.
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p0Z
     *              the z coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param p1Z
     *              the z coordinate of the line segment's second end point
     * @param a
     *              the x factor in the plane equation
     * @param b
     *              the y factor in the plane equation
     * @param c
     *              the z factor in the plane equation
     * @param d
     *              the constant in the plane equation
     * @param intersectionPoint
     *              the point of intersection
     * @return <code>true</code> if the given line segment intersects the plane; <code>false</code> otherwise
     */
    public static boolean intersectLineSegmentPlane(double p0X, double p0Y, double p0Z, double p1X, double p1Y, double p1Z,
            double a, double b, double c, double d, Vector3d intersectionPoint) {
        double dirX = p1X - p0X;
        double dirY = p1Y - p0Y;
        double dirZ = p1Z - p0Z;
        double denom = a * dirX + b * dirY + c * dirZ;
        double t = -(a * p0X + b * p0Y + c * p0Z + d) / denom;
        if (t >= 0.0 && t <= 1.0) {
            intersectionPoint.x = p0X + t * dirX;
            intersectionPoint.y = p0Y + t * dirY;
            intersectionPoint.z = p0Z + t * dirZ;
            return true;
        }
        return false;
    }

    /**
     * Test whether the line with the general line equation <i>a*x + b*y + c = 0</i> intersects the circle with center
     * <code>(centerX, centerY)</code> and <code>radius</code>.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/943383/determine-circle-of-intersection-of-plane-and-sphere">http://math.stackexchange.com</a>
     *
     * @param a
     *          the x factor in the line equation
     * @param b
     *          the y factor in the line equation
     * @param c
     *          the constant in the line equation
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radius
     *          the radius of the circle
     * @return <code>true</code> iff the line intersects the circle; <code>false</code> otherwise
     */
    public static boolean testLineCircle(double a, double b, double c, double centerX, double centerY, double radius) {
        double denom = Math.sqrt(a * a + b * b);
        double dist = (a * centerX + b * centerY + c) / denom;
        return -radius <= dist && dist <= radius;
    }

    /**
     * Test whether the line with the general line equation <i>a*x + b*y + c = 0</i> intersects the circle with center
     * <code>(centerX, centerY)</code> and <code>radius</code>, and store the center of the line segment of
     * intersection in the <code>(x, y)</code> components of the supplied vector and the half-length of that line segment in the z component.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/943383/determine-circle-of-intersection-of-plane-and-sphere">http://math.stackexchange.com</a>
     *
     * @param a
     *          the x factor in the line equation
     * @param b
     *          the y factor in the line equation
     * @param c
     *          the constant in the line equation
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radius
     *          the radius of the circle
     * @param intersectionCenterAndHL
     *          will hold the center of the line segment of intersection in the <code>(x, y)</code> components and the half-length in the z component
     * @return <code>true</code> iff the line intersects the circle; <code>false</code> otherwise
     */
    public static boolean intersectLineCircle(double a, double b, double c, double centerX, double centerY, double radius, Vector3d intersectionCenterAndHL) {
        double invDenom = Math.invsqrt(a * a + b * b);
        double dist = (a * centerX + b * centerY + c) * invDenom;
        if (-radius <= dist && dist <= radius) {
            intersectionCenterAndHL.x = centerX + dist * a * invDenom;
            intersectionCenterAndHL.y = centerY + dist * b * invDenom;
            intersectionCenterAndHL.z = Math.sqrt(radius * radius - dist * dist);
            return true;
        }
        return false;
    }

    /**
     * Test whether the line defined by the two points <code>(x0, y0)</code> and <code>(x1, y1)</code> intersects the circle with center
     * <code>(centerX, centerY)</code> and <code>radius</code>, and store the center of the line segment of
     * intersection in the <code>(x, y)</code> components of the supplied vector and the half-length of that line segment in the z component.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/943383/determine-circle-of-intersection-of-plane-and-sphere">http://math.stackexchange.com</a>
     *
     * @param x0
     *          the x coordinate of the first point on the line
     * @param y0
     *          the y coordinate of the first point on the line
     * @param x1
     *          the x coordinate of the second point on the line
     * @param y1
     *          the y coordinate of the second point on the line
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radius
     *          the radius of the circle
     * @param intersectionCenterAndHL
     *          will hold the center of the line segment of intersection in the <code>(x, y)</code> components and the half-length in the z component
     * @return <code>true</code> iff the line intersects the circle; <code>false</code> otherwise
     */
    public static boolean intersectLineCircle(double x0, double y0, double x1, double y1, double centerX, double centerY, double radius, Vector3d intersectionCenterAndHL) {
        // Build general line equation from two points and use the other method
        return intersectLineCircle(y0 - y1, x1 - x0, (x0 - x1) * y0 + (y1 - y0) * x0, centerX, centerY, radius, intersectionCenterAndHL);
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>
     * intersects the line with the general equation <i>a*x + b*y + c = 0</i>.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     * 
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned rectangle
     * @param a
     *          the x factor in the line equation
     * @param b
     *          the y factor in the line equation
     * @param c
     *          the constant in the plane equation
     * @return <code>true</code> iff the axis-aligned rectangle intersects the line; <code>false</code> otherwise
     */
    public static boolean testAarLine(double minX, double minY, double maxX, double maxY, double a, double b, double c) {
        double pX, pY, nX, nY;
        if (a > 0.0) {
            pX = maxX;
            nX = minX;
        } else {
            pX = minX;
            nX = maxX;
        }
        if (b > 0.0) {
            pY = maxY;
            nY = minY;
        } else {
            pY = minY;
            nY = maxY;
        }
        double distN = c + a * nX + b * nY;
        double distP = c + a * pX + b * pY;
        return distN <= 0.0 && distP >= 0.0;
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>min</code> and maximum corner <code>max</code>
     * intersects the line with the general equation <i>a*x + b*y + c = 0</i>.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     * 
     * @param min
     *          the minimum corner of the axis-aligned rectangle
     * @param max
     *          the maximum corner of the axis-aligned rectangle
     * @param a
     *          the x factor in the line equation
     * @param b
     *          the y factor in the line equation
     * @param c
     *          the constant in the line equation
     * @return <code>true</code> iff the axis-aligned rectangle intersects the line; <code>false</code> otherwise
     */
    public static boolean testAarLine(Vector2dc min, Vector2dc max, double a, double b, double c) {
        return testAarLine(min.x(), min.y(), max.x(), max.y(), a, b, c);
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>
     * intersects the line defined by the two points <code>(x0, y0)</code> and <code>(x1, y1)</code>.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     * 
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned rectangle
     * @param x0
     *          the x coordinate of the first point on the line
     * @param y0
     *          the y coordinate of the first point on the line
     * @param x1
     *          the x coordinate of the second point on the line
     * @param y1
     *          the y coordinate of the second point on the line
     * @return <code>true</code> iff the axis-aligned rectangle intersects the line; <code>false</code> otherwise
     */
    public static boolean testAarLine(double minX, double minY, double maxX, double maxY, double x0, double y0, double x1, double y1) {
        double a = y0 - y1;
        double b = x1 - x0;
        double c = -b * y0 - a * x0;
        return testAarLine(minX, minY, maxX, maxY, a, b, c);
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>(minXA, minYA)</code> and maximum corner <code>(maxXA, maxYA)</code>
     * intersects the axis-aligned rectangle with minimum corner <code>(minXB, minYB)</code> and maximum corner <code>(maxXB, maxYB)</code>.
     * 
     * @param minXA
     *              the x coordinate of the minimum corner of the first axis-aligned rectangle
     * @param minYA
     *              the y coordinate of the minimum corner of the first axis-aligned rectangle
     * @param maxXA
     *              the x coordinate of the maximum corner of the first axis-aligned rectangle
     * @param maxYA
     *              the y coordinate of the maximum corner of the first axis-aligned rectangle
     * @param minXB
     *              the x coordinate of the minimum corner of the second axis-aligned rectangle
     * @param minYB
     *              the y coordinate of the minimum corner of the second axis-aligned rectangle
     * @param maxXB
     *              the x coordinate of the maximum corner of the second axis-aligned rectangle
     * @param maxYB
     *              the y coordinate of the maximum corner of the second axis-aligned rectangle
     * @return <code>true</code> iff both axis-aligned rectangles intersect; <code>false</code> otherwise
     */
    public static boolean testAarAar(double minXA, double minYA, double maxXA, double maxYA, double minXB, double minYB, double maxXB, double maxYB) {
        return maxXA >= minXB && maxYA >= minYB &&  minXA <= maxXB && minYA <= maxYB;
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>minA</code> and maximum corner <code>maxA</code>
     * intersects the axis-aligned rectangle with minimum corner <code>minB</code> and maximum corner <code>maxB</code>.
     * 
     * @param minA
     *              the minimum corner of the first axis-aligned rectangle
     * @param maxA
     *              the maximum corner of the first axis-aligned rectangle
     * @param minB
     *              the minimum corner of the second axis-aligned rectangle
     * @param maxB
     *              the maximum corner of the second axis-aligned rectangle
     * @return <code>true</code> iff both axis-aligned rectangles intersect; <code>false</code> otherwise
     */
    public static boolean testAarAar(Vector2dc minA, Vector2dc maxA, Vector2dc minB, Vector2dc maxB) {
        return testAarAar(minA.x(), minA.y(), maxA.x(), maxA.y(), minB.x(), minB.y(), maxB.x(), maxB.y());
    }

    /**
     * Test whether a given circle with center <code>(aX, aY)</code> and radius <code>aR</code> and travelled distance vector <code>(maX, maY)</code>
     * intersects a given static circle with center <code>(bX, bY)</code> and radius <code>bR</code>.
     * <p>
     * Note that the case of two moving circles can always be reduced to this case by expressing the moved distance of one of the circles relative
     * to the other.
     * <p>
     * Reference: <a href="https://www.gamasutra.com/view/feature/131424/pool_hall_lessons_fast_accurate_.php?page=2">https://www.gamasutra.com</a>
     * 
     * @param aX
     *              the x coordinate of the first circle's center
     * @param aY
     *              the y coordinate of the first circle's center
     * @param maX
     *              the x coordinate of the first circle's travelled distance vector
     * @param maY
     *              the y coordinate of the first circle's travelled distance vector
     * @param aR
     *              the radius of the first circle
     * @param bX
     *              the x coordinate of the second circle's center
     * @param bY
     *              the y coordinate of the second circle's center
     * @param bR
     *              the radius of the second circle
     * @return <code>true</code> if both circle intersect; <code>false</code> otherwise
     */
    public static boolean testMovingCircleCircle(double aX, double aY, double maX, double maY, double aR, double bX, double bY, double bR) {
        double aRbR = aR + bR;
        double dist = Math.sqrt((aX - bX) * (aX - bX) + (aY - bY) * (aY - bY)) - aRbR;
        double mLen = Math.sqrt(maX * maX + maY * maY);
        if (mLen < dist)
            return false;
        double invMLen = 1.0 / mLen;
        double nX = maX * invMLen;
        double nY = maY * invMLen;
        double cX = bX - aX;
        double cY = bY - aY;
        double nDotC = nX * cX + nY * cY;
        if (nDotC <= 0.0)
            return false;
        double cLen = Math.sqrt(cX * cX + cY * cY);
        double cLenNdotC = cLen * cLen - nDotC * nDotC;
        double aRbR2 = aRbR * aRbR;
        if (cLenNdotC >= aRbR2)
            return false;
        double t = aRbR2 - cLenNdotC;
        if (t < 0.0)
            return false;
        double distance = nDotC - Math.sqrt(t);
        double mag = mLen;
        if (mag < distance)
            return false;
        return true;
    }

    /**
     * Test whether a given circle with center <code>centerA</code> and radius <code>aR</code> and travelled distance vector <code>moveA</code>
     * intersects a given static circle with center <code>centerB</code> and radius <code>bR</code>.
     * <p>
     * Note that the case of two moving circles can always be reduced to this case by expressing the moved distance of one of the circles relative
     * to the other.
     * <p>
     * Reference: <a href="https://www.gamasutra.com/view/feature/131424/pool_hall_lessons_fast_accurate_.php?page=2">https://www.gamasutra.com</a>
     * 
     * @param centerA
     *              the coordinates of the first circle's center
     * @param moveA
     *              the coordinates of the first circle's travelled distance vector
     * @param aR
     *              the radius of the first circle
     * @param centerB
     *              the coordinates of the second circle's center
     * @param bR
     *              the radius of the second circle
     * @return <code>true</code> if both circle intersect; <code>false</code> otherwise
     */
    public static boolean testMovingCircleCircle(Vector2d centerA, Vector2d moveA, double aR, Vector2d centerB, double bR) {
        return testMovingCircleCircle(centerA.x, centerA.y, moveA.x, moveA.y, aR, centerB.x, centerB.y, bR);
    }

    /**
     * Test whether the one circle with center <code>(aX, aY)</code> and square radius <code>radiusSquaredA</code> intersects the other
     * circle with center <code>(bX, bY)</code> and square radius <code>radiusSquaredB</code>, and store the center of the line segment of
     * intersection in the <code>(x, y)</code> components of the supplied vector and the half-length of that line segment in the z component.
     * <p>
     * This method returns <code>false</code> when one circle contains the other circle.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param aX
     *              the x coordinate of the first circle's center
     * @param aY
     *              the y coordinate of the first circle's center
     * @param radiusSquaredA
     *              the square of the first circle's radius
     * @param bX
     *              the x coordinate of the second circle's center
     * @param bY
     *              the y coordinate of the second circle's center
     * @param radiusSquaredB
     *              the square of the second circle's radius
     * @param intersectionCenterAndHL
     *              will hold the center of the circle of intersection in the <code>(x, y, z)</code> components and the radius in the w component
     * @return <code>true</code> iff both circles intersect; <code>false</code> otherwise
     */
    public static boolean intersectCircleCircle(double aX, double aY, double radiusSquaredA, double bX, double bY, double radiusSquaredB, Vector3d intersectionCenterAndHL) {
        double dX = bX - aX, dY = bY - aY;
        double distSquared = dX * dX + dY * dY;
        double h = 0.5 + (radiusSquaredA - radiusSquaredB) / distSquared;
        double r_i = Math.sqrt(radiusSquaredA - h * h * distSquared);
        if (r_i >= 0.0) {
            intersectionCenterAndHL.x = aX + h * dX;
            intersectionCenterAndHL.y = aY + h * dY;
            intersectionCenterAndHL.z = r_i;
            return true;
        }
        return false;
    }

    /**
     * Test whether the one circle with center <code>centerA</code> and square radius <code>radiusSquaredA</code> intersects the other
     * circle with center <code>centerB</code> and square radius <code>radiusSquaredB</code>, and store the center of the line segment of
     * intersection in the <code>(x, y)</code> components of the supplied vector and the half-length of that line segment in the z component.
     * <p>
     * This method returns <code>false</code> when one circle contains the other circle.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param centerA
     *              the first circle's center
     * @param radiusSquaredA
     *              the square of the first circle's radius
     * @param centerB
     *              the second circle's center
     * @param radiusSquaredB
     *              the square of the second circle's radius
     * @param intersectionCenterAndHL
     *              will hold the center of the line segment of intersection in the <code>(x, y)</code> components and the half-length in the z component
     * @return <code>true</code> iff both circles intersect; <code>false</code> otherwise
     */
    public static boolean intersectCircleCircle(Vector2dc centerA, double radiusSquaredA, Vector2dc centerB, double radiusSquaredB, Vector3d intersectionCenterAndHL) {
        return intersectCircleCircle(centerA.x(), centerA.y(), radiusSquaredA, centerB.x(), centerB.y(), radiusSquaredB, intersectionCenterAndHL);
    }

    /**
     * Test whether the one circle with center <code>(aX, aY)</code> and radius <code>rA</code> intersects the other circle with center <code>(bX, bY)</code> and radius <code>rB</code>.
     * <p>
     * This method returns <code>true</code> when one circle contains the other circle.
     * <p>
     * Reference: <a href="http://math.stackexchange.com/questions/275514/two-circles-overlap">http://math.stackexchange.com/</a>
     * 
     * @param aX
     *              the x coordinate of the first circle's center
     * @param aY
     *              the y coordinate of the first circle's center
     * @param rA
     *              the square of the first circle's radius
     * @param bX
     *              the x coordinate of the second circle's center
     * @param bY
     *              the y coordinate of the second circle's center
     * @param rB
     *              the square of the second circle's radius
     * @return <code>true</code> iff both circles intersect; <code>false</code> otherwise
     */
    public static boolean testCircleCircle(double aX, double aY, double rA, double bX, double bY, double rB) {
        double d = (aX - bX) * (aX - bX) + (aY - bY) * (aY - bY);
        return d <= (rA + rB) * (rA + rB);
    }

    /**
     * Test whether the one circle with center <code>centerA</code> and square radius <code>radiusSquaredA</code> intersects the other
     * circle with center <code>centerB</code> and square radius <code>radiusSquaredB</code>.
     * <p>
     * This method returns <code>true</code> when one circle contains the other circle.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/75756/sphere-sphere-intersection-and-circle-sphere-intersection">http://gamedev.stackexchange.com</a>
     * 
     * @param centerA
     *              the first circle's center
     * @param radiusSquaredA
     *              the square of the first circle's radius
     * @param centerB
     *              the second circle's center
     * @param radiusSquaredB
     *              the square of the second circle's radius
     * @return <code>true</code> iff both circles intersect; <code>false</code> otherwise
     */
    public static boolean testCircleCircle(Vector2dc centerA, double radiusSquaredA, Vector2dc centerB, double radiusSquaredB) {
        return testCircleCircle(centerA.x(), centerA.y(), radiusSquaredA, centerB.x(), centerB.y(), radiusSquaredB);
    }

    /**
     * Determine the signed distance of the given point <code>(pointX, pointY)</code> to the line specified via its general plane equation
     * <i>a*x + b*y + c = 0</i>.
     * <p>
     * Reference: <a href="http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html">http://mathworld.wolfram.com</a>
     * 
     * @param pointX
     *              the x coordinate of the point
     * @param pointY
     *              the y coordinate of the point
     * @param a
     *              the x factor in the plane equation
     * @param b
     *              the y factor in the plane equation
     * @param c
     *              the constant in the plane equation
     * @return the distance between the point and the line
     */
    public static double distancePointLine(double pointX, double pointY, double a, double b, double c) {
        double denom = Math.sqrt(a * a + b * b);
        return (a * pointX + b * pointY + c) / denom;
    }

    /**
     * Determine the signed distance of the given point <code>(pointX, pointY)</code> to the line defined by the two points <code>(x0, y0)</code> and <code>(x1, y1)</code>.
     * <p>
     * Reference: <a href="http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html">http://mathworld.wolfram.com</a>
     * 
     * @param pointX
     *              the x coordinate of the point
     * @param pointY
     *              the y coordinate of the point
     * @param x0
     *              the x coordinate of the first point on the line
     * @param y0
     *              the y coordinate of the first point on the line
     * @param x1
     *              the x coordinate of the second point on the line
     * @param y1
     *              the y coordinate of the second point on the line
     * @return the distance between the point and the line
     */
    public static double distancePointLine(double pointX, double pointY, double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double denom = Math.sqrt(dx * dx + dy * dy);
        return (dx * (y0 - pointY) - (x0 - pointX) * dy) / denom;
    }

    /**
     * Compute the distance of the given point <code>(pX, pY, pZ)</code> to the line defined by the two points <code>(x0, y0, z0)</code> and <code>(x1, y1, z1)</code>.
     * <p>
     * Reference: <a href="http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html">http://mathworld.wolfram.com</a>
     * 
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param pZ
     *          the z coordinate of the point
     * @param x0
     *          the x coordinate of the first point on the line
     * @param y0
     *          the y coordinate of the first point on the line
     * @param z0
     *          the z coordinate of the first point on the line
     * @param x1
     *          the x coordinate of the second point on the line
     * @param y1
     *          the y coordinate of the second point on the line
     * @param z1
     *          the z coordinate of the second point on the line
     * @return the distance between the point and the line
     */
    public static double distancePointLine(double pX, double pY, double pZ, 
            double x0, double y0, double z0, double x1, double y1, double z1) {
        double d21x = x1 - x0, d21y = y1 - y0, d21z = z1 - z0;
        double d10x = x0 - pX, d10y = y0 - pY, d10z = z0 - pZ;
        double cx = d21y * d10z - d21z * d10y, cy = d21z * d10x - d21x * d10z, cz = d21x * d10y - d21y * d10x;
        return Math.sqrt((cx*cx + cy*cy + cz*cz) / (d21x*d21x + d21y*d21y + d21z*d21z));
    }

    /**
     * Test whether the ray with given origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code> intersects the line
     * containing the given point <code>(pointX, pointY)</code> and having the normal <code>(normalX, normalY)</code>, and return the
     * value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the line, because it is either parallel to the line or its direction points
     * away from the line or the ray's origin is on the <i>negative</i> side of the line (i.e. the line's normal points away from the ray's origin).
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param pointX
     *              the x coordinate of a point on the line
     * @param pointY
     *              the y coordinate of a point on the line
     * @param normalX
     *              the x coordinate of the line's normal
     * @param normalY
     *              the y coordinate of the line's normal
     * @param epsilon
     *              some small epsilon for when the ray is parallel to the line
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the line; <code>-1.0</code> otherwise
     */
    public static double intersectRayLine(double originX, double originY, double dirX, double dirY, double pointX, double pointY, double normalX, double normalY, double epsilon) {
        double denom = normalX * dirX + normalY * dirY;
        if (denom < epsilon) {
            double t = ((pointX - originX) * normalX + (pointY - originY) * normalY) / denom;
            if (t >= 0.0)
                return t;
        }
        return -1.0;
    }

    /**
     * Test whether the ray with given <code>origin</code> and direction <code>dir</code> intersects the line
     * containing the given <code>point</code> and having the given <code>normal</code>, and return the
     * value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the line, because it is either parallel to the line or its direction points
     * away from the line or the ray's origin is on the <i>negative</i> side of the line (i.e. the line's normal points away from the ray's origin).
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param point
     *              a point on the line
     * @param normal
     *              the line's normal
     * @param epsilon
     *              some small epsilon for when the ray is parallel to the line
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the line; <code>-1.0</code> otherwise
     */
    public static double intersectRayLine(Vector2dc origin, Vector2dc dir, Vector2dc point, Vector2dc normal, double epsilon) {
        return intersectRayLine(origin.x(), origin.y(), dir.x(), dir.y(), point.x(), point.y(), normal.x(), normal.y(), epsilon);
    }

    /**
     * Determine whether the ray with given origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code> intersects the undirected line segment
     * given by the two end points <code>(aX, bY)</code> and <code>(bX, bY)</code>, and return the value of the parameter <i>t</i> in the ray equation
     * <i>p(t) = origin + t * dir</i> of the intersection point, if any.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the line segment.
     * 
     * @see #intersectRayLineSegment(Vector2dc, Vector2dc, Vector2dc, Vector2dc)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param aX
     *              the x coordinate of the line segment's first end point
     * @param aY
     *              the y coordinate of the line segment's first end point
     * @param bX
     *              the x coordinate of the line segment's second end point
     * @param bY
     *              the y coordinate of the line segment's second end point
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the line segment; <code>-1.0</code> otherwise
     */
    public static double intersectRayLineSegment(double originX, double originY, double dirX, double dirY, double aX, double aY, double bX, double bY) {
        double v1X = originX - aX;
        double v1Y = originY - aY;
        double v2X = bX - aX;
        double v2Y = bY - aY;
        double invV23 = 1.0 / (v2Y * dirX - v2X * dirY);
        double t1 = (v2X * v1Y - v2Y * v1X) * invV23;
        double t2 = (v1Y * dirX - v1X * dirY) * invV23;
        if (t1 >= 0.0 && t2 >= 0.0 && t2 <= 1.0)
            return t1;
        return -1.0;
    }

    /**
     * Determine whether the ray with given <code>origin</code> and direction <code>dir</code> intersects the undirected line segment
     * given by the two end points <code>a</code> and <code>b</code>, and return the value of the parameter <i>t</i> in the ray equation
     * <i>p(t) = origin + t * dir</i> of the intersection point, if any.
     * <p>
     * This method returns <code>-1.0</code> if the ray does not intersect the line segment.
     * 
     * @see #intersectRayLineSegment(double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param a
     *              the line segment's first end point
     * @param b
     *              the line segment's second end point
     * @return the value of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the intersection point, if the ray
     *         intersects the line segment; <code>-1.0</code> otherwise
     */
    public static double intersectRayLineSegment(Vector2dc origin, Vector2dc dir, Vector2dc a, Vector2dc b) {
        return intersectRayLineSegment(origin.x(), origin.y(), dir.x(), dir.y(), a.x(), a.y(), b.x(), b.y());
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>
     * intersects the circle with the given center <code>(centerX, centerY)</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     * 
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned rectangle
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radiusSquared
     *          the square of the circle's radius
     * @return <code>true</code> iff the axis-aligned rectangle intersects the circle; <code>false</code> otherwise
     */
    public static boolean testAarCircle(double minX, double minY, double maxX, double maxY, double centerX, double centerY, double radiusSquared) {
        double radius2 = radiusSquared;
        if (centerX < minX) {
            double d = (centerX - minX);
            radius2 -= d * d;
        } else if (centerX > maxX) {
            double d = (centerX - maxX);
            radius2 -= d * d;
        }
        if (centerY < minY) {
            double d = (centerY - minY);
            radius2 -= d * d;
        } else if (centerY > maxY) {
            double d = (centerY - maxY);
            radius2 -= d * d;
        }
        return radius2 >= 0.0;
    }

    /**
     * Test whether the axis-aligned rectangle with minimum corner <code>min</code> and maximum corner <code>max</code>
     * intersects the circle with the given <code>center</code> and square radius <code>radiusSquared</code>.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     * 
     * @param min
     *          the minimum corner of the axis-aligned rectangle
     * @param max
     *          the maximum corner of the axis-aligned rectangle
     * @param center
     *          the circle's center
     * @param radiusSquared
     *          the squared of the circle's radius
     * @return <code>true</code> iff the axis-aligned rectangle intersects the circle; <code>false</code> otherwise
     */
    public static boolean testAarCircle(Vector2dc min, Vector2dc max, Vector2dc center, double radiusSquared) {
        return testAarCircle(min.x(), min.y(), max.x(), max.y(), center.x(), center.y(), radiusSquared);
    }

    /**
     * Determine the closest point on the triangle with the given vertices <code>(v0X, v0Y)</code>, <code>(v1X, v1Y)</code>, <code>(v2X, v2Y)</code>
     * between that triangle and the given point <code>(pX, pY)</code> and store that point into the given <code>result</code>.
     * <p>
     * Additionally, this method returns whether the closest point is a vertex ({@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2})
     * of the triangle, lies on an edge ({@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20})
     * or on the {@link #POINT_ON_TRIANGLE_FACE face} of the triangle.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.5 "Closest Point on Triangle to Point"
     * 
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param result
     *          will hold the closest point
     * @return one of {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2},
     *                {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20} or
     *                {@link #POINT_ON_TRIANGLE_FACE}
     */
    public static int findClosestPointOnTriangle(double v0X, double v0Y, double v1X, double v1Y, double v2X, double v2Y, double pX, double pY, Vector2d result) {
        double abX = v1X - v0X, abY = v1Y - v0Y;
        double acX = v2X - v0X, acY = v2Y - v0Y;
        double apX = pX - v0X, apY = pY - v0Y;
        double d1 = abX * apX + abY * apY;
        double d2 = acX * apX + acY * apY;
        if (d1 <= 0.0 && d2 <= 0.0) {
            result.x = v0X;
            result.y = v0Y;
            return POINT_ON_TRIANGLE_VERTEX_0;
        }
        double bpX = pX - v1X, bpY = pY - v1Y;
        double d3 = abX * bpX + abY * bpY;
        double d4 = acX * bpX + acY * bpY;
        if (d3 >= 0.0 && d4 <= d3) {
            result.x = v1X;
            result.y = v1Y;
            return POINT_ON_TRIANGLE_VERTEX_1;
        }
        double vc = d1 * d4 - d3 * d2;
        if (vc <= 0.0 && d1 >= 0.0 && d3 <= 0.0) {
            double v = d1 / (d1 - d3);
            result.x = v0X + v * abX;
            result.y = v0Y + v * abY;
            return POINT_ON_TRIANGLE_EDGE_01;
        }
        double cpX = pX - v2X, cpY = pY - v2Y;
        double d5 = abX * cpX + abY * cpY;
        double d6 = acX * cpX + acY * cpY;
        if (d6 >= 0.0 && d5 <= d6) {
            result.x = v2X;
            result.y = v2Y;
            return POINT_ON_TRIANGLE_VERTEX_2;
        }
        double vb = d5 * d2 - d1 * d6;
        if (vb <= 0.0 && d2 >= 0.0 && d6 <= 0.0) {
            double w = d2 / (d2 - d6);
            result.x = v0X + w * acX;
            result.y = v0Y + w * acY;
            return POINT_ON_TRIANGLE_EDGE_20;
        }
        double va = d3 * d6 - d5 * d4;
        if (va <= 0.0 && d4 - d3 >= 0.0 && d5 - d6 >= 0.0) {
            double w = (d4 - d3) / (d4 - d3 + d5 - d6);
            result.x = v1X + w * (v2X - v1X);
            result.y = v1Y + w * (v2Y - v1Y);
            return POINT_ON_TRIANGLE_EDGE_12;
        }
        double denom = 1.0 / (va + vb + vc);
        double v = vb * denom;
        double w = vc * denom;
        result.x = v0X + abX * v + acX * w;
        result.y = v0Y + abY * v + acY * w;
        return POINT_ON_TRIANGLE_FACE;
    }

    /**
     * Determine the closest point on the triangle with the vertices <code>v0</code>, <code>v1</code>, <code>v2</code>
     * between that triangle and the given point <code>p</code> and store that point into the given <code>result</code>.
     * <p>
     * Additionally, this method returns whether the closest point is a vertex ({@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2})
     * of the triangle, lies on an edge ({@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20})
     * or on the {@link #POINT_ON_TRIANGLE_FACE face} of the triangle.
     * <p>
     * Reference: Book "Real-Time Collision Detection" chapter 5.1.5 "Closest Point on Triangle to Point"
     * 
     * @param v0
     *          the first vertex of the triangle
     * @param v1
     *          the second vertex of the triangle
     * @param v2
     *          the third vertex of the triangle
     * @param p
     *          the point
     * @param result
     *          will hold the closest point
     * @return one of {@link #POINT_ON_TRIANGLE_VERTEX_0}, {@link #POINT_ON_TRIANGLE_VERTEX_1}, {@link #POINT_ON_TRIANGLE_VERTEX_2},
     *                {@link #POINT_ON_TRIANGLE_EDGE_01}, {@link #POINT_ON_TRIANGLE_EDGE_12}, {@link #POINT_ON_TRIANGLE_EDGE_20} or
     *                {@link #POINT_ON_TRIANGLE_FACE}
     */
    public static int findClosestPointOnTriangle(Vector2dc v0, Vector2dc v1, Vector2dc v2, Vector2dc p, Vector2d result) {
        return findClosestPointOnTriangle(v0.x(), v0.y(), v1.x(), v1.y(), v2.x(), v2.y(), p.x(), p.y(), result);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code>
     * intersects the given circle with center <code>(centerX, centerY)</code> and square radius <code>radiusSquared</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> for both points (near
     * and far) of intersections into the given <code>result</code> vector.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the circle.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param centerX
     *              the x coordinate of the circle's center
     * @param centerY
     *              the y coordinate of the circle's center
     * @param radiusSquared
     *              the circle radius squared
     * @param result
     *              a vector that will contain the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> for both points (near, far) of intersections with the circle
     * @return <code>true</code> if the ray intersects the circle; <code>false</code> otherwise
     */
    public static boolean intersectRayCircle(double originX, double originY, double dirX, double dirY, 
            double centerX, double centerY, double radiusSquared, Vector2d result) {
        double Lx = centerX - originX;
        double Ly = centerY - originY;
        double tca = Lx * dirX + Ly * dirY;
        double d2 = Lx * Lx + Ly * Ly - tca * tca;
        if (d2 > radiusSquared)
            return false;
        double thc = Math.sqrt(radiusSquared - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;
        if (t0 < t1 && t1 >= 0.0) {
            result.x = t0;
            result.y = t1;
            return true;
        }
        return false;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the circle with the given <code>center</code> and square radius <code>radiusSquared</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> for both points (near
     * and far) of intersections into the given <code>result</code> vector.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the circle.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param center
     *              the circle's center
     * @param radiusSquared
     *              the circle radius squared
     * @param result
     *              a vector that will contain the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> for both points (near, far) of intersections with the circle
     * @return <code>true</code> if the ray intersects the circle; <code>false</code> otherwise
     */
    public static boolean intersectRayCircle(Vector2dc origin, Vector2dc dir, Vector2dc center, double radiusSquared, Vector2d result) {
        return intersectRayCircle(origin.x(), origin.y(), dir.x(), dir.y(), center.x(), center.y(), radiusSquared, result);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code>
     * intersects the given circle with center <code>(centerX, centerY)</code> and square radius <code>radiusSquared</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the circle.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param centerX
     *              the x coordinate of the circle's center
     * @param centerY
     *              the y coordinate of the circle's center
     * @param radiusSquared
     *              the circle radius squared
     * @return <code>true</code> if the ray intersects the circle; <code>false</code> otherwise
     */
    public static boolean testRayCircle(double originX, double originY, double dirX, double dirY, 
            double centerX, double centerY, double radiusSquared) {
        double Lx = centerX - originX;
        double Ly = centerY - originY;
        double tca = Lx * dirX + Ly * dirY;
        double d2 = Lx * Lx + Ly * Ly - tca * tca;
        if (d2 > radiusSquared)
            return false;
        double thc = Math.sqrt(radiusSquared - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;
        return t0 < t1 && t1 >= 0.0;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the circle with the given <code>center</code> and square radius.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the circle.
     * <p>
     * Reference: <a href="http://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection">http://www.scratchapixel.com/</a>
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param center
     *              the circle's center
     * @param radiusSquared
     *              the circle radius squared
     * @return <code>true</code> if the ray intersects the circle; <code>false</code> otherwise
     */
    public static boolean testRayCircle(Vector2dc origin, Vector2dc dir, Vector2dc center, double radiusSquared) {
        return testRayCircle(origin.x(), origin.y(), dir.x(), dir.y(), center.x(), center.y(), radiusSquared);
    }

    /**
     * Determine whether the given ray with the origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code>
     * intersects the axis-aligned rectangle given as its minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     * as well as the side of the axis-aligned rectangle the ray intersects.
     * <p>
     * This method also detects an intersection for a ray whose origin lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param minX
     *              the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *              the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *              the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *              the y coordinate of the maximum corner of the axis-aligned rectangle
     * @param result
     *              a vector which will hold the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     * @return the side on which the near intersection occurred as one of
     *         {@link #AAR_SIDE_MINX}, {@link #AAR_SIDE_MINY}, {@link #AAR_SIDE_MAXX} or {@link #AAR_SIDE_MAXY};
     *         or <code>-1</code> if the ray does not intersect the axis-aligned rectangle;
     */
    public static int intersectRayAar(double originX, double originY, double dirX, double dirY, 
            double minX, double minY, double maxX, double maxY, Vector2d result) {
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY;
        double tNear, tFar, tymin, tymax;
        if (invDirX >= 0.0) {
            tNear = (minX - originX) * invDirX;
            tFar = (maxX - originX) * invDirX;
        } else {
            tNear = (maxX - originX) * invDirX;
            tFar = (minX - originX) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - originY) * invDirY;
            tymax = (maxY - originY) * invDirY;
        } else {
            tymin = (maxY - originY) * invDirY;
            tymax = (minY - originY) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return OUTSIDE;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        int side = -1; // no intersection side
        if (tNear < tFar && tFar >= 0.0) {
            double px = originX + tNear * dirX;
            double py = originY + tNear * dirY;
            result.x = tNear;
            result.y = tFar;
            double daX = Math.abs(px - minX);
            double daY = Math.abs(py - minY);
            double dbX = Math.abs(px - maxX);
            double dbY = Math.abs(py - maxY);
            side = 0; // min x coordinate
            double min = daX;
            if (daY < min) {
                min = daY;
                side = 1; // min y coordinate
            }
            if (dbX < min) {
                min = dbX;
                side = 2; // max xcoordinate
            }
            if (dbY < min)
                side = 3; // max y coordinate
        }
        return side;
    }

    /**
     * Determine whether the given ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the axis-aligned rectangle given as its minimum corner <code>min</code> and maximum corner <code>max</code>,
     * and return the values of the parameter <i>t</i> in the ray equation <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     * as well as the side of the axis-aligned rectangle the ray intersects.
     * <p>
     * This method also detects an intersection for a ray whose origin lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #intersectRayAar(double, double, double, double, double, double, double, double, Vector2d)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param min
     *              the minimum corner of the axis-aligned rectangle
     * @param max
     *              the maximum corner of the axis-aligned rectangle
     * @param result
     *              a vector which will hold the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = origin + t * dir</i> of the near and far point of intersection
     * @return the side on which the near intersection occurred as one of
     *         {@link #AAR_SIDE_MINX}, {@link #AAR_SIDE_MINY}, {@link #AAR_SIDE_MAXX} or {@link #AAR_SIDE_MAXY};
     *         or <code>-1</code> if the ray does not intersect the axis-aligned rectangle;
     */
    public static int intersectRayAar(Vector2dc origin, Vector2dc dir, Vector2dc min, Vector2dc max, Vector2d result) {
        return intersectRayAar(origin.x(), origin.y(), dir.x(), dir.y(), min.x(), min.y(), max.x(), max.y(), result);
    }

    /**
     * Determine whether the undirected line segment with the end points <code>(p0X, p0Y)</code> and <code>(p1X, p1Y)</code>
     * intersects the axis-aligned rectangle given as its minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     * into <code>result</code>.
     * <p>
     * This method also detects an intersection of a line segment whose either end point lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @see #intersectLineSegmentAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc, Vector2d)
     * 
     * @param p0X
     *              the x coordinate of the line segment's first end point
     * @param p0Y
     *              the y coordinate of the line segment's first end point
     * @param p1X
     *              the x coordinate of the line segment's second end point
     * @param p1Y
     *              the y coordinate of the line segment's second end point
     * @param minX
     *              the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *              the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *              the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *              the y coordinate of the maximum corner of the axis-aligned rectangle
     * @param result
     *              a vector which will hold the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     * @return {@link #INSIDE} if the line segment lies completely inside of the axis-aligned rectangle; or
     *         {@link #OUTSIDE} if the line segment lies completely outside of the axis-aligned rectangle; or
     *         {@link #ONE_INTERSECTION} if one of the end points of the line segment lies inside of the axis-aligned rectangle; or
     *         {@link #TWO_INTERSECTION} if the line segment intersects two edges of the axis-aligned rectangle or lies on one edge of the rectangle
     */
    public static int intersectLineSegmentAar(double p0X, double p0Y, double p1X, double p1Y, 
            double minX, double minY, double maxX, double maxY, Vector2d result) {
        double dirX = p1X - p0X, dirY = p1Y - p0Y;
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY;
        double tNear, tFar, tymin, tymax;
        if (invDirX >= 0.0) {
            tNear = (minX - p0X) * invDirX;
            tFar = (maxX - p0X) * invDirX;
        } else {
            tNear = (maxX - p0X) * invDirX;
            tFar = (minX - p0X) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - p0Y) * invDirY;
            tymax = (maxY - p0Y) * invDirY;
        } else {
            tymin = (maxY - p0Y) * invDirY;
            tymax = (minY - p0Y) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return OUTSIDE;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        int type = OUTSIDE;
        if (tNear <= tFar && tNear <= 1.0f && tFar >= 0.0f) {
            if (tNear >= 0.0f && tFar > 1.0f) {
                tFar = tNear;
                type = ONE_INTERSECTION;
            } else if (tNear < 0.0f && tFar <= 1.0f) {
                tNear = tFar;
                type = ONE_INTERSECTION;
            } else if (tNear < 0.0f && tFar > 1.0f) {
                type = INSIDE;
            } else {
                type = TWO_INTERSECTION;
            }
            result.x = tNear;
            result.y = tFar;
        }
        return type;
    }

    /**
     * Determine whether the undirected line segment with the end points <code>p0</code> and <code>p1</code>
     * intersects the axis-aligned rectangle given as its minimum corner <code>min</code> and maximum corner <code>max</code>,
     * and store the values of the parameter <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     * into <code>result</code>.
     * <p>
     * This method also detects an intersection of a line segment whose either end point lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * #see {@link #intersectLineSegmentAar(double, double, double, double, double, double, double, double, Vector2d)}
     * 
     * @param p0
     *              the line segment's first end point
     * @param p1
     *              the line segment's second end point
     * @param min
     *              the minimum corner of the axis-aligned rectangle
     * @param max
     *              the maximum corner of the axis-aligned rectangle
     * @param result
     *              a vector which will hold the values of the parameter <i>t</i> in the ray equation
     *              <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     * @return {@link #INSIDE} if the line segment lies completely inside of the axis-aligned rectangle; or
     *         {@link #OUTSIDE} if the line segment lies completely outside of the axis-aligned rectangle; or
     *         {@link #ONE_INTERSECTION} if one of the end points of the line segment lies inside of the axis-aligned rectangle; or
     *         {@link #TWO_INTERSECTION} if the line segment intersects two edges of the axis-aligned rectangle
     */
    public static int intersectLineSegmentAar(Vector2dc p0, Vector2dc p1, Vector2dc min, Vector2dc max, Vector2d result) {
        return intersectLineSegmentAar(p0.x(), p0.y(), p1.x(), p1.y(), min.x(), min.y(), max.x(), max.y(), result);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY)</code> and direction <code>(dirX, dirY)</code>
     * intersects the given axis-aligned rectangle given as its minimum corner <code>(minX, minY)</code> and maximum corner <code>(maxX, maxY)</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #testRayAar(Vector2dc, Vector2dc, Vector2dc, Vector2dc)
     * 
     * @param originX
     *              the x coordinate of the ray's origin
     * @param originY
     *              the y coordinate of the ray's origin
     * @param dirX
     *              the x coordinate of the ray's direction
     * @param dirY
     *              the y coordinate of the ray's direction
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned rectangle
     * @return <code>true</code> if the given ray intersects the axis-aligned rectangle; <code>false</code> otherwise
     */
    public static boolean testRayAar(double originX, double originY, double dirX, double dirY, double minX, double minY, double maxX, double maxY) {
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY;
        double tNear, tFar, tymin, tymax;
        if (invDirX >= 0.0) {
            tNear = (minX - originX) * invDirX;
            tFar = (maxX - originX) * invDirX;
        } else {
            tNear = (maxX - originX) * invDirX;
            tFar = (minX - originX) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - originY) * invDirY;
            tymax = (maxY - originY) * invDirY;
        } else {
            tymin = (maxY - originY) * invDirY;
            tymax = (minY - originY) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return false;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        return tNear < tFar && tFar >= 0.0;
    }

    /**
     * Test whether the ray with the given <code>origin</code> and direction <code>dir</code>
     * intersects the given axis-aligned rectangle specified as its minimum corner <code>min</code> and maximum corner <code>max</code>.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside the axis-aligned rectangle.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     * 
     * @see #testRayAar(double, double, double, double, double, double, double, double)
     * 
     * @param origin
     *              the ray's origin
     * @param dir
     *              the ray's direction
     * @param min
     *              the minimum corner of the axis-aligned rectangle
     * @param max
     *              the maximum corner of the axis-aligned rectangle
     * @return <code>true</code> if the given ray intersects the axis-aligned rectangle; <code>false</code> otherwise
     */
    public static boolean testRayAar(Vector2dc origin, Vector2dc dir, Vector2dc min, Vector2dc max) {
        return testRayAar(origin.x(), origin.y(), dir.x(), dir.y(), min.x(), min.y(), max.x(), max.y());
    }

    /**
     * Test whether the given point <code>(pX, pY)</code> lies inside the triangle with the vertices <code>(v0X, v0Y)</code>, <code>(v1X, v1Y)</code>, <code>(v2X, v2Y)</code>.
     * 
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @return <code>true</code> iff the point lies inside the triangle; <code>false</code> otherwise
     */
    public static boolean testPointTriangle(double pX, double pY, double v0X, double v0Y, double v1X, double v1Y, double v2X, double v2Y) {
        boolean b1 = (pX - v1X) * (v0Y - v1Y) - (v0X - v1X) * (pY - v1Y) < 0.0;
        boolean b2 = (pX - v2X) * (v1Y - v2Y) - (v1X - v2X) * (pY - v2Y) < 0.0;
        if (b1 != b2)
            return false;
        boolean b3 = (pX - v0X) * (v2Y - v0Y) - (v2X - v0X) * (pY - v0Y) < 0.0;
        return b2 == b3;
    }

    /**
     * Test whether the given <code>point</code> lies inside the triangle with the vertices <code>v0</code>, <code>v1</code>, <code>v2</code>.
     * 
     * @param v0
     *          the first vertex of the triangle
     * @param v1
     *          the second vertex of the triangle
     * @param v2
     *          the third vertex of the triangle
     * @param point
     *          the point
     * @return <code>true</code> iff the point lies inside the triangle; <code>false</code> otherwise
     */
    public static boolean testPointTriangle(Vector2dc point, Vector2dc v0, Vector2dc v1, Vector2dc v2) {
        return testPointTriangle(point.x(), point.y(), v0.x(), v0.y(), v1.x(), v1.y(), v2.x(), v2.y());
    }

    /**
     * Test whether the given point <code>(pX, pY)</code> lies inside the axis-aligned rectangle with the minimum corner <code>(minX, minY)</code>
     * and maximum corner <code>(maxX, maxY)</code>.
     * 
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param minX
     *          the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY
     *          the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX
     *          the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY
     *          the y coordinate of the maximum corner of the axis-aligned rectangle
     * @return <code>true</code> iff the point lies inside the axis-aligned rectangle; <code>false</code> otherwise
     */
    public static boolean testPointAar(double pX, double pY, double minX, double minY, double maxX, double maxY) {
        return pX >= minX && pY >= minY && pX <= maxX && pY <= maxY;
    }

    /**
     * Test whether the point <code>(pX, pY)</code> lies inside the circle with center <code>(centerX, centerY)</code> and square radius <code>radiusSquared</code>.
     * 
     * @param pX
     *          the x coordinate of the point
     * @param pY
     *          the y coordinate of the point
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radiusSquared
     *          the square radius of the circle
     * @return <code>true</code> iff the point lies inside the circle; <code>false</code> otherwise
     */
    public static boolean testPointCircle(double pX, double pY, double centerX, double centerY, double radiusSquared) {
        double dx = pX - centerX;
        double dy = pY - centerY;
        double dx2 = dx * dx;
        double dy2 = dy * dy;
        return dx2 + dy2 <= radiusSquared;
    }

    /**
     * Test whether the circle with center <code>(centerX, centerY)</code> and square radius <code>radiusSquared</code> intersects the triangle with counter-clockwise vertices
     * <code>(v0X, v0Y)</code>, <code>(v1X, v1Y)</code>, <code>(v2X, v2Y)</code>.
     * <p>
     * The vertices of the triangle must be specified in counter-clockwise order.
     * <p>
     * Reference: <a href="http://www.phatcode.net/articles.php?id=459">http://www.phatcode.net/</a>
     * 
     * @param centerX
     *          the x coordinate of the circle's center
     * @param centerY
     *          the y coordinate of the circle's center
     * @param radiusSquared
     *          the square radius of the circle
     * @param v0X
     *          the x coordinate of the first vertex of the triangle
     * @param v0Y
     *          the y coordinate of the first vertex of the triangle
     * @param v1X
     *          the x coordinate of the second vertex of the triangle
     * @param v1Y
     *          the y coordinate of the second vertex of the triangle
     * @param v2X
     *          the x coordinate of the third vertex of the triangle
     * @param v2Y
     *          the y coordinate of the third vertex of the triangle
     * @return <code>true</code> iff the circle intersects the triangle; <code>false</code> otherwise
     */
    public static boolean testCircleTriangle(double centerX, double centerY, double radiusSquared, double v0X, double v0Y, double v1X, double v1Y, double v2X, double v2Y) {
        double c1x = centerX - v0X, c1y = centerY - v0Y;
        double c1sqr = c1x * c1x + c1y * c1y - radiusSquared;
        if (c1sqr <= 0.0)
            return true;
        double c2x = centerX - v1X, c2y = centerY - v1Y;
        double c2sqr = c2x * c2x + c2y * c2y - radiusSquared;
        if (c2sqr <= 0.0)
            return true;
        double c3x = centerX - v2X, c3y = centerY - v2Y;
        double c3sqr = c3x * c3x + c3y * c3y - radiusSquared;
        if (c3sqr <= 0.0)
            return true;
        double e1x = v1X - v0X, e1y = v1Y - v0Y;
        double e2x = v2X - v1X, e2y = v2Y - v1Y;
        double e3x = v0X - v2X, e3y = v0Y - v2Y;
        if (e1x * c1y - e1y * c1x >= 0.0 && e2x * c2y - e2y * c2x >= 0.0 && e3x * c3y - e3y * c3x >= 0.0)
            return true;
        double k = c1x * e1x + c1y * e1y;
        if (k >= 0.0) {
            double len = e1x * e1x + e1y * e1y;
            if (k <= len) {
                if (c1sqr * len <= k * k)
                    return true;
            }
        }
        k = c2x * e2x + c2y * e2y;
        if (k > 0.0) {
            double len = e2x * e2x + e2y * e2y;
            if (k <= len) {
                if (c2sqr * len <= k * k)
                    return true;
            }
        }
        k = c3x * e3x + c3y * e3y;
        if (k >= 0.0) {
            double len = e3x * e3x + e3y * e3y;
            if (k < len) {
                if (c3sqr * len <= k * k)
                    return true;
            }
        }
        return false;
    }

    /**
     * Test whether the circle with given <code>center</code> and square radius <code>radiusSquared</code> intersects the triangle with counter-clockwise vertices
     * <code>v0</code>, <code>v1</code>, <code>v2</code>.
     * <p>
     * The vertices of the triangle must be specified in counter-clockwise order.
     * <p>
     * Reference: <a href="http://www.phatcode.net/articles.php?id=459">http://www.phatcode.net/</a>
     * 
     * @param center
     *          the circle's center
     * @param radiusSquared
     *          the square radius of the circle
     * @param v0
     *          the first vertex of the triangle
     * @param v1
     *          the second vertex of the triangle
     * @param v2
     *          the third vertex of the triangle
     * @return <code>true</code> iff the circle intersects the triangle; <code>false</code> otherwise
     */
    public static boolean testCircleTriangle(Vector2dc center, double radiusSquared, Vector2dc v0, Vector2dc v1, Vector2dc v2) {
        return testCircleTriangle(center.x(), center.y(), radiusSquared, v0.x(), v0.y(), v1.x(), v1.y(), v2.x(), v2.y());
    }

    /**
     * Determine whether the polygon specified by the given sequence of <code>(x, y)</code> coordinate pairs intersects with the ray
     * with given origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>, and store the point of intersection
     * into the given vector <code>p</code>.
     * <p>
     * If the polygon intersects the ray, this method returns the index of the polygon edge intersecting the ray, that is, the index of the 
     * first vertex of the directed line segment. The second vertex is always that index + 1, modulus the number of polygon vertices.
     * 
     * @param verticesXY
     *          the sequence of <code>(x, y)</code> coordinate pairs of all vertices of the polygon
     * @param originX
     *          the x coordinate of the ray's origin
     * @param originY
     *          the y coordinate of the ray's origin
     * @param dirX
     *          the x coordinate of the ray's direction
     * @param dirY
     *          the y coordinate of the ray's direction
     * @param p
     *          will hold the point of intersection
     * @return the index of the first vertex of the polygon edge that intersects the ray; or <code>-1</code> if the ray does not intersect the polygon
     */
    public static int intersectPolygonRay(double[] verticesXY, double originX, double originY, double dirX, double dirY, Vector2d p) {
        double nearestT = Double.POSITIVE_INFINITY;
        int count = verticesXY.length >> 1;
        int edgeIndex = -1;
        double aX = verticesXY[(count-1)<<1], aY = verticesXY[((count-1)<<1) + 1];
        for (int i = 0; i < count; i++) {
            double bX = verticesXY[i << 1], bY = verticesXY[(i << 1) + 1];
            double doaX = originX - aX, doaY = originY - aY;
            double dbaX = bX - aX, dbaY = bY - aY;
            double invDbaDir = 1.0 / (dbaY * dirX - dbaX * dirY);
            double t = (dbaX * doaY - dbaY * doaX) * invDbaDir;
            if (t >= 0.0 && t < nearestT) {
                double t2 = (doaY * dirX - doaX * dirY) * invDbaDir;
                if (t2 >= 0.0 && t2 <= 1.0) {
                    edgeIndex = (i - 1 + count) % count;
                    nearestT = t;
                    p.x = originX + t * dirX;
                    p.y = originY + t * dirY;
                }
            }
            aX = bX;
            aY = bY;
        }
        return edgeIndex;
    }

    /**
     * Determine whether the polygon specified by the given sequence of <code>vertices</code> intersects with the ray
     * with given origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>, and store the point of intersection
     * into the given vector <code>p</code>.
     * <p>
     * If the polygon intersects the ray, this method returns the index of the polygon edge intersecting the ray, that is, the index of the 
     * first vertex of the directed line segment. The second vertex is always that index + 1, modulus the number of polygon vertices.
     * 
     * @param vertices
     *          the sequence of <code>(x, y)</code> coordinate pairs of all vertices of the polygon
     * @param originX
     *          the x coordinate of the ray's origin
     * @param originY
     *          the y coordinate of the ray's origin
     * @param dirX
     *          the x coordinate of the ray's direction
     * @param dirY
     *          the y coordinate of the ray's direction
     * @param p
     *          will hold the point of intersection
     * @return the index of the first vertex of the polygon edge that intersects the ray; or <code>-1</code> if the ray does not intersect the polygon
     */
    public static int intersectPolygonRay(Vector2dc[] vertices, double originX, double originY, double dirX, double dirY, Vector2d p) {
        double nearestT = Double.POSITIVE_INFINITY;
        int count = vertices.length;
        int edgeIndex = -1;
        double aX = vertices[count-1].x(), aY = vertices[count-1].y();
        for (int i = 0; i < count; i++) {
            Vector2dc b = vertices[i];
            double bX = b.x(), bY = b.y();
            double doaX = originX - aX, doaY = originY - aY;
            double dbaX = bX - aX, dbaY = bY - aY;
            double invDbaDir = 1.0 / (dbaY * dirX - dbaX * dirY);
            double t = (dbaX * doaY - dbaY * doaX) * invDbaDir;
            if (t >= 0.0 && t < nearestT) {
                double t2 = (doaY * dirX - doaX * dirY) * invDbaDir;
                if (t2 >= 0.0 && t2 <= 1.0) {
                    edgeIndex = (i - 1 + count) % count;
                    nearestT = t;
                    p.x = originX + t * dirX;
                    p.y = originY + t * dirY;
                }
            }
            aX = bX;
            aY = bY;
        }
        return edgeIndex;
    }

    /**
     * Determine whether the two lines, specified via two points lying on each line, intersect each other, and store the point of intersection
     * into the given vector <code>p</code>.
     * 
     * @param ps1x
     *          the x coordinate of the first point on the first line
     * @param ps1y
     *          the y coordinate of the first point on the first line
     * @param pe1x
     *          the x coordinate of the second point on the first line
     * @param pe1y
     *          the y coordinate of the second point on the first line
     * @param ps2x
     *          the x coordinate of the first point on the second line
     * @param ps2y
     *          the y coordinate of the first point on the second line
     * @param pe2x
     *          the x coordinate of the second point on the second line
     * @param pe2y
     *          the y coordinate of the second point on the second line
     * @param p
     *          will hold the point of intersection
     * @return <code>true</code> iff the two lines intersect; <code>false</code> otherwise
     */
    public static boolean intersectLineLine(double ps1x, double ps1y, double pe1x, double pe1y, double ps2x, double ps2y, double pe2x, double pe2y, Vector2d p) {
        double d1x = ps1x - pe1x;
        double d1y = pe1y - ps1y;
        double d1ps1 = d1y * ps1x + d1x * ps1y;
        double d2x = ps2x - pe2x;
        double d2y = pe2y - ps2y;
        double d2ps2 = d2y * ps2x + d2x * ps2y;
        double det = d1y * d2x - d2y * d1x;
        if (det == 0.0)
            return false;
        p.x = (d2x * d1ps1 - d1x * d2ps2) / det;
        p.y = (d1y * d2ps2 - d2y * d1ps1) / det;
        return true;
    }

    private static boolean separatingAxis(Vector2d[] v1s, Vector2d[] v2s, double aX, double aY) {
        double minA = Double.POSITIVE_INFINITY, maxA = Double.NEGATIVE_INFINITY;
        double minB = Double.POSITIVE_INFINITY, maxB = Double.NEGATIVE_INFINITY;
        int maxLen = Math.max(v1s.length, v2s.length);
        /* Project both polygons on axis */
        for (int k = 0; k < maxLen; k++) {
            if (k < v1s.length) {
                Vector2d v1 = v1s[k];
                double d = v1.x * aX + v1.y * aY;
                if (d < minA) minA = d;
                if (d > maxA) maxA = d;
            }
            if (k < v2s.length) {
                Vector2d v2 = v2s[k];
                double d = v2.x * aX + v2.y * aY;
                if (d < minB) minB = d;
                if (d > maxB) maxB = d;
            }
            /* Early-out if overlap found */
            if (minA <= maxB && minB <= maxA) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if the two polygons, given via their vertices, intersect.
     * 
     * @param v1s
     *          the vertices of the first polygon
     * @param v2s
     *          the vertices of the second polygon
     * @return <code>true</code> if the polygons intersect; <code>false</code> otherwise
     */
    public static boolean testPolygonPolygon(Vector2d[] v1s, Vector2d[] v2s) {
        /* Try to find a separating axis using the first polygon's edges */
        for (int i = 0, j = v1s.length - 1; i < v1s.length; j = i, i++) {
            Vector2d s = v1s[i], t = v1s[j];
            if (separatingAxis(v1s, v2s, s.y - t.y, t.x - s.x))
                return false;
        }
        /* Try to find a separating axis using the second polygon's edges */
        for (int i = 0, j = v2s.length - 1; i < v2s.length; j = i, i++) {
            Vector2d s = v2s[i], t = v2s[j];
            if (separatingAxis(v1s, v2s, s.y - t.y, t.x - s.x))
                return false;
        }
        return true;
    }

}
