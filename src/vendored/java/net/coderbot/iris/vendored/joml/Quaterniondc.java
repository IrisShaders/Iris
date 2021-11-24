/*
 * The MIT License
 *
 * Copyright (c) 2015-2021 JOML
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

import java.util.*;
/**
 * Interface to a read-only view of a quaternion of double-precision floats.
 * 
 * @author Kai Burjack
 */
public interface Quaterniondc {

    /**
     * @return the first component of the vector part
     */
    double x();

    /**
     * @return the second component of the vector part
     */
    double y();

    /**
     * @return the third component of the vector part
     */
    double z();

    /**
     * @return the real/scalar part of the quaternion
     */
    double w();

    /**
     * Normalize this quaternion and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond normalize(Quaterniond dest);

    /**
     * Add the quaternion <code>(x, y, z, w)</code> to this quaternion and store the result in <code>dest</code>.
     * 
     * @param x
     *          the x component of the vector part
     * @param y
     *          the y component of the vector part
     * @param z
     *          the z component of the vector part
     * @param w
     *          the real/scalar component
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond add(double x, double y, double z, double w, Quaterniond dest);

    /**
     * Add <code>q2</code> to this quaternion and store the result in <code>dest</code>.
     * 
     * @param q2
     *          the quaternion to add to this
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond add(Quaterniondc q2, Quaterniond dest);

    /**
     * Return the dot product of this {@link Quaterniond} and <code>otherQuat</code>.
     * 
     * @param otherQuat
     *          the other quaternion
     * @return the dot product
     */
    double dot(Quaterniondc otherQuat);

    /**
     * Return the angle in radians represented by this normalized quaternion rotation.
     * <p>
     * This quaternion must be {@link #normalize(Quaterniond) normalized}.
     * 
     * @return the angle in radians
     */
    double angle();

    /**
     * Set the given destination matrix to the rotation represented by <code>this</code>.
     * 
     * @see Matrix3d#set(Quaterniondc)
     * 
     * @param dest
     *          the matrix to write the rotation into
     * @return the passed in destination
     */
    Matrix3d get(Matrix3d dest);

    /**
     * Set the given destination matrix to the rotation represented by <code>this</code>.
     * 
     * @see Matrix3f#set(Quaterniondc)
     * 
     * @param dest
     *          the matrix to write the rotation into
     * @return the passed in destination
     */
    Matrix3f get(Matrix3f dest);

    /**
     * Set the given destination matrix to the rotation represented by <code>this</code>.
     * 
     * @see Matrix4d#set(Quaterniondc)
     * 
     * @param dest
     *          the matrix to write the rotation into
     * @return the passed in destination
     */
    Matrix4d get(Matrix4d dest);

    /**
     * Set the given destination matrix to the rotation represented by <code>this</code>.
     * 
     * @see Matrix4f#set(Quaterniondc)
     * 
     * @param dest
     *          the matrix to write the rotation into
     * @return the passed in destination
     */
    Matrix4f get(Matrix4f dest);

    /**
     * Set the given {@link AxisAngle4f} to represent the rotation of
     * <code>this</code> quaternion.
     * 
     * @param dest
     *            the {@link AxisAngle4f} to set
     * @return the passed in destination
     */
    AxisAngle4f get(AxisAngle4f dest);

    /**
     * Set the given {@link AxisAngle4d} to represent the rotation of
     * <code>this</code> quaternion.
     * 
     * @param dest
     *            the {@link AxisAngle4d} to set
     * @return the passed in destination
     */
    AxisAngle4d get(AxisAngle4d dest);

    /**
     * Set the given {@link Quaterniond} to the values of <code>this</code>.
     * 
     * @param dest
     *          the {@link Quaterniond} to set
     * @return the passed in destination
     */
    Quaterniond get(Quaterniond dest);

    /**
     * Set the given {@link Quaternionf} to the values of <code>this</code>.
     * 
     * @param dest
     *          the {@link Quaternionf} to set
     * @return the passed in destination
     */
    Quaternionf get(Quaternionf dest);

