/*
 * The MIT License
 *
 * Copyright (c) 2016-2021 JOML
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
 * Computes the weighted average of multiple rotations represented as {@link Quaterniond} instances.
 * <p>
 * Instances of this class are <i>not</i> thread-safe.
 * 
 * @author Kai Burjack
 */
public class QuaterniondInterpolator {

    /**
     * Performs singular value decomposition on {@link Matrix3d}.
     * <p>
     * This code was adapted from <a href="http://www.public.iastate.edu/~dicook/JSS/paper/code/svd.c">http://www.public.iastate.edu/</a>.
     * 
     * @author Kai Burjack
     */
    private static class SvdDecomposition3d {
        private final double rv1[];
        private final double w[];
        private final double v[];

        SvdDecomposition3d() {
            this.rv1 = new double[3];
            this.w = new double[3];
            this.v = new double[9];
        }

        private double SIGN(double a, double b) {
            return (b) >= 0.0 ? java.lang.Math.abs(a) : -java.lang.Math.abs(a);
        }

        void svd(double[] a, int maxIterations, Matrix3d destU, Matrix3d destV) {
            int flag, i, its, j, jj, k, l = 0, nm = 0;
            double c, f, h, s, x, y, z;
            double anorm = 0.0, g = 0.0, scale = 0.0;
            /* Householder reduction to bidiagonal form */
            for (i = 0; i < 3; i++) {
                /* left-hand reduction */
                l = i + 1;
                rv1[i] = scale * g;
                g = s = scale = 0.0;
                for (k = i; k < 3; k++)
                    scale += java.lang.Math.abs(a[k + 3 * i]);
                if (scale != 0.0) {
                    for (k = i; k < 3; k++) {
                        a[k + 3 * i] = (a[k + 3 * i] / scale);
                        s += (a[k + 3 * i] * a[k + 3 * i]);
                    }
                    f = a[i + 3 * i];
                    g = -SIGN(java.lang.Math.sqrt(s), f);
                    h = f * g - s;
                    a[i + 3 * i] = f - g;
                    if (i != 3 - 1) {
                        for (j = l; j < 3; j++) {
                            for (s = 0.0, k = i; k < 3; k++)
                                s += a[k + 3 * i] * a[k + 3 * j];
                            f = s / h;
                            for (k = i; k < 3; k++)
                                a[k + 3 * j] += f * a[k + 3 * i];
                        }
                    }
                    for (k = i; k < 3; k++)
                        a[k + 3 * i] = a[k + 3 * i] * scale;
                }
                w[i] = (scale * g);

                /* right-hand reduction */
                g = s = scale = 0.0;
                if (i < 3 && i != 3 - 1) {
                    for (k = l; k < 3; k++)
                        scale += java.lang.Math.abs(a[i + 3 * k]);
                    if (scale != 0.0) {
                        for (k = l; k < 3; k++) {
                            a[i + 3 * k] = a[i + 3 * k] / scale;
                            s += a[i + 3 * k] * a[i + 3 * k];
                        }
                        f = a[i + 3 * l];
                        g = -SIGN(java.lang.Math.sqrt(s), f);
                        h = f * g - s;
                        a[i + 3 * l] = f - g;
                        for (k = l; k < 3; k++)
                            rv1[k] = a[i + 3 * k] / h;
                        if (i != 3 - 1) {
                            for (j = l; j < 3; j++) {
                                for (s = 0.0, k = l; k < 3; k++)
                                    s += a[j + 3 * k] * a[i + 3 * k];
                                for (k = l; k < 3; k++)
                                    a[j + 3 * k] += s * rv1[k];
                            }
                        }
                        for (k = l; k < 3; k++)
                            a[i + 3 * k] = a[i + 3 * k] * scale;
                    }
                }
                anorm = java.lang.Math.max(anorm, (java.lang.Math.abs(w[i]) + java.lang.Math.abs(rv1[i])));
            }

            /* accumulate the right-hand transformation */
            for (i = 3 - 1; i >= 0; i--) {
                if (i < 3 - 1) {
                    if (g != 0.0) {
                        for (j = l; j < 3; j++)
                            v[j + 3 * i] = (a[i + 3 * j] / a[i + 3 * l]) / g;
                        /* double division to avoid underflow */
                        for (j = l; j < 3; j++) {
                            for (s = 0.0, k = l; k < 3; k++)
                                s += a[i + 3 * k] * v[k + 3 * j];
                            for (k = l; k < 3; k++)
                                v[k + 3 * j] += s * v[k + 3 * i];
                        }
                    }
                    for (j = l; j < 3; j++)
                        v[i + 3 * j] = v[j + 3 * i] = 0.0;
                }
                v[i + 3 * i] = 1.0;
                g = rv1[i];
                l = i;
            }

            /* accumulate the left-hand transformation */
            for (i = 3 - 1; i >= 0; i--) {
                l = i + 1;
                g = w[i];
                if (i < 3 - 1)
                    for (j = l; j < 3; j++)
                        a[i + 3 * j] = 0.0;
                if (g != 0.0) {
                    g = 1.0 / g;
                    if (i != 3 - 1) {
                        for (j = l; j < 3; j++) {
                            for (s = 0.0, k = l; k < 3; k++)
                                s += a[k + 3 * i] * a[k + 3 * j];
                            f = s / a[i + 3 * i] * g;
                            for (k = i; k < 3; k++)
                                a[k + 3 * j] += f * a[k + 3 * i];
                        }
                    }
                    for (j = i; j < 3; j++)
                        a[j + 3 * i] = a[j + 3 * i] * g;
                } else {
                    for (j = i; j < 3; j++)
                        a[j + 3 * i] = 0.0;
                }
                ++a[i + 3 * i];
            }

            /* diagonalize the bidiagonal form */
            for (k = 3 - 1; k >= 0; k--) { /* loop over singular values */
                for (its = 0; its < maxIterations; its++) { /* loop over allowed iterations */
                    flag = 1;
                    for (l = k; l >= 0; l--) { /* test for splitting */
                        nm = l - 1;
                        if (java.lang.Math.abs(rv1[l]) + anorm == anorm) {
                            flag = 0;
                            break;
                        }
                        if (java.lang.Math.abs(w[nm]) + anorm == anorm)
                            break;
                    }
                    if (flag != 0) {
                        c = 0.0;
                        s = 1.0;
                        for (i = l; i <= k; i++) {
                            f = s * rv1[i];
                            if (java.lang.Math.abs(f) + anorm != anorm) {
                                g = w[i];
                                h = PYTHAG(f, g);
                                w[i] = h;
                                h = 1.0 / h;
                                c = g * h;
                                s = (-f * h);
                                for (j = 0; j < 3; j++) {
                                    y = a[j + 3 * nm];
                                    z = a[j + 3 * i];
                                    a[j + 3 * nm] = y * c + z * s;
                                    a[j + 3 * i] = z * c - y * s;
                                }
                            }
                        }
                    }
                    z = w[k];
                    if (l == k) { /* convergence */
                        if (z < 0.0) { /* make singular value nonnegative */
                            w[k] = -z;
                            for (j = 0; j < 3; j++)
                                v[j + 3 * k] = (-v[j + 3 * k]);
                        }
                        break;
                    }
                    if (its == maxIterations - 1) {
                        throw new RuntimeException("No convergence after " + maxIterations + " iterations");
                    }

                    /* shift from bottom 2 x 2 minor */
                    x = w[l];
                    nm = k - 1;
                    y = w[nm];
                    g = rv1[nm];
                    h = rv1[k];
                    f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
                    g = PYTHAG(f, 1.0);
                    f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;

                    /* next QR transformation */
                    c = s = 1.0;
                    for (j = l; j <= nm; j++) {
                        i = j + 1;
                        g = rv1[i];
                        y = w[i];
                        h = s * g;
                        g = c * g;
                        z = PYTHAG(f, h);
                        rv1[j] = z;
                        c = f / z;
                        s = h / z;
                        f = x * c + g * s;
                        g = g * c - x * s;
                        h = y * s;
                        y = y * c;
                        for (jj = 0; jj < 3; jj++) {
                            x = v[jj + 3 * j];
                            z = v[jj + 3 * i];
                            v[jj + 3 * j] = x * c + z * s;
                            v[jj + 3 * i] = z * c - x * s;
                        }
                        z = PYTHAG(f, h);
                        w[j] = z;
                        if (z != 0.0) {
                            z = 1.0 / z;
                            c = f * z;
                            s = h * z;
                        }
                        f = (c * g) + (s * y);
                        x = (c * y) - (s * g);
                        for (jj = 0; jj < 3; jj++) {
                            y = a[jj + 3 * j];
                            z = a[jj + 3 * i];
                            a[jj + 3 * j] = y * c + z * s;
                            a[jj + 3 * i] = z * c - y * s;
                        }
                    }
                    rv1[l] = 0.0;
                    rv1[k] = f;
                    w[k] = x;
                }
            }
            destU.set(a);
            destV.set(v);
        }

