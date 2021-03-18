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
 * Interface to a read-only view of a 3-dimensional vector of single-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector3fc {

    /**
     * @return the value of the x component
     */
    float x();

    /**
     * @return the value of the y component
     */
    float y();

    /**
     * @return the value of the z component
     */
    float z();


    /**
     * Store this vector into the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the vector is stored, use {@link #get(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     * 
     * @see #get(int, FloatBuffer)
     * 
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z</code> order
     * @return the passed in buffer
     * @see #get(int, FloatBuffer)
     */
    FloatBuffer get(FloatBuffer buffer);

    /**
     * Store this vector into the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
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
     * 
     * @see #get(int, ByteBuffer)
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
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f sub(Vector3fc v, Vector3f dest);

    /**
     * Decrement the components of this vector by the given values and store the result in <code>dest</code>.
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
    Vector3f sub(float x, float y, float z, Vector3f dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f add(Vector3fc v, Vector3f dest);

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
    Vector3f add(float x, float y, float z, Vector3f dest);

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
    Vector3f fma(Vector3fc a, Vector3fc b, Vector3f dest);

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
    Vector3f fma(float a, Vector3fc b, Vector3f dest);

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
    Vector3f mulAdd(Vector3fc a, Vector3fc b, Vector3f dest);

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
    Vector3f mulAdd(float a, Vector3fc b, Vector3f dest);

    /**
     * Multiply this Vector3f component-wise by another Vector3f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mul(Vector3fc v, Vector3f dest);

    /**
     * Divide this Vector3f component-wise by another Vector3f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f div(Vector3fc v, Vector3f dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3f, perform perspective division
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
    Vector3f mulProject(Matrix4fc mat, Vector3f dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3f, perform perspective division
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
    Vector3f mulProject(Matrix4fc mat, float w, Vector3f dest);

    /**
     * Multiply the given matrix with this Vector3f and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mul(Matrix3fc mat, Vector3f dest);

    /**
     * Multiply the given matrix with this Vector3f and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mul(Matrix3dc mat, Vector3f dest);

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
    Vector3f mul(Matrix3x2fc mat, Vector3f dest);

    /**
     * Multiply the transpose of the given matrix with this Vector3f and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mulTranspose(Matrix3fc mat, Vector3f dest);

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
    Vector3f mulPosition(Matrix4fc mat, Vector3f dest);

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
    Vector3f mulPosition(Matrix4x3fc mat, Vector3f dest);

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
    Vector3f mulTransposePosition(Matrix4fc mat, Vector3f dest);

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
    float mulPositionW(Matrix4fc mat, Vector3f dest);

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
    Vector3f mulDirection(Matrix4dc mat, Vector3f dest);

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
    Vector3f mulDirection(Matrix4fc mat, Vector3f dest);

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
    Vector3f mulDirection(Matrix4x3fc mat, Vector3f dest);

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
    Vector3f mulTransposeDirection(Matrix4fc mat, Vector3f dest);

    /**
     * Multiply all components of this {@link Vector3f} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mul(float scalar, Vector3f dest);

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
    Vector3f mul(float x, float y, float z, Vector3f dest);

    /**
     * Divide all components of this {@link Vector3f} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f div(float scalar, Vector3f dest);

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
    Vector3f div(float x, float y, float z, Vector3f dest);

    /**
     * Rotate this vector by the given quaternion <code>quat</code> and store the result in <code>dest</code>.
     * 
     * @see Quaternionfc#transform(Vector3f)
     * 
     * @param quat
     *          the quaternion to rotate this vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f rotate(Quaternionfc quat, Vector3f dest);

    /**
     * Compute the quaternion representing a rotation of <code>this</code> vector to point along <code>toDir</code>
     * and store the result in <code>dest</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * 
     * @see Quaternionf#rotationTo(Vector3fc, Vector3fc)
     * 
     * @param toDir
     *          the destination direction
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaternionf rotationTo(Vector3fc toDir, Quaternionf dest);

    /**
     * Compute the quaternion representing a rotation of <code>this</code> vector to point along <code>(toDirX, toDirY, toDirZ)</code>
     * and store the result in <code>dest</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * 
     * @see Quaternionf#rotationTo(float, float, float, float, float, float)
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
    Quaternionf rotationTo(float toDirX, float toDirY, float toDirZ, Quaternionf dest);

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
    Vector3f rotateAxis(float angle, float aX, float aY, float aZ, Vector3f dest);

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
    Vector3f rotateX(float angle, Vector3f dest);

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
    Vector3f rotateY(float angle, Vector3f dest);

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
    Vector3f rotateZ(float angle, Vector3f dest);

    /**
     * Return the length squared of this vector.
     *
     * @return the length squared
     */
    float lengthSquared();

    /**
     * Return the length of this vector.
     *
     * @return the length
     */
    float length();

    /**
     * Normalize this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f normalize(Vector3f dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f normalize(float length, Vector3f dest);

    /**
     * Compute the cross product of this vector and <code>v</code> and store the result in <code>dest</code>.
     * 
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f cross(Vector3fc v, Vector3f dest);

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
    Vector3f cross(float x, float y, float z, Vector3f dest);

    /**
     * Return the distance between this Vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the distance
     */
    float distance(Vector3fc v);

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
    float distance(float x, float y, float z);

    /**
     * Return the square of the distance between this vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the squared of the distance
     */
    float distanceSquared(Vector3fc v);

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
    float distanceSquared(float x, float y, float z);

    /**
     * Return the dot product of this vector and the supplied vector.
     * 
     * @param v
     *          the other vector
     * @return the dot product
     */
    float dot(Vector3fc v);

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
    float dot(float x, float y, float z);

    /**
     * Return the cosine of the angle between this vector and the supplied vector. Use this instead of Math.cos(this.angle(v)).
     * 
     * @see #angle(Vector3fc)
     * 
     * @param v
     *          the other vector
     * @return the cosine of the angle
     */
    float angleCos(Vector3fc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @see #angleCos(Vector3fc)
     * 
     * @param v
     *          the other vector
     * @return the angle, in radians
     */
    float angle(Vector3fc v);

    /**
     * Return the signed angle between this vector and the supplied vector with
     * respect to the plane with the given normal vector <code>n</code>.
     * 
     * @see #angleCos(Vector3fc)
     * 
     * @param v
     *          the other vector
     * @param n
     *          the plane's normal vector
     * @return the angle, in radians
     */
    float angleSigned(Vector3fc v, Vector3fc n);

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
    float angleSigned(float x, float y, float z, float nx, float ny, float nz);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f min(Vector3fc v, Vector3f dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f max(Vector3fc v, Vector3f dest);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f negate(Vector3f dest);

    /**
     * Compute the absolute values of the individual components of <code>this</code> and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f absolute(Vector3f dest);

    /**
     * Reflect this vector about the given <code>normal</code> vector and store the result in <code>dest</code>.
     * 
     * @param normal
     *          the vector to reflect about
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f reflect(Vector3fc normal, Vector3f dest);

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
    Vector3f reflect(float x, float y, float z, Vector3f dest);

    /**
     * Compute the half vector between this and the other vector and store the result in <code>dest</code>.
     * 
     * @param other
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f half(Vector3fc other, Vector3f dest);

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
    Vector3f half(float x, float y, float z, Vector3f dest);

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
    Vector3f smoothStep(Vector3fc v, float t, Vector3f dest);

    /**
     * Compute a hermite interpolation between <code>this</code> vector with its
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
    Vector3f hermite(Vector3fc t0, Vector3fc v1, Vector3fc t1, float t, Vector3f dest);

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
    Vector3f lerp(Vector3fc other, float t, Vector3f dest);

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..2]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..2]</code>
     */
    float get(int component) throws IllegalArgumentException;

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
    Vector3f orthogonalize(Vector3fc v, Vector3f dest);

    /**
     * Transform <code>this</code> vector so that it is orthogonal to the given unit vector <code>v</code>, normalize the result and store it into <code>dest</code>.
     * <p>
     * The vector <code>v</code> is assumed to be a {@link #normalize(Vector3f) unit} vector.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Gram%E2%80%93Schmidt_process">Gram–Schmidt process</a>
     * 
     * @param v
     *          the reference unit vector which the result should be orthogonal to
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f orthogonalizeUnit(Vector3fc v, Vector3f dest);

    /**
     * Compute for each component of this vector the largest (closest to positive
     * infinity) {@code float} value that is less than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f floor(Vector3f dest);

    /**
     * Compute for each component of this vector the smallest (closest to negative
     * infinity) {@code float} value that is greater than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f ceil(Vector3f dest);

    /**
     * Compute for each component of this vector the closest float that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f round(Vector3f dest);

    /**
     * Determine whether all components are finite floating-point values, that
     * is, they are not {@link Float#isNaN() NaN} and not
     * {@link Float#isInfinite() infinity}.
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
    boolean equals(Vector3fc v, float delta);

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
    boolean equals(float x, float y, float z);

}
