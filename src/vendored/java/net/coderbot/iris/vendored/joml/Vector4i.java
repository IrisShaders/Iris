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
import java.nio.IntBuffer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the definition of a Vector comprising 4 ints and associated
 * transformations.
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 * @author Hans Uhlig
 */
public class Vector4i implements Externalizable, Cloneable, Vector4ic {

    private static final long serialVersionUID = 1L;

    /**
     * The x component of the vector.
     */
    public int x;
    /**
     * The y component of the vector.
     */
    public int y;
    /**
     * The z component of the vector.
     */
    public int z;
    /**
     * The w component of the vector.
     */
    public int w;

    /**
     * Create a new {@link Vector4i} of <code>(0, 0, 0, 1)</code>.
     */
    public Vector4i() {
        this.w = 1;
    }

    /**
     * Create a new {@link Vector4i} with the same values as <code>v</code>.
     *
     * @param v
     *          the {@link Vector4ic} to copy the values from
     */
    public Vector4i(Vector4ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
    }

    /**
     * Create a new {@link Vector4i} with the first three components from the
     * given <code>v</code> and the given <code>w</code>.
     *
     * @param v
     *          the {@link Vector3ic}
     * @param w
     *          the w component
     */
    public Vector4i(Vector3ic v, int w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
    }

    /**
     * Create a new {@link Vector4i} with the first two components from the
     * given <code>v</code> and the given <code>z</code>, and <code>w</code>.
     *
     * @param v
     *          the {@link Vector2ic}
     * @param z
     *          the z component
     * @param w
     *          the w component
     */
    public Vector4i(Vector2ic v, int z, int w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4i} with the first three components from the
     * given <code>v</code> and the given <code>w</code> and round using the given {@link RoundingMode}.
     *
     * @param v
     *          the {@link Vector3fc} to copy the values from
     * @param w
     *          the w component
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector4i(Vector3fc v, float w, int mode) {
        x = Math.roundUsing(v.x(), mode);
        y = Math.roundUsing(v.y(), mode);
        z = Math.roundUsing(v.z(), mode);
        w = Math.roundUsing(w, mode);
    }

    /**
     * Create a new {@link Vector4i} and initialize its components to the rounded value of
     * the given vector.
     *
     * @param v
     *          the {@link Vector4fc} to round and copy the values from
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector4i(Vector4fc v, int mode) {
        x = Math.roundUsing(v.x(), mode);
        y = Math.roundUsing(v.y(), mode);
        z = Math.roundUsing(v.z(), mode);
        w = Math.roundUsing(v.w(), mode);
    }

    /**
     * Create a new {@link Vector4i} and initialize its components to the rounded value of
     * the given vector.
     *
     * @param v
     *          the {@link Vector4dc} to round and copy the values from
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector4i(Vector4dc v, int mode) {
        x = Math.roundUsing(v.x(), mode);
        y = Math.roundUsing(v.y(), mode);
        z = Math.roundUsing(v.z(), mode);
        w = Math.roundUsing(v.w(), mode);
    }

    /**
     * Create a new {@link Vector4i} and initialize all four components with the
     * given value.
     *
     * @param s
     *          scalar value of all four components
     */
    public Vector4i(int s) {
        this.x = s;
        this.y = s;
        this.z = s;
        this.w = s;
    }

    /**
     * Create a new {@link Vector4i} with the given component values.
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
    public Vector4i(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Vector4i} and initialize its four components from the first
     * four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     */
    public Vector4i(int[] xyzw) {
        this.x = xyzw[0];
        this.y = xyzw[1];
        this.z = xyzw[2];
        this.w = xyzw[3];
    }


