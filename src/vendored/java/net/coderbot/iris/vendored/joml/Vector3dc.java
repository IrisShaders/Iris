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


import java.nio.*;

import java.util.*;

/**
 * Interface to a read-only view of a 3-dimensional vector of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector3dc {

    /**
     * @return the value of the x component
     */
    double x();

    /**
     * @return the value of the y component
     */
    double y();

    /**
     * @return the value of the z component
     */
    double z();


    /**
     * Store this vector into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is stored, use {@link #get(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     * @see #get(int, ByteBuffer)
     */
    ByteBuffer get(ByteBuffer buffer);

    /**
     * Store this vector into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     */
    ByteBuffer get(int index, ByteBuffer buffer);

    /**
     * Store this vector into the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the vector is stored, use {@link #get(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     * @see #get(int, DoubleBuffer)
     */
    DoubleBuffer get(DoubleBuffer buffer);

    /**
     * Store this vector into the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     *
     * @param index
     *          the absolute position into the DoubleBuffer
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     */
    DoubleBuffer get(int index, DoubleBuffer buffer);

    /**
     * Store this vector into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the vector is stored, use {@link #get(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * <p>
     * Please note that due to this vector storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given FloatBuffer.
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     * @see #get(int, DoubleBuffer)
     */
    FloatBuffer get(FloatBuffer buffer);

    /**
     * Store this vector into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * Please note that due to this vector storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given FloatBuffer.
     *
     * @param index
     *          the absolute position into the FloatBuffer
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     */
    FloatBuffer get(int index, FloatBuffer buffer);

    /**
     * Store this vector into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is stored, use {@link #get(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     * <p>
     * Please note that due to this vector storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given ByteBuffer.
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     * @see #get(int, ByteBuffer)
     */
    ByteBuffer getf(ByteBuffer buffer);

    /**
     * Store this vector into the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * Please note that due to this vector storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given ByteBuffer.
     *
     * @param index
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     */
    ByteBuffer getf(int index, ByteBuffer buffer);


    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract from <code>this</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d sub(Vector3dc v, Vector3d dest);

    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract from <code>this</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d sub(Vector3fc v, Vector3d dest);

    /**
     * Subtract <code>(x, y, z)</code> from this vector and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param z
     *          the z component to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d sub(double x, double y, double z, Vector3d dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d add(Vector3dc v, Vector3d dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d add(Vector3fc v, Vector3d dest);

    /**
     * Increment the components of this vector by the given values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param z
     *          the z component to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d add(double x, double y, double z, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d fma(Vector3dc a, Vector3dc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d fma(double a, Vector3dc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d fma(Vector3dc a, Vector3fc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d fma(Vector3fc a, Vector3fc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d fma(double a, Vector3fc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>this * a</code> to <code>b</code>
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the multiplicand
     * @param b
     *          the addend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulAdd(Vector3dc a, Vector3dc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>this * a</code> to <code>b</code>
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the multiplicand
     * @param b
     *          the addend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulAdd(double a, Vector3dc b, Vector3d dest);

    /**
     * Add the component-wise multiplication of <code>this * a</code> to <code>b</code>
     * and store the result in <code>dest</code>.
     * 
     * @param a
     *          the multiplicand
     * @param b
     *          the addend
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulAdd(Vector3fc a, Vector3dc b, Vector3d dest);

    /**
     * Multiply this Vector3d component-wise by another Vector3f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to multiply by
     * @param dest
     *             will hold the result
     * @return dest
     */
    Vector3d mul(Vector3fc v, Vector3d dest);

    /**
     * Multiply this by <code>v</code> component-wise and store the result into <code>dest</code>.
     * 
     * @param v
     *          the vector to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(Vector3dc v, Vector3d dest);

    /**
     * Divide this Vector3d component-wise by another Vector3f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d div(Vector3fc v, Vector3d dest);

    /**
     * Divide this by <code>v</code> component-wise and store the result into <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d div(Vector3dc v, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3d, perform perspective division
     * and store the result in <code>dest</code>.
     * <p>
     * This method uses the given <code>w</code> as the fourth vector component.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param w
     *          the w component to use
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulProject(Matrix4dc mat, double w, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3d, perform perspective division
     * and store the result in <code>dest</code>.
     * <p>
     * This method uses <code>w=1.0</code> as the fourth vector component.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulProject(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3d, perform perspective division
     * and store the result in <code>dest</code>.
     * <p>
     * This method uses <code>w=1.0</code> as the fourth vector component.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulProject(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(Matrix3dc mat, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mul(Matrix3dc mat, Vector3f dest);

    /**
     * Multiply the given matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(Matrix3fc mat, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with <code>this</code> by assuming a
     * third row in the matrix of <code>(0, 0, 1)</code> and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(Matrix3x2dc mat, Vector3d dest);

    /**
     * Multiply the given matrix <code>mat</code> with <code>this</code> by assuming a
     * third row in the matrix of <code>(0, 0, 1)</code> and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(Matrix3x2fc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given matrix with this Vector3f and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTranspose(Matrix3dc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given matrix with this Vector3f and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTranspose(Matrix3fc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulPosition(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulPosition(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulPosition(Matrix4x3dc mat, Vector3d dest);

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulPosition(Matrix4x3fc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTransposePosition(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTransposePosition(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code>, store the
     * result in <code>dest</code> and return the <i>w</i> component of the resulting 4D vector.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the <code>(x, y, z)</code> components of the resulting vector
     * @return the <i>w</i> component of the resulting 4D vector after multiplication
     */
    double mulPositionW(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code>, store the
     * result in <code>dest</code> and return the <i>w</i> component of the resulting 4D vector.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the <code>(x, y, z)</code> components of the resulting vector
     * @return the <i>w</i> component of the resulting 4D vector after multiplication
     */
    double mulPositionW(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulDirection(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulDirection(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulDirection(Matrix4x3dc mat, Vector3d dest);

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulDirection(Matrix4x3fc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTransposeDirection(Matrix4dc mat, Vector3d dest);

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulTransposeDirection(Matrix4fc mat, Vector3d dest);

    /**
     * Multiply this Vector3d by the given scalar value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar factor
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(double scalar, Vector3d dest);

    /**
     * Multiply the components of this Vector3f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to multiply this vector by
     * @param y
     *          the y component to multiply this vector by
     * @param z
     *          the z component to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mul(double x, double y, double z, Vector3d dest);

    /**
     * Rotate this vector by the given quaternion <code>quat</code> and store the result in <code>dest</code>.
     * 
     * @see Quaterniond#transform(Vector3d)
     * 
     * @param quat
     *          the quaternion to rotate this vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d rotate(Quaterniondc quat, Vector3d dest);
    
    /**
     * Compute the quaternion representing a rotation of <code>this</code> vector to point along <code>toDir</code>
     * and store the result in <code>dest</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * 
     * @see Quaterniond#rotationTo(Vector3dc, Vector3dc)
     * 
     * @param toDir
     *          the destination direction
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond rotationTo(Vector3dc toDir, Quaterniond dest);

    /**
     * Compute the quaternion representing a rotation of <code>this</code> vector to point along <code>(toDirX, toDirY, toDirZ)</code>
     * and store the result in <code>dest</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * 
     * @see Quaterniond#rotationTo(double, double, double, double, double, double)
     * 
     * @param toDirX
     *          the x coordinate of the destination direction
     * @param toDirY
     *          the y coordinate of the destination direction
     * @param toDirZ
     *          the z coordinate of the destination direction
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond rotationTo(double toDirX, double toDirY, double toDirZ, Quaterniond dest);

    /**
     * Rotate this vector the specified radians around the given rotation axis and store the result
     * into <code>dest</code>.
     * 
     * @param angle
     *          the angle in radians
     * @param aX
     *          the x component of the rotation axis
     * @param aY
     *          the y component of the rotation axis
     * @param aZ
     *          the z component of the rotation axis
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d rotateAxis(double angle, double aX, double aY, double aZ, Vector3d dest);

    /**
     * Rotate this vector the specified radians around the X axis and store the result
     * into <code>dest</code>.
     * 
     * @param angle
     *          the angle in radians
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d rotateX(double angle, Vector3d dest);

    /**
     * Rotate this vector the specified radians around the Y axis and store the result
     * into <code>dest</code>.
     * 
     * @param angle
     *          the angle in radians
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d rotateY(double angle, Vector3d dest);

    /**
     * Rotate this vector the specified radians around the Z axis and store the result
     * into <code>dest</code>.
     * 
     * @param angle
     *          the angle in radians
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d rotateZ(double angle, Vector3d dest);

    /**
     * Divide this Vector3d by the given scalar value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to divide this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d div(double scalar, Vector3d dest);

    /**
     * Divide the components of this Vector3f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to divide this vector by
     * @param y
     *          the y component to divide this vector by
     * @param z
     *          the z component to divide this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d div(double x, double y, double z, Vector3d dest);

    /**
     * Return the length squared of this vector.
     *
     * @return the length squared
     */
    double lengthSquared();

    /**
     * Return the length of this vector.
     *
     * @return the length
     */
    double length();

    /**
     * Normalize this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d normalize(Vector3d dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d normalize(double length, Vector3d dest);

    /**
     * Calculate the cross product of this and v2 and store the result in <code>dest</code>.
     * 
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d cross(Vector3dc v, Vector3d dest);

    /**
     * Compute the cross product of this vector and <code>(x, y, z)</code> and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d cross(double x, double y, double z, Vector3d dest);

    /**
     * Return the distance between this vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the distance
     */
    double distance(Vector3dc v);

    /**
     * Return the distance between <code>this</code> vector and <code>(x, y, z)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @return the euclidean distance
     */
    double distance(double x, double y, double z);

    /**
     * Return the square of the distance between this vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the squared of the distance
     */
    double distanceSquared(Vector3dc v);

    /**
     * Return the square of the distance between <code>this</code> vector and <code>(x, y, z)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @return the square of the distance
     */
    double distanceSquared(double x, double y, double z);

    /**
     * Return the dot product of this vector and the supplied vector.
     * 
     * @param v
     *          the other vector
     * @return the dot product
     */
    double dot(Vector3dc v);

    /**
     * Return the dot product of this vector and the vector <code>(x, y, z)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @return the dot product
     */
    double dot(double x, double y, double z);

    /**
     * Return the cosine of the angle between <code>this</code> vector and
     * the supplied vector. Use this instead of <code>Math.cos(angle(v))</code>.
     * 
     * @see #angle(Vector3dc)
     * 
     * @param v
     *          the other vector
     * @return the cosine of the angle
     */
    double angleCos(Vector3dc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @see #angleCos(Vector3dc)
     * 
     * @param v
     *          the other vector
     * @return the angle, in radians
     */
    double angle(Vector3dc v);

    /**
     * Return the signed angle between this vector and the supplied vector with
     * respect to the plane with the given normal vector <code>n</code>.
     * 
     * @see #angleCos(Vector3dc)
     * 
     * @param v
     *          the other vector
     * @param n
     *          the plane's normal vector
     * @return the angle, in radians
     */
    double angleSigned(Vector3dc v, Vector3dc n);

    /**
     * Return the signed angle between this vector and the supplied vector with
     * respect to the plane with the given normal vector <code>(nx, ny, nz)</code>.
     * 
     * @param x
     *          the x coordinate of the other vector
     * @param y
     *          the y coordinate of the other vector
     * @param z
     *          the z coordinate of the other vector
     * @param nx
     *          the x coordinate of the plane's normal vector
     * @param ny
     *          the y coordinate of the plane's normal vector
     * @param nz
     *          the z coordinate of the plane's normal vector
     * @return the angle, in radians
     */
    double angleSigned(double x, double y, double z, double nx, double ny, double nz);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d min(Vector3dc v, Vector3d dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d max(Vector3dc v, Vector3d dest);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d negate(Vector3d dest);

    /**
     * Compute the absolute values of the individual components of <code>this</code> and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d absolute(Vector3d dest);

    /**
     * Reflect this vector about the given normal vector and store the result in <code>dest</code>.
     * 
     * @param normal
     *          the vector to reflect about
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d reflect(Vector3dc normal, Vector3d dest);

    /**
     * Reflect this vector about the given normal vector and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component of the normal
     * @param y
     *          the y component of the normal
     * @param z
     *          the z component of the normal
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d reflect(double x, double y, double z, Vector3d dest);

    /**
     * Compute the half vector between this and the other vector and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d half(Vector3dc other, Vector3d dest);

    /**
     * Compute the half vector between this and the vector <code>(x, y, z)</code> 
     * and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d half(double x, double y, double z, Vector3d dest);

    /**
     * Compute a smooth-step (i.e. hermite with zero tangents) interpolation
     * between <code>this</code> vector and the given vector <code>v</code> and
     * store the result in <code>dest</code>.
     * 
     * @param v
     *          the other vector
     * @param t
     *          the interpolation factor, within <code>[0..1]</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d smoothStep(Vector3dc v, double t, Vector3d dest);

    /**
     * Compute a hermite interpolation between <code>this</code> vector and its
     * associated tangent <code>t0</code> and the given vector <code>v</code>
     * with its tangent <code>t1</code> and store the result in
     * <code>dest</code>.
     * 
     * @param t0
     *          the tangent of <code>this</code> vector
     * @param v1
     *          the other vector
     * @param t1
     *          the tangent of the other vector
     * @param t
     *          the interpolation factor, within <code>[0..1]</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d hermite(Vector3dc t0, Vector3dc v1, Vector3dc t1, double t, Vector3d dest);

    /**
     * Linearly interpolate <code>this</code> and <code>other</code> using the given interpolation factor <code>t</code>
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>t</code> is <code>0.0</code> then the result is <code>this</code>. If the interpolation factor is <code>1.0</code>
     * then the result is <code>other</code>.
     * 
     * @param other
     *          the other vector
     * @param t
     *          the interpolation factor between 0.0 and 1.0
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d lerp(Vector3dc other, double t, Vector3d dest);

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..2]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..2]</code>
     */
    double get(int component) throws IllegalArgumentException;

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector
     * using the given {@link RoundingMode}.
     *
     * @param mode
     *          the {@link RoundingMode} to use
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3i get(int mode, Vector3i dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f get(Vector3f dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d get(Vector3d dest);

    /**
     * Determine the component with the biggest absolute value.
     * 
     * @return the component index, within <code>[0..2]</code>
     */
    int maxComponent();

    /**
     * Determine the component with the smallest (towards zero) absolute value.
     * 
     * @return the component index, within <code>[0..2]</code>
     */
    int minComponent();

    /**
     * Transform <code>this</code> vector so that it is orthogonal to the given vector <code>v</code>, normalize the result and store it into <code>dest</code>.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Gram%E2%80%93Schmidt_process">Gram–Schmidt process</a>
     * 
     * @param v
     *          the reference vector which the result should be orthogonal to
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d orthogonalize(Vector3dc v, Vector3d dest);

    /**
     * Transform <code>this</code> vector so that it is orthogonal to the given unit vector <code>v</code>, normalize the result and store it into <code>dest</code>.
     * <p>
     * The vector <code>v</code> is assumed to be a {@link #normalize(Vector3d) unit} vector.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Gram%E2%80%93Schmidt_process">Gram–Schmidt process</a>
     * 
     * @param v
     *          the reference unit vector which the result should be orthogonal to
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d orthogonalizeUnit(Vector3dc v, Vector3d dest);

    /**
     * Compute for each component of this vector the largest (closest to positive
     * infinity) {@code double} value that is less than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d floor(Vector3d dest);

    /**
     * Compute for each component of this vector the smallest (closest to negative
     * infinity) {@code double} value that is greater than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d ceil(Vector3d dest);

    /**
     * Compute for each component of this vector the closest double that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d round(Vector3d dest);

    /**
     * Determine whether all components are finite floating-point values, that
     * is, they are not {@link Double#isNaN() NaN} and not
     * {@link Double#isInfinite() infinity}.
     *
     * @return {@code true} if all components are finite floating-point values;
     *         {@code false} otherwise
     */
    boolean isFinite();

    /**
     * Compare the vector components of <code>this</code> vector with the given vector using the given <code>delta</code>
     * and return whether all of them are equal within a maximum difference of <code>delta</code>.
     * <p>
     * Please note that this method is not used by any data structure such as {@link ArrayList} {@link HashSet} or {@link HashMap}
     * and their operations, such as {@link ArrayList#contains(Object)} or {@link HashSet#remove(Object)}, since those
     * data structures only use the {@link Object#equals(Object)} and {@link Object#hashCode()} methods.
     * 
     * @param v
     *          the other vector
     * @param delta
     *          the allowed maximum difference
     * @return <code>true</code> whether all of the vector components are equal; <code>false</code> otherwise
     */
    boolean equals(Vector3dc v, double delta);

    /**
     * Compare the vector components of <code>this</code> vector with the given <code>(x, y, z)</code>
     * and return whether all of them are equal.
     *
     * @param x
     *          the x component to compare to
     * @param y
     *          the y component to compare to
     * @param z
     *          the z component to compare to
     * @return <code>true</code> if all the vector components are equal
     */
    boolean equals(double x, double y, double z);

}