    /**
     * Multiply this quaternion by <code>q</code> and store the result in <code>dest</code>.
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
     *            the quaternion to multiply <code>this</code> by
     * @param dest
     *            will hold the result
     * @return dest
     */
    Quaterniond mul(Quaterniondc q, Quaterniond dest);

    /**
     * Multiply this quaternion by the quaternion represented via <code>(qx, qy, qz, qw)</code> and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Quaterniond mul(double qx, double qy, double qz, double qw, Quaterniond dest);

    /**
     * Pre-multiply this quaternion by <code>q</code> and store the result in <code>dest</code>.
     * <p>
     * If <code>T</code> is <code>this</code> and <code>Q</code> is the given quaternion, then the resulting quaternion <code>R</code> is:
     * <p>
     * <code>R = Q * T</code>
     * <p>
     * So, this method uses pre-multiplication, resulting in a vector to be transformed by <code>T</code> first, and then by <code>Q</code>.
     * 
     * @param q
     *            the quaternion to pre-multiply <code>this</code> by
     * @param dest
     *            will hold the result
     * @return dest
     */
    Quaterniond premul(Quaterniondc q, Quaterniond dest);

    /**
     * Pre-multiply this quaternion by the quaternion represented via <code>(qx, qy, qz, qw)</code> and store the result in <code>dest</code>.
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
     * @param dest
     *            will hold the result
     * @return dest
     */
    Quaterniond premul(double qx, double qy, double qz, double qw, Quaterniond dest);

    /**
     * Transform the given vector by this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3d transform(Vector3d vec);

    /**
     * Transform the given vector by the inverse of this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3d transformInverse(Vector3d vec);

    /**
     * Transform the given vector by this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3d transformUnit(Vector3d vec);

    /**
     * Transform the given vector by the inverse of this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3d transformInverseUnit(Vector3d vec);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformPositiveX(Vector3d dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformPositiveX(Vector4d dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformUnitPositiveX(Vector3d dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformUnitPositiveX(Vector4d dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformPositiveY(Vector3d dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformPositiveY(Vector4d dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformUnitPositiveY(Vector3d dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformUnitPositiveY(Vector4d dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformPositiveZ(Vector3d dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformPositiveZ(Vector4d dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformUnitPositiveZ(Vector3d dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformUnitPositiveZ(Vector4d dest);

    /**
     * Transform the given vector by this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4d transform(Vector4d vec);

    /**
     * Transform the given vector by the inverse of this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4d transformInverse(Vector4d vec);

    /**
     * Transform the given vector by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transform(Vector3dc vec, Vector3d dest);

    /**
     * Transform the given vector by the inverse of this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformInverse(Vector3dc vec, Vector3d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transform(double x, double y, double z, Vector3d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformInverse(double x, double y, double z, Vector3d dest);

    /**
     * Transform the given vector by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transform(Vector4dc vec, Vector4d dest);

    /**
     * Transform the given vector by the inverse of this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformInverse(Vector4dc vec, Vector4d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transform(double x, double y, double z, Vector4d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformInverse(double x, double y, double z, Vector4d dest);

    /**
     * Transform the given vector by this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3f transform(Vector3f vec);

    /**
     * Transform the given vector by the inverse of this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3f transformInverse(Vector3f vec);

    /**
     * Transform the given vector by this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4d transformUnit(Vector4d vec);

    /**
     * Transform the given vector by the inverse of this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4d transformInverseUnit(Vector4d vec);

    /**
     * Transform the given vector by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformUnit(Vector3dc vec, Vector3d dest);

    /**
     * Transform the given vector by the inverse of this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformInverseUnit(Vector3dc vec, Vector3d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformUnit(double x, double y, double z, Vector3d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3d transformInverseUnit(double x, double y, double z, Vector3d dest);

    /**
     * Transform the given vector by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformUnit(Vector4dc vec, Vector4d dest);

    /**
     * Transform the given vector by the inverse of this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformInverseUnit(Vector4dc vec, Vector4d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformUnit(double x, double y, double z, Vector4d dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4d transformInverseUnit(double x, double y, double z, Vector4d dest);

    /**
     * Transform the given vector by this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3f transformUnit(Vector3f vec);

    /**
     * Transform the given vector by the inverse of this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector3f transformInverseUnit(Vector3f vec);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformPositiveX(Vector3f dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformPositiveX(Vector4f dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformUnitPositiveX(Vector3f dest);

    /**
     * Transform the vector <code>(1, 0, 0)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformUnitPositiveX(Vector4f dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformPositiveY(Vector3f dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformPositiveY(Vector4f dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformUnitPositiveY(Vector3f dest);

    /**
     * Transform the vector <code>(0, 1, 0)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformUnitPositiveY(Vector4f dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this quaternion.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformPositiveZ(Vector3f dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformPositiveZ(Vector4f dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this unit quaternion.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformUnitPositiveZ(Vector3f dest);

    /**
     * Transform the vector <code>(0, 0, 1)</code> by this unit quaternion.
     * <p>
     * Only the first three components of the given 4D vector are modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * <p>
     * Reference: <a href="https://de.mathworks.com/help/aerotbx/ug/quatrotate.html?requestedDomain=true">https://de.mathworks.com/</a>
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformUnitPositiveZ(Vector4f dest);

    /**
     * Transform the given vector by this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4f transform(Vector4f vec);

    /**
     * Transform the given vector by the inverse of this quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4f transformInverse(Vector4f vec);

    /**
     * Transform the given vector by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transform(Vector3fc vec, Vector3f dest);

    /**
     * Transform the given vector by the inverse of this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformInverse(Vector3fc vec, Vector3f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transform(double x, double y, double z, Vector3f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformInverse(double x, double y, double z, Vector3f dest);

    /**
     * Transform the given vector by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transform(Vector4fc vec, Vector4f dest);

    /**
     * Transform the given vector by the inverse of this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformInverse(Vector4fc vec, Vector4f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transform(double x, double y, double z, Vector4f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformInverse(double x, double y, double z, Vector4f dest);

    /**
     * Transform the given vector by this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4f transformUnit(Vector4f vec);

    /**
     * Transform the given vector by the inverse of this unit quaternion.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and modified.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @return vec
     */
    Vector4f transformInverseUnit(Vector4f vec);

