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


import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import java.util.*;

/**
 * Interface to a read-only view of a 3x3 matrix of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Matrix3dc {

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
     * Return the value of the matrix element at column 0 and row 2.
     * 
     * @return the value of the matrix element
     */
    double m02();

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
     * Return the value of the matrix element at column 1 and row 2.
     * 
     * @return the value of the matrix element
     */
    double m12();

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
     * Return the value of the matrix element at column 2 and row 2.
     * 
     * @return the value of the matrix element
     */
    double m22();

    /**
     * Multiply this matrix by the supplied matrix and store the result in <code>dest</code>.
     * This matrix will be the left one.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     * 
     * @param right
     *          the right operand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d mul(Matrix3dc right, Matrix3d dest);

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
    Matrix3d mulLocal(Matrix3dc left, Matrix3d dest);

    /**
     * Multiply this matrix by the supplied matrix and store the result in <code>dest</code>.
     * This matrix will be the left one.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the <code>right</code> matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * transformation of the right matrix will be applied first!
     * 
     * @param right
     *          the right operand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d mul(Matrix3fc right, Matrix3d dest);

    /**
     * Return the determinant of this matrix.
     * 
     * @return the determinant
     */
    double determinant();

    /**
     * Invert <code>this</code> matrix and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d invert(Matrix3d dest);

    /**
     * Transpose <code>this</code> matrix and store the result in <code>dest</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3d transpose(Matrix3d dest);

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * 
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix3d get(Matrix3d dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link AxisAngle4f}.
     * 
     * @see AxisAngle4f#set(Matrix3dc)
     * 
     * @param dest
     *          the destination {@link AxisAngle4f}
     * @return the passed in destination
     */
    AxisAngle4f getRotation(AxisAngle4f dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link Quaternionf}.
     * <p>
     * This method assumes that the three column vectors of this matrix are not normalized and
     * thus allows to ignore any additional scaling factor that is applied to the matrix.
     * 
     * @see Quaternionf#setFromUnnormalized(Matrix3dc)
     * 
     * @param dest
     *          the destination {@link Quaternionf}
     * @return the passed in destination
     */
    Quaternionf getUnnormalizedRotation(Quaternionf dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link Quaternionf}.
     * <p>
     * This method assumes that the three column vectors of this matrix are normalized.
     * 
     * @see Quaternionf#setFromNormalized(Matrix3dc)
     * 
     * @param dest
     *          the destination {@link Quaternionf}
     * @return the passed in destination
     */
    Quaternionf getNormalizedRotation(Quaternionf dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link Quaterniond}.
     * <p>
     * This method assumes that the three column vectors of this matrix are not normalized and
     * thus allows to ignore any additional scaling factor that is applied to the matrix.
     * 
     * @see Quaterniond#setFromUnnormalized(Matrix3dc)
     * 
     * @param dest
     *          the destination {@link Quaterniond}
     * @return the passed in destination
     */
    Quaterniond getUnnormalizedRotation(Quaterniond dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link Quaterniond}.
     * <p>
     * This method assumes that the three column vectors of this matrix are normalized.
     * 
     * @see Quaterniond#setFromNormalized(Matrix3dc)
     * 
     * @param dest
     *          the destination {@link Quaterniond}
     * @return the passed in destination
     */
    Quaterniond getNormalizedRotation(Quaterniond dest);


    /**
     * Store this matrix into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position} using column-major order.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer} at which
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
     * Store this matrix into the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index using column-major order.
     * <p>
     * This method will not increment the position of the given {@link DoubleBuffer}.
     * 
     * @param index
     *            the absolute position into the {@link DoubleBuffer}
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    DoubleBuffer get(int index, DoubleBuffer buffer);

    /**
     * Store this matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #get(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given FloatBuffer.
     * 
     * @see #get(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    FloatBuffer get(FloatBuffer buffer);

    /**
     * Store this matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given FloatBuffer.
     * 
     * @param index
     *            the absolute position into the FloatBuffer
     * @param buffer
     *            will receive the values of this matrix in column-major order
     * @return the passed in buffer
     */
    FloatBuffer get(int index, FloatBuffer buffer);

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
     * Store the elements of this matrix as float values in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #getFloats(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #getFloats(int, ByteBuffer)
     * 
     * @param buffer
     *            will receive the elements of this matrix as float values in column-major order at its current position
     * @return the passed in buffer
     */
    ByteBuffer getFloats(ByteBuffer buffer);

    /**
     * Store the elements of this matrix as float values in column-major order into the supplied {@link ByteBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given ByteBuffer.
     * 
     * @param index
     *            the absolute position into the ByteBuffer
     * @param buffer
     *            will receive the elements of this matrix as float values in column-major order
     * @return the passed in buffer
     */
    ByteBuffer getFloats(int index, ByteBuffer buffer);


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
     * Store the elements of this matrix as float values in column-major order into the supplied float array at the given offset.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given float array.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    float[] get(float[] arr, int offset);

    /**
     * Store the elements of this matrix as float values in column-major order into the supplied float array.
     * <p>
     * Please note that due to this matrix storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given float array.
     * <p>
     * In order to specify an explicit offset into the array, use the method {@link #get(float[], int)}.
     * 
     * @see #get(float[], int)
     * 
     * @param arr
     *          the array to write the matrix values into
     * @return the passed in array
     */
    float[] get(float[] arr);

    /**
     * Apply scaling to <code>this</code> matrix by scaling the base axes by the given <code>xyz.x</code>,
     * <code>xyz.y</code> and <code>xyz.z</code> factors, respectively and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * 
     * @param xyz
     *            the factors of the x, y and z component, respectively
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d scale(Vector3dc xyz, Matrix3d dest);

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x,
     * y and z factors and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d scale(double x, double y, double z, Matrix3d dest);

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xyz</code> factor
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * 
     * @see #scale(double, double, double, Matrix3d)
     * 
     * @param xyz
     *            the factor for all components
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d scale(double xyz, Matrix3d dest);

    /**
     * Pre-multiply scaling to <code>this</code> matrix by scaling the base axes by the given x,
     * y and z factors and store the result in <code>dest</code>.
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
     * @param z
     *            the factor of the z component
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d scaleLocal(double x, double y, double z, Matrix3d dest);

    /**
     * Transform the given vector by this matrix.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector3d transform(Vector3d v);

    /**
     * Transform the given vector by this matrix and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transform(Vector3dc v, Vector3d dest);

    /**
     * Transform the given vector by this matrix.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector3f transform(Vector3f v);

    /**
     * Transform the given vector by this matrix and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transform(Vector3fc v, Vector3f dest);

    /**
     * Transform the vector <code>(x, y, z)</code> by this matrix and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transform(double x, double y, double z, Vector3d dest);

    /**
     * Transform the given vector by the transpose of this matrix.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector3d transformTranspose(Vector3d v);

    /**
     * Transform the given vector by the transpose of this matrix and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformTranspose(Vector3dc v, Vector3d dest);

    /**
     * Transform the vector <code>(x, y, z)</code> by the transpose of this matrix and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformTranspose(double x, double y, double z, Vector3d dest);

    /**
     * Apply rotation about the X axis to this matrix by rotating the given amount of radians
     * and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateX(double ang, Matrix3d dest);

    /**
     * Apply rotation about the Y axis to this matrix by rotating the given amount of radians
     * and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateY(double ang, Matrix3d dest);

    /**
     * Apply rotation about the Z axis to this matrix by rotating the given amount of radians
     * and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateZ(double ang, Matrix3d dest);

    /**
     * Apply rotation of <code>angleX</code> radians about the X axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis and store the result in <code>dest</code>.
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
     * This method is equivalent to calling: <code>rotateX(angleX, dest).rotateY(angleY).rotateZ(angleZ)</code>
     * 
     * @param angleX
     *            the angle to rotate about X
     * @param angleY
     *            the angle to rotate about Y
     * @param angleZ
     *            the angle to rotate about Z
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateXYZ(double angleX, double angleY, double angleZ, Matrix3d dest);

    /**
     * Apply rotation of <code>angleZ</code> radians about the Z axis, followed by a rotation of <code>angleY</code> radians about the Y axis and
     * followed by a rotation of <code>angleX</code> radians about the X axis and store the result in <code>dest</code>.
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
     * This method is equivalent to calling: <code>rotateZ(angleZ, dest).rotateY(angleY).rotateX(angleX)</code>
     * 
     * @param angleZ
     *            the angle to rotate about Z
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateZYX(double angleZ, double angleY, double angleX, Matrix3d dest);

    /**
     * Apply rotation of <code>angleY</code> radians about the Y axis, followed by a rotation of <code>angleX</code> radians about the X axis and
     * followed by a rotation of <code>angleZ</code> radians about the Z axis and store the result in <code>dest</code>.
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
     * This method is equivalent to calling: <code>rotateY(angleY, dest).rotateX(angleX).rotateZ(angleZ)</code>
     * 
     * @param angleY
     *            the angle to rotate about Y
     * @param angleX
     *            the angle to rotate about X
     * @param angleZ
     *            the angle to rotate about Z
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateYXZ(double angleY, double angleX, double angleZ, Matrix3d dest);

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the given axis specified as x, y and z components, and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotate(double ang, double x, double y, double z, Matrix3d dest);

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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateLocal(double ang, double x, double y, double z, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians to rotate about the X axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateLocalX(double ang, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians to rotate about the Y axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateLocalY(double ang, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @param ang
     *            the angle in radians to rotate about the Z axis
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d rotateLocalZ(double ang, Matrix3d dest);

    /**
     * Pre-multiply the rotation - and possibly scaling - transformation of the given {@link Quaterniondc} to this matrix and store
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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaterniondc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotateLocal(Quaterniondc quat, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotateLocal(Quaternionfc quat, Matrix3d dest);

    /**
     * Apply the rotation - and possibly scaling - transformation of the given {@link Quaterniondc} to this matrix and store
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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaterniondc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(Quaterniondc quat, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
     * 
     * @param quat
     *          the {@link Quaternionfc}
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(Quaternionfc quat, Matrix3d dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(double, double, double, double, Matrix3d)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f} (needs to be {@link AxisAngle4f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(AxisAngle4f axisAngle, Matrix3d dest);

    /**
     * Apply a rotation transformation, rotating about the given {@link AxisAngle4d} and store the result in <code>dest</code>.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given {@link AxisAngle4d},
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the {@link AxisAngle4d} rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(double, double, double, double, Matrix3d)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d} (needs to be {@link AxisAngle4d#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(AxisAngle4d axisAngle, Matrix3d dest);

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis and store the result in <code>dest</code>.
     * <p>
     * The axis described by the <code>axis</code> vector needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given axis and angle,
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the axis-angle rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(double, double, double, double, Matrix3d)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the rotation axis (needs to be {@link Vector3d#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(double angle, Vector3dc axis, Matrix3d dest);

    /**
     * Apply a rotation transformation, rotating the given radians about the specified axis and store the result in <code>dest</code>.
     * <p>
     * The axis described by the <code>axis</code> vector needs to be a unit vector.
     * <p>
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector 
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation matrix obtained from the given axis and angle,
     * then the new matrix will be <code>M * A</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * A * v</code>,
     * the axis-angle rotation will be applied first!
     * <p>
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(double, double, double, double, Matrix3d)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the rotation axis (needs to be {@link Vector3f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d rotate(double angle, Vector3fc axis, Matrix3d dest);

    /**
     * Get the row at the given <code>row</code> index, starting with <code>0</code>.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param dest
     *          will hold the row components
     * @return the passed in destination
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..2]</code>
     */
    Vector3d getRow(int row, Vector3d dest) throws IndexOutOfBoundsException;

    /**
     * Get the column at the given <code>column</code> index, starting with <code>0</code>.
     * 
     * @param column
     *          the column index in <code>[0..2]</code>
     * @param dest
     *          will hold the column components
     * @return the passed in destination
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..2]</code>
     */
    Vector3d getColumn(int column, Vector3d dest) throws IndexOutOfBoundsException;

    /**
     * Get the matrix element value at the given column and row.
     * 
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @param row
     *          the row index in <code>[0..2]</code>
     * @return the element value
     */
    double get(int column, int row);

    /**
     * Get the matrix element value at the given row and column.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @return the element value
     */
    double getRowColumn(int column, int row);

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3d normal(Matrix3d dest);

    /**
     * Compute the cofactor matrix of <code>this</code> and store it into <code>dest</code>.
     * <p>
     * The cofactor matrix can be used instead of {@link #normal(Matrix3d)} to transform normals
     * when the orientation of the normals with respect to the surface should be preserved.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3d cofactor(Matrix3d dest);

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>
     * and store the result in <code>dest</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * 
     * @see #lookAlong(double, double, double, double, double, double, Matrix3d)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3d lookAlong(Vector3dc dir, Vector3dc up, Matrix3d dest);

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>
     * and store the result in <code>dest</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
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
    Matrix3d lookAlong(double dirX, double dirY, double dirZ, double upX, double upY, double upZ, Matrix3d dest);

    /**
     * Get the scaling factors of <code>this</code> matrix for the three base axes.
     * 
     * @param dest
     *          will hold the scaling factors for <code>x</code>, <code>y</code> and <code>z</code>
     * @return dest
     */
    Vector3d getScale(Vector3d dest);

    /**
     * Obtain the direction of <code>+Z</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).invert();
     * inv.transform(dir.set(0, 0, 1)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveZ(Vector3d)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3d positiveZ(Vector3d dir);

    /**
     * Obtain the direction of <code>+Z</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).transpose();
     * inv.transform(dir.set(0, 0, 1));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3d normalizedPositiveZ(Vector3d dir);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).invert();
     * inv.transform(dir.set(1, 0, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveX(Vector3d)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3d positiveX(Vector3d dir);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).transpose();
     * inv.transform(dir.set(1, 0, 0));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3d normalizedPositiveX(Vector3d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).invert();
     * inv.transform(dir.set(0, 1, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveY(Vector3d)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3d positiveY(Vector3d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3d inv = new Matrix3d(this).transpose();
     * inv.transform(dir.set(0, 1, 0));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3d normalizedPositiveY(Vector3d dir);

    /**
     * Component-wise add <code>this</code> and <code>other</code> and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other addend 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d add(Matrix3dc other, Matrix3d dest);

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code> and store the result in <code>dest</code>.
     * 
     * @param subtrahend
     *          the subtrahend 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d sub(Matrix3dc subtrahend, Matrix3d dest);

    /**
     * Component-wise multiply <code>this</code> by <code>other</code> and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3d mulComponentWise(Matrix3dc other, Matrix3d dest);

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
    Matrix3d lerp(Matrix3dc other, double t, Matrix3d dest);

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
     * This method is equivalent to calling: <code>mul(new Matrix3d().lookAlong(new Vector3d(dir).negate(), up).invert(), dest)</code>
     * 
     * @see #rotateTowards(double, double, double, double, double, double, Matrix3d)
     * 
     * @param direction
     *              the direction to rotate towards
     * @param up
     *              the model's up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    Matrix3d rotateTowards(Vector3dc direction, Vector3dc up, Matrix3d dest);

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
     * This method is equivalent to calling: <code>mul(new Matrix3d().lookAlong(-dirX, -dirY, -dirZ, upX, upY, upZ).invert(), dest)</code>
     * 
     * @see #rotateTowards(Vector3dc, Vector3dc, Matrix3d)
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
    Matrix3d rotateTowards(double dirX, double dirY, double dirZ, double upX, double upY, double upZ, Matrix3d dest);

    /**
     * Extract the Euler angles from the rotation represented by <code>this</code> matrix and store the extracted Euler angles in <code>dest</code>.
     * <p>
     * This method assumes that <code>this</code> matrix only represents a rotation without scaling.
     * <p>
     * Note that the returned Euler angles must be applied in the order <code>Z * Y * X</code> to obtain the identical matrix.
     * This means that calling {@link Matrix3dc#rotateZYX(double, double, double, Matrix3d)} using the obtained Euler angles will yield
     * the same rotation as the original matrix from which the Euler angles were obtained, so in the below code the matrix
     * <code>m2</code> should be identical to <code>m</code> (disregarding possible floating-point inaccuracies).
     * <pre>
     * Matrix3d m = ...; // &lt;- matrix only representing rotation
     * Matrix3d n = new Matrix3d();
     * n.rotateZYX(m.getEulerAnglesZYX(new Vector3d()));
     * </pre>
     * <p>
     * Reference: <a href="http://nghiaho.com/?page_id=846">http://nghiaho.com/</a>
     * 
     * @param dest
     *          will hold the extracted Euler angles
     * @return dest
     */
    Vector3d getEulerAnglesZYX(Vector3d dest);

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
    Matrix3d obliqueZ(double a, double b, Matrix3d dest);

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
    boolean equals(Matrix3dc m, double delta);

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through the given plane
     * specified via the plane normal <code>(nx, ny, nz)</code>, and store the result in <code>dest</code>.
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
     * @param dest
     *          will hold the result
     * @return this
     */
    Matrix3d reflect(double nx, double ny, double nz, Matrix3d dest);

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through a plane
     * specified via the plane orientation, and store the result in <code>dest</code>.
     * <p>
     * This method can be used to build a reflection transformation based on the orientation of a mirror object in the scene.
     * It is assumed that the default mirror plane's normal is <code>(0, 0, 1)</code>. So, if the given {@link Quaterniondc} is
     * the identity (does not apply any additional rotation), the reflection plane will be <code>z=0</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param orientation
     *          the plane orientation
     * @param dest
     *          will hold the result
     * @return this
     */
    Matrix3d reflect(Quaterniondc orientation, Matrix3d dest);

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through the given plane
     * specified via the plane normal, and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>R</code> the reflection matrix,
     * then the new matrix will be <code>M * R</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * R * v</code>, the
     * reflection will be applied first!
     * 
     * @param normal
     *          the plane normal
     * @param dest
     *          will hold the result
     * @return this
     */
    Matrix3d reflect(Vector3dc normal, Matrix3d dest);

    /**
     * Determine whether all matrix elements are finite floating-point values, that
     * is, they are not {@link Double#isNaN() NaN} and not
     * {@link Double#isInfinite() infinity}.
     *
     * @return {@code true} if all components are finite floating-point values;
     *         {@code false} otherwise
     */
    boolean isFinite();

    /**
     * Compute <code>(x, y, z)^T * this * (x, y, z)</code>.
     * 
     * @param x
     *          the x coordinate of the vector to multiply
     * @param y
     *          the y coordinate of the vector to multiply
     * @param z
     *          the z coordinate of the vector to multiply
     * @return the result
     */
    double quadraticFormProduct(double x, double y, double z);

    /**
     * Compute <code>v^T * this * v</code>.
     * 
     * @param v
     *          the vector to multiply
     * @return the result
     */
    double quadraticFormProduct(Vector3dc v);

    /**
     * Compute <code>v^T * this * v</code>.
     * 
     * @param v
     *          the vector to multiply
     * @return the result
     */
    double quadraticFormProduct(Vector3fc v);

}