    /**
     * Create a new {@link Vector4i} and read this vector from the supplied
     * {@link ByteBuffer} at the current buffer
     * {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which the vector is
     * read, use {@link #Vector4i(int, ByteBuffer)}, taking the absolute
     * position as parameter.
     *
     * @see #Vector4i(int, ByteBuffer)
     * 
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     */
    public Vector4i(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector4i} and read this vector from the supplied
     * {@link ByteBuffer} starting at the specified absolute buffer
     * position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     */
    public Vector4i(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    /**
     * Create a new {@link Vector4i} and read this vector from the supplied
     * {@link IntBuffer} at the current buffer
     * {@link IntBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     * <p>
     * In order to specify the offset into the IntBuffer at which the vector is
     * read, use {@link #Vector4i(int, IntBuffer)}, taking the absolute position
     * as parameter.
     *
     * @see #Vector4i(int, IntBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     */
    public Vector4i(IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector4i} and read this vector from the supplied
     * {@link IntBuffer} starting at the specified absolute buffer
     * position/index.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     *
     * @param index
     *          the absolute position into the IntBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     */
    public Vector4i(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }


    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int z() {
        return this.z;
    }

    public int w() {
        return this.w;
    }

    /**
     * Set this {@link Vector4i} to the values of the given <code>v</code>.
     *
     * @param v
     *          the vector whose values will be copied into this
     * @return this
     */
    public Vector4i set(Vector4ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = v.w();
        return this;
    }

    /**
     * Set this {@link Vector4i} to the values of v using {@link RoundingMode#TRUNCATE} rounding.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components
     * in double-precision, there is the possibility to lose precision.
     *
     * @param v
     *          the vector to copy from
     * @return this
     */
    public Vector4i set(Vector4dc v) {
        this.x = (int) v.x();
        this.y = (int) v.y();
        this.z = (int) v.z();
        this.w = (int) v.w();
        return this;
    }

    /**
     * Set this {@link Vector4i} to the values of v using the given {@link RoundingMode}.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components
     * in double-precision, there is the possibility to lose precision.
     *
     * @param v
     *          the vector to copy from
     * @param mode
     *          the {@link RoundingMode} to use
     * @return this
     */
    public Vector4i set(Vector4dc v, int mode) {
        this.x = Math.roundUsing(v.x(), mode);
        this.y = Math.roundUsing(v.y(), mode);
        this.z = Math.roundUsing(v.z(), mode);
        this.w = Math.roundUsing(v.w(), mode);
        return this;
    }

    /**
     * Set this {@link Vector4i} to the values of v using the given {@link RoundingMode}.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components
     * in double-precision, there is the possibility to lose precision.
     *
     * @param v
     *          the vector to copy from
     * @param mode
     *          the {@link RoundingMode} to use
     * @return this
     */
    public Vector4i set(Vector4fc v, int mode) {
        this.x = Math.roundUsing(v.x(), mode);
        this.y = Math.roundUsing(v.y(), mode);
        this.z = Math.roundUsing(v.z(), mode);
        this.w = Math.roundUsing(v.w(), mode);
        return this;
    }

    /**
     * Set the first three components of this to the components of
     * <code>v</code> and the last component to <code>w</code>.
     *
     * @param v
     *          the {@link Vector3ic} to copy
     * @param w
     *          the w component
     * @return this
     */
    public Vector4i set(Vector3ic v, int w) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        this.w = w;
        return this;
    }