    /**
     * Transform the given vector by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformUnit(Vector3fc vec, Vector3f dest);

    /**
     * Transform the given vector by the inverse of this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformInverseUnit(Vector3fc vec, Vector3f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformUnit(double x, double y, double z, Vector3f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector3f transformInverseUnit(double x, double y, double z, Vector3f dest);

    /**
     * Transform the given vector by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformUnit(Vector4fc vec, Vector4f dest);

    /**
     * Transform the given vector by the inverse of this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * Only the first three components of the given 4D vector are being used and set on the destination.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param vec
     *          the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformInverseUnit(Vector4fc vec, Vector4f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformUnit(double x, double y, double z, Vector4f dest);

    /**
     * Transform the given vector <code>(x, y, z)</code> by the inverse of
     * this unit quaternion and store the result in <code>dest</code>.
     * <p>
     * This will apply the rotation described by this quaternion to the given vector.
     * <p>
     * This method is only applicable when <code>this</code> is a unit quaternion.
     * 
     * @param x
     *          the x coordinate of the vector to transform
     * @param y
     *          the y coordinate of the vector to transform
     * @param z
     *          the z coordinate of the vector to transform
     * @param dest
     *          will hold the result
     * @return dest
     */
    Vector4f transformInverseUnit(double x, double y, double z, Vector4f dest);

