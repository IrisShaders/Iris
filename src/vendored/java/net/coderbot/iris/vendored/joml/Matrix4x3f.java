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
 * Contains the definition of an affine 4x3 matrix (4 columns, 3 rows) of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10  m20  m30<br>
 *      m01  m11  m21  m31<br>
 *      m02  m12  m22  m32<br>
 * 
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Matrix4x3f implements Externalizable, Cloneable, Matrix4x3fc {

    private static final long serialVersionUID = 1L;

    float m00, m01, m02;
    float m10, m11, m12;
    float m20, m21, m22;
    float m30, m31, m32;

    int properties;

    /**
     * Create a new {@link Matrix4x3f} and set it to {@link #identity() identity}.
     */
    public Matrix4x3f() {
        m00 = 1.0f;
        m11 = 1.0f;
        m22 = 1.0f;
        properties = PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL;
    }

    /**
     * Create a new {@link Matrix4x3f} by setting its left 3x3 submatrix to the values of the given {@link Matrix3fc}
     * and the rest to identity.
     * 
     * @param mat
     *          the {@link Matrix3fc}
     */
    public Matrix4x3f(Matrix3fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix4x3f} and make it a copy of the given matrix.
     * 
     * @param mat
     *          the {@link Matrix4x3fc} to copy the values from
     */
    public Matrix4x3f(Matrix4x3fc mat) {
        set(mat);
    }

    /**
     * Create a new 4x4 matrix using the supplied float values.
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
     * @param m30
     *          the value of m30
     * @param m31
     *          the value of m31
     * @param m32
     *          the value of m32
     */
    public Matrix4x3f(float m00, float m01, float m02, 
                      float m10, float m11, float m12, 
                      float m20, float m21, float m22,
                      float m30, float m31, float m32) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        determineProperties();
    }


    /**
     * Create a new {@link Matrix4x3f} by reading its 12 float components from the given {@link FloatBuffer}
     * at the buffer's current position.
     * <p>
     * That FloatBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     * 
     * @param buffer
     *          the {@link FloatBuffer} to read the matrix values from
     */
    public Matrix4x3f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        determineProperties();
    }


    /**
     * Create a new {@link Matrix4x3f} and initialize its four columns using the supplied vectors.
     * 
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     * @param col2
     *          the third column
     * @param col3
     *          the fourth column
     */
    public Matrix4x3f(Vector3fc col0, Vector3fc col1, Vector3fc col2, Vector3fc col3) {
        set(col0, col1, col2, col3).
        determineProperties();
    }

    /**
     * Assume the given properties about this matrix.
     * <p>
     * Use one or multiple of 0, {@link Matrix4x3fc#PROPERTY_IDENTITY},
     * {@link Matrix4x3fc#PROPERTY_TRANSLATION}, {@link Matrix4x3fc#PROPERTY_ORTHONORMAL}.
     * 
     * @param properties
     *          bitset of the properties to assume about this matrix
     * @return this
     */
    public Matrix4x3f assume(int properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Compute and set the matrix properties returned by {@link #properties()} based
     * on the current matrix element values.
     * 
     * @return this
     */
    public Matrix4x3f determineProperties() {
        int properties = 0;
        if (m00 == 1.0f && m01 == 0.0f && m02 == 0.0f && m10 == 0.0f && m11 == 1.0f && m12 == 0.0f
                && m20 == 0.0f && m21 == 0.0f && m22 == 1.0f) {
            properties |= PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL;
            if (m30 == 0.0f && m31 == 0.0f && m32 == 0.0f)
                properties |= PROPERTY_IDENTITY;
        }
        /* 
         * We do not determine orthogonality, since it would require arbitrary epsilons
         * and is rather expensive (6 dot products) in the worst case.
         */
        this.properties = properties;
        return this;
    }

    public int properties() {
        return properties;
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
    public float m30() {
        return m30;
    }
    public float m31() {
        return m31;
    }
    public float m32() {
        return m32;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    public Matrix4x3f m00(float m00) {
        this.m00 = m00;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m00 != 1.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 1.
     * 
     * @param m01
     *          the new value
     * @return this
     */
    public Matrix4x3f m01(float m01) {
        this.m01 = m01;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m01 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 2.
     * 
     * @param m02
     *          the new value
     * @return this
     */
    public Matrix4x3f m02(float m02) {
        this.m02 = m02;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m02 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    public Matrix4x3f m10(float m10) {
        this.m10 = m10;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m10 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 1.
     * 
     * @param m11
     *          the new value
     * @return this
     */
    public Matrix4x3f m11(float m11) {
        this.m11 = m11;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m11 != 1.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 2.
     * 
     * @param m12
     *          the new value
     * @return this
     */
    public Matrix4x3f m12(float m12) {
        this.m12 = m12;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m12 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    public Matrix4x3f m20(float m20) {
        this.m20 = m20;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m20 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 1.
     * 
     * @param m21
     *          the new value
     * @return this
     */
    public Matrix4x3f m21(float m21) {
        this.m21 = m21;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m21 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 2.
     * 
     * @param m22
     *          the new value
     * @return this
     */
    public Matrix4x3f m22(float m22) {
        this.m22 = m22;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m22 != 1.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 0.
     * 
     * @param m30
     *          the new value
     * @return this
     */
    public Matrix4x3f m30(float m30) {
        this.m30 = m30;
        if (m30 != 0.0f)
            properties &= ~PROPERTY_IDENTITY;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 1.
     * 
     * @param m31
     *          the new value
     * @return this
     */
    public Matrix4x3f m31(float m31) {
        this.m31 = m31;
        if (m31 != 0.0f)
            properties &= ~PROPERTY_IDENTITY;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 2.
     * 
     * @param m32
     *          the new value
     * @return this
     */
    public Matrix4x3f m32(float m32) {
        this.m32 = m32;
        if (m32 != 0.0f)
            properties &= ~PROPERTY_IDENTITY;
        return this;
    }

    Matrix4x3f _properties(int properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0 without updating the properties of the matrix.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    Matrix4x3f _m00(float m00) {
        this.m00 = m00;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 1 without updating the properties of the matrix.
     * 
     * @param m01
     *          the new value
     * @return this
     */
    Matrix4x3f _m01(float m01) {
        this.m01 = m01;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 2 without updating the properties of the matrix.
     * 
     * @param m02
     *          the new value
     * @return this
     */
    Matrix4x3f _m02(float m02) {
        this.m02 = m02;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0 without updating the properties of the matrix.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    Matrix4x3f _m10(float m10) {
        this.m10 = m10;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 1 without updating the properties of the matrix.
     * 
     * @param m11
     *          the new value
     * @return this
     */
    Matrix4x3f _m11(float m11) {
        this.m11 = m11;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 2 without updating the properties of the matrix.
     * 
     * @param m12
     *          the new value
     * @return this
     */
    Matrix4x3f _m12(float m12) {
        this.m12 = m12;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0 without updating the properties of the matrix.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    Matrix4x3f _m20(float m20) {
        this.m20 = m20;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 1 without updating the properties of the matrix.
     * 
     * @param m21
     *          the new value
     * @return this
     */
    Matrix4x3f _m21(float m21) {
        this.m21 = m21;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 2 without updating the properties of the matrix.
     * 
     * @param m22
     *          the new value
     * @return this
     */
    Matrix4x3f _m22(float m22) {
        this.m22 = m22;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 0 without updating the properties of the matrix.
     * 
     * @param m30
     *          the new value
     * @return this
     */
    Matrix4x3f _m30(float m30) {
        this.m30 = m30;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 1 without updating the properties of the matrix.
     * 
     * @param m31
     *          the new value
     * @return this
     */
    Matrix4x3f _m31(float m31) {
        this.m31 = m31;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 2 without updating the properties of the matrix.
     * 
     * @param m32
     *          the new value
     * @return this
     */
    Matrix4x3f _m32(float m32) {
        this.m32 = m32;
        return this;
    }

    /**
     * Reset this matrix to the identity.
     * <p>
     * Please note that if a call to {@link #identity()} is immediately followed by a call to:
     * {@link #translate(float, float, float) translate}, 
     * {@link #rotate(float, float, float, float) rotate},
     * {@link #scale(float, float, float) scale},
     * {@link #ortho(float, float, float, float, float, float) ortho},
     * {@link #ortho2D(float, float, float, float) ortho2D},
     * {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt},
     * {@link #lookAlong(float, float, float, float, float, float) lookAlong},
     * or any of their overloads, then the call to {@link #identity()} can be omitted and the subsequent call replaced with:
     * {@link #translation(float, float, float) translation},
     * {@link #rotation(float, float, float, float) rotation},
     * {@link #scaling(float, float, float) scaling},
     * {@link #setOrtho(float, float, float, float, float, float) setOrtho},
     * {@link #setOrtho2D(float, float, float, float) setOrtho2D},
     * {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt},
     * {@link #setLookAlong(float, float, float, float, float, float) setLookAlong},
     * or any of their overloads.
     * 
     * @return this
     */
    public Matrix4x3f identity() {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return this;
        MemUtil.INSTANCE.identity(this);
        properties = PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Store the values of the given matrix <code>m</code> into <code>this</code> matrix.
     * 
     * @see #Matrix4x3f(Matrix4x3fc)
     * @see #get(Matrix4x3f)
     * 
     * @param m
     *          the matrix to copy the values from
     * @return this
     */
    public Matrix4x3f set(Matrix4x3fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m02 = m.m02();
        m10 = m.m10();
        m11 = m.m11();
        m12 = m.m12();
        m20 = m.m20();
        m21 = m.m21();
        m22 = m.m22();
        m30 = m.m30();
        m31 = m.m31();
        m32 = m.m32();
        properties = m.properties();
        return this;
    }

    /**
     * Store the values of the upper 4x3 submatrix of <code>m</code> into <code>this</code> matrix.
     * 
     * @see Matrix4fc#get4x3(Matrix4x3f)
     * 
     * @param m
     *          the matrix to copy the values from
     * @return this
     */
    public Matrix4x3f set(Matrix4fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m02 = m.m02();
        m10 = m.m10();
        m11 = m.m11();
        m12 = m.m12();
        m20 = m.m20();
        m21 = m.m21();
        m22 = m.m22();
        m30 = m.m30();
        m31 = m.m31();
        m32 = m.m32();
        properties = m.properties() & (PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
        return this;
    }

    public Matrix4f get(Matrix4f dest) {
        return dest.set4x3(this);
    }

    public Matrix4d get(Matrix4d dest) {
        return dest.set4x3(this);
    }

    /**
     * Set the left 3x3 submatrix of this {@link Matrix4x3f} to the given {@link Matrix3fc} 
     * and the rest to identity.
     * 
     * @see #Matrix4x3f(Matrix3fc)
     * 
     * @param mat
     *          the {@link Matrix3fc}
     * @return this
     */
    public Matrix4x3f set(Matrix3fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m02 = mat.m02();
        m10 = mat.m10();
        m11 = mat.m11();
        m12 = mat.m12();
        m20 = mat.m20();
        m21 = mat.m21();
        m22 = mat.m22();
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        return determineProperties();
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4f}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f}
     * @return this
     */
    public Matrix4x3f set(AxisAngle4f axisAngle) {
        float x = axisAngle.x;
        float y = axisAngle.y;
        float z = axisAngle.z;
        float angle = axisAngle.angle;
        float n = Math.sqrt(x*x + y*y + z*z);
        n = 1/n;
        x *= n;
        y *= n;
        z *= n;
        float s = Math.sin(angle);
        float c = Math.cosFromSin(s, angle);
        float omc = 1.0f - c;
        m00 = (float)(c + x*x*omc);
        m11 = (float)(c + y*y*omc);
        m22 = (float)(c + z*z*omc);
        float tmp1 = x*y*omc;
        float tmp2 = z*s;
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4d}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d}
     * @return this
     */
    public Matrix4x3f set(AxisAngle4d axisAngle) {
        double x = axisAngle.x;
        double y = axisAngle.y;
        double z = axisAngle.z;
        double angle = axisAngle.angle;
        double n = Math.sqrt(x*x + y*y + z*z);
        n = 1/n;
        x *= n;
        y *= n;
        z *= n;
        double s = Math.sin(angle);
        double c = Math.cosFromSin(s, angle);
        double omc = 1.0 - c;
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to be equivalent to the rotation - and possibly scaling - specified by the given {@link Quaternionfc}.
     * <p>
     * This method is equivalent to calling: <code>rotation(q)</code>
     * 
     * @see #rotation(Quaternionfc)
     * 
     * @param q
     *          the {@link Quaternionfc}
     * @return this
     */
    public Matrix4x3f set(Quaternionfc q) {
        return rotation(q);
    }

    /**
     * Set this matrix to be equivalent to the rotation - and possibly scaling - specified by the given {@link Quaterniondc}.
     * <p>
     * This method is equivalent to calling: <code>rotation(q)</code>
     * 
     * @param q
     *          the {@link Quaterniondc}
     * @return this
     */
    public Matrix4x3f set(Quaterniondc q) {
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
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set the four columns of this matrix to the supplied vectors, respectively.
     * 
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     * @param col2
     *          the third column
     * @param col3
     *          the fourth column
     * @return this
     */
    public Matrix4x3f set(Vector3fc col0, Vector3fc col1, Vector3fc col2, Vector3fc col3) {
        this.m00 = col0.x();
        this.m01 = col0.y();
        this.m02 = col0.z();
        this.m10 = col1.x();
        this.m11 = col1.y();
        this.m12 = col1.z();
        this.m20 = col2.x();
        this.m21 = col2.y();
        this.m22 = col2.z();
        this.m30 = col3.x();
        this.m31 = col3.y();
        this.m32 = col3.z();
        return determineProperties();
    }

    /**
     * Set the left 3x3 submatrix of this {@link Matrix4x3f} to that of the given {@link Matrix4x3fc} 
     * and don't change the other elements.
     * 
     * @param mat
     *          the {@link Matrix4x3fc}
     * @return this
     */
    public Matrix4x3f set3x3(Matrix4x3fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m02 = mat.m02();
        m10 = mat.m10();
        m11 = mat.m11();
        m12 = mat.m12();
        m20 = mat.m20();
        m21 = mat.m21();
        m22 = mat.m22();
        properties &= mat.properties();
        return this;
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix and store the result in <code>this</code>.
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
    public Matrix4x3f mul(Matrix4x3fc right) {
       return mul(right, this);
    }

    public Matrix4x3f mul(Matrix4x3fc right, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(right);
        else if ((right.properties() & PROPERTY_IDENTITY) != 0)
            return dest.set(this);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return mulTranslation(right, dest);
        return mulGeneric(right, dest);
    }
    private Matrix4x3f mulGeneric(Matrix4x3fc right, Matrix4x3f dest) {
        float m00 = this.m00, m01 = this.m01, m02 = this.m02;
        float m10 = this.m10, m11 = this.m11, m12 = this.m12;
        float m20 = this.m20, m21 = this.m21, m22 = this.m22;
        float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02();
        float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12();
        float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22();
        float rm30 = right.m30(), rm31 = right.m31(), rm32 = right.m32();
        return dest
        ._m00(Math.fma(m00, rm00, Math.fma(m10, rm01, m20 * rm02)))
        ._m01(Math.fma(m01, rm00, Math.fma(m11, rm01, m21 * rm02)))
        ._m02(Math.fma(m02, rm00, Math.fma(m12, rm01, m22 * rm02)))
        ._m10(Math.fma(m00, rm10, Math.fma(m10, rm11, m20 * rm12)))
        ._m11(Math.fma(m01, rm10, Math.fma(m11, rm11, m21 * rm12)))
        ._m12(Math.fma(m02, rm10, Math.fma(m12, rm11, m22 * rm12)))
        ._m20(Math.fma(m00, rm20, Math.fma(m10, rm21, m20 * rm22)))
        ._m21(Math.fma(m01, rm20, Math.fma(m11, rm21, m21 * rm22)))
        ._m22(Math.fma(m02, rm20, Math.fma(m12, rm21, m22 * rm22)))
        ._m30(Math.fma(m00, rm30, Math.fma(m10, rm31, Math.fma(m20, rm32, m30))))
        ._m31(Math.fma(m01, rm30, Math.fma(m11, rm31, Math.fma(m21, rm32, m31))))
        ._m32(Math.fma(m02, rm30, Math.fma(m12, rm31, Math.fma(m22, rm32, m32))))
        ._properties(properties & right.properties() & PROPERTY_ORTHONORMAL);
    }

    public Matrix4x3f mulTranslation(Matrix4x3fc right, Matrix4x3f dest) {
        return dest
        ._m00(right.m00())
        ._m01(right.m01())
        ._m02(right.m02())
        ._m10(right.m10())
        ._m11(right.m11())
        ._m12(right.m12())
        ._m20(right.m20())
        ._m21(right.m21())
        ._m22(right.m22())
        ._m30(right.m30() + m30)
        ._m31(right.m31() + m31)
        ._m32(right.m32() + m32)
        ._properties(right.properties() & PROPERTY_ORTHONORMAL);
    }

    /**
     * Multiply <code>this</code> orthographic projection matrix by the supplied <code>view</code> matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>V</code> the <code>view</code> matrix,
     * then the new matrix will be <code>M * V</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * V * v</code>, the
     * transformation of the <code>view</code> matrix will be applied first!
     *
     * @param view
     *          the matrix which to multiply <code>this</code> with
     * @return this
     */
    public Matrix4x3f mulOrtho(Matrix4x3fc view) {
        return mulOrtho(view, this);
    }

    public Matrix4x3f mulOrtho(Matrix4x3fc view, Matrix4x3f dest) {
        float nm00 = m00 * view.m00();
        float nm01 = m11 * view.m01();
        float nm02 = m22 * view.m02();
        float nm10 = m00 * view.m10();
        float nm11 = m11 * view.m11();
        float nm12 = m22 * view.m12();
        float nm20 = m00 * view.m20();
        float nm21 = m11 * view.m21();
        float nm22 = m22 * view.m22();
        float nm30 = m00 * view.m30() + m30;
        float nm31 = m11 * view.m31() + m31;
        float nm32 = m22 * view.m32() + m32;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = (this.properties & view.properties() & PROPERTY_ORTHONORMAL);
        return dest;
    }

    /**
     * Component-wise add <code>this</code> and <code>other</code>
     * by first multiplying each component of <code>other</code> by <code>otherFactor</code> and
     * adding that result to <code>this</code>.
     * <p>
     * The matrix <code>other</code> will not be changed.
     * 
     * @param other
     *          the other matrix 
     * @param otherFactor
     *          the factor to multiply each of the other matrix's components
     * @return this
     */
    public Matrix4x3f fma(Matrix4x3fc other, float otherFactor) {
        return fma(other, otherFactor, this);
    }

    public Matrix4x3f fma(Matrix4x3fc other, float otherFactor, Matrix4x3f dest) {
        dest
        ._m00(Math.fma(other.m00(), otherFactor, m00))
        ._m01(Math.fma(other.m01(), otherFactor, m01))
        ._m02(Math.fma(other.m02(), otherFactor, m02))
        ._m10(Math.fma(other.m10(), otherFactor, m10))
        ._m11(Math.fma(other.m11(), otherFactor, m11))
        ._m12(Math.fma(other.m12(), otherFactor, m12))
        ._m20(Math.fma(other.m20(), otherFactor, m20))
        ._m21(Math.fma(other.m21(), otherFactor, m21))
        ._m22(Math.fma(other.m22(), otherFactor, m22))
        ._m30(Math.fma(other.m30(), otherFactor, m30))
        ._m31(Math.fma(other.m31(), otherFactor, m31))
        ._m32(Math.fma(other.m32(), otherFactor, m32))
        ._properties(0);
        return dest;
    }

    /**
     * Component-wise add <code>this</code> and <code>other</code>.
     * 
     * @param other
     *          the other addend 
     * @return this
     */
    public Matrix4x3f add(Matrix4x3fc other) {
        return add(other, this);
    }

    public Matrix4x3f add(Matrix4x3fc other, Matrix4x3f dest) {
        dest.m00 = m00 + other.m00();
        dest.m01 = m01 + other.m01();
        dest.m02 = m02 + other.m02();
        dest.m10 = m10 + other.m10();
        dest.m11 = m11 + other.m11();
        dest.m12 = m12 + other.m12();
        dest.m20 = m20 + other.m20();
        dest.m21 = m21 + other.m21();
        dest.m22 = m22 + other.m22();
        dest.m30 = m30 + other.m30();
        dest.m31 = m31 + other.m31();
        dest.m32 = m32 + other.m32();
        dest.properties = 0;
        return dest;
    }

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code>.
     * 
     * @param subtrahend
     *          the subtrahend
     * @return this
     */
    public Matrix4x3f sub(Matrix4x3fc subtrahend) {
        return sub(subtrahend, this);
    }

    public Matrix4x3f sub(Matrix4x3fc subtrahend, Matrix4x3f dest) {
        dest.m00 = m00 - subtrahend.m00();
        dest.m01 = m01 - subtrahend.m01();
        dest.m02 = m02 - subtrahend.m02();
        dest.m10 = m10 - subtrahend.m10();
        dest.m11 = m11 - subtrahend.m11();
        dest.m12 = m12 - subtrahend.m12();
        dest.m20 = m20 - subtrahend.m20();
        dest.m21 = m21 - subtrahend.m21();
        dest.m22 = m22 - subtrahend.m22();
        dest.m30 = m30 - subtrahend.m30();
        dest.m31 = m31 - subtrahend.m31();
        dest.m32 = m32 - subtrahend.m32();
        dest.properties = 0;
        return dest;
    }

    /**
     * Component-wise multiply <code>this</code> by <code>other</code>.
     * 
     * @param other
     *          the other matrix
     * @return this
     */
    public Matrix4x3f mulComponentWise(Matrix4x3fc other) {
        return mulComponentWise(other, this);
    }

    public Matrix4x3f mulComponentWise(Matrix4x3fc other, Matrix4x3f dest) {
        dest.m00 = m00 * other.m00();
        dest.m01 = m01 * other.m01();
        dest.m02 = m02 * other.m02();
        dest.m10 = m10 * other.m10();
        dest.m11 = m11 * other.m11();
        dest.m12 = m12 * other.m12();
        dest.m20 = m20 * other.m20();
        dest.m21 = m21 * other.m21();
        dest.m22 = m22 * other.m22();
        dest.m30 = m30 * other.m30();
        dest.m31 = m31 * other.m31();
        dest.m32 = m32 * other.m32();
        dest.properties = 0;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied float values. The matrix will look like this:<br><br>
     *
     *  m00, m10, m20, m30<br>
     *  m01, m11, m21, m31<br>
     *  m02, m12, m22, m32<br>
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
     * @param m30
     *          the new value of m30
     * @param m31
     *          the new value of m31
     * @param m32
     *          the new value of m32
     * @return this
     */
    public Matrix4x3f set(float m00, float m01, float m02,
                          float m10, float m11, float m12,
                          float m20, float m21, float m22, 
                          float m30, float m31, float m32) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        return determineProperties();
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in column-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 3, 6, 9<br>
     * 1, 4, 7, 10<br>
     * 2, 5, 8, 11<br>
     * 
     * @see #set(float[])
     * 
     * @param m
     *          the array to read the matrix values from
     * @param off
     *          the offset into the array
     * @return this
     */
    public Matrix4x3f set(float m[], int off) {
        MemUtil.INSTANCE.copy(m, off, this);
        return determineProperties();
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in column-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 3, 6, 9<br>
     * 1, 4, 7, 10<br>
     * 2, 5, 8, 11<br>
     * 
     * @see #set(float[], int)
     * 
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix4x3f set(float m[]) {
        return set(m, 0);
    }


    /**
     * Set the values of this matrix by reading 12 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix4x3f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 12 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix4x3f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 12 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix4x3f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 12 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix4x3f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return determineProperties();
    }


    public float determinant() {
        return (m00 * m11 - m01 * m10) * m22
             + (m02 * m10 - m00 * m12) * m21
             + (m01 * m12 - m02 * m11) * m20;
    }

    public Matrix4x3f invert(Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.identity();
        else if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return invertOrthonormal(dest);
        return invertGeneric(dest);
    }
    private Matrix4x3f invertGeneric(Matrix4x3f dest) {
        float m11m00 = m00 * m11, m10m01 = m01 * m10, m10m02 = m02 * m10;
        float m12m00 = m00 * m12, m12m01 = m01 * m12, m11m02 = m02 * m11;
        float s = 1.0f / ((m11m00 - m10m01) * m22 + (m10m02 - m12m00) * m21 + (m12m01 - m11m02) * m20);
        float m10m22 = m10 * m22, m10m21 = m10 * m21, m11m22 = m11 * m22;
        float m11m20 = m11 * m20, m12m21 = m12 * m21, m12m20 = m12 * m20;
        float m20m02 = m20 * m02, m20m01 = m20 * m01, m21m02 = m21 * m02;
        float m21m00 = m21 * m00, m22m01 = m22 * m01, m22m00 = m22 * m00;
        float nm00 = (m11m22 - m12m21) * s;
        float nm01 = (m21m02 - m22m01) * s;
        float nm02 = (m12m01 - m11m02) * s;
        float nm10 = (m12m20 - m10m22) * s;
        float nm11 = (m22m00 - m20m02) * s;
        float nm12 = (m10m02 - m12m00) * s;
        float nm20 = (m10m21 - m11m20) * s;
        float nm21 = (m20m01 - m21m00) * s;
        float nm22 = (m11m00 - m10m01) * s;
        float nm30 = (m10m22 * m31 - m10m21 * m32 + m11m20 * m32 - m11m22 * m30 + m12m21 * m30 - m12m20 * m31) * s;
        float nm31 = (m20m02 * m31 - m20m01 * m32 + m21m00 * m32 - m21m02 * m30 + m22m01 * m30 - m22m00 * m31) * s;
        float nm32 = (m11m02 * m30 - m12m01 * m30 + m12m00 * m31 - m10m02 * m31 + m10m01 * m32 - m11m00 * m32) * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = 0;
        return dest;
    }
    private Matrix4x3f invertOrthonormal(Matrix4x3f dest) {
        float nm30 = -(m00 * m30 + m01 * m31 + m02 * m32);
        float nm31 = -(m10 * m30 + m11 * m31 + m12 * m32);
        float nm32 = -(m20 * m30 + m21 * m31 + m22 * m32);
        float m01 = this.m01;
        float m02 = this.m02;
        float m12 = this.m12;
        dest.m00 = m00;
        dest.m01 = m10;
        dest.m02 = m20;
        dest.m10 = m01;
        dest.m11 = m11;
        dest.m12 = m21;
        dest.m20 = m02;
        dest.m21 = m12;
        dest.m22 = m22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = PROPERTY_ORTHONORMAL;
        return dest;
    }

    public Matrix4f invert(Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.identity();
        else if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return invertOrthonormal(dest);
        return invertGeneric(dest);
    }
    private Matrix4f invertGeneric(Matrix4f dest) {
        float m11m00 = m00 * m11, m10m01 = m01 * m10, m10m02 = m02 * m10;
        float m12m00 = m00 * m12, m12m01 = m01 * m12, m11m02 = m02 * m11;
        float s = 1.0f / ((m11m00 - m10m01) * m22 + (m10m02 - m12m00) * m21 + (m12m01 - m11m02) * m20);
        float m10m22 = m10 * m22, m10m21 = m10 * m21, m11m22 = m11 * m22;
        float m11m20 = m11 * m20, m12m21 = m12 * m21, m12m20 = m12 * m20;
        float m20m02 = m20 * m02, m20m01 = m20 * m01, m21m02 = m21 * m02;
        float m21m00 = m21 * m00, m22m01 = m22 * m01, m22m00 = m22 * m00;
        float nm00 = (m11m22 - m12m21) * s;
        float nm01 = (m21m02 - m22m01) * s;
        float nm02 = (m12m01 - m11m02) * s;
        float nm10 = (m12m20 - m10m22) * s;
        float nm11 = (m22m00 - m20m02) * s;
        float nm12 = (m10m02 - m12m00) * s;
        float nm20 = (m10m21 - m11m20) * s;
        float nm21 = (m20m01 - m21m00) * s;
        float nm22 = (m11m00 - m10m01) * s;
        float nm30 = (m10m22 * m31 - m10m21 * m32 + m11m20 * m32 - m11m22 * m30 + m12m21 * m30 - m12m20 * m31) * s;
        float nm31 = (m20m02 * m31 - m20m01 * m32 + m21m00 * m32 - m21m02 * m30 + m22m01 * m30 - m22m00 * m31) * s;
        float nm32 = (m11m02 * m30 - m12m01 * m30 + m12m00 * m31 - m10m02 * m31 + m10m01 * m32 - m11m00 * m32) * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m03 = 0.0f;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m13 = 0.0f;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m23 = 0.0f;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.m33 = 0.0f;
        dest.properties = 0;
        return dest;
    }
    private Matrix4f invertOrthonormal(Matrix4f dest) {
        float nm30 = -(m00 * m30 + m01 * m31 + m02 * m32);
        float nm31 = -(m10 * m30 + m11 * m31 + m12 * m32);
        float nm32 = -(m20 * m30 + m21 * m31 + m22 * m32);
        float m01 = this.m01;
        float m02 = this.m02;
        float m12 = this.m12;
        dest.m00 = m00;
        dest.m01 = m10;
        dest.m02 = m20;
        dest.m03 = 0.0f;
        dest.m10 = m01;
        dest.m11 = m11;
        dest.m12 = m21;
        dest.m13 = 0.0f;
        dest.m20 = m02;
        dest.m21 = m12;
        dest.m22 = m22;
        dest.m23 = 0.0f;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.m33 = 0.0f;
        dest.properties = PROPERTY_ORTHONORMAL;
        return dest;
    }

    /**
     * Invert this matrix.
     * 
     * @return this
     */
    public Matrix4x3f invert() {
        return invert(this);
    }

    public Matrix4x3f invertOrtho(Matrix4x3f dest) {
        float invM00 = 1.0f / m00;
        float invM11 = 1.0f / m11;
        float invM22 = 1.0f / m22;
        dest.set(invM00, 0, 0,
                 0, invM11, 0,
                 0, 0, invM22,
                 -m30 * invM00, -m31 * invM11, -m32 * invM22);
        dest.properties = 0;
        return dest;
    }

    /**
     * Invert <code>this</code> orthographic projection matrix.
     * <p>
     * This method can be used to quickly obtain the inverse of an orthographic projection matrix.
     * 
     * @return this
     */
    public Matrix4x3f invertOrtho() {
        return invertOrtho(this);
    }

    /**
     * Transpose only the left 3x3 submatrix of this matrix and set the rest of the matrix elements to identity.
     * 
     * @return this
     */
    public Matrix4x3f transpose3x3() {
        return transpose3x3(this);
    }

    public Matrix4x3f transpose3x3(Matrix4x3f dest) {
        float nm00 = m00;
        float nm01 = m10;
        float nm02 = m20;
        float nm10 = m01;
        float nm11 = m11;
        float nm12 = m21;
        float nm20 = m02;
        float nm21 = m12;
        float nm22 = m22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.properties = properties;
        return dest;
    }

    public Matrix3f transpose3x3(Matrix3f dest) {
        dest.m00(m00);
        dest.m01(m10);
        dest.m02(m20);
        dest.m10(m01);
        dest.m11(m11);
        dest.m12(m21);
        dest.m20(m02);
        dest.m21(m12);
        dest.m22(m22);
        return dest;
    }

    /**
     * Set this matrix to be a simple translation matrix.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional translation.
     * <p>
     * In order to post-multiply a translation transformation directly to a
     * matrix, use {@link #translate(float, float, float) translate()} instead.
     * 
     * @see #translate(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @return this
     */
    public Matrix4x3f translation(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        m30 = x;
        m31 = y;
        m32 = z;
        properties = PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to be a simple translation matrix.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional translation.
     * <p>
     * In order to post-multiply a translation transformation directly to a
     * matrix, use {@link #translate(Vector3fc) translate()} instead.
     * 
     * @see #translate(float, float, float)
     * 
     * @param offset
     *              the offsets in x, y and z to translate
     * @return this
     */
    public Matrix4x3f translation(Vector3fc offset) {
        return translation(offset.x(), offset.y(), offset.z());
    }

    /**
     * Set only the translation components <code>(m30, m31, m32)</code> of this matrix to the given values <code>(x, y, z)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(float, float, float)}.
     * To apply a translation, use {@link #translate(float, float, float)}.
     * 
     * @see #translation(float, float, float)
     * @see #translate(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @return this
     */
    public Matrix4x3f setTranslation(float x, float y, float z) {
        m30 = x;
        m31 = y;
        m32 = z;
        properties &= ~(PROPERTY_IDENTITY);
        return this;
    }

    /**
     * Set only the translation components <code>(m30, m31, m32)</code> of this matrix to the values <code>(xyz.x, xyz.y, xyz.z)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(Vector3fc)}.
     * To apply a translation, use {@link #translate(Vector3fc)}.
     * 
     * @see #translation(Vector3fc)
     * @see #translate(Vector3fc)
     * 
     * @param xyz
     *          the units to translate in <code>(x, y, z)</code>
     * @return this
     */
    public Matrix4x3f setTranslation(Vector3fc xyz) {
        return setTranslation(xyz.x(), xyz.y(), xyz.z());
    }

    public Vector3f getTranslation(Vector3f dest) {
        dest.x = m30;
        dest.y = m31;
        dest.z = m32;
        return dest;
    }

    public Vector3f getScale(Vector3f dest) {
        dest.x = Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02);
        dest.y = Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12);
        dest.z = Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22);
        return dest;
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
        return Runtime.format(m00, formatter) + " " + Runtime.format(m10, formatter) + " " + Runtime.format(m20, formatter) + " " + Runtime.format(m30, formatter) + "\n"
             + Runtime.format(m01, formatter) + " " + Runtime.format(m11, formatter) + " " + Runtime.format(m21, formatter) + " " + Runtime.format(m31, formatter) + "\n"
             + Runtime.format(m02, formatter) + " " + Runtime.format(m12, formatter) + " " + Runtime.format(m22, formatter) + " " + Runtime.format(m32, formatter) + "\n";
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix4x3fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix4x3fc)
     * 
     * @param dest
     *            the destination matrix
     * @return the passed in destination
     */
    public Matrix4x3f get(Matrix4x3f dest) {
        return dest.set(this);
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link Matrix4x3d#set(Matrix4x3fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see Matrix4x3d#set(Matrix4x3fc)
     * 
     * @param dest
     *            the destination matrix
     * @return the passed in destination
     */
    public Matrix4x3d get(Matrix4x3d dest) {
        return dest.set(this);
    }

    public AxisAngle4f getRotation(AxisAngle4f dest) {
        return dest.set(this);
    }

    public AxisAngle4d getRotation(AxisAngle4d dest) {
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


    public float[] get(float[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    public float[] get(float[] arr) {
        return get(arr, 0);
    }

    public float[] get4x4(float[] arr, int offset) {
        MemUtil.INSTANCE.copy4x4(this, arr, offset);
        return arr;
    }

    public float[] get4x4(float[] arr) {
        return get4x4(arr, 0);
    }


    public FloatBuffer get4x4(FloatBuffer buffer) {
        return get4x4(buffer.position(), buffer);
    }

    public FloatBuffer get4x4(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get4x4(ByteBuffer buffer) {
        return get4x4(buffer.position(), buffer);
    }

    public ByteBuffer get4x4(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, index, buffer);
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


    public float[] getTransposed(float[] arr, int offset) {
        arr[offset+0]  = m00;
        arr[offset+1]  = m10;
        arr[offset+2]  = m20;
        arr[offset+3]  = m30;
        arr[offset+4]  = m01;
        arr[offset+5]  = m11;
        arr[offset+6]  = m21;
        arr[offset+7]  = m31;
        arr[offset+8]  = m02;
        arr[offset+9]  = m12;
        arr[offset+10] = m22;
        arr[offset+11] = m32;
        return arr;
    }

    public float[] getTransposed(float[] arr) {
        return getTransposed(arr, 0);
    }

    /**
     * Set all the values within this matrix to <code>0</code>.
     * 
     * @return this
     */
    public Matrix4x3f zero() {
        MemUtil.INSTANCE.zero(this);
        properties = 0;
        return this;
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
    public Matrix4x3f scaling(float factor) {
        return scaling(factor, factor, factor);
    }

    /**
     * Set this matrix to be a simple scale matrix.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a
     * matrix, use {@link #scale(float, float, float) scale()} instead.
     * 
     * @see #scale(float, float, float)
     * 
     * @param x
     *             the scale in x
     * @param y
     *             the scale in y
     * @param z
     *             the scale in z
     * @return this
     */
    public Matrix4x3f scaling(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        m00 = x;
        m11 = y;
        m22 = z;
        boolean one = Math.absEqualsOne(x) && Math.absEqualsOne(y) && Math.absEqualsOne(z);
        properties = one ? PROPERTY_ORTHONORMAL : 0;
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
    public Matrix4x3f scaling(Vector3fc xyz) {
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
    public Matrix4x3f rotation(float angle, Vector3fc axis) {
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
    public Matrix4x3f rotation(AxisAngle4f axisAngle) {
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
    public Matrix4x3f rotation(float angle, float x, float y, float z) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotationX(x * angle);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotationY(y * angle);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotationZ(z * angle);
        return rotationInternal(angle, x, y, z);
    }
    private Matrix4x3f rotationInternal(float angle, float x, float y, float z) {
        float sin = Math.sin(angle);
        float cos = Math.cosFromSin(sin, angle);
        float C = 1.0f - cos;
        float xy = x * y, xz = x * z, yz = y * z;
        m00 = cos + x * x * C;
        m01 = xy * C + z * sin;
        m02 = xz * C - y * sin;
        m10 = xy * C - z * sin;
        m11 = cos + y * y * C;
        m12 = yz * C + x * sin;
        m20 = xz * C + y * sin;
        m21 = yz * C - x * sin;
        m22 = cos + z * z * C;
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationX(float ang) {
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationY(float ang) {
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationZ(float ang) {
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationXYZ(float angleX, float angleY, float angleZ) {
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
        // set last column to identity
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationZYX(float angleZ, float angleY, float angleX) {
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
        // set last column to identity
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
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
    public Matrix4x3f rotationYXZ(float angleY, float angleX, float angleZ) {
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
        // set last column to identity
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set only the left 3x3 submatrix of this matrix to a rotation of <code>angleX</code> radians about the X axis, followed by a rotation
     * of <code>angleY</code> radians about the Y axis and followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * 
     * @param angleX
     *            the angle to rotate about X
     * @param angleY
     *            the angle to rotate about Y
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix4x3f setRotationXYZ(float angleX, float angleY, float angleZ) {
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
        properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }

    /**
     * Set only the left 3x3 submatrix of this matrix to a rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation
     * of <code>angleY</code> radians about the Y axis and followed by a rotation of <code>angleX</code> radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * 
     * @param angleZ
     *            the angle to rotate about Z
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @return this
     */
    public Matrix4x3f setRotationZYX(float angleZ, float angleY, float angleX) {
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
        properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }

    /**
     * Set only the left 3x3 submatrix of this matrix to a rotation of <code>angleY</code> radians about the Y axis, followed by a rotation
     * of <code>angleX</code> radians about the X axis and followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * 
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix4x3f setRotationYXZ(float angleY, float angleX, float angleZ) {
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
        properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotation(Quaternionfc quat) {
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
        _m00(w2 + x2 - z2 - y2);
        _m01(dxy + dzw);
        _m02(dxz - dyw);
        _m10(dxy - dzw);
        _m11(y2 - z2 + w2 - x2);
        _m12(dyz + dxw);
        _m20(dyw + dxz);
        _m21(dyz - dxw);
        _m22(z2 - y2 - x2 + w2);
        _m30(0.0f);
        _m31(0.0f);
        _m32(0.0f);
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>, and <code>S</code> is a scaling transformation
     * which scales the three axes x, y and z by <code>(sx, sy, sz)</code>.
     * <p>
     * When transforming a vector by the resulting matrix the scaling transformation will be applied first, then the rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).scale(sx, sy, sz)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #scale(float, float, float)
     * 
     * @param tx
     *          the number of units by which to translate the x-component
     * @param ty
     *          the number of units by which to translate the y-component
     * @param tz
     *          the number of units by which to translate the z-component
     * @param qx
     *          the x-coordinate of the vector part of the quaternion
     * @param qy
     *          the y-coordinate of the vector part of the quaternion
     * @param qz
     *          the z-coordinate of the vector part of the quaternion
     * @param qw
     *          the scalar part of the quaternion
     * @param sx
     *          the scaling factor for the x-axis
     * @param sy
     *          the scaling factor for the y-axis
     * @param sz
     *          the scaling factor for the z-axis
     * @return this
     */
    public Matrix4x3f translationRotateScale(float tx, float ty, float tz, 
                                             float qx, float qy, float qz, float qw, 
                                             float sx, float sy, float sz) {
        float dqx = qx + qx;
        float dqy = qy + qy;
        float dqz = qz + qz;
        float q00 = dqx * qx;
        float q11 = dqy * qy;
        float q22 = dqz * qz;
        float q01 = dqx * qy;
        float q02 = dqx * qz;
        float q03 = dqx * qw;
        float q12 = dqy * qz;
        float q13 = dqy * qw;
        float q23 = dqz * qw;
        m00 = sx - (q11 + q22) * sx;
        m01 = (q01 + q23) * sx;
        m02 = (q02 - q13) * sx;
        m10 = (q01 - q23) * sy;
        m11 = sy - (q22 + q00) * sy;
        m12 = (q12 + q03) * sy;
        m20 = (q02 + q13) * sz;
        m21 = (q12 - q03) * sz;
        m22 = sz - (q11 + q00) * sz;
        m30 = tx;
        m31 = ty;
        m32 = tz;
        properties = 0;
        return this;
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S</code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation transformation specified by the given quaternion, and <code>S</code> is a scaling transformation
     * which scales the axes by <code>scale</code>.
     * <p>
     * When transforming a vector by the resulting matrix the scaling transformation will be applied first, then the rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(translation).rotate(quat).scale(scale)</code>
     * 
     * @see #translation(Vector3fc)
     * @see #rotate(Quaternionfc)
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @return this
     */
    public Matrix4x3f translationRotateScale(Vector3fc translation, 
                                           Quaternionfc quat, 
                                           Vector3fc scale) {
        return translationRotateScale(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale.x(), scale.y(), scale.z());
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S * M</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>, <code>S</code> is a scaling transformation
     * which scales the three axes x, y and z by <code>(sx, sy, sz)</code>.
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).scale(sx, sy, sz).mul(m)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #scale(float, float, float)
     * @see #mul(Matrix4x3fc)
     * 
     * @param tx
     *          the number of units by which to translate the x-component
     * @param ty
     *          the number of units by which to translate the y-component
     * @param tz
     *          the number of units by which to translate the z-component
     * @param qx
     *          the x-coordinate of the vector part of the quaternion
     * @param qy
     *          the y-coordinate of the vector part of the quaternion
     * @param qz
     *          the z-coordinate of the vector part of the quaternion
     * @param qw
     *          the scalar part of the quaternion
     * @param sx
     *          the scaling factor for the x-axis
     * @param sy
     *          the scaling factor for the y-axis
     * @param sz
     *          the scaling factor for the z-axis
     * @param m
     *          the matrix to multiply by
     * @return this
     */
    public Matrix4x3f translationRotateScaleMul(float tx, float ty, float tz, 
                                                float qx, float qy, float qz, float qw, 
                                                float sx, float sy, float sz,
                                                Matrix4x3f m) {
        float dqx = qx + qx;
        float dqy = qy + qy;
        float dqz = qz + qz;
        float q00 = dqx * qx;
        float q11 = dqy * qy;
        float q22 = dqz * qz;
        float q01 = dqx * qy;
        float q02 = dqx * qz;
        float q03 = dqx * qw;
        float q12 = dqy * qz;
        float q13 = dqy * qw;
        float q23 = dqz * qw;
        float nm00 = sx - (q11 + q22) * sx;
        float nm01 = (q01 + q23) * sx;
        float nm02 = (q02 - q13) * sx;
        float nm10 = (q01 - q23) * sy;
        float nm11 = sy - (q22 + q00) * sy;
        float nm12 = (q12 + q03) * sy;
        float nm20 = (q02 + q13) * sz;
        float nm21 = (q12 - q03) * sz;
        float nm22 = sz - (q11 + q00) * sz;
        float m00 = nm00 * m.m00 + nm10 * m.m01 + nm20 * m.m02;
        float m01 = nm01 * m.m00 + nm11 * m.m01 + nm21 * m.m02;
        m02 = nm02 * m.m00 + nm12 * m.m01 + nm22 * m.m02;
        this.m00 = m00;
        this.m01 = m01;
        float m10 = nm00 * m.m10 + nm10 * m.m11 + nm20 * m.m12;
        float m11 = nm01 * m.m10 + nm11 * m.m11 + nm21 * m.m12;
        m12 = nm02 * m.m10 + nm12 * m.m11 + nm22 * m.m12;
        this.m10 = m10;
        this.m11 = m11;
        float m20 = nm00 * m.m20 + nm10 * m.m21 + nm20 * m.m22;
        float m21 = nm01 * m.m20 + nm11 * m.m21 + nm21 * m.m22;
        m22 = nm02 * m.m20 + nm12 * m.m21 + nm22 * m.m22;
        this.m20 = m20;
        this.m21 = m21;
        float m30 = nm00 * m.m30 + nm10 * m.m31 + nm20 * m.m32 + tx;
        float m31 = nm01 * m.m30 + nm11 * m.m31 + nm21 * m.m32 + ty;
        m32 = nm02 * m.m30 + nm12 * m.m31 + nm22 * m.m32 + tz;
        this.m30 = m30;
        this.m31 = m31;
        properties = 0;
        return this;
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S * M</code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation transformation specified by the given quaternion, <code>S</code> is a scaling transformation
     * which scales the axes by <code>scale</code>.
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(translation).rotate(quat).scale(scale).mul(m)</code>
     * 
     * @see #translation(Vector3fc)
     * @see #rotate(Quaternionfc)
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @param m
     *          the matrix to multiply by
     * @return this
     */
    public Matrix4x3f translationRotateScaleMul(Vector3fc translation, Quaternionfc quat, Vector3fc scale, Matrix4x3f m) {
        return translationRotateScaleMul(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale.x(), scale.y(), scale.z(), m);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code> and
     * <code>R</code> is a rotation transformation specified by the given quaternion.
     * <p>
     * When transforming a vector by the resulting matrix the rotation transformation will be applied first and then the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * 
     * @param tx
     *          the number of units by which to translate the x-component
     * @param ty
     *          the number of units by which to translate the y-component
     * @param tz
     *          the number of units by which to translate the z-component
     * @param quat
     *          the quaternion representing a rotation
     * @return this
     */
    public Matrix4x3f translationRotate(float tx, float ty, float tz, Quaternionfc quat) {
        float dqx = quat.x() + quat.x();
        float dqy = quat.y() + quat.y();
        float dqz = quat.z() + quat.z();
        float q00 = dqx * quat.x();
        float q11 = dqy * quat.y();
        float q22 = dqz * quat.z();
        float q01 = dqx * quat.y();
        float q02 = dqx * quat.z();
        float q03 = dqx * quat.w();
        float q12 = dqy * quat.z();
        float q13 = dqy * quat.w();
        float q23 = dqz * quat.w();
        m00 = 1.0f - (q11 + q22);
        m01 = q01 + q23;
        m02 = q02 - q13;
        m10 = q01 - q23;
        m11 = 1.0f - (q22 + q00);
        m12 = q12 + q03;
        m20 = q02 + q13;
        m21 = q12 - q03;
        m22 = 1.0f - (q11 + q00);
        m30 = tx;
        m31 = ty;
        m32 = tz;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * M</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the given quaternion and <code>M</code> is the given matrix <code>mat</code>.
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).mul(mat)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #mul(Matrix4x3fc)
     * 
     * @param tx
     *          the number of units by which to translate the x-component
     * @param ty
     *          the number of units by which to translate the y-component
     * @param tz
     *          the number of units by which to translate the z-component
     * @param quat
     *          the quaternion representing a rotation
     * @param mat
     *          the matrix to multiply with
     * @return this
     */
    public Matrix4x3f translationRotateMul(float tx, float ty, float tz, Quaternionfc quat, Matrix4x3fc mat) {
        return translationRotateMul(tx, ty, tz, quat.x(), quat.y(), quat.z(), quat.w(), mat);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * M</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the quaternion <code>(qx, qy, qz, qw)</code> and <code>M</code> is the given matrix <code>mat</code>
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).mul(mat)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #mul(Matrix4x3fc)
     * 
     * @param tx
     *          the number of units by which to translate the x-component
     * @param ty
     *          the number of units by which to translate the y-component
     * @param tz
     *          the number of units by which to translate the z-component
     * @param qx
     *          the x-coordinate of the vector part of the quaternion
     * @param qy
     *          the y-coordinate of the vector part of the quaternion
     * @param qz
     *          the z-coordinate of the vector part of the quaternion
     * @param qw
     *          the scalar part of the quaternion
     * @param mat
     *          the matrix to multiply with
     * @return this
     */
    public Matrix4x3f translationRotateMul(float tx, float ty, float tz, float qx, float qy, float qz, float qw, Matrix4x3fc mat) {
        float w2 = qw * qw;
        float x2 = qx * qx;
        float y2 = qy * qy;
        float z2 = qz * qz;
        float zw = qz * qw;
        float xy = qx * qy;
        float xz = qx * qz;
        float yw = qy * qw;
        float yz = qy * qz;
        float xw = qx * qw;
        float nm00 = w2 + x2 - z2 - y2;
        float nm01 = xy + zw + zw + xy;
        float nm02 = xz - yw + xz - yw;
        float nm10 = -zw + xy - zw + xy;
        float nm11 = y2 - z2 + w2 - x2;
        float nm12 = yz + yz + xw + xw;
        float nm20 = yw + xz + xz + yw;
        float nm21 = yz + yz - xw - xw;
        float nm22 = z2 - y2 - x2 + w2;
        m00 = nm00 * mat.m00() + nm10 * mat.m01() + nm20 * mat.m02();
        m01 = nm01 * mat.m00() + nm11 * mat.m01() + nm21 * mat.m02();
        m02 = nm02 * mat.m00() + nm12 * mat.m01() + nm22 * mat.m02();
        m10 = nm00 * mat.m10() + nm10 * mat.m11() + nm20 * mat.m12();
        m11 = nm01 * mat.m10() + nm11 * mat.m11() + nm21 * mat.m12();
        m12 = nm02 * mat.m10() + nm12 * mat.m11() + nm22 * mat.m12();
        m20 = nm00 * mat.m20() + nm10 * mat.m21() + nm20 * mat.m22();
        m21 = nm01 * mat.m20() + nm11 * mat.m21() + nm21 * mat.m22();
        m22 = nm02 * mat.m20() + nm12 * mat.m21() + nm22 * mat.m22();
        m30 = nm00 * mat.m30() + nm10 * mat.m31() + nm20 * mat.m32() + tx;
        m31 = nm01 * mat.m30() + nm11 * mat.m31() + nm21 * mat.m32() + ty;
        m32 = nm02 * mat.m30() + nm12 * mat.m31() + nm22 * mat.m32() + tz;
        this.properties = 0;
        return this;
    }

    /**
     * Set the left 3x3 submatrix of this {@link Matrix4x3f} to the given {@link Matrix3fc} and don't change the other elements.
     * 
     * @param mat
     *          the 3x3 matrix
     * @return this
     */
    public Matrix4x3f set3x3(Matrix3fc mat) {
        if (mat instanceof Matrix3f) {
            MemUtil.INSTANCE.copy3x3((Matrix3f) mat, this);
        } else {
            set3x3Matrix3fc(mat);
        }
        properties = 0;
        return this;
    }
    private void set3x3Matrix3fc(Matrix3fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m02 = mat.m02();
        m10 = mat.m10();
        m11 = mat.m11();
        m12 = mat.m12();
        m20 = mat.m20();
        m21 = mat.m21();
        m22 = mat.m22();
    }

    public Vector4f transform(Vector4f v) {
        return v.mul(this);
    }

    public Vector4f transform(Vector4fc v, Vector4f dest) {
        return v.mul(this, dest);
    }

    public Vector3f transformPosition(Vector3f v) {
        v.set(m00 * v.x + m10 * v.y + m20 * v.z + m30,
              m01 * v.x + m11 * v.y + m21 * v.z + m31,
              m02 * v.x + m12 * v.y + m22 * v.z + m32);
        return v;
    }

    public Vector3f transformPosition(Vector3fc v, Vector3f dest) {
        dest.set(m00 * v.x() + m10 * v.y() + m20 * v.z() + m30,
                 m01 * v.x() + m11 * v.y() + m21 * v.z() + m31,
                 m02 * v.x() + m12 * v.y() + m22 * v.z() + m32);
        return dest;
    }

    public Vector3f transformDirection(Vector3f v) {
        v.set(m00 * v.x + m10 * v.y + m20 * v.z,
              m01 * v.x + m11 * v.y + m21 * v.z,
              m02 * v.x + m12 * v.y + m22 * v.z);
        return v;
    }

    public Vector3f transformDirection(Vector3fc v, Vector3f dest) {
        dest.set(m00 * v.x() + m10 * v.y() + m20 * v.z(),
                 m01 * v.x() + m11 * v.y() + m21 * v.z(),
                 m02 * v.x() + m12 * v.y() + m22 * v.z());
        return dest;
    }

    public Matrix4x3f scale(Vector3fc xyz, Matrix4x3f dest) {
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
    public Matrix4x3f scale(Vector3fc xyz) {
        return scale(xyz.x(), xyz.y(), xyz.z(), this);
    }

    public Matrix4x3f scale(float xyz, Matrix4x3f dest) {
        return scale(xyz, xyz, xyz, dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xyz</code> factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * Individual scaling of all three axes can be applied using {@link #scale(float, float, float)}. 
     * 
     * @see #scale(float, float, float)
     * 
     * @param xyz
     *            the factor for all components
     * @return this
     */
    public Matrix4x3f scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Matrix4x3f scaleXY(float x, float y, Matrix4x3f dest) {
        return scale(x, y, 1.0f, dest);
    }

    /**
     * Apply scaling to this matrix by scaling the X axis by <code>x</code> and the Y axis by <code>y</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @return this
     */
    public Matrix4x3f scaleXY(float x, float y) {
        return scale(x, y, 1.0f);
    }

    public Matrix4x3f scale(float x, float y, float z, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.scaling(x, y, z);
        return scaleGeneric(x, y, z, dest);
    }
    private Matrix4x3f scaleGeneric(float x, float y, float z, Matrix4x3f dest) {
        dest.m00 = m00 * x;
        dest.m01 = m01 * x;
        dest.m02 = m02 * x;
        dest.m10 = m10 * y;
        dest.m11 = m11 * y;
        dest.m12 = m12 * y;
        dest.m20 = m20 * z;
        dest.m21 = m21 * z;
        dest.m22 = m22 * z;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
        return dest;
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x,
     * y and z factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @param z
     *            the factor of the z component
     * @return this
     */
    public Matrix4x3f scale(float x, float y, float z) {
        return scale(x, y, z, this);
    }

    public Matrix4x3f scaleLocal(float x, float y, float z, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.scaling(x, y, z);
        float nm00 = x * m00;
        float nm01 = y * m01;
        float nm02 = z * m02;
        float nm10 = x * m10;
        float nm11 = y * m11;
        float nm12 = z * m12;
        float nm20 = x * m20;
        float nm21 = y * m21;
        float nm22 = z * m22;
        float nm30 = x * m30;
        float nm31 = y * m31;
        float nm32 = z * m32;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
        return dest;
    }

    public Matrix4x3f scaleAround(float sx, float sy, float sz, float ox, float oy, float oz, Matrix4x3f dest) {
        float nm30 = m00 * ox + m10 * oy + m20 * oz + m30;
        float nm31 = m01 * ox + m11 * oy + m21 * oz + m31;
        float nm32 = m02 * ox + m12 * oy + m22 * oz + m32;
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        return dest
        ._m00(m00 * sx)
        ._m01(m01 * sx)
        ._m02(m02 * sx)
        ._m10(m10 * sy)
        ._m11(m11 * sy)
        ._m12(m12 * sy)
        ._m20(m20 * sz)
        ._m21(m21 * sz)
        ._m22(m22 * sz)
        ._m30(-dest.m00 * ox - dest.m10 * oy - dest.m20 * oz + nm30)
        ._m31(-dest.m01 * ox - dest.m11 * oy - dest.m21 * oz + nm31)
        ._m32(-dest.m02 * ox - dest.m12 * oy - dest.m22 * oz + nm32)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | (one ? 0 : PROPERTY_ORTHONORMAL)));
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given sx,
     * sy and sz factors while using <code>(ox, oy, oz)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy, oz).scale(sx, sy, sz).translate(-ox, -oy, -oz)</code>
     * 
     * @param sx
     *            the scaling factor of the x component
     * @param sy
     *            the scaling factor of the y component
     * @param sz
     *            the scaling factor of the z component
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @param oz
     *            the z coordinate of the scaling origin
     * @return this
     */
    public Matrix4x3f scaleAround(float sx, float sy, float sz, float ox, float oy, float oz) {
        return scaleAround(sx, sy, sz, ox, oy, oz, this);
    }

    /**
     * Apply scaling to this matrix by scaling all three base axes by the given <code>factor</code>
     * while using <code>(ox, oy, oz)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy, oz).scale(factor).translate(-ox, -oy, -oz)</code>
     * 
     * @param factor
     *            the scaling factor for all three axes
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @param oz
     *            the z coordinate of the scaling origin
     * @return this
     */
    public Matrix4x3f scaleAround(float factor, float ox, float oy, float oz) {
        return scaleAround(factor, factor, factor, ox, oy, oz, this);
    }

    public Matrix4x3f scaleAround(float factor, float ox, float oy, float oz, Matrix4x3f dest) {
        return scaleAround(factor, factor, factor, ox, oy, oz, dest);
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
    public Matrix4x3f scaleLocal(float x, float y, float z) {
        return scaleLocal(x, y, z, this);
    }

    public Matrix4x3f rotateX(float ang, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationX(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationX(ang).setTranslation(x, y, z);
        }
        return rotateXInternal(ang, dest);
    }
    private Matrix4x3f rotateXInternal(float ang, Matrix4x3f dest) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm11 = cos;
        float rm12 = sin;
        float rm21 = -sin;
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix4x3f rotateX(float ang) {
        return rotateX(ang, this);
    }

    public Matrix4x3f rotateY(float ang, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationY(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationY(ang).setTranslation(x, y, z);
        }
        return rotateYInternal(ang, dest);
    }
    private Matrix4x3f rotateYInternal(float ang, Matrix4x3f dest) {
        float cos, sin;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm00 = cos;
        float rm02 = -sin;
        float rm20 = sin;
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix4x3f rotateY(float ang) {
        return rotateY(ang, this);
    }

    public Matrix4x3f rotateZ(float ang, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationZ(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationZ(ang).setTranslation(x, y, z);
        }
        return rotateZInternal(ang, dest);
    }
    private Matrix4x3f rotateZInternal(float ang, Matrix4x3f dest) {
        float sin, cos;
        sin = Math.sin(ang);
        cos = Math.cosFromSin(sin, ang);
        float rm00 = cos;
        float rm01 = sin;
        float rm10 = -sin;
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix4x3f rotateZ(float ang) {
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
    public Matrix4x3f rotateXYZ(Vector3f angles) {
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
    public Matrix4x3f rotateXYZ(float angleX, float angleY, float angleZ) {
        return rotateXYZ(angleX, angleY, angleZ, this);
    }

    public Matrix4x3f rotateXYZ(float angleX, float angleY, float angleZ, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationXYZ(angleX, angleY, angleZ);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationXYZ(angleX, angleY, angleZ).setTranslation(tx, ty, tz);
        }
        return rotateXYZInternal(angleX, angleY, angleZ, dest);
    }
    private Matrix4x3f rotateXYZInternal(float angleX, float angleY, float angleZ, Matrix4x3f dest) {
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
        // copy last column from 'this'
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotateZYX(Vector3f angles) {
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
    public Matrix4x3f rotateZYX(float angleZ, float angleY, float angleX) {
        return rotateZYX(angleZ, angleY, angleX, this);
    }

    public Matrix4x3f rotateZYX(float angleZ, float angleY, float angleX, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationZYX(angleZ, angleY, angleX);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationZYX(angleZ, angleY, angleX).setTranslation(tx, ty, tz);
        }
        return rotateZYXInternal(angleZ, angleY, angleX, dest);
    }
    private Matrix4x3f rotateZYXInternal(float angleZ, float angleY, float angleX, Matrix4x3f dest) {
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
        // copy last column from 'this'
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotateYXZ(Vector3f angles) {
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
    public Matrix4x3f rotateYXZ(float angleY, float angleX, float angleZ) {
        return rotateYXZ(angleY, angleX, angleZ, this);
    }

    public Matrix4x3f rotateYXZ(float angleY, float angleX, float angleZ, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationYXZ(angleY, angleX, angleZ);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationYXZ(angleY, angleX, angleZ).setTranslation(tx, ty, tz);
        }
        return rotateYXZInternal(angleY, angleX, angleZ, dest);
    }
    private Matrix4x3f rotateYXZInternal(float angleY, float angleX, float angleZ, Matrix4x3f dest) {
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
        // copy last column from 'this'
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis and store the result in <code>dest</code>.
     * <p>
     * The axis described by the three components needs to be a unit vector.
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
     * In order to set the matrix to a rotation matrix without post-multiplying the rotation
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
    public Matrix4x3f rotate(float ang, float x, float y, float z, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotation(ang, x, y, z);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return rotateTranslation(ang, x, y, z, dest);
        return rotateGeneric(ang, x, y, z, dest);
    }
    private Matrix4x3f rotateGeneric(float ang, float x, float y, float z, Matrix4x3f dest) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateX(x * ang, dest);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateY(y * ang, dest);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateZ(z * ang, dest);
        return rotateGenericInternal(ang, x, y, z, dest);
    }
    private Matrix4x3f rotateGenericInternal(float ang, float x, float y, float z, Matrix4x3f dest) {
        float s = Math.sin(ang);
        float c = Math.cosFromSin(s, ang);
        float C = 1.0f - c;
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis.
     * <p>
     * The axis described by the three components needs to be a unit vector.
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
     * In order to set the matrix to a rotation matrix without post-multiplying the rotation
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
    public Matrix4x3f rotate(float ang, float x, float y, float z) {
        return rotate(ang, x, y, z, this);
    }

    /**
     * Apply rotation to this matrix, which is assumed to only contain a translation, by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis and store the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to only contain a translation.
     * <p>
     * The axis described by the three components needs to be a unit vector.
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
     * In order to set the matrix to a rotation matrix without post-multiplying the rotation
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
    public Matrix4x3f rotateTranslation(float ang, float x, float y, float z, Matrix4x3f dest) {
        float tx = m30, ty = m31, tz = m32;
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return dest.rotationX(x * ang).setTranslation(tx, ty, tz);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return dest.rotationY(y * ang).setTranslation(tx, ty, tz);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return dest.rotationZ(z * ang).setTranslation(tx, ty, tz);
        return rotateTranslationInternal(ang, x, y, z, dest);
    }
    private Matrix4x3f rotateTranslationInternal(float ang, float x, float y, float z, Matrix4x3f dest) {
        float s = Math.sin(ang);
        float c = Math.cosFromSin(s, ang);
        float C = 1.0f - c;
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
        float nm00 = rm00;
        float nm01 = rm01;
        float nm02 = rm02;
        float nm10 = rm10;
        float nm11 = rm11;
        float nm12 = rm12;
        // set non-dependent values directly
        dest.m20 = rm20;
        dest.m21 = rm21;
        dest.m22 = rm22;
        // set other values
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);

        return dest;
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this matrix while using <code>(ox, oy, oz)</code> as the rotation origin.
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
     * This method is equivalent to calling: <code>translate(ox, oy, oz).rotate(quat).translate(-ox, -oy, -oz)</code>
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param ox
     *          the x coordinate of the rotation origin
     * @param oy
     *          the y coordinate of the rotation origin
     * @param oz
     *          the z coordinate of the rotation origin
     * @return this
     */
    public Matrix4x3f rotateAround(Quaternionfc quat, float ox, float oy, float oz) {
        return rotateAround(quat, ox, oy, oz, this);
    }

    private Matrix4x3f rotateAroundAffine(Quaternionfc quat, float ox, float oy, float oz, Matrix4x3f dest) {
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
        float tm30 = m00 * ox + m10 * oy + m20 * oz + m30;
        float tm31 = m01 * ox + m11 * oy + m21 * oz + m31;
        float tm32 = m02 * ox + m12 * oy + m22 * oz + m32;
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m30(-nm00 * ox - nm10 * oy - m20 * oz + tm30)
        ._m31(-nm01 * ox - nm11 * oy - m21 * oz + tm31)
        ._m32(-nm02 * ox - nm12 * oy - m22 * oz + tm32)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    public Matrix4x3f rotateAround(Quaternionfc quat, float ox, float oy, float oz, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return rotationAround(quat, ox, oy, oz);
        return rotateAroundAffine(quat, ox, oy, oz, dest);
    }

    /**
     * Set this matrix to a transformation composed of a rotation of the specified {@link Quaternionfc} while using <code>(ox, oy, oz)</code> as the rotation origin.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(ox, oy, oz).rotate(quat).translate(-ox, -oy, -oz)</code>
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param ox
     *          the x coordinate of the rotation origin
     * @param oy
     *          the y coordinate of the rotation origin
     * @param oz
     *          the z coordinate of the rotation origin
     * @return this
     */
    public Matrix4x3f rotationAround(Quaternionfc quat, float ox, float oy, float oz) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        this._m20(dyw + dxz);
        this._m21(dyz - dxw);
        this._m22(z2 - y2 - x2 + w2);
        this._m00(w2 + x2 - z2 - y2);
        this._m01(dxy + dzw);
        this._m02(dxz - dyw);
        this._m10(dxy - dzw);
        this._m11(y2 - z2 + w2 - x2);
        this._m12(dyz + dxw);
        this._m30(-m00 * ox - m10 * oy - m20 * oz + ox);
        this._m31(-m01 * ox - m11 * oy - m21 * oz + oy);
        this._m32(-m02 * ox - m12 * oy - m22 * oz + oz);
        this.properties = PROPERTY_ORTHONORMAL;
        return this;
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
    public Matrix4x3f rotateLocal(float ang, float x, float y, float z, Matrix4x3f dest) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateLocalX(x * ang, dest);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateLocalY(y * ang, dest);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateLocalZ(z * ang, dest);
        return rotateLocalInternal(ang, x, y, z, dest);
    }
    private Matrix4x3f rotateLocalInternal(float ang, float x, float y, float z, Matrix4x3f dest) {
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
        float nm30 = lm00 * m30 + lm10 * m31 + lm20 * m32;
        float nm31 = lm01 * m30 + lm11 * m31 + lm21 * m32;
        float nm32 = lm02 * m30 + lm12 * m31 + lm22 * m32;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotateLocal(float ang, float x, float y, float z) {
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
    public Matrix4x3f rotateLocalX(float ang, Matrix4x3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm01 = cos * m01 - sin * m02;
        float nm02 = sin * m01 + cos * m02;
        float nm11 = cos * m11 - sin * m12;
        float nm12 = sin * m11 + cos * m12;
        float nm21 = cos * m21 - sin * m22;
        float nm22 = sin * m21 + cos * m22;
        float nm31 = cos * m31 - sin * m32;
        float nm32 = sin * m31 + cos * m32;
        dest
        ._m00(m00)
        ._m01(nm01)
        ._m02(nm02)
        ._m10(m10)
        ._m11(nm11)
        ._m12(nm12)
        ._m20(m20)
        ._m21(nm21)
        ._m22(nm22)
        ._m30(m30)
        ._m31(nm31)
        ._m32(nm32)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4x3f rotateLocalX(float ang) {
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
    public Matrix4x3f rotateLocalY(float ang, Matrix4x3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm00 =  cos * m00 + sin * m02;
        float nm02 = -sin * m00 + cos * m02;
        float nm10 =  cos * m10 + sin * m12;
        float nm12 = -sin * m10 + cos * m12;
        float nm20 =  cos * m20 + sin * m22;
        float nm22 = -sin * m20 + cos * m22;
        float nm30 =  cos * m30 + sin * m32;
        float nm32 = -sin * m30 + cos * m32;
        dest
        ._m00(nm00)
        ._m01(m01)
        ._m02(nm02)
        ._m10(nm10)
        ._m11(m11)
        ._m12(nm12)
        ._m20(nm20)
        ._m21(m21)
        ._m22(nm22)
        ._m30(nm30)
        ._m31(m31)
        ._m32(nm32)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4x3f rotateLocalY(float ang) {
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
    public Matrix4x3f rotateLocalZ(float ang, Matrix4x3f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm00 = cos * m00 - sin * m01;
        float nm01 = sin * m00 + cos * m01;
        float nm10 = cos * m10 - sin * m11;
        float nm11 = sin * m10 + cos * m11;
        float nm20 = cos * m20 - sin * m21;
        float nm21 = sin * m20 + cos * m21;
        float nm30 = cos * m30 - sin * m31;
        float nm31 = sin * m30 + cos * m31;
        dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(m02)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(m12)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(m22)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(m32)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4x3f rotateLocalZ(float ang) {
        return rotateLocalZ(ang, this);
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(Vector3fc)}.
     * 
     * @see #translation(Vector3fc)
     * 
     * @param offset
     *          the number of units in x, y and z by which to translate
     * @return this
     */
    public Matrix4x3f translate(Vector3fc offset) {
        return translate(offset.x(), offset.y(), offset.z());
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(Vector3fc)}.
     * 
     * @see #translation(Vector3fc)
     * 
     * @param offset
     *          the number of units in x, y and z by which to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f translate(Vector3fc offset, Matrix4x3f dest) {
        return translate(offset.x(), offset.y(), offset.z(), dest);
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(float, float, float)}.
     * 
     * @see #translation(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f translate(float x, float y, float z, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.translation(x, y, z);
        return translateGeneric(x, y, z, dest);
    }
    private Matrix4x3f translateGeneric(float x, float y, float z, Matrix4x3f dest) {
        MemUtil.INSTANCE.copy(this, dest);
        dest.m30 = m00 * x + m10 * y + m20 * z + m30;
        dest.m31 = m01 * x + m11 * y + m21 * z + m31;
        dest.m32 = m02 * x + m12 * y + m22 * z + m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY);
        return dest;
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(float, float, float)}.
     * 
     * @see #translation(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @return this
     */
    public Matrix4x3f translate(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return translation(x, y, z);
        Matrix4x3f c = this;
        c.m30 = c.m00 * x + c.m10 * y + c.m20 * z + c.m30;
        c.m31 = c.m01 * x + c.m11 * y + c.m21 * z + c.m31;
        c.m32 = c.m02 * x + c.m12 * y + c.m22 * z + c.m32;
        c.properties &= ~(PROPERTY_IDENTITY);
        return this;
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x, y and z.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(Vector3fc)}.
     * 
     * @see #translation(Vector3fc)
     * 
     * @param offset
     *          the number of units in x, y and z by which to translate
     * @return this
     */
    public Matrix4x3f translateLocal(Vector3fc offset) {
        return translateLocal(offset.x(), offset.y(), offset.z());
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x, y and z and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(Vector3fc)}.
     * 
     * @see #translation(Vector3fc)
     * 
     * @param offset
     *          the number of units in x, y and z by which to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f translateLocal(Vector3fc offset, Matrix4x3f dest) {
        return translateLocal(offset.x(), offset.y(), offset.z(), dest);
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x, y and z and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(float, float, float)}.
     * 
     * @see #translation(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f translateLocal(float x, float y, float z, Matrix4x3f dest) {
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m30 = m30 + x;
        dest.m31 = m31 + y;
        dest.m32 = m32 + z;
        dest.properties = properties & ~(PROPERTY_IDENTITY);
        return dest;
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x, y and z.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(float, float, float)}.
     * 
     * @see #translation(float, float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param z
     *          the offset to translate in z
     * @return this
     */
    public Matrix4x3f translateLocal(float x, float y, float z) {
        return translateLocal(x, y, z, this);
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
        out.writeFloat(m30);
        out.writeFloat(m31);
        out.writeFloat(m32);
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
        m30 = in.readFloat();
        m31 = in.readFloat();
        m32 = in.readFloat();
        determineProperties();
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho(float, float, float, float, float, float, boolean) setOrtho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrtho(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f ortho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        float rm30 = (left + right) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = m20 * rm22;
        dest.m21 = m21 * rm22;
        dest.m22 = m22 * rm22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho(float, float, float, float, float, float) setOrtho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrtho(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f ortho(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4x3f dest) {
        return ortho(left, right, bottom, top, zNear, zFar, false, dest);
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho(float, float, float, float, float, float, boolean) setOrtho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrtho(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f ortho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        return ortho(left, right, bottom, top, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho(float, float, float, float, float, float) setOrtho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrtho(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f ortho(float left, float right, float bottom, float top, float zNear, float zFar) {
        return ortho(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordiante system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrthoLH(float, float, float, float, float, float, boolean) setOrthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        float rm30 = (left + right) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = m20 * rm22;
        dest.m21 = m21 * rm22;
        dest.m22 = m22 * rm22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordiante system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrthoLH(float, float, float, float, float, float) setOrthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4x3f dest) {
        return orthoLH(left, right, bottom, top, zNear, zFar, false, dest);
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordiante system
     * using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrthoLH(float, float, float, float, float, float, boolean) setOrthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        return orthoLH(left, right, bottom, top, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordiante system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrthoLH(float, float, float, float, float, float) setOrthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar) {
        return orthoLH(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #ortho(float, float, float, float, float, float, boolean) ortho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #ortho(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / (right - left);
        m11 = 2.0f / (top - bottom);
        m22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        m30 = (right + left) / (left - right);
        m31 = (top + bottom) / (bottom - top);
        m32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        properties = 0;
        return this;
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #ortho(float, float, float, float, float, float) ortho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #ortho(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setOrtho(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a left-handed coordinate system
     * using the given NDC z range.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #orthoLH(float, float, float, float, float, float, boolean) orthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f setOrthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / (right - left);
        m11 = 2.0f / (top - bottom);
        m22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        m30 = (right + left) / (left - right);
        m31 = (top + bottom) / (bottom - top);
        m32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        properties = 0;
        return this;
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #orthoLH(float, float, float, float, float, float) orthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f setOrthoLH(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setOrthoLH(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, boolean, Matrix4x3f) ortho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetric(float, float, float, float, boolean) setOrthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetric(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return dest
     */
    public Matrix4x3f orthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / width;
        float rm11 = 2.0f / height;
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m20 * rm32 + m30;
        dest.m31 = m21 * rm32 + m31;
        dest.m32 = m22 * rm32 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = m20 * rm22;
        dest.m21 = m21 * rm22;
        dest.m22 = m22 * rm22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, Matrix4x3f) ortho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetric(float, float, float, float) setOrthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetric(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f orthoSymmetric(float width, float height, float zNear, float zFar, Matrix4x3f dest) {
        return orthoSymmetric(width, height, zNear, zFar, false, dest);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, boolean) ortho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetric(float, float, float, float, boolean) setOrthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetric(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f orthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        return orthoSymmetric(width, height, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float) ortho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetric(float, float, float, float) setOrthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetric(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f orthoSymmetric(float width, float height, float zNear, float zFar) {
        return orthoSymmetric(width, height, zNear, zFar, false, this);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, boolean, Matrix4x3f) orthoLH()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetricLH(float, float, float, float, boolean) setOrthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetricLH(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return dest
     */
    public Matrix4x3f orthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / width;
        float rm11 = 2.0f / height;
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m20 * rm32 + m30;
        dest.m31 = m21 * rm32 + m31;
        dest.m32 = m22 * rm32 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = m20 * rm22;
        dest.m21 = m21 * rm22;
        dest.m22 = m22 * rm22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, Matrix4x3f) orthoLH()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetricLH(float, float, float, float) setOrthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetricLH(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f orthoSymmetricLH(float width, float height, float zNear, float zFar, Matrix4x3f dest) {
        return orthoSymmetricLH(width, height, zNear, zFar, false, dest);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, boolean) orthoLH()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetricLH(float, float, float, float, boolean) setOrthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetricLH(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f orthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        return orthoSymmetricLH(width, height, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float) orthoLH()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to a symmetric orthographic projection without post-multiplying it,
     * use {@link #setOrthoSymmetricLH(float, float, float, float) setOrthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoSymmetricLH(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f orthoSymmetricLH(float width, float height, float zNear, float zFar) {
        return orthoSymmetricLH(width, height, zNear, zFar, false, this);
    }

    /**
     * Set this matrix to be a symmetric orthographic projection transformation for a right-handed coordinate system using the given NDC z range.
     * <p>
     * This method is equivalent to calling {@link #setOrtho(float, float, float, float, float, float, boolean) setOrtho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * In order to apply the symmetric orthographic projection to an already existing transformation,
     * use {@link #orthoSymmetric(float, float, float, float, boolean) orthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoSymmetric(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f setOrthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / width;
        m11 = 2.0f / height;
        m22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        m32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        properties = 0;
        return this;
    }

    /**
     * Set this matrix to be a symmetric orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * This method is equivalent to calling {@link #setOrtho(float, float, float, float, float, float) setOrtho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * In order to apply the symmetric orthographic projection to an already existing transformation,
     * use {@link #orthoSymmetric(float, float, float, float) orthoSymmetric()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoSymmetric(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f setOrthoSymmetric(float width, float height, float zNear, float zFar) {
        return setOrthoSymmetric(width, height, zNear, zFar, false);
    }

    /**
     * Set this matrix to be a symmetric orthographic projection transformation for a left-handed coordinate system using the given NDC z range.
     * <p>
     * This method is equivalent to calling {@link #setOrtho(float, float, float, float, float, float, boolean) setOrtho()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * In order to apply the symmetric orthographic projection to an already existing transformation,
     * use {@link #orthoSymmetricLH(float, float, float, float, boolean) orthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoSymmetricLH(float, float, float, float, boolean)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4x3f setOrthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / width;
        m11 = 2.0f / height;
        m22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        m32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        properties = 0;
        return this;
    }

    /**
     * Set this matrix to be a symmetric orthographic projection transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * This method is equivalent to calling {@link #setOrthoLH(float, float, float, float, float, float) setOrthoLH()} with
     * <code>left=-width/2</code>, <code>right=+width/2</code>, <code>bottom=-height/2</code> and <code>top=+height/2</code>.
     * <p>
     * In order to apply the symmetric orthographic projection to an already existing transformation,
     * use {@link #orthoSymmetricLH(float, float, float, float) orthoSymmetricLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoSymmetricLH(float, float, float, float)
     * 
     * @param width
     *            the distance between the right and left frustum edges
     * @param height
     *            the distance between the top and bottom frustum edges
     * @param zNear
     *            near clipping plane distance
     * @param zFar
     *            far clipping plane distance
     * @return this
     */
    public Matrix4x3f setOrthoSymmetricLH(float width, float height, float zNear, float zFar) {
        return setOrthoSymmetricLH(width, height, zNear, zFar, false);
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system to this matrix
     * and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, Matrix4x3f) ortho()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho2D(float, float, float, float) setOrtho()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #ortho(float, float, float, float, float, float, Matrix4x3f)
     * @see #setOrtho2D(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f ortho2D(float left, float right, float bottom, float top, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm30 = -(right + left) / (right - left);
        float rm31 = -(top + bottom) / (top - bottom);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = -m20;
        dest.m21 = -m21;
        dest.m22 = -m22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system to this matrix.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float) ortho()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho2D(float, float, float, float) setOrtho2D()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #ortho(float, float, float, float, float, float)
     * @see #setOrtho2D(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @return this
     */
    public Matrix4x3f ortho2D(float left, float right, float bottom, float top) {
        return ortho2D(left, right, bottom, top, this);
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordinate system to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, Matrix4x3f) orthoLH()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho2DLH(float, float, float, float) setOrthoLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoLH(float, float, float, float, float, float, Matrix4x3f)
     * @see #setOrtho2DLH(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f ortho2DLH(float left, float right, float bottom, float top, Matrix4x3f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm30 = -(right + left) / (right - left);
        float rm31 = -(top + bottom) / (top - bottom);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m32;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m02 = m02 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        dest.m12 = m12 * rm11;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordinate system to this matrix.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float) orthoLH()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * <p>
     * In order to set the matrix to an orthographic projection without post-multiplying it,
     * use {@link #setOrtho2DLH(float, float, float, float) setOrtho2DLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #orthoLH(float, float, float, float, float, float)
     * @see #setOrtho2DLH(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @return this
     */
    public Matrix4x3f ortho2DLH(float left, float right, float bottom, float top) {
        return ortho2DLH(left, right, bottom, top, this);
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a right-handed coordinate system.
     * <p>
     * This method is equivalent to calling {@link #setOrtho(float, float, float, float, float, float) setOrtho()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #ortho2D(float, float, float, float) ortho2D()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrtho(float, float, float, float, float, float)
     * @see #ortho2D(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @return this
     */
    public Matrix4x3f setOrtho2D(float left, float right, float bottom, float top) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / (right - left);
        m11 = 2.0f / (top - bottom);
        m22 = -1.0f;
        m30 = -(right + left) / (right - left);
        m31 = -(top + bottom) / (top - bottom);
        properties = 0;
        return this;
    }

    /**
     * Set this matrix to be an orthographic projection transformation for a left-handed coordinate system.
     * <p>
     * This method is equivalent to calling {@link #setOrtho(float, float, float, float, float, float) setOrthoLH()} with
     * <code>zNear=-1</code> and <code>zFar=+1</code>.
     * <p>
     * In order to apply the orthographic projection to an already existing transformation,
     * use {@link #ortho2DLH(float, float, float, float) ortho2DLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho">http://www.songho.ca</a>
     * 
     * @see #setOrthoLH(float, float, float, float, float, float)
     * @see #ortho2DLH(float, float, float, float)
     * 
     * @param left
     *            the distance from the center to the left frustum edge
     * @param right
     *            the distance from the center to the right frustum edge
     * @param bottom
     *            the distance from the center to the bottom frustum edge
     * @param top
     *            the distance from the center to the top frustum edge
     * @return this
     */
    public Matrix4x3f setOrtho2DLH(float left, float right, float bottom, float top) {
        MemUtil.INSTANCE.identity(this);
        m00 = 2.0f / (right - left);
        m11 = 2.0f / (top - bottom);
        m22 = 1.0f;
        m30 = -(right + left) / (right - left);
        m31 = -(top + bottom) / (top - bottom);
        properties = 0;
        return this;
    }

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * <p>
     * This is equivalent to calling
     * {@link #lookAt(Vector3fc, Vector3fc, Vector3fc) lookAt}
     * with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(Vector3fc, Vector3fc) setLookAlong()}.
     * 
     * @see #lookAlong(float, float, float, float, float, float)
     * @see #lookAt(Vector3fc, Vector3fc, Vector3fc)
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix4x3f lookAlong(Vector3fc dir, Vector3fc up) {
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
     * This is equivalent to calling
     * {@link #lookAt(Vector3fc, Vector3fc, Vector3fc) lookAt}
     * with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(Vector3fc, Vector3fc) setLookAlong()}.
     * 
     * @see #lookAlong(float, float, float, float, float, float)
     * @see #lookAt(Vector3fc, Vector3fc, Vector3fc)
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
    public Matrix4x3f lookAlong(Vector3fc dir, Vector3fc up, Matrix4x3f dest) {
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
     * This is equivalent to calling
     * {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()}
     * with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(float, float, float, float, float, float) setLookAlong()}
     * 
     * @see #lookAt(float, float, float, float, float, float, float, float, float)
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
    public Matrix4x3f lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return setLookAlong(dirX, dirY, dirZ, upX, upY, upZ);

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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);

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
     * This is equivalent to calling
     * {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()}
     * with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
     * <p>
     * In order to set the matrix to a lookalong transformation without post-multiplying it,
     * use {@link #setLookAlong(float, float, float, float, float, float) setLookAlong()}
     * 
     * @see #lookAt(float, float, float, float, float, float, float, float, float)
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
    public Matrix4x3f lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        return lookAlong(dirX, dirY, dirZ, upX, upY, upZ, this);
    }

    /**
     * Set this matrix to a rotation transformation to make <code>-z</code>
     * point along <code>dir</code>.
     * <p>
     * This is equivalent to calling
     * {@link #setLookAt(Vector3fc, Vector3fc, Vector3fc) setLookAt()} 
     * with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
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
    public Matrix4x3f setLookAlong(Vector3fc dir, Vector3fc up) {
        return setLookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a rotation transformation to make <code>-z</code>
     * point along <code>dir</code>.
     * <p>
     * This is equivalent to calling
     * {@link #setLookAt(float, float, float, float, float, float, float, float, float)
     * setLookAt()} with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
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
    public Matrix4x3f setLookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;

        return this;
    }

    /**
     * Set this matrix to be a "lookat" transformation for a right-handed coordinate system, that aligns
     * <code>-z</code> with <code>center - eye</code>.
     * <p>
     * In order to not make use of vectors to specify <code>eye</code>, <code>center</code> and <code>up</code> but use primitives,
     * like in the GLU function, use {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt()}
     * instead.
     * <p>
     * In order to apply the lookat transformation to a previous existing transformation,
     * use {@link #lookAt(Vector3fc, Vector3fc, Vector3fc) lookAt()}.
     * 
     * @see #setLookAt(float, float, float, float, float, float, float, float, float)
     * @see #lookAt(Vector3fc, Vector3fc, Vector3fc)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix4x3f setLookAt(Vector3fc eye, Vector3fc center, Vector3fc up) {
        return setLookAt(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to be a "lookat" transformation for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code>.
     * <p>
     * In order to apply the lookat transformation to a previous existing transformation,
     * use {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt}.
     * 
     * @see #setLookAt(Vector3fc, Vector3fc, Vector3fc)
     * @see #lookAt(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix4x3f setLookAt(float eyeX, float eyeY, float eyeZ,
                                float centerX, float centerY, float centerZ,
                                float upX, float upY, float upZ) {
        // Compute direction from position to lookAt
        float dirX, dirY, dirZ;
        dirX = eyeX - centerX;
        dirY = eyeY - centerY;
        dirZ = eyeZ - centerZ;
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLength;
        dirY *= invDirLength;
        dirZ *= invDirLength;
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
        m30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        m31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        m32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        properties = PROPERTY_ORTHONORMAL;

        return this;
    }

    /**
     * Apply a "lookat" transformation to this matrix for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAt(Vector3fc, Vector3fc, Vector3fc)}.
     * 
     * @see #lookAt(float, float, float, float, float, float, float, float, float)
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f lookAt(Vector3fc eye, Vector3fc center, Vector3fc up, Matrix4x3f dest) {
        return lookAt(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a "lookat" transformation to this matrix for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAt(Vector3fc, Vector3fc, Vector3fc)}.
     * 
     * @see #lookAt(float, float, float, float, float, float, float, float, float)
     * @see #setLookAlong(Vector3fc, Vector3fc)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix4x3f lookAt(Vector3fc eye, Vector3fc center, Vector3fc up) {
        return lookAt(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z(), this);
    }

    /**
     * Apply a "lookat" transformation to this matrix for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt()}.
     * 
     * @see #lookAt(Vector3fc, Vector3fc, Vector3fc)
     * @see #setLookAt(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f lookAt(float eyeX, float eyeY, float eyeZ,
                             float centerX, float centerY, float centerZ,
                             float upX, float upY, float upZ, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        return lookAtGeneric(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
    }
    private Matrix4x3f lookAtGeneric(float eyeX, float eyeY, float eyeZ,
                                     float centerX, float centerY, float centerZ,
                                     float upX, float upY, float upZ, Matrix4x3f dest) {
        // Compute direction from position to lookAt
        float dirX, dirY, dirZ;
        dirX = eyeX - centerX;
        dirY = eyeY - centerY;
        dirZ = eyeZ - centerZ;
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLength;
        dirY *= invDirLength;
        dirZ *= invDirLength;
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);

        // perform optimized matrix multiplication
        // compute last column first, because others do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
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
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    /**
     * Apply a "lookat" transformation to this matrix for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt()}.
     * 
     * @see #lookAt(Vector3fc, Vector3fc, Vector3fc)
     * @see #setLookAt(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix4x3f lookAt(float eyeX, float eyeY, float eyeZ,
                             float centerX, float centerY, float centerZ,
                             float upX, float upY, float upZ) {
        return lookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, this);
    }

    /**
     * Set this matrix to be a "lookat" transformation for a left-handed coordinate system, that aligns
     * <code>+z</code> with <code>center - eye</code>.
     * <p>
     * In order to not make use of vectors to specify <code>eye</code>, <code>center</code> and <code>up</code> but use primitives,
     * like in the GLU function, use {@link #setLookAtLH(float, float, float, float, float, float, float, float, float) setLookAtLH()}
     * instead.
     * <p>
     * In order to apply the lookat transformation to a previous existing transformation,
     * use {@link #lookAtLH(Vector3fc, Vector3fc, Vector3fc) lookAt()}.
     * 
     * @see #setLookAtLH(float, float, float, float, float, float, float, float, float)
     * @see #lookAtLH(Vector3fc, Vector3fc, Vector3fc)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix4x3f setLookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up) {
        return setLookAtLH(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to be a "lookat" transformation for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code>.
     * <p>
     * In order to apply the lookat transformation to a previous existing transformation,
     * use {@link #lookAtLH(float, float, float, float, float, float, float, float, float) lookAtLH}.
     * 
     * @see #setLookAtLH(Vector3fc, Vector3fc, Vector3fc)
     * @see #lookAtLH(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix4x3f setLookAtLH(float eyeX, float eyeY, float eyeZ,
                                  float centerX, float centerY, float centerZ,
                                  float upX, float upY, float upZ) {
        // Compute direction from position to lookAt
        float dirX, dirY, dirZ;
        dirX = centerX - eyeX;
        dirY = centerY - eyeY;
        dirZ = centerZ - eyeZ;
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLength;
        dirY *= invDirLength;
        dirZ *= invDirLength;
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
        m30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        m31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        m32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        properties = PROPERTY_ORTHONORMAL;

        return this;
    }

    /**
     * Apply a "lookat" transformation to this matrix for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAtLH(Vector3fc, Vector3fc, Vector3fc)}.
     * 
     * @see #lookAtLH(float, float, float, float, float, float, float, float, float)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4x3f lookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up, Matrix4x3f dest) {
        return lookAtLH(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a "lookat" transformation to this matrix for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAtLH(Vector3fc, Vector3fc, Vector3fc)}.
     * 
     * @see #lookAtLH(float, float, float, float, float, float, float, float, float)
     * 
     * @param eye
     *            the position of the camera
     * @param center
     *            the point in space to look at
     * @param up
     *            the direction of 'up'
     * @return this
     */
    public Matrix4x3f lookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up) {
        return lookAtLH(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z(), this);
    }

    /**
     * Apply a "lookat" transformation to this matrix for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAtLH(float, float, float, float, float, float, float, float, float) setLookAtLH()}.
     * 
     * @see #lookAtLH(Vector3fc, Vector3fc, Vector3fc)
     * @see #setLookAtLH(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix4x3f lookAtLH(float eyeX, float eyeY, float eyeZ,
                               float centerX, float centerY, float centerZ,
                               float upX, float upY, float upZ, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setLookAtLH(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        return lookAtLHGeneric(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
    }
    private Matrix4x3f lookAtLHGeneric(float eyeX, float eyeY, float eyeZ,
                                       float centerX, float centerY, float centerZ,
                                       float upX, float upY, float upZ, Matrix4x3f dest) {
        // Compute direction from position to lookAt
        float dirX, dirY, dirZ;
        dirX = centerX - eyeX;
        dirY = centerY - eyeY;
        dirZ = centerZ - eyeZ;
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLength;
        dirY *= invDirLength;
        dirZ *= invDirLength;
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);

        // perform optimized matrix multiplication
        // compute last column first, because others do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
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
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);

        return dest;
    }

    /**
     * Apply a "lookat" transformation to this matrix for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAtLH(float, float, float, float, float, float, float, float, float) setLookAtLH()}.
     * 
     * @see #lookAtLH(Vector3fc, Vector3fc, Vector3fc)
     * @see #setLookAtLH(float, float, float, float, float, float, float, float, float)
     * 
     * @param eyeX
     *              the x-coordinate of the eye/camera location
     * @param eyeY
     *              the y-coordinate of the eye/camera location
     * @param eyeZ
     *              the z-coordinate of the eye/camera location
     * @param centerX
     *              the x-coordinate of the point to look at
     * @param centerY
     *              the y-coordinate of the point to look at
     * @param centerZ
     *              the z-coordinate of the point to look at
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Matrix4x3f lookAtLH(float eyeX, float eyeY, float eyeZ,
                               float centerX, float centerY, float centerZ,
                               float upX, float upY, float upZ) {
        return lookAtLH(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, this);
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
    public Matrix4x3f rotate(Quaternionfc quat, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotation(quat);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return rotateTranslation(quat, dest);
        return rotateGeneric(quat, dest);
    }
    private Matrix4x3f rotateGeneric(Quaternionfc quat, Matrix4x3f dest) {
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
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
    public Matrix4x3f rotate(Quaternionfc quat) {
        return rotate(quat, this);
    }

    /**
     * Apply the rotation - and possibly scaling - transformation of the given {@link Quaternionfc} to this matrix, which is assumed to only contain a translation, and store
     * the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to only contain a translation.
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
    public Matrix4x3f rotateTranslation(Quaternionfc quat, Matrix4x3f dest) {
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
        dest.m20 = rm20;
        dest.m21 = rm21;
        dest.m22 = rm22;
        dest.m00 = rm00;
        dest.m01 = rm01;
        dest.m02 = rm02;
        dest.m10 = rm10;
        dest.m11 = rm11;
        dest.m12 = rm12;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotateLocal(Quaternionfc quat, Matrix4x3f dest) {
        float w2 = quat.w() * quat.w();
        float x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y();
        float z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w();
        float xy = quat.x() * quat.y();
        float xz = quat.x() * quat.z();
        float yw = quat.y() * quat.w();
        float yz = quat.y() * quat.z();
        float xw = quat.x() * quat.w();
        float lm00 = w2 + x2 - z2 - y2;
        float lm01 = xy + zw + zw + xy;
        float lm02 = xz - yw + xz - yw;
        float lm10 = -zw + xy - zw + xy;
        float lm11 = y2 - z2 + w2 - x2;
        float lm12 = yz + yz + xw + xw;
        float lm20 = yw + xz + xz + yw;
        float lm21 = yz + yz - xw - xw;
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
        float nm30 = lm00 * m30 + lm10 * m31 + lm20 * m32;
        float nm31 = lm01 * m30 + lm11 * m31 + lm21 * m32;
        float nm32 = lm02 * m30 + lm12 * m31 + lm22 * m32;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m30 = nm30;
        dest.m31 = nm31;
        dest.m32 = nm32;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
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
    public Matrix4x3f rotateLocal(Quaternionfc quat) {
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
    public Matrix4x3f rotate(AxisAngle4f axisAngle) {
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
    public Matrix4x3f rotate(AxisAngle4f axisAngle, Matrix4x3f dest) {
        return rotate(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z, dest);
    }

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis, to this matrix.
     * <p>
     * The axis described by the <code>axis</code> vector needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given axis-angle,
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
    public Matrix4x3f rotate(float angle, Vector3fc axis) {
        return rotate(angle, axis.x(), axis.y(), axis.z());
    }

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis and store the result in <code>dest</code>.
     * <p>
     * The axis described by the <code>axis</code> vector needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given axis-angle,
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
    public Matrix4x3f rotate(float angle, Vector3fc axis, Matrix4x3f dest) {
        return rotate(angle, axis.x(), axis.y(), axis.z(), dest);
    }

    public Matrix4x3f reflect(float a, float b, float c, float d, Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.reflection(a, b, c, d);

        float da = a + a, db = b + b, dc = c + c, dd = d + d;
        float rm00 = 1.0f - da * a;
        float rm01 = -da * b;
        float rm02 = -da * c;
        float rm10 = -db * a;
        float rm11 = 1.0f - db * b;
        float rm12 = -db * c;
        float rm20 = -dc * a;
        float rm21 = -dc * b;
        float rm22 = 1.0f - dc * c;
        float rm30 = -dd * a;
        float rm31 = -dd * b;
        float rm32 = -dd * c;

        // matrix multiplication
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
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
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);

        return dest;
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects about the given plane
     * specified via the equation <code>x*a + y*b + z*c + d = 0</code>.
     * <p>
     * The vector <code>(a, b, c)</code> must be a unit vector.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * <p>
     * Reference: <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/bb281733(v=vs.85).aspx">msdn.microsoft.com</a>
     * 
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return this
     */
    public Matrix4x3f reflect(float a, float b, float c, float d) {
        return reflect(a, b, c, d, this);
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects about the given plane
     * specified via the plane normal and a point on the plane.
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
     * @param px
     *          the x-coordinate of a point on the plane
     * @param py
     *          the y-coordinate of a point on the plane
     * @param pz
     *          the z-coordinate of a point on the plane
     * @return this
     */
    public Matrix4x3f reflect(float nx, float ny, float nz, float px, float py, float pz) {
        return reflect(nx, ny, nz, px, py, pz, this);
    }

    public Matrix4x3f reflect(float nx, float ny, float nz, float px, float py, float pz, Matrix4x3f dest) {
        float invLength = Math.invsqrt(nx * nx + ny * ny + nz * nz);
        float nnx = nx * invLength;
        float nny = ny * invLength;
        float nnz = nz * invLength;
        /* See: http://mathworld.wolfram.com/Plane.html */
        return reflect(nnx, nny, nnz, -nnx * px - nny * py - nnz * pz, dest);
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects about the given plane
     * specified via the plane normal and a point on the plane.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param normal
     *          the plane normal
     * @param point
     *          a point on the plane
     * @return this
     */
    public Matrix4x3f reflect(Vector3fc normal, Vector3fc point) {
        return reflect(normal.x(), normal.y(), normal.z(), point.x(), point.y(), point.z());
    }

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects about a plane
     * specified via the plane orientation and a point on the plane.
     * <p>
     * This method can be used to build a reflection transformation based on the orientation of a mirror object in the scene.
     * It is assumed that the default mirror plane's normal is <code>(0, 0, 1)</code>. So, if the given {@link Quaternionfc} is
     * the identity (does not apply any additional rotation), the reflection plane will be <code>z=0</code>, offset by the given <code>point</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param orientation
     *          the plane orientation
     * @param point
     *          a point on the plane
     * @return this
     */
    public Matrix4x3f reflect(Quaternionfc orientation, Vector3fc point) {
        return reflect(orientation, point, this);
    }

    public Matrix4x3f reflect(Quaternionfc orientation, Vector3fc point, Matrix4x3f dest) {
        double num1 = orientation.x() + orientation.x();
        double num2 = orientation.y() + orientation.y();
        double num3 = orientation.z() + orientation.z();
        float normalX = (float) (orientation.x() * num3 + orientation.w() * num2);
        float normalY = (float) (orientation.y() * num3 - orientation.w() * num1);
        float normalZ = (float) (1.0 - (orientation.x() * num1 + orientation.y() * num2));
        return reflect(normalX, normalY, normalZ, point.x(), point.y(), point.z(), dest);
    }

    public Matrix4x3f reflect(Vector3fc normal, Vector3fc point, Matrix4x3f dest) {
        return reflect(normal.x(), normal.y(), normal.z(), point.x(), point.y(), point.z(), dest);
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects about the given plane
     * specified via the equation <code>x*a + y*b + z*c + d = 0</code>.
     * <p>
     * The vector <code>(a, b, c)</code> must be a unit vector.
     * <p>
     * Reference: <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/bb281733(v=vs.85).aspx">msdn.microsoft.com</a>
     * 
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return this
     */
    public Matrix4x3f reflection(float a, float b, float c, float d) {
        float da = a + a, db = b + b, dc = c + c, dd = d + d;
        m00 = 1.0f - da * a;
        m01 = -da * b;
        m02 = -da * c;
        m10 = -db * a;
        m11 = 1.0f - db * b;
        m12 = -db * c;
        m20 = -dc * a;
        m21 = -dc * b;
        m22 = 1.0f - dc * c;
        m30 = -dd * a;
        m31 = -dd * b;
        m32 = -dd * c;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects about the given plane
     * specified via the plane normal and a point on the plane.
     * 
     * @param nx
     *          the x-coordinate of the plane normal
     * @param ny
     *          the y-coordinate of the plane normal
     * @param nz
     *          the z-coordinate of the plane normal
     * @param px
     *          the x-coordinate of a point on the plane
     * @param py
     *          the y-coordinate of a point on the plane
     * @param pz
     *          the z-coordinate of a point on the plane
     * @return this
     */
    public Matrix4x3f reflection(float nx, float ny, float nz, float px, float py, float pz) {
        float invLength = Math.invsqrt(nx * nx + ny * ny + nz * nz);
        float nnx = nx * invLength;
        float nny = ny * invLength;
        float nnz = nz * invLength;
        /* See: http://mathworld.wolfram.com/Plane.html */
        return reflection(nnx, nny, nnz, -nnx * px - nny * py - nnz * pz);
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects about the given plane
     * specified via the plane normal and a point on the plane.
     * 
     * @param normal
     *          the plane normal
     * @param point
     *          a point on the plane
     * @return this
     */
    public Matrix4x3f reflection(Vector3fc normal, Vector3fc point) {
        return reflection(normal.x(), normal.y(), normal.z(), point.x(), point.y(), point.z());
    }

    /**
     * Set this matrix to a mirror/reflection transformation that reflects about a plane
     * specified via the plane orientation and a point on the plane.
     * <p>
     * This method can be used to build a reflection transformation based on the orientation of a mirror object in the scene.
     * It is assumed that the default mirror plane's normal is <code>(0, 0, 1)</code>. So, if the given {@link Quaternionfc} is
     * the identity (does not apply any additional rotation), the reflection plane will be <code>z=0</code>, offset by the given <code>point</code>.
     * 
     * @param orientation
     *          the plane orientation
     * @param point
     *          a point on the plane
     * @return this
     */
    public Matrix4x3f reflection(Quaternionfc orientation, Vector3fc point) {
        double num1 = orientation.x() + orientation.x();
        double num2 = orientation.y() + orientation.y();
        double num3 = orientation.z() + orientation.z();
        float normalX = (float) (orientation.x() * num3 + orientation.w() * num2);
        float normalY = (float) (orientation.y() * num3 - orientation.w() * num1);
        float normalZ = (float) (1.0 - (orientation.x() * num1 + orientation.y() * num2));
        return reflection(normalX, normalY, normalZ, point.x(), point.y(), point.z());
    }

    public Vector4f getRow(int row, Vector4f dest) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            dest.x = m00;
            dest.y = m10;
            dest.z = m20;
            dest.w = m30;
            break;
        case 1:
            dest.x = m01;
            dest.y = m11;
            dest.z = m21;
            dest.w = m31;
            break;
        case 2:
            dest.x = m02;
            dest.y = m12;
            dest.z = m22;
            dest.w = m32;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return dest;
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
    public Matrix4x3f setRow(int row, Vector4fc src) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            this.m00 = src.x();
            this.m10 = src.y();
            this.m20 = src.z();
            this.m30 = src.w();
            break;
        case 1:
            this.m01 = src.x();
            this.m11 = src.y();
            this.m21 = src.z();
            this.m31 = src.w();
            break;
        case 2:
            this.m02 = src.x();
            this.m12 = src.y();
            this.m22 = src.z();
            this.m32 = src.w();
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        properties = 0;
        return this;
    }

    public Vector3f getColumn(int column, Vector3f dest) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            dest.x = m00;
            dest.y = m01;
            dest.z = m02;
            break;
        case 1:
            dest.x = m10;
            dest.y = m11;
            dest.z = m12;
            break;
        case 2:
            dest.x = m20;
            dest.y = m21;
            dest.z = m22;
            break;
        case 3:
            dest.x = m30;
            dest.y = m31;
            dest.z = m32;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return dest;
    }

    /**
     * Set the column at the given <code>column</code> index, starting with <code>0</code>.
     * 
     * @param column
     *          the column index in <code>[0..3]</code>
     * @param src
     *          the column components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..3]</code>
     */
    public Matrix4x3f setColumn(int column, Vector3fc src) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            this.m00 = src.x();
            this.m01 = src.y();
            this.m02 = src.z();
            break;
        case 1:
            this.m10 = src.x();
            this.m11 = src.y();
            this.m12 = src.z();
            break;
        case 2:
            this.m20 = src.x();
            this.m21 = src.y();
            this.m22 = src.z();
            break;
        case 3:
            this.m30 = src.x();
            this.m31 = src.y();
            this.m32 = src.z();
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        properties = 0;
        return this;
    }

    /**
     * Compute a normal matrix from the left 3x3 submatrix of <code>this</code>
     * and store it into the left 3x3 submatrix of <code>this</code>.
     * All other values of <code>this</code> will be set to {@link #identity() identity}.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In that case, use {@link #set3x3(Matrix4x3fc)} to set a given Matrix4x3f to only the left 3x3 submatrix
     * of this matrix.
     * 
     * @see #set3x3(Matrix4x3fc)
     * 
     * @return this
     */
    public Matrix4x3f normal() {
        return normal(this);
    }

    /**
     * Compute a normal matrix from the left 3x3 submatrix of <code>this</code>
     * and store it into the left 3x3 submatrix of <code>dest</code>.
     * All other values of <code>dest</code> will be set to {@link #identity() identity}.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In that case, use {@link #set3x3(Matrix4x3fc)} to set a given Matrix4x3f to only the left 3x3 submatrix
     * of this matrix.
     * 
     * @see #set3x3(Matrix4x3fc)
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix4x3f normal(Matrix4x3f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.identity();
        else if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalOrthonormal(dest);
        return normalGeneric(dest);
    }
    private Matrix4x3f normalOrthonormal(Matrix4x3f dest) {
        if (dest != this)
            dest.set(this);
        return dest._properties(PROPERTY_ORTHONORMAL);
    }
    private Matrix4x3f normalGeneric(Matrix4x3f dest) {
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
        dest.m30 = 0.0f;
        dest.m31 = 0.0f;
        dest.m32 = 0.0f;
        dest.properties = properties & ~PROPERTY_TRANSLATION;
        return dest;
    }

    public Matrix3f normal(Matrix3f dest) {
        if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalOrthonormal(dest);
        return normalGeneric(dest);
    }
    private Matrix3f normalOrthonormal(Matrix3f dest) {
        return dest.set(this);
    }
    private Matrix3f normalGeneric(Matrix3f dest) {
        float m00m11 = m00 * m11;
        float m01m10 = m01 * m10;
        float m02m10 = m02 * m10;
        float m00m12 = m00 * m12;
        float m01m12 = m01 * m12;
        float m02m11 = m02 * m11;
        float det = (m00m11 - m01m10) * m22 + (m02m10 - m00m12) * m21 + (m01m12 - m02m11) * m20;
        float s = 1.0f / det;
        /* Invert and transpose in one go */
        dest.m00((m11 * m22 - m21 * m12) * s);
        dest.m01((m20 * m12 - m10 * m22) * s);
        dest.m02((m10 * m21 - m20 * m11) * s);
        dest.m10((m21 * m02 - m01 * m22) * s);
        dest.m11((m00 * m22 - m20 * m02) * s);
        dest.m12((m20 * m01 - m00 * m21) * s);
        dest.m20((m01m12 - m02m11) * s);
        dest.m21((m02m10 - m00m12) * s);
        dest.m22((m00m11 - m01m10) * s);
        return dest;
    }

    /**
     * Compute the cofactor matrix of the left 3x3 submatrix of <code>this</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal()} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @return this
     */
    public Matrix4x3f cofactor3x3() {
        return cofactor3x3(this);
    }

    /**
     * Compute the cofactor matrix of the left 3x3 submatrix of <code>this</code>
     * and store it into <code>dest</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal(Matrix3f)} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix3f cofactor3x3(Matrix3f dest) {
        dest.m00 = m11 * m22 - m21 * m12;
        dest.m01 = m20 * m12 - m10 * m22;
        dest.m02 = m10 * m21 - m20 * m11;
        dest.m10 = m21 * m02 - m01 * m22;
        dest.m11 = m00 * m22 - m20 * m02;
        dest.m12 = m20 * m01 - m00 * m21;
        dest.m20 = m01 * m12 - m02 * m11;
        dest.m21 = m02 * m10 - m00 * m12;
        dest.m22 = m00 * m11 - m01 * m10;
        return dest;
    }

    /**
     * Compute the cofactor matrix of the left 3x3 submatrix of <code>this</code>
     * and store it into <code>dest</code>.
     * All other values of <code>dest</code> will be set to {@link #identity() identity}.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal(Matrix4x3f)} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix4x3f cofactor3x3(Matrix4x3f dest) {
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
        dest.m30 = 0.0f;
        dest.m31 = 0.0f;
        dest.m32 = 0.0f;
        dest.properties = properties & ~PROPERTY_TRANSLATION;
        return dest;
    }

    /**
     * Normalize the left 3x3 submatrix of this matrix.
     * <p>
     * The resulting matrix will map unit vectors to unit vectors, though a pair of orthogonal input unit
     * vectors need not be mapped to a pair of orthogonal output vectors if the original matrix was not orthogonal itself
     * (i.e. had <i>skewing</i>).
     * 
     * @return this
     */
    public Matrix4x3f normalize3x3() {
        return normalize3x3(this);
    }

    public Matrix4x3f normalize3x3(Matrix4x3f dest) {
        float invXlen = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float invYlen = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float invZlen = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        dest.m00 = m00 * invXlen; dest.m01 = m01 * invXlen; dest.m02 = m02 * invXlen;
        dest.m10 = m10 * invYlen; dest.m11 = m11 * invYlen; dest.m12 = m12 * invYlen;
        dest.m20 = m20 * invZlen; dest.m21 = m21 * invZlen; dest.m22 = m22 * invZlen;
        dest.properties = properties;
        return dest;
    }

    public Matrix3f normalize3x3(Matrix3f dest) {
        float invXlen = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float invYlen = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float invZlen = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        dest.m00(m00 * invXlen); dest.m01(m01 * invXlen); dest.m02(m02 * invXlen);
        dest.m10(m10 * invYlen); dest.m11(m11 * invYlen); dest.m12(m12 * invYlen);
        dest.m20(m20 * invZlen); dest.m21(m21 * invZlen); dest.m22(m22 * invZlen);
        return dest;
    }

    public Vector4f frustumPlane(int which, Vector4f dest) {
        switch (which) {
        case PLANE_NX:
            dest.set(m00, m10, m20, 1.0f + m30).normalize();
            break;
        case PLANE_PX:
            dest.set(-m00, -m10, -m20, 1.0f - m30).normalize();
            break;
        case PLANE_NY:
            dest.set(m01, m11, m21, 1.0f + m31).normalize();
            break;
        case PLANE_PY:
            dest.set(-m01, -m11, -m21, 1.0f - m31).normalize();
            break;
        case PLANE_NZ:
            dest.set(m02, m12, m22, 1.0f + m32).normalize();
            break;
        case PLANE_PZ:
            dest.set(-m02, -m12, -m22, 1.0f - m32).normalize();
            break;
        default:
            throw new IllegalArgumentException("which"); //$NON-NLS-1$
        }
        return dest;
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

    public Vector3f origin(Vector3f origin) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float d = m01 * m12 - m02 * m11;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float j = m21 * m32 - m22 * m31;
        origin.x = -m10 * j + m11 * h - m12 * g;
        origin.y =  m00 * j - m01 * h + m02 * g;
        origin.z = -m30 * d + m31 * b - m32 * a;
        return origin;
    }

    /**
     * Apply a projection transformation to this matrix that projects onto the plane specified via the general plane equation
     * <code>x*a + y*b + z*c + d = 0</code> as if casting a shadow from a given light position/direction <code>light</code>.
     * <p>
     * If <code>light.w</code> is <code>0.0</code> the light is being treated as a directional light; if it is <code>1.0</code> it is a point light.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the shadow matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * reflection will be applied first!
     * <p>
     * Reference: <a href="ftp://ftp.sgi.com/opengl/contrib/blythe/advanced99/notes/node192.html">ftp.sgi.com</a>
     * 
     * @param light
     *          the light's vector
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return this
     */
    public Matrix4x3f shadow(Vector4fc light, float a, float b, float c, float d) {
        return shadow(light.x(), light.y(), light.z(), light.w(), a, b, c, d, this);
    }

    public Matrix4x3f shadow(Vector4fc light, float a, float b, float c, float d, Matrix4x3f dest) {
        return shadow(light.x(), light.y(), light.z(), light.w(), a, b, c, d, dest);
    }

    /**
     * Apply a projection transformation to this matrix that projects onto the plane specified via the general plane equation
     * <code>x*a + y*b + z*c + d = 0</code> as if casting a shadow from a given light position/direction <code>(lightX, lightY, lightZ, lightW)</code>.
     * <p>
     * If <code>lightW</code> is <code>0.0</code> the light is being treated as a directional light; if it is <code>1.0</code> it is a point light.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the shadow matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * reflection will be applied first!
     * <p>
     * Reference: <a href="ftp://ftp.sgi.com/opengl/contrib/blythe/advanced99/notes/node192.html">ftp.sgi.com</a>
     * 
     * @param lightX
     *          the x-component of the light's vector
     * @param lightY
     *          the y-component of the light's vector
     * @param lightZ
     *          the z-component of the light's vector
     * @param lightW
     *          the w-component of the light's vector
     * @param a
     *          the x factor in the plane equation
     * @param b
     *          the y factor in the plane equation
     * @param c
     *          the z factor in the plane equation
     * @param d
     *          the constant in the plane equation
     * @return this
     */
    public Matrix4x3f shadow(float lightX, float lightY, float lightZ, float lightW, float a, float b, float c, float d) {
        return shadow(lightX, lightY, lightZ, lightW, a, b, c, d, this);
    }

    public Matrix4x3f shadow(float lightX, float lightY, float lightZ, float lightW, float a, float b, float c, float d, Matrix4x3f dest) {
        // normalize plane
        float invPlaneLen = Math.invsqrt(a*a + b*b + c*c);
        float an = a * invPlaneLen;
        float bn = b * invPlaneLen;
        float cn = c * invPlaneLen;
        float dn = d * invPlaneLen;

        float dot = an * lightX + bn * lightY + cn * lightZ + dn * lightW;

        // compute right matrix elements
        float rm00 = dot - an * lightX;
        float rm01 = -an * lightY;
        float rm02 = -an * lightZ;
        float rm03 = -an * lightW;
        float rm10 = -bn * lightX;
        float rm11 = dot - bn * lightY;
        float rm12 = -bn * lightZ;
        float rm13 = -bn * lightW;
        float rm20 = -cn * lightX;
        float rm21 = -cn * lightY;
        float rm22 = dot - cn * lightZ;
        float rm23 = -cn * lightW;
        float rm30 = -dn * lightX;
        float rm31 = -dn * lightY;
        float rm32 = -dn * lightZ;
        float rm33 = dot - dn * lightW;

        // matrix multiplication
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02 + m30 * rm03;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02 + m31 * rm03;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02 + m32 * rm03;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12 + m30 * rm13;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12 + m31 * rm13;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12 + m32 * rm13;
        float nm20 = m00 * rm20 + m10 * rm21 + m20 * rm22 + m30 * rm23;
        float nm21 = m01 * rm20 + m11 * rm21 + m21 * rm22 + m31 * rm23;
        float nm22 = m02 * rm20 + m12 * rm21 + m22 * rm22 + m32 * rm23;
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30 * rm33;
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31 * rm33;
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32 * rm33;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);

        return dest;
    }

    public Matrix4x3f shadow(Vector4fc light, Matrix4x3fc planeTransform, Matrix4x3f dest) {
        // compute plane equation by transforming (y = 0)
        float a = planeTransform.m10();
        float b = planeTransform.m11();
        float c = planeTransform.m12();
        float d = -a * planeTransform.m30() - b * planeTransform.m31() - c * planeTransform.m32();
        return shadow(light.x(), light.y(), light.z(), light.w(), a, b, c, d, dest);
    }

    /**
     * Apply a projection transformation to this matrix that projects onto the plane with the general plane equation
     * <code>y = 0</code> as if casting a shadow from a given light position/direction <code>light</code>.
     * <p>
     * Before the shadow projection is applied, the plane is transformed via the specified <code>planeTransformation</code>.
     * <p>
     * If <code>light.w</code> is <code>0.0</code> the light is being treated as a directional light; if it is <code>1.0</code> it is a point light.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the shadow matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * reflection will be applied first!
     * 
     * @param light
     *          the light's vector
     * @param planeTransform
     *          the transformation to transform the implied plane <code>y = 0</code> before applying the projection
     * @return this
     */
    public Matrix4x3f shadow(Vector4fc light, Matrix4x3fc planeTransform) {
        return shadow(light, planeTransform, this);
    }

    public Matrix4x3f shadow(float lightX, float lightY, float lightZ, float lightW, Matrix4x3fc planeTransform, Matrix4x3f dest) {
        // compute plane equation by transforming (y = 0)
        float a = planeTransform.m10();
        float b = planeTransform.m11();
        float c = planeTransform.m12();
        float d = -a * planeTransform.m30() - b * planeTransform.m31() - c * planeTransform.m32();
        return shadow(lightX, lightY, lightZ, lightW, a, b, c, d, dest);
    }

    /**
     * Apply a projection transformation to this matrix that projects onto the plane with the general plane equation
     * <code>y = 0</code> as if casting a shadow from a given light position/direction <code>(lightX, lightY, lightZ, lightW)</code>.
     * <p>
     * Before the shadow projection is applied, the plane is transformed via the specified <code>planeTransformation</code>.
     * <p>
     * If <code>lightW</code> is <code>0.0</code> the light is being treated as a directional light; if it is <code>1.0</code> it is a point light.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the shadow matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * reflection will be applied first!
     * 
     * @param lightX
     *          the x-component of the light vector
     * @param lightY
     *          the y-component of the light vector
     * @param lightZ
     *          the z-component of the light vector
     * @param lightW
     *          the w-component of the light vector
     * @param planeTransform
     *          the transformation to transform the implied plane <code>y = 0</code> before applying the projection
     * @return this
     */
    public Matrix4x3f shadow(float lightX, float lightY, float lightZ, float lightW, Matrix4x3f planeTransform) {
        return shadow(lightX, lightY, lightZ, lightW, planeTransform, this);
    }

    /**
     * Set this matrix to a cylindrical billboard transformation that rotates the local +Z axis of a given object with position <code>objPos</code> towards
     * a target position at <code>targetPos</code> while constraining a cylindrical rotation around the given <code>up</code> vector.
     * <p>
     * This method can be used to create the complete model transformation for a given object, including the translation of the object to
     * its position <code>objPos</code>.
     * 
     * @param objPos
     *          the position of the object to rotate towards <code>targetPos</code>
     * @param targetPos
     *          the position of the target (for example the camera) towards which to rotate the object
     * @param up
     *          the rotation axis (must be {@link Vector3f#normalize() normalized})
     * @return this
     */
    public Matrix4x3f billboardCylindrical(Vector3fc objPos, Vector3fc targetPos, Vector3fc up) {
        float dirX = targetPos.x() - objPos.x();
        float dirY = targetPos.y() - objPos.y();
        float dirZ = targetPos.z() - objPos.z();
        // left = up x dir
        float leftX = up.y() * dirZ - up.z() * dirY;
        float leftY = up.z() * dirX - up.x() * dirZ;
        float leftZ = up.x() * dirY - up.y() * dirX;
        // normalize left
        float invLeftLen = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLen;
        leftY *= invLeftLen;
        leftZ *= invLeftLen;
        // recompute dir by constraining rotation around 'up'
        // dir = left x up
        dirX = leftY * up.z() - leftZ * up.y();
        dirY = leftZ * up.x() - leftX * up.z();
        dirZ = leftX * up.y() - leftY * up.x();
        // normalize dir
        float invDirLen = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLen;
        dirY *= invDirLen;
        dirZ *= invDirLen;
        // set matrix elements
        m00 = leftX;
        m01 = leftY;
        m02 = leftZ;
        m10 = up.x();
        m11 = up.y();
        m12 = up.z();
        m20 = dirX;
        m21 = dirY;
        m22 = dirZ;
        m30 = objPos.x();
        m31 = objPos.y();
        m32 = objPos.z();
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to a spherical billboard transformation that rotates the local +Z axis of a given object with position <code>objPos</code> towards
     * a target position at <code>targetPos</code>.
     * <p>
     * This method can be used to create the complete model transformation for a given object, including the translation of the object to
     * its position <code>objPos</code>.
     * <p>
     * If preserving an <i>up</i> vector is not necessary when rotating the +Z axis, then a shortest arc rotation can be obtained 
     * using {@link #billboardSpherical(Vector3fc, Vector3fc)}.
     * 
     * @see #billboardSpherical(Vector3fc, Vector3fc)
     * 
     * @param objPos
     *          the position of the object to rotate towards <code>targetPos</code>
     * @param targetPos
     *          the position of the target (for example the camera) towards which to rotate the object
     * @param up
     *          the up axis used to orient the object
     * @return this
     */
    public Matrix4x3f billboardSpherical(Vector3fc objPos, Vector3fc targetPos, Vector3fc up) {
        float dirX = targetPos.x() - objPos.x();
        float dirY = targetPos.y() - objPos.y();
        float dirZ = targetPos.z() - objPos.z();
        // normalize dir
        float invDirLen = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLen;
        dirY *= invDirLen;
        dirZ *= invDirLen;
        // left = up x dir
        float leftX = up.y() * dirZ - up.z() * dirY;
        float leftY = up.z() * dirX - up.x() * dirZ;
        float leftZ = up.x() * dirY - up.y() * dirX;
        // normalize left
        float invLeftLen = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLen;
        leftY *= invLeftLen;
        leftZ *= invLeftLen;
        // up = dir x left
        float upX = dirY * leftZ - dirZ * leftY;
        float upY = dirZ * leftX - dirX * leftZ;
        float upZ = dirX * leftY - dirY * leftX;
        // set matrix elements
        m00 = leftX;
        m01 = leftY;
        m02 = leftZ;
        m10 = upX;
        m11 = upY;
        m12 = upZ;
        m20 = dirX;
        m21 = dirY;
        m22 = dirZ;
        m30 = objPos.x();
        m31 = objPos.y();
        m32 = objPos.z();
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to a spherical billboard transformation that rotates the local +Z axis of a given object with position <code>objPos</code> towards
     * a target position at <code>targetPos</code> using a shortest arc rotation by not preserving any <i>up</i> vector of the object.
     * <p>
     * This method can be used to create the complete model transformation for a given object, including the translation of the object to
     * its position <code>objPos</code>.
     * <p>
     * In order to specify an <i>up</i> vector which needs to be maintained when rotating the +Z axis of the object,
     * use {@link #billboardSpherical(Vector3fc, Vector3fc, Vector3fc)}.
     * 
     * @see #billboardSpherical(Vector3fc, Vector3fc, Vector3fc)
     * 
     * @param objPos
     *          the position of the object to rotate towards <code>targetPos</code>
     * @param targetPos
     *          the position of the target (for example the camera) towards which to rotate the object
     * @return this
     */
    public Matrix4x3f billboardSpherical(Vector3fc objPos, Vector3fc targetPos) {
        float toDirX = targetPos.x() - objPos.x();
        float toDirY = targetPos.y() - objPos.y();
        float toDirZ = targetPos.z() - objPos.z();
        float x = -toDirY;
        float y = toDirX;
        float w = Math.sqrt(toDirX * toDirX + toDirY * toDirY + toDirZ * toDirZ) + toDirZ;
        float invNorm = Math.invsqrt(x * x + y * y + w * w);
        x *= invNorm;
        y *= invNorm;
        w *= invNorm;
        float q00 = (x + x) * x;
        float q11 = (y + y) * y;
        float q01 = (x + x) * y;
        float q03 = (x + x) * w;
        float q13 = (y + y) * w;
        m00 = 1.0f - q11;
        m01 = q01;
        m02 = -q13;
        m10 = q01;
        m11 = 1.0f - q00;
        m12 = q03;
        m20 = q13;
        m21 = -q03;
        m22 = 1.0f - q11 - q00;
        m30 = objPos.x();
        m31 = objPos.y();
        m32 = objPos.z();
        properties = PROPERTY_ORTHONORMAL;
        return this;
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
        result = prime * result + Float.floatToIntBits(m30);
        result = prime * result + Float.floatToIntBits(m31);
        result = prime * result + Float.floatToIntBits(m32);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Matrix4x3f))
            return false;
        Matrix4x3f other = (Matrix4x3f) obj;
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
        if (Float.floatToIntBits(m30) != Float.floatToIntBits(other.m30))
            return false;
        if (Float.floatToIntBits(m31) != Float.floatToIntBits(other.m31))
            return false;
        if (Float.floatToIntBits(m32) != Float.floatToIntBits(other.m32))
            return false;
        return true;
    }

    public boolean equals(Matrix4x3fc m, float delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix4x3f))
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
        if (!Runtime.equals(m30, m.m30(), delta))
            return false;
        if (!Runtime.equals(m31, m.m31(), delta))
            return false;
        if (!Runtime.equals(m32, m.m32(), delta))
            return false;
        return true;
    }

    public Matrix4x3f pick(float x, float y, float width, float height, int[] viewport, Matrix4x3f dest) {
        float sx = viewport[2] / width;
        float sy = viewport[3] / height;
        float tx = (viewport[2] + 2.0f * (viewport[0] - x)) / width;
        float ty = (viewport[3] + 2.0f * (viewport[1] - y)) / height;
        dest.m30 = m00 * tx + m10 * ty + m30;
        dest.m31 = m01 * tx + m11 * ty + m31;
        dest.m32 = m02 * tx + m12 * ty + m32;
        dest.m00 = m00 * sx;
        dest.m01 = m01 * sx;
        dest.m02 = m02 * sx;
        dest.m10 = m10 * sy;
        dest.m11 = m11 * sy;
        dest.m12 = m12 * sy;
        dest.properties = 0;
        return dest;
    }

    /**
     * Apply a picking transformation to this matrix using the given window coordinates <code>(x, y)</code> as the pick center
     * and the given <code>(width, height)</code> as the size of the picking region in window coordinates.
     * 
     * @param x
     *          the x coordinate of the picking region center in window coordinates
     * @param y
     *          the y coordinate of the picking region center in window coordinates
     * @param width
     *          the width of the picking region in window coordinates
     * @param height
     *          the height of the picking region in window coordinates
     * @param viewport
     *          the viewport described by <code>[x, y, width, height]</code>
     * @return this
     */
    public Matrix4x3f pick(float x, float y, float width, float height, int[] viewport) {
        return pick(x, y, width, height, viewport, this);
    }

    /**
     * Exchange the values of <code>this</code> matrix with the given <code>other</code> matrix.
     * 
     * @param other
     *          the other matrix to exchange the values with
     * @return this
     */
    public Matrix4x3f swap(Matrix4x3f other) {
        MemUtil.INSTANCE.swap(this, other);
        int props = properties;
        this.properties = other.properties;
        other.properties = props;
        return this;
    }

    public Matrix4x3f arcball(float radius, float centerX, float centerY, float centerZ, float angleX, float angleY, Matrix4x3f dest) {
        float m30 = m20 * -radius + this.m30;
        float m31 = m21 * -radius + this.m31;
        float m32 = m22 * -radius + this.m32;
        float sin = Math.sin(angleX);
        float cos = Math.cosFromSin(sin, angleX);
        float nm10 = m10 * cos + m20 * sin;
        float nm11 = m11 * cos + m21 * sin;
        float nm12 = m12 * cos + m22 * sin;
        float m20 = this.m20 * cos - m10 * sin;
        float m21 = this.m21 * cos - m11 * sin;
        float m22 = this.m22 * cos - m12 * sin;
        sin = Math.sin(angleY);
        cos = Math.cosFromSin(sin, angleY);
        float nm00 = m00 * cos - m20 * sin;
        float nm01 = m01 * cos - m21 * sin;
        float nm02 = m02 * cos - m22 * sin;
        float nm20 = m00 * sin + m20 * cos;
        float nm21 = m01 * sin + m21 * cos;
        float nm22 = m02 * sin + m22 * cos;
        dest.m30 = -nm00 * centerX - nm10 * centerY - nm20 * centerZ + m30;
        dest.m31 = -nm01 * centerX - nm11 * centerY - nm21 * centerZ + m31;
        dest.m32 = -nm02 * centerX - nm12 * centerY - nm22 * centerZ + m32;
        dest.m20 = nm20;
        dest.m21 = nm21;
        dest.m22 = nm22;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    public Matrix4x3f arcball(float radius, Vector3fc center, float angleX, float angleY, Matrix4x3f dest) {
        return arcball(radius, center.x(), center.y(), center.z(), angleX, angleY, dest);
    }

    /**
     * Apply an arcball view transformation to this matrix with the given <code>radius</code> and center <code>(centerX, centerY, centerZ)</code>
     * position of the arcball and the specified X and Y rotation angles.
     * <p>
     * This method is equivalent to calling: <code>translate(0, 0, -radius).rotateX(angleX).rotateY(angleY).translate(-centerX, -centerY, -centerZ)</code>
     * 
     * @param radius
     *          the arcball radius
     * @param centerX
     *          the x coordinate of the center position of the arcball
     * @param centerY
     *          the y coordinate of the center position of the arcball
     * @param centerZ
     *          the z coordinate of the center position of the arcball
     * @param angleX
     *          the rotation angle around the X axis in radians
     * @param angleY
     *          the rotation angle around the Y axis in radians
     * @return this
     */
    public Matrix4x3f arcball(float radius, float centerX, float centerY, float centerZ, float angleX, float angleY) {
        return arcball(radius, centerX, centerY, centerZ, angleX, angleY, this);
    }

    /**
     * Apply an arcball view transformation to this matrix with the given <code>radius</code> and <code>center</code>
     * position of the arcball and the specified X and Y rotation angles.
     * <p>
     * This method is equivalent to calling: <code>translate(0, 0, -radius).rotateX(angleX).rotateY(angleY).translate(-center.x, -center.y, -center.z)</code>
     * 
     * @param radius
     *          the arcball radius
     * @param center
     *          the center position of the arcball
     * @param angleX
     *          the rotation angle around the X axis in radians
     * @param angleY
     *          the rotation angle around the Y axis in radians
     * @return this
     */
    public Matrix4x3f arcball(float radius, Vector3fc center, float angleX, float angleY) {
        return arcball(radius, center.x(), center.y(), center.z(), angleX, angleY, this);
    }

    public Matrix4x3f transformAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Vector3f outMin, Vector3f outMax) {
        float xax = m00 * minX, xay = m01 * minX, xaz = m02 * minX;
        float xbx = m00 * maxX, xby = m01 * maxX, xbz = m02 * maxX;
        float yax = m10 * minY, yay = m11 * minY, yaz = m12 * minY;
        float ybx = m10 * maxY, yby = m11 * maxY, ybz = m12 * maxY;
        float zax = m20 * minZ, zay = m21 * minZ, zaz = m22 * minZ;
        float zbx = m20 * maxZ, zby = m21 * maxZ, zbz = m22 * maxZ;
        float xminx, xminy, xminz, yminx, yminy, yminz, zminx, zminy, zminz;
        float xmaxx, xmaxy, xmaxz, ymaxx, ymaxy, ymaxz, zmaxx, zmaxy, zmaxz;
        if (xax < xbx) {
            xminx = xax;
            xmaxx = xbx;
        } else {
            xminx = xbx;
            xmaxx = xax;
        }
        if (xay < xby) {
            xminy = xay;
            xmaxy = xby;
        } else {
            xminy = xby;
            xmaxy = xay;
        }
        if (xaz < xbz) {
            xminz = xaz;
            xmaxz = xbz;
        } else {
            xminz = xbz;
            xmaxz = xaz;
        }
        if (yax < ybx) {
            yminx = yax;
            ymaxx = ybx;
        } else {
            yminx = ybx;
            ymaxx = yax;
        }
        if (yay < yby) {
            yminy = yay;
            ymaxy = yby;
        } else {
            yminy = yby;
            ymaxy = yay;
        }
        if (yaz < ybz) {
            yminz = yaz;
            ymaxz = ybz;
        } else {
            yminz = ybz;
            ymaxz = yaz;
        }
        if (zax < zbx) {
            zminx = zax;
            zmaxx = zbx;
        } else {
            zminx = zbx;
            zmaxx = zax;
        }
        if (zay < zby) {
            zminy = zay;
            zmaxy = zby;
        } else {
            zminy = zby;
            zmaxy = zay;
        }
        if (zaz < zbz) {
            zminz = zaz;
            zmaxz = zbz;
        } else {
            zminz = zbz;
            zmaxz = zaz;
        }
        outMin.x = xminx + yminx + zminx + m30;
        outMin.y = xminy + yminy + zminy + m31;
        outMin.z = xminz + yminz + zminz + m32;
        outMax.x = xmaxx + ymaxx + zmaxx + m30;
        outMax.y = xmaxy + ymaxy + zmaxy + m31;
        outMax.z = xmaxz + ymaxz + zmaxz + m32;
        return this;
    }

    public Matrix4x3f transformAab(Vector3fc min, Vector3fc max, Vector3f outMin, Vector3f outMax) {
        return transformAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), outMin, outMax);
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
    public Matrix4x3f lerp(Matrix4x3fc other, float t) {
        return lerp(other, t, this);
    }

    public Matrix4x3f lerp(Matrix4x3fc other, float t, Matrix4x3f dest) {
        dest.m00 = Math.fma(other.m00() - m00, t, m00);
        dest.m01 = Math.fma(other.m01() - m01, t, m01);
        dest.m02 = Math.fma(other.m02() - m02, t, m02);
        dest.m10 = Math.fma(other.m10() - m10, t, m10);
        dest.m11 = Math.fma(other.m11() - m11, t, m11);
        dest.m12 = Math.fma(other.m12() - m12, t, m12);
        dest.m20 = Math.fma(other.m20() - m20, t, m20);
        dest.m21 = Math.fma(other.m21() - m21, t, m21);
        dest.m22 = Math.fma(other.m22() - m22, t, m22);
        dest.m30 = Math.fma(other.m30() - m30, t, m30);
        dest.m31 = Math.fma(other.m31() - m31, t, m31);
        dest.m32 = Math.fma(other.m32() - m32, t, m32);
        dest.properties = properties & other.properties();
        return dest;
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
     * use {@link #rotationTowards(Vector3fc, Vector3fc) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix4x3f().lookAt(new Vector3f(), new Vector3f(dir).negate(), up).invert(), dest)</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float, Matrix4x3f)
     * @see #rotationTowards(Vector3fc, Vector3fc)
     * 
     * @param dir
     *              the direction to rotate towards
     * @param up
     *              the up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    public Matrix4x3f rotateTowards(Vector3fc dir, Vector3fc up, Matrix4x3f dest) {
        return rotateTowards(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>dir</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(Vector3fc, Vector3fc) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix4x3f().lookAt(new Vector3f(), new Vector3f(dir).negate(), up).invert())</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float)
     * @see #rotationTowards(Vector3fc, Vector3fc)
     * 
     * @param dir
     *              the direction to orient towards
     * @param up
     *              the up vector
     * @return this
     */
    public Matrix4x3f rotateTowards(Vector3fc dir, Vector3fc up) {
        return rotateTowards(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), this);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>(dirX, dirY, dirZ)</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a rotation transformation without post-multiplying it,
     * use {@link #rotationTowards(float, float, float, float, float, float) rotationTowards()}.
     * <p>
     * This method is equivalent to calling: <code>mul(new Matrix4x3f().lookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invert())</code>
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
    public Matrix4x3f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        return rotateTowards(dirX, dirY, dirZ, upX, upY, upZ, this);
    }

    /**
     * Apply a model transformation to this matrix for a right-handed coordinate system, 
     * that aligns the local <code>+Z</code> axis with <code>(dirX, dirY, dirZ)</code>
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
     * This method is equivalent to calling: <code>mul(new Matrix4x3f().lookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invert(), dest)</code>
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
    public Matrix4x3f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix4x3f dest) {
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
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
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
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>dir</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAt(new Vector3f(), new Vector3f(dir).negate(), up).invert()</code>
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
    public Matrix4x3f rotationTowards(Vector3fc dir, Vector3fc up) {
        return rotationTowards(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>(dirX, dirY, dirZ)</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invert()</code>
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
    public Matrix4x3f rotationTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that translates to the given <code>pos</code> and aligns the local <code>-z</code>
     * axis with <code>dir</code>.
     * <p>
     * This method is equivalent to calling: <code>translation(pos).rotateTowards(dir, up)</code>
     * 
     * @see #translation(Vector3fc)
     * @see #rotateTowards(Vector3fc, Vector3fc)
     *
     * @param pos
     *              the position to translate to
     * @param dir
     *              the direction to rotate towards
     * @param up
     *              the up vector
     * @return this
     */
    public Matrix4x3f translationRotateTowards(Vector3fc pos, Vector3fc dir, Vector3fc up) {
        return translationRotateTowards(pos.x(), pos.y(), pos.z(), dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that translates to the given <code>(posX, posY, posZ)</code> and aligns the local <code>-z</code>
     * axis with <code>(dirX, dirY, dirZ)</code>.
     * <p>
     * This method is equivalent to calling: <code>translation(posX, posY, posZ).rotateTowards(dirX, dirY, dirZ, upX, upY, upZ)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotateTowards(float, float, float, float, float, float)
     * 
     * @param posX
     *              the x-coordinate of the position to translate to
     * @param posY
     *              the y-coordinate of the position to translate to
     * @param posZ
     *              the z-coordinate of the position to translate to
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
    public Matrix4x3f translationRotateTowards(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
        this.m30 = posX;
        this.m31 = posY;
        this.m32 = posZ;
        properties = PROPERTY_ORTHONORMAL;
        return this;
    }

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
     * 1 0 a 0
     * 0 1 b 0
     * 0 0 1 0
     * </pre>
     * 
     * @param a
     *            the value for the z factor that applies to x
     * @param b
     *            the value for the z factor that applies to y
     * @return this
     */
    public Matrix4x3f obliqueZ(float a, float b) {
        this.m20 = m00 * a + m10 * b + m20;
        this.m21 = m01 * a + m11 * b + m21;
        this.m22 = m02 * a + m12 * b + m22;
        this.properties = 0;
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
     * 1 0 a 0
     * 0 1 b 0
     * 0 0 1 0
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
    public Matrix4x3f obliqueZ(float a, float b, Matrix4x3f dest) {
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m20 = m00 * a + m10 * b + m20;
        dest.m21 = m01 * a + m11 * b + m21;
        dest.m22 = m02 * a + m12 * b + m22;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.properties = 0;
        return dest;
    }

    /**
     * Apply a transformation to this matrix to ensure that the local Y axis (as obtained by {@link #positiveY(Vector3f)})
     * will be coplanar to the plane spanned by the local Z axis (as obtained by {@link #positiveZ(Vector3f)}) and the
     * given vector <code>up</code>.
     * <p>
     * This effectively ensures that the resulting matrix will be equal to the one obtained from 
     * {@link #setLookAt(Vector3fc, Vector3fc, Vector3fc)} called with the current 
     * local origin of this matrix (as obtained by {@link #origin(Vector3f)}), the sum of this position and the 
     * negated local Z axis as well as the given vector <code>up</code>.
     * 
     * @param up
     *            the up vector
     * @return this
     */
    public Matrix4x3f withLookAtUp(Vector3fc up) {
        return withLookAtUp(up.x(), up.y(), up.z(), this);
    }

    public Matrix4x3f withLookAtUp(Vector3fc up, Matrix4x3f dest) {
        return withLookAtUp(up.x(), up.y(), up.z());
    }

    /**
     * Apply a transformation to this matrix to ensure that the local Y axis (as obtained by {@link #positiveY(Vector3f)})
     * will be coplanar to the plane spanned by the local Z axis (as obtained by {@link #positiveZ(Vector3f)}) and the
     * given vector <code>(upX, upY, upZ)</code>.
     * <p>
     * This effectively ensures that the resulting matrix will be equal to the one obtained from 
     * {@link #setLookAt(float, float, float, float, float, float, float, float, float)} called with the current 
     * local origin of this matrix (as obtained by {@link #origin(Vector3f)}), the sum of this position and the 
     * negated local Z axis as well as the given vector <code>(upX, upY, upZ)</code>.
     * 
     * @param upX
     *            the x coordinate of the up vector
     * @param upY
     *            the y coordinate of the up vector
     * @param upZ
     *            the z coordinate of the up vector
     * @return this
     */
    public Matrix4x3f withLookAtUp(float upX, float upY, float upZ) {
        return withLookAtUp(upX, upY, upZ, this);
    }

    public Matrix4x3f withLookAtUp(float upX, float upY, float upZ, Matrix4x3f dest) {
        float y = (upY * m21 - upZ * m11) * m02 +
                  (upZ * m01 - upX * m21) * m12 +
                  (upX * m11 - upY * m01) * m22;
        float x = upX * m01 + upY * m11 + upZ * m21;
        if ((properties & PROPERTY_ORTHONORMAL) == 0)
            x *= Math.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
        float invsqrt = Math.invsqrt(y * y + x * x);
        float c = x * invsqrt, s = y * invsqrt;
        float nm00 = c * m00 - s * m01, nm10 = c * m10 - s * m11, nm20 = c * m20 - s * m21, nm31 = s * m30 + c * m31;
        float nm01 = s * m00 + c * m01, nm11 = s * m10 + c * m11, nm21 = s * m20 + c * m21, nm30 = c * m30 - s * m31;
        dest
        ._m00(nm00)._m10(nm10)._m20(nm20)._m30(nm30)
        ._m01(nm01)._m11(nm11)._m21(nm21)._m31(nm31);
        if (dest != this) {
            dest
            ._m02(m02)._m12(m12)._m22(m22)._m32(m32);
        }
        dest.properties = properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(m00) && Math.isFinite(m01) && Math.isFinite(m02) &&
               Math.isFinite(m10) && Math.isFinite(m11) && Math.isFinite(m12) &&
               Math.isFinite(m20) && Math.isFinite(m21) && Math.isFinite(m22) &&
               Math.isFinite(m30) && Math.isFinite(m31) && Math.isFinite(m32);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
