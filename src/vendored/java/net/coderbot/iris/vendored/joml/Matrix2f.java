/*
 * The MIT License
 *
 * Copyright (c) 2020-2021 JOML
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


import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a 2x2 matrix of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10<br>
 *      m01  m11<br>
 *
 * @author Joseph Burton
 */
public class Matrix2f implements Externalizable, Cloneable, Matrix2fc {

    private static final long serialVersionUID = 1L;

    public float m00, m01;
    public float m10, m11;

    /**
     * Create a new {@link Matrix2f} and set it to {@link #identity() identity}.
     */
    public Matrix2f() {
        m00 = 1.0f;
        m11 = 1.0f;
    }

    /**
     * Create a new {@link Matrix2f} and make it a copy of the given matrix.
     *
     * @param mat
     *          the {@link Matrix2fc} to copy the values from
     */
    public Matrix2f(Matrix2fc mat) {
        if (mat instanceof Matrix2f) {
            MemUtil.INSTANCE.copy((Matrix2f) mat, this);
        } else {
            setMatrix2fc(mat);
        }
    }

    /**
     * Create a new {@link Matrix2f} and make it a copy of the upper left 2x2 of the given {@link Matrix3fc}.
     *
     * @param mat
     *          the {@link Matrix3fc} to copy the values from
     */
    public Matrix2f(Matrix3fc mat) {
        if (mat instanceof Matrix3f) {
            MemUtil.INSTANCE.copy((Matrix3f) mat, this);
        } else {
            setMatrix3fc(mat);
        }
    }

    /**
     * Create a new 2x2 matrix using the supplied float values. The order of the parameter is column-major,
     * so the first two parameters specify the two elements of the first column.
     *
     * @param m00
     *          the value of m00
     * @param m01
     *          the value of m01
     * @param m10
     *          the value of m10
     * @param m11
     *          the value of m11
     */
    public Matrix2f(float m00, float m01,
                    float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }


