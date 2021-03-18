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
import java.nio.FloatBuffer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a 3x2 matrix of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 * <p>
 *      m00  m10  m20<br>
 *      m01  m11  m21<br>
 * 
 * @author Kai Burjack
 */
public class Matrix3x2f implements Matrix3x2fc, Externalizable, Cloneable {

    private static final long serialVersionUID = 1L;

    public float m00, m01;
    public float m10, m11;
    public float m20, m21;

    /**
     * Create a new {@link Matrix3x2f} and set it to {@link #identity() identity}.
     */
    public Matrix3x2f() {
        this.m00 = 1.0f;
        this.m11 = 1.0f;
    }

    /**
     * Create a new {@link Matrix3x2f} and make it a copy of the given matrix.
     * 
     * @param mat
     *          the {@link Matrix3x2fc} to copy the values from
     */
    public Matrix3x2f(Matrix3x2fc mat) {
        if (mat instanceof Matrix3x2f) {
            MemUtil.INSTANCE.copy((Matrix3x2f) mat, this);
        } else {
            setMatrix3x2fc(mat);
        }
    }

    /**
     * Create a new {@link Matrix3x2f} by setting its left 2x2 submatrix to the values of the given {@link Matrix2fc}
     * and the rest to identity.
     *
     * @param mat
     *          the {@link Matrix2fc}
     */
    public Matrix3x2f(Matrix2fc mat) {
        if (mat instanceof Matrix2f) {
            MemUtil.INSTANCE.copy((Matrix2f) mat, this);
        } else {
            setMatrix2fc(mat);
        }
    }

    /**
     * Create a new 3x2 matrix using the supplied float values. The order of the parameter is column-major, 
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
    public Matrix3x2f(float m00, float m01,
                      float m10, float m11,
                      float m20, float m21) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
    }


    /**
     * Create a new {@link Matrix3x2f} by reading its 6 float components from the given {@link FloatBuffer}
     * at the buffer's current position.
     * <p>
     * That FloatBuffer is expected to hold the values in column-major order.
     * <p>
     * The buffer's position will not be changed by this method.
     * 
     * @param buffer
     *          the {@link FloatBuffer} to read the matrix values from
     */
    public Matrix3x2f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
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
    public float m20() {
        return m20;
    }
    public float m21() {
        return m21;
    }

