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
 * Interface to a read-only view of a 4-dimensional vector of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Vector4dc {

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
     * @return the value of the w component
     */
    double w();


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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     * <p>
     * Please note that due to this vector storing double values those values will potentially
     * lose precision when they are converted to float values before being put into the given ByteBuffer.
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y, z, w</code> order
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
     *          will receive the values of this vector in <code>x, y, z, w</code> order
     * @return the passed in buffer
     */
    ByteBuffer getf(int index, ByteBuffer buffer);


    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d sub(Vector4dc v, Vector4d dest);

    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d sub(Vector4fc v, Vector4d dest);

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
    Vector4d sub(double x, double y, double z, double w, Vector4d dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d add(Vector4dc v, Vector4d dest);

    /**
     * Add the supplied vector to this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d add(Vector4fc v, Vector4d dest);

    /**
     * Add <code>(x, y, z, w)</code> to this and store the result in <code>dest</code>.
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
    Vector4d add(double x, double y, double z, double w, Vector4d dest);

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
    Vector4d fma(Vector4dc a, Vector4dc b, Vector4d dest);

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
    Vector4d fma(double a, Vector4dc b, Vector4d dest);

    /**
     * Multiply this {@link Vector4d} component-wise by the given {@link Vector4dc} and store the result in <code>dest</code>.
     * 
     * @param v
     *             the vector to multiply this by
     * @param dest
     *             will hold the result
     * @return dest
     */
    Vector4d mul(Vector4dc v, Vector4d dest);

    /**
     * Multiply this {@link Vector4d} component-wise by the given {@link Vector4fc} and store the result in <code>dest</code>.
     * 
     * @param v
     *             the vector to multiply this by
     * @param dest
     *             will hold the result
     * @return dest
     */
    Vector4d mul(Vector4fc v, Vector4d dest);

    /**
     * Divide this {@link Vector4d} component-wise by the given {@link Vector4dc} and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to divide this by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d div(Vector4dc v, Vector4d dest);

    /**
     * Multiply the given matrix mat with this {@link Vector4d} and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply <code>this</code> by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d mul(Matrix4dc mat, Vector4d dest);

    /**
     * Multiply the given matrix mat with this Vector4d and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4d mul(Matrix4x3dc mat, Vector4d dest);

    /**
     * Multiply the given matrix mat with this Vector4d and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4d mul(Matrix4x3fc mat, Vector4d dest);

    /**
     * Multiply the given matrix mat with this Vector4d and store the result in <code>dest</code>.
     *
     * @param mat
     *          the matrix to multiply <code>this</code> by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d mul(Matrix4fc mat, Vector4d dest);

    /**
     * Multiply the transpose of the given matrix <code>mat</code> with this Vector4d and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4d mulTranspose(Matrix4dc mat, Vector4d dest);

    /**
     * Multiply the given affine matrix mat with this Vector4d and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the affine matrix to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4d mulAffine(Matrix4dc mat, Vector4d dest);

    /**
     * Multiply the transpose of the given affine matrix <code>mat</code> with this Vector4d and store the result in
     * <code>dest</code>.
     * 
     * @param mat
     *          the affine matrix whose transpose to multiply the vector with
     * @param dest
     *          the destination vector to hold the result
     * @return dest
     */
    Vector4d mulAffineTranspose(Matrix4dc mat, Vector4d dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector4d, perform perspective division
     * and store the result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d mulProject(Matrix4dc mat, Vector4d dest);

    /**
     * Multiply the given matrix <code>mat</code> with this Vector4d, perform perspective division
     * and store the <code>(x, y, z)</code> result in <code>dest</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d mulProject(Matrix4dc mat, Vector3d dest);

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
    Vector4d mulAdd(Vector4dc a, Vector4dc b, Vector4d dest);

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
    Vector4d mulAdd(double a, Vector4dc b, Vector4d dest);

    /**
     * Multiply this Vector4d by the given scalar value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the factor to multiply by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d mul(double scalar, Vector4d dest);

    /**
     * Divide this Vector4d by the given scalar value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the factor to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d div(double scalar, Vector4d dest);

    /**
     * Transform this vector by the given quaternion <code>quat</code> and store the result in <code>dest</code>.
     * 
     * @see Quaterniond#transform(Vector4d)
     * 
     * @param quat
     *          the quaternion to transform this vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d rotate(Quaterniondc quat, Vector4d dest);

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
    Vector4d rotateAxis(double angle, double aX, double aY, double aZ, Vector4d dest);

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
    Vector4d rotateX(double angle, Vector4d dest);

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
    Vector4d rotateY(double angle, Vector4d dest);

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
    Vector4d rotateZ(double angle, Vector4d dest);

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
     * Normalizes this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d normalize(Vector4d dest);

    /**
     * Scale this vector to have the given length and store the result in <code>dest</code>.
     * 
     * @param length
     *          the desired length
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d normalize(double length, Vector4d dest);

    /**
     * Normalize this vector by computing only the norm of <code>(x, y, z)</code> and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d normalize3(Vector4d dest);

    /**
     * Return the distance between this Vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the distance
     */
    double distance(Vector4dc v);

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
    double distance(double x, double y, double z, double w);

    /**
     * Return the square of the distance between this vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the squared of the distance
     */
    double distanceSquared(Vector4dc v);

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
    double distanceSquared(double x, double y, double z, double w);

    /**
     * Compute the dot product (inner product) of this vector and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return the dot product
     */
    double dot(Vector4dc v);

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
    double dot(double x, double y, double z, double w);

    /**
     * Return the cosine of the angle between this vector and the supplied vector.
     * <p>
     * Use this instead of <code>Math.cos(angle(v))</code>.
     * 
     * @see #angle(Vector4dc)
     * 
     * @param v
     *          the other vector
     * @return the cosine of the angle
     */
    double angleCos(Vector4dc v);

    /**
     * Return the angle between this vector and the supplied vector.
     * 
     * @see #angleCos(Vector4dc)
     * 
     * @param v
     *          the other vector
     * @return the angle, in radians
     */
    double angle(Vector4dc v);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d negate(Vector4d dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d min(Vector4dc v, Vector4d dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d max(Vector4dc v, Vector4d dest);

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
    Vector4d smoothStep(Vector4dc v, double t, Vector4d dest);

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
    Vector4d hermite(Vector4dc t0, Vector4dc v1, Vector4dc t1, double t, Vector4d dest);

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
    Vector4d lerp(Vector4dc other, double t, Vector4d dest);

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..3]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..3]</code>
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
     * infinity) {@code double} value that is less than or equal to that
     * component and is equal to a mathematical integer and store the result in
     * <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d floor(Vector4d dest);

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
    Vector4d ceil(Vector4d dest);

    /**
     * Compute for each component of this vector the closest double that is equal to
     * a mathematical integer, with ties rounding to positive infinity and store
     * the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d round(Vector4d dest);

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
    Vector4d absolute(Vector4d dest);

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
    boolean equals(Vector4dc v, double delta);

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
    boolean equals(double x, double y, double z, double w);

}
