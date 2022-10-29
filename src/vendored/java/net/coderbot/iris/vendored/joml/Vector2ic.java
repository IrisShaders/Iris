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
import java.nio.IntBuffer;


/**
 * Interface to a read-only view of a 2-dimensional vector of integers.
 * 
 * @author Kai Burjack
 */
public interface Vector2ic {

    /**
     * @return the value of the x component
     */
    int x();

    /**
     * @return the value of the y component
     */
    int y();


    /**
     * Store this vector into the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which the vector is
     * stored, use {@link #get(int, ByteBuffer)}, taking the absolute position
     * as parameter.
     *
     * @see #get(int, ByteBuffer)
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    ByteBuffer get(ByteBuffer buffer);

    /**
     * Store this vector into the supplied {@link ByteBuffer} starting at the
     * specified absolute buffer position/index.
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
     * Store this vector into the supplied {@link IntBuffer} at the current
     * buffer {@link IntBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     * <p>
     * In order to specify the offset into the IntBuffer at which the vector is
     * stored, use {@link #get(int, IntBuffer)}, taking the absolute position as
     * parameter.
     *
     * @see #get(int, IntBuffer)
     *
     * @param buffer
     *          will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    IntBuffer get(IntBuffer buffer);

    /**
     * Store this vector into the supplied {@link IntBuffer} starting at the
     * specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     *
     * @param index
     *          the absolute position into the IntBuffer
     * @param buffer
     *          will receive the values of this vector in <code>x, y</code> order
     * @return the passed in buffer
     */
    IntBuffer get(int index, IntBuffer buffer);


    /**
     * Subtract the supplied vector from this one and store the result in
     * <code>dest</code>.
     *
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i sub(Vector2ic v, Vector2i dest);

    /**
     * Decrement the components of this vector by the given values and store the
     * result in <code>dest</code>.
     *
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i sub(int x, int y, Vector2i dest);

    /**
     * Return the length squared of this vector.
     *
     * @return the length squared
     */
    long lengthSquared();

    /**
     * Return the length of this vector.
     *
     * @return the length
     */
    double length();

    /**
     * Return the distance between this Vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the distance
     */
    double distance(Vector2ic v);

    /**
     * Return the distance between <code>this</code> vector and <code>(x, y)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the euclidean distance
     */
    double distance(int x, int y);

    /**
     * Return the square of the distance between this vector and <code>v</code>.
     *
     * @param v
     *          the other vector
     * @return the squared of the distance
     */
    long distanceSquared(Vector2ic v);

    /**
     * Return the square of the distance between <code>this</code> vector and
     * <code>(x, y)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the square of the distance
     */
    long distanceSquared(int x, int y);

    /**
     * Return the grid distance in between (aka 1-Norm, Minkowski or Manhattan distance)
     * <code>(x, y)</code>.
     *
     * @param v
     *          the other vector
     * @return the grid distance
     */
    long gridDistance(Vector2ic v);

    /**
     * Return the grid distance in between (aka 1-Norm, Minkowski or Manhattan distance)
     * <code>(x, y)</code>.
     *
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @return the grid distance
     */
    long gridDistance(int x, int y);

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
    Vector2i add(Vector2ic v, Vector2i dest);

    /**
     * Increment the components of this vector by the given values and store the
     * result in <code>dest</code>.
     *
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i add(int x, int y, Vector2i dest);

    /**
     * Multiply all components of this {@link Vector2ic} by the given scalar
     * value and store the result in <code>dest</code>.
     * 
     * @param scalar
     *          the scalar to multiply this vector by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i mul(int scalar, Vector2i dest);

    /**
     * Multiply the supplied vector by this one and store the result in
     * <code>dest</code>.
     *
     * @param v
     *          the vector to multiply
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i mul(Vector2ic v, Vector2i dest);

    /**
     * Multiply the components of this vector by the given values and store the
     * result in <code>dest</code>.
     *
     * @param x
     *          the x component to multiply
     * @param y
     *          the y component to multiply
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i mul(int x, int y, Vector2i dest);

    /**
     * Divide all components of this {@link Vector2i} by the given scalar value
     * and store the result in <code>dest</code>.
     *
     * @param scalar
     *          the scalar to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i div(float scalar, Vector2i dest);

    /**
     * Divide all components of this {@link Vector2i} by the given scalar value
     * and store the result in <code>dest</code>.
     *
     * @param scalar
     *          the scalar to divide by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i div(int scalar, Vector2i dest);

    /**
     * Negate this vector and store the result in <code>dest</code>.
     *
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i negate(Vector2i dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i min(Vector2ic v, Vector2i dest);

    /**
     * Set the components of <code>dest</code> to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i max(Vector2ic v, Vector2i dest);

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
     * Compute the absolute of each of this vector's components
     * and store the result into <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector2i absolute(Vector2i dest);

    /**
     * Get the value of the specified component of this vector.
     * 
     * @param component
     *          the component, within <code>[0..1]</code>
     * @return the value
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..1]</code>
     */
    int get(int component) throws IllegalArgumentException;

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
    boolean equals(int x, int y);

}
