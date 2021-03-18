/*
 * The MIT License
 *
 * Copyright (c) 2016-2021 Kai Burjack
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

//#ifndef __GWT__
import java.lang.reflect.Field;
//#endif
//#ifdef __HAS_NIO__
import java.nio.*;
//#endif

/**
 * Helper class to do efficient memory operations on all JOML objects, NIO buffers and primitive arrays.
 * This class is used internally throughout JOML, is undocumented and is subject to change.
 * Use with extreme caution!
 * 
 * @author The LWJGL authors
 * @author Kai Burjack
 */
abstract class MemUtil {
    public static final MemUtil INSTANCE = createInstance();
    private static MemUtil createInstance() {
        MemUtil accessor;
//#ifdef __NHAS_UNSAFE__
        accessor = new MemUtilNIO();
//#else
        try {
            if (Options.NO_UNSAFE && Options.FORCE_UNSAFE)
                throw new ConfigurationException("Cannot enable both -Djoml.nounsafe and -Djoml.forceUnsafe", null);
            else if (Options.NO_UNSAFE)
                accessor = new MemUtilNIO();
            else
                accessor = new MemUtilUnsafe();
        } catch (Throwable e) {
            if (Options.FORCE_UNSAFE)
                throw new ConfigurationException("Unsafe is not supported but its use was forced via -Djoml.forceUnsafe", e);
            accessor = new MemUtilNIO();
        }
//#endif
        return accessor;
    }

//#ifdef __HAS_NIO__
    public abstract void put(Matrix4f m, int offset, FloatBuffer dest);
    public abstract void put(Matrix4f m, int offset, ByteBuffer dest);
    public abstract void put(Matrix4x3f m, int offset, FloatBuffer dest);
    public abstract void put(Matrix4x3f m, int offset, ByteBuffer dest);
    public abstract void put4x4(Matrix4x3f m, int offset, FloatBuffer dest);
    public abstract void put4x4(Matrix4x3f m, int offset, ByteBuffer dest);
    public abstract void put4x4(Matrix4x3d m, int offset, DoubleBuffer dest);
    public abstract void put4x4(Matrix4x3d m, int offset, ByteBuffer dest);
    public abstract void put4x4(Matrix3x2f m, int offset, FloatBuffer dest);
    public abstract void put4x4(Matrix3x2f m, int offset, ByteBuffer dest);
    public abstract void put4x4(Matrix3x2d m, int offset, DoubleBuffer dest);
    public abstract void put4x4(Matrix3x2d m, int offset, ByteBuffer dest);
    public abstract void put3x3(Matrix3x2f m, int offset, FloatBuffer dest);
    public abstract void put3x3(Matrix3x2f m, int offset, ByteBuffer dest);
    public abstract void put3x3(Matrix3x2d m, int offset, DoubleBuffer dest);
    public abstract void put3x3(Matrix3x2d m, int offset, ByteBuffer dest);
    public abstract void put4x3(Matrix4f m, int offset, FloatBuffer dest);
    public abstract void put4x3(Matrix4f m, int offset, ByteBuffer dest);
    public abstract void put3x4(Matrix4f m, int offset, FloatBuffer dest);
    public abstract void put3x4(Matrix4f m, int offset, ByteBuffer dest);
    public abstract void put3x4(Matrix4x3f m, int offset, FloatBuffer dest);
    public abstract void put3x4(Matrix4x3f m, int offset, ByteBuffer dest);
    public abstract void put3x4(Matrix3f m, int offset, FloatBuffer dest);
    public abstract void put3x4(Matrix3f m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix4f m, int offset, FloatBuffer dest);
    public abstract void putTransposed(Matrix4f m, int offset, ByteBuffer dest);
    public abstract void put4x3Transposed(Matrix4f m, int offset, FloatBuffer dest);
    public abstract void put4x3Transposed(Matrix4f m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix4x3f m, int offset, FloatBuffer dest);
    public abstract void putTransposed(Matrix4x3f m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix3f m, int offset, FloatBuffer dest);
    public abstract void putTransposed(Matrix3f m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix2f m, int offset, FloatBuffer dest);
    public abstract void putTransposed(Matrix2f m, int offset, ByteBuffer dest);
    public abstract void put(Matrix4d m, int offset, DoubleBuffer dest);
    public abstract void put(Matrix4d m, int offset, ByteBuffer dest);
    public abstract void put(Matrix4x3d m, int offset, DoubleBuffer dest);
    public abstract void put(Matrix4x3d m, int offset, ByteBuffer dest);
    public abstract void putf(Matrix4d m, int offset, FloatBuffer dest);
    public abstract void putf(Matrix4d m, int offset, ByteBuffer dest);
    public abstract void putf(Matrix4x3d m, int offset, FloatBuffer dest);
    public abstract void putf(Matrix4x3d m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix4d m, int offset, DoubleBuffer dest);
    public abstract void putTransposed(Matrix4d m, int offset, ByteBuffer dest);
    public abstract void put4x3Transposed(Matrix4d m, int offset, DoubleBuffer dest);
    public abstract void put4x3Transposed(Matrix4d m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix4x3d m, int offset, DoubleBuffer dest);
    public abstract void putTransposed(Matrix4x3d m, int offset, ByteBuffer dest);
    public abstract void putTransposed(Matrix2d m, int offset, DoubleBuffer dest);
    public abstract void putTransposed(Matrix2d m, int offset, ByteBuffer dest);
    public abstract void putfTransposed(Matrix4d m, int offset, FloatBuffer dest);
    public abstract void putfTransposed(Matrix4d m, int offset, ByteBuffer dest);
    public abstract void putfTransposed(Matrix4x3d m, int offset, FloatBuffer dest);
    public abstract void putfTransposed(Matrix4x3d m, int offset, ByteBuffer dest);
    public abstract void putfTransposed(Matrix2d m, int offset, FloatBuffer dest);
    public abstract void putfTransposed(Matrix2d m, int offset, ByteBuffer dest);
    public abstract void put(Matrix3f m, int offset, FloatBuffer dest);
    public abstract void put(Matrix3f m, int offset, ByteBuffer dest);
    public abstract void put(Matrix3d m, int offset, DoubleBuffer dest);
    public abstract void put(Matrix3d m, int offset, ByteBuffer dest);
    public abstract void putf(Matrix3d m, int offset, FloatBuffer dest);
    public abstract void putf(Matrix3d m, int offset, ByteBuffer dest);
    public abstract void put(Matrix3x2f m, int offset, FloatBuffer dest);
    public abstract void put(Matrix3x2f m, int offset, ByteBuffer dest);
    public abstract void put(Matrix3x2d m, int offset, DoubleBuffer dest);
    public abstract void put(Matrix3x2d m, int offset, ByteBuffer dest);
    public abstract void put(Matrix2f m, int offset, FloatBuffer dest);
    public abstract void put(Matrix2f m, int offset, ByteBuffer dest);
    public abstract void put(Matrix2d m, int offset, DoubleBuffer dest);
    public abstract void put(Matrix2d m, int offset, ByteBuffer dest);
    public abstract void putf(Matrix2d m, int offset, FloatBuffer dest);
    public abstract void putf(Matrix2d m, int offset, ByteBuffer dest);
    public abstract void put(Vector4d src, int offset, DoubleBuffer dest);
    public abstract void put(Vector4d src, int offset, FloatBuffer dest);
    public abstract void put(Vector4d src, int offset, ByteBuffer dest);
    public abstract void putf(Vector4d src, int offset, ByteBuffer dest);
    public abstract void put(Vector4f src, int offset, FloatBuffer dest);
    public abstract void put(Vector4f src, int offset, ByteBuffer dest);
    public abstract void put(Vector4i src, int offset, IntBuffer dest);
    public abstract void put(Vector4i src, int offset, ByteBuffer dest);
    public abstract void put(Vector3f src, int offset, FloatBuffer dest);
    public abstract void put(Vector3f src, int offset, ByteBuffer dest);
    public abstract void put(Vector3d src, int offset, DoubleBuffer dest);
    public abstract void put(Vector3d src, int offset, FloatBuffer dest);
    public abstract void put(Vector3d src, int offset, ByteBuffer dest);
    public abstract void putf(Vector3d src, int offset, ByteBuffer dest);
    public abstract void put(Vector3i src, int offset, IntBuffer dest);
    public abstract void put(Vector3i src, int offset, ByteBuffer dest);
    public abstract void put(Vector2f src, int offset, FloatBuffer dest);
    public abstract void put(Vector2f src, int offset, ByteBuffer dest);
    public abstract void put(Vector2d src, int offset, DoubleBuffer dest);
    public abstract void put(Vector2d src, int offset, ByteBuffer dest);
    public abstract void put(Vector2i src, int offset, IntBuffer dest);
    public abstract void put(Vector2i src, int offset, ByteBuffer dest);
    public abstract void get(Matrix4f m, int offset, FloatBuffer src);
    public abstract void get(Matrix4f m, int offset, ByteBuffer src);
    public abstract void getTransposed(Matrix4f m, int offset, FloatBuffer src);
    public abstract void getTransposed(Matrix4f m, int offset, ByteBuffer src);
    public abstract void get(Matrix4x3f m, int offset, FloatBuffer src);
    public abstract void get(Matrix4x3f m, int offset, ByteBuffer src);
    public abstract void get(Matrix4d m, int offset, DoubleBuffer src);
    public abstract void get(Matrix4d m, int offset, ByteBuffer src);
    public abstract void get(Matrix4x3d m, int offset, DoubleBuffer src);
    public abstract void get(Matrix4x3d m, int offset, ByteBuffer src);
    public abstract void getf(Matrix4d m, int offset, FloatBuffer src);
    public abstract void getf(Matrix4d m, int offset, ByteBuffer src);
    public abstract void getf(Matrix4x3d m, int offset, FloatBuffer src);
    public abstract void getf(Matrix4x3d m, int offset, ByteBuffer src);
    public abstract void get(Matrix3f m, int offset, FloatBuffer src);
    public abstract void get(Matrix3f m, int offset, ByteBuffer src);
    public abstract void get(Matrix3d m, int offset, DoubleBuffer src);
    public abstract void get(Matrix3d m, int offset, ByteBuffer src);
    public abstract void get(Matrix3x2f m, int offset, FloatBuffer src);
    public abstract void get(Matrix3x2f m, int offset, ByteBuffer src);
    public abstract void get(Matrix3x2d m, int offset, DoubleBuffer src);
    public abstract void get(Matrix3x2d m, int offset, ByteBuffer src);
    public abstract void getf(Matrix3d m, int offset, FloatBuffer src);
    public abstract void getf(Matrix3d m, int offset, ByteBuffer src);
    public abstract void get(Matrix2f m, int offset, FloatBuffer src);
    public abstract void get(Matrix2f m, int offset, ByteBuffer src);
    public abstract void get(Matrix2d m, int offset, DoubleBuffer src);
    public abstract void get(Matrix2d m, int offset, ByteBuffer src);
    public abstract void getf(Matrix2d m, int offset, FloatBuffer src);
    public abstract void getf(Matrix2d m, int offset, ByteBuffer src);
    public abstract void get(Vector4d dst, int offset, DoubleBuffer src);
    public abstract void get(Vector4d dst, int offset, ByteBuffer src);
    public abstract void get(Vector4f dst, int offset, FloatBuffer src);
    public abstract void get(Vector4f dst, int offset, ByteBuffer src);
    public abstract void get(Vector4i dst, int offset, IntBuffer src);
    public abstract void get(Vector4i dst, int offset, ByteBuffer src);
    public abstract void get(Vector3f dst, int offset, FloatBuffer src);
    public abstract void get(Vector3f dst, int offset, ByteBuffer src);
    public abstract void get(Vector3d dst, int offset, DoubleBuffer src);
    public abstract void get(Vector3d dst, int offset, ByteBuffer src);
    public abstract void get(Vector3i dst, int offset, IntBuffer src);
    public abstract void get(Vector3i dst, int offset, ByteBuffer src);
    public abstract void get(Vector2f dst, int offset, FloatBuffer src);
    public abstract void get(Vector2f dst, int offset, ByteBuffer src);
    public abstract void get(Vector2d dst, int offset, DoubleBuffer src);
    public abstract void get(Vector2d dst, int offset, ByteBuffer src);
    public abstract void get(Vector2i dst, int offset, IntBuffer src);
    public abstract void get(Vector2i dst, int offset, ByteBuffer src);
    public abstract void putMatrix3f(Quaternionf q, int position, ByteBuffer dest);
    public abstract void putMatrix3f(Quaternionf q, int position, FloatBuffer dest);
    public abstract void putMatrix4f(Quaternionf q, int position, ByteBuffer dest);
    public abstract void putMatrix4f(Quaternionf q, int position, FloatBuffer dest);
    public abstract void putMatrix4x3f(Quaternionf q, int position, ByteBuffer dest);
    public abstract void putMatrix4x3f(Quaternionf q, int position, FloatBuffer dest);
//#endif

    public abstract float get(Matrix4f m, int column, int row);
    public abstract Matrix4f set(Matrix4f m, int column, int row, float v);
    public abstract double get(Matrix4d m, int column, int row);
    public abstract Matrix4d set(Matrix4d m, int column, int row, double v);
    public abstract float get(Matrix3f m, int column, int row);
    public abstract Matrix3f set(Matrix3f m, int column, int row, float v);
    public abstract double get(Matrix3d m, int column, int row);
    public abstract Matrix3d set(Matrix3d m, int column, int row, double v);
    public abstract Vector4f getColumn(Matrix4f m, int column, Vector4f dest);
    public abstract Matrix4f setColumn(Vector4f v, int column, Matrix4f dest);
    public abstract Matrix4f setColumn(Vector4fc v, int column, Matrix4f dest);
    public abstract void copy(Matrix4f src, Matrix4f dest);
    public abstract void copy(Matrix4x3f src, Matrix4x3f dest);
    public abstract void copy(Matrix4f src, Matrix4x3f dest);
    public abstract void copy(Matrix4x3f src, Matrix4f dest);
    public abstract void copy(Matrix3f src, Matrix3f dest);
    public abstract void copy(Matrix3f src, Matrix4f dest);
    public abstract void copy(Matrix4f src, Matrix3f dest);
    public abstract void copy(Matrix3f src, Matrix4x3f dest);
    public abstract void copy(Matrix3x2f src, Matrix3x2f dest);
    public abstract void copy(Matrix3x2d src, Matrix3x2d dest);
    public abstract void copy(Matrix2f src, Matrix2f dest);
    public abstract void copy(Matrix2d src, Matrix2d dest);
    public abstract void copy(Matrix2f src, Matrix3f dest);
    public abstract void copy(Matrix3f src, Matrix2f dest);
    public abstract void copy(Matrix2f src, Matrix3x2f dest);
    public abstract void copy(Matrix3x2f src, Matrix2f dest);
    public abstract void copy(Matrix2d src, Matrix3d dest);
    public abstract void copy(Matrix3d src, Matrix2d dest);
    public abstract void copy(Matrix2d src, Matrix3x2d dest);
    public abstract void copy(Matrix3x2d src, Matrix2d dest);
    public abstract void copy3x3(Matrix4f src, Matrix4f dest);
    public abstract void copy3x3(Matrix4x3f src, Matrix4x3f dest);
    public abstract void copy3x3(Matrix3f src, Matrix4x3f dest);
    public abstract void copy3x3(Matrix3f src, Matrix4f dest);
    public abstract void copy4x3(Matrix4f src, Matrix4f dest);
    public abstract void copy4x3(Matrix4x3f src, Matrix4f dest);
    public abstract void copy(float[] arr, int off, Matrix4f dest);
    public abstract void copyTransposed(float[] arr, int off, Matrix4f dest);
    public abstract void copy(float[] arr, int off, Matrix3f dest);
    public abstract void copy(float[] arr, int off, Matrix4x3f dest);
    public abstract void copy(float[] arr, int off, Matrix3x2f dest);
    public abstract void copy(double[] arr, int off, Matrix3x2d dest);
    public abstract void copy(float[] arr, int off, Matrix2f dest);
    public abstract void copy(double[] arr, int off, Matrix2d dest);
    public abstract void copy(Matrix4f src, float[] dest, int off);
    public abstract void copy(Matrix3f src, float[] dest, int off);
    public abstract void copy(Matrix4x3f src, float[] dest, int off);
    public abstract void copy(Matrix3x2f src, float[] dest, int off);
    public abstract void copy(Matrix3x2d src, double[] dest, int off);
    public abstract void copy(Matrix2f src, float[] dest, int off);
    public abstract void copy(Matrix2d src, double[] dest, int off);
    public abstract void copy4x4(Matrix4x3f src, float[] dest, int off);
    public abstract void copy4x4(Matrix4x3d src, float[] dest, int off);
    public abstract void copy4x4(Matrix4x3d src, double[] dest, int off);
    public abstract void copy4x4(Matrix3x2f src, float[] dest, int off);
    public abstract void copy4x4(Matrix3x2d src, double[] dest, int off);
    public abstract void copy3x3(Matrix3x2f src, float[] dest, int off);
    public abstract void copy3x3(Matrix3x2d src, double[] dest, int off);
    public abstract void identity(Matrix4f dest);
    public abstract void identity(Matrix4x3f dest);
    public abstract void identity(Matrix3f dest);
    public abstract void identity(Matrix3x2f dest);
    public abstract void identity(Matrix3x2d dest);
    public abstract void identity(Matrix2f dest);
    public abstract void swap(Matrix4f m1, Matrix4f m2);
    public abstract void swap(Matrix4x3f m1, Matrix4x3f m2);
    public abstract void swap(Matrix3f m1, Matrix3f m2);
    public abstract void swap(Matrix2f m1, Matrix2f m2);
    public abstract void swap(Matrix2d m1, Matrix2d m2);
    public abstract void zero(Matrix4f dest);
    public abstract void zero(Matrix4x3f dest);
    public abstract void zero(Matrix3f dest);
    public abstract void zero(Matrix3x2f dest);
    public abstract void zero(Matrix3x2d dest);
    public abstract void zero(Matrix2f dest);
    public abstract void zero(Matrix2d dest);

