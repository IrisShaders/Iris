/*
 * The MIT License
 *
 * Copyright (c) 2018-2021 JOML
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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A stack of many {@link Matrix4x3d} instances. This resembles the matrix stack known from legacy OpenGL.
 * <p>
 * This {@link Matrix4x3dStack} class inherits from {@link Matrix4x3d}, so the current/top matrix is always the
 * {@link Matrix4x3dStack}/{@link Matrix4x3d} itself. This affects all operations in {@link Matrix4x3d} that take
 * another {@link Matrix4x3d} as parameter. If a {@link Matrix4x3dStack} is used as argument to those methods, the
 * effective argument will always be the <i>current</i> matrix of the matrix stack.
 * 
 * @author Kai Burjack
 */
public class Matrix4x3dStack extends Matrix4x3d {

    private static final long serialVersionUID = 1L;

    /**
     * The matrix stack as a non-growable array. The size of the stack must be specified in the {@link #Matrix4x3dStack(int) constructor}.
     */
    private Matrix4x3d[] mats;

    /**
     * The index of the "current" matrix within {@link #mats}.
     */
    private int curr;

    /**
     * Create a new {@link Matrix4x3dStack} of the given size.
     * <p>
     * Initially the stack pointer is at zero and the current matrix is set to identity.
     * 
     * @param stackSize
     *            the size of the stack. This must be at least 1, in which case the {@link Matrix4x3dStack} simply only consists of <code>this</code>
     *            {@link Matrix4x3d}
     */
    public Matrix4x3dStack(int stackSize) {
        if (stackSize < 1) {
            throw new IllegalArgumentException("stackSize must be >= 1"); //$NON-NLS-1$
        }
        mats = new Matrix4x3d[stackSize - 1];
        // Allocate all matrices up front to keep the promise of being "allocation-free"
        for (int i = 0; i < mats.length; i++) {
            mats[i] = new Matrix4x3d();
        }
    }

    /**
     * Do not invoke manually! Only meant for serialization.
     * <p>
     * Invoking this constructor from client code will result in an inconsistent state of the 
     * created {@link Matrix4x3dStack} instance.
     */
    public Matrix4x3dStack() {
        /* Empty! */
    }

    /**
     * Set the stack pointer to zero and set the current/bottom matrix to {@link #identity() identity}.
     * 
     * @return this
     */
    public Matrix4x3dStack clear() {
        curr = 0;
        identity();
        return this;
    }

    /**
     * Increment the stack pointer by one and set the values of the new current matrix to the one directly below it.
     * 
     * @return this
     */
    public Matrix4x3dStack pushMatrix() {
        if (curr == mats.length) {
            throw new IllegalStateException("max stack size of " + (curr + 1) + " reached"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        mats[curr++].set(this);
        return this;
    }

    /**
     * Decrement the stack pointer by one.
     * <p>
     * This will effectively dispose of the current matrix.
     * 
     * @return this
     */
    public Matrix4x3dStack popMatrix() {
        if (curr == 0) {
            throw new IllegalStateException("already at the buttom of the stack"); //$NON-NLS-1$
        }
        set(mats[--curr]);
        return this;
    }

    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + curr;
        for (int i = 0; i < curr; i++) {
            result = prime * result + mats[i].hashCode();
        }
        return result;
    }

    /*
     * Contract between Matrix4x3d and Matrix4x3dStack:
     * 
     * - Matrix4x3d.equals(Matrix4x3dStack) is true iff all the 12 matrix elements are equal
     * - Matrix4x3dStack.equals(Matrix4x3d) is true iff all the 12 matrix elements are equal
     * - Matrix4x3dStack.equals(Matrix4x3dStack) is true iff all 12 matrix elements are equal AND the matrix arrays as well as the stack pointer are equal
     * - everything else is inequal
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (obj instanceof Matrix4x3dStack) {
            Matrix4x3dStack other = (Matrix4x3dStack) obj;
            if (curr != other.curr)
                return false;
            for (int i = 0; i < curr; i++) {
                if (!mats[i].equals(other.mats[i]))
                    return false;
            }
        }
        return true;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(curr);
        for (int i = 0; i < curr; i++) {
            out.writeObject(mats[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException {
        super.readExternal(in);
        curr = in.readInt();
        mats = new Matrix4x3dStack[curr];
        for (int i = 0; i < curr; i++) {
            Matrix4x3d m = new Matrix4x3d();
            m.readExternal(in);
            mats[i] = m;
        }
    }

    public Object clone() throws CloneNotSupportedException {
        Matrix4x3dStack cloned = (Matrix4x3dStack) super.clone();
        Matrix4x3d[] clonedMats = new Matrix4x3d[mats.length];
        for (int i = 0; i < mats.length; i++)
            clonedMats[i] = (Matrix4x3d) mats[i].clone();
        cloned.mats = clonedMats;
        return cloned;
    }

}
