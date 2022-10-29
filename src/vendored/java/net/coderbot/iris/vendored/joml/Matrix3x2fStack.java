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
 * A stack of many {@link Matrix3x2f} instances. This resembles the matrix stack known from legacy OpenGL.
 * <p>
 * This {@link Matrix3x2fStack} class inherits from {@link Matrix3x2f}, so the current/top matrix is always the
 * {@link Matrix3x2fStack}/{@link Matrix3x2f} itself. This affects all operations in {@link Matrix3x2f} that take
 * another {@link Matrix3x2f} as parameter. If a {@link Matrix3x2fStack} is used as argument to those methods, the
 * effective argument will always be the <i>current</i> matrix of the matrix stack.
 * 
 * @author Kai Burjack
 */
public class Matrix3x2fStack extends Matrix3x2f {

    private static final long serialVersionUID = 1L;

    /**
     * The matrix stack as a non-growable array. The size of the stack must be specified in the {@link #Matrix3x2fStack(int) constructor}.
     */
    private Matrix3x2f[] mats;

    /**
     * The index of the "current" matrix within {@link #mats}.
     */
    private int curr;

    /**
     * Create a new {@link Matrix3x2fStack} of the given size.
     * <p>
     * Initially the stack pointer is at zero and the current matrix is set to identity.
     * 
     * @param stackSize
     *            the size of the stack. This must be at least 1, in which case the {@link Matrix3x2fStack} simply only consists of <code>this</code>
     *            {@link Matrix3x2f}
     */
    public Matrix3x2fStack(int stackSize) {
        if (stackSize < 1) {
            throw new IllegalArgumentException("stackSize must be >= 1"); //$NON-NLS-1$
        }
        mats = new Matrix3x2f[stackSize - 1];
        // Allocate all matrices up front to keep the promise of being "allocation-free"
        for (int i = 0; i < mats.length; i++) {
            mats[i] = new Matrix3x2f();
        }
    }

    /**
     * Do not invoke manually! Only meant for serialization.
     * <p>
     * Invoking this constructor from client code will result in an inconsistent state of the 
     * created {@link Matrix3x2fStack} instance.
     */
    public Matrix3x2fStack() {
        /* Empty! */
    }

    /**
     * Set the stack pointer to zero and set the current/bottom matrix to {@link #identity() identity}.
     * 
     * @return this
     */
    public Matrix3x2fStack clear() {
        curr = 0;
        identity();
        return this;
    }

    /**
     * Increment the stack pointer by one and set the values of the new current matrix to the one directly below it.
     * 
     * @return this
     */
    public Matrix3x2fStack pushMatrix() {
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
    public Matrix3x2fStack popMatrix() {
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
     * Contract between Matrix3x2f and Matrix3x2fStack:
     * 
     * - Matrix3x2f.equals(Matrix3x2fStack) is true iff all the 6 matrix elements are equal
     * - Matrix3x2fStack.equals(Matrix3x2f) is true iff all the 6 matrix elements are equal
     * - Matrix3x2fStack.equals(Matrix3x2fStack) is true iff all 6 matrix elements are equal AND the matrix arrays as well as the stack pointer are equal
     * - everything else is inequal
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (obj instanceof Matrix3x2fStack) {
            Matrix3x2fStack other = (Matrix3x2fStack) obj;
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
        mats = new Matrix3x2fStack[curr];
        for (int i = 0; i < curr; i++) {
            Matrix3x2f m = new Matrix3x2f();
            m.readExternal(in);
            mats[i] = m;
        }
    }

    public Object clone() throws CloneNotSupportedException {
        Matrix3x2fStack cloned = (Matrix3x2fStack) super.clone();
        Matrix3x2f[] clonedMats = new Matrix3x2f[mats.length];
        for (int i = 0; i < mats.length; i++)
            clonedMats[i] = (Matrix3x2f) mats[i].clone();
        cloned.mats = clonedMats;
        return cloned;
    }

}
