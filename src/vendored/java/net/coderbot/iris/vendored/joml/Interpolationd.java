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
 * Contains various interpolation functions.
 * 
 * @author Kai Burjack
 */
public class Interpolationd {

    /**
     * Bilinearly interpolate the single scalar value <i>f</i> over the given triangle.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Barycentric_coordinate_system">https://en.wikipedia.org/</a>
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param f0
     *            the value of <i>f</i> at the first vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param f1
     *            the value of <i>f</i> at the second vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param f2
     *            the value of <i>f</i> at the third vertex
     * @param x
     *            the x coordinate of the point to interpolate <i>f</i> at
     * @param y
     *            the y coordinate of the point to interpolate <i>f</i> at
     * @return the interpolated value of <i>f</i>
     */
    public static double interpolateTriangle(
            double v0X, double v0Y, double f0,
            double v1X, double v1Y, double f1,
            double v2X, double v2Y, double f2,
            double x, double y) {
        double v12Y = v1Y - v2Y;
        double v21X = v2X - v1X;
        double v02X = v0X - v2X;
        double yv2Y = y - v2Y;
        double xv2X = x - v2X;
        double v02Y = v0Y - v2Y;
        double invDen = 1.0 / (v12Y * v02X + v21X * v02Y);
        double l1 = (v12Y * xv2X + v21X * yv2Y) * invDen;
        double l2 = (v02X * yv2Y - v02Y * xv2X) * invDen;
        return l1 * f0 + l2 * f1 + (1.0f - l1 - l2) * f2;
    }

    /**
     * Bilinearly interpolate the two-dimensional vector <i>f</i> over the given triangle and store the result in <code>dest</code>.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Barycentric_coordinate_system">https://en.wikipedia.org/</a>
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param f0X
     *            the x component of the value of <i>f</i> at the first vertex
     * @param f0Y
     *            the y component of the value of <i>f</i> at the first vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param f1X
     *            the x component of the value of <i>f</i> at the second vertex
     * @param f1Y
     *            the y component of the value of <i>f</i> at the second vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param f2X
     *            the x component of the value of <i>f</i> at the third vertex
     * @param f2Y
     *            the y component of the value of <i>f</i> at the third vertex
     * @param x
     *            the x coordinate of the point to interpolate <i>f</i> at
     * @param y
     *            the y coordinate of the point to interpolate <i>f</i> at
     * @param dest
     *            will hold the interpolation result
     * @return dest
     */
    public static Vector2d interpolateTriangle(
            double v0X, double v0Y, double f0X, double f0Y,
            double v1X, double v1Y, double f1X, double f1Y,
            double v2X, double v2Y, double f2X, double f2Y,
            double x, double y, Vector2d dest) {
        double v12Y = v1Y - v2Y;
        double v21X = v2X - v1X;
        double v02X = v0X - v2X;
        double yv2Y = y - v2Y;
        double xv2X = x - v2X;
        double v02Y = v0Y - v2Y;
        double invDen = 1.0 / (v12Y * v02X + v21X * v02Y);
        double l1 = (v12Y * xv2X + v21X * yv2Y) * invDen;
        double l2 = (v02X * yv2Y - v02Y * xv2X) * invDen;
        double l3 = 1.0 - l1 - l2;
        dest.x = l1 * f0X + l2 * f1X + l3 * f2X;
        dest.y = l1 * f0Y + l2 * f1Y + l3 * f2Y;
        return dest;
    }

    /**
     * Compute the first-order derivative of a linear two-dimensional function <i>f</i> with respect to X
     * and store the result in <code>dest</code>.
     * <p>
     * This method computes the constant rate of change for <i>f</i> given the three values of <i>f</i>
     * at the specified three inputs <code>(v0X, v0Y)</code>, <code>(v1X, v1Y)</code> and <code>(v2X, v2Y)</code>.
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param f0X
     *            the x component of the value of <i>f</i> at the first vertex
     * @param f0Y
     *            the y component of the value of <i>f</i> at the first vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param f1X
     *            the x component of the value of <i>f</i> at the second vertex
     * @param f1Y
     *            the y component of the value of <i>f</i> at the second vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param f2X
     *            the x component of the value of <i>f</i> at the third vertex
     * @param f2Y
     *            the y component of the value of <i>f</i> at the third vertex
     * @param dest
     *            will hold the result
     * @return dest
     */
    public static Vector2d dFdxLinear(
            double v0X, double v0Y, double f0X, double f0Y,
            double v1X, double v1Y, double f1X, double f1Y,
            double v2X, double v2Y, double f2X, double f2Y, Vector2d dest) {
        double v12Y = v1Y - v2Y;
        double v02Y = v0Y - v2Y;
        double den = v12Y * (v0X - v2X) + (v2X - v1X) * v02Y;
        double l3_1 = den - v12Y + v02Y;
        double invDen = 1.0f / den;
        dest.x = invDen * (v12Y * f0X - v02Y * f1X + l3_1 * f2X) - f2X;
        dest.y = invDen * (v12Y * f0Y - v02Y * f1Y + l3_1 * f2Y) - f2Y;
        return dest;
    }

