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
 * Efficiently performs frustum intersection tests by caching the frustum planes of an arbitrary transformation {@link Matrix4fc matrix}.
 * <p>
 * This class is preferred over the frustum intersection methods in {@link Matrix4fc} when many objects need to be culled by the same static frustum.
 * 
 * @author Kai Burjack
 */
public class FrustumIntersection {

    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>x=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NX = 0;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>x=1</code> when using the identity frustum.
     */
    public static final int PLANE_PX = 1;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>y=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NY= 2;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>y=1</code> when using the identity frustum.
     */
    public static final int PLANE_PY = 3;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>z=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NZ = 4;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>z=1</code> when using the identity frustum.
     */
    public static final int PLANE_PZ = 5;

    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads indicating that the axis-aligned box intersects the frustum.
     */
    public static final int INTERSECT = -1;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads indicating that the axis-aligned box is fully inside of the frustum.
     */
    public static final int INSIDE = -2;
    /**
     * Return value of {@link #intersectSphere(Vector3fc, float)} or {@link #intersectSphere(float, float, float, float)}
     * indicating that the sphere is completely outside of the frustum.
     */
    public static final int OUTSIDE = -3;

    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>x=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NX = 1<<PLANE_NX;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>x=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PX = 1<<PLANE_PX;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>y=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NY = 1<<PLANE_NY;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>y=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PY = 1<<PLANE_PY;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>z=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NZ = 1<<PLANE_NZ;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>z=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PZ = 1<<PLANE_PZ;

    private float nxX, nxY, nxZ, nxW;
    private float pxX, pxY, pxZ, pxW;
    private float nyX, nyY, nyZ, nyW;
    private float pyX, pyY, pyZ, pyW;
    private float nzX, nzY, nzZ, nzW;
    private float pzX, pzY, pzZ, pzW;

