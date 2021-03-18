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
 * Interface to a read-only view of a 4-dimensional vector of single-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector4fc {

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
     * @return the value of the w component
     */
    float w();


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
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
     * @return the passed in buffer
     */
    ByteBuffer get(int index, ByteBuffer buffer);


    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract from <code>this</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f sub(Vector4fc v, Vector4f dest);

    /**
     * Subtract <code>(x, y, z, w)</code> from this and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param z
     *          the z component to subtract
     * @param w
     *          the w component to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f sub(float x, float y, float z, float w, Vector4f dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f add(Vector4fc v, Vector4f dest);

    /**
     * Increment the components of this vector by the given values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param z
     *          the z component to add
     * @param w
     *          the w component to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f add(float x, float y, float z, float w, Vector4f dest);

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
    Vector4f fma(Vector4fc a, Vector4fc b, Vector4f dest);

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
    Vector4f fma(float a, Vector4fc b, Vector4f dest);

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
    Vector4f mulAdd(Vector4fc a, Vector4fc b, Vector4f dest);

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
    Vector4f mulAdd(float a, Vector4fc b, Vector4f dest);

    /**
     * Multiply this Vector4f component-wise by another Vector4f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f mul(Vector4fc v, Vector4f dest);

    /**
     * Divide this Vector4f component-wise by another Vector4f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f div(Vector4fc v, Vector4f dest);

    /**
     * Multiply the given matrix mat with this Vector4f and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4f mul(Matrix4fc mat, Vector4f dest);

    /**
     * Multiply the transpose of the given matrix <code>mat</code> with this Vector4f and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4f mulTranspose(Matrix4fc mat, Vector4f dest);

    /**
     * Multiply the given affine matrix mat with this Vector4f and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the affine matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4f mulAffine(Matrix4fc mat, Vector4f dest);

    /**
     * Multiply the transpose of the given affine matrix <code>mat</code> with this Vector4f and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the affine matrix whose transpose to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4f mulAffineTranspose(Matrix4fc mat, Vector4f dest);

    /**
     * Multiply the given matrix mat with this Vector4f and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4f mul(Matrix4x3fc mat, Vector4f dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector4f, perform perspective division
     * and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f mulProject(Matrix4fc mat, Vector4f dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector4f, perform perspective division
     * and store the <code>(x, y, z)</code> result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f mulProject(Matrix4fc mat, Vector3f dest);

    /**
     * Multiply all components of this {@link Vector4f} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f mul(float scalar, Vector4f dest);

    /**
     * Multiply the components of this Vector4f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to multiply by
     * @param y
     *          the y component to multiply by
     * @param z
     *          the z component to multiply by
     * @param w
     *          the w component to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f mul(float x, float y, float z, float w, Vector4f dest);

    /**
     * Divide all components of this {@link Vector4f} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f div(float scalar, Vector4f dest);

    /**
     * Divide the components of this Vector4f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to divide by
     * @param y
     *          the y component to divide by
     * @param z
     *          the z component to divide by
     * @param w
     *          the w component to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f div(float x, float y, float z, float w, Vector4f dest);

    /**
     * Rotate this vector by the given quaternion <code>quat</code> and store the result in <code>dest</code>.
     * 
     * @see Quaternionf#transform(Vector4f)
     * 
     * @param quat
     *          the quaternion to rotate this vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f rotate(Quaternionfc quat, Vector4f dest);

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
    Vector4f rotateAxis(float angle, float aX, float aY, float aZ, Vector4f dest);

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
    Vector4f rotateX(float angle, Vector4f dest);

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
    Vector4f rotateY(float angle, Vector4f dest);

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
    Vector4f rotateZ(float angle, Vector4f dest);

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
     * Normalizes this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f normalize(Vector4f dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f normalize(float length, Vector4f dest);

    /**
     * Normalize this vector by computing only the norm of <code>(x, y, z)</code> and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f normalize3(Vector4f dest);

    /**
     * Return the distance between this Vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the distance
     */
    float distance(Vector4fc v);

    /**
     * Return the distance between <code>this</code> vector and <code>(x, y, z, w)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @param w
     *          the w component of the other vector
     * @return the euclidean distance
     */
    float distance(float x, float y, float z, float w);

    /**
     * Return the square of the distance between this vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the squared of the distance
     */
    float distanceSquared(Vector4fc v);

    /**
     * Return the square of the distance between <code>this</code> vector and
     * <code>(x, y, z, w)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @param w
     *          the w component of the other vector
     * @return the square of the distance
     */
    float distanceSquared(float x, float y, float z, float w);

    /**
     * Compute the dot product (inner product) of this vector and <code>v</code>
     * .
     * 
     * @param v
     *          the other vector
     * @return the dot product
     */
    float dot(Vector4fc v);

    /**
     * Compute the dot product (inner product) of this vector and <code>(x, y, z, w)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @param w
     *          the w component of the other vector
     * @return the dot product
     */
    float dot(float x, float y, float z, float w);

    /**
     * Return the cosine of the angle between this vector and the supplied vector. Use this instead of <code>Math.cos(angle(v))</code>.
     * 
     * @see #angle(Vector4fc)
     * 
     * @param v
     *          the other vector
     * @return the cosine of the angle
     */
    float angleCos(Vector4fc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @see #angleCos(Vector4fc)
     * 
     * @param v
     *          the other vector
     * @return the angle, in radians
     */
    float angle(Vector4fc v);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f negate(Vector4f dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f min(Vector4fc v, Vector4f dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f max(Vector4fc v, Vector4f dest);

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
    Vector4f lerp(Vector4fc other, float t, Vector4f dest);

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
    Vector4f smoothStep(Vector4fc v, float t, Vector4f dest);
    
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
    Vector4f hermite(Vector4fc t0, Vector4fc v1, Vector4fc t1, float t, Vector4f dest);

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..3]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..3]</code>
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
    Vector4i get(int mode, Vector4i dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f get(Vector4f dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d get(Vector4d dest);

    /**
     * Determine the component with the biggest absolute value.
     * 
     * @return the component index, within <code>[0..3]</code>
     */
    int maxComponent();

    /**
     * Determine the component with the smallest (towards zero) absolute value.
     * 
     * @return the component index, within <code>[0..3]</code>
     */
    int minComponent();

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
    Vector4f floor(Vector4f dest);

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
    Vector4f ceil(Vector4f dest);

    /**
     * Compute for each component of this vector the closest float that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f round(Vector4f dest);

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
     * Compute the absolute of each of this vector's components
     * and store the result into <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f absolute(Vector4f dest);

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
    boolean equals(Vector4fc v, float delta);

    /**
     * Compare the vector components of <code>this</code> vector with the given <code>(x, y, z, w)</code>
     * and return whether all of them are equal.
     *
     * @param x
     *          the x component to compare to
     * @param y
     *          the y component to compare to
     * @param z
     *          the z component to compare to
     * @param w
     *          the w component to compare to
     * @return <code>true</code> if all the vector components are equal
     */
    boolean equals(float x, float y, float z, float w);

}