    public static class MemUtilNIO extends MemUtil {
//#ifdef __HAS_NIO__
        public void put0(Matrix4f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  m.m03())
            .put(4,  m.m10())
            .put(5,  m.m11())
            .put(6,  m.m12())
            .put(7,  m.m13())
            .put(8,  m.m20())
            .put(9,  m.m21())
            .put(10, m.m22())
            .put(11, m.m23())
            .put(12, m.m30())
            .put(13, m.m31())
            .put(14, m.m32())
            .put(15, m.m33());
        }
        public void putN(Matrix4f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m03())
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  m.m13())
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, m.m23())
            .put(offset+12, m.m30())
            .put(offset+13, m.m31())
            .put(offset+14, m.m32())
            .put(offset+15, m.m33());
        }
        public void put(Matrix4f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put0(Matrix4f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, m.m03())
            .putFloat(16, m.m10())
            .putFloat(20, m.m11())
            .putFloat(24, m.m12())
            .putFloat(28, m.m13())
            .putFloat(32, m.m20())
            .putFloat(36, m.m21())
            .putFloat(40, m.m22())
            .putFloat(44, m.m23())
            .putFloat(48, m.m30())
            .putFloat(52, m.m31())
            .putFloat(56, m.m32())
            .putFloat(60, m.m33());
        }
        private void putN(Matrix4f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, m.m03())
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m12())
            .putFloat(offset+28, m.m13())
            .putFloat(offset+32, m.m20())
            .putFloat(offset+36, m.m21())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, m.m23())
            .putFloat(offset+48, m.m30())
            .putFloat(offset+52, m.m31())
            .putFloat(offset+56, m.m32())
            .putFloat(offset+60, m.m33());
        }
        public void put(Matrix4f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put4x3_0(Matrix4f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  m.m10())
            .put(4,  m.m11())
            .put(5,  m.m12())
            .put(6,  m.m20())
            .put(7,  m.m21())
            .put(8,  m.m22())
            .put(9,  m.m30())
            .put(10, m.m31())
            .put(11, m.m32());
        }
        public void put4x3_N(Matrix4f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m10())
            .put(offset+4,  m.m11())
            .put(offset+5,  m.m12())
            .put(offset+6,  m.m20())
            .put(offset+7,  m.m21())
            .put(offset+8,  m.m22())
            .put(offset+9,  m.m30())
            .put(offset+10, m.m31())
            .put(offset+11, m.m32());
        }
        public void put4x3(Matrix4f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put4x3_0(m, dest);
            else
                put4x3_N(m, offset, dest);
        }

        public void put4x3_0(Matrix4f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, m.m10())
            .putFloat(16, m.m11())
            .putFloat(20, m.m12())
            .putFloat(24, m.m20())
            .putFloat(28, m.m21())
            .putFloat(32, m.m22())
            .putFloat(36, m.m30())
            .putFloat(40, m.m31())
            .putFloat(44, m.m32());
        }
        private void put4x3_N(Matrix4f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, m.m10())
            .putFloat(offset+16, m.m11())
            .putFloat(offset+20, m.m12())
            .putFloat(offset+24, m.m20())
            .putFloat(offset+28, m.m21())
            .putFloat(offset+32, m.m22())
            .putFloat(offset+36, m.m30())
            .putFloat(offset+40, m.m31())
            .putFloat(offset+44, m.m32());
        }
        public void put4x3(Matrix4f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put4x3_0(m, dest);
            else
                put4x3_N(m, offset, dest);
        }

        public void put3x4_0(Matrix4f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, m.m03())
            .putFloat(16, m.m10())
            .putFloat(20, m.m11())
            .putFloat(24, m.m12())
            .putFloat(28, m.m13())
            .putFloat(32, m.m20())
            .putFloat(36, m.m21())
            .putFloat(40, m.m22())
            .putFloat(44, m.m23());
        }
        private void put3x4_N(Matrix4f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, m.m03())
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m12())
            .putFloat(offset+28, m.m13())
            .putFloat(offset+32, m.m20())
            .putFloat(offset+36, m.m21())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, m.m23());
        }
        public void put3x4(Matrix4f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put3x4_0(Matrix4f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  m.m03())
            .put(4,  m.m10())
            .put(5,  m.m11())
            .put(6,  m.m12())
            .put(7,  m.m13())
            .put(8,  m.m20())
            .put(9,  m.m21())
            .put(10, m.m22())
            .put(11, m.m23());
        }
        public void put3x4_N(Matrix4f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m03())
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  m.m13())
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, m.m23());
        }
        public void put3x4(Matrix4f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put3x4_0(Matrix4x3f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, 0.0f)
            .putFloat(16, m.m10())
            .putFloat(20, m.m11())
            .putFloat(24, m.m12())
            .putFloat(28, 0.0f)
            .putFloat(32, m.m20())
            .putFloat(36, m.m21())
            .putFloat(40, m.m22())
            .putFloat(44, 0.0f);
        }
        private void put3x4_N(Matrix4x3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, 0.0f)
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m12())
            .putFloat(offset+28, 0.0f)
            .putFloat(offset+32, m.m20())
            .putFloat(offset+36, m.m21())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, 0.0f);
        }
        public void put3x4(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put3x4_0(Matrix4x3f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  0.0f)
            .put(4,  m.m10())
            .put(5,  m.m11())
            .put(6,  m.m12())
            .put(7,  0.0f)
            .put(8,  m.m20())
            .put(9,  m.m21())
            .put(10, m.m22())
            .put(11, 0.0f);
        }
        public void put3x4_N(Matrix4x3f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  0.0f)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  0.0f)
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, 0.0f);
        }
        public void put3x4(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put0(Matrix4x3f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  m.m10())
            .put(4,  m.m11())
            .put(5,  m.m12())
            .put(6,  m.m20())
            .put(7,  m.m21())
            .put(8,  m.m22())
            .put(9,  m.m30())
            .put(10, m.m31())
            .put(11, m.m32());
        }
        public void putN(Matrix4x3f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m10())
            .put(offset+4,  m.m11())
            .put(offset+5,  m.m12())
            .put(offset+6,  m.m20())
            .put(offset+7,  m.m21())
            .put(offset+8,  m.m22())
            .put(offset+9,  m.m30())
            .put(offset+10, m.m31())
            .put(offset+11, m.m32());
        }
        public void put(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put0(Matrix4x3f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, m.m10())
            .putFloat(16, m.m11())
            .putFloat(20, m.m12())
            .putFloat(24, m.m20())
            .putFloat(28, m.m21())
            .putFloat(32, m.m22())
            .putFloat(36, m.m30())
            .putFloat(40, m.m31())
            .putFloat(44, m.m32());
        }
        public void putN(Matrix4x3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, m.m10())
            .putFloat(offset+16, m.m11())
            .putFloat(offset+20, m.m12())
            .putFloat(offset+24, m.m20())
            .putFloat(offset+28, m.m21())
            .putFloat(offset+32, m.m22())
            .putFloat(offset+36, m.m30())
            .putFloat(offset+40, m.m31())
            .putFloat(offset+44, m.m32());
        }
        public void put(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put4x4(Matrix4x3f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  0.0f)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  0.0f)
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, 0.0f)
            .put(offset+12, m.m30())
            .put(offset+13, m.m31())
            .put(offset+14, m.m32())
            .put(offset+15, 1.0f);
        }

        public void put4x4(Matrix4x3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, 0.0f)
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m12())
            .putFloat(offset+28, 0.0f)
            .putFloat(offset+32, m.m20())
            .putFloat(offset+36, m.m21())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, 0.0f)
            .putFloat(offset+48, m.m30())
            .putFloat(offset+52, m.m31())
            .putFloat(offset+56, m.m32())
            .putFloat(offset+60, 1.0f);
        }

        public void put4x4(Matrix4x3d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  0.0)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  0.0)
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, 0.0)
            .put(offset+12, m.m30())
            .put(offset+13, m.m31())
            .put(offset+14, m.m32())
            .put(offset+15, 1.0);
        }

        public void put4x4(Matrix4x3d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,     m.m00())
            .putDouble(offset+8,   m.m01())
            .putDouble(offset+16,   m.m02())
            .putDouble(offset+24,  0.0)
            .putDouble(offset+32,  m.m10())
            .putDouble(offset+40,  m.m11())
            .putDouble(offset+48,  m.m12())
            .putDouble(offset+56,  0.0)
            .putDouble(offset+64,  m.m20())
            .putDouble(offset+72,  m.m21())
            .putDouble(offset+80,  m.m22())
            .putDouble(offset+88,  0.0)
            .putDouble(offset+96,  m.m30())
            .putDouble(offset+104, m.m31())
            .putDouble(offset+112, m.m32())
            .putDouble(offset+120, 1.0);
        }

        public void put4x4(Matrix3x2f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  0.0f)
            .put(offset+3,  0.0f)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  0.0f)
            .put(offset+7,  0.0f)
            .put(offset+8,  0.0f)
            .put(offset+9,  0.0f)
            .put(offset+10, 1.0f)
            .put(offset+11, 0.0f)
            .put(offset+12, m.m20())
            .put(offset+13, m.m21())
            .put(offset+14, 0.0f)
            .put(offset+15, 1.0f);
        }

        public void put4x4(Matrix3x2f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  0.0f)
            .putFloat(offset+12, 0.0f)
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, 0.0f)
            .putFloat(offset+28, 0.0f)
            .putFloat(offset+32, 0.0f)
            .putFloat(offset+36, 0.0f)
            .putFloat(offset+40, 1.0f)
            .putFloat(offset+44, 0.0f)
            .putFloat(offset+48, m.m20())
            .putFloat(offset+52, m.m21())
            .putFloat(offset+56, 0.0f)
            .putFloat(offset+60, 1.0f);
        }

        public void put4x4(Matrix3x2d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  0.0)
            .put(offset+3,  0.0)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  0.0)
            .put(offset+7,  0.0)
            .put(offset+8,  0.0)
            .put(offset+9,  0.0)
            .put(offset+10, 1.0)
            .put(offset+11, 0.0)
            .put(offset+12, m.m20())
            .put(offset+13, m.m21())
            .put(offset+14, 0.0)
            .put(offset+15, 1.0);
        }

        public void put4x4(Matrix3x2d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,     m.m00())
            .putDouble(offset+8,   m.m01())
            .putDouble(offset+16,  0.0)
            .putDouble(offset+24,  0.0)
            .putDouble(offset+32,  m.m10())
            .putDouble(offset+40,  m.m11())
            .putDouble(offset+48,  0.0)
            .putDouble(offset+56,  0.0)
            .putDouble(offset+64,  0.0)
            .putDouble(offset+72,  0.0)
            .putDouble(offset+80,  1.0)
            .putDouble(offset+88,  0.0)
            .putDouble(offset+96,  m.m20())
            .putDouble(offset+104, m.m21())
            .putDouble(offset+112, 0.0)
            .putDouble(offset+120, 1.0);
        }

        public void put3x3(Matrix3x2f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, 0.0f)
            .put(offset+3, m.m10())
            .put(offset+4, m.m11())
            .put(offset+5, 0.0f)
            .put(offset+6, m.m20())
            .put(offset+7, m.m21())
            .put(offset+8, 1.0f);
        }

        public void put3x3(Matrix3x2f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  0.0f)
            .putFloat(offset+12, m.m10())
            .putFloat(offset+16, m.m11())
            .putFloat(offset+20, 0.0f)
            .putFloat(offset+24, m.m20())
            .putFloat(offset+28, m.m21())
            .putFloat(offset+32, 1.0f);
        }

        public void put3x3(Matrix3x2d m, int offset, DoubleBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, 0.0)
            .put(offset+3, m.m10())
            .put(offset+4, m.m11())
            .put(offset+5, 0.0)
            .put(offset+6, m.m20())
            .put(offset+7, m.m21())
            .put(offset+8, 1.0);
        }

        public void put3x3(Matrix3x2d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m01())
            .putDouble(offset+16, 0.0)
            .putDouble(offset+24, m.m10())
            .putDouble(offset+32, m.m11())
            .putDouble(offset+40, 0.0)
            .putDouble(offset+48, m.m20())
            .putDouble(offset+56, m.m21())
            .putDouble(offset+64, 1.0);
        }

        private void putTransposedN(Matrix4f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32())
            .put(offset+12, m.m03())
            .put(offset+13, m.m13())
            .put(offset+14, m.m23())
            .put(offset+15, m.m33());
        }
        private void putTransposed0(Matrix4f m, FloatBuffer dest) {
            dest.put(0,    m.m00())
            .put(1,  m.m10())
            .put(2,  m.m20())
            .put(3,  m.m30())
            .put(4,  m.m01())
            .put(5,  m.m11())
            .put(6,  m.m21())
            .put(7,  m.m31())
            .put(8,  m.m02())
            .put(9,  m.m12())
            .put(10, m.m22())
            .put(11, m.m32())
            .put(12, m.m03())
            .put(13, m.m13())
            .put(14, m.m23())
            .put(15, m.m33());
        }
        public void putTransposed(Matrix4f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                putTransposed0(m, dest);
            else
                putTransposedN(m, offset, dest);
        }

        private void putTransposedN(Matrix4f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m10())
            .putFloat(offset+8,  m.m20())
            .putFloat(offset+12, m.m30())
            .putFloat(offset+16, m.m01())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m21())
            .putFloat(offset+28, m.m31())
            .putFloat(offset+32, m.m02())
            .putFloat(offset+36, m.m12())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, m.m32())
            .putFloat(offset+48, m.m03())
            .putFloat(offset+52, m.m13())
            .putFloat(offset+56, m.m23())
            .putFloat(offset+60, m.m33());
        }
        private void putTransposed0(Matrix4f m, ByteBuffer dest) {
            dest.putFloat(0,    m.m00())
            .putFloat(4,  m.m10())
            .putFloat(8,  m.m20())
            .putFloat(12, m.m30())
            .putFloat(16, m.m01())
            .putFloat(20, m.m11())
            .putFloat(24, m.m21())
            .putFloat(28, m.m31())
            .putFloat(32, m.m02())
            .putFloat(36, m.m12())
            .putFloat(40, m.m22())
            .putFloat(44, m.m32())
            .putFloat(48, m.m03())
            .putFloat(52, m.m13())
            .putFloat(56, m.m23())
            .putFloat(60, m.m33());
        }
        public void putTransposed(Matrix4f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                putTransposed0(m, dest);
            else
                putTransposedN(m, offset, dest);
        }

        public void put4x3Transposed(Matrix4f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32());
        }

        public void put4x3Transposed(Matrix4f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m10())
            .putFloat(offset+8,  m.m20())
            .putFloat(offset+12, m.m30())
            .putFloat(offset+16, m.m01())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m21())
            .putFloat(offset+28, m.m31())
            .putFloat(offset+32, m.m02())
            .putFloat(offset+36, m.m12())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, m.m32());
        }

        public void putTransposed(Matrix4x3f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32());
        }

        public void putTransposed(Matrix4x3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m10())
            .putFloat(offset+8,  m.m20())
            .putFloat(offset+12, m.m30())
            .putFloat(offset+16, m.m01())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m21())
            .putFloat(offset+28, m.m31())
            .putFloat(offset+32, m.m02())
            .putFloat(offset+36, m.m12())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, m.m32());
        }

        public void putTransposed(Matrix3f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m10())
            .put(offset+2, m.m20())
            .put(offset+3, m.m01())
            .put(offset+4, m.m11())
            .put(offset+5, m.m21())
            .put(offset+6, m.m02())
            .put(offset+7, m.m12())
            .put(offset+8, m.m22());
        }

        public void putTransposed(Matrix3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m10())
            .putFloat(offset+8,  m.m20())
            .putFloat(offset+12, m.m01())
            .putFloat(offset+16, m.m11())
            .putFloat(offset+20, m.m21())
            .putFloat(offset+24, m.m02())
            .putFloat(offset+28, m.m12())
            .putFloat(offset+32, m.m22());
        }

        public void putTransposed(Matrix2f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m10())
            .put(offset+2, m.m01())
            .put(offset+3, m.m11());
        }

        public void putTransposed(Matrix2f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m10())
            .putFloat(offset+8,  m.m01())
            .putFloat(offset+12, m.m11());
        }

        public void put(Matrix4d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m03())
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  m.m13())
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, m.m23())
            .put(offset+12, m.m30())
            .put(offset+13, m.m31())
            .put(offset+14, m.m32())
            .put(offset+15, m.m33());
        }

        public void put(Matrix4d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,     m.m00())
            .putDouble(offset+8,   m.m01())
            .putDouble(offset+16,   m.m02())
            .putDouble(offset+24,  m.m03())
            .putDouble(offset+32,  m.m10())
            .putDouble(offset+40,  m.m11())
            .putDouble(offset+48,  m.m12())
            .putDouble(offset+56,  m.m13())
            .putDouble(offset+64,  m.m20())
            .putDouble(offset+72,  m.m21())
            .putDouble(offset+80,  m.m22())
            .putDouble(offset+88,  m.m23())
            .putDouble(offset+96,  m.m30())
            .putDouble(offset+104, m.m31())
            .putDouble(offset+112, m.m32())
            .putDouble(offset+120, m.m33());
        }

        public void put(Matrix4x3d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  m.m10())
            .put(offset+4,  m.m11())
            .put(offset+5,  m.m12())
            .put(offset+6,  m.m20())
            .put(offset+7,  m.m21())
            .put(offset+8,  m.m22())
            .put(offset+9,  m.m30())
            .put(offset+10, m.m31())
            .put(offset+11, m.m32());
        }

        public void put(Matrix4x3d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m01())
            .putDouble(offset+16,  m.m02())
            .putDouble(offset+24, m.m10())
            .putDouble(offset+32, m.m11())
            .putDouble(offset+40, m.m12())
            .putDouble(offset+48, m.m20())
            .putDouble(offset+56, m.m21())
            .putDouble(offset+64, m.m22())
            .putDouble(offset+72, m.m30())
            .putDouble(offset+80, m.m31())
            .putDouble(offset+88, m.m32());
        }

        public void putf(Matrix4d m, int offset, FloatBuffer dest) {
            dest.put(offset,    (float)m.m00())
            .put(offset+1,  (float)m.m01())
            .put(offset+2,  (float)m.m02())
            .put(offset+3,  (float)m.m03())
            .put(offset+4,  (float)m.m10())
            .put(offset+5,  (float)m.m11())
            .put(offset+6,  (float)m.m12())
            .put(offset+7,  (float)m.m13())
            .put(offset+8,  (float)m.m20())
            .put(offset+9,  (float)m.m21())
            .put(offset+10, (float)m.m22())
            .put(offset+11, (float)m.m23())
            .put(offset+12, (float)m.m30())
            .put(offset+13, (float)m.m31())
            .put(offset+14, (float)m.m32())
            .put(offset+15, (float)m.m33());
        }

        public void putf(Matrix4d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m01())
            .putFloat(offset+8,  (float)m.m02())
            .putFloat(offset+12, (float)m.m03())
            .putFloat(offset+16, (float)m.m10())
            .putFloat(offset+20, (float)m.m11())
            .putFloat(offset+24, (float)m.m12())
            .putFloat(offset+28, (float)m.m13())
            .putFloat(offset+32, (float)m.m20())
            .putFloat(offset+36, (float)m.m21())
            .putFloat(offset+40, (float)m.m22())
            .putFloat(offset+44, (float)m.m23())
            .putFloat(offset+48, (float)m.m30())
            .putFloat(offset+52, (float)m.m31())
            .putFloat(offset+56, (float)m.m32())
            .putFloat(offset+60, (float)m.m33());
        }

        public void putf(Matrix4x3d m, int offset, FloatBuffer dest) {
            dest.put(offset,    (float)m.m00())
            .put(offset+1,  (float)m.m01())
            .put(offset+2,  (float)m.m02())
            .put(offset+3,  (float)m.m10())
            .put(offset+4,  (float)m.m11())
            .put(offset+5,  (float)m.m12())
            .put(offset+6,  (float)m.m20())
            .put(offset+7,  (float)m.m21())
            .put(offset+8,  (float)m.m22())
            .put(offset+9,  (float)m.m30())
            .put(offset+10, (float)m.m31())
            .put(offset+11, (float)m.m32());
        }

        public void putf(Matrix4x3d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m01())
            .putFloat(offset+8,  (float)m.m02())
            .putFloat(offset+12, (float)m.m10())
            .putFloat(offset+16, (float)m.m11())
            .putFloat(offset+20, (float)m.m12())
            .putFloat(offset+24, (float)m.m20())
            .putFloat(offset+28, (float)m.m21())
            .putFloat(offset+32, (float)m.m22())
            .putFloat(offset+36, (float)m.m30())
            .putFloat(offset+40, (float)m.m31())
            .putFloat(offset+44, (float)m.m32());
        }

        public void putTransposed(Matrix4d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32())
            .put(offset+12, m.m03())
            .put(offset+13, m.m13())
            .put(offset+14, m.m23())
            .put(offset+15, m.m33());
        }

        public void putTransposed(Matrix4d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,     m.m00())
            .putDouble(offset+8,   m.m10())
            .putDouble(offset+16,  m.m20())
            .putDouble(offset+24,  m.m30())
            .putDouble(offset+32,  m.m01())
            .putDouble(offset+40,  m.m11())
            .putDouble(offset+48,  m.m21())
            .putDouble(offset+56,  m.m31())
            .putDouble(offset+64,  m.m02())
            .putDouble(offset+72,  m.m12())
            .putDouble(offset+80,  m.m22())
            .putDouble(offset+88,  m.m32())
            .putDouble(offset+96,  m.m03())
            .putDouble(offset+104, m.m13())
            .putDouble(offset+112, m.m23())
            .putDouble(offset+120, m.m33());
        }

        public void put4x3Transposed(Matrix4d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32());
        }

        public void put4x3Transposed(Matrix4d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,     m.m00())
            .putDouble(offset+8,   m.m10())
            .putDouble(offset+16,  m.m20())
            .putDouble(offset+24,  m.m30())
            .putDouble(offset+32,  m.m01())
            .putDouble(offset+40,  m.m11())
            .putDouble(offset+48,  m.m21())
            .putDouble(offset+56,  m.m31())
            .putDouble(offset+64,  m.m02())
            .putDouble(offset+72,  m.m12())
            .putDouble(offset+80,  m.m22())
            .putDouble(offset+88,  m.m32());
        }

        public void putTransposed(Matrix4x3d m, int offset, DoubleBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m10())
            .put(offset+2,  m.m20())
            .put(offset+3,  m.m30())
            .put(offset+4,  m.m01())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m21())
            .put(offset+7,  m.m31())
            .put(offset+8,  m.m02())
            .put(offset+9,  m.m12())
            .put(offset+10, m.m22())
            .put(offset+11, m.m32());
        }

        public void putTransposed(Matrix4x3d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m10())
            .putDouble(offset+16,  m.m20())
            .putDouble(offset+24, m.m30())
            .putDouble(offset+32, m.m01())
            .putDouble(offset+40, m.m11())
            .putDouble(offset+48, m.m21())
            .putDouble(offset+56, m.m31())
            .putDouble(offset+64, m.m02())
            .putDouble(offset+72, m.m12())
            .putDouble(offset+80, m.m22())
            .putDouble(offset+88, m.m32());
        }

        public void putTransposed(Matrix2d m, int offset, DoubleBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m10())
            .put(offset+2, m.m01())
            .put(offset+3, m.m11());
        }

        public void putTransposed(Matrix2d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m10())
            .putDouble(offset+16, m.m01())
            .putDouble(offset+24, m.m11());
        }

        public void putfTransposed(Matrix4x3d m, int offset, FloatBuffer dest) {
            dest.put(offset,    (float)m.m00())
            .put(offset+1,  (float)m.m10())
            .put(offset+2,  (float)m.m20())
            .put(offset+3,  (float)m.m30())
            .put(offset+4,  (float)m.m01())
            .put(offset+5,  (float)m.m11())
            .put(offset+6,  (float)m.m21())
            .put(offset+7,  (float)m.m31())
            .put(offset+8,  (float)m.m02())
            .put(offset+9,  (float)m.m12())
            .put(offset+10, (float)m.m22())
            .put(offset+11, (float)m.m32());
        }

        public void putfTransposed(Matrix4x3d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m10())
            .putFloat(offset+8,  (float)m.m20())
            .putFloat(offset+12, (float)m.m30())
            .putFloat(offset+16, (float)m.m01())
            .putFloat(offset+20, (float)m.m11())
            .putFloat(offset+24, (float)m.m21())
            .putFloat(offset+28, (float)m.m31())
            .putFloat(offset+32, (float)m.m02())
            .putFloat(offset+36, (float)m.m12())
            .putFloat(offset+40, (float)m.m22())
            .putFloat(offset+44, (float)m.m32());
        }

        public void putfTransposed(Matrix2d m, int offset, FloatBuffer dest) {
            dest.put(offset,   (float)m.m00())
            .put(offset+1, (float)m.m10())
            .put(offset+2, (float)m.m01())
            .put(offset+3, (float)m.m11());
        }

        public void putfTransposed(Matrix2d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m10())
            .putFloat(offset+8,  (float)m.m01())
            .putFloat(offset+12, (float)m.m11());
        }

        public void putfTransposed(Matrix4d m, int offset, FloatBuffer dest) {
            dest.put(offset,    (float)m.m00())
            .put(offset+1,  (float)m.m10())
            .put(offset+2,  (float)m.m20())
            .put(offset+3,  (float)m.m30())
            .put(offset+4,  (float)m.m01())
            .put(offset+5,  (float)m.m11())
            .put(offset+6,  (float)m.m21())
            .put(offset+7,  (float)m.m31())
            .put(offset+8,  (float)m.m02())
            .put(offset+9,  (float)m.m12())
            .put(offset+10, (float)m.m22())
            .put(offset+11, (float)m.m32())
            .put(offset+12, (float)m.m03())
            .put(offset+13, (float)m.m13())
            .put(offset+14, (float)m.m23())
            .put(offset+15, (float)m.m33());
        }

        public void putfTransposed(Matrix4d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m10())
            .putFloat(offset+8,  (float)m.m20())
            .putFloat(offset+12, (float)m.m30())
            .putFloat(offset+16, (float)m.m01())
            .putFloat(offset+20, (float)m.m11())
            .putFloat(offset+24, (float)m.m21())
            .putFloat(offset+28, (float)m.m31())
            .putFloat(offset+32, (float)m.m02())
            .putFloat(offset+36, (float)m.m12())
            .putFloat(offset+40, (float)m.m22())
            .putFloat(offset+44, (float)m.m32())
            .putFloat(offset+48, (float)m.m03())
            .putFloat(offset+52, (float)m.m13())
            .putFloat(offset+56, (float)m.m23())
            .putFloat(offset+60, (float)m.m33());
        }

        public void put0(Matrix3f m, FloatBuffer dest) {
            dest.put(0,   m.m00())
            .put(1, m.m01())
            .put(2, m.m02())
            .put(3, m.m10())
            .put(4, m.m11())
            .put(5, m.m12())
            .put(6, m.m20())
            .put(7, m.m21())
            .put(8, m.m22());
        }
        public void putN(Matrix3f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m02())
            .put(offset+3, m.m10())
            .put(offset+4, m.m11())
            .put(offset+5, m.m12())
            .put(offset+6, m.m20())
            .put(offset+7, m.m21())
            .put(offset+8, m.m22());
        }
        public void put(Matrix3f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put0(Matrix3f m, ByteBuffer dest) {
            dest.putFloat(0,    m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, m.m10())
            .putFloat(16, m.m11())
            .putFloat(20, m.m12())
            .putFloat(24, m.m20())
            .putFloat(28, m.m21())
            .putFloat(32, m.m22());
        }
        public void putN(Matrix3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, m.m10())
            .putFloat(offset+16, m.m11())
            .putFloat(offset+20, m.m12())
            .putFloat(offset+24, m.m20())
            .putFloat(offset+28, m.m21())
            .putFloat(offset+32, m.m22());
        }
        public void put(Matrix3f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put0(m, dest);
            else
                putN(m, offset, dest);
        }

        public void put3x4_0(Matrix3f m, ByteBuffer dest) {
            dest.putFloat(0,  m.m00())
            .putFloat(4,  m.m01())
            .putFloat(8,  m.m02())
            .putFloat(12, 0.0f)
            .putFloat(16, m.m10())
            .putFloat(20, m.m11())
            .putFloat(24, m.m12())
            .putFloat(28, 0.0f)
            .putFloat(32, m.m20())
            .putFloat(36, m.m21())
            .putFloat(40, m.m22())
            .putFloat(44, 0.0f);
        }
        private void put3x4_N(Matrix3f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m02())
            .putFloat(offset+12, 0.0f)
            .putFloat(offset+16, m.m10())
            .putFloat(offset+20, m.m11())
            .putFloat(offset+24, m.m12())
            .putFloat(offset+28, 0.0f)
            .putFloat(offset+32, m.m20())
            .putFloat(offset+36, m.m21())
            .putFloat(offset+40, m.m22())
            .putFloat(offset+44, 0.0f);
        }
        public void put3x4(Matrix3f m, int offset, ByteBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put3x4_0(Matrix3f m, FloatBuffer dest) {
            dest.put(0,  m.m00())
            .put(1,  m.m01())
            .put(2,  m.m02())
            .put(3,  0.0f)
            .put(4,  m.m10())
            .put(5,  m.m11())
            .put(6,  m.m12())
            .put(7,  0.0f)
            .put(8,  m.m20())
            .put(9,  m.m21())
            .put(10, m.m22())
            .put(11, 0.0f);
        }
        public void put3x4_N(Matrix3f m, int offset, FloatBuffer dest) {
            dest.put(offset,    m.m00())
            .put(offset+1,  m.m01())
            .put(offset+2,  m.m02())
            .put(offset+3,  0.0f)
            .put(offset+4,  m.m10())
            .put(offset+5,  m.m11())
            .put(offset+6,  m.m12())
            .put(offset+7,  0.0f)
            .put(offset+8,  m.m20())
            .put(offset+9,  m.m21())
            .put(offset+10, m.m22())
            .put(offset+11, 0.0f);
        }
        public void put3x4(Matrix3f m, int offset, FloatBuffer dest) {
            if (offset == 0)
                put3x4_0(m, dest);
            else
                put3x4_N(m, offset, dest);
        }

        public void put(Matrix3d m, int offset, DoubleBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m02())
            .put(offset+3, m.m10())
            .put(offset+4, m.m11())
            .put(offset+5, m.m12())
            .put(offset+6, m.m20())
            .put(offset+7, m.m21())
            .put(offset+8, m.m22());
        }

        public void put(Matrix3d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m01())
            .putDouble(offset+16, m.m02())
            .putDouble(offset+24, m.m10())
            .putDouble(offset+32, m.m11())
            .putDouble(offset+40, m.m12())
            .putDouble(offset+48, m.m20())
            .putDouble(offset+56, m.m21())
            .putDouble(offset+64, m.m22());
        }

        public void put(Matrix3x2f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m10())
            .put(offset+3, m.m11())
            .put(offset+4, m.m20())
            .put(offset+5, m.m21());
        }

        public void put(Matrix3x2f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m10())
            .putFloat(offset+12, m.m11())
            .putFloat(offset+16, m.m20())
            .putFloat(offset+20, m.m21());
        }

        public void put(Matrix3x2d m, int offset, DoubleBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m10())
            .put(offset+3, m.m11())
            .put(offset+4, m.m20())
            .put(offset+5, m.m21());
        }

        public void put(Matrix3x2d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m01())
            .putDouble(offset+16, m.m10())
            .putDouble(offset+24, m.m11())
            .putDouble(offset+32, m.m20())
            .putDouble(offset+40, m.m21());
        }

        public void putf(Matrix3d m, int offset, FloatBuffer dest) {
            dest.put(offset,   (float)m.m00())
            .put(offset+1, (float)m.m01())
            .put(offset+2, (float)m.m02())
            .put(offset+3, (float)m.m10())
            .put(offset+4, (float)m.m11())
            .put(offset+5, (float)m.m12())
            .put(offset+6, (float)m.m20())
            .put(offset+7, (float)m.m21())
            .put(offset+8, (float)m.m22());
        }

        public void put(Matrix2f m, int offset, FloatBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m10())
            .put(offset+3, m.m11());
        }

        public void put(Matrix2f m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    m.m00())
            .putFloat(offset+4,  m.m01())
            .putFloat(offset+8,  m.m10())
            .putFloat(offset+12, m.m11());
        }

        public void put(Matrix2d m, int offset, DoubleBuffer dest) {
            dest.put(offset,   m.m00())
            .put(offset+1, m.m01())
            .put(offset+2, m.m10())
            .put(offset+3, m.m11());
        }

        public void put(Matrix2d m, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    m.m00())
            .putDouble(offset+8,  m.m01())
            .putDouble(offset+16, m.m10())
            .putDouble(offset+24, m.m11());
        }

        public void putf(Matrix2d m, int offset, FloatBuffer dest) {
            dest.put(offset,   (float)m.m00())
            .put(offset+1, (float)m.m01())
            .put(offset+2, (float)m.m10())
            .put(offset+3, (float)m.m11());
        }

        public void putf(Matrix2d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m01())
            .putFloat(offset+8,  (float)m.m10())
            .putFloat(offset+12, (float)m.m11());
        }

        public void putf(Matrix3d m, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float)m.m00())
            .putFloat(offset+4,  (float)m.m01())
            .putFloat(offset+8,  (float)m.m02())
            .putFloat(offset+12, (float)m.m10())
            .putFloat(offset+16, (float)m.m11())
            .putFloat(offset+20, (float)m.m12())
            .putFloat(offset+24, (float)m.m20())
            .putFloat(offset+28, (float)m.m21())
            .putFloat(offset+32, (float)m.m22());
        }

        public void put(Vector4d src, int offset, DoubleBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z)
            .put(offset+3, src.w);
        }

        public void put(Vector4d src, int offset, FloatBuffer dest) {
            dest.put(offset,   (float)src.x)
            .put(offset+1, (float)src.y)
            .put(offset+2, (float)src.z)
            .put(offset+3, (float)src.w);
        }

        public void put(Vector4d src, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    src.x)
            .putDouble(offset+8,  src.y)
            .putDouble(offset+16, src.z)
            .putDouble(offset+24, src.w);
        }

        public void putf(Vector4d src, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    (float) src.x)
            .putFloat(offset+4,  (float) src.y)
            .putFloat(offset+8,  (float) src.z)
            .putFloat(offset+12, (float) src.w);
        }

        public void put(Vector4f src, int offset, FloatBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z)
            .put(offset+3, src.w);
        }

        public void put(Vector4f src, int offset, ByteBuffer dest) {
            dest.putFloat(offset,    src.x)
            .putFloat(offset+4,  src.y)
            .putFloat(offset+8,  src.z)
            .putFloat(offset+12, src.w);
        }

        public void put(Vector4i src, int offset, IntBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z)
            .put(offset+3, src.w);
        }

        public void put(Vector4i src, int offset, ByteBuffer dest) {
            dest.putInt(offset,    src.x)
            .putInt(offset+4,  src.y)
            .putInt(offset+8,  src.z)
            .putInt(offset+12, src.w);
        }

        public void put(Vector3f src, int offset, FloatBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z);
        }

        public void put(Vector3f src, int offset, ByteBuffer dest) {
            dest.putFloat(offset,   src.x)
            .putFloat(offset+4, src.y)
            .putFloat(offset+8, src.z);
        }

        public void put(Vector3d src, int offset, DoubleBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z);
        }

        public void put(Vector3d src, int offset, FloatBuffer dest) {
            dest.put(offset,   (float)src.x)
            .put(offset+1, (float)src.y)
            .put(offset+2, (float)src.z);
        }

        public void put(Vector3d src, int offset, ByteBuffer dest) {
            dest.putDouble(offset,    src.x)
            .putDouble(offset+8,  src.y)
            .putDouble(offset+16, src.z);
        }

        public void putf(Vector3d src, int offset, ByteBuffer dest) {
            dest.putFloat(offset,   (float) src.x)
            .putFloat(offset+4, (float) src.y)
            .putFloat(offset+8, (float) src.z);
        }

        public void put(Vector3i src, int offset, IntBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y)
            .put(offset+2, src.z);
        }

        public void put(Vector3i src, int offset, ByteBuffer dest) {
            dest.putInt(offset,   src.x)
            .putInt(offset+4, src.y)
            .putInt(offset+8, src.z);
        }

        public void put(Vector2f src, int offset, FloatBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y);
        }

        public void put(Vector2f src, int offset, ByteBuffer dest) {
            dest.putFloat(offset,   src.x)
            .putFloat(offset+4, src.y);
        }

        public void put(Vector2d src, int offset, DoubleBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y);
        }

        public void put(Vector2d src, int offset, ByteBuffer dest) {
            dest.putDouble(offset,   src.x)
            .putDouble(offset+8, src.y);
        }

        public void put(Vector2i src, int offset, IntBuffer dest) {
            dest.put(offset,   src.x)
            .put(offset+1, src.y);
        }

        public void put(Vector2i src, int offset, ByteBuffer dest) {
            dest.putInt(offset,   src.x)
            .putInt(offset+4, src.y);
        }

        public void get(Matrix4f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m03(src.get(offset+3))
            ._m10(src.get(offset+4))
            ._m11(src.get(offset+5))
            ._m12(src.get(offset+6))
            ._m13(src.get(offset+7))
            ._m20(src.get(offset+8))
            ._m21(src.get(offset+9))
            ._m22(src.get(offset+10))
            ._m23(src.get(offset+11))
            ._m30(src.get(offset+12))
            ._m31(src.get(offset+13))
            ._m32(src.get(offset+14))
            ._m33(src.get(offset+15));
        }

        public void get(Matrix4f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m03(src.getFloat(offset+12))
            ._m10(src.getFloat(offset+16))
            ._m11(src.getFloat(offset+20))
            ._m12(src.getFloat(offset+24))
            ._m13(src.getFloat(offset+28))
            ._m20(src.getFloat(offset+32))
            ._m21(src.getFloat(offset+36))
            ._m22(src.getFloat(offset+40))
            ._m23(src.getFloat(offset+44))
            ._m30(src.getFloat(offset+48))
            ._m31(src.getFloat(offset+52))
            ._m32(src.getFloat(offset+56))
            ._m33(src.getFloat(offset+60));
        }

        public void getTransposed(Matrix4f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m10(src.get(offset+1))
            ._m20(src.get(offset+2))
            ._m30(src.get(offset+3))
            ._m01(src.get(offset+4))
            ._m11(src.get(offset+5))
            ._m21(src.get(offset+6))
            ._m31(src.get(offset+7))
            ._m02(src.get(offset+8))
            ._m12(src.get(offset+9))
            ._m22(src.get(offset+10))
            ._m32(src.get(offset+11))
            ._m03(src.get(offset+12))
            ._m13(src.get(offset+13))
            ._m23(src.get(offset+14))
            ._m33(src.get(offset+15));
        }

        public void getTransposed(Matrix4f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m10(src.getFloat(offset+4))
            ._m20(src.getFloat(offset+8))
            ._m30(src.getFloat(offset+12))
            ._m01(src.getFloat(offset+16))
            ._m11(src.getFloat(offset+20))
            ._m21(src.getFloat(offset+24))
            ._m31(src.getFloat(offset+28))
            ._m02(src.getFloat(offset+32))
            ._m12(src.getFloat(offset+36))
            ._m22(src.getFloat(offset+40))
            ._m32(src.getFloat(offset+44))
            ._m03(src.getFloat(offset+48))
            ._m13(src.getFloat(offset+52))
            ._m23(src.getFloat(offset+56))
            ._m33(src.getFloat(offset+60));
        }

        public void get(Matrix4x3f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8))
            ._m30(src.get(offset+9))
            ._m31(src.get(offset+10))
            ._m32(src.get(offset+11));
        }

        public void get(Matrix4x3f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m10(src.getFloat(offset+12))
            ._m11(src.getFloat(offset+16))
            ._m12(src.getFloat(offset+20))
            ._m20(src.getFloat(offset+24))
            ._m21(src.getFloat(offset+28))
            ._m22(src.getFloat(offset+32))
            ._m30(src.getFloat(offset+36))
            ._m31(src.getFloat(offset+40))
            ._m32(src.getFloat(offset+44));
        }

        public void get(Matrix4d m, int offset, DoubleBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m03(src.get(offset+3))
            ._m10(src.get(offset+4))
            ._m11(src.get(offset+5))
            ._m12(src.get(offset+6))
            ._m13(src.get(offset+7))
            ._m20(src.get(offset+8))
            ._m21(src.get(offset+9))
            ._m22(src.get(offset+10))
            ._m23(src.get(offset+11))
            ._m30(src.get(offset+12))
            ._m31(src.get(offset+13))
            ._m32(src.get(offset+14))
            ._m33(src.get(offset+15));
        }

        public void get(Matrix4d m, int offset, ByteBuffer src) {
            m._m00(src.getDouble(offset))
            ._m01(src.getDouble(offset+8))
            ._m02(src.getDouble(offset+16))
            ._m03(src.getDouble(offset+24))
            ._m10(src.getDouble(offset+32))
            ._m11(src.getDouble(offset+40))
            ._m12(src.getDouble(offset+48))
            ._m13(src.getDouble(offset+56))
            ._m20(src.getDouble(offset+64))
            ._m21(src.getDouble(offset+72))
            ._m22(src.getDouble(offset+80))
            ._m23(src.getDouble(offset+88))
            ._m30(src.getDouble(offset+96))
            ._m31(src.getDouble(offset+104))
            ._m32(src.getDouble(offset+112))
            ._m33(src.getDouble(offset+120));
        }

        public void get(Matrix4x3d m, int offset, DoubleBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8))
            ._m30(src.get(offset+9))
            ._m31(src.get(offset+10))
            ._m32(src.get(offset+11));
        }

        public void get(Matrix4x3d m, int offset, ByteBuffer src) {
            m._m00(src.getDouble(offset))
            ._m01(src.getDouble(offset+8))
            ._m02(src.getDouble(offset+16))
            ._m10(src.getDouble(offset+24))
            ._m11(src.getDouble(offset+32))
            ._m12(src.getDouble(offset+40))
            ._m20(src.getDouble(offset+48))
            ._m21(src.getDouble(offset+56))
            ._m22(src.getDouble(offset+64))
            ._m30(src.getDouble(offset+72))
            ._m31(src.getDouble(offset+80))
            ._m32(src.getDouble(offset+88));
        }

        public void getf(Matrix4d m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m03(src.get(offset+3))
            ._m10(src.get(offset+4))
            ._m11(src.get(offset+5))
            ._m12(src.get(offset+6))
            ._m13(src.get(offset+7))
            ._m20(src.get(offset+8))
            ._m21(src.get(offset+9))
            ._m22(src.get(offset+10))
            ._m23(src.get(offset+11))
            ._m30(src.get(offset+12))
            ._m31(src.get(offset+13))
            ._m32(src.get(offset+14))
            ._m33(src.get(offset+15));
        }

        public void getf(Matrix4d m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m03(src.getFloat(offset+12))
            ._m10(src.getFloat(offset+16))
            ._m11(src.getFloat(offset+20))
            ._m12(src.getFloat(offset+24))
            ._m13(src.getFloat(offset+28))
            ._m20(src.getFloat(offset+32))
            ._m21(src.getFloat(offset+36))
            ._m22(src.getFloat(offset+40))
            ._m23(src.getFloat(offset+44))
            ._m30(src.getFloat(offset+48))
            ._m31(src.getFloat(offset+52))
            ._m32(src.getFloat(offset+56))
            ._m33(src.getFloat(offset+60));
        }

        public void getf(Matrix4x3d m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8))
            ._m30(src.get(offset+9))
            ._m31(src.get(offset+10))
            ._m32(src.get(offset+11));
        }

        public void getf(Matrix4x3d m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m10(src.getFloat(offset+12))
            ._m11(src.getFloat(offset+16))
            ._m12(src.getFloat(offset+20))
            ._m20(src.getFloat(offset+24))
            ._m21(src.getFloat(offset+28))
            ._m22(src.getFloat(offset+32))
            ._m30(src.getFloat(offset+36))
            ._m31(src.getFloat(offset+40))
            ._m32(src.getFloat(offset+44));
        }

        public void get(Matrix3f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8));
        }

        public void get(Matrix3f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m10(src.getFloat(offset+12))
            ._m11(src.getFloat(offset+16))
            ._m12(src.getFloat(offset+20))
            ._m20(src.getFloat(offset+24))
            ._m21(src.getFloat(offset+28))
            ._m22(src.getFloat(offset+32));
        }

        public void get(Matrix3d m, int offset, DoubleBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8));
        }

        public void get(Matrix3d m, int offset, ByteBuffer src) {
            m._m00(src.getDouble(offset))
            ._m01(src.getDouble(offset+8))
            ._m02(src.getDouble(offset+16))
            ._m10(src.getDouble(offset+24))
            ._m11(src.getDouble(offset+32))
            ._m12(src.getDouble(offset+40))
            ._m20(src.getDouble(offset+48))
            ._m21(src.getDouble(offset+56))
            ._m22(src.getDouble(offset+64));
        }

        public void get(Matrix3x2f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m10(src.get(offset+2))
            ._m11(src.get(offset+3))
            ._m20(src.get(offset+4))
            ._m21(src.get(offset+5));
        }

        public void get(Matrix3x2f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m10(src.getFloat(offset+8))
            ._m11(src.getFloat(offset+12))
            ._m20(src.getFloat(offset+16))
            ._m21(src.getFloat(offset+20));
        }

        public void get(Matrix3x2d m, int offset, DoubleBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m10(src.get(offset+2))
            ._m11(src.get(offset+3))
            ._m20(src.get(offset+4))
            ._m21(src.get(offset+5));
        }

        public void get(Matrix3x2d m, int offset, ByteBuffer src) {
            m._m00(src.getDouble(offset))
            ._m01(src.getDouble(offset+8))
            ._m10(src.getDouble(offset+16))
            ._m11(src.getDouble(offset+24))
            ._m20(src.getDouble(offset+32))
            ._m21(src.getDouble(offset+40));
        }

        public void getf(Matrix3d m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m02(src.get(offset+2))
            ._m10(src.get(offset+3))
            ._m11(src.get(offset+4))
            ._m12(src.get(offset+5))
            ._m20(src.get(offset+6))
            ._m21(src.get(offset+7))
            ._m22(src.get(offset+8));
        }

        public void getf(Matrix3d m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m02(src.getFloat(offset+8))
            ._m10(src.getFloat(offset+12))
            ._m11(src.getFloat(offset+16))
            ._m12(src.getFloat(offset+20))
            ._m20(src.getFloat(offset+24))
            ._m21(src.getFloat(offset+28))
            ._m22(src.getFloat(offset+32));
        }

        public void get(Matrix2f m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m10(src.get(offset+2))
            ._m11(src.get(offset+3));
        }

        public void get(Matrix2f m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m10(src.getFloat(offset+8))
            ._m11(src.getFloat(offset+12));
        }

        public void get(Matrix2d m, int offset, DoubleBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m10(src.get(offset+2))
            ._m11(src.get(offset+3));
        }

        public void get(Matrix2d m, int offset, ByteBuffer src) {
            m._m00(src.getDouble(offset))
            ._m01(src.getDouble(offset+8))
            ._m10(src.getDouble(offset+16))
            ._m11(src.getDouble(offset+24));
        }

        public void getf(Matrix2d m, int offset, FloatBuffer src) {
            m._m00(src.get(offset))
            ._m01(src.get(offset+1))
            ._m10(src.get(offset+2))
            ._m11(src.get(offset+3));
        }

        public void getf(Matrix2d m, int offset, ByteBuffer src) {
            m._m00(src.getFloat(offset))
            ._m01(src.getFloat(offset+4))
            ._m10(src.getFloat(offset+8))
            ._m11(src.getFloat(offset+12));
        }

        public void get(Vector4d dst, int offset, DoubleBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
            dst.w = src.get(offset+3);
        }

        public void get(Vector4d dst, int offset, ByteBuffer src) {
            dst.x = src.getDouble(offset);
            dst.y = src.getDouble(offset+8);
            dst.z = src.getDouble(offset+16);
            dst.w = src.getDouble(offset+24);
        }

        public void get(Vector4f dst, int offset, FloatBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
            dst.w = src.get(offset+3);
        }

        public void get(Vector4f dst, int offset, ByteBuffer src) {
            dst.x = src.getFloat(offset);
            dst.y = src.getFloat(offset+4);
            dst.z = src.getFloat(offset+8);
            dst.w = src.getFloat(offset+12);
        }

        public void get(Vector4i dst, int offset, IntBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
            dst.w = src.get(offset+3);
        }

        public void get(Vector4i dst, int offset, ByteBuffer src) {
            dst.x = src.getInt(offset);
            dst.y = src.getInt(offset+4);
            dst.z = src.getInt(offset+8);
            dst.w = src.getInt(offset+12);
        }

        public void get(Vector3f dst, int offset, FloatBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
        }

        public void get(Vector3f dst, int offset, ByteBuffer src) {
            dst.x = src.getFloat(offset);
            dst.y = src.getFloat(offset+4);
            dst.z = src.getFloat(offset+8);
        }

        public void get(Vector3d dst, int offset, DoubleBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
        }

        public void get(Vector3d dst, int offset, ByteBuffer src) {
            dst.x = src.getDouble(offset);
            dst.y = src.getDouble(offset+8);
            dst.z = src.getDouble(offset+16);
        }

        public void get(Vector3i dst, int offset, IntBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
            dst.z = src.get(offset+2);
        }

        public void get(Vector3i dst, int offset, ByteBuffer src) {
            dst.x = src.getInt(offset);
            dst.y = src.getInt(offset+4);
            dst.z = src.getInt(offset+8);
        }

        public void get(Vector2f dst, int offset, FloatBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
        }

        public void get(Vector2f dst, int offset, ByteBuffer src) {
            dst.x = src.getFloat(offset);
            dst.y = src.getFloat(offset+4);
        }

        public void get(Vector2d dst, int offset, DoubleBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
        }

        public void get(Vector2d dst, int offset, ByteBuffer src) {
            dst.x = src.getDouble(offset);
            dst.y = src.getDouble(offset+8);
        }

        public void get(Vector2i dst, int offset, IntBuffer src) {
            dst.x = src.get(offset);
            dst.y = src.get(offset+1);
        }

        public void get(Vector2i dst, int offset, ByteBuffer src) {
            dst.x = src.getInt(offset);
            dst.y = src.getInt(offset+4);
        }
