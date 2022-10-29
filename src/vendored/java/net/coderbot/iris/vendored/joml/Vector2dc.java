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

import java.util.*;

/**
 * Interface to a read-only view of a 2-dimensional vector of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector2dc {

    /**
     * @return the value of the x component
     */
    double x();

    /**
     * @return the value of the y component
     */
    double y();


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
     *          will receive the values of this vector in <code>x, y</code> order
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
     *          will receive the values of this vector in <code>x, y</code> order
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
     *          will receive the values of this vector in <code>x, y</code> order
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
     *          will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    DoubleBuffer get(int index, DoubleBuffer buffer);


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
    Vector2d sub(double x, double y, Vector2d dest);

    /**
     * Subtract <code>v</code> from <code>this</code> vector and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d sub(Vector2dc v, Vector2d dest);

    /**
     * Subtract <code>v</code> from <code>this</code> vector and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d sub(Vector2fc v, Vector2d dest);

    /**
     * Multiply the components of this vector by the given scalar and store the result in <code>dest</code>.
     * 
     * @param scalar
     *        the value to multiply this vector's components by
     * @param dest
     *        will hold the result
     * @return dest
     */
    Vector2d mul(double scalar, Vector2d dest);

    /**
     * Multiply the components of this Vector2d by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to multiply this vector by
     * @param y
     *          the y component to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d mul(double x, double y, Vector2d dest);

    /**
     * Multiply this Vector2d component-wise by another Vector2d and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d mul(Vector2dc v, Vector2d dest);

    /**
     * Divide this Vector2d by the given scalar value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to divide this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d div(double scalar, Vector2d dest);

    /**
     * Divide the components of this Vector3f by the given scalar values and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to divide this vector by
     * @param y
     *          the y component to divide this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d div(double x, double y, Vector2d dest);

    /**
     * Divide this Vector2d component-wise by another Vector2f and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d div(Vector2fc v, Vector2d dest);

    /**
     * Divide this by <code>v</code> component-wise and store the result into <code>dest</code>.
     * 
     * @param v
     *          the vector to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d div(Vector2dc v, Vector2d dest);

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
    Vector2d mul(Matrix2dc mat, Vector2d dest);

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
    Vector2d mul(Matrix2fc mat, Vector2d dest);

    /**
     * Multiply the transpose of the given matrix with this Vector2f and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d mulTranspose(Matrix2dc mat, Vector2d dest);

    /**
     * Multiply the transpose of the given matrix with this Vector2f and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d mulTranspose(Matrix2fc mat, Vector2d dest);

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
    Vector2d mulPosition(Matrix3x2dc mat, Vector2d dest);

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
    Vector2d mulDirection(Matrix3x2dc mat, Vector2d dest);

    /**
     * Return the dot product of this vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the dot product
     */
    double dot(Vector2dc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @param v
     *          the other vector
     * @return the angle, in radians
     */
    double angle(Vector2dc v);

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
     * Return the distance between this and <code>v</code>.
     *
     * @param v
     *        the other vector
     * @return the distance
     */
    double distance(Vector2dc v);

    /**
     * Return the distance squared between this and <code>v</code>.
     *
     * @param v
     *        the other vector
     * @return the distance squared
     */
    double distanceSquared(Vector2dc v);

    /**
     * Return the distance between this and <code>v</code>.
     *
     * @param v
     *        the other vector
     * @return the distance
     */
    double distance(Vector2fc v);

    /**
     * Return the distance squared between this and <code>v</code>.
     *
     * @param v
     *        the other vector
     * @return the distance squared
     */
    double distanceSquared(Vector2fc v);

    /**
     * Return the distance between <code>this</code> vector and <code>(x, y)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the euclidean distance
     */
    double distance(double x, double y);

    /**
     * Return the distance squared between <code>this</code> vector and <code>(x, y)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the euclidean distance squared
     */
    double distanceSquared(double x, double y);

    /**
     * Normalize this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d normalize(Vector2d dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d normalize(double length, Vector2d dest);

    /**
     * Add <code>(x, y)</code> to this vector and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d add(double x, double y, Vector2d dest);

    /**
     * Add <code>v</code> to this vector and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d add(Vector2dc v, Vector2d dest);

    /**
     * Add <code>v</code> to this vector and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d add(Vector2fc v, Vector2d dest);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d negate(Vector2d dest);

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
    Vector2d lerp(Vector2dc other, double t, Vector2d dest);

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
    Vector2d fma(Vector2dc a, Vector2dc b, Vector2d dest);

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
    Vector2d fma(double a, Vector2dc b, Vector2d dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d min(Vector2dc v, Vector2d dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d max(Vector2dc v, Vector2d dest);

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
     * infinity) {@code double} value that is less than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d floor(Vector2d dest);

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
    Vector2d ceil(Vector2d dest);

    /**
     * Compute for each component of this vector the closest double that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d round(Vector2d dest);

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
     * Compute the absolute of each of this vector's components
     * and store the result into <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2d absolute(Vector2d dest);

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
    boolean equals(Vector2dc v, double delta);

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
    boolean equals(double x, double y);

}