    /**
     * Sets the first two components of this to the components of given
     * <code>v</code> and last two components to the given <code>z</code>, and
     * <code>w</code>.
     *
     * @param v
     *          the {@link Vector2ic}
     * @param z
     *          the z component
     * @param w
     *          the w component
     * @return this
     */
    public Vector4i set(Vector2ic v, int z, int w) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the x, y, z, and w components to the supplied value.
     *
     * @param s
     *          the value of all four components
     * @return this
     */
    public Vector4i set(int s) {
        this.x = s;
        this.y = s;
        this.z = s;
        this.w = s;
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
    public Vector4i set(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the four components of this vector to the first four elements of the given array.
     * 
     * @param xyzw
     *          the array containing at least four elements
     * @return this
     */
    public Vector4i set(int[] xyzw) {
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
     * In order to specify the offset into the ByteBuffer at which the vector is
     * read, use {@link #set(int, ByteBuffer)}, taking the absolute position as
     * parameter.
     *
     * @see #set(int, ByteBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4i set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link ByteBuffer} starting at the
     * specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4i set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link IntBuffer} at the current
     * buffer {@link IntBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     * <p>
     * In order to specify the offset into the IntBuffer at which the vector is
     * read, use {@link #set(int, IntBuffer)}, taking the absolute position as
     * parameter.
     *
     * @see #set(int, IntBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4i set(IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    /**
     * Read this vector from the supplied {@link IntBuffer} starting at the
     * specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     *
     * @param index
     *          the absolute position into the IntBuffer
     * @param buffer
     *          values will be read in <code>x, y, z, w</code> order
     * @return this
     */
    public Vector4i set(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    public int get(int component) throws IllegalArgumentException {
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

    public int maxComponent() {
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        int absZ = Math.abs(z);
        int absW = Math.abs(w);
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
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        int absZ = Math.abs(z);
        int absW = Math.abs(w);
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
     * Set the value of the specified component of this vector.
     *
     * @param component
     *          the component whose value to set, within <code>[0..3]</code>
     * @param value
     *          the value to set
     * @return this
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..3]</code>
     */
    public Vector4i setComponent(int component, int value) throws IllegalArgumentException {
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


    public IntBuffer get(IntBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public IntBuffer get(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    public ByteBuffer get(ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public ByteBuffer get(int index, ByteBuffer buffer) {
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
    public Vector4i sub(Vector4ic v) {
        this.x = this.x - v.x();
        this.y = this.y - v.y();
        this.z = this.z - v.z();
        this.w = this.w - v.w();
        return this;
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
    public Vector4i sub(int x, int y, int z, int w) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.z = this.z - z;
        this.w = this.w - w;
        return this;
    }

    public Vector4i sub(Vector4ic v, Vector4i dest) {
        dest.x = this.x - v.x();
        dest.y = this.y - v.y();
        dest.z = this.z - v.z();
        dest.w = this.w - v.w();
        return dest;
    }

    public Vector4i sub(int x, int y, int z, int w, Vector4i dest) {
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
    public Vector4i add(Vector4ic v) {
        this.x = this.x + v.x();
        this.y = this.y + v.y();
        this.z = this.z + v.z();
        this.w = this.w + v.w();
        return this;
    }

    public Vector4i add(Vector4ic v, Vector4i dest) {
        dest.x = this.x + v.x();
        dest.y = this.y + v.y();
        dest.z = this.z + v.z();
        dest.w = this.w + v.w();
        return dest;
    }

    /**
     * Increment the components of this vector by the given values.
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
    public Vector4i add(int x, int y, int z, int w) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        this.w = this.w + w;
        return this;
    }

    public Vector4i add(int x, int y, int z, int w, Vector4i dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        dest.w = this.w + w;
        return dest;
    }

    /**
     * Multiply this Vector4i component-wise by another Vector4i.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector4i mul(Vector4ic v) {
        this.x = x * v.x();
        this.y = y * v.y();
        this.z = z * v.z();
        this.w = w * v.w();
        return this;
    }

    public Vector4i mul(Vector4ic v, Vector4i dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        dest.z = z * v.z();
        dest.w = w * v.w();
        return dest;
    }

    /**
     * Divide this Vector4i component-wise by another Vector4i.
     *
     * @param v
     *          the vector to divide by
     * @return this
     */
    public Vector4i div(Vector4ic v) {
        this.x = x / v.x();
        this.y = y / v.y();
        this.z = z / v.z();
        this.w = w / v.w();
        return this;
    }

    public Vector4i div(Vector4ic v, Vector4i dest) {
        dest.x = x / v.x();
        dest.y = y / v.y();
        dest.z = z / v.z();
        dest.w = w / v.w();
        return dest;
    }

    /**
     * Multiply all components of this {@link Vector4i} by the given scalar
     * value.
     *
     * @param scalar
     *          the scalar to multiply by
     * @return this
     */
    public Vector4i mul(int scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        this.z = z * scalar;
        this.w = w * scalar;
        return this;
    }

    public Vector4i mul(int scalar, Vector4i dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        dest.z = z * scalar;
        dest.w = w * scalar;
        return dest;
    }

    /**
     * Divide all components of this {@link Vector3i} by the given scalar value.
     *
     * @param scalar
     *          the scalar to divide by
     * @return this
     */
    public Vector4i div(float scalar) {
        float invscalar = 1.0f / scalar;
        this.x = (int) (x * invscalar);
        this.y = (int) (y * invscalar);
        this.z = (int) (z * invscalar);
        this.w = (int) (w * invscalar);
        return this;
    }

    public Vector4i div(float scalar, Vector4i dest) {
        float invscalar = 1.0f / scalar;
        dest.x = (int) (x * invscalar);
        dest.y = (int) (y * invscalar);
        dest.z = (int) (z * invscalar);
        dest.w = (int) (w * invscalar);
        return dest;
    }

    /**
     * Divide all components of this {@link Vector4i} by the given scalar value.
     *
     * @param scalar
     *          the scalar to divide by
     * @return this
     */
    public Vector4i div(int scalar) {
        this.x = x / scalar;
        this.y = y / scalar;
        this.z = z / scalar;
        this.w = w / scalar;
        return this;
    }

    public Vector4i div(int scalar, Vector4i dest) {
        dest.x = x / scalar;
        dest.y = y / scalar;
        dest.z = z / scalar;
        dest.w = w / scalar;
        return dest;
    }

    public long lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Get the length squared of a 4-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     * @param w The vector's w component
     *
     * @return the length squared of the given vector
     */
    public static long lengthSquared(int x, int y, int z, int w) {
        return x * x + y * y + z * z + w * w;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Get the length of a 4-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     * @param w The vector's w component
     *
     * @return the length squared of the given vector
     */
    public static double length(int x, int y, int z, int w) {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public double distance(Vector4ic v) {
        int dx = this.x - v.x();
        int dy = this.y - v.y();
        int dz = this.z - v.z();
        int dw = this.w - v.w();
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw))));
    }

    public double distance(int x, int y, int z, int w) {
        int dx = this.x - x;
        int dy = this.y - y;
        int dz = this.z - z;
        int dw = this.w - w;
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, Math.fma(dz, dz, dw * dw))));
    }

    public long gridDistance(Vector4ic v) {
        return Math.abs(v.x() - x()) + Math.abs(v.y() - y())  + Math.abs(v.z() - z())  + Math.abs(v.w() - w());
    }

    public long gridDistance(int x, int y, int z, int w) {
        return Math.abs(x - x()) + Math.abs(y - y()) + Math.abs(z - z()) + Math.abs(w - w());
    }

    public int distanceSquared(Vector4ic v) {
        int dx = this.x - v.x();
        int dy = this.y - v.y();
        int dz = this.z - v.z();
        int dw = this.w - v.w();
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public int distanceSquared(int x, int y, int z, int w) {
        int dx = this.x - x;
        int dy = this.y - y;
        int dz = this.z - z;
        int dw = this.w - w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
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
    public static double distance(int x1, int y1, int z1, int w1, int x2, int y2, int z2, int w2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        int dz = z1 - z2;
        int dw = w1 - w2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz + dw * dw);
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
    public static long distanceSquared(int x1, int y1, int z1, int w1, int x2, int y2, int z2, int w2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        int dz = z1 - z2;
        int dw = w1 - w2;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public int dot(Vector4ic v) {
        return x * v.x() + y * v.y() + z * v.z() + w * v.w();
    }

    /**
     * Set all components to zero.
     *
     * @return this
     */
    public Vector4i zero() {
        x = 0;
        y = 0;
        z = 0;
        w = 0;
        return this;
    }

    /**
     * Negate this vector.
     *
     * @return this
     */
    public Vector4i negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        this.w = -w;
        return this;
    }

    public Vector4i negate(Vector4i dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        dest.w = -w;
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
        return "(" + formatter.format(x) + " " + formatter.format(y) + " " + formatter.format(z) + " " + formatter.format(w) + ")";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeInt(w);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();
        w = in.readInt();
    }

    /**
     * Set the components of this vector to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector4i min(Vector4ic v) {
        this.x = x < v.x() ? x : v.x();
        this.y = y < v.y() ? y : v.y();
        this.z = z < v.z() ? z : v.z();
        this.w = w < v.w() ? w : v.w();
        return this;
    }

    public Vector4i min(Vector4ic v, Vector4i dest) {
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
    public Vector4i max(Vector4ic v) {
        this.x = x > v.x() ? x : v.x();
        this.y = y > v.y() ? y : v.y();
        this.z = z > v.z() ? z : v.z();
        this.w = w > v.w() ? w : v.w();
        return this;
    }

    public Vector4i max(Vector4ic v, Vector4i dest) {
        dest.x = x > v.x() ? x : v.x();
        dest.y = y > v.y() ? y : v.y();
        dest.z = z > v.z() ? z : v.z();
        dest.w = w > v.w() ? w : v.w();
        return dest;
    }

    /**
     * Compute the absolute of each of this vector's components.
     * 
     * @return this
     */
    public Vector4i absolute() {
        this.x = Math.abs(x);
        this.y = Math.abs(y);
        this.z = Math.abs(z);
        this.w = Math.abs(w);
        return this;
    }

    public Vector4i absolute(Vector4i dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        dest.z = Math.abs(z);
        dest.w = Math.abs(w);
        return dest;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        result = prime * result + w;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vector4i other = (Vector4i) obj;
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        if (z != other.z) {
            return false;
        }
        if (w != other.w) {
            return false;
        }
        return true;
    }

    public boolean equals(int x, int y, int z, int w) {
        if (this.x != x)
            return false;
        if (this.y != y)
            return false;
        if (this.z != z)
            return false;
        if (this.w != w)
            return false;
        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
