/*
 * The MIT License
 *
 * Copyright (c) 2017-2021 JOML
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
import java.nio.DoubleBuffer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a 3x2 matrix of doubles, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10  m20<br>
 *      m01  m11  m21<br>
 * 
 * @author Kai Burjack
 */
public class Matrix3x2d implements Matrix3x2dc, Cloneable, Externalizable {

    private static final long serialVersionUID = 1L;

    public double m00, m01;
    public double m10, m11;
    public double m20, m21;

    /**
     * Create a new {@link Matrix3x2d} and set it to {@link #identity() identity}.
     */
    public Matrix3x2d() {
        this.m00 = 1.0;
        this.m11 = 1.0;
    }

    /**
     * Create a new {@link Matrix3x2d} by setting its left 2x2 submatrix to the values of the given {@link Matrix2dc}
     * and the rest to identity.
     *
     * @param mat
     *          the {@link Matrix2dc}
     */
    public Matrix3x2d(Matrix2dc mat) {
        if (mat instanceof Matrix2d) {
            MemUtil.INSTANCE.copy((Matrix2d) mat, this);
        } else {
            setMatrix2dc(mat);
        }
    }

    /**
     * Create a new {@link Matrix3x2d} by setting its left 2x2 submatrix to the values of the given {@link Matrix2fc}
     * and the rest to identity.
     *
     * @param mat
     *          the {@link Matrix2fc}
     */
    public Matrix3x2d(Matrix2fc mat) {
        m00 = mat.m00();
        m01 = mat.m01();
        m10 = mat.m10();
        m11 = mat.m11();
    }

    /**
     * Create a new {@link Matrix3x2d} and make it a copy of the given matrix.
     * 
     * @param mat
     *          the {@link Matrix3x2dc} to copy the values from
     */
    public Matrix3x2d(Matrix3x2dc mat) {
        if (mat instanceof Matrix3x2d) {
            MemUtil.INSTANCE.copy((Matrix3x2d) mat, this);
        } else {
            setMatrix3x2dc(mat);
        }
    }