    private final Vector4f[] planes = new Vector4f[6];
    {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Vector4f();
        }
    }

    /**
     * Create a new {@link FrustumIntersection} with undefined frustum planes.
     * <p>
     * Before using any of the frustum culling methods, make sure to define the frustum planes using {@link #set(Matrix4fc)}.
     */
    public FrustumIntersection() {
    }

    /**
     * Create a new {@link FrustumIntersection} from the given {@link Matrix4fc matrix} by extracing the matrix's frustum planes.
     * <p>
     * In order to update the compute frustum planes later on, call {@link #set(Matrix4fc)}.
     * 
     * @see #set(Matrix4fc)
     * 
     * @param m
     *          the {@link Matrix4fc} to create the frustum culler from
     */
    public FrustumIntersection(Matrix4fc m) {
        set(m, true);
    }

    /**
     * Create a new {@link FrustumIntersection} from the given {@link Matrix4fc matrix} by extracing the matrix's frustum planes.
     * <p>
     * In order to update the compute frustum planes later on, call {@link #set(Matrix4fc)}.
     * 
     * @see #set(Matrix4fc)
     * 
     * @param m
     *          the {@link Matrix4fc} to create the frustum culler from
     * @param allowTestSpheres
     *          whether the methods {@link #testSphere(Vector3fc, float)}, {@link #testSphere(float, float, float, float)},
     *          {@link #intersectSphere(Vector3fc, float)} or {@link #intersectSphere(float, float, float, float)} will used.
     *          If no spheres need to be tested, then <code>false</code> should be used 
     */
    public FrustumIntersection(Matrix4fc m, boolean allowTestSpheres) {
        set(m, allowTestSpheres);
    }

    /**
     * Update the stored frustum planes of <code>this</code> {@link FrustumIntersection} with the given {@link Matrix4fc matrix}.
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @param m
     *          the {@link Matrix4fc matrix} to update <code>this</code> frustum culler's frustum planes from
     * @return this
     */
    public FrustumIntersection set(Matrix4fc m) {
        return set(m, true);
    }

    /**
     * Update the stored frustum planes of <code>this</code> {@link FrustumIntersection} with the given {@link Matrix4fc matrix} and
     * allow to optimize the frustum plane extraction in the case when no intersection test is needed for spheres.
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @param m
     *          the {@link Matrix4fc matrix} to update <code>this</code> frustum culler's frustum planes from
     * @param allowTestSpheres
     *          whether the methods {@link #testSphere(Vector3fc, float)}, {@link #testSphere(float, float, float, float)},
     *          {@link #intersectSphere(Vector3fc, float)} or {@link #intersectSphere(float, float, float, float)} will be used.
     *          If no spheres need to be tested, then <code>false</code> should be used
     * @return this
     */
    public FrustumIntersection set(Matrix4fc m, boolean allowTestSpheres) {
        float invl;
        nxX = m.m03() + m.m00(); nxY = m.m13() + m.m10(); nxZ = m.m23() + m.m20(); nxW = m.m33() + m.m30();
        if (allowTestSpheres) {
            invl = Math.invsqrt(nxX * nxX + nxY * nxY + nxZ * nxZ);
            nxX *= invl; nxY *= invl; nxZ *= invl; nxW *= invl;
        }
        planes[0].set(nxX, nxY, nxZ, nxW);
        pxX = m.m03() - m.m00(); pxY = m.m13() - m.m10(); pxZ = m.m23() - m.m20(); pxW = m.m33() - m.m30();
        if (allowTestSpheres) {
            invl = Math.invsqrt(pxX * pxX + pxY * pxY + pxZ * pxZ);
            pxX *= invl; pxY *= invl; pxZ *= invl; pxW *= invl;
        }
        planes[1].set(pxX, pxY, pxZ, pxW);
        nyX = m.m03() + m.m01(); nyY = m.m13() + m.m11(); nyZ = m.m23() + m.m21(); nyW = m.m33() + m.m31();
        if (allowTestSpheres) {
            invl = Math.invsqrt(nyX * nyX + nyY * nyY + nyZ * nyZ);
            nyX *= invl; nyY *= invl; nyZ *= invl; nyW *= invl;
        }
        planes[2].set(nyX, nyY, nyZ, nyW);
        pyX = m.m03() - m.m01(); pyY = m.m13() - m.m11(); pyZ = m.m23() - m.m21(); pyW = m.m33() - m.m31();
        if (allowTestSpheres) {
            invl = Math.invsqrt(pyX * pyX + pyY * pyY + pyZ * pyZ);
            pyX *= invl; pyY *= invl; pyZ *= invl; pyW *= invl;
        }
        planes[3].set(pyX, pyY, pyZ, pyW);
        nzX = m.m03() + m.m02(); nzY = m.m13() + m.m12(); nzZ = m.m23() + m.m22(); nzW = m.m33() + m.m32();
        if (allowTestSpheres) {
            invl = Math.invsqrt(nzX * nzX + nzY * nzY + nzZ * nzZ);
            nzX *= invl; nzY *= invl; nzZ *= invl; nzW *= invl;
        }
        planes[4].set(nzX, nzY, nzZ, nzW);
        pzX = m.m03() - m.m02(); pzY = m.m13() - m.m12(); pzZ = m.m23() - m.m22(); pzW = m.m33() - m.m32();
        if (allowTestSpheres) {
            invl = Math.invsqrt(pzX * pzX + pzY * pzY + pzZ * pzZ);
            pzX *= invl; pzY *= invl; pzZ *= invl; pzW *= invl;
        }
        planes[5].set(pzX, pzY, pzZ, pzW);
        return this;
    }

    /**
     * Test whether the given point is within the frustum defined by <code>this</code> frustum culler.
     * 
     * @param point
     *          the point to test
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    public boolean testPoint(Vector3fc point) {
        return testPoint(point.x(), point.y(), point.z());
    }

    /**
     * Test whether the given point <code>(x, y, z)</code> is within the frustum defined by <code>this</code> frustum culler.
     * 
     * @param x
     *          the x-coordinate of the point
     * @param y
     *          the y-coordinate of the point
     * @param z
     *          the z-coordinate of the point
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    public boolean testPoint(float x, float y, float z) {
        return nxX * x + nxY * y + nxZ * z + nxW >= 0 &&
               pxX * x + pxY * y + pxZ * z + pxW >= 0 &&
               nyX * x + nyY * y + nyZ * z + nyW >= 0 &&
               pyX * x + pyY * y + pyZ * z + pyW >= 0 &&
               nzX * x + nzY * y + nzZ * z + nzW >= 0 &&
               pzX * x + pzY * y + pzZ * z + pzW >= 0;
    }

    /**
     * Test whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param center
     *          the sphere's center
     * @param radius
     *          the sphere's radius
     * @return <code>true</code> if the given sphere is partly or completely inside the frustum;
     *         <code>false</code> otherwise
     */
    public boolean testSphere(Vector3fc center, float radius) {
        return testSphere(center.x(), center.y(), center.z(), radius);
    }

    /**
     * Test whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param x
     *          the x-coordinate of the sphere's center
     * @param y
     *          the y-coordinate of the sphere's center
     * @param z
     *          the z-coordinate of the sphere's center
     * @param r
     *          the sphere's radius
     * @return <code>true</code> if the given sphere is partly or completely inside the frustum;
     *         <code>false</code> otherwise
     */
    public boolean testSphere(float x, float y, float z, float r) {
        return nxX * x + nxY * y + nxZ * z + nxW >= -r &&
               pxX * x + pxY * y + pxZ * z + pxW >= -r &&
               nyX * x + nyY * y + nyZ * z + nyW >= -r &&
               pyX * x + pyY * y + pyZ * z + pyW >= -r &&
               nzX * x + nzY * y + nzZ * z + nzW >= -r &&
               pzX * x + pzY * y + pzZ * z + pzW >= -r;
    }

    /**
     * Determine whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param center
     *          the sphere's center
     * @param radius
     *          the sphere's radius
     * @return {@link #INSIDE} if the given sphere is completely inside the frustum, or {@link #INTERSECT} if the sphere intersects
     *         the frustum, or {@link #OUTSIDE} if the sphere is outside of the frustum
     */
    public int intersectSphere(Vector3fc center, float radius) {
        return intersectSphere(center.x(), center.y(), center.z(), radius);
    }

    /**
     * Determine whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param x
     *          the x-coordinate of the sphere's center
     * @param y
     *          the y-coordinate of the sphere's center
     * @param z
     *          the z-coordinate of the sphere's center
     * @param r
     *          the sphere's radius
     * @return {@link #INSIDE} if the given sphere is completely inside the frustum, or {@link #INTERSECT} if the sphere intersects
     *         the frustum, or {@link #OUTSIDE} if the sphere is outside of the frustum
     */
    public int intersectSphere(float x, float y, float z, float r) {
        boolean inside = true;
        float dist;
        dist = nxX * x + nxY * y + nxZ * z + nxW;
        if (dist >= -r) {
            inside &= dist >= r;
            dist = pxX * x + pxY * y + pxZ * z + pxW;
            if (dist >= -r) {
                inside &= dist >= r;
                dist = nyX * x + nyY * y + nyZ * z + nyW;
                if (dist >= -r) {
                    inside &= dist >= r;
                    dist = pyX * x + pyY * y + pyZ * z + pyW;
                    if (dist >= -r) {
                        inside &= dist >= r;
                        dist = nzX * x + nzY * y + nzZ * z + nzW;
                        if (dist >= -r) {
                            inside &= dist >= r;
                            dist = pzX * x + pzY * y + pzZ * z + pzW;
                            if (dist >= -r) {
                                inside &= dist >= r;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return OUTSIDE;
    }

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param min
     *          the minimum corner coordinates of the axis-aligned box
     * @param max
     *          the maximum corner coordinates of the axis-aligned box
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    public boolean testAab(Vector3fc min, Vector3fc max) {
        return testAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * It does not distinguish between partially inside and fully inside, though, so the test with the 'p' vertex is omitted.
         */
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW &&
               pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW &&
               nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW &&
               pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW &&
               nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW &&
               pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW;
    }

    /**
     * Test whether the given XY-plane (at <code>Z = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param min
     *          the minimum corner coordinates of the XY-plane
     * @param max
     *          the maximum corner coordinates of the XY-plane
     * @return <code>true</code> if the XY-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    public boolean testPlaneXY(Vector2fc min, Vector2fc max) {
        return testPlaneXY(min.x(), min.y(), max.x(), max.y());
    }

    /**
     * Test whether the given XY-plane (at <code>Z = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @return <code>true</code> if the XY-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    public boolean testPlaneXY(float minX, float minY, float maxX, float maxY) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * It does not distinguish between partially inside and fully inside, though, so the test with the 'p' vertex is omitted.
         */
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) >= -nxW &&
               pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) >= -pxW &&
               nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) >= -nyW &&
               pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) >= -pyW &&
               nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) >= -nzW &&
               pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) >= -pzW;
    }

    /**
     * Test whether the given XZ-plane (at <code>Y = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @return <code>true</code> if the XZ-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    public boolean testPlaneXZ(float minX, float minZ, float maxX, float maxZ) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * It does not distinguish between partially inside and fully inside, though, so the test with the 'p' vertex is omitted.
         */
        return nxX * (nxX < 0 ? minX : maxX) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW &&
               pxX * (pxX < 0 ? minX : maxX) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW &&
               nyX * (nyX < 0 ? minX : maxX) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW &&
               pyX * (pyX < 0 ? minX : maxX) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW &&
               nzX * (nzX < 0 ? minX : maxX) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW &&
               pzX * (pzX < 0 ? minX : maxX) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW;
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param min
     *          the minimum corner coordinates of the axis-aligned box
     * @param max
     *          the maximum corner coordinates of the axis-aligned box
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum;
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(Vector3fc min, Vector3fc max) {
        return intersectAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum,
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * 
         * In addition to the algorithm in the paper, this method also returns the index of the first plane that culled the box.
         */
        int plane = PLANE_NX;
        boolean inside = true;
        if (nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW) {
            plane = PLANE_PX;
            inside &= nxX * (nxX < 0 ? maxX : minX) + nxY * (nxY < 0 ? maxY : minY) + nxZ * (nxZ < 0 ? maxZ : minZ) >= -nxW;
            if (pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW) {
                plane = PLANE_NY;
                inside &= pxX * (pxX < 0 ? maxX : minX) + pxY * (pxY < 0 ? maxY : minY) + pxZ * (pxZ < 0 ? maxZ : minZ) >= -pxW;
                if (nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW) {
                    plane = PLANE_PY;
                    inside &= nyX * (nyX < 0 ? maxX : minX) + nyY * (nyY < 0 ? maxY : minY) + nyZ * (nyZ < 0 ? maxZ : minZ) >= -nyW;
                    if (pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW) {
                        plane = PLANE_NZ;
                        inside &= pyX * (pyX < 0 ? maxX : minX) + pyY * (pyY < 0 ? maxY : minY) + pyZ * (pyZ < 0 ? maxZ : minZ) >= -pyW;
                        if (nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW) {
                            plane = PLANE_PZ;
                            inside &= nzX * (nzX < 0 ? maxX : minX) + nzY * (nzY < 0 ? maxY : minY) + nzZ * (nzZ < 0 ? maxZ : minZ) >= -nzW;
                            if (pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW) {
                                inside &= pzX * (pzX < 0 ? maxX : minX) + pzY * (pzY < 0 ? maxY : minY) + pzZ * (pzZ < 0 ? maxZ : minZ) >= -pzW;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return plane;
    }

    /**
     * Compute the signed distance from the given axis-aligned box to the <code>plane</code>.
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @param plane
     *          one of 
     *          {@link #PLANE_NX}, {@link #PLANE_PX},
     *          {@link #PLANE_NY}, {@link #PLANE_PY}, 
     *          {@link #PLANE_NZ} and {@link #PLANE_PZ}
     * @return the signed distance of the axis-aligned box to the plane
     */
    public float distanceToPlane(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int plane) {
        return planes[plane].x * (planes[plane].x < 0 ? maxX : minX) + planes[plane].y * (planes[plane].y < 0 ? maxY : minY)
                + planes[plane].z * (planes[plane].z < 0 ? maxZ : minZ) + planes[plane].w;
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * This method differs from {@link #intersectAab(Vector3fc, Vector3fc)} in that
     * it allows to mask-off planes that should not be calculated. For example, in order to only test a box against the
     * left frustum plane, use a mask of {@link #PLANE_MASK_NX}. Or in order to test all planes <i>except</i> the left plane, use 
     * a mask of <code>(~0 ^ PLANE_MASK_NX)</code>.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param min
     *          the minimum corner coordinates of the axis-aligned box
     * @param max
     *          the maximum corner coordinates of the axis-aligned box
     * @param mask
     *          contains as bitset all the planes that should be tested.
     *          This value can be any combination of 
     *          {@link #PLANE_MASK_NX}, {@link #PLANE_MASK_PX},
     *          {@link #PLANE_MASK_NY}, {@link #PLANE_MASK_PY}, 
     *          {@link #PLANE_MASK_NZ} and {@link #PLANE_MASK_PZ}
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum,
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(Vector3fc min, Vector3fc max, int mask) {
        return intersectAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), mask);
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its min and max corner coordinates.
     * <p>
     * This method differs from {@link #intersectAab(float, float, float, float, float, float)} in that
     * it allows to mask-off planes that should not be calculated. For example, in order to only test a box against the
     * left frustum plane, use a mask of {@link #PLANE_MASK_NX}. Or in order to test all planes <i>except</i> the left plane, use 
     * a mask of <code>(~0 ^ PLANE_MASK_NX)</code>.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @param mask
     *          contains as bitset all the planes that should be tested.
     *          This value can be any combination of 
     *          {@link #PLANE_MASK_NX}, {@link #PLANE_MASK_PX},
     *          {@link #PLANE_MASK_NY}, {@link #PLANE_MASK_PY}, 
     *          {@link #PLANE_MASK_NZ} and {@link #PLANE_MASK_PZ}
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum,
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int mask) {
        /*
         * This is an implementation of the first algorithm in "2.5 Plane masking and coherency" of the mentioned site.
         * 
         * In addition to the algorithm in the paper, this method also returns the index of the first plane that culled the box.
         */
        int plane = PLANE_NX;
        boolean inside = true;
        if ((mask & PLANE_MASK_NX) == 0 || nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW) {
            plane = PLANE_PX;
            inside &= nxX * (nxX < 0 ? maxX : minX) + nxY * (nxY < 0 ? maxY : minY) + nxZ * (nxZ < 0 ? maxZ : minZ) >= -nxW;
            if ((mask & PLANE_MASK_PX) == 0 || pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW) {
                plane = PLANE_NY;
                inside &= pxX * (pxX < 0 ? maxX : minX) + pxY * (pxY < 0 ? maxY : minY) + pxZ * (pxZ < 0 ? maxZ : minZ) >= -pxW;
                if ((mask & PLANE_MASK_NY) == 0 || nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW) {
                    plane = PLANE_PY;
                    inside &= nyX * (nyX < 0 ? maxX : minX) + nyY * (nyY < 0 ? maxY : minY) + nyZ * (nyZ < 0 ? maxZ : minZ) >= -nyW;
                    if ((mask & PLANE_MASK_PY) == 0 || pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW) {
                        plane = PLANE_NZ;
                        inside &= pyX * (pyX < 0 ? maxX : minX) + pyY * (pyY < 0 ? maxY : minY) + pyZ * (pyZ < 0 ? maxZ : minZ) >= -pyW;
                        if ((mask & PLANE_MASK_NZ) == 0 || nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW) {
                            plane = PLANE_PZ;
                            inside &= nzX * (nzX < 0 ? maxX : minX) + nzY * (nzY < 0 ? maxY : minY) + nzZ * (nzZ < 0 ? maxZ : minZ) >= -nzW;
                            if ((mask & PLANE_MASK_PZ) == 0 || pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW) {
                                inside &= pzX * (pzX < 0 ? maxX : minX) + pzY * (pzY < 0 ? maxY : minY) + pzZ * (pzZ < 0 ? maxZ : minZ) >= -pzW;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return plane;
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * This method differs from {@link #intersectAab(Vector3fc, Vector3fc)} in that
     * it allows to mask-off planes that should not be calculated. For example, in order to only test a box against the
     * left frustum plane, use a mask of {@link #PLANE_MASK_NX}. Or in order to test all planes <i>except</i> the left plane, use 
     * a mask of <code>(~0 ^ PLANE_MASK_NX)</code>.
     * <p>
     * In addition, the <code>startPlane</code> denotes the first frustum plane to test the box against. To use this effectively means to store the
     * plane that previously culled an axis-aligned box (as returned by <code>intersectAab()</code>) and in the next frame use the return value
     * as the argument to the <code>startPlane</code> parameter of this method. The assumption is that the plane that culled the object previously will also
     * cull it now (temporal coherency) and the culling computation is likely reduced in that case.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * 
     * @param min
     *          the minimum corner coordinates of the axis-aligned box
     * @param max
     *          the maximum corner coordinates of the axis-aligned box
     * @param mask
     *          contains as bitset all the planes that should be tested.
     *          This value can be any combination of 
     *          {@link #PLANE_MASK_NX}, {@link #PLANE_MASK_PX},
     *          {@link #PLANE_MASK_NY}, {@link #PLANE_MASK_PY}, 
     *          {@link #PLANE_MASK_NZ} and {@link #PLANE_MASK_PZ}
     * @param startPlane
     *          the first frustum plane to test the axis-aligned box against. It is one of
     *          {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum,
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(Vector3fc min, Vector3fc max, int mask, int startPlane) {
        return intersectAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), mask, startPlane);
    }

    /**
     * Determine whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler
     * and, if the box is not inside this frustum, return the index of the plane that culled it.
     * The box is specified via its min and max corner coordinates.
     * <p>
     * This method differs from {@link #intersectAab(float, float, float, float, float, float)} in that
     * it allows to mask-off planes that should not be calculated. For example, in order to only test a box against the
     * left frustum plane, use a mask of {@link #PLANE_MASK_NX}. Or in order to test all planes <i>except</i> the left plane, use 
     * a mask of <code>(~0 ^ PLANE_MASK_NX)</code>.
     * <p>
     * In addition, the <code>startPlane</code> denotes the first frustum plane to test the box against. To use this effectively means to store the
     * plane that previously culled an axis-aligned box (as returned by <code>intersectAab()</code>) and in the next frame use the return value
     * as the argument to the <code>startPlane</code> parameter of this method. The assumption is that the plane that culled the object previously will also
     * cull it now (temporal coherency) and the culling computation is likely reduced in that case.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns {@link #INTERSECT} for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param minZ
     *          the z-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @param maxZ
     *          the z-coordinate of the maximum corner
     * @param mask
     *          contains as bitset all the planes that should be tested.
     *          This value can be any combination of 
     *          {@link #PLANE_MASK_NX}, {@link #PLANE_MASK_PX},
     *          {@link #PLANE_MASK_NY}, {@link #PLANE_MASK_PY}, 
     *          {@link #PLANE_MASK_NZ} and {@link #PLANE_MASK_PZ}
     * @param startPlane
     *          the first frustum plane to test the axis-aligned box against. It is one of
     *          {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     * @return the index of the first plane that culled the box, if the box does not intersect the frustum,
     *         or {@link #INTERSECT} if the box intersects the frustum, or {@link #INSIDE} if the box is fully inside of the frustum.
     *         The plane index is one of {@link #PLANE_NX}, {@link #PLANE_PX}, {@link #PLANE_NY}, {@link #PLANE_PY}, {@link #PLANE_NZ} and {@link #PLANE_PZ}
     */
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int mask, int startPlane) {
        /*
         * This is an implementation of the second algorithm in "2.5 Plane masking and coherency" of the mentioned site.
         * 
         * In addition to the algorithm in the paper, this method also returns the index of the first plane that culled the box.
         */
        int plane = startPlane;
        boolean inside = true;
        Vector4f p = planes[startPlane];
        if ((mask & 1<<startPlane) != 0 && p.x * (p.x < 0 ? minX : maxX) + p.y * (p.y < 0 ? minY : maxY) + p.z * (p.z < 0 ? minZ : maxZ) < -p.w) {
            return plane;
        }
        if ((mask & PLANE_MASK_NX) == 0 || nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW) {
            plane = PLANE_PX;
            inside &= nxX * (nxX < 0 ? maxX : minX) + nxY * (nxY < 0 ? maxY : minY) + nxZ * (nxZ < 0 ? maxZ : minZ) >= -nxW;
            if ((mask & PLANE_MASK_PX) == 0 || pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW) {
                plane = PLANE_NY;
                inside &= pxX * (pxX < 0 ? maxX : minX) + pxY * (pxY < 0 ? maxY : minY) + pxZ * (pxZ < 0 ? maxZ : minZ) >= -pxW;
                if ((mask & PLANE_MASK_NY) == 0 || nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW) {
                    plane = PLANE_PY;
                    inside &= nyX * (nyX < 0 ? maxX : minX) + nyY * (nyY < 0 ? maxY : minY) + nyZ * (nyZ < 0 ? maxZ : minZ) >= -nyW;
                    if ((mask & PLANE_MASK_PY) == 0 || pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW) {
                        plane = PLANE_NZ;
                        inside &= pyX * (pyX < 0 ? maxX : minX) + pyY * (pyY < 0 ? maxY : minY) + pyZ * (pyZ < 0 ? maxZ : minZ) >= -pyW;
                        if ((mask & PLANE_MASK_NZ) == 0 || nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW) {
                            plane = PLANE_PZ;
                            inside &= nzX * (nzX < 0 ? maxX : minX) + nzY * (nzY < 0 ? maxY : minY) + nzZ * (nzZ < 0 ? maxZ : minZ) >= -nzW;
                            if ((mask & PLANE_MASK_PZ) == 0 || pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW) {
                                inside &= pzX * (pzX < 0 ? maxX : minX) + pzY * (pzY < 0 ? maxY : minY) + pzZ * (pzZ < 0 ? maxZ : minZ) >= -pzW;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return plane;
    }

}
