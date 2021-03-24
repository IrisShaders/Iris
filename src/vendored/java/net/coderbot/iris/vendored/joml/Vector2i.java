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
 * Represents a 2D vector with single-precision.
 *
 * @author RGreenlees
 * @author Kai Burjack
 * @author Hans Uhlig
 */
public class Vector2i implements Externalizable, Cloneable, Vector2ic {

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
     * Create a new {@link Vector2i} and initialize its components to zero.
     */
    public Vector2i() {
    }

    /**
     * Create a new {@link Vector2i} and initialize both of its components with
     * the given value.
     *
     * @param s
     *          the value of both components
     */
    public Vector2i(int s) {
        this.x = s;
        this.y = s;
    }

    /**
     * Create a new {@link Vector2i} and initialize its components to the given values.
     *
     * @param x
     *          the x component
     * @param y
     *          the y component
     */
    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new {@link Vector2i} and initialize its component values and
     * round using the given {@link RoundingMode}.
     * @param x
     *          the x component
     * @param y
     *          the y component
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector2i(float x, float y, int mode) {
        this.x = Math.roundUsing(x, mode);
        this.y = Math.roundUsing(y, mode);
    }

    /**
     * Create a new {@link Vector2i} and initialize its component values and
     * round using the given {@link RoundingMode}.
     * @param x
     *          the x component
     * @param y
     *          the y component
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector2i(double x, double y, int mode) {
        this.x = Math.roundUsing(x, mode);
        this.y = Math.roundUsing(y, mode);
    }

    /**
     * Create a new {@link Vector2i} and initialize its components to the one of
     * the given vector.
     *
     * @param v
     *          the {@link Vector2ic} to copy the values from
     */
    public Vector2i(Vector2ic v) {
        x = v.x();
        y = v.y();
    }

    /**
     * Create a new {@link Vector2i} and initialize its components to the rounded value of
     * the given vector.
     *
     * @param v
     *          the {@link Vector2fc} to round and copy the values from
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector2i(Vector2fc v, int mode) {
        x = Math.roundUsing(v.x(), mode);
        y = Math.roundUsing(v.y(), mode);
    }

    /**
     * Create a new {@link Vector2i} and initialize its components to the rounded value of
     * the given vector.
     *
     * @param v
     *          the {@link Vector2dc} to round and copy the values from
     * @param mode
     *          the {@link RoundingMode} to use
     */
    public Vector2i(Vector2dc v, int mode) {
        x = Math.roundUsing(v.x(), mode);
        y = Math.roundUsing(v.y(), mode);
    }

    /**
     * Create a new {@link Vector2i} and initialize its two components from the first
     * two elements of the given array.
     * 
     * @param xy
     *          the array containing at least three elements
     */
    public Vector2i(int[] xy) {
        this.x = xy[0];
        this.y = xy[1];
    }