    /**
     * Compute the first-order derivative of a linear two-dimensional function <i>f</i> with respect to Y
     * and store the result in <code>dest</code>.
     * <p>
     * This method computes the constant rate of change for <i>f</i> given the three values of <i>f</i>
     * at the specified three inputs <code>(v0X, v0Y)</code>, <code>(v1X, v1Y)</code> and <code>(v2X, v2Y)</code>.
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param f0X
     *            the x component of the value of <i>f</i> at the first vertex
     * @param f0Y
     *            the y component of the value of <i>f</i> at the first vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param f1X
     *            the x component of the value of <i>f</i> at the second vertex
     * @param f1Y
     *            the y component of the value of <i>f</i> at the second vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param f2X
     *            the x component of the value of <i>f</i> at the third vertex
     * @param f2Y
     *            the y component of the value of <i>f</i> at the third vertex
     * @param dest
     *            will hold the result
     * @return dest
     */
    public static Vector2d dFdyLinear(
            double v0X, double v0Y, double f0X, double f0Y,
            double v1X, double v1Y, double f1X, double f1Y,
            double v2X, double v2Y, double f2X, double f2Y,
            Vector2d dest) {
        double v21X = v2X - v1X;
        double v02X = v0X - v2X;
        double den = (v1Y - v2Y) * v02X + v21X * (v0Y - v2Y);
        double l3_1 = den - v21X - v02X;
        double invDen = 1.0f / den;
        dest.x = invDen * (v21X * f0X + v02X * f1X + l3_1 * f2X) - f2X;
        dest.y = invDen * (v21X * f0Y + v02X * f1Y + l3_1 * f2Y) - f2Y;
        return dest;
    }

    /**
     * Bilinearly interpolate the three-dimensional vector <i>f</i> over the given triangle and store the result in <code>dest</code>.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Barycentric_coordinate_system">https://en.wikipedia.org/</a>
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param f0X
     *            the x component of the value of <i>f</i> at the first vertex
     * @param f0Y
     *            the y component of the value of <i>f</i> at the first vertex
     * @param f0Z
     *            the z component of the value of <i>f</i> at the first vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param f1X
     *            the x component of the value of <i>f</i> at the second vertex
     * @param f1Y
     *            the y component of the value of <i>f</i> at the second vertex
     * @param f1Z
     *            the z component of the value of <i>f</i> at the second vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param f2X
     *            the x component of the value of <i>f</i> at the third vertex
     * @param f2Y
     *            the y component of the value of <i>f</i> at the third vertex
     * @param f2Z
     *            the z component of the value of <i>f</i> at the third vertex
     * @param x
     *            the x coordinate of the point to interpolate <i>f</i> at
     * @param y
     *            the y coordinate of the point to interpolate <i>f</i> at
     * @param dest
     *            will hold the interpolation result
     * @return dest
     */
    public static Vector3d interpolateTriangle(
            double v0X, double v0Y, double f0X, double f0Y, double f0Z,
            double v1X, double v1Y, double f1X, double f1Y, double f1Z,
            double v2X, double v2Y, double f2X, double f2Y, double f2Z,
            double x, double y, Vector3d dest) {
        // compute interpolation factors
        Vector3d t = dest;
        interpolationFactorsTriangle(v0X, v0Y, v1X, v1Y, v2X, v2Y, x, y, t);
        // interpolate using these factors
        return dest.set(t.x * f0X + t.y * f1X + t.z * f2X,
                        t.x * f0Y + t.y * f1Y + t.z * f2Y,
                        t.x * f0Z + t.y * f1Z + t.z * f2Z);
    }

    /**
     * Compute the interpolation factors <code>(t0, t1, t2)</code> in order to interpolate an arbitrary value over a given 
     * triangle at the given point <code>(x, y)</code>.
     * <p>
     * This method takes in the 2D vertex positions of the three vertices of a triangle and stores in <code>dest</code> the 
     * factors <code>(t0, t1, t2)</code> in the equation <code>v' = v0 * t0 + v1 * t1 + v2 * t2</code> where <code>(v0, v1, v2)</code> are
     * arbitrary (scalar or vector) values associated with the respective vertices of the triangle. The computed value <code>v'</code>
     * is the interpolated value at the given position <code>(x, y)</code>.
     * 
     * @param v0X
     *            the x coordinate of the first triangle vertex
     * @param v0Y
     *            the y coordinate of the first triangle vertex
     * @param v1X
     *            the x coordinate of the second triangle vertex
     * @param v1Y
     *            the y coordinate of the second triangle vertex
     * @param v2X
     *            the x coordinate of the third triangle vertex
     * @param v2Y
     *            the y coordinate of the third triangle vertex
     * @param x
     *            the x coordinate of the point to interpolate at
     * @param y
     *            the y coordinate of the point to interpolate at
     * @param dest
     *            will hold the interpolation factors <code>(t0, t1, t2)</code>
     * @return dest
     */
    public static Vector3d interpolationFactorsTriangle(
            double v0X, double v0Y, double v1X, double v1Y, double v2X, double v2Y,
            double x, double y, Vector3d dest) {
        double v12Y = v1Y - v2Y;
        double v21X = v2X - v1X;
        double v02X = v0X - v2X;
        double yv2Y = y - v2Y;
        double xv2X = x - v2X;
        double v02Y = v0Y - v2Y;
        double invDen = 1.0 / (v12Y * v02X + v21X * v02Y);
        dest.x = (v12Y * xv2X + v21X * yv2Y) * invDen;
        dest.y = (v02X * yv2Y - v02Y * xv2X) * invDen;
        dest.z = 1.0 - dest.x - dest.y;
        return dest;
    }

}