    /**
     * Set the value of the matrix element at column 0 and row 0.
     * 
     * @param m00
     *          the new value
     * @return this
     */
    Matrix3x2f _m00(float m00) {
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
    Matrix3x2f _m01(float m01) {
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
    Matrix3x2f _m10(float m10) {
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
    Matrix3x2f _m11(float m11) {
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
    Matrix3x2f _m20(float m20) {
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
    Matrix3x2f _m21(float m21) {
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
    public Matrix3x2f set(Matrix3x2fc m) {
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
        m20 = mat.m20();
        m21 = mat.m21();
    }

    /**
     * Set the left 2x2 submatrix of this {@link Matrix3x2f} to the given {@link Matrix2fc} and don't change the other elements.
     *
     * @param m
     *          the 2x2 matrix
     * @return this
     */
    public Matrix3x2f set(Matrix2fc m) {
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
    public Matrix3x2f mul(Matrix3x2fc right) {
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
    public Matrix3x2f mul(Matrix3x2fc right, Matrix3x2f dest) {
        float nm00 = m00 * right.m00() + m10 * right.m01();
        float nm01 = m01 * right.m00() + m11 * right.m01();
        float nm10 = m00 * right.m10() + m10 * right.m11();
        float nm11 = m01 * right.m10() + m11 * right.m11();
        float nm20 = m00 * right.m20() + m10 * right.m21() + m20;
        float nm21 = m01 * right.m20() + m11 * right.m21() + m21;
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
    public Matrix3x2f mulLocal(Matrix3x2fc left) {
       return mulLocal(left, this);
    }

    public Matrix3x2f mulLocal(Matrix3x2fc left, Matrix3x2f dest) {
        float nm00 = left.m00() * m00 + left.m10() * m01;
        float nm01 = left.m01() * m00 + left.m11() * m01;
        float nm10 = left.m00() * m10 + left.m10() * m11;
        float nm11 = left.m01() * m10 + left.m11() * m11;
        float nm20 = left.m00() * m20 + left.m10() * m21 + left.m20();
        float nm21 = left.m01() * m20 + left.m11() * m21 + left.m21();
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = nm20;
        dest.m21 = nm21;
        return dest;
    }

    /**
     * Set the values within this matrix to the supplied float values. The result looks like this:
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
    public Matrix3x2f set(float m00, float m01, 
                          float m10, float m11, 
                          float m20, float m21) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
        return this;
    }

    /**
     * Set the values in this matrix based on the supplied float array. The result looks like this:
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
    public Matrix3x2f set(float m[]) {
        MemUtil.INSTANCE.copy(m, 0, this);
        return this;
    }

    /**
     * Return the determinant of this matrix.
     * 
     * @return the determinant
     */
    public float determinant() {
        return m00 * m11 - m01 * m10;
    }

    /**
     * Invert this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>.
     *
     * @return this
     */
    public Matrix3x2f invert() {
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
    public Matrix3x2f invert(Matrix3x2f dest) {
        // client must make sure that matrix is invertible
        float s = 1.0f / (m00 * m11 - m01 * m10);
        float nm00 =  m11 * s;
        float nm01 = -m01 * s;
        float nm10 = -m10 * s;
        float nm11 =  m00 * s;
        float nm20 = (m10 * m21 - m20 * m11) * s;
        float nm21 = (m20 * m01 - m00 * m21) * s;
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
     * matrix, use {@link #translate(float, float) translate()} instead.
     * 
     * @see #translate(float, float)
     * 
     * @param x
     *          the units to translate in x
     * @param y
     *          the units to translate in y
     * @return this
     */
    public Matrix3x2f translation(float x, float y) {
        m00 = 1.0f;
        m01 = 0.0f;
        m10 = 0.0f;
        m11 = 1.0f;
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
     * matrix, use {@link #translate(Vector2fc) translate()} instead.
     * 
     * @see #translate(Vector2fc)
     * 
     * @param offset
     *          the translation
     * @return this
     */
    public Matrix3x2f translation(Vector2fc offset) {
        return translation(offset.x(), offset.y());
    }

    /**
     * Set only the translation components of this matrix <code>(m20, m21)</code> to the given values <code>(x, y)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(float, float)}.
     * To apply a translation to another matrix, use {@link #translate(float, float)}.
     * 
     * @see #translation(float, float)
     * @see #translate(float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2f setTranslation(float x, float y) {
        m20 = x;
        m21 = y;
        return this;
    }

    /**
     * Set only the translation components of this matrix <code>(m20, m21)</code> to the given values <code>(offset.x, offset.y)</code>.
     * <p>
     * To build a translation matrix instead, use {@link #translation(Vector2fc)}.
     * To apply a translation to another matrix, use {@link #translate(Vector2fc)}.
     * 
     * @see #translation(Vector2fc)
     * @see #translate(Vector2fc)
     * 
     * @param offset
     *          the new translation to set
     * @return this
     */
    public Matrix3x2f setTranslation(Vector2f offset) {
        return setTranslation(offset.x, offset.y);
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f translate(float x, float y, Matrix3x2f dest) {
        float rm20 = x;
        float rm21 = y;
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2f translate(float x, float y) {
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(Vector2fc)
     * 
     * @param offset
     *          the offset to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f translate(Vector2fc offset, Matrix3x2f dest) {
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(Vector2fc)
     * 
     * @param offset
     *          the offset to translate
     * @return this
     */
    public Matrix3x2f translate(Vector2fc offset) {
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
     * it, use {@link #translation(Vector2fc)}.
     * 
     * @see #translation(Vector2fc)
     * 
     * @param offset
     *          the number of units in x and y by which to translate
     * @return this
     */
    public Matrix3x2f translateLocal(Vector2fc offset) {
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
     * it, use {@link #translation(Vector2fc)}.
     * 
     * @see #translation(Vector2fc)
     * 
     * @param offset
     *          the number of units in x and y by which to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f translateLocal(Vector2fc offset, Matrix3x2f dest) {
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f translateLocal(float x, float y, Matrix3x2f dest) {
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
     * it, use {@link #translation(float, float)}.
     * 
     * @see #translation(float, float)
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @return this
     */
    public Matrix3x2f translateLocal(float x, float y) {
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
     * This is the reverse method of {@link #set(Matrix3x2fc)} and allows to obtain
     * intermediate calculation results when chaining multiple transformations.
     * 
     * @see #set(Matrix3x2fc)
     * 
     * @param dest
     *          the destination matrix
     * @return dest
     */
    public Matrix3x2f get(Matrix3x2f dest) {
        return dest.set(this);
    }


    /**
     * Store this matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #get(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public FloatBuffer get(FloatBuffer buffer) {
        return get(buffer.position(), buffer);
    }

    /**
     * Store this matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * 
     * @param index
     *            the absolute position into the FloatBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public FloatBuffer get(int index, FloatBuffer buffer) {
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
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #get3x3(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get3x3(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public FloatBuffer get3x3(FloatBuffer buffer) {
        MemUtil.INSTANCE.put3x3(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * 
     * @param index
     *            the absolute position into the FloatBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public FloatBuffer get3x3(int index, FloatBuffer buffer) {
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
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #get4x4(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get4x4(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    public FloatBuffer get4x4(FloatBuffer buffer) {
        MemUtil.INSTANCE.put4x4(this, 0, buffer);
        return buffer;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * 
     * @param index
     *            the absolute position into the FloatBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    public FloatBuffer get4x4(int index, FloatBuffer buffer) {
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
     * Store this matrix into the supplied float array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    public float[] get(float[] arr, int offset) {
        MemUtil.INSTANCE.copy(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix into the supplied float array in column-major order.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get(float[], int)}.
     * 
     * @see #get(float[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public float[] get(float[] arr) {
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
    public float[] get3x3(float[] arr, int offset) {
        MemUtil.INSTANCE.copy3x3(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied float array.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get3x3(float[], int)}.
     * 
     * @see #get3x3(float[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public float[] get3x3(float[] arr) {
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
    public float[] get4x4(float[] arr, int offset) {
        MemUtil.INSTANCE.copy4x4(this, arr, offset);
        return arr;
    }

    /**
     * Store this matrix as an equivalent 4x4 matrix in column-major order into the supplied float array.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get4x4(float[], int)}.
     * 
     * @see #get4x4(float[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    public float[] get4x4(float[] arr) {
        return get4x4(arr, 0);
    }


    /**
     * Set the values of this matrix by reading 6 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix3x2f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix3x2f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 float values from the given {@link FloatBuffer} in column-major order,
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
    public Matrix3x2f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Set the values of this matrix by reading 6 float values from the given {@link ByteBuffer} in column-major order,
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
    public Matrix3x2f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set all values within this matrix to zero.
     * 
     * @return this
     */
    public Matrix3x2f zero() {
        MemUtil.INSTANCE.zero(this);
        return this;
    }

    /**
     * Set this matrix to the identity.
     * 
     * @return this
     */
    public Matrix3x2f identity() {
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
    public Matrix3x2f scale(float x, float y, Matrix3x2f dest) {
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
    public Matrix3x2f scale(float x, float y) {
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
    public Matrix3x2f scale(Vector2fc xy) {
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
    public Matrix3x2f scale(Vector2fc xy, Matrix3x2f dest) {
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
     * @see #scale(float, float, Matrix3x2f)
     * 
     * @param xy
     *            the factor for the two components
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2f scale(float xy, Matrix3x2f dest) {
        return scale(xy, xy, dest);
    }

    /**
     * Apply scaling to this matrix by uniformly scaling the two base axes by the given <code>xyz</code> factor.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>, the scaling will be applied first!
     * 
     * @see #scale(float, float)
     * 
     * @param xy
     *            the factor for the two components
     * @return this
     */
    public Matrix3x2f scale(float xy) {
        return scale(xy, xy);
    }

    public Matrix3x2f scaleLocal(float x, float y, Matrix3x2f dest) {
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
    public Matrix3x2f scaleLocal(float x, float y) {
        return scaleLocal(x, y, this);
    }

    public Matrix3x2f scaleLocal(float xy, Matrix3x2f dest) {
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
    public Matrix3x2f scaleLocal(float xy) {
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
    public Matrix3x2f scaleAround(float sx, float sy, float ox, float oy, Matrix3x2f dest) {
        float nm20 = m00 * ox + m10 * oy + m20;
        float nm21 = m01 * ox + m11 * oy + m21;
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
    public Matrix3x2f scaleAround(float sx, float sy, float ox, float oy) {
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
    public Matrix3x2f scaleAround(float factor, float ox, float oy, Matrix3x2f dest) {
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
    public Matrix3x2f scaleAround(float factor, float ox, float oy) {
        return scaleAround(factor, factor, ox, oy, this);
    }

    public Matrix3x2f scaleAroundLocal(float sx, float sy, float ox, float oy, Matrix3x2f dest) {
        dest.m00 = sx * m00;
        dest.m01 = sy * m01;
        dest.m10 = sx * m10;
        dest.m11 = sy * m11;
        dest.m20 = sx * m20 - sx * ox + ox;
        dest.m21 = sy * m21 - sy * oy + oy;
        return dest;
    }

    public Matrix3x2f scaleAroundLocal(float factor, float ox, float oy, Matrix3x2f dest) {
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
     * This method is equivalent to calling: <code>new Matrix3x2f().translate(ox, oy).scale(sx, sy).translate(-ox, -oy).mul(this, this)</code>
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
    public Matrix3x2f scaleAroundLocal(float sx, float sy, float sz, float ox, float oy, float oz) {
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
     * This method is equivalent to calling: <code>new Matrix3x2f().translate(ox, oy).scale(factor).translate(-ox, -oy).mul(this, this)</code>
     * 
     * @param factor
     *            the scaling factor for all three axes
     * @param ox
     *            the x coordinate of the scaling origin
     * @param oy
     *            the y coordinate of the scaling origin
     * @return this
     */
    public Matrix3x2f scaleAroundLocal(float factor, float ox, float oy) {
        return scaleAroundLocal(factor, factor, ox, oy, this);
    }

    /**
     * Set this matrix to be a simple scale matrix, which scales the two base axes uniformly by the given factor.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional scaling.
     * <p>
     * In order to post-multiply a scaling transformation directly to a matrix, use {@link #scale(float) scale()} instead.
     * 
     * @see #scale(float)
     * 
     * @param factor
     *             the scale factor in x and y
     * @return this
     */
    public Matrix3x2f scaling(float factor) {
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
    public Matrix3x2f scaling(float x, float y) {
        m00 = x;
        m01 = 0.0f;
        m10 = 0.0f;
        m11 = y;
        m20 = 0.0f;
        m21 = 0.0f;
        return this;
    }

    /**
     * Set this matrix to a rotation matrix which rotates the given radians.
     * <p>
     * The resulting matrix can be multiplied against another transformation
     * matrix to obtain an additional rotation.
     * <p>
     * In order to apply the rotation transformation to an existing transformation,
     * use {@link #rotate(float) rotate()} instead.
     * 
     * @see #rotate(float)
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Matrix3x2f rotation(float angle) {
        float cos = Math.cos(angle);
        float sin = Math.sin(angle);
        m00 = cos;
        m10 = -sin;
        m20 = 0.0f;
        m01 = sin;
        m11 = cos;
        m21 = 0.0f;
        return this;
    }

    /**
     * Transform/multiply the given vector by this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in that vector.
     * 
     * @see Vector3f#mul(Matrix3x2fc)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector3f transform(Vector3f v) {
        return v.mul(this);
    }

    /**
     * Transform/multiply the given vector by this matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in <code>dest</code>.
     * 
     * @see Vector3f#mul(Matrix3x2fc, Vector3f)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will contain the result
     * @return dest
     */
    public Vector3f transform(Vector3f v, Vector3f dest) {
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
    public Vector3f transform(float x, float y, float z, Vector3f dest) {
       return dest.set(m00 * x + m10 * y + m20 * z, m01 * x + m11 * y + m21 * z, z);
    }

    /**
     * Transform/multiply the given 2D-vector, as if it was a 3D-vector with z=1, by
     * this matrix and store the result in that vector.
     * <p>
     * The given 2D-vector is treated as a 3D-vector with its z-component being 1.0, so it
     * will represent a position/location in 2D-space rather than a direction.
     * <p>
     * In order to store the result in another vector, use {@link #transformPosition(Vector2fc, Vector2f)}.
     * 
     * @see #transformPosition(Vector2fc, Vector2f)
     * @see #transform(Vector3f)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector2f transformPosition(Vector2f v) {
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
     * In order to store the result in the same vector, use {@link #transformPosition(Vector2f)}.
     * 
     * @see #transformPosition(Vector2f)
     * @see #transform(Vector3f, Vector3f)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2f transformPosition(Vector2fc v, Vector2f dest) {
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
     * In order to store the result in the same vector, use {@link #transformPosition(Vector2f)}.
     * 
     * @see #transformPosition(Vector2f)
     * @see #transform(Vector3f, Vector3f)
     * 
     * @param x
     *          the x component of the vector to transform
     * @param y
     *          the y component of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2f transformPosition(float x, float y, Vector2f dest) {
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
     * In order to store the result in another vector, use {@link #transformDirection(Vector2fc, Vector2f)}.
     * 
     * @see #transformDirection(Vector2fc, Vector2f)
     * 
     * @param v
     *          the vector to transform and to hold the final result
     * @return v
     */
    public Vector2f transformDirection(Vector2f v) {
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
     * In order to store the result in the same vector, use {@link #transformDirection(Vector2f)}.
     * 
     * @see #transformDirection(Vector2f)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2f transformDirection(Vector2fc v, Vector2f dest) {
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
     * In order to store the result in the same vector, use {@link #transformDirection(Vector2f)}.
     * 
     * @see #transformDirection(Vector2f)
     * 
     * @param x
     *          the x component of the vector to transform
     * @param y
     *          the y component of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector2f transformDirection(float x, float y, Vector2f dest) {
        return dest.set(m00 * x + m10 * y, m01 * x + m11 * y);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(m00);
        out.writeFloat(m01);
        out.writeFloat(m10);
        out.writeFloat(m11);
        out.writeFloat(m20);
        out.writeFloat(m21);
    }

    public void readExternal(ObjectInput in) throws IOException {
        m00 = in.readFloat();
        m01 = in.readFloat();
        m10 = in.readFloat();
        m11 = in.readFloat();
        m20 = in.readFloat();
        m21 = in.readFloat();
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
    public Matrix3x2f rotate(float ang) {
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
    public Matrix3x2f rotate(float ang, Matrix3x2f dest) {
        float cos = Math.cos(ang);
        float sin = Math.sin(ang);
        float rm00 = cos;
        float rm01 = sin;
        float rm10 = -sin;
        float rm11 = cos;
        float nm00 = m00 * rm00 + m10 * rm01;
        float nm01 = m01 * rm00 + m11 * rm01;
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
     * transformation, use {@link #rotation(float) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(float)
     * 
     * @param ang
     *            the angle in radians to rotate
     * @param dest
     *            will hold the result
     * @return dest
     */
    public Matrix3x2f rotateLocal(float ang, Matrix3x2f dest) {
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
     * transformation, use {@link #rotation(float) rotation()}.
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotation(float)
     * 
     * @param ang
     *            the angle in radians to rotate
     * @return this
     */
    public Matrix3x2f rotateLocal(float ang) {
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
     * @see #translate(float, float)
     * @see #rotate(float)
     * 
     * @param ang
     *            the angle in radians
     * @param x
     *            the x component of the rotation center
     * @param y
     *            the y component of the rotation center
     * @return this
     */
    public Matrix3x2f rotateAbout(float ang, float x, float y) {
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
     * @see #translate(float, float, Matrix3x2f)
     * @see #rotate(float, Matrix3x2f)
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
    public Matrix3x2f rotateAbout(float ang, float x, float y, Matrix3x2f dest) {
        float tm20 = m00 * x + m10 * y + m20;
        float tm21 = m01 * x + m11 * y + m21;
        float cos = Math.cos(ang);
        float sin = Math.sin(ang);
        float nm00 = m00 * cos + m10 * sin;
        float nm01 = m01 * cos + m11 * sin;
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
    public Matrix3x2f rotateTo(Vector2fc fromDir, Vector2fc toDir, Matrix3x2f dest) {
        float dot = fromDir.x() * toDir.x() + fromDir.y() * toDir.y();
        float det = fromDir.x() * toDir.y() - fromDir.y() * toDir.x();
        float rm00 = dot;
        float rm01 = det;
        float rm10 = -det;
        float rm11 = dot;
        float nm00 = m00 * rm00 + m10 * rm01;
        float nm01 = m01 * rm00 + m11 * rm01;
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
    public Matrix3x2f rotateTo(Vector2fc fromDir, Vector2fc toDir) {
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
     * @see #setView(float, float, float, float)
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
    public Matrix3x2f view(float left, float right, float bottom, float top, Matrix3x2f dest) {
        float rm00 = 2.0f / (right - left);
        float rm11 = 2.0f / (top - bottom);
        float rm20 = (left + right) / (left - right);
        float rm21 = (bottom + top) / (bottom - top);
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
     * @see #setView(float, float, float, float)
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
    public Matrix3x2f view(float left, float right, float bottom, float top) {
        return view(left, right, bottom, top, this);
    }

    /**
     * Set this matrix to define a "view" transformation that maps the given <code>(left, bottom)</code> and
     * <code>(right, top)</code> corners to <code>(-1, -1)</code> and <code>(1, 1)</code> respectively.
     * 
     * @see #view(float, float, float, float)
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
    public Matrix3x2f setView(float left, float right, float bottom, float top) {
        m00 = 2.0f / (right - left);
        m01 = 0.0f;
        m10 = 0.0f;
        m11 = 2.0f / (top - bottom);
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
     * Matrix3x2f inv = new Matrix3x2f(this).invert();
     * inv.transform(origin.set(0, 0));
     * </pre>
     * 
     * @param origin
     *          will hold the position transformed to the origin
     * @return origin
     */
    public Vector2f origin(Vector2f origin) {
        float s = 1.0f / (m00 * m11 - m01 * m10);
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
    public float[] viewArea(float[] area) {
        float s = 1.0f / (m00 * m11 - m01 * m10);
        float rm00 =  m11 * s;
        float rm01 = -m01 * s;
        float rm10 = -m10 * s;
        float rm11 =  m00 * s;
        float rm20 = (m10 * m21 - m20 * m11) * s;
        float rm21 = (m20 * m01 - m00 * m21) * s;
        float nxnyX = -rm00 - rm10;
        float nxnyY = -rm01 - rm11;
        float pxnyX =  rm00 - rm10;
        float pxnyY =  rm01 - rm11;
        float nxpyX = -rm00 + rm10;
        float nxpyY = -rm01 + rm11;
        float pxpyX =  rm00 + rm10;
        float pxpyY =  rm01 + rm11;
        float minX = nxnyX;
        minX = minX < nxpyX ? minX : nxpyX;
        minX = minX < pxnyX ? minX : pxnyX;
        minX = minX < pxpyX ? minX : pxpyX;
        float minY = nxnyY;
        minY = minY < nxpyY ? minY : nxpyY;
        minY = minY < pxnyY ? minY : pxnyY;
        minY = minY < pxpyY ? minY : pxpyY;
        float maxX = nxnyX;
        maxX = maxX > nxpyX ? maxX : nxpyX;
        maxX = maxX > pxnyX ? maxX : pxnyX;
        maxX = maxX > pxpyX ? maxX : pxpyX;
        float maxY = nxnyY;
        maxY = maxY > nxpyY ? maxY : nxpyY;
        maxY = maxY > pxnyY ? maxY : pxnyY;
        maxY = maxY > pxpyY ? maxY : pxpyY;
        area[0] = minX + rm20;
        area[1] = minY + rm21;
        area[2] = maxX + rm20;
        area[3] = maxY + rm21;
        return area;
    }

    public Vector2f positiveX(Vector2f dir) {
        float s = m00 * m11 - m01 * m10;
        s = 1.0f / s;
        dir.x =  m11 * s;
        dir.y = -m01 * s;
        return dir.normalize(dir);
    }

    public Vector2f normalizedPositiveX(Vector2f dir) {
        dir.x =  m11;
        dir.y = -m01;
        return dir;
    }

    public Vector2f positiveY(Vector2f dir) {
        float s = m00 * m11 - m01 * m10;
        s = 1.0f / s;
        dir.x = -m10 * s;
        dir.y =  m00 * s;
        return dir.normalize(dir);
    }

    public Vector2f normalizedPositiveY(Vector2f dir) {
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
     * once outside using {@link #invert(Matrix3x2f)} and then the method {@link #unprojectInv(float, float, int[], Vector2f) unprojectInv()} can be invoked on it.
     * 
     * @see #unprojectInv(float, float, int[], Vector2f)
     * @see #invert(Matrix3x2f)
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
    public Vector2f unproject(float winX, float winY, int[] viewport, Vector2f dest) {
        float s = 1.0f / (m00 * m11 - m01 * m10);
        float im00 =  m11 * s;
        float im01 = -m01 * s;
        float im10 = -m10 * s;
        float im11 =  m00 * s;
        float im20 = (m10 * m21 - m20 * m11) * s;
        float im21 = (m20 * m01 - m00 * m21) * s;
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        dest.x = im00 * ndcX + im10 * ndcY + im20;
        dest.y = im01 * ndcX + im11 * ndcY + im21;
        return dest;
    }

    /**
     * Unproject the given window coordinates <code>(winX, winY)</code> by <code>this</code> matrix using the specified viewport.
     * <p>
     * This method differs from {@link #unproject(float, float, int[], Vector2f) unproject()} 
     * in that it assumes that <code>this</code> is already the inverse matrix of the original projection matrix.
     * It exists to avoid recomputing the matrix inverse with every invocation.
     * 
     * @see #unproject(float, float, int[], Vector2f)
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
    public Vector2f unprojectInv(float winX, float winY, int[] viewport, Vector2f dest) {
        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
        dest.x = m00 * ndcX + m10 * ndcY + m20;
        dest.y = m01 * ndcX + m11 * ndcY + m21;
        return dest;
    }

    /**
     * Apply shearing to this matrix by shearing along the X axis using the Y axis factor <code>yFactor</code>.
     * 
     * @param yFactor
     *          the factor for the Y component to shear along the X axis
     * @return this
     */
    public Matrix3x2f shearX(float yFactor) {
        return shearX(yFactor, this);
    }

    /**
     * Apply shearing to this matrix by shearing along the X axis using the Y axis factor <code>yFactor</code>,
     * and store the result in <code>dest</code>.
     * 
     * @param yFactor
     *          the factor for the Y component to shear along the X axis
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f shearX(float yFactor, Matrix3x2f dest) {
        float nm10 = m00 * yFactor + m10;
        float nm11 = m01 * yFactor + m11;
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m20 = m20;
        dest.m21 = m21;
        return dest;
    }

    /**
     * Apply shearing to this matrix by shearing along the Y axis using the X axis factor <code>xFactor</code>.
     * 
     * @param xFactor
     *          the factor for the X component to shear along the Y axis
     * @return this
     */
    public Matrix3x2f shearY(float xFactor) {
        return shearY(xFactor, this);
    }

    /**
     * Apply shearing to this matrix by shearing along the Y axis using the X axis factor <code>xFactor</code>,
     * and store the result in <code>dest</code>.
     * 
     * @param xFactor
     *          the factor for the X component to shear along the Y axis
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Matrix3x2f shearY(float xFactor, Matrix3x2f dest) {
        float nm00 = m00 + m10 * xFactor;
        float nm01 = m01 + m11 * xFactor;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m20 = m20;
        dest.m21 = m21;
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
    public Matrix3x2f span(Vector2f corner, Vector2f xDir, Vector2f yDir) {
        float s = 1.0f / (m00 * m11 - m01 * m10);
        float nm00 =  m11 * s, nm01 = -m01 * s, nm10 = -m10 * s, nm11 =  m00 * s;
        corner.x = -nm00 - nm10 + (m10 * m21 - m20 * m11) * s;
        corner.y = -nm01 - nm11 + (m20 * m01 - m00 * m21) * s;
        xDir.x = 2.0f * nm00; xDir.y = 2.0f * nm01;
        yDir.x = 2.0f * nm10; yDir.y = 2.0f * nm11;
        return this;
    }

    public boolean testPoint(float x, float y) {
        float nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        float pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        float nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        float pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
        return nxX * x + nxY * y + nxW >= 0 && pxX * x + pxY * y + pxW >= 0 &&
               nyX * x + nyY * y + nyW >= 0 && pyX * x + pyY * y + pyW >= 0;
    }

    public boolean testCircle(float x, float y, float r) {
        float invl;
        float nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        invl = Math.invsqrt(nxX * nxX + nxY * nxY);
        nxX *= invl; nxY *= invl; nxW *= invl;
        float pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        invl = Math.invsqrt(pxX * pxX + pxY * pxY);
        pxX *= invl; pxY *= invl; pxW *= invl;
        float nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        invl = Math.invsqrt(nyX * nyX + nyY * nyY);
        nyX *= invl; nyY *= invl; nyW *= invl;
        float pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
        invl = Math.invsqrt(pyX * pyX + pyY * pyY);
        pyX *= invl; pyY *= invl; pyW *= invl;
        return nxX * x + nxY * y + nxW >= -r && pxX * x + pxY * y + pxW >= -r &&
               nyX * x + nyY * y + nyW >= -r && pyX * x + pyY * y + pyW >= -r;
    }

    public boolean testAar(float minX, float minY, float maxX, float maxY) {
        float nxX = +m00, nxY = +m10, nxW = 1.0f + m20;
        float pxX = -m00, pxY = -m10, pxW = 1.0f - m20;
        float nyX = +m01, nyY = +m11, nyW = 1.0f + m21;
        float pyX = -m01, pyY = -m11, pyW = 1.0f - m21;
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
        result = prime * result + Float.floatToIntBits(m00);
        result = prime * result + Float.floatToIntBits(m01);
        result = prime * result + Float.floatToIntBits(m10);
        result = prime * result + Float.floatToIntBits(m11);
        result = prime * result + Float.floatToIntBits(m20);
        result = prime * result + Float.floatToIntBits(m21);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Matrix3x2f other = (Matrix3x2f) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00))
            return false;
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01))
            return false;
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10))
            return false;
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11))
            return false;
        if (Float.floatToIntBits(m20) != Float.floatToIntBits(other.m20))
            return false;
        if (Float.floatToIntBits(m21) != Float.floatToIntBits(other.m21))
            return false;
        return true;
    }

    public boolean equals(Matrix3x2fc m, float delta) {
        if (this == m)
            return true;
        if (m == null)
            return false;
        if (!(m instanceof Matrix3x2f))
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