        private static double PYTHAG(double a, double b) {
            double at = java.lang.Math.abs(a), bt = java.lang.Math.abs(b), ct, result;
            if (at > bt) {
                ct = bt / at;
                result = at * java.lang.Math.sqrt(1.0 + ct * ct);
            } else if (bt > 0.0) {
                ct = at / bt;
                result = bt * java.lang.Math.sqrt(1.0 + ct * ct);
            } else
                result = 0.0;
            return (result);
        }
    }

    private final SvdDecomposition3d svdDecomposition3d = new SvdDecomposition3d();
    private final double[] m = new double[9];
    private final Matrix3d u = new Matrix3d();
    private final Matrix3d v = new Matrix3d();

    /**
     * Compute the weighted average of all of the quaternions given in <code>qs</code> using the specified interpolation factors <code>weights</code>, and store the result in <code>dest</code>.
     * 
     * @param qs
     *            the quaternions to interpolate over
     * @param weights
     *            the weights of each individual quaternion in <code>qs</code>
     * @param maxSvdIterations
     *            the maximum number of iterations in the Singular Value Decomposition step used by this method
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Quaterniond computeWeightedAverage(Quaterniond[] qs, double[] weights, int maxSvdIterations, Quaterniond dest) {
        double m00 = 0.0, m01 = 0.0, m02 = 0.0;
        double m10 = 0.0, m11 = 0.0, m12 = 0.0;
        double m20 = 0.0, m21 = 0.0, m22 = 0.0;
        // Sum the rotation matrices of qs
        for (int i = 0; i < qs.length; i++) {
            Quaterniond q = qs[i];
            double dx = q.x + q.x;
            double dy = q.y + q.y;
            double dz = q.z + q.z;
            double q00 = dx * q.x;
            double q11 = dy * q.y;
            double q22 = dz * q.z;
            double q01 = dx * q.y;
            double q02 = dx * q.z;
            double q03 = dx * q.w;
            double q12 = dy * q.z;
            double q13 = dy * q.w;
            double q23 = dz * q.w;
            m00 += weights[i] * (1.0 - q11 - q22);
            m01 += weights[i] * (q01 + q23);
            m02 += weights[i] * (q02 - q13);
            m10 += weights[i] * (q01 - q23);
            m11 += weights[i] * (1.0 - q22 - q00);
            m12 += weights[i] * (q12 + q03);
            m20 += weights[i] * (q02 + q13);
            m21 += weights[i] * (q12 - q03);
            m22 += weights[i] * (1.0 - q11 - q00);
        }
        m[0] = m00;
        m[1] = m01;
        m[2] = m02;
        m[3] = m10;
        m[4] = m11;
        m[5] = m12;
        m[6] = m20;
        m[7] = m21;
        m[8] = m22;
        // Compute the Singular Value Decomposition of 'm'
        svdDecomposition3d.svd(m, maxSvdIterations, u, v);
        // Compute rotation matrix
        u.mul(v.transpose());
        // Build quaternion from it
        return dest.setFromNormalized(u).normalize();
    }

}