    /**
     * Create a new 3x2 matrix using the supplied double values. The order of the parameter is column-major, 
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
     * @param m20
     *          the value of m20
     * @param m21
     *          the value of m21
     */
    public Matrix3x2d(double m00, double m01,
                      double m10, double m11,
                      double m20, double m21) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
    }


    /**
     * Create a new {@link Matrix3x2d} by reading its 6 double components from the given {@link DoubleBuffer}
     * at the buffer's current position.
     * <p>
     * That DoubleBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     * 
     * @param buffer
     *          the {@link DoubleBuffer} to read the matrix values from
     */
    public Matrix3x2d(DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
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
    public double m20() {
        return m20;
    }
    public double m21() {
        return m21;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    Matrix3x2d _m00(double m00) {
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
    Matrix3x2d _m01(double m01) {
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
    Matrix3x2d _m10(double m10) {
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
    Matrix3x2d _m11(double m11) {
        this.m11 = m11;
        return this;
    }
    /**
     * Set the value of the matrix element at column 2 and row 0.
     * 
     * @param m20
     *          the new value
     * @return this
     */
    Matrix3x2d _m20(double m20) {
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
    Matrix3x2d _m21(double m21) {
        this.m21 = m21;
        return this;
    }

    /**
     * Set the elements of this matrix to the ones in <code>m</code>.
     * 
     * @param m
     *          the matrix to copy the elements from
     * @return this
     */
    public Matrix3x2d set(Matrix3x2dc m) {
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
        m20 = mat.m20();
        m21 = mat.m21();
    }

    /**
     * Set the left 2x2 submatrix of this {@link Matrix3x2d} to the given {@link Matrix2dc} and don't change the other elements.
     *
     * @param m
     *          the 2x2 matrix
     * @return this
     */
    public Matrix3x2d set(Matrix2dc m) {
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
     * Set the left 2x2 submatrix of this {@link Matrix3x2d} to the given {@link Matrix2fc} and don't change the other elements.
     *
     * @param m
     *          the 2x2 matrix
     * @return this
     */
    public Matrix3x2d set(Matrix2fc m) {
        m00 = m.m00();
        m01 = m.m01();
        m10 = m.m10();
        m11 = m.m11();
        return this;
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix by assuming a third row in
     * both matrices of <code>(0, 0, 1)</code>.
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
    public Matrix3x2d mul(Matrix3x2dc right) {
        return mul(right, this);
    }

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix by assuming a third row in
     * both matrices of <code>(0, 0, 1)</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     * 
     * @param right
     *          the right operand of the matrix multiplication
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2d mul(Matrix3x2dc right, Matrix3x2d dest) {
        double nm00 = m00 * right.m00() + m10 * right.m01();
        double nm01 = m01 * right.m00() + m11 * right.m01();
        double nm10 = m00 * right.m10() + m10 * right.m11();
        double nm11 = m01 * right.m10() + m11 * right.m11();
        double nm20 = m00 * right.m20() + m10 * right.m21() + m20;
        double nm21 = m01 * right.m20() + m11 * right.m21() + m21;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = nm20;
        dest.m21 = nm21;
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
    public Matrix3x2d mulLocal(Matrix3x2dc left) {
       return mulLocal(left, this);
    }

    public Matrix3x2d mulLocal(Matrix3x2dc left, Matrix3x2d dest) {
        double nm00 = left.m00() * m00 + left.m10() * m01;
        double nm01 = left.m01() * m00 + left.m11() * m01;
        double nm10 = left.m00() * m10 + left.m10() * m11;
        double nm11 = left.m01() * m10 + left.m11() * m11;
        double nm20 = left.m00() * m20 + left.m10() * m21 + left.m20();
        double nm21 = left.m01() * m20 + left.m11() * m21 + left.m21();
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = nm20;
        dest.m21 = nm21;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied double values. The result looks like this:
     * <p>
     * m00, m10, m20<br>
     * m01, m11, m21<br>
     * 
     * @param m00
     *          the new value of m00
     * @param m01
     *          the new value of m01
     * @param m10
     *          the new value of m10
     * @param m11
     *          the new value of m11
     * @param m20
     *          the new value of m20
     * @param m21
     *          the new value of m21
     * @return this
     */
    public Matrix3x2d set(double m00, double m01, 
                          double m10, double m11, 
                          double m20, double m21) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
        return this;
    }

    /**
     * Set the values in this matrix based on the supplied double array. The result looks like this:
     * <p>
     * 0, 2, 4<br>
     * 1, 3, 5<br>
     * 
     * This method only uses the first 6 values, all others are ignored.
     * 
     * @param m
     *          the array to read the matrix values from
     * @return this
     */
    public Matrix3x2d set(double m[]) {
        MemUtil.INSTANCE.copy(m, 0, this);
        return this;
    }

    /**
     * Return the determinant of this matrix.
     * 
     * @return the determinant
     */
    public double determinant() {
        return m00 * m11 - m01 * m10;
    }

    /**
     * Invert this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>.
     *
     * @return this
     */
    public Matrix3x2d invert() {
        return invert(this);
    }

    /**
     * Invert the <code>this</code> matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in <code>dest</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    public Matrix3x2d invert(Matrix3x2d dest) {
        // client must make sure that matrix is invertible
        double s = 1.0 / (m00 * m11 - m01 * m10);
        double nm00 =  m11 * s;
        double nm01 = -m01 * s;
        double nm10 = -m10 * s;
        double nm11 =  m00 * s;
        double nm20 = (m10 * m21 - m20 * m11) * s;
        double nm21 = (m20 * m01 - m00 * m21) * s;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = nm20;
        dest.m21 = nm21;
        return dest;
    }

    /**
     * Set this matrix to be a simple translation matrix in a two-dimensional coordinate system.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional translation.
     * <p>
     * In order to apply a translation via to an already existing transformation
     * matrix, use {@link #translate(double, double) translate()} instead.
     * 
     * @see #translate(double, double)
     * 
     * @param x
     *          the units to translate in x
     * @param y
     *          the units to translate in y
     * @return this
     */
    public Matrix3x2d translation(double x, double y) {
        m00 = 1.0;
        m01 = 0.0;
        m10 = 0.0;
        m11 = 1.0;
        m20 = x;
        m21 = y;
        return this;
    }

    /**
     * Set this matrix to be a simple translation matrix in a two-dimensional coordinate system.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional translation.
     * <p>
     * In order to apply a translation via to an already existing transformation
     * matrix, use {@link #translate(Vector2dc) translate()} instead.
     * 
     * @see #translate(Vector2dc)
     * 
     * @param offset
     *          the translation
     * @return this
     */
    public Matrix3x2d translation(Vector2dc offset) {
        return translation(offset.x(), offset.y());
    }

    /**
     * Set only the translation components of this matrix <code>(m20, m21)</code> to the given values <code>(x, y)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(double, double)}.
     * To apply a translation to another matrix, use {@link #translate(double, double)}.
     * 
     * @see #translation(double, double)
     * @see #translate(double, double)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2d setTranslation(double x, double y) {
        m20 = x;
        m21 = y;
        return this;
    }

    /**
     * Set only the translation components of this matrix <code>(m20, m21)</code> to the given values <code>(offset.x, offset.y)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(Vector2dc)}.
     * To apply a translation to another matrix, use {@link #translate(Vector2dc)}.
     * 
     * @see #translation(Vector2dc)
     * @see #translate(Vector2dc)
     * 
     * @param offset
     *          the new translation to set
     * @return this
     */
    public Matrix3x2d setTranslation(Vector2dc offset) {
        return setTranslation(offset.x(), offset.y());
    }

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y and store the result
     * in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(double, double)}.
     * 
     * @see #translation(double, double)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2d translate(double x, double y, Matrix3x2d dest) {
        double rm20 = x;
        double rm21 = y;
        dest.m20 = m00 * rm20 + m10 * rm21 + m20;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21;
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m10 = m10;
        dest.m11 = m11;
        return dest;
    }

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(double, double)}.
     * 
     * @see #translation(double, double)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2d translate(double x, double y) {
        return translate(x, y, this);
    }

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y, and
     * store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(Vector2dc)}.
     * 
     * @see #translation(Vector2dc)
     * 
     * @param offset
     *          the offset to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2d translate(Vector2dc offset, Matrix3x2d dest) {
        return translate(offset.x(), offset.y(), dest);
    }

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * <p>
     * In order to set the matrix to a translation transformation without post-multiplying
     * it, use {@link #translation(Vector2dc)}.
     * 
     * @see #translation(Vector2dc)
     * 
     * @param offset
     *          the offset to translate
     * @return this
     */
    public Matrix3x2d translate(Vector2dc offset) {
        return translate(offset.x(), offset.y(), this);
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(Vector2dc)}.
     * 
     * @see #translation(Vector2dc)
     * 
     * @param offset
     *          the number of units in x and y by which to translate
     * @return this
     */
    public Matrix3x2d translateLocal(Vector2dc offset) {
        return translateLocal(offset.x(), offset.y());
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(Vector2dc)}.
     * 
     * @see #translation(Vector2dc)
     * 
     * @param offset
     *          the number of units in x and y by which to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2d translateLocal(Vector2dc offset, Matrix3x2d dest) {
        return translateLocal(offset.x(), offset.y(), dest);
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(double, double)}.
     * 
     * @see #translation(double, double)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2d translateLocal(double x, double y, Matrix3x2d dest) {
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m20 = m20 + x;
        dest.m21 = m21 + y;
        return dest;
    }

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * <p>
     * In order to set the matrix to a translation transformation without pre-multiplying
     * it, use {@link #translation(double, double)}.
     * 
     * @see #translation(double, double)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2d translateLocal(double x, double y) {
        return translateLocal(x, y, this);
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
             + Runtime.format(m01, formatter) + " " + Runtime.format(m11, formatter) + " " + Runtime.format(m21, formatter) + "\n";
    }

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * <p>
     * This is the reverse method of {@link #set(Matrix3x2dc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix3x2dc)
     * 
     * @param dest
     *          the destination matrix
     * @return dest
     */
    public Matrix3x2d get(Matrix3x2d dest) {
        return dest.set(this);
    }


    /**
     * Store this matrix in column-major order into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the matrix is stored, use {@link #get(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get(int, DoubleBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public DoubleBuffer get(DoubleBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    /**
     * Store this matrix in column-major order into the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * 
     * @param index
     *            the absolute position into the DoubleBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public DoubleBuffer get(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    /**
     * Store this matrix in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #get(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get(int, ByteBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public ByteBuffer get(ByteBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    /**
     * Store this matrix in column-major order into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * 
     * @param index
     *            the absolute position into the ByteBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public ByteBuffer get(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the matrix is stored, use {@link #get3x3(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get3x3(int, DoubleBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public DoubleBuffer get3x3(DoubleBuffer buffer) {
        MemUtil.INSTANCE.put3x3(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * 
     * @param index
     *            the absolute position into the DoubleBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public DoubleBuffer get3x3(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.put3x3(this, index, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #get3x3(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get3x3(int, ByteBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public ByteBuffer get3x3(ByteBuffer buffer) {
        MemUtil.INSTANCE.put3x3(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * 
     * @param index
     *            the absolute position into the ByteBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public ByteBuffer get3x3(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put3x3(this, index, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the matrix is stored, use {@link #get4x4(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get4x4(int, DoubleBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public DoubleBuffer get4x4(DoubleBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * 
     * @param index
     *            the absolute position into the DoubleBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public DoubleBuffer get4x4(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, index, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #get4x4(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get4x4(int, ByteBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public ByteBuffer get4x4(ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * 
     * @param index
     *            the absolute position into the ByteBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public ByteBuffer get4x4(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, index, buffer);
        return buffer;
    }


    /**
     * Store this matrix into the supplied double array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    public double[] get(double[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix into the supplied double array in column-major order.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get(double[], int)}.
     * 
     * @see #get(double[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public double[] get(double[] arr) {
        return get(arr, 0);
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied float array at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    public double[] get3x3(double[] arr, int offset) {
        MemUtil.INSTANCE.copy3x3(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied float array.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get3x3(double[], int)}.
     * 
     * @see #get3x3(double[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public double[] get3x3(double[] arr) {
        return get3x3(arr, 0);
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied float array at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    public double[] get4x4(double[] arr, int offset) {
        MemUtil.INSTANCE.copy4x4(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied float array.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get4x4(double[], int)}.
     * 
     * @see #get4x4(double[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public double[] get4x4(double[] arr) {
        return get4x4(arr, 0);
    }


    /**
     * Set the values of this matrix by reading 6 double values from the given {@link DoubleBuffer} in column-major order,
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
    public Matrix3x2d set(DoubleBuffer buffer) {
        int pos = buffer.position();
        MemUtil.INSTANCE.get(this, pos, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 double values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix3x2d set(ByteBuffer buffer) {
        int pos = buffer.position();
        MemUtil.INSTANCE.get(this, pos, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 double values from the given {@link DoubleBuffer} in column-major order,
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
    public Matrix3x2d set(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 double values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix3x2d set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set all values within this matrix to zero.
     * 
     * @return this
     */
    public Matrix3x2d zero() {
        MemUtil.INSTANCE.zero(this);
        return this;
    }

    /**
     * Set this matrix to the identity.
     * 
     * @return this
     */
    public Matrix3x2d identity() {
        MemUtil.INSTANCE.identity(this);
        return this;
    }

    /**
     * Apply scaling to this matrix by scaling the unit axes by the given x and y and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d scale(double x, double y, Matrix3x2d dest) {
        dest.m00 = m00 * x;
        dest.m01 = m01 * x;
        dest.m10 = m10 * y;
        dest.m11 = m11 * y;
        dest.m20 = m20;
        dest.m21 = m21;
        return dest;
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x and y factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @return this
     */
    public Matrix3x2d scale(double x, double y) {
        return scale(x, y, this);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xy</code> factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param xy
     *            the factors of the x and y component, respectively
     * @return this
     */
    public Matrix3x2d scale(Vector2dc xy) {
        return scale(xy.x(), xy.y(), this);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xy</code> factors
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param xy
     *            the factors of the x and y component, respectively
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d scale(Vector2dc xy, Matrix3x2d dest) {
        return scale(xy.x(), xy.y(), dest);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xy</code> factors.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param xy
     *            the factors of the x and y component, respectively
     * @return this
     */
    public Matrix3x2d scale(Vector2fc xy) {
        return scale(xy.x(), xy.y(), this);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>xy</code> factors
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @param xy
     *            the factors of the x and y component, respectively
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d scale(Vector2fc xy, Matrix3x2d dest) {
        return scale(xy.x(), xy.y(), dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling the two base axes by the given <code>xy</code> factor
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @see #scale(double, double, Matrix3x2d)
     * 
     * @param xy
     *            the factor for the two components
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d scale(double xy, Matrix3x2d dest) {
        return scale(xy, xy, dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling the two base axes by the given <code>xyz</code> factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @see #scale(double, double)
     * 
     * @param xy
     *            the factor for the two components
     * @return this
     */
    public Matrix3x2d scale(double xy) {
        return scale(xy, xy);
    }

    public Matrix3x2d scaleLocal(double x, double y, Matrix3x2d dest) {
        dest.m00 = x * m00;
        dest.m01 = y * m01;
        dest.m10 = x * m10;
        dest.m11 = y * m11;
        dest.m20 = x * m20;
        dest.m21 = y * m21;
        return dest;
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given x and y factors.
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
    public Matrix3x2d scaleLocal(double x, double y) {
        return scaleLocal(x, y, this);
    }

    public Matrix3x2d scaleLocal(double xy, Matrix3x2d dest) {
        return scaleLocal(xy, xy, dest);
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given xy factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * 
     * @param xy
     *            the factor of the x and y component
     * @return this
     */
    public Matrix3x2d scaleLocal(double xy) {
        return scaleLocal(xy, xy, this);
    }

    /**
     * Apply scaling to <code>this</code> matrix by scaling the base axes by the given sx and
     * sy factors while using <code>(ox, oy)</code> as the scaling origin, and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy, dest).scale(sx, sy).translate(-ox, -oy)</code>
     * 
     * @param sx
     *            the scaling factor of the x component
     * @param sy
     *            the scaling factor of the y component
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d scaleAround(double sx, double sy, double ox, double oy, Matrix3x2d dest) {
        double nm20 = m00 * ox + m10 * oy + m20;
        double nm21 = m01 * ox + m11 * oy + m21;
        dest.m00 = m00 * sx;
        dest.m01 = m01 * sx;
        dest.m10 = m10 * sy;
        dest.m11 = m11 * sy;
        dest.m20 = dest.m00 * -ox + dest.m10 * -oy + nm20;
        dest.m21 = dest.m01 * -ox + dest.m11 * -oy + nm21;
        return dest;
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given sx and
     * sy factors while using <code>(ox, oy)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy).scale(sx, sy).translate(-ox, -oy)</code>
     * 
     * @param sx
     *            the scaling factor of the x component
     * @param sy
     *            the scaling factor of the y component
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @return this
     */
    public Matrix3x2d scaleAround(double sx, double sy, double ox, double oy) {
        return scaleAround(sx, sy, ox, oy, this);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>factor</code>
     * while using <code>(ox, oy)</code> as the scaling origin,
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy, dest).scale(factor).translate(-ox, -oy)</code>
     * 
     * @param factor
     *            the scaling factor for all three axes
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @param dest
     *            will hold the result
     * @return this
     */
    public Matrix3x2d scaleAround(double factor, double ox, double oy, Matrix3x2d dest) {
        return scaleAround(factor, factor, ox, oy, this);
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given <code>factor</code>
     * while using <code>(ox, oy)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the
     * scaling will be applied first!
     * <p>
     * This method is equivalent to calling: <code>translate(ox, oy).scale(factor).translate(-ox, -oy)</code>
     * 
     * @param factor
     *            the scaling factor for all axes
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @return this
     */
    public Matrix3x2d scaleAround(double factor, double ox, double oy) {
        return scaleAround(factor, factor, ox, oy, this);
    }

    public Matrix3x2d scaleAroundLocal(double sx, double sy, double ox, double oy, Matrix3x2d dest) {
        dest.m00 = sx * m00;
        dest.m01 = sy * m01;
        dest.m10 = sx * m10;
        dest.m11 = sy * m11;
        dest.m20 = sx * m20 - sx * ox + ox;
        dest.m21 = sy * m21 - sy * oy + oy;
        return dest;
    }

    public Matrix3x2d scaleAroundLocal(double factor, double ox, double oy, Matrix3x2d dest) {
        return scaleAroundLocal(factor, factor, ox, oy, dest);
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given sx and
     * sy factors while using <code>(ox, oy)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix3x2d().translate(ox, oy).scale(sx, sy).translate(-ox, -oy).mul(this, this)</code>
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
    public Matrix3x2d scaleAroundLocal(double sx, double sy, double sz, double ox, double oy, double oz) {
        return scaleAroundLocal(sx, sy, ox, oy, this);
    }

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given <code>factor</code>
     * while using <code>(ox, oy)</code> as the scaling origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix3x2d().translate(ox, oy).scale(factor).translate(-ox, -oy).mul(this, this)</code>
     * 
     * @param factor
     *            the scaling factor for all three axes
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @return this
     */
    public Matrix3x2d scaleAroundLocal(double factor, double ox, double oy) {
        return scaleAroundLocal(factor, factor, ox, oy, this);
    }

    /**
     * Set this matrix to be a simple scale matrix, which scales the two base axes uniformly by the given factor.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a matrix, use {@link #scale(double) scale()} instead.
     * 
     * @see #scale(double)
     * 
     * @param factor
     *             the scale factor in x and y
     * @return this
     */
    public Matrix3x2d scaling(double factor) {
        return scaling(factor, factor);
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
    public Matrix3x2d scaling(double x, double y) {
        m00 = x;
        m01 = 0.0;
        m10 = 0.0;
        m11 = y;
        m20 = 0.0;
        m21 = 0.0;
        return this;
    }

    /**
     * Set this matrix to a rotation matrix which rotates the given radians.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to apply the rotation transformation to an existing transformation,
     * use {@link #rotate(double) rotate()} instead.
     * 
     * @see #rotate(double)
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Matrix3x2d rotation(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        m00 = cos;
        m10 = -sin;
        m20 = 0.0;
        m01 = sin;
        m11 = cos;
        m21 = 0.0;
        return this;
    }

    /**
     * Transform/multiply the given vector by this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in that vector.
     * 
     * @see Vector3d#mul(Matrix3x2dc)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector3d transform(Vector3d v) {
        return v.mul(this);
    }

    /**
     * Transform/multiply the given vector by this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in <code>dest</code>.
     * 
     * @see Vector3d#mul(Matrix3x2dc, Vector3d)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will contain the result
     * @return dest
     */
    public Vector3d transform(Vector3dc v, Vector3d dest) {
        return v.mul(this, dest);
    }

    /**
     * Transform/multiply the given vector <code>(x, y, z)</code> by this matrix and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component of the vector to transform
     * @param y
     *          the y component of the vector to transform
     * @param z
     *          the z component of the vector to transform
     * @param dest
     *          will contain the result
     * @return dest
     */
    public Vector3d transform(double x, double y, double z, Vector3d dest) {
       return dest.set(m00 * x + m10 * y + m20 * z, m01 * x + m11 * y + m21 * z, z);
    }

    /**
     * Transform/multiply the given 2D-vector, as if it was a 3D-vector with z=1, by
     * this matrix and store the result in that vector.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being 1.0, so it
     * will represent a position/location in 2D-space rather than a direction.
     * <p>
     * In order to store the result in another vector, use {@link #transformPosition(Vector2dc, Vector2d)}.
     * 
     * @see #transformPosition(Vector2dc, Vector2d)
     * @see #transform(Vector3d)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector2d transformPosition(Vector2d v) {
        v.set(m00 * v.x + m10 * v.y + m20,
              m01 * v.x + m11 * v.y + m21);
        return v;
    }

    /**
     * Transform/multiply the given 2D-vector, as if it was a 3D-vector with z=1, by
     * this matrix and store the result in <code>dest</code>.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being 1.0, so it
     * will represent a position/location in 2D-space rather than a direction.
     * <p>
     * In order to store the result in the same vector, use {@link #transformPosition(Vector2d)}.
     * 
     * @see #transformPosition(Vector2d)
     * @see #transform(Vector3dc, Vector3d)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2d transformPosition(Vector2dc v, Vector2d dest) {
        dest.set(m00 * v.x() + m10 * v.y() + m20,
                 m01 * v.x() + m11 * v.y() + m21);
        return dest;
    }

    /**
     * Transform/multiply the given 2D-vector <code>(x, y)</code>, as if it was a 3D-vector with z=1, by
     * this matrix and store the result in <code>dest</code>.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being 1.0, so it
     * will represent a position/location in 2D-space rather than a direction.
     * <p>
     * In order to store the result in the same vector, use {@link #transformPosition(Vector2d)}.
     * 
     * @see #transformPosition(Vector2d)
     * @see #transform(Vector3dc, Vector3d)
     * 
     * @param x
     *          the x component of the vector to transform
     * @param y
     *          the y component of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2d transformPosition(double x, double y, Vector2d dest) {
        return dest.set(m00 * x + m10 * y + m20, m01 * x + m11 * y + m21);
    }

    /**
     * Transform/multiply the given 2D-vector, as if it was a 3D-vector with z=0, by
     * this matrix and store the result in that vector.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being <code>0.0</code>, so it
     * will represent a direction in 2D-space rather than a position. This method will therefore
     * not take the translation part of the matrix into account.
     * <p>
     * In order to store the result in another vector, use {@link #transformDirection(Vector2dc, Vector2d)}.
     * 
     * @see #transformDirection(Vector2dc, Vector2d)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector2d transformDirection(Vector2d v) {
        v.set(m00 * v.x + m10 * v.y,
              m01 * v.x + m11 * v.y);
        return v;
    }

    /**
     * Transform/multiply the given 2D-vector, as if it was a 3D-vector with z=0, by
     * this matrix and store the result in <code>dest</code>.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being <code>0.0</code>, so it
     * will represent a direction in 2D-space rather than a position. This method will therefore
     * not take the translation part of the matrix into account.
     * <p>
     * In order to store the result in the same vector, use {@link #transformDirection(Vector2d)}.
     * 
     * @see #transformDirection(Vector2d)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2d transformDirection(Vector2dc v, Vector2d dest) {
        dest.set(m00 * v.x() + m10 * v.y(),
                 m01 * v.x() + m11 * v.y());
        return dest;
    }

    /**
     * Transform/multiply the given 2D-vector <code>(x, y)</code>, as if it was a 3D-vector with z=0, by
     * this matrix and store the result in <code>dest</code>.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being <code>0.0</code>, so it
     * will represent a direction in 2D-space rather than a position. This method will therefore
     * not take the translation part of the matrix into account.
     * <p>
     * In order to store the result in the same vector, use {@link #transformDirection(Vector2d)}.
     * 
     * @see #transformDirection(Vector2d)
     * 
     * @param x
     *          the x component of the vector to transform
     * @param y
     *          the y component of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2d transformDirection(double x, double y, Vector2d dest) {
        return dest.set(m00 * x + m10 * y, m01 * x + m11 * y);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(m00);
        out.writeDouble(m01);
        out.writeDouble(m10);
        out.writeDouble(m11);
        out.writeDouble(m20);
        out.writeDouble(m21);
    }

    public void readExternal(ObjectInput in) throws IOException {
        m00 = in.readDouble();
        m01 = in.readDouble();
        m10 = in.readDouble();
        m11 = in.readDouble();
        m20 = in.readDouble();
        m21 = in.readDouble();
    }

    /**
     * Apply a rotation transformation to this matrix by rotating the given amount of radians.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>
     * , the rotation will be applied first!
     * 
     * @param ang
     *            the angle in radians
     * @return this
     */
    public Matrix3x2d rotate(double ang) {
        return rotate(ang, this);
    }

    /**
     * Apply a rotation transformation to this matrix by rotating the given amount of radians and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the rotation will be applied first!
     * 
     * @param ang
     *            the angle in radians
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d rotate(double ang, Matrix3x2d dest) {
        double cos = Math.cos(ang);
        double sin = Math.sin(ang);
        double rm00 = cos;
        double rm01 = sin;
        double rm10 = -sin;
        double rm11 = cos;
        double nm00 = m00 * rm00 + m10 * rm01;
        double nm01 = m01 * rm00 + m11 * rm01;
        dest.m10 = m00 * rm10 + m10 * rm11;
        dest.m11 = m01 * rm10 + m11 * rm11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m20 = m20;
        dest.m21 = m21;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotation(double) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(double)
     * 
     * @param ang
     *            the angle in radians to rotate
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d rotateLocal(double ang, Matrix3x2d dest) {
        double sin = Math.sin(ang);
        double cos = Math.cosFromSin(sin, ang);
        double nm00 = cos * m00 - sin * m01;
        double nm01 = sin * m00 + cos * m01;
        double nm10 = cos * m10 - sin * m11;
        double nm11 = sin * m10 + cos * m11;
        double nm20 = cos * m20 - sin * m21;
        double nm21 = sin * m20 + cos * m21;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = nm20;
        dest.m21 = nm21;
        return dest;
    }

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * In order to set the matrix to a rotation matrix without pre-multiplying the rotation
     * transformation, use {@link #rotation(double) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(double)
     * 
     * @param ang
     *            the angle in radians to rotate
     * @return this
     */
    public Matrix3x2d rotateLocal(double ang) {
        return rotateLocal(ang, this);
    }

    /**
     * Apply a rotation transformation to this matrix by rotating the given amount of radians about
     * the specified rotation center <code>(x, y)</code>.
     * <p>
     * This method is equivalent to calling: <code>translate(x, y).rotate(ang).translate(-x, -y)</code>
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the rotation will be applied first!
     * 
     * @see #translate(double, double)
     * @see #rotate(double)
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the rotation center
     * @param y
     *            the y component of the rotation center
     * @return this
     */
    public Matrix3x2d rotateAbout(double ang, double x, double y) {
        return rotateAbout(ang, x, y, this);
    }

    /**
     * Apply a rotation transformation to this matrix by rotating the given amount of radians about
     * the specified rotation center <code>(x, y)</code> and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling: <code>translate(x, y, dest).rotate(ang).translate(-x, -y)</code>
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the rotation will be applied first!
     * 
     * @see #translate(double, double, Matrix3x2d)
     * @see #rotate(double, Matrix3x2d)
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the rotation center
     * @param y
     *            the y component of the rotation center
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d rotateAbout(double ang, double x, double y, Matrix3x2d dest) {
        double tm20 = m00 * x + m10 * y + m20;
        double tm21 = m01 * x + m11 * y + m21;
        double cos = Math.cos(ang);
        double sin = Math.sin(ang);
        double nm00 = m00 * cos + m10 * sin;
        double nm01 = m01 * cos + m11 * sin;
        dest.m10 = m00 * -sin + m10 * cos;
        dest.m11 = m01 * -sin + m11 * cos;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m20 = dest.m00 * -x + dest.m10 * -y + tm20;
        dest.m21 = dest.m01 * -x + dest.m11 * -y + tm21;
        return dest;
    }

    /**
     * Apply a rotation transformation to this matrix that rotates the given normalized <code>fromDir</code> direction vector
     * to point along the normalized <code>toDir</code>, and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the rotation will be applied first!
     * 
     * @param fromDir
     *            the normalized direction which should be rotate to point along <code>toDir</code>
     * @param toDir
     *            the normalized destination direction
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d rotateTo(Vector2dc fromDir, Vector2dc toDir, Matrix3x2d dest) {
        double dot = fromDir.x() * toDir.x() + fromDir.y() * toDir.y();
        double det = fromDir.x() * toDir.y() - fromDir.y() * toDir.x();
        double rm00 = dot;
        double rm01 = det;
        double rm10 = -det;
        double rm11 = dot;
        double nm00 = m00 * rm00 + m10 * rm01;
        double nm01 = m01 * rm00 + m11 * rm01;
        dest.m10 = m00 * rm10 + m10 * rm11;
        dest.m11 = m01 * rm10 + m11 * rm11;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m20 = m20;
        dest.m21 = m21;
        return dest;
    }

    /**
     * Apply a rotation transformation to this matrix that rotates the given normalized <code>fromDir</code> direction vector
     * to point along the normalized <code>toDir</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the rotation will be applied first!
     * 
     * @param fromDir
     *            the normalized direction which should be rotate to point along <code>toDir</code>
     * @param toDir
     *            the normalized destination direction
     * @return this
     */
    public Matrix3x2d rotateTo(Vector2dc fromDir, Vector2dc toDir) {
        return rotateTo(fromDir, toDir, this);
    }

    /**
     * Apply a "view" transformation to this matrix that maps the given <code>(left, bottom)</code> and
     * <code>(right, top)</code> corners to <code>(-1, -1)</code> and <code>(1, 1)</code> respectively and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * 
     * @see #setView(double, double, double, double)
     * 
     * @param left
     *            the distance from the center to the left view edge
     * @param right
     *            the distance from the center to the right view edge
     * @param bottom
     *            the distance from the center to the bottom view edge
     * @param top
     *            the distance from the center to the top view edge
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2d view(double left, double right, double bottom, double top, Matrix3x2d dest) {
        double rm00 = 2.0 / (right - left);
        double rm11 = 2.0 / (top - bottom);
        double rm20 = (left + right) / (left - right);
        double rm21 = (bottom + top) / (bottom - top);
        dest.m20 = m00 * rm20 + m10 * rm21 + m20;
        dest.m21 = m01 * rm20 + m11 * rm21 + m21;
        dest.m00 = m00 * rm00;
        dest.m01 = m01 * rm00;
        dest.m10 = m10 * rm11;
        dest.m11 = m11 * rm11;
        return dest;
    }

    /**
     * Apply a "view" transformation to this matrix that maps the given <code>(left, bottom)</code> and
     * <code>(right, top)</code> corners to <code>(-1, -1)</code> and <code>(1, 1)</code> respectively.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
     * 
     * @see #setView(double, double, double, double)
     * 
     * @param left
     *            the distance from the center to the left view edge
     * @param right
     *            the distance from the center to the right view edge
     * @param bottom
     *            the distance from the center to the bottom view edge
     * @param top
     *            the distance from the center to the top view edge
     * @return this
     */
    public Matrix3x2d view(double left, double right, double bottom, double top) {
        return view(left, right, bottom, top, this);
    }

    /**
     * Set this matrix to define a "view" transformation that maps the given <code>(left, bottom)</code> and
     * <code>(right, top)</code> corners to <code>(-1, -1)</code> and <code>(1, 1)</code> respectively.
     * 
     * @see #view(double, double, double, double)
     * 
     * @param left
     *            the distance from the center to the left view edge
     * @param right
     *            the distance from the center to the right view edge
     * @param bottom
     *            the distance from the center to the bottom view edge
     * @param top
     *            the distance from the center to the top view edge
     * @return this
     */
    public Matrix3x2d setView(double left, double right, double bottom, double top) {
        m00 = 2.0 / (right - left);
        m01 = 0.0;
        m10 = 0.0;
        m11 = 2.0 / (top - bottom);
        m20 = (left + right) / (left - right);
        m21 = (bottom + top) / (bottom - top);
        return this;
    }

    /**
     * Obtain the position that gets transformed to the origin by <code>this</code> matrix.
     * This can be used to get the position of the "camera" from a given <i>view</i> transformation matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).invert();
     * inv.transform(origin.set(0, 0));
     * </pre>
     * 
     * @param origin
     *          will hold the position transformed to the origin
     * @return origin
     */
    public Vector2d origin(Vector2d origin) {
        double s = 1.0 / (m00 * m11 - m01 * m10);
        origin.x = (m10 * m21 - m20 * m11) * s;
        origin.y = (m20 * m01 - m00 * m21) * s;
        return origin;
    }

    /**
     * Obtain the extents of the view transformation of <code>this</code> matrix and store it in <code>area</code>.
     * This can be used to determine which region of the screen (i.e. the NDC space) is covered by the view.
     * 
     * @param area
     *          will hold the view area as <code>[minX, minY, maxX, maxY]</code>
     * @return area
     */
    public double[] viewArea(double[] area) {
        double s = 1.0 / (m00 * m11 - m01 * m10);
        double rm00 =  m11 * s;
        double rm01 = -m01 * s;
        double rm10 = -m10 * s;
        double rm11 =  m00 * s;
        double rm20 = (m10 * m21 - m20 * m11) * s;
        double rm21 = (m20 * m01 - m00 * m21) * s;
        double nxnyX = -rm00 - rm10;
        double nxnyY = -rm01 - rm11;
        double pxnyX =  rm00 - rm10;
        double pxnyY =  rm01 - rm11;
        double nxpyX = -rm00 + rm10;
        double nxpyY = -rm01 + rm11;
        double pxpyX =  rm00 + rm10;
        double pxpyY =  rm01 + rm11;
        double minX = nxnyX;
        minX = minX < nxpyX ? minX : nxpyX;
        minX = minX < pxnyX ? minX : pxnyX;
        minX = minX < pxpyX ? minX : pxpyX;
        double minY = nxnyY;
        minY = minY < nxpyY ? minY : nxpyY;
        minY = minY < pxnyY ? minY : pxnyY;
        minY = minY < pxpyY ? minY : pxpyY;
        double maxX = nxnyX;
        maxX = maxX > nxpyX ? maxX : nxpyX;
        maxX = maxX > pxnyX ? maxX : pxnyX;
        maxX = maxX > pxpyX ? maxX : pxpyX;
        double maxY = nxnyY;
        maxY = maxY > nxpyY ? maxY : nxpyY;
        maxY = maxY > pxnyY ? maxY : pxnyY;
        maxY = maxY > pxpyY ? maxY : pxpyY;
        area[0] = minX + rm20;
        area[1] = minY + rm21;
        area[2] = maxX + rm20;
        area[3] = maxY + rm21;
        return area;
    }

    public Vector2d positiveX(Vector2d dir) {
        double s = m00 * m11 - m01 * m10;
        s = 1.0 / s;
        dir.x =  m11 * s;
        dir.y = -m01 * s;
        return dir.normalize(dir);
    }

    public Vector2d normalizedPositiveX(Vector2d dir) {
        dir.x =  m11;
        dir.y = -m01;
        return dir;
    }

    public Vector2d positiveY(Vector2d dir) {
        double s = m00 * m11 - m01 * m10;
        s = 1.0 / s;
        dir.x = -m10 * s;
        dir.y =  m00 * s;
        return dir.normalize(dir);
    }

    public Vector2d normalizedPositiveY(Vector2d dir) {
        dir.x = -m10;
        dir.y =  m00;
        return dir;
    }

    /**
     * Unproject the given window coordinates <code>(winX, winY)</code> by <code>this</code> matrix using the specified viewport.
     * <p>
     * This method first converts the given window coordinates to normalized device coordinates in the range <code>[-1..1]</code>
     * and then transforms those NDC coordinates by the inverse of <code>this</code> matrix.  
     * <p>
     * As a necessary computation step for unprojecting, this method computes the inverse of <code>this</code> matrix.
     * In order to avoid computing the matrix inverse with every invocation, the inverse of <code>this</code> matrix can be built
     * once outside using {@link #invert(Matrix3x2d)} and then the method {@link #unprojectInv(double, double, int[], Vector2d) unprojectInv()} can be invoked on it.
     * 
     * @see #unprojectInv(double, double, int[], Vector2d)
     * @see #invert(Matrix3x2d)
     * 
     * @param winX
     *          the x-coordinate in window coordinates (pixels)
     * @param winY
     *          the y-coordinate in window coordinates (pixels)
     * @param viewport
     *          the viewport described by <code>[x, y, width, height]</code>
     * @param dest
     *          will hold the unprojected position
     * @return dest
     */
    public Vector2d unproject(double winX, double winY, int[] viewport, Vector2d dest) {
        double s = 1.0 / (m00 * m11 - m01 * m10);
        double im00 =  m11 * s;
        double im01 = -m01 * s;
        double im10 = -m10 * s;
        double im11 =  m00 * s;
        double im20 = (m10 * m21 - m20 * m11) * s;
        double im21 = (m20 * m01 - m00 * m21) * s;
        double ndcX = (winX-viewport[0])/viewport[2]*2.0-1.0;
        double ndcY = (winY-viewport[1])/viewport[3]*2.0-1.0;
        dest.x = im00 * ndcX + im10 * ndcY + im20;
        dest.y = im01 * ndcX + im11 * ndcY + im21;
        return dest;
    }

    /**
     * Unproject the given window coordinates <code>(winX, winY)</code> by <code>this</code> matrix using the specified viewport.
     * <p>
     * This method differs from {@link #unproject(double, double, int[], Vector2d) unproject()} 
     * in that it assumes that <code>this</code> is already the inverse matrix of the original projection matrix.
     * It exists to avoid recomputing the matrix inverse with every invocation.
     * 
     * @see #unproject(double, double, int[], Vector2d)
     * 
     * @param winX
     *          the x-coordinate in window coordinates (pixels)
     * @param winY
     *          the y-coordinate in window coordinates (pixels)
     * @param viewport
     *          the viewport described by <code>[x, y, width, height]</code>
     * @param dest
     *          will hold the unprojected position
     * @return dest
     */
    public Vector2d unprojectInv(double winX, double winY, int[] viewport, Vector2d dest) {
        double ndcX = (winX-viewport[0])/viewport[2]*2.0-1.0;
        double ndcY = (winY-viewport[1])/viewport[3]*2.0-1.0;
        dest.x = m00 * ndcX + m10 * ndcY + m20;
        dest.y = m01 * ndcX + m11 * ndcY + m21;
        return dest;
    }

    /**
     * Compute the extents of the coordinate system before this transformation was applied and store the resulting
     * corner coordinates in <code>corner</code> and the span vectors in <code>xDir</code> and <code>yDir</code>.
     * <p>
     * That means, given the maximum extents of the coordinate system between <code>[-1..+1]</code> in all dimensions,
     * this method returns one corner and the length and direction of the two base axis vectors in the coordinate
     * system before this transformation is applied, which transforms into the corner coordinates <code>[-1, +1]</code>.
     * 
     * @param corner
     *          will hold one corner of the span
     * @param xDir
     *          will hold the direction and length of the span along the positive X axis
     * @param yDir
     *          will hold the direction and length of the span along the positive Y axis
     * @return this
     */
    public Matrix3x2d span(Vector2d corner, Vector2d xDir, Vector2d yDir) {
        double s = 1.0 / (m00 * m11 - m01 * m10);
        double nm00 =  m11 * s, nm01 = -m01 * s, nm10 = -m10 * s, nm11 =  m00 * s;
        corner.x = -nm00 - nm10 + (m10 * m21 - m20 * m11) * s;
        corner.y = -nm01 - nm11 + (m20 * m01 - m00 * m21) * s;
        xDir.x = 2.0 * nm00; xDir.y = 2.0 * nm01;
        yDir.x = 2.0 * nm10; yDir.y = 2.0 * nm11;
        return this;
    }

    public boolean testPoint(double x, double y) {
        double nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        double pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        double nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        double pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
        return nxX * x + nxY * y + nxW >= 0 && pxX * x + pxY * y + pxW >= 0 &&
               nyX * x + nyY * y + nyW >= 0 && pyX * x + pyY * y + pyW >= 0;
    }

    public boolean testCircle(double x, double y, double r) {
        double invl;
        double nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        invl = Math.invsqrt(nxX * nxX + nxY * nxY);
        nxX *= invl; nxY *= invl; nxW *= invl;
        double pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        invl = Math.invsqrt(pxX * pxX + pxY * pxY);
        pxX *= invl; pxY *= invl; pxW *= invl;
        double nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        invl = Math.invsqrt(nyX * nyX + nyY * nyY);
        nyX *= invl; nyY *= invl; nyW *= invl;
        double pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
        invl = Math.invsqrt(pyX * pyX + pyY * pyY);
        pyX *= invl; pyY *= invl; pyW *= invl;
        return nxX * x + nxY * y + nxW >= -r && pxX * x + pxY * y + pxW >= -r &&
               nyX * x + nyY * y + nyW >= -r && pyX * x + pyY * y + pyW >= -r;
    }

    public boolean testAar(double minX, double minY, double maxX, double maxY) {
        double nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        double pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        double nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        double pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * It does not distinguish between partially inside and fully inside, though, so the test with the 'p' vertex is omitted.
         */
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) >= -nxW &&
               pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) >= -pxW &&
               nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) >= -nyW &&
               pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) >= -pyW;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(m00);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m01);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m10);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m11);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m20);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m21);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Matrix3x2d other = (Matrix3x2d) obj;
        if (Double.doubleToLongBits(m00) != Double.doubleToLongBits(other.m00))
            return false;
        if (Double.doubleToLongBits(m01) != Double.doubleToLongBits(other.m01))
            return false;
        if (Double.doubleToLongBits(m10) != Double.doubleToLongBits(other.m10))
            return false;
        if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11))
            return false;
        if (Double.doubleToLongBits(m20) != Double.doubleToLongBits(other.m20))
            return false;
        if (Double.doubleToLongBits(m21) != Double.doubleToLongBits(other.m21))
            return false;
        return true;
    }

    public boolean equals(Matrix3x2dc m, double delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix3x2d))
            return false;
        if (!Runtime.equals(m00, m.m00(), delta))
            return false;
        if (!Runtime.equals(m01, m.m01(), delta))
            return false;
        if (!Runtime.equals(m10, m.m10(), delta))
            return false;
        if (!Runtime.equals(m11, m.m11(), delta))
            return false;
        if (!Runtime.equals(m20, m.m20(), delta))
            return false;
        if (!Runtime.equals(m21, m.m21(), delta))
            return false;
        return true;
    }

    public boolean isFinite() {
        return Math.isFinite(m00) && Math.isFinite(m01) &&
               Math.isFinite(m10) && Math.isFinite(m11) &&
               Math.isFinite(m20) && Math.isFinite(m21);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