    /**
     * Invert this quaternion and store the {@link #normalize(Quaterniond) normalized} result in <code>dest</code>.
     * <p>
     * If this quaternion is already normalized, then {@link #conjugate(Quaterniond)} should be used instead.
     * 
     * @see #conjugate(Quaterniond)
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond invert(Quaterniond dest);

    /**
     * Divide <code>this</code> quaternion by <code>b</code> and store the result in <code>dest</code>.
     * <p>
     * The division expressed using the inverse is performed in the following way:
     * <p>
     * <code>dest = this * b^-1</code>, where <code>b^-1</code> is the inverse of <code>b</code>.
     * 
     * @param b
     *          the {@link Quaterniondc} to divide this by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond div(Quaterniondc b, Quaterniond dest);

    /**
     * Conjugate this quaternion and store the result in <code>dest</code>.
     * 
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond conjugate(Quaterniond dest);

    /**
     * Return the square of the length of this quaternion.
     * 
     * @return the length
     */
    double lengthSquared();

    /**
     * Interpolate between <code>this</code> {@link #normalize(Quaterniond) unit} quaternion and the specified
     * <code>target</code> {@link #normalize(Quaterniond) unit} quaternion using spherical linear interpolation using the specified interpolation factor <code>alpha</code>,
     * and store the result in <code>dest</code>.
     * <p>
     * This method resorts to non-spherical linear interpolation when the absolute dot product between <code>this</code> and <code>target</code> is
     * below <code>1E-6</code>.
     * <p>
     * Reference: <a href="http://fabiensanglard.net/doom3_documentation/37725-293747_293747.pdf">http://fabiensanglard.net</a>
     * 
     * @param target
     *          the target of the interpolation, which should be reached with <code>alpha = 1.0</code>
     * @param alpha
     *          the interpolation factor, within <code>[0..1]</code>
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond slerp(Quaterniondc target, double alpha, Quaterniond dest);

    /**
     * Apply scaling to this quaternion, which results in any vector transformed by the quaternion to change
     * its length by the given <code>factor</code>, and store the result in <code>dest</code>.
     * 
     * @param factor
     *          the scaling factor
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond scale(double factor, Quaterniond dest);

    /**
     * Integrate the rotation given by the angular velocity <code>(vx, vy, vz)</code> around the x, y and z axis, respectively,
     * with respect to the given elapsed time delta <code>dt</code> and add the differentiate rotation to the rotation represented by this quaternion
     * and store the result into <code>dest</code>.
     * <p>
     * This method pre-multiplies the rotation given by <code>dt</code> and <code>(vx, vy, vz)</code> by <code>this</code>, so
     * the angular velocities are always relative to the local coordinate system of the rotation represented by <code>this</code> quaternion.
     * <p>
     * This method is equivalent to calling: <code>rotateLocal(dt * vx, dt * vy, dt * vz, dest)</code>
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
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond integrate(double dt, double vx, double vy, double vz, Quaterniond dest);

    /**
     * Compute a linear (non-spherical) interpolation of <code>this</code> and the given quaternion <code>q</code>
     * and store the result in <code>dest</code>.
     * <p>
     * Reference: <a href="http://fabiensanglard.net/doom3_documentation/37725-293747_293747.pdf">http://fabiensanglard.net</a>
     * 
     * @param q
     *          the other quaternion
     * @param factor
     *          the interpolation factor. It is between 0.0 and 1.0
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond nlerp(Quaterniondc q, double factor, Quaterniond dest);

    /**
     * Compute linear (non-spherical) interpolations of <code>this</code> and the given quaternion <code>q</code>
     * iteratively and store the result in <code>dest</code>.
     * <p>
     * This method performs a series of small-step nlerp interpolations to avoid doing a costly spherical linear interpolation, like
     * {@link #slerp(Quaterniondc, double, Quaterniond) slerp},
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
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond nlerpIterative(Quaterniondc q, double alpha, double dotThreshold, Quaterniond dest);

    /**
     * Apply a rotation to this quaternion that maps the given direction to the positive Z axis, and store the result in <code>dest</code>.
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
     * @see #lookAlong(double, double, double, double, double, double, Quaterniond)
     * 
     * @param dir
     *              the direction to map to the positive Z axis
     * @param up
     *              the vector which will be mapped to a vector parallel to the plane 
     *              spanned by the given <code>dir</code> and <code>up</code>
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond lookAlong(Vector3dc dir, Vector3dc up, Quaterniond dest);

    /**
     * Apply a rotation to this quaternion that maps the given direction to the positive Z axis, and store the result in <code>dest</code>.
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
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond lookAlong(double dirX, double dirY, double dirZ, double upX, double upY, double upZ, Quaterniond dest);

    /**
     * Compute the difference between <code>this</code> and the <code>other</code> quaternion
     * and store the result in <code>dest</code>.
     * <p>
     * The difference is the rotation that has to be applied to get from
     * <code>this</code> rotation to <code>other</code>. If <code>T</code> is <code>this</code>, <code>Q</code>
     * is <code>other</code> and <code>D</code> is the computed difference, then the following equation holds:
     * <p>
     * <code>T * D = Q</code>
     * <p>
     * It is defined as: <code>D = T^-1 * Q</code>, where <code>T^-1</code> denotes the {@link #invert(Quaterniond) inverse} of <code>T</code>.
     * 
     * @param other
     *          the other quaternion
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond difference(Quaterniondc other, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> that rotates the <code>fromDir</code> vector to point along <code>toDir</code> and
     * store the result in <code>dest</code>.
     * <p>
     * Since there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
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
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond rotateTo(double fromDirX, double fromDirY, double fromDirZ, double toDirX, double toDirY, double toDirZ, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> that rotates the <code>fromDir</code> vector to point along <code>toDir</code> and
     * store the result in <code>dest</code>.
     * <p>
     * Because there can be multiple possible rotations, this method chooses the one with the shortest arc.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateTo(double, double, double, double, double, double, Quaterniond)
     * 
     * @param fromDir
     *          the starting direction
     * @param toDir
     *          the destination direction
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond rotateTo(Vector3dc fromDir, Vector3dc toDir, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the x axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the x axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateX(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the y axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the y axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateY(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the z axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the z axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateZ(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local x axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local x axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateLocalX(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local y axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local y axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateLocalY(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the local z axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>R * Q</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>R * Q * v</code>, the
     * rotation represented by <code>this</code> will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the local z axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateLocalZ(double angle, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles using rotation sequence <code>XYZ</code> and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateX(angleX, dest).rotateY(angleY).rotateZ(angleZ)</code>
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
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateXYZ(double angleX, double angleY, double angleZ, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles, using the rotation sequence <code>ZYX</code> and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateZ(angleZ, dest).rotateY(angleY).rotateX(angleX)</code>
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
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateZYX(double angleZ, double angleY, double angleX, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the cartesian base unit axes,
     * called the euler angles, using the rotation sequence <code>YXZ</code> and store the result in <code>dest</code>.
     * <p>
     * This method is equivalent to calling: <code>rotateY(angleY, dest).rotateX(angleX).rotateZ(angleZ)</code>
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
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateYXZ(double angleY, double angleX, double angleZ, Quaterniond dest);

    /**
     * Get the euler angles in radians in rotation sequence <code>XYZ</code> of this quaternion and store them in the 
     * provided parameter <code>eulerAngles</code>.
     * 
     * @param eulerAngles
     *          will hold the euler angles in radians
     * @return the passed in vector
     */
    Vector3d getEulerAnglesXYZ(Vector3d eulerAngles);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the specified axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @param angle
     *              the angle in radians to rotate about the specified axis
     * @param axisX
     *              the x coordinate of the rotation axis
     * @param axisY
     *              the y coordinate of the rotation axis
     * @param axisZ
     *              the z coordinate of the rotation axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateAxis(double angle, double axisX, double axisY, double axisZ, Quaterniond dest);

