/*
 * The MIT License
 *
 * Copyright (c) 2015-2021 Richard Greenlees
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a 3x3 matrix of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10  m20<br>
 *      m01  m11  m21<br>
 *      m02  m12  m22<br>
 * 
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Matrix3f implements Externalizable, Cloneable, Matrix3fc {

    private static final long serialVersionUID = 1L;

    public float m00, m01, m02;
    public float m10, m11, m12;
    public float m20, m21, m22;

    /**
     * Create a new {@link Matrix3f} and set it to {@link #identity() identity}.
     */
    public Matrix3f() {
        m00 = 1.0f;
        m11 = 1.0f;
        m22 = 1.0f;
    }

    /**
     * Create a new {@link Matrix3f} by setting its uppper left 2x2 submatrix to the values of the given {@link Matrix2fc}
     * and the rest to identity.
     *
     * @param mat
     *          the {@link Matrix2fc}
     */
    public Matrix3f(Matrix2fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix3f} and make it a copy of the given matrix.
     * 
     * @param mat
     *          the {@link Matrix3fc} to copy the values from
     */
    public Matrix3f(Matrix3fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix3f} and make it a copy of the upper left 3x3 of the given {@link Matrix4fc}.
     * 
     * @param mat
     *          the {@link Matrix4fc} to copy the values from
     */
    public Matrix3f(Matrix4fc mat) {
        set(mat);
    }

    /**
     * Create a new 3x3 matrix using the supplied float values. The order of the parameter is column-major, 
     * so the first three parameters specify the three elements of the first column.
     * 
     * @param m00
     *          the value of m00
     * @param m01
     *          the value of m01
     * @param m02
     *          the value of m02
     * @param m10
     *          the value of m10
     * @param m11
     *          the value of m11
     * @param m12
     *          the value of m12
     * @param m20
     *          the value of m20
     * @param m21
     *          the value of m21
     * @param m22
     *          the value of m22
     */
    public Matrix3f(float m00, float m01, float m02,
                    float m10, float m11, float m12, 
                    float m20, float m21, float m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }


    /**
     * Create a new {@link Matrix3f} by reading its 9 float components from the given {@link FloatBuffer}
     * at the buffer's current position.
     * <p>
     * That FloatBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     * 
     * @param buffer
     *          the {@link FloatBuffer} to read the matrix values from
     */
    public Matrix3f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }


    /**
     * Create a new {@link Matrix3f} and initialize its three columns using the supplied vectors.
     * 
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     * @param col2
     *          the third column
     */
    public Matrix3f(Vector3fc col0, Vector3fc col1, Vector3fc col2) {
        set(col0, col1, col2);
    }

    public float m00() {
        return m00;
    }
    public float m01() {
        return m01;
    }
    public float m02() {
        return m02;
    }
    public float m10() {
        return m10;
    }
    public float m11() {
        return m11;
    }
    public float m12() {
        return m12;
    }
    public float m20() {
        return m20;
    }
    public float m21() {
        return m21;
    }
    public float m22() {
        return m22;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    public Matrix3f m00(float m00) {
        this.m00 = m00;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 1.
     * 
     * @param m01
     *          the new value
     * @return this
     */
    public Matrix3f m01(float m01) {
        this.m01 = m01;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 2.
     * 
     * @param m02
     *          the new value
     * @return this
     */
    public Matrix3f m02(float m02) {
        this.m02 = m02;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    public Matrix3f m10(float m10) {
        this.m10 = m10;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 1.
     * 
     * @param m11
     *          the new value
     * @return this
     */
    public Matrix3f m11(float m11) {
        this.m11 = m11;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 2.
     * 
     * @param m12
     *          the new value
     * @return this
     */
    public Matrix3f m12(float m12) {
        this.m12 = m12;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    public Matrix3f m20(float m20) {
        this.m20 = m20;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 1.
     * 
     * @param m21
     *          the new value
     * @return this
     */
    public Matrix3f m21(float m21) {
        this.m21 = m21;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 2.
     * 
     * @param m22
     *          the new value
     * @return this
     */
    public Matrix3f m22(float m22) {
        this.m22 = m22;
        return this;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    Matrix3f _m00(float m00) {
        this.m00 = m00;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 1.
     * 
     * @param m01
     *          the new value
     * @return this
     */
    Matrix3f _m01(float m01) {
        this.m01 = m01;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 2.
     * 
     * @param m02
     *          the new value
     * @return this
     */
    Matrix3f _m02(float m02) {
        this.m02 = m02;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    Matrix3f _m10(float m10) {
        this.m10 = m10;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 1.
     * 
     * @param m11
     *          the new value
     * @return this
     */
    Matrix3f _m11(float m11) {
        this.m11 = m11;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 2.
     * 
     * @param m12
     *          the new value
     * @return this
     */
    Matrix3f _m12(float m12) {
        this.m12 = m12;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    Matrix3f _m20(float m20) {
        this.m20 = m20;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 1.
     * 
     * @param m21
     *          the new value
     * @return this
     */
    Matrix3f _m21(float m21) {
        this.m21 = m21;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 2.
     * 
     * @param m22
     *          the new value
     * @return this
     */
    Matrix3f _m22(float m22) {
        this.m22 = m22;
        return this;
    }

    /**
     * Set the elements of this matrix to the ones in <code>m</code>.
     * 
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix3f set(Matrix3fc m) {
        return 
        _m00(m.m00()).
        _m01(m.m01()).
        _m02(m.m02()).
        _m10(m.m10()).
        _m11(m.m11()).
        _m12(m.m12()).
        _m20(m.m20()).
        _m21(m.m21()).
        _m22(m.m22());
    }

    /**
     * Store the values of the transpose of the given matrix <code>m</code> into <code>this</code> matrix.
     * 
     * @param m
     *          the matrix to copy the transposed values from
     * @return this
     */
    public Matrix3f setTransposed(Matrix3fc m) {
        float nm10 = m.m01(), nm12 = m.m21();
        float nm20 = m.m02(), nm21 = m.m12();
        return this
        ._m00(m.m00())._m01(m.m10())._m02(m.m20())
        ._m10(nm10)._m11(m.m11())._m12(nm12)
        ._m20(nm20)._m21(nm21)._m22(m.m22());
    }

    /**
     * Set the elements of this matrix to the left 3x3 submatrix of <code>m</code>.
     * 
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix3f set(Matrix4x3fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m02 = m.m02();
        m10 = m.m10();
        m11 = m.m11();
        m12 = m.m12();
        m20 = m.m20();
        m21 = m.m21();
        m22 = m.m22();
        return this;
    }

    /**
     * Set the elements of this matrix to the upper left 3x3 of the given {@link Matrix4fc}.
     *
     * @param mat
     *          the {@link Matrix4fc} to copy the values from
     * @return this
     */
    public Matrix3f set(Matrix4fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m02 = mat.m02();
        m10 = mat.m10();
        m11 = mat.m11();
        m12 = mat.m12();
        m20 = mat.m20();
        m21 = mat.m21();
        m22 = mat.m22();
        return this;
    }

    /**
     * Set the upper left 2x2 submatrix of this {@link Matrix3f} to the given {@link Matrix2fc}
     * and the rest to identity.
     *
     * @see #Matrix3f(Matrix2fc)
     *
     * @param mat
     *          the {@link Matrix2fc}
     * @return this
     */
    public Matrix3f set(Matrix2fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m02 = 0.0f;
        m10 = mat.m10();
        m11 = mat.m11();
        m12 = 0.0f;
        m20 = 0.0f;
        m21 = 0.0f;
        m22 = 1.0f;
        return this;
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4f}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f}
     * @return this
     */
    public Matrix3f set(AxisAngle4f axisAngle) {
        float x = axisAngle.x;
        float y = axisAngle.y;
        float z = axisAngle.z;
        float angle = axisAngle.angle;
        float invLength = Math.invsqrt(x*x + y*y + z*z);
        x *= invLength;
        y *= invLength;
        z *= invLength;
        float s = Math.sin(angle);
        float c = Math.cosFromSin(s, angle);
        float omc = 1.0f - c;
        m00 = c + x*x*omc;
        m11 = c + y*y*omc;
        m22 = c + z*z*omc;
        float tmp1 = x*y*omc;
        float tmp2 = z*s;
        m10 = tmp1 - tmp2;
        m01 = tmp1 + tmp2;
        tmp1 = x*z*omc;
        tmp2 = y*s;
        m20 = tmp1 + tmp2;
        m02 = tmp1 - tmp2;
        tmp1 = y*z*omc;
        tmp2 = x*s;
        m21 = tmp1 - tmp2;
        m12 = tmp1 + tmp2;
        return this;
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4d}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d}
     * @return this
     */
    public Matrix3f set(AxisAngle4d axisAngle) {
        double x = axisAngle.x;
        double y = axisAngle.y;
        double z = axisAngle.z;
        double angle = axisAngle.angle;
        double invLength = Math.invsqrt(x*x + y*y + z*z);
        x *= invLength;
        y *= invLength;
        z *= invLength;
        double s = Math.sin(angle);
        double c = Math.cosFromSin(s, angle);
        double omc = 1.0f - c;
        m00 = (float)(c + x*x*omc);
        m11 = (float)(c + y*y*omc);
        m22 = (float)(c + z*z*omc);
        double tmp1 = x*y*omc;
        double tmp2 = z*s;
        m10 = (float)(tmp1 - tmp2);
        m01 = (float)(tmp1 + tmp2);
        tmp1 = x*z*omc;
        tmp2 = y*s;
        m20 = (float)(tmp1 + tmp2);
        m02 = (float)(tmp1 - tmp2);
        tmp1 = y*z*omc;
        tmp2 = x*s;
        m21 = (float)(tmp1 - tmp2);
        m12 = (float)(tmp1 + tmp2);
        return this;
    }

    /**
     * Set this matrix to be equivalent to the rotation - and possibly scaling - specified by the given {@link Quaternionfc}.
     * <p>
     * This method is equivalent to calling: <code>rotation(q)</code>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/">http://www.euclideanspace.com/</a>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param q
     *          the {@link Quaternionfc}
     * @return this
     */
    public Matrix3f set(Quaternionfc q) {
        return rotation(q);
    }

    /**
     * Set this matrix to a rotation - and possibly scaling - equivalent to the given quaternion.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/">http://www.euclideanspace.com/</a>
     * 
     * @param q
     *          the quaternion
     * @return this
     */
    public Matrix3f set(Quaterniondc q) {
        double w2 = q.w() * q.w();
        double x2 = q.x() * q.x();
        double y2 = q.y() * q.y();
        double z2 = q.z() * q.z();
        double zw = q.z() * q.w();
        double xy = q.x() * q.y();
        double xz = q.x() * q.z();
        double yw = q.y() * q.w();
        double yz = q.y() * q.z();
        double xw = q.x() * q.w();
        m00 = (float) (w2 + x2 - z2 - y2);
        m01 = (float) (xy + zw + zw + xy);
        m02 = (float) (xz - yw + xz - yw);
        m10 = (float) (-zw + xy - zw + xy);
        m11 = (float) (y2 - z2 + w2 - x2);
        m12 = (float) (yz + yz + xw + xw);
        m20 = (float) (yw + xz + xz + yw);
        m21 = (float) (yz + yz - xw - xw);
        m22 = (float) (z2 - y2 - x2 + w2);
        return this;
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     * 
     * @param right
     *          the right operand of the matrix multiplication
     * @return this
     */
    public Matrix3f mul(Matrix3fc right) {
        return mul(right, this);
    }

    public Matrix3f mul(Matrix3fc right, Matrix3f dest) {
        float nm00 = Math.fma(m00, right.m00(), Math.fma(m10, right.m01(), m20 * right.m02()));
        float nm01 = Math.fma(m01, right.m00(), Math.fma(m11, right.m01(), m21 * right.m02()));
        float nm02 = Math.fma(m02, right.m00(), Math.fma(m12, right.m01(), m22 * right.m02()));
        float nm10 = Math.fma(m00, right.m10(), Math.fma(m10, right.m11(), m20 * right.m12()));
        float nm11 = Math.fma(m01, right.m10(), Math.fma(m11, right.m11(), m21 * right.m12()));
        float nm12 = Math.fma(m02, right.m10(), Math.fma(m12, right.m11(), m22 * right.m12()));
        float nm20 = Math.fma(m00, right.m20(), Math.fma(m10, right.m21(), m20 * right.m22()));
        float nm21 = Math.fma(m01, right.m20(), Math.fma(m11, right.m21(), m21 * right.m22()));
        float nm22 = Math.fma(m02, right.m20(), Math.fma(m12, right.m21(), m22 * right.m22()));
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply this matrix by the supplied <code>left</code> matrix and store the result in <code>this</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the <code>left</code> matrix,
     * then the new matrix will be <code>L * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>L * M * v</code>, the
     * transformation of <code>this</code> matrix will be applied first!
     *
     * @param left
     *          the left operand of the matrix multiplication
     * @return this
     */
    public Matrix3f mulLocal(Matrix3fc left) {
       return mulLocal(left, this);
    }

    public Matrix3f mulLocal(Matrix3fc left, Matrix3f dest) {
        float nm00 = left.m00() * m00 + left.m10() * m01 + left.m20() * m02;
        float nm01 = left.m01() * m00 + left.m11() * m01 + left.m21() * m02;
        float nm02 = left.m02() * m00 + left.m12() * m01 + left.m22() * m02;
        float nm10 = left.m00() * m10 + left.m10() * m11 + left.m20() * m12;
        float nm11 = left.m01() * m10 + left.m11() * m11 + left.m21() * m12;
        float nm12 = left.m02() * m10 + left.m12() * m11 + left.m22() * m12;
        float nm20 = left.m00() * m20 + left.m10() * m21 + left.m20() * m22;
        float nm21 = left.m01() * m20 + left.m11() * m21 + left.m21() * m22;
        float nm22 = left.m02() * m20 + left.m12() * m21 + left.m22() * m22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied float values. The result looks like this:
     * <p>
     * m00, m10, m20<br>
     * m01, m11, m21<br>
     * m02, m12, m22<br>
     * 
     * @param m00
     *          the new value of m00
     * @param m01
     *          the new value of m01
     * @param m02
     *          the new value of m02
     * @param m10
     *          the new value of m10
     * @param m11
     *          the new value of m11
     * @param m12
     *          the new value of m12
     * @param m20
     *          the new value of m20
     * @param m21
     *          the new value of m21
     * @param m22
     *          the new value of m22
     * @return this
     */
    public Matrix3f set(float m00, float m01, float m02,
                        float m10, float m11, float m12, 
                        float m20, float m21, float m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        return this;
    }

    /**
     * Set the values in this matrix based on the supplied float array. The result looks like this:
     * <p>
     * 0, 3, 6<br>
     * 1, 4, 7<br>
     * 2, 5, 8<br>
     * 
     * This method only uses the first 9 values, all others are ignored.
     * 
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix3f set(float m[]) {
        MemUtil.INSTANCE.copy(m, 0, this);
        return this;
    }

    /**
     * Set the three columns of this matrix to the supplied vectors, respectively.
     * 
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     * @param col2
     *          the third column
     * @return this
     */
    public Matrix3f set(Vector3fc col0, Vector3fc col1, Vector3fc col2) {
        this.m00 = col0.x();
        this.m01 = col0.y();
        this.m02 = col0.z();
        this.m10 = col1.x();
        this.m11 = col1.y();
        this.m12 = col1.z();
        this.m20 = col2.x();
        this.m21 = col2.y();
        this.m22 = col2.z();
        return this;
    }

    public float determinant() {
        return (m00 * m11 - m01 * m10) * m22
             + (m02 * m10 - m00 * m12) * m21
             + (m01 * m12 - m02 * m11) * m20;
    }

    /**
     * Invert this matrix.
     *
     * @return this
     */
    public Matrix3f invert() {
        return invert(this);
    }

    public Matrix3f invert(Matrix3f dest) {
        float a = Math.fma(m00, m11, -m01 * m10);
        float b = Math.fma(m02, m10, -m00 * m12);
        float c = Math.fma(m01, m12, -m02 * m11);
        float d = Math.fma(a, m22, Math.fma(b, m21, c * m20));
        float s = 1.0f / d;
        float nm00 = Math.fma(m11, m22, -m21 * m12) * s;
        float nm01 = Math.fma(m21, m02, -m01 * m22) * s;
        float nm02 = c * s;
        float nm10 = Math.fma(m20, m12, -m10 * m22) * s;
        float nm11 = Math.fma(m00, m22, -m20 * m02) * s;
        float nm12 = b * s;
        float nm20 = Math.fma(m10, m21, -m20 * m11) * s;
        float nm21 = Math.fma(m20, m01, -m00 * m21) * s;
        float nm22 = a * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Transpose this matrix.
     * 
     * @return this
     */
    public Matrix3f transpose() {
        return transpose(this);
    }

    public Matrix3f transpose(Matrix3f dest) {
        return dest.set(m00, m10, m20,
                        m01, m11, m21,
                        m02, m12, m22);
    }

    /**
     * Return a string representation of this matrix.
     * <p>
     * This method creates a new {@link DecimalFormat} on every invocation with the format string "<code>0.000E0;-</code>".
     * 
     * @return the string representation
     */
    public String toString() {
        DecimalFormat formatter = new DecimalFormat(" 0.000E0;-");
        String str = toString(formatter);
        StringBuffer res = new StringBuffer();
        int eIndex = Integer.MIN_VALUE;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 'E') {
                eIndex = i;
            } else if (c == ' ' && eIndex == i - 1) {
                // workaround Java 1.4 DecimalFormat bug
                res.append('+');
                continue;
            } else if (Character.isDigit(c) && eIndex == i - 1) {
                res.append('+');
            }
            res.append(c);
        }
        return res.toString();
    }

    /**
     * Return a string representation of this matrix by formatting the matrix elements with the given {@link NumberFormat}.
     * 
     * @param formatter
     *          the {@link NumberFormat} used to format the matrix values with
     * @return the string representation
     */
    public String toString(NumberFormat formatter) {
        return Runtime.format(m00, formatter) + " " + Runtime.format(m10, formatter) + " " + Runtime.format(m20, formatter) + "\n"
             + Runtime.format(m01, formatter) + " " + Runtime.format(m11, formatter) + " " + Runtime.format(m21, formatter) + "\n"
             + Runtime.format(m02, formatter) + " " + Runtime.format(m12, formatter) + " " + Runtime.format(m22, formatter) + "\n";
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix3fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix3fc)
     * 
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    public Matrix3f get(Matrix3f dest) {
        return dest.set(this);
    }

    public Matrix4f get(Matrix4f dest) {
        return dest.set(this);
    }

    public AxisAngle4f getRotation(AxisAngle4f dest) {
        return dest.set(this);
    }

    public Quaternionf getUnnormalizedRotation(Quaternionf dest) {
        return dest.setFromUnnormalized(this);
    }

    public Quaternionf getNormalizedRotation(Quaternionf dest) {
        return dest.setFromNormalized(this);
    }

    public Quaterniond getUnnormalizedRotation(Quaterniond dest) {
        return dest.setFromUnnormalized(this);
    }

    public Quaterniond getNormalizedRotation(Quaterniond dest) {
        return dest.setFromNormalized(this);
    }


    public FloatBuffer get(FloatBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    public FloatBuffer get(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get(ByteBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    public ByteBuffer get(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public FloatBuffer get3x4(FloatBuffer buffer) {
        return get3x4(buffer.position(), buffer);
    }

    public FloatBuffer get3x4(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get3x4(ByteBuffer buffer) {
        return get3x4(buffer.position(), buffer);
    }

    public ByteBuffer get3x4(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, index, buffer);
        return buffer;
    }

    public FloatBuffer getTransposed(FloatBuffer buffer) {
        return getTransposed(buffer.position(), buffer);
    }

    public FloatBuffer getTransposed(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, index, buffer);
        return buffer;
    }

    public ByteBuffer getTransposed(ByteBuffer buffer) {
        return getTransposed(buffer.position(), buffer);
    }

    public ByteBuffer getTransposed(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, index, buffer);
        return buffer;
    }


    public float[] get(float[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    public float[] get(float[] arr) {
        return get(arr, 0);
    }


    /**
     * Set the values of this matrix by reading 9 float values from the given {@link FloatBuffer} in column-major order,
     * starting at its current position.
     * <p>
     * The FloatBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the FloatBuffer will not be changed by this method.
     * 
     * @param buffer
     *              the FloatBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix3f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 9 float values from the given {@link ByteBuffer} in column-major order,
     * starting at its current position.
     * <p>
     * The ByteBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the ByteBuffer will not be changed by this method.
     * 
     * @param buffer
     *              the ByteBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix3f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 9 float values from the given {@link FloatBuffer} in column-major order,
     * starting at the specified absolute buffer position/index.
     * <p>
     * The FloatBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the FloatBuffer will not be changed by this method.
     * 
     * @param index
     *              the absolute position into the FloatBuffer
     * @param buffer
     *              the FloatBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix3f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 9 float values from the given {@link ByteBuffer} in column-major order,
     * starting at the specified absolute buffer position/index.
     * <p>
     * The ByteBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the ByteBuffer will not be changed by this method.
     * 
     * @param index
     *              the absolute position into the ByteBuffer
     * @param buffer
     *              the ByteBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix3f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set all values within this matrix to zero.
     * 
     * @return this
     */
    public Matrix3f zero() {
        MemUtil.INSTANCE.zero(this);
        return this;
    }

    /**
     * Set this matrix to the identity.
     * 
     * @return this
     */
    public Matrix3f identity() {
        MemUtil.INSTANCE.identity(this);
        return this;
    }

    public Matrix3f scale(Vector3fc xyz, Matrix3f dest) {
        return scale(xyz.x(), xyz.y(), xyz.z(), dest);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xyz.x</code>,
     * <code>xyz.y</code> and <code>xyz.z</code> factors, respectively.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * 
     * @param xyz
     *            the factors of the x, y and z component, respectively
     * @return this
     */
    public Matrix3f scale(Vector3fc xyz) {
        return scale(xyz.x(), xyz.y(), xyz.z(), this);
    }

    public Matrix3f scale(float x, float y, float z, Matrix3f dest) {
        // scale matrix elements:
        // m00 = x, m11 = y, m22 = z
        // all others = 0
        dest.m00 = m00 * x;
        dest.m01 = m01 * x;
        dest.m02 = m02 * x;
        dest.m10 = m10 * y;
        dest.m11 = m11 * y;
        dest.m12 = m12 * y;
        dest.m20 = m20 * z;
        dest.m21 = m21 * z;
        dest.m22 = m22 * z;
        return dest;
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x,
     * y and z factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @param z
     *            the factor of the z component
     * @return this
     */
    public Matrix3f scale(float x, float y, float z) {
        return scale(x, y, z, this);
    }

    public Matrix3f scale(float xyz, Matrix3f dest) {
        return scale(xyz, xyz, xyz, dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xyz</code> factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * 
     * @see #scale(float, float, float)
     * 
     * @param xyz
     *            the factor for all components
     * @return this
     */
    public Matrix3f scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Matrix3f scaleLocal(float x, float y, float z, Matrix3f dest) {
        float nm00 = x * m00;
        float nm01 = y * m01;
        float nm02 = z * m02;
        float nm10 = x * m10;
        float nm11 = y * m11;
        float nm12 = z * m12;
        float nm20 = x * m20;
        float nm21 = y * m21;
        float nm22 = z * m22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given x,
     * y and z factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @param z
     *            the factor of the z component
     * @return this
     */
    public Matrix3f scaleLocal(float x, float y, float z) {
        return scaleLocal(x, y, z, this);
    }

    /**
     * Set this matrix to be a simple scale matrix, which scales all axes uniformly by the given factor.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a
     * matrix, use {@link #scale(float) scale()} instead.
     * 
     * @see #scale(float)
     * 
     * @param factor
     *             the scale factor in x, y and z
     * @return this
     */
    public Matrix3f scaling(float factor) {
        MemUtil.INSTANCE.zero(this);
        m00 = factor;
        m11 = factor;
        m22 = factor;
        return this;
    }

    /**
     * Set this matrix to be a simple scale matrix.
     * 
     * @param x
     *             the scale in x
     * @param y
     *             the scale in y
     * @param z
     *             the scale in z
     * @return this
     */
    public Matrix3f scaling(float x, float y, float z) {
        MemUtil.INSTANCE.zero(this);
        m00 = x;
        m11 = y;
        m22 = z;
        return this;
    }

    /**
     * Set this matrix to be a simple scale matrix which scales the base axes by <code>xyz.x</code>, <code>xyz.y</code> and <code>xyz.z</code> respectively.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a
     * matrix use {@link #scale(Vector3fc) scale()} instead.
     * 
     * @see #scale(Vector3fc)
     * 
     * @param xyz
     *             the scale in x, y and z respectively
     * @return this
     */
    public Matrix3f scaling(Vector3fc xyz) {
        return scaling(xyz.x(), xyz.y(), xyz.z());
    }

    /**
     * Set this matrix to a rotation matrix which rotates the given radians about a given axis.
     * <p>
     * The axis described by the <code>axis</code> vector needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to post-multiply a rotation transformation directly to a
     * matrix, use {@link #rotate(float, Vector3fc) rotate()} instead.
     * 
     * @see #rotate(float, Vector3fc)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the axis to rotate about (needs to be {@link Vector3f#normalize() normalized})
     * @return this
     */
    public Matrix3f rotation(float angle, Vector3fc axis) {
        return rotation(angle, axis.x(), axis.y(), axis.z());
    }

    /**
     * Set this matrix to a rotation transformation using the given {@link AxisAngle4f}.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to apply the rotation transformation to an existing transformation,
     * use {@link #rotate(AxisAngle4f) rotate()} instead.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     *
     * @see #rotate(AxisAngle4f)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f} (needs to be {@link AxisAngle4f#normalize() normalized})
     * @return this
     */
    public Matrix3f rotation(AxisAngle4f axisAngle) {
        return rotation(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z);
    }

    /**
     * Set this matrix to a rotation matrix which rotates the given radians about a given axis.
     * <p>
     * The axis described by the three components needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to apply the rotation transformation to an existing transformation,
     * use {@link #rotate(float, float, float, float) rotate()} instead.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float)
     * 
     * @param angle
     *          the angle in radians
     * @param x
     *          the x-component of the rotation axis
     * @param y
     *          the y-component of the rotation axis
     * @param z
     *          the z-component of the rotation axis
     * @return this
     */
    public Matrix3f rotation(float angle, float x, float y, float z) {
        float sin = Math.sin(angle);
        float cos = Math.cosFromSin(sin, angle);
        float C = 1.0f - cos;
        float xy = x * y, xz = x * z, yz = y * z;
        m00 = cos + x * x * C;
        m10 = xy * C - z * sin;
        m20 = xz * C + y * sin;
        m01 = xy * C + z * sin;
        m11 = cos + y * y * C;
        m21 = yz * C - x * sin;
        m02 = xz * C - y * sin;
        m12 = yz * C + x * sin;
        m22 = cos + z * z * C;
        return this;
    }

    /**
     * Set this matrix to a rotation transformation about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotationX(float ang) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        m00 = 1.0f;
        m01 = 0.0f;
        m02 = 0.0f;
        m10 = 0.0f;
        m11 = cos;
        m12 = sin;
        m20 = 0.0f;
        m21 = -sin;
        m22 = cos;
        return this;
    }

    /**
     * Set this matrix to a rotation transformation about the Y axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotationY(float ang) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        m00 = cos;
        m01 = 0.0f;
        m02 = -sin;
        m10 = 0.0f;
        m11 = 1.0f;
        m12 = 0.0f;
        m20 = sin;
        m21 = 0.0f;
        m22 = cos;
        return this;
    }

    /**
     * Set this matrix to a rotation transformation about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotationZ(float ang) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        m00 = cos;
        m01 = sin;
        m02 = 0.0f;
        m10 = -sin;
        m11 = cos;
        m12 = 0.0f;
        m20 = 0.0f;
        m21 = 0.0f;
        m22 = 1.0f;
        return this;
    }

    /**
     * Set this matrix to a rotation of <code>angleX</code> radians about the X axis, followed by a rotation
     * of <code>angleY</code> radians about the Y axis and followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>rotationX(angleX).rotateY(angleY).rotateZ(angleZ)</code>
     * 
     * @param angleX
     *            the angle to rotate about X
     * @param angleY
     *            the angle to rotate about Y
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix3f rotationXYZ(float angleX, float angleY, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinX = -sinX;
        float m_sinY = -sinY;
        float m_sinZ = -sinZ;

        // rotateX
        float nm11 = cosX;
        float nm12 = sinX;
        float nm21 = m_sinX;
        float nm22 = cosX;
        // rotateY
        float nm00 = cosY;
        float nm01 = nm21 * m_sinY;
        float nm02 = nm22 * m_sinY;
        m20 = sinY;
        m21 = nm21 * cosY;
        m22 = nm22 * cosY;
        // rotateZ
        m00 = nm00 * cosZ;
        m01 = nm01 * cosZ + nm11 * sinZ;
        m02 = nm02 * cosZ + nm12 * sinZ;
        m10 = nm00 * m_sinZ;
        m11 = nm01 * m_sinZ + nm11 * cosZ;
        m12 = nm02 * m_sinZ + nm12 * cosZ;
        return this;
    }

    /**
     * Set this matrix to a rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation
     * of <code>angleY</code> radians about the Y axis and followed by a rotation of <code>angleX</code> radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>rotationZ(angleZ).rotateY(angleY).rotateX(angleX)</code>
     * 
     * @param angleZ
     *            the angle to rotate about Z
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @return this
     */
    public Matrix3f rotationZYX(float angleZ, float angleY, float angleX) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinZ = -sinZ;
        float m_sinY = -sinY;
        float m_sinX = -sinX;

        // rotateZ
        float nm00 = cosZ;
        float nm01 = sinZ;
        float nm10 = m_sinZ;
        float nm11 = cosZ;
        // rotateY
        float nm20 = nm00 * sinY;
        float nm21 = nm01 * sinY;
        float nm22 = cosY;
        m00 = nm00 * cosY;
        m01 = nm01 * cosY;
        m02 = m_sinY;
        // rotateX
        m10 = nm10 * cosX + nm20 * sinX;
        m11 = nm11 * cosX + nm21 * sinX;
        m12 = nm22 * sinX;
        m20 = nm10 * m_sinX + nm20 * cosX;
        m21 = nm11 * m_sinX + nm21 * cosX;
        m22 = nm22 * cosX;
        return this;
    }

    /**
     * Set this matrix to a rotation of <code>angleY</code> radians about the Y axis, followed by a rotation
     * of <code>angleX</code> radians about the X axis and followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>rotationY(angleY).rotateX(angleX).rotateZ(angleZ)</code>
     * 
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix3f rotationYXZ(float angleY, float angleX, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinY = -sinY;
        float m_sinX = -sinX;
        float m_sinZ = -sinZ;

        // rotateY
        float nm00 = cosY;
        float nm02 = m_sinY;
        float nm20 = sinY;
        float nm22 = cosY;
        // rotateX
        float nm10 = nm20 * sinX;
        float nm11 = cosX;
        float nm12 = nm22 * sinX;
        m20 = nm20 * cosX;
        m21 = m_sinX;
        m22 = nm22 * cosX;
        // rotateZ
        m00 = nm00 * cosZ + nm10 * sinZ;
        m01 = nm11 * sinZ;
        m02 = nm02 * cosZ + nm12 * sinZ;
        m10 = nm00 * m_sinZ + nm10 * cosZ;
        m11 = nm11 * cosZ;
        m12 = nm02 * m_sinZ + nm12 * cosZ;
        return this;
    }

    /**
     * Set this matrix to the rotation - and possibly scaling - transformation of the given {@link Quaternionfc}.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to apply the rotation transformation to an existing transformation,
     * use {@link #rotate(Quaternionfc) rotate()} instead.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @see #rotate(Quaternionfc)
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @return this
     */
    public Matrix3f rotation(Quaternionfc quat) {
        float w2 = quat.w() * quat.w();
        float x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y();
        float z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw;
        float xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz;
        float yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz;
        float xw = quat.x() * quat.w(), dxw = xw + xw;
        m00 = w2 + x2 - z2 - y2;
        m01 = dxy + dzw;
        m02 = dxz - dyw;
        m10 = -dzw + dxy;
        m11 = y2 - z2 + w2 - x2;
        m12 = dyz + dxw;
        m20 = dyw + dxz;
        m21 = dyz - dxw;
        m22 = z2 - y2 - x2 + w2;
        return this;
    }

    public Vector3f transform(Vector3f v) {
        return v.mul(this);
    }

    public Vector3f transform(Vector3fc v, Vector3f dest) {
        return v.mul(this, dest);
    }

    public Vector3f transform(float x, float y, float z, Vector3f dest) {
        return dest.set(Math.fma(m00, x, Math.fma(m10, y, m20 * z)),
                        Math.fma(m01, x, Math.fma(m11, y, m21 * z)),
                        Math.fma(m02, x, Math.fma(m12, y, m22 * z)));
    }

    public Vector3f transformTranspose(Vector3f v) {
        return v.mulTranspose(this);
    }

    public Vector3f transformTranspose(Vector3fc v, Vector3f dest) {
        return v.mulTranspose(this, dest);
    }

    public Vector3f transformTranspose(float x, float y, float z, Vector3f dest) {
        return dest.set(Math.fma(m00, x, Math.fma(m01, y, m02 * z)),
                        Math.fma(m10, x, Math.fma(m11, y, m12 * z)),
                        Math.fma(m20, x, Math.fma(m21, y, m22 * z)));
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(m00);
        out.writeFloat(m01);
        out.writeFloat(m02);
        out.writeFloat(m10);
        out.writeFloat(m11);
        out.writeFloat(m12);
        out.writeFloat(m20);
        out.writeFloat(m21);
        out.writeFloat(m22);
    }

    public void readExternal(ObjectInput in) throws IOException {
        m00 = in.readFloat();
        m01 = in.readFloat();
        m02 = in.readFloat();
        m10 = in.readFloat();
        m11 = in.readFloat();
        m12 = in.readFloat();
        m20 = in.readFloat();
        m21 = in.readFloat();
        m22 = in.readFloat();
    }

    public Matrix3f rotateX(float ang, Matrix3f dest) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm11 = cos;
        float rm21 = -sin;
        float rm12 = sin;
        float rm22 = cos;

        // add temporaries for dependent values
        float nm10 = m10 * rm11 + m20 * rm12;
        float nm11 = m11 * rm11 + m21 * rm12;
        float nm12 = m12 * rm11 + m22 * rm12;
        // set non-dependent values directly
        dest.m20 = m10 * rm21 + m20 * rm22;
        dest.m21 = m11 * rm21 + m21 * rm22;
        dest.m22 = m12 * rm21 + m22 * rm22;
        // set other values
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        return dest;
    }

    /**
     * Apply rotation about the X axis to this matrix by rotating the given amount of radians.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotateX(float ang) {
        return rotateX(ang, this);
    }

    public Matrix3f rotateY(float ang, Matrix3f dest) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm00 = cos;
        float rm20 = sin;
        float rm02 = -sin;
        float rm22 = cos;

        // add temporaries for dependent values
        float nm00 = m00 * rm00 + m20 * rm02;
        float nm01 = m01 * rm00 + m21 * rm02;
        float nm02 = m02 * rm00 + m22 * rm02;
        // set non-dependent values directly
        dest.m20 = m00 * rm20 + m20 * rm22;
        dest.m21 = m01 * rm20 + m21 * rm22;
        dest.m22 = m02 * rm20 + m22 * rm22;
        // set other values
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        return dest;
    }

    /**
     * Apply rotation about the Y axis to this matrix by rotating the given amount of radians.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotateY(float ang) {
        return rotateY(ang, this);
    }

    public Matrix3f rotateZ(float ang, Matrix3f dest) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm00 = cos;
        float rm10 = -sin;
        float rm01 = sin;
        float rm11 = cos;

        // add temporaries for dependent values
        float nm00 = m00 * rm00 + m10 * rm01;
        float nm01 = m01 * rm00 + m11 * rm01;
        float nm02 = m02 * rm00 + m12 * rm01;
        // set non-dependent values directly
        dest.m10 = m00 * rm10 + m10 * rm11;
        dest.m11 = m01 * rm10 + m11 * rm11;
        dest.m12 = m02 * rm10 + m12 * rm11;
        // set other values
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        return dest;
    }

    /**
     * Apply rotation about the Z axis to this matrix by rotating the given amount of radians.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3f rotateZ(float ang) {
        return rotateZ(ang, this);
    }

    /**
     * Apply rotation of <code>angles.x</code> radians about the X axis, followed by a rotation of <code>angles.y</code> radians about the Y axis and
     * followed by a rotation of <code>angles.z</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateX(angles.x).rotateY(angles.y).rotateZ(angles.z)</code>
     * 
     * @param angles
     *            the Euler angles
     * @return this
     */
    public Matrix3f rotateXYZ(Vector3f angles) {
        return rotateXYZ(angles.x, angles.y, angles.z);
    }

    /**
     * Apply rotation of <code>angleX</code> radians about the X axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateX(angleX).rotateY(angleY).rotateZ(angleZ)</code>
     * 
     * @param angleX
     *            the angle to rotate about X
     * @param angleY
     *            the angle to rotate about Y
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix3f rotateXYZ(float angleX, float angleY, float angleZ) {
        return rotateXYZ(angleX, angleY, angleZ, this);
    }

    public Matrix3f rotateXYZ(float angleX, float angleY, float angleZ, Matrix3f dest) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinX = -sinX;
        float m_sinY = -sinY;
        float m_sinZ = -sinZ;

        // rotateX
        float nm10 = m10 * cosX + m20 * sinX;
        float nm11 = m11 * cosX + m21 * sinX;
        float nm12 = m12 * cosX + m22 * sinX;
        float nm20 = m10 * m_sinX + m20 * cosX;
        float nm21 = m11 * m_sinX + m21 * cosX;
        float nm22 = m12 * m_sinX + m22 * cosX;
        // rotateY
        float nm00 = m00 * cosY + nm20 * m_sinY;
        float nm01 = m01 * cosY + nm21 * m_sinY;
        float nm02 = m02 * cosY + nm22 * m_sinY;
        dest.m20 = m00 * sinY + nm20 * cosY;
        dest.m21 = m01 * sinY + nm21 * cosY;
        dest.m22 = m02 * sinY + nm22 * cosY;
        // rotateZ
        dest.m00 = nm00 * cosZ + nm10 * sinZ;
        dest.m01 = nm01 * cosZ + nm11 * sinZ;
        dest.m02 = nm02 * cosZ + nm12 * sinZ;
        dest.m10 = nm00 * m_sinZ + nm10 * cosZ;
        dest.m11 = nm01 * m_sinZ + nm11 * cosZ;
        dest.m12 = nm02 * m_sinZ + nm12 * cosZ;
        return dest;
    }

    /**
     * Apply rotation of <code>angles.z</code> radians about the Z axis, followed by a rotation of <code>angles.y</code> radians about the Y axis and
     * followed by a rotation of <code>angles.x</code> radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateZ(angles.z).rotateY(angles.y).rotateX(angles.x)</code>
     * 
     * @param angles
     *            the Euler angles
     * @return this
     */
    public Matrix3f rotateZYX(Vector3f angles) {
        return rotateZYX(angles.z, angles.y, angles.x);
    }

    /**
     * Apply rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleX</code> radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateZ(angleZ).rotateY(angleY).rotateX(angleX)</code>
     * 
     * @param angleZ
     *            the angle to rotate about Z
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @return this
     */
    public Matrix3f rotateZYX(float angleZ, float angleY, float angleX) {
        return rotateZYX(angleZ, angleY, angleX, this);
    }

    public Matrix3f rotateZYX(float angleZ, float angleY, float angleX, Matrix3f dest) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinZ = -sinZ;
        float m_sinY = -sinY;
        float m_sinX = -sinX;

        // rotateZ
        float nm00 = m00 * cosZ + m10 * sinZ;
        float nm01 = m01 * cosZ + m11 * sinZ;
        float nm02 = m02 * cosZ + m12 * sinZ;
        float nm10 = m00 * m_sinZ + m10 * cosZ;
        float nm11 = m01 * m_sinZ + m11 * cosZ;
        float nm12 = m02 * m_sinZ + m12 * cosZ;
        // rotateY
        float nm20 = nm00 * sinY + m20 * cosY;
        float nm21 = nm01 * sinY + m21 * cosY;
        float nm22 = nm02 * sinY + m22 * cosY;
        dest.m00 = nm00 * cosY + m20 * m_sinY;
        dest.m01 = nm01 * cosY + m21 * m_sinY;
        dest.m02 = nm02 * cosY + m22 * m_sinY;
        // rotateX
        dest.m10 = nm10 * cosX + nm20 * sinX;
        dest.m11 = nm11 * cosX + nm21 * sinX;
        dest.m12 = nm12 * cosX + nm22 * sinX;
        dest.m20 = nm10 * m_sinX + nm20 * cosX;
        dest.m21 = nm11 * m_sinX + nm21 * cosX;
        dest.m22 = nm12 * m_sinX + nm22 * cosX;
        return dest;
    }

    /**
     * Apply rotation of <code>angles.y</code> radians about the Y axis, followed by a rotation of <code>angles.x</code> radians about the X axis and
     * followed by a rotation of <code>angles.z</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateY(angles.y).rotateX(angles.x).rotateZ(angles.z)</code>
     * 
     * @param angles
     *            the Euler angles
     * @return this
     */
    public Matrix3f rotateYXZ(Vector3f angles) {
        return rotateYXZ(angles.y, angles.x, angles.z);
    }

    /**
     * Apply rotation of <code>angleY</code> radians about the Y axis, followed by a rotation of <code>angleX</code> radians about the X axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * This method is equivalent to calling: <code>rotateY(angleY).rotateX(angleX).rotateZ(angleZ)</code>
     * 
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix3f rotateYXZ(float angleY, float angleX, float angleZ) {
        return rotateYXZ(angleY, angleX, angleZ, this);
    }

    public Matrix3f rotateYXZ(float angleY, float angleX, float angleZ, Matrix3f dest) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float m_sinY = -sinY;
        float m_sinX = -sinX;
        float m_sinZ = -sinZ;

        // rotateY
        float nm20 = m00 * sinY + m20 * cosY;
        float nm21 = m01 * sinY + m21 * cosY;
        float nm22 = m02 * sinY + m22 * cosY;
        float nm00 = m00 * cosY + m20 * m_sinY;
        float nm01 = m01 * cosY + m21 * m_sinY;
        float nm02 = m02 * cosY + m22 * m_sinY;
        // rotateX
        float nm10 = m10 * cosX + nm20 * sinX;
        float nm11 = m11 * cosX + nm21 * sinX;
        float nm12 = m12 * cosX + nm22 * sinX;
        dest.m20 = m10 * m_sinX + nm20 * cosX;
        dest.m21 = m11 * m_sinX + nm21 * cosX;
        dest.m22 = m12 * m_sinX + nm22 * cosX;
        // rotateZ
        dest.m00 = nm00 * cosZ + nm10 * sinZ;
        dest.m01 = nm01 * cosZ + nm11 * sinZ;
        dest.m02 = nm02 * cosZ + nm12 * sinZ;
        dest.m10 = nm00 * m_sinZ + nm10 * cosZ;
        dest.m11 = nm01 * m_sinZ + nm11 * cosZ;
        dest.m12 = nm02 * m_sinZ + nm12 * cosZ;
        return dest;
    }

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the given axis specified as x, y and z components.
     * <p>
     * The axis described by the three components needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the axis
     * @param y
     *            the y component of the axis
     * @param z
     *            the z component of the axis
     * @return this
     */
    public Matrix3f rotate(float ang, float x, float y, float z) {
        return rotate(ang, x, y, z, this);
    }

    public Matrix3f rotate(float ang, float x, float y, float z, Matrix3f dest) {
        float s = Math.sin(ang);
        float c = Math.cosFromSin(s, ang);
        float C = 1.0f - c;

        // rotation matrix elements:
        // m30, m31, m32, m03, m13, m23 = 0
        float xx = x * x, xy = x * y, xz = x * z;
        float yy = y * y, yz = y * z;
        float zz = z * z;
        float rm00 = xx * C + c;
        float rm01 = xy * C + z * s;
        float rm02 = xz * C - y * s;
        float rm10 = xy * C - z * s;
        float rm11 = yy * C + c;
        float rm12 = yz * C + x * s;
        float rm20 = xz * C + y * s;
        float rm21 = yz * C - x * s;
        float rm22 = zz * C + c;

        // add temporaries for dependent values
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        // set non-dependent values directly
        dest.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        dest.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
        // set other values
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis and store the result in <code>dest</code>.
     * <p>
     * The axis described by the three components needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotation(float, float, float, float) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(float, float, float, float)
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the axis
     * @param y
     *            the y component of the axis
     * @param z
     *            the z component of the axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f rotateLocal(float ang, float x, float y, float z, Matrix3f dest) {
        float s = Math.sin(ang);
        float c = Math.cosFromSin(s, ang);
        float C = 1.0f - c;
        float xx = x * x, xy = x * y, xz = x * z;
        float yy = y * y, yz = y * z;
        float zz = z * z;
        float lm00 = xx * C + c;
        float lm01 = xy * C + z * s;
        float lm02 = xz * C - y * s;
        float lm10 = xy * C - z * s;
        float lm11 = yy * C + c;
        float lm12 = yz * C + x * s;
        float lm20 = xz * C + y * s;
        float lm21 = yz * C - x * s;
        float lm22 = zz * C + c;
        float nm00 = lm00 * m00 + lm10 * m01 + lm20 * m02;
        float nm01 = lm01 * m00 + lm11 * m01 + lm21 * m02;
        float nm02 = lm02 * m00 + lm12 * m01 + lm22 * m02;
        float nm10 = lm00 * m10 + lm10 * m11 + lm20 * m12;
        float nm11 = lm01 * m10 + lm11 * m11 + lm21 * m12;
        float nm12 = lm02 * m10 + lm12 * m11 + lm22 * m12;
        float nm20 = lm00 * m20 + lm10 * m21 + lm20 * m22;
        float nm21 = lm01 * m20 + lm11 * m21 + lm21 * m22;
        float nm22 = lm02 * m20 + lm12 * m21 + lm22 * m22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis.
     * <p>
     * The axis described by the three components needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotation(float, float, float, float) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(float, float, float, float)
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the axis
     * @param y
     *            the y component of the axis
     * @param z
     *            the z component of the axis
     * @return this
     */
    public Matrix3f rotateLocal(float ang, float x, float y, float z) {
        return rotateLocal(ang, x, y, z, this);
    }

    /**
     * Pre-multiply a rotation around the X axis to this matrix by rotating the given amount of radians
     * about the X axis and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationX(float) rotationX()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationX(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the X axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f rotateLocalX(float ang, Matrix3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm01 = cos * m01 - sin * m02;
        float nm02 = sin * m01 + cos * m02;
        float nm11 = cos * m11 - sin * m12;
        float nm12 = sin * m11 + cos * m12;
        float nm21 = cos * m21 - sin * m22;
        float nm22 = sin * m21 + cos * m22;
        dest.m00 = m00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = m10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = m20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationX(float) rotationX()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationX(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the X axis
     * @return this
     */
    public Matrix3f rotateLocalX(float ang) {
        return rotateLocalX(ang, this);
    }

    /**
     * Pre-multiply a rotation around the Y axis to this matrix by rotating the given amount of radians
     * about the Y axis and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationY(float) rotationY()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationY(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the Y axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f rotateLocalY(float ang, Matrix3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm00 =  cos * m00 + sin * m02;
        float nm02 = -sin * m00 + cos * m02;
        float nm10 =  cos * m10 + sin * m12;
        float nm12 = -sin * m10 + cos * m12;
        float nm20 =  cos * m20 + sin * m22;
        float nm22 = -sin * m20 + cos * m22;
        dest.m00 = nm00;
        dest.m01 = m01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = m11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = m21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians about the Y axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationY(float) rotationY()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationY(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the Y axis
     * @return this
     */
    public Matrix3f rotateLocalY(float ang) {
        return rotateLocalY(ang, this);
    }

    /**
     * Pre-multiply a rotation around the Z axis to this matrix by rotating the given amount of radians
     * about the Z axis and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationZ(float) rotationZ()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationZ(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the Z axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f rotateLocalZ(float ang, Matrix3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm00 = cos * m00 - sin * m01;
        float nm01 = sin * m00 + cos * m01;
        float nm10 = cos * m10 - sin * m11;
        float nm11 = sin * m10 + cos * m11;
        float nm20 = cos * m20 - sin * m21;
        float nm21 = sin * m20 + cos * m21;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = m02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = m12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = m22;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotationZ(float) rotationY()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotationY(float)
     * 
     * @param ang
     *            the angle in radians to rotate about the Z axis
     * @return this
     */
    public Matrix3f rotateLocalZ(float ang) {
        return rotateLocalZ(ang, this);
    }

    /**
     * Apply the rotation - and possibly scaling - transformation of the given {@link Quaternionfc} to this matrix.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>Q</code> the rotation matrix obtained from the given quaternion,
     * then the new matrix will be <code>M * Q</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * Q * v</code>,
     * the quaternion rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(Quaternionfc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @return this
     */
    public Matrix3f rotate(Quaternionfc quat) {
        return rotate(quat, this);
    }

    /**
     * Apply the rotation - and possibly scaling - transformation of the given {@link Quaternionfc} to this matrix and store
     * the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>Q</code> the rotation matrix obtained from the given quaternion,
     * then the new matrix will be <code>M * Q</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * Q * v</code>,
     * the quaternion rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(Quaternionfc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3f rotate(Quaternionfc quat, Matrix3f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = dxy - dzw;
        float rm11 = y2 - z2 + w2 - x2;
        float rm12 = dyz + dxw;
        float rm20 = dyw + dxz;
        float rm21 = dyz - dxw;
        float rm22 = z2 - y2 - x2 + w2;
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        dest.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        dest.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        return dest;
    }

    /**
     * Pre-multiply the rotation - and possibly scaling - transformation of the given {@link Quaternionfc} to this matrix and store
     * the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>Q</code> the rotation matrix obtained from the given quaternion,
     * then the new matrix will be <code>Q * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>Q * M * v</code>,
     * the quaternion rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation transformation without pre-multiplying,
     * use {@link #rotation(Quaternionfc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3f rotateLocal(Quaternionfc quat, Matrix3f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float lm00 = w2 + x2 - z2 - y2;
        float lm01 = dxy + dzw;
        float lm02 = dxz - dyw;
        float lm10 = dxy - dzw;
        float lm11 = y2 - z2 + w2 - x2;
        float lm12 = dyz + dxw;
        float lm20 = dyw + dxz;
        float lm21 = dyz - dxw;
        float lm22 = z2 - y2 - x2 + w2;
        float nm00 = lm00 * m00 + lm10 * m01 + lm20 * m02;
        float nm01 = lm01 * m00 + lm11 * m01 + lm21 * m02;
        float nm02 = lm02 * m00 + lm12 * m01 + lm22 * m02;
        float nm10 = lm00 * m10 + lm10 * m11 + lm20 * m12;
        float nm11 = lm01 * m10 + lm11 * m11 + lm21 * m12;
        float nm12 = lm02 * m10 + lm12 * m11 + lm22 * m12;
        float nm20 = lm00 * m20 + lm10 * m21 + lm20 * m22;
        float nm21 = lm01 * m20 + lm11 * m21 + lm21 * m22;
        float nm22 = lm02 * m20 + lm12 * m21 + lm22 * m22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Pre-multiply the rotation - and possibly scaling - transformation of the given {@link Quaternionfc} to this matrix.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>Q</code> the rotation matrix obtained from the given quaternion,
     * then the new matrix will be <code>Q * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>Q * M * v</code>,
     * the quaternion rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation transformation without pre-multiplying,
     * use {@link #rotation(Quaternionfc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @return this
     */
    public Matrix3f rotateLocal(Quaternionfc quat) {
        return rotateLocal(quat, this);
    }

    /**
     * Apply a rotation transformation, rotating about the given {@link AxisAngle4f}, to this matrix.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given {@link AxisAngle4f},
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the {@link AxisAngle4f} rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(AxisAngle4f)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float)
     * @see #rotation(AxisAngle4f)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f} (needs to be {@link AxisAngle4f#normalize() normalized})
     * @return this
     */
    public Matrix3f rotate(AxisAngle4f axisAngle) {
        return rotate(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z);
    }

    /**
     * Apply a rotation transformation, rotating about the given {@link AxisAngle4f} and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given {@link AxisAngle4f},
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the {@link AxisAngle4f} rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(AxisAngle4f)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float)
     * @see #rotation(AxisAngle4f)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f} (needs to be {@link AxisAngle4f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3f rotate(AxisAngle4f axisAngle, Matrix3f dest) {
        return rotate(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z, dest);
    }

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis, to this matrix.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given angle and axis,
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the axis-angle rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(float, Vector3fc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float)
     * @see #rotation(float, Vector3fc)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the rotation axis (needs to be {@link Vector3f#normalize() normalized})
     * @return this
     */
    public Matrix3f rotate(float angle, Vector3fc axis) {
        return rotate(angle, axis.x(), axis.y(), axis.z());
    }

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given angle and axis,
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the axis-angle rotation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying,
     * use {@link #rotation(float, Vector3fc)}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float)
     * @see #rotation(float, Vector3fc)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the rotation axis (needs to be {@link Vector3f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3f rotate(float angle, Vector3fc axis, Matrix3f dest) {
        return rotate(angle, axis.x(), axis.y(), axis.z(), dest);
    }

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(Vector3fc, Vector3fc) setLookAlong()}.
     * 
     * @see #lookAlong(float, float, float, float, float, float)
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix3f lookAlong(Vector3fc dir, Vector3fc up) {
        return lookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), this);
    }

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>
     * and store the result in <code>dest</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(Vector3fc, Vector3fc) setLookAlong()}.
     * 
     * @see #lookAlong(float, float, float, float, float, float)
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f lookAlong(Vector3fc dir, Vector3fc up, Matrix3f dest) {
        return lookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>
     * and store the result in <code>dest</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(float, float, float, float, float, float) setLookAlong()}
     * 
     * @see #setLookAlong(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to look along
     * @param dirY
     *              the y-coordinate of the direction to look along
     * @param dirZ
     *              the z-coordinate of the direction to look along
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    public Matrix3f lookAlong(float dirX, float dirY, float dirZ,
                              float upX, float upY, float upZ, Matrix3f dest) {
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= -invDirLength;
        dirY *= -invDirLength;
        dirZ *= -invDirLength;
        // left = up x direction
        float leftX, leftY, leftZ;
        leftX = upY * dirZ - upZ * dirY;
        leftY = upZ * dirX - upX * dirZ;
        leftZ = upX * dirY - upY * dirX;
        // normalize left
        float invLeftLength = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirY * leftZ - dirZ * leftY;
        float upnY = dirZ * leftX - dirX * leftZ;
        float upnZ = dirX * leftY - dirY * leftX;

        // calculate right matrix elements
        float rm00 = leftX;
        float rm01 = upnX;
        float rm02 = dirX;
        float rm10 = leftY;
        float rm11 = upnY;
        float rm12 = dirY;
        float rm20 = leftZ;
        float rm21 = upnZ;
        float rm22 = dirZ;

        // perform optimized matrix multiplication
        // introduce temporaries for dependent results
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        dest.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        dest.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
        // set the rest of the matrix elements
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        return dest;
    }

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(float, float, float, float, float, float) setLookAlong()}
     * 
     * @see #setLookAlong(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to look along
     * @param dirY
     *              the y-coordinate of the direction to look along
     * @param dirZ
     *              the z-coordinate of the direction to look along
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix3f lookAlong(float dirX, float dirY, float dirZ,
                              float upX, float upY, float upZ) {
        return lookAlong(dirX, dirY, dirZ, upX, upY, upZ, this);
    }

    /**
     * Set this matrix to a rotation transformation to make <code>-z</code>
     * point along <code>dir</code>.
     * <p>
     * In order to apply the lookalong transformation to any previous existing transformation,
     * use {@link #lookAlong(Vector3fc, Vector3fc)}.
     * 
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * @see #lookAlong(Vector3fc, Vector3fc)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix3f setLookAlong(Vector3fc dir, Vector3fc up) {
        return setLookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a rotation transformation to make <code>-z</code>
     * point along <code>dir</code>.
     * <p>
     * In order to apply the lookalong transformation to any previous existing transformation,
     * use {@link #lookAlong(float, float, float, float, float, float) lookAlong()}
     * 
     * @see #setLookAlong(float, float, float, float, float, float)
     * @see #lookAlong(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to look along
     * @param dirY
     *              the y-coordinate of the direction to look along
     * @param dirZ
     *              the z-coordinate of the direction to look along
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix3f setLookAlong(float dirX, float dirY, float dirZ,
                                 float upX, float upY, float upZ) {
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= -invDirLength;
        dirY *= -invDirLength;
        dirZ *= -invDirLength;
        // left = up x direction
        float leftX, leftY, leftZ;
        leftX = upY * dirZ - upZ * dirY;
        leftY = upZ * dirX - upX * dirZ;
        leftZ = upX * dirY - upY * dirX;
        // normalize left
        float invLeftLength = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirY * leftZ - dirZ * leftY;
        float upnY = dirZ * leftX - dirX * leftZ;
        float upnZ = dirX * leftY - dirY * leftX;

        m00 = leftX;
        m01 = upnX;
        m02 = dirX;
        m10 = leftY;
        m11 = upnY;
        m12 = dirY;
        m20 = leftZ;
        m21 = upnZ;
        m22 = dirZ;

        return this;
    }

    public Vector3f getRow(int row, Vector3f dest) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            return dest.set(m00, m10, m20);
        case 1:
            return dest.set(m01, m11, m21);
        case 2:
            return dest.set(m02, m12, m22);
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Set the row at the given <code>row</code> index, starting with <code>0</code>.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param src
     *          the row components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..2]</code>
     */
    public Matrix3f setRow(int row, Vector3fc src) throws IndexOutOfBoundsException {
        return setRow(row, src.x(), src.y(), src.z());
    }

    /**
     * Set the row at the given <code>row</code> index, starting with <code>0</code>.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param x
     *          the first element in the row
     * @param y
     *          the second element in the row
     * @param z
     *          the third element in the row
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..2]</code>
     */
    public Matrix3f setRow(int row, float x, float y, float z) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            this.m00 = x;
            this.m10 = y;
            this.m20 = z;
            break;
        case 1:
            this.m01 = x;
            this.m11 = y;
            this.m21 = z;
            break;
        case 2:
            this.m02 = x;
            this.m12 = y;
            this.m22 = z;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public Vector3f getColumn(int column, Vector3f dest) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            return dest.set(m00, m01, m02);
        case 1:
            return dest.set(m10, m11, m12);
        case 2:
            return dest.set(m20, m21, m22);
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Set the column at the given <code>column</code> index, starting with <code>0</code>.
     * 
     * @param column
     *          the column index in <code>[0..2]</code>
     * @param src
     *          the column components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..2]</code>
     */
    public Matrix3f setColumn(int column, Vector3fc src) throws IndexOutOfBoundsException {
        return setColumn(column, src.x(), src.y(), src.z());
    }

    /**
     * Set the column at the given <code>column</code> index, starting with <code>0</code>.
     * 
     * @param column
     *          the column index in <code>[0..2]</code>
     * @param x
     *          the first element in the column
     * @param y
     *          the second element in the column
     * @param z
     *          the third element in the column
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..2]</code>
     */
    public Matrix3f setColumn(int column, float x, float y, float z) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            this.m00 = x;
            this.m01 = y;
            this.m02 = z;
            break;
        case 1:
            this.m10 = x;
            this.m11 = y;
            this.m12 = z;
            break;
        case 2:
            this.m20 = x;
            this.m21 = y;
            this.m22 = z;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public float get(int column, int row) {
        return MemUtil.INSTANCE.get(this, column, row);
    }

    /**
     * Set the matrix element at the given column and row to the specified value.
     * 
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param value
     *          the value
     * @return this
     */
    public Matrix3f set(int column, int row, float value) {
        return MemUtil.INSTANCE.set(this, column, row, value);
    }

    public float getRowColumn(int row, int column) {
        return MemUtil.INSTANCE.get(this, column, row);
    }

    /**
     * Set the matrix element at the given row and column to the specified value.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @param value
     *          the value
     * @return this
     */
    public Matrix3f setRowColumn(int row, int column, float value) {
        return MemUtil.INSTANCE.set(this, column, row, value);
    }

    /**
     * Set <code>this</code> matrix to its own normal matrix.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In this case, use {@link #set(Matrix3fc)} to set a given Matrix3f to this matrix.
     * 
     * @see #set(Matrix3fc)
     * 
     * @return this
     */
    public Matrix3f normal() {
        return normal(this);
    }

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In this case, use {@link #set(Matrix3fc)} to set a given Matrix3f to this matrix.
     * 
     * @see #set(Matrix3fc)
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix3f normal(Matrix3f dest) {
        float m00m11 = m00 * m11;
        float m01m10 = m01 * m10;
        float m02m10 = m02 * m10;
        float m00m12 = m00 * m12;
        float m01m12 = m01 * m12;
        float m02m11 = m02 * m11;
        float det = (m00m11 - m01m10) * m22 + (m02m10 - m00m12) * m21 + (m01m12 - m02m11) * m20;
        float s = 1.0f / det;
        /* Invert and transpose in one go */
        float nm00 = (m11 * m22 - m21 * m12) * s;
        float nm01 = (m20 * m12 - m10 * m22) * s;
        float nm02 = (m10 * m21 - m20 * m11) * s;
        float nm10 = (m21 * m02 - m01 * m22) * s;
        float nm11 = (m00 * m22 - m20 * m02) * s;
        float nm12 = (m20 * m01 - m00 * m21) * s;
        float nm20 = (m01m12 - m02m11) * s;
        float nm21 = (m02m10 - m00m12) * s;
        float nm22 = (m00m11 - m01m10) * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    /**
     * Compute the cofactor matrix of <code>this</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal()} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @return this
     */
    public Matrix3f cofactor() {
        return cofactor(this);
    }

    /**
     * Compute the cofactor matrix of <code>this</code> and store it into <code>dest</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal(Matrix3f)} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix3f cofactor(Matrix3f dest) {
        float nm00 = m11 * m22 - m21 * m12;
        float nm01 = m20 * m12 - m10 * m22;
        float nm02 = m10 * m21 - m20 * m11;
        float nm10 = m21 * m02 - m01 * m22;
        float nm11 = m00 * m22 - m20 * m02;
        float nm12 = m20 * m01 - m00 * m21;
        float nm20 = m01 * m12 - m11 * m02;
        float nm21 = m02 * m10 - m12 * m00;
        float nm22 = m00 * m11 - m10 * m01;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        return dest;
    }

    public Vector3f getScale(Vector3f dest) {
        return dest.set(Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02),
                        Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12),
                        Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22));
    }

    public Vector3f positiveZ(Vector3f dir) {
        dir.x = m10 * m21 - m11 * m20;
        dir.y = m20 * m01 - m21 * m00;
        dir.z = m00 * m11 - m01 * m10;
        return dir.normalize(dir);
    }

    public Vector3f normalizedPositiveZ(Vector3f dir) {
        dir.x = m02;
        dir.y = m12;
        dir.z = m22;
        return dir;
    }

    public Vector3f positiveX(Vector3f dir) {
        dir.x = m11 * m22 - m12 * m21;
        dir.y = m02 * m21 - m01 * m22;
        dir.z = m01 * m12 - m02 * m11;
        return dir.normalize(dir);
    }

    public Vector3f normalizedPositiveX(Vector3f dir) {
        dir.x = m00;
        dir.y = m10;
        dir.z = m20;
        return dir;
    }

    public Vector3f positiveY(Vector3f dir) {
        dir.x = m12 * m20 - m10 * m22;
        dir.y = m00 * m22 - m02 * m20;
        dir.z = m02 * m10 - m00 * m12;
        return dir.normalize(dir);
    }

    public Vector3f normalizedPositiveY(Vector3f dir) {
        dir.x = m01;
        dir.y = m11;
        dir.z = m21;
        return dir;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(m00);
        result = prime * result + Float.floatToIntBits(m01);
        result = prime * result + Float.floatToIntBits(m02);
        result = prime * result + Float.floatToIntBits(m10);
        result = prime * result + Float.floatToIntBits(m11);
        result = prime * result + Float.floatToIntBits(m12);
        result = prime * result + Float.floatToIntBits(m20);
        result = prime * result + Float.floatToIntBits(m21);
        result = prime * result + Float.floatToIntBits(m22);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Matrix3f other = (Matrix3f) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00))
            return false;
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01))
            return false;
        if (Float.floatToIntBits(m02) != Float.floatToIntBits(other.m02))
            return false;
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10))
            return false;
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11))
            return false;
        if (Float.floatToIntBits(m12) != Float.floatToIntBits(other.m12))
            return false;
        if (Float.floatToIntBits(m20) != Float.floatToIntBits(other.m20))
            return false;
        if (Float.floatToIntBits(m21) != Float.floatToIntBits(other.m21))
            return false;
        if (Float.floatToIntBits(m22) != Float.floatToIntBits(other.m22))
            return false;
        return true;
    }

    public boolean equals(Matrix3fc m, float delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix3f))
            return false;
        if (!Runtime.equals(m00, m.m00(), delta))
            return false;
        if (!Runtime.equals(m01, m.m01(), delta))
            return false;
        if (!Runtime.equals(m02, m.m02(), delta))
            return false;
        if (!Runtime.equals(m10, m.m10(), delta))
            return false;
        if (!Runtime.equals(m11, m.m11(), delta))
            return false;
        if (!Runtime.equals(m12, m.m12(), delta))
            return false;
        if (!Runtime.equals(m20, m.m20(), delta))
            return false;
        if (!Runtime.equals(m21, m.m21(), delta))
            return false;
        if (!Runtime.equals(m22, m.m22(), delta))
            return false;
        return true;
    }

    /**
     * Exchange the values of <code>this</code> matrix with the given <code>other</code> matrix.
     * 
     * @param other
     *          the other matrix to exchange the values with
     * @return this
     */
    public Matrix3f swap(Matrix3f other) {
        MemUtil.INSTANCE.swap(this, other);
        return this;
    }

    /**
     * Component-wise add <code>this</code> and <code>other</code>.
     * 
     * @param other
     *          the other addend 
     * @return this
     */
    public Matrix3f add(Matrix3fc other) {
        return add(other, this);
    }

    public Matrix3f add(Matrix3fc other, Matrix3f dest) {
        dest.m00 = m00 + other.m00();
        dest.m01 = m01 + other.m01();
        dest.m02 = m02 + other.m02();
        dest.m10 = m10 + other.m10();
        dest.m11 = m11 + other.m11();
        dest.m12 = m12 + other.m12();
        dest.m20 = m20 + other.m20();
        dest.m21 = m21 + other.m21();
        dest.m22 = m22 + other.m22();
        return dest;
    }

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code>.
     * 
     * @param subtrahend
     *          the subtrahend
     * @return this
     */
    public Matrix3f sub(Matrix3fc subtrahend) {
        return sub(subtrahend, this);
    }

    public Matrix3f sub(Matrix3fc subtrahend, Matrix3f dest) {
        dest.m00 = m00 - subtrahend.m00();
        dest.m01 = m01 - subtrahend.m01();
        dest.m02 = m02 - subtrahend.m02();
        dest.m10 = m10 - subtrahend.m10();
        dest.m11 = m11 - subtrahend.m11();
        dest.m12 = m12 - subtrahend.m12();
        dest.m20 = m20 - subtrahend.m20();
        dest.m21 = m21 - subtrahend.m21();
        dest.m22 = m22 - subtrahend.m22();
        return dest;
    }

    /**
     * Component-wise multiply <code>this</code> by <code>other</code>.
     * 
     * @param other
     *          the other matrix
     * @return this
     */
    public Matrix3f mulComponentWise(Matrix3fc other) {
        return mulComponentWise(other, this);
    }

    public Matrix3f mulComponentWise(Matrix3fc other, Matrix3f dest) {
        dest.m00 = m00 * other.m00();
        dest.m01 = m01 * other.m01();
        dest.m02 = m02 * other.m02();
        dest.m10 = m10 * other.m10();
        dest.m11 = m11 * other.m11();
        dest.m12 = m12 * other.m12();
        dest.m20 = m20 * other.m20();
        dest.m21 = m21 * other.m21();
        dest.m22 = m22 * other.m22();
        return dest;
    }

    /**
     * Set this matrix to a skew-symmetric matrix using the following layout:
     * <pre>
     *  0,  a, -b
     * -a,  0,  c
     *  b, -c,  0
     * </pre>
     * 
     * Reference: <a href="https://en.wikipedia.org/wiki/Skew-symmetric_matrix">https://en.wikipedia.org</a>
     * 
     * @param a
     *          the value used for the matrix elements m01 and m10
     * @param b
     *          the value used for the matrix elements m02 and m20
     * @param c
     *          the value used for the matrix elements m12 and m21
     * @return this
     */
    public Matrix3f setSkewSymmetric(float a, float b, float c) {
        m00 = m11 = m22 = 0;
        m01 = -a;
        m02 = b;
        m10 = a;
        m12 = -c;
        m20 = -b;
        m21 = c;
        return this;
    }

    /**
     * Linearly interpolate <code>this</code> and <code>other</code> using the given interpolation factor <code>t</code>
     * and store the result in <code>this</code>.
     * <p>
     * If <code>t</code> is <code>0.0</code> then the result is <code>this</code>. If the interpolation factor is <code>1.0</code>
     * then the result is <code>other</code>.
     *
     * @param other
     *          the other matrix
     * @param t
     *          the interpolation factor between 0.0 and 1.0
     * @return this
     */
    public Matrix3f lerp(Matrix3fc other, float t) {
        return lerp(other, t, this);
    }

    public Matrix3f lerp(Matrix3fc other, float t, Matrix3f dest) {
        dest.m00 = Math.fma(other.m00() - m00, t, m00);
        dest.m01 = Math.fma(other.m01() - m01, t, m01);
        dest.m02 = Math.fma(other.m02() - m02, t, m02);
        dest.m10 = Math.fma(other.m10() - m10, t, m10);
        dest.m11 = Math.fma(other.m11() - m11, t, m11);
        dest.m12 = Math.fma(other.m12() - m12, t, m12);
        dest.m20 = Math.fma(other.m20() - m20, t, m20);
        dest.m21 = Math.fma(other.m21() - m21, t, m21);
        dest.m22 = Math.fma(other.m22() - m22, t, m22);
        return dest;
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>direction</code>
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(Vector3fc, Vector3fc) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(new Vector3f(dir).negate(), up).invert(), dest)</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float, Matrix3f)
     * @see #rotationTowards(Vector3fc, Vector3fc)
     * 
     * @param direction
     *              the direction to rotate towards
     * @param up
     *              the model's up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    public Matrix3f rotateTowards(Vector3fc direction, Vector3fc up, Matrix3f dest) {
        return rotateTowards(direction.x(), direction.y(), direction.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>direction</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(Vector3fc, Vector3fc) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(new Vector3f(dir).negate(), up).invert())</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float)
     * @see #rotationTowards(Vector3fc, Vector3fc)
     * 
     * @param direction
     *              the direction to orient towards
     * @param up
     *              the up vector
     * @return this
     */
    public Matrix3f rotateTowards(Vector3fc direction, Vector3fc up) {
        return rotateTowards(direction.x(), direction.y(), direction.z(), up.x(), up.y(), up.z(), this);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>direction</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(float, float, float, float, float, float) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(-dirX, -dirY, -dirZ, upX, upY, upZ).invert())</code>
     * 
     * @see #rotateTowards(Vector3fc, Vector3fc)
     * @see #rotationTowards(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to rotate towards
     * @param dirY
     *              the y-coordinate of the direction to rotate towards
     * @param dirZ
     *              the z-coordinate of the direction to rotate towards
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix3f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        return rotateTowards(dirX, dirY, dirZ, upX, upY, upZ, this);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>dir</code>
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(float, float, float, float, float, float) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(-dirX, -dirY, -dirZ, upX, upY, upZ).invert(), dest)</code>
     * 
     * @see #rotateTowards(Vector3fc, Vector3fc)
     * @see #rotationTowards(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to rotate towards
     * @param dirY
     *              the y-coordinate of the direction to rotate towards
     * @param dirZ
     *              the z-coordinate of the direction to rotate towards
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    public Matrix3f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix3f dest) {
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        float ndirX = dirX * invDirLength;
        float ndirY = dirY * invDirLength;
        float ndirZ = dirZ * invDirLength;
        // left = up x direction
        float leftX, leftY, leftZ;
        leftX = upY * ndirZ - upZ * ndirY;
        leftY = upZ * ndirX - upX * ndirZ;
        leftZ = upX * ndirY - upY * ndirX;
        // normalize left
        float invLeftLength = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = ndirY * leftZ - ndirZ * leftY;
        float upnY = ndirZ * leftX - ndirX * leftZ;
        float upnZ = ndirX * leftY - ndirY * leftX;
        float rm00 = leftX;
        float rm01 = leftY;
        float rm02 = leftZ;
        float rm10 = upnX;
        float rm11 = upnY;
        float rm12 = upnZ;
        float rm20 = ndirX;
        float rm21 = ndirY;
        float rm22 = ndirZ;
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        dest.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        dest.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        return dest;
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>center - eye</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAlong(new Vector3f(dir).negate(), up).invert()</code>
     * 
     * @see #rotationTowards(Vector3fc, Vector3fc)
     * @see #rotateTowards(float, float, float, float, float, float)
     * 
     * @param dir
     *              the direction to orient the local -z axis towards
     * @param up
     *              the up vector
     * @return this
     */
    public Matrix3f rotationTowards(Vector3fc dir, Vector3fc up) {
        return rotationTowards(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>center - eye</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAlong(-dirX, -dirY, -dirZ, upX, upY, upZ).invert()</code>
     * 
     * @see #rotateTowards(Vector3fc, Vector3fc)
     * @see #rotationTowards(float, float, float, float, float, float)
     * 
     * @param dirX
     *              the x-coordinate of the direction to rotate towards
     * @param dirY
     *              the y-coordinate of the direction to rotate towards
     * @param dirZ
     *              the z-coordinate of the direction to rotate towards
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix3f rotationTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        float ndirX = dirX * invDirLength;
        float ndirY = dirY * invDirLength;
        float ndirZ = dirZ * invDirLength;
        // left = up x direction
        float leftX, leftY, leftZ;
        leftX = upY * ndirZ - upZ * ndirY;
        leftY = upZ * ndirX - upX * ndirZ;
        leftZ = upX * ndirY - upY * ndirX;
        // normalize left
        float invLeftLength = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = ndirY * leftZ - ndirZ * leftY;
        float upnY = ndirZ * leftX - ndirX * leftZ;
        float upnZ = ndirX * leftY - ndirY * leftX;
        this.m00 = leftX;
        this.m01 = leftY;
        this.m02 = leftZ;
        this.m10 = upnX;
        this.m11 = upnY;
        this.m12 = upnZ;
        this.m20 = ndirX;
        this.m21 = ndirY;
        this.m22 = ndirZ;
        return this;
    }

    /**
     * Extract the Euler angles from the rotation represented by <code>this</code> matrix and store the extracted Euler angles in <code>dest</code>.
     * <p>
     * This method assumes that <code>this</code> matrix only represents a rotation without scaling.
     * <p>
     * Note that the returned Euler angles must be applied in the order <code>Z * Y * X</code> to obtain the identical matrix.
     * This means that calling {@link Matrix3f#rotateZYX(float, float, float)} using the obtained Euler angles will yield
     * the same rotation as the original matrix from which the Euler angles were obtained, so in the below code the matrix
     * <code>m2</code> should be identical to <code>m</code> (disregarding possible floating-point inaccuracies).
     * <pre>
     * Matrix3f m = ...; // &lt;- matrix only representing rotation
     * Matrix3f n = new Matrix3f();
     * n.rotateZYX(m.getEulerAnglesZYX(new Vector3f()));
     * </pre>
     * <p>
     * Reference: <a href="http://nghiaho.com/?page_id=846">http://nghiaho.com/</a>
     * 
     * @param dest
     *          will hold the extracted Euler angles
     * @return dest
     */
    public Vector3f getEulerAnglesZYX(Vector3f dest) {
        dest.x = Math.atan2(m12, m22);
        dest.y = Math.atan2(-m02, Math.sqrt(m12 * m12 + m22 * m22));
        dest.z = Math.atan2(m01, m00);
        return dest;
    }

    /**
     * Apply an oblique projection transformation to this matrix with the given values for <code>a</code> and
     * <code>b</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the oblique transformation matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * oblique transformation will be applied first!
     * <p>
     * The oblique transformation is defined as:
     * <pre>
     * x' = x + a*z
     * y' = y + a*z
     * z' = z
     * </pre>
     * or in matrix form:
     * <pre>
     * 1 0 a
     * 0 1 b
     * 0 0 1
     * </pre>
     * 
     * @param a
     *            the value for the z factor that applies to x
     * @param b
     *            the value for the z factor that applies to y
     * @return this
     */
    public Matrix3f obliqueZ(float a, float b) {
        this.m20 = m00 * a + m10 * b + m20;
        this.m21 = m01 * a + m11 * b + m21;
        this.m22 = m02 * a + m12 * b + m22;
        return this;
    }

    /**
     * Apply an oblique projection transformation to this matrix with the given values for <code>a</code> and
     * <code>b</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the oblique transformation matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * oblique transformation will be applied first!
     * <p>
     * The oblique transformation is defined as:
     * <pre>
     * x' = x + a*z
     * y' = y + a*z
     * z' = z
     * </pre>
     * or in matrix form:
     * <pre>
     * 1 0 a
     * 0 1 b
     * 0 0 1
     * </pre>
     * 
     * @param a
     *            the value for the z factor that applies to x
     * @param b
     *            the value for the z factor that applies to y
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3f obliqueZ(float a, float b, Matrix3f dest) {
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m20 = m00 * a + m10 * b + m20;
        dest.m21 = m01 * a + m11 * b + m21;
        dest.m22 = m02 * a + m12 * b + m22;
        return dest;
    }

    public Matrix3f reflect(float nx, float ny, float nz, Matrix3f dest) {
        float da = nx + nx, db = ny + ny, dc = nz + nz;
        float rm00 = 1.0f - da * nx;
        float rm01 = -da * ny;
        float rm02 = -da * nz;
        float rm10 = -db * nx;
        float rm11 = 1.0f - db * ny;
        float rm12 = -db * nz;
        float rm20 = -dc * nx;
        float rm21 = -dc * ny;
        float rm22 = 1.0f - dc * nz;
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        return dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12);
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through the given plane
     * specified via the plane normal.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param nx
     *          the x-coordinate of the plane normal
     * @param ny
     *          the y-coordinate of the plane normal
     * @param nz
     *          the z-coordinate of the plane normal
     * @return this
     */
    public Matrix3f reflect(float nx, float ny, float nz) {
        return reflect(nx, ny, nz, this);
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through the given plane
     * specified via the plane normal.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param normal
     *          the plane normal
     * @return this
     */
    public Matrix3f reflect(Vector3fc normal) {
        return reflect(normal.x(), normal.y(), normal.z());
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects about a plane
     * specified via the plane orientation.
     * <p>
     * This method can be used to build a reflection transformation based on the orientation of a mirror object in the scene.
     * It is assumed that the default mirror plane's normal is <code>(0, 0, 1)</code>. So, if the given {@link Quaternionfc} is
     * the identity (does not apply any additional rotation), the reflection plane will be <code>z=0</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param orientation
     *          the plane orientation
     * @return this
     */
    public Matrix3f reflect(Quaternionfc orientation) {
        return reflect(orientation, this);
    }

    public Matrix3f reflect(Quaternionfc orientation, Matrix3f dest) {
        float num1 = orientation.x() + orientation.x();
        float num2 = orientation.y() + orientation.y();
        float num3 = orientation.z() + orientation.z();
        float normalX = orientation.x() * num3 + orientation.w() * num2;
        float normalY = orientation.y() * num3 - orientation.w() * num1;
        float normalZ = 1.0f - (orientation.x() * num1 + orientation.y() * num2);
        return reflect(normalX, normalY, normalZ, dest);
    }

    public Matrix3f reflect(Vector3fc normal, Matrix3f dest) {
        return reflect(normal.x(), normal.y(), normal.z(), dest);
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects through the given plane
     * specified via the plane normal.
     * 
     * @param nx
     *          the x-coordinate of the plane normal
     * @param ny
     *          the y-coordinate of the plane normal
     * @param nz
     *          the z-coordinate of the plane normal
     * @return this
     */
    public Matrix3f reflection(float nx, float ny, float nz) {
        float da = nx + nx, db = ny + ny, dc = nz + nz;
        this._m00(1.0f - da * nx);
        this._m01(-da * ny);
        this._m02(-da * nz);
        this._m10(-db * nx);
        this._m11(1.0f - db * ny);
        this._m12(-db * nz);
        this._m20(-dc * nx);
        this._m21(-dc * ny);
        this._m22(1.0f - dc * nz);
        return this;
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects through the given plane
     * specified via the plane normal.
     * 
     * @param normal
     *          the plane normal
     * @return this
     */
    public Matrix3f reflection(Vector3fc normal) {
        return reflection(normal.x(), normal.y(), normal.z());
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects through a plane
     * specified via the plane orientation.
     * <p>
     * This method can be used to build a reflection transformation based on the orientation of a mirror object in the scene.
     * It is assumed that the default mirror plane's normal is <code>(0, 0, 1)</code>. So, if the given {@link Quaternionfc} is
     * the identity (does not apply any additional rotation), the reflection plane will be <code>z=0</code>, offset by the given <code>point</code>.
     * 
     * @param orientation
     *          the plane orientation
     * @return this
     */
    public Matrix3f reflection(Quaternionfc orientation) {
        float num1 = orientation.x() + orientation.x();
        float num2 = orientation.y() + orientation.y();
        float num3 = orientation.z() + orientation.z();
        float normalX = orientation.x() * num3 + orientation.w() * num2;
        float normalY = orientation.y() * num3 - orientation.w() * num1;
        float normalZ = 1.0f - (orientation.x() * num1 + orientation.y() * num2);
        return reflection(normalX, normalY, normalZ);
    }

    public boolean isFinite() {
        return Math.isFinite(m00) && Math.isFinite(m01) && Math.isFinite(m02) &&
               Math.isFinite(m10) && Math.isFinite(m11) && Math.isFinite(m12) &&
               Math.isFinite(m20) && Math.isFinite(m21) && Math.isFinite(m22);
    }

    public float quadraticFormProduct(float x, float y, float z) {
        float Axx = m00 * x + m10 * y + m20 * z;
        float Axy = m01 * x + m11 * y + m21 * z;
        float Axz = m02 * x + m12 * y + m22 * z;
        return x * Axx + y * Axy + z * Axz; 
    }

    public float quadraticFormProduct(Vector3fc v) {
        return quadraticFormProduct(v.x(), v.y(), v.z()); 
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
