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
 * Contains the definition of a Vector comprising 3 floats and associated
 * transformations.
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 * @author F. Neurath
 */
public class Vector3f implements Externalizable, Cloneable, Vector3fc {

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
     * The z component of the vector.
     */
    public float z;

    /**
     * Create a new {@link Vector3f} of <code>(0, 0, 0)</code>.
     */
    public Vector3f() {
    }

    /**
     * Create a new {@link Vector3f} and initialize all three components with the given value.
     *
     * @param d
     *          the value of all three components
     */
    public Vector3f(float d) {
        this.x = d;
        this.y = d;
        this.z = d;
    }

    /**
     * Create a new {@link Vector3f} with the given component values.
     * 
     * @param x
     *          the value of x
     * @param y
     *          the value of y
     * @param z
     *          the value of z
     */
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create a new {@link Vector3f} with the same values as <code>v</code>.
     * 
     * @param v
     *          the {@link Vector3fc} to copy the values from
     */
    public Vector3f(Vector3fc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
    }

    /**
     * Create a new {@link Vector3f} with the same values as <code>v</code>.
     * 
     * @param v
     *          the {@link Vector3ic} to copy the values from
     */
    public Vector3f(Vector3ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
    }

    /**
     * Create a new {@link Vector3f} with the first two components from the
     * given <code>v</code> and the given <code>z</code>
     * 
     * @param v
     *          the {@link Vector2fc} to copy the values from
     * @param z
     *          the z component
     */
    public Vector3f(Vector2fc v, float z) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
    }

    /**
     * Create a new {@link Vector3f} with the first two components from the
     * given <code>v</code> and the given <code>z</code>
     * 
     * @param v
     *          the {@link Vector2ic} to copy the values from
     * @param z
     *          the z component
     */
    public Vector3f(Vector2ic v, float z) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
    }

    /**
     * Create a new {@link Vector3f} and initialize its three components from the first
     * three elements of the given array.
     * 
     * @param xyz
     *          the array containing at least three elements
     */
    public Vector3f(float[] xyz) {
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }


    /**
     * Create a new {@link Vector3f} and read this vector from the supplied {@link ByteBuffer}
     * at the current buffer {@link ByteBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     * <p>
     * In order to specify the offset into the ByteBuffer at which
     * the vector is read, use {@link #Vector3f(int, ByteBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer values will be read in <code>x, y, z</code> order
     * @see #Vector3f(int, ByteBuffer)
     */
    public Vector3f(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector3f} and read this vector from the supplied {@link ByteBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given ByteBuffer.
     *
     * @param index  the absolute position into the ByteBuffer
     * @param buffer values will be read in <code>x, y, z</code> order
     */
    public Vector3f(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    /**
     * Create a new {@link Vector3f} and read this vector from the supplied {@link FloatBuffer}
     * at the current buffer {@link FloatBuffer#position() position}.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     * <p>
     * In order to specify the offset into the FloatBuffer at which
     * the vector is read, use {@link #Vector3f(int, FloatBuffer)}, taking
     * the absolute position as parameter.
     *
     * @param buffer values will be read in <code>x, y, z</code> order
     * @see #Vector3f(int, FloatBuffer)
     */
    public Vector3f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    /**
     * Create a new {@link Vector3f} and read this vector from the supplied {@link FloatBuffer}
     * starting at the specified absolute buffer position/index.
     * <p>
     * This method will not increment the position of the given FloatBuffer.
     *
     * @param index  the absolute position into the FloatBuffer
     * @param buffer values will be read in <code>x, y, z</code> order
     */
    public Vector3f(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }


    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    /**
     * Set the x, y and z components to match the supplied vector.
     * 
     * @param v
     *          contains the values of x, y and z to set
     * @return this
     */
    public Vector3f set(Vector3fc v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        return this;
    }

    /**
     * Set the x, y and z components to match the supplied vector.
     * <p>
     * Note that due to the given vector <code>v</code> storing the components in double-precision,
     * there is the possibility to lose precision.
     * 
     * @param v
     *          contains the values of x, y and z to set
     * @return this
     */
    public Vector3f set(Vector3dc v) {
        this.x = (float) v.x();
        this.y = (float) v.y();
        this.z = (float) v.z();
        return this;
    }

    /**
     * Set the x, y and z components to match the supplied vector.
     * 
     * @param v
     *          contains the values of x, y and z to set
     * @return this
     */
    public Vector3f set(Vector3ic v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
        return this;
    }

    /**
     * Set the first two components from the given <code>v</code>
     * and the z component from the given <code>z</code>
     *
     * @param v
     *          the {@link Vector2fc} to copy the values from
     * @param z
     *          the z component
     * @return this
     */
    public Vector3f set(Vector2fc v, float z) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        return this;
    }

    /**
     * Set the first two components from the given <code>v</code>
     * and the z component from the given <code>z</code>
     *
     * @param v
     *          the {@link Vector2dc} to copy the values from
     * @param z
     *          the z component
     * @return this
     */
    public Vector3f set(Vector2dc v, float z) {
        this.x = (float) v.x();
        this.y = (float) v.y();
        this.z = z;
        return this;
    }

    /**
     * Set the first two components from the given <code>v</code>
     * and the z component from the given <code>z</code>
     *
     * @param v
     *          the {@link Vector2ic} to copy the values from
     * @param z
     *          the z component
     * @return this
     */
    public Vector3f set(Vector2ic v, float z) {
        this.x = v.x();
        this.y = v.y();
        this.z = z;
        return this;
    }

    /**
     * Set the x, y, and z components to the supplied value.
     *
     * @param d
     *          the value of all three components
     * @return this
     */
    public Vector3f set(float d) {
        this.x = d;
        this.y = d;
        this.z = d;
        return this;
    }

    /**
     * Set the x, y and z components to the supplied values.
     * 
     * @param x
     *          the x component
     * @param y
     *          the y component 
     * @param z
     *          the z component
     * @return this
     */
    public Vector3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set the x, y, and z components to the supplied value.
     *
     * @param d
     *          the value of all three components
     * @return this
     */
    public Vector3f set(double d) {
        this.x = (float) d;
        this.y = (float) d;
        this.z = (float) d;
        return this;
    }

    /**
     * Set the x, y and z components to the supplied values.
     * 
     * @param x
     *          the x component
     * @param y
     *          the y component 
     * @param z
     *          the z component
     * @return this
     */
    public Vector3f set(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        return this;
    }

    /**
     * Set the three components of this vector to the first three elements of the given array.
     * 
     * @param xyz
     *          the array containing at least three elements
     * @return this
     */
    public Vector3f set(float[] xyz) {
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
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
     *          values will be read in <code>x, y, z</code> order
     * @return this
     * @see #set(int, ByteBuffer)
     */
    public Vector3f set(ByteBuffer buffer) {
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
     *          values will be read in <code>x, y, z</code> order
     * @return this
     */
    public Vector3f set(int index, ByteBuffer buffer) {
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
     *          values will be read in <code>x, y, z</code> order
     * @return this
     * @see #set(int, FloatBuffer)
     */
    public Vector3f set(FloatBuffer buffer) {
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
     *          the absolute position into the FloatBuffer
     * @param buffer
     *          values will be read in <code>x, y, z</code> order
     * @return this
     */
    public Vector3f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }


    /**
     * Set the value of the specified component of this vector.
     *
     * @param component
     *          the component whose value to set, within <code>[0..2]</code>
     * @param value
     *          the value to set
     * @return this
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..2]</code>
     */
    public Vector3f setComponent(int component, float value) throws IllegalArgumentException {
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
            default:
                throw new IllegalArgumentException();
        }
        return this;
    }


    public FloatBuffer get(FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    public FloatBuffer get(int index, FloatBuffer buffer) {
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
     * Subtract the supplied vector from this one and store the result in <code>this</code>.
     * 
     * @param v
     *          the vector to subtract
     * @return this
     */
    public Vector3f sub(Vector3fc v) {
        this.x = x - v.x();
        this.y = y - v.y();
        this.z = z - v.z();
        return this;
    }

    public Vector3f sub(Vector3fc v, Vector3f dest) {
        dest.x = x - v.x();
        dest.y = y - v.y();
        dest.z = z - v.z();
        return dest;
    }

    /**
     * Decrement the components of this vector by the given values.
     * 
     * @param x
     *          the x component to subtract
     * @param y
     *          the y component to subtract
     * @param z
     *          the z component to subtract
     * @return this
     */
    public Vector3f sub(float x, float y, float z) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.z = this.z - z;
        return this;
    }

    public Vector3f sub(float x, float y, float z, Vector3f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        dest.z = this.z - z;
        return dest;
    }

    /**
     * Add the supplied vector to this one.
     * 
     * @param v
     *          the vector to add
     * @return this
     */
    public Vector3f add(Vector3fc v) {
        this.x = this.x + v.x();
        this.y = this.y + v.y();
        this.z = this.z + v.z();
        return this;
    }

    public Vector3f add(Vector3fc v, Vector3f dest) {
        dest.x = this.x + v.x();
        dest.y = this.y + v.y();
        dest.z = this.z + v.z();
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
     * @return this
     */
    public Vector3f add(float x, float y, float z) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        return this;
    }

    public Vector3f add(float x, float y, float z, Vector3f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        return dest;
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
    public Vector3f fma(Vector3fc a, Vector3fc b) {
        this.x = Math.fma(a.x(), b.x(), x);
        this.y = Math.fma(a.y(), b.y(), y);
        this.z = Math.fma(a.z(), b.z(), z);
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
    public Vector3f fma(float a, Vector3fc b) {
        this.x = Math.fma(a, b.x(), x);
        this.y = Math.fma(a, b.y(), y);
        this.z = Math.fma(a, b.z(), z);
        return this;
    }

    public Vector3f fma(Vector3fc a, Vector3fc b, Vector3f dest) {
        dest.x = Math.fma(a.x(), b.x(), x);
        dest.y = Math.fma(a.y(), b.y(), y);
        dest.z = Math.fma(a.z(), b.z(), z);
        return dest;
    }

    public Vector3f fma(float a, Vector3fc b, Vector3f dest) {
        dest.x = Math.fma(a, b.x(), x);
        dest.y = Math.fma(a, b.y(), y);
        dest.z = Math.fma(a, b.z(), z);
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
    public Vector3f mulAdd(Vector3fc a, Vector3fc b) {
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
    public Vector3f mulAdd(float a, Vector3fc b) {
        this.x = Math.fma(x, a, b.x());
        this.y = Math.fma(y, a, b.y());
        this.z = Math.fma(z, a, b.z());
        return this;
    }

    public Vector3f mulAdd(Vector3fc a, Vector3fc b, Vector3f dest) {
        dest.x = Math.fma(x, a.x(), b.x());
        dest.y = Math.fma(y, a.y(), b.y());
        dest.z = Math.fma(z, a.z(), b.z());
        return dest;
    }

    public Vector3f mulAdd(float a, Vector3fc b, Vector3f dest) {
        dest.x = Math.fma(x, a, b.x());
        dest.y = Math.fma(y, a, b.y());
        dest.z = Math.fma(z, a, b.z());
        return dest;
    }

    /**
     * Multiply this Vector3f component-wise by another Vector3fc.
     * 
     * @param v
     *          the vector to multiply by
     * @return this
     */
    public Vector3f mul(Vector3fc v) {
        this.x = x * v.x();
        this.y = y * v.y();
        this.z = z * v.z();
        return this;
    }

    public Vector3f mul(Vector3fc v, Vector3f dest) {
        dest.x = x * v.x();
        dest.y = y * v.y();
        dest.z = z * v.z();
        return dest;
    }

    /**
     * Divide this Vector3f component-wise by another Vector3fc.
     * 
     * @param v
     *          the vector to divide by
     * @return this
     */
    public Vector3f div(Vector3fc v) {
        this.x = this.x / v.x();
        this.y = this.y / v.y();
        this.z = this.z / v.z();
        return this;
    }

    public Vector3f div(Vector3fc v, Vector3f dest) {
        dest.x = x / v.x();
        dest.y = y / v.y();
        dest.z = z / v.z();
        return dest;
    }

    public Vector3f mulProject(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        float invW = 1.0f / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33())));
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30()))) * invW;
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31()))) * invW;
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32()))) * invW;
        return dest;
    }

    public Vector3f mulProject(Matrix4fc mat, float w, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        float invW = 1.0f / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) * invW;
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) * invW;
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) * invW;
        return dest;
    }

    /**
     * Multiply the given matrix <code>mat</code> with this Vector3f, perform perspective division.
     * <p>
     * This method uses <code>w=1.0</code> as the fourth vector component.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulProject(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        float invW = 1.0f / Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33())));
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30()))) * invW;
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31()))) * invW;
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32()))) * invW;
        return this;
    }

    /**
     * Multiply the given matrix with this Vector3f and store the result in <code>this</code>.
     * 
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector3f mul(Matrix3fc mat) {
        float lx = x, ly = y, lz = z;
        this.x = Math.fma(mat.m00(), lx, Math.fma(mat.m10(), ly, mat.m20() * lz));
        this.y = Math.fma(mat.m01(), lx, Math.fma(mat.m11(), ly, mat.m21() * lz));
        this.z = Math.fma(mat.m02(), lx, Math.fma(mat.m12(), ly, mat.m22() * lz));
        return this;
    }

    public Vector3f mul(Matrix3fc mat, Vector3f dest) {
        float lx = x, ly = y, lz = z;
        dest.x = Math.fma(mat.m00(), lx, Math.fma(mat.m10(), ly, mat.m20() * lz));
        dest.y = Math.fma(mat.m01(), lx, Math.fma(mat.m11(), ly, mat.m21() * lz));
        dest.z = Math.fma(mat.m02(), lx, Math.fma(mat.m12(), ly, mat.m22() * lz));
        return dest;
    }

    /**
     * Multiply the given matrix with this Vector3f and store the result in <code>this</code>.
     * 
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector3f mul(Matrix3dc mat) {
        float lx = x, ly = y, lz = z;
        this.x = (float) Math.fma(mat.m00(), lx, Math.fma(mat.m10(), ly, mat.m20() * lz));
        this.y = (float) Math.fma(mat.m01(), lx, Math.fma(mat.m11(), ly, mat.m21() * lz));
        this.z = (float) Math.fma(mat.m02(), lx, Math.fma(mat.m12(), ly, mat.m22() * lz));
        return this;
    }

    public Vector3f mul(Matrix3dc mat, Vector3f dest) {
        float lx = x, ly = y, lz = z;
        dest.x = (float) Math.fma(mat.m00(), lx, Math.fma(mat.m10(), ly, mat.m20() * lz));
        dest.y = (float) Math.fma(mat.m01(), lx, Math.fma(mat.m11(), ly, mat.m21() * lz));
        dest.z = (float) Math.fma(mat.m02(), lx, Math.fma(mat.m12(), ly, mat.m22() * lz));
        return dest;
    }

    /**
     * Multiply the given matrix with this Vector3f and store the result in <code>this</code>.
     * 
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector3f mul(Matrix3x2fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        this.z = z;
        return this;
    }

    public Vector3f mul(Matrix3x2fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        dest.z = z;
        return dest;
    }

    /**
     * Multiply the transpose of the given matrix with this Vector3f store the result in <code>this</code>.
     * 
     * @param mat
     *          the matrix
     * @return this
     */
    public Vector3f mulTranspose(Matrix3fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, mat.m02() * z));
        this.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, mat.m12() * z));
        this.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, mat.m22() * z));
        return this;
    }

    public Vector3f mulTranspose(Matrix3fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, mat.m02() * z));
        dest.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, mat.m12() * z));
        dest.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, mat.m22() * z));
        return dest;
    }

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulPosition(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return this;
    }

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulPosition(Matrix4x3fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return this;
    }

    public Vector3f mulPosition(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return dest;
    }

    public Vector3f mulPosition(Matrix4x3fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return dest;
    }

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @return this
     */
    public Vector3f mulTransposePosition(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, Math.fma(mat.m02(), z, mat.m03())));
        this.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, Math.fma(mat.m12(), z, mat.m13())));
        this.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, Math.fma(mat.m22(), z, mat.m23())));
        return this;
    }

    public Vector3f mulTransposePosition(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, Math.fma(mat.m02(), z, mat.m03())));
        dest.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, Math.fma(mat.m12(), z, mat.m13())));
        dest.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, Math.fma(mat.m22(), z, mat.m23())));
        return dest;
    }

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and return the <i>w</i> component
     * of the resulting 4D vector.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return the <i>w</i> component of the resulting 4D vector after multiplication
     */
    public float mulPositionW(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        float w = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33())));
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return w;
    }

    public float mulPositionW(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        float w = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33())));
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30())));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31())));
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32())));
        return w;
    }

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulDirection(Matrix4dc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = (float) Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        this.y = (float) Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        this.z = (float) Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return this;
    }

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulDirection(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return this;
    }

    /**
     * Multiply the given 4x3 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix to multiply this vector by
     * @return this
     */
    public Vector3f mulDirection(Matrix4x3fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return this;
    }

    public Vector3f mulDirection(Matrix4dc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = (float) Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        dest.y = (float) Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        dest.z = (float) Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return dest;
    }

    public Vector3f mulDirection(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return dest;
    }

    public Vector3f mulDirection(Matrix4x3fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, mat.m20() * z));
        dest.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, mat.m21() * z));
        dest.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, mat.m22() * z));
        return dest;
    }

    /**
     * Multiply the transpose of the given 4x4 matrix <code>mat</code> with <code>this</code>.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * 
     * @param mat
     *          the matrix whose transpose to multiply this vector by
     * @return this
     */
    public Vector3f mulTransposeDirection(Matrix4fc mat) {
        float x = this.x, y = this.y, z = this.z;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, mat.m02() * z));
        this.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, mat.m12() * z));
        this.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, mat.m22() * z));
        return this;
    }

    public Vector3f mulTransposeDirection(Matrix4fc mat, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = Math.fma(mat.m00(), x, Math.fma(mat.m01(), y, mat.m02() * z));
        dest.y = Math.fma(mat.m10(), x, Math.fma(mat.m11(), y, mat.m12() * z));
        dest.z = Math.fma(mat.m20(), x, Math.fma(mat.m21(), y, mat.m22() * z));
        return dest;
    }

    /**
     * Multiply all components of this {@link Vector3f} by the given scalar
     * value.
     * 
     * @param scalar
     *          the scalar to multiply this vector by
     * @return this
     */
    public Vector3f mul(float scalar) {
        this.x = this.x * scalar;
        this.y = this.y * scalar;
        this.z = this.z * scalar;
        return this;
    }

    public Vector3f mul(float scalar, Vector3f dest) {
        dest.x = this.x * scalar;
        dest.y = this.y * scalar;
        dest.z = this.z * scalar;
        return dest;
    }

    /**
     * Multiply the components of this Vector3f by the given scalar values and store the result in <code>this</code>.
     * 
     * @param x
     *          the x component to multiply this vector by
     * @param y
     *          the y component to multiply this vector by
     * @param z
     *          the z component to multiply this vector by
     * @return this
     */
    public Vector3f mul(float x, float y, float z) {
        this.x = this.x * x;
        this.y = this.y * y;
        this.z = this.z * z;
        return this;
    }

    public Vector3f mul(float x, float y, float z, Vector3f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        dest.z = this.z * z;
        return dest;
    }

    /**
     * Divide all components of this {@link Vector3f} by the given scalar
     * value.
     * 
     * @param scalar
     *          the scalar to divide by
     * @return this
     */
    public Vector3f div(float scalar) {
        float inv = 1.0f / scalar;
        this.x = this.x * inv;
        this.y = this.y * inv;
        this.z = this.z * inv;
        return this;
    }

    public Vector3f div(float scalar, Vector3f dest) {
        float inv = 1.0f / scalar;
        dest.x = this.x * inv;
        dest.y = this.y * inv;
        dest.z = this.z * inv;
        return dest;
    }

    /**
     * Divide the components of this Vector3f by the given scalar values and store the result in <code>this</code>.
     * 
     * @param x
     *          the x component to divide this vector by
     * @param y
     *          the y component to divide this vector by
     * @param z
     *          the z component to divide this vector by
     * @return this
     */
    public Vector3f div(float x, float y, float z) {
        this.x = this.x / x;
        this.y = this.y / y;
        this.z = this.z / z;
        return this;
    }

    public Vector3f div(float x, float y, float z, Vector3f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        dest.z = this.z / z;
        return dest;
    }

    /**
     * Rotate this vector by the given quaternion <code>quat</code> and store the result in <code>this</code>.
     * 
     * @see Quaternionfc#transform(Vector3f)
     * 
     * @param quat
     *          the quaternion to rotate this vector
     * @return this
     */
    public Vector3f rotate(Quaternionfc quat) {
        return quat.transform(this, this);
    }

    public Vector3f rotate(Quaternionfc quat, Vector3f dest) {
        return quat.transform(this, dest);
    }

    public Quaternionf rotationTo(Vector3fc toDir, Quaternionf dest) {
        return dest.rotationTo(this, toDir);
    }

    public Quaternionf rotationTo(float toDirX, float toDirY, float toDirZ, Quaternionf dest) {
        return dest.rotationTo(x, y, z, toDirX, toDirY, toDirZ);
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
    public Vector3f rotateAxis(float angle, float x, float y, float z) {
        if (y == 0.0f && z == 0.0f && Math.absEqualsOne(x))
            return rotateX(x * angle, this);
        else if (x == 0.0f && z == 0.0f && Math.absEqualsOne(y))
            return rotateY(y * angle, this);
        else if (x == 0.0f && y == 0.0f && Math.absEqualsOne(z))
            return rotateZ(z * angle, this);
        return rotateAxisInternal(angle, x, y, z, this);
    }

    public Vector3f rotateAxis(float angle, float aX, float aY, float aZ, Vector3f dest) {
        if (aY == 0.0f && aZ == 0.0f && Math.absEqualsOne(aX))
            return rotateX(aX * angle, dest);
        else if (aX == 0.0f && aZ == 0.0f && Math.absEqualsOne(aY))
            return rotateY(aY * angle, dest);
        else if (aX == 0.0f && aY == 0.0f && Math.absEqualsOne(aZ))
            return rotateZ(aZ * angle, dest);
        return rotateAxisInternal(angle, aX, aY, aZ, dest);
    }
    private Vector3f rotateAxisInternal(float angle, float aX, float aY, float aZ, Vector3f dest) {
        float hangle = angle * 0.5f;
        float sinAngle = Math.sin(hangle);
        float qx = aX * sinAngle, qy = aY * sinAngle, qz = aZ * sinAngle;
        float qw = Math.cosFromSin(sinAngle, hangle);
        float w2 = qw * qw, x2 = qx * qx, y2 = qy * qy, z2 = qz * qz, zw = qz * qw;
        float xy = qx * qy, xz = qx * qz, yw = qy * qw, yz = qy * qz, xw = qx * qw;
        float x = this.x, y = this.y, z = this.z;
        dest.x = (w2 + x2 - z2 - y2) * x + (-zw + xy - zw + xy) * y + (yw + xz + xz + yw) * z;
        dest.y = (xy + zw + zw + xy) * x + ( y2 - z2 + w2 - x2) * y + (yz + yz - xw - xw) * z;
        dest.z = (xz - yw + xz - yw) * x + ( yz + yz + xw + xw) * y + (z2 - y2 - x2 + w2) * z;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the X axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector3f rotateX(float angle) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float y = this.y * cos - this.z * sin;
        float z = this.y * sin + this.z * cos;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3f rotateX(float angle, Vector3f dest) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float y = this.y * cos - this.z * sin;
        float z = this.y * sin + this.z * cos;
        dest.x = this.x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Y axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector3f rotateY(float angle) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float x =  this.x * cos + this.z * sin;
        float z = -this.x * sin + this.z * cos;
        this.x = x;
        this.z = z;
        return this;
    }

    public Vector3f rotateY(float angle, Vector3f dest) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float x =  this.x * cos + this.z * sin;
        float z = -this.x * sin + this.z * cos;
        dest.x = x;
        dest.y = this.y;
        dest.z = z;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Z axis.
     * 
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Vector3f rotateZ(float angle) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float x = this.x * cos - this.y * sin;
        float y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector3f rotateZ(float angle, Vector3f dest) {
        float sin = Math.sin(angle), cos = Math.cosFromSin(sin, angle);
        float x = this.x * cos - this.y * sin;
        float y = this.x * sin + this.y * cos;
        dest.x = x;
        dest.y = y;
        dest.z = this.z;
        return dest;
    }

    public float lengthSquared() {
        return Math.fma(x, x, Math.fma(y, y, z * z));
    }

    /**
     * Get the length squared of a 3-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     *
     * @return the length squared of the given vector
     *
     * @author F. Neurath
     */
    public static float lengthSquared(float x, float y, float z) {
        return Math.fma(x, x, Math.fma(y, y, z * z));
    }

    public float length() {
        return Math.sqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
    }

    /**
     * Get the length of a 3-dimensional single-precision vector.
     *
     * @param x The vector's x component
     * @param y The vector's y component
     * @param z The vector's z component
     *
     * @return the length of the given vector
     *
     * @author F. Neurath
     */
    public static float length(float x, float y, float z) {
        return Math.sqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
    }

    /**
     * Normalize this vector.
     * 
     * @return this
     */
    public Vector3f normalize() {
        float scalar = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
        this.x = this.x * scalar;
        this.y = this.y * scalar;
        this.z = this.z * scalar;
        return this;
    }

    public Vector3f normalize(Vector3f dest) {
        float scalar = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
        dest.x = this.x * scalar;
        dest.y = this.y * scalar;
        dest.z = this.z * scalar;
        return dest;
    }

    /**
     * Scale this vector to have the given length.
     * 
     * @param length
     *          the desired length
     * @return this
     */
    public Vector3f normalize(float length) {
        float scalar = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z))) * length;
        this.x = this.x * scalar;
        this.y = this.y * scalar;
        this.z = this.z * scalar;
        return this;
    }

    public Vector3f normalize(float length, Vector3f dest) {
        float scalar = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, z * z))) * length;
        dest.x = this.x * scalar;
        dest.y = this.y * scalar;
        dest.z = this.z * scalar;
        return dest;
    }

    /**
     * Set this vector to be the cross product of itself and <code>v</code>.
     * 
     * @param v
     *          the other vector
     * @return this
     */
    public Vector3f cross(Vector3fc v) {
        float rx = Math.fma(y, v.z(), -z * v.y());
        float ry = Math.fma(z, v.x(), -x * v.z());
        float rz = Math.fma(x, v.y(), -y * v.x());
        this.x = rx;
        this.y = ry;
        this.z = rz;
        return this;
    }

    /**
     * Set this vector to be the cross product of itself and <code>(x, y, z)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @return this
     */
    public Vector3f cross(float x, float y, float z) {
        float rx = Math.fma(this.y, z, -this.z * y);
        float ry = Math.fma(this.z, x, -this.x * z);
        float rz = Math.fma(this.x, y, -this.y * x);
        this.x = rx;
        this.y = ry;
        this.z = rz;
        return this;
    }

    public Vector3f cross(Vector3fc v, Vector3f dest) {
        float rx = Math.fma(y, v.z(), -z * v.y());
        float ry = Math.fma(z, v.x(), -x * v.z());
        float rz = Math.fma(x, v.y(), -y * v.x());
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        return dest;
    }

    public Vector3f cross(float x, float y, float z, Vector3f dest) {
        float rx = Math.fma(this.y, z, -this.z * y);
        float ry = Math.fma(this.z, x, -this.x * z);
        float rz = Math.fma(this.x, y, -this.y * x);
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        return dest;
    }

    public float distance(Vector3fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        float dz = this.z - v.z();
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    public float distanceSquared(Vector3fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        float dz = this.z - v.z();
        return Math.fma(dx, dx, Math.fma(dy, dy, dz * dz));
    }

    public float distanceSquared(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return Math.fma(dx, dx, Math.fma(dy, dy, dz * dz));
    }

    /**
     * Return the distance between <code>(x1, y1, z1)</code> and <code>(x2, y2, z2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param z1
     *          the z component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @param z2
     *          the z component of the second vector
     * @return the euclidean distance
     */
    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return Math.sqrt(distanceSquared(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Return the squared distance between <code>(x1, y1, z1)</code> and <code>(x2, y2, z2)</code>.
     *
     * @param x1
     *          the x component of the first vector
     * @param y1
     *          the y component of the first vector
     * @param z1
     *          the z component of the first vector
     * @param x2
     *          the x component of the second vector
     * @param y2
     *          the y component of the second vector
     * @param z2
     *          the z component of the second vector
     * @return the euclidean distance squared
     */
    public static float distanceSquared(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z1 - z2;
        return Math.fma(dx, dx, Math.fma(dy, dy, dz * dz));
    }

    public float dot(Vector3fc v) {
        return Math.fma(this.x, v.x(), Math.fma(this.y, v.y(), this.z * v.z()));
    }

    public float dot(float x, float y, float z) {
        return Math.fma(this.x, x, Math.fma(this.y, y, this.z * z));
    }

    public float angleCos(Vector3fc v) {
        float x = this.x, y = this.y, z = this.z;
        float length1Squared = Math.fma(x, x, Math.fma(y, y, z * z));
        float length2Squared = Math.fma(v.x(), v.x(), Math.fma(v.y(), v.y(), v.z() * v.z()));
        float dot = Math.fma(x, v.x(), Math.fma(y, v.y(), z * v.z()));
        return dot / (float)Math.sqrt(length1Squared * length2Squared);
    }

    public float angle(Vector3fc v) {
        float cos = angleCos(v);
        // This is because sometimes cos goes above 1 or below -1 because of lost precision
        cos = cos < 1 ? cos : 1;
        cos = cos > -1 ? cos : -1;
        return Math.acos(cos);
    }

    public float angleSigned(Vector3fc v, Vector3fc n) {
        return angleSigned(v.x(), v.y(), v.z(), n.x(), n.y(), n.z());
    }

    public float angleSigned(float x, float y, float z, float nx, float ny, float nz) {
        float tx = this.x, ty = this.y, tz = this.z;
        return Math.atan2(
                (ty * z - tz * y) * nx + (tz * x - tx * z) * ny + (tx * y - ty * x) * nz,
                tx * x + ty * y + tz * z);
    }

    /**
     * Set the components of this vector to be the component-wise minimum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector3f min(Vector3fc v) {
        float x = this.x, y = this.y, z = this.z;
        this.x = x < v.x() ? x : v.x();
        this.y = y < v.y() ? y : v.y();
        this.z = z < v.z() ? z : v.z();
        return this;
    }

    public Vector3f min(Vector3fc v, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = x < v.x() ? x : v.x();
        dest.y = y < v.y() ? y : v.y();
        dest.z = z < v.z() ? z : v.z();
        return dest;
    }

    /**
     * Set the components of this vector to be the component-wise maximum of this and the other vector.
     *
     * @param v
     *          the other vector
     * @return this
     */
    public Vector3f max(Vector3fc v) {
        float x = this.x, y = this.y, z = this.z;
        this.x = x > v.x() ? x : v.x();
        this.y = y > v.y() ? y : v.y();
        this.z = z > v.z() ? z : v.z();
        return this;
    }

    public Vector3f max(Vector3fc v, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        dest.x = x > v.x() ? x : v.x();
        dest.y = y > v.y() ? y : v.y();
        dest.z = z > v.z() ? z : v.z();
        return dest;
    }

    /**
     * Set all components to zero.
     * 
     * @return this
     */
    public Vector3f zero() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        return this;
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
        return "(" + Runtime.format(x, formatter) + " " + Runtime.format(y, formatter) + " " + Runtime.format(z, formatter) + ")";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        set(in.readFloat(), in.readFloat(), in.readFloat());
    }

    /**
     * Negate this vector.
     * 
     * @return this
     */
    public Vector3f negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        return this;
    }

    public Vector3f negate(Vector3f dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        return dest;
    }

    /**
     * Set <code>this</code> vector's components to their respective absolute values.
     * 
     * @return this
     */
    public Vector3f absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public Vector3f absolute(Vector3f dest) {
        dest.x = Math.abs(this.x);
        dest.y = Math.abs(this.y);
        dest.z = Math.abs(this.z);
        return dest;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        result = prime * result + Float.floatToIntBits(z);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector3f other = (Vector3f) obj;
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
            return false;
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
            return false;
        if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
            return false;
        return true;
    }

    public boolean equals(Vector3fc v, float delta) {
        if (this == v)
            return true;
        if (v == null)
            return false;
        if (!(v instanceof Vector3fc))
            return false;
        if (!Runtime.equals(x, v.x(), delta))
            return false;
        if (!Runtime.equals(y, v.y(), delta))
            return false;
        if (!Runtime.equals(z, v.z(), delta))
            return false;
        return true;
    }

    public boolean equals(float x, float y, float z) {
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(x))
            return false;
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(y))
            return false;
        if (Float.floatToIntBits(this.z) != Float.floatToIntBits(z))
            return false;
        return true;
    }

    /**
     * Reflect this vector about the given <code>normal</code> vector.
     * 
     * @param normal
     *          the vector to reflect about
     * @return this
     */
    public Vector3f reflect(Vector3fc normal) {
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();
        float dot = Math.fma(this.x, x, Math.fma(this.y, y, this.z * z));
        this.x = this.x - (dot + dot) * x;
        this.y = this.y - (dot + dot) * y;
        this.z = this.z - (dot + dot) * z;
        return this;
    }

    /**
     * Reflect this vector about the given normal vector.
     * 
     * @param x
     *          the x component of the normal
     * @param y
     *          the y component of the normal
     * @param z
     *          the z component of the normal
     * @return this
     */
    public Vector3f reflect(float x, float y, float z) {
        float dot = Math.fma(this.x, x, Math.fma(this.y, y, this.z * z));
        this.x = this.x - (dot + dot) * x;
        this.y = this.y - (dot + dot) * y;
        this.z = this.z - (dot + dot) * z;
        return this;
    }

    public Vector3f reflect(Vector3fc normal, Vector3f dest) {
        return reflect(normal.x(), normal.y(), normal.z(), dest);
    }

    public Vector3f reflect(float x, float y, float z, Vector3f dest) {
        float dot = this.dot(x, y, z);
        dest.x = this.x - (dot + dot) * x;
        dest.y = this.y - (dot + dot) * y;
        dest.z = this.z - (dot + dot) * z;
        return dest;
    }

    /**
     * Compute the half vector between this and the other vector.
     * 
     * @param other
     *          the other vector
     * @return this
     */
    public Vector3f half(Vector3fc other) {
        return this.set(this).add(other.x(), other.y(), other.z()).normalize();
    }

    /**
     * Compute the half vector between this and the vector <code>(x, y, z)</code>.
     * 
     * @param x
     *          the x component of the other vector
     * @param y
     *          the y component of the other vector
     * @param z
     *          the z component of the other vector
     * @return this
     */
    public Vector3f half(float x, float y, float z) {
        return half(x, y, z, this);
    }

    public Vector3f half(Vector3fc other, Vector3f dest) {
        return half(other.x(), other.y(), other.z(), dest);
    }

    public Vector3f half(float x, float y, float z, Vector3f dest) {
        return dest.set(this).add(x, y, z).normalize();
    }

    public Vector3f smoothStep(Vector3fc v, float t, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        float t2 = t * t;
        float t3 = t2 * t;
        dest.x = (x + x - v.x() - v.x()) * t3 + (3.0f * v.x() - 3.0f * x) * t2 + x * t + x;
        dest.y = (y + y - v.y() - v.y()) * t3 + (3.0f * v.y() - 3.0f * y) * t2 + y * t + y;
        dest.z = (z + z - v.z() - v.z()) * t3 + (3.0f * v.z() - 3.0f * z) * t2 + z * t + z;
        return dest;
    }

    public Vector3f hermite(Vector3fc t0, Vector3fc v1, Vector3fc t1, float t, Vector3f dest) {
        float x = this.x, y = this.y, z = this.z;
        float t2 = t * t;
        float t3 = t2 * t;
        dest.x = (x + x - v1.x() - v1.x() + t1.x() + t0.x()) * t3 + (3.0f * v1.x() - 3.0f * x - t0.x() - t0.x() - t1.x()) * t2 + x * t + x;
        dest.y = (y + y - v1.y() - v1.y() + t1.y() + t0.y()) * t3 + (3.0f * v1.y() - 3.0f * y - t0.y() - t0.y() - t1.y()) * t2 + y * t + y;
        dest.z = (z + z - v1.z() - v1.z() + t1.z() + t0.z()) * t3 + (3.0f * v1.z() - 3.0f * z - t0.z() - t0.z() - t1.z()) * t2 + z * t + z;
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
    public Vector3f lerp(Vector3fc other, float t) {
        return lerp(other, t, this);
    }

    public Vector3f lerp(Vector3fc other, float t, Vector3f dest) {
        dest.x = Math.fma(other.x() - x, t, x);
        dest.y = Math.fma(other.y() - y, t, y);
        dest.z = Math.fma(other.z() - z, t, z);
        return dest;
    }

    public float get(int component) throws IllegalArgumentException {
        switch (component) {
        case 0:
            return x;
        case 1:
            return y;
        case 2:
            return z;
        default:
            throw new IllegalArgumentException();
        }
    }

    public Vector3i get(int mode, Vector3i dest) {
        dest.x = Math.roundUsing(this.x(), mode);
        dest.y = Math.roundUsing(this.y(), mode);
        dest.z = Math.roundUsing(this.z(), mode);
        return dest;
    }

    public Vector3f get(Vector3f dest) {
        dest.x = this.x();
        dest.y = this.y();
        dest.z = this.z();
        return dest;
    }

    public Vector3d get(Vector3d dest) {
        dest.x = this.x();
        dest.y = this.y();
        dest.z = this.z();
        return dest;
    }

    public int maxComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);
        if (absX >= absY && absX >= absZ) {
            return 0;
        } else if (absY >= absZ) {
            return 1;
        }
        return 2;
    }

    public int minComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);
        if (absX < absY && absX < absZ) {
            return 0;
        } else if (absY < absZ) {
            return 1;
        }
        return 2;
    }

    public Vector3f orthogonalize(Vector3fc v, Vector3f dest) {
        /*
         * http://lolengine.net/blog/2013/09/21/picking-orthogonal-vector-combing-coconuts
         */
        float rx, ry, rz;
        if (Math.abs(v.x()) > Math.abs(v.z())) {
            rx = -v.y();
            ry = v.x();
            rz = 0.0f;
        } else {
            rx = 0.0f;
            ry = -v.z();
            rz = v.y();
        }
        float invLen = Math.invsqrt(rx * rx + ry * ry + rz * rz);
        dest.x = rx * invLen;
        dest.y = ry * invLen;
        dest.z = rz * invLen;
        return dest;
    }

    /**
     * Transform <code>this</code> vector so that it is orthogonal to the given vector <code>v</code> and normalize the result.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Gram%E2%80%93Schmidt_process">Gram–Schmidt process</a>
     * 
     * @param v
     *          the reference vector which the result should be orthogonal to
     * @return this
     */
    public Vector3f orthogonalize(Vector3fc v) {
        return orthogonalize(v, this);
    }

    public Vector3f orthogonalizeUnit(Vector3fc v, Vector3f dest) {
        return orthogonalize(v, dest);
    }

    /**
     * Transform <code>this</code> vector so that it is orthogonal to the given unit vector <code>v</code> and normalize the result.
     * <p>
     * The vector <code>v</code> is assumed to be a {@link #normalize() unit} vector.
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Gram%E2%80%93Schmidt_process">Gram–Schmidt process</a>
     * 
     * @param v
     *          the reference unit vector which the result should be orthogonal to
     * @return this
     */
    public Vector3f orthogonalizeUnit(Vector3fc v) {
        return orthogonalizeUnit(v, this);
    }

    /**
     * Set each component of this vector to the largest (closest to positive
     * infinity) {@code float} value that is less than or equal to that
     * component and is equal to a mathematical integer.
     *
     * @return this
     */
    public Vector3f floor() {
        return floor(this);
    }

    public Vector3f floor(Vector3f dest) {
        dest.x = Math.floor(x);
        dest.y = Math.floor(y);
        dest.z = Math.floor(z);
        return dest;
    }

    /**
     * Set each component of this vector to the smallest (closest to negative
     * infinity) {@code float} value that is greater than or equal to that
     * component and is equal to a mathematical integer.
     *
     * @return this
     */
    public Vector3f ceil() {
        return ceil(this);
    }

    public Vector3f ceil(Vector3f dest) {
        dest.x = Math.ceil(x);
        dest.y = Math.ceil(y);
        dest.z = Math.ceil(z);
        return dest;
    }

    /**
     * Set each component of this vector to the closest float that is equal to
     * a mathematical integer, with ties rounding to positive infinity.
     *
     * @return this
     */
    public Vector3f round() {
        return round(this);
    }

    public Vector3f round(Vector3f dest) {
        dest.x = Math.round(x);
        dest.y = Math.round(y);
        dest.z = Math.round(z);
        return dest;
    }

    public boolean isFinite() {
        return Math.isFinite(x) && Math.isFinite(y) && Math.isFinite(z);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