    /**
     * Apply a rotation to <code>this</code> quaternion rotating the given radians about the specified axis
     * and store the result in <code>dest</code>.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion representing the 
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * 
     * @see #rotateAxis(double, double, double, double, Quaterniond)
     * 
     * @param angle
     *              the angle in radians to rotate about the specified axis
     * @param axis
     *              the rotation axis
     * @param dest
     *              will hold the result
     * @return dest
     */
    Quaterniond rotateAxis(double angle, Vector3dc axis, Quaterniond dest);

    /**
     * Obtain the direction of <code>+X</code> before the rotation transformation represented by <code>this</code> quaternion is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).invert();
     * inv.transform(dir.set(1, 0, 0));
     * </pre>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3d positiveX(Vector3d dir);

    /**
     * Obtain the direction of <code>+X</code> before the rotation transformation represented by <code>this</code> <i>normalized</i> quaternion is applied.
     * The quaternion <i>must</i> be {@link #normalize(Quaterniond) normalized} for this method to work.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).conjugate();
     * inv.transform(dir.set(1, 0, 0));
     * </pre>
     * 
     * @param dir
     *          will hold the direction of <code>+X</code>
     * @return dir
     */
    Vector3d normalizedPositiveX(Vector3d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the rotation transformation represented by <code>this</code> quaternion is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).invert();
     * inv.transform(dir.set(0, 1, 0));
     * </pre>
     * 
     * @param dir
     *            will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3d positiveY(Vector3d dir);

