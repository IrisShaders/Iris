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
 * Interface to a read-only view of a 2-dimensional vector of single-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector2fc {

    /**
     * @return the value of the x component
     */
    float x();

    /**
     * @return the value of the y component
     */
    float y();


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
     *        will receive the values of this vector in <code>x, y</code> order
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
     *        the absolute position into the ByteBuffer
     * @param buffer
     *        will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    ByteBuffer get(int index, ByteBuffer buffer);

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
     *        will receive the values of this vector in <code>x, y</code> order
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
     *        the absolute position into the FloatBuffer
     * @param buffer
     *        will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    FloatBuffer get(int index, FloatBuffer buffer);


    /**
     * Subtract <code>v</code> from <code>this</code> vector and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f sub(Vector2fc v, Vector2f dest);

    /**
     * Subtract <code>(x, y)</code> from this vector and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param dest
     *          will hold the result         
     * @return dest
     */
    Vector2f sub(float x, float y, Vector2f dest);

    /**
     * Return the dot product of this vector and <code>v</code>.
     * 
     * @param v
     *        the other vector
     * @return the dot product
     */
    float dot(Vector2fc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @param v
     *        the other vector
     * @return the angle, in radians
     */
    float angle(Vector2fc v);

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
     * Return the distance between this and <code>v</code>.
     * 
     * @param v
     *        the other vector
     * @return the distance
     */
    float distance(Vector2fc v);

    /**
     * Return the distance squared between this and <code>v</code>.
     * 
     * @param v
     *        the other vector
     * @return the distance squared
     */
    float distanceSquared(Vector2fc v);

    /**
     * Return the distance between <code>this</code> vector and <code>(x, y)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the euclidean distance
     */
    float distance(float x, float y);

    /**
     * Return the distance squared between <code>this</code> vector and <code>(x, y)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the euclidean distance squared
     */
    float distanceSquared(float x, float y);

    /**
     * Normalize this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *        will hold the result
     * @return dest
     */
    Vector2f normalize(Vector2f dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f normalize(float length, Vector2f dest);

    /**
     * Add the supplied vector to this one and store the result in
     * <code>dest</code>.
     *
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f add(Vector2fc v, Vector2f dest);

    /**
     * Increment the components of this vector by the given values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f add(float x, float y, Vector2f dest);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *        will hold the result
     * @return dest
     */
    Vector2f negate(Vector2f dest);

    /**
     * Multiply the components of this vector by the given scalar and store the result in <code>dest</code>.
     * 
     * @param scalar
     *        the value to multiply this vector's components by
     * @param dest
     *        will hold the result
     * @return dest
     */
    Vector2f mul(float scalar, Vector2f dest);

    /**
     * Multiply the components of this Vector2f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to multiply this vector by
     * @param y
     *          the y component to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mul(float x, float y, Vector2f dest);

    /**
     * Multiply this Vector2f component-wise by another Vector2f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mul(Vector2fc v, Vector2f dest);

    /**
     * Divide all components of this {@link Vector2f} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f div(float scalar, Vector2f dest);
    
    /**
     * Divide this Vector2f component-wise by another Vector2fc
     * and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f div(Vector2fc v, Vector2f dest);

    /**
     * Divide the components of this Vector2f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to divide this vector by
     * @param y
     *          the y component to divide this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f div(float x, float y, Vector2f dest);

    /**
     * Multiply the given matrix with this Vector2f and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mul(Matrix2fc mat, Vector2f dest);

    /**
     * Multiply the given matrix with this Vector2f and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mul(Matrix2dc mat, Vector2f dest);

    /**
     * Multiply the transpose of the given matrix with this Vector3f and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mulTranspose(Matrix2fc mat, Vector2f dest);

    /**
     * Multiply the given 3x2 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>z</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mulPosition(Matrix3x2fc mat, Vector2f dest);

    /**
     * Multiply the given 3x2 matrix <code>mat</code> with <code>this</code> and store the
     * result in <code>dest</code>.
     * <p>
     * This method assumes the <code>z</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f mulDirection(Matrix3x2fc mat, Vector2f dest);

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
    Vector2f lerp(Vector2fc other, float t, Vector2f dest);

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
    Vector2f fma(Vector2fc a, Vector2fc b, Vector2f dest);

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
    Vector2f fma(float a, Vector2fc b, Vector2f dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f min(Vector2fc v, Vector2f dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f max(Vector2fc v, Vector2f dest);

    /**
     * Determine the component with the biggest absolute value.
     * 
     * @return the component index, within <code>[0..1]</code>
     */
    int maxComponent();

    /**
     * Determine the component with the smallest (towards zero) absolute value.
     * 
     * @return the component index, within <code>[0..1]</code>
     */
    int minComponent();

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..1]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..1]</code>
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
    Vector2i get(int mode, Vector2i dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f get(Vector2f dest);

    /**
     * Set the components of the given vector <code>dest</code> to those of <code>this</code> vector.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d get(Vector2d dest);

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
    Vector2f floor(Vector2f dest);

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
    Vector2f ceil(Vector2f dest);

    /**
     * Compute for each component of this vector the closest float that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2f round(Vector2f dest);

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
    Vector2f absolute(Vector2f dest);

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
    boolean equals(Vector2fc v, float delta);

    /**
     * Compare the vector components of <code>this</code> vector with the given <code>(x, y)</code>
     * and return whether all of them are equal.
     *
     * @param x
     *          the x component to compare to
     * @param y
     *          the y component to compare to
     * @return <code>true</code> if all the vector components are equal
     */
    boolean equals(float x, float y);

}
