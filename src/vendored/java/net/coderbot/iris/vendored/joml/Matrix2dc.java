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
import java.util.*;

/**
 * Interface to a read-only view of a 2x2 matrix of double-precision floats.
 *
 * @author Joseph Burton
 */
public interface Matrix2dc {

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
     * Multiply this matrix by the supplied <code>right</code> matrix and store the result in <code>dest</code>.
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
    Matrix2d mul(Matrix2dc right, Matrix2d dest);

    /**
     * Multiply this matrix by the supplied <code>right</code> matrix and store the result in <code>dest</code>.
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
    Matrix2d mul(Matrix2fc right, Matrix2d dest);

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
    Matrix2d mulLocal(Matrix2dc left, Matrix2d dest);

    /**
     * Return the determinant of this matrix.
     *
     * @return the determinant
     */
    double determinant();

    /**
     * Invert the <code>this</code> matrix and store the result in <code>dest</code>.
     *
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix2d invert(Matrix2d dest);

    /**
     * Transpose <code>this</code> matrix and store the result in <code>dest</code>.
     *
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix2d transpose(Matrix2d dest);

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     *
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix2d get(Matrix2d dest);

    /**
     * Get the current values of <code>this</code> matrix and store them as
     * the rotational component of <code>dest</code>. All other values of <code>dest</code> will
     * be set to 0.
     *
     * @see Matrix3x2d#set(Matrix2dc)
     *
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix3x2d get(Matrix3x2d dest);

    /**
     * Get the current values of <code>this</code> matrix and store them as
     * the rotational component of <code>dest</code>. All other values of <code>dest</code> will
     * be set to identity.
     *
     * @see Matrix3d#set(Matrix2dc)
     *
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix3d get(Matrix3d dest);

    /**
     * Get the angle of the rotation component of <code>this</code> matrix.
     * <p>
     * This method assumes that there is a valid rotation to be returned, i.e. that
     * <code>atan2(-m10, m00) == atan2(m01, m11)</code>.
     *
     * @return the angle
     */
    double getRotation();


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
     * Store the transpose of this matrix in column-major order into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the matrix is stored, use {@link #getTransposed(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     *
     * @see #getTransposed(int, DoubleBuffer)
     *
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    DoubleBuffer getTransposed(DoubleBuffer buffer);

    /**
     * Store the transpose of this matrix in column-major order into the supplied {@link DoubleBuffer} starting at the specified
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
    DoubleBuffer getTransposed(int index, DoubleBuffer buffer);

    /**
     * Store the transpose of this matrix in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #getTransposed(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @see #getTransposed(int, ByteBuffer)
     *
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    ByteBuffer getTransposed(ByteBuffer buffer);

    /**
     * Store the transpose of this matrix in column-major order into the supplied {@link ByteBuffer} starting at the specified
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
    ByteBuffer getTransposed(int index, ByteBuffer buffer);


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
     * Apply scaling to <code>this</code> matrix by scaling the base axes by the given <code>xy.x</code> and
     * <code>xy.y</code> factors, respectively and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     *
     * @param xy
     *            the factors of the x and y component, respectively
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix2d scale(Vector2dc xy, Matrix2d dest);

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x and
     * y factors and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix2d scale(double x, double y, Matrix2d dest);

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xy</code> factor
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     *
     * @see #scale(double, double, Matrix2d)
     *
     * @param xy
     *            the factor for all components
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix2d scale(double xy, Matrix2d dest);

    /**
     * Pre-multiply scaling to <code>this</code> matrix by scaling the base axes by the given x and
     * y factors and store the result in <code>dest</code>.
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
    Matrix2d scaleLocal(double x, double y, Matrix2d dest);

    /**
     * Transform the given vector by this matrix.
     *
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector2d transform(Vector2d v);

    /**
     * Transform the given vector by this matrix and store the result in <code>dest</code>.
     *
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d transform(Vector2dc v, Vector2d dest);

    /**
     * Transform the vector <code>(x, y)</code> by this matrix and store the result in <code>dest</code>.
     *
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d transform(double x, double y, Vector2d dest);

    /**
     * Transform the given vector by the transpose of this matrix.
     *
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector2d transformTranspose(Vector2d v);

    /**
     * Transform the given vector by the transpose of this matrix and store the result in <code>dest</code>.
     *
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d transformTranspose(Vector2dc v, Vector2d dest);

    /**
     * Transform the vector <code>(x, y)</code> by the transpose of this matrix and store the result in <code>dest</code>.
     *
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d transformTranspose(double x, double y, Vector2d dest);

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * and store the result in <code>dest</code>.
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
     * @param ang
     *            the angle in radians
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix2d rotate(double ang, Matrix2d dest);

    /**
     * Pre-multiply a rotation to this matrix by rotating the given amount of radians
     * and store the result in <code>dest</code>.
     * <p>
     * The produced rotation will rotate a vector counter-clockwise around the origin.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation matrix,
     * then the new matrix will be <code>R * M</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>R * M * v</code>, the
     * rotation will be applied last!
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions">http://en.wikipedia.org</a>
     *
     * @param ang
     *            the angle in radians
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix2d rotateLocal(double ang, Matrix2d dest);

    /**
     * Get the row at the given <code>row</code> index, starting with <code>0</code>.
     *
     * @param row
     *          the row index in <code>[0..1]</code>
     * @param dest
     *          will hold the row components
     * @return the passed in destination
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..1]</code>
     */
    Vector2d getRow(int row, Vector2d dest) throws IndexOutOfBoundsException;

    /**
     * Get the column at the given <code>column</code> index, starting with <code>0</code>.
     *
     * @param column
     *          the column index in <code>[0..1]</code>
     * @param dest
     *          will hold the column components
     * @return the passed in destination
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..1]</code>
     */
    Vector2d getColumn(int column, Vector2d dest) throws IndexOutOfBoundsException;

    /**
     * Get the matrix element value at the given column and row.
     *
     * @param column
     *          the colum index in <code>[0..1]</code>
     * @param row
     *          the row index in <code>[0..1]</code>
     * @return the element value
     */
    double get(int column, int row);

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     *
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix2d normal(Matrix2d dest);

    /**
     * Get the scaling factors of <code>this</code> matrix for the three base axes.
     *
     * @param dest
     *          will hold the scaling factors for <code>x</code> and <code>y</code>
     * @return dest
     */
    Vector2d getScale(Vector2d dest);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix2d inv = new Matrix2d(this).invert();
     * inv.transform(dir.set(1, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveX(Vector2d)} instead.
     *
     * @param dest
     *          will hold the direction of <code>+X</code>
     * @return dest
     */
    Vector2d positiveX(Vector2d dest);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix2d inv = new Matrix2d(this).transpose();
     * inv.transform(dir.set(1, 0));
     * </pre>
     *
     * @param dest
     *          will hold the direction of <code>+X</code>
     * @return dest
     */
    Vector2d normalizedPositiveX(Vector2d dest);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix2d inv = new Matrix2d(this).invert();
     * inv.transform(dir.set(0, 1)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveY(Vector2d)} instead.
     *
     * @param dest
     *          will hold the direction of <code>+Y</code>
     * @return dest
     */
    Vector2d positiveY(Vector2d dest);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix2d inv = new Matrix2d(this).transpose();
     * inv.transform(dir.set(0, 1));
     * </pre>
     *
     * @param dest
     *          will hold the direction of <code>+Y</code>
     * @return dest
     */
    Vector2d normalizedPositiveY(Vector2d dest);

    /**
     * Component-wise add <code>this</code> and <code>other</code> and store the result in <code>dest</code>.
     *
     * @param other
     *          the other addend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix2d add(Matrix2dc other, Matrix2d dest);

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code> and store the result in <code>dest</code>.
     *
     * @param subtrahend
     *          the subtrahend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix2d sub(Matrix2dc subtrahend, Matrix2d dest);

    /**
     * Component-wise multiply <code>this</code> by <code>other</code> and store the result in <code>dest</code>.
     *
     * @param other
     *          the other matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix2d mulComponentWise(Matrix2dc other, Matrix2d dest);

    /**
     * Linearly interpolate <code>this</code> and <code>other</code> using the given interpolation factor <code>t</code>
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>t</code> is <code>0.0</code> then the result is <code>this</code>. If the interpolation factor is <code>1.0</code>
     * then the result is <code>other</code>.
     *
     * @param other
     *          the other matrix
     * @param t
     *          the interpolation factor between 0.0 and 1.0
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix2d lerp(Matrix2dc other, double t, Matrix2d dest);

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
    boolean equals(Matrix2dc m, double delta);

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