    /**
     * Obtain the direction of <code>+Y</code> before the rotation transformation represented by <code>this</code> <i>normalized</i> quaternion is applied.
     * The quaternion <i>must</i> be {@link #normalize(Quaterniond) normalized} for this method to work.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).conjugate();
     * inv.transform(dir.set(0, 1, 0));
     * </pre>
     * 
     * @param dir
     *            will hold the direction of <code>+Y</code>
     * @return dir
     */
    Vector3d normalizedPositiveY(Vector3d dir);

    /**
     * Obtain the direction of <code>+Z</code> before the rotation transformation represented by <code>this</code> quaternion is applied.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).invert();
     * inv.transform(dir.set(0, 0, 1));
     * </pre>
     * 
     * @param dir
     *            will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3d positiveZ(Vector3d dir);

    /**
     * Obtain the direction of <code>+Z</code> before the rotation transformation represented by <code>this</code> <i>normalized</i> quaternion is applied.
     * The quaternion <i>must</i> be {@link #normalize(Quaterniond) normalized} for this method to work.
     * <p>
     * This method is equivalent to the following code:
     * <pre>
     * Quaterniond inv = new Quaterniond(this).conjugate();
     * inv.transform(dir.set(0, 0, 1));
     * </pre>
     * 
     * @param dir
     *            will hold the direction of <code>+Z</code>
     * @return dir
     */
    Vector3d normalizedPositiveZ(Vector3d dir);

    /**
     * Conjugate <code>this</code> by the given quaternion <code>q</code> by computing <code>q * this * q^-1</code>
     * and store the result into <code>dest</code>.
     * 
     * @param q
     *          the {@link Quaterniondc} to conjugate <code>this</code> by
     * @param dest
     *          will hold the result
     * @return dest
     */
    Quaterniond conjugateBy(Quaterniondc q, Quaterniond dest);

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
     Compare the quaternion components of <code>this</code> quaternion with the given quaternion using the given <code>delta</code>
     * and return whether all of them are equal within a maximum difference of <code>delta</code>.
     * <p>
     * Please note that this method is not used by any data structure such as {@link ArrayList} {@link HashSet} or {@link HashMap}
     * and their operations, such as {@link ArrayList#contains(Object)} or {@link HashSet#remove(Object)}, since those
     * data structures only use the {@link Object#equals(Object)} and {@link Object#hashCode()} methods.
     *
     * @param q
     *       the other quaternion
     * @param delta
     *      the allowed maximum difference
     * @return <code>true</code> whether all of the quaternion components are equal; <code>false</code> otherwise
     */
    boolean equals(Quaterniondc q, double delta);

    /**
     *
     * @param x
     *      the x component to compare to
     * @param y
     *      the y component to compare to
     * @param z
     *      the z component to compare to
     * @param w
     *      the w component to compare to
     * @return <code>true</code> if all the quaternion components are equal
     */
    boolean equals(double x, double y, double z, double w);

}
