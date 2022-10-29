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


import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import java.util.*;

/**
 * Interface to a read-only view of a 3x2 matrix of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Matrix3x2dc {

    /**
     * Return the value of the matrix element at column 0 and row 0.
     * 
     * @return the value of the matrix element
     */
    double m00();

    /**
     * Return the value of the matrix element at column 0 and row 1.
     * 
     * @return the value of the matrix element
     */
    double m01();

    /**
     * Return the value of the matrix element at column 1 and row 0.
     * 
     * @return the value of the matrix element
     */
    double m10();

    /**
     * Return the value of the matrix element at column 1 and row 1.
     * 
     * @return the value of the matrix element
     */
    double m11();

    /**
     * Return the value of the matrix element at column 2 and row 0.
     * 
     * @return the value of the matrix element
     */
    double m20();

    /**
     * Return the value of the matrix element at column 2 and row 1.
     * 
     * @return the value of the matrix element
     */
    double m21();

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
    Matrix3x2d mul(Matrix3x2dc right, Matrix3x2d dest);

    /**
     * Pre-multiply this matrix by the supplied <code>left</code> matrix and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the <code>left</code> matrix,
     * then the new matrix will be <code>L * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>L * M * v</code>, the
     * transformation of <code>this</code> matrix will be applied first!
     *
     * @param left
     *          the left operand of the matrix multiplication
     * @param dest
     *          the destination matrix, which will hold the result
     * @return dest
     */
    Matrix3x2d mulLocal(Matrix3x2dc left, Matrix3x2d dest);

    /**
     * Return the determinant of this matrix.
     * 
     * @return the determinant
     */
    double determinant();

    /**
     * Invert the <code>this</code> matrix by assuming a third row in this matrix of <code>(0, 0, 1)</code>
     * and store the result in <code>dest</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3x2d invert(Matrix3x2d dest);

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y and store the result
     * in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3x2d translate(double x, double y, Matrix3x2d dest);

    /**
     * Apply a translation to this matrix by translating by the given number of units in x and y, and
     * store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>M * T</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>M * T * v</code>, the translation will be applied first!
     * 
     * @param offset
     *          the offset to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3x2d translate(Vector2dc offset, Matrix3x2d dest);

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * 
     * @param offset
     *          the number of units in x and y by which to translate
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3x2d translateLocal(Vector2dc offset, Matrix3x2d dest);

    /**
     * Pre-multiply a translation to this matrix by translating by the given number of
     * units in x and y and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>T</code> the translation
     * matrix, then the new matrix will be <code>T * M</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>T * M * v</code>, the translation will be applied last!
     * 
     * @param x
     *          the offset to translate in x
     * @param y
     *          the offset to translate in y
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3x2d translateLocal(double x, double y, Matrix3x2d dest);

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * 
     * @param dest
     *          the destination matrix
     * @return dest
     */
    Matrix3x2d get(Matrix3x2d dest);


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
    DoubleBuffer get(DoubleBuffer buffer);

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
    DoubleBuffer get(int index, DoubleBuffer buffer);

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
    ByteBuffer get(ByteBuffer buffer);

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
    ByteBuffer get(int index, ByteBuffer buffer);

    /**
     * Store this matrix as an equivalent 3x3 matrix in column-major order into the supplied {@link DoubleBuffer} at the current
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
    DoubleBuffer get3x3(DoubleBuffer buffer);

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
    DoubleBuffer get3x3(int index, DoubleBuffer buffer);

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
    ByteBuffer get3x3(ByteBuffer buffer);

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
    ByteBuffer get3x3(int index, ByteBuffer buffer);

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
    DoubleBuffer get4x4(DoubleBuffer buffer);

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
    DoubleBuffer get4x4(int index, DoubleBuffer buffer);

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
    ByteBuffer get4x4(ByteBuffer buffer);

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
    ByteBuffer get4x4(int index, ByteBuffer buffer);


    /**
     * Store this matrix into the supplied double array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    double[] get(double[] arr, int offset);

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
    double[] get(double[] arr);

    /**
     * Store this matrix as an equivalent 3x3 matrix into the supplied double array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    double[] get3x3(double[] arr, int offset);

    /**
     * Store this matrix as an equivalent 3x3 matrix into the supplied double array in column-major order.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get3x3(double[], int)}.
     * 
     * @see #get3x3(double[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    double[] get3x3(double[] arr);

    /**
     * Store this matrix as an equivalent 4x4 matrix into the supplied double array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    double[] get4x4(double[] arr, int offset);

    /**
     * Store this matrix as an equivalent 4x4 matrix into the supplied double array in column-major order.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get4x4(double[], int)}.
     * 
     * @see #get4x4(double[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    double[] get4x4(double[] arr);

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
    Matrix3x2d scale(double x, double y, Matrix3x2d dest);

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
    Matrix3x2d scale(Vector2dc xy, Matrix3x2d dest);

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
    Matrix3x2d scale(Vector2fc xy, Matrix3x2d dest);

    /**
     * Pre-multiply scaling to <code>this</code> matrix by scaling the two base axes by the given <code>xy</code> factor,
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>
     * , the scaling will be applied last!
     * 
     * @param xy
     *            the factor to scale all two base axes by
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3x2d scaleLocal(double xy, Matrix3x2d dest);

    /**
     * Pre-multiply scaling to <code>this</code> matrix by scaling the base axes by the given x and y
     * factors and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>
     * , the scaling will be applied last!
     * 
     * @param x
     *            the factor of the x component
     * @param y
     *            the factor of the y component
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3x2d scaleLocal(double x, double y, Matrix3x2d dest);

    /**
     * Pre-multiply scaling to <code>this</code> matrix by scaling the base axes by the given sx and
     * sy factors while using the given <code>(ox, oy)</code> as the scaling origin,
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>
     * , the scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix3x2d().translate(ox, oy).scale(sx, sy).translate(-ox, -oy).mul(this, dest)</code>
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
    Matrix3x2d scaleAroundLocal(double sx, double sy, double ox, double oy, Matrix3x2d dest);

    /**
     * Pre-multiply scaling to this matrix by scaling the base axes by the given <code>factor</code>
     * while using <code>(ox, oy)</code> as the scaling origin,
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>S * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>S * M * v</code>, the
     * scaling will be applied last!
     * <p>
     * This method is equivalent to calling: <code>new Matrix3x2d().translate(ox, oy).scale(factor).translate(-ox, -oy).mul(this, dest)</code>
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
    Matrix3x2d scaleAroundLocal(double factor, double ox, double oy, Matrix3x2d dest);

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
    Matrix3x2d scale(double xy, Matrix3x2d dest);

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
    Matrix3x2d scaleAround(double sx, double sy, double ox, double oy, Matrix3x2d dest);

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
    Matrix3x2d scaleAround(double factor, double ox, double oy, Matrix3x2d dest);

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
    Vector3d transform(Vector3d v);

    /**
     * Transform/multiply the given vector by this matrix and store the result in <code>dest</code>.
     * 
     * @see Vector3d#mul(Matrix3x2dc, Vector3d)
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will contain the result
     * @return dest
     */
    Vector3d transform(Vector3dc v, Vector3d dest);

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
    Vector3d transform(double x, double y, double z, Vector3d dest);

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
    Vector2d transformPosition(Vector2d v);

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
    Vector2d transformPosition(Vector2dc v, Vector2d dest);

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
    Vector2d transformPosition(double x, double y, Vector2d dest);

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
    Vector2d transformDirection(Vector2d v);

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
    Vector2d transformDirection(Vector2dc v, Vector2d dest);

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
    Vector2d transformDirection(double x, double y, Vector2d dest);

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
    Matrix3x2d rotate(double ang, Matrix3x2d dest);

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3x2d rotateLocal(double ang, Matrix3x2d dest);

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
    Matrix3x2d rotateAbout(double ang, double x, double y, Matrix3x2d dest);

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
    Matrix3x2d rotateTo(Vector2dc fromDir, Vector2dc toDir, Matrix3x2d dest);

    /**
     * Apply a "view" transformation to this matrix that maps the given <code>(left, bottom)</code> and
     * <code>(right, top)</code> corners to <code>(-1, -1)</code> and <code>(1, 1)</code> respectively and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>O</code> the orthographic projection matrix,
     * then the new matrix will be <code>M * O</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * O * v</code>, the
     * orthographic projection transformation will be applied first!
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
    Matrix3x2d view(double left, double right, double bottom, double top, Matrix3x2d dest);

    /**
     * Obtain the position that gets transformed to the origin by <code>this</code> matrix.
     * This can be used to get the position of the "camera" from a given <i>view</i> transformation matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).invertAffine();
     * inv.transform(origin.set(0, 0));
     * </pre>
     * 
     * @param origin
     *          will hold the position transformed to the origin
     * @return origin
     */
    Vector2d origin(Vector2d origin);

    /**
     * Obtain the extents of the view transformation of <code>this</code> matrix and store it in <code>area</code>.
     * This can be used to determine which region of the screen (i.e. the NDC space) is covered by the view.
     * 
     * @param area
     *          will hold the view area as <code>[minX, minY, maxX, maxY]</code>
     * @return area
     */
    double[] viewArea(double[] area);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method uses the rotation component of the left 2x2 submatrix to obtain the direction 
     * that is transformed to <code>+X</code> by <code>this</code> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).invert();
     * inv.transformDirection(dir.set(1, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveX(Vector2d)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector2d positiveX(Vector2d dir);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method uses the rotation component of the left 2x2 submatrix to obtain the direction 
     * that is transformed to <code>+X</code> by <code>this</code> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).transpose();
     * inv.transformDirection(dir.set(1, 0));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector2d normalizedPositiveX(Vector2d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method uses the rotation component of the left 2x2 submatrix to obtain the direction 
     * that is transformed to <code>+Y</code> by <code>this</code> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).invert();
     * inv.transformDirection(dir.set(0, 1)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveY(Vector2d)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector2d positiveY(Vector2d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method uses the rotation component of the left 2x2 submatrix to obtain the direction 
     * that is transformed to <code>+Y</code> by <code>this</code> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3x2d inv = new Matrix3x2d(this).transpose();
     * inv.transformDirection(dir.set(0, 1));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector2d normalizedPositiveY(Vector2d dir);

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
    Vector2d unproject(double winX, double winY, int[] viewport, Vector2d dest);

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
    Vector2d unprojectInv(double winX, double winY, int[] viewport, Vector2d dest);

    /**
     * Test whether the given point <code>(x, y)</code> is within the frustum defined by <code>this</code> matrix.
     * <p>
     * This method assumes <code>this</code> matrix to be a transformation from any arbitrary coordinate system/space <code>M</code>
     * into standard OpenGL clip space and tests whether the given point with the coordinates <code>(x, y, z)</code> given
     * in space <code>M</code> is within the clip space.
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @param x
     *          the x-coordinate of the point
     * @param y
     *          the y-coordinate of the point
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    boolean testPoint(double x, double y);

    /**
     * Test whether the given circle is partly or completely within or outside of the frustum defined by <code>this</code> matrix.
     * <p>
     * This method assumes <code>this</code> matrix to be a transformation from any arbitrary coordinate system/space <code>M</code>
     * into standard OpenGL clip space and tests whether the given sphere with the coordinates <code>(x, y, z)</code> given
     * in space <code>M</code> is within the clip space.
     * <p>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @param x
     *          the x-coordinate of the circle's center
     * @param y
     *          the y-coordinate of the circle's center
     * @param r
     *          the circle's radius
     * @return <code>true</code> if the given circle is partly or completely inside the frustum; <code>false</code> otherwise
     */
    boolean testCircle(double x, double y, double r);

    /**
     * Test whether the given axis-aligned rectangle is partly or completely within or outside of the frustum defined by <code>this</code> matrix.
     * The rectangle is specified via its min and max corner coordinates.
     * <p>
     * This method assumes <code>this</code> matrix to be a transformation from any arbitrary coordinate system/space <code>M</code>
     * into standard OpenGL clip space and tests whether the given axis-aligned rectangle with its minimum corner coordinates <code>(minX, minY, minZ)</code>
     * and maximum corner coordinates <code>(maxX, maxY, maxZ)</code> given in space <code>M</code> is within the clip space.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     * <br>
     * Reference: <a href="http://gamedevs.org/uploads/fast-extraction-viewing-frustum-planes-from-world-view-projection-matrix.pdf">
     * Fast Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>
     * 
     * @param minX
     *          the x-coordinate of the minimum corner
     * @param minY
     *          the y-coordinate of the minimum corner
     * @param maxX
     *          the x-coordinate of the maximum corner
     * @param maxY
     *          the y-coordinate of the maximum corner
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    boolean testAar(double minX, double minY, double maxX, double maxY);

    /**
     * Compare the matrix elements of <code>this</code> matrix with the given matrix using the given <code>delta</code>
     * and return whether all of them are equal within a maximum difference of <code>delta</code>.
     * <p>
     * Please note that this method is not used by any data structure such as {@link ArrayList} {@link HashSet} or {@link HashMap}
     * and their operations, such as {@link ArrayList#contains(Object)} or {@link HashSet#remove(Object)}, since those
     * data structures only use the {@link Object#equals(Object)} and {@link Object#hashCode()} methods.
     * 
     * @param m
     *          the other matrix
     * @param delta
     *          the allowed maximum difference
     * @return <code>true</code> whether all of the matrix elements are equal; <code>false</code> otherwise
     */
    boolean equals(Matrix3x2dc m, double delta);

    /**
     * Determine whether all matrix elements are finite floating-point values, that
     * is, they are not {@link Double#isNaN() NaN} and not
     * {@link Double#isInfinite() infinity}.
     *
     * @return {@code true} if all components are finite floating-point values;
     *         {@code false} otherwise
     */
    boolean isFinite();

}
