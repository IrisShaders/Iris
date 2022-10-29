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
import java.nio.DoubleBuffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a 2x2 matrix of doubles, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10<br>
 *      m01  m11<br>
 *
 * @author Joseph Burton
 */
public class Matrix2d implements Externalizable, Cloneable, Matrix2dc {

    private static final long serialVersionUID = 1L;

    public double m00, m01;
    public double m10, m11;

    /**
     * Create a new {@link Matrix2d} and set it to {@link #identity() identity}.
     */
    public Matrix2d() {
        m00 = 1.0;
        m11 = 1.0;
    }

    /**
     * Create a new {@link Matrix2d} and make it a copy of the given matrix.
     *
     * @param mat
     *          the {@link Matrix2dc} to copy the values from
     */
    public Matrix2d(Matrix2dc mat) {
        if (mat instanceof Matrix2d) {
            MemUtil.INSTANCE.copy((Matrix2d) mat, this);
        } else {
            setMatrix2dc(mat);
        }
    }

    /**
     * Create a new {@link Matrix2d} and initialize it with the values from the given matrix.
     *
     * @param mat
     *          the matrix to initialize this matrix with
     */
    public Matrix2d(Matrix2fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Create a new {@link Matrix2d} and make it a copy of the upper left 2x2 of the given {@link Matrix3dc}.
     *
     * @param mat
     *          the {@link Matrix3dc} to copy the values from
     */
    public Matrix2d(Matrix3dc mat) {
        if (mat instanceof Matrix3d) {
            MemUtil.INSTANCE.copy((Matrix3d) mat, this);
        } else {
            setMatrix3dc(mat);
        }
    }

    /**
     * Create a new {@link Matrix2d} and make it a copy of the upper left 2x2 of the given {@link Matrix3fc}.
     *
     * @param mat
     *          the {@link Matrix3fc} to copy the values from
     */
    public Matrix2d(Matrix3fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Create a new 2x2 matrix using the supplied double values. The order of the parameter is column-major,
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
    public Matrix2d(double m00, double m01,
                    double m10, double m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }


    /**
     * Create a new {@link Matrix2d} by reading its 4 double components from the given {@link DoubleBuffer}
     * at the buffer's current position.
     * <p>
     * That DoubleBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     *
     * @param buffer
     *          the {@link DoubleBuffer} to read the matrix values from
     */
    public Matrix2d(DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }


    /**
     * Create a new {@link Matrix2d} and initialize its two columns using the supplied vectors.
     *
     * @param col0
     *          the first column
     * @param col1
     *          the second column
     */
    public Matrix2d(Vector2dc col0, Vector2dc col1) {
        m00 = col0.x();
        m01 = col0.y();
        m10 = col1.x();
        m11 = col1.y();
    }

    public double m00() {
        return m00;
    }
    public double m01() {
        return m01;
    }
    public double m10() {
        return m10;
    }
    public double m11() {
        return m11;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     *
     * @param m00
     *          the new value
     * @return this
     */
    public Matrix2d m00(double m00) {
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
    public Matrix2d m01(double m01) {
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
    public Matrix2d m10(double m10) {
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
    public Matrix2d m11(double m11) {
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
    Matrix2d _m00(double m00) {
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
    Matrix2d _m01(double m01) {
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
    Matrix2d _m10(double m10) {
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
    Matrix2d _m11(double m11) {
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
    public Matrix2d set(Matrix2dc m) {
        if (m instanceof Matrix2d) {
            MemUtil.INSTANCE.copy((Matrix2d) m, this);
        } else {
            setMatrix2dc(m);
        }
        return this;
    }
    private void setMatrix2dc(Matrix2dc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Set the elements of this matrix to the ones in <code>m</code>.
     *
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix2d set(Matrix2fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m10 = m.m10();
        m11 = m.m11();
        return this;
    }

    /**
     * Set the elements of this matrix to the left 2x2 submatrix of <code>m</code>.
     *
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix2d set(Matrix3x2dc m) {
        if (m instanceof Matrix3x2d) {
            MemUtil.INSTANCE.copy((Matrix3x2d) m, this);
        } else {
            setMatrix3x2dc(m);
        }
        return this;
    }
    private void setMatrix3x2dc(Matrix3x2dc mat) {
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
    public Matrix2d set(Matrix3x2fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m10 = m.m10();
        m11 = m.m11();
        return this;
    }

    /**
     * Set the elements of this matrix to the upper left 2x2 of the given {@link Matrix3dc}.
     *
     * @param m
     *          the {@link Matrix3dc} to copy the values from
     * @return this
     */
    public Matrix2d set(Matrix3dc m) {
        if (m instanceof Matrix3d) {
            MemUtil.INSTANCE.copy((Matrix3d) m, this);
        } else {
            setMatrix3dc(m);
        }
        return this;
    }
    private void setMatrix3dc(Matrix3dc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Set the elements of this matrix to the upper left 2x2 of the given {@link Matrix3dc}.
     *
     * @param m
     *          the {@link Matrix3fc} to copy the values from
     * @return this
     */
    public Matrix2d set(Matrix3fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m10 = m.m10();
        m11 = m.m11();
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
    public Matrix2d mul(Matrix2dc right) {
        return mul(right, this);
    }

    public Matrix2d mul(Matrix2dc right, Matrix2d dest) {
        double nm00 = m00 * right.m00() + m10 * right.m01();
        double nm01 = m01 * right.m00() + m11 * right.m01();
        double nm10 = m00 * right.m10() + m10 * right.m11();
        double nm11 = m01 * right.m10() + m11 * right.m11();
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
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
    public Matrix2d mul(Matrix2fc right) {
        return mul(right, this);
    }

    public Matrix2d mul(Matrix2fc right, Matrix2d dest) {
        double nm00 = m00 * right.m00() + m10 * right.m01();
        double nm01 = m01 * right.m00() + m11 * right.m01();
        double nm10 = m00 * right.m10() + m10 * right.m11();
        double nm11 = m01 * right.m10() + m11 * right.m11();
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
    public Matrix2d mulLocal(Matrix2dc left) {
        return mulLocal(left, this);
    }

    public Matrix2d mulLocal(Matrix2dc left, Matrix2d dest) {
        double nm00 = left.m00() * m00 + left.m10() * m01;
        double nm01 = left.m01() * m00 + left.m11() * m01;
        double nm10 = left.m00() * m10 + left.m10() * m11;
        double nm11 = left.m01() * m10 + left.m11() * m11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied double values. The result looks like this:
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
    public Matrix2d set(double m00, double m01,
                        double m10, double m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        return this;
    }

    /**
     * Set the values in this matrix based on the supplied double array. The result looks like this:
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
    public Matrix2d set(double m[]) {
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
    public Matrix2d set(Vector2dc col0, Vector2dc col1) {
        m00 = col0.x();
        m01 = col0.y();
        m10 = col1.x();
        m11 = col1.y();
        return this;
    }

    public double determinant() {
        return m00 * m11 - m10 * m01;
    }

    /**
     * Invert this matrix.
     *
     * @return this
     */
    public Matrix2d invert() {
        return invert(this);
    }

    public Matrix2d invert(Matrix2d dest) {
        double s = 1.0 / determinant();
        double nm00 = m11 * s;
        double nm01 = -m01 * s;
        double nm10 = -m10 * s;
        double nm11 = m00 * s;
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
    public Matrix2d transpose() {
        return transpose(this);
    }

    public Matrix2d transpose(Matrix2d dest) {
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
     * This is the reverse method of {@link #set(Matrix2dc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     *
     * @see #set(Matrix2dc)
     *
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    public Matrix2d get(Matrix2d dest) {
        return dest.set(this);
    }

    public Matrix3x2d get(Matrix3x2d dest) {
        return dest.set(this);
    }

    public Matrix3d get(Matrix3d dest) {
        return dest.set(this);
    }

    public double getRotation() {
        return (double) Math.atan2(m01, m11);
    }


    public DoubleBuffer get(DoubleBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    public DoubleBuffer get(int index, DoubleBuffer buffer) {
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

    public DoubleBuffer getTransposed(DoubleBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    public DoubleBuffer getTransposed(int index, DoubleBuffer buffer) {
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


    public double[] get(double[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    public double[] get(double[] arr) {
        return get(arr, 0);
    }


    /**
     * Set the values of this matrix by reading 4 double values from the given {@link DoubleBuffer} in column-major order,
     * starting at its current position.
     * <p>
     * The DoubleBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the DoubleBuffer will not be changed by this method.
     *
     * @param buffer
     *              the DoubleBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix2d set(DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 double values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix2d set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 double values from the given {@link DoubleBuffer} in column-major order,
     * starting at the specified absolute buffer position/index.
     * <p>
     * The DoubleBuffer is expected to contain the values in column-major order.
     * <p>
     * The position of the DoubleBuffer will not be changed by this method.
     * 
     * @param index
     *              the absolute position into the DoubleBuffer
     * @param buffer
     *              the DoubleBuffer to read the matrix values from in column-major order
     * @return this
     */
    public Matrix2d set(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 4 double values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix2d set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set all values within this matrix to zero.
     *
     * @return this
     */
    public Matrix2d zero() {
        MemUtil.INSTANCE.zero(this);
        return this;
    }

    /**
     * Set this matrix to the identity.
     *
     * @return this
     */
    public Matrix2d identity() {
        m00 = 1.0;
        m01 = 0.0;
        m10 = 0.0;
        m11 = 1.0;
        return this;
    }

    public Matrix2d scale(Vector2dc xy, Matrix2d dest) {
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
    public Matrix2d scale(Vector2dc xy) {
        return scale(xy.x(), xy.y(), this);
    }

    public Matrix2d scale(double x, double y, Matrix2d dest) {
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
    public Matrix2d scale(double x, double y) {
        return scale(x, y, this);
    }

    public Matrix2d scale(double xy, Matrix2d dest) {
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
     * @see #scale(double, double)
     *
     * @param xy
     *            the factor for all components
     * @return this
     */
    public Matrix2d scale(double xy) {
        return scale(xy, xy);
    }

    public Matrix2d scaleLocal(double x, double y, Matrix2d dest) {
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
    public Matrix2d scaleLocal(double x, double y) {
        return scaleLocal(x, y, this);
    }

    /**
     * Set this matrix to be a simple scale matrix, which scales all axes uniformly by the given factor.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a
     * matrix, use {@link #scale(double) scale()} instead.
     *
     * @see #scale(double)
     *
     * @param factor
     *             the scale factor in x and y
     * @return this
     */
    public Matrix2d scaling(double factor) {
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
    public Matrix2d scaling(double x, double y) {
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
     * matrix use {@link #scale(Vector2dc) scale()} instead.
     *
     * @see #scale(Vector2dc)
     *
     * @param xy
     *             the scale in x and y respectively
     * @return this
     */
    public Matrix2d scaling(Vector2dc xy) {
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
     * matrix, use {@link #rotate(double) rotate()} instead.
     *
     * @see #rotate(double)
     *
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Matrix2d rotation(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cosFromSin(sin, angle);
        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
        return this;
    }

    public Vector2d transform(Vector2d v) {
        return v.mul(this);
    }

    public Vector2d transform(Vector2dc v, Vector2d dest) {
        v.mul(this, dest);
        return dest;
    }

    public Vector2d transform(double x, double y, Vector2d dest) {
        dest.set(m00 * x + m10 * y,
                m01 * x + m11 * y);
        return dest;
    }

    public Vector2d transformTranspose(Vector2d v) {
        return v.mulTranspose(this);
    }

    public Vector2d transformTranspose(Vector2dc v, Vector2d dest) {
        v.mulTranspose(this, dest);
        return dest;
    }

    public Vector2d transformTranspose(double x, double y, Vector2d dest) {
        dest.set(m00 * x + m01 * y,
                m10 * x + m11 * y);
        return dest;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(m00);
        out.writeDouble(m01);
        out.writeDouble(m10);
        out.writeDouble(m11);
    }

    public void readExternal(ObjectInput in) throws IOException {
        m00 = in.readDouble();
        m01 = in.readDouble();
        m10 = in.readDouble();
        m11 = in.readDouble();
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
    public Matrix2d rotate(double angle) {
        return rotate(angle, this);
    }

    public Matrix2d rotate(double angle, Matrix2d dest) {
        double s = Math.sin(angle);
        double c = Math.cosFromSin(s, angle);
        // rotation matrix elements:
        // m00 = c, m01 = s, m10 = -s, m11 = c
        double nm00 = m00 * c + m10 * s;
        double nm01 = m01 * c + m11 * s;
        double nm10 = m10 * c - m00 * s;
        double nm11 = m11 * c - m01 * s;
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
     * transformation, use {@link #rotation(double) rotation()}.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions">http://en.wikipedia.org</a>
     *
     * @see #rotation(double)
     *
     * @param angle
     *            the angle in radians to rotate about the X axis
     * @return this
     */
    public Matrix2d rotateLocal(double angle) {
        return rotateLocal(angle, this);
    }

    public Matrix2d rotateLocal(double angle, Matrix2d dest) {
        double s = Math.sin(angle);
        double c = Math.cosFromSin(s, angle);
        // rotation matrix elements:
        // m00 = c, m01 = s, m10 = -s, m11 = c
        double nm00 = c * m00 - s * m01;
        double nm01 = s * m00 + c * m01;
        double nm10 = c * m10 - s * m11;
        double nm11 = s * m10 + c * m11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    public Vector2d getRow(int row, Vector2d dest) throws IndexOutOfBoundsException {
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
    public Matrix2d setRow(int row, Vector2dc src) throws IndexOutOfBoundsException {
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
    public Matrix2d setRow(int row, double x, double y) throws IndexOutOfBoundsException {
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

    public Vector2d getColumn(int column, Vector2d dest) throws IndexOutOfBoundsException {
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
    public Matrix2d setColumn(int column, Vector2dc src) throws IndexOutOfBoundsException {
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
    public Matrix2d setColumn(int column, double x, double y) throws IndexOutOfBoundsException {
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

    public double get(int column, int row) {
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
    public Matrix2d set(int column, int row, double value) {
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
     * In this case, use {@link #set(Matrix2dc)} to set a given Matrix2d to this matrix.
     *
     * @see #set(Matrix2dc)
     *
     * @return this
     */
    public Matrix2d normal() {
        return normal(this);
    }

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     * <p>
     * Please note that, if <code>this</code> is an orthogonal matrix or a matrix whose columns are orthogonal vectors,
     * then this method <i>need not</i> be invoked, since in that case <code>this</code> itself is its normal matrix.
     * In this case, use {@link #set(Matrix2dc)} to set a given Matrix2d to this matrix.
     *
     * @see #set(Matrix2dc)
     *
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix2d normal(Matrix2d dest) {
        double det = m00 * m11 - m10 * m01;
        double s = 1.0 / det;
        /* Invert and transpose in one go */
        double nm00 = m11 * s;
        double nm01 = -m10 * s;
        double nm10 = -m01 * s;
        double nm11 = m00 * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        return dest;
    }

    public Vector2d getScale(Vector2d dest) {
        dest.x = Math.sqrt(m00 * m00 + m01 * m01);
        dest.y = Math.sqrt(m10 * m10 + m11 * m11);
        return dest;
    }

    public Vector2d positiveX(Vector2d dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = -m11;
            dir.y = m01;
        } else {
            dir.x = m11;
            dir.y = -m01;
        }
        return dir.normalize(dir);
    }

    public Vector2d normalizedPositiveX(Vector2d dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = -m11;
            dir.y = m01;
        } else {
            dir.x = m11;
            dir.y = -m01;
        }
        return dir;
    }

    public Vector2d positiveY(Vector2d dir) {
        if (m00 * m11 < m01 * m10) { // negative determinant?
            dir.x = m10;
            dir.y = -m00;
        } else {
            dir.x = -m10;
            dir.y = m00;
        }
        return dir.normalize(dir);
    }

    public Vector2d normalizedPositiveY(Vector2d dir) {
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
        long temp;
        temp = Double.doubleToLongBits(m00);
        result = prime * result + (int) ((temp >>> 32) ^ temp);
        temp = Double.doubleToLongBits(m01);
        result = prime * result + (int) ((temp >>> 32) ^ temp);
        temp = Double.doubleToLongBits(m10);
        result = prime * result + (int) ((temp >>> 32) ^ temp);
        temp = Double.doubleToLongBits(m11);
        result = prime * result + (int) ((temp >>> 32) ^ temp);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Matrix2d other = (Matrix2d) obj;
        if (Double.doubleToLongBits(m00) != Double.doubleToLongBits(other.m00))
            return false;
        if (Double.doubleToLongBits(m01) != Double.doubleToLongBits(other.m01))
            return false;
        if (Double.doubleToLongBits(m10) != Double.doubleToLongBits(other.m10))
            return false;
        if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11))
            return false;
        return true;
    }

    public boolean equals(Matrix2dc m, double delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix2d))
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
    public Matrix2d swap(Matrix2d other) {
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
    public Matrix2d add(Matrix2dc other) {
        return add(other, this);
    }

    public Matrix2d add(Matrix2dc other, Matrix2d dest) {
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
    public Matrix2d sub(Matrix2dc subtrahend) {
        return sub(subtrahend, this);
    }

    public Matrix2d sub(Matrix2dc other, Matrix2d dest) {
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
    public Matrix2d mulComponentWise(Matrix2dc other) {
        return sub(other, this);
    }

    public Matrix2d mulComponentWise(Matrix2dc other, Matrix2d dest) {
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
    public Matrix2d lerp(Matrix2dc other, double t) {
        return lerp(other, t, this);
    }

    public Matrix2d lerp(Matrix2dc other, double t, Matrix2d dest) {
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