//#endif

        public float get(Matrix4f m, int column, int row) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00;
                case 1:
                    return m.m01;
                case 2:
                    return m.m02;
                case 3:
                    return m.m03;
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10;
                case 1:
                    return m.m11;
                case 2:
                    return m.m12;
                case 3:
                    return m.m13;
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20;
                case 1:
                    return m.m21;
                case 2:
                    return m.m22;
                case 3:
                    return m.m23;
                default:
                    break;
                }
                break;
            case 3:
                switch (row) {
                case 0:
                    return m.m30;
                case 1:
                    return m.m31;
                case 2:
                    return m.m32;
                case 3:
                    return m.m33;
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public Matrix4f set(Matrix4f m, int column, int row, float value) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00(value);
                case 1:
                    return m.m01(value);
                case 2:
                    return m.m02(value);
                case 3:
                    return m.m03(value);
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10(value);
                case 1:
                    return m.m11(value);
                case 2:
                    return m.m12(value);
                case 3:
                    return m.m13(value);
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20(value);
                case 1:
                    return m.m21(value);
                case 2:
                    return m.m22(value);
                case 3:
                    return m.m23(value);
                default:
                    break;
                }
                break;
            case 3:
                switch (row) {
                case 0:
                    return m.m30(value);
                case 1:
                    return m.m31(value);
                case 2:
                    return m.m32(value);
                case 3:
                    return m.m33(value);
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public double get(Matrix4d m, int column, int row) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00;
                case 1:
                    return m.m01;
                case 2:
                    return m.m02;
                case 3:
                    return m.m03;
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10;
                case 1:
                    return m.m11;
                case 2:
                    return m.m12;
                case 3:
                    return m.m13;
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20;
                case 1:
                    return m.m21;
                case 2:
                    return m.m22;
                case 3:
                    return m.m23;
                default:
                    break;
                }
                break;
            case 3:
                switch (row) {
                case 0:
                    return m.m30;
                case 1:
                    return m.m31;
                case 2:
                    return m.m32;
                case 3:
                    return m.m33;
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public Matrix4d set(Matrix4d m, int column, int row, double value) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00(value);
                case 1:
                    return m.m01(value);
                case 2:
                    return m.m02(value);
                case 3:
                    return m.m03(value);
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10(value);
                case 1:
                    return m.m11(value);
                case 2:
                    return m.m12(value);
                case 3:
                    return m.m13(value);
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20(value);
                case 1:
                    return m.m21(value);
                case 2:
                    return m.m22(value);
                case 3:
                    return m.m23(value);
                default:
                    break;
                }
                break;
            case 3:
                switch (row) {
                case 0:
                    return m.m30(value);
                case 1:
                    return m.m31(value);
                case 2:
                    return m.m32(value);
                case 3:
                    return m.m33(value);
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }
        
        public float get(Matrix3f m, int column, int row) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00;
                case 1:
                    return m.m01;
                case 2:
                    return m.m02;
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10;
                case 1:
                    return m.m11;
                case 2:
                    return m.m12;
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20;
                case 1:
                    return m.m21;
                case 2:
                    return m.m22;
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public Matrix3f set(Matrix3f m, int column, int row, float value) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00(value);
                case 1:
                    return m.m01(value);
                case 2:
                    return m.m02(value);
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10(value);
                case 1:
                    return m.m11(value);
                case 2:
                    return m.m12(value);
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20(value);
                case 1:
                    return m.m21(value);
                case 2:
                    return m.m22(value);
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public double get(Matrix3d m, int column, int row) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00;
                case 1:
                    return m.m01;
                case 2:
                    return m.m02;
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10;
                case 1:
                    return m.m11;
                case 2:
                    return m.m12;
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20;
                case 1:
                    return m.m21;
                case 2:
                    return m.m22;
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public Matrix3d set(Matrix3d m, int column, int row, double value) {
            switch (column) {
            case 0:
                switch (row) {
                case 0:
                    return m.m00(value);
                case 1:
                    return m.m01(value);
                case 2:
                    return m.m02(value);
                default:
                    break;
                }
                break;
            case 1:
                switch (row) {
                case 0:
                    return m.m10(value);
                case 1:
                    return m.m11(value);
                case 2:
                    return m.m12(value);
                default:
                    break;
                }
                break;
            case 2:
                switch (row) {
                case 0:
                    return m.m20(value);
                case 1:
                    return m.m21(value);
                case 2:
                    return m.m22(value);
                default:
                    break;
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException();
        }

        public Vector4f getColumn(Matrix4f m, int column, Vector4f dest) {
            switch (column) {
            case 0:
                return dest.set(m.m00, m.m01, m.m02, m.m03);
            case 1:
                return dest.set(m.m10, m.m11, m.m12, m.m13);
            case 2:
                return dest.set(m.m20, m.m21, m.m22, m.m23);
            case 3:
                return dest.set(m.m30, m.m31, m.m32, m.m33);
            default:
                throw new IndexOutOfBoundsException();
            }
        }

        public Matrix4f setColumn(Vector4f v, int column, Matrix4f dest) {
            switch (column) {
            case 0:
                return dest._m00(v.x)._m01(v.y)._m02(v.z)._m03(v.w);
            case 1:
                return dest._m10(v.x)._m11(v.y)._m12(v.z)._m13(v.w);
            case 2:
                return dest._m20(v.x)._m21(v.y)._m22(v.z)._m23(v.w);
            case 3:
                return dest._m30(v.x)._m31(v.y)._m32(v.z)._m33(v.w);
            default:
                throw new IndexOutOfBoundsException();
            }
        }

        public Matrix4f setColumn(Vector4fc v, int column, Matrix4f dest) {
            switch (column) {
            case 0:
                return dest._m00(v.x())._m01(v.y())._m02(v.z())._m03(v.w());
            case 1:
                return dest._m10(v.x())._m11(v.y())._m12(v.z())._m13(v.w());
            case 2:
                return dest._m20(v.x())._m21(v.y())._m22(v.z())._m23(v.w());
            case 3:
                return dest._m30(v.x())._m31(v.y())._m32(v.z())._m33(v.w());
            default:
                throw new IndexOutOfBoundsException();
            }
        }

        public void copy(Matrix4f src, Matrix4f dest) {
            dest._m00(src.m00()).
            _m01(src.m01()).
            _m02(src.m02()).
            _m03(src.m03()).
            _m10(src.m10()).
            _m11(src.m11()).
            _m12(src.m12()).
            _m13(src.m13()).
            _m20(src.m20()).
            _m21(src.m21()).
            _m22(src.m22()).
            _m23(src.m23()).
            _m30(src.m30()).
            _m31(src.m31()).
            _m32(src.m32()).
            _m33(src.m33());
        }

        public void copy(Matrix3f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m03(0.0f)
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m13(0.0f)
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m23(0.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f)
            ._m33(1.0f);
        }

        public void copy(Matrix4f src, Matrix3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy(Matrix3f src, Matrix4x3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f);
        }

        public void copy(Matrix3x2f src, Matrix3x2f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m20(src.m20())
            ._m21(src.m21());
        }

        public void copy(Matrix3x2d src, Matrix3x2d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m20(src.m20())
            ._m21(src.m21());
        }

        public void copy(Matrix2f src, Matrix2f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy(Matrix2d src, Matrix2d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy(Matrix2f src, Matrix3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(0.0f)
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(1.0f);
        }

        public void copy(Matrix3f src, Matrix2f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy(Matrix2f src, Matrix3x2f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m20(0.0f)
            ._m21(0.0f);
        }

        public void copy(Matrix3x2f src, Matrix2f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy(Matrix2d src, Matrix3d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(0.0)
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(0.0)
            ._m20(0.0)
            ._m21(0.0)
            ._m22(1.0);
        }

        public void copy(Matrix3d src, Matrix2d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy(Matrix2d src, Matrix3x2d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m20(0.0)
            ._m21(0.0);
        }

        public void copy(Matrix3x2d src, Matrix2d dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m10(src.m10())
            ._m11(src.m11());
        }

        public void copy3x3(Matrix4f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy3x3(Matrix4x3f src, Matrix4x3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy3x3(Matrix3f src, Matrix4x3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy3x3(Matrix3f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy4x3(Matrix4x3f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m30(src.m30())
            ._m31(src.m31())
            ._m32(src.m32());
        }

        public void copy4x3(Matrix4f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m30(src.m30())
            ._m31(src.m31())
            ._m32(src.m32());
        }

        public void copy(Matrix4f src, Matrix4x3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m30(src.m30())
            ._m31(src.m31())
            ._m32(src.m32());
        }

        public void copy(Matrix4x3f src, Matrix4f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m03(0.0f)
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m13(0.0f)
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m23(0.0f)
            ._m30(src.m30())
            ._m31(src.m31())
            ._m32(src.m32())
            ._m33(1.0f);
        }

        public void copy(Matrix4x3f src, Matrix4x3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22())
            ._m30(src.m30())
            ._m31(src.m31())
            ._m32(src.m32());
        }

        public void copy(Matrix3f src, Matrix3f dest) {
            dest._m00(src.m00())
            ._m01(src.m01())
            ._m02(src.m02())
            ._m10(src.m10())
            ._m11(src.m11())
            ._m12(src.m12())
            ._m20(src.m20())
            ._m21(src.m21())
            ._m22(src.m22());
        }

        public void copy(float[] arr, int off, Matrix4f dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m02(arr[off+2])
            ._m03(arr[off+3])
            ._m10(arr[off+4])
            ._m11(arr[off+5])
            ._m12(arr[off+6])
            ._m13(arr[off+7])
            ._m20(arr[off+8])
            ._m21(arr[off+9])
            ._m22(arr[off+10])
            ._m23(arr[off+11])
            ._m30(arr[off+12])
            ._m31(arr[off+13])
            ._m32(arr[off+14])
            ._m33(arr[off+15]);
        }

        public void copyTransposed(float[] arr, int off, Matrix4f dest) {
            dest._m00(arr[off+0])
            ._m10(arr[off+1])
            ._m20(arr[off+2])
            ._m30(arr[off+3])
            ._m01(arr[off+4])
            ._m11(arr[off+5])
            ._m21(arr[off+6])
            ._m31(arr[off+7])
            ._m02(arr[off+8])
            ._m12(arr[off+9])
            ._m22(arr[off+10])
            ._m32(arr[off+11])
            ._m03(arr[off+12])
            ._m13(arr[off+13])
            ._m23(arr[off+14])
            ._m33(arr[off+15]);
        }

        public void copy(float[] arr, int off, Matrix3f dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m02(arr[off+2])
            ._m10(arr[off+3])
            ._m11(arr[off+4])
            ._m12(arr[off+5])
            ._m20(arr[off+6])
            ._m21(arr[off+7])
            ._m22(arr[off+8]);
        }

        public void copy(float[] arr, int off, Matrix4x3f dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m02(arr[off+2])
            ._m10(arr[off+3])
            ._m11(arr[off+4])
            ._m12(arr[off+5])
            ._m20(arr[off+6])
            ._m21(arr[off+7])
            ._m22(arr[off+8])
            ._m30(arr[off+9])
            ._m31(arr[off+10])
            ._m32(arr[off+11]);
        }

        public void copy(float[] arr, int off, Matrix3x2f dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m10(arr[off+2])
            ._m11(arr[off+3])
            ._m20(arr[off+4])
            ._m21(arr[off+5]);
        }

        public void copy(double[] arr, int off, Matrix3x2d dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m10(arr[off+2])
            ._m11(arr[off+3])
            ._m20(arr[off+4])
            ._m21(arr[off+5]);
        }

        public void copy(float[] arr, int off, Matrix2f dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m10(arr[off+2])
            ._m11(arr[off+3]);
        }

        public void copy(double[] arr, int off, Matrix2d dest) {
            dest._m00(arr[off+0])
            ._m01(arr[off+1])
            ._m10(arr[off+2])
            ._m11(arr[off+3]);
        }

        public void copy(Matrix4f src, float[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = src.m02();
            dest[off+3]  = src.m03();
            dest[off+4]  = src.m10();
            dest[off+5]  = src.m11();
            dest[off+6]  = src.m12();
            dest[off+7]  = src.m13();
            dest[off+8]  = src.m20();
            dest[off+9]  = src.m21();
            dest[off+10] = src.m22();
            dest[off+11] = src.m23();
            dest[off+12] = src.m30();
            dest[off+13] = src.m31();
            dest[off+14] = src.m32();
            dest[off+15] = src.m33();
        }

        public void copy(Matrix3f src, float[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = src.m02();
            dest[off+3] = src.m10();
            dest[off+4] = src.m11();
            dest[off+5] = src.m12();
            dest[off+6] = src.m20();
            dest[off+7] = src.m21();
            dest[off+8] = src.m22();
        }

        public void copy(Matrix4x3f src, float[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = src.m02();
            dest[off+3]  = src.m10();
            dest[off+4]  = src.m11();
            dest[off+5]  = src.m12();
            dest[off+6]  = src.m20();
            dest[off+7]  = src.m21();
            dest[off+8]  = src.m22();
            dest[off+9]  = src.m30();
            dest[off+10] = src.m31();
            dest[off+11] = src.m32();
        }

        public void copy(Matrix3x2f src, float[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = src.m10();
            dest[off+3] = src.m11();
            dest[off+4] = src.m20();
            dest[off+5] = src.m21();
        }

        public void copy(Matrix3x2d src, double[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = src.m10();
            dest[off+3] = src.m11();
            dest[off+4] = src.m20();
            dest[off+5] = src.m21();
        }

        public void copy(Matrix2f src, float[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = src.m10();
            dest[off+3] = src.m11();
        }

        public void copy(Matrix2d src, double[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = src.m10();
            dest[off+3] = src.m11();
        }

        public void copy4x4(Matrix4x3f src, float[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = src.m02();
            dest[off+3]  = 0.0f;
            dest[off+4]  = src.m10();
            dest[off+5]  = src.m11();
            dest[off+6]  = src.m12();
            dest[off+7]  = 0.0f;
            dest[off+8]  = src.m20();
            dest[off+9]  = src.m21();
            dest[off+10] = src.m22();
            dest[off+11] = 0.0f;
            dest[off+12] = src.m30();
            dest[off+13] = src.m31();
            dest[off+14] = src.m32();
            dest[off+15] = 1.0f;
        }

        public void copy4x4(Matrix4x3d src, float[] dest, int off) {
            dest[off+0]  = (float) src.m00();
            dest[off+1]  = (float) src.m01();
            dest[off+2]  = (float) src.m02();
            dest[off+3]  = 0.0f;
            dest[off+4]  = (float) src.m10();
            dest[off+5]  = (float) src.m11();
            dest[off+6]  = (float) src.m12();
            dest[off+7]  = 0.0f;
            dest[off+8]  = (float) src.m20();
            dest[off+9]  = (float) src.m21();
            dest[off+10] = (float) src.m22();
            dest[off+11] = 0.0f;
            dest[off+12] = (float) src.m30();
            dest[off+13] = (float) src.m31();
            dest[off+14] = (float) src.m32();
            dest[off+15] = 1.0f;
        }

        public void copy4x4(Matrix4x3d src, double[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = src.m02();
            dest[off+3]  = 0.0;
            dest[off+4]  = src.m10();
            dest[off+5]  = src.m11();
            dest[off+6]  = src.m12();
            dest[off+7]  = 0.0;
            dest[off+8]  = src.m20();
            dest[off+9]  = src.m21();
            dest[off+10] = src.m22();
            dest[off+11] = 0.0;
            dest[off+12] = src.m30();
            dest[off+13] = src.m31();
            dest[off+14] = src.m32();
            dest[off+15] = 1.0;
        }

        public void copy3x3(Matrix3x2f src, float[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = 0.0f;
            dest[off+3] = src.m10();
            dest[off+4] = src.m11();
            dest[off+5] = 0.0f;
            dest[off+6] = src.m20();
            dest[off+7] = src.m21();
            dest[off+8] = 1.0f;
        }

        public void copy3x3(Matrix3x2d src, double[] dest, int off) {
            dest[off+0] = src.m00();
            dest[off+1] = src.m01();
            dest[off+2] = 0.0;
            dest[off+3] = src.m10();
            dest[off+4] = src.m11();
            dest[off+5] = 0.0;
            dest[off+6] = src.m20();
            dest[off+7] = src.m21();
            dest[off+8] = 1.0;
        }

        public void copy4x4(Matrix3x2f src, float[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = 0.0f;
            dest[off+3]  = 0.0f;
            dest[off+4]  = src.m10();
            dest[off+5]  = src.m11();
            dest[off+6]  = 0.0f;
            dest[off+7]  = 0.0f;
            dest[off+8]  = 0.0f;
            dest[off+9]  = 0.0f;
            dest[off+10] = 1.0f;
            dest[off+11] = 0.0f;
            dest[off+12] = src.m20();
            dest[off+13] = src.m21();
            dest[off+14] = 0.0f;
            dest[off+15] = 1.0f;
        }

        public void copy4x4(Matrix3x2d src, double[] dest, int off) {
            dest[off+0]  = src.m00();
            dest[off+1]  = src.m01();
            dest[off+2]  = 0.0;
            dest[off+3]  = 0.0;
            dest[off+4]  = src.m10();
            dest[off+5]  = src.m11();
            dest[off+6]  = 0.0;
            dest[off+7]  = 0.0;
            dest[off+8]  = 0.0;
            dest[off+9]  = 0.0;
            dest[off+10] = 1.0;
            dest[off+11] = 0.0;
            dest[off+12] = src.m20();
            dest[off+13] = src.m21();
            dest[off+14] = 0.0;
            dest[off+15] = 1.0;
        }

        public void identity(Matrix4f dest) {
            dest._m00(1.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m03(0.0f)
            ._m10(0.0f)
            ._m11(1.0f)
            ._m12(0.0f)
            ._m13(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(1.0f)
            ._m23(0.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f)
            ._m33(1.0f);
        }

        public void identity(Matrix4x3f dest) {
            dest._m00(1.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m10(0.0f)
            ._m11(1.0f)
            ._m12(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(1.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f);
        }

        public void identity(Matrix3f dest) {
            dest._m00(1.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m10(0.0f)
            ._m11(1.0f)
            ._m12(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(1.0f);
        }

        public void identity(Matrix3x2f dest) {
            dest._m00(1.0f)
            ._m01(0.0f)
            ._m10(0.0f)
            ._m11(1.0f)
            ._m20(0.0f)
            ._m21(0.0f);
        }

        public void identity(Matrix3x2d dest) {
            dest._m00(1.0)
            ._m01(0.0)
            ._m10(0.0)
            ._m11(1.0)
            ._m20(0.0)
            ._m21(0.0);
        }

        public void identity(Matrix2f dest) {
            dest._m00(1.0f)
            ._m01(0.0f)
            ._m10(0.0f)
            ._m11(1.0f);
        }

        public void swap(Matrix4f m1, Matrix4f m2) {
            float tmp;
            tmp = m1.m00(); m1._m00(m2.m00()); m2._m00(tmp);
            tmp = m1.m01(); m1._m01(m2.m01()); m2._m01(tmp);
            tmp = m1.m02(); m1._m02(m2.m02()); m2._m02(tmp);
            tmp = m1.m03(); m1._m03(m2.m03()); m2._m03(tmp);
            tmp = m1.m10(); m1._m10(m2.m10()); m2._m10(tmp);
            tmp = m1.m11(); m1._m11(m2.m11()); m2._m11(tmp);
            tmp = m1.m12(); m1._m12(m2.m12()); m2._m12(tmp);
            tmp = m1.m13(); m1._m13(m2.m13()); m2._m13(tmp);
            tmp = m1.m20(); m1._m20(m2.m20()); m2._m20(tmp);
            tmp = m1.m21(); m1._m21(m2.m21()); m2._m21(tmp);
            tmp = m1.m22(); m1._m22(m2.m22()); m2._m22(tmp);
            tmp = m1.m23(); m1._m23(m2.m23()); m2._m23(tmp);
            tmp = m1.m30(); m1._m30(m2.m30()); m2._m30(tmp);
            tmp = m1.m31(); m1._m31(m2.m31()); m2._m31(tmp);
            tmp = m1.m32(); m1._m32(m2.m32()); m2._m32(tmp);
            tmp = m1.m33(); m1._m33(m2.m33()); m2._m33(tmp);
        }

        public void swap(Matrix4x3f m1, Matrix4x3f m2) {
            float tmp;
            tmp = m1.m00(); m1._m00(m2.m00()); m2._m00(tmp);
            tmp = m1.m01(); m1._m01(m2.m01()); m2._m01(tmp);
            tmp = m1.m02(); m1._m02(m2.m02()); m2._m02(tmp);
            tmp = m1.m10(); m1._m10(m2.m10()); m2._m10(tmp);
            tmp = m1.m11(); m1._m11(m2.m11()); m2._m11(tmp);
            tmp = m1.m12(); m1._m12(m2.m12()); m2._m12(tmp);
            tmp = m1.m20(); m1._m20(m2.m20()); m2._m20(tmp);
            tmp = m1.m21(); m1._m21(m2.m21()); m2._m21(tmp);
            tmp = m1.m22(); m1._m22(m2.m22()); m2._m22(tmp);
            tmp = m1.m30(); m1._m30(m2.m30()); m2._m30(tmp);
            tmp = m1.m31(); m1._m31(m2.m31()); m2._m31(tmp);
            tmp = m1.m32(); m1._m32(m2.m32()); m2._m32(tmp);
        }
        
        public void swap(Matrix3f m1, Matrix3f m2) {
            float tmp;
            tmp = m1.m00(); m1._m00(m2.m00()); m2._m00(tmp);
            tmp = m1.m01(); m1._m01(m2.m01()); m2._m01(tmp);
            tmp = m1.m02(); m1._m02(m2.m02()); m2._m02(tmp);
            tmp = m1.m10(); m1._m10(m2.m10()); m2._m10(tmp);
            tmp = m1.m11(); m1._m11(m2.m11()); m2._m11(tmp);
            tmp = m1.m12(); m1._m12(m2.m12()); m2._m12(tmp);
            tmp = m1.m20(); m1._m20(m2.m20()); m2._m20(tmp);
            tmp = m1.m21(); m1._m21(m2.m21()); m2._m21(tmp);
            tmp = m1.m22(); m1._m22(m2.m22()); m2._m22(tmp);
        }

        public void swap(Matrix2f m1, Matrix2f m2) {
            float tmp;
            tmp = m1.m00(); m1._m00(m2.m00()); m2._m00(tmp);
            tmp = m1.m01(); m1._m00(m2.m01()); m2._m01(tmp);
            tmp = m1.m10(); m1._m00(m2.m10()); m2._m10(tmp);
            tmp = m1.m11(); m1._m00(m2.m11()); m2._m11(tmp);
        }

        public void swap(Matrix2d m1, Matrix2d m2) {
            double tmp;
            tmp = m1.m00(); m1._m00(m2.m00()); m2._m00(tmp);
            tmp = m1.m01(); m1._m00(m2.m01()); m2._m01(tmp);
            tmp = m1.m10(); m1._m00(m2.m10()); m2._m10(tmp);
            tmp = m1.m11(); m1._m00(m2.m11()); m2._m11(tmp);
        }

        public void zero(Matrix4f dest) {
            dest._m00(0.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m03(0.0f)
            ._m10(0.0f)
            ._m11(0.0f)
            ._m12(0.0f)
            ._m13(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(0.0f)
            ._m23(0.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f)
            ._m33(0.0f);
        }

        public void zero(Matrix4x3f dest) {
            dest._m00(0.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m10(0.0f)
            ._m11(0.0f)
            ._m12(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(0.0f)
            ._m30(0.0f)
            ._m31(0.0f)
            ._m32(0.0f);
        }

        public void zero(Matrix3f dest) {
            dest._m00(0.0f)
            ._m01(0.0f)
            ._m02(0.0f)
            ._m10(0.0f)
            ._m11(0.0f)
            ._m12(0.0f)
            ._m20(0.0f)
            ._m21(0.0f)
            ._m22(0.0f);
        }

        public void zero(Matrix3x2f dest) {
            dest._m00(0.0f)
            ._m01(0.0f)
            ._m10(0.0f)
            ._m11(0.0f)
            ._m20(0.0f)
            ._m21(0.0f);
        }

        public void zero(Matrix3x2d dest) {
            dest._m00(0.0)
            ._m01(0.0)
            ._m10(0.0)
            ._m11(0.0)
            ._m20(0.0)
            ._m21(0.0);
        }

        public void zero(Matrix2f dest) {
            dest._m00(0.0f)
            ._m01(0.0f)
            ._m10(0.0f)
            ._m11(0.0f);
        }

        public void zero(Matrix2d dest) {
            dest._m00(0.0)
            ._m01(0.0)
            ._m10(0.0)
            ._m11(0.0);
        }

//#ifdef __HAS_NIO__
        public void putMatrix3f(Quaternionf q, int position, ByteBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.putFloat(position, w2 + x2 - z2 - y2)
            .putFloat(position + 4, xy + zw + zw + xy)
            .putFloat(position + 8, xz - yw + xz - yw)
            .putFloat(position + 12, -zw + xy - zw + xy)
            .putFloat(position + 16, y2 - z2 + w2 - x2)
            .putFloat(position + 20, yz + yz + xw + xw)
            .putFloat(position + 24, yw + xz + xz + yw)
            .putFloat(position + 28, yz + yz - xw - xw)
            .putFloat(position + 32, z2 - y2 - x2 + w2);
        }

        public void putMatrix3f(Quaternionf q, int position, FloatBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.put(position, w2 + x2 - z2 - y2)
            .put(position + 1, xy + zw + zw + xy)
            .put(position + 2, xz - yw + xz - yw)
            .put(position + 3, -zw + xy - zw + xy)
            .put(position + 4, y2 - z2 + w2 - x2)
            .put(position + 5, yz + yz + xw + xw)
            .put(position + 6, yw + xz + xz + yw)
            .put(position + 7, yz + yz - xw - xw)
            .put(position + 8, z2 - y2 - x2 + w2);
        }

        public void putMatrix4f(Quaternionf q, int position, ByteBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.putFloat(position, w2 + x2 - z2 - y2)
            .putFloat(position + 4, xy + zw + zw + xy)
            .putFloat(position + 8, xz - yw + xz - yw)
            .putFloat(position + 12, 0.0f)
            .putFloat(position + 16, -zw + xy - zw + xy)
            .putFloat(position + 20, y2 - z2 + w2 - x2)
            .putFloat(position + 24, yz + yz + xw + xw)
            .putFloat(position + 28, 0.0f)
            .putFloat(position + 32, yw + xz + xz + yw)
            .putFloat(position + 36, yz + yz - xw - xw)
            .putFloat(position + 40, z2 - y2 - x2 + w2)
            .putFloat(position + 44, 0.0f)
            .putLong(position + 48, 0L)
            .putLong(position + 56, 0x3F80000000000000L);
        }

        public void putMatrix4f(Quaternionf q, int position, FloatBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.put(position, w2 + x2 - z2 - y2)
            .put(position + 1, xy + zw + zw + xy)
            .put(position + 2, xz - yw + xz - yw)
            .put(position + 3, 0.0f)
            .put(position + 4, -zw + xy - zw + xy)
            .put(position + 5, y2 - z2 + w2 - x2)
            .put(position + 6, yz + yz + xw + xw)
            .put(position + 7, 0.0f)
            .put(position + 8, yw + xz + xz + yw)
            .put(position + 9, yz + yz - xw - xw)
            .put(position + 10, z2 - y2 - x2 + w2)
            .put(position + 11, 0.0f)
            .put(position + 12, 0.0f)
            .put(position + 13, 0.0f)
            .put(position + 14, 0.0f)
            .put(position + 15, 1.0f);
        }

        public void putMatrix4x3f(Quaternionf q, int position, ByteBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.putFloat(position, w2 + x2 - z2 - y2)
            .putFloat(position + 4, xy + zw + zw + xy)
            .putFloat(position + 8, xz - yw + xz - yw)
            .putFloat(position + 12, -zw + xy - zw + xy)
            .putFloat(position + 16, y2 - z2 + w2 - x2)
            .putFloat(position + 20, yz + yz + xw + xw)
            .putFloat(position + 24, yw + xz + xz + yw)
            .putFloat(position + 28, yz + yz - xw - xw)
            .putFloat(position + 32, z2 - y2 - x2 + w2)
            .putLong(position + 36, 0L)
            .putFloat(position + 44, 0.0f);
        }

        public void putMatrix4x3f(Quaternionf q, int position, FloatBuffer dest) {
            float w2 = q.w * q.w;
            float x2 = q.x * q.x;
            float y2 = q.y * q.y;
            float z2 = q.z * q.z;
            float zw = q.z * q.w;
            float xy = q.x * q.y;
            float xz = q.x * q.z;
            float yw = q.y * q.w;
            float yz = q.y * q.z;
            float xw = q.x * q.w;
            dest.put(position, w2 + x2 - z2 - y2)
            .put(position + 1, xy + zw + zw + xy)
            .put(position + 2, xz - yw + xz - yw)
            .put(position + 3, -zw + xy - zw + xy)
            .put(position + 4, y2 - z2 + w2 - x2)
            .put(position + 5, yz + yz + xw + xw)
            .put(position + 6, yw + xz + xz + yw)
            .put(position + 7, yz + yz - xw - xw)
            .put(position + 8, z2 - y2 - x2 + w2)
            .put(position + 9, 0.0f)
            .put(position + 10, 0.0f)
            .put(position + 11, 0.0f);
        }
//#endif
    }

//#ifdef __HAS_UNSAFE__
    public static class MemUtilUnsafe extends MemUtilNIO {
        public static final sun.misc.Unsafe UNSAFE;

//#ifdef __HAS_NIO__
        public static final long ADDRESS;
//#endif
        public static final long Matrix2f_m00;
        public static final long Matrix3f_m00;
        public static final long Matrix3d_m00;
        public static final long Matrix4f_m00;
        public static final long Matrix4d_m00;
        public static final long Matrix4x3f_m00;
        public static final long Matrix3x2f_m00;
        public static final long Vector4f_x;
        public static final long Vector4i_x;
        public static final long Vector3f_x;
        public static final long Vector3i_x;
        public static final long Vector2f_x;
        public static final long Vector2i_x;
        public static final long Quaternionf_x;
        public static final long floatArrayOffset;

        static {
            UNSAFE = getUnsafeInstance();
            try {
//#ifdef __HAS_NIO__
                ADDRESS = findBufferAddress();
//#endif
                Matrix4f_m00 = checkMatrix4f();
                Matrix4d_m00 = checkMatrix4d();
                Matrix4x3f_m00 = checkMatrix4x3f();
                Matrix3f_m00 = checkMatrix3f();
                Matrix3d_m00 = checkMatrix3d();
                Matrix3x2f_m00 = checkMatrix3x2f();
                Matrix2f_m00 = checkMatrix2f();
                Vector4f_x = checkVector4f();
                Vector4i_x = checkVector4i();
                Vector3f_x = checkVector3f();
                Vector3i_x = checkVector3i();
                Vector2f_x = checkVector2f();
                Vector2i_x = checkVector2i();
                Quaternionf_x = checkQuaternionf();
                floatArrayOffset = UNSAFE.arrayBaseOffset(float[].class);
                // Check if we can use object field offset/address put/get methods
                sun.misc.Unsafe.class.getDeclaredMethod("getLong", new Class[] {Object.class, long.class});
                sun.misc.Unsafe.class.getDeclaredMethod("putLong", new Class[] {Object.class, long.class, long.class});
            } catch (NoSuchFieldException e) {
                throw new UnsupportedOperationException(e);
            } catch (NoSuchMethodException e) {
                throw new UnsupportedOperationException(e);
            }
        }

//#ifdef __HAS_NIO__
        private static long findBufferAddress() {
            try {
                return UNSAFE.objectFieldOffset(getDeclaredField(Buffer.class, "address")); //$NON-NLS-1$
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
        }
//#endif

        private static long checkMatrix4f() throws NoSuchFieldException, SecurityException {
            Field f = Matrix4f.class.getDeclaredField("m00");
            long Matrix4f_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 16; i++) {
                int c = i >>> 2;
                int r = i & 3;
                f = Matrix4f.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix4f_m00 + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Matrix4f element offset");
            }
            return Matrix4f_m00;
        }

        private static long checkMatrix4d() throws NoSuchFieldException, SecurityException {
            Field f = Matrix4d.class.getDeclaredField("m00");
            long Matrix4d_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 16; i++) {
                int c = i >>> 2;
                int r = i & 3;
                f = Matrix4d.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix4d_m00 + (i << 3))
                    throw new UnsupportedOperationException("Unexpected Matrix4d element offset");
            }
            return Matrix4d_m00;
        }

        private static long checkMatrix4x3f() throws NoSuchFieldException, SecurityException {
            Field f = Matrix4x3f.class.getDeclaredField("m00");
            long Matrix4x3f_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 12; i++) {
                int c = i / 3;
                int r = i % 3;
                f = Matrix4x3f.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix4x3f_m00 + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Matrix4x3f element offset");
            }
            return Matrix4x3f_m00;
        }

        private static long checkMatrix3f() throws NoSuchFieldException, SecurityException {
            Field f = Matrix3f.class.getDeclaredField("m00");
            long Matrix3f_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 9; i++) {
                int c = i / 3;
                int r = i % 3;
                f = Matrix3f.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix3f_m00 + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Matrix3f element offset");
            }
            return Matrix3f_m00;
        }

        private static long checkMatrix3d() throws NoSuchFieldException, SecurityException {
            Field f = Matrix3d.class.getDeclaredField("m00");
            long Matrix3d_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 9; i++) {
                int c = i / 3;
                int r = i % 3;
                f = Matrix3d.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix3d_m00 + (i << 3))
                    throw new UnsupportedOperationException("Unexpected Matrix3d element offset");
            }
            return Matrix3d_m00;
        }

        private static long checkMatrix3x2f() throws NoSuchFieldException, SecurityException {
            Field f = Matrix3x2f.class.getDeclaredField("m00");
            long Matrix3x2f_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 6; i++) {
                int c = i / 2;
                int r = i % 2;
                f = Matrix3x2f.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix3x2f_m00 + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Matrix3x2f element offset");
            }
            return Matrix3x2f_m00;
        }

        private static long checkMatrix2f() throws NoSuchFieldException, SecurityException {
            Field f = Matrix2f.class.getDeclaredField("m00");
            long Matrix2f_m00 = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            for (int i = 1; i < 4; i++) {
                int c = i / 2;
                int r = i % 2;
                f = Matrix2f.class.getDeclaredField("m" + c + r);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Matrix2f_m00 + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Matrix2f element offset");
            }
            return Matrix2f_m00;
        }

        private static long checkVector4f() throws NoSuchFieldException, SecurityException {
            Field f = Vector4f.class.getDeclaredField("x");
            long Vector4f_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            String[] names = {"y", "z", "w"};
            for (int i = 1; i < 4; i++) {
                f = Vector4f.class.getDeclaredField(names[i-1]);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Vector4f_x + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Vector4f element offset");
            }
            return Vector4f_x;
        }

        private static long checkVector4i() throws NoSuchFieldException, SecurityException {
            Field f = Vector4i.class.getDeclaredField("x");
            long Vector4i_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            String[] names = {"y", "z", "w"};
            for (int i = 1; i < 4; i++) {
                f = Vector4i.class.getDeclaredField(names[i-1]);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Vector4i_x + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Vector4i element offset");
            }
            return Vector4i_x;
        }

        private static long checkVector3f() throws NoSuchFieldException, SecurityException {
            Field f = Vector3f.class.getDeclaredField("x");
            long Vector3f_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            String[] names = {"y", "z"};
            for (int i = 1; i < 3; i++) {
                f = Vector3f.class.getDeclaredField(names[i-1]);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Vector3f_x + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Vector3f element offset");
            }
            return Vector3f_x;
        }

        private static long checkVector3i() throws NoSuchFieldException, SecurityException {
            Field f = Vector3i.class.getDeclaredField("x");
            long Vector3i_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            String[] names = {"y", "z"};
            for (int i = 1; i < 3; i++) {
                f = Vector3i.class.getDeclaredField(names[i-1]);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Vector3i_x + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Vector3i element offset");
            }
            return Vector3i_x;
        }

        private static long checkVector2f() throws NoSuchFieldException, SecurityException {
            Field f = Vector2f.class.getDeclaredField("x");
            long Vector2f_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            f = Vector2f.class.getDeclaredField("y");
            long offset = UNSAFE.objectFieldOffset(f);
            if (offset != Vector2f_x + (1 << 2))
                throw new UnsupportedOperationException("Unexpected Vector2f element offset");
            return Vector2f_x;
        }

        private static long checkVector2i() throws NoSuchFieldException, SecurityException {
            Field f = Vector2i.class.getDeclaredField("x");
            long Vector2i_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            f = Vector2i.class.getDeclaredField("y");
            long offset = UNSAFE.objectFieldOffset(f);
            if (offset != Vector2i_x + (1 << 2))
                throw new UnsupportedOperationException("Unexpected Vector2i element offset");
            return Vector2i_x;
        }

        private static long checkQuaternionf() throws NoSuchFieldException, SecurityException {
            Field f = Quaternionf.class.getDeclaredField("x");
            long Quaternionf_x = UNSAFE.objectFieldOffset(f);
            // Validate expected field offsets
            String[] names = {"y", "z", "w"};
            for (int i = 1; i < 4; i++) {
                f = Quaternionf.class.getDeclaredField(names[i-1]);
                long offset = UNSAFE.objectFieldOffset(f);
                if (offset != Quaternionf_x + (i << 2))
                    throw new UnsupportedOperationException("Unexpected Quaternionf element offset");
            }
            return Quaternionf_x;
        }

        private static Field getDeclaredField(Class root, String fieldName) throws NoSuchFieldException {
            Class type = root;
            do {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    return field;
                } catch (NoSuchFieldException e) {
                    type = type.getSuperclass();
                } catch (SecurityException e) {
                    type = type.getSuperclass();
                }
            } while (type != null);
            throw new NoSuchFieldException(fieldName + " does not exist in " + root.getName() + " or any of its superclasses."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public static sun.misc.Unsafe getUnsafeInstance() throws SecurityException {
            Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (!field.getType().equals(sun.misc.Unsafe.class))
                    continue;
                int modifiers = field.getModifiers();
                if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers)))
                    continue;
                field.setAccessible(true);
                try {
                    return (sun.misc.Unsafe) field.get(null);
                } catch (IllegalAccessException e) {
                    /* Ignore */
                }
                break;
            }
            throw new UnsupportedOperationException();
        }

        public static void put(Matrix4f m, long destAddr) {
            for (int i = 0; i < 8; i++) {
                UNSAFE.putLong(null, destAddr + (i << 3), UNSAFE.getLong(m, Matrix4f_m00 + (i << 3)));
            }
        }

        public static void put4x3(Matrix4f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            for (int i = 0; i < 4; i++) {
                u.putLong(null, destAddr + 12 * i, u.getLong(m, Matrix4f_m00 + (i << 4)));
            }
            u.putFloat(null, destAddr +  8, m.m02());
            u.putFloat(null, destAddr + 20, m.m12());
            u.putFloat(null, destAddr + 32, m.m22());
            u.putFloat(null, destAddr + 44, m.m32());
        }

        public static void put3x4(Matrix4f m, long destAddr) {
            for (int i = 0; i < 6; i++) {
                UNSAFE.putLong(null, destAddr + (i << 3), UNSAFE.getLong(m, Matrix4f_m00 + (i << 3)));
            }
        }

        public static void put(Matrix4x3f m, long destAddr) {
            for (int i = 0; i < 6; i++) {
                UNSAFE.putLong(null, destAddr + (i << 3), UNSAFE.getLong(m, Matrix4x3f_m00 + (i << 3)));
            }
        }

        public static void put4x4(Matrix4x3f m, long destAddr) {
            for (int i = 0; i < 4; i++) {
                UNSAFE.putLong(null, destAddr + (i << 4), UNSAFE.getLong(m, Matrix4x3f_m00 + 12 * i));
                long lng = UNSAFE.getInt(m, Matrix4x3f_m00 + 8 + 12 * i) & 0xFFFFFFFFL;
                UNSAFE.putLong(null, destAddr + 8 + (i << 4), lng);
            }
            UNSAFE.putFloat(null, destAddr + 60, 1.0f);
        }

        public static void put3x4(Matrix4x3f m, long destAddr) {
            for (int i = 0; i < 3; i++) {
                UNSAFE.putLong(null, destAddr + (i << 4), UNSAFE.getLong(m, Matrix4x3f_m00 + 12 * i));
                UNSAFE.putFloat(null, destAddr + (i << 4) + 8, UNSAFE.getFloat(m, Matrix4x3f_m00 + 8 + 12 * i));
                UNSAFE.putFloat(null, destAddr + (i << 4) + 12, 0.0f);
            }
        }

        public static void put4x4(Matrix4x3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,       m.m00());
            u.putDouble(null, destAddr + 8,   m.m01());
            u.putDouble(null, destAddr + 16,  m.m02());
            u.putDouble(null, destAddr + 24,  0.0);
            u.putDouble(null, destAddr + 32,  m.m10());
            u.putDouble(null, destAddr + 40,  m.m11());
            u.putDouble(null, destAddr + 48,  m.m12());
            u.putDouble(null, destAddr + 56,  0.0);
            u.putDouble(null, destAddr + 64,  m.m20());
            u.putDouble(null, destAddr + 72,  m.m21());
            u.putDouble(null, destAddr + 80,  m.m22());
            u.putDouble(null, destAddr + 88,  0.0);
            u.putDouble(null, destAddr + 96,  m.m30());
            u.putDouble(null, destAddr + 104, m.m31());
            u.putDouble(null, destAddr + 112, m.m32());
            u.putDouble(null, destAddr + 120, 1.0);
        }

        public static void put4x4(Matrix3x2f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putLong(null, destAddr,    u.getLong(m, Matrix3x2f_m00));
            u.putLong(null, destAddr+8,  0L);
            u.putLong(null, destAddr+16, u.getLong(m, Matrix3x2f_m00+8));
            u.putLong(null, destAddr+24, 0L);
            u.putLong(null, destAddr+32, 0L);
            u.putLong(null, destAddr+40, 0x3F800000L);
            u.putLong(null, destAddr+48, u.getLong(m, Matrix3x2f_m00+16));
            u.putLong(null, destAddr+56, 0x3F80000000000000L);
        }

        public static void put4x4(Matrix3x2d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,       m.m00());
            u.putDouble(null, destAddr + 8,   m.m01());
            u.putDouble(null, destAddr + 16,  0.0);
            u.putDouble(null, destAddr + 24,  0.0);
            u.putDouble(null, destAddr + 32,  m.m10());
            u.putDouble(null, destAddr + 40,  m.m11());
            u.putDouble(null, destAddr + 48,  0.0);
            u.putDouble(null, destAddr + 56,  0.0);
            u.putDouble(null, destAddr + 64,  0.0);
            u.putDouble(null, destAddr + 72,  0.0);
            u.putDouble(null, destAddr + 80,  1.0);
            u.putDouble(null, destAddr + 88,  0.0);
            u.putDouble(null, destAddr + 96,  m.m20());
            u.putDouble(null, destAddr + 104, m.m21());
            u.putDouble(null, destAddr + 112, 0.0);
            u.putDouble(null, destAddr + 120, 1.0);
        }

        public static void put3x3(Matrix3x2f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putLong( null, destAddr,    u.getLong(m, Matrix3x2f_m00));
            u.putInt(  null, destAddr+8,  0);
            u.putLong( null, destAddr+12, u.getLong(m, Matrix3x2f_m00+8));
            u.putInt(  null, destAddr+20, 0);
            u.putLong( null, destAddr+24, u.getLong(m, Matrix3x2f_m00+16));
            u.putFloat(null, destAddr+32, 1.0f);
        }

        public static void put3x3(Matrix3x2d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m01());
            u.putDouble(null, destAddr + 16, 0.0);
            u.putDouble(null, destAddr + 24, m.m10());
            u.putDouble(null, destAddr + 32, m.m11());
            u.putDouble(null, destAddr + 40, 0.0);
            u.putDouble(null, destAddr + 48, m.m20());
            u.putDouble(null, destAddr + 56, m.m21());
            u.putDouble(null, destAddr + 64, 1.0);
        }

        public static void putTransposed(Matrix4f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      m.m00());
            u.putFloat(null, destAddr + 4,  m.m10());
            u.putFloat(null, destAddr + 8,  m.m20());
            u.putFloat(null, destAddr + 12, m.m30());
            u.putFloat(null, destAddr + 16, m.m01());
            u.putFloat(null, destAddr + 20, m.m11());
            u.putFloat(null, destAddr + 24, m.m21());
            u.putFloat(null, destAddr + 28, m.m31());
            u.putFloat(null, destAddr + 32, m.m02());
            u.putFloat(null, destAddr + 36, m.m12());
            u.putFloat(null, destAddr + 40, m.m22());
            u.putFloat(null, destAddr + 44, m.m32());
            u.putFloat(null, destAddr + 48, m.m03());
            u.putFloat(null, destAddr + 52, m.m13());
            u.putFloat(null, destAddr + 56, m.m23());
            u.putFloat(null, destAddr + 60, m.m33());
        }

        public static void put4x3Transposed(Matrix4f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      m.m00());
            u.putFloat(null, destAddr + 4,  m.m10());
            u.putFloat(null, destAddr + 8,  m.m20());
            u.putFloat(null, destAddr + 12, m.m30());
            u.putFloat(null, destAddr + 16, m.m01());
            u.putFloat(null, destAddr + 20, m.m11());
            u.putFloat(null, destAddr + 24, m.m21());
            u.putFloat(null, destAddr + 28, m.m31());
            u.putFloat(null, destAddr + 32, m.m02());
            u.putFloat(null, destAddr + 36, m.m12());
            u.putFloat(null, destAddr + 40, m.m22());
            u.putFloat(null, destAddr + 44, m.m32());
        }

        public static void putTransposed(Matrix4x3f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      m.m00());
            u.putFloat(null, destAddr + 4,  m.m10());
            u.putFloat(null, destAddr + 8,  m.m20());
            u.putFloat(null, destAddr + 12, m.m30());
            u.putFloat(null, destAddr + 16, m.m01());
            u.putFloat(null, destAddr + 20, m.m11());
            u.putFloat(null, destAddr + 24, m.m21());
            u.putFloat(null, destAddr + 28, m.m31());
            u.putFloat(null, destAddr + 32, m.m02());
            u.putFloat(null, destAddr + 36, m.m12());
            u.putFloat(null, destAddr + 40, m.m22());
            u.putFloat(null, destAddr + 44, m.m32());
        }

        public static void putTransposed(Matrix3f m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      m.m00());
            u.putFloat(null, destAddr + 4,  m.m10());
            u.putFloat(null, destAddr + 8,  m.m20());
            u.putFloat(null, destAddr + 12, m.m01());
            u.putFloat(null, destAddr + 16, m.m11());
            u.putFloat(null, destAddr + 20, m.m21());
            u.putFloat(null, destAddr + 24, m.m02());
            u.putFloat(null, destAddr + 28, m.m12());
            u.putFloat(null, destAddr + 32, m.m22());
        }

        public static void putTransposed(Matrix2f m, long destAddr) {
            UNSAFE.putFloat(null, destAddr,      m.m00());
            UNSAFE.putFloat(null, destAddr + 4,  m.m10());
            UNSAFE.putFloat(null, destAddr + 8,  m.m01());
            UNSAFE.putFloat(null, destAddr + 12, m.m11());
        }

        public static void put(Matrix4d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,       m.m00());
            u.putDouble(null, destAddr + 8,   m.m01());
            u.putDouble(null, destAddr + 16,  m.m02());
            u.putDouble(null, destAddr + 24,  m.m03());
            u.putDouble(null, destAddr + 32,  m.m10());
            u.putDouble(null, destAddr + 40,  m.m11());
            u.putDouble(null, destAddr + 48,  m.m12());
            u.putDouble(null, destAddr + 56,  m.m13());
            u.putDouble(null, destAddr + 64,  m.m20());
            u.putDouble(null, destAddr + 72,  m.m21());
            u.putDouble(null, destAddr + 80,  m.m22());
            u.putDouble(null, destAddr + 88,  m.m23());
            u.putDouble(null, destAddr + 96,  m.m30());
            u.putDouble(null, destAddr + 104, m.m31());
            u.putDouble(null, destAddr + 112, m.m32());
            u.putDouble(null, destAddr + 120, m.m33());
        }

        public static void put(Matrix4x3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m01());
            u.putDouble(null, destAddr + 16, m.m02());
            u.putDouble(null, destAddr + 24, m.m10());
            u.putDouble(null, destAddr + 32, m.m11());
            u.putDouble(null, destAddr + 40, m.m12());
            u.putDouble(null, destAddr + 48, m.m20());
            u.putDouble(null, destAddr + 56, m.m21());
            u.putDouble(null, destAddr + 64, m.m22());
            u.putDouble(null, destAddr + 72, m.m30());
            u.putDouble(null, destAddr + 80, m.m31());
            u.putDouble(null, destAddr + 88, m.m32());
        }

        public static void putTransposed(Matrix4d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,       m.m00());
            u.putDouble(null, destAddr + 8,   m.m10());
            u.putDouble(null, destAddr + 16,  m.m20());
            u.putDouble(null, destAddr + 24,  m.m30());
            u.putDouble(null, destAddr + 32,  m.m01());
            u.putDouble(null, destAddr + 40,  m.m11());
            u.putDouble(null, destAddr + 48,  m.m21());
            u.putDouble(null, destAddr + 56,  m.m31());
            u.putDouble(null, destAddr + 64,  m.m02());
            u.putDouble(null, destAddr + 72,  m.m12());
            u.putDouble(null, destAddr + 80,  m.m22());
            u.putDouble(null, destAddr + 88,  m.m32());
            u.putDouble(null, destAddr + 96,  m.m03());
            u.putDouble(null, destAddr + 104, m.m13());
            u.putDouble(null, destAddr + 112, m.m23());
            u.putDouble(null, destAddr + 120, m.m33());
        }

        public static void putfTransposed(Matrix4d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      (float)m.m00());
            u.putFloat(null, destAddr + 4,  (float)m.m10());
            u.putFloat(null, destAddr + 8,  (float)m.m20());
            u.putFloat(null, destAddr + 12, (float)m.m30());
            u.putFloat(null, destAddr + 16, (float)m.m01());
            u.putFloat(null, destAddr + 20, (float)m.m11());
            u.putFloat(null, destAddr + 24, (float)m.m21());
            u.putFloat(null, destAddr + 28, (float)m.m31());
            u.putFloat(null, destAddr + 32, (float)m.m02());
            u.putFloat(null, destAddr + 36, (float)m.m12());
            u.putFloat(null, destAddr + 40, (float)m.m22());
            u.putFloat(null, destAddr + 44, (float)m.m32());
            u.putFloat(null, destAddr + 48, (float)m.m03());
            u.putFloat(null, destAddr + 52, (float)m.m13());
            u.putFloat(null, destAddr + 56, (float)m.m23());
            u.putFloat(null, destAddr + 60, (float)m.m33());
        }

        public static void put4x3Transposed(Matrix4d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m10());
            u.putDouble(null, destAddr + 16, m.m20());
            u.putDouble(null, destAddr + 24, m.m30());
            u.putDouble(null, destAddr + 32, m.m01());
            u.putDouble(null, destAddr + 40, m.m11());
            u.putDouble(null, destAddr + 48, m.m21());
            u.putDouble(null, destAddr + 56, m.m31());
            u.putDouble(null, destAddr + 64, m.m02());
            u.putDouble(null, destAddr + 72, m.m12());
            u.putDouble(null, destAddr + 80, m.m22());
            u.putDouble(null, destAddr + 88, m.m32());
        }

        public static void putTransposed(Matrix4x3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m10());
            u.putDouble(null, destAddr + 16, m.m20());
            u.putDouble(null, destAddr + 24, m.m30());
            u.putDouble(null, destAddr + 32, m.m01());
            u.putDouble(null, destAddr + 40, m.m11());
            u.putDouble(null, destAddr + 48, m.m21());
            u.putDouble(null, destAddr + 56, m.m31());
            u.putDouble(null, destAddr + 64, m.m02());
            u.putDouble(null, destAddr + 72, m.m12());
            u.putDouble(null, destAddr + 80, m.m22());
            u.putDouble(null, destAddr + 88, m.m32());
        }

        public static void putTransposed(Matrix2d m, long destAddr) {
            UNSAFE.putDouble(null, destAddr,      m.m00());
            UNSAFE.putDouble(null, destAddr + 8,  m.m10());
            UNSAFE.putDouble(null, destAddr + 16, m.m10());
            UNSAFE.putDouble(null, destAddr + 24, m.m10());
        }

        public static void putfTransposed(Matrix4x3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      (float)m.m00());
            u.putFloat(null, destAddr + 4,  (float)m.m10());
            u.putFloat(null, destAddr + 8,  (float)m.m20());
            u.putFloat(null, destAddr + 12, (float)m.m30());
            u.putFloat(null, destAddr + 16, (float)m.m01());
            u.putFloat(null, destAddr + 20, (float)m.m11());
            u.putFloat(null, destAddr + 24, (float)m.m21());
            u.putFloat(null, destAddr + 28, (float)m.m31());
            u.putFloat(null, destAddr + 32, (float)m.m02());
            u.putFloat(null, destAddr + 36, (float)m.m12());
            u.putFloat(null, destAddr + 40, (float)m.m22());
            u.putFloat(null, destAddr + 44, (float)m.m32());
        }

        public static void putfTransposed(Matrix2d m, long destAddr) {
            UNSAFE.putFloat(null, destAddr,      (float)m.m00());
            UNSAFE.putFloat(null, destAddr + 4,  (float)m.m00());
            UNSAFE.putFloat(null, destAddr + 8,  (float)m.m00());
            UNSAFE.putFloat(null, destAddr + 12, (float)m.m00());
        }

        public static void putf(Matrix4d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      (float)m.m00());
            u.putFloat(null, destAddr + 4,  (float)m.m01());
            u.putFloat(null, destAddr + 8,  (float)m.m02());
            u.putFloat(null, destAddr + 12, (float)m.m03());
            u.putFloat(null, destAddr + 16, (float)m.m10());
            u.putFloat(null, destAddr + 20, (float)m.m11());
            u.putFloat(null, destAddr + 24, (float)m.m12());
            u.putFloat(null, destAddr + 28, (float)m.m13());
            u.putFloat(null, destAddr + 32, (float)m.m20());
            u.putFloat(null, destAddr + 36, (float)m.m21());
            u.putFloat(null, destAddr + 40, (float)m.m22());
            u.putFloat(null, destAddr + 44, (float)m.m23());
            u.putFloat(null, destAddr + 48, (float)m.m30());
            u.putFloat(null, destAddr + 52, (float)m.m31());
            u.putFloat(null, destAddr + 56, (float)m.m32());
            u.putFloat(null, destAddr + 60, (float)m.m33());
        }

        public static void putf(Matrix4x3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      (float)m.m00());
            u.putFloat(null, destAddr + 4,  (float)m.m01());
            u.putFloat(null, destAddr + 8,  (float)m.m02());
            u.putFloat(null, destAddr + 12, (float)m.m10());
            u.putFloat(null, destAddr + 16, (float)m.m11());
            u.putFloat(null, destAddr + 20, (float)m.m12());
            u.putFloat(null, destAddr + 24, (float)m.m20());
            u.putFloat(null, destAddr + 28, (float)m.m21());
            u.putFloat(null, destAddr + 32, (float)m.m22());
            u.putFloat(null, destAddr + 36, (float)m.m30());
            u.putFloat(null, destAddr + 40, (float)m.m31());
            u.putFloat(null, destAddr + 44, (float)m.m32());
        }

        public static void put(Matrix3f m, long destAddr) {
            for (int i = 0; i < 4; i++) {
                UNSAFE.putLong(null, destAddr + (i << 3), UNSAFE.getLong(m, Matrix3f_m00 + (i << 3)));
            }
            UNSAFE.putFloat(null, destAddr + 32, m.m22());
        }

        public static void put3x4(Matrix3f m, long destAddr) {
            for (int i = 0; i < 3; i++) {
                UNSAFE.putLong(null, destAddr + (i << 4), UNSAFE.getLong(m, Matrix3f_m00 + 12 * i));
                UNSAFE.putFloat(null, destAddr + (i << 4) + 8, UNSAFE.getFloat(m, Matrix3f_m00 + 8 + 12 * i));
                UNSAFE.putFloat(null, destAddr + 12 * i, 0.0f);
            }
        }

        public static void put(Matrix3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m01());
            u.putDouble(null, destAddr + 16, m.m02());
            u.putDouble(null, destAddr + 24, m.m10());
            u.putDouble(null, destAddr + 32, m.m11());
            u.putDouble(null, destAddr + 40, m.m12());
            u.putDouble(null, destAddr + 48, m.m20());
            u.putDouble(null, destAddr + 56, m.m21());
            u.putDouble(null, destAddr + 64, m.m22());
        }

        public static void put(Matrix3x2f m, long destAddr) {
            for (int i = 0; i < 3; i++) {
                UNSAFE.putLong(null, destAddr + (i << 3), UNSAFE.getLong(m, Matrix3x2f_m00 + (i << 3)));
            }
        }

        public static void put(Matrix3x2d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putDouble(null, destAddr,      m.m00());
            u.putDouble(null, destAddr + 8,  m.m01());
            u.putDouble(null, destAddr + 16, m.m10());
            u.putDouble(null, destAddr + 24, m.m11());
            u.putDouble(null, destAddr + 32, m.m20());
            u.putDouble(null, destAddr + 40, m.m21());
        }

        public static void putf(Matrix3d m, long destAddr) {
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, destAddr,      (float)m.m00());
            u.putFloat(null, destAddr + 4,  (float)m.m01());
            u.putFloat(null, destAddr + 8,  (float)m.m02());
            u.putFloat(null, destAddr + 12, (float)m.m10());
            u.putFloat(null, destAddr + 16, (float)m.m11());
            u.putFloat(null, destAddr + 20, (float)m.m12());
            u.putFloat(null, destAddr + 24, (float)m.m20());
            u.putFloat(null, destAddr + 28, (float)m.m21());
            u.putFloat(null, destAddr + 32, (float)m.m22());
        }

        public static void put(Matrix2f m, long destAddr) {
            UNSAFE.putLong(null, destAddr,     UNSAFE.getLong(m, Matrix2f_m00));
            UNSAFE.putLong(null, destAddr + 8, UNSAFE.getLong(m, Matrix2f_m00 + 8));
        }

        public static void put(Matrix2d m, long destAddr) {
            UNSAFE.putDouble(null, destAddr,      m.m00());
            UNSAFE.putDouble(null, destAddr + 8,  m.m01());
            UNSAFE.putDouble(null, destAddr + 16, m.m10());
            UNSAFE.putDouble(null, destAddr + 24, m.m11());
        }

        public static void putf(Matrix2d m, long destAddr) {
            UNSAFE.putFloat(null, destAddr,      (float)m.m00());
            UNSAFE.putFloat(null, destAddr + 4,  (float)m.m01());
            UNSAFE.putFloat(null, destAddr + 8,  (float)m.m10());
            UNSAFE.putFloat(null, destAddr + 12, (float)m.m11());
        }

        public static void put(Vector4d src, long destAddr) {
            UNSAFE.putDouble(null, destAddr,    src.x);
            UNSAFE.putDouble(null, destAddr+8,  src.y);
            UNSAFE.putDouble(null, destAddr+16, src.z);
            UNSAFE.putDouble(null, destAddr+24, src.w);
        }

        public static void putf(Vector4d src, long destAddr) {
            UNSAFE.putFloat(null, destAddr,    (float) src.x);
            UNSAFE.putFloat(null, destAddr+4,  (float) src.y);
            UNSAFE.putFloat(null, destAddr+8,  (float) src.z);
            UNSAFE.putFloat(null, destAddr+12, (float) src.w);
        }

        public static void put(Vector4f src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector4f_x));
            UNSAFE.putLong(null, destAddr+8, UNSAFE.getLong(src, Vector4f_x+8));
        }

        public static void put(Vector4i src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector4i_x));
            UNSAFE.putLong(null, destAddr+8, UNSAFE.getLong(src, Vector4i_x+8));
        }

        public static void put(Vector3f src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector3f_x));
            UNSAFE.putFloat(null, destAddr+8, src.z);
        }

        public static void put(Vector3d src, long destAddr) {
            UNSAFE.putDouble(null, destAddr,    src.x);
            UNSAFE.putDouble(null, destAddr+8,  src.y);
            UNSAFE.putDouble(null, destAddr+16, src.z);
        }

        public static void putf(Vector3d src, long destAddr) {
            UNSAFE.putFloat(null, destAddr,   (float) src.x);
            UNSAFE.putFloat(null, destAddr+4, (float) src.y);
            UNSAFE.putFloat(null, destAddr+8, (float) src.z);
        }

        public static void put(Vector3i src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector3i_x));
            UNSAFE.putInt(null, destAddr+8, src.z);
        }

        public static void put(Vector2f src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector2f_x));
        }

        public static void put(Vector2d src, long destAddr) {
            UNSAFE.putDouble(null, destAddr,   src.x);
            UNSAFE.putDouble(null, destAddr+8, src.y);
        }

        public static void put(Vector2i src, long destAddr) {
            UNSAFE.putLong(null, destAddr, UNSAFE.getLong(src, Vector2i_x));
        }

        public static void get(Matrix4f m, long srcAddr) {
            for (int i = 0; i < 8; i++) {
                UNSAFE.putLong(m, Matrix4f_m00 + (i << 3), UNSAFE.getLong(srcAddr + (i << 3)));
            }
        }

        public static void getTransposed(Matrix4f m, long srcAddr) {
            m._m00(UNSAFE.getFloat(srcAddr))
             ._m10(UNSAFE.getFloat(srcAddr + 4))
             ._m20(UNSAFE.getFloat(srcAddr + 8))
             ._m30(UNSAFE.getFloat(srcAddr + 12))
             ._m01(UNSAFE.getFloat(srcAddr + 16))
             ._m11(UNSAFE.getFloat(srcAddr + 20))
             ._m21(UNSAFE.getFloat(srcAddr + 24))
             ._m31(UNSAFE.getFloat(srcAddr + 28))
             ._m02(UNSAFE.getFloat(srcAddr + 32))
             ._m12(UNSAFE.getFloat(srcAddr + 36))
             ._m22(UNSAFE.getFloat(srcAddr + 40))
             ._m32(UNSAFE.getFloat(srcAddr + 44))
             ._m03(UNSAFE.getFloat(srcAddr + 48))
             ._m13(UNSAFE.getFloat(srcAddr + 52))
             ._m23(UNSAFE.getFloat(srcAddr + 56))
             ._m33(UNSAFE.getFloat(srcAddr + 60));
        }

        public static void get(Matrix4x3f m, long srcAddr) {
            for (int i = 0; i < 6; i++) {
                UNSAFE.putLong(m, Matrix4x3f_m00 + (i << 3), UNSAFE.getLong(srcAddr + (i << 3)));
            }
        }

        public static void get(Matrix4d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getDouble(null, srcAddr))
            ._m01(u.getDouble(null, srcAddr+8))
            ._m02(u.getDouble(null, srcAddr+16))
            ._m03(u.getDouble(null, srcAddr+24))
            ._m10(u.getDouble(null, srcAddr+32))
            ._m11(u.getDouble(null, srcAddr+40))
            ._m12(u.getDouble(null, srcAddr+48))
            ._m13(u.getDouble(null, srcAddr+56))
            ._m20(u.getDouble(null, srcAddr+64))
            ._m21(u.getDouble(null, srcAddr+72))
            ._m22(u.getDouble(null, srcAddr+80))
            ._m23(u.getDouble(null, srcAddr+88))
            ._m30(u.getDouble(null, srcAddr+96))
            ._m31(u.getDouble(null, srcAddr+104))
            ._m32(u.getDouble(null, srcAddr+112))
            ._m33(u.getDouble(null, srcAddr+120));
        }

        public static void get(Matrix4x3d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getDouble(null, srcAddr))
            ._m01(u.getDouble(null, srcAddr+8))
            ._m02(u.getDouble(null, srcAddr+16))
            ._m10(u.getDouble(null, srcAddr+24))
            ._m11(u.getDouble(null, srcAddr+32))
            ._m12(u.getDouble(null, srcAddr+40))
            ._m20(u.getDouble(null, srcAddr+48))
            ._m21(u.getDouble(null, srcAddr+56))
            ._m22(u.getDouble(null, srcAddr+64))
            ._m30(u.getDouble(null, srcAddr+72))
            ._m31(u.getDouble(null, srcAddr+80))
            ._m32(u.getDouble(null, srcAddr+88));
        }

        public static void getf(Matrix4d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getFloat(null, srcAddr))
            ._m01(u.getFloat(null, srcAddr+4))
            ._m02(u.getFloat(null, srcAddr+8))
            ._m03(u.getFloat(null, srcAddr+12))
            ._m10(u.getFloat(null, srcAddr+16))
            ._m11(u.getFloat(null, srcAddr+20))
            ._m12(u.getFloat(null, srcAddr+24))
            ._m13(u.getFloat(null, srcAddr+28))
            ._m20(u.getFloat(null, srcAddr+32))
            ._m21(u.getFloat(null, srcAddr+36))
            ._m22(u.getFloat(null, srcAddr+40))
            ._m23(u.getFloat(null, srcAddr+44))
            ._m30(u.getFloat(null, srcAddr+48))
            ._m31(u.getFloat(null, srcAddr+52))
            ._m32(u.getFloat(null, srcAddr+56))
            ._m33(u.getFloat(null, srcAddr+60));
        }

        public static void getf(Matrix4x3d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getFloat(null, srcAddr))
            ._m01(u.getFloat(null, srcAddr+4))
            ._m02(u.getFloat(null, srcAddr+8))
            ._m10(u.getFloat(null, srcAddr+12))
            ._m11(u.getFloat(null, srcAddr+16))
            ._m12(u.getFloat(null, srcAddr+20))
            ._m20(u.getFloat(null, srcAddr+24))
            ._m21(u.getFloat(null, srcAddr+28))
            ._m22(u.getFloat(null, srcAddr+32))
            ._m30(u.getFloat(null, srcAddr+36))
            ._m31(u.getFloat(null, srcAddr+40))
            ._m32(u.getFloat(null, srcAddr+44));
        }

        public static void get(Matrix3f m, long srcAddr) {
            for (int i = 0; i < 4; i++) {
                UNSAFE.putLong(m, Matrix3f_m00 + (i << 3), UNSAFE.getLong(null, srcAddr + (i << 3)));
            }
            m._m22(UNSAFE.getFloat(null, srcAddr+32));
        }

        public static void get(Matrix3d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getDouble(null, srcAddr))
            ._m01(u.getDouble(null, srcAddr+8))
            ._m02(u.getDouble(null, srcAddr+16))
            ._m10(u.getDouble(null, srcAddr+24))
            ._m11(u.getDouble(null, srcAddr+32))
            ._m12(u.getDouble(null, srcAddr+40))
            ._m20(u.getDouble(null, srcAddr+48))
            ._m21(u.getDouble(null, srcAddr+56))
            ._m22(u.getDouble(null, srcAddr+64));
        }

        public static void get(Matrix3x2f m, long srcAddr) {
            for (int i = 0; i < 3; i++) {
                UNSAFE.putLong(m, Matrix3x2f_m00 + (i << 3), UNSAFE.getLong(null, srcAddr + (i << 3)));
            }
        }

        public static void get(Matrix3x2d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getDouble(null, srcAddr))
            ._m01(u.getDouble(null, srcAddr+8))
            ._m10(u.getDouble(null, srcAddr+16))
            ._m11(u.getDouble(null, srcAddr+24))
            ._m20(u.getDouble(null, srcAddr+32))
            ._m21(u.getDouble(null, srcAddr+40));
        }

        public static void getf(Matrix3d m, long srcAddr) {
            sun.misc.Unsafe u = UNSAFE;
            m._m00(u.getFloat(null, srcAddr))
            ._m01(u.getFloat(null, srcAddr+4))
            ._m02(u.getFloat(null, srcAddr+8))
            ._m10(u.getFloat(null, srcAddr+12))
            ._m11(u.getFloat(null, srcAddr+16))
            ._m12(u.getFloat(null, srcAddr+20))
            ._m20(u.getFloat(null, srcAddr+24))
            ._m21(u.getFloat(null, srcAddr+28))
            ._m22(u.getFloat(null, srcAddr+32));
        }

        public static void get(Matrix2f m, long srcAddr) {
            UNSAFE.putLong(m, Matrix2f_m00,     UNSAFE.getLong(null, srcAddr));
            UNSAFE.putLong(m, Matrix2f_m00 + 8, UNSAFE.getLong(null, srcAddr + 8));
        }

        public static void get(Matrix2d m, long srcAddr) {
            m._m00(UNSAFE.getDouble(null, srcAddr))
            ._m01(UNSAFE.getDouble(null, srcAddr+8))
            ._m10(UNSAFE.getDouble(null, srcAddr+16))
            ._m11(UNSAFE.getDouble(null, srcAddr+24));
        }

        public static void getf(Matrix2d m, long srcAddr) {
            m._m00(UNSAFE.getFloat(null, srcAddr))
            ._m01(UNSAFE.getFloat(null, srcAddr+4))
            ._m10(UNSAFE.getFloat(null, srcAddr+8))
            ._m11(UNSAFE.getFloat(null, srcAddr+12));
        }

        public static void get(Vector4d dst, long srcAddr) {
            dst.x = UNSAFE.getDouble(null, srcAddr);
            dst.y = UNSAFE.getDouble(null, srcAddr+8);
            dst.z = UNSAFE.getDouble(null, srcAddr+16);
            dst.w = UNSAFE.getDouble(null, srcAddr+24);
        }

        public static void get(Vector4f dst, long srcAddr) {
            dst.x = UNSAFE.getFloat(null, srcAddr);
            dst.y = UNSAFE.getFloat(null, srcAddr+4);
            dst.z = UNSAFE.getFloat(null, srcAddr+8);
            dst.w = UNSAFE.getFloat(null, srcAddr+12);
        }

        public static void get(Vector4i dst, long srcAddr) {
            dst.x = UNSAFE.getInt(null, srcAddr);
            dst.y = UNSAFE.getInt(null, srcAddr+4);
            dst.z = UNSAFE.getInt(null, srcAddr+8);
            dst.w = UNSAFE.getInt(null, srcAddr+12);
        }

        public static void get(Vector3f dst, long srcAddr) {
            dst.x = UNSAFE.getFloat(null, srcAddr);
            dst.y = UNSAFE.getFloat(null, srcAddr+4);
            dst.z = UNSAFE.getFloat(null, srcAddr+8);
        }

        public static void get(Vector3d dst, long srcAddr) {
            dst.x = UNSAFE.getDouble(null, srcAddr);
            dst.y = UNSAFE.getDouble(null, srcAddr+8);
            dst.z = UNSAFE.getDouble(null, srcAddr+16);
        }

        public static void get(Vector3i dst, long srcAddr) {
            dst.x = UNSAFE.getInt(null, srcAddr);
            dst.y = UNSAFE.getInt(null, srcAddr+4);
            dst.z = UNSAFE.getInt(null, srcAddr+8);
        }

        public static void get(Vector2f dst, long srcAddr) {
            dst.x = UNSAFE.getFloat(null, srcAddr);
            dst.y = UNSAFE.getFloat(null, srcAddr+4);
        }

        public static void get(Vector2d dst, long srcAddr) {
            dst.x = UNSAFE.getDouble(null, srcAddr);
            dst.y = UNSAFE.getDouble(null, srcAddr+8);
        }

        public static void get(Vector2i dst, long srcAddr) {
            dst.x = UNSAFE.getInt(null, srcAddr);
            dst.y = UNSAFE.getInt(null, srcAddr+4);
        }

        public static void putMatrix3f(Quaternionf q, long addr) {
            float dx = q.x + q.x;
            float dy = q.y + q.y;
            float dz = q.z + q.z;
            float q00 = dx * q.x;
            float q11 = dy * q.y;
            float q22 = dz * q.z;
            float q01 = dx * q.y;
            float q02 = dx * q.z;
            float q03 = dx * q.w;
            float q12 = dy * q.z;
            float q13 = dy * q.w;
            float q23 = dz * q.w;
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, addr, 1.0f - q11 - q22);
            u.putFloat(null, addr + 4, q01 + q23);
            u.putFloat(null, addr + 8, q02 - q13);
            u.putFloat(null, addr + 12, q01 - q23);
            u.putFloat(null, addr + 16, 1.0f - q22 - q00);
            u.putFloat(null, addr + 20, q12 + q03);
            u.putFloat(null, addr + 24, q02 + q13);
            u.putFloat(null, addr + 28, q12 - q03);
            u.putFloat(null, addr + 32, 1.0f - q11 - q00); 
        }

        public static void putMatrix4f(Quaternionf q, long addr) {
            float dx = q.x + q.x;
            float dy = q.y + q.y;
            float dz = q.z + q.z;
            float q00 = dx * q.x;
            float q11 = dy * q.y;
            float q22 = dz * q.z;
            float q01 = dx * q.y;
            float q02 = dx * q.z;
            float q03 = dx * q.w;
            float q12 = dy * q.z;
            float q13 = dy * q.w;
            float q23 = dz * q.w;
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, addr, 1.0f - q11 - q22);
            u.putFloat(null, addr + 4, q01 + q23);
            u.putLong(null, addr + 8, Float.floatToRawIntBits(q02 - q13) & 0xFFFFFFFFL);
            u.putFloat(null, addr + 16, q01 - q23);
            u.putFloat(null, addr + 20, 1.0f - q22 - q00);
            u.putLong(null, addr + 24, Float.floatToRawIntBits(q12 + q03) & 0xFFFFFFFFL);
            u.putFloat(null, addr + 32, q02 + q13);
            u.putFloat(null, addr + 36, q12 - q03);
            u.putLong(null, addr + 40, Float.floatToRawIntBits(1.0f - q11 - q00) & 0xFFFFFFFFL);
            u.putLong(null, addr + 48, 0L);
            u.putLong(null, addr + 56, 0x3F80000000000000L);
        }

        public static void putMatrix4x3f(Quaternionf q, long addr) {
            float dx = q.x + q.x;
            float dy = q.y + q.y;
            float dz = q.z + q.z;
            float q00 = dx * q.x;
            float q11 = dy * q.y;
            float q22 = dz * q.z;
            float q01 = dx * q.y;
            float q02 = dx * q.z;
            float q03 = dx * q.w;
            float q12 = dy * q.z;
            float q13 = dy * q.w;
            float q23 = dz * q.w;
            sun.misc.Unsafe u = UNSAFE;
            u.putFloat(null, addr, 1.0f - q11 - q22);
            u.putFloat(null, addr + 4, q01 + q23);
            u.putFloat(null, addr + 8, q02 - q13);
            u.putFloat(null, addr + 12, q01 - q23);
            u.putFloat(null, addr + 16, 1.0f - q22 - q00);
            u.putFloat(null, addr + 20, q12 + q03);
            u.putFloat(null, addr + 24, q02 + q13);
            u.putFloat(null, addr + 28, q12 - q03);
            u.putFloat(null, addr + 32, 1.0f - q11 - q00);
            u.putLong(null, addr + 36, 0L);
            u.putFloat(null, addr + 44, 0.0f);
        }

        private static void throwNoDirectBufferException() {
            throw new IllegalArgumentException("Must use a direct buffer");
        }

