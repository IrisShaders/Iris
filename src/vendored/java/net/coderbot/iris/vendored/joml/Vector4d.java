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

import java.nio.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a Vector comprising 4 doubles and associated transformations.
 * 
 * @author Richard Greenlees
 * @author Kai Burjack
 * @author F. Neurath
 */
public class Vector4d implements Externalizable, Cloneable, Vector4dc {

    private static final long serialVersionUID = 1L;

    /**
     * The x component of the vector.
     */
    public double x;
    /**
     * The y component of the vector.
     */
    public double y;
    /**
     * The z component of the vector.
     */
    public double z;
    /**
     * The w component of the vector.
     */
    public double w;

    /**
     * Create a new {@link Vector4d} of <code>(0, 0, 0, 1)</code>.
     */
    public Vector4d() {
        this.w = 1.0;
    }

    /**
     * Create a new {@link Vector4d} with the same values as <code>v</code>.
     * 
     * @param v
     *          the {@link Vector4dc} to copy the values from
     */
    public Vector4d(Vector4dc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
    }

    /**
     * Create a new {@link Vector4d} with the same values as <code>v</code>.
     * 
     * @param v
     *          the {@link Vector4ic} to copy the values from
     */
    public Vector4d(Vector4ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
    }

    /**
     * Create a new {@link Vector4d} with the first three components from the
     * given <code>v</code> and the given <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3dc}
     * @param w
     *          the w component
     */
    public Vector4d(Vector3dc v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} with the first three components from the
     * given <code>v</code> and the given <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3ic}
     * @param w
     *          the w component
     */
    public Vector4d(Vector3ic v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} with the first two components from the
     * given <code>v</code> and the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2dc}
     * @param z
     *          the z component
     * @param w
     *          the w component
     */
    public Vector4d(Vector2dc v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} with the first two components from the
     * given <code>v</code> and the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2ic}
     * @param z
     *          the z component
     * @param w
     *          the w component
     */
    public Vector4d(Vector2ic v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} and initialize all four components with the given value.
     *
     * @param d
     *          the value of all four components
     */
    public Vector4d(double d) {
        this.x = d;
        this.y = d;
        this.z = d;
        this.w = d; 
    }

    /**
     * Create a new {@link Vector4d} with the same values as <code>v</code>.
     * 
     * @param v
     *          the {@link Vector4fc} to copy the values from
     */
    public Vector4d(Vector4fc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
    }

    /**
     * Create a new {@link Vector4d} with the x, y, and z components from the
     * given <code>v</code> and the w component from the given <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3fc}
     * @param w
     *          the w component
     */
    public Vector4d(Vector3fc v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} with the x and y components from the
     * given <code>v</code> and the z and w components from the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2fc}
     * @param z
     *          the z component
     * @param w
     *          the w component
     */
    public Vector4d(Vector2fc v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} with the given component values.
     * 
     * @param x    
     *          the x component
     * @param y
     *          the y component
     * @param z
     *          the z component
     * @param w
     *          the w component
     */
    public Vector4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4d} and initialize its four components from the first
     * four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     */
    public Vector4d(float[] xyzw) {
        this.x = xyzw[0];
        this.y = xyzw[1];
        this.z = xyzw[2];
        this.w = xyzw[3];
    }

    /**
     * Create a new {@link Vector4d} and initialize its four components from the first
     * four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     */
    public Vector4d(double[] xyzw) {
        this.x = xyzw[0];
        this.y = xyzw[1];
        this.z = xyzw[2];
        this.w = xyzw[3];
    }


    /**
     * Create a new {@link Vector4d} and read this vector from the supplied {@link ByteBuffer}
     * at the current buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is read, use {@link #Vector4d(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @see #Vector4d(int, ByteBuffer)
     */
    public Vector4d(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector4d} and read this vector from the supplied {@link ByteBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index  the absolute position into the ByteBuffer
     * @param buffer values will be read in <code>x, y, z, w</code> order
     */
    public Vector4d(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    /**
     * Create a new {@link Vector4d} and read this vector from the supplied {@link DoubleBuffer}
     * at the current buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the vector is read, use {@link #Vector4d(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer values will be read in <code>x, y, z, w</code> order
     * @see #Vector4d(int, DoubleBuffer)
     */
    public Vector4d(DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector4d} and read this vector from the supplied {@link DoubleBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     *
     * @param index  the absolute position into the DoubleBuffer
     * @param buffer values will be read in <code>x, y, z, w</code> order
     */
    public Vector4d(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }


    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public double w() {
        return this.w;
    }

    /**
     * Set this {@link Vector4d} to the values of the given <code>v</code>.
     * 
     * @param v
     *          the vector whose values will be copied into this
     * @return this
     */
    public Vector4d set(Vector4dc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
        return this;
    }

    /**
     * Set this {@link Vector4d} to the values of the given <code>v</code>.
     * 
     * @param v
     *          the vector whose values will be copied into this
     * @return this
     */
    public Vector4d set(Vector4fc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
        return this;
    }

    /**
     * Set this {@link Vector4d} to the values of the given <code>v</code>.
     * 
     * @param v
     *          the vector whose values will be copied into this
     * @return this
     */
    public Vector4d set(Vector4ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
        return this;
    }

    /**
     * Set the x, y, and z components of this to the components of
     * <code>v</code> and the w component to <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3dc} to copy
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(Vector3dc v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, and z components of this to the components of
     * <code>v</code> and the w component to <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3ic} to copy
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(Vector3ic v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, and z components of this to the components of
     * <code>v</code> and the w component to <code>w</code>.
     * 
     * @param v
     *          the {@link Vector3fc} to copy
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(Vector3fc v, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
        return this;
    }

    /**
     * Set the x and y components from the given <code>v</code>
     * and the z and w components to the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2dc}
     * @param z
     *          the z component
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(Vector2dc v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the x and y components from the given <code>v</code>
     * and the z and w components to the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2ic}
     * @param z
     *          the z component
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(Vector2ic v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, z, and w components to the supplied value.
     *
     * @param d
     *          the value of all four components
     * @return this
     */
    public Vector4d set(double d) {
        this.x = d;
        this.y = d;
        this.z = d;
        this.w = d;
        return this;
    }

    /**
     * Set the x and y components from the given <code>v</code>
     * and the z and w components to the given <code>z</code> and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2fc}
     * @param z
     *          the z components
     * @param w
     *          the w components
     * @return this
     */
    public Vector4d set(Vector2fc v, double z, double w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, z, and w components to the supplied values.
     * 
     * @param x
     *          the x component
     * @param y
     *          the y component
     * @param z
     *          the z component
     * @param w
     *          the w component
     * @return this
     */
    public Vector4d set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, z components to the supplied values.
     * 
     * @param x
     *          the x component
     * @param y
     *          the y component
     * @param z
     *          the z component
     * @return this
     */
    public Vector4d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set the four components of this vector to the first four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     * @return this
     */
    public Vector4d set(double[] xyzw) {
        this.x = xyzw[0];
        this.y = xyzw[1];
        this.z = xyzw[2];
        this.w = xyzw[3];
        return this;
    }

    /**
     * Set the four components of this vector to the first four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     * @return this
     */
    public Vector4d set(float[] xyzw) {
        this.x = xyzw[0];
        this.y = xyzw[1];
        this.z = xyzw[2];
        this.w = xyzw[3];
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
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     * @see #set(int, ByteBuffer)
     */
    public Vector4d set(ByteBuffer buffer) {
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
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4d set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link DoubleBuffer} at the current
     * buffer {@link DoubleBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     * <p>
     * In order to specify the offset into the DoubleBuffer at which
     * the vector is read, use {@link #set(int, DoubleBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     * @see #set(int, DoubleBuffer)
     */
    public Vector4d set(DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link DoubleBuffer} starting at the specified
     * absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given DoubleBuffer.
     *
     * @param index
     *          the absolute position into the DoubleBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4d set(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set the value of the specified component of this vector.
     *
     * @param component
     *          the component whose value to set, within <code>[0..3]</code>
     * @param value
     *          the value to set
     * @return this
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..3]</code>
     */
    public Vector4d setComponent(int component, double value) throws IllegalArgumentException {
        switch (component) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            case 2:
                z = value;
                break;
            case 3:
                w = value;
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

    public DoubleBuffer get(DoubleBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public DoubleBuffer get(int index, DoubleBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public ByteBuffer getf(ByteBuffer buffer) {
        MemUtil.INSTANCE.putf(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer getf(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.putf(this, index, buffer);
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
     * Subtract the supplied vector from this one.
     * 
     * @param v
     *          the vector to subtract
     * @return this
     */
    public Vector4d sub(Vector4dc v) {
        this.x = x - v.x();
        this.y = y - v.y();
        this.z = z - v.z();
        this.w = w - v.w();
        return this;
    }

    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector4d sub(Vector4dc v, Vector4d dest) {
        dest.x = x - v.x();
        dest.y = y - v.y();
        dest.z = z - v.z();
        dest.w = w - v.w();
        return dest;
    }

    /**
     * Subtract the supplied vector from this one.
     * 
     * @param v
     *          the vector to subtract
     * @return this
     */
    public Vector4d sub(Vector4fc v) {
        this.x = x - v.x();
        this.y = y - v.y();
        this.z = z - v.z();
        this.w = w - v.w();
        return this;
    }

    /**
     * Subtract the supplied vector from this one and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to subtract
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector4d sub(Vector4fc v, Vector4d dest) {
        dest.x = x - v.x();
        dest.y = y - v.y();
        dest.z = z - v.z();
        dest.w = w - v.w();
        return dest;
    }

    /**
     * Subtract <code>(x, y, z, w)</code> from this.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param z
     *          the z component to subtract
     * @param w
     *          the w component to subtract
     * @return this
     */
    public Vector4d sub(double x, double y, double z, double w) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.z = this.z - z;
        this.w = this.w - w;
        return this;
    }

    public Vector4d sub(double x, double y, double z, double w, Vector4d dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        dest.z = this.z - z;
        dest.w = this.w - w;
        return dest;
    }

    /**
     * Add the supplied vector to this one.
     * 
     * @param v
     *          the vector to add
     * @return this
     */
    public Vector4d add(Vector4dc v) {
        this.x = x + v.x();
        this.y = y + v.y();
        this.z = z + v.z();
        this.w = w + v.w();
        return this;
    }

    public Vector4d add(Vector4dc v, Vector4d dest) {
        dest.x = x + v.x();
        dest.y = y + v.y();
        dest.z = z + v.z();
        dest.w = w + v.w();
        return dest;
    }

    public Vector4d add(Vector4fc v, Vector4d dest) {
        dest.x = x + v.x();
        dest.y = y + v.y();
        dest.z = z + v.z();
        dest.w = w + v.w();
        return dest;
    }

    /**
     * Add <code>(x, y, z, w)</code> to this.
     * 
     * @param x
     *          the x component to add
     * @param y
     *          the y component to add
     * @param z
     *          the z component to add
     * @param w
     *          the w component to add
     * @return this
     */
    public Vector4d add(double x, double y, double z, double w) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        this.w = this.w + w;
        return this;
    }

    public Vector4d add(double x, double y, double z, double w, Vector4d dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        dest.w = this.w + w;
        return dest;
    }

    /**
     * Add the supplied vector to this one.
     * 
     * @param v
     *          the vector to add
     * @return this
     */
    public Vector4d add(Vector4fc v) {
        this.x = x + v.x();
        this.y = y + v.y();
        this.z = z + v.z();
        this.w = w + v.w();
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
    public Vector4d fma(Vector4dc a, Vector4dc b) {
        this.x = Math.fma(a.x(), b.x(), x);
        this.y = Math.fma(a.y(), b.y(), y);
        this.z = Math.fma(a.z(), b.z(), z);
        this.w = Math.fma(a.w(), b.w(), w);
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
    public Vector4d fma(double a, Vector4dc b) {
        this.x = Math.fma(a, b.x(), x);
        this.y = Math.fma(a, b.y(), y);
        this.z = Math.fma(a, b.z(), z);
        this.w = Math.fma(a, b.w(), w);
        return this;
    }

    public Vector4d fma(Vector4dc a, Vector4dc b, Vector4d dest) {
        dest.x = Math.fma(a.x(), b.x(), x);
        dest.y = Math.fma(a.y(), b.y(), y);
        dest.z = Math.fma(a.z(), b.z(), z);
        dest.w = Math.fma(a.w(), b.w(), w);
        return dest;
    }

    public Vector4d fma(double a, Vector4dc b, Vector4d dest) {
        dest.x = Math.fma(a, b.x(), x);
        dest.y = Math.fma(a, b.y(), y);
        dest.z = Math.fma(a, b.z(), z);
        dest.w = Math.fma(a, b.w(), w);
        return dest;
    }

    /**
     * Add the component-wise multiplication of <code>this * a</code> to <code>b</code>
     * and store the result in <code>this</code>.
     * 
     * @param a
     *          the multiplicand
     * @param b
     *          the addend
     * @return this
     */
    public Vector4d mulAdd(Vector4dc a, Vector4dc b) {
        this.x = Math.fma(x, a.x(), b.x());
        this.y = Math.fma(y, a.y(), b.y());
        this.z = Math.fma(z, a.z(), b.z());
        return this;
    }

    /**
     * Add the component-wise multiplication of <code>this * a</code> to <code>b</code>
     * and store the result in <code>this</code>.
     * 
     * @param a
     *          the multiplicand
     * @param b
     *          the addend
     * @return this
     */
    public Vector4d mulAdd(double a, Vector4dc b) {
        this.x = Math.fma(x, a, b.x());
        this.y = Math.fma(y, a, b.y());
        this.z = Math.fma(z, a, b.z());
        return this;
    }

    public Vector4d mulAdd(Vector4dc a, Vector4dc b, Vector4d dest) {
        dest.x = Math.fma(x, a.x(), b.x());
        dest.y = Math.fma(y, a.y(), b.y());
        dest.z = Math.fma(z, a.z(), b.z());
        return dest;
    }

    public Vector4d mulAdd(double a, Vector4dc b, Vector4d dest) {
        dest.x = Math.fma(x, a, b.x());
        dest.y = Math.fma(y, a, b.y());
        dest.z = Math.fma(z, a, b.z());
        return dest;
    }

    /**
     * Multiply this {@link Vector4d} component-wise by the given {@link Vector4d}.
     * 
     * @param v
     *          the vector to multiply by
     * @return this
     */
    public Vector4d mul(Vector4dc v) {
        this.x = x * v.x();
        this.y = y * v.y();
        this.z = z * v.z();
        this.w = w * v.w();
        return this;
    }

    public Vector4d mul(Vector4dc v, Vector4d dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        dest.z = z * v.z();
        dest.w = w * v.w();
        return dest;
    }

    /**
     * Divide this {@link Vector4d} component-wise by the given {@link Vector4dc}.
     * 
     * @param v
     *          the vector to divide by
     * @return this
     */
    public Vector4d div(Vector4dc v) {
        this.x = x / v.x();
        this.y = y / v.y();
        this.z = z / v.z();
        this.w = w / v.w();
        return this;
    }

    public Vector4d div(Vector4dc v, Vector4d dest) {
        dest.x = x / v.x();
        dest.y = y / v.y();
        dest.z = z / v.z();
        dest.w = w / v.w();
        return dest;
    }

    /**
     * Multiply this {@link Vector4d} component-wise by the given {@link Vector4fc}.
     * 
     * @param v
     *          the vector to multiply by
     * @return this
     */
    public Vector4d mul(Vector4fc v) {
        this.x = x * v.x();
        this.y = y * v.y();
        this.z = z * v.z();
        this.w = w * v.w();
        return this;
    }

    public Vector4d mul(Vector4fc v, Vector4d dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        dest.z = z * v.z();
        dest.w = w * v.w();
        return dest;
    }

    /**
     * Multiply the given matrix <code>mat</code> with this {@link Vector4d}.
     * 
     * @param mat
     *          the matrix to multiply by
     * @return this
     */
    public Vector4d mul(Matrix4dc mat) {
        if ((mat.properties() & Matrix4fc.PROPERTY_AFFINE) != 0)
            return mulAffine(mat, this);
        return mulGeneric(mat, this);
    }

    public Vector4d mul(Matrix4dc mat, Vector4d dest) {
        if ((mat.properties() & Matrix4fc.PROPERTY_AFFINE) != 0)
            return mulAffine(mat, dest);
        return mulGeneric(mat, dest);
    }

    /**
     * Multiply the transpose of the given matrix <code>mat</code> with this Vector4f and store the result in
     * <code>this</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply the vector with
     * @return this
     */
    public Vector4d mulTranspose(Matrix4dc mat) {
        if ((mat.properties() & Matrix4dc.PROPERTY_AFFINE) != 0)
            return mulAffineTranspose(mat, this);
        return mulGenericTranspose(mat, this);
    }
    public Vector4d mulTranspose(Matrix4dc mat, Vector4d dest) {
        if ((mat.properties() & Matrix4dc.PROPERTY_AFFINE) != 0)
            return mulAffineTranspose(mat, dest);
        return mulGenericTranspose(mat, dest);
    }

    public Vector4d mulAffine(Matrix4dc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = w;
        return dest;
    }

    private Vector4d mulGeneric(Matrix4dc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        double rw = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = rw;
        return dest;
    }

    public Vector4d mulAffineTranspose(Matrix4dc mat, Vector4d dest) {
        double x = this.x, y = this.y, z = this.z, w = this.w;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, mat.m02() * z));
        dest.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, mat.m12() * z));
        dest.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, mat.m22() * z));
        dest.w = Math.fma(mat.m30(), x, Math.fma(mat.m31(), y, mat.m32() * z + w));
        return dest;
    }
    private Vector4d mulGenericTranspose(Matrix4dc mat, Vector4d dest) {
        double x = this.x, y = this.y, z = this.z, w = this.w;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, Math.fma(mat.m02(), z, mat.m03() * w)));
        dest.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, Math.fma(mat.m12(), z, mat.m13() * w)));
        dest.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, Math.fma(mat.m22(), z, mat.m23() * w)));
        dest.w = Math.fma(mat.m30(), x, Math.fma(mat.m31(), y, Math.fma(mat.m32(), z, mat.m33() * w)));
        return dest;
    }

    /**
     * Multiply the given matrix mat with this Vector4d and store the result in
     * <code>this</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @return this
     */
    public Vector4d mul(Matrix4x3dc mat) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.x = rx;
        this.y = ry;
        this.z = rz;
        return this;
    }

    public Vector4d mul(Matrix4x3dc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = w;
        return dest;
    }

    /**
     * Multiply the given matrix mat with this Vector4d and store the result in
     * <code>this</code>.
     * 
     * @param mat
     *          the matrix to multiply the vector with
     * @return this
     */
    public Vector4d mul(Matrix4x3fc mat) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.x = rx;
        this.y = ry;
        this.z = rz;
        return this;
    }

    public Vector4d mul(Matrix4x3fc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = w;
        return dest;
    }

    /**
     * Multiply the given matrix <code>mat</code> with this {@link Vector4d}.
     * 
     * @param mat
     *          the matrix to multiply by
     * @return this
     */
    public Vector4d mul(Matrix4fc mat) {
        if ((mat.properties() & Matrix4fc.PROPERTY_AFFINE) != 0)
            return mulAffine(mat, this);
        return mulGeneric(mat, this);
    }

    public Vector4d mul(Matrix4fc mat, Vector4d dest) {
        if ((mat.properties() & Matrix4fc.PROPERTY_AFFINE) != 0)
            return mulAffine(mat, dest);
        return mulGeneric(mat, dest);
    }
    private Vector4d mulAffine(Matrix4fc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = w;
        return dest;
    }
    private Vector4d mulGeneric(Matrix4fc mat, Vector4d dest) {
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        double rw = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = rw;
        return dest;
    }

    public Vector4d mulProject(Matrix4dc mat, Vector4d dest) {
        double invW = 1.0 / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) * invW;
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) * invW;
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) * invW;
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        dest.w = 1.0;
        return dest;
    }

    /**
     * Multiply the given matrix <code>mat</code> with this Vector4d, perform perspective division.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector4d mulProject(Matrix4dc mat) {
        double invW = 1.0 / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) * invW;
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) * invW;
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) * invW;
        this.x = rx;
        this.y = ry;
        this.z = rz;
        this.w = 1.0;
        return this;
    }

    public Vector3d mulProject(Matrix4dc mat, Vector3d dest) {
        double invW = 1.0 / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) * invW;
        double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) * invW;
        double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) * invW;
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        return dest;
    }

    /**
     * Multiply this Vector4d by the given scalar value.
     * 
     * @param scalar
     *          the scalar to multiply by
     * @return this
     */
    public Vector4d mul(double scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        this.z = z * scalar;
        this.w = w * scalar;
        return this;
    }

    public Vector4d mul(double scalar, Vector4d dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        dest.z = z * scalar;
        dest.w = w * scalar;
        return dest;
    }

    /**
     * Divide this Vector4d by the given scalar value.
     * 
     * @param scalar
     *          the scalar to divide by
     * @return this
     */
    public Vector4d div(double scalar) {
        double inv = 1.0 / scalar;
        this.x = x * inv;
        this.y = y * inv;
        this.z = z * inv;
        this.w = w * inv;
        return this;
    }

    public Vector4d div(double scalar, Vector4d dest) {
        double inv = 1.0 / scalar;
        dest.x = x * inv;
        dest.y = y * inv;
        dest.z = z * inv;
        dest.w = w * inv;
        return dest;
    }

    /**
     * Transform this vector by the given quaternion <code>quat</code> and store the result in <code>this</code>.
     * 
     * @see Quaterniond#transform(Vector4d)
     * 
     * @param quat
     *          the quaternion to transform this vector
     * @return this
     */
    public Vector4d rotate(Quaterniondc quat) {
        quat.transform(this, this);
        return this;
    }

    public Vector4d rotate(Quaterniondc quat, Vector4d dest) {
        quat.transform(this, dest);
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the given rotation axis.
     * 
     * @param angle
     *          the angle in radians
     * @param x
     *          the x component of the rotation axis
     * @param y
     *          the y component of the rotation axis
     * @param z
     *          the z component of the rotation axis
     * @return this
     */
    public Vector4d rotateAxis(double angle, double x, double y, double z) {
        if (y == 0.0 && z == 0.0 && Math.absEqualsOne(x))
            return rotateX(x * angle, this);
        else if (x == 0.0 && z == 0.0 && Math.absEqualsOne(y))
            return rotateY(y * angle, this);
        else if (x == 0.0 && y == 0.0 && Math.absEqualsOne(z))
            return rotateZ(z * angle, this);
        return rotateAxisInternal(angle, x, y, z, this);
    }

    public Vector4d rotateAxis(double angle, double aX, double aY, double aZ, Vector4d dest) {
        if (aY == 0.0 && aZ == 0.0 && Math.absEqualsOne(aX))
            return rotateX(aX * angle, dest);
        else if (aX == 0.0 && aZ == 0.0 && Math.absEqualsOne(aY))
            return rotateY(aY * angle, dest);
        else if (aX == 0.0 && aY == 0.0 && Math.absEqualsOne(aZ))
            return rotateZ(aZ * angle, dest);
        return rotateAxisInternal(angle, aX, aY, aZ, dest);
    }
    private Vector4d rotateAxisInternal(double angle, double aX, double aY, double aZ, Vector4d dest) {
        double hangle = angle * 0.5;
        double sinAngle = Math.sin(hangle);
        double qx = aX * sinAngle, qy = aY * sinAngle, qz = aZ * sinAngle;
        double qw = Math.cosFromSin(sinAngle, hangle);
        double w2 = qw * qw, x2 = qx * qx, y2 = qy * qy, z2 = qz * qz, zw = qz * qw;
        double xy = qx * qy, xz = qx * qz, yw = qy * qw, yz = qy * qz, xw = qx * qw;
        double nx = (w2 + x2 - z2 - y2) * x + (-zw + xy - zw + xy) * y + (yw + xz + xz + yw) * z;
        double ny = (xy + zw + zw + xy) * x + ( y2 - z2 + w2 - x2) * y + (yz + yz - xw - xw) * z;
        double nz = (xz - yw + xz - yw) * x + ( yz + yz + xw + xw) * y + (z2 - y2 - x2 + w2) * z;
        dest.x = nx;
        dest.y = ny;
        dest.z = nz;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the X axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector4d rotateX(double angle) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double y = this.y * cos - this.z * sin;
        double z = this.y * sin + this.z * cos;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector4d rotateX(double angle, Vector4d dest) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double y = this.y * cos - this.z * sin;
        double z = this.y * sin + this.z * cos;
        dest.x = this.x;
        dest.y = y;
        dest.z = z;
        dest.w = this.w;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Y axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector4d rotateY(double angle) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double x =  this.x * cos + this.z * sin;
        double z = -this.x * sin + this.z * cos;
        this.x = x;
        this.z = z;
        return this;
    }

    public Vector4d rotateY(double angle, Vector4d dest) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double x =  this.x * cos + this.z * sin;
        double z = -this.x * sin + this.z * cos;
        dest.x = x;
        dest.y = this.y;
        dest.z = z;
        dest.w = this.w;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Z axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector4d rotateZ(double angle) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double x = this.x * cos - this.y * sin;
        double y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector4d rotateZ(double angle, Vector4d dest) {
        double sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        double x = this.x * cos - this.y * sin;
        double y = this.x * sin + this.y * cos;
        dest.x = x;
        dest.y = y;
        dest.z = this.z;
        dest.w = this.w;
        return dest;
    }

    public double lengthSquared() {
        return Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
    }

    /**
     * Get the length squared of a 4-dimensional double-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     * @param w The vector's w component
     *
     * @return the length squared of the given vector
     *
     * @author F. Neurath
     */
    public static double lengthSquared(double x, double y, double z, double w) {
        return Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
    }

    public double length() {
        return Math.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
    }

    /**
     * Get the length of a 4-dimensional double-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     * @param w The vector's w component
     *
     * @return the length of the given vector
     *
     * @author F. Neurath
     */
    public static double length(double x, double y, double z, double w) {
        return Math.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
    }

    /**
     * Normalizes this vector.
     * 
     * @return this
     */
    public Vector4d normalize() {
        double invLength = 1.0 / length();
        this.x = x * invLength;
        this.y = y * invLength;
        this.z = z * invLength;
        this.w = w * invLength;
        return this;
    }

    public Vector4d normalize(Vector4d dest) {
        double invLength = 1.0 / length();
        dest.x = x * invLength;
        dest.y = y * invLength;
        dest.z = z * invLength;
        dest.w = w * invLength;
        return dest;
    }

    /**
     * Scale this vector to have the given length.
     * 
     * @param length
     *          the desired length
     * @return this
     */
    public Vector4d normalize(double length) {
        double invLength = 1.0 / length() * length;
        this.x = x * invLength;
        this.y = y * invLength;
        this.z = z * invLength;
        this.w = w * invLength;
        return this;
    }

    public Vector4d normalize(double length, Vector4d dest) {
        double invLength = 1.0 / length() * length;
        dest.x = x * invLength;
        dest.y = y * invLength;
        dest.z = z * invLength;
        dest.w = w * invLength;
        return dest;
    }

    /**
     * Normalize this vector by computing only the norm of <code>(x, y, z)</code>.
     * 
     * @return this
     */
    public Vector4d normalize3() {
        double invLength = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
        this.x = x * invLength;
        this.y = y * invLength;
        this.z = z * invLength;
        this.w = w * invLength;
        return this;
    }

    public Vector4d normalize3(Vector4d dest) {
        double invLength = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
        dest.x = x * invLength;
        dest.y = y * invLength;
        dest.z = z * invLength;
        dest.w = w * invLength;
        return dest;
    }

    public double distance(Vector4dc v) {
        double dx = this.x - v.x();
        double dy = this.y - v.y();
        double dz = this.z - v.z();
        double dw = this.w - v.w();
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw))));
    }

    public double distance(double x, double y, double z, double w) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        double dw = this.w - w;
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw))));
    }

    public double distanceSquared(Vector4dc v) {
        double dx = this.x - v.x();
        double dy = this.y - v.y();
        double dz = this.z - v.z();
        double dw = this.w - v.w();
        return Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw)));
    }

    public double distanceSquared(double x, double y, double z, double w) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        double dw = this.w - w;
        return Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw)));
    }

    /**
     * Return the distance between <code>(x1, y1, z1, w1)</code> and <code>(x2, y2, z2, w2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param z1
     *          the z component of the first vector
     * @param w1
     *          the w component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @param z2
     *          the z component of the second vector
     * @param w2
     *          the 2 component of the second vector
     * @return the euclidean distance
     */
    public static double distance(double x1, double y1, double z1, double w1, double x2, double y2, double z2, double w2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        double dw = w1 - w2;
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw))));
    }

    /**
     * Return the squared distance between <code>(x1, y1, z1, w1)</code> and <code>(x2, y2, z2, w2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param z1
     *          the z component of the first vector
     * @param w1
     *          the w component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @param z2
     *          the z component of the second vector
     * @param w2
     *          the w component of the second vector
     * @return the euclidean distance squared
     */
    public static double distanceSquared(double x1, double y1, double z1, double w1, double x2, double y2, double z2, double w2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        double dw = w1 - w2;
        return Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw)));
    }

    public double dot(Vector4dc v) {
        return Math.fma(this.x, v.x(), Math.fma(this.y, v.y(), Math.fma(this.z, v.z(), this.w * v.w())));
    }

    public double dot(double x, double y, double z, double w) {
        return Math.fma(this.x, x, Math.fma(this.y, y, Math.fma(this.z, z, this.w * w)));
    }

    public double angleCos(Vector4dc v) {
        double length1Squared = Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
        double length2Squared = Math.fma(v.x(), v.x(), Math.fma(v.y(), v.y(), Math.fma(v.z(), v.z(), v.w() * v.w())));
        double dot = Math.fma(x, v.x(), Math.fma(y, v.y(), Math.fma(z, v.z(), w * v.w())));
        return dot / Math.sqrt(length1Squared * length2Squared);
    }

    public double angle(Vector4dc v) {
        double cos = angleCos(v);
        // This is because sometimes cos goes above 1 or below -1 because of lost precision
        cos = cos < 1 ? cos : 1;
        cos = cos > -1 ? cos : -1;
        return Math.acos(cos);
    }

    /**
     * Set all components to zero.
     * 
     * @return this
     */
    public Vector4d zero() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
        return this;
    }

    /**
     * Negate this vector.
     * 
     * @return this
     */
    public Vector4d negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        this.w = -w;
        return this;
    }

    public Vector4d negate(Vector4d dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        dest.w = -w;
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector4d min(Vector4dc v) {
        this.x = x < v.x() ? x : v.x();
        this.y = y < v.y() ? y : v.y();
        this.z = z < v.z() ? z : v.z();
        this.w = w < v.w() ? w : v.w();
        return this;
    }

    public Vector4d min(Vector4dc v, Vector4d dest) {
        dest.x = x < v.x() ? x : v.x();
        dest.y = y < v.y() ? y : v.y();
        dest.z = z < v.z() ? z : v.z();
        dest.w = w < v.w() ? w : v.w();
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector4d max(Vector4dc v) {
        this.x = x > v.x() ? x : v.x();
        this.y = y > v.y() ? y : v.y();
        this.z = z > v.z() ? z : v.z();
        this.w = w > v.w() ? w : v.w();
        return this;
    }

    public Vector4d max(Vector4dc v, Vector4d dest) {
        dest.x = x > v.x() ? x : v.x();
        dest.y = y > v.y() ? y : v.y();
        dest.z = z > v.z() ? z : v.z();
        dest.w = w > v.w() ? w : v.w();
        return dest;
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
        return "(" + Runtime.format(x, formatter) + " " + Runtime.format(y, formatter) + " " + Runtime.format(z, formatter) + " " + Runtime.format(w, formatter) + ")";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        out.writeDouble(w);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
        w = in.readDouble();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(w);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector4d other = (Vector4d) obj;
        if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
            return false;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

    public boolean equals(Vector4dc v, double delta) {
        if (this == v)
            return true;
        if (v == null)
            return false;
        if (!(v instanceof Vector4dc))
            return false;
        if (!Runtime.equals(x, v.x(), delta))
            return false;
        if (!Runtime.equals(y, v.y(), delta))
            return false;
        if (!Runtime.equals(z, v.z(), delta))
            return false;
        if (!Runtime.equals(w, v.w(), delta))
            return false;
        return true;
    }

    public boolean equals(double x, double y, double z, double w) {
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(y))
            return false;
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(z))
            return false;
        if (Double.doubleToLongBits(this.w) != Double.doubleToLongBits(w))
            return false;
        return true;
    }

    public Vector4d smoothStep(Vector4dc v, double t, Vector4d dest) {
        double t2 = t * t;
        double t3 = t2 * t;
        dest.x = (x + x - v.x() - v.x()) * t3 + (3.0 * v.x() - 3.0 * x) * t2 + x * t + x;
        dest.y = (y + y - v.y() - v.y()) * t3 + (3.0 * v.y() - 3.0 * y) * t2 + y * t + y;
        dest.z = (z + z - v.z() - v.z()) * t3 + (3.0 * v.z() - 3.0 * z) * t2 + z * t + z;
        dest.w = (w + w - v.w() - v.w()) * t3 + (3.0 * v.w() - 3.0 * w) * t2 + w * t + w;
        return dest;
    }

    public Vector4d hermite(Vector4dc t0, Vector4dc v1, Vector4dc t1, double t, Vector4d dest) {
        double t2 = t * t;
        double t3 = t2 * t;
        dest.x = (x + x - v1.x() - v1.x() + t1.x() + t0.x()) * t3 + (3.0 * v1.x() - 3.0 * x - t0.x() - t0.x() - t1.x()) * t2 + x * t + x;
        dest.y = (y + y - v1.y() - v1.y() + t1.y() + t0.y()) * t3 + (3.0 * v1.y() - 3.0 * y - t0.y() - t0.y() - t1.y()) * t2 + y * t + y;
        dest.z = (z + z - v1.z() - v1.z() + t1.z() + t0.z()) * t3 + (3.0 * v1.z() - 3.0 * z - t0.z() - t0.z() - t1.z()) * t2 + z * t + z;
        dest.w = (w + w - v1.w() - v1.w() + t1.w() + t0.w()) * t3 + (3.0 * v1.w() - 3.0 * w - t0.w() - t0.w() - t1.w()) * t2 + w * t + w;
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
    public Vector4d lerp(Vector4dc other, double t) {
        this.x = Math.fma(other.x() - x, t, x);
        this.y = Math.fma(other.y() - y, t, y);
        this.z = Math.fma(other.z() - z, t, z);
        this.w = Math.fma(other.w() - w, t, w);
        return this;
    }

    public Vector4d lerp(Vector4dc other, double t, Vector4d dest) {
        dest.x = Math.fma(other.x() - x, t, x);
        dest.y = Math.fma(other.y() - y, t, y);
        dest.z = Math.fma(other.z() - z, t, z);
        dest.w = Math.fma(other.w() - w, t, w);
        return dest;
    }

    public double get(int component) throws IllegalArgumentException {
        switch (component) {
        case 0:
            return x;
        case 1:
            return y;
        case 2:
            return z;
        case 3:
            return w;
        default:
            throw new IllegalArgumentException();
        }
    }

    public Vector4i get(int mode, Vector4i dest) {
        dest.x = Math.roundUsing(this.x(), mode);
        dest.y = Math.roundUsing(this.y(), mode);
        dest.z = Math.roundUsing(this.z(), mode);
        dest.w = Math.roundUsing(this.w(), mode);
        return dest;
    }

    public Vector4f get(Vector4f dest) {
        dest.x = (float) this.x();
        dest.y = (float) this.y();
        dest.z = (float) this.z();
        dest.w = (float) this.w();
        return dest;
    }

    public Vector4d get(Vector4d dest) {
        dest.x = this.x();
        dest.y = this.y();
        dest.z = this.z();
        dest.w = this.w();
        return dest;
    }

    public int maxComponent() {
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        double absW = Math.abs(w);
        if (absX >= absY && absX >= absZ && absX >= absW) {
            return 0;
        } else if (absY >= absZ && absY >= absW) {
            return 1;
        } else if (absZ >= absW) {
            return 2;
        }
        return 3;
    }

    public int minComponent() {
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        double absW = Math.abs(w);
        if (absX < absY && absX < absZ && absX < absW) {
            return 0;
        } else if (absY < absZ && absY < absW) {
            return 1;
        } else if (absZ < absW) {
            return 2;
        }
        return 3;
    }

    /**
     * Set each component of this vector to the largest (closest to positive
     * infinity) {@code double} value that is less than or equal to that
     * component and is equal to a mathematical integer.
     *
     * @return this
     */
    public Vector4d floor() {
        this.x = Math.floor(x);
        this.y = Math.floor(y);
        this.z = Math.floor(z);
        this.w = Math.floor(w);
        return this;
    }

    public Vector4d floor(Vector4d dest) {
        dest.x = Math.floor(x);
        dest.y = Math.floor(y);
        dest.z = Math.floor(z);
        dest.w = Math.floor(w);
        return dest;
    }

    /**
     * Set each component of this vector to the smallest (closest to negative
     * infinity) {@code double} value that is greater than or equal to that
     * component and is equal to a mathematical integer.
     *
     * @return this
     */
    public Vector4d ceil() {
        this.x = Math.ceil(x);
        this.y = Math.ceil(y);
        this.z = Math.ceil(z);
        this.w = Math.ceil(w);
        return this;
    }

    public Vector4d ceil(Vector4d dest) {
        dest.x = Math.ceil(x);
        dest.y = Math.ceil(y);
        dest.z = Math.ceil(z);
        dest.w = Math.ceil(w);
        return dest;
    }

    /**
     * Set each component of this vector to the closest double that is equal to
     * a mathematical integer, with ties rounding to positive infinity.
     *
     * @return this
     */
    public Vector4d round() {
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.z = Math.round(z);
        this.w = Math.round(w);
        return this;
    }

    public Vector4d round(Vector4d dest) {
        dest.x = Math.round(x);
        dest.y = Math.round(y);
        dest.z = Math.round(z);
        dest.w = Math.round(w);
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(x) && Math.isFinite(y) && Math.isFinite(z) && Math.isFinite(w);
    }

    /**
     * Compute the absolute of each of this vector's components.
     * 
     * @return this
     */
    public Vector4d absolute() {
        this.x = Math.abs(x);
        this.y = Math.abs(y);
        this.z = Math.abs(z);
        this.w = Math.abs(w);
        return this;
    }

    public Vector4d absolute(Vector4d dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        dest.z = Math.abs(z);
        dest.w = Math.abs(w);
        return dest;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
