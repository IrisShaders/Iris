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
import java.nio.FloatBuffer;

import java.util.*;

/**
 * Interface to a read-only view of a 3x3 matrix of single-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Matrix3fc {

    /**
     * Return the value of the matrix element at column 0 and row 0.
     * 
     * @return the value of the matrix element
     */
    float m00();

    /**
     * Return the value of the matrix element at column 0 and row 1.
     * 
     * @return the value of the matrix element
     */
    float m01();

    /**
     * Return the value of the matrix element at column 0 and row 2.
     * 
     * @return the value of the matrix element
     */
    float m02();

    /**
     * Return the value of the matrix element at column 1 and row 0.
     * 
     * @return the value of the matrix element
     */
    float m10();

    /**
     * Return the value of the matrix element at column 1 and row 1.
     * 
     * @return the value of the matrix element
     */
    float m11();

    /**
     * Return the value of the matrix element at column 1 and row 2.
     * 
     * @return the value of the matrix element
     */
    float m12();

    /**
     * Return the value of the matrix element at column 2 and row 0.
     * 
     * @return the value of the matrix element
     */
    float m20();

    /**
     * Return the value of the matrix element at column 2 and row 1.
     * 
     * @return the value of the matrix element
     */
    float m21();

    /**
     * Return the value of the matrix element at column 2 and row 2.
     * 
     * @return the value of the matrix element
     */
    float m22();

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
    Matrix3f mul(Matrix3fc right, Matrix3f dest);

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
    Matrix3f mulLocal(Matrix3fc left, Matrix3f dest);

    /**
     * Return the determinant of this matrix.
     * 
     * @return the determinant
     */
    float determinant();

    /**
     * Invert the <code>this</code> matrix and store the result in <code>dest</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3f invert(Matrix3f dest);

    /**
     * Transpose <code>this</code> matrix and store the result in <code>dest</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3f transpose(Matrix3f dest);

    /**
     * Get the current values of <code>this</code> matrix and store them into
     * <code>dest</code>.
     * 
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix3f get(Matrix3f dest);

    /**
     * Get the current values of <code>this</code> matrix and store them as
     * the rotational component of <code>dest</code>. All other values of <code>dest</code> will
     * be set to identity.
     * 
     * @see Matrix4f#set(Matrix3fc)
     * 
     * @param dest
     *          the destination matrix
     * @return the passed in destination
     */
    Matrix4f get(Matrix4f dest);

    /**
     * Get the current values of <code>this</code> matrix and store the represented rotation
     * into the given {@link AxisAngle4f}.
     * 
     * @see AxisAngle4f#set(Matrix3fc)
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
     * @see Quaternionf#setFromUnnormalized(Matrix3fc)
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
     * @see Quaternionf#setFromNormalized(Matrix3fc)
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
     * @see Quaterniond#setFromUnnormalized(Matrix3fc)
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
     * @see Quaterniond#setFromNormalized(Matrix3fc)
     * 
     * @param dest
     *          the destination {@link Quaterniond}
     * @return the passed in destination
     */
    Quaterniond getNormalizedRotation(Quaterniond dest);


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
    FloatBuffer get(FloatBuffer buffer);

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
     * Store this matrix as 3x4 matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}, with the m03, m13 and m23 components being zero.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #get3x4(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get3x4(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this 3x3 matrix as 3x4 matrix in column-major order at its current position
     * @return the passed in buffer
     */
    FloatBuffer get3x4(FloatBuffer buffer);

    /**
     * Store this matrix as 3x4 matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index, with the m03, m13 and m23 components being zero.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * 
     * @param index
     *            the absolute position into the FloatBuffer
     * @param buffer
     *            will receive the values of this 3x3 matrix as 3x4 matrix in column-major order
     * @return the passed in buffer
     */
    FloatBuffer get3x4(int index, FloatBuffer buffer);

    /**
     * Store this matrix as 3x4 matrix in column-major order into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}, with the m03, m13 and m23 components being zero.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the matrix is stored, use {@link #get3x4(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get3x4(int, ByteBuffer)
     * 
     * @param buffer
     *            will receive the values of this 3x3 matrix as 3x4 matrix in column-major order at its current position
     * @return the passed in buffer
     */
    ByteBuffer get3x4(ByteBuffer buffer);

    /**
     * Store this matrix as 3x4 matrix in column-major order into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index, with the m03, m13 and m23 components being zero.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * 
     * @param index
     *            the absolute position into the ByteBuffer
     * @param buffer
     *            will receive the values of this 3x3 matrix as 3x4 matrix in column-major order
     * @return the passed in buffer
     */
    ByteBuffer get3x4(int index, ByteBuffer buffer);

    /**
     * Store the transpose of this matrix in column-major order into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the matrix is stored, use {@link #getTransposed(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #getTransposed(int, FloatBuffer)
     * 
     * @param buffer
     *            will receive the values of this matrix in column-major order at its current position
     * @return the passed in buffer
     */
    FloatBuffer getTransposed(FloatBuffer buffer);

    /**
     * Store the transpose of this matrix in column-major order into the supplied {@link FloatBuffer} starting at the specified
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
    FloatBuffer getTransposed(int index, FloatBuffer buffer);

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
     * Store this matrix into the supplied float array in column-major order at the given offset.
     * 
     * @param arr
     *          the array to write the matrix values into
     * @param offset
     *          the offset into the array
     * @return the passed in array
     */
    float[] get(float[] arr, int offset);

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
    Matrix3f scale(Vector3fc xyz, Matrix3f dest);

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
    Matrix3f scale(float x, float y, float z, Matrix3f dest);

    /**
     * Apply scaling to this matrix by uniformly scaling all base axes by the given <code>xyz</code> factor
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling matrix,
     * then the new matrix will be <code>M * S</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * S * v</code>
     * , the scaling will be applied first!
     * 
     * @see #scale(float, float, float, Matrix3f)
     * 
     * @param xyz
     *            the factor for all components
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3f scale(float xyz, Matrix3f dest);

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
    Matrix3f scaleLocal(float x, float y, float z, Matrix3f dest);

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
    Vector3f transform(float x, float y, float z, Vector3f dest);

    /**
     * Transform the given vector by the transpose of this matrix.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    Vector3f transformTranspose(Vector3f v);

    /**
     * Transform the given vector by the transpose of this matrix and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformTranspose(Vector3fc v, Vector3f dest);

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
    Vector3f transformTranspose(float x, float y, float z, Vector3f dest);

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
    Matrix3f rotateX(float ang, Matrix3f dest);

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
    Matrix3f rotateY(float ang, Matrix3f dest);

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
    Matrix3f rotateZ(float ang, Matrix3f dest);

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
    Matrix3f rotateXYZ(float angleX, float angleY, float angleZ, Matrix3f dest);

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
    Matrix3f rotateZYX(float angleZ, float angleY, float angleX, Matrix3f dest);

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
    Matrix3f rotateYXZ(float angleY, float angleX, float angleZ, Matrix3f dest);

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
    Matrix3f rotate(float ang, float x, float y, float z, Matrix3f dest);

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
    Matrix3f rotateLocal(float ang, float x, float y, float z, Matrix3f dest);

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
    Matrix3f rotateLocalX(float ang, Matrix3f dest);

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
    Matrix3f rotateLocalY(float ang, Matrix3f dest);

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
    Matrix3f rotateLocalZ(float ang, Matrix3f dest);

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
    Matrix3f rotate(Quaternionfc quat, Matrix3f dest);

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
    Matrix3f rotateLocal(Quaternionfc quat, Matrix3f dest);

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
     * @see #rotate(float, float, float, float, Matrix3f)
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f} (needs to be {@link AxisAngle4f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3f rotate(AxisAngle4f axisAngle, Matrix3f dest);

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
     * Reference: <a href="http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
     * 
     * @see #rotate(float, float, float, float, Matrix3f)
     * 
     * @param angle
     *          the angle in radians
     * @param axis
     *          the rotation axis (needs to be {@link Vector3f#normalize() normalized})
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3f rotate(float angle, Vector3fc axis, Matrix3f dest);

    /**
     * Apply a rotation transformation to this matrix to make <code>-z</code> point along <code>dir</code>
     * and store the result in <code>dest</code>. 
     * <p>
     * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookalong rotation matrix,
     * then the new matrix will be <code>M * L</code>. So when transforming a
     * vector <code>v</code> with the new matrix by using <code>M * L * v</code>, the
     * lookalong rotation transformation will be applied first!
     * 
     * @see #lookAlong(float, float, float, float, float, float, Matrix3f)
     * 
     * @param dir
     *            the direction in space to look along
     * @param up
     *            the direction of 'up'
     * @param dest
     *            will hold the result
     * @return dest
     */
    Matrix3f lookAlong(Vector3fc dir, Vector3fc up, Matrix3f dest);

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
    Matrix3f lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix3f dest);

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
    Vector3f getRow(int row, Vector3f dest) throws IndexOutOfBoundsException;

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
    Vector3f getColumn(int column, Vector3f dest) throws IndexOutOfBoundsException;

    /**
     * Get the matrix element value at the given column and row.
     * 
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @param row
     *          the row index in <code>[0..2]</code>
     * @return the element value
     */
    float get(int column, int row);

    /**
     * Get the matrix element value at the given row and column.
     * 
     * @param row
     *          the row index in <code>[0..2]</code>
     * @param column
     *          the colum index in <code>[0..2]</code>
     * @return the element value
     */
    float getRowColumn(int row, int column);

    /**
     * Compute a normal matrix from <code>this</code> matrix and store it into <code>dest</code>.
     * <p>
     * The normal matrix of <code>m</code> is the transpose of the inverse of <code>m</code>.
     * 
     * @param dest
     *             will hold the result
     * @return dest
     */
    Matrix3f normal(Matrix3f dest);

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
    Matrix3f cofactor(Matrix3f dest);

    /**
     * Get the scaling factors of <code>this</code> matrix for the three base axes.
     * 
     * @param dest
     *          will hold the scaling factors for <code>x</code>, <code>y</code> and <code>z</code>
     * @return dest
     */
    Vector3f getScale(Vector3f dest);

    /**
     * Obtain the direction of <code>+Z</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).invert();
     * inv.transform(dir.set(0, 0, 1)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveZ(Vector3f)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3f positiveZ(Vector3f dir);

    /**
     * Obtain the direction of <code>+Z</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).transpose();
     * inv.transform(dir.set(0, 0, 1));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3f normalizedPositiveZ(Vector3f dir);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).invert();
     * inv.transform(dir.set(1, 0, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveX(Vector3f)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3f positiveX(Vector3f dir);

    /**
     * Obtain the direction of <code>+X</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).transpose();
     * inv.transform(dir.set(1, 0, 0));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3f normalizedPositiveX(Vector3f dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> matrix is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).invert();
     * inv.transform(dir.set(0, 1, 0)).normalize();
     * </pre>
     * If <code>this</code> is already an orthogonal matrix, then consider using {@link #normalizedPositiveY(Vector3f)} instead.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3f positiveY(Vector3f dir);

    /**
     * Obtain the direction of <code>+Y</code> before the transformation represented by <code>this</code> <i>orthogonal</i> matrix is applied.
     * This method only produces correct results if <code>this</code> is an <i>orthogonal</i> matrix.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Matrix3f inv = new Matrix3f(this).transpose();
     * inv.transform(dir.set(0, 1, 0));
     * </pre>
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/threeD/">http://www.euclideanspace.com</a>
     * 
     * @param dir
     *          will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3f normalizedPositiveY(Vector3f dir);

    /**
     * Component-wise add <code>this</code> and <code>other</code> and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other addend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3f add(Matrix3fc other, Matrix3f dest);

    /**
     * Component-wise subtract <code>subtrahend</code> from <code>this</code> and store the result in <code>dest</code>.
     * 
     * @param subtrahend
     *          the subtrahend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3f sub(Matrix3fc subtrahend, Matrix3f dest);

    /**
     * Component-wise multiply <code>this</code> by <code>other</code> and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Matrix3f mulComponentWise(Matrix3fc other, Matrix3f dest);

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
    Matrix3f lerp(Matrix3fc other, float t, Matrix3f dest);

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
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(new Vector3f(dir).negate(), up).invert(), dest)</code>
     * 
     * @see #rotateTowards(float, float, float, float, float, float, Matrix3f)
     * 
     * @param direction
     *              the direction to rotate towards
     * @param up
     *              the model's up vector
     * @param dest
     *              will hold the result
     * @return dest
     */
    Matrix3f rotateTowards(Vector3fc direction, Vector3fc up, Matrix3f dest);

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
     * This method is equivalent to calling: <code>mul(new Matrix3f().lookAlong(-dirX, -dirY, -dirZ, upX, upY, upZ).invert(), dest)</code>
     * 
     * @see #rotateTowards(Vector3fc, Vector3fc, Matrix3f)
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
    Matrix3f rotateTowards(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Matrix3f dest);

    /**
     * Extract the Euler angles from the rotation represented by <code>this</code> matrix and store the extracted Euler angles in <code>dest</code>.
     * <p>
     * This method assumes that <code>this</code> matrix only represents a rotation without scaling.
     * <p>
     * Note that the returned Euler angles must be applied in the order <code>Z * Y * X</code> to obtain the identical matrix.
     * This means that calling {@link Matrix3fc#rotateZYX(float, float, float, Matrix3f)} using the obtained Euler angles will yield
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
    Vector3f getEulerAnglesZYX(Vector3f dest);

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
    Matrix3f obliqueZ(float a, float b, Matrix3f dest);

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
    boolean equals(Matrix3fc m, float delta);

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
    Matrix3f reflect(float nx, float ny, float nz, Matrix3f dest);

    /**
     * Apply a mirror/reflection transformation to this matrix that reflects through a plane
     * specified via the plane orientation, and store the result in <code>dest</code>.
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
     * @param dest
     *          will hold the result
     * @return this
     */
    Matrix3f reflect(Quaternionfc orientation, Matrix3f dest);

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
    Matrix3f reflect(Vector3fc normal, Matrix3f dest);

    /**
     * Determine whether all matrix elements are finite floating-point values, that
     * is, they are not {@link Float#isNaN() NaN} and not
     * {@link Float#isInfinite() infinity}.
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
    float quadraticFormProduct(float x, float y, float z);

    /**
     * Compute <code>v^T * this * v</code>.
     * 
     * @param v
     *          the vector to multiply
     * @return the result
     */
    float quadraticFormProduct(Vector3fc v);

}