//#ifdef __HAS_NIO__
        public void putMatrix3f(Quaternionf q, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 2);
            putMatrix3f(q, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putMatrix3f(Quaternionf q, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            putMatrix3f(q, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        private static void checkPut(int offset, boolean direct, int capacity, int i) {
            if (!direct)
                throwNoDirectBufferException();
            if (capacity - offset < i)
                throw new BufferOverflowException();
        }

        public void putMatrix4f(Quaternionf q, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            putMatrix4f(q, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putMatrix4f(Quaternionf q, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            putMatrix4f(q, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putMatrix4x3f(Quaternionf q, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            putMatrix4x3f(q, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putMatrix4x3f(Quaternionf q, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            putMatrix4x3f(q, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix4f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix4f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x3(Matrix4f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put4x3(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put4x3(Matrix4f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put4x3(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put3x4(Matrix4f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put3x4(Matrix4f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x4(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put4x4(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put3x4(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put3x4(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x4(Matrix4x3d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put4x4(Matrix4x3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 3);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x4(Matrix3x2f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put4x4(Matrix3x2f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x4(Matrix3x2d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put4x4(Matrix3x2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 3);
            put4x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put3x3(Matrix3x2f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            put3x3(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put3x3(Matrix3x2f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 2);
            put3x3(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put3x3(Matrix3x2d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            put3x3(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put3x3(Matrix3x2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 3);
            put3x3(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix4f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putTransposed(Matrix4f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x3Transposed(Matrix4f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put4x3Transposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put4x3Transposed(Matrix4f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put4x3Transposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix4x3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putTransposed(Matrix4x3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putTransposed(Matrix3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 2);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix2f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putTransposed(Matrix2f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix4d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Matrix4d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 3);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix4x3d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Matrix4x3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 3);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Matrix4d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putf(Matrix4d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Matrix4x3d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putf(Matrix4x3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix4d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void putTransposed(Matrix4d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 3);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put4x3Transposed(Matrix4d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put4x3Transposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put4x3Transposed(Matrix4d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 3);
            put4x3Transposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix4x3d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void putTransposed(Matrix4x3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 3);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putTransposed(Matrix2d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void putTransposed(Matrix2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 3);
            putTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putfTransposed(Matrix4d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putfTransposed(Matrix4d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 16 << 2);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putfTransposed(Matrix4x3d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putfTransposed(Matrix4x3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putfTransposed(Matrix2d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putfTransposed(Matrix2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            putfTransposed(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 2);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put3x4(Matrix3f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put3x4(Matrix3f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 12 << 2);
            put3x4(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix3d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Matrix3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 3);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix3x2f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 6);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix3x2f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 6 << 2);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix3x2d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 6);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Matrix3x2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 6 << 3);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Matrix3d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putf(Matrix3d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 9 << 2);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix2f m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Matrix2f m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Matrix2d m, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            put(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Matrix2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2 << 3);
            put(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Matrix2d m, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void putf(Matrix2d m, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            putf(m, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector4d src, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Vector4d src, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            putf(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector4d src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Vector4d src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            putf(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector4f src, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector4f src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector4i src, int offset, IntBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector4i src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 4 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector3f src, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector3f src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector3d src, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Vector3d src, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3);
            putf(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector3d src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3 << 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void putf(Vector3d src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3 << 2);
            putf(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector3i src, int offset, IntBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector3i src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 3 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector2f src, int offset, FloatBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector2f src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector2d src, int offset, DoubleBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 3));
        }

        public void put(Vector2d src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2 << 3);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void put(Vector2i src, int offset, IntBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + (offset << 2));
        }

        public void put(Vector2i src, int offset, ByteBuffer dest) {
            if (Options.DEBUG) checkPut(offset, dest.isDirect(), dest.capacity(), 2 << 2);
            put(src, UNSAFE.getLong(dest, ADDRESS) + offset);
        }

        public void get(Matrix4f m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Matrix4f m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16 << 2);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public float get(Matrix4f m, int column, int row) {
            return UNSAFE.getFloat(m, Matrix4f_m00 + (column << 4) + (row << 2));
        }

        public Matrix4f set(Matrix4f m, int column, int row, float value) {
            UNSAFE.putFloat(m, Matrix4f_m00 + (column << 4) + (row << 2), value);
            return m;
        }

        public double get(Matrix4d m, int column, int row) {
            return UNSAFE.getDouble(m, Matrix4d_m00 + (column << 5) + (row << 3));
        }

        public Matrix4d set(Matrix4d m, int column, int row, double value) {
            UNSAFE.putDouble(m, Matrix4d_m00 + (column << 5) + (row << 3), value);
            return m;
        }

        public float get(Matrix3f m, int column, int row) {
            return UNSAFE.getFloat(m, Matrix3f_m00 + (column * (3<<2)) + (row << 2));
        }

        public Matrix3f set(Matrix3f m, int column, int row, float value) {
            UNSAFE.putFloat(m, Matrix3f_m00 + (column * (3<<2)) + (row << 2), value);
            return m;
        }

        public double get(Matrix3d m, int column, int row) {
            return UNSAFE.getDouble(m, Matrix3d_m00 + (column * (3<<3)) + (row << 3));
        }

        public Matrix3d set(Matrix3d m, int column, int row, double value) {
            UNSAFE.putDouble(m, Matrix3d_m00 + (column * (3<<3)) + (row << 3), value);
            return m;
        }

        public void get(Matrix4x3f m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Matrix4x3f m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12 << 2);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix4d m, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Matrix4d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16 << 3);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix4x3d m, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Matrix4x3d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12 << 3);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void getf(Matrix4d m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16);
            getf(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void getf(Matrix4d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 16 << 2);
            getf(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void getf(Matrix4x3d m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12);
            getf(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        private static void checkGet(int offset, boolean direct, int capacity, int i) {
            if (!direct)
                throwNoDirectBufferException();
            if (capacity - offset < i)
                throw new BufferUnderflowException();
        }

        public void getf(Matrix4x3d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 12 << 2);
            getf(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix3f m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Matrix3f m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9 << 2);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix3d m, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Matrix3d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9 << 3);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix3x2f m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 6);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Matrix3x2f m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 6 << 2);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix3x2d m, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 6);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Matrix3x2d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 6 << 3);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void getf(Matrix3d m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9);
            getf(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void getf(Matrix3d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 9 << 2);
            getf(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix2f m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Matrix2f m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 2);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Matrix2d m, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            get(m, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Matrix2d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 3);
            get(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void getf(Matrix2d m, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            getf(m, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void getf(Matrix2d m, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 2);
            getf(m, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector4d dst, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Vector4d dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector4f dst, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector4f dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector4i dst, int offset, IntBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector4i dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 4 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector3f dst, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector3f dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector3d dst, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Vector3d dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3 << 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector3i dst, int offset, IntBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector3i dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 3 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector2f dst, int offset, FloatBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector2f dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector2d dst, int offset, DoubleBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 3));
        }

        public void get(Vector2d dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2 << 3);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }

        public void get(Vector2i dst, int offset, IntBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + (offset << 2));
        }

        public void get(Vector2i dst, int offset, ByteBuffer src) {
            if (Options.DEBUG) checkGet(offset, src.isDirect(), src.capacity(), 2 << 2);
            get(dst, UNSAFE.getLong(src, ADDRESS) + offset);
        }
//#endif
    }
//#endif
}
