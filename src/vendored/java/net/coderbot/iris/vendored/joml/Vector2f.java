/*
 * The MIT License
 *
 * Copyright (c) 2015-2021 Richard Greenlees
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Represents a 2D vector with single-precision.
 *
 * @author RGreenlees
 * @author Kai Burjack
 * @author F. Neurath
 */
public class Vector2f implements Externalizable, Cloneable, Vector2fc {

    private static final long serialVersionUID = 1L;

    /**
     * The x component of the vector.
     */
    public float x;
    /**
     * The y component of the vector.
     */
    public float y;

    /**
     * Create a new {@link Vector2f} and initialize its components to zero.
     */
    public Vector2f() {
    }

    /**
     * Create a new {@link Vector2f} and initialize both of its components with the given value.
     *
     * @param d
     *        the value of both components
     */
    public Vector2f(float d) {
        this.x = d;
        this.y = d;
    }

    /**
     * Create a new {@link Vector2f} and initialize its components to the given values.
     * 
     * @param x
     *        the x component
     * @param y
     *        the y component
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new {@link Vector2f} and initialize its components to the one of the given vector.
     * 
     * @param v
     *        the {@link Vector2fc} to copy the values from
     */
    public Vector2f(Vector2fc v) {
        x = v.x();
        y = v.y();
    }

    /**
     * Create a new {@link Vector2f} and initialize its components to the one of the given vector.
     * 
     * @param v
     *        the {@link Vector2ic} to copy the values from
     */
    public Vector2f(Vector2ic v) {
        x = v.x();
        y = v.y();
    }

    /**
     * Create a new {@link Vector2f} and initialize its two components from the first
     * two elements of the given array.
     * 
     * @param xy
     *          the array containing at least two elements
     */
    public Vector2f(float[] xy) {
        this.x = xy[0];
        this.y = xy[1];
    }


