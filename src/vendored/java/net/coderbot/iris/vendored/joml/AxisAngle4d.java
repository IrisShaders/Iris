/*
 * The MIT License
 *
 * Copyright (c) 2015-2021 Kai Burjack
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
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Represents a 3D rotation of a given radians about an axis represented as an
 * unit 3D vector.
 * <p>
 * This class uses double-precision components.
 * 
 * @author Kai Burjack
 */
public class AxisAngle4d implements Externalizable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * The angle in radians.
     */
    public double angle;
    /**
     * The x-component of the rotation axis.
     */
    public double x;
    /**
     * The y-component of the rotation axis.
     */
    public double y;
    /**
     * The z-component of the rotation axis.
     */
    public double z;

    /**
     * Create a new {@link AxisAngle4d} with zero rotation about <code>(0, 0, 1)</code>.
     */
    public AxisAngle4d() {
        z = 1.0;
    }

    /**
     * Create a new {@link AxisAngle4d} with the same values of <code>a</code>.
     * 
     * @param a
     *            the AngleAxis4d to copy the values from
     */
    public AxisAngle4d(AxisAngle4d a) {
        x = a.x;
        y = a.y;
        z = a.z;
        angle = (a.angle < 0.0 ? Math.PI + Math.PI + a.angle % (Math.PI + Math.PI) : a.angle) % (Math.PI + Math.PI);
    }

    /**
     * Create a new {@link AxisAngle4d} with the same values of <code>a</code>.
     * 
     * @param a
     *            the AngleAxis4f to copy the values from
     */
    public AxisAngle4d(AxisAngle4f a) {
        x = a.x;
        y = a.y;
        z = a.z;
        angle = (a.angle < 0.0 ? Math.PI + Math.PI + a.angle % (Math.PI + Math.PI) : a.angle) % (Math.PI + Math.PI);
    }

    /**
     * Create a new {@link AxisAngle4d} from the given {@link Quaternionfc}.
     * <p>
     * Reference: <a href=
     * "http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle/"
     * >http://www.euclideanspace.com</a>
     * 
     * @param q
     *            the quaternion from which to create the new AngleAxis4f
     */
    public AxisAngle4d(Quaternionfc q) {
        float acos = Math.safeAcos(q.w());
        float invSqrt = Math.invsqrt(1.0f - q.w() * q.w());
        if (Float.isInfinite(invSqrt)) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 1.0;
        } else {
            this.x = q.x() * invSqrt;
            this.y = q.y() * invSqrt;
            this.z = q.z() * invSqrt;
        }
        this.angle = acos + acos;
    }

    /**
     * Create a new {@link AxisAngle4d} from the given {@link Quaterniondc}.
     * <p>
     * Reference: <a href=
     * "http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle/"
     * >http://www.euclideanspace.com</a>
     * 
     * @param q
     *            the quaternion from which to create the new AngleAxis4d
     */
    public AxisAngle4d(Quaterniondc q) {
        double acos = Math.safeAcos(q.w());
        double invSqrt = Math.invsqrt(1.0 - q.w() * q.w());
        if (Double.isInfinite(invSqrt)) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 1.0;
        } else {
            this.x = q.x() * invSqrt;
            this.y = q.y() * invSqrt;
            this.z = q.z() * invSqrt;
        }
        this.angle = acos + acos;
    }

    /**
     * Create a new {@link AxisAngle4d} with the given values.
     *
     * @param angle
     *            the angle in radians
     * @param x
     *            the x-coordinate of the rotation axis
     * @param y
     *            the y-coordinate of the rotation axis
     * @param z
     *            the z-coordinate of the rotation axis
     */
    public AxisAngle4d(double angle, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = (angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI);
    }

    /**
     * Create a new {@link AxisAngle4d} with the given values.
     *
     * @param angle the angle in radians
     * @param v     the rotation axis as a {@link Vector3dc}
     */
    public AxisAngle4d(double angle, Vector3dc v) {
        this(angle, v.x(), v.y(), v.z());
    }

    /**
     * Create a new {@link AxisAngle4d} with the given values.
     *
     * @param angle the angle in radians
     * @param v     the rotation axis as a {@link Vector3f}
     */
    public AxisAngle4d(double angle, Vector3f v) {
        this(angle, v.x, v.y, v.z);
    }

    /**
     * Set this {@link AxisAngle4d} to the values of <code>a</code>.
     * 
     * @param a
     *            the AngleAxis4f to copy the values from
     * @return this
     */
    public AxisAngle4d set(AxisAngle4d a) {
        x = a.x;
        y = a.y;
        z = a.z;
        angle = (a.angle < 0.0 ? Math.PI + Math.PI + a.angle % (Math.PI + Math.PI) : a.angle) % (Math.PI + Math.PI);
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to the values of <code>a</code>.
     * 
     * @param a
     *            the AngleAxis4f to copy the values from
     * @return this
     */
    public AxisAngle4d set(AxisAngle4f a) {
        x = a.x;
        y = a.y;
        z = a.z;
        angle = (a.angle < 0.0 ? Math.PI + Math.PI + a.angle % (Math.PI + Math.PI) : a.angle) % (Math.PI + Math.PI);
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to the given values.
     * 
     * @param angle
     *            the angle in radians
     * @param x
     *            the x-coordinate of the rotation axis
     * @param y
     *            the y-coordinate of the rotation axis
     * @param z
     *            the z-coordinate of the rotation axis
     * @return this
     */
    public AxisAngle4d set(double angle, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = (angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI);
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to the given values.
     *
     * @param angle
     *            the angle in radians
     * @param v    
     *            the rotation axis as a {@link Vector3dc}
     * @return this
     */
    public AxisAngle4d set(double angle, Vector3dc v) {
        return set(angle, v.x(), v.y(), v.z());
    }

    /**
     * Set this {@link AxisAngle4d} to the given values.
     *
     * @param angle
     *            the angle in radians
     * @param v    
     *            the rotation axis as a {@link Vector3f}
     * @return this
     */
    public AxisAngle4d set(double angle, Vector3f v) {
        return set(angle, v.x, v.y, v.z);
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the given
     * {@link Quaternionfc}.
     * 
     * @param q
     *            the quaternion to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Quaternionfc q) {
        float acos = Math.safeAcos(q.w());
        float invSqrt = Math.invsqrt(1.0f - q.w() * q.w());
        if (Float.isInfinite(invSqrt)) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 1.0;
        } else {
            this.x = q.x() * invSqrt;
            this.y = q.y() * invSqrt;
            this.z = q.z() * invSqrt;
        }
        this.angle = acos + acos;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the given
     * {@link Quaterniondc}.
     * 
     * @param q
     *            the quaternion to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Quaterniondc q) {
        double acos = Math.safeAcos(q.w());
        double invSqrt = Math.invsqrt(1.0f - q.w() * q.w());
        if (Double.isInfinite(invSqrt)) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 1.0;
        } else {
            this.x = q.x() * invSqrt;
            this.y = q.y() * invSqrt;
            this.z = q.z() * invSqrt;
        }
        this.angle = acos + acos;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the rotation 
     * of the given {@link Matrix3fc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/">http://www.euclideanspace.com</a>
     * 
     * @param m
     *            the Matrix3fc to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Matrix3fc m) {
        double nm00 = m.m00(), nm01 = m.m01(), nm02 = m.m02();
        double nm10 = m.m10(), nm11 = m.m11(), nm12 = m.m12();
        double nm20 = m.m20(), nm21 = m.m21(), nm22 = m.m22();
        double lenX = Math.invsqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double lenY = Math.invsqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double lenZ = Math.invsqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        double epsilon = 1E-4, epsilon2 = 1E-3;
        if (Math.abs(nm10 - nm01) < epsilon && Math.abs(nm20 - nm02) < epsilon && Math.abs(nm21 - nm12) < epsilon) {
            if (Math.abs(nm10 + nm01) < epsilon2 && Math.abs(nm20 + nm02) < epsilon2 && Math.abs(nm21 + nm12) < epsilon2
                    && Math.abs(nm00 + nm11 + nm22 - 3) < epsilon2) {
                x = 0;
                y = 0;
                z = 1;
                angle = 0;
                return this;
            }
            angle = Math.PI;
            double xx = (nm00 + 1) / 2;
            double yy = (nm11 + 1) / 2;
            double zz = (nm22 + 1) / 2;
            double xy = (nm10 + nm01) / 4;
            double xz = (nm20 + nm02) / 4;
            double yz = (nm21 + nm12) / 4;
            if ((xx > yy) && (xx > zz)) {
                x = Math.sqrt(xx);
                y = xy / x;
                z = xz / x;
            } else if (yy > zz) {
                y = Math.sqrt(yy);
                x = xy / y;
                z = yz / y;
            } else {
                z = Math.sqrt(zz);
                x = xz / z;
                y = yz / z;
            }
            return this;
        }
        double s = Math.sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10));
        angle = Math.safeAcos((nm00 + nm11 + nm22 - 1) / 2);
        x = (nm12 - nm21) / s;
        y = (nm20 - nm02) / s;
        z = (nm01 - nm10) / s;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the rotation 
     * of the given {@link Matrix3dc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/">http://www.euclideanspace.com</a>
     * 
     * @param m
     *            the Matrix3dc to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Matrix3dc m) {
        double nm00 = m.m00(), nm01 = m.m01(), nm02 = m.m02();
        double nm10 = m.m10(), nm11 = m.m11(), nm12 = m.m12();
        double nm20 = m.m20(), nm21 = m.m21(), nm22 = m.m22();
        double lenX = Math.invsqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double lenY = Math.invsqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double lenZ = Math.invsqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        double epsilon = 1E-4, epsilon2 = 1E-3;
        if (Math.abs(nm10 - nm01) < epsilon && Math.abs(nm20 - nm02) < epsilon && Math.abs(nm21 - nm12) < epsilon) {
            if (Math.abs(nm10 + nm01) < epsilon2 && Math.abs(nm20 + nm02) < epsilon2 && Math.abs(nm21 + nm12) < epsilon2
                    && Math.abs(nm00 + nm11 + nm22 - 3) < epsilon2) {
                x = 0;
                y = 0;
                z = 1;
                angle = 0;
                return this;
            }
            angle = Math.PI;
            double xx = (nm00 + 1) / 2;
            double yy = (nm11 + 1) / 2;
            double zz = (nm22 + 1) / 2;
            double xy = (nm10 + nm01) / 4;
            double xz = (nm20 + nm02) / 4;
            double yz = (nm21 + nm12) / 4;
            if ((xx > yy) && (xx > zz)) {
                x = Math.sqrt(xx);
                y = xy / x;
                z = xz / x;
            } else if (yy > zz) {
                y = Math.sqrt(yy);
                x = xy / y;
                z = yz / y;
            } else {
                z = Math.sqrt(zz);
                x = xz / z;
                y = yz / z;
            }
            return this;
        }
        double s = Math.sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10));
        angle = Math.safeAcos((nm00 + nm11 + nm22 - 1) / 2);
        x = (nm12 - nm21) / s;
        y = (nm20 - nm02) / s;
        z = (nm01 - nm10) / s;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the rotational component 
     * of the given {@link Matrix4fc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/">http://www.euclideanspace.com</a>
     * 
     * @param m
     *            the Matrix4fc to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Matrix4fc m) {
        double nm00 = m.m00(), nm01 = m.m01(), nm02 = m.m02();
        double nm10 = m.m10(), nm11 = m.m11(), nm12 = m.m12();
        double nm20 = m.m20(), nm21 = m.m21(), nm22 = m.m22();
        double lenX = Math.invsqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double lenY = Math.invsqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double lenZ = Math.invsqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        double epsilon = 1E-4, epsilon2 = 1E-3;
        if (Math.abs(nm10 - nm01) < epsilon && Math.abs(nm20 - nm02) < epsilon && Math.abs(nm21 - nm12) < epsilon) {
            if (Math.abs(nm10 + nm01) < epsilon2 && Math.abs(nm20 + nm02) < epsilon2 && Math.abs(nm21 + nm12) < epsilon2
                    && Math.abs(nm00 + nm11 + nm22 - 3) < epsilon2) {
                x = 0;
                y = 0;
                z = 1;
                angle = 0;
                return this;
            }
            angle = Math.PI;
            double xx = (nm00 + 1) / 2;
            double yy = (nm11 + 1) / 2;
            double zz = (nm22 + 1) / 2;
            double xy = (nm10 + nm01) / 4;
            double xz = (nm20 + nm02) / 4;
            double yz = (nm21 + nm12) / 4;
            if ((xx > yy) && (xx > zz)) {
                x = Math.sqrt(xx);
                y = xy / x;
                z = xz / x;
            } else if (yy > zz) {
                y = Math.sqrt(yy);
                x = xy / y;
                z = yz / y;
            } else {
                z = Math.sqrt(zz);
                x = xz / z;
                y = yz / z;
            }
            return this;
        }
        double s = Math.sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10));
        angle = Math.safeAcos((nm00 + nm11 + nm22 - 1) / 2);
        x = (nm12 - nm21) / s;
        y = (nm20 - nm02) / s;
        z = (nm01 - nm10) / s;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the rotational component 
     * of the given {@link Matrix4x3fc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/">http://www.euclideanspace.com</a>
     * 
     * @param m
     *            the Matrix4x3fc to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Matrix4x3fc m) {
        double nm00 = m.m00(), nm01 = m.m01(), nm02 = m.m02();
        double nm10 = m.m10(), nm11 = m.m11(), nm12 = m.m12();
        double nm20 = m.m20(), nm21 = m.m21(), nm22 = m.m22();
        double lenX = Math.invsqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double lenY = Math.invsqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double lenZ = Math.invsqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        double epsilon = 1E-4, epsilon2 = 1E-3;
        if (Math.abs(nm10 - nm01) < epsilon && Math.abs(nm20 - nm02) < epsilon && Math.abs(nm21 - nm12) < epsilon) {
            if (Math.abs(nm10 + nm01) < epsilon2 && Math.abs(nm20 + nm02) < epsilon2 && Math.abs(nm21 + nm12) < epsilon2
                    && Math.abs(nm00 + nm11 + nm22 - 3) < epsilon2) {
                x = 0;
                y = 0;
                z = 1;
                angle = 0;
                return this;
            }
            angle = Math.PI;
            double xx = (nm00 + 1) / 2;
            double yy = (nm11 + 1) / 2;
            double zz = (nm22 + 1) / 2;
            double xy = (nm10 + nm01) / 4;
            double xz = (nm20 + nm02) / 4;
            double yz = (nm21 + nm12) / 4;
            if ((xx > yy) && (xx > zz)) {
                x = Math.sqrt(xx);
                y = xy / x;
                z = xz / x;
            } else if (yy > zz) {
                y = Math.sqrt(yy);
                x = xy / y;
                z = yz / y;
            } else {
                z = Math.sqrt(zz);
                x = xz / z;
                y = yz / z;
            }
            return this;
        }
        double s = Math.sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10));
        angle = Math.safeAcos((nm00 + nm11 + nm22 - 1) / 2);
        x = (nm12 - nm21) / s;
        y = (nm20 - nm02) / s;
        z = (nm01 - nm10) / s;
        return this;
    }

    /**
     * Set this {@link AxisAngle4d} to be equivalent to the rotational component 
     * of the given {@link Matrix4dc}.
     * <p>
     * Reference: <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/">http://www.euclideanspace.com</a>
     * 
     * @param m
     *            the Matrix4dc to set this AngleAxis4d from
     * @return this
     */
    public AxisAngle4d set(Matrix4dc m) {
        double nm00 = m.m00(), nm01 = m.m01(), nm02 = m.m02();
        double nm10 = m.m10(), nm11 = m.m11(), nm12 = m.m12();
        double nm20 = m.m20(), nm21 = m.m21(), nm22 = m.m22();
        double lenX = Math.invsqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double lenY = Math.invsqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double lenZ = Math.invsqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
        nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
        nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
        double epsilon = 1E-4, epsilon2 = 1E-3;
        if (Math.abs(nm10 - nm01) < epsilon && Math.abs(nm20 - nm02) < epsilon && Math.abs(nm21 - nm12) < epsilon) {
            if (Math.abs(nm10 + nm01) < epsilon2 && Math.abs(nm20 + nm02) < epsilon2 && Math.abs(nm21 + nm12) < epsilon2
                    && Math.abs(nm00 + nm11 + nm22 - 3) < epsilon2) {
                x = 0;
                y = 0;
                z = 1;
                angle = 0;
                return this;
            }
            angle = Math.PI;
            double xx = (nm00 + 1) / 2;
            double yy = (nm11 + 1) / 2;
            double zz = (nm22 + 1) / 2;
            double xy = (nm10 + nm01) / 4;
            double xz = (nm20 + nm02) / 4;
            double yz = (nm21 + nm12) / 4;
            if ((xx > yy) && (xx > zz)) {
                x = Math.sqrt(xx);
                y = xy / x;
                z = xz / x;
            } else if (yy > zz) {
                y = Math.sqrt(yy);
                x = xy / y;
                z = yz / y;
            } else {
                z = Math.sqrt(zz);
                x = xz / z;
                y = yz / z;
            }
            return this;
        }
        double s = Math.sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10));
        angle = Math.safeAcos((nm00 + nm11 + nm22 - 1) / 2);
        x = (nm12 - nm21) / s;
        y = (nm20 - nm02) / s;
        z = (nm01 - nm10) / s;
        return this;
    }

    /**
     * Set the given {@link Quaternionf} to be equivalent to this {@link AxisAngle4d} rotation.
     * 
     * @see Quaternionf#set(AxisAngle4d)
     * 
     * @param q
     *          the quaternion to set
     * @return q
     */
    public Quaternionf get(Quaternionf q) {
        return q.set(this);
    }

    /**
     * Set the given {@link Quaterniond} to be equivalent to this {@link AxisAngle4d} rotation.
     * 
     * @see Quaterniond#set(AxisAngle4d)
     * 
     * @param q
     *          the quaternion to set
     * @return q
     */
    public Quaterniond get(Quaterniond q) {
        return q.set(this);
    }

    /**
     * Set the given {@link Matrix4f} to a rotation transformation equivalent to this {@link AxisAngle4d}.
     * 
     * @see Matrix4f#set(AxisAngle4d)
     * 
     * @param m
     *          the matrix to set
     * @return m
     */
    public Matrix4f get(Matrix4f m) {
        return m.set(this);
    }

    /**
     * Set the given {@link Matrix3f} to a rotation transformation equivalent to this {@link AxisAngle4d}.
     * 
     * @see Matrix3f#set(AxisAngle4d)
     * 
     * @param m
     *          the matrix to set
     * @return m
     */
    public Matrix3f get(Matrix3f m) {
        return m.set(this);
    }

    /**
     * Set the given {@link Matrix4d} to a rotation transformation equivalent to this {@link AxisAngle4d}.
     * 
     * @see Matrix4f#set(AxisAngle4d)
     * 
     * @param m
     *          the matrix to set
     * @return m
     */
    public Matrix4d get(Matrix4d m) {
        return m.set(this);
    }

    /**
     * Set the given {@link Matrix3d} to a rotation transformation equivalent to this {@link AxisAngle4d}.
     * 
     * @see Matrix3f#set(AxisAngle4d)
     * 
     * @param m
     *          the matrix to set
     * @return m
     */
    public Matrix3d get(Matrix3d m) {
        return m.set(this);
    }

    /**
     * Set the given {@link AxisAngle4d} to this {@link AxisAngle4d}.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    public AxisAngle4d get(AxisAngle4d dest) {
        return dest.set(this);
    }

    /**
     * Set the given {@link AxisAngle4f} to this {@link AxisAngle4d}.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    public AxisAngle4f get(AxisAngle4f dest) {
        return dest.set(this);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(angle);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        angle = in.readDouble();
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
    }

    /**
     * Normalize the axis vector.
     * 
     * @return this
     */
    public AxisAngle4d normalize() {
        double invLength = Math.invsqrt(x * x + y * y + z * z);
        x *= invLength;
        y *= invLength;
        z *= invLength;
        return this;
    }

    /**
     * Increase the rotation angle by the given amount.
     * <p>
     * This method also takes care of wrapping around.
     * 
     * @param ang
     *          the angle increase
     * @return this
     */
    public AxisAngle4d rotate(double ang) {
        angle += ang;
        angle = (angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI);
        return this;
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    public Vector3d transform(Vector3d v) {
        return transform(v, v);
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}
     * and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector3d transform(Vector3dc v, Vector3d dest) {
        double sin = Math.sin(angle);
        double cos = Math.cosFromSin(sin, angle);
        double dot = x * v.x() + y * v.y() + z * v.z();
        dest.set(v.x() * cos + sin * (y * v.z() - z * v.y()) + (1.0 - cos) * dot * x,
                 v.y() * cos + sin * (z * v.x() - x * v.z()) + (1.0 - cos) * dot * y,
                 v.z() * cos + sin * (x * v.y() - y * v.x()) + (1.0 - cos) * dot * z);
        return dest;
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    public Vector3f transform(Vector3f v) {
        return transform(v, v);
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}
     * and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector3f transform(Vector3fc v, Vector3f dest) {
        double sin = Math.sin(angle);
        double cos = Math.cosFromSin(sin, angle);
        double dot = x * v.x() + y * v.y() + z * v.z();
        dest.set((float) (v.x() * cos + sin * (y * v.z() - z * v.y()) + (1.0 - cos) * dot * x),
                 (float) (v.y() * cos + sin * (z * v.x() - x * v.z()) + (1.0 - cos) * dot * y),
                 (float) (v.z() * cos + sin * (x * v.y() - y * v.x()) + (1.0 - cos) * dot * z));
        return dest;
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}.
     * 
     * @param v
     *          the vector to transform
     * @return v
     */
    public Vector4d transform(Vector4d v) {
        return transform(v, v);
    }

    /**
     * Transform the given vector by the rotation transformation described by this {@link AxisAngle4d}
     * and store the result in <code>dest</code>.
     * 
     * @param v
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    public Vector4d transform(Vector4dc v, Vector4d dest) {
        double sin = Math.sin(angle);
        double cos = Math.cosFromSin(sin, angle);
        double dot = x * v.x() + y * v.y() + z * v.z();
        dest.set(v.x() * cos + sin * (y * v.z() - z * v.y()) + (1.0 - cos) * dot * x,
                 v.y() * cos + sin * (z * v.x() - x * v.z()) + (1.0 - cos) * dot * y,
                 v.z() * cos + sin * (x * v.y() - y * v.x()) + (1.0 - cos) * dot * z,
                 dest.w);
        return dest;
    }

    /**
     * Return a string representation of this {@link AxisAngle4d}.
     * <p>
     * This method creates a new {@link DecimalFormat} on every invocation with the format string "<code> 0.000E0;-</code>".
     * 
     * @return the string representation
     */
    public String toString() {
        return Runtime.formatNumbers(toString(Options.NUMBER_FORMAT));
    }

    /**
     * Return a string representation of this {@link AxisAngle4d} by formatting the components with the given {@link NumberFormat}.
     * 
     * @param formatter
     *          the {@link NumberFormat} used to format the vector components with
     * @return the string representation
     */
    public String toString(NumberFormat formatter) {
        return "(" + Runtime.format(x, formatter) + " " + Runtime.format(y, formatter) + " " + Runtime.format(z, formatter) + " <| " + Runtime.format(angle, formatter) + ")";
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits((angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI));
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
        AxisAngle4d other = (AxisAngle4d) obj;
        if (Double.doubleToLongBits((angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI)) != 
                Double.doubleToLongBits((other.angle < 0.0 ? Math.PI + Math.PI + other.angle % (Math.PI + Math.PI) : other.angle) % (Math.PI + Math.PI)))
            return false;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
