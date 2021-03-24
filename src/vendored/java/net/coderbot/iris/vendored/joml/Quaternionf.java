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
 * Quaternion of 4 single-precision floats which can represent rotation and uniform scaling.
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Quaternionf implements Externalizable, Cloneable, Quaternionfc {

    private static final long serialVersionUID = 1L;

    /**
     * The first component of the vector part.
     */
    public float x;
    /**
     * The second component of the vector part.
     */
    public float y;
    /**
     * The third component of the vector part.
     */
    public float z;
    /**
     * The real/scalar part of the quaternion.
     */
    public float w;

    /**
     * Create a new {@link Quaternionf} and initialize it with <code>(x=0, y=0, z=0, w=1)</code>, 
     * where <code>(x, y, z)</code> is the vector part of the quaternion and <code>w</code> is the real/scalar part.
     */
    public Quaternionf() {
        this.w = 1.0f;
    }

    /**
     * Create a new {@link Quaternionf} and initialize its components to the given values.
     * 
     * @param x
     *          the first component of the imaginary part
     * @param y
     *          the second component of the imaginary part
     * @param z
     *          the third component of the imaginary part
     * @param w
     *          the real part
     */
    public Quaternionf(double x, double y, double z, double w) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.w = (float) w;
    }

    /**
     * Create a new {@link Quaternionf} and initialize its components to the given values.
     * 
     * @param x
     *          the first component of the imaginary part
     * @param y
     *          the second component of the imaginary part
     * @param z
     *          the third component of the imaginary part
     * @param w
     *          the real part
     */
    public Quaternionf(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Create a new {@link Quaternionf} and initialize its components to the same values as the given {@link Quaternionfc}.
     * 
     * @param source
     *          the {@link Quaternionfc} to take the component values from
     */
    public Quaternionf(Quaternionfc source) {
        set(source);
    }

    /**
     * Create a new {@link Quaternionf} and initialize its components to the same values as the given {@link Quaterniondc}.
     * 
     * @param source
     *          the {@link Quaterniondc} to take the component values from
     */
    public Quaternionf(Quaterniondc source) {
        set(source);
    }

    /**
     * Create a new {@link Quaternionf} which represents the rotation of the given {@link AxisAngle4f}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f}
     */
    public Quaternionf(AxisAngle4f axisAngle) {
        float sin = Math.sin(axisAngle.angle * 0.5f);
        float cos = Math.cosFromSin(sin, axisAngle.angle * 0.5f);
        x = axisAngle.x * sin;
        y = axisAngle.y * sin;
        z = axisAngle.z * sin;
        w = cos;
    }

    /**
     * Create a new {@link Quaterniond} which represents the rotation of the given {@link AxisAngle4d}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d}
     */
    public Quaternionf(AxisAngle4d axisAngle) {
        double sin = Math.sin(axisAngle.angle * 0.5f);
        double cos = Math.cosFromSin(sin, axisAngle.angle * 0.5f);
        x = (float) (axisAngle.x * sin);
        y = (float) (axisAngle.y * sin);
        z = (float) (axisAngle.z * sin);
        w = (float) cos;
    }

    /**
     * @return the first component of the vector part
     */
    public float x() {
        return this.x;
    }

    /**
     * @return the second component of the vector part
     */
    public float y() {
        return this.y;
    }

    /**
     * @return the third component of the vector part
     */
    public float z() {
        return this.z;
    }

    /**
     * @return the real/scalar part of the quaternion
     */
    public float w() {
        return this.w;
    }

    /**
     * Normalize this quaternion.
     * 
     * @return this
     */
    public Quaternionf normalize() {
        return normalize(this);
    }

    public Quaternionf normalize(Quaternionf dest) {
        float invNorm = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
        dest.x = x * invNorm;
        dest.y = y * invNorm;
        dest.z = z * invNorm;
        dest.w = w * invNorm;
        return dest;
    }

    /**
     * Add the quaternion <code>(x, y, z, w)</code> to this quaternion.
     * 
     * @param x
     *          the x component of the vector part
     * @param y
     *          the y component of the vector part
     * @param z
     *          the z component of the vector part
     * @param w
     *          the real/scalar component
     * @return this
     */
    public Quaternionf add(float x, float y, float z, float w) {
        return add(x, y, z, w, this);
    }

    public Quaternionf add(float x, float y, float z, float w, Quaternionf dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        dest.w = this.w + w;
        return dest;
    }

    /**
     * Add <code>q2</code> to this quaternion.
     * 
     * @param q2
     *          the quaternion to add to this
     * @return this
     */
    public Quaternionf add(Quaternionfc q2) {
        return add(q2, this);
    }

    public Quaternionf add(Quaternionfc q2, Quaternionf dest) {
        dest.x = x + q2.x();
        dest.y = y + q2.y();
        dest.z = z + q2.z();
        dest.w = w + q2.w();
        return dest;
    }

    /**
     * Return the dot of this quaternion and <code>otherQuat</code>.
     * 
     * @param otherQuat
     *          the other quaternion
     * @return the dot product
     */
    public float dot(Quaternionf otherQuat) {
        return this.x * otherQuat.x + this.y * otherQuat.y + this.z * otherQuat.z + this.w * otherQuat.w;
    }

    public float angle() {
        return (float) (2.0 * Math.safeAcos(w));
    }

    public Matrix3f get(Matrix3f dest) {
        return dest.set(this);
    }

    public Matrix3d get(Matrix3d dest) {
        return dest.set(this);
    }

    public Matrix4f get(Matrix4f dest) {
        return dest.set(this);
    }

    public Matrix4d get(Matrix4d dest) {
        return dest.set(this);
    }

    public Matrix4x3f get(Matrix4x3f dest) {
        return dest.set(this);
    }

    public Matrix4x3d get(Matrix4x3d dest) {
        return dest.set(this);
    }

    public AxisAngle4f get(AxisAngle4f dest) {
        float x = this.x;
        float y = this.y;
        float z = this.z;
        float w = this.w;
        if (w > 1.0f) {
            float invNorm = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
            x *= invNorm;
            y *= invNorm;
            z *= invNorm;
            w *= invNorm;
        }
        dest.angle = (float) (2.0f * Math.acos(w));
        float s = Math.sqrt(1.0f - w * w);
        if (s < 0.001f) {
            dest.x = x;
            dest.y = y;
            dest.z = z;
        } else {
            s = 1.0f / s;
            dest.x = x * s;
            dest.y = y * s;
            dest.z = z * s;
        }
        return dest;
    }

    public AxisAngle4d get(AxisAngle4d dest) {
        float x = this.x;
        float y = this.y;
        float z = this.z;
        float w = this.w;
        if (w > 1.0f) {
            float invNorm = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
            x *= invNorm;
            y *= invNorm;
            z *= invNorm;
            w *= invNorm;
        }
        dest.angle = (float) (2.0f * Math.acos(w));
        float s = Math.sqrt(1.0f - w * w);
        if (s < 0.001f) {
            dest.x = x;
            dest.y = y;
            dest.z = z;
        } else {
            s = 1.0f / s;
            dest.x = x * s;
            dest.y = y * s;
            dest.z = z * s;
        }
        return dest;
    }

    public Quaterniond get(Quaterniond dest) {
        return dest.set(this);
    }

    /**
     * Set the given {@link Quaternionf} to the values of <code>this</code>.
     * 
     * @see #set(Quaternionfc)
     * 
     * @param dest
     *          the {@link Quaternionf} to set
     * @return the passed in destination
     */
    public Quaternionf get(Quaternionf dest) {
        return dest.set(this);
    }


    public ByteBuffer getAsMatrix3f(ByteBuffer dest) {
        MemUtil.INSTANCE.putMatrix3f(this, dest.position(), dest);
        return dest;
    }

    public FloatBuffer getAsMatrix3f(FloatBuffer dest) {
        MemUtil.INSTANCE.putMatrix3f(this, dest.position(), dest);
        return dest;
    }

    public ByteBuffer getAsMatrix4f(ByteBuffer dest) {
        MemUtil.INSTANCE.putMatrix4f(this, dest.position(), dest);
        return dest;
    }

    public FloatBuffer getAsMatrix4f(FloatBuffer dest) {
        MemUtil.INSTANCE.putMatrix4f(this, dest.position(), dest);
        return dest;
    }

    public ByteBuffer getAsMatrix4x3f(ByteBuffer dest) {
        MemUtil.INSTANCE.putMatrix4x3f(this, dest.position(), dest);
        return dest;
    }

    public FloatBuffer getAsMatrix4x3f(FloatBuffer dest) {
        MemUtil.INSTANCE.putMatrix4x3f(this, dest.position(), dest);
        return dest;
    }


    /**
     * Set this quaternion to the given values.
     * 
     * @param x
     *          the new value of x
     * @param y
     *          the new value of y
     * @param z
     *          the new value of z
     * @param w
     *          the new value of w
     * @return this
     */
    public Quaternionf set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set this quaternion to be a copy of <code>q</code>.
     * 
     * @param q
     *          the {@link Quaternionfc} to copy
     * @return this
     */
    public Quaternionf set(Quaternionfc q) {
        this.x = q.x();
        this.y = q.y();
        this.z = q.z();
        this.w = q.w();
        return this;
    }

    /**
     * Set this quaternion to be a copy of <code>q</code>.
     * 
     * @param q
     *          the {@link Quaterniondc} to copy
     * @return this
     */
    public Quaternionf set(Quaterniondc q) {
        this.x = (float) q.x();
        this.y = (float) q.y();
        this.z = (float) q.z();
        this.w = (float) q.w();
        return this;
    }

    /**
     * Set this quaternion to a rotation equivalent to the given {@link AxisAngle4f}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4f}
     * @return this
     */
    public Quaternionf set(AxisAngle4f axisAngle) {
        return setAngleAxis(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z);
    }

    /**
     * Set this quaternion to a rotation equivalent to the given {@link AxisAngle4d}.
     * 
     * @param axisAngle
     *          the {@link AxisAngle4d}
     * @return this
     */
    public Quaternionf set(AxisAngle4d axisAngle) {
        return setAngleAxis(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z);
    }

    /**
     * Set this quaternion to a rotation equivalent to the supplied axis and
     * angle (in radians).
     * <p>
     * This method assumes that the given rotation axis <code>(x, y, z)</code> is already normalized
     * 
     * @param angle
     *          the angle in radians
     * @param x
     *          the x-component of the normalized rotation axis
     * @param y
     *          the y-component of the normalized rotation axis
     * @param z
     *          the z-component of the normalized rotation axis
     * @return this
     */
    public Quaternionf setAngleAxis(float angle, float x, float y, float z) {
        float s = Math.sin(angle * 0.5f);
        this.x = x * s;
        this.y = y * s;
        this.z = z * s;
        this.w = Math.cosFromSin(s, angle * 0.5f);
        return this;
    }

    /**
     * Set this quaternion to a rotation equivalent to the supplied axis and
     * angle (in radians).
     * <p>
     * This method assumes that the given rotation axis <code>(x, y, z)</code> is already normalized
     * 
     * @param angle
     *          the angle in radians
     * @param x
     *          the x-component of the normalized rotation axis
     * @param y
     *          the y-component of the normalized rotation axis
     * @param z
     *          the z-component of the normalized rotation axis
     * @return this
     */
    public Quaternionf setAngleAxis(double angle, double x, double y, double z) {
        double s = Math.sin(angle * 0.5f);
        this.x = (float) (x * s);
        this.y = (float) (y * s);
        this.z = (float) (z * s);
        this.w = (float) Math.cosFromSin(s, angle * 0.5f);
        return this;
    }

    /**
     * Set this {@link Quaternionf} to a rotation of the given angle in radians about the supplied
     * axis, all of which are specified via the {@link AxisAngle4f}.
     * 
     * @see #rotationAxis(float, float, float, float)
     * 
     * @param axisAngle
     *            the {@link AxisAngle4f} giving the rotation angle in radians and the axis to rotate about
     * @return this
     */
    public Quaternionf rotationAxis(AxisAngle4f axisAngle) {
        return rotationAxis(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z);
    }

    /**
     * Set this quaternion to a rotation of the given angle in radians about the supplied axis.
     * 
     * @param angle
     *          the rotation angle in radians
     * @param axisX
     *          the x-coordinate of the rotation axis
     * @param axisY
     *          the y-coordinate of the rotation axis
     * @param axisZ
     *          the z-coordinate of the rotation axis
     * @return this
     */
    public Quaternionf rotationAxis(float angle, float axisX, float axisY, float axisZ) {
        float hangle = angle / 2.0f;
        float sinAngle = Math.sin(hangle);
        float invVLength = Math.invsqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        return set(axisX * invVLength * sinAngle,
                   axisY * invVLength * sinAngle,
                   axisZ * invVLength * sinAngle,
                   Math.cosFromSin(sinAngle, hangle));
    }

    /**
     * Set this quaternion to a rotation of the given angle in radians about the supplied axis.
     * 
     * @see #rotationAxis(float, float, float, float)
     * 
     * @param angle
     *          the rotation angle in radians
     * @param axis
     *          the axis to rotate about
     * @return this
     */
    public Quaternionf rotationAxis(float angle, Vector3fc axis) {
        return rotationAxis(angle, axis.x(), axis.y(), axis.z());
    }

    /**
     * Set this quaternion to represent a rotation of the given radians about the x axis.
     * 
     * @param angle
     *              the angle in radians to rotate about the x axis
     * @return this
     */
    public Quaternionf rotationX(float angle) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return set(sin, 0, 0, cos);
    }

    /**
     * Set this quaternion to represent a rotation of the given radians about the y axis.
     * 
     * @param angle
     *              the angle in radians to rotate about the y axis
     * @return this
     */
    public Quaternionf rotationY(float angle) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return set(0, sin, 0, cos);
    }

    /**
     * Set this quaternion to represent a rotation of the given radians about the z axis.
     * 
     * @param angle
     *              the angle in radians to rotate about the z axis
     * @return this
     */
    public Quaternionf rotationZ(float angle) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return set(0, 0, sin, cos);
    }

    private void setFromUnnormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float nm00 = m00, nm01 = m01, nm02 = m02;
        float nm10 = m10, nm11 = m11, nm12 = m12;
        float nm20 = m20, nm21 = m21, nm22 = m22;
        float lenX = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float lenY = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float lenZ = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22);
    }

    private void setFromNormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float t;
        float tr = m00 + m11 + m22;
        if (tr >= 0.0f) {
            t = Math.sqrt(tr + 1.0f);
            w = t * 0.5f;
            t = 0.5f / t;
            x = (m12 - m21) * t;
            y = (m20 - m02) * t;
            z = (m01 - m10) * t;
        } else {
            if (m00 >= m11 && m00 >= m22) {
                t = Math.sqrt(m00 - (m11 + m22) + 1.0f);
                x = t * 0.5f;
                t = 0.5f / t;
                y = (m10 + m01) * t;
                z = (m02 + m20) * t;
                w = (m12 - m21) * t;
            } else if (m11 > m22) {
                t = Math.sqrt(m11 - (m22 + m00) + 1.0f);
                y = t * 0.5f;
                t = 0.5f / t;
                z = (m21 + m12) * t;
                x = (m10 + m01) * t;
                w = (m20 - m02) * t;
            } else {
                t = Math.sqrt(m22 - (m00 + m11) + 1.0f);
                z = t * 0.5f;
                t = 0.5f / t;
                x = (m02 + m20) * t;
                y = (m21 + m12) * t;
                w = (m01 - m10) * t;
            }
        }
    }

    private void setFromUnnormalized(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        double nm00 = m00, nm01 = m01, nm02 = m02;
        double nm10 = m10, nm11 = m11, nm12 = m12;
        double nm20 = m20, nm21 = m21, nm22 = m22;
        double lenX = Math.invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
        double lenY = Math.invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
        double lenZ = Math.invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22);
    }

    private void setFromNormalized(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        double t;
        double tr = m00 + m11 + m22;
        if (tr >= 0.0) {
            t = Math.sqrt(tr + 1.0);
            w = (float) (t * 0.5);
            t = 0.5 / t;
            x = (float) ((m12 - m21) * t);
            y = (float) ((m20 - m02) * t);
            z = (float) ((m01 - m10) * t);
        } else {
            if (m00 >= m11 && m00 >= m22) {
                t = Math.sqrt(m00 - (m11 + m22) + 1.0);
                x = (float) (t * 0.5);
                t = 0.5 / t;
                y = (float) ((m10 + m01) * t);
                z = (float) ((m02 + m20) * t);
                w = (float) ((m12 - m21) * t);
            } else if (m11 > m22) {
                t = Math.sqrt(m11 - (m22 + m00) + 1.0);
                y = (float) (t * 0.5);
                t = 0.5 / t;
                z = (float) ((m21 + m12) * t);
                x = (float) ((m10 + m01) * t);
                w = (float) ((m20 - m02) * t);
            } else {
                t = Math.sqrt(m22 - (m00 + m11) + 1.0);
                z = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((m02 + m20) * t);
                y = (float) ((m21 + m12) * t);
                w = (float) ((m01 - m10) * t);
            }
        }
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix4fc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix4x3fc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix4x3dc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix4fc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix4x3fc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix4x3dc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix4dc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix4dc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix3fc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix3fc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * <p>
     * This method assumes that the first three columns of the upper left 3x3 submatrix are no unit vectors.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromUnnormalized(Matrix3dc mat) {
        setFromUnnormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the rotational component of the given matrix.
     * 
     * @param mat
     *          the matrix whose rotational component is used to set this quaternion
     * @return this
     */
    public Quaternionf setFromNormalized(Matrix3dc mat) {
        setFromNormalized(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
        return this;
    }

    /**
     * Set this quaternion to be a representation of the supplied axis and
     * angle (in radians).
     * 
     * @param axis
     *          the rotation axis
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Quaternionf fromAxisAngleRad(Vector3fc axis, float angle) {
        return fromAxisAngleRad(axis.x(), axis.y(), axis.z(), angle);
    }

    /**
     * Set this quaternion to be a representation of the supplied axis and
     * angle (in radians).
     * 
     * @param axisX
     *          the x component of the rotation axis
     * @param axisY
     *          the y component of the rotation axis
     * @param axisZ
     *          the z component of the rotation axis         
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Quaternionf fromAxisAngleRad(float axisX, float axisY, float axisZ, float angle) {
        float hangle = angle / 2.0f;
        float sinAngle = Math.sin(hangle);
        float vLength = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        x = axisX / vLength * sinAngle;
        y = axisY / vLength * sinAngle;
        z = axisZ / vLength * sinAngle;
        w = Math.cosFromSin(sinAngle, hangle);
        return this;
    }

    /**
     * Set this quaternion to be a representation of the supplied axis and
     * angle (in degrees).
     * 
     * @param axis
     *          the rotation axis
     * @param angle
     *          the angle in degrees
     * @return this
     */
    public Quaternionf fromAxisAngleDeg(Vector3fc axis, float angle) {
        return fromAxisAngleRad(axis.x(), axis.y(), axis.z(), Math.toRadians(angle));
    }

    /**
     * Set this quaternion to be a representation of the supplied axis and
     * angle (in degrees).
     * 
     * @param axisX
     *          the x component of the rotation axis
     * @param axisY
     *          the y component of the rotation axis
     * @param axisZ
     *          the z component of the rotation axis         
     * @param angle
     *          the angle in radians
     * @return this
     */
    public Quaternionf fromAxisAngleDeg(float axisX, float axisY, float axisZ, float angle) {
        return fromAxisAngleRad(axisX, axisY, axisZ, Math.toRadians(angle));
    }

    /**
     * Multiply this quaternion by <code>q</code>.
     * <p>
     * If <code>T</code> is <code>this</code> and <code>Q</code> is the given
     * quaternion, then the resulting quaternion <code>R</code> is:
     * <p>
     * <code>R = T * Q</code>
     * <p>
     * So, this method uses post-multiplication like the matrix classes, resulting in a
     * vector to be transformed by <code>Q</code> first, and then by <code>T</code>.
     * 
     * @param q
     *          the quaternion to multiply <code>this</code> by
     * @return this
     */
    public Quaternionf mul(Quaternionfc q) {
        return mul(q, this);
    }

    public Quaternionf mul(Quaternionfc q, Quaternionf dest) {
        return dest.set(Math.fma(w, q.x(), Math.fma(x, q.w(), Math.fma(y, q.z(), -z * q.y()))),
                        Math.fma(w, q.y(), Math.fma(-x, q.z(), Math.fma(y, q.w(), z * q.x()))),
                        Math.fma(w, q.z(), Math.fma(x, q.y(), Math.fma(-y, q.x(), z * q.w()))),
                        Math.fma(w, q.w(), Math.fma(-x, q.x(), Math.fma(-y, q.y(), -z * q.z()))));
    }

    /**
     * Multiply this quaternion by the quaternion represented via <code>(qx, qy, qz, qw)</code>.
     * <p>
     * If <code>T</code> is <code>this</code> and <code>Q</code> is the given
     * quaternion, then the resulting quaternion <code>R</code> is:
     * <p>
     * <code>R = T * Q</code>
     * <p>
     * So, this method uses post-multiplication like the matrix classes, resulting in a
     * vector to be transformed by <code>Q</code> first, and then by <code>T</code>.
     * 
     * @param qx
     *          the x component of the quaternion to multiply <code>this</code> by
     * @param qy
     *          the y component of the quaternion to multiply <code>this</code> by
     * @param qz
     *          the z component of the quaternion to multiply <code>this</code> by
     * @param qw
     *          the w component of the quaternion to multiply <code>this</code> by
     * @return this
     */
    public Quaternionf mul(float qx, float qy, float qz, float qw) {
        return mul(qx, qy, qz, qw, this);
    }

    public Quaternionf mul(float qx, float qy, float qz, float qw, Quaternionf dest) {
        return dest.set(Math.fma(w, qx, Math.fma(x, qw, Math.fma(y, qz, -z * qy))),
                        Math.fma(w, qy, Math.fma(-x, qz, Math.fma(y, qw, z * qx))),
                        Math.fma(w, qz, Math.fma(x, qy, Math.fma(-y, qx, z * qw))),
                        Math.fma(w, qw, Math.fma(-x, qx, Math.fma(-y, qy, -z * qz))));
    }

    /**
     * Pre-multiply this quaternion by <code>q</code>.
     * <p>
     * If <code>T</code> is <code>this</code> and <code>Q</code> is the given quaternion, then the resulting quaternion <code>R</code> is:
     * <p>
     * <code>R = Q * T</code>
     * <p>
     * So, this method uses pre-multiplication, resulting in a vector to be transformed by <code>T</code> first, and then by <code>Q</code>.
     * 
     * @param q
     *            the quaternion to pre-multiply <code>this</code> by
     * @return this
     */
    public Quaternionf premul(Quaternionfc q) {
        return premul(q, this);
    }

    public Quaternionf premul(Quaternionfc q, Quaternionf dest) {
        return dest.set(Math.fma(q.w(), x, Math.fma(q.x(), w, Math.fma(q.y(), z, -q.z() * y))),
                        Math.fma(q.w(), y, Math.fma(-q.x(), z, Math.fma(q.y(), w, q.z() * x))),
                        Math.fma(q.w(), z, Math.fma(q.x(), y, Math.fma(-q.y(), x, q.z() * w))),
                        Math.fma(q.w(), w, Math.fma(-q.x(), x, Math.fma(-q.y(), y, -q.z() * z))));
    }

    /**
     * Pre-multiply this quaternion by the quaternion represented via <code>(qx, qy, qz, qw)</code>.
     * <p>
     * If <code>T</code> is <code>this</code> and <code>Q</code> is the given quaternion, then the resulting quaternion <code>R</code> is:
     * <p>
     * <code>R = Q * T</code>
     * <p>
     * So, this method uses pre-multiplication, resulting in a vector to be transformed by <code>T</code> first, and then by <code>Q</code>.
     * 
     * @param qx
     *          the x component of the quaternion to multiply <code>this</code> by
     * @param qy
     *          the y component of the quaternion to multiply <code>this</code> by
     * @param qz
     *          the z component of the quaternion to multiply <code>this</code> by
     * @param qw
     *          the w component of the quaternion to multiply <code>this</code> by
     * @return this
     */
    public Quaternionf premul(float qx, float qy, float qz, float qw) {
        return premul(qx, qy, qz, qw, this);
    }

    public Quaternionf premul(float qx, float qy, float qz, float qw, Quaternionf dest) {
        return dest.set(Math.fma(qw, x, Math.fma(qx, w, Math.fma(qy, z, -qz * y))),
                        Math.fma(qw, y, Math.fma(-qx, z, Math.fma(qy, w, qz * x))),
                        Math.fma(qw, z, Math.fma(qx, y, Math.fma(-qy, x, qz * w))),
                        Math.fma(qw, w, Math.fma(-qx, x, Math.fma(-qy, y, -qz * z))));
    }

    public Vector3f transform(Vector3f vec){
        return transform(vec.x, vec.y, vec.z, vec);
    }

    public Vector3f transformInverse(Vector3f vec){
        return transformInverse(vec.x, vec.y, vec.z, vec);
    }

    public Vector3f transformPositiveX(Vector3f dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        dest.x = ww + xx - zz - yy;
        dest.y = xy + zw + zw + xy;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector4f transformPositiveX(Vector4f dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        dest.x = ww + xx - zz - yy;
        dest.y = xy + zw + zw + xy;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector3f transformUnitPositiveX(Vector3f dest) {
        float xy = x * y, xz = x * z, yy = y * y;
        float yw = y * w, zz = z * z, zw = z * w;
        dest.x = 1 - yy - zz - yy - zz;
        dest.y = xy + zw + xy + zw;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector4f transformUnitPositiveX(Vector4f dest) {
        float yy = y * y, zz = z * z, xy = x * y;
        float xz = x * z, yw = y * w, zw = z * w;
        dest.x = 1 - yy - yy - zz - zz;
        dest.y = xy + zw + xy + zw;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector3f transformPositiveY(Vector3f dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float yz = y * z;
        float xw = x * w;
        dest.x = -zw + xy - zw + xy;
        dest.y = yy - zz + ww - xx;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector4f transformPositiveY(Vector4f dest) {
        float ww = w * w, xx = x * x, yy = y * y;
        float zz = z * z, zw = z * w, xy = x * y;
        float yz = y * z, xw = x * w;
        dest.x = -zw + xy - zw + xy;
        dest.y = yy - zz + ww - xx;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector4f transformUnitPositiveY(Vector4f dest) {
        float xx = x * x, zz = z * z, xy = x * y;
        float yz = y * z, xw = x * w, zw = z * w;
        dest.x = xy - zw + xy - zw;
        dest.y = 1 - xx - xx - zz - zz;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector3f transformUnitPositiveY(Vector3f dest) {
        float xx = x * x, zz = z * z, xy = x * y;
        float yz = y * z, xw = x * w, zw = z * w;
        dest.x = xy - zw + xy - zw;
        dest.y = 1 - xx - xx - zz - zz;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector3f transformPositiveZ(Vector3f dest) {
        float ww = w * w, xx = x * x, yy = y * y;
        float zz = z * z, xz = x * z, yw = y * w;
        float yz = y * z, xw = x * w;
        dest.x = yw + xz + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = zz - yy - xx + ww;
        return dest;
    }

    public Vector4f transformPositiveZ(Vector4f dest) {
        float ww = w * w, xx = x * x, yy = y * y;
        float zz = z * z, xz = x * z, yw = y * w;
        float yz = y * z, xw = x * w;
        dest.x = yw + xz + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = zz - yy - xx + ww;
        return dest;
    }

    public Vector4f transformUnitPositiveZ(Vector4f dest) {
        float xx = x * x, yy = y * y, xz = x * z;
        float yz = y * z, xw = x * w, yw = y * w;
        dest.x = xz + yw + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = 1 - xx - xx - yy - yy;
        return dest;
    }

    public Vector3f transformUnitPositiveZ(Vector3f dest) {
        float xx = x * x, yy = y * y, xz = x * z;
        float yz = y * z, xw = x * w, yw = y * w;
        dest.x = xz + yw + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = 1.0f - xx - xx - yy - yy;
        return dest;
    }

    public Vector4f transform(Vector4f vec){
        return transform(vec, vec);
    }

    public Vector4f transformInverse(Vector4f vec){
        return transformInverse(vec, vec);
    }

    public Vector3f transform(Vector3fc vec, Vector3f dest) {
        return transform(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3f transformInverse(Vector3fc vec, Vector3f dest) {
        return transformInverse(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3f transform(float x, float y, float z, Vector3f dest) {
        float xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
        float xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
        float zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z)),
                        Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z)),
                        Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector3f transformInverse(float x, float y, float z, Vector3f dest) {
        float n = 1.0f / Math.fma(this.x, this.x, Math.fma(this.y, this.y, Math.fma(this.z, this.z, this.w * this.w)));
        float qx = this.x * n, qy = this.y * n, qz = this.z * n, qw = this.w * n;
        float xx = qx * qx, yy = qy * qy, zz = qz * qz, ww = qw * qw;
        float xy = qx * qy, xz = qx * qz, yz = qy * qz, xw = qx * qw;
        float zw = qz * qw, yw = qy * qw, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy + zw) * k, y, (2 * (xz - yw) * k) * z)),
                        Math.fma(2 * (xy - zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz + xw) * k) * z)),
                        Math.fma(2 * (xz + yw) * k, x, Math.fma(2 * (yz - xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector3f transformUnit(Vector3f vec) {
        return transformUnit(vec.x, vec.y, vec.z, vec);
    }

    public Vector3f transformInverseUnit(Vector3f vec) {
        return transformInverseUnit(vec.x, vec.y, vec.z, vec);
    }

    public Vector3f transformUnit(Vector3fc vec, Vector3f dest) {
        return transformUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3f transformInverseUnit(Vector3fc vec, Vector3f dest) {
        return transformInverseUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3f transformUnit(float x, float y, float z, Vector3f dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy - zw), y, (2 * (xz + yw)) * z)),
                        Math.fma(2 * (xy + zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz - xw)) * z)),
                        Math.fma(2 * (xz - yw), x, Math.fma(2 * (yz + xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector3f transformInverseUnit(float x, float y, float z, Vector3f dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy + zw), y, (2 * (xz - yw)) * z)),
                        Math.fma(2 * (xy - zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz + xw)) * z)),
                        Math.fma(2 * (xz + yw), x, Math.fma(2 * (yz - xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector4f transform(Vector4fc vec, Vector4f dest) {
        return transform(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4f transformInverse(Vector4fc vec, Vector4f dest) {
        return transformInverse(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4f transform(float x, float y, float z, Vector4f dest) {
        float xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
        float xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
        float zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z)),
                        Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z)),
                        Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector4f transformInverse(float x, float y, float z, Vector4f dest) {
        float n = 1.0f / Math.fma(this.x, this.x, Math.fma(this.y, this.y, Math.fma(this.z, this.z, this.w * this.w)));
        float qx = this.x * n, qy = this.y * n, qz = this.z * n, qw = this.w * n;
        float xx = qx * qx, yy = qy * qy, zz = qz * qz, ww = qw * qw;
        float xy = qx * qy, xz = qx * qz, yz = qy * qz, xw = qx * qw;
        float zw = qz * qw, yw = qy * qw, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy + zw) * k, y, (2 * (xz - yw) * k) * z)),
                        Math.fma(2 * (xy - zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz + xw) * k) * z)),
                        Math.fma(2 * (xz + yw) * k, x, Math.fma(2 * (yz - xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector3d transform(Vector3d vec){
        return transform(vec.x, vec.y, vec.z, vec);
    }

    public Vector3d transformInverse(Vector3d vec){
        return transformInverse(vec.x, vec.y, vec.z, vec);
    }

    public Vector4f transformUnit(Vector4f vec) {
        return transformUnit(vec.x, vec.y, vec.z, vec);
    }

    public Vector4f transformInverseUnit(Vector4f vec) {
        return transformInverseUnit(vec.x, vec.y, vec.z, vec);
    }

    public Vector4f transformUnit(Vector4fc vec, Vector4f dest) {
        return transformUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4f transformInverseUnit(Vector4fc vec, Vector4f dest) {
        return transformInverseUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4f transformUnit(float x, float y, float z, Vector4f dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy - zw), y, (2 * (xz + yw)) * z)),
                        Math.fma(2 * (xy + zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz - xw)) * z)),
                        Math.fma(2 * (xz - yw), x, Math.fma(2 * (yz + xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector4f transformInverseUnit(float x, float y, float z, Vector4f dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy + zw), y, (2 * (xz - yw)) * z)),
                        Math.fma(2 * (xy - zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz + xw)) * z)),
                        Math.fma(2 * (xz + yw), x, Math.fma(2 * (yz - xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector3d transformPositiveX(Vector3d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        dest.x = ww + xx - zz - yy;
        dest.y = xy + zw + zw + xy;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector4d transformPositiveX(Vector4d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        dest.x = ww + xx - zz - yy;
        dest.y = xy + zw + zw + xy;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector3d transformUnitPositiveX(Vector3d dest) {
        float yy = y * y;
        float zz = z * z;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        float zw = z * w;
        dest.x = 1 - yy - yy - zz - zz;
        dest.y = xy + zw + xy + zw;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector4d transformUnitPositiveX(Vector4d dest) {
        float yy = y * y;
        float zz = z * z;
        float xy = x * y;
        float xz = x * z;
        float yw = y * w;
        float zw = z * w;
        dest.x = 1 - yy - yy - zz - zz;
        dest.y = xy + zw + xy + zw;
        dest.z = xz - yw + xz - yw;
        return dest;
    }

    public Vector3d transformPositiveY(Vector3d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float yz = y * z;
        float xw = x * w;
        dest.x = -zw + xy - zw + xy;
        dest.y = yy - zz + ww - xx;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector4d transformPositiveY(Vector4d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float zw = z * w;
        float xy = x * y;
        float yz = y * z;
        float xw = x * w;
        dest.x = -zw + xy - zw + xy;
        dest.y = yy - zz + ww - xx;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector4d transformUnitPositiveY(Vector4d dest) {
        float xx = x * x;
        float zz = z * z;
        float xy = x * y;
        float yz = y * z;
        float xw = x * w;
        float zw = z * w;
        dest.x = xy - zw + xy - zw;
        dest.y = 1 - xx - xx - zz - zz;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector3d transformUnitPositiveY(Vector3d dest) {
        float xx = x * x;
        float zz = z * z;
        float xy = x * y;
        float yz = y * z;
        float xw = x * w;
        float zw = z * w;
        dest.x = xy - zw + xy - zw;
        dest.y = 1 - xx - xx - zz - zz;
        dest.z = yz + yz + xw + xw;
        return dest;
    }

    public Vector3d transformPositiveZ(Vector3d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float xz = x * z;
        float yw = y * w;
        float yz = y * z;
        float xw = x * w;
        dest.x = yw + xz + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = zz - yy - xx + ww;
        return dest;
    }

    public Vector4d transformPositiveZ(Vector4d dest) {
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float xz = x * z;
        float yw = y * w;
        float yz = y * z;
        float xw = x * w;
        dest.x = yw + xz + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = zz - yy - xx + ww;
        return dest;
    }

    public Vector4d transformUnitPositiveZ(Vector4d dest) {
        float xx = x * x;
        float yy = y * y;
        float xz = x * z;
        float yz = y * z;
        float xw = x * w;
        float yw = y * w;
        dest.x = xz + yw + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = 1 - xx - xx - yy - yy;
        return dest;
    }

    public Vector3d transformUnitPositiveZ(Vector3d dest) {
        float xx = x * x;
        float yy = y * y;
        float xz = x * z;
        float yz = y * z;
        float xw = x * w;
        float yw = y * w;
        dest.x = xz + yw + xz + yw;
        dest.y = yz + yz - xw - xw;
        dest.z = 1 - xx - xx - yy - yy;
        return dest;
    }

    public Vector4d transform(Vector4d vec){
        return transform(vec, vec);
    }

    public Vector4d transformInverse(Vector4d vec){
        return transformInverse(vec, vec);
    }

    public Vector3d transform(Vector3dc vec, Vector3d dest) {
        return transform(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3d transformInverse(Vector3dc vec, Vector3d dest) {
        return transformInverse(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3d transform(float x, float y, float z, Vector3d dest) {
        return transform((double) x, (double) y, (double) z, dest);
    }

    public Vector3d transformInverse(float x, float y, float z, Vector3d dest) {
        return transformInverse((double) x, (double) y, (double) z, dest);
    }

    public Vector3d transform(double x, double y, double z, Vector3d dest) {
        float xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
        float xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
        float zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z)),
                        Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z)),
                        Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector3d transformInverse(double x, double y, double z, Vector3d dest) {
        float n = 1.0f / Math.fma(this.x, this.x, Math.fma(this.y, this.y, Math.fma(this.z, this.z, this.w * this.w)));
        float qx = this.x * n, qy = this.y * n, qz = this.z * n, qw = this.w * n;
        float xx = qx * qx, yy = qy * qy, zz = qz * qz, ww = qw * qw;
        float xy = qx * qy, xz = qx * qz, yz = qy * qz, xw = qx * qw;
        float zw = qz * qw, yw = qy * qw, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy + zw) * k, y, (2 * (xz - yw) * k) * z)),
                        Math.fma(2 * (xy - zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz + xw) * k) * z)),
                        Math.fma(2 * (xz + yw) * k, x, Math.fma(2 * (yz - xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector4d transform(Vector4dc vec, Vector4d dest) {
        return transform(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4d transformInverse(Vector4dc vec, Vector4d dest) {
        return transformInverse(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4d transform(double x, double y, double z, Vector4d dest) {
        float xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
        float xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
        float zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z)),
                        Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z)),
                        Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector4d transformInverse(double x, double y, double z, Vector4d dest) {
        float n = 1.0f / Math.fma(this.x, this.x, Math.fma(this.y, this.y, Math.fma(this.z, this.z, this.w * this.w)));
        float qx = this.x * n, qy = this.y * n, qz = this.z * n, qw = this.w * n;
        float xx = qx * qx, yy = qy * qy, zz = qz * qz, ww = qw * qw;
        float xy = qx * qy, xz = qx * qz, yz = qy * qz, xw = qx * qw;
        float zw = qz * qw, yw = qy * qw, k = 1 / (xx + yy + zz + ww);
        return dest.set(Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy + zw) * k, y, (2 * (xz - yw) * k) * z)),
                        Math.fma(2 * (xy - zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz + xw) * k) * z)),
                        Math.fma(2 * (xz + yw) * k, x, Math.fma(2 * (yz - xw) * k, y, ((zz - xx - yy + ww) * k) * z)));
    }

    public Vector4d transformUnit(Vector4d vec){
        return transformUnit(vec, vec);
    }

    public Vector4d transformInverseUnit(Vector4d vec){
        return transformInverseUnit(vec, vec);
    }

    public Vector3d transformUnit(Vector3dc vec, Vector3d dest) {
        return transformUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3d transformInverseUnit(Vector3dc vec, Vector3d dest) {
        return transformInverseUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector3d transformUnit(float x, float y, float z, Vector3d dest) {
        return transformUnit((double) x, (double) y, (double) z, dest);
    }

    public Vector3d transformInverseUnit(float x, float y, float z, Vector3d dest) {
        return transformInverseUnit((double) x, (double) y, (double) z, dest);
    }

    public Vector3d transformUnit(double x, double y, double z, Vector3d dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy - zw), y, (2 * (xz + yw)) * z)),
                        Math.fma(2 * (xy + zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz - xw)) * z)),
                        Math.fma(2 * (xz - yw), x, Math.fma(2 * (yz + xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector3d transformInverseUnit(double x, double y, double z, Vector3d dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy + zw), y, (2 * (xz - yw)) * z)),
                        Math.fma(2 * (xy - zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz + xw)) * z)),
                        Math.fma(2 * (xz + yw), x, Math.fma(2 * (yz - xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector4d transformUnit(Vector4dc vec, Vector4d dest) {
        return transformUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4d transformInverseUnit(Vector4dc vec, Vector4d dest) {
        return transformInverseUnit(vec.x(), vec.y(), vec.z(), dest);
    }

    public Vector4d transformUnit(double x, double y, double z, Vector4d dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy - zw), y, (2 * (xz + yw)) * z)),
                        Math.fma(2 * (xy + zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz - xw)) * z)),
                        Math.fma(2 * (xz - yw), x, Math.fma(2 * (yz + xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Vector4d transformInverseUnit(double x, double y, double z, Vector4d dest) {
        float xx = this.x * this.x, xy = this.x * this.y, xz = this.x * this.z;
        float xw = this.x * this.w, yy = this.y * this.y, yz = this.y * this.z;
        float yw = this.y * this.w, zz = this.z * this.z, zw = this.z * this.w;
        return dest.set(Math.fma(Math.fma(-2, yy + zz, 1), x, Math.fma(2 * (xy + zw), y, (2 * (xz - yw)) * z)),
                        Math.fma(2 * (xy - zw), x, Math.fma(Math.fma(-2, xx + zz, 1), y, (2 * (yz + xw)) * z)),
                        Math.fma(2 * (xz + yw), x, Math.fma(2 * (yz - xw), y, Math.fma(-2, xx + yy, 1) * z)));
    }

    public Quaternionf invert(Quaternionf dest) {
        float invNorm = 1.0f / Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
        dest.x = -x * invNorm;
        dest.y = -y * invNorm;
        dest.z = -z * invNorm;
        dest.w = w * invNorm;
        return dest;
    }

    /**
     * Invert this quaternion and {@link #normalize() normalize} it.
     * <p>
     * If this quaternion is already normalized, then {@link #conjugate()} should be used instead.
     * 
     * @see #conjugate()
     * 
     * @return this
     */
    public Quaternionf invert() {
        return invert(this);
    }

    public Quaternionf div(Quaternionfc b, Quaternionf dest) {
        float invNorm = 1.0f / Math.fma(b.x(), b.x(), Math.fma(b.y(), b.y(), Math.fma(b.z(), b.z(), b.w() * b.w())));
        float x = -b.x() * invNorm;
        float y = -b.y() * invNorm;
        float z = -b.z() * invNorm;
        float w = b.w() * invNorm;
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    /**
     * Divide <code>this</code> quaternion by <code>b</code>.
     * <p>
     * The division expressed using the inverse is performed in the following way:
     * <p>
     * <code>this = this * b^-1</code>, where <code>b^-1</code> is the inverse of <code>b</code>.
     * 
     * @param b
     *          the {@link Quaternionf} to divide this by
     * @return this
     */
    public Quaternionf div(Quaternionfc b) {
        return div(b, this);
    }

    /**
     * Conjugate this quaternion.
     * 
     * @return this
     */
    public Quaternionf conjugate() {
        return conjugate(this);
    }

    public Quaternionf conjugate(Quaternionf dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        dest.w = w;
        return dest;
    }

    /**
     * Set this quaternion to the identity.
     * 
     * @return this
     */
    public Quaternionf identity() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
        return this;
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles using rotation sequence <code>XYZ</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateX(angleX).rotateY(angleY).rotateZ(angleZ)</code>
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angleX
     *              the angle in radians to rotate about the x axis
     * @param angleY
     *              the angle in radians to rotate about the y axis
     * @param angleZ
     *              the angle in radians to rotate about the z axis
     * @return this
     */
    public Quaternionf rotateXYZ(float angleX, float angleY, float angleZ) {
        return rotateXYZ(angleX, angleY, angleZ, this);
    }

    public Quaternionf rotateXYZ(float angleX, float angleY, float angleZ, Quaternionf dest) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        float w = cx*cycz - sx*sysz;
        float x = sx*cycz + cx*sysz;
        float y = cx*sycz - sx*cysz;
        float z = cx*cysz + sx*sycz;
        // right-multiply
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles, using the rotation sequence <code>ZYX</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateZ(angleZ).rotateY(angleY).rotateX(angleX)</code>
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angleZ
     *              the angle in radians to rotate about the z axis
     * @param angleY
     *              the angle in radians to rotate about the y axis
     * @param angleX
     *              the angle in radians to rotate about the x axis
     * @return this
     */
    public Quaternionf rotateZYX(float angleZ, float angleY, float angleX) {
        return rotateZYX(angleZ, angleY, angleX, this);
    }

    public Quaternionf rotateZYX(float angleZ, float angleY, float angleX, Quaternionf dest) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        float w = cx*cycz + sx*sysz;
        float x = sx*cycz - cx*sysz;
        float y = cx*sycz + sx*cysz;
        float z = cx*cysz - sx*sycz;
        // right-multiply
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles, using the rotation sequence <code>YXZ</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateY(angleY).rotateX(angleX).rotateZ(angleZ)</code>
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angleY
     *              the angle in radians to rotate about the y axis
     * @param angleX
     *              the angle in radians to rotate about the x axis
     * @param angleZ
     *              the angle in radians to rotate about the z axis
     * @return this
     */
    public Quaternionf rotateYXZ(float angleY, float angleX, float angleZ) {
        return rotateYXZ(angleY, angleX, angleZ, this);
    }

    public Quaternionf rotateYXZ(float angleY, float angleX, float angleZ, Quaternionf dest) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float yx = cy * sx;
        float yy = sy * cx;
        float yz = sy * sx;
        float yw = cy * cx;
        float x = yx * cz + yy * sz;
        float y = yy * cz - yx * sz;
        float z = yw * sz - yz * cz;
        float w = yw * cz + yz * sz;
        // right-multiply
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    public Vector3f getEulerAnglesXYZ(Vector3f eulerAngles) {
        eulerAngles.x = Math.atan2(2.0f * (x*w - y*z), 1.0f - 2.0f * (x*x + y*y));
        eulerAngles.y = Math.safeAsin(2.0f * (x*z + y*w));
        eulerAngles.z = Math.atan2(2.0f * (z*w - x*y), 1.0f - 2.0f * (y*y + z*z));
        return eulerAngles;
    }

    public float lengthSquared() {
        return Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
    }

    /**
     * Set this quaternion from the supplied euler angles (in radians) with rotation order XYZ.
     * <p>
     * This method is equivalent to calling: <code>rotationX(angleX).rotateY(angleY).rotateZ(angleZ)</code>
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this stackexchange answer</a>
     * 
     * @param angleX
     *          the angle in radians to rotate about x
     * @param angleY
     *          the angle in radians to rotate about y
     * @param angleZ
     *          the angle in radians to rotate about z
     * @return this
     */
    public Quaternionf rotationXYZ(float angleX, float angleY, float angleZ) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        w = cx*cycz - sx*sysz;
        x = sx*cycz + cx*sysz;
        y = cx*sycz - sx*cysz;
        z = cx*cysz + sx*sycz;

        return this;
    }

    /**
     * Set this quaternion from the supplied euler angles (in radians) with rotation order ZYX.
     * <p>
     * This method is equivalent to calling: <code>rotationZ(angleZ).rotateY(angleY).rotateX(angleX)</code>
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this stackexchange answer</a>
     * 
     * @param angleX
     *          the angle in radians to rotate about x
     * @param angleY
     *          the angle in radians to rotate about y
     * @param angleZ
     *          the angle in radians to rotate about z
     * @return this
     */
    public Quaternionf rotationZYX(float angleZ, float angleY, float angleX) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        w = cx*cycz + sx*sysz;
        x = sx*cycz - cx*sysz;
        y = cx*sycz + sx*cysz;
        z = cx*cysz - sx*sycz;

        return this;
    }

    /**
     * Set this quaternion from the supplied euler angles (in radians) with rotation order YXZ.
     * <p>
     * This method is equivalent to calling: <code>rotationY(angleY).rotateX(angleX).rotateZ(angleZ)</code>
     * <p>
     * Reference: <a href="https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles">https://en.wikipedia.org</a>
     * 
     * @param angleY
     *          the angle in radians to rotate about y
     * @param angleX
     *          the angle in radians to rotate about x
     * @param angleZ
     *          the angle in radians to rotate about z
     * @return this
     */
    public Quaternionf rotationYXZ(float angleY, float angleX, float angleZ) {
        float sx = Math.sin(angleX * 0.5f);
        float cx = Math.cosFromSin(sx, angleX * 0.5f);
        float sy = Math.sin(angleY * 0.5f);
        float cy = Math.cosFromSin(sy, angleY * 0.5f);
        float sz = Math.sin(angleZ * 0.5f);
        float cz = Math.cosFromSin(sz, angleZ * 0.5f);

        float x = cy * sx;
        float y = sy * cx;
        float z = sy * sx;
        float w = cy * cx;
        this.x = x * cz + y * sz;
        this.y = y * cz - x * sz;
        this.z = w * sz - z * cz;
        this.w = w * cz + z * sz;

        return this;
    }

    /**
     * Interpolate between <code>this</code> {@link #normalize() unit} quaternion and the specified
     * <code>target</code> {@link #normalize() unit} quaternion using spherical linear interpolation using the specified interpolation factor <code>alpha</code>.
     * <p>
     * This method resorts to non-spherical linear interpolation when the absolute dot product of <code>this</code> and <code>target</code> is
     * below <code>1E-6f</code>.
     * 
     * @param target
     *          the target of the interpolation, which should be reached with <code>alpha = 1.0</code>
     * @param alpha
     *          the interpolation factor, within <code>[0..1]</code>
     * @return this
     */
    public Quaternionf slerp(Quaternionfc target, float alpha) {
        return slerp(target, alpha, this);
    }

    public Quaternionf slerp(Quaternionfc target, float alpha, Quaternionf dest) {
        float cosom = Math.fma(x, target.x(), Math.fma(y, target.y(), Math.fma(z, target.z(), w * target.w())));
        float absCosom = Math.abs(cosom);
        float scale0, scale1;
        if (1.0f - absCosom > 1E-6f) {
            float sinSqr = 1.0f - absCosom * absCosom;
            float sinom = Math.invsqrt(sinSqr);
            float omega = Math.atan2(sinSqr * sinom, absCosom);
            scale0 = (float) (Math.sin((1.0 - alpha) * omega) * sinom);
            scale1 = (float) (Math.sin(alpha * omega) * sinom);
        } else {
            scale0 = 1.0f - alpha;
            scale1 = alpha;
        }
        scale1 = cosom >= 0.0f ? scale1 : -scale1;
        dest.x = Math.fma(scale0, x, scale1 * target.x());
        dest.y = Math.fma(scale0, y, scale1 * target.y());
        dest.z = Math.fma(scale0, z, scale1 * target.z());
        dest.w = Math.fma(scale0, w, scale1 * target.w());
        return dest;
    }

    /**
     * Interpolate between all of the quaternions given in <code>qs</code> via spherical linear interpolation using the specified interpolation factors <code>weights</code>,
     * and store the result in <code>dest</code>.
     * <p>
     * This method will interpolate between each two successive quaternions via {@link #slerp(Quaternionfc, float)} using their relative interpolation weights.
     * <p>
     * This method resorts to non-spherical linear interpolation when the absolute dot product of any two interpolated quaternions is below <code>1E-6f</code>.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/62354/method-for-interpolation-between-3-quaternions#answer-62356">http://gamedev.stackexchange.com/</a>
     * 
     * @param qs
     *          the quaternions to interpolate over
     * @param weights
     *          the weights of each individual quaternion in <code>qs</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    public static Quaternionfc slerp(Quaternionf[] qs, float[] weights, Quaternionf dest) {
        dest.set(qs[0]);
        float w = weights[0];
        for (int i = 1; i < qs.length; i++) {
            float w0 = w;
            float w1 = weights[i];
            float rw1 = w1 / (w0 + w1);
            w += w1;
            dest.slerp(qs[i], rw1);
        }
        return dest;
    }

    /**
     * Apply scaling to this quaternion, which results in any vector transformed by this quaternion to change
     * its length by the given <code>factor</code>.
     * 
     * @param factor
     *          the scaling factor
     * @return this
     */
    public Quaternionf scale(float factor) {
        return scale(factor, this);
    }

    public Quaternionf scale(float factor, Quaternionf dest) {
        float sqrt = Math.sqrt(factor);
        dest.x = sqrt * x;
        dest.y = sqrt * y;
        dest.z = sqrt * z;
        dest.w = sqrt * w;
        return dest;
    }

    /**
     * Set this quaternion to represent scaling, which results in a transformed vector to change
     * its length by the given <code>factor</code>.
     * 
     * @param factor
     *          the scaling factor
     * @return this
     */
    public Quaternionf scaling(float factor) {
        float sqrt = Math.sqrt(factor);
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = sqrt;
        return this;
    }

    /**
     * Integrate the rotation given by the angular velocity <code>(vx, vy, vz)</code> around the x, y and z axis, respectively,
     * with respect to the given elapsed time delta <code>dt</code> and add the differentiate rotation to the rotation represented by this quaternion.
     * <p>
     * This method pre-multiplies the rotation given by <code>dt</code> and <code>(vx, vy, vz)</code> by <code>this</code>, so
     * the angular velocities are always relative to the local coordinate system of the rotation represented by <code>this</code> quaternion.
     * <p>
     * This method is equivalent to calling: <code>rotateLocal(dt * vx, dt * vy, dt * vz)</code>
     * <p>
     * Reference: <a href="http://physicsforgames.blogspot.de/2010/02/quaternions.html">http://physicsforgames.blogspot.de/</a>
     * 
     * @param dt
     *          the delta time
     * @param vx
     *          the angular velocity around the x axis
     * @param vy
     *          the angular velocity around the y axis
     * @param vz
     *          the angular velocity around the z axis
     * @return this
     */
    public Quaternionf integrate(float dt, float vx, float vy, float vz) {
        return integrate(dt, vx, vy, vz, this);
    }

    public Quaternionf integrate(float dt, float vx, float vy, float vz, Quaternionf dest) {
        float thetaX = dt * vx * 0.5f;
        float thetaY = dt * vy * 0.5f;
        float thetaZ = dt * vz * 0.5f;
        float thetaMagSq = thetaX * thetaX + thetaY * thetaY + thetaZ * thetaZ;
        float s;
        float dqX, dqY, dqZ, dqW;
        if (thetaMagSq * thetaMagSq / 24.0f < 1E-8f) {
            dqW = 1.0f - thetaMagSq * 0.5f;
            s = 1.0f - thetaMagSq / 6.0f;
        } else {
            float thetaMag = Math.sqrt(thetaMagSq);
            float sin = Math.sin(thetaMag);
            s = sin / thetaMag;
            dqW = Math.cosFromSin(sin, thetaMag);
        }
        dqX = thetaX * s;
        dqY = thetaY * s;
        dqZ = thetaZ * s;
        /* Pre-multiplication */
        return dest.set(Math.fma(dqW, x, Math.fma(dqX, w, Math.fma(dqY, z, -dqZ * y))),
                        Math.fma(dqW, y, Math.fma(-dqX, z, Math.fma(dqY, w, dqZ * x))),
                        Math.fma(dqW, z, Math.fma(dqX, y, Math.fma(-dqY, x, dqZ * w))),
                        Math.fma(dqW, w, Math.fma(-dqX, x, Math.fma(-dqY, y, -dqZ * z))));
    }

    /**
     * Compute a linear (non-spherical) interpolation of <code>this</code> and the given quaternion <code>q</code>
     * and store the result in <code>this</code>.
     * 
     * @param q
     *          the other quaternion
     * @param factor
     *          the interpolation factor. It is between 0.0 and 1.0
     * @return this
     */
    public Quaternionf nlerp(Quaternionfc q, float factor) {
        return nlerp(q, factor, this);
    }

    public Quaternionf nlerp(Quaternionfc q, float factor, Quaternionf dest) {
        float cosom = Math.fma(x, q.x(), Math.fma(y, q.y(), Math.fma(z, q.z(), w * q.w())));
        float scale0 = 1.0f - factor;
        float scale1 = (cosom >= 0.0f) ? factor : -factor;
        dest.x = Math.fma(scale0, x, scale1 * q.x());
        dest.y = Math.fma(scale0, y, scale1 * q.y());
        dest.z = Math.fma(scale0, z, scale1 * q.z());
        dest.w = Math.fma(scale0, w, scale1 * q.w());
        float s = Math.invsqrt(Math.fma(dest.x, dest.x, Math.fma(dest.y, dest.y, Math.fma(dest.z, dest.z, dest.w * dest.w))));
        dest.x *= s;
        dest.y *= s;
        dest.z *= s;
        dest.w *= s;
        return dest;
    }

    /**
     * Interpolate between all of the quaternions given in <code>qs</code> via non-spherical linear interpolation using the
     * specified interpolation factors <code>weights</code>, and store the result in <code>dest</code>.
     * <p>
     * This method will interpolate between each two successive quaternions via {@link #nlerp(Quaternionfc, float)}
     * using their relative interpolation weights.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/62354/method-for-interpolation-between-3-quaternions#answer-62356">http://gamedev.stackexchange.com/</a>
     * 
     * @param qs
     *          the quaternions to interpolate over
     * @param weights
     *          the weights of each individual quaternion in <code>qs</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    public static Quaternionfc nlerp(Quaternionfc[] qs, float[] weights, Quaternionf dest) {
        dest.set(qs[0]);
        float w = weights[0];
        for (int i = 1; i < qs.length; i++) {
            float w0 = w;
            float w1 = weights[i];
            float rw1 = w1 / (w0 + w1);
            w += w1;
            dest.nlerp(qs[i], rw1);
        }
        return dest;
    }

    public Quaternionf nlerpIterative(Quaternionfc q, float alpha, float dotThreshold, Quaternionf dest) {
        float q1x = x, q1y = y, q1z = z, q1w = w;
        float q2x = q.x(), q2y = q.y(), q2z = q.z(), q2w = q.w();
        float dot = Math.fma(q1x, q2x, Math.fma(q1y, q2y, Math.fma(q1z, q2z, q1w * q2w)));
        float absDot = Math.abs(dot);
        if (1.0f - 1E-6f < absDot) {
            return dest.set(this);
        }
        float alphaN = alpha;
        while (absDot < dotThreshold) {
            float scale0 = 0.5f;
            float scale1 = dot >= 0.0f ? 0.5f : -0.5f;
            if (alphaN < 0.5f) {
                q2x = Math.fma(scale0, q2x, scale1 * q1x);
                q2y = Math.fma(scale0, q2y, scale1 * q1y);
                q2z = Math.fma(scale0, q2z, scale1 * q1z);
                q2w = Math.fma(scale0, q2w, scale1 * q1w);
                float s = Math.invsqrt(Math.fma(q2x, q2x, Math.fma(q2y, q2y, Math.fma(q2z, q2z, q2w * q2w))));
                q2x *= s;
                q2y *= s;
                q2z *= s;
                q2w *= s;
                alphaN = alphaN + alphaN;
            } else {
                q1x = Math.fma(scale0, q1x, scale1 * q2x);
                q1y = Math.fma(scale0, q1y, scale1 * q2y);
                q1z = Math.fma(scale0, q1z, scale1 * q2z);
                q1w = Math.fma(scale0, q1w, scale1 * q2w);
                float s = Math.invsqrt(Math.fma(q1x, q1x, Math.fma(q1y, q1y, Math.fma(q1z, q1z, q1w * q1w))));
                q1x *= s;
                q1y *= s;
                q1z *= s;
                q1w *= s;
                alphaN = alphaN + alphaN - 1.0f;
            }
            dot = Math.fma(q1x, q2x, Math.fma(q1y, q2y, Math.fma(q1z, q2z, q1w * q2w)));
            absDot = Math.abs(dot);
        }
        float scale0 = 1.0f - alphaN;
        float scale1 = dot >= 0.0f ? alphaN : -alphaN;
        float resX = Math.fma(scale0, q1x, scale1 * q2x);
        float resY = Math.fma(scale0, q1y, scale1 * q2y);
        float resZ = Math.fma(scale0, q1z, scale1 * q2z);
        float resW = Math.fma(scale0, q1w, scale1 * q2w);
        float s = Math.invsqrt(Math.fma(resX, resX, Math.fma(resY, resY, Math.fma(resZ, resZ, resW * resW))));
        dest.x = resX * s;
        dest.y = resY * s;
        dest.z = resZ * s;
        dest.w = resW * s;
        return dest;
    }

    /**
     * Compute linear (non-spherical) interpolations of <code>this</code> and the given quaternion <code>q</code>
     * iteratively and store the result in <code>this</code>.
     * <p>
     * This method performs a series of small-step nlerp interpolations to avoid doing a costly spherical linear interpolation, like
     * {@link #slerp(Quaternionfc, float, Quaternionf) slerp},
     * by subdividing the rotation arc between <code>this</code> and <code>q</code> via non-spherical linear interpolations as long as
     * the absolute dot product of <code>this</code> and <code>q</code> is greater than the given <code>dotThreshold</code> parameter.
     * <p>
     * Thanks to <code>@theagentd</code> at <a href="http://www.java-gaming.org/">http://www.java-gaming.org/</a> for providing the code.
     * 
     * @param q
     *          the other quaternion
     * @param alpha
     *          the interpolation factor, between 0.0 and 1.0
     * @param dotThreshold
     *          the threshold for the dot product of <code>this</code> and <code>q</code> above which this method performs another iteration
     *          of a small-step linear interpolation
     * @return this
     */
    public Quaternionf nlerpIterative(Quaternionfc q, float alpha, float dotThreshold) {
        return nlerpIterative(q, alpha, dotThreshold, this);
    }

    /**
     * Interpolate between all of the quaternions given in <code>qs</code> via iterative non-spherical linear interpolation using the
     * specified interpolation factors <code>weights</code>, and store the result in <code>dest</code>.
     * <p>
     * This method will interpolate between each two successive quaternions via {@link #nlerpIterative(Quaternionfc, float, float)}
     * using their relative interpolation weights.
     * <p>
     * Reference: <a href="http://gamedev.stackexchange.com/questions/62354/method-for-interpolation-between-3-quaternions#answer-62356">http://gamedev.stackexchange.com/</a>
     * 
     * @param qs
     *          the quaternions to interpolate over
     * @param weights
     *          the weights of each individual quaternion in <code>qs</code>
     * @param dotThreshold
     *          the threshold for the dot product of each two interpolated quaternions above which {@link #nlerpIterative(Quaternionfc, float, float)} performs another iteration
     *          of a small-step linear interpolation
     * @param dest
     *          will hold the result
     * @return dest
     */
    public static Quaternionfc nlerpIterative(Quaternionf[] qs, float[] weights, float dotThreshold, Quaternionf dest) {
        dest.set(qs[0]);
        float w = weights[0];
        for (int i = 1; i < qs.length; i++) {
            float w0 = w;
            float w1 = weights[i];
            float rw1 = w1 / (w0 + w1);
            w += w1;
            dest.nlerpIterative(qs[i], rw1, dotThreshold);
        }
        return dest;
    }

    /**
     * Apply a rotation to this quaternion that maps the given direction to the positive Z axis.
     * <p>
     * Because there are multiple possibilities for such a rotation, this method will choose the one that ensures the given up direction to remain
     * parallel to the plane spanned by the <code>up</code> and <code>dir</code> vectors. 
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * <p>
     * Reference: <a href="http://answers.unity3d.com/questions/467614/what-is-the-source-code-of-quaternionlookrotation.html">http://answers.unity3d.com</a>
     * 
     * @see #lookAlong(float, float, float, float, float, float, Quaternionf)
     * 
     * @param dir
     *              the direction to map to the positive Z axis
     * @param up
     *              the vector which will be mapped to a vector parallel to the plane
     *              spanned by the given <code>dir</code> and <code>up</code>
     * @return this
     */
    public Quaternionf lookAlong(Vector3fc dir, Vector3fc up) {
        return lookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), this);
    }

    public Quaternionf lookAlong(Vector3fc dir, Vector3fc up, Quaternionf dest) {
        return lookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z(), dest);
    }

    /**
     * Apply a rotation to this quaternion that maps the given direction to the positive Z axis.
     * <p>
     * Because there are multiple possibilities for such a rotation, this method will choose the one that ensures the given up direction to remain
     * parallel to the plane spanned by the <code>up</code> and <code>dir</code> vectors. 
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * <p>
     * Reference: <a href="http://answers.unity3d.com/questions/467614/what-is-the-source-code-of-quaternionlookrotation.html">http://answers.unity3d.com</a>
     * 
     * @see #lookAlong(float, float, float, float, float, float, Quaternionf)
     * 
     * @param dirX
     *              the x-coordinate of the direction to look along
     * @param dirY
     *              the y-coordinate of the direction to look along
     * @param dirZ
     *              the z-coordinate of the direction to look along
     * @param upX
     *              the x-coordinate of the up vector
     * @param upY
     *              the y-coordinate of the up vector
     * @param upZ
     *              the z-coordinate of the up vector
     * @return this
     */
    public Quaternionf lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        return lookAlong(dirX, dirY, dirZ, upX, upY, upZ, this);
    }

    public Quaternionf lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Quaternionf dest) {
        // Normalize direction
        float invDirLength = Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        float dirnX = -dirX * invDirLength;
        float dirnY = -dirY * invDirLength;
        float dirnZ = -dirZ * invDirLength;
        // left = up x dir
        float leftX, leftY, leftZ;
        leftX = upY * dirnZ - upZ * dirnY;
        leftY = upZ * dirnX - upX * dirnZ;
        leftZ = upX * dirnY - upY * dirnX;
        // normalize left
        float invLeftLength = Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirnY * leftZ - dirnZ * leftY;
        float upnY = dirnZ * leftX - dirnX * leftZ;
        float upnZ = dirnX * leftY - dirnY * leftX;

        /* Convert orthonormal basis vectors to quaternion */
        float x, y, z, w;
        double t;
        double tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = Math.sqrt(tr + 1.0);
            w = (float) (t * 0.5);
            t = 0.5 / t;
            x = (float) ((dirnY - upnZ) * t);
            y = (float) ((leftZ - dirnX) * t);
            z = (float) ((upnX - leftY) * t);
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = Math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (float) (t * 0.5);
                t = 0.5 / t;
                y = (float) ((leftY + upnX) * t);
                z = (float) ((dirnX + leftZ) * t);
                w = (float) ((dirnY - upnZ) * t);
            } else if (upnY > dirnZ) {
                t = Math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((leftY + upnX) * t);
                z = (float) ((upnZ + dirnY) * t);
                w = (float) ((leftZ - dirnX) * t);
            } else {
                t = Math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((dirnX + leftZ) * t);
                y = (float) ((upnZ + dirnY) * t);
                w = (float) ((upnX - leftY) * t);
            }
        }
        /* Multiply */
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    /**
     * Set <code>this</code> quaternion to a rotation that rotates the <code>fromDir</code> vector to point along <code>toDir</code>.
     * <p>
     * Since there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/1171849/finding-quaternion-representing-the-rotation-from-one-vector-to-another#answer-1171995">stackoverflow.com</a>
     * 
     * @param fromDirX
     *              the x-coordinate of the direction to rotate into the destination direction
     * @param fromDirY
     *              the y-coordinate of the direction to rotate into the destination direction
     * @param fromDirZ
     *              the z-coordinate of the direction to rotate into the destination direction
     * @param toDirX
     *              the x-coordinate of the direction to rotate to
     * @param toDirY
     *              the y-coordinate of the direction to rotate to
     * @param toDirZ
     *              the z-coordinate of the direction to rotate to
     * @return this
     */
    public Quaternionf rotationTo(float fromDirX, float fromDirY, float fromDirZ, float toDirX, float toDirY, float toDirZ) {
        float fn = Math.invsqrt(Math.fma(fromDirX, fromDirX, Math.fma(fromDirY, fromDirY, fromDirZ * fromDirZ)));
        float tn = Math.invsqrt(Math.fma(toDirX, toDirX, Math.fma(toDirY, toDirY, toDirZ * toDirZ)));
        float fx = fromDirX * fn, fy = fromDirY * fn, fz = fromDirZ * fn;
        float tx = toDirX * tn, ty = toDirY * tn, tz = toDirZ * tn;
        float dot = fx * tx + fy * ty + fz * tz;
        float x, y, z, w;
        if (dot < -1.0f + 1E-6f) {
            x = fy;
            y = -fx;
            z = 0.0f;
            w = 0.0f;
            if (x * x + y * y == 0.0f) {
                x = 0.0f;
                y = fz;
                z = -fy;
                w = 0.0f;
            }
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = 0;
        } else {
            float sd2 = Math.sqrt((1.0f + dot) * 2.0f);
            float isd2 = 1.0f / sd2;
            float cx = fy * tz - fz * ty;
            float cy = fz * tx - fx * tz;
            float cz = fx * ty - fy * tx;
            x = cx * isd2;
            y = cy * isd2;
            z = cz * isd2;
            w = sd2 * 0.5f;
            float n2 = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
            this.x = x * n2;
            this.y = y * n2;
            this.z = z * n2;
            this.w = w * n2;
        }
        return this;
    }

    /**
     * Set <code>this</code> quaternion to a rotation that rotates the <code>fromDir</code> vector to point along <code>toDir</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * 
     * @see #rotationTo(float, float, float, float, float, float)
     * 
     * @param fromDir
     *          the starting direction
     * @param toDir
     *          the destination direction
     * @return this
     */
    public Quaternionf rotationTo(Vector3fc fromDir, Vector3fc toDir) {
        return rotationTo(fromDir.x(), fromDir.y(), fromDir.z(), toDir.x(), toDir.y(), toDir.z());
    }

    public Quaternionf rotateTo(float fromDirX, float fromDirY, float fromDirZ, float toDirX, float toDirY, float toDirZ, Quaternionf dest) {
        float fn = Math.invsqrt(Math.fma(fromDirX, fromDirX, Math.fma(fromDirY, fromDirY, fromDirZ * fromDirZ)));
        float tn = Math.invsqrt(Math.fma(toDirX, toDirX, Math.fma(toDirY, toDirY, toDirZ * toDirZ)));
        float fx = fromDirX * fn, fy = fromDirY * fn, fz = fromDirZ * fn;
        float tx = toDirX * tn, ty = toDirY * tn, tz = toDirZ * tn;
        float dot = fx * tx + fy * ty + fz * tz;
        float x, y, z, w;
        if (dot < -1.0f + 1E-6f) {
            x = fy;
            y = -fx;
            z = 0.0f;
            w = 0.0f;
            if (x * x + y * y == 0.0f) {
                x = 0.0f;
                y = fz;
                z = -fy;
                w = 0.0f;
            }
        } else {
            float sd2 = Math.sqrt((1.0f + dot) * 2.0f);
            float isd2 = 1.0f / sd2;
            float cx = fy * tz - fz * ty;
            float cy = fz * tx - fx * tz;
            float cz = fx * ty - fy * tx;
            x = cx * isd2;
            y = cy * isd2;
            z = cz * isd2;
            w = sd2 * 0.5f;
            float n2 = Math.invsqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
            x *= n2;
            y *= n2;
            z *= n2;
            w *= n2;
        }
        /* Multiply */
        return dest.set(Math.fma(this.w, x, Math.fma(this.x, w, Math.fma(this.y, z, -this.z * y))),
                        Math.fma(this.w, y, Math.fma(-this.x, z, Math.fma(this.y, w, this.z * x))),
                        Math.fma(this.w, z, Math.fma(this.x, y, Math.fma(-this.y, x, this.z * w))),
                        Math.fma(this.w, w, Math.fma(-this.x, x, Math.fma(-this.y, y, -this.z * z))));
    }

    /**
     * Apply a rotation to <code>this</code> that rotates the <code>fromDir</code> vector to point along <code>toDir</code>.
     * <p>
     * Since there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateTo(float, float, float, float, float, float, Quaternionf)
     * 
     * @param fromDirX
     *              the x-coordinate of the direction to rotate into the destination direction
     * @param fromDirY
     *              the y-coordinate of the direction to rotate into the destination direction
     * @param fromDirZ
     *              the z-coordinate of the direction to rotate into the destination direction
     * @param toDirX
     *              the x-coordinate of the direction to rotate to
     * @param toDirY
     *              the y-coordinate of the direction to rotate to
     * @param toDirZ
     *              the z-coordinate of the direction to rotate to
     * @return this
     */
    public Quaternionf rotateTo(float fromDirX, float fromDirY, float fromDirZ, float toDirX, float toDirY, float toDirZ) {
        return rotateTo(fromDirX, fromDirY, fromDirZ, toDirX, toDirY, toDirZ, this);
    }

    public Quaternionf rotateTo(Vector3fc fromDir, Vector3fc toDir, Quaternionf dest) {
        return rotateTo(fromDir.x(), fromDir.y(), fromDir.z(), toDir.x(), toDir.y(), toDir.z(), dest);
    }

    /**
     * Apply a rotation to <code>this</code> that rotates the <code>fromDir</code> vector to point along <code>toDir</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateTo(float, float, float, float, float, float, Quaternionf)
     * 
     * @param fromDir
     *          the starting direction
     * @param toDir
     *          the destination direction
     * @return this
     */
    public Quaternionf rotateTo(Vector3fc fromDir, Vector3fc toDir) {
        return rotateTo(fromDir.x(), fromDir.y(), fromDir.z(), toDir.x(), toDir.y(), toDir.z(), this);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the x axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the x axis
     * @return this
     */
    public Quaternionf rotateX(float angle) {
        return rotateX(angle, this);
    }

    public Quaternionf rotateX(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return dest.set(w * sin + x * cos,
                        y * cos + z * sin,
                        z * cos - y * sin,
                        w * cos - x * sin);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the y axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the y axis
     * @return this
     */
    public Quaternionf rotateY(float angle) {
        return rotateY(angle, this);
    }

    public Quaternionf rotateY(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return dest.set(x * cos - z * sin,
                        w * sin + y * cos,
                        x * sin + z * cos,
                        w * cos - y * sin);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the z axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the z axis
     * @return this
     */
    public Quaternionf rotateZ(float angle) {
        return rotateZ(angle, this);
    }

    public Quaternionf rotateZ(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return dest.set(x * cos + y * sin,
                        y * cos - x * sin,
                        w * sin + z * cos,
                        w * cos - z * sin);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local x axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local x axis
     * @return this
     */
    public Quaternionf rotateLocalX(float angle) {
        return rotateLocalX(angle, this);
    }

    public Quaternionf rotateLocalX(float angle, Quaternionf dest) {
        float hangle = angle * 0.5f;
        float s = Math.sin(hangle);
        float c = Math.cosFromSin(s, hangle);
        dest.set(c * x + s * w,
                 c * y - s * z,
                 c * z + s * y,
                 c * w - s * x);
        return dest;
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local y axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local y axis
     * @return this
     */
    public Quaternionf rotateLocalY(float angle) {
        return rotateLocalY(angle, this);
    }

    public Quaternionf rotateLocalY(float angle, Quaternionf dest) {
        float hangle = angle * 0.5f;
        float s = Math.sin(hangle);
        float c = Math.cosFromSin(s, hangle);
        dest.set(c * x + s * z,
                 c * y + s * w,
                 c * z - s * x,
                 c * w - s * y);
        return dest;
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local z axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local z axis
     * @return this
     */
    public Quaternionf rotateLocalZ(float angle) {
        return rotateLocalZ(angle, this);
    }

    public Quaternionf rotateLocalZ(float angle, Quaternionf dest) {
        float hangle = angle * 0.5f;
        float s = Math.sin(hangle);
        float c = Math.cosFromSin(s, hangle);
        dest.set(c * x - s * y,
                 c * y + s * x,
                 c * z + s * w,
                 c * w - s * z);
        return dest;
    }

    public Quaternionf rotateAxis(float angle, float axisX, float axisY, float axisZ, Quaternionf dest) {
        float hangle = angle / 2.0f;
        float sinAngle = Math.sin(hangle);
        float invVLength = Math.invsqrt(Math.fma(axisX, axisX, Math.fma(axisY, axisY, axisZ * axisZ)));
        float rx = axisX * invVLength * sinAngle;
        float ry = axisY * invVLength * sinAngle;
        float rz = axisZ * invVLength * sinAngle;
        float rw = Math.cosFromSin(sinAngle, hangle);
        return dest.set(Math.fma(this.w, rx, Math.fma(this.x, rw, Math.fma(this.y, rz, -this.z * ry))),
                        Math.fma(this.w, ry, Math.fma(-this.x, rz, Math.fma(this.y, rw, this.z * rx))),
                        Math.fma(this.w, rz, Math.fma(this.x, ry, Math.fma(-this.y, rx, this.z * rw))),
                        Math.fma(this.w, rw, Math.fma(-this.x, rx, Math.fma(-this.y, ry, -this.z * rz))));
    }

    public Quaternionf rotateAxis(float angle, Vector3fc axis, Quaternionf dest) {
        return rotateAxis(angle, axis.x(), axis.y(), axis.z(), dest);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the specified axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateAxis(float, float, float, float, Quaternionf)
     * 
     * @param angle
     *              the angle in radians to rotate about the specified axis
     * @param axis
     *              the rotation axis
     * @return this
     */
    public Quaternionf rotateAxis(float angle, Vector3fc axis) {
        return rotateAxis(angle, axis.x(), axis.y(), axis.z(), this);
    }

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the specified axis.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateAxis(float, float, float, float, Quaternionf)
     * 
     * @param angle
     *              the angle in radians to rotate about the specified axis
     * @param axisX
     *              the x coordinate of the rotation axis
     * @param axisY
     *              the y coordinate of the rotation axis
     * @param axisZ
     *              the z coordinate of the rotation axis
     * @return this
     */
    public Quaternionf rotateAxis(float angle, float axisX, float axisY, float axisZ) {
        return rotateAxis(angle, axisX, axisY, axisZ, this);
    }

    /**
     * Return a string representation of this quaternion.
     * <p>
     * This method creates a new {@link DecimalFormat} on every invocation with the format string "<code>0.000E0;-</code>".
     * 
     * @return the string representation
     */
    public String toString() {
        return Runtime.formatNumbers(toString(Options.NUMBER_FORMAT));
    }

    /**
     * Return a string representation of this quaternion by formatting the components with the given {@link NumberFormat}.
     * 
     * @param formatter
     *          the {@link NumberFormat} used to format the quaternion components with
     * @return the string representation
     */
    public String toString(NumberFormat formatter) {
        return "(" + Runtime.format(x, formatter) + " " + Runtime.format(y, formatter) + " " + Runtime.format(z, formatter) + " " + Runtime.format(w, formatter) + ")";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
        out.writeFloat(w);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        w = in.readFloat();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(w);
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
        Quaternionf other = (Quaternionf) obj;
        if (Float.floatToIntBits(w) != Float.floatToIntBits(other.w))
            return false;
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
            return false;
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
            return false;
        if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
            return false;
        return true;
    }

    /**
     * Compute the difference between <code>this</code> and the <code>other</code> quaternion
     * and store the result in <code>this</code>.
     * <p>
     * The difference is the rotation that has to be applied to get from
     * <code>this</code> rotation to <code>other</code>. If <code>T</code> is <code>this</code>, <code>Q</code>
     * is <code>other</code> and <code>D</code> is the computed difference, then the following equation holds:
     * <p>
     * <code>T * D = Q</code>
     * <p>
     * It is defined as: <code>D = T^-1 * Q</code>, where <code>T^-1</code> denotes the {@link #invert() inverse} of <code>T</code>.
     * 
     * @param other
     *          the other quaternion
     * @return this
     */
    public Quaternionf difference(Quaternionf other) {
        return difference(other, this);
    }

    public Quaternionf difference(Quaternionfc other, Quaternionf dest) {
        float invNorm = 1.0f / lengthSquared();
        float x = -this.x * invNorm;
        float y = -this.y * invNorm;
        float z = -this.z * invNorm;
        float w = this.w * invNorm;
        dest.set(Math.fma(w, other.x(), Math.fma(x, other.w(), Math.fma(y, other.z(), -z * other.y()))),
                 Math.fma(w, other.y(), Math.fma(-x, other.z(), Math.fma(y, other.w(), z * other.x()))),
                 Math.fma(w, other.z(), Math.fma(x, other.y(), Math.fma(-y, other.x(), z * other.w()))),
                 Math.fma(w, other.w(), Math.fma(-x, other.x(), Math.fma(-y, other.y(), -z * other.z()))));
        return dest;
    }

    public Vector3f positiveX(Vector3f dir) {
        float invNorm = 1.0f / lengthSquared();
        float nx = -x * invNorm;
        float ny = -y * invNorm;
        float nz = -z * invNorm;
        float nw =  w * invNorm;
        float dy = ny + ny;
        float dz = nz + nz;
        dir.x = -ny * dy - nz * dz + 1.0f;
        dir.y =  nx * dy + nw * dz;
        dir.z =  nx * dz - nw * dy;
        return dir;
    }

    public Vector3f normalizedPositiveX(Vector3f dir) {
        float dy = y + y;
        float dz = z + z;
        dir.x = -y * dy - z * dz + 1.0f;
        dir.y =  x * dy - w * dz;
        dir.z =  x * dz + w * dy;
        return dir;
    }

    public Vector3f positiveY(Vector3f dir) {
        float invNorm = 1.0f / lengthSquared();
        float nx = -x * invNorm;
        float ny = -y * invNorm;
        float nz = -z * invNorm;
        float nw =  w * invNorm;
        float dx = nx + nx;
        float dy = ny + ny;
        float dz = nz + nz;
        dir.x =  nx * dy - nw * dz;
        dir.y = -nx * dx - nz * dz + 1.0f;
        dir.z =  ny * dz + nw * dx;
        return dir;
    }

    public Vector3f normalizedPositiveY(Vector3f dir) {
        float dx = x + x;
        float dy = y + y;
        float dz = z + z;
        dir.x =  x * dy + w * dz;
        dir.y = -x * dx - z * dz + 1.0f;
        dir.z =  y * dz - w * dx;
        return dir;
    }

    public Vector3f positiveZ(Vector3f dir) {
        float invNorm = 1.0f / lengthSquared();
        float nx = -x * invNorm;
        float ny = -y * invNorm;
        float nz = -z * invNorm;
        float nw =  w * invNorm;
        float dx = nx + nx;
        float dy = ny + ny;
        float dz = nz + nz;
        dir.x =  nx * dz + nw * dy;
        dir.y =  ny * dz - nw * dx;
        dir.z = -nx * dx - ny * dy + 1.0f;
        return dir;
    }

    public Vector3f normalizedPositiveZ(Vector3f dir) {
        float dx = x + x;
        float dy = y + y;
        float dz = z + z;
        dir.x =  x * dz - w * dy;
        dir.y =  y * dz + w * dx;
        dir.z = -x * dx - y * dy + 1.0f;
        return dir;
    }

    /**
     * Conjugate <code>this</code> by the given quaternion <code>q</code> by computing <code>q * this * q^-1</code>.
     * 
     * @param q
     *          the {@link Quaternionfc} to conjugate <code>this</code> by
     * @return this
     */
    public Quaternionf conjugateBy(Quaternionfc q) {
        return conjugateBy(q, this);
    }

    /**
     * Conjugate <code>this</code> by the given quaternion <code>q</code> by computing <code>q * this * q^-1</code>
     * and store the result into <code>dest</code>.
     * 
     * @param q
     *          the {@link Quaternionfc} to conjugate <code>this</code> by
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Quaternionf conjugateBy(Quaternionfc q, Quaternionf dest) {
        float invNorm = 1.0f / q.lengthSquared();
        float qix = -q.x() * invNorm, qiy = -q.y() * invNorm, qiz = -q.z() * invNorm, qiw = q.w() * invNorm;
        float qpx = Math.fma(q.w(), x, Math.fma(q.x(), w, Math.fma(q.y(), z, -q.z() * y)));
        float qpy = Math.fma(q.w(), y, Math.fma(-q.x(), z, Math.fma(q.y(), w, q.z() * x)));
        float qpz = Math.fma(q.w(), z, Math.fma(q.x(), y, Math.fma(-q.y(), x, q.z() * w)));
        float qpw = Math.fma(q.w(), w, Math.fma(-q.x(), x, Math.fma(-q.y(), y, -q.z() * z)));
        return dest.set(Math.fma(qpw, qix, Math.fma(qpx, qiw, Math.fma(qpy, qiz, -qpz * qiy))),
                        Math.fma(qpw, qiy, Math.fma(-qpx, qiz, Math.fma(qpy, qiw, qpz * qix))),
                        Math.fma(qpw, qiz, Math.fma(qpx, qiy, Math.fma(-qpy, qix, qpz * qiw))),
                        Math.fma(qpw, qiw, Math.fma(-qpx, qix, Math.fma(-qpy, qiy, -qpz * qiz))));
    }

    public boolean isFinite() {
        return Math.isFinite(x) && Math.isFinite(y) && Math.isFinite(z) && Math.isFinite(w);
    }

    public boolean equals(Quaternionfc q, float delta) {
        if (this == q)
            return true;
        if (q == null)
            return false;
        if (!(q instanceof Quaternionfc))
            return false;
        if (!Runtime.equals(x, q.x(), delta))
            return false;
        if (!Runtime.equals(y, q.y(), delta))
            return false;
        if (!Runtime.equals(z, q.z(), delta))
            return false;
        if (!Runtime.equals(w, q.w(), delta))
            return false;
        return true;
    }

    public boolean equals(float x, float y, float z, float w) {
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(x))
            return false;
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(y))
            return false;
        if (Float.floatToIntBits(this.z) != Float.floatToIntBits(z))
            return false;
        if (Float.floatToIntBits(this.w) != Float.floatToIntBits(w))
            return false;
        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