    /**
     * Create a new {@link Vector2i} and read this vector from the supplied
     * {@link ByteBuffer} at the current buffer
     * {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which the vector is
     * read, use {@link #Vector2i(int, ByteBuffer)}, taking the absolute
     * position as parameter.
     *
     * @see #Vector2i(int, ByteBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y</code> order
     */
    public Vector2i(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector2i} and read this vector from the supplied
     * {@link ByteBuffer} starting at the specified absolute buffer
     * position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index
     *          the absolute position into the ByteBuffer
     * @param buffer
     *          values will be read in <code>x, y</code> order
     */
    public Vector2i(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    /**
     * Create a new {@link Vector2i} and read this vector from the supplied
     * {@link IntBuffer} at the current buffer
     * {@link IntBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     * <p>
     * In order to specify the offset into the IntBuffer at which the vector is
     * read, use {@link #Vector2i(int, IntBuffer)}, taking the absolute position
     * as parameter.
     *
     * @see #Vector2i(int, IntBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y</code> order
     */
    public Vector2i(IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector2i} and read this vector from the supplied
     * {@link IntBuffer} starting at the specified absolute buffer
     * position/index.
     * <p>
     * This method will not increment the position of the given IntBuffer.
     *
     * @param index
     *          the absolute position into the IntBuffer
     * @param buffer
     *          values will be read in <code>x, y</code> order
     */
    public Vector2i(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }


    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    /**
     * Set the x and y components to the supplied value.
     *
     * @param s
     *          scalar value of both components
     * @return this
     */
    public Vector2i set(int s) {
        this.x = s;
        this.y = s;
        return this;
    }

    /**
     * Set the x and y components to the supplied values.
     *
     * @param x
     *          the x component
     * @param y
     *          the y component
     * @return this
     */
    public Vector2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Set this {@link Vector2i} to the values of v.
     *
     * @param v
     *          the vector to copy from
     * @return this
     */
    public Vector2i set(Vector2ic v) {
        this.x = v.x();
        this.y = v.y();
        return this;
    }

    /**
     * Set this {@link Vector2i} to the values of v using {@link RoundingMode#TRUNCATE} rounding.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components
     * in double-precision, there is the possibility to lose precision.
     *
     * @param v
     *          the vector to copy from
     * @return this
     */
    public Vector2i set(Vector2dc v) {
        this.x = (int) v.x();
        this.y = (int) v.y();
        return this;
    }

    /**
     * Set this {@link Vector2i} to the values of v using the given {@link RoundingMode}.
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
    public Vector2i set(Vector2dc v, int mode) {
        this.x = Math.roundUsing(v.x(), mode);
        this.y = Math.roundUsing(v.y(), mode);
        return this;
    }

    /**
     * Set this {@link Vector2i} to the values of v using the given {@link RoundingMode}.
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
    public Vector2i set(Vector2fc v, int mode) {
        this.x = Math.roundUsing(v.x(), mode);
        this.y = Math.roundUsing(v.y(), mode);
        return this;
    }

    /**
     * Set the two components of this vector to the first two elements of the given array.
     * 
     * @param xy
     *          the array containing at least two elements
     * @return this
     */
    public Vector2i set(int[] xy) {
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
     * In order to specify the offset into the ByteBuffer at which the vector is
     * read, use {@link #set(int, ByteBuffer)}, taking the absolute position as
     * parameter.
     *
     * @see #set(int, ByteBuffer)
     *
     * @param buffer
     *          values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2i set(ByteBuffer buffer) {
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
     *          values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2i set(int index, ByteBuffer buffer) {
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
     *          values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2i set(IntBuffer buffer) {
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
     *          values will be read in <code>x, y</code> order
     * @return this
     */
    public Vector2i set(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    public int get(int component) throws IllegalArgumentException {
        switch (component) {
        case 0:
            return x;
        case 1:
            return y;
        default:
            throw new IllegalArgumentException();
        }
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
    public Vector2i setComponent(int component, int value) throws IllegalArgumentException {
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

    public IntBuffer get(IntBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public IntBuffer get(int index, IntBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }


    /**
     * Subtract the supplied vector from this one and store the result in
     * <code>this</code>.
     *
     * @param v
     *          the vector to subtract
     * @return this
     */
    public Vector2i sub(Vector2ic v) {
        this.x = x - v.x();
        this.y = y - v.y();
        return this;
    }

    public Vector2i sub(Vector2ic v, Vector2i dest) {
        dest.x = x - v.x();
        dest.y = y - v.y();
        return dest;
    }

    /**
     * Decrement the components of this vector by the given values.
     *
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @return this
     */
    public Vector2i sub(int x, int y) {
        this.x = this.x - x;
        this.y = this.y - y;
        return this;
    }

    public Vector2i sub(int x, int y, Vector2i dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    public long lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Get the length squared of a 2-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     *
     * @return the length squared of the given vector
     */
    public static long lengthSquared(int x, int y) {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Get the length of a 2-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     *
     * @return the length squared of the given vector
     */
    public static double length(int x, int y) {
        return Math.sqrt(x * x + y * y);
    }

    public double distance(Vector2ic v) {
        int dx = this.x - v.x();
        int dy = this.y - v.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distance(int x, int y) {
        int dx = this.x - x;
        int dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public long distanceSquared(Vector2ic v) {
        int dx = this.x - v.x();
        int dy = this.y - v.y();
        return dx * dx + dy * dy;
    }

    public long distanceSquared(int x, int y) {
        int dx = this.x - x;
        int dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public long gridDistance(Vector2ic v) {
        return Math.abs(v.x() - x()) + Math.abs(v.y() - y());
    }

    public long gridDistance(int x, int y) {
        return Math.abs(x - x()) + Math.abs(y - y());
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
    public static double distance(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
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
    public static long distanceSquared(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }
    
    /**
     * Add <code>v</code> to this vector.
     *
     * @param v
     *          the vector to add
     * @return this
     */
    public Vector2i add(Vector2ic v) {
        this.x = x + v.x();
        this.y = y + v.y();
        return this;
    }

    public Vector2i add(Vector2ic v, Vector2i dest) {
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
    public Vector2i add(int x, int y) {
        this.x = this.x + x;
        this.y = this.y + y;
        return this;
    }

    public Vector2i add(int x, int y, Vector2i dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    /**
     * Multiply all components of this {@link Vector2i} by the given scalar
     * value.
     * 
     * @param scalar
     *          the scalar to multiply this vector by
     * @return this
     */
    public Vector2i mul(int scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        return this;
    }

    public Vector2i mul(int scalar, Vector2i dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        return dest;
    }

    /**
     * Add the supplied vector by this one.
     *
     * @param v
     *          the vector to multiply
     * @return this
     */
    public Vector2i mul(Vector2ic v) {
        this.x = x * v.x();
        this.y = y * v.y();
        return this;
    }

    public Vector2i mul(Vector2ic v, Vector2i dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        return dest;
    }

    /**
     * Multiply the components of this vector by the given values.
     *
     * @param x
     *          the x component to multiply
     * @param y
     *          the y component to multiply
     * @return this
     */
    public Vector2i mul(int x, int y) {
        this.x = this.x * x;
        this.y = this.y * y;
        return this;
    }

    public Vector2i mul(int x, int y, Vector2i dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    /**
     * Divide all components of this {@link Vector2i} by the given scalar value.
     *
     * @param scalar
     *          the scalar to divide by
     * @return a vector holding the result
     */
    public Vector2i div(float scalar) {
        float invscalar = 1.0f / scalar;
        this.x = (int) (x * invscalar);
        this.y = (int) (y * invscalar);
        return this;
    }

    public Vector2i div(float scalar, Vector2i dest) {
        float invscalar = 1.0f / scalar;
        dest.x = (int) (x * invscalar);
        dest.y = (int) (y * invscalar);
        return dest;
    }

    /**
     * Divide all components of this {@link Vector2i} by the given scalar value.
     *
     * @param scalar
     *          the scalar to divide by
     * @return a vector holding the result
     */
    public Vector2i div(int scalar) {
        this.x = x / scalar;
        this.y = y / scalar;
        return this;
    }

    public Vector2i div(int scalar, Vector2i dest) {
        dest.x = x / scalar;
        dest.y = y / scalar;
        return dest;
    }
    
    /**
     * Set all components to zero.
     *
     * @return this
     */
    public Vector2i zero() {
        this.x = 0;
        this.y = 0;
        return this;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readInt();
        y = in.readInt();
    }

    /**
     * Negate this vector.
     *
     * @return this
     */
    public Vector2i negate() {
        this.x = -x;
        this.y = -y;
        return this;
    }

    public Vector2i negate(Vector2i dest) {
        dest.x = -x;
        dest.y = -y;
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector2i min(Vector2ic v) {
        this.x = x < v.x() ? x : v.x();
        this.y = y < v.y() ? y : v.y();
        return this;
    }

    public Vector2i min(Vector2ic v, Vector2i dest) {
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
    public Vector2i max(Vector2ic v) {
        this.x = x > v.x() ? x : v.x();
        this.y = y > v.y() ? y : v.y();
        return this;
    }

    public Vector2i max(Vector2ic v, Vector2i dest) {
        dest.x = x > v.x() ? x : v.x();
        dest.y = y > v.y() ? y : v.y();
        return dest;
    }

    public int maxComponent() {
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        if (absX >= absY)
            return 0;
        return 1;
    }

    public int minComponent() {
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        if (absX < absY)
            return 0;
        return 1;
    }

    /**
     * Set <code>this</code> vector's components to their respective absolute values.
     * 
     * @return this
     */
    public Vector2i absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vector2i absolute(Vector2i dest) {
        dest.x = Math.abs(this.x);
        dest.y = Math.abs(this.y);
        return dest;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
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
        Vector2i other = (Vector2i) obj;
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        return true;
    }

    public boolean equals(int x, int y) {
        if (this.x != x)
            return false;
        if (this.y != y)
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
        return "(" + formatter.format(x) + " " + formatter.format(y) + ")";
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
