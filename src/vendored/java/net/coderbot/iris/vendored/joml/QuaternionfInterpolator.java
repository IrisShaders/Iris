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
 * Computes the weighted average of multiple rotations represented as {@link Quaternionf} instances.
 * <p>
 * Instances of this class are <i>not</i> thread-safe.
 * 
 * @author Kai Burjack
 */
public class QuaternionfInterpolator {

    /**
     * Performs singular value decomposition on {@link Matrix3f}.
     * <p>
     * This code was adapted from <a href="http://www.public.iastate.edu/~dicook/JSS/paper/code/svd.c">http://www.public.iastate.edu/</a>.
     * 
     * @author Kai Burjack
     */
    private static class SvdDecomposition3f {
        private final float rv1[];
        private final float w[];
        private final float v[];

        SvdDecomposition3f() {
            this.rv1 = new float[3];
            this.w = new float[3];
            this.v = new float[9];
        }

        private float SIGN(float a, float b) {
            return ((b) >= 0.0 ? java.lang.Math.abs(a) : -java.lang.Math.abs(a));
        }

        void svd(float[] a, int maxIterations, Matrix3f destU, Matrix3f destV) {
            int flag, i, its, j, jj, k, l = 0, nm = 0;
            float c, f, h, s, x, y, z;
            float anorm = 0.0f, g = 0.0f, scale = 0.0f;
            /* Householder reduction to bidiagonal form */
            for (i = 0; i < 3; i++) {
                /* left-hand reduction */
                l = i + 1;
                rv1[i] = scale * g;
                g = s = scale = 0.0f;
                for (k = i; k < 3; k++)
                    scale += java.lang.Math.abs(a[k + 3 * i]);
                if (scale != 0.0f) {
                    for (k = i; k < 3; k++) {
                        a[k + 3 * i] = (a[k + 3 * i] / scale);
                        s += (a[k + 3 * i] * a[k + 3 * i]);
                    }
                    f = a[i + 3 * i];
                    g = -SIGN((float) java.lang.Math.sqrt(s), f);
                    h = f * g - s;
                    a[i + 3 * i] = f - g;
                    if (i != 3 - 1) {
                        for (j = l; j < 3; j++) {
                            for (s = 0.0f, k = i; k < 3; k++)
                                s += a[k + 3 * i] * a[k + 3 * j];
                            f = s / h;
                            for (k = i; k < 3; k++)
                                a[k + 3 * j] += f * a[k + 3 * i];
                        }
                    }
                    for (k = i; k < 3; k++)
                        a[k + 3 * i] = a[k + 3 * i] * scale;
                }
                w[i] = scale * g;

                /* right-hand reduction */
                g = s = scale = 0.0f;
                if (i < 3 && i != 3 - 1) {
                    for (k = l; k < 3; k++)
                        scale += java.lang.Math.abs(a[i + 3 * k]);
                    if (scale != 0.0f) {
                        for (k = l; k < 3; k++) {
                            a[i + 3 * k] = a[i + 3 * k] / scale;
                            s += a[i + 3 * k] * a[i + 3 * k];
                        }
                        f = a[i + 3 * l];
                        g = -SIGN((float) java.lang.Math.sqrt(s), f);
                        h = f * g - s;
                        a[i + 3 * l] = f - g;
                        for (k = l; k < 3; k++)
                            rv1[k] = a[i + 3 * k] / h;
                        if (i != 3 - 1) {
                            for (j = l; j < 3; j++) {
                                for (s = 0.0f, k = l; k < 3; k++)
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
                    if (g != 0.0f) {
                        for (j = l; j < 3; j++)
                            v[j + 3 * i] = (a[i + 3 * j] / a[i + 3 * l]) / g;
                        /* double division to avoid underflow */
                        for (j = l; j < 3; j++) {
                            for (s = 0.0f, k = l; k < 3; k++)
                                s += a[i + 3 * k] * v[k + 3 * j];
                            for (k = l; k < 3; k++)
                                v[k + 3 * j] += s * v[k + 3 * i];
                        }
                    }
                    for (j = l; j < 3; j++)
                        v[i + 3 * j] = v[j + 3 * i] = 0.0f;
                }
                v[i + 3 * i] = 1.0f;
                g = rv1[i];
                l = i;
            }

            /* accumulate the left-hand transformation */
            for (i = 3 - 1; i >= 0; i--) {
                l = i + 1;
                g = w[i];
                if (i < 3 - 1)
                    for (j = l; j < 3; j++)
                        a[i + 3 * j] = 0.0f;
                if (g != 0.0f) {
                    g = 1.0f / g;
                    if (i != 3 - 1) {
                        for (j = l; j < 3; j++) {
                            for (s = 0.0f, k = l; k < 3; k++)
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
                        a[j + 3 * i] = 0.0f;
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
                        c = 0.0f;
                        s = 1.0f;
                        for (i = l; i <= k; i++) {
                            f = s * rv1[i];
                            if (java.lang.Math.abs(f) + anorm != anorm) {
                                g = w[i];
                                h = PYTHAG(f, g);
                                w[i] = h;
                                h = 1.0f / h;
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
                        if (z < 0.0f) { /* make singular value nonnegative */
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
                    f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0f * h * y);
                    g = PYTHAG(f, 1.0f);
                    f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;

                    /* next QR transformation */
                    c = s = 1.0f;
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
                        if (z != 0.0f) {
                            z = 1.0f / z;
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
                    rv1[l] = 0.0f;
                    rv1[k] = f;
                    w[k] = x;
                }
            }
            destU.set(a);
            destV.set(v);
        }

        private static float PYTHAG(float a, float b) {
            float at = java.lang.Math.abs(a), bt = java.lang.Math.abs(b), ct, result;
            if (at > bt) {
                ct = bt / at;
                result = at * (float) java.lang.Math.sqrt(1.0 + ct * ct);
            } else if (bt > 0.0f) {
                ct = at / bt;
                result = bt * (float) java.lang.Math.sqrt(1.0 + ct * ct);
            } else
                result = 0.0f;
            return (result);
        }
    }

    private final SvdDecomposition3f svdDecomposition3f = new SvdDecomposition3f();
    private final float[] m = new float[9];
    private final Matrix3f u = new Matrix3f();
    private final Matrix3f v = new Matrix3f();

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
    public Quaternionf computeWeightedAverage(Quaternionfc[] qs, float[] weights, int maxSvdIterations, Quaternionf dest) {
        float m00 = 0.0f, m01 = 0.0f, m02 = 0.0f;
        float m10 = 0.0f, m11 = 0.0f, m12 = 0.0f;
        float m20 = 0.0f, m21 = 0.0f, m22 = 0.0f;
        // Sum the rotation matrices of qs
        for (int i = 0; i < qs.length; i++) {
            Quaternionfc q = qs[i];
            float dx = q.x() + q.x();
            float dy = q.y() + q.y();
            float dz = q.z() + q.z();
            float q00 = dx * q.x();
            float q11 = dy * q.y();
            float q22 = dz * q.z();
            float q01 = dx * q.y();
            float q02 = dx * q.z();
            float q03 = dx * q.w();
            float q12 = dy * q.z();
            float q13 = dy * q.w();
            float q23 = dz * q.w();
            m00 += weights[i] * (1.0f - q11 - q22);
            m01 += weights[i] * (q01 + q23);
            m02 += weights[i] * (q02 - q13);
            m10 += weights[i] * (q01 - q23);
            m11 += weights[i] * (1.0f - q22 - q00);
            m12 += weights[i] * (q12 + q03);
            m20 += weights[i] * (q02 + q13);
            m21 += weights[i] * (q12 - q03);
            m22 += weights[i] * (1.0f - q11 - q00);
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
        svdDecomposition3f.svd(m, maxSvdIterations, u, v);
        // Compute rotation matrix
        u.mul(v.transpose());
        // Build quaternion from it
        return dest.setFromNormalized(u).normalize();
    }

}