    /**
     * Create a new {@link Matrix2f} by reading its 4 float components from the given {@link FloatBuffer}
     * at the buffer's current position.
     * <p>
     * That FloatBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     *
     * @param buffer
     *          the {@link FloatBuffer} to read the matrix values from
     */
    public Matrix2f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }


    /**
     * Create a new {@link Matrix2f} and initialize its two columns using the supplied vectors.
     *
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     */
    public Matrix2f(Vector2fc col0, Vector2fc col1) {
        m00 = col0.x();
        m01 = col0.y();
        m10 = col1.x();
        m11 = col1.y();
    }

    public float m00() {
        return m00;
    }
    public float m01() {
        return m01;
    }
    public float m10() {
        return m10;
    }
    public float m11() {
        return m11;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     *
     * @param m00
     *          the new value
     * @return this
     */
    public Matrix2f m00(float m00) {
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
    public Matrix2f m01(float m01) {
        this.m01 = m01;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     *
     * @param m10
     *          the new value
     * @return this
     */
    public Matrix2f m10(float m10) {
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
    public Matrix2f m11(float m11) {
        this.m11 = m11;
        return this;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     *
     * @param m00
     *          the new value
     * @return this
     */
    Matrix2f _m00(float m00) {
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
    Matrix2f _m01(float m01) {
        this.m01 = m01;
        return this;
    }
    /**
     * Set the value of the matrix element at column 1 and row 0.
     *
     * @param m10
     *          the new value
     * @return this
     */
    Matrix2f _m10(float m10) {
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
    Matrix2f _m11(float m11) {
        this.m11 = m11;
        return this;
    }

    /**
     * Set the elements of this matrix to the ones in <code>m</code>.
     *
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix2f set(Matrix2fc m) {
        if (m instanceof Matrix2f) {
            MemUtil.INSTANCE.copy((Matrix2f) m, this);
        } else {
            setMatrix2fc(m);
        }
        return this;
    }
    private void setMatrix2fc(Matrix2fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Set the elements of this matrix to the left 2x2 submatrix of <code>m</code>.
     *
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix2f set(Matrix3x2fc m) {
        if (m instanceof Matrix3x2f) {
            MemUtil.INSTANCE.copy((Matrix3x2f) m, this);
        } else {
            setMatrix3x2fc(m);
        }
        return this;
    }
    private void setMatrix3x2fc(Matrix3x2fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Set the elements of this matrix to the upper left 2x2 of the given {@link Matrix3fc}.
     *
     * @param m
     *          the {@link Matrix3fc} to copy the values from
     * @return this
     */
    public Matrix2f set(Matrix3fc m) {
        if (m instanceof Matrix3f) {
            MemUtil.INSTANCE.copy((Matrix3f) m, this);
        } else {
            setMatrix3fc(m);
        }
        return this;
    }
    private void setMatrix3fc(Matrix3fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
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
    public Matrix2f mul(Matrix2fc right) {
        return mul(right, this);
    }

    public Matrix2f mul(Matrix2fc right, Matrix2f dest) {
        float nm00 = m00 * right.m00() + m10 * right.m01();
        float nm01 = m01 * right.m00() + m11 * right.m01();
        float nm10 = m00 * right.m10() + m10 * right.m11();
        float nm11 = m01 * right.m10() + m11 * right.m11();
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
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
    public Matrix2f mulLocal(Matrix2fc left) {
        return mulLocal(left, this);
    }

    public Matrix2f mulLocal(Matrix2fc left, Matrix2f dest) {
        float nm00 = left.m00() * m00 + left.m10() * m01;
        float nm01 = left.m01() * m00 + left.m11() * m01;
        float nm10 = left.m00() * m10 + left.m10() * m11;
        float nm11 = left.m01() * m10 + left.m11() * m11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied float values. The result looks like this:
     * <p>
     * m00, m10<br>
     * m01, m11<br>
     *
     * @param m00
     *          the new value of m00
     * @param m01
     *          the new value of m01
     * @param m10
     *          the new value of m10
     * @param m11
     *          the new value of m11
     * @return this
     */
    public Matrix2f set(float m00, float m01,
                        float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        return this;
    }

    /**
     * Set the values in this matrix based on the supplied float array. The result looks like this:
     * <p>
     * 0, 2<br>
     * 1, 3<br>
     *
     * This method only uses the first 4 values, all others are ignored.
     *
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix2f set(float m[]) {
        MemUtil.INSTANCE.copy(m, 0, this);
        return this;
    }

    /**
     * Set the two columns of this matrix to the supplied vectors, respectively.
     *
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     * @return this
     */
    public Matrix2f set(Vector2fc col0, Vector2fc col1) {
        m00 = col0.x();
        m01 = col0.y();
        m10 = col1.x();
        m11 = col1.y();
        return this;
    }

    public float determinant() {
        return m00 * m11 - m10 * m01;
    }

    /**
     * Invert this matrix.
     *
     * @return this
     */
    public Matrix2f invert() {
        return invert(this);
    }

    public Matrix2f invert(Matrix2f dest) {
        float s = 1.0f / determinant();
        float nm00 = m11 * s;
        float nm01 = -m01 * s;
        float nm10 = -m10 * s;
        float nm11 = m00 * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    /**
     * Transpose this matrix.
     *
     * @return this
     */
    public Matrix2f transpose() {
        return transpose(this);
    }

    public Matrix2f transpose(Matrix2f dest) {
        dest.set(m00, m10,
                 m01, m11);
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
        return Runtime.format(m00, formatter) + " " + Runtime.format(m10, formatter) + "\n"
             + Runtime.format(m01, formatter) + " " + Runtime.format(m11, formatter) + "\n";
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix2fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     *
     * @see #set(Matrix2fc)
     *
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    public Matrix2f get(Matrix2f dest) {
        return dest.set(this);
    }

    public Matrix3x2f get(Matrix3x2f dest) {
        return dest.set(this);
    }

    public Matrix3f get(Matrix3f dest) {
        return dest.set(this);
    }

    public float getRotation() {
        return Math.atan2(m01, m11);
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

    public FloatBuffer getTransposed(FloatBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    public FloatBuffer getTransposed(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.putTransposed(this, index, buffer);
        return buffer;
    }

    public ByteBuffer getTransposed(ByteBuffer buffer) {
        return get(buffer.position(), buffer);
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
     * Set the values of this matrix by reading 4 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix2f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix2f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix2f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix2f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set all values within this matrix to zero.
     *
     * @return this
     */
    public Matrix2f zero() {
        MemUtil.INSTANCE.zero(this);
        return this;
    }

    /**
     * Set this matrix to the identity.
     *
     * @return this
     */
    public Matrix2f identity() {
        MemUtil.INSTANCE.identity(this);
        return this;
    }

    public Matrix2f scale(Vector2fc xy, Matrix2f dest) {
        return scale(xy.x(), xy.y(), dest);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xy.x</code> and
     * <code>xy.y</code> factors, respectively.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     *
     * @param xy
     *            the factors of the x and y component, respectively
     * @return this
     */
    public Matrix2f scale(Vector2fc xy) {
        return scale(xy.x(), xy.y(), this);
    }

    public Matrix2f scale(float x, float y, Matrix2f dest) {
        // scale matrix elements:
        // m00 = x, m11 = y
        // all others = 0
        dest.m00 = m00 * x;
        dest.m01 = m01 * x;
        dest.m10 = m10 * y;
        dest.m11 = m11 * y;
        return dest;
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x and
     * y factors.
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
     * @return this
     */
    public Matrix2f scale(float x, float y) {
        return scale(x, y, this);
    }

    public Matrix2f scale(float xy, Matrix2f dest) {
        return scale(xy, xy, dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xy</code> factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     *
     * @see #scale(float, float)
     *
     * @param xy
     *            the factor for all components
     * @return this
     */
    public Matrix2f scale(float xy) {
        return scale(xy, xy);
    }

    public Matrix2f scaleLocal(float x, float y, Matrix2f dest) {
        dest.m00 = x * m00;
        dest.m01 = y * m01;
        dest.m10 = x * m10;
        dest.m11 = y * m11;
        return dest;
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given x and
     * y factors.
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
     * @return this
     */
    public Matrix2f scaleLocal(float x, float y) {
        return scaleLocal(x, y, this);
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
     *             the scale factor in x and y
     * @return this
     */
    public Matrix2f scaling(float factor) {
        MemUtil.INSTANCE.zero(this);
        m00 = factor;
        m11 = factor;
        return this;
    }

    /**
     * Set this matrix to be a simple scale matrix.
     *
     * @param x
     *             the scale in x
     * @param y
     *             the scale in y
     * @return this
     */
    public Matrix2f scaling(float x, float y) {
        MemUtil.INSTANCE.zero(this);
        m00 = x;
        m11 = y;
        return this;
    }

    /**
     * Set this matrix to be a simple scale matrix which scales the base axes by <code>xy.x</code> and <code>xy.y</code> respectively.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a
     * matrix use {@link #scale(Vector2fc) scale()} instead.
     *
     * @see #scale(Vector2fc)
     *
     * @param xy
     *             the scale in x and y respectively
     * @return this
     */
    public Matrix2f scaling(Vector2fc xy) {
        return scaling(xy.x(), xy.y());
    }

    /**
     * Set this matrix to a rotation matrix which rotates the given radians about the origin.
     * <p>
     * The produced rotation will rotate a vector counter-clockwise around the origin.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to post-multiply a rotation transformation directly to a
     * matrix, use {@link #rotate(float) rotate()} instead.
     *
     * @see #rotate(float)
     *
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Matrix2f rotation(float angle) {
        float sin = Math.sin(angle);
        float cos = Math.cosFromSin(sin, angle);
        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
        return this;
    }

    public Vector2f transform(Vector2f v) {
        return v.mul(this);
    }

    public Vector2f transform(Vector2fc v, Vector2f dest) {
        v.mul(this, dest);
        return dest;
    }

    public Vector2f transform(float x, float y, Vector2f dest) {
        dest.set(m00 * x + m10 * y,
                 m01 * x + m11 * y);
        return dest;
    }

    public Vector2f transformTranspose(Vector2f v) {
        return v.mulTranspose(this);
    }

    public Vector2f transformTranspose(Vector2fc v, Vector2f dest) {
        v.mulTranspose(this, dest);
        return dest;
    }

    public Vector2f transformTranspose(float x, float y, Vector2f dest) {
        dest.set(m00 * x + m01 * y,
                 m10 * x + m11 * y);
        return dest;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(m00);
        out.writeFloat(m01);
        out.writeFloat(m10);
        out.writeFloat(m11);
    }

    public void readExternal(ObjectInput in) throws IOException {
        m00 = in.readFloat();
        m01 = in.readFloat();
        m10 = in.readFloat();
        m11 = in.readFloat();
    }

    /**
     * Apply rotation about the origin to this matrix by rotating the given amount of radians.
     * <p>
     * The produced rotation will rotate a vector counter-clockwise around the origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions">http://en.wikipedia.org</a>
     *
     * @param angle
     *            the angle in radians
     * @return this
     */
    public Matrix2f rotate(float angle) {
        return rotate(angle, this);
    }

    public Matrix2f rotate(float angle, Matrix2f dest) {
        float s = Math.sin(angle);
        float c = Math.cosFromSin(s, angle);
        // rotation matrix elements:
        // m00 = c, m01 = s, m10 = -s, m11 = c
        float nm00 = m00 * c + m10 * s;
        float nm01 = m01 * c + m11 * s;
        float nm10 = m10 * c - m00 * s;
        float nm11 = m11 * c - m01 * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians about the origin.
     * <p>
     * The produced rotation will rotate a vector counter-clockwise around the origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotation(float) rotation()}.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions">http://en.wikipedia.org</a>
     *
     * @see #rotation(float)
     *
     * @param angle
     *            the angle in radians to rotate about the X axis
     * @return this
     */
    public Matrix2f rotateLocal(float angle) {
        return rotateLocal(angle, this);
    }

    public Matrix2f rotateLocal(float angle, Matrix2f dest) {
        float s = Math.sin(angle);
        float c = Math.cosFromSin(s, angle);
        // rotation matrix elements:
        // m00 = c, m01 = s, m10 = -s, m11 = c
        float nm00 = c * m00 - s * m01;
        float nm01 = s * m00 + c * m01;
        float nm10 = c * m10 - s * m11;
        float nm11 = s * m10 + c * m11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    public Vector2f getRow(int row, Vector2f dest) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            dest.x = m00;
            dest.y = m10;
            break;
        case 1:
            dest.x = m01;
            dest.y = m11;
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
     *          the row index in <code>[0..1]</code>
     * @param src
     *          the row components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..1]</code>
     */
    public Matrix2f setRow(int row, Vector2fc src) throws IndexOutOfBoundsException {
        return setRow(row, src.x(), src.y());
    }

    /**
     * Set the row at the given <code>row</code> index, starting with <code>0</code>.
     *
     * @param row
     *          the row index in <code>[0..1]</code>
     * @param x
     *          the first element in the row
     * @param y
     *          the second element in the row
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..1]</code>
     */
    public Matrix2f setRow(int row, float x, float y) throws IndexOutOfBoundsException {
        switch (row) {
        case 0:
            this.m00 = x;
            this.m10 = y;
            break;
        case 1:
            this.m01 = x;
            this.m11 = y;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public Vector2f getColumn(int column, Vector2f dest) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            dest.x = m00;
            dest.y = m01;
            break;
        case 1:
            dest.x = m10;
            dest.y = m11;
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
     *          the column index in <code>[0..1]</code>
     * @param src
     *          the column components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..1]</code>
     */
    public Matrix2f setColumn(int column, Vector2fc src) throws IndexOutOfBoundsException {
        return setColumn(column, src.x(), src.y());
    }

    /**
     * Set the column at the given <code>column</code> index, starting with <code>0</code>.
     *
     * @param column
     *          the column index in <code>[0..1]</code>
     * @param x
     *          the first element in the column
     * @param y
     *          the second element in the column
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..1]</code>
     */
    public Matrix2f setColumn(int column, float x, float y) throws IndexOutOfBoundsException {
        switch (column) {
        case 0:
            this.m00 = x;
            this.m01 = y;
            break;
        case 1:
            this.m10 = x;
            this.m11 = y;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public float get(int column, int row) {
        switch (column) {
        case 0:
            switch (row) {
            case 0:
                return m00;
            case 1:
                return m01;
            default:
                break;
            }
            break;
        case 1:
            switch (row) {
            case 0:
                return m10;
            case 1:
                return m11;
            default:
                break;
            }
            break;
        default:
            break;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Set the matrix element at the given column and row to the specified value.
     *
     * @param column
     *          the colum index in <code>[0..1]</code>
     * @param row
     *          the row index in <code>[0..1]</code>
     * @param value
     *          the value
     * @return this
     */
    public Matrix2f set(int column, int row, float value) {
        switch (column) {
            case 0:
                switch (row) {
                    case 0:
                        this.m00 = value;
                        return this;
                    case 1:
                        this.m01 = value;
                        return this;
                    default:
                        break;
                }
                break;
            case 1:
                switch (row) {
                    case 0:
                        this.m10 = value;
                        return this;
                    case 1:
                        this.m11 = value;
                        return this;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Set <code>this</code> matrix to its own normal matrix.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors,
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In this case, use {@link #set(Matrix2fc)} to set a given Matrix2f to this matrix.
     *
     * @see #set(Matrix2fc)
     *
     * @return this
     */
    public Matrix2f normal() {
        return normal(this);
    }

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors,
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In this case, use {@link #set(Matrix2fc)} to set a given Matrix2f to this matrix.
     *
     * @see #set(Matrix2fc)
     *
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix2f normal(Matrix2f dest) {
        float det = m00 * m11 - m10 * m01;
        float s = 1.0f / det;
        /* Invert and transpose in one go */
        float nm00 = m11 * s;
        float nm01 = -m10 * s;
        float nm10 = -m01 * s;
        float nm11 = m00 * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    public Vector2f getScale(Vector2f dest) {
        dest.x = Math.sqrt(m00 * m00 + m01 * m01);
        dest.y = Math.sqrt(m10 * m10 + m11 * m11);
        return dest;
    }

    public Vector2f positiveX(Vector2f dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = -m11;
            dir.y = m01;
        } else {
            dir.x = m11;
            dir.y = -m01;
        }
        return dir.normalize(dir);
    }

    public Vector2f normalizedPositiveX(Vector2f dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = -m11;
            dir.y = m01;
        } else {
            dir.x = m11;
            dir.y = -m01;
        }
        return dir;
    }

    public Vector2f positiveY(Vector2f dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = m10;
            dir.y = -m00;
        } else {
            dir.x = -m10;
            dir.y = m00;
        }
        return dir.normalize(dir);
    }

    public Vector2f normalizedPositiveY(Vector2f dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = m10;
            dir.y = -m00;
        } else {
            dir.x = -m10;
            dir.y = m00;
        }
        return dir;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(m00);
        result = prime * result + Float.floatToIntBits(m01);
        result = prime * result + Float.floatToIntBits(m10);
        result = prime * result + Float.floatToIntBits(m11);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Matrix2f other = (Matrix2f) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00))
            return false;
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01))
            return false;
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10))
            return false;
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11))
            return false;
        return true;
    }

    public boolean equals(Matrix2fc m, float delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix2f))
            return false;
        if (!Runtime.equals(m00, m.m00(), delta))
            return false;
        if (!Runtime.equals(m01, m.m01(), delta))
            return false;
        if (!Runtime.equals(m10, m.m10(), delta))
            return false;
        if (!Runtime.equals(m11, m.m11(), delta))
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
    public Matrix2f swap(Matrix2f other) {
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
    public Matrix2f add(Matrix2fc other) {
        return add(other, this);
    }

    public Matrix2f add(Matrix2fc other, Matrix2f dest) {
        dest.m00 = m00 + other.m00();
        dest.m01 = m01 + other.m01();
        dest.m10 = m10 + other.m10();
        dest.m11 = m11 + other.m11();
        return dest;
    }

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code>.
     *
     * @param subtrahend
     *          the subtrahend
     * @return this
     */
    public Matrix2f sub(Matrix2fc subtrahend) {
        return sub(subtrahend, this);
    }

    public Matrix2f sub(Matrix2fc other, Matrix2f dest) {
        dest.m00 = m00 - other.m00();
        dest.m01 = m01 - other.m01();
        dest.m10 = m10 - other.m10();
        dest.m11 = m11 - other.m11();
        return dest;
    }

    /**
     * Component-wise multiply <code>this</code> by <code>other</code>.
     *
     * @param other
     *          the other matrix
     * @return this
     */
    public Matrix2f mulComponentWise(Matrix2fc other) {
        return sub(other, this);
    }

    public Matrix2f mulComponentWise(Matrix2fc other, Matrix2f dest) {
        dest.m00 = m00 * other.m00();
        dest.m01 = m01 * other.m01();
        dest.m10 = m10 * other.m10();
        dest.m11 = m11 * other.m11();
        return dest;
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
    public Matrix2f lerp(Matrix2fc other, float t) {
        return lerp(other, t, this);
    }

    public Matrix2f lerp(Matrix2fc other, float t, Matrix2f dest) {
        dest.m00 = Math.fma(other.m00() - m00, t, m00);
        dest.m01 = Math.fma(other.m01() - m01, t, m01);
        dest.m10 = Math.fma(other.m10() - m10, t, m10);
        dest.m11 = Math.fma(other.m11() - m11, t, m11);
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(m00) && Math.isFinite(m01) &&
               Math.isFinite(m10) && Math.isFinite(m11);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
