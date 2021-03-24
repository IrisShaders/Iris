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
 * Contains the definition of a 4x4 matrix of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10  m20  m30<br>
 *      m01  m11  m21  m31<br>
 *      m02  m12  m22  m32<br>
 *      m03  m13  m23  m33<br>
 * 
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Matrix4f implements Externalizable, Cloneable, Matrix4fc {

    private static final long serialVersionUID = 1L;

    float m00, m01, m02, m03;
    float m10, m11, m12, m13;
    float m20, m21, m22, m23;
    float m30, m31, m32, m33;

    int properties;

    /**
     * Create a new {@link Matrix4f} and set it to {@link #identity() identity}.
     */
    public Matrix4f() {
        this._m00(1.0f)
            ._m11(1.0f)
            ._m22(1.0f)
            ._m33(1.0f)
            ._properties(PROPERTY_IDENTITY | PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
    }

    /**
     * Create a new {@link Matrix4f} by setting its uppper left 3x3 submatrix to the values of the given {@link Matrix3fc}
     * and the rest to identity.
     * 
     * @param mat
     *          the {@link Matrix3fc}
     */
    public Matrix4f(Matrix3fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix4f} and make it a copy of the given matrix.
     * 
     * @param mat
     *          the {@link Matrix4fc} to copy the values from
     */
    public Matrix4f(Matrix4fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix4f} and set its upper 4x3 submatrix to the given matrix <code>mat</code>
     * and all other elements to identity.
     * 
     * @param mat
     *          the {@link Matrix4x3fc} to copy the values from
     */
    public Matrix4f(Matrix4x3fc mat) {
        set(mat);
    }

    /**
     * Create a new {@link Matrix4f} and make it a copy of the given matrix.
     * <p>
     * Note that due to the given {@link Matrix4dc} storing values in double-precision and the constructed {@link Matrix4f} storing them
     * in single-precision, there is the possibility of losing precision.
     * 
     * @param mat
     *          the {@link Matrix4dc} to copy the values from
     */
    public Matrix4f(Matrix4dc mat) {
        set(mat);
    }

    /**
     * Create a new 4x4 matrix using the supplied float values.
     * <p>
     * The matrix layout will be:<br><br>
     *   m00, m10, m20, m30<br>
     *   m01, m11, m21, m31<br>
     *   m02, m12, m22, m32<br>
     *   m03, m13, m23, m33
     * 
     * @param m00
     *          the value of m00
     * @param m01
     *          the value of m01
     * @param m02
     *          the value of m02
     * @param m03
     *          the value of m03
     * @param m10
     *          the value of m10
     * @param m11
     *          the value of m11
     * @param m12
     *          the value of m12
     * @param m13
     *          the value of m13
     * @param m20
     *          the value of m20
     * @param m21
     *          the value of m21
     * @param m22
     *          the value of m22
     * @param m23
     *          the value of m23
     * @param m30
     *          the value of m30
     * @param m31
     *          the value of m31
     * @param m32
     *          the value of m32
     * @param m33
     *          the value of m33
     */
    public Matrix4f(float m00, float m01, float m02, float m03, 
                    float m10, float m11, float m12, float m13, 
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        this._m00(m00)
            ._m01(m01)
            ._m02(m02)
            ._m03(m03)
            ._m10(m10)
            ._m11(m11)
            ._m12(m12)
            ._m13(m13)
            ._m20(m20)
            ._m21(m21)
            ._m22(m22)
            ._m23(m23)
            ._m30(m30)
            ._m31(m31)
            ._m32(m32)
            ._m33(m33)
            .determineProperties();
    }


    /**
     * Create a new {@link Matrix4f} by reading its 16 float components from the given {@link FloatBuffer}
     * at the buffer's current position.
     * <p>
     * That FloatBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     * 
     * @param buffer
     *          the {@link FloatBuffer} to read the matrix values from
     */
    public Matrix4f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        determineProperties();
    }


    /**
     * Create a new {@link Matrix4f} and initialize its four columns using the supplied vectors.
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
    public Matrix4f(Vector4fc col0, Vector4fc col1, Vector4fc col2, Vector4fc col3) {
        set(col0, col1, col2, col3);
    }

    Matrix4f _properties(int properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Assume the given properties about this matrix.
     * <p>
     * Use one or multiple of 0, {@link Matrix4fc#PROPERTY_IDENTITY},
     * {@link Matrix4fc#PROPERTY_TRANSLATION}, {@link Matrix4fc#PROPERTY_AFFINE},
     * {@link Matrix4fc#PROPERTY_PERSPECTIVE}, {@link Matrix4fc#PROPERTY_ORTHONORMAL}.
     * 
     * @param properties
     *          bitset of the properties to assume about this matrix
     * @return this
     */
    public Matrix4f assume(int properties) {
        this._properties(properties);
        return this;
    }

    /**
     * Compute and set the matrix properties returned by {@link #properties()} based
     * on the current matrix element values.
     * 
     * @return this
     */
    public Matrix4f determineProperties() {
        int properties = 0;
        if (m03 == 0.0f && m13 == 0.0f) {
            if (m23 == 0.0f && m33 == 1.0f) {
                properties |= PROPERTY_AFFINE;
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
            } else if (m01 == 0.0f && m02 == 0.0f && m10 == 0.0f && m12 == 0.0f && m20 == 0.0f && m21 == 0.0f
                    && m30 == 0.0f && m31 == 0.0f && m33 == 0.0f) {
                properties |= PROPERTY_PERSPECTIVE;
            }
        }
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
    public float m03() {
        return m03;
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
    public float m13() {
        return m13;
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
    public float m23() {
        return m23;
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
    public float m33() {
        return m33;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    public Matrix4f m00(float m00) {
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
    public Matrix4f m01(float m01) {
        this.m01 = m01;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m01 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 2.
     * 
     * @param m02
     *          the new value
     * @return this
     */
    public Matrix4f m02(float m02) {
        this.m02 = m02;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m02 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 3.
     * 
     * @param m03
     *          the new value
     * @return this
     */
    public Matrix4f m03(float m03) {
        this.m03 = m03;
        if (m03 != 0.0f)
            properties = 0;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    public Matrix4f m10(float m10) {
        this.m10 = m10;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m10 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 1.
     * 
     * @param m11
     *          the new value
     * @return this
     */
    public Matrix4f m11(float m11) {
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
    public Matrix4f m12(float m12) {
        this.m12 = m12;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m12 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 3.
     * 
     * @param m13
     *          the new value
     * @return this
     */
    public Matrix4f m13(float m13) {
        this.m13 = m13;
        if (m13 != 0.0f)
            properties = 0;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    public Matrix4f m20(float m20) {
        this.m20 = m20;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m20 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 1.
     * 
     * @param m21
     *          the new value
     * @return this
     */
    public Matrix4f m21(float m21) {
        this.m21 = m21;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m21 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 2.
     * 
     * @param m22
     *          the new value
     * @return this
     */
    public Matrix4f m22(float m22) {
        this.m22 = m22;
        properties &= ~PROPERTY_ORTHONORMAL;
        if (m22 != 1.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION);
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 3.
     * 
     * @param m23
     *          the new value
     * @return this
     */
    public Matrix4f m23(float m23) {
        this.m23 = m23;
        if (m23 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 0.
     * 
     * @param m30
     *          the new value
     * @return this
     */
    public Matrix4f m30(float m30) {
        this.m30 = m30;
        if (m30 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE);
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 1.
     * 
     * @param m31
     *          the new value
     * @return this
     */
    public Matrix4f m31(float m31) {
        this.m31 = m31;
        if (m31 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE);
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 2.
     * 
     * @param m32
     *          the new value
     * @return this
     */
    public Matrix4f m32(float m32) {
        this.m32 = m32;
        if (m32 != 0.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE);
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 3.
     * 
     * @param m33
     *          the new value
     * @return this
     */
    public Matrix4f m33(float m33) {
        this.m33 = m33;
        if (m33 != 0.0f)
            properties &= ~(PROPERTY_PERSPECTIVE);
        if (m33 != 1.0f)
            properties &= ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL | PROPERTY_AFFINE);
        return this;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0 without updating the properties of the matrix.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    Matrix4f _m00(float m00) {
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
    Matrix4f _m01(float m01) {
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
    Matrix4f _m02(float m02) {
        this.m02 = m02;
        return this;
    }
    /**
     * Set the value of the matrix element at column 0 and row 3 without updating the properties of the matrix.
     * 
     * @param m03
     *          the new value
     * @return this
     */
    Matrix4f _m03(float m03) {
        this.m03 = m03;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0 without updating the properties of the matrix.
     * 
     * @param m10
     *          the new value
     * @return this
     */
    Matrix4f _m10(float m10) {
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
    Matrix4f _m11(float m11) {
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
    Matrix4f _m12(float m12) {
        this.m12 = m12;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 3 without updating the properties of the matrix.
     * 
     * @param m13
     *          the new value
     * @return this
     */
    Matrix4f _m13(float m13) {
        this.m13 = m13;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0 without updating the properties of the matrix.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    Matrix4f _m20(float m20) {
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
    Matrix4f _m21(float m21) {
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
    Matrix4f _m22(float m22) {
        this.m22 = m22;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 3 without updating the properties of the matrix.
     * 
     * @param m23
     *          the new value
     * @return this
     */
    Matrix4f _m23(float m23) {
        this.m23 = m23;
        return this;
    }
    /**
     * Set the value of the matrix element at column 3 and row 0 without updating the properties of the matrix.
     * 
     * @param m30
     *          the new value
     * @return this
     */
    Matrix4f _m30(float m30) {
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
    Matrix4f _m31(float m31) {
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
    Matrix4f _m32(float m32) {
        this.m32 = m32;
        return this;
    }

    /**
     * Set the value of the matrix element at column 3 and row 3 without updating the properties of the matrix.
     * 
     * @param m33
     *          the new value
     * @return this
     */
    Matrix4f _m33(float m33) {
        this.m33 = m33;
        return this;
    }

    /**
     * Reset this matrix to the identity.
     * <p>
     * Please note that if a call to {@link #identity()} is immediately followed by a call to:
     * {@link #translate(float, float, float) translate}, 
     * {@link #rotate(float, float, float, float) rotate},
     * {@link #scale(float, float, float) scale},
     * {@link #perspective(float, float, float, float) perspective},
     * {@link #frustum(float, float, float, float, float, float) frustum},
     * {@link #ortho(float, float, float, float, float, float) ortho},
     * {@link #ortho2D(float, float, float, float) ortho2D},
     * {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt},
     * {@link #lookAlong(float, float, float, float, float, float) lookAlong},
     * or any of their overloads, then the call to {@link #identity()} can be omitted and the subsequent call replaced with:
     * {@link #translation(float, float, float) translation},
     * {@link #rotation(float, float, float, float) rotation},
     * {@link #scaling(float, float, float) scaling},
     * {@link #setPerspective(float, float, float, float) setPerspective},
     * {@link #setFrustum(float, float, float, float, float, float) setFrustum},
     * {@link #setOrtho(float, float, float, float, float, float) setOrtho},
     * {@link #setOrtho2D(float, float, float, float) setOrtho2D},
     * {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt},
     * {@link #setLookAlong(float, float, float, float, float, float) setLookAlong},
     * or any of their overloads.
     * 
     * @return this
     */
    public Matrix4f identity() {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return this;
        return
        _m00(1.0f).
        _m01(0.0f).
        _m02(0.0f).
        _m03(0.0f).
        _m10(0.0f).
        _m11(1.0f).
        _m12(0.0f).
        _m13(0.0f).
        _m20(0.0f).
        _m21(0.0f).
        _m22(1.0f).
        _m23(0.0f).
        _m30(0.0f).
        _m31(0.0f).
        _m32(0.0f).
        _m33(1.0f).
        _properties(PROPERTY_IDENTITY | PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
    }

    /**
     * Store the values of the given matrix <code>m</code> into <code>this</code> matrix.
     * 
     * @see #Matrix4f(Matrix4fc)
     * @see #get(Matrix4f)
     * 
     * @param m
     *          the matrix to copy the values from
     * @return this
     */
    public Matrix4f set(Matrix4fc m) {
        return
        _m00(m.m00()).
        _m01(m.m01()).
        _m02(m.m02()).
        _m03(m.m03()).
        _m10(m.m10()).
        _m11(m.m11()).
        _m12(m.m12()).
        _m13(m.m13()).
        _m20(m.m20()).
        _m21(m.m21()).
        _m22(m.m22()).
        _m23(m.m23()).
        _m30(m.m30()).
        _m31(m.m31()).
        _m32(m.m32()).
        _m33(m.m33()).
        _properties(m.properties());
    }

    /**
     * Store the values of the transpose of the given matrix <code>m</code> into <code>this</code> matrix.
     * 
     * @param m
     *          the matrix to copy the transposed values from
     * @return this
     */
    public Matrix4f setTransposed(Matrix4fc m) {
        if ((m.properties() & PROPERTY_IDENTITY) != 0)
            return this.identity();
        return setTransposedInternal(m);
    }
    private Matrix4f setTransposedInternal(Matrix4fc m) {
        float nm10 = m.m01(), nm12 = m.m21(), nm13 = m.m31();
        float nm20 = m.m02(), nm21 = m.m12(), nm30 = m.m03();
        float nm31 = m.m13(), nm32 = m.m23();
        return this
        ._m00(m.m00())._m01(m.m10())._m02(m.m20())._m03(m.m30())
        ._m10(nm10)._m11(m.m11())._m12(nm12)._m13(nm13)
        ._m20(nm20)._m21(nm21)._m22(m.m22())._m23(m.m32())
        ._m30(nm30)._m31(nm31)._m32(nm32)._m33(m.m33())
        ._properties(m.properties() & PROPERTY_IDENTITY);
    }

    /**
     * Store the values of the given matrix <code>m</code> into <code>this</code> matrix
     * and set the other matrix elements to identity.
     * 
     * @see #Matrix4f(Matrix4x3fc)
     * 
     * @param m
     *          the matrix to copy the values from
     * @return this
     */
    public Matrix4f set(Matrix4x3fc m) {
        return
        _m00(m.m00()).
        _m01(m.m01()).
        _m02(m.m02()).
        _m03(0.0f).
        _m10(m.m10()).
        _m11(m.m11()).
        _m12(m.m12()).
        _m13(0.0f).
        _m20(m.m20()).
        _m21(m.m21()).
        _m22(m.m22()).
        _m23(0.0f).
        _m30(m.m30()).
        _m31(m.m31()).
        _m32(m.m32()).
        _m33(1.0f).
        _properties(m.properties() | PROPERTY_AFFINE);
    }

    /**
     * Store the values of the given matrix <code>m</code> into <code>this</code> matrix.
     * <p>
     * Note that due to the given matrix <code>m</code> storing values in double-precision and <code>this</code> matrix storing
     * them in single-precision, there is the possibility to lose precision.
     * 
     * @see #Matrix4f(Matrix4dc)
     * @see #get(Matrix4d)
     * 
     * @param m
     *          the matrix to copy the values from
     * @return this
     */
    public Matrix4f set(Matrix4dc m) {
        return this
        ._m00((float) m.m00())
        ._m01((float) m.m01())
        ._m02((float) m.m02())
        ._m03((float) m.m03())
        ._m10((float) m.m10())
        ._m11((float) m.m11())
        ._m12((float) m.m12())
        ._m13((float) m.m13())
        ._m20((float) m.m20())
        ._m21((float) m.m21())
        ._m22((float) m.m22())
        ._m23((float) m.m23())
        ._m30((float) m.m30())
        ._m31((float) m.m31())
        ._m32((float) m.m32())
        ._m33((float) m.m33())
        ._properties(m.properties());
    }

    /**
     * Set the upper left 3x3 submatrix of this {@link Matrix4f} to the given {@link Matrix3fc} 
     * and the rest to identity.
     * 
     * @see #Matrix4f(Matrix3fc)
     * 
     * @param mat
     *          the {@link Matrix3fc}
     * @return this
     */
    public Matrix4f set(Matrix3fc mat) {
        return this
        ._m00(mat.m00())
        ._m01(mat.m01())
        ._m02(mat.m02())
        ._m03(0.0f)
        ._m10(mat.m10())
        ._m11(mat.m11())
        ._m12(mat.m12())
        ._m13(0.0f)
        ._m20(mat.m20())
        ._m21(mat.m21())
        ._m22(mat.m22())
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f).
        _properties(PROPERTY_AFFINE);
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4f}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f}
     * @return this
     */
    public Matrix4f set(AxisAngle4f axisAngle) {
        float x = axisAngle.x;
        float y = axisAngle.y;
        float z = axisAngle.z;
        float angle = axisAngle.angle;
        double n = Math.sqrt(x*x + y*y + z*z);
        n = 1/n;
        x *= n;
        y *= n;
        z *= n;
        float s = Math.sin(angle);
        float c = Math.cosFromSin(s, angle);
        float omc = 1.0f - c;
        this._m00((float)(c + x*x*omc))
            ._m11((float)(c + y*y*omc))
            ._m22((float)(c + z*z*omc));
        float tmp1 = x*y*omc;
        float tmp2 = z*s;
        this._m10((float)(tmp1 - tmp2))
            ._m01((float)(tmp1 + tmp2));
        tmp1 = x*z*omc;
        tmp2 = y*s;
        this._m20((float)(tmp1 + tmp2))
            ._m02((float)(tmp1 - tmp2));
        tmp1 = y*z*omc;
        tmp2 = x*s;
        return this
        ._m21((float)(tmp1 - tmp2))
        ._m12((float)(tmp1 + tmp2))
        ._m03(0.0f)
        ._m13(0.0f)
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link AxisAngle4d}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d}
     * @return this
     */
    public Matrix4f set(AxisAngle4d axisAngle) {
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
        this._m00((float)(c + x*x*omc))
            ._m11((float)(c + y*y*omc))
            ._m22((float)(c + z*z*omc));
        double tmp1 = x*y*omc;
        double tmp2 = z*s;
        this._m10((float)(tmp1 - tmp2))
            ._m01((float)(tmp1 + tmp2));
        tmp1 = x*z*omc;
        tmp2 = y*s;
        this._m20((float)(tmp1 + tmp2))
            ._m02((float)(tmp1 - tmp2));
        tmp1 = y*z*omc;
        tmp2 = x*s;
        return this
        ._m21((float)(tmp1 - tmp2))
        ._m12((float)(tmp1 + tmp2))
        ._m03(0.0f)
        ._m13(0.0f)
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link Quaternionfc}.
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
    public Matrix4f set(Quaternionfc q) {
        return rotation(q);
    }

    /**
     * Set this matrix to be equivalent to the rotation specified by the given {@link Quaterniondc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/">http://www.euclideanspace.com/</a>
     * 
     * @param q
     *          the {@link Quaterniondc}
     * @return this
     */
    public Matrix4f set(Quaterniondc q) {
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
        return
        _m00((float) (w2 + x2 - z2 - y2)).
        _m01((float) (xy + zw + zw + xy)).
        _m02((float) (xz - yw + xz - yw)).
        _m03(0.0f).
        _m10((float) (-zw + xy - zw + xy)).
        _m11((float) (y2 - z2 + w2 - x2)).
        _m12((float) (yz + yz + xw + xw)).
        _m13(0.0f).
        _m20((float) (yw + xz + xz + yw)).
        _m21((float) (yz + yz - xw - xw)).
        _m22((float) (z2 - y2 - x2 + w2)).
        _m30(0.0f).
        _m31(0.0f).
        _m32(0.0f).
        _m33(1.0f).
        _properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set the upper left 3x3 submatrix of this {@link Matrix4f} to that of the given {@link Matrix4f} 
     * and don't change the other elements.
     * 
     * @param mat
     *          the {@link Matrix4f}
     * @return this
     */
    public Matrix4f set3x3(Matrix4f mat) {
        MemUtil.INSTANCE.copy3x3(mat, this);
        return _properties(properties & mat.properties & ~(PROPERTY_PERSPECTIVE));
    }


    /**
     * Set the upper 4x3 submatrix of this {@link Matrix4f} to the given {@link Matrix4x3fc} 
     * and don't change the other elements.
     * 
     * @see Matrix4x3f#get(Matrix4f)
     * 
     * @param mat
     *          the {@link Matrix4x3fc}
     * @return this
     */
    public Matrix4f set4x3(Matrix4x3fc mat) {
        return
        _m00(mat.m00()).
        _m01(mat.m01()).
        _m02(mat.m02()).
        _m10(mat.m10()).
        _m11(mat.m11()).
        _m12(mat.m12()).
        _m20(mat.m20()).
        _m21(mat.m21()).
        _m22(mat.m22()).
        _m30(mat.m30()).
        _m31(mat.m31()).
        _m32(mat.m32()).
        _properties(properties & mat.properties() & ~(PROPERTY_PERSPECTIVE));
    }

    /**
     * Set the upper 4x3 submatrix of this {@link Matrix4f} to the upper 4x3 submatrix of the given {@link Matrix4f} 
     * and don't change the other elements.
     * 
     * @param mat
     *          the {@link Matrix4f}
     * @return this
     */
    public Matrix4f set4x3(Matrix4f mat) {
        MemUtil.INSTANCE.copy4x3(mat, this);
        return _properties(properties & mat.properties & ~(PROPERTY_PERSPECTIVE));
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
    public Matrix4f mul(Matrix4fc right) {
       return mul(right, this);
    }

    public Matrix4f mul(Matrix4fc right, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(right);
        else if ((right.properties() & PROPERTY_IDENTITY) != 0)
            return dest.set(this);
        else if ((properties & PROPERTY_TRANSLATION) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulTranslationAffine(right, dest);
        else if ((properties & PROPERTY_AFFINE) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulAffine(right, dest);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulPerspectiveAffine(right, dest);
        else if ((right.properties() & PROPERTY_AFFINE) != 0)
            return mulAffineR(right, dest);
        return mul0(right, dest);
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     * <p>
     * This method neither assumes nor checks for any matrix properties of <code>this</code> or <code>right</code>
     * and will always perform a complete 4x4 matrix multiplication. This method should only be used whenever the
     * multiplied matrices do not have any properties for which there are optimized multiplication methods available.
     * 
     * @param right
     *          the right operand of the matrix multiplication
     * @return this
     */
    public Matrix4f mul0(Matrix4fc right) {
       return mul0(right, this);
    }

    public Matrix4f mul0(Matrix4fc right, Matrix4f dest) {
        float nm00 = Math.fma(m00, right.m00(), Math.fma(m10, right.m01(), Math.fma(m20, right.m02(), m30 * right.m03())));
        float nm01 = Math.fma(m01, right.m00(), Math.fma(m11, right.m01(), Math.fma(m21, right.m02(), m31 * right.m03())));
        float nm02 = Math.fma(m02, right.m00(), Math.fma(m12, right.m01(), Math.fma(m22, right.m02(), m32 * right.m03())));
        float nm03 = Math.fma(m03, right.m00(), Math.fma(m13, right.m01(), Math.fma(m23, right.m02(), m33 * right.m03())));
        float nm10 = Math.fma(m00, right.m10(), Math.fma(m10, right.m11(), Math.fma(m20, right.m12(), m30 * right.m13())));
        float nm11 = Math.fma(m01, right.m10(), Math.fma(m11, right.m11(), Math.fma(m21, right.m12(), m31 * right.m13())));
        float nm12 = Math.fma(m02, right.m10(), Math.fma(m12, right.m11(), Math.fma(m22, right.m12(), m32 * right.m13())));
        float nm13 = Math.fma(m03, right.m10(), Math.fma(m13, right.m11(), Math.fma(m23, right.m12(), m33 * right.m13())));
        float nm20 = Math.fma(m00, right.m20(), Math.fma(m10, right.m21(), Math.fma(m20, right.m22(), m30 * right.m23())));
        float nm21 = Math.fma(m01, right.m20(), Math.fma(m11, right.m21(), Math.fma(m21, right.m22(), m31 * right.m23())));
        float nm22 = Math.fma(m02, right.m20(), Math.fma(m12, right.m21(), Math.fma(m22, right.m22(), m32 * right.m23())));
        float nm23 = Math.fma(m03, right.m20(), Math.fma(m13, right.m21(), Math.fma(m23, right.m22(), m33 * right.m23())));
        float nm30 = Math.fma(m00, right.m30(), Math.fma(m10, right.m31(), Math.fma(m20, right.m32(), m30 * right.m33())));
        float nm31 = Math.fma(m01, right.m30(), Math.fma(m11, right.m31(), Math.fma(m21, right.m32(), m31 * right.m33())));
        float nm32 = Math.fma(m02, right.m30(), Math.fma(m12, right.m31(), Math.fma(m22, right.m32(), m32 * right.m33())));
        float nm33 = Math.fma(m03, right.m30(), Math.fma(m13, right.m31(), Math.fma(m23, right.m32(), m33 * right.m33())));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
    }

    /**
     * Multiply this matrix by the matrix with the supplied elements.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix whose 
     * elements are supplied via the parameters, then the new matrix will be <code>M * R</code>.
     * So when transforming a vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     *
     * @param r00
     *          the m00 element of the right matrix
     * @param r01
     *          the m01 element of the right matrix
     * @param r02
     *          the m02 element of the right matrix
     * @param r03
     *          the m03 element of the right matrix
     * @param r10
     *          the m10 element of the right matrix
     * @param r11
     *          the m11 element of the right matrix
     * @param r12
     *          the m12 element of the right matrix
     * @param r13
     *          the m13 element of the right matrix
     * @param r20
     *          the m20 element of the right matrix
     * @param r21
     *          the m21 element of the right matrix
     * @param r22
     *          the m22 element of the right matrix
     * @param r23
     *          the m23 element of the right matrix
     * @param r30
     *          the m30 element of the right matrix
     * @param r31
     *          the m31 element of the right matrix
     * @param r32
     *          the m32 element of the right matrix
     * @param r33
     *          the m33 element of the right matrix
     * @return this
     */
    public Matrix4f mul(
            float r00, float r01, float r02, float r03,
            float r10, float r11, float r12, float r13,
            float r20, float r21, float r22, float r23,
            float r30, float r31, float r32, float r33) {
        return mul(r00, r01, r02, r03, r10, r11, r12, r13, r20, r21, r22, r23, r30, r31, r32, r33, this);
    }

    public Matrix4f mul(
            float r00, float r01, float r02, float r03,
            float r10, float r11, float r12, float r13,
            float r20, float r21, float r22, float r23,
            float r30, float r31, float r32, float r33, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(r00, r01, r02, r03, r10, r11, r12, r13, r20, r21, r22, r23, r30, r31, r32, r33);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return mulAffineL(r00, r01, r02, r03, r10, r11, r12, r13, r20, r21, r22, r23, r30, r31, r32, r33, dest);
        return mulGeneric(r00, r01, r02, r03, r10, r11, r12, r13, r20, r21, r22, r23, r30, r31, r32, r33, dest);
    }
    private Matrix4f mulAffineL(
            float r00, float r01, float r02, float r03,
            float r10, float r11, float r12, float r13,
            float r20, float r21, float r22, float r23,
            float r30, float r31, float r32, float r33, Matrix4f dest) {
        float nm00 = Math.fma(m00, r00, Math.fma(m10, r01, Math.fma(m20, r02, m30 * r03)));
        float nm01 = Math.fma(m01, r00, Math.fma(m11, r01, Math.fma(m21, r02, m31 * r03)));
        float nm02 = Math.fma(m02, r00, Math.fma(m12, r01, Math.fma(m22, r02, m32 * r03)));
        float nm03 = r03;
        float nm10 = Math.fma(m00, r10, Math.fma(m10, r11, Math.fma(m20, r12, m30 * r13)));
        float nm11 = Math.fma(m01, r10, Math.fma(m11, r11, Math.fma(m21, r12, m31 * r13)));
        float nm12 = Math.fma(m02, r10, Math.fma(m12, r11, Math.fma(m22, r12, m32 * r13)));
        float nm13 = r13;
        float nm20 = Math.fma(m00, r20, Math.fma(m10, r21, Math.fma(m20, r22, m30 * r23)));
        float nm21 = Math.fma(m01, r20, Math.fma(m11, r21, Math.fma(m21, r22, m31 * r23)));
        float nm22 = Math.fma(m02, r20, Math.fma(m12, r21, Math.fma(m22, r22, m32 * r23)));
        float nm23 = r23;
        float nm30 = Math.fma(m00, r30, Math.fma(m10, r31, Math.fma(m20, r32, m30 * r33)));
        float nm31 = Math.fma(m01, r30, Math.fma(m11, r31, Math.fma(m21, r32, m31 * r33)));
        float nm32 = Math.fma(m02, r30, Math.fma(m12, r31, Math.fma(m22, r32, m32 * r33)));
        float nm33 = r33;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(PROPERTY_AFFINE);
    }
    private Matrix4f mulGeneric(
            float r00, float r01, float r02, float r03,
            float r10, float r11, float r12, float r13,
            float r20, float r21, float r22, float r23,
            float r30, float r31, float r32, float r33, Matrix4f dest) {
        float nm00 = Math.fma(m00, r00, Math.fma(m10, r01, Math.fma(m20, r02, m30 * r03)));
        float nm01 = Math.fma(m01, r00, Math.fma(m11, r01, Math.fma(m21, r02, m31 * r03)));
        float nm02 = Math.fma(m02, r00, Math.fma(m12, r01, Math.fma(m22, r02, m32 * r03)));
        float nm03 = Math.fma(m03, r00, Math.fma(m13, r01, Math.fma(m23, r02, m33 * r03)));
        float nm10 = Math.fma(m00, r10, Math.fma(m10, r11, Math.fma(m20, r12, m30 * r13)));
        float nm11 = Math.fma(m01, r10, Math.fma(m11, r11, Math.fma(m21, r12, m31 * r13)));
        float nm12 = Math.fma(m02, r10, Math.fma(m12, r11, Math.fma(m22, r12, m32 * r13)));
        float nm13 = Math.fma(m03, r10, Math.fma(m13, r11, Math.fma(m23, r12, m33 * r13)));
        float nm20 = Math.fma(m00, r20, Math.fma(m10, r21, Math.fma(m20, r22, m30 * r23)));
        float nm21 = Math.fma(m01, r20, Math.fma(m11, r21, Math.fma(m21, r22, m31 * r23)));
        float nm22 = Math.fma(m02, r20, Math.fma(m12, r21, Math.fma(m22, r22, m32 * r23)));
        float nm23 = Math.fma(m03, r20, Math.fma(m13, r21, Math.fma(m23, r22, m33 * r23)));
        float nm30 = Math.fma(m00, r30, Math.fma(m10, r31, Math.fma(m20, r32, m30 * r33)));
        float nm31 = Math.fma(m01, r30, Math.fma(m11, r31, Math.fma(m21, r32, m31 * r33)));
        float nm32 = Math.fma(m02, r30, Math.fma(m12, r31, Math.fma(m22, r32, m32 * r33)));
        float nm33 = Math.fma(m03, r30, Math.fma(m13, r31, Math.fma(m23, r32, m33 * r33)));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
    }

    /**
     * Multiply this matrix by the 3x3 matrix with the supplied elements expanded to a 4x4 matrix with 
     * all other matrix elements set to identity.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix whose 
     * elements are supplied via the parameters, then the new matrix will be <code>M * R</code>.
     * So when transforming a vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     *
     * @param r00
     *          the m00 element of the right matrix
     * @param r01
     *          the m01 element of the right matrix
     * @param r02
     *          the m02 element of the right matrix
     * @param r10
     *          the m10 element of the right matrix
     * @param r11
     *          the m11 element of the right matrix
     * @param r12
     *          the m12 element of the right matrix
     * @param r20
     *          the m20 element of the right matrix
     * @param r21
     *          the m21 element of the right matrix
     * @param r22
     *          the m22 element of the right matrix
     * @return this
     */
    public Matrix4f mul3x3(
            float r00, float r01, float r02,
            float r10, float r11, float r12,
            float r20, float r21, float r22) {
        return mul3x3(r00, r01, r02, r10, r11, r12, r20, r21, r22, this);
    }
    public Matrix4f mul3x3(
            float r00, float r01, float r02,
            float r10, float r11, float r12,
            float r20, float r21, float r22, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(r00, r01, r02, 0, r10, r11, r12, 0, r20, r21, r22, 0, 0, 0, 0, 1);
        return mulGeneric3x3(r00, r01, r02, r10, r11, r12, r20, r21, r22, dest);
    }
    private Matrix4f mulGeneric3x3(
            float r00, float r01, float r02,
            float r10, float r11, float r12,
            float r20, float r21, float r22, Matrix4f dest) {
        float nm00 = Math.fma(m00, r00, Math.fma(m10, r01, m20 * r02));
        float nm01 = Math.fma(m01, r00, Math.fma(m11, r01, m21 * r02));
        float nm02 = Math.fma(m02, r00, Math.fma(m12, r01, m22 * r02));
        float nm03 = Math.fma(m03, r00, Math.fma(m13, r01, m23 * r02));
        float nm10 = Math.fma(m00, r10, Math.fma(m10, r11, m20 * r12));
        float nm11 = Math.fma(m01, r10, Math.fma(m11, r11, m21 * r12));
        float nm12 = Math.fma(m02, r10, Math.fma(m12, r11, m22 * r12));
        float nm13 = Math.fma(m03, r10, Math.fma(m13, r11, m23 * r12));
        float nm20 = Math.fma(m00, r20, Math.fma(m10, r21, m20 * r22));
        float nm21 = Math.fma(m01, r20, Math.fma(m11, r21, m21 * r22));
        float nm22 = Math.fma(m02, r20, Math.fma(m12, r21, m22 * r22));
        float nm23 = Math.fma(m03, r20, Math.fma(m13, r21, m23 * r22));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(this.properties & PROPERTY_AFFINE);
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
    public Matrix4f mulLocal(Matrix4fc left) {
       return mulLocal(left, this);
    }

    public Matrix4f mulLocal(Matrix4fc left, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(left);
        else if ((left.properties() & PROPERTY_IDENTITY) != 0)
            return dest.set(this);
        else if ((properties & PROPERTY_AFFINE) != 0 && (left.properties() & PROPERTY_AFFINE) != 0)
            return mulLocalAffine(left, dest);
        return mulLocalGeneric(left, dest);
    }
    private Matrix4f mulLocalGeneric(Matrix4fc left, Matrix4f dest) {
        float nm00 = Math.fma(left.m00(), m00, Math.fma(left.m10(), m01, Math.fma(left.m20(), m02, left.m30() * m03)));
        float nm01 = Math.fma(left.m01(), m00, Math.fma(left.m11(), m01, Math.fma(left.m21(), m02, left.m31() * m03)));
        float nm02 = Math.fma(left.m02(), m00, Math.fma(left.m12(), m01, Math.fma(left.m22(), m02, left.m32() * m03)));
        float nm03 = Math.fma(left.m03(), m00, Math.fma(left.m13(), m01, Math.fma(left.m23(), m02, left.m33() * m03)));
        float nm10 = Math.fma(left.m00(), m10, Math.fma(left.m10(), m11, Math.fma(left.m20(), m12, left.m30() * m13)));
        float nm11 = Math.fma(left.m01(), m10, Math.fma(left.m11(), m11, Math.fma(left.m21(), m12, left.m31() * m13)));
        float nm12 = Math.fma(left.m02(), m10, Math.fma(left.m12(), m11, Math.fma(left.m22(), m12, left.m32() * m13)));
        float nm13 = Math.fma(left.m03(), m10, Math.fma(left.m13(), m11, Math.fma(left.m23(), m12, left.m33() * m13)));
        float nm20 = Math.fma(left.m00(), m20, Math.fma(left.m10(), m21, Math.fma(left.m20(), m22, left.m30() * m23)));
        float nm21 = Math.fma(left.m01(), m20, Math.fma(left.m11(), m21, Math.fma(left.m21(), m22, left.m31() * m23)));
        float nm22 = Math.fma(left.m02(), m20, Math.fma(left.m12(), m21, Math.fma(left.m22(), m22, left.m32() * m23)));
        float nm23 = Math.fma(left.m03(), m20, Math.fma(left.m13(), m21, Math.fma(left.m23(), m22, left.m33() * m23)));
        float nm30 = Math.fma(left.m00(), m30, Math.fma(left.m10(), m31, Math.fma(left.m20(), m32, left.m30() * m33)));
        float nm31 = Math.fma(left.m01(), m30, Math.fma(left.m11(), m31, Math.fma(left.m21(), m32, left.m31() * m33)));
        float nm32 = Math.fma(left.m02(), m30, Math.fma(left.m12(), m31, Math.fma(left.m22(), m32, left.m32() * m33)));
        float nm33 = Math.fma(left.m03(), m30, Math.fma(left.m13(), m31, Math.fma(left.m23(), m32, left.m33() * m33)));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
    }

    /**
     * Pre-multiply this matrix by the supplied <code>left</code> matrix, both of which are assumed to be {@link #isAffine() affine}, and store the result in <code>this</code>.
     * <p>
     * This method assumes that <code>this</code> matrix and the given <code>left</code> matrix both represent an {@link #isAffine() affine} transformation
     * (i.e. their last rows are equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrices only represent affine transformations, such as translation, rotation, scaling and shearing (in any combination).
     * <p>
     * This method will not modify either the last row of <code>this</code> or the last row of <code>left</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the <code>left</code> matrix,
     * then the new matrix will be <code>L * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>L * M * v</code>, the
     * transformation of <code>this</code> matrix will be applied first!
     *
     * @param left
     *          the left operand of the matrix multiplication (the last row is assumed to be <code>(0, 0, 0, 1)</code>)
     * @return this
     */
    public Matrix4f mulLocalAffine(Matrix4fc left) {
       return mulLocalAffine(left, this);
    }

    public Matrix4f mulLocalAffine(Matrix4fc left, Matrix4f dest) {
        float nm00 = left.m00() * m00 + left.m10() * m01 + left.m20() * m02;
        float nm01 = left.m01() * m00 + left.m11() * m01 + left.m21() * m02;
        float nm02 = left.m02() * m00 + left.m12() * m01 + left.m22() * m02;
        float nm03 = left.m03();
        float nm10 = left.m00() * m10 + left.m10() * m11 + left.m20() * m12;
        float nm11 = left.m01() * m10 + left.m11() * m11 + left.m21() * m12;
        float nm12 = left.m02() * m10 + left.m12() * m11 + left.m22() * m12;
        float nm13 = left.m13();
        float nm20 = left.m00() * m20 + left.m10() * m21 + left.m20() * m22;
        float nm21 = left.m01() * m20 + left.m11() * m21 + left.m21() * m22;
        float nm22 = left.m02() * m20 + left.m12() * m21 + left.m22() * m22;
        float nm23 = left.m23();
        float nm30 = left.m00() * m30 + left.m10() * m31 + left.m20() * m32 + left.m30();
        float nm31 = left.m01() * m30 + left.m11() * m31 + left.m21() * m32 + left.m31();
        float nm32 = left.m02() * m30 + left.m12() * m31 + left.m22() * m32 + left.m32();
        float nm33 = left.m33();
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(PROPERTY_AFFINE | (this.properties() & left.properties() & PROPERTY_ORTHONORMAL));
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
    public Matrix4f mul(Matrix4x3fc right) {
        return mul(right, this);
    }

    public Matrix4f mul(Matrix4x3fc right, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.set(right);
        else if ((right.properties() & PROPERTY_IDENTITY) != 0)
            return dest.set(this);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return mulTranslation(right, dest);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return mulAffine(right, dest);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0)
            return mulPerspectiveAffine(right, dest);
        return mulGeneric(right, dest);
    }
    private Matrix4f mulTranslation(Matrix4x3fc right, Matrix4f dest) {
        return dest
        ._m00(right.m00())
        ._m01(right.m01())
        ._m02(right.m02())
        ._m03(m03)
        ._m10(right.m10())
        ._m11(right.m11())
        ._m12(right.m12())
        ._m13(m13)
        ._m20(right.m20())
        ._m21(right.m21())
        ._m22(right.m22())
        ._m23(m23)
        ._m30(right.m30() + m30)
        ._m31(right.m31() + m31)
        ._m32(right.m32() + m32)
        ._m33(m33)
        ._properties(PROPERTY_AFFINE | (right.properties() & PROPERTY_ORTHONORMAL));
    }
    private Matrix4f mulAffine(Matrix4x3fc right, Matrix4f dest) {
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
        ._m03(m03)
        ._m10(Math.fma(m00, rm10, Math.fma(m10, rm11, m20 * rm12)))
        ._m11(Math.fma(m01, rm10, Math.fma(m11, rm11, m21 * rm12)))
        ._m12(Math.fma(m02, rm10, Math.fma(m12, rm11, m22 * rm12)))
        ._m13(m13)
        ._m20(Math.fma(m00, rm20, Math.fma(m10, rm21, m20 * rm22)))
        ._m21(Math.fma(m01, rm20, Math.fma(m11, rm21, m21 * rm22)))
        ._m22(Math.fma(m02, rm20, Math.fma(m12, rm21, m22 * rm22)))
        ._m23(m23)
        ._m30(Math.fma(m00, rm30, Math.fma(m10, rm31, Math.fma(m20, rm32, m30))))
        ._m31(Math.fma(m01, rm30, Math.fma(m11, rm31, Math.fma(m21, rm32, m31))))
        ._m32(Math.fma(m02, rm30, Math.fma(m12, rm31, Math.fma(m22, rm32, m32))))
        ._m33(m33)
        ._properties(PROPERTY_AFFINE | (this.properties & right.properties() & PROPERTY_ORTHONORMAL));
    }
    private Matrix4f mulGeneric(Matrix4x3fc right, Matrix4f dest) {
        float nm00 = Math.fma(m00, right.m00(), Math.fma(m10, right.m01(), m20 * right.m02()));
        float nm01 = Math.fma(m01, right.m00(), Math.fma(m11, right.m01(), m21 * right.m02()));
        float nm02 = Math.fma(m02, right.m00(), Math.fma(m12, right.m01(), m22 * right.m02()));
        float nm03 = Math.fma(m03, right.m00(), Math.fma(m13, right.m01(), m23 * right.m02()));
        float nm10 = Math.fma(m00, right.m10(), Math.fma(m10, right.m11(), m20 * right.m12()));
        float nm11 = Math.fma(m01, right.m10(), Math.fma(m11, right.m11(), m21 * right.m12()));
        float nm12 = Math.fma(m02, right.m10(), Math.fma(m12, right.m11(), m22 * right.m12()));
        float nm13 = Math.fma(m03, right.m10(), Math.fma(m13, right.m11(), m23 * right.m12()));
        float nm20 = Math.fma(m00, right.m20(), Math.fma(m10, right.m21(), m20 * right.m22()));
        float nm21 = Math.fma(m01, right.m20(), Math.fma(m11, right.m21(), m21 * right.m22()));
        float nm22 = Math.fma(m02, right.m20(), Math.fma(m12, right.m21(), m22 * right.m22()));
        float nm23 = Math.fma(m03, right.m20(), Math.fma(m13, right.m21(), m23 * right.m22()));
        float nm30 = Math.fma(m00, right.m30(), Math.fma(m10, right.m31(), Math.fma(m20, right.m32(), m30)));
        float nm31 = Math.fma(m01, right.m30(), Math.fma(m11, right.m31(), Math.fma(m21, right.m32(), m31)));
        float nm32 = Math.fma(m02, right.m30(), Math.fma(m12, right.m31(), Math.fma(m22, right.m32(), m32)));
        float nm33 = Math.fma(m03, right.m30(), Math.fma(m13, right.m31(), Math.fma(m23, right.m32(), m33)));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
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
    public Matrix4f mul(Matrix3x2fc right) {
        return mul(right, this);
    }

    public Matrix4f mul(Matrix3x2fc right, Matrix4f dest) {
        float nm00 = m00 * right.m00() + m10 * right.m01();
        float nm01 = m01 * right.m00() + m11 * right.m01();
        float nm02 = m02 * right.m00() + m12 * right.m01();
        float nm03 = m03 * right.m00() + m13 * right.m01();
        float nm10 = m00 * right.m10() + m10 * right.m11();
        float nm11 = m01 * right.m10() + m11 * right.m11();
        float nm12 = m02 * right.m10() + m12 * right.m11();
        float nm13 = m03 * right.m10() + m13 * right.m11();
        float nm30 = m00 * right.m20() + m10 * right.m21() + m30;
        float nm31 = m01 * right.m20() + m11 * right.m21() + m31;
        float nm32 = m02 * right.m20() + m12 * right.m21() + m32;
        float nm33 = m03 * right.m20() + m13 * right.m21() + m33;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(m20)
        ._m21(m21)
        ._m22(m22)
        ._m23(m23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
    }

    /**
     * Multiply <code>this</code> symmetric perspective projection matrix by the supplied {@link #isAffine() affine} <code>view</code> matrix.
     * <p>
     * If <code>P</code> is <code>this</code> matrix and <code>V</code> the <code>view</code> matrix,
     * then the new matrix will be <code>P * V</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>P * V * v</code>, the
     * transformation of the <code>view</code> matrix will be applied first!
     *
     * @param view
     *          the {@link #isAffine() affine} matrix to multiply <code>this</code> symmetric perspective projection matrix by
     * @return this
     */
    public Matrix4f mulPerspectiveAffine(Matrix4fc view) {
       return mulPerspectiveAffine(view, this);
    }

    public Matrix4f mulPerspectiveAffine(Matrix4fc view, Matrix4f dest) {
        float nm00 = m00 * view.m00(), nm01 = m11 * view.m01(), nm02 = m22 * view.m02(), nm03 = m23 * view.m02();
        float nm10 = m00 * view.m10(), nm11 = m11 * view.m11(), nm12 = m22 * view.m12(), nm13 = m23 * view.m12();
        float nm20 = m00 * view.m20(), nm21 = m11 * view.m21(), nm22 = m22 * view.m22(), nm23 = m23 * view.m22();
        float nm30 = m00 * view.m30(), nm31 = m11 * view.m31(), nm32 = m22 * view.m32() + m32, nm33 = m23 * view.m32();
        return dest
            ._m00(nm00)._m01(nm01)._m02(nm02)._m03(nm03)
            ._m10(nm10)._m11(nm11)._m12(nm12)._m13(nm13)
            ._m20(nm20)._m21(nm21)._m22(nm22)._m23(nm23)
            ._m30(nm30)._m31(nm31)._m32(nm32)._m33(nm33)
            ._properties(0);
    }

    /**
     * Multiply <code>this</code> symmetric perspective projection matrix by the supplied <code>view</code> matrix.
     * <p>
     * If <code>P</code> is <code>this</code> matrix and <code>V</code> the <code>view</code> matrix,
     * then the new matrix will be <code>P * V</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>P * V * v</code>, the
     * transformation of the <code>view</code> matrix will be applied first!
     *
     * @param view
     *          the matrix to multiply <code>this</code> symmetric perspective projection matrix by
     * @return this
     */
    public Matrix4f mulPerspectiveAffine(Matrix4x3fc view) {
       return mulPerspectiveAffine(view, this);
    }

    public Matrix4f mulPerspectiveAffine(Matrix4x3fc view, Matrix4f dest) {
        float lm00 = m00, lm11 = m11, lm22 = m22, lm23 = m23;
        return dest.
        _m00(lm00 * view.m00())._m01(lm11 * view.m01())._m02(lm22 * view.m02())._m03(lm23 * view.m02()).
        _m10(lm00 * view.m10())._m11(lm11 * view.m11())._m12(lm22 * view.m12())._m13(lm23 * view.m12()).
        _m20(lm00 * view.m20())._m21(lm11 * view.m21())._m22(lm22 * view.m22())._m23(lm23 * view.m22()).
        _m30(lm00 * view.m30())._m31(lm11 * view.m31())._m32(lm22 * view.m32() + m32)._m33(lm23 * view.m32()).
        _properties(0);
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix, which is assumed to be {@link #isAffine() affine}, and store the result in <code>this</code>.
     * <p>
     * This method assumes that the given <code>right</code> matrix represents an {@link #isAffine() affine} transformation (i.e. its last row is equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrix only represents affine transformations, such as translation, rotation, scaling and shearing (in any combination).
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     *
     * @param right
     *          the right operand of the matrix multiplication (the last row is assumed to be <code>(0, 0, 0, 1)</code>)
     * @return this
     */
    public Matrix4f mulAffineR(Matrix4fc right) {
       return mulAffineR(right, this);
    }

    public Matrix4f mulAffineR(Matrix4fc right, Matrix4f dest) {
        float nm00 = Math.fma(m00, right.m00(), Math.fma(m10, right.m01(), m20 * right.m02()));
        float nm01 = Math.fma(m01, right.m00(), Math.fma(m11, right.m01(), m21 * right.m02()));
        float nm02 = Math.fma(m02, right.m00(), Math.fma(m12, right.m01(), m22 * right.m02()));
        float nm03 = Math.fma(m03, right.m00(), Math.fma(m13, right.m01(), m23 * right.m02()));
        float nm10 = Math.fma(m00, right.m10(), Math.fma(m10, right.m11(), m20 * right.m12()));
        float nm11 = Math.fma(m01, right.m10(), Math.fma(m11, right.m11(), m21 * right.m12()));
        float nm12 = Math.fma(m02, right.m10(), Math.fma(m12, right.m11(), m22 * right.m12()));
        float nm13 = Math.fma(m03, right.m10(), Math.fma(m13, right.m11(), m23 * right.m12()));
        float nm20 = Math.fma(m00, right.m20(), Math.fma(m10, right.m21(), m20 * right.m22()));
        float nm21 = Math.fma(m01, right.m20(), Math.fma(m11, right.m21(), m21 * right.m22()));
        float nm22 = Math.fma(m02, right.m20(), Math.fma(m12, right.m21(), m22 * right.m22()));
        float nm23 = Math.fma(m03, right.m20(), Math.fma(m13, right.m21(), m23 * right.m22()));
        float nm30 = Math.fma(m00, right.m30(), Math.fma(m10, right.m31(), Math.fma(m20, right.m32(), m30)));
        float nm31 = Math.fma(m01, right.m30(), Math.fma(m11, right.m31(), Math.fma(m21, right.m32(), m31)));
        float nm32 = Math.fma(m02, right.m30(), Math.fma(m12, right.m31(), Math.fma(m22, right.m32(), m32)));
        float nm33 = Math.fma(m03, right.m30(), Math.fma(m13, right.m31(), Math.fma(m23, right.m32(), m33)));
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix, both of which are assumed to be {@link #isAffine() affine}, and store the result in <code>this</code>.
     * <p>
     * This method assumes that <code>this</code> matrix and the given <code>right</code> matrix both represent an {@link #isAffine() affine} transformation
     * (i.e. their last rows are equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrices only represent affine transformations, such as translation, rotation, scaling and shearing (in any combination).
     * <p>
     * This method will not modify either the last row of <code>this</code> or the last row of <code>right</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     *
     * @param right
     *          the right operand of the matrix multiplication (the last row is assumed to be <code>(0, 0, 0, 1)</code>)
     * @return this
     */
    public Matrix4f mulAffine(Matrix4fc right) {
       return mulAffine(right, this);
    }

    public Matrix4f mulAffine(Matrix4fc right, Matrix4f dest) {
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
        ._m03(m03)
        ._m10(Math.fma(m00, rm10, Math.fma(m10, rm11, m20 * rm12)))
        ._m11(Math.fma(m01, rm10, Math.fma(m11, rm11, m21 * rm12)))
        ._m12(Math.fma(m02, rm10, Math.fma(m12, rm11, m22 * rm12)))
        ._m13(m13)
        ._m20(Math.fma(m00, rm20, Math.fma(m10, rm21, m20 * rm22)))
        ._m21(Math.fma(m01, rm20, Math.fma(m11, rm21, m21 * rm22)))
        ._m22(Math.fma(m02, rm20, Math.fma(m12, rm21, m22 * rm22)))
        ._m23(m23)
        ._m30(Math.fma(m00, rm30, Math.fma(m10, rm31, Math.fma(m20, rm32, m30))))
        ._m31(Math.fma(m01, rm30, Math.fma(m11, rm31, Math.fma(m21, rm32, m31))))
        ._m32(Math.fma(m02, rm30, Math.fma(m12, rm31, Math.fma(m22, rm32, m32))))
        ._m33(m33)
        ._properties(PROPERTY_AFFINE | (this.properties & right.properties() & PROPERTY_ORTHONORMAL));
    }

    public Matrix4f mulTranslationAffine(Matrix4fc right, Matrix4f dest) {
        return dest
        ._m00(right.m00())
        ._m01(right.m01())
        ._m02(right.m02())
        ._m03(m03)
        ._m10(right.m10())
        ._m11(right.m11())
        ._m12(right.m12())
        ._m13(m13)
        ._m20(right.m20())
        ._m21(right.m21())
        ._m22(right.m22())
        ._m23(m23)
        ._m30(right.m30() + m30)
        ._m31(right.m31() + m31)
        ._m32(right.m32() + m32)
        ._m33(m33)
        ._properties(PROPERTY_AFFINE | (right.properties() & PROPERTY_ORTHONORMAL));
    }

    /**
     * Multiply <code>this</code> orthographic projection matrix by the supplied {@link #isAffine() affine} <code>view</code> matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>V</code> the <code>view</code> matrix,
     * then the new matrix will be <code>M * V</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * V * v</code>, the
     * transformation of the <code>view</code> matrix will be applied first!
     *
     * @param view
     *          the affine matrix which to multiply <code>this</code> with
     * @return this
     */
    public Matrix4f mulOrthoAffine(Matrix4fc view) {
        return mulOrthoAffine(view, this);
    }

    public Matrix4f mulOrthoAffine(Matrix4fc view, Matrix4f dest) {
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
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(0.0f)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(0.0f)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE);
    }

    /**
     * Component-wise add the upper 4x3 submatrices of <code>this</code> and <code>other</code>
     * by first multiplying each component of <code>other</code>'s 4x3 submatrix by <code>otherFactor</code> and
     * adding that result to <code>this</code>.
     * <p>
     * The matrix <code>other</code> will not be changed.
     * 
     * @param other
     *          the other matrix 
     * @param otherFactor
     *          the factor to multiply each of the other matrix's 4x3 components
     * @return this
     */
    public Matrix4f fma4x3(Matrix4fc other, float otherFactor) {
        return fma4x3(other, otherFactor, this);
    }

    public Matrix4f fma4x3(Matrix4fc other, float otherFactor, Matrix4f dest) {
        dest._m00(Math.fma(other.m00(), otherFactor, m00))
            ._m01(Math.fma(other.m01(), otherFactor, m01))
            ._m02(Math.fma(other.m02(), otherFactor, m02))
            ._m03(m03)
            ._m10(Math.fma(other.m10(), otherFactor, m10))
            ._m11(Math.fma(other.m11(), otherFactor, m11))
            ._m12(Math.fma(other.m12(), otherFactor, m12))
            ._m13(m13)
            ._m20(Math.fma(other.m20(), otherFactor, m20))
            ._m21(Math.fma(other.m21(), otherFactor, m21))
            ._m22(Math.fma(other.m22(), otherFactor, m22))
            ._m23(m23)
            ._m30(Math.fma(other.m30(), otherFactor, m30))
            ._m31(Math.fma(other.m31(), otherFactor, m31))
            ._m32(Math.fma(other.m32(), otherFactor, m32))
            ._m33(m33)
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
    public Matrix4f add(Matrix4fc other) {
        return add(other, this);
    }

    public Matrix4f add(Matrix4fc other, Matrix4f dest) {
        dest._m00(m00 + other.m00())
            ._m01(m01 + other.m01())
            ._m02(m02 + other.m02())
            ._m03(m03 + other.m03())
            ._m10(m10 + other.m10())
            ._m11(m11 + other.m11())
            ._m12(m12 + other.m12())
            ._m13(m13 + other.m13())
            ._m20(m20 + other.m20())
            ._m21(m21 + other.m21())
            ._m22(m22 + other.m22())
            ._m23(m23 + other.m23())
            ._m30(m30 + other.m30())
            ._m31(m31 + other.m31())
            ._m32(m32 + other.m32())
            ._m33(m33 + other.m33())
            ._properties(0);
        return dest;
    }

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code>.
     * 
     * @param subtrahend
     *          the subtrahend
     * @return this
     */
    public Matrix4f sub(Matrix4fc subtrahend) {
        return sub(subtrahend, this);
    }

    public Matrix4f sub(Matrix4fc subtrahend, Matrix4f dest) {
        dest._m00(m00 - subtrahend.m00())
            ._m01(m01 - subtrahend.m01())
            ._m02(m02 - subtrahend.m02())
            ._m03(m03 - subtrahend.m03())
            ._m10(m10 - subtrahend.m10())
            ._m11(m11 - subtrahend.m11())
            ._m12(m12 - subtrahend.m12())
            ._m13(m13 - subtrahend.m13())
            ._m20(m20 - subtrahend.m20())
            ._m21(m21 - subtrahend.m21())
            ._m22(m22 - subtrahend.m22())
            ._m23(m23 - subtrahend.m23())
            ._m30(m30 - subtrahend.m30())
            ._m31(m31 - subtrahend.m31())
            ._m32(m32 - subtrahend.m32())
            ._m33(m33 - subtrahend.m33())
            ._properties(0);
        return dest;
    }

    /**
     * Component-wise multiply <code>this</code> by <code>other</code>.
     * 
     * @param other
     *          the other matrix
     * @return this
     */
    public Matrix4f mulComponentWise(Matrix4fc other) {
        return mulComponentWise(other, this);
    }

    public Matrix4f mulComponentWise(Matrix4fc other, Matrix4f dest) {
        dest._m00(m00 * other.m00())
            ._m01(m01 * other.m01())
            ._m02(m02 * other.m02())
            ._m03(m03 * other.m03())
            ._m10(m10 * other.m10())
            ._m11(m11 * other.m11())
            ._m12(m12 * other.m12())
            ._m13(m13 * other.m13())
            ._m20(m20 * other.m20())
            ._m21(m21 * other.m21())
            ._m22(m22 * other.m22())
            ._m23(m23 * other.m23())
            ._m30(m30 * other.m30())
            ._m31(m31 * other.m31())
            ._m32(m32 * other.m32())
            ._m33(m33 * other.m33())
            ._properties(0);
        return dest;
    }

    /**
     * Component-wise add the upper 4x3 submatrices of <code>this</code> and <code>other</code>.
     * 
     * @param other
     *          the other addend 
     * @return this
     */
    public Matrix4f add4x3(Matrix4fc other) {
        return add4x3(other, this);
    }

    public Matrix4f add4x3(Matrix4fc other, Matrix4f dest) {
        dest._m00(m00 + other.m00())
            ._m01(m01 + other.m01())
            ._m02(m02 + other.m02())
            ._m03(m03)
            ._m10(m10 + other.m10())
            ._m11(m11 + other.m11())
            ._m12(m12 + other.m12())
            ._m13(m13)
            ._m20(m20 + other.m20())
            ._m21(m21 + other.m21())
            ._m22(m22 + other.m22())
            ._m23(m23)
            ._m30(m30 + other.m30())
            ._m31(m31 + other.m31())
            ._m32(m32 + other.m32())
            ._m33(m33)
            ._properties(0);
        return dest;
    }

    /**
     * Component-wise subtract the upper 4x3 submatrices of <code>subtrahend</code> from <code>this</code>.
     * 
     * @param subtrahend
     *          the subtrahend
     * @return this
     */
    public Matrix4f sub4x3(Matrix4f subtrahend) {
        return sub4x3(subtrahend, this);
    }

    public Matrix4f sub4x3(Matrix4fc subtrahend, Matrix4f dest) {
        dest._m00(m00 - subtrahend.m00())
            ._m01(m01 - subtrahend.m01())
            ._m02(m02 - subtrahend.m02())
            ._m03(m03)
            ._m10(m10 - subtrahend.m10())
            ._m11(m11 - subtrahend.m11())
            ._m12(m12 - subtrahend.m12())
            ._m13(m13)
            ._m20(m20 - subtrahend.m20())
            ._m21(m21 - subtrahend.m21())
            ._m22(m22 - subtrahend.m22())
            ._m23(m23)
            ._m30(m30 - subtrahend.m30())
            ._m31(m31 - subtrahend.m31())
            ._m32(m32 - subtrahend.m32())
            ._m33(m33)
            ._properties(0);
        return dest;
    }

    /**
     * Component-wise multiply the upper 4x3 submatrices of <code>this</code> by <code>other</code>.
     * 
     * @param other
     *          the other matrix
     * @return this
     */
    public Matrix4f mul4x3ComponentWise(Matrix4fc other) {
        return mul4x3ComponentWise(other, this);
    }

    public Matrix4f mul4x3ComponentWise(Matrix4fc other, Matrix4f dest) {
        dest._m00(m00 * other.m00())
            ._m01(m01 * other.m01())
            ._m02(m02 * other.m02())
            ._m03(m03)
            ._m10(m10 * other.m10())
            ._m11(m11 * other.m11())
            ._m12(m12 * other.m12())
            ._m13(m13)
            ._m20(m20 * other.m20())
            ._m21(m21 * other.m21())
            ._m22(m22 * other.m22())
            ._m23(m23)
            ._m30(m30 * other.m30())
            ._m31(m31 * other.m31())
            ._m32(m32 * other.m32())
            ._m33(m33)
            ._properties(0);
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied float values. The matrix will look like this:<br><br>
     *
     *  m00, m10, m20, m30<br>
     *  m01, m11, m21, m31<br>
     *  m02, m12, m22, m32<br>
     *  m03, m13, m23, m33
     * 
     * @param m00
     *          the new value of m00
     * @param m01
     *          the new value of m01
     * @param m02
     *          the new value of m02
     * @param m03
     *          the new value of m03
     * @param m10
     *          the new value of m10
     * @param m11
     *          the new value of m11
     * @param m12
     *          the new value of m12
     * @param m13
     *          the new value of m13
     * @param m20
     *          the new value of m20
     * @param m21
     *          the new value of m21
     * @param m22
     *          the new value of m22
     * @param m23
     *          the new value of m23
     * @param m30
     *          the new value of m30
     * @param m31
     *          the new value of m31
     * @param m32
     *          the new value of m32
     * @param m33
     *          the new value of m33
     * @return this
     */
    public Matrix4f set(float m00, float m01, float m02, float m03,
                        float m10, float m11, float m12, float m13,
                        float m20, float m21, float m22, float m23, 
                        float m30, float m31, float m32, float m33) {
        return this
        ._m00(m00)
        ._m10(m10)
        ._m20(m20)
        ._m30(m30)
        ._m01(m01)
        ._m11(m11)
        ._m21(m21)
        ._m31(m31)
        ._m02(m02)
        ._m12(m12)
        ._m22(m22)
        ._m32(m32)
        ._m03(m03)
        ._m13(m13)
        ._m23(m23)
        ._m33(m33)
        .determineProperties();
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in column-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 4, 8, 12<br>
     * 1, 5, 9, 13<br>
     * 2, 6, 10, 14<br>
     * 3, 7, 11, 15<br>
     * 
     * @see #set(float[])
     * 
     * @param m
     *          the array to read the matrix values from
     * @param off
     *          the offset into the array
     * @return this
     */
    public Matrix4f set(float m[], int off) {
        MemUtil.INSTANCE.copy(m, off, this);
        return determineProperties();
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in column-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 4, 8, 12<br>
     * 1, 5, 9, 13<br>
     * 2, 6, 10, 14<br>
     * 3, 7, 11, 15<br>
     * 
     * @see #set(float[], int)
     * 
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix4f set(float m[]) {
        return set(m, 0);
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in row-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 1, 2, 3<br>
     * 4, 5, 6, 7<br>
     * 8, 9, 10, 11<br>
     * 12, 13, 14, 15<br>
     * 
     * @see #setTransposed(float[])
     * 
     * @param m
     *          the array to read the matrix values from
     * @param off
     *          the offset into the array
     * @return this
     */
    public Matrix4f setTransposed(float m[], int off) {
        MemUtil.INSTANCE.copyTransposed(m, off, this);
        return determineProperties();
    }

    /**
     * Set the values in the matrix using a float array that contains the matrix elements in row-major order.
     * <p>
     * The results will look like this:<br><br>
     * 
     * 0, 1, 2, 3<br>
     * 4, 5, 6, 7<br>
     * 8, 9, 10, 11<br>
     * 12, 13, 14, 15<br>
     * 
     * @see #setTransposed(float[], int)
     * 
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix4f setTransposed(float m[]) {
        return setTransposed(m, 0);
    }


    /**
     * Set the values of this matrix by reading 16 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix4f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 16 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix4f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 16 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix4f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 16 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix4f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 16 float values from the given {@link FloatBuffer} in row-major order,
     * starting at its current position.
     * <p>
     * The FloatBuffer is expected to contain the values in row-major order.
     * <p>
     * The position of the FloatBuffer will not be changed by this method.
     * 
     * @param buffer
     *              the FloatBuffer to read the matrix values from in row-major order
     * @return this
     */
    public Matrix4f setTransposed(FloatBuffer buffer) {
        MemUtil.INSTANCE.getTransposed(this, buffer.position(), buffer);
        return determineProperties();
    }

    /**
     * Set the values of this matrix by reading 16 float values from the given {@link ByteBuffer} in row-major order,
     * starting at its current position.
     * <p>
     * The ByteBuffer is expected to contain the values in row-major order.
     * <p>
     * The position of the ByteBuffer will not be changed by this method.
     * 
     * @param buffer
     *              the ByteBuffer to read the matrix values from in row-major order
     * @return this
     */
    public Matrix4f setTransposed(ByteBuffer buffer) {
        MemUtil.INSTANCE.getTransposed(this, buffer.position(), buffer);
        return determineProperties();
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
    public Matrix4f set(Vector4fc col0, Vector4fc col1, Vector4fc col2, Vector4fc col3) {
        return
        _m00(col0.x()).
        _m01(col0.y()).
        _m02(col0.z()).
        _m03(col0.w()).
        _m10(col1.x()).
        _m11(col1.y()).
        _m12(col1.z()).
        _m13(col1.w()).
        _m20(col2.x()).
        _m21(col2.y()).
        _m22(col2.z()).
        _m23(col2.w()).
        _m30(col3.x()).
        _m31(col3.y()).
        _m32(col3.z()).
        _m33(col3.w()).
        determineProperties();
    }

    public float determinant() {
        if ((properties & PROPERTY_AFFINE) != 0)
            return determinantAffine();
        return (m00 * m11 - m01 * m10) * (m22 * m33 - m23 * m32)
             + (m02 * m10 - m00 * m12) * (m21 * m33 - m23 * m31)
             + (m00 * m13 - m03 * m10) * (m21 * m32 - m22 * m31)
             + (m01 * m12 - m02 * m11) * (m20 * m33 - m23 * m30)
             + (m03 * m11 - m01 * m13) * (m20 * m32 - m22 * m30)
             + (m02 * m13 - m03 * m12) * (m20 * m31 - m21 * m30);
    }

    public float determinant3x3() {
        return (m00 * m11 - m01 * m10) * m22
             + (m02 * m10 - m00 * m12) * m21
             + (m01 * m12 - m02 * m11) * m20;
    }

    public float determinantAffine() {
        return (m00 * m11 - m01 * m10) * m22
             + (m02 * m10 - m00 * m12) * m21
             + (m01 * m12 - m02 * m11) * m20;
    }

    public Matrix4f invert(Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0) {
            return dest.identity();
        } else if ((properties & PROPERTY_TRANSLATION) != 0)
            return invertTranslation(dest);
        else if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return invertOrthonormal(dest);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return invertAffine(dest);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0)
            return invertPerspective(dest);
        return invertGeneric(dest);
    }
    private Matrix4f invertTranslation(Matrix4f dest) {
        if (dest != this)
            dest.set(this);
        return dest._m30(-m30)._m31(-m31)._m32(-m32)._properties(PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
    }
    private Matrix4f invertOrthonormal(Matrix4f dest) {
        float nm30 = -(m00 * m30 + m01 * m31 + m02 * m32);
        float nm31 = -(m10 * m30 + m11 * m31 + m12 * m32);
        float nm32 = -(m20 * m30 + m21 * m31 + m22 * m32);
        float m01 = this.m01;
        float m02 = this.m02;
        float m12 = this.m12;
        return dest
        ._m00(m00)
        ._m01(m10)
        ._m02(m20)
        ._m03(0.0f)
        ._m10(m01)
        ._m11(m11)
        ._m12(m21)
        ._m13(0.0f)
        ._m20(m02)
        ._m21(m12)
        ._m22(m22)
        ._m23(0.0f)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }
    private Matrix4f invertGeneric(Matrix4f dest) {
        if (this != dest)
            return invertGenericNonThis(dest);
        return invertGenericThis(dest);
    }
    private Matrix4f invertGenericNonThis(Matrix4f dest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        return dest
        ._m00(Math.fma( m11, l, Math.fma(-m12, k,  m13 * j)) * det)
        ._m01(Math.fma(-m01, l, Math.fma( m02, k, -m03 * j)) * det)
        ._m02(Math.fma( m31, f, Math.fma(-m32, e,  m33 * d)) * det)
        ._m03(Math.fma(-m21, f, Math.fma( m22, e, -m23 * d)) * det)
        ._m10(Math.fma(-m10, l, Math.fma( m12, i, -m13 * h)) * det)
        ._m11(Math.fma( m00, l, Math.fma(-m02, i,  m03 * h)) * det)
        ._m12(Math.fma(-m30, f, Math.fma( m32, c, -m33 * b)) * det)
        ._m13(Math.fma( m20, f, Math.fma(-m22, c,  m23 * b)) * det)
        ._m20(Math.fma( m10, k, Math.fma(-m11, i,  m13 * g)) * det)
        ._m21(Math.fma(-m00, k, Math.fma( m01, i, -m03 * g)) * det)
        ._m22(Math.fma( m30, e, Math.fma(-m31, c,  m33 * a)) * det)
        ._m23(Math.fma(-m20, e, Math.fma( m21, c, -m23 * a)) * det)
        ._m30(Math.fma(-m10, j, Math.fma( m11, h, -m12 * g)) * det)
        ._m31(Math.fma( m00, j, Math.fma(-m01, h,  m02 * g)) * det)
        ._m32(Math.fma(-m30, d, Math.fma( m31, b, -m32 * a)) * det)
        ._m33(Math.fma( m20, d, Math.fma(-m21, b,  m22 * a)) * det)
        ._properties(0);
    }
    private Matrix4f invertGenericThis(Matrix4f dest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        float nm00 = Math.fma( m11, l, Math.fma(-m12, k,  m13 * j)) * det;
        float nm01 = Math.fma(-m01, l, Math.fma( m02, k, -m03 * j)) * det;
        float nm02 = Math.fma( m31, f, Math.fma(-m32, e,  m33 * d)) * det;
        float nm03 = Math.fma(-m21, f, Math.fma( m22, e, -m23 * d)) * det;
        float nm10 = Math.fma(-m10, l, Math.fma( m12, i, -m13 * h)) * det;
        float nm11 = Math.fma( m00, l, Math.fma(-m02, i,  m03 * h)) * det;
        float nm12 = Math.fma(-m30, f, Math.fma( m32, c, -m33 * b)) * det;
        float nm13 = Math.fma( m20, f, Math.fma(-m22, c,  m23 * b)) * det;
        float nm20 = Math.fma( m10, k, Math.fma(-m11, i,  m13 * g)) * det;
        float nm21 = Math.fma(-m00, k, Math.fma( m01, i, -m03 * g)) * det;
        float nm22 = Math.fma( m30, e, Math.fma(-m31, c,  m33 * a)) * det;
        float nm23 = Math.fma(-m20, e, Math.fma( m21, c, -m23 * a)) * det;
        float nm30 = Math.fma(-m10, j, Math.fma( m11, h, -m12 * g)) * det;
        float nm31 = Math.fma( m00, j, Math.fma(-m01, h,  m02 * g)) * det;
        float nm32 = Math.fma(-m30, d, Math.fma( m31, b, -m32 * a)) * det;
        float nm33 = Math.fma( m20, d, Math.fma(-m21, b,  m22 * a)) * det;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
    }

    /**
     * Invert this matrix.
     * <p>
     * If <code>this</code> matrix represents an {@link #isAffine() affine} transformation, such as translation, rotation, scaling and shearing,
     * and thus its last row is equal to <code>(0, 0, 0, 1)</code>, then {@link #invertAffine()} can be used instead of this method.
     * 
     * @see #invertAffine()
     * 
     * @return this
     */
    public Matrix4f invert() {
        return invert(this);
    }

    /**
     * If <code>this</code> is a perspective projection matrix obtained via one of the {@link #perspective(float, float, float, float) perspective()} methods
     * or via {@link #setPerspective(float, float, float, float) setPerspective()}, that is, if <code>this</code> is a symmetrical perspective frustum transformation,
     * then this method builds the inverse of <code>this</code> and stores it into the given <code>dest</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of a perspective projection matrix when being obtained via {@link #perspective(float, float, float, float) perspective()}.
     * 
     * @see #perspective(float, float, float, float)
     * 
     * @param dest
     *          will hold the inverse of <code>this</code>
     * @return dest
     */
    public Matrix4f invertPerspective(Matrix4f dest) {
        float a =  1.0f / (m00 * m11);
        float l = -1.0f / (m23 * m32);
        return dest
        .set(m11 * a, 0, 0, 0,
                 0, m00 * a, 0, 0,
                 0, 0, 0, -m23 * l,
                 0, 0, -m32 * l, m22 * l)
        ._properties(0);
    }

    /**
     * If <code>this</code> is a perspective projection matrix obtained via one of the {@link #perspective(float, float, float, float) perspective()} methods
     * or via {@link #setPerspective(float, float, float, float) setPerspective()}, that is, if <code>this</code> is a symmetrical perspective frustum transformation,
     * then this method builds the inverse of <code>this</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of a perspective projection matrix when being obtained via {@link #perspective(float, float, float, float) perspective()}.
     * 
     * @see #perspective(float, float, float, float)
     * 
     * @return this
     */
    public Matrix4f invertPerspective() {
        return invertPerspective(this);
    }

    /**
     * If <code>this</code> is an arbitrary perspective projection matrix obtained via one of the {@link #frustum(float, float, float, float, float, float) frustum()}  methods
     * or via {@link #setFrustum(float, float, float, float, float, float) setFrustum()},
     * then this method builds the inverse of <code>this</code> and stores it into the given <code>dest</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of a perspective projection matrix.
     * <p>
     * If this matrix represents a symmetric perspective frustum transformation, as obtained via {@link #perspective(float, float, float, float) perspective()}, then
     * {@link #invertPerspective(Matrix4f)} should be used instead.
     * 
     * @see #frustum(float, float, float, float, float, float)
     * @see #invertPerspective(Matrix4f)
     * 
     * @param dest
     *          will hold the inverse of <code>this</code>
     * @return dest
     */
    public Matrix4f invertFrustum(Matrix4f dest) {
        float invM00 = 1.0f / m00;
        float invM11 = 1.0f / m11;
        float invM23 = 1.0f / m23;
        float invM32 = 1.0f / m32;
        return dest
        .set(invM00, 0, 0, 0,
                 0, invM11, 0, 0,
                 0, 0, 0, invM32,
                 -m20 * invM00 * invM23, -m21 * invM11 * invM23, invM23, -m22 * invM23 * invM32);
    }

    /**
     * If <code>this</code> is an arbitrary perspective projection matrix obtained via one of the {@link #frustum(float, float, float, float, float, float) frustum()}  methods
     * or via {@link #setFrustum(float, float, float, float, float, float) setFrustum()},
     * then this method builds the inverse of <code>this</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of a perspective projection matrix.
     * <p>
     * If this matrix represents a symmetric perspective frustum transformation, as obtained via {@link #perspective(float, float, float, float) perspective()}, then
     * {@link #invertPerspective()} should be used instead.
     * 
     * @see #frustum(float, float, float, float, float, float)
     * @see #invertPerspective()
     * 
     * @return this
     */
    public Matrix4f invertFrustum() {
        return invertFrustum(this);
    }

    public Matrix4f invertOrtho(Matrix4f dest) {
        float invM00 = 1.0f / m00;
        float invM11 = 1.0f / m11;
        float invM22 = 1.0f / m22;
        return dest
        .set(invM00, 0, 0, 0,
                 0, invM11, 0, 0,
                 0, 0, invM22, 0,
                 -m30 * invM00, -m31 * invM11, -m32 * invM22, 1)
        ._properties(PROPERTY_AFFINE | (this.properties & PROPERTY_ORTHONORMAL));
    }

    /**
     * Invert <code>this</code> orthographic projection matrix.
     * <p>
     * This method can be used to quickly obtain the inverse of an orthographic projection matrix.
     * 
     * @return this
     */
    public Matrix4f invertOrtho() {
        return invertOrtho(this);
    }

    /**
     * If <code>this</code> is a perspective projection matrix obtained via one of the {@link #perspective(float, float, float, float) perspective()} methods
     * or via {@link #setPerspective(float, float, float, float) setPerspective()}, that is, if <code>this</code> is a symmetrical perspective frustum transformation
     * and the given <code>view</code> matrix is {@link #isAffine() affine} and has unit scaling (for example by being obtained via {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()}),
     * then this method builds the inverse of <code>this * view</code> and stores it into the given <code>dest</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of the combination of the view and projection matrices, when both were obtained
     * via the common methods {@link #perspective(float, float, float, float) perspective()} and {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()} or
     * other methods, that build affine matrices, such as {@link #translate(float, float, float) translate} and {@link #rotate(float, float, float, float)}, except for {@link #scale(float, float, float) scale()}.
     * <p>
     * For the special cases of the matrices <code>this</code> and <code>view</code> mentioned above, this method is equivalent to the following code:
     * <pre>
     * dest.set(this).mul(view).invert();
     * </pre>
     * 
     * @param view
     *          the view transformation (must be {@link #isAffine() affine} and have unit scaling)
     * @param dest
     *          will hold the inverse of <code>this * view</code>
     * @return dest
     */
    public Matrix4f invertPerspectiveView(Matrix4fc view, Matrix4f dest) {
        float a =  1.0f / (m00 * m11);
        float l = -1.0f / (m23 * m32);
        float pm00 =  m11 * a;
        float pm11 =  m00 * a;
        float pm23 = -m23 * l;
        float pm32 = -m32 * l;
        float pm33 =  m22 * l;
        float vm30 = -view.m00() * view.m30() - view.m01() * view.m31() - view.m02() * view.m32();
        float vm31 = -view.m10() * view.m30() - view.m11() * view.m31() - view.m12() * view.m32();
        float vm32 = -view.m20() * view.m30() - view.m21() * view.m31() - view.m22() * view.m32();
        float nm10 = view.m01() * pm11;
        float nm30 = view.m02() * pm32 + vm30 * pm33;
        float nm31 = view.m12() * pm32 + vm31 * pm33;
        float nm32 = view.m22() * pm32 + vm32 * pm33;
        return dest
        ._m00(view.m00() * pm00)
        ._m01(view.m10() * pm00)
        ._m02(view.m20() * pm00)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(view.m11() * pm11)
        ._m12(view.m21() * pm11)
        ._m13(0.0f)
        ._m20(vm30 * pm23)
        ._m21(vm31 * pm23)
        ._m22(vm32 * pm23)
        ._m23(pm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(pm33)
        ._properties(0);
    }

    /**
     * If <code>this</code> is a perspective projection matrix obtained via one of the {@link #perspective(float, float, float, float) perspective()} methods
     * or via {@link #setPerspective(float, float, float, float) setPerspective()}, that is, if <code>this</code> is a symmetrical perspective frustum transformation
     * and the given <code>view</code> matrix has unit scaling,
     * then this method builds the inverse of <code>this * view</code> and stores it into the given <code>dest</code>.
     * <p>
     * This method can be used to quickly obtain the inverse of the combination of the view and projection matrices, when both were obtained
     * via the common methods {@link #perspective(float, float, float, float) perspective()} and {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()} or
     * other methods, that build affine matrices, such as {@link #translate(float, float, float) translate} and {@link #rotate(float, float, float, float)}, except for {@link #scale(float, float, float) scale()}.
     * <p>
     * For the special cases of the matrices <code>this</code> and <code>view</code> mentioned above, this method is equivalent to the following code:
     * <pre>
     * dest.set(this).mul(view).invert();
     * </pre>
     * 
     * @param view
     *          the view transformation (must have unit scaling)
     * @param dest
     *          will hold the inverse of <code>this * view</code>
     * @return dest
     */
    public Matrix4f invertPerspectiveView(Matrix4x3fc view, Matrix4f dest) {
        float a =  1.0f / (m00 * m11);
        float l = -1.0f / (m23 * m32);
        float pm00 =  m11 * a;
        float pm11 =  m00 * a;
        float pm23 = -m23 * l;
        float pm32 = -m32 * l;
        float pm33 =  m22 * l;
        float vm30 = -view.m00() * view.m30() - view.m01() * view.m31() - view.m02() * view.m32();
        float vm31 = -view.m10() * view.m30() - view.m11() * view.m31() - view.m12() * view.m32();
        float vm32 = -view.m20() * view.m30() - view.m21() * view.m31() - view.m22() * view.m32();
        return dest
        ._m00(view.m00() * pm00)
        ._m01(view.m10() * pm00)
        ._m02(view.m20() * pm00)
        ._m03(0.0f)
        ._m10(view.m01() * pm11)
        ._m11(view.m11() * pm11)
        ._m12(view.m21() * pm11)
        ._m13(0.0f)
        ._m20(vm30 * pm23)
        ._m21(vm31 * pm23)
        ._m22(vm32 * pm23)
        ._m23(pm23)
        ._m30(view.m02() * pm32 + vm30 * pm33)
        ._m31(view.m12() * pm32 + vm31 * pm33)
        ._m32(view.m22() * pm32 + vm32 * pm33)
        ._m33(pm33)
        ._properties(0);
    }

    public Matrix4f invertAffine(Matrix4f dest) {
        float m11m00 = m00 * m11, m10m01 = m01 * m10, m10m02 = m02 * m10;
        float m12m00 = m00 * m12, m12m01 = m01 * m12, m11m02 = m02 * m11;
        float det = (m11m00 - m10m01) * m22 + (m10m02 - m12m00) * m21 + (m12m01 - m11m02) * m20;
        float s = 1.0f / det;
        float m10m22 = m10 * m22, m10m21 = m10 * m21, m11m22 = m11 * m22;
        float m11m20 = m11 * m20, m12m21 = m12 * m21, m12m20 = m12 * m20;
        float m20m02 = m20 * m02, m20m01 = m20 * m01, m21m02 = m21 * m02;
        float m21m00 = m21 * m00, m22m01 = m22 * m01, m22m00 = m22 * m00;
        float nm31 = (m20m02 * m31 - m20m01 * m32 + m21m00 * m32 - m21m02 * m30 + m22m01 * m30 - m22m00 * m31) * s;
        float nm32 = (m11m02 * m30 - m12m01 * m30 + m12m00 * m31 - m10m02 * m31 + m10m01 * m32 - m11m00 * m32) * s;
        return dest
        ._m00((m11m22 - m12m21) * s)
        ._m01((m21m02 - m22m01) * s)
        ._m02((m12m01 - m11m02) * s)
        ._m03(0.0f)
        ._m10((m12m20 - m10m22) * s)
        ._m11((m22m00 - m20m02) * s)
        ._m12((m10m02 - m12m00) * s)
        ._m13(0.0f)
        ._m20((m10m21 - m11m20) * s)
        ._m21((m20m01 - m21m00) * s)
        ._m22((m11m00 - m10m01) * s)
        ._m23(0.0f)
        ._m30((m10m22 * m31 - m10m21 * m32 + m11m20 * m32 - m11m22 * m30 + m12m21 * m30 - m12m20 * m31) * s)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE);
    }

    /**
     * Invert this matrix by assuming that it is an {@link #isAffine() affine} transformation (i.e. its last row is equal to <code>(0, 0, 0, 1)</code>).
     * 
     * @return this
     */
    public Matrix4f invertAffine() {
        return invertAffine(this);
    }

    public Matrix4f transpose(Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.identity();
        else if (this != dest)
            return transposeNonThisGeneric(dest);
        return transposeThisGeneric(dest);
    }
    private Matrix4f transposeNonThisGeneric(Matrix4f dest) {
        return dest
        ._m00(m00)
        ._m01(m10)
        ._m02(m20)
        ._m03(m30)
        ._m10(m01)
        ._m11(m11)
        ._m12(m21)
        ._m13(m31)
        ._m20(m02)
        ._m21(m12)
        ._m22(m22)
        ._m23(m32)
        ._m30(m03)
        ._m31(m13)
        ._m32(m23)
        ._m33(m33)
        ._properties(0);
    }
    private Matrix4f transposeThisGeneric(Matrix4f dest) {
        float nm10 = m01;
        float nm20 = m02;
        float nm21 = m12;
        float nm30 = m03;
        float nm31 = m13;
        float nm32 = m23;
        return dest
        ._m01(m10)
        ._m02(m20)
        ._m03(m30)
        ._m10(nm10)
        ._m12(m21)
        ._m13(m31)
        ._m20(nm20)
        ._m21(nm21)
        ._m23(m32)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._properties(0);
    }

    /**
     * Transpose only the upper left 3x3 submatrix of this matrix.
     * <p>
     * All other matrix elements are left unchanged.
     * 
     * @return this
     */
    public Matrix4f transpose3x3() {
        return transpose3x3(this);
    }

    public Matrix4f transpose3x3(Matrix4f dest) {
        float nm10 = m01, nm20 = m02, nm21 = m12;
        return dest
        ._m00(m00)
        ._m01(m10)
        ._m02(m20)
        ._m10(nm10)
        ._m11(m11)
        ._m12(m21)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(m22)
        ._properties(this.properties & (PROPERTY_AFFINE | PROPERTY_ORTHONORMAL | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    public Matrix3f transpose3x3(Matrix3f dest) {
        return dest
        ._m00(m00)
        ._m01(m10)
        ._m02(m20)
        ._m10(m01)
        ._m11(m11)
        ._m12(m21)
        ._m20(m02)
        ._m21(m12)
        ._m22(m22);
    }

    /**
     * Transpose this matrix.
     * 
     * @return this
     */
    public Matrix4f transpose() {
        return transpose(this);
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
    public Matrix4f translation(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        return this._m30(x)._m31(y)._m32(z)._properties(PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL);
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
    public Matrix4f translation(Vector3fc offset) {
        return translation(offset.x(), offset.y(), offset.z());
    }

    /**
     * Set only the translation components <code>(m30, m31, m32)</code> of this matrix to the given values <code>(x, y, z)</code>.
     * <p>
     * Note that this will only work properly for orthogonal matrices (without any perspective).
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
    public Matrix4f setTranslation(float x, float y, float z) {
        return this._m30(x)._m31(y)._m32(z)._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY));
    }

    /**
     * Set only the translation components <code>(m30, m31, m32)</code> of this matrix to the values <code>(xyz.x, xyz.y, xyz.z)</code>.
     * <p>
     * Note that this will only work properly for orthogonal matrices (without any perspective).
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
    public Matrix4f setTranslation(Vector3fc xyz) {
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
             + Runtime.format(m02, formatter) + " " + Runtime.format(m12, formatter) + " " + Runtime.format(m22, formatter) + " " + Runtime.format(m32, formatter) + "\n"
             + Runtime.format(m03, formatter) + " " + Runtime.format(m13, formatter) + " " + Runtime.format(m23, formatter) + " " + Runtime.format(m33, formatter) + "\n";
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix4fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix4fc)
     * 
     * @param dest
     *            the destination matrix
     * @return the passed in destination
     */
    public Matrix4f get(Matrix4f dest) {
        return dest.set(this);
    }

    public Matrix4x3f get4x3(Matrix4x3f dest) {
        return dest.set(this);
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix4dc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix4dc)
     * 
     * @param dest
     *            the destination matrix
     * @return the passed in destination
     */
    public Matrix4d get(Matrix4d dest) {
        return dest.set(this);
    }

    public Matrix3f get3x3(Matrix3f dest) {
        return dest.set(this);
    }

    public Matrix3d get3x3(Matrix3d dest) {
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
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get(ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public FloatBuffer get4x3(FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x3(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get4x3(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x3(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get4x3(ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x3(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get4x3(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x3(this, index, buffer);
        return buffer;
    }

    public FloatBuffer get3x4(FloatBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get3x4(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get3x4(ByteBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get3x4(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put3x4(this, index, buffer);
        return buffer;
    }

    public FloatBuffer getTransposed(FloatBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer getTransposed(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, index, buffer);
        return buffer;
    }

    public ByteBuffer getTransposed(ByteBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer getTransposed(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, index, buffer);
        return buffer;
    }

    public FloatBuffer get4x3Transposed(FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x3Transposed(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get4x3Transposed(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x3Transposed(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get4x3Transposed(ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x3Transposed(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get4x3Transposed(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x3Transposed(this, index, buffer);
        return buffer;
    }


    public float[] get(float[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    public float[] get(float[] arr) {
        MemUtil.INSTANCE.copy(this, arr, 0);
        return arr;
    }

    /**
     * Set all the values within this matrix to <code>0</code>.
     * 
     * @return this
     */
    public Matrix4f zero() {
        MemUtil.INSTANCE.zero(this);
        return _properties(0);
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
    public Matrix4f scaling(float factor) {
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
    public Matrix4f scaling(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        boolean one = Math.absEqualsOne(x) && Math.absEqualsOne(y) && Math.absEqualsOne(z);
        return this
        ._m00(x)
        ._m11(y)
        ._m22(z)
        ._properties(PROPERTY_AFFINE | (one ? PROPERTY_ORTHONORMAL : 0));
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
    public Matrix4f scaling(Vector3fc xyz) {
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
    public Matrix4f rotation(float angle, Vector3fc axis) {
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
    public Matrix4f rotation(AxisAngle4f axisAngle) {
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
    public Matrix4f rotation(float angle, float x, float y, float z) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotationX(x * angle);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotationY(y * angle);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotationZ(z * angle);
        return rotationInternal(angle, x, y, z);
    }
    private Matrix4f rotationInternal(float angle, float x, float y, float z) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float C = 1.0f - cos, xy = x * y, xz = x * z, yz = y * z;
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        return this
        ._m00(cos + x * x * C)
        ._m10(xy * C - z * sin)
        ._m20(xz * C + y * sin)
        ._m01(xy * C + z * sin)
        ._m11(cos + y * y * C)
        ._m21(yz * C - x * sin)
        ._m02(xz * C - y * sin)
        ._m12(yz * C + x * sin)
        ._m22(cos + z * z * C)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationX(float ang) {
        float sin = Math.sin(ang), cos = Math.cosFromSin(sin, ang);
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m11(cos)._m12(sin)._m21(-sin)._m22(cos)._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationY(float ang) {
        float sin = Math.sin(ang), cos = Math.cosFromSin(sin, ang);
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(cos)._m02(-sin)._m20(sin)._m22(cos)._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationZ(float ang) {
        float sin = Math.sin(ang), cos = Math.cosFromSin(sin, ang);
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        return this._m00(cos)._m01(sin)._m10(-sin)._m11(cos)._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set this matrix to a rotation transformation about the Z axis to align the local <code>+X</code> towards <code>(dirX, dirY)</code>.
     * <p>
     * The vector <code>(dirX, dirY)</code> must be a unit vector.
     * 
     * @param dirX
     *            the x component of the normalized direction
     * @param dirY
     *            the y component of the normalized direction
     * @return this
     */
    public Matrix4f rotationTowardsXY(float dirX, float dirY) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        return this._m00(dirY)._m01(dirX)._m10(-dirX)._m11(dirY)._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationXYZ(float angleX, float angleY, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        float nm01 = -sinX * -sinY, nm02 = cosX * -sinY;
        return this
        ._m20(sinY)
        ._m21(-sinX * cosY)
        ._m22(cosX * cosY)
        ._m00(cosY * cosZ)
        ._m01(nm01 * cosZ + cosX * sinZ)
        ._m02(nm02 * cosZ + sinX * sinZ)
        ._m10(cosY * -sinZ)
        ._m11(nm01 * -sinZ + cosX * cosZ)
        ._m12(nm02 * -sinZ + sinX * cosZ)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationZYX(float angleZ, float angleY, float angleX) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float nm20 = cosZ * sinY;
        float nm21 = sinZ * sinY;
        return this
        ._m00(cosZ * cosY)
        ._m01(sinZ * cosY)
        ._m02(-sinY)
        ._m03(0.0f)
        ._m10(-sinZ * cosX + nm20 * sinX)
        ._m11(cosZ * cosX + nm21 * sinX)
        ._m12(cosY * sinX)
        ._m13(0.0f)
        ._m20(-sinZ * -sinX + nm20 * cosX)
        ._m21(cosZ * -sinX + nm21 * cosX)
        ._m22(cosY * cosX)
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f rotationYXZ(float angleY, float angleX, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float nm10 = sinY * sinX, nm12 = cosY * sinX;
        return this
        ._m20(sinY * cosX)
        ._m21(-sinX)
        ._m22(cosY * cosX)
        ._m23(0.0f)
        ._m00(cosY * cosZ + nm10 * sinZ)
        ._m01(cosX * sinZ)
        ._m02(-sinY * cosZ + nm12 * sinZ)
        ._m03(0.0f)
        ._m10(cosY * -sinZ + nm10 * cosZ)
        ._m11(cosX * cosZ)
        ._m12(-sinY * -sinZ + nm12 * cosZ)
        ._m13(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set only the upper left 3x3 submatrix of this matrix to a rotation of <code>angleX</code> radians about the X axis, followed by a rotation
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
    public Matrix4f setRotationXYZ(float angleX, float angleY, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float nm01 = -sinX * -sinY;
        float nm02 = cosX * -sinY;
        return this
        ._m20(sinY)
        ._m21(-sinX * cosY)
        ._m22(cosX * cosY)
        ._m00(cosY * cosZ)
        ._m01(nm01 * cosZ + cosX * sinZ)
        ._m02(nm02 * cosZ + sinX * sinZ)
        ._m10(cosY * -sinZ)
        ._m11(nm01 * -sinZ + cosX * cosZ)
        ._m12(nm02 * -sinZ + sinX * cosZ)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Set only the upper left 3x3 submatrix of this matrix to a rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation
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
    public Matrix4f setRotationZYX(float angleZ, float angleY, float angleX) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float nm20 = cosZ * sinY, nm21 = sinZ * sinY;
        return this
        ._m00(cosZ * cosY)
        ._m01(sinZ * cosY)
        ._m02(-sinY)
        ._m10(-sinZ * cosX + nm20 * sinX)
        ._m11(cosZ * cosX + nm21 * sinX)
        ._m12(cosY * sinX)
        ._m20(-sinZ * -sinX + nm20 * cosX)
        ._m21(cosZ * -sinX + nm21 * cosX)
        ._m22(cosY * cosX)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Set only the upper left 3x3 submatrix of this matrix to a rotation of <code>angleY</code> radians about the Y axis, followed by a rotation
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
    public Matrix4f setRotationYXZ(float angleY, float angleX, float angleZ) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float nm10 = sinY * sinX, nm12 = cosY * sinX;
        return this
        ._m20(sinY * cosX)
        ._m21(-sinX)
        ._m22(cosY * cosX)
        ._m00(cosY * cosZ + nm10 * sinZ)
        ._m01(cosX * sinZ)
        ._m02(-sinY * cosZ + nm12 * sinZ)
        ._m10(cosY * -sinZ + nm10 * cosZ)
        ._m11(cosX * cosZ)
        ._m12(-sinY * -sinZ + nm12 * cosZ)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Set this matrix to the rotation transformation of the given {@link Quaternionfc}.
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
    public Matrix4f rotation(Quaternionfc quat) {
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
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        return this
        ._m00(w2 + x2 - z2 - y2)
        ._m01(dxy + dzw)
        ._m02(dxz - dyw)
        ._m10(-dzw + dxy)
        ._m11(y2 - z2 + w2 - x2)
        ._m12(dyz + dxw)
        ._m20(dyw + dxz)
        ._m21(dyz - dxw)
        ._m22(z2 - y2 - x2 + w2)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f translationRotateScale(float tx, float ty, float tz, 
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
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        return this
        ._m00(sx - (q11 + q22) * sx)
        ._m01((q01 + q23) * sx)
        ._m02((q02 - q13) * sx)
        ._m03(0.0f)
        ._m10((q01 - q23) * sy)
        ._m11(sy - (q22 + q00) * sy)
        ._m12((q12 + q03) * sy)
        ._m13(0.0f)
        ._m20((q02 + q13) * sz)
        ._m21((q12 - q03) * sz)
        ._m22(sz - (q11 + q00) * sz)
        ._m23(0.0f)
        ._m30(tx)
        ._m31(ty)
        ._m32(tz)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | (one ? PROPERTY_ORTHONORMAL : 0));
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
     * @see #scale(Vector3fc)
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @return this
     */
    public Matrix4f translationRotateScale(Vector3fc translation, 
                                           Quaternionfc quat, 
                                           Vector3fc scale) {
        return translationRotateScale(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale.x(), scale.y(), scale.z());
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>, and <code>S</code> is a scaling transformation
     * which scales all three axes by <code>scale</code>.
     * <p>
     * When transforming a vector by the resulting matrix the scaling transformation will be applied first, then the rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).scale(scale)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #scale(float)
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
     * @param scale
     *          the scaling factor for all three axes
     * @return this
     */
    public Matrix4f translationRotateScale(float tx, float ty, float tz, 
                                           float qx, float qy, float qz, float qw, 
                                           float scale) {
        return translationRotateScale(tx, ty, tz, qx, qy, qz, qw, scale, scale, scale);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S</code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation transformation specified by the given quaternion, and <code>S</code> is a scaling transformation
     * which scales all three axes by <code>scale</code>.
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
     * @see #scale(float)
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @return this
     */
    public Matrix4f translationRotateScale(Vector3fc translation, 
                                           Quaternionfc quat, 
                                           float scale) {
        return translationRotateScale(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale, scale, scale);
    }

    /**
     * Set <code>this</code> matrix to <code>(T * R * S)<sup>-1</sup></code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>, and <code>S</code> is a scaling transformation
     * which scales the three axes x, y and z by <code>(sx, sy, sz)</code>.
     * <p>
     * This method is equivalent to calling: <code>translationRotateScale(...).invert()</code>
     * 
     * @see #translationRotateScale(float, float, float, float, float, float, float, float, float, float)
     * @see #invert()
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
    public Matrix4f translationRotateScaleInvert(float tx, float ty, float tz, 
                                                 float qx, float qy, float qz, float qw, 
                                                 float sx, float sy, float sz) {
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        if (one)
            return translationRotateScale(tx, ty, tz, qx, qy, qz, qw, sx, sy, sz).invertOrthonormal(this);
        float nqx = -qx, nqy = -qy, nqz = -qz;
        float dqx = nqx + nqx;
        float dqy = nqy + nqy;
        float dqz = nqz + nqz;
        float q00 = dqx * nqx;
        float q11 = dqy * nqy;
        float q22 = dqz * nqz;
        float q01 = dqx * nqy;
        float q02 = dqx * nqz;
        float q03 = dqx * qw;
        float q12 = dqy * nqz;
        float q13 = dqy * qw;
        float q23 = dqz * qw;
        float isx = 1/sx, isy = 1/sy, isz = 1/sz;
        return this
        ._m00(isx * (1.0f - q11 - q22))
        ._m01(isy * (q01 + q23))
        ._m02(isz * (q02 - q13))
        ._m03(0.0f)
        ._m10(isx * (q01 - q23))
        ._m11(isy * (1.0f - q22 - q00))
        ._m12(isz * (q12 + q03))
        ._m13(0.0f)
        ._m20(isx * (q02 + q13))
        ._m21(isy * (q12 - q03))
        ._m22(isz * (1.0f - q11 - q00))
        ._m23(0.0f)
        ._m30(-m00 * tx - m10 * ty - m20 * tz)
        ._m31(-m01 * tx - m11 * ty - m21 * tz)
        ._m32(-m02 * tx - m12 * ty - m22 * tz)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE);
    }

    /**
     * Set <code>this</code> matrix to <code>(T * R * S)<sup>-1</sup></code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation transformation specified by the given quaternion, and <code>S</code> is a scaling transformation
     * which scales the axes by <code>scale</code>.
     * <p>
     * This method is equivalent to calling: <code>translationRotateScale(...).invert()</code>
     * 
     * @see #translationRotateScale(Vector3fc, Quaternionfc, Vector3fc)
     * @see #invert()
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @return this
     */
    public Matrix4f translationRotateScaleInvert(Vector3fc translation, 
                                                 Quaternionfc quat, 
                                                 Vector3fc scale) {
        return translationRotateScaleInvert(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale.x(), scale.y(), scale.z());
    }

    /**
     * Set <code>this</code> matrix to <code>(T * R * S)<sup>-1</sup></code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation transformation specified by the given quaternion, and <code>S</code> is a scaling transformation
     * which scales all three axes by <code>scale</code>.
     * <p>
     * This method is equivalent to calling: <code>translationRotateScale(...).invert()</code>
     * 
     * @see #translationRotateScale(Vector3fc, Quaternionfc, float)
     * @see #invert()
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @return this
     */
    public Matrix4f translationRotateScaleInvert(Vector3fc translation, 
                                                 Quaternionfc quat, 
                                                 float scale) {
        return translationRotateScaleInvert(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale, scale, scale);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S * M</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code>,
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>, <code>S</code> is a scaling transformation
     * which scales the three axes x, y and z by <code>(sx, sy, sz)</code> and <code>M</code> is an {@link #isAffine() affine} matrix.
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(tx, ty, tz).rotate(quat).scale(sx, sy, sz).mulAffine(m)</code>
     * 
     * @see #translation(float, float, float)
     * @see #rotate(Quaternionfc)
     * @see #scale(float, float, float)
     * @see #mulAffine(Matrix4fc)
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
     *          the {@link #isAffine() affine} matrix to multiply by
     * @return this
     */
    public Matrix4f translationRotateScaleMulAffine(float tx, float ty, float tz, 
                                                    float qx, float qy, float qz, float qw, 
                                                    float sx, float sy, float sz,
                                                    Matrix4f m) {
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
        float m00 = nm00 * m.m00 + nm10 * m.m01 + nm20 * m.m02;
        float m01 = nm01 * m.m00 + nm11 * m.m01 + nm21 * m.m02;
        this._m02(nm02 * m.m00 + nm12 * m.m01 + nm22 * m.m02)
            ._m00(m00)
            ._m01(m01)
            ._m03(0.0f);
        float m10 = nm00 * m.m10 + nm10 * m.m11 + nm20 * m.m12;
        float m11 = nm01 * m.m10 + nm11 * m.m11 + nm21 * m.m12;
        this._m12(nm02 * m.m10 + nm12 * m.m11 + nm22 * m.m12)
            ._m10(m10)
            ._m11(m11)
            ._m13(0.0f);
        float m20 = nm00 * m.m20 + nm10 * m.m21 + nm20 * m.m22;
        float m21 = nm01 * m.m20 + nm11 * m.m21 + nm21 * m.m22;
        this._m22(nm02 * m.m20 + nm12 * m.m21 + nm22 * m.m22)
            ._m20(m20)
            ._m21(m21)
            ._m23(0.0f);
        float m30 = nm00 * m.m30 + nm10 * m.m31 + nm20 * m.m32 + tx;
        float m31 = nm01 * m.m30 + nm11 * m.m31 + nm21 * m.m32 + ty;
        this._m32(nm02 * m.m30 + nm12 * m.m31 + nm22 * m.m32 + tz)
            ._m30(m30)
            ._m31(m31)
            ._m33(1.0f);
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        return _properties(PROPERTY_AFFINE | (one && (m.properties & PROPERTY_ORTHONORMAL) != 0 ? PROPERTY_ORTHONORMAL : 0));
    }

    /**
     * Set <code>this</code> matrix to <code>T * R * S * M</code>, where <code>T</code> is the given <code>translation</code>,
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the given quaternion, <code>S</code> is a scaling transformation
     * which scales the axes by <code>scale</code> and <code>M</code> is an {@link #isAffine() affine} matrix.
     * <p>
     * When transforming a vector by the resulting matrix the transformation described by <code>M</code> will be applied first, then the scaling, then rotation and
     * at last the translation.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method is equivalent to calling: <code>translation(translation).rotate(quat).scale(scale).mulAffine(m)</code>
     * 
     * @see #translation(Vector3fc)
     * @see #rotate(Quaternionfc)
     * @see #mulAffine(Matrix4fc)
     * 
     * @param translation
     *          the translation
     * @param quat
     *          the quaternion representing a rotation
     * @param scale
     *          the scaling factors
     * @param m
     *          the {@link #isAffine() affine} matrix to multiply by
     * @return this
     */
    public Matrix4f translationRotateScaleMulAffine(Vector3fc translation, 
                                                    Quaternionfc quat, 
                                                    Vector3fc scale,
                                                    Matrix4f m) {
        return translationRotateScaleMulAffine(translation.x(), translation.y(), translation.z(), quat.x(), quat.y(), quat.z(), quat.w(), scale.x(), scale.y(), scale.z(), m);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code> and
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the quaternion <code>(qx, qy, qz, qw)</code>.
     * <p>
     * When transforming a vector by the resulting matrix the rotation - and possibly scaling - transformation will be applied first and then the translation.
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
     * @param qx
     *          the x-coordinate of the vector part of the quaternion
     * @param qy
     *          the y-coordinate of the vector part of the quaternion
     * @param qz
     *          the z-coordinate of the vector part of the quaternion
     * @param qw
     *          the scalar part of the quaternion
     * @return this
     */
    public Matrix4f translationRotate(float tx, float ty, float tz, float qx, float qy, float qz, float qw) {
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
        return this
        ._m00(w2 + x2 - z2 - y2)
        ._m01(xy + zw + zw + xy)
        ._m02(xz - yw + xz - yw)
        ._m10(-zw + xy - zw + xy)
        ._m11(y2 - z2 + w2 - x2)
        ._m12(yz + yz + xw + xw)
        ._m20(yw + xz + xz + yw)
        ._m21(yz + yz - xw - xw)
        ._m22(z2 - y2 - x2 + w2)
        ._m30(tx)
        ._m31(ty)
        ._m32(tz)
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }

    /**
     * Set <code>this</code> matrix to <code>T * R</code>, where <code>T</code> is a translation by the given <code>(tx, ty, tz)</code> and
     * <code>R</code> is a rotation - and possibly scaling - transformation specified by the given quaternion.
     * <p>
     * When transforming a vector by the resulting matrix the rotation - and possibly scaling - transformation will be applied first and then the translation.
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
    public Matrix4f translationRotate(float tx, float ty, float tz, Quaternionfc quat) {
        return translationRotate(tx, ty, tz, quat.x(), quat.y(), quat.z(), quat.w());
    }

    /**
     * Set the upper left 3x3 submatrix of this {@link Matrix4f} to the given {@link Matrix3fc} and don't change the other elements.
     * 
     * @param mat
     *          the 3x3 matrix
     * @return this
     */
    public Matrix4f set3x3(Matrix3fc mat) {
        return
        set3x3Matrix3fc(mat).
        _properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
    }
    private Matrix4f set3x3Matrix3fc(Matrix3fc mat) {
        return this
        ._m00(mat.m00())
        ._m01(mat.m01())
        ._m02(mat.m02())
        ._m10(mat.m10())
        ._m11(mat.m11())
        ._m12(mat.m12())
        ._m20(mat.m20())
        ._m21(mat.m21())
        ._m22(mat.m22());
    }

    public Vector4f transform(Vector4f v) {
        return v.mul(this);
    }

    public Vector4f transform(Vector4fc v, Vector4f dest) {
        return v.mul(this, dest);
    }

    public Vector4f transform(float x, float y, float z, float w, Vector4f dest) {
       return dest.set(x, y, z, w).mul(this);
    }

    public Vector4f transformTranspose(Vector4f v) {
        return v.mulTranspose(this);
    }
    public Vector4f transformTranspose(Vector4fc v, Vector4f dest) {
        return v.mulTranspose(this, dest);
    }
    public Vector4f transformTranspose(float x, float y, float z, float w, Vector4f dest) {
       return dest.set(x, y, z, w).mulTranspose(this);
    }

    public Vector4f transformProject(Vector4f v) {
        return v.mulProject(this);
    }

    public Vector4f transformProject(Vector4fc v, Vector4f dest) {
        return v.mulProject(this, dest);
    }

    public Vector4f transformProject(float x, float y, float z, float w, Vector4f dest) {
        return dest.set(x, y, z, w).mulProject(this);
    }

    public Vector3f transformProject(Vector4fc v, Vector3f dest) {
        return v.mulProject(this, dest);
    }

    public Vector3f transformProject(float x, float y, float z, float w, Vector3f dest) {
        return dest.set(x, y, z).mulProject(this, w, dest);
    }

    public Vector3f transformProject(Vector3f v) {
        return v.mulProject(this);
    }

    public Vector3f transformProject(Vector3fc v, Vector3f dest) {
        return v.mulProject(this, dest);
    }

    public Vector3f transformProject(float x, float y, float z, Vector3f dest) {
        return dest.set(x, y, z).mulProject(this);
    }

    public Vector3f transformPosition(Vector3f v) {
        return v.mulPosition(this);
    }

    public Vector3f transformPosition(Vector3fc v, Vector3f dest) {
        return transformPosition(v.x(), v.y(), v.z(), dest);
    }

    public Vector3f transformPosition(float x, float y, float z, Vector3f dest) {
        return dest.set(x, y, z).mulPosition(this);
    }

    public Vector3f transformDirection(Vector3f v) {
        return transformDirection(v.x, v.y, v.z, v);
    }

    public Vector3f transformDirection(Vector3fc v, Vector3f dest) {
        return transformDirection(v.x(), v.y(), v.z(), dest);
    }

    public Vector3f transformDirection(float x, float y, float z, Vector3f dest) {
        return dest.set(x, y, z).mulDirection(this);
    }

    public Vector4f transformAffine(Vector4f v) {
        return v.mulAffine(this, v);
    }

    public Vector4f transformAffine(Vector4fc v, Vector4f dest) {
        return transformAffine(v.x(), v.y(), v.z(), v.w(), dest);
    }

    public Vector4f transformAffine(float x, float y, float z, float w, Vector4f dest) {
        return dest.set(x, y, z, w).mulAffine(this, dest);
    }

    public Matrix4f scale(Vector3fc xyz, Matrix4f dest) {
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
    public Matrix4f scale(Vector3fc xyz) {
        return scale(xyz.x(), xyz.y(), xyz.z(), this);
    }

    public Matrix4f scale(float xyz, Matrix4f dest) {
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
    public Matrix4f scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Matrix4f scaleXY(float x, float y, Matrix4f dest) {
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
    public Matrix4f scaleXY(float x, float y) {
        return scale(x, y, 1.0f);
    }

    public Matrix4f scale(float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.scaling(x, y, z);
        return scaleGeneric(x, y, z, dest);
    }
    private Matrix4f scaleGeneric(float x, float y, float z, Matrix4f dest) {
        boolean one = Math.absEqualsOne(x) && Math.absEqualsOne(y) && Math.absEqualsOne(z);
        return dest
        ._m00(m00 * x)
        ._m01(m01 * x)
        ._m02(m02 * x)
        ._m03(m03 * x)
        ._m10(m10 * y)
        ._m11(m11 * y)
        ._m12(m12 * y)
        ._m13(m13 * y)
        ._m20(m20 * z)
        ._m21(m21 * z)
        ._m22(m22 * z)
        ._m23(m23 * z)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
            | (one ? 0 : PROPERTY_ORTHONORMAL)));
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given sx,
     * sy and sz factors.
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
    public Matrix4f scale(float x, float y, float z) {
        return scale(x, y, z, this);
    }

    public Matrix4f scaleAround(float sx, float sy, float sz, float ox, float oy, float oz, Matrix4f dest) {
        float nm30 = m00 * ox + m10 * oy + m20 * oz + m30;
        float nm31 = m01 * ox + m11 * oy + m21 * oz + m31;
        float nm32 = m02 * ox + m12 * oy + m22 * oz + m32;
        float nm33 = m03 * ox + m13 * oy + m23 * oz + m33;
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        return dest
        ._m00(m00 * sx)
        ._m01(m01 * sx)
        ._m02(m02 * sx)
        ._m03(m03 * sx)
        ._m10(m10 * sy)
        ._m11(m11 * sy)
        ._m12(m12 * sy)
        ._m13(m13 * sy)
        ._m20(m20 * sz)
        ._m21(m21 * sz)
        ._m22(m22 * sz)
        ._m23(m23 * sz)
        ._m30(-dest.m00 * ox - dest.m10 * oy - dest.m20 * oz + nm30)
        ._m31(-dest.m01 * ox - dest.m11 * oy - dest.m21 * oz + nm31)
        ._m32(-dest.m02 * ox - dest.m12 * oy - dest.m22 * oz + nm32)
        ._m33(-dest.m03 * ox - dest.m13 * oy - dest.m23 * oz + nm33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
            | (one ? 0 : PROPERTY_ORTHONORMAL)));
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
    public Matrix4f scaleAround(float sx, float sy, float sz, float ox, float oy, float oz) {
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
    public Matrix4f scaleAround(float factor, float ox, float oy, float oz) {
        return scaleAround(factor, factor, factor, ox, oy, oz, this);
    }

    public Matrix4f scaleAround(float factor, float ox, float oy, float oz, Matrix4f dest) {
        return scaleAround(factor, factor, factor, ox, oy, oz, dest);
    }

    public Matrix4f scaleLocal(float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.scaling(x, y, z);
        return scaleLocalGeneric(x, y, z, dest);
    }
    private Matrix4f scaleLocalGeneric(float x, float y, float z, Matrix4f dest) {
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
        boolean one = Math.absEqualsOne(x) && Math.absEqualsOne(y) && Math.absEqualsOne(z);
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(m03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(m13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(m23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
            | (one ? 0 : PROPERTY_ORTHONORMAL)));
    }

    public Matrix4f scaleLocal(float xyz, Matrix4f dest) {
        return scaleLocal(xyz, xyz, xyz, dest);
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given xyz factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * 
     * @param xyz
     *            the factor of the x, y and z component
     * @return this
     */
    public Matrix4f scaleLocal(float xyz) {
        return scaleLocal(xyz, this);
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
    public Matrix4f scaleLocal(float x, float y, float z) {
        return scaleLocal(x, y, z, this);
    }

    public Matrix4f scaleAroundLocal(float sx, float sy, float sz, float ox, float oy, float oz, Matrix4f dest) {
        boolean one = Math.absEqualsOne(sx) && Math.absEqualsOne(sy) && Math.absEqualsOne(sz);
        return dest
        ._m00(sx * (m00 - ox * m03) + ox * m03)
        ._m01(sy * (m01 - oy * m03) + oy * m03)
        ._m02(sz * (m02 - oz * m03) + oz * m03)
        ._m03(m03)
        ._m10(sx * (m10 - ox * m13) + ox * m13)
        ._m11(sy * (m11 - oy * m13) + oy * m13)
        ._m12(sz * (m12 - oz * m13) + oz * m13)
        ._m13(m13)
        ._m20(sx * (m20 - ox * m23) + ox * m23)
        ._m21(sy * (m21 - oy * m23) + oy * m23)
        ._m22(sz * (m22 - oz * m23) + oz * m23)
        ._m23(m23)
        ._m30(sx * (m30 - ox * m33) + ox * m33)
        ._m31(sy * (m31 - oy * m33) + oy * m33)
        ._m32(sz * (m32 - oz * m33) + oz * m33)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
            | (one ? 0 : PROPERTY_ORTHONORMAL)));
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given sx,
     * sy and sz factors while using <code>(ox, oy, oz)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix4f().translate(ox, oy, oz).scale(sx, sy, sz).translate(-ox, -oy, -oz).mul(this, this)</code>
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
    public Matrix4f scaleAroundLocal(float sx, float sy, float sz, float ox, float oy, float oz) {
        return scaleAroundLocal(sx, sy, sz, ox, oy, oz, this);
    }

    /**
     * Pre-multiply scaling to this matrix by scaling all three base axes by the given <code>factor</code>
     * while using <code>(ox, oy, oz)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix4f().translate(ox, oy, oz).scale(factor).translate(-ox, -oy, -oz).mul(this, this)</code>
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
    public Matrix4f scaleAroundLocal(float factor, float ox, float oy, float oz) {
        return scaleAroundLocal(factor, factor, factor, ox, oy, oz, this);
    }

    public Matrix4f scaleAroundLocal(float factor, float ox, float oy, float oz, Matrix4f dest) {
        return scaleAroundLocal(factor, factor, factor, ox, oy, oz, dest);
    }

    public Matrix4f rotateX(float ang, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationX(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationX(ang).setTranslation(x, y, z);
        }
        return rotateXInternal(ang, dest);
    }
    private Matrix4f rotateXInternal(float ang, Matrix4f dest) {
        float sin = Math.sin(ang), cos = Math.cosFromSin(sin, ang);
        float lm10 = m10, lm11 = m11, lm12 = m12, lm13 = m13, lm20 = m20, lm21 = m21, lm22 = m22, lm23 = m23;
        return dest
        ._m20(Math.fma(lm10, -sin, lm20 * cos))
        ._m21(Math.fma(lm11, -sin, lm21 * cos))
        ._m22(Math.fma(lm12, -sin, lm22 * cos))
        ._m23(Math.fma(lm13, -sin, lm23 * cos))
        ._m10(Math.fma(lm10, cos, lm20 * sin))
        ._m11(Math.fma(lm11, cos, lm21 * sin))
        ._m12(Math.fma(lm12, cos, lm22 * sin))
        ._m13(Math.fma(lm13, cos, lm23 * sin))
        ._m00(m00)
        ._m01(m01)
        ._m02(m02)
        ._m03(m03)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateX(float ang) {
        return rotateX(ang, this);
    }

    public Matrix4f rotateY(float ang, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationY(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationY(ang).setTranslation(x, y, z);
        }
        return rotateYInternal(ang, dest);
    }
    private Matrix4f rotateYInternal(float ang, Matrix4f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        // add temporaries for dependent values
        float nm00 = Math.fma(m00, cos, m20 * -sin);
        float nm01 = Math.fma(m01, cos, m21 * -sin);
        float nm02 = Math.fma(m02, cos, m22 * -sin);
        float nm03 = Math.fma(m03, cos, m23 * -sin);
        // set non-dependent values directly
        return dest
        ._m20(Math.fma(m00, sin, m20 * cos))
        ._m21(Math.fma(m01, sin, m21 * cos))
        ._m22(Math.fma(m02, sin, m22 * cos))
        ._m23(Math.fma(m03, sin, m23 * cos))
        // set other values
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(m10)
        ._m11(m11)
        ._m12(m12)
        ._m13(m13)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateY(float ang) {
        return rotateY(ang, this);
    }

    public Matrix4f rotateZ(float ang, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationZ(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30, y = m31, z = m32;
            return dest.rotationZ(ang).setTranslation(x, y, z);
        }
        return rotateZInternal(ang, dest);
    }
    private Matrix4f rotateZInternal(float ang, Matrix4f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        return rotateTowardsXY(sin, cos, dest);
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
    public Matrix4f rotateZ(float ang) {
        return rotateZ(ang, this);
    }

    /**
     * Apply rotation about the Z axis to align the local <code>+X</code> towards <code>(dirX, dirY)</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * <p>
     * The vector <code>(dirX, dirY)</code> must be a unit vector.
     * 
     * @param dirX
     *            the x component of the normalized direction
     * @param dirY
     *            the y component of the normalized direction
     * @return this
     */
    public Matrix4f rotateTowardsXY(float dirX, float dirY) {
        return rotateTowardsXY(dirX, dirY, this);
    }

    public Matrix4f rotateTowardsXY(float dirX, float dirY, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationTowardsXY(dirX, dirY);
        float nm00 = Math.fma(m00, dirY, m10 * dirX);
        float nm01 = Math.fma(m01, dirY, m11 * dirX);
        float nm02 = Math.fma(m02, dirY, m12 * dirX);
        float nm03 = Math.fma(m03, dirY, m13 * dirX);
        return dest
        ._m10(Math.fma(m00, -dirX, m10 * dirY))
        ._m11(Math.fma(m01, -dirX, m11 * dirY))
        ._m12(Math.fma(m02, -dirX, m12 * dirY))
        ._m13(Math.fma(m03, -dirX, m13 * dirY))
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m20(m20)
        ._m21(m21)
        ._m22(m22)
        ._m23(m23)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
     * This method is equivalent to calling: <code>rotateX(angles.x()).rotateY(angles.y()).rotateZ(angles.z())</code>
     * 
     * @param angles
     *            the Euler angles
     * @return this
     */
    public Matrix4f rotateXYZ(Vector3fc angles) {
        return rotateXYZ(angles.x(), angles.y(), angles.z());
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
    public Matrix4f rotateXYZ(float angleX, float angleY, float angleZ) {
        return rotateXYZ(angleX, angleY, angleZ, this);
    }

    public Matrix4f rotateXYZ(float angleX, float angleY, float angleZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationXYZ(angleX, angleY, angleZ);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationXYZ(angleX, angleY, angleZ).setTranslation(tx, ty, tz);
        } else if ((properties & PROPERTY_AFFINE) != 0)
            return dest.rotateAffineXYZ(angleX, angleY, angleZ);
        return rotateXYZInternal(angleX, angleY, angleZ, dest);
    }
    private Matrix4f rotateXYZInternal(float angleX, float angleY, float angleZ, Matrix4f dest) {
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
        float nm10 = Math.fma(m10, cosX, m20 * sinX);
        float nm11 = Math.fma(m11, cosX, m21 * sinX);
        float nm12 = Math.fma(m12, cosX, m22 * sinX);
        float nm13 = Math.fma(m13, cosX, m23 * sinX);
        float nm20 = Math.fma(m10, m_sinX, m20 * cosX);
        float nm21 = Math.fma(m11, m_sinX, m21 * cosX);
        float nm22 = Math.fma(m12, m_sinX, m22 * cosX);
        float nm23 = Math.fma(m13, m_sinX, m23 * cosX);
        // rotateY
        float nm00 = Math.fma(m00, cosY, nm20 * m_sinY);
        float nm01 = Math.fma(m01, cosY, nm21 * m_sinY);
        float nm02 = Math.fma(m02, cosY, nm22 * m_sinY);
        float nm03 = Math.fma(m03, cosY, nm23 * m_sinY);
        return dest
        ._m20(Math.fma(m00, sinY, nm20 * cosY))
        ._m21(Math.fma(m01, sinY, nm21 * cosY))
        ._m22(Math.fma(m02, sinY, nm22 * cosY))
        ._m23(Math.fma(m03, sinY, nm23 * cosY))
        // rotateZ
        ._m00(Math.fma(nm00, cosZ, nm10 * sinZ))
        ._m01(Math.fma(nm01, cosZ, nm11 * sinZ))
        ._m02(Math.fma(nm02, cosZ, nm12 * sinZ))
        ._m03(Math.fma(nm03, cosZ, nm13 * sinZ))
        ._m10(Math.fma(nm00, m_sinZ, nm10 * cosZ))
        ._m11(Math.fma(nm01, m_sinZ, nm11 * cosZ))
        ._m12(Math.fma(nm02, m_sinZ, nm12 * cosZ))
        ._m13(Math.fma(nm03, m_sinZ, nm13 * cosZ))
        // copy last column from 'this'
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply rotation of <code>angleX</code> radians about the X axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method assumes that <code>this</code> matrix represents an {@link #isAffine() affine} transformation (i.e. its last row is equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrix only represents affine transformations, such as translation, rotation, scaling and shearing (in any combination).
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
    public Matrix4f rotateAffineXYZ(float angleX, float angleY, float angleZ) {
        return rotateAffineXYZ(angleX, angleY, angleZ, this);
    }

    public Matrix4f rotateAffineXYZ(float angleX, float angleY, float angleZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationXYZ(angleX, angleY, angleZ);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationXYZ(angleX, angleY, angleZ).setTranslation(tx, ty, tz);
        }
        return rotateAffineXYZInternal(angleX, angleY, angleZ, dest);
    }
    private Matrix4f rotateAffineXYZInternal(float angleX, float angleY, float angleZ, Matrix4f dest) {
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
        float nm10 = Math.fma(m10, cosX, m20 * sinX);
        float nm11 = Math.fma(m11, cosX, m21 * sinX);
        float nm12 = Math.fma(m12, cosX, m22 * sinX);
        float nm20 = Math.fma(m10, m_sinX, m20 * cosX);
        float nm21 = Math.fma(m11, m_sinX, m21 * cosX);
        float nm22 = Math.fma(m12, m_sinX, m22 * cosX);
        // rotateY
        float nm00 = Math.fma(m00, cosY, nm20 * m_sinY);
        float nm01 = Math.fma(m01, cosY, nm21 * m_sinY);
        float nm02 = Math.fma(m02, cosY, nm22 * m_sinY);
        return dest
        ._m20(Math.fma(m00, sinY, nm20 * cosY))
        ._m21(Math.fma(m01, sinY, nm21 * cosY))
        ._m22(Math.fma(m02, sinY, nm22 * cosY))
        ._m23(0.0f)
        // rotateZ
        ._m00(Math.fma(nm00, cosZ, nm10 * sinZ))
        ._m01(Math.fma(nm01, cosZ, nm11 * sinZ))
        ._m02(Math.fma(nm02, cosZ, nm12 * sinZ))
        ._m03(0.0f)
        ._m10(Math.fma(nm00, m_sinZ, nm10 * cosZ))
        ._m11(Math.fma(nm01, m_sinZ, nm11 * cosZ))
        ._m12(Math.fma(nm02, m_sinZ, nm12 * cosZ))
        ._m13(0.0f)
        // copy last column from 'this'
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateZYX(Vector3f angles) {
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
    public Matrix4f rotateZYX(float angleZ, float angleY, float angleX) {
        return rotateZYX(angleZ, angleY, angleX, this);
    }

    public Matrix4f rotateZYX(float angleZ, float angleY, float angleX, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationZYX(angleZ, angleY, angleX);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationZYX(angleZ, angleY, angleX).setTranslation(tx, ty, tz);
        } else if ((properties & PROPERTY_AFFINE) != 0)
            return dest.rotateAffineZYX(angleZ, angleY, angleX);
        return rotateZYXInternal(angleZ, angleY, angleX, dest);
    }
    private Matrix4f rotateZYXInternal(float angleZ, float angleY, float angleX, Matrix4f dest) {
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
        float nm03 = m03 * cosZ + m13 * sinZ;
        float nm10 = m00 * m_sinZ + m10 * cosZ;
        float nm11 = m01 * m_sinZ + m11 * cosZ;
        float nm12 = m02 * m_sinZ + m12 * cosZ;
        float nm13 = m03 * m_sinZ + m13 * cosZ;
        // rotateY
        float nm20 = nm00 * sinY + m20 * cosY;
        float nm21 = nm01 * sinY + m21 * cosY;
        float nm22 = nm02 * sinY + m22 * cosY;
        float nm23 = nm03 * sinY + m23 * cosY;
        return dest
        ._m00(nm00 * cosY + m20 * m_sinY)
        ._m01(nm01 * cosY + m21 * m_sinY)
        ._m02(nm02 * cosY + m22 * m_sinY)
        ._m03(nm03 * cosY + m23 * m_sinY)
        ._m10(nm10 * cosX + nm20 * sinX)
        ._m11(nm11 * cosX + nm21 * sinX)
        ._m12(nm12 * cosX + nm22 * sinX)
        ._m13(nm13 * cosX + nm23 * sinX)
        ._m20(nm10 * m_sinX + nm20 * cosX)
        ._m21(nm11 * m_sinX + nm21 * cosX)
        ._m22(nm12 * m_sinX + nm22 * cosX)
        ._m23(nm13 * m_sinX + nm23 * cosX)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleX</code> radians about the X axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method assumes that <code>this</code> matrix represents an {@link #isAffine() affine} transformation (i.e. its last row is equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrix only represents affine transformations, such as translation, rotation, scaling and shearing (in any combination).
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * 
     * @param angleZ
     *            the angle to rotate about Z
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @return this
     */
    public Matrix4f rotateAffineZYX(float angleZ, float angleY, float angleX) {
        return rotateAffineZYX(angleZ, angleY, angleX, this);
    }

    public Matrix4f rotateAffineZYX(float angleZ, float angleY, float angleX, Matrix4f dest) {
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
        return dest
        ._m00(nm00 * cosY + m20 * m_sinY)
        ._m01(nm01 * cosY + m21 * m_sinY)
        ._m02(nm02 * cosY + m22 * m_sinY)
        ._m03(0.0f)
        ._m10(nm10 * cosX + nm20 * sinX)
        ._m11(nm11 * cosX + nm21 * sinX)
        ._m12(nm12 * cosX + nm22 * sinX)
        ._m13(0.0f)
        ._m20(nm10 * m_sinX + nm20 * cosX)
        ._m21(nm11 * m_sinX + nm21 * cosX)
        ._m22(nm12 * m_sinX + nm22 * cosX)
        ._m23(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateYXZ(Vector3f angles) {
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
    public Matrix4f rotateYXZ(float angleY, float angleX, float angleZ) {
        return rotateYXZ(angleY, angleX, angleZ, this);
    }

    public Matrix4f rotateYXZ(float angleY, float angleX, float angleZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotationYXZ(angleY, angleX, angleZ);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float tx = m30, ty = m31, tz = m32;
            return dest.rotationYXZ(angleY, angleX, angleZ).setTranslation(tx, ty, tz);
        } else if ((properties & PROPERTY_AFFINE) != 0)
            return dest.rotateAffineYXZ(angleY, angleX, angleZ);
        return rotateYXZInternal(angleY, angleX, angleZ, dest);
    }
    private Matrix4f rotateYXZInternal(float angleY, float angleX, float angleZ, Matrix4f dest) {
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
        float nm23 = m03 * sinY + m23 * cosY;
        float nm00 = m00 * cosY + m20 * m_sinY;
        float nm01 = m01 * cosY + m21 * m_sinY;
        float nm02 = m02 * cosY + m22 * m_sinY;
        float nm03 = m03 * cosY + m23 * m_sinY;
        // rotateX
        float nm10 = m10 * cosX + nm20 * sinX;
        float nm11 = m11 * cosX + nm21 * sinX;
        float nm12 = m12 * cosX + nm22 * sinX;
        float nm13 = m13 * cosX + nm23 * sinX;
        return dest
        ._m20(m10 * m_sinX + nm20 * cosX)
        ._m21(m11 * m_sinX + nm21 * cosX)
        ._m22(m12 * m_sinX + nm22 * cosX)
        ._m23(m13 * m_sinX + nm23 * cosX)
        ._m00(nm00 * cosZ + nm10 * sinZ)
        ._m01(nm01 * cosZ + nm11 * sinZ)
        ._m02(nm02 * cosZ + nm12 * sinZ)
        ._m03(nm03 * cosZ + nm13 * sinZ)
        ._m10(nm00 * m_sinZ + nm10 * cosZ)
        ._m11(nm01 * m_sinZ + nm11 * cosZ)
        ._m12(nm02 * m_sinZ + nm12 * cosZ)
        ._m13(nm03 * m_sinZ + nm13 * cosZ)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply rotation of <code>angleY</code> radians about the Y axis, followed by a rotation of <code>angleX</code> radians about the X axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * This method assumes that <code>this</code> matrix represents an {@link #isAffine() affine} transformation (i.e. its last row is equal to <code>(0, 0, 0, 1)</code>)
     * and can be used to speed up matrix multiplication if the matrix only represents affine transformations, such as translation, rotation, scaling and shearing (in any combination).
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * rotation will be applied first!
     * 
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param angleZ
     *            the angle to rotate about Z
     * @return this
     */
    public Matrix4f rotateAffineYXZ(float angleY, float angleX, float angleZ) {
        return rotateAffineYXZ(angleY, angleX, angleZ, this);
    }

    public Matrix4f rotateAffineYXZ(float angleY, float angleX, float angleZ, Matrix4f dest) {
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
        return dest
        ._m20(m10 * m_sinX + nm20 * cosX)
        ._m21(m11 * m_sinX + nm21 * cosX)
        ._m22(m12 * m_sinX + nm22 * cosX)
        ._m23(0.0f)
        ._m00(nm00 * cosZ + nm10 * sinZ)
        ._m01(nm01 * cosZ + nm11 * sinZ)
        ._m02(nm02 * cosZ + nm12 * sinZ)
        ._m03(0.0f)
        ._m10(nm00 * m_sinZ + nm10 * cosZ)
        ._m11(nm01 * m_sinZ + nm11 * cosZ)
        ._m12(nm02 * m_sinZ + nm12 * cosZ)
        ._m13(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotate(float ang, float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotation(ang, x, y, z);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return rotateTranslation(ang, x, y, z, dest);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return rotateAffine(ang, x, y, z, dest);
        return rotateGeneric(ang, x, y, z, dest);
    }
    private Matrix4f rotateGeneric(float ang, float x, float y, float z, Matrix4f dest) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateX(x * ang, dest);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateY(y * ang, dest);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateZ(z * ang, dest);
        return rotateGenericInternal(ang, x, y, z, dest);
    }
    private Matrix4f rotateGenericInternal(float ang, float x, float y, float z, Matrix4f dest) {
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
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
        return dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m23(m03 * rm20 + m13 * rm21 + m23 * rm22)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotate(float ang, float x, float y, float z) {
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
    public Matrix4f rotateTranslation(float ang, float x, float y, float z, Matrix4f dest) {
        float tx = m30, ty = m31, tz = m32;
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return dest.rotationX(x * ang).setTranslation(tx, ty, tz);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return dest.rotationY(y * ang).setTranslation(tx, ty, tz);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return dest.rotationZ(z * ang).setTranslation(tx, ty, tz);
        return rotateTranslationInternal(ang, x, y, z, dest);
    }
    private Matrix4f rotateTranslationInternal(float ang, float x, float y, float z, Matrix4f dest) {
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
        return dest
        ._m20(rm20)
        ._m21(rm21)
        ._m22(rm22)
        ._m00(rm00)
        ._m01(rm01)
        ._m02(rm02)
        ._m03(0.0f)
        ._m10(rm10)
        ._m11(rm11)
        ._m12(rm12)
        ._m13(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(1.0f)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply rotation to this {@link #isAffine() affine} matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis and store the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to be {@link #isAffine() affine}.
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
    public Matrix4f rotateAffine(float ang, float x, float y, float z, Matrix4f dest) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateX(x * ang, dest);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateY(y * ang, dest);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateZ(z * ang, dest);
        return rotateAffineInternal(ang, x, y, z, dest);
    }
    private Matrix4f rotateAffineInternal(float ang, float x, float y, float z, Matrix4f dest) {
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
        // add temporaries for dependent values
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        // set non-dependent values directly
        return dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m23(0.0f)
        // set other values
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(1.0f)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply rotation to this {@link #isAffine() affine} matrix by rotating the given amount of radians
     * about the specified <code>(x, y, z)</code> axis.
     * <p>
     * This method assumes <code>this</code> to be {@link #isAffine() affine}.
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
    public Matrix4f rotateAffine(float ang, float x, float y, float z) {
        return rotateAffine(ang, x, y, z, this);
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
    public Matrix4f rotateLocal(float ang, float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotation(ang, x, y, z);
        return rotateLocalGeneric(ang, x, y, z, dest);
    }
    private Matrix4f rotateLocalGeneric(float ang, float x, float y, float z, Matrix4f dest) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateLocalX(x * ang, dest);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateLocalY(y * ang, dest);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateLocalZ(z * ang, dest);
        return rotateLocalGenericInternal(ang, x, y, z, dest);
    }
    private Matrix4f rotateLocalGenericInternal(float ang, float x, float y, float z, Matrix4f dest) {
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
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(m03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(m13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(m23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateLocal(float ang, float x, float y, float z) {
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
    public Matrix4f rotateLocalX(float ang, Matrix4f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm02 = sin * m01 + cos * m02;
        float nm12 = sin * m11 + cos * m12;
        float nm22 = sin * m21 + cos * m22;
        float nm32 = sin * m31 + cos * m32;
        return dest
        ._m00(m00)
        ._m01(cos * m01 - sin * m02)
        ._m02(nm02)
        ._m03(m03)
        ._m10(m10)
        ._m11(cos * m11 - sin * m12)
        ._m12(nm12)
        ._m13(m13)
        ._m20(m20)
        ._m21(cos * m21 - sin * m22)
        ._m22(nm22)
        ._m23(m23)
        ._m30(m30)
        ._m31(cos * m31 - sin * m32)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateLocalX(float ang) {
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
    public Matrix4f rotateLocalY(float ang, Matrix4f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm02 = -sin * m00 + cos * m02;
        float nm12 = -sin * m10 + cos * m12;
        float nm22 = -sin * m20 + cos * m22;
        float nm32 = -sin * m30 + cos * m32;
        return dest
        ._m00(cos * m00 + sin * m02)
        ._m01(m01)
        ._m02(nm02)
        ._m03(m03)
        ._m10(cos * m10 + sin * m12)
        ._m11(m11)
        ._m12(nm12)
        ._m13(m13)
        ._m20(cos * m20 + sin * m22)
        ._m21(m21)
        ._m22(nm22)
        ._m23(m23)
        ._m30(cos * m30 + sin * m32)
        ._m31(m31)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateLocalY(float ang) {
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
    public Matrix4f rotateLocalZ(float ang, Matrix4f dest) {
        float sin = Math.sin(ang);
        float cos = Math.cosFromSin(sin, ang);
        float nm01 = sin * m00 + cos * m01;
        float nm11 = sin * m10 + cos * m11;
        float nm21 = sin * m20 + cos * m21;
        float nm31 = sin * m30 + cos * m31;
        return dest
        ._m00(cos * m00 - sin * m01)
        ._m01(nm01)
        ._m02(m02)
        ._m03(m03)
        ._m10(cos * m10 - sin * m11)
        ._m11(nm11)
        ._m12(m12)
        ._m13(m13)
        ._m20(cos * m20 - sin * m21)
        ._m21(nm21)
        ._m22(m22)
        ._m23(m23)
        ._m30(cos * m30 - sin * m31)
        ._m31(nm31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateLocalZ(float ang) {
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
    public Matrix4f translate(Vector3fc offset) {
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
    public Matrix4f translate(Vector3fc offset, Matrix4f dest) {
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
    public Matrix4f translate(float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.translation(x, y, z);
        return translateGeneric(x, y, z, dest);
    }
    private Matrix4f translateGeneric(float x, float y, float z, Matrix4f dest) {
        MemUtil.INSTANCE.copy(this, dest);
        return dest
        ._m30(Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30))))
        ._m31(Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31))))
        ._m32(Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32))))
        ._m33(Math.fma(m03, x, Math.fma(m13, y, Math.fma(m23, z, m33))))
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY));
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
    public Matrix4f translate(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return translation(x, y, z);
        return translateGeneric(x, y, z);
    }
    private Matrix4f translateGeneric(float x, float y, float z) {
        return this
        ._m30(Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30))))
        ._m31(Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31))))
        ._m32(Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32))))
        ._m33(Math.fma(m03, x, Math.fma(m13, y, Math.fma(m23, z, m33))))
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY));
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
    public Matrix4f translateLocal(Vector3fc offset) {
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
    public Matrix4f translateLocal(Vector3fc offset, Matrix4f dest) {
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
    public Matrix4f translateLocal(float x, float y, float z, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.translation(x, y, z);
        return translateLocalGeneric(x, y, z, dest);
    }
    private Matrix4f translateLocalGeneric(float x, float y, float z, Matrix4f dest) {
        float nm00 = m00 + x * m03;
        float nm01 = m01 + y * m03;
        float nm02 = m02 + z * m03;
        float nm10 = m10 + x * m13;
        float nm11 = m11 + y * m13;
        float nm12 = m12 + z * m13;
        float nm20 = m20 + x * m23;
        float nm21 = m21 + y * m23;
        float nm22 = m22 + z * m23;
        float nm30 = m30 + x * m33;
        float nm31 = m31 + y * m33;
        float nm32 = m32 + z * m33;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(m03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(m13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(m23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY));
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
    public Matrix4f translateLocal(float x, float y, float z) {
        return translateLocal(x, y, z, this);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(m00);
        out.writeFloat(m01);
        out.writeFloat(m02);
        out.writeFloat(m03);
        out.writeFloat(m10);
        out.writeFloat(m11);
        out.writeFloat(m12);
        out.writeFloat(m13);
        out.writeFloat(m20);
        out.writeFloat(m21);
        out.writeFloat(m22);
        out.writeFloat(m23);
        out.writeFloat(m30);
        out.writeFloat(m31);
        out.writeFloat(m32);
        out.writeFloat(m33);
    }

    public void readExternal(ObjectInput in) throws IOException {
        this._m00(in.readFloat())
            ._m01(in.readFloat())
            ._m02(in.readFloat())
            ._m03(in.readFloat())
            ._m10(in.readFloat())
            ._m11(in.readFloat())
            ._m12(in.readFloat())
            ._m13(in.readFloat())
            ._m20(in.readFloat())
            ._m21(in.readFloat())
            ._m22(in.readFloat())
            ._m23(in.readFloat())
            ._m30(in.readFloat())
            ._m31(in.readFloat())
            ._m32(in.readFloat())
            ._m33(in.readFloat())
            .determineProperties();
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
    public Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrtho(left, right, bottom, top, zNear, zFar, zZeroToOne);
        return orthoGeneric(left, right, bottom, top, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f orthoGeneric(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        float rm30 = (left + right) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
            ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(m20 * rm22)
            ._m21(m21 * rm22)
            ._m22(m22 * rm22)
            ._m23(m23 * rm22)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
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
    public Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4f dest) {
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
    public Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
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
    public Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar) {
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
    public Matrix4f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrthoLH(left, right, bottom, top, zNear, zFar, zZeroToOne);
        return orthoLHGeneric(left, right, bottom, top, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f orthoLHGeneric(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        float rm30 = (left + right) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
            ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(m20 * rm22)
            ._m21(m21 * rm22)
            ._m22(m22 * rm22)
            ._m23(m23 * rm22)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
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
    public Matrix4f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4f dest) {
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
    public Matrix4f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
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
    public Matrix4f orthoLH(float left, float right, float bottom, float top, float zNear, float zFar) {
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
    public Matrix4f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / (right - left))
            ._m11(2.0f / (top - bottom))
            ._m22((zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar))
            ._m30((right + left) / (left - right))
            ._m31((top + bottom) / (bottom - top))
            ._m32((zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
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
    public Matrix4f setOrthoLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / (right - left))
            ._m11(2.0f / (top - bottom))
            ._m22((zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear))
            ._m30((right + left) / (left - right))
            ._m31((top + bottom) / (bottom - top))
            ._m32((zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f setOrthoLH(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setOrthoLH(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, boolean, Matrix4f) ortho()} with
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
    public Matrix4f orthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrthoSymmetric(width, height, zNear, zFar, zZeroToOne);
        return orthoSymmetricGeneric(width, height, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f orthoSymmetricGeneric(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / width;
        float rm11 = 2.0f / height;
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m20 * rm32 + m30)
            ._m31(m21 * rm32 + m31)
            ._m32(m22 * rm32 + m32)
            ._m33(m23 * rm32 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(m20 * rm22)
            ._m21(m21 * rm22)
            ._m22(m22 * rm22)
            ._m23(m23 * rm22)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Apply a symmetric orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, Matrix4f) ortho()} with
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
    public Matrix4f orthoSymmetric(float width, float height, float zNear, float zFar, Matrix4f dest) {
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
    public Matrix4f orthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
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
    public Matrix4f orthoSymmetric(float width, float height, float zNear, float zFar) {
        return orthoSymmetric(width, height, zNear, zFar, false, this);
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, boolean, Matrix4f) orthoLH()} with
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
    public Matrix4f orthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrthoSymmetricLH(width, height, zNear, zFar, zZeroToOne);
        return orthoSymmetricLHGeneric(width, height, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f orthoSymmetricLHGeneric(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / width;
        float rm11 = 2.0f / height;
        float rm22 = (zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear);
        float rm32 = (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar);
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m20 * rm32 + m30)
            ._m31(m21 * rm32 + m31)
            ._m32(m22 * rm32 + m32)
            ._m33(m23 * rm32 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(m20 * rm22)
            ._m21(m21 * rm22)
            ._m22(m22 * rm22)
            ._m23(m23 * rm22)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Apply a symmetric orthographic projection transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, Matrix4f) orthoLH()} with
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
    public Matrix4f orthoSymmetricLH(float width, float height, float zNear, float zFar, Matrix4f dest) {
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
    public Matrix4f orthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
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
    public Matrix4f orthoSymmetricLH(float width, float height, float zNear, float zFar) {
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
    public Matrix4f setOrthoSymmetric(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / width)
            ._m11(2.0f / height)
            ._m22((zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar))
            ._m32((zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f setOrthoSymmetric(float width, float height, float zNear, float zFar) {
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
    public Matrix4f setOrthoSymmetricLH(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / width)
            ._m11(2.0f / height)
            ._m22((zZeroToOne ? 1.0f : 2.0f) / (zFar - zNear))
            ._m32((zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f setOrthoSymmetricLH(float width, float height, float zNear, float zFar) {
        return setOrthoSymmetricLH(width, height, zNear, zFar, false);
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system to this matrix
     * and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #ortho(float, float, float, float, float, float, Matrix4f) ortho()} with
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
     * @see #ortho(float, float, float, float, float, float, Matrix4f)
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
    public Matrix4f ortho2D(float left, float right, float bottom, float top, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrtho2D(left, right, bottom, top);
        return ortho2DGeneric(left, right, bottom, top, dest);
    }
    private Matrix4f ortho2DGeneric(float left, float right, float bottom, float top, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm30 = (right + left) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m00 * rm30 + m10 * rm31 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m32)
            ._m33(m03 * rm30 + m13 * rm31 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(-m20)
            ._m21(-m21)
            ._m22(-m22)
            ._m23(-m23)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
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
    public Matrix4f ortho2D(float left, float right, float bottom, float top) {
        return ortho2D(left, right, bottom, top, this);
    }

    /**
     * Apply an orthographic projection transformation for a left-handed coordinate system to this matrix and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling {@link #orthoLH(float, float, float, float, float, float, Matrix4f) orthoLH()} with
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
     * @see #orthoLH(float, float, float, float, float, float, Matrix4f)
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
    public Matrix4f ortho2DLH(float left, float right, float bottom, float top, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setOrtho2DLH(left, right, bottom, top);
        return ortho2DLHGeneric(left, right, bottom, top, dest);
    }
    private Matrix4f ortho2DLHGeneric(float left, float right, float bottom, float top, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm30 = (right + left) / (left - right);
        float rm31 = (top + bottom) / (bottom - top);

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest._m30(m00 * rm30 + m10 * rm31 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m32)
            ._m33(m03 * rm30 + m13 * rm31 + m33)
            ._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m20(m20)
            ._m21(m21)
            ._m22(m22)
            ._m23(m23)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
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
    public Matrix4f ortho2DLH(float left, float right, float bottom, float top) {
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
    public Matrix4f setOrtho2D(float left, float right, float bottom, float top) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / (right - left))
            ._m11(2.0f / (top - bottom))
            ._m22(-1.0f)
            ._m30((right + left) / (left - right))
            ._m31((top + bottom) / (bottom - top))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f setOrtho2DLH(float left, float right, float bottom, float top) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(2.0f / (right - left))
            ._m11(2.0f / (top - bottom))
            ._m30((right + left) / (left - right))
            ._m31((top + bottom) / (bottom - top))
            ._properties(PROPERTY_AFFINE);
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
    public Matrix4f lookAlong(Vector3fc dir, Vector3fc up) {
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
    public Matrix4f lookAlong(Vector3fc dir, Vector3fc up, Matrix4f dest) {
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
    public Matrix4f lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setLookAlong(dirX, dirY, dirZ, upX, upY, upZ);
        return lookAlongGeneric(dirX, dirY, dirZ, upX, upY, upZ, dest);
    }

    private Matrix4f lookAlongGeneric(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix4f dest) {
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
        // perform optimized matrix multiplication
        // introduce temporaries for dependent results
        float nm00 = m00 * leftX + m10 * upnX + m20 * dirX;
        float nm01 = m01 * leftX + m11 * upnX + m21 * dirX;
        float nm02 = m02 * leftX + m12 * upnX + m22 * dirX;
        float nm03 = m03 * leftX + m13 * upnX + m23 * dirX;
        float nm10 = m00 * leftY + m10 * upnY + m20 * dirY;
        float nm11 = m01 * leftY + m11 * upnY + m21 * dirY;
        float nm12 = m02 * leftY + m12 * upnY + m22 * dirY;
        float nm13 = m03 * leftY + m13 * upnY + m23 * dirY;
        return dest
        ._m20(m00 * leftZ + m10 * upnZ + m20 * dirZ)
        ._m21(m01 * leftZ + m11 * upnZ + m21 * dirZ)
        ._m22(m02 * leftZ + m12 * upnZ + m22 * dirZ)
        ._m23(m03 * leftZ + m13 * upnZ + m23 * dirZ)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f lookAlong(float dirX, float dirY, float dirZ,
                              float upX, float upY, float upZ) {
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
    public Matrix4f setLookAlong(Vector3fc dir, Vector3fc up) {
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
    public Matrix4f setLookAlong(float dirX, float dirY, float dirZ,
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
        this._m00(leftX)
            ._m01(dirY * leftZ - dirZ * leftY)
            ._m02(dirX)
            ._m03(0.0f)
            ._m10(leftY)
            ._m11(dirZ * leftX - dirX * leftZ)
            ._m12(dirY)
            ._m13(0.0f)
            ._m20(leftZ)
            ._m21(dirX * leftY - dirY * leftX)
            ._m22(dirZ)
            ._m23(0.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f)
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f setLookAt(Vector3fc eye, Vector3fc center, Vector3fc up) {
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
    public Matrix4f setLookAt(float eyeX, float eyeY, float eyeZ,
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
        return this
        ._m00(leftX)
        ._m01(upnX)
        ._m02(dirX)
        ._m03(0.0f)
        ._m10(leftY)
        ._m11(upnY)
        ._m12(dirY)
        ._m13(0.0f)
        ._m20(leftZ)
        ._m21(upnZ)
        ._m22(dirZ)
        ._m23(0.0f)
        ._m30(-(leftX * eyeX + leftY * eyeY + leftZ * eyeZ))
        ._m31(-(upnX * eyeX + upnY * eyeY + upnZ * eyeZ))
        ._m32(-(dirX * eyeX + dirY * eyeY + dirZ * eyeZ))
        ._m33(1.0f)
        ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f lookAt(Vector3fc eye, Vector3fc center, Vector3fc up, Matrix4f dest) {
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
    public Matrix4f lookAt(Vector3fc eye, Vector3fc center, Vector3fc up) {
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
    public Matrix4f lookAt(float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0)
            return lookAtPerspective(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
        return lookAtGeneric(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
    }
    private Matrix4f lookAtGeneric(float eyeX, float eyeY, float eyeZ,
                                   float centerX, float centerY, float centerZ,
                                   float upX, float upY, float upZ, Matrix4f dest) {
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        // introduce temporaries for dependent results
        float nm00 = m00 * leftX + m10 * upnX + m20 * dirX;
        float nm01 = m01 * leftX + m11 * upnX + m21 * dirX;
        float nm02 = m02 * leftX + m12 * upnX + m22 * dirX;
        float nm03 = m03 * leftX + m13 * upnX + m23 * dirX;
        float nm10 = m00 * leftY + m10 * upnY + m20 * dirY;
        float nm11 = m01 * leftY + m11 * upnY + m21 * dirY;
        float nm12 = m02 * leftY + m12 * upnY + m22 * dirY;
        float nm13 = m03 * leftY + m13 * upnY + m23 * dirY;

        // perform optimized matrix multiplication
        // compute last column first, because others do not depend on it
        return dest
        ._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
        ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
        ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
        ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33)
        ._m20(m00 * leftZ + m10 * upnZ + m20 * dirZ)
        ._m21(m01 * leftZ + m11 * upnZ + m21 * dirZ)
        ._m22(m02 * leftZ + m12 * upnZ + m22 * dirZ)
        ._m23(m03 * leftZ + m13 * upnZ + m23 * dirZ)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply a "lookat" transformation to this matrix for a right-handed coordinate system, 
     * that aligns <code>-z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to be a perspective transformation, obtained via
     * {@link #frustum(float, float, float, float, float, float) frustum()} or {@link #perspective(float, float, float, float) perspective()} or
     * one of their overloads.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAt(float, float, float, float, float, float, float, float, float) setLookAt()}.
     * 
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
    public Matrix4f lookAtPerspective(float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ, Matrix4f dest) {
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        float nm10 = m00 * leftY;
        float nm20 = m00 * leftZ;
        float nm21 = m11 * upnZ;
        float nm30 = m00 * rm30;
        float nm31 = m11 * rm31;
        float nm32 = m22 * rm32 + m32;
        float nm33 = m23 * rm32;
        return dest
        ._m00(m00 * leftX)
        ._m01(m11 * upnX)
        ._m02(m22 * dirX)
        ._m03(m23 * dirX)
        ._m10(nm10)
        ._m11(m11 * upnY)
        ._m12(m22 * dirY)
        ._m13(m23 * dirY)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(m22 * dirZ)
        ._m23(m23 * dirZ)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
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
    public Matrix4f lookAt(float eyeX, float eyeY, float eyeZ,
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
    public Matrix4f setLookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up) {
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
    public Matrix4f setLookAtLH(float eyeX, float eyeY, float eyeZ,
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
        this._m00(leftX)
            ._m01(upnX)
            ._m02(dirX)
            ._m03(0.0f)
            ._m10(leftY)
            ._m11(upnY)
            ._m12(dirY)
            ._m13(0.0f)
            ._m20(leftZ)
            ._m21(upnZ)
            ._m22(dirZ)
            ._m23(0.0f)
            ._m30(-(leftX * eyeX + leftY * eyeY + leftZ * eyeZ))
            ._m31(-(upnX * eyeX + upnY * eyeY + upnZ * eyeZ))
            ._m32(-(dirX * eyeX + dirY * eyeY + dirZ * eyeZ))
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f lookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up, Matrix4f dest) {
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
    public Matrix4f lookAtLH(Vector3fc eye, Vector3fc center, Vector3fc up) {
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
    public Matrix4f lookAtLH(float eyeX, float eyeY, float eyeZ,
                             float centerX, float centerY, float centerZ,
                             float upX, float upY, float upZ, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setLookAtLH(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0)
            return lookAtPerspectiveLH(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
        return lookAtLHGeneric(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, dest);
    }
    private Matrix4f lookAtLHGeneric(float eyeX, float eyeY, float eyeZ,
                                     float centerX, float centerY, float centerZ,
                                     float upX, float upY, float upZ, Matrix4f dest) {
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        // introduce temporaries for dependent results
        float nm00 = m00 * leftX + m10 * upnX + m20 * dirX;
        float nm01 = m01 * leftX + m11 * upnX + m21 * dirX;
        float nm02 = m02 * leftX + m12 * upnX + m22 * dirX;
        float nm03 = m03 * leftX + m13 * upnX + m23 * dirX;
        float nm10 = m00 * leftY + m10 * upnY + m20 * dirY;
        float nm11 = m01 * leftY + m11 * upnY + m21 * dirY;
        float nm12 = m02 * leftY + m12 * upnY + m22 * dirY;
        float nm13 = m03 * leftY + m13 * upnY + m23 * dirY;

        // perform optimized matrix multiplication
        // compute last column first, because others do not depend on it
        return dest
        ._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
        ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
        ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
        ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33)
        ._m20(m00 * leftZ + m10 * upnZ + m20 * dirZ)
        ._m21(m01 * leftZ + m11 * upnZ + m21 * dirZ)
        ._m22(m02 * leftZ + m12 * upnZ + m22 * dirZ)
        ._m23(m03 * leftZ + m13 * upnZ + m23 * dirZ)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f lookAtLH(float eyeX, float eyeY, float eyeZ,
                             float centerX, float centerY, float centerZ,
                             float upX, float upY, float upZ) {
        return lookAtLH(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ, this);
    }

    /**
     * Apply a "lookat" transformation to this matrix for a left-handed coordinate system, 
     * that aligns <code>+z</code> with <code>center - eye</code> and store the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to be a perspective transformation, obtained via
     * {@link #frustumLH(float, float, float, float, float, float) frustumLH()} or {@link #perspectiveLH(float, float, float, float) perspectiveLH()} or
     * one of their overloads.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>,
     * the lookat transformation will be applied first!
     * <p>
     * In order to set the matrix to a lookat transformation without post-multiplying it,
     * use {@link #setLookAtLH(float, float, float, float, float, float, float, float, float) setLookAtLH()}.
     * 
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
    public Matrix4f lookAtPerspectiveLH(float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ, Matrix4f dest) {
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
        float rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        float rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        float rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);

        float nm00 = m00 * leftX;
        float nm01 = m11 * upnX;
        float nm02 = m22 * dirX;
        float nm03 = m23 * dirX;
        float nm10 = m00 * leftY;
        float nm11 = m11 * upnY;
        float nm12 = m22 * dirY;
        float nm13 = m23 * dirY;
        float nm20 = m00 * leftZ;
        float nm21 = m11 * upnZ;
        float nm22 = m22 * dirZ;
        float nm23 = m23 * dirZ;
        float nm30 = m00 * rm30;
        float nm31 = m11 * rm31;
        float nm32 = m22 * rm32 + m32;
        float nm33 = m23 * rm32;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(nm23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(nm33)
        ._properties(0);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspective(float, float, float, float, boolean) setPerspective}.
     * 
     * @see #setPerspective(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return dest
     */
    public Matrix4f perspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setPerspective(fovy, aspect, zNear, zFar, zZeroToOne);
        return perspectiveGeneric(fovy, aspect, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f perspectiveGeneric(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        float h = Math.tan(fovy * 0.5f);
        // calculate right matrix elements
        float rm00 = 1.0f / (h * aspect);
        float rm11 = 1.0f / h;
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = e - 1.0f;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m20 * rm22 - m30;
        float nm21 = m21 * rm22 - m31;
        float nm22 = m22 * rm22 - m32;
        float nm23 = m23 * rm22 - m33;
        dest._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(properties & ~(PROPERTY_AFFINE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspective(float, float, float, float) setPerspective}.
     * 
     * @see #setPerspective(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f perspective(float fovy, float aspect, float zNear, float zFar, Matrix4f dest) {
        return perspective(fovy, aspect, zNear, zFar, false, dest);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation using for a right-handed coordinate system
     * the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspective(float, float, float, float, boolean) setPerspective}.
     * 
     * @see #setPerspective(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f perspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        return perspective(fovy, aspect, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspective(float, float, float, float) setPerspective}.
     * 
     * @see #setPerspective(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f perspective(float fovy, float aspect, float zNear, float zFar) {
        return perspective(fovy, aspect, zNear, zFar, this);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveRect(float, float, float, float, boolean) setPerspectiveRect}.
     * 
     * @see #setPerspectiveRect(float, float, float, float, boolean)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return dest
     */
    public Matrix4f perspectiveRect(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setPerspectiveRect(width, height, zNear, zFar, zZeroToOne);
        return perspectiveRectGeneric(width, height, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f perspectiveRectGeneric(float width, float height, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        float rm00 = (zNear + zNear) / width;
        float rm11 = (zNear + zNear) / height;
        float rm22, rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = e - 1.0f;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m20 * rm22 - m30;
        float nm21 = m21 * rm22 - m31;
        float nm22 = m22 * rm22 - m32;
        float nm23 = m23 * rm22 - m33;
        dest._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(properties & ~(PROPERTY_AFFINE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveRect(float, float, float, float) setPerspectiveRect}.
     * 
     * @see #setPerspectiveRect(float, float, float, float)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f perspectiveRect(float width, float height, float zNear, float zFar, Matrix4f dest) {
        return perspectiveRect(width, height, zNear, zFar, false, dest);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation using for a right-handed coordinate system
     * the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveRect(float, float, float, float, boolean) setPerspectiveRect}.
     * 
     * @see #setPerspectiveRect(float, float, float, float, boolean)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f perspectiveRect(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        return perspectiveRect(width, height, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveRect(float, float, float, float) setPerspectiveRect}.
     * 
     * @see #setPerspectiveRect(float, float, float, float)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f perspectiveRect(float width, float height, float zNear, float zFar) {
        return perspectiveRect(width, height, zNear, zFar, this);
    }

    /**
     * Apply an asymmetric off-center perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveOffCenter(float, float, float, float, float, float, boolean) setPerspectiveOffCenter}.
     * 
     * @see #setPerspectiveOffCenter(float, float, float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return dest
     */
    public Matrix4f perspectiveOffCenter(float fovy, float offAngleX, float offAngleY, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setPerspectiveOffCenter(fovy, offAngleX, offAngleY, aspect, zNear, zFar, zZeroToOne);
        return perspectiveOffCenterGeneric(fovy, offAngleX, offAngleY, aspect, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f perspectiveOffCenterGeneric(float fovy, float offAngleX, float offAngleY, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        float h = Math.tan(fovy * 0.5f);
        // calculate right matrix elements
        float xScale = 1.0f / (h * aspect);
        float yScale = 1.0f / h;
        float offX = Math.tan(offAngleX), offY = Math.tan(offAngleY);
        float rm20 = offX * xScale;
        float rm21 = offY * yScale;
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = e - 1.0f;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m00 * rm20 + m10 * rm21 + m20 * rm22 - m30;
        float nm21 = m01 * rm20 + m11 * rm21 + m21 * rm22 - m31;
        float nm22 = m02 * rm20 + m12 * rm21 + m22 * rm22 - m32;
        float nm23 = m03 * rm20 + m13 * rm21 + m23 * rm22 - m33;
        dest._m00(m00 * xScale)
            ._m01(m01 * xScale)
            ._m02(m02 * xScale)
            ._m03(m03 * xScale)
            ._m10(m10 * yScale)
            ._m11(m11 * yScale)
            ._m12(m12 * yScale)
            ._m13(m13 * yScale)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(properties & ~(PROPERTY_AFFINE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
                | PROPERTY_ORTHONORMAL | (rm20 == 0.0f && rm21 == 0.0f ? 0 : PROPERTY_PERSPECTIVE)));
        return dest;
    }

    /**
     * Apply an asymmetric off-center perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveOffCenter(float, float, float, float, float, float) setPerspectiveOffCenter}.
     * 
     * @see #setPerspectiveOffCenter(float, float, float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f perspectiveOffCenter(float fovy, float offAngleX, float offAngleY, float aspect, float zNear, float zFar, Matrix4f dest) {
        return perspectiveOffCenter(fovy, offAngleX, offAngleY, aspect, zNear, zFar, false, dest);
    }

    /**
     * Apply an asymmetric off-center perspective projection frustum transformation using for a right-handed coordinate system
     * the given NDC z range to this matrix.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveOffCenter(float, float, float, float, float, float, boolean) setPerspectiveOffCenter}.
     * 
     * @see #setPerspectiveOffCenter(float, float, float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f perspectiveOffCenter(float fovy, float offAngleX, float offAngleY, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        return perspectiveOffCenter(fovy, offAngleX, offAngleY, aspect, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply an asymmetric off-center perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveOffCenter(float, float, float, float, float, float) setPerspectiveOffCenter}.
     * 
     * @see #setPerspectiveOffCenter(float, float, float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f perspectiveOffCenter(float fovy, float offAngleX, float offAngleY, float aspect, float zNear, float zFar) {
        return perspectiveOffCenter(fovy, offAngleX, offAngleY, aspect, zNear, zFar, this);
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspective(float, float, float, float, boolean) perspective()}.
     * 
     * @see #perspective(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setPerspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.zero(this);
        float h = Math.tan(fovy * 0.5f);
        this._m00(1.0f / (h * aspect))
            ._m11(1.0f / h);
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(e - 1.0f)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        return this
        ._m23(-1.0f)
        ._properties(PROPERTY_PERSPECTIVE);
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspective(float, float, float, float) perspective()}.
     * 
     * @see #perspective(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setPerspective(float fovy, float aspect, float zNear, float zFar) {
        return setPerspective(fovy, aspect, zNear, zFar, false);
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveRect(float, float, float, float, boolean) perspectiveRect()}.
     * 
     * @see #perspectiveRect(float, float, float, float, boolean)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setPerspectiveRect(float width, float height, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.zero(this);
        this._m00((zNear + zNear) / width)
            ._m11((zNear + zNear) / height);
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(e - 1.0f)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        this._m23(-1.0f)
            ._properties(PROPERTY_PERSPECTIVE);
        return this;
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveRect(float, float, float, float) perspectiveRect()}.
     * 
     * @see #perspectiveRect(float, float, float, float)
     * 
     * @param width
     *            the width of the near frustum plane
     * @param height
     *            the height of the near frustum plane
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setPerspectiveRect(float width, float height, float zNear, float zFar) {
        return setPerspectiveRect(width, height, zNear, zFar, false);
    }

    /**
     * Set this matrix to be an asymmetric off-center perspective projection frustum transformation for a right-handed
     * coordinate system using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveOffCenter(float, float, float, float, float, float) perspectiveOffCenter()}.
     * 
     * @see #perspectiveOffCenter(float, float, float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setPerspectiveOffCenter(float fovy, float offAngleX, float offAngleY,
            float aspect, float zNear, float zFar) {
        return setPerspectiveOffCenter(fovy, offAngleX, offAngleY, aspect, zNear, zFar, false);
    }
    /**
     * Set this matrix to be an asymmetric off-center perspective projection frustum transformation for a right-handed
     * coordinate system using the given NDC z range.
     * <p>
     * The given angles <code>offAngleX</code> and <code>offAngleY</code> are the horizontal and vertical angles between
     * the line of sight and the line given by the center of the near and far frustum planes. So, when <code>offAngleY</code>
     * is just <code>fovy/2</code> then the projection frustum is rotated towards +Y and the bottom frustum plane 
     * is parallel to the XZ-plane.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveOffCenter(float, float, float, float, float, float) perspectiveOffCenter()}.
     * 
     * @see #perspectiveOffCenter(float, float, float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param offAngleX
     *            the horizontal angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param offAngleY
     *            the vertical angle between the line of sight and the line crossing the center of the near and far frustum planes
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setPerspectiveOffCenter(float fovy, float offAngleX, float offAngleY,
                                            float aspect, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.zero(this);
        float h = Math.tan(fovy * 0.5f);
        float xScale = 1.0f / (h * aspect), yScale = 1.0f / h;
        float offX = Math.tan(offAngleX), offY = Math.tan(offAngleY);
        this._m00(xScale)
            ._m11(yScale)
            ._m20(offX * xScale)
            ._m21(offY * yScale);
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(e - 1.0f)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        this._m23(-1.0f)
            ._properties(offAngleX == 0.0f && offAngleY == 0.0f ? PROPERTY_PERSPECTIVE : 0);
        return this;
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveLH(float, float, float, float, boolean) setPerspectiveLH}.
     * 
     * @see #setPerspectiveLH(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f perspectiveLH(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setPerspectiveLH(fovy, aspect, zNear, zFar, zZeroToOne);
        return perspectiveLHGeneric(fovy, aspect, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f perspectiveLHGeneric(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        float h = Math.tan(fovy * 0.5f);
        // calculate right matrix elements
        float rm00 = 1.0f / (h * aspect);
        float rm11 = 1.0f / h;
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = 1.0f - e;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zFar - zNear);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m20 * rm22 + m30;
        float nm21 = m21 * rm22 + m31;
        float nm22 = m22 * rm22 + m32;
        float nm23 = m23 * rm22 + m33;
        dest._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(properties & ~(PROPERTY_AFFINE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveLH(float, float, float, float, boolean) setPerspectiveLH}.
     * 
     * @see #setPerspectiveLH(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f perspectiveLH(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        return perspectiveLH(fovy, aspect, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveLH(float, float, float, float) setPerspectiveLH}.
     * 
     * @see #setPerspectiveLH(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f perspectiveLH(float fovy, float aspect, float zNear, float zFar, Matrix4f dest) {
        return perspectiveLH(fovy, aspect, zNear, zFar, false, dest);
    }

    /**
     * Apply a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>P</code> the perspective projection matrix,
     * then the new matrix will be <code>M * P</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * P * v</code>,
     * the perspective projection will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setPerspectiveLH(float, float, float, float) setPerspectiveLH}.
     * 
     * @see #setPerspectiveLH(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f perspectiveLH(float fovy, float aspect, float zNear, float zFar) {
        return perspectiveLH(fovy, aspect, zNear, zFar, this);
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveLH(float, float, float, float, boolean) perspectiveLH()}.
     * 
     * @see #perspectiveLH(float, float, float, float, boolean)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setPerspectiveLH(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        MemUtil.INSTANCE.zero(this);
        float h = Math.tan(fovy * 0.5f);
        this._m00(1.0f / (h * aspect))
            ._m11(1.0f / h);
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(1.0f - e)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zFar - zNear))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        this._m23(1.0f)
            ._properties(PROPERTY_PERSPECTIVE);
        return this;
    }

    /**
     * Set this matrix to be a symmetric perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective projection transformation to an existing transformation,
     * use {@link #perspectiveLH(float, float, float, float) perspectiveLH()}.
     * 
     * @see #perspectiveLH(float, float, float, float)
     * 
     * @param fovy
     *            the vertical field of view in radians (must be greater than zero and less than {@link Math#PI PI})
     * @param aspect
     *            the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setPerspectiveLH(float fovy, float aspect, float zNear, float zFar) {
        return setPerspectiveLH(fovy, aspect, zNear, zFar, false);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustum(float, float, float, float, float, float, boolean) setFrustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustum(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f frustum(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setFrustum(left, right, bottom, top, zNear, zFar, zZeroToOne);
        return frustumGeneric(left, right, bottom, top, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f frustumGeneric(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = (zNear + zNear) / (right - left);
        float rm11 = (zNear + zNear) / (top - bottom);
        float rm20 = (right + left) / (right - left);
        float rm21 = (top + bottom) / (top - bottom);
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = e - 1.0f;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m00 * rm20 + m10 * rm21 + m20 * rm22 - m30;
        float nm21 = m01 * rm20 + m11 * rm21 + m21 * rm22 - m31;
        float nm22 = m02 * rm20 + m12 * rm21 + m22 * rm22 - m32;
        float nm23 = m03 * rm20 + m13 * rm21 + m23 * rm22 - m33;
        dest._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(0);
        return dest;
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustum(float, float, float, float, float, float) setFrustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustum(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f frustum(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4f dest) {
        return frustum(left, right, bottom, top, zNear, zFar, false, dest);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustum(float, float, float, float, float, float, boolean) setFrustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustum(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f frustum(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        return frustum(left, right, bottom, top, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustum(float, float, float, float, float, float) setFrustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustum(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f frustum(float left, float right, float bottom, float top, float zNear, float zFar) {
        return frustum(left, right, bottom, top, zNear, zFar, this);
    }

    /**
     * Set this matrix to be an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using the given NDC z range.
     * <p>
     * In order to apply the perspective frustum transformation to an existing transformation,
     * use {@link #frustum(float, float, float, float, float, float, boolean) frustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #frustum(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setFrustum(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00((zNear + zNear) / (right - left))
            ._m11((zNear + zNear) / (top - bottom))
            ._m20((right + left) / (right - left))
            ._m21((top + bottom) / (top - bottom));
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(e - 1.0f)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        this._m23(-1.0f)
            ._m33(0.0f)
            ._properties(this.m20 == 0.0f && this.m21 == 0.0f ? PROPERTY_PERSPECTIVE : 0);
        return this;
    }

    /**
     * Set this matrix to be an arbitrary perspective projection frustum transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective frustum transformation to an existing transformation,
     * use {@link #frustum(float, float, float, float, float, float) frustum()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #frustum(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setFrustum(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setFrustum(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustumLH(float, float, float, float, float, float, boolean) setFrustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustumLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f frustumLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.setFrustumLH(left, right, bottom, top, zNear, zFar, zZeroToOne);
        return frustumLHGeneric(left, right, bottom, top, zNear, zFar, zZeroToOne, dest);
    }
    private Matrix4f frustumLHGeneric(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
        // calculate right matrix elements
        float rm00 = (zNear + zNear) / (right - left);
        float rm11 = (zNear + zNear) / (top - bottom);
        float rm20 = (right + left) / (right - left);
        float rm21 = (top + bottom) / (top - bottom);
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = 1.0f - e;
            rm32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            rm32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            rm22 = (zZeroToOne ? zFar : zFar + zNear) / (zFar - zNear);
            rm32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m00 * rm20 + m10 * rm21 + m20 * rm22 + m30;
        float nm21 = m01 * rm20 + m11 * rm21 + m21 * rm22 + m31;
        float nm22 = m02 * rm20 + m12 * rm21 + m22 * rm22 + m32;
        float nm23 = m03 * rm20 + m13 * rm21 + m23 * rm22 + m33;
        dest._m00(m00 * rm00)
            ._m01(m01 * rm00)
            ._m02(m02 * rm00)
            ._m03(m03 * rm00)
            ._m10(m10 * rm11)
            ._m11(m11 * rm11)
            ._m12(m12 * rm11)
            ._m13(m13 * rm11)
            ._m30(m20 * rm32)
            ._m31(m21 * rm32)
            ._m32(m22 * rm32)
            ._m33(m23 * rm32)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(0);
        return dest;
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustumLH(float, float, float, float, float, float, boolean) setFrustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustumLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f frustumLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        return frustumLH(left, right, bottom, top, zNear, zFar, zZeroToOne, this);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code> to this matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustumLH(float, float, float, float, float, float) setFrustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustumLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix4f frustumLH(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4f dest) {
        return frustumLH(left, right, bottom, top, zNear, zFar, false, dest);
    }

    /**
     * Apply an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using the given NDC z range to this matrix.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum matrix,
     * then the new matrix will be <code>M * F</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * F * v</code>,
     * the frustum transformation will be applied first!
     * <p>
     * In order to set the matrix to a perspective frustum transformation without post-multiplying,
     * use {@link #setFrustumLH(float, float, float, float, float, float) setFrustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #setFrustumLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f frustumLH(float left, float right, float bottom, float top, float zNear, float zFar) {
        return frustumLH(left, right, bottom, top, zNear, zFar, this);
    }

    /**
     * Set this matrix to be an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective frustum transformation to an existing transformation,
     * use {@link #frustumLH(float, float, float, float, float, float, boolean) frustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #frustumLH(float, float, float, float, float, float, boolean)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zZeroToOne
     *            whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *            or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @return this
     */
    public Matrix4f setFrustumLH(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00((zNear + zNear) / (right - left))
            ._m11((zNear + zNear) / (top - bottom))
            ._m20((right + left) / (right - left))
            ._m21((top + bottom) / (top - bottom));
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            this._m22(1.0f - e)
                ._m32((e - (zZeroToOne ? 1.0f : 2.0f)) * zNear);
        } else if (nearInf) {
            float e = 1E-6f;
            this._m22((zZeroToOne ? 0.0f : 1.0f) - e)
                ._m32(((zZeroToOne ? 1.0f : 2.0f) - e) * zFar);
        } else {
            this._m22((zZeroToOne ? zFar : zFar + zNear) / (zFar - zNear))
                ._m32((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        return this
        ._m23(1.0f)
        ._m33(0.0f)
        ._properties(0);
    }

    /**
     * Set this matrix to be an arbitrary perspective projection frustum transformation for a left-handed coordinate system
     * using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * In order to apply the perspective frustum transformation to an existing transformation,
     * use {@link #frustumLH(float, float, float, float, float, float) frustumLH()}.
     * <p>
     * Reference: <a href="http://www.songho.ca/opengl/gl_projectionmatrix.html#perspective">http://www.songho.ca</a>
     * 
     * @see #frustumLH(float, float, float, float, float, float)
     * 
     * @param left
     *            the distance along the x-axis to the left frustum edge
     * @param right
     *            the distance along the x-axis to the right frustum edge
     * @param bottom
     *            the distance along the y-axis to the bottom frustum edge
     * @param top
     *            the distance along the y-axis to the top frustum edge
     * @param zNear
     *            near clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the near clipping plane will be at positive infinity.
     *            In that case, <code>zFar</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @param zFar
     *            far clipping plane distance. If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *            In that case, <code>zNear</code> may not also be {@link Float#POSITIVE_INFINITY}.
     * @return this
     */
    public Matrix4f setFrustumLH(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setFrustumLH(left, right, bottom, top, zNear, zFar, false);
    }

    /**
     * Set this matrix to represent a perspective projection equivalent to the given intrinsic camera calibration parameters.
     * The resulting matrix will be suited for a right-handed coordinate system using OpenGL's NDC z range of <code>[-1..+1]</code>.
     * <p>
     * See: <a href="https://en.wikipedia.org/wiki/Camera_resectioning#Intrinsic_parameters">https://en.wikipedia.org/</a>
     * <p>
     * Reference: <a href="http://ksimek.github.io/2013/06/03/calibrated_cameras_in_opengl/">http://ksimek.github.io/</a>
     * 
     * @param alphaX
     *          specifies the focal length and scale along the X axis
     * @param alphaY
     *          specifies the focal length and scale along the Y axis
     * @param gamma
     *          the skew coefficient between the X and Y axis (may be <code>0</code>)
     * @param u0
     *          the X coordinate of the principal point in image/sensor units
     * @param v0
     *          the Y coordinate of the principal point in image/sensor units
     * @param imgWidth
     *          the width of the sensor/image image/sensor units
     * @param imgHeight
     *          the height of the sensor/image image/sensor units
     * @param near
     *          the distance to the near plane
     * @param far
     *          the distance to the far plane
     * @return this
     */
    public Matrix4f setFromIntrinsic(float alphaX, float alphaY, float gamma, float u0, float v0, int imgWidth, int imgHeight, float near, float far) {
        float l00 = 2.0f / imgWidth;
        float l11 = 2.0f / imgHeight;
        float l22 = 2.0f / (near - far);
        this.m00 = l00 * alphaX;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = 0.0f;
        this.m10 = l00 * gamma;
        this.m11 = l11 * alphaY;
        this.m12 = 0.0f;
        this.m13 = 0.0f;
        this.m20 = l00 * u0 - 1.0f;
        this.m21 = l11 * v0 - 1.0f;
        this.m22 = l22 * -(near + far) + (far + near) / (near - far);
        this.m23 = -1.0f;
        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = l22 * -near * far;
        this.m33 = 0.0f;
        this.properties = PROPERTY_PERSPECTIVE;
        return this;
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this matrix and store
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
    public Matrix4f rotate(Quaternionfc quat, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.rotation(quat);
        else if ((properties & PROPERTY_TRANSLATION) != 0)
            return rotateTranslation(quat, dest);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return rotateAffine(quat, dest);
        return rotateGeneric(quat, dest);
    }
    private Matrix4f rotateGeneric(Quaternionfc quat, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
        float rm11 = y2 - z2 + w2 - x2;
        float rm12 = dyz + dxw;
        float rm20 = dyw + dxz;
        float rm21 = dyz - dxw;
        float rm22 = z2 - y2 - x2 + w2;
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
        return dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m23(m03 * rm20 + m13 * rm21 + m23 * rm22)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(nm03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(nm13)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this matrix.
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
    public Matrix4f rotate(Quaternionfc quat) {
        return rotate(quat, this);
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this {@link #isAffine() affine} matrix and store
     * the result in <code>dest</code>.
     * <p>
     * This method assumes <code>this</code> to be {@link #isAffine() affine}.
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
    public Matrix4f rotateAffine(Quaternionfc quat, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
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
        return dest
        ._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
        ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
        ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
        ._m23(0.0f)
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this matrix.
     * <p>
     * This method assumes <code>this</code> to be {@link #isAffine() affine}.
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
    public Matrix4f rotateAffine(Quaternionfc quat) {
        return rotateAffine(quat, this);
    }

    /**
     * Apply the rotation transformation of the given {@link Quaternionfc} to this matrix, which is assumed to only contain a translation, and store
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
    public Matrix4f rotateTranslation(Quaternionfc quat, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
        float rm11 = y2 - z2 + w2 - x2;
        float rm12 = dyz + dxw;
        float rm20 = dyw + dxz;
        float rm21 = dyz - dxw;
        float rm22 = z2 - y2 - x2 + w2;
        return dest
        ._m20(rm20)
        ._m21(rm21)
        ._m22(rm22)
        ._m23(0.0f)
        ._m00(rm00)
        ._m01(rm01)
        ._m02(rm02)
        ._m03(0.0f)
        ._m10(rm10)
        ._m11(rm11)
        ._m12(rm12)
        ._m13(0.0f)
        ._m30(m30)
        ._m31(m31)
        ._m32(m32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f rotateAround(Quaternionfc quat, float ox, float oy, float oz) {
        return rotateAround(quat, ox, oy, oz, this);
    }

    public Matrix4f rotateAroundAffine(Quaternionfc quat, float ox, float oy, float oz, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
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
        dest._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
            ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
            ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
            ._m23(0.0f)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(0.0f)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(0.0f)
            ._m30(-nm00 * ox - nm10 * oy - m20 * oz + tm30)
            ._m31(-nm01 * ox - nm11 * oy - m21 * oz + tm31)
            ._m32(-nm02 * ox - nm12 * oy - m22 * oz + tm32)
            ._m33(1.0f)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    public Matrix4f rotateAround(Quaternionfc quat, float ox, float oy, float oz, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return rotationAround(quat, ox, oy, oz);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return rotateAroundAffine(quat, ox, oy, oz, dest);
        return rotateAroundGeneric(quat, ox, oy, oz, dest);
    }
    private Matrix4f rotateAroundGeneric(Quaternionfc quat, float ox, float oy, float oz, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
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
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
        dest._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
            ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
            ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
            ._m23(m03 * rm20 + m13 * rm21 + m23 * rm22)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(nm03)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(nm13)
            ._m30(-nm00 * ox - nm10 * oy - m20 * oz + tm30)
            ._m31(-nm01 * ox - nm11 * oy - m21 * oz + tm31)
            ._m32(-nm02 * ox - nm12 * oy - m22 * oz + tm32)
            ._m33(m33)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
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
    public Matrix4f rotationAround(Quaternionfc quat, float ox, float oy, float oz) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        this._m20(dyw + dxz)
            ._m21(dyz - dxw)
            ._m22(z2 - y2 - x2 + w2)
            ._m23(0.0f)
            ._m00(w2 + x2 - z2 - y2)
            ._m01(dxy + dzw)
            ._m02(dxz - dyw)
            ._m03(0.0f)
            ._m10(-dzw + dxy)
            ._m11(y2 - z2 + w2 - x2)
            ._m12(dyz + dxw)
            ._m13(0.0f)
            ._m30(-m00 * ox - m10 * oy - m20 * oz + ox)
            ._m31(-m01 * ox - m11 * oy - m21 * oz + oy)
            ._m32(-m02 * ox - m12 * oy - m22 * oz + oz)
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
        return this;
    }

    /**
     * Pre-multiply the rotation transformation of the given {@link Quaternionfc} to this matrix and store
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
    public Matrix4f rotateLocal(Quaternionfc quat, Matrix4f dest) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float lm00 = w2 + x2 - z2 - y2;
        float lm01 = dxy + dzw;
        float lm02 = dxz - dyw;
        float lm10 = -dzw + dxy;
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
        float nm30 = lm00 * m30 + lm10 * m31 + lm20 * m32;
        float nm31 = lm01 * m30 + lm11 * m31 + lm21 * m32;
        float nm32 = lm02 * m30 + lm12 * m31 + lm22 * m32;
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(m03)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(m13)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(m23)
        ._m30(nm30)
        ._m31(nm31)
        ._m32(nm32)
        ._m33(m33)
        ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
    }

    /**
     * Pre-multiply the rotation transformation of the given {@link Quaternionfc} to this matrix.
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
    public Matrix4f rotateLocal(Quaternionfc quat) {
        return rotateLocal(quat, this);
    }

    public Matrix4f rotateAroundLocal(Quaternionfc quat, float ox, float oy, float oz, Matrix4f dest) {
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
        float tm00 = m00 - ox * m03;
        float tm01 = m01 - oy * m03;
        float tm02 = m02 - oz * m03;
        float tm10 = m10 - ox * m13;
        float tm11 = m11 - oy * m13;
        float tm12 = m12 - oz * m13;
        float tm20 = m20 - ox * m23;
        float tm21 = m21 - oy * m23;
        float tm22 = m22 - oz * m23;
        float tm30 = m30 - ox * m33;
        float tm31 = m31 - oy * m33;
        float tm32 = m32 - oz * m33;
        dest._m00(lm00 * tm00 + lm10 * tm01 + lm20 * tm02 + ox * m03)
            ._m01(lm01 * tm00 + lm11 * tm01 + lm21 * tm02 + oy * m03)
            ._m02(lm02 * tm00 + lm12 * tm01 + lm22 * tm02 + oz * m03)
            ._m03(m03)
            ._m10(lm00 * tm10 + lm10 * tm11 + lm20 * tm12 + ox * m13)
            ._m11(lm01 * tm10 + lm11 * tm11 + lm21 * tm12 + oy * m13)
            ._m12(lm02 * tm10 + lm12 * tm11 + lm22 * tm12 + oz * m13)
            ._m13(m13)
            ._m20(lm00 * tm20 + lm10 * tm21 + lm20 * tm22 + ox * m23)
            ._m21(lm01 * tm20 + lm11 * tm21 + lm21 * tm22 + oy * m23)
            ._m22(lm02 * tm20 + lm12 * tm21 + lm22 * tm22 + oz * m23)
            ._m23(m23)
            ._m30(lm00 * tm30 + lm10 * tm31 + lm20 * tm32 + ox * m33)
            ._m31(lm01 * tm30 + lm11 * tm31 + lm21 * tm32 + oy * m33)
            ._m32(lm02 * tm30 + lm12 * tm31 + lm22 * tm32 + oz * m33)
            ._m33(m33)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    /**
     * Pre-multiply the rotation transformation of the given {@link Quaternionfc} to this matrix while using <code>(ox, oy, oz)</code>
     * as the rotation origin.
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
     * This method is equivalent to calling: <code>translateLocal(-ox, -oy, -oz).rotateLocal(quat).translateLocal(ox, oy, oz)</code>
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
    public Matrix4f rotateAroundLocal(Quaternionfc quat, float ox, float oy, float oz) {
        return rotateAroundLocal(quat, ox, oy, oz, this);
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
    public Matrix4f rotate(AxisAngle4f axisAngle) {
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
    public Matrix4f rotate(AxisAngle4f axisAngle, Matrix4f dest) {
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
    public Matrix4f rotate(float angle, Vector3fc axis) {
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
    public Matrix4f rotate(float angle, Vector3fc axis, Matrix4f dest) {
        return rotate(angle, axis.x(), axis.y(), axis.z(), dest);
    }

    public Vector4f unproject(float winX, float winY, float winZ, int[] viewport, Vector4f dest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        float im00 = ( m11 * l - m12 * k + m13 * j) * det;
        float im01 = (-m01 * l + m02 * k - m03 * j) * det;
        float im02 = ( m31 * f - m32 * e + m33 * d) * det;
        float im03 = (-m21 * f + m22 * e - m23 * d) * det;
        float im10 = (-m10 * l + m12 * i - m13 * h) * det;
        float im11 = ( m00 * l - m02 * i + m03 * h) * det;
        float im12 = (-m30 * f + m32 * c - m33 * b) * det;
        float im13 = ( m20 * f - m22 * c + m23 * b) * det;
        float im20 = ( m10 * k - m11 * i + m13 * g) * det;
        float im21 = (-m00 * k + m01 * i - m03 * g) * det;
        float im22 = ( m30 * e - m31 * c + m33 * a) * det;
        float im23 = (-m20 * e + m21 * c - m23 * a) * det;
        float im30 = (-m10 * j + m11 * h - m12 * g) * det;
        float im31 = ( m00 * j - m01 * h + m02 * g) * det;
        float im32 = (-m30 * d + m31 * b - m32 * a) * det;
        float im33 = ( m20 * d - m21 * b + m22 * a) * det;
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float ndcZ = winZ+winZ-1.0f;
        float invW = 1.0f / (im03 * ndcX + im13 * ndcY + im23 * ndcZ + im33);
        return dest.set((im00 * ndcX + im10 * ndcY + im20 * ndcZ + im30) * invW,
                        (im01 * ndcX + im11 * ndcY + im21 * ndcZ + im31) * invW,
                        (im02 * ndcX + im12 * ndcY + im22 * ndcZ + im32) * invW,
                        1.0f);
    }

    public Vector3f unproject(float winX, float winY, float winZ, int[] viewport, Vector3f dest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        float im00 = ( m11 * l - m12 * k + m13 * j) * det;
        float im01 = (-m01 * l + m02 * k - m03 * j) * det;
        float im02 = ( m31 * f - m32 * e + m33 * d) * det;
        float im03 = (-m21 * f + m22 * e - m23 * d) * det;
        float im10 = (-m10 * l + m12 * i - m13 * h) * det;
        float im11 = ( m00 * l - m02 * i + m03 * h) * det;
        float im12 = (-m30 * f + m32 * c - m33 * b) * det;
        float im13 = ( m20 * f - m22 * c + m23 * b) * det;
        float im20 = ( m10 * k - m11 * i + m13 * g) * det;
        float im21 = (-m00 * k + m01 * i - m03 * g) * det;
        float im22 = ( m30 * e - m31 * c + m33 * a) * det;
        float im23 = (-m20 * e + m21 * c - m23 * a) * det;
        float im30 = (-m10 * j + m11 * h - m12 * g) * det;
        float im31 = ( m00 * j - m01 * h + m02 * g) * det;
        float im32 = (-m30 * d + m31 * b - m32 * a) * det;
        float im33 = ( m20 * d - m21 * b + m22 * a) * det;
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float ndcZ = winZ+winZ-1.0f;
        float invW = 1.0f / (im03 * ndcX + im13 * ndcY + im23 * ndcZ + im33);
        return dest.set((im00 * ndcX + im10 * ndcY + im20 * ndcZ + im30) * invW,
                        (im01 * ndcX + im11 * ndcY + im21 * ndcZ + im31) * invW,
                        (im02 * ndcX + im12 * ndcY + im22 * ndcZ + im32) * invW);
    }

    public Vector4f unproject(Vector3fc winCoords, int[] viewport, Vector4f dest) {
        return unproject(winCoords.x(), winCoords.y(), winCoords.z(), viewport, dest);
    }

    public Vector3f unproject(Vector3fc winCoords, int[] viewport, Vector3f dest) {
        return unproject(winCoords.x(), winCoords.y(), winCoords.z(), viewport, dest);
    }

    public Matrix4f unprojectRay(float winX, float winY, int[] viewport, Vector3f originDest, Vector3f dirDest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        float im00 = ( m11 * l - m12 * k + m13 * j) * det;
        float im01 = (-m01 * l + m02 * k - m03 * j) * det;
        float im02 = ( m31 * f - m32 * e + m33 * d) * det;
        float im03 = (-m21 * f + m22 * e - m23 * d) * det;
        float im10 = (-m10 * l + m12 * i - m13 * h) * det;
        float im11 = ( m00 * l - m02 * i + m03 * h) * det;
        float im12 = (-m30 * f + m32 * c - m33 * b) * det;
        float im13 = ( m20 * f - m22 * c + m23 * b) * det;
        float im20 = ( m10 * k - m11 * i + m13 * g) * det;
        float im21 = (-m00 * k + m01 * i - m03 * g) * det;
        float im22 = ( m30 * e - m31 * c + m33 * a) * det;
        float im23 = (-m20 * e + m21 * c - m23 * a) * det;
        float im30 = (-m10 * j + m11 * h - m12 * g) * det;
        float im31 = ( m00 * j - m01 * h + m02 * g) * det;
        float im32 = (-m30 * d + m31 * b - m32 * a) * det;
        float im33 = ( m20 * d - m21 * b + m22 * a) * det;
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float px = im00 * ndcX + im10 * ndcY + im30;
        float py = im01 * ndcX + im11 * ndcY + im31;
        float pz = im02 * ndcX + im12 * ndcY + im32;
        float invNearW = 1.0f / (im03 * ndcX + im13 * ndcY - im23 + im33);
        float nearX = (px - im20) * invNearW;
        float nearY = (py - im21) * invNearW;
        float nearZ = (pz - im22) * invNearW;
        float invW0 = 1.0f / (im03 * ndcX + im13 * ndcY + im33);
        float x0 = px * invW0;
        float y0 = py * invW0;
        float z0 = pz * invW0;
        originDest.x = nearX; originDest.y = nearY; originDest.z = nearZ;
        dirDest.x = x0 - nearX; dirDest.y = y0 - nearY; dirDest.z = z0 - nearZ;
        return this;
    }

    public Matrix4f unprojectRay(Vector2fc winCoords, int[] viewport, Vector3f originDest, Vector3f dirDest) {
        return unprojectRay(winCoords.x(), winCoords.y(), viewport, originDest, dirDest);
    }

    public Vector4f unprojectInv(Vector3fc winCoords, int[] viewport, Vector4f dest) {
        return unprojectInv(winCoords.x(), winCoords.y(), winCoords.z(), viewport, dest);
    }

    public Vector4f unprojectInv(float winX, float winY, float winZ, int[] viewport, Vector4f dest) {
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float ndcZ = winZ+winZ-1.0f;
        float invW = 1.0f / (m03 * ndcX + m13 * ndcY + m23 * ndcZ + m33);
        return dest.set((m00 * ndcX + m10 * ndcY + m20 * ndcZ + m30) * invW,
                        (m01 * ndcX + m11 * ndcY + m21 * ndcZ + m31) * invW,
                        (m02 * ndcX + m12 * ndcY + m22 * ndcZ + m32) * invW,
                        1.0f);
    }

    public Matrix4f unprojectInvRay(Vector2fc winCoords, int[] viewport, Vector3f originDest, Vector3f dirDest) {
        return unprojectInvRay(winCoords.x(), winCoords.y(), viewport, originDest, dirDest);
    }

    public Matrix4f unprojectInvRay(float winX, float winY, int[] viewport, Vector3f originDest, Vector3f dirDest) {
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float px = m00 * ndcX + m10 * ndcY + m30;
        float py = m01 * ndcX + m11 * ndcY + m31;
        float pz = m02 * ndcX + m12 * ndcY + m32;
        float invNearW = 1.0f / (m03 * ndcX + m13 * ndcY - m23 + m33);
        float nearX = (px - m20) * invNearW;
        float nearY = (py - m21) * invNearW;
        float nearZ = (pz - m22) * invNearW;
        float invW0 = 1.0f / (m03 * ndcX + m13 * ndcY + m33);
        float x0 = px * invW0;
        float y0 = py * invW0;
        float z0 = pz * invW0;
        originDest.x = nearX; originDest.y = nearY; originDest.z = nearZ;
        dirDest.x = x0 - nearX; dirDest.y = y0 - nearY; dirDest.z = z0 - nearZ;
        return this;
    }

    public Vector3f unprojectInv(Vector3fc winCoords, int[] viewport, Vector3f dest) {
        return unprojectInv(winCoords.x(), winCoords.y(), winCoords.z(), viewport, dest);
    }

    public Vector3f unprojectInv(float winX, float winY, float winZ, int[] viewport, Vector3f dest) {
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        float ndcZ = winZ+winZ-1.0f;
        float invW = 1.0f / (m03 * ndcX + m13 * ndcY + m23 * ndcZ + m33);
        return dest.set((m00 * ndcX + m10 * ndcY + m20 * ndcZ + m30) * invW,
                        (m01 * ndcX + m11 * ndcY + m21 * ndcZ + m31) * invW,
                        (m02 * ndcX + m12 * ndcY + m22 * ndcZ + m32) * invW);
    }

    public Vector4f project(float x, float y, float z, int[] viewport, Vector4f winCoordsDest) {
        float invW = 1.0f / Math.fma(m03, x, Math.fma(m13, y, Math.fma(m23, z, m33)));
        float nx = Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30))) * invW;
        float ny = Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31))) * invW;
        float nz = Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32))) * invW;
        return winCoordsDest.set(Math.fma(Math.fma(nx, 0.5f, 0.5f), viewport[2], viewport[0]),
                                 Math.fma(Math.fma(ny, 0.5f, 0.5f), viewport[3], viewport[1]),
                                 Math.fma(0.5f, nz, 0.5f),
                                 1.0f);
    }

    public Vector3f project(float x, float y, float z, int[] viewport, Vector3f winCoordsDest) {
        float invW = 1.0f / Math.fma(m03, x, Math.fma(m13, y, Math.fma(m23, z, m33)));
        float nx = Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30))) * invW;
        float ny = Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31))) * invW;
        float nz = Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32))) * invW;
        winCoordsDest.x = Math.fma(Math.fma(nx, 0.5f, 0.5f), viewport[2], viewport[0]);
        winCoordsDest.y = Math.fma(Math.fma(ny, 0.5f, 0.5f), viewport[3], viewport[1]);
        winCoordsDest.z = Math.fma(0.5f, nz, 0.5f);
        return winCoordsDest;
    }

    public Vector4f project(Vector3fc position, int[] viewport, Vector4f winCoordsDest) {
        return project(position.x(), position.y(), position.z(), viewport, winCoordsDest);
    }

    public Vector3f project(Vector3fc position, int[] viewport, Vector3f winCoordsDest) {
        return project(position.x(), position.y(), position.z(), viewport, winCoordsDest);
    }

    public Matrix4f reflect(float a, float b, float c, float d, Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.reflection(a, b, c, d);
        else if ((properties & PROPERTY_AFFINE) != 0)
            return reflectAffine(a, b, c, d, dest);
        return reflectGeneric(a, b, c, d, dest);
    }
    private Matrix4f reflectAffine(float a, float b, float c, float d, Matrix4f dest) {
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
        dest._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
            ._m33(m33);
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        dest._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
            ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
            ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
            ._m23(0.0f)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(0.0f)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(0.0f)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }
    private Matrix4f reflectGeneric(float a, float b, float c, float d, Matrix4f dest) {
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
        dest._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30)
            ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31)
            ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32)
            ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33);
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
        dest._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
            ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
            ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
            ._m23(m03 * rm20 + m13 * rm21 + m23 * rm22)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(nm03)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(nm13)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
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
    public Matrix4f reflect(float a, float b, float c, float d) {
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
    public Matrix4f reflect(float nx, float ny, float nz, float px, float py, float pz) {
        return reflect(nx, ny, nz, px, py, pz, this);
    }

    public Matrix4f reflect(float nx, float ny, float nz, float px, float py, float pz, Matrix4f dest) {
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
    public Matrix4f reflect(Vector3fc normal, Vector3fc point) {
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
    public Matrix4f reflect(Quaternionfc orientation, Vector3fc point) {
        return reflect(orientation, point, this);
    }

    public Matrix4f reflect(Quaternionfc orientation, Vector3fc point, Matrix4f dest) {
        double num1 = orientation.x() + orientation.x();
        double num2 = orientation.y() + orientation.y();
        double num3 = orientation.z() + orientation.z();
        float normalX = (float) (orientation.x() * num3 + orientation.w() * num2);
        float normalY = (float) (orientation.y() * num3 - orientation.w() * num1);
        float normalZ = (float) (1.0 - (orientation.x() * num1 + orientation.y() * num2));
        return reflect(normalX, normalY, normalZ, point.x(), point.y(), point.z(), dest);
    }

    public Matrix4f reflect(Vector3fc normal, Vector3fc point, Matrix4f dest) {
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
    public Matrix4f reflection(float a, float b, float c, float d) {
        float da = a + a, db = b + b, dc = c + c, dd = d + d;
        this._m00(1.0f - da * a)
            ._m01(-da * b)
            ._m02(-da * c)
            ._m03(0.0f)
            ._m10(-db * a)
            ._m11(1.0f - db * b)
            ._m12(-db * c)
            ._m13(0.0f)
            ._m20(-dc * a)
            ._m21(-dc * b)
            ._m22(1.0f - dc * c)
            ._m23(0.0f)
            ._m30(-dd * a)
            ._m31(-dd * b)
            ._m32(-dd * c)
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f reflection(float nx, float ny, float nz, float px, float py, float pz) {
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
    public Matrix4f reflection(Vector3fc normal, Vector3fc point) {
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
    public Matrix4f reflection(Quaternionfc orientation, Vector3fc point) {
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
            return dest.set(m00, m10, m20, m30);
        case 1:
            return dest.set(m01, m11, m21, m31);
        case 2:
            return dest.set(m02, m12, m22, m32);
        case 3:
            return dest.set(m03, m13, m23, m33);
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    public Vector3f getRow(int row, Vector3f dest) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            return dest.set(m00, m10, m20);
        case 1:
            return dest.set(m01, m11, m21);
        case 2:
            return dest.set(m02, m12, m22);
        case 3:
            return dest.set(m03, m13, m23);
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Set the row at the given <code>row</code> index, starting with <code>0</code>.
     * 
     * @param row
     *          the row index in <code>[0..3]</code>
     * @param src
     *          the row components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..3]</code>
     */
    public Matrix4f setRow(int row, Vector4fc src) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            return _m00(src.x())._m10(src.y())._m20(src.z())._m30(src.w())._properties(0);
        case 1:
            return _m01(src.x())._m11(src.y())._m21(src.z())._m31(src.w())._properties(0);
        case 2:
            return _m02(src.x())._m12(src.y())._m22(src.z())._m32(src.w())._properties(0);
        case 3:
            return _m03(src.x())._m13(src.y())._m23(src.z())._m33(src.w())._properties(0);
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    public Vector4f getColumn(int column, Vector4f dest) throws IndexOutOfBoundsException {
        return MemUtil.INSTANCE.getColumn(this, column, dest);
    }

    public Vector3f getColumn(int column, Vector3f dest) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            return dest.set(m00, m01, m02);
        case 1:
            return dest.set(m10, m11, m12);
        case 2:
            return dest.set(m20, m21, m22);
        case 3:
            return dest.set(m30, m31, m32);
        default:
            throw new IndexOutOfBoundsException();
        }
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
    public Matrix4f setColumn(int column, Vector4fc src) throws IndexOutOfBoundsException {
        if (src instanceof Vector4f)
            return MemUtil.INSTANCE.setColumn((Vector4f) src, column, this)._properties(0);
        return MemUtil.INSTANCE.setColumn(src, column, this)._properties(0);
    }

    public float get(int column, int row) {
        return MemUtil.INSTANCE.get(this, column, row);
    }

    /**
     * Set the matrix element at the given column and row to the specified value.
     * 
     * @param column
     *          the colum index in <code>[0..3]</code>
     * @param row
     *          the row index in <code>[0..3]</code>
     * @param value
     *          the value
     * @return this
     */
    public Matrix4f set(int column, int row, float value) {
        return MemUtil.INSTANCE.set(this, column, row, value);
    }

    public float getRowColumn(int row, int column) {
        return MemUtil.INSTANCE.get(this, column, row);
    }

    /**
     * Set the matrix element at the given row and column to the specified value.
     * 
     * @param row
     *          the row index in <code>[0..3]</code>
     * @param column
     *          the colum index in <code>[0..3]</code>
     * @param value
     *          the value
     * @return this
     */
    public Matrix4f setRowColumn(int row, int column, float value) {
        return MemUtil.INSTANCE.set(this, column, row, value);
    }

    /**
     * Compute a normal matrix from the upper left 3x3 submatrix of <code>this</code>
     * and store it into the upper left 3x3 submatrix of <code>this</code>.
     * All other values of <code>this</code> will be set to {@link #identity() identity}.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In that case, use {@link #set3x3(Matrix4f)} to set a given Matrix4f to only the upper left 3x3 submatrix
     * of this matrix.
     * 
     * @see #set3x3(Matrix4f)
     * 
     * @return this
     */
    public Matrix4f normal() {
        return normal(this);
    }

    /**
     * Compute a normal matrix from the upper left 3x3 submatrix of <code>this</code>
     * and store it into the upper left 3x3 submatrix of <code>dest</code>.
     * All other values of <code>dest</code> will be set to {@link #identity() identity}.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In that case, use {@link #set3x3(Matrix4f)} to set a given Matrix4f to only the upper left 3x3 submatrix
     * of this matrix.
     * 
     * @see #set3x3(Matrix4f)
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix4f normal(Matrix4f dest) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return dest.identity();
        else if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalOrthonormal(dest);
        return normalGeneric(dest);
    }
    private Matrix4f normalOrthonormal(Matrix4f dest) {
        if (dest != this)
            dest.set(this);
        return dest._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
    }
    private Matrix4f normalGeneric(Matrix4f dest) {
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
        return dest
        ._m00(nm00)
        ._m01(nm01)
        ._m02(nm02)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(0.0f)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties((properties | PROPERTY_AFFINE) & ~(PROPERTY_TRANSLATION | PROPERTY_PERSPECTIVE));
    }

    /**
     * Compute a normal matrix from the upper left 3x3 submatrix of <code>this</code>
     * and store it into <code>dest</code>.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors, 
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In that case, use {@link Matrix3f#set(Matrix4fc)} to set a given Matrix3f to only the upper left 3x3 submatrix
     * of this matrix.
     * 
     * @see Matrix3f#set(Matrix4fc)
     * @see #get3x3(Matrix3f)
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix3f normal(Matrix3f dest) {
        if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalOrthonormal(dest);
        return normalGeneric(dest);
    }
    private Matrix3f normalOrthonormal(Matrix3f dest) {
        dest.set(this);
        return dest;
    }
    private Matrix3f normalGeneric(Matrix3f dest) {
        float det = (m00 * m11 - (m01 * m10)) * m22
                  + (m02 * m10 - (m00 * m12)) * m21
                  + (m01 * m12 - (m02 * m11)) * m20;
        float s = 1.0f / det;
        /* Invert and transpose in one go */
        return dest._m00((m11 * m22 - m21 * m12) * s)
                   ._m01((m20 * m12 - m10 * m22) * s)
                   ._m02((m10 * m21 - m20 * m11) * s)
                   ._m10((m21 * m02 - m01 * m22) * s)
                   ._m11((m00 * m22 - m20 * m02) * s)
                   ._m12((m20 * m01 - m00 * m21) * s)
                   ._m20((m01 * m12 - m02 * m11) * s)
                   ._m21((m02 * m10 - m00 * m12) * s)
                   ._m22((m00 * m11 - m01 * m10) * s);
    }

    /**
     * Compute the cofactor matrix of the upper left 3x3 submatrix of <code>this</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal()} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @return this
     */
    public Matrix4f cofactor3x3() {
        return cofactor3x3(this);
    }

    /**
     * Compute the cofactor matrix of the upper left 3x3 submatrix of <code>this</code>
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
        return dest._m00(m11 * m22 - m21 * m12)
                   ._m01(m20 * m12 - m10 * m22)
                   ._m02(m10 * m21 - m20 * m11)
                   ._m10(m21 * m02 - m01 * m22)
                   ._m11(m00 * m22 - m20 * m02)
                   ._m12(m20 * m01 - m00 * m21)
                   ._m20(m01 * m12 - m02 * m11)
                   ._m21(m02 * m10 - m00 * m12)
                   ._m22(m00 * m11 - m01 * m10);
    }

    /**
     * Compute the cofactor matrix of the upper left 3x3 submatrix of <code>this</code>
     * and store it into <code>dest</code>.
     * All other values of <code>dest</code> will be set to {@link #identity() identity}.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal(Matrix4f)} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix4f cofactor3x3(Matrix4f dest) {
        float nm10 = m21 * m02 - m01 * m22;
        float nm11 = m00 * m22 - m20 * m02;
        float nm12 = m20 * m01 - m00 * m21;
        float nm20 = m01 * m12 - m11 * m02;
        float nm21 = m02 * m10 - m12 * m00;
        float nm22 = m00 * m11 - m10 * m01;
        return dest
        ._m00(m11 * m22 - m21 * m12)
        ._m01(m20 * m12 - m10 * m22)
        ._m02(m10 * m21 - m20 * m11)
        ._m03(0.0f)
        ._m10(nm10)
        ._m11(nm11)
        ._m12(nm12)
        ._m13(0.0f)
        ._m20(nm20)
        ._m21(nm21)
        ._m22(nm22)
        ._m23(0.0f)
        ._m30(0.0f)
        ._m31(0.0f)
        ._m32(0.0f)
        ._m33(1.0f)
        ._properties((properties | PROPERTY_AFFINE) & ~(PROPERTY_TRANSLATION | PROPERTY_PERSPECTIVE));
    }

    /**
     * Normalize the upper left 3x3 submatrix of this matrix.
     * <p>
     * The resulting matrix will map unit vectors to unit vectors, though a pair of orthogonal input unit
     * vectors need not be mapped to a pair of orthogonal output vectors if the original matrix was not orthogonal itself
     * (i.e. had <i>skewing</i>).
     * 
     * @return this
     */
    public Matrix4f normalize3x3() {
        return normalize3x3(this);
    }

    public Matrix4f normalize3x3(Matrix4f dest) {
        float invXlen = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float invYlen = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float invZlen = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        return dest
        ._m00(m00 * invXlen)._m01(m01 * invXlen)._m02(m02 * invXlen)
        ._m10(m10 * invYlen)._m11(m11 * invYlen)._m12(m12 * invYlen)
        ._m20(m20 * invZlen)._m21(m21 * invZlen)._m22(m22 * invZlen)
        ._m30(m30)._m31(m31)._m32(m32)._m33(m33)
        ._properties(properties);
    }

    public Matrix3f normalize3x3(Matrix3f dest) {
        float invXlen = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float invYlen = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float invZlen = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        return dest
        ._m00(m00 * invXlen)._m01(m01 * invXlen)._m02(m02 * invXlen)
        ._m10(m10 * invYlen)._m11(m11 * invYlen)._m12(m12 * invYlen)
        ._m20(m20 * invZlen)._m21(m21 * invZlen)._m22(m22 * invZlen);
    }

    public Vector4f frustumPlane(int plane, Vector4f dest) {
        switch (plane) {
        case PLANE_NX:
            dest.set(m03 + m00, m13 + m10, m23 + m20, m33 + m30).normalize3();
            break;
        case PLANE_PX:
            dest.set(m03 - m00, m13 - m10, m23 - m20, m33 - m30).normalize3();
            break;
        case PLANE_NY:
            dest.set(m03 + m01, m13 + m11, m23 + m21, m33 + m31).normalize3();
            break;
        case PLANE_PY:
            dest.set(m03 - m01, m13 - m11, m23 - m21, m33 - m31).normalize3();
            break;
        case PLANE_NZ:
            dest.set(m03 + m02, m13 + m12, m23 + m22, m33 + m32).normalize3();
            break;
        case PLANE_PZ:
            dest.set(m03 - m02, m13 - m12, m23 - m22, m33 - m32).normalize3();
            break;
        default:
            throw new IllegalArgumentException("dest"); //$NON-NLS-1$
        }
        return dest;
    }

    public Vector3f frustumCorner(int corner, Vector3f point) {
        float d1, d2, d3;
        float n1x, n1y, n1z, n2x, n2y, n2z, n3x, n3y, n3z;
        switch (corner) {
        case CORNER_NXNYNZ: // left, bottom, near
            n1x = m03 + m00; n1y = m13 + m10; n1z = m23 + m20; d1 = m33 + m30; // left
            n2x = m03 + m01; n2y = m13 + m11; n2z = m23 + m21; d2 = m33 + m31; // bottom
            n3x = m03 + m02; n3y = m13 + m12; n3z = m23 + m22; d3 = m33 + m32; // near
            break;
        case CORNER_PXNYNZ: // right, bottom, near
            n1x = m03 - m00; n1y = m13 - m10; n1z = m23 - m20; d1 = m33 - m30; // right
            n2x = m03 + m01; n2y = m13 + m11; n2z = m23 + m21; d2 = m33 + m31; // bottom
            n3x = m03 + m02; n3y = m13 + m12; n3z = m23 + m22; d3 = m33 + m32; // near
            break;
        case CORNER_PXPYNZ: // right, top, near
            n1x = m03 - m00; n1y = m13 - m10; n1z = m23 - m20; d1 = m33 - m30; // right
            n2x = m03 - m01; n2y = m13 - m11; n2z = m23 - m21; d2 = m33 - m31; // top
            n3x = m03 + m02; n3y = m13 + m12; n3z = m23 + m22; d3 = m33 + m32; // near
            break;
        case CORNER_NXPYNZ: // left, top, near
            n1x = m03 + m00; n1y = m13 + m10; n1z = m23 + m20; d1 = m33 + m30; // left
            n2x = m03 - m01; n2y = m13 - m11; n2z = m23 - m21; d2 = m33 - m31; // top
            n3x = m03 + m02; n3y = m13 + m12; n3z = m23 + m22; d3 = m33 + m32; // near
            break;
        case CORNER_PXNYPZ: // right, bottom, far
            n1x = m03 - m00; n1y = m13 - m10; n1z = m23 - m20; d1 = m33 - m30; // right
            n2x = m03 + m01; n2y = m13 + m11; n2z = m23 + m21; d2 = m33 + m31; // bottom
            n3x = m03 - m02; n3y = m13 - m12; n3z = m23 - m22; d3 = m33 - m32; // far
            break;
        case CORNER_NXNYPZ: // left, bottom, far
            n1x = m03 + m00; n1y = m13 + m10; n1z = m23 + m20; d1 = m33 + m30; // left
            n2x = m03 + m01; n2y = m13 + m11; n2z = m23 + m21; d2 = m33 + m31; // bottom
            n3x = m03 - m02; n3y = m13 - m12; n3z = m23 - m22; d3 = m33 - m32; // far
            break;
        case CORNER_NXPYPZ: // left, top, far
            n1x = m03 + m00; n1y = m13 + m10; n1z = m23 + m20; d1 = m33 + m30; // left
            n2x = m03 - m01; n2y = m13 - m11; n2z = m23 - m21; d2 = m33 - m31; // top
            n3x = m03 - m02; n3y = m13 - m12; n3z = m23 - m22; d3 = m33 - m32; // far
            break;
        case CORNER_PXPYPZ: // right, top, far
            n1x = m03 - m00; n1y = m13 - m10; n1z = m23 - m20; d1 = m33 - m30; // right
            n2x = m03 - m01; n2y = m13 - m11; n2z = m23 - m21; d2 = m33 - m31; // top
            n3x = m03 - m02; n3y = m13 - m12; n3z = m23 - m22; d3 = m33 - m32; // far
            break;
        default:
            throw new IllegalArgumentException("corner"); //$NON-NLS-1$
        }
        float c23x, c23y, c23z;
        c23x = n2y * n3z - n2z * n3y;
        c23y = n2z * n3x - n2x * n3z;
        c23z = n2x * n3y - n2y * n3x;
        float c31x, c31y, c31z;
        c31x = n3y * n1z - n3z * n1y;
        c31y = n3z * n1x - n3x * n1z;
        c31z = n3x * n1y - n3y * n1x;
        float c12x, c12y, c12z;
        c12x = n1y * n2z - n1z * n2y;
        c12y = n1z * n2x - n1x * n2z;
        c12z = n1x * n2y - n1y * n2x;
        float invDot = 1.0f / (n1x * c23x + n1y * c23y + n1z * c23z);
        point.x = (-c23x * d1 - c31x * d2 - c12x * d3) * invDot;
        point.y = (-c23y * d1 - c31y * d2 - c12y * d3) * invDot;
        point.z = (-c23z * d1 - c31z * d2 - c12z * d3) * invDot;
        return point;
    }

    /**
     * Compute the eye/origin of the perspective frustum transformation defined by <code>this</code> matrix, 
     * which can be a projection matrix or a combined modelview-projection matrix, and store the result
     * in the given <code>origin</code>.
     * <p>
     * Note that this method will only work using perspective projections obtained via one of the
     * perspective methods, such as {@link #perspective(float, float, float, float) perspective()}
     * or {@link #frustum(float, float, float, float, float, float) frustum()}.
     * <p>
     * Generally, this method computes the origin in the local frame of
     * any coordinate system that existed before <code>this</code>
     * transformation was applied to it in order to yield homogeneous clipping space.
     * <p>
     * This method is equivalent to calling: <code>invert(new Matrix4f()).transformProject(0, 0, -1, 0, origin)</code>
     * and in the case of an already available inverse of <code>this</code> matrix, the method {@link #perspectiveInvOrigin(Vector3f)}
     * on the inverse of the matrix should be used instead.
     * <p>
     * Reference: <a href="http://geomalgorithms.com/a05-_intersect-1.html">http://geomalgorithms.com</a>
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @see #perspectiveInvOrigin(Vector3f)
     * 
     * @param origin
     *          will hold the origin of the coordinate system before applying <code>this</code>
     *          perspective projection transformation
     * @return origin
     */
    public Vector3f perspectiveOrigin(Vector3f origin) {
        /*
         * Simply compute the intersection point of the left, right and top frustum plane.
         */
        float d1, d2, d3;
        float n1x, n1y, n1z, n2x, n2y, n2z, n3x, n3y, n3z;
        n1x = m03 + m00; n1y = m13 + m10; n1z = m23 + m20; d1 = m33 + m30; // left
        n2x = m03 - m00; n2y = m13 - m10; n2z = m23 - m20; d2 = m33 - m30; // right
        n3x = m03 - m01; n3y = m13 - m11; n3z = m23 - m21; d3 = m33 - m31; // top
        float c23x, c23y, c23z;
        c23x = n2y * n3z - n2z * n3y;
        c23y = n2z * n3x - n2x * n3z;
        c23z = n2x * n3y - n2y * n3x;
        float c31x, c31y, c31z;
        c31x = n3y * n1z - n3z * n1y;
        c31y = n3z * n1x - n3x * n1z;
        c31z = n3x * n1y - n3y * n1x;
        float c12x, c12y, c12z;
        c12x = n1y * n2z - n1z * n2y;
        c12y = n1z * n2x - n1x * n2z;
        c12z = n1x * n2y - n1y * n2x;
        float invDot = 1.0f / (n1x * c23x + n1y * c23y + n1z * c23z);
        origin.x = (-c23x * d1 - c31x * d2 - c12x * d3) * invDot;
        origin.y = (-c23y * d1 - c31y * d2 - c12y * d3) * invDot;
        origin.z = (-c23z * d1 - c31z * d2 - c12z * d3) * invDot;
        return origin;
    }

    /**
     * Compute the eye/origin of the inverse of the perspective frustum transformation defined by <code>this</code> matrix, 
     * which can be the inverse of a projection matrix or the inverse of a combined modelview-projection matrix, and store the result
     * in the given <code>dest</code>.
     * <p>
     * Note that this method will only work using perspective projections obtained via one of the
     * perspective methods, such as {@link #perspective(float, float, float, float) perspective()}
     * or {@link #frustum(float, float, float, float, float, float) frustum()}.
     * <p>
     * If the inverse of the modelview-projection matrix is not available, then calling {@link #perspectiveOrigin(Vector3f)}
     * on the original modelview-projection matrix is preferred.
     * 
     * @see #perspectiveOrigin(Vector3f)
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector3f perspectiveInvOrigin(Vector3f dest) {
        float invW = 1.0f / m23;
        dest.x = m20 * invW;
        dest.y = m21 * invW;
        dest.z = m22 * invW;
        return dest;
    }

    /**
     * Return the vertical field-of-view angle in radians of this perspective transformation matrix.
     * <p>
     * Note that this method will only work using perspective projections obtained via one of the
     * perspective methods, such as {@link #perspective(float, float, float, float) perspective()}
     * or {@link #frustum(float, float, float, float, float, float) frustum()}.
     * <p>
     * For orthogonal transformations this method will return <code>0.0</code>.
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @return the vertical field-of-view angle in radians
     */
    public float perspectiveFov() {
        /*
         * Compute the angle between the bottom and top frustum plane normals.
         */
        float n1x, n1y, n1z, n2x, n2y, n2z;
        n1x = m03 + m01; n1y = m13 + m11; n1z = m23 + m21; // bottom
        n2x = m01 - m03; n2y = m11 - m13; n2z = m21 - m23; // top
        float n1len = Math.sqrt(n1x * n1x + n1y * n1y + n1z * n1z);
        float n2len = Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
        return Math.acos((n1x * n2x + n1y * n2y + n1z * n2z) / (n1len * n2len));
    }

    /**
     * Extract the near clip plane distance from <code>this</code> perspective projection matrix.
     * <p>
     * This method only works if <code>this</code> is a perspective projection matrix, for example obtained via {@link #perspective(float, float, float, float)}.
     * 
     * @return the near clip plane distance
     */
    public float perspectiveNear() {
        return m32 / (m23 + m22);
    }

    /**
     * Extract the far clip plane distance from <code>this</code> perspective projection matrix.
     * <p>
     * This method only works if <code>this</code> is a perspective projection matrix, for example obtained via {@link #perspective(float, float, float, float)}.
     * 
     * @return the far clip plane distance
     */
    public float perspectiveFar() {
        return m32 / (m22 - m23);
    }

    public Vector3f frustumRayDir(float x, float y, Vector3f dir) {
        /*
         * This method works by first obtaining the frustum plane normals,
         * then building the cross product to obtain the corner rays,
         * and finally bilinearly interpolating to obtain the desired direction.
         * The code below uses a condense form of doing all this making use 
         * of some mathematical identities to simplify the overall expression.
         */
        float a = m10 * m23, b = m13 * m21, c = m10 * m21, d = m11 * m23, e = m13 * m20, f = m11 * m20;
        float g = m03 * m20, h = m01 * m23, i = m01 * m20, j = m03 * m21, k = m00 * m23, l = m00 * m21;
        float m = m00 * m13, n = m03 * m11, o = m00 * m11, p = m01 * m13, q = m03 * m10, r = m01 * m10;
        float m1x, m1y, m1z;
        m1x = (d + e + f - a - b - c) * (1.0f - y) + (a - b - c + d - e + f) * y;
        m1y = (j + k + l - g - h - i) * (1.0f - y) + (g - h - i + j - k + l) * y;
        m1z = (p + q + r - m - n - o) * (1.0f - y) + (m - n - o + p - q + r) * y;
        float m2x, m2y, m2z;
        m2x = (b - c - d + e + f - a) * (1.0f - y) + (a + b - c - d - e + f) * y;
        m2y = (h - i - j + k + l - g) * (1.0f - y) + (g + h - i - j - k + l) * y;
        m2z = (n - o - p + q + r - m) * (1.0f - y) + (m + n - o - p - q + r) * y;
        dir.x = m1x + (m2x - m1x) * x;
        dir.y = m1y + (m2y - m1y) * x;
        dir.z = m1z + (m2z - m1z) * x;
        return dir.normalize(dir);
    }

    public Vector3f positiveZ(Vector3f dir) {
        if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalizedPositiveZ(dir);
        return positiveZGeneric(dir);
    }
    private Vector3f positiveZGeneric(Vector3f dir) {
        return dir.set(m10 * m21 - m11 * m20, m20 * m01 - m21 * m00, m00 * m11 - m01 * m10).normalize();
    }

    public Vector3f normalizedPositiveZ(Vector3f dir) {
        return dir.set(m02, m12, m22);
    }

    public Vector3f positiveX(Vector3f dir) {
        if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalizedPositiveX(dir);
        return positiveXGeneric(dir);
    }
    private Vector3f positiveXGeneric(Vector3f dir) {
        return dir.set(m11 * m22 - m12 * m21, m02 * m21 - m01 * m22, m01 * m12 - m02 * m11).normalize();
    }

    public Vector3f normalizedPositiveX(Vector3f dir) {
        return dir.set(m00, m10, m20);
    }

    public Vector3f positiveY(Vector3f dir) {
        if ((properties & PROPERTY_ORTHONORMAL) != 0)
            return normalizedPositiveY(dir);
        return positiveYGeneric(dir);
    }
    private Vector3f positiveYGeneric(Vector3f dir) {
        return dir.set(m12 * m20 - m10 * m22, m00 * m22 - m02 * m20, m02 * m10 - m00 * m12).normalize();
    }

    public Vector3f normalizedPositiveY(Vector3f dir) {
        return dir.set(m01, m11, m21);
    }

    public Vector3f originAffine(Vector3f origin) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float d = m01 * m12 - m02 * m11;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float j = m21 * m32 - m22 * m31;
        return origin.set(-m10 * j + m11 * h - m12 * g, m00 * j - m01 * h + m02 * g, -m30 * d + m31 * b - m32 * a);
    }

    public Vector3f origin(Vector3f dest) {
        if ((properties & PROPERTY_AFFINE) != 0)
            return originAffine(dest);
        return originGeneric(dest);
    }
    private Vector3f originGeneric(Vector3f dest) {
        float a = m00 * m11 - m01 * m10;
        float b = m00 * m12 - m02 * m10;
        float c = m00 * m13 - m03 * m10;
        float d = m01 * m12 - m02 * m11;
        float e = m01 * m13 - m03 * m11;
        float f = m02 * m13 - m03 * m12;
        float g = m20 * m31 - m21 * m30;
        float h = m20 * m32 - m22 * m30;
        float i = m20 * m33 - m23 * m30;
        float j = m21 * m32 - m22 * m31;
        float k = m21 * m33 - m23 * m31;
        float l = m22 * m33 - m23 * m32;
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        float invDet = 1.0f / det;
        float nm30 = (-m10 * j + m11 * h - m12 * g) * invDet;
        float nm31 = ( m00 * j - m01 * h + m02 * g) * invDet;
        float nm32 = (-m30 * d + m31 * b - m32 * a) * invDet;
        float nm33 = det / ( m20 * d - m21 * b + m22 * a);
        return dest.set(nm30 * nm33, nm31 * nm33, nm32 * nm33);
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
    public Matrix4f shadow(Vector4f light, float a, float b, float c, float d) {
        return shadow(light.x, light.y, light.z, light.w, a, b, c, d, this);
    }

    public Matrix4f shadow(Vector4f light, float a, float b, float c, float d, Matrix4f dest) {
        return shadow(light.x, light.y, light.z, light.w, a, b, c, d, dest);
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
    public Matrix4f shadow(float lightX, float lightY, float lightZ, float lightW, float a, float b, float c, float d) {
        return shadow(lightX, lightY, lightZ, lightW, a, b, c, d, this);
    }

    public Matrix4f shadow(float lightX, float lightY, float lightZ, float lightW, float a, float b, float c, float d, Matrix4f dest) {
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
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02 + m33 * rm03;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12 + m30 * rm13;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12 + m31 * rm13;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12 + m32 * rm13;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12 + m33 * rm13;
        float nm20 = m00 * rm20 + m10 * rm21 + m20 * rm22 + m30 * rm23;
        float nm21 = m01 * rm20 + m11 * rm21 + m21 * rm22 + m31 * rm23;
        float nm22 = m02 * rm20 + m12 * rm21 + m22 * rm22 + m32 * rm23;
        float nm23 = m03 * rm20 + m13 * rm21 + m23 * rm22 + m33 * rm23;
        dest._m30(m00 * rm30 + m10 * rm31 + m20 * rm32 + m30 * rm33)
            ._m31(m01 * rm30 + m11 * rm31 + m21 * rm32 + m31 * rm33)
            ._m32(m02 * rm30 + m12 * rm31 + m22 * rm32 + m32 * rm33)
            ._m33(m03 * rm30 + m13 * rm31 + m23 * rm32 + m33 * rm33)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(nm03)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(nm13)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    public Matrix4f shadow(Vector4f light, Matrix4fc planeTransform, Matrix4f dest) {
        // compute plane equation by transforming (y = 0)
        float a = planeTransform.m10();
        float b = planeTransform.m11();
        float c = planeTransform.m12();
        float d = -a * planeTransform.m30() - b * planeTransform.m31() - c * planeTransform.m32();
        return shadow(light.x, light.y, light.z, light.w, a, b, c, d, dest);
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
    public Matrix4f shadow(Vector4f light, Matrix4f planeTransform) {
        return shadow(light, planeTransform, this);
    }

    public Matrix4f shadow(float lightX, float lightY, float lightZ, float lightW, Matrix4fc planeTransform, Matrix4f dest) {
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
    public Matrix4f shadow(float lightX, float lightY, float lightZ, float lightW, Matrix4f planeTransform) {
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
    public Matrix4f billboardCylindrical(Vector3fc objPos, Vector3fc targetPos, Vector3fc up) {
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
        this._m00(leftX)
            ._m01(leftY)
            ._m02(leftZ)
            ._m03(0.0f)
            ._m10(up.x())
            ._m11(up.y())
            ._m12(up.z())
            ._m13(0.0f)
            ._m20(dirX)
            ._m21(dirY)
            ._m22(dirZ)
            ._m23(0.0f)
            ._m30(objPos.x())
            ._m31(objPos.y())
            ._m32(objPos.z())
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f billboardSpherical(Vector3fc objPos, Vector3fc targetPos, Vector3fc up) {
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
        this._m00(leftX)
            ._m01(leftY)
            ._m02(leftZ)
            ._m03(0.0f)
            ._m10(upX)
            ._m11(upY)
            ._m12(upZ)
            ._m13(0.0f)
            ._m20(dirX)
            ._m21(dirY)
            ._m22(dirZ)
            ._m23(0.0f)
            ._m30(objPos.x())
            ._m31(objPos.y())
            ._m32(objPos.z())
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f billboardSpherical(Vector3fc objPos, Vector3fc targetPos) {
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
        this._m00(1.0f - q11)
            ._m01(q01)
            ._m02(-q13)
            ._m03(0.0f)
            ._m10(q01)
            ._m11(1.0f - q00)
            ._m12(q03)
            ._m13(0.0f)
            ._m20(q13)
            ._m21(-q03)
            ._m22(1.0f - q11 - q00)
            ._m23(0.0f)
            ._m30(objPos.x())
            ._m31(objPos.y())
            ._m32(objPos.z())
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
        return this;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(m00);
        result = prime * result + Float.floatToIntBits(m01);
        result = prime * result + Float.floatToIntBits(m02);
        result = prime * result + Float.floatToIntBits(m03);
        result = prime * result + Float.floatToIntBits(m10);
        result = prime * result + Float.floatToIntBits(m11);
        result = prime * result + Float.floatToIntBits(m12);
        result = prime * result + Float.floatToIntBits(m13);
        result = prime * result + Float.floatToIntBits(m20);
        result = prime * result + Float.floatToIntBits(m21);
        result = prime * result + Float.floatToIntBits(m22);
        result = prime * result + Float.floatToIntBits(m23);
        result = prime * result + Float.floatToIntBits(m30);
        result = prime * result + Float.floatToIntBits(m31);
        result = prime * result + Float.floatToIntBits(m32);
        result = prime * result + Float.floatToIntBits(m33);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Matrix4f))
            return false;
        Matrix4fc other = (Matrix4fc) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00()))
            return false;
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01()))
            return false;
        if (Float.floatToIntBits(m02) != Float.floatToIntBits(other.m02()))
            return false;
        if (Float.floatToIntBits(m03) != Float.floatToIntBits(other.m03()))
            return false;
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10()))
            return false;
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11()))
            return false;
        if (Float.floatToIntBits(m12) != Float.floatToIntBits(other.m12()))
            return false;
        if (Float.floatToIntBits(m13) != Float.floatToIntBits(other.m13()))
            return false;
        if (Float.floatToIntBits(m20) != Float.floatToIntBits(other.m20()))
            return false;
        if (Float.floatToIntBits(m21) != Float.floatToIntBits(other.m21()))
            return false;
        if (Float.floatToIntBits(m22) != Float.floatToIntBits(other.m22()))
            return false;
        if (Float.floatToIntBits(m23) != Float.floatToIntBits(other.m23()))
            return false;
        if (Float.floatToIntBits(m30) != Float.floatToIntBits(other.m30()))
            return false;
        if (Float.floatToIntBits(m31) != Float.floatToIntBits(other.m31()))
            return false;
        if (Float.floatToIntBits(m32) != Float.floatToIntBits(other.m32()))
            return false;
        if (Float.floatToIntBits(m33) != Float.floatToIntBits(other.m33()))
            return false;
        return true;
    }

    public boolean equals(Matrix4fc m, float delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix4f))
            return false;
        if (!Runtime.equals(m00, m.m00(), delta))
            return false;
        if (!Runtime.equals(m01, m.m01(), delta))
            return false;
        if (!Runtime.equals(m02, m.m02(), delta))
            return false;
        if (!Runtime.equals(m03, m.m03(), delta))
            return false;
        if (!Runtime.equals(m10, m.m10(), delta))
            return false;
        if (!Runtime.equals(m11, m.m11(), delta))
            return false;
        if (!Runtime.equals(m12, m.m12(), delta))
            return false;
        if (!Runtime.equals(m13, m.m13(), delta))
            return false;
        if (!Runtime.equals(m20, m.m20(), delta))
            return false;
        if (!Runtime.equals(m21, m.m21(), delta))
            return false;
        if (!Runtime.equals(m22, m.m22(), delta))
            return false;
        if (!Runtime.equals(m23, m.m23(), delta))
            return false;
        if (!Runtime.equals(m30, m.m30(), delta))
            return false;
        if (!Runtime.equals(m31, m.m31(), delta))
            return false;
        if (!Runtime.equals(m32, m.m32(), delta))
            return false;
        if (!Runtime.equals(m33, m.m33(), delta))
            return false;
        return true;
    }

    public Matrix4f pick(float x, float y, float width, float height, int[] viewport, Matrix4f dest) {
        float sx = viewport[2] / width;
        float sy = viewport[3] / height;
        float tx = (viewport[2] + 2.0f * (viewport[0] - x)) / width;
        float ty = (viewport[3] + 2.0f * (viewport[1] - y)) / height;
        dest._m30(m00 * tx + m10 * ty + m30)
            ._m31(m01 * tx + m11 * ty + m31)
            ._m32(m02 * tx + m12 * ty + m32)
            ._m33(m03 * tx + m13 * ty + m33)
            ._m00(m00 * sx)
            ._m01(m01 * sx)
            ._m02(m02 * sx)
            ._m03(m03 * sx)
            ._m10(m10 * sy)
            ._m11(m11 * sy)
            ._m12(m12 * sy)
            ._m13(m13 * sy)
            ._properties(0);
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
    public Matrix4f pick(float x, float y, float width, float height, int[] viewport) {
        return pick(x, y, width, height, viewport, this);
    }

    public boolean isAffine() {
        return m03 == 0.0f && m13 == 0.0f && m23 == 0.0f && m33 == 1.0f;
    }

    /**
     * Exchange the values of <code>this</code> matrix with the given <code>other</code> matrix.
     * 
     * @param other
     *          the other matrix to exchange the values with
     * @return this
     */
    public Matrix4f swap(Matrix4f other) {
        MemUtil.INSTANCE.swap(this, other);
        int props = properties;
        this.properties = other.properties();
        other.properties = props;
        return this;
    }

    public Matrix4f arcball(float radius, float centerX, float centerY, float centerZ, float angleX, float angleY, Matrix4f dest) {
        float m30 = m20 * -radius + this.m30;
        float m31 = m21 * -radius + this.m31;
        float m32 = m22 * -radius + this.m32;
        float m33 = m23 * -radius + this.m33;
        float sin = Math.sin(angleX);
        float cos = Math.cosFromSin(sin, angleX);
        float nm10 = m10 * cos + m20 * sin;
        float nm11 = m11 * cos + m21 * sin;
        float nm12 = m12 * cos + m22 * sin;
        float nm13 = m13 * cos + m23 * sin;
        float m20 = this.m20 * cos - m10 * sin;
        float m21 = this.m21 * cos - m11 * sin;
        float m22 = this.m22 * cos - m12 * sin;
        float m23 = this.m23 * cos - m13 * sin;
        sin = Math.sin(angleY);
        cos = Math.cosFromSin(sin, angleY);
        float nm00 = m00 * cos - m20 * sin;
        float nm01 = m01 * cos - m21 * sin;
        float nm02 = m02 * cos - m22 * sin;
        float nm03 = m03 * cos - m23 * sin;
        float nm20 = m00 * sin + m20 * cos;
        float nm21 = m01 * sin + m21 * cos;
        float nm22 = m02 * sin + m22 * cos;
        float nm23 = m03 * sin + m23 * cos;
        dest._m30(-nm00 * centerX - nm10 * centerY - nm20 * centerZ + m30)
            ._m31(-nm01 * centerX - nm11 * centerY - nm21 * centerZ + m31)
            ._m32(-nm02 * centerX - nm12 * centerY - nm22 * centerZ + m32)
            ._m33(-nm03 * centerX - nm13 * centerY - nm23 * centerZ + m33)
            ._m20(nm20)
            ._m21(nm21)
            ._m22(nm22)
            ._m23(nm23)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(nm13)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(nm03)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    public Matrix4f arcball(float radius, Vector3fc center, float angleX, float angleY, Matrix4f dest) {
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
    public Matrix4f arcball(float radius, float centerX, float centerY, float centerZ, float angleX, float angleY) {
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
    public Matrix4f arcball(float radius, Vector3fc center, float angleX, float angleY) {
        return arcball(radius, center.x(), center.y(), center.z(), angleX, angleY, this);
    }

    /**
     * Compute the axis-aligned bounding box of the frustum described by <code>this</code> matrix and store the minimum corner
     * coordinates in the given <code>min</code> and the maximum corner coordinates in the given <code>max</code> vector.
     * <p>
     * The matrix <code>this</code> is assumed to be the {@link #invert() inverse} of the origial view-projection matrix
     * for which to compute the axis-aligned bounding box in world-space.
     * <p>
     * The axis-aligned bounding box of the unit frustum is <code>(-1, -1, -1)</code>, <code>(1, 1, 1)</code>.
     * 
     * @param min
     *          will hold the minimum corner coordinates of the axis-aligned bounding box
     * @param max
     *          will hold the maximum corner coordinates of the axis-aligned bounding box
     * @return this
     */
    public Matrix4f frustumAabb(Vector3f min, Vector3f max) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (int t = 0; t < 8; t++) {
            float x = ((t & 1) << 1) - 1.0f;
            float y = (((t >>> 1) & 1) << 1) - 1.0f;
            float z = (((t >>> 2) & 1) << 1) - 1.0f;
            float invW = 1.0f / (m03 * x + m13 * y + m23 * z + m33);
            float nx = (m00 * x + m10 * y + m20 * z + m30) * invW;
            float ny = (m01 * x + m11 * y + m21 * z + m31) * invW;
            float nz = (m02 * x + m12 * y + m22 * z + m32) * invW;
            minX = minX < nx ? minX : nx;
            minY = minY < ny ? minY : ny;
            minZ = minZ < nz ? minZ : nz;
            maxX = maxX > nx ? maxX : nx;
            maxY = maxY > ny ? maxY : ny;
            maxZ = maxZ > nz ? maxZ : nz;
        }
        min.x = minX;
        min.y = minY;
        min.z = minZ;
        max.x = maxX;
        max.y = maxY;
        max.z = maxZ;
        return this;
    }

    public Matrix4f projectedGridRange(Matrix4fc projector, float sLower, float sUpper, Matrix4f dest) {
        // Compute intersection with frustum edges and plane
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        boolean intersection = false;
        for (int t = 0; t < 3 * 4; t++) {
            float c0X, c0Y, c0Z;
            float c1X, c1Y, c1Z;
            if (t < 4) {
                // all x edges
                c0X = -1; c1X = +1;
                c0Y = c1Y = ((t & 1) << 1) - 1.0f;
                c0Z = c1Z = (((t >>> 1) & 1) << 1) - 1.0f;
            } else if (t < 8) {
                // all y edges
                c0Y = -1; c1Y = +1;
                c0X = c1X = ((t & 1) << 1) - 1.0f;
                c0Z = c1Z = (((t >>> 1) & 1) << 1) - 1.0f;
            } else {
                // all z edges
                c0Z = -1; c1Z = +1;
                c0X = c1X = ((t & 1) << 1) - 1.0f;
                c0Y = c1Y = (((t >>> 1) & 1) << 1) - 1.0f;
            }
            // unproject corners
            float invW = 1.0f / (m03 * c0X + m13 * c0Y + m23 * c0Z + m33);
            float p0x = (m00 * c0X + m10 * c0Y + m20 * c0Z + m30) * invW;
            float p0y = (m01 * c0X + m11 * c0Y + m21 * c0Z + m31) * invW;
            float p0z = (m02 * c0X + m12 * c0Y + m22 * c0Z + m32) * invW;
            invW = 1.0f / (m03 * c1X + m13 * c1Y + m23 * c1Z + m33);
            float p1x = (m00 * c1X + m10 * c1Y + m20 * c1Z + m30) * invW;
            float p1y = (m01 * c1X + m11 * c1Y + m21 * c1Z + m31) * invW;
            float p1z = (m02 * c1X + m12 * c1Y + m22 * c1Z + m32) * invW;
            float dirX = p1x - p0x;
            float dirY = p1y - p0y;
            float dirZ = p1z - p0z;
            float invDenom = 1.0f / dirY;
            // test for intersection
            for (int s = 0; s < 2; s++) {
                float isectT = -(p0y + (s == 0 ? sLower : sUpper)) * invDenom;
                if (isectT >= 0.0f && isectT <= 1.0f) {
                    intersection = true;
                    // project with projector matrix
                    float ix = p0x + isectT * dirX;
                    float iz = p0z + isectT * dirZ;
                    invW = 1.0f / (projector.m03() * ix + projector.m23() * iz + projector.m33());
                    float px = (projector.m00() * ix + projector.m20() * iz + projector.m30()) * invW;
                    float py = (projector.m01() * ix + projector.m21() * iz + projector.m31()) * invW;
                    minX = minX < px ? minX : px;
                    minY = minY < py ? minY : py;
                    maxX = maxX > px ? maxX : px;
                    maxY = maxY > py ? maxY : py;
                }
            }
        }
        if (!intersection)
            return null; // <- projected grid is not visible
        dest.set(maxX - minX, 0, 0, 0, 0, maxY - minY, 0, 0, 0, 0, 1, 0, minX, minY, 0, 1);
        dest._properties(PROPERTY_AFFINE);
        return dest;
    }

    /**
     * Change the near and far clip plane distances of <code>this</code> perspective frustum transformation matrix
     * and store the result in <code>dest</code>.
     * <p>
     * This method only works if <code>this</code> is a perspective projection frustum transformation, for example obtained
     * via {@link #perspective(float, float, float, float) perspective()} or {@link #frustum(float, float, float, float, float, float) frustum()}.
     * 
     * @see #perspective(float, float, float, float)
     * @see #frustum(float, float, float, float, float, float)
     * 
     * @param near
     *          the new near clip plane distance
     * @param far
     *          the new far clip plane distance
     * @param dest
     *          will hold the resulting matrix
     * @return dest
     */
    public Matrix4f perspectiveFrustumSlice(float near, float far, Matrix4f dest) {
        float invOldNear = (m23 + m22) / m32;
        float invNearFar = 1.0f / (near - far);
        dest._m00(m00 * invOldNear * near)
            ._m01(m01)
            ._m02(m02)
            ._m03(m03)
            ._m10(m10)
            ._m11(m11 * invOldNear * near)
            ._m12(m12)
            ._m13(m13)
            ._m20(m20)
            ._m21(m21)
            ._m22((far + near) * invNearFar)
            ._m23(m23)
            ._m30(m30)
            ._m31(m31)
            ._m32((far + far) * near * invNearFar)
            ._m33(m33)
            ._properties(properties & ~(PROPERTY_IDENTITY | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL));
        return dest;
    }

    /**
     * Build an ortographic projection transformation that fits the view-projection transformation represented by <code>this</code>
     * into the given affine <code>view</code> transformation.
     * <p>
     * The transformation represented by <code>this</code> must be given as the {@link #invert() inverse} of a typical combined camera view-projection
     * transformation, whose projection can be either orthographic or perspective.
     * <p>
     * The <code>view</code> must be an {@link #isAffine() affine} transformation which in the application of Cascaded Shadow Maps is usually the light view transformation.
     * It be obtained via any affine transformation or for example via {@link #lookAt(float, float, float, float, float, float, float, float, float) lookAt()}.
     * <p>
     * Reference: <a href="http://developer.download.nvidia.com/SDK/10.5/opengl/screenshots/samples/cascaded_shadow_maps.html">OpenGL SDK - Cascaded Shadow Maps</a>
     * 
     * @param view
     *          the view transformation to build a corresponding orthographic projection to fit the frustum of <code>this</code>
     * @param dest
     *          will hold the crop projection transformation
     * @return dest
     */
    public Matrix4f orthoCrop(Matrix4fc view, Matrix4f dest) {
        // determine min/max world z and min/max orthographically view-projected x/y
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (int t = 0; t < 8; t++) {
            float x = ((t & 1) << 1) - 1.0f;
            float y = (((t >>> 1) & 1) << 1) - 1.0f;
            float z = (((t >>> 2) & 1) << 1) - 1.0f;
            float invW = 1.0f / (m03 * x + m13 * y + m23 * z + m33);
            float wx = (m00 * x + m10 * y + m20 * z + m30) * invW;
            float wy = (m01 * x + m11 * y + m21 * z + m31) * invW;
            float wz = (m02 * x + m12 * y + m22 * z + m32) * invW;
            invW = 1.0f / (view.m03() * wx + view.m13() * wy + view.m23() * wz + view.m33());
            float vx = view.m00() * wx + view.m10() * wy + view.m20() * wz + view.m30();
            float vy = view.m01() * wx + view.m11() * wy + view.m21() * wz + view.m31();
            float vz = (view.m02() * wx + view.m12() * wy + view.m22() * wz + view.m32()) * invW;
            minX = minX < vx ? minX : vx;
            maxX = maxX > vx ? maxX : vx;
            minY = minY < vy ? minY : vy;
            maxY = maxY > vy ? maxY : vy;
            minZ = minZ < vz ? minZ : vz;
            maxZ = maxZ > vz ? maxZ : vz;
        }
        // build crop projection matrix to fit 'this' frustum into view
        return dest.setOrtho(minX, maxX, minY, maxY, -maxZ, -minZ);
    }

    /**
     * Set <code>this</code> matrix to a perspective transformation that maps the trapezoid spanned by the four corner coordinates
     * <code>(p0x, p0y)</code>, <code>(p1x, p1y)</code>, <code>(p2x, p2y)</code> and <code>(p3x, p3y)</code> to the unit square <code>[(-1, -1)..(+1, +1)]</code>.
     * <p>
     * The corner coordinates are given in counter-clockwise order starting from the <i>left</i> corner on the smaller parallel side of the trapezoid
     * seen when looking at the trapezoid oriented with its shorter parallel edge at the bottom and its longer parallel edge at the top.
     * <p>
     * Reference: <a href="http://www.comp.nus.edu.sg/~tants/tsm/TSM_recipe.html">Trapezoidal Shadow Maps (TSM) - Recipe</a>
     * 
     * @param p0x
     *          the x coordinate of the left corner at the shorter edge of the trapezoid
     * @param p0y
     *          the y coordinate of the left corner at the shorter edge of the trapezoid
     * @param p1x
     *          the x coordinate of the right corner at the shorter edge of the trapezoid
     * @param p1y
     *          the y coordinate of the right corner at the shorter edge of the trapezoid
     * @param p2x
     *          the x coordinate of the right corner at the longer edge of the trapezoid
     * @param p2y
     *          the y coordinate of the right corner at the longer edge of the trapezoid
     * @param p3x
     *          the x coordinate of the left corner at the longer edge of the trapezoid
     * @param p3y
     *          the y coordinate of the left corner at the longer edge of the trapezoid
     * @return this
     */
    public Matrix4f trapezoidCrop(float p0x, float p0y, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        float aX = p1y - p0y, aY = p0x - p1x;
        float nm00 = aY;
        float nm10 = -aX;
        float nm30 = aX * p0y - aY * p0x;
        float nm01 = aX;
        float nm11 = aY;
        float nm31 = -(aX * p0x + aY * p0y);
        float c3x = nm00 * p3x + nm10 * p3y + nm30;
        float c3y = nm01 * p3x + nm11 * p3y + nm31;
        float s = -c3x / c3y;
        nm00 += s * nm01;
        nm10 += s * nm11;
        nm30 += s * nm31;
        float d1x = nm00 * p1x + nm10 * p1y + nm30;
        float d2x = nm00 * p2x + nm10 * p2y + nm30;
        float d = d1x * c3y / (d2x - d1x);
        nm31 += d;
        float sx = 2.0f / d2x;
        float sy = 1.0f / (c3y + d);
        float u = (sy + sy) * d / (1.0f - sy * d);
        float m03 = nm01 * sy;
        float m13 = nm11 * sy;
        float m33 = nm31 * sy;
        nm01 = (u + 1.0f) * m03;
        nm11 = (u + 1.0f) * m13;
        nm31 = (u + 1.0f) * m33 - u;
        nm00 = sx * nm00 - m03;
        nm10 = sx * nm10 - m13;
        nm30 = sx * nm30 - m33;
        set(nm00, nm01, 0, m03,
            nm10, nm11, 0, m13,
              0,   0, 1,   0,
            nm30, nm31, 0, m33);
        _properties(0);
        return this;
    }

    public Matrix4f transformAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Vector3f outMin, Vector3f outMax) {
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

    public Matrix4f transformAab(Vector3fc min, Vector3fc max, Vector3f outMin, Vector3f outMax) {
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
    public Matrix4f lerp(Matrix4fc other, float t) {
        return lerp(other, t, this);
    }

    public Matrix4f lerp(Matrix4fc other, float t, Matrix4f dest) {
        dest._m00(Math.fma(other.m00() - m00, t, m00))
            ._m01(Math.fma(other.m01() - m01, t, m01))
            ._m02(Math.fma(other.m02() - m02, t, m02))
            ._m03(Math.fma(other.m03() - m03, t, m03))
            ._m10(Math.fma(other.m10() - m10, t, m10))
            ._m11(Math.fma(other.m11() - m11, t, m11))
            ._m12(Math.fma(other.m12() - m12, t, m12))
            ._m13(Math.fma(other.m13() - m13, t, m13))
            ._m20(Math.fma(other.m20() - m20, t, m20))
            ._m21(Math.fma(other.m21() - m21, t, m21))
            ._m22(Math.fma(other.m22() - m22, t, m22))
            ._m23(Math.fma(other.m23() - m23, t, m23))
            ._m30(Math.fma(other.m30() - m30, t, m30))
            ._m31(Math.fma(other.m31() - m31, t, m31))
            ._m32(Math.fma(other.m32() - m32, t, m32))
            ._m33(Math.fma(other.m33() - m33, t, m33))
            ._properties(properties & other.properties());
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
     * This method is equivalent to calling: <code>mulAffine(new Matrix4f().lookAt(new Vector3f(), new Vector3f(dir).negate(), up).invertAffine(), dest)</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float, Matrix4f)
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
    public Matrix4f rotateTowards(Vector3fc dir, Vector3fc up, Matrix4f dest) {
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
     * This method is equivalent to calling: <code>mulAffine(new Matrix4f().lookAt(new Vector3f(), new Vector3f(dir).negate(), up).invertAffine())</code>
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
    public Matrix4f rotateTowards(Vector3fc dir, Vector3fc up) {
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
     * This method is equivalent to calling: <code>mulAffine(new Matrix4f().lookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invertAffine())</code>
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
    public Matrix4f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
     * This method is equivalent to calling: <code>mulAffine(new Matrix4f().lookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invertAffine(), dest)</code>
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
    public Matrix4f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix4f dest) {
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
        dest._m30(m30)
            ._m31(m31)
            ._m32(m32)
            ._m33(m33);
        float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
        float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
        float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
        float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
        float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
        float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
        float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
        float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
        dest._m20(m00 * rm20 + m10 * rm21 + m20 * rm22)
            ._m21(m01 * rm20 + m11 * rm21 + m21 * rm22)
            ._m22(m02 * rm20 + m12 * rm21 + m22 * rm22)
            ._m23(m03 * rm20 + m13 * rm21 + m23 * rm22)
            ._m00(nm00)
            ._m01(nm01)
            ._m02(nm02)
            ._m03(nm03)
            ._m10(nm10)
            ._m11(nm11)
            ._m12(nm12)
            ._m13(nm13)
            ._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>dir</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAt(new Vector3f(), new Vector3f(dir).negate(), up).invertAffine()</code>
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
    public Matrix4f rotationTowards(Vector3fc dir, Vector3fc up) {
        return rotationTowards(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }

    /**
     * Set this matrix to a model transformation for a right-handed coordinate system, 
     * that aligns the local <code>-z</code> axis with <code>(dirX, dirY, dirZ)</code>.
     * <p>
     * In order to apply the rotation transformation to a previous existing transformation,
     * use {@link #rotateTowards(float, float, float, float, float, float) rotateTowards}.
     * <p>
     * This method is equivalent to calling: <code>setLookAt(0, 0, 0, -dirX, -dirY, -dirZ, upX, upY, upZ).invertAffine()</code>
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
    public Matrix4f rotationTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
        if ((properties & PROPERTY_IDENTITY) == 0)
            MemUtil.INSTANCE.identity(this);
        this._m00(leftX)
            ._m01(leftY)
            ._m02(leftZ)
            ._m10(upnX)
            ._m11(upnY)
            ._m12(upnZ)
            ._m20(ndirX)
            ._m21(ndirY)
            ._m22(ndirZ)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
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
    public Matrix4f translationRotateTowards(Vector3fc pos, Vector3fc dir, Vector3fc up) {
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
    public Matrix4f translationRotateTowards(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
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
        this._m00(leftX)
            ._m01(leftY)
            ._m02(leftZ)
            ._m03(0.0f)
            ._m10(upnX)
            ._m11(upnY)
            ._m12(upnZ)
            ._m13(0.0f)
            ._m20(ndirX)
            ._m21(ndirY)
            ._m22(ndirZ)
            ._m23(0.0f)
            ._m30(posX)
            ._m31(posY)
            ._m32(posZ)
            ._m33(1.0f)
            ._properties(PROPERTY_AFFINE | PROPERTY_ORTHONORMAL);
        return this;
    }

    /**
     * Extract the Euler angles from the rotation represented by the upper left 3x3 submatrix of <code>this</code>
     * and store the extracted Euler angles in <code>dest</code>.
     * <p>
     * This method assumes that the upper left of <code>this</code> only represents a rotation without scaling.
     * <p>
     * Note that the returned Euler angles must be applied in the order <code>Z * Y * X</code> to obtain the identical matrix.
     * This means that calling {@link Matrix4f#rotateZYX(float, float, float)} using the obtained Euler angles will yield
     * the same rotation as the original matrix from which the Euler angles were obtained, so in the below code the matrix
     * <code>m2</code> should be identical to <code>m</code> (disregarding possible floating-point inaccuracies).
     * <pre>
     * Matrix4f m = ...; // &lt;- matrix only representing rotation
     * Matrix4f n = new Matrix4f();
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
     * Compute the extents of the coordinate system before this {@link #isAffine() affine} transformation was applied
     * and store the resulting corner coordinates in <code>corner</code> and the span vectors in
     * <code>xDir</code>, <code>yDir</code> and <code>zDir</code>.
     * <p>
     * That means, given the maximum extents of the coordinate system between <code>[-1..+1]</code> in all dimensions,
     * this method returns one corner and the length and direction of the three base axis vectors in the coordinate
     * system before this transformation is applied, which transforms into the corner coordinates <code>[-1, +1]</code>.
     * <p>
     * This method is equivalent to computing at least three adjacent corners using {@link #frustumCorner(int, Vector3f)}
     * and subtracting them to obtain the length and direction of the span vectors.
     * 
     * @param corner
     *          will hold one corner of the span (usually the corner {@link Matrix4fc#CORNER_NXNYNZ})
     * @param xDir
     *          will hold the direction and length of the span along the positive X axis
     * @param yDir
     *          will hold the direction and length of the span along the positive Y axis
     * @param zDir
     *          will hold the direction and length of the span along the positive z axis
     * @return this
     */
    public Matrix4f affineSpan(Vector3f corner, Vector3f xDir, Vector3f yDir, Vector3f zDir) {
        float a = m10 * m22, b = m10 * m21, c = m10 * m02, d = m10 * m01;
        float e = m11 * m22, f = m11 * m20, g = m11 * m02, h = m11 * m00;
        float i = m12 * m21, j = m12 * m20, k = m12 * m01, l = m12 * m00;
        float m = m20 * m02, n = m20 * m01, o = m21 * m02, p = m21 * m00;
        float q = m22 * m01, r = m22 * m00;
        float s = 1.0f / (m00 * m11 - m01 * m10) * m22 + (m02 * m10 - m00 * m12) * m21 + (m01 * m12 - m02 * m11) * m20;
        float nm00 = (e - i) * s, nm01 = (o - q) * s, nm02 = (k - g) * s;
        float nm10 = (j - a) * s, nm11 = (r - m) * s, nm12 = (c - l) * s;
        float nm20 = (b - f) * s, nm21 = (n - p) * s, nm22 = (h - d) * s;
        corner.x = -nm00 - nm10 - nm20 + (a * m31 - b * m32 + f * m32 - e * m30 + i * m30 - j * m31) * s;
        corner.y = -nm01 - nm11 - nm21 + (m * m31 - n * m32 + p * m32 - o * m30 + q * m30 - r * m31) * s;
        corner.z = -nm02 - nm12 - nm22 + (g * m30 - k * m30 + l * m31 - c * m31 + d * m32 - h * m32) * s;
        xDir.x = 2.0f * nm00; xDir.y = 2.0f * nm01; xDir.z = 2.0f * nm02;
        yDir.x = 2.0f * nm10; yDir.y = 2.0f * nm11; yDir.z = 2.0f * nm12;
        zDir.x = 2.0f * nm20; zDir.y = 2.0f * nm21; zDir.z = 2.0f * nm22;
        return this;
    }

    public boolean testPoint(float x, float y, float z) {
        float nxX = m03 + m00, nxY = m13 + m10, nxZ = m23 + m20, nxW = m33 + m30;
        float pxX = m03 - m00, pxY = m13 - m10, pxZ = m23 - m20, pxW = m33 - m30;
        float nyX = m03 + m01, nyY = m13 + m11, nyZ = m23 + m21, nyW = m33 + m31;
        float pyX = m03 - m01, pyY = m13 - m11, pyZ = m23 - m21, pyW = m33 - m31;
        float nzX = m03 + m02, nzY = m13 + m12, nzZ = m23 + m22, nzW = m33 + m32;
        float pzX = m03 - m02, pzY = m13 - m12, pzZ = m23 - m22, pzW = m33 - m32;
        return nxX * x + nxY * y + nxZ * z + nxW >= 0 && pxX * x + pxY * y + pxZ * z + pxW >= 0 &&
               nyX * x + nyY * y + nyZ * z + nyW >= 0 && pyX * x + pyY * y + pyZ * z + pyW >= 0 &&
               nzX * x + nzY * y + nzZ * z + nzW >= 0 && pzX * x + pzY * y + pzZ * z + pzW >= 0;
    }

    public boolean testSphere(float x, float y, float z, float r) {
        float invl;
        float nxX = m03 + m00, nxY = m13 + m10, nxZ = m23 + m20, nxW = m33 + m30;
        invl = Math.invsqrt(nxX * nxX + nxY * nxY + nxZ * nxZ);
        nxX *= invl; nxY *= invl; nxZ *= invl; nxW *= invl;
        float pxX = m03 - m00, pxY = m13 - m10, pxZ = m23 - m20, pxW = m33 - m30;
        invl = Math.invsqrt(pxX * pxX + pxY * pxY + pxZ * pxZ);
        pxX *= invl; pxY *= invl; pxZ *= invl; pxW *= invl;
        float nyX = m03 + m01, nyY = m13 + m11, nyZ = m23 + m21, nyW = m33 + m31;
        invl = Math.invsqrt(nyX * nyX + nyY * nyY + nyZ * nyZ);
        nyX *= invl; nyY *= invl; nyZ *= invl; nyW *= invl;
        float pyX = m03 - m01, pyY = m13 - m11, pyZ = m23 - m21, pyW = m33 - m31;
        invl = Math.invsqrt(pyX * pyX + pyY * pyY + pyZ * pyZ);
        pyX *= invl; pyY *= invl; pyZ *= invl; pyW *= invl;
        float nzX = m03 + m02, nzY = m13 + m12, nzZ = m23 + m22, nzW = m33 + m32;
        invl = Math.invsqrt(nzX * nzX + nzY * nzY + nzZ * nzZ);
        nzX *= invl; nzY *= invl; nzZ *= invl; nzW *= invl;
        float pzX = m03 - m02, pzY = m13 - m12, pzZ = m23 - m22, pzW = m33 - m32;
        invl = Math.invsqrt(pzX * pzX + pzY * pzY + pzZ * pzZ);
        pzX *= invl; pzY *= invl; pzZ *= invl; pzW *= invl;
        return nxX * x + nxY * y + nxZ * z + nxW >= -r && pxX * x + pxY * y + pxZ * z + pxW >= -r &&
               nyX * x + nyY * y + nyZ * z + nyW >= -r && pyX * x + pyY * y + pyZ * z + pyW >= -r &&
               nzX * x + nzY * y + nzZ * z + nzW >= -r && pzX * x + pzY * y + pzZ * z + pzW >= -r;
    }

    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        float nxX = m03 + m00, nxY = m13 + m10, nxZ = m23 + m20, nxW = m33 + m30;
        float pxX = m03 - m00, pxY = m13 - m10, pxZ = m23 - m20, pxW = m33 - m30;
        float nyX = m03 + m01, nyY = m13 + m11, nyZ = m23 + m21, nyW = m33 + m31;
        float pyX = m03 - m01, pyY = m13 - m11, pyZ = m23 - m21, pyW = m33 - m31;
        float nzX = m03 + m02, nzY = m13 + m12, nzZ = m23 + m22, nzW = m33 + m32;
        float pzX = m03 - m02, pzY = m13 - m12, pzZ = m23 - m22, pzW = m33 - m32;
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
     * 0 0 0 1
     * </pre>
     * 
     * @param a
     *            the value for the z factor that applies to x
     * @param b
     *            the value for the z factor that applies to y
     * @return this
     */
    public Matrix4f obliqueZ(float a, float b) {
        this.m20 = m00 * a + m10 * b + m20;
        this.m21 = m01 * a + m11 * b + m21;
        this.m22 = m02 * a + m12 * b + m22;
        this._properties(this.properties & PROPERTY_AFFINE);
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
     * 0 0 0 1
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
    public Matrix4f obliqueZ(float a, float b, Matrix4f dest) {
        dest._m00(m00)
            ._m01(m01)
            ._m02(m02)
            ._m03(m03)
            ._m10(m10)
            ._m11(m11)
            ._m12(m12)
            ._m13(m13)
            ._m20(m00 * a + m10 * b + m20)
            ._m21(m01 * a + m11 * b + m21)
            ._m22(m02 * a + m12 * b + m22)
            ._m23(m23)
            ._m30(m30)
            ._m31(m31)
            ._m32(m32)
            ._m33(m33)
            ._properties(this.properties & PROPERTY_AFFINE);
        return dest;
    }

    /**
     * Create a view and projection matrix from a given <code>eye</code> position, a given bottom left corner position <code>p</code> of the near plane rectangle
     * and the extents of the near plane rectangle along its local <code>x</code> and <code>y</code> axes, and store the resulting matrices
     * in <code>projDest</code> and <code>viewDest</code>.
     * <p>
     * This method creates a view and perspective projection matrix assuming that there is a pinhole camera at position <code>eye</code>
     * projecting the scene onto the near plane defined by the rectangle.
     * <p>
     * All positions and lengths are in the same (world) unit.
     * 
     * @param eye
     *          the position of the camera
     * @param p
     *          the bottom left corner of the near plane rectangle (will map to the bottom left corner in window coordinates)
     * @param x
     *          the direction and length of the local "bottom/top" X axis/side of the near plane rectangle
     * @param y
     *          the direction and length of the local "left/right" Y axis/side of the near plane rectangle
     * @param nearFarDist
     *          the distance between the far and near plane (the near plane will be calculated by this method).
     *          If the special value {@link Float#POSITIVE_INFINITY} is used, the far clipping plane will be at positive infinity.
     *          If the special value {@link Float#NEGATIVE_INFINITY} is used, the near and far planes will be swapped and 
     *          the near clipping plane will be at positive infinity.
     *          If a negative value is used (except for {@link Float#NEGATIVE_INFINITY}) the near and far planes will be swapped
     * @param zeroToOne
     *          whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when <code>true</code>
     *          or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when <code>false</code>
     * @param projDest
     *          will hold the resulting projection matrix
     * @param viewDest
     *          will hold the resulting view matrix
     */
    public static void projViewFromRectangle(
            Vector3f eye, Vector3f p, Vector3f x, Vector3f y, float nearFarDist, boolean zeroToOne,
            Matrix4f projDest, Matrix4f viewDest) {
        float zx = y.y * x.z - y.z * x.y, zy = y.z * x.x - y.x * x.z, zz = y.x * x.y - y.y * x.x;
        float zd = zx * (p.x - eye.x) + zy * (p.y - eye.y) + zz * (p.z - eye.z);
        float zs = zd >= 0 ? 1 : -1; zx *= zs; zy *= zs; zz *= zs; zd *= zs; 
        viewDest.setLookAt(eye.x, eye.y, eye.z, eye.x + zx, eye.y + zy, eye.z + zz, y.x, y.y, y.z);
        float px = viewDest.m00 * p.x + viewDest.m10 * p.y + viewDest.m20 * p.z + viewDest.m30;
        float py = viewDest.m01 * p.x + viewDest.m11 * p.y + viewDest.m21 * p.z + viewDest.m31;
        float tx = viewDest.m00 * x.x + viewDest.m10 * x.y + viewDest.m20 * x.z;
        float ty = viewDest.m01 * y.x + viewDest.m11 * y.y + viewDest.m21 * y.z;
        float len = Math.sqrt(zx * zx + zy * zy + zz * zz);
        float near = zd / len, far;
        if (Float.isInfinite(nearFarDist) && nearFarDist < 0.0f) {
            far = near;
            near = Float.POSITIVE_INFINITY;
        } else if (Float.isInfinite(nearFarDist) && nearFarDist > 0.0f) {
            far = Float.POSITIVE_INFINITY;
        } else if (nearFarDist < 0.0f) {
            far = near;
            near = near + nearFarDist;
        } else {
            far = near + nearFarDist;
        }
        projDest.setFrustum(px, px + tx, py, py + ty, near, far, zeroToOne);
    }

    /**
     * Apply a transformation to this matrix to ensure that the local Y axis (as obtained by {@link #positiveY(Vector3f)})
     * will be coplanar to the plane spanned by the local Z axis (as obtained by {@link #positiveZ(Vector3f)}) and the
     * given vector <code>up</code>.
     * <p>
     * This effectively ensures that the resulting matrix will be equal to the one obtained from 
     * {@link #setLookAt(Vector3fc, Vector3fc, Vector3fc)} called with the current 
     * local origin of this matrix (as obtained by {@link #originAffine(Vector3f)}), the sum of this position and the 
     * negated local Z axis as well as the given vector <code>up</code>.
     * <p>
     * This method must only be called on {@link #isAffine()} matrices.
     * 
     * @param up
     *            the up vector
     * @return this
     */
    public Matrix4f withLookAtUp(Vector3fc up) {
        return withLookAtUp(up.x(), up.y(), up.z(), this);
    }

    public Matrix4f withLookAtUp(Vector3fc up, Matrix4f dest) {
        return withLookAtUp(up.x(), up.y(), up.z());
    }

    /**
     * Apply a transformation to this matrix to ensure that the local Y axis (as obtained by {@link #positiveY(Vector3f)})
     * will be coplanar to the plane spanned by the local Z axis (as obtained by {@link #positiveZ(Vector3f)}) and the
     * given vector <code>(upX, upY, upZ)</code>.
     * <p>
     * This effectively ensures that the resulting matrix will be equal to the one obtained from 
     * {@link #setLookAt(float, float, float, float, float, float, float, float, float)} called with the current 
     * local origin of this matrix (as obtained by {@link #originAffine(Vector3f)}), the sum of this position and the 
     * negated local Z axis as well as the given vector <code>(upX, upY, upZ)</code>.
     * <p>
     * This method must only be called on {@link #isAffine()} matrices.
     * 
     * @param upX
     *            the x coordinate of the up vector
     * @param upY
     *            the y coordinate of the up vector
     * @param upZ
     *            the z coordinate of the up vector
     * @return this
     */
    public Matrix4f withLookAtUp(float upX, float upY, float upZ) {
        return withLookAtUp(upX, upY, upZ, this);
    }

    public Matrix4f withLookAtUp(float upX, float upY, float upZ, Matrix4f dest) {
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
        dest._m00(nm00)._m10(nm10)._m20(nm20)._m30(nm30)
            ._m01(nm01)._m11(nm11)._m21(nm21)._m31(nm31);
        if (dest != this) {
            dest._m02(m02)._m12(m12)._m22(m22)._m32(m32)
                ._m03(m03)._m13(m13)._m23(m23)._m33(m33);
        }
        dest._properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(m00) && Math.isFinite(m01) && Math.isFinite(m02) && Math.isFinite(m03) &&
               Math.isFinite(m10) && Math.isFinite(m11) && Math.isFinite(m12) && Math.isFinite(m13) &&
               Math.isFinite(m20) && Math.isFinite(m21) && Math.isFinite(m22) && Math.isFinite(m23) &&
               Math.isFinite(m30) && Math.isFinite(m31) && Math.isFinite(m32) && Math.isFinite(m33);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