    /**
     * Create a new {@link Vector2f} and read this vector from the supplied {@link ByteBuffer}
     * at the current buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is read, use {@link #Vector2f(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @see #Vector2f(int, ByteBuffer)
     */
    public Vector2f(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector2f} and read this vector from the supplied {@link ByteBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *        the absolute position into the ByteBuffer
     * @param buffer values will be read in <code>x, y</code> order
     */
    public Vector2f(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    /**
     * Create a new {@link Vector2f} and read this vector from the supplied {@link FloatBuffer}
     * at the current buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the vector is read, use {@link #Vector2f(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @see #Vector2f(int, FloatBuffer)
     */
    public Vector2f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector2f} and read this vector from the supplied {@link FloatBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     *
     * @param index 
     *        the absolute position into the FloatBuffer
     * @param buffer
     *        values will be read in <code>x, y</code> order
     */
    public Vector2f(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }


    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    /**
     * Set the x and y components to the supplied value.
     *
     * @param d
     *        the value of both components
     * @return this
     */
    public Vector2f set(float d) {
        this.x = d;
        this.y = d;
        return this;
    }

    /**
     * Set the x and y components to the supplied values.
     * 
     * @param x
     *        the x component
     * @param y
     *        the y component
     * @return this
     */
    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Set the x and y components to the supplied value.
     *
     * @param d
     *        the value of both components
     * @return this
     */
    public Vector2f set(double d) {
        this.x = (float) d;
        this.y = (float) d;
        return this;
    }

    /**
     * Set the x and y components to the supplied values.
     * 
     * @param x
     *        the x component
     * @param y
     *        the y component
     * @return this
     */
    public Vector2f set(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
        return this;
    }

    /**
     * Set this {@link Vector2f} to the values of v.
     * 
     * @param v
     *        the vector to copy from
     * @return this
     */
    public Vector2f set(Vector2fc v) {
        this.x = v.x();
        this.y = v.y();
        return this;
    }

    /**
     * Set this {@link Vector2f} to the values of v.
     * 
     * @param v
     *        the vector to copy from
     * @return this
     */
    public Vector2f set(Vector2ic v) {
        this.x = v.x();
        this.y = v.y();
        return this;
    }

    /**
     * Set this {@link Vector2f} to the values of v.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components in double-precision,
     * there is the possibility to lose precision.
     * 
     * @param v
     *        the vector to copy from
     * @return this
     */
    public Vector2f set(Vector2dc v) {
        this.x = (float) v.x();
        this.y = (float) v.y();
        return this;
    }

    /**
     * Set the two components of this vector to the first two elements of the given array.
     * 
     * @param xy
     *          the array containing at least two elements
     * @return this
     */
    public Vector2f set(float[] xy) {
        this.x = xy[0];
        this.y = xy[1];
        return this;
    }


    /**
     * Read this vector from the supplied {@link ByteBuffer} at the current
     * buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is read, use {@link #set(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @return this
     * @see #set(int, ByteBuffer)
     */
    public Vector2f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link ByteBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *        the absolute position into the ByteBuffer
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link FloatBuffer} at the current
     * buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the vector is read, use {@link #set(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @return this
     * @see #set(int, FloatBuffer)
     */
    public Vector2f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link FloatBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     *
     * @param index 
     *        the absolute position into the FloatBuffer
     * @param buffer
     *        values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    public float get(int component) throws IllegalArgumentException {
        switch (component) {
        case 0:
            return x;
        case 1:
            return y;
        default:
            throw new IllegalArgumentException();
        }
    }

    public Vector2i get(int mode, Vector2i dest) {
        dest.x = Math.roundUsing(this.x(), mode);
        dest.y = Math.roundUsing(this.y(), mode);
        return dest;
    }

    public Vector2f get(Vector2f dest) {
        dest.x = this.x();
        dest.y = this.y();
        return dest;
    }

    public Vector2d get(Vector2d dest) {
        dest.x = this.x();
        dest.y = this.y();
        return dest;
    }

    /**
     * Set the value of the specified component of this vector.
     *
     * @param component
     *          the component whose value to set, within <code>[0..1]</code>
     * @param value
     *          the value to set
     * @return this
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..1]</code>
     */
    public Vector2f setComponent(int component, float value) throws IllegalArgumentException {
        switch (component) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return this;
    }


    public ByteBuffer get(ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public FloatBuffer get(FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }


    /**
     * Set this vector to be one of its perpendicular vectors.
     * 
     * @return this
     */
    public Vector2f perpendicular() {
        float xTemp = y;
        this.y = this.x * -1;
        this.x = xTemp;
        return this;
    }

    /**
     * Subtract <code>v</code> from this vector.
     * 
     * @param v
     *          the vector to subtract
     * @return this
     */
    public Vector2f sub(Vector2fc v) {
        this.x = x - v.x();
        this.y = y - v.y();
        return this;
    }

    public Vector2f sub(Vector2fc v, Vector2f dest) {
        dest.x = x - v.x();
        dest.y = y - v.y();
        return dest;
    }

    /**
     * Subtract <code>(x, y)</code> from this vector.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @return this
     */
    public Vector2f sub(float x, float y) {
        this.x = this.x - x;
        this.y = this.y - y;
        return this;
    }

    public Vector2f sub(float x, float y, Vector2f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    public float dot(Vector2fc v) {
        return x * v.x() + y * v.y();
    }

    public float angle(Vector2fc v) {
        float dot = x*v.x() + y*v.y();
        float det = x*v.y() - y*v.x();
        return Math.atan2(det, dot);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Get the length squared of a 2-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     *
     * @return the length squared of the given vector
     *
     * @author F. Neurath
     */
    public static float lengthSquared(float x, float y) {
        return x * x + y * y;
    }

    public float length() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Get the length of a 2-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     *
     * @return the length of the given vector
     *
     * @author F. Neurath
     */
    public static float length(float x, float y) {
        return Math.sqrt(x * x + y * y);
    }

    public float distance(Vector2fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSquared(Vector2fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        return dx * dx + dy * dy;
    }

    public float distance(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSquared(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    /**
     * Return the distance between <code>(x1, y1)</code> and <code>(x2, y2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @return the euclidean distance
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Return the squared distance between <code>(x1, y1)</code> and <code>(x2, y2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @return the euclidean distance squared
     */
    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    /**
     * Normalize this vector.
     * 
     * @return this
     */
    public Vector2f normalize() {
        float invLength = Math.invsqrt(x * x + y * y);
        this.x = x * invLength;
        this.y = y * invLength;
        return this;
    }

    public Vector2f normalize(Vector2f dest) {
        float invLength = Math.invsqrt(x * x + y * y);
        dest.x = x * invLength;
        dest.y = y * invLength;
        return dest;
    }

    /**
     * Scale this vector to have the given length.
     * 
     * @param length
     *          the desired length
     * @return this
     */
    public Vector2f normalize(float length) {
        float invLength = Math.invsqrt(x * x + y * y) * length;
        this.x = x * invLength;
        this.y = y * invLength;
        return this;
    }

    public Vector2f normalize(float length, Vector2f dest) {
        float invLength = Math.invsqrt(x * x + y * y) * length;
        dest.x = x * invLength;
        dest.y = y * invLength;
        return dest;
    }

    /**
     * Add <code>v</code> to this vector.
     * 
     * @param v
     *        the vector to add
     * @return this
     */
    public Vector2f add(Vector2fc v) {
        this.x = x + v.x();
        this.y = y + v.y();
        return this;
    }

    public Vector2f add(Vector2fc v, Vector2f dest) {
        dest.x = x + v.x();
        dest.y = y + v.y();
        return dest;
    }

    /**
     * Increment the components of this vector by the given values.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @return this
     */
    public Vector2f add(float x, float y) {
        return add(x, y, this);
    }

    public Vector2f add(float x, float y, Vector2f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    /**
     * Set all components to zero.
     * 
     * @return this
     */
    public Vector2f zero() {
        this.x = 0;
        this.y = 0;
        return this;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        x = in.readFloat();
        y = in.readFloat();
    }

    /**
     * Negate this vector.
     * 
     * @return this
     */
    public Vector2f negate() {
        this.x = -x;
        this.y = -y;
        return this;
    }

    public Vector2f negate(Vector2f dest) {
        dest.x = -x;
        dest.y = -y;
        return dest;
    }

    /**
     * Multiply the components of this vector by the given scalar.
     * 
     * @param scalar
     *        the value to multiply this vector's components by
     * @return this
     */
    public Vector2f mul(float scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        return this;
    }

    public Vector2f mul(float scalar, Vector2f dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        return dest;
    }

    /**
     * Multiply the components of this Vector2f by the given scalar values and store the result in <code>this</code>.
     * 
     * @param x
     *          the x component to multiply this vector by
     * @param y
     *          the y component to multiply this vector by
     * @return this
     */
    public Vector2f mul(float x, float y) {
        this.x = this.x * x;
        this.y = this.y * y;
        return this;
    }

    public Vector2f mul(float x, float y, Vector2f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    /**
     * Multiply this Vector2f component-wise by another Vector2f.
     * 
     * @param v
     *          the vector to multiply by
     * @return this
     */
    public Vector2f mul(Vector2fc v) {
        this.x = x * v.x();
        this.y = y * v.y();
        return this;
    }

    public Vector2f mul(Vector2fc v, Vector2f dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        return dest;
    }

    /**
     * Divide this Vector2f component-wise by another Vector2fc.
     * 
     * @param v
     *          the vector to divide by
     * @return this
     */
    public Vector2f div(Vector2fc v) {
        this.x = this.x / v.x();
        this.y = this.y / v.y();
        return this;
    }

    public Vector2f div(Vector2fc v, Vector2f dest) {
        dest.x = x / v.x();
        dest.y = y / v.y();
        return dest;
    }

    /**
     * Divide all components of this {@link Vector2f} by the given scalar
     * value.
     * 
     * @param scalar
     *          the scalar to divide by
     * @return this
     */
    public Vector2f div(float scalar) {
        float inv = 1.0f / scalar;
        this.x = this.x * inv;
        this.y = this.y * inv;
        return this;
    }

    public Vector2f div(float scalar, Vector2f dest) {
        float inv = 1.0f / scalar;
        dest.x = this.x * inv;
        dest.y = this.y * inv;
        return dest;
    }

    /**
     * Divide the components of this Vector2f by the given scalar values and store the result in <code>this</code>.
     * 
     * @param x
     *          the x component to divide this vector by
     * @param y
     *          the y component to divide this vector by
     * @return this
     */
    public Vector2f div(float x, float y) {
        this.x = this.x / x;
        this.y = this.y / y;
        return this;
    }

    public Vector2f div(float x, float y, Vector2f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        return dest;
    }

    /**
     * Multiply the given matrix with this Vector2f and store the result in <code>this</code>.
     *
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector2f mul(Matrix2fc mat) {
        float rx = mat.m00() * x + mat.m10() * y;
        float ry = mat.m01() * x + mat.m11() * y;
        this.x = rx;
        this.y = ry;
        return this;
    }

    public Vector2f mul(Matrix2fc mat, Vector2f dest) {
        float rx = mat.m00() * x + mat.m10() * y;
        float ry = mat.m01() * x + mat.m11() * y;
        dest.x = rx;
        dest.y = ry;
        return dest;
    }

    /**
     * Multiply the given matrix with this Vector2f and store the result in <code>this</code>.
     *
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector2f mul(Matrix2dc mat) {
        double rx = mat.m00() * x + mat.m10() * y;
        double ry = mat.m01() * x + mat.m11() * y;
        this.x = (float) rx;
        this.y = (float) ry;
        return this;
    }

    public Vector2f mul(Matrix2dc mat, Vector2f dest) {
        double rx = mat.m00() * x + mat.m10() * y;
        double ry = mat.m01() * x + mat.m11() * y;
        dest.x = (float) rx;
        dest.y = (float) ry;
        return dest;
    }

    /**
     * Multiply the transpose of the given matrix with this Vector2f store the result in <code>this</code>.
     *
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector2f mulTranspose(Matrix2fc mat) {
        float rx = mat.m00() * x + mat.m01() * y;
        float ry = mat.m10() * x + mat.m11() * y;
        this.x = rx;
        this.y = ry;
        return this;
    }

    public Vector2f mulTranspose(Matrix2fc mat, Vector2f dest) {
        float rx = mat.m00() * x + mat.m01() * y;
        float ry = mat.m10() * x + mat.m11() * y;
        dest.x = rx;
        dest.y = ry;
        return dest;
    }

    /**
     * Multiply the given 3x2 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>z</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector2f mulPosition(Matrix3x2fc mat) {
        this.x = mat.m00() * x + mat.m10() * y + mat.m20();
        this.y = mat.m01() * x + mat.m11() * y + mat.m21();
        return this;
    }

    public Vector2f mulPosition(Matrix3x2fc mat, Vector2f dest) {
        dest.x = mat.m00() * x + mat.m10() * y + mat.m20();
        dest.y = mat.m01() * x + mat.m11() * y + mat.m21();
        return dest;
    }

    /**
     * Multiply the given 3x2 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>z</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector2f mulDirection(Matrix3x2fc mat) {
        this.x = mat.m00() * x + mat.m10() * y;
        this.y = mat.m01() * x + mat.m11() * y;
        return this;
    }

    public Vector2f mulDirection(Matrix3x2fc mat, Vector2f dest) {
        dest.x = mat.m00() * x + mat.m10() * y;
        dest.y = mat.m01() * x + mat.m11() * y;
        return dest;
    }

    /**
     * Linearly interpolate <code>this</code> and <code>other</code> using the given interpolation factor <code>t</code>
     * and store the result in <code>this</code>.
     * <p>
     * If <code>t</code> is <code>0.0</code> then the result is <code>this</code>. If the interpolation factor is <code>1.0</code>
     * then the result is <code>other</code>.
     * 
     * @param other
     *          the other vector
     * @param t
     *          the interpolation factor between 0.0 and 1.0
     * @return this
     */
    public Vector2f lerp(Vector2fc other, float t) {
        this.x = x + (other.x() - x) * t;
        this.y = y + (other.y() - y) * t;
        return this;
    }

    public Vector2f lerp(Vector2fc other, float t, Vector2f dest) {
        dest.x = x + (other.x() - x) * t;
        dest.y = y + (other.y() - y) * t;
        return dest;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector2f other = (Vector2f) obj;
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
            return false;
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
            return false;
        return true;
    }

    public boolean equals(Vector2fc v, float delta) {
        if (this == v)
            return true;
        if (v == null)
            return false;
        if (!(v instanceof Vector2fc))
            return false;
        if (!Runtime.equals(x, v.x(), delta))
            return false;
        if (!Runtime.equals(y, v.y(), delta))
            return false;
        return true;
    }

    public boolean equals(float x, float y) {
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(x))
            return false;
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(y))
            return false;
        return true;
    }

    /**
     * Return a string representation of this vector.
     * <p>
     * This method creates a new {@link DecimalFormat} on every invocation with the format string "<code>0.000E0;-</code>".
     * 
     * @return the string representation
     */
    public String toString() {
        return Runtime.formatNumbers(toString(Options.NUMBER_FORMAT));
    }

    /**
     * Return a string representation of this vector by formatting the vector components with the given {@link NumberFormat}.
     * 
     * @param formatter
     *          the {@link NumberFormat} used to format the vector components with
     * @return the string representation
     */
    public String toString(NumberFormat formatter) {
        return "(" + Runtime.format(x, formatter) + " " + Runtime.format(y, formatter) + ")";
    }

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @return this
     */
    public Vector2f fma(Vector2fc a, Vector2fc b) {
        this.x = x + a.x() * b.x();
        this.y = y + a.y() * b.y();
        return this;
    }

    /**
     * Add the component-wise multiplication of <code>a * b</code> to this vector.
     * 
     * @param a
     *          the first multiplicand
     * @param b
     *          the second multiplicand
     * @return this
     */
    public Vector2f fma(float a, Vector2fc b) {
        this.x = x + a * b.x();
        this.y = y + a * b.y();
        return this;
    }

    public Vector2f fma(Vector2fc a, Vector2fc b, Vector2f dest) {
        dest.x = x + a.x() * b.x();
        dest.y = y + a.y() * b.y();
        return dest;
    }

    public Vector2f fma(float a, Vector2fc b, Vector2f dest) {
        dest.x = x + a * b.x();
        dest.y = y + a * b.y();
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector2f min(Vector2fc v) {
        this.x = x < v.x() ? x : v.x();
        this.y = y < v.y() ? y : v.y();
        return this;
    }

    public Vector2f min(Vector2fc v, Vector2f dest) {
        dest.x = x < v.x() ? x : v.x();
        dest.y = y < v.y() ? y : v.y();
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector2f max(Vector2fc v) {
        this.x = x > v.x() ? x : v.x();
        this.y = y > v.y() ? y : v.y();
        return this;
    }

    public Vector2f max(Vector2fc v, Vector2f dest) {
        dest.x = x > v.x() ? x : v.x();
        dest.y = y > v.y() ? y : v.y();
        return dest;
    }

    public int maxComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        if (absX >= absY)
            return 0;
        return 1;
    }

    public int minComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        if (absX < absY)
            return 0;
        return 1;
    }

    /**
     * Set each component of this vector to the largest (closest to positive
     * infinity) {@code float} value that is less than or equal to that
     * component and is equal to a mathematical integer.
     *
     * @return this
     */
    public Vector2f floor() {
        this.x = Math.floor(x);
        this.y = Math.floor(y);
        return this;
    }

    public Vector2f floor(Vector2f dest) {
        dest.x = Math.floor(x);
        dest.y = Math.floor(y);
        return dest;
    }

    /**
     * Ceil each component of this vector
     *
     * @return this
     */
    public Vector2f ceil() {
        this.x = Math.ceil(x);
        this.y = Math.ceil(y);
        return this;
    }

    public Vector2f ceil(Vector2f dest) {
        dest.x = Math.ceil(x);
        dest.y = Math.ceil(y);
        return dest;
    }

    /**
     * Set each component of this vector to the closest float that is equal to
     * a mathematical integer, with ties rounding to positive infinity.
     *
     * @return this
     */
    public Vector2f round() {
        this.x = Math.ceil(x);
        this.y = Math.ceil(y);
        return this;
    }

    public Vector2f round(Vector2f dest) {
        dest.x = Math.round(x);
        dest.y = Math.round(y);
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(x) && Math.isFinite(y);
    }

    /**
     * Set <code>this</code> vector's components to their respective absolute values.
     * 
     * @return this
     */
    public Vector2f absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vector2f absolute(Vector2f dest) {
        dest.x = Math.abs(this.x);
        dest.y = Math.abs(this.y);
        return dest;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
