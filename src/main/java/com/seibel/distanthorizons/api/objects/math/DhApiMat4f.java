/*
 *    This file is part of the Distant Horizons mod
 *    licensed under the GNU LGPL v3 License.
 *
 *    Copyright (C) 2020-2023 James Seibel
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, version 3.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.seibel.distanthorizons.api.objects.math;

import com.seibel.distanthorizons.api.interfaces.util.IDhApiCopyable;

/**
 * An (almost) exact copy of Minecraft's 1.16.5
 * implementation of a 4x4 float matrix. <br><br>
 *
 * <code>
 * m00, m10, m20, m30,  <br>
 * m01, m11, m21, m31,  <br>
 * m02, m12, m22, m32,  <br>
 * m03, m13, m23, m33   <br>
 * </code>
 * 
 * @author James Seibel
 * @version 2024-6-30
 */
public class DhApiMat4f implements IDhApiCopyable
{
	public float m00;
	public float m01;
	public float m02;
	public float m03;
	
	public float m10;
	public float m11;
	public float m12;
	public float m13;
	
	public float m20;
	public float m21;
	public float m22;
	public float m23;
	
	public float m30;
	public float m31;
	public float m32;
	public float m33;
	
	
	
	//==============//
	// constructors //
	//==============//
	
	public DhApiMat4f() { /* all values are 0 */ }
	
	public DhApiMat4f(DhApiMat4f sourceMatrix)
	{
		this.m00 = sourceMatrix.m00;
		this.m01 = sourceMatrix.m01;
		this.m02 = sourceMatrix.m02;
		this.m03 = sourceMatrix.m03;
		this.m10 = sourceMatrix.m10;
		this.m11 = sourceMatrix.m11;
		this.m12 = sourceMatrix.m12;
		this.m13 = sourceMatrix.m13;
		this.m20 = sourceMatrix.m20;
		this.m21 = sourceMatrix.m21;
		this.m22 = sourceMatrix.m22;
		this.m23 = sourceMatrix.m23;
		this.m30 = sourceMatrix.m30;
		this.m31 = sourceMatrix.m31;
		this.m32 = sourceMatrix.m32;
		this.m33 = sourceMatrix.m33;
	}
	
	/** Expects the values of the input array to be in row major order (AKA rows then columns) */
	public DhApiMat4f(float[] values)
	{
		m00 = values[0];
		m01 = values[1];
		m02 = values[2];
		m03 = values[3];
		
		m10 = values[4];
		m11 = values[5];
		m12 = values[6];
		m13 = values[7];
		
		m20 = values[8];
		m21 = values[9];
		m22 = values[10];
		m23 = values[11];
		
		m30 = values[12];
		m31 = values[13];
		m32 = values[14];
		m33 = values[15];
	}
	
	
	
	
	//=========//
	// methods //
	//=========//
	
	public void setIdentity()
	{
		this.m00 = 1.0F;
		this.m01 = 0.0F;
		this.m02 = 0.0F;
		this.m03 = 0.0F;
		this.m10 = 0.0F;
		this.m11 = 1.0F;
		this.m12 = 0.0F;
		this.m13 = 0.0F;
		this.m20 = 0.0F;
		this.m21 = 0.0F;
		this.m22 = 1.0F;
		this.m23 = 0.0F;
		this.m30 = 0.0F;
		this.m31 = 0.0F;
		this.m32 = 0.0F;
		this.m33 = 1.0F;
	}
	
	/** adjudicate and determinate */
	public float adjudicateAndDet()
	{
		float f = this.m00 * this.m11 - this.m01 * this.m10;
		float f1 = this.m00 * this.m12 - this.m02 * this.m10;
		float f2 = this.m00 * this.m13 - this.m03 * this.m10;
		float f3 = this.m01 * this.m12 - this.m02 * this.m11;
		float f4 = this.m01 * this.m13 - this.m03 * this.m11;
		float f5 = this.m02 * this.m13 - this.m03 * this.m12;
		float f6 = this.m20 * this.m31 - this.m21 * this.m30;
		float f7 = this.m20 * this.m32 - this.m22 * this.m30;
		float f8 = this.m20 * this.m33 - this.m23 * this.m30;
		float f9 = this.m21 * this.m32 - this.m22 * this.m31;
		float f10 = this.m21 * this.m33 - this.m23 * this.m31;
		float f11 = this.m22 * this.m33 - this.m23 * this.m32;
		float f12 = this.m11 * f11 - this.m12 * f10 + this.m13 * f9;
		float f13 = -this.m10 * f11 + this.m12 * f8 - this.m13 * f7;
		float f14 = this.m10 * f10 - this.m11 * f8 + this.m13 * f6;
		float f15 = -this.m10 * f9 + this.m11 * f7 - this.m12 * f6;
		float f16 = -this.m01 * f11 + this.m02 * f10 - this.m03 * f9;
		float f17 = this.m00 * f11 - this.m02 * f8 + this.m03 * f7;
		float f18 = -this.m00 * f10 + this.m01 * f8 - this.m03 * f6;
		float f19 = this.m00 * f9 - this.m01 * f7 + this.m02 * f6;
		float f20 = this.m31 * f5 - this.m32 * f4 + this.m33 * f3;
		float f21 = -this.m30 * f5 + this.m32 * f2 - this.m33 * f1;
		float f22 = this.m30 * f4 - this.m31 * f2 + this.m33 * f;
		float f23 = -this.m30 * f3 + this.m31 * f1 - this.m32 * f;
		float f24 = -this.m21 * f5 + this.m22 * f4 - this.m23 * f3;
		float f25 = this.m20 * f5 - this.m22 * f2 + this.m23 * f1;
		float f26 = -this.m20 * f4 + this.m21 * f2 - this.m23 * f;
		float f27 = this.m20 * f3 - this.m21 * f1 + this.m22 * f;
		this.m00 = f12;
		this.m10 = f13;
		this.m20 = f14;
		this.m30 = f15;
		this.m01 = f16;
		this.m11 = f17;
		this.m21 = f18;
		this.m31 = f19;
		this.m02 = f20;
		this.m12 = f21;
		this.m22 = f22;
		this.m32 = f23;
		this.m03 = f24;
		this.m13 = f25;
		this.m23 = f26;
		this.m33 = f27;
		return f * f11 - f1 * f10 + f2 * f9 + f3 * f8 - f4 * f7 + f5 * f6;
	}
	
	public void transpose()
	{
		float f = this.m10;
		this.m10 = this.m01;
		this.m01 = f;
		f = this.m20;
		this.m20 = this.m02;
		this.m02 = f;
		f = this.m21;
		this.m21 = this.m12;
		this.m12 = f;
		f = this.m30;
		this.m30 = this.m03;
		this.m03 = f;
		f = this.m31;
		this.m31 = this.m13;
		this.m13 = f;
		f = this.m32;
		this.m32 = this.m23;
		this.m23 = f;
	}
	
	public boolean canInvert()
	{
		float det = this.adjudicateAndDet();
		return (Math.abs(det) > 1.0E-6F);
	}
	
	public void invert()
	{
		float det = this.adjudicateAndDet();
		if (Math.abs(det) > 1.0E-6F)
		{
			this.multiply(det);
		}
	}
	
	public void multiply(DhApiMat4f multMatrix)
	{
		float f = this.m00 * multMatrix.m00 + this.m01 * multMatrix.m10 + this.m02 * multMatrix.m20 + this.m03 * multMatrix.m30;
		float f1 = this.m00 * multMatrix.m01 + this.m01 * multMatrix.m11 + this.m02 * multMatrix.m21 + this.m03 * multMatrix.m31;
		float f2 = this.m00 * multMatrix.m02 + this.m01 * multMatrix.m12 + this.m02 * multMatrix.m22 + this.m03 * multMatrix.m32;
		float f3 = this.m00 * multMatrix.m03 + this.m01 * multMatrix.m13 + this.m02 * multMatrix.m23 + this.m03 * multMatrix.m33;
		float f4 = this.m10 * multMatrix.m00 + this.m11 * multMatrix.m10 + this.m12 * multMatrix.m20 + this.m13 * multMatrix.m30;
		float f5 = this.m10 * multMatrix.m01 + this.m11 * multMatrix.m11 + this.m12 * multMatrix.m21 + this.m13 * multMatrix.m31;
		float f6 = this.m10 * multMatrix.m02 + this.m11 * multMatrix.m12 + this.m12 * multMatrix.m22 + this.m13 * multMatrix.m32;
		float f7 = this.m10 * multMatrix.m03 + this.m11 * multMatrix.m13 + this.m12 * multMatrix.m23 + this.m13 * multMatrix.m33;
		float f8 = this.m20 * multMatrix.m00 + this.m21 * multMatrix.m10 + this.m22 * multMatrix.m20 + this.m23 * multMatrix.m30;
		float f9 = this.m20 * multMatrix.m01 + this.m21 * multMatrix.m11 + this.m22 * multMatrix.m21 + this.m23 * multMatrix.m31;
		float f10 = this.m20 * multMatrix.m02 + this.m21 * multMatrix.m12 + this.m22 * multMatrix.m22 + this.m23 * multMatrix.m32;
		float f11 = this.m20 * multMatrix.m03 + this.m21 * multMatrix.m13 + this.m22 * multMatrix.m23 + this.m23 * multMatrix.m33;
		float f12 = this.m30 * multMatrix.m00 + this.m31 * multMatrix.m10 + this.m32 * multMatrix.m20 + this.m33 * multMatrix.m30;
		float f13 = this.m30 * multMatrix.m01 + this.m31 * multMatrix.m11 + this.m32 * multMatrix.m21 + this.m33 * multMatrix.m31;
		float f14 = this.m30 * multMatrix.m02 + this.m31 * multMatrix.m12 + this.m32 * multMatrix.m22 + this.m33 * multMatrix.m32;
		float f15 = this.m30 * multMatrix.m03 + this.m31 * multMatrix.m13 + this.m32 * multMatrix.m23 + this.m33 * multMatrix.m33;
		this.m00 = f;
		this.m01 = f1;
		this.m02 = f2;
		this.m03 = f3;
		this.m10 = f4;
		this.m11 = f5;
		this.m12 = f6;
		this.m13 = f7;
		this.m20 = f8;
		this.m21 = f9;
		this.m22 = f10;
		this.m23 = f11;
		this.m30 = f12;
		this.m31 = f13;
		this.m32 = f14;
		this.m33 = f15;
	}
	
	public void multiply(float scalar)
	{
		this.m00 *= scalar;
		this.m01 *= scalar;
		this.m02 *= scalar;
		this.m03 *= scalar;
		this.m10 *= scalar;
		this.m11 *= scalar;
		this.m12 *= scalar;
		this.m13 *= scalar;
		this.m20 *= scalar;
		this.m21 *= scalar;
		this.m22 *= scalar;
		this.m23 *= scalar;
		this.m30 *= scalar;
		this.m31 *= scalar;
		this.m32 *= scalar;
		this.m33 *= scalar;
	}
	
	
	
	
	//==================//
	// Distant Horizons //
	//      methods     //
	//==================//
	
	private static int getArrayIndex(int xIndex, int zIndex) { return (zIndex * 4) + xIndex; }
	
	/** Returns the values of this matrix in row major order (AKA rows then columns) */
	public float[] getValuesAsArray()
	{
		return new float[]{
				this.m00,
				this.m01,
				this.m02,
				this.m03,
				
				this.m10,
				this.m11,
				this.m12,
				this.m13,
				
				this.m20,
				this.m21,
				this.m22,
				this.m23,
				
				this.m30,
				this.m31,
				this.m32,
				this.m33,
		};
	}
	
	
	
	//================//
	// base overrides //
	//================//
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj != null && this.getClass() == obj.getClass())
		{
			DhApiMat4f otherMatrix = (DhApiMat4f) obj;
			return Float.compare(otherMatrix.m00, this.m00) == 0
					&& Float.compare(otherMatrix.m01, this.m01) == 0
					&& Float.compare(otherMatrix.m02, this.m02) == 0
					&& Float.compare(otherMatrix.m03, this.m03) == 0
					&& Float.compare(otherMatrix.m10, this.m10) == 0
					&& Float.compare(otherMatrix.m11, this.m11) == 0
					&& Float.compare(otherMatrix.m12, this.m12) == 0
					&& Float.compare(otherMatrix.m13, this.m13) == 0
					&& Float.compare(otherMatrix.m20, this.m20) == 0
					&& Float.compare(otherMatrix.m21, this.m21) == 0
					&& Float.compare(otherMatrix.m22, this.m22) == 0
					&& Float.compare(otherMatrix.m23, this.m23) == 0
					&& Float.compare(otherMatrix.m30, this.m30) == 0
					&& Float.compare(otherMatrix.m31, this.m31) == 0
					&& Float.compare(otherMatrix.m32, this.m32) == 0
					&& Float.compare(otherMatrix.m33, this.m33) == 0;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
		i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
		i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
		i = 31 * i + (this.m03 != 0.0F ? Float.floatToIntBits(this.m03) : 0);
		i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
		i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
		i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
		i = 31 * i + (this.m13 != 0.0F ? Float.floatToIntBits(this.m13) : 0);
		i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
		i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
		i = 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
		i = 31 * i + (this.m23 != 0.0F ? Float.floatToIntBits(this.m23) : 0);
		i = 31 * i + (this.m30 != 0.0F ? Float.floatToIntBits(this.m30) : 0);
		i = 31 * i + (this.m31 != 0.0F ? Float.floatToIntBits(this.m31) : 0);
		i = 31 * i + (this.m32 != 0.0F ? Float.floatToIntBits(this.m32) : 0);
		return 31 * i + (this.m33 != 0.0F ? Float.floatToIntBits(this.m33) : 0);
	}
	
	@Override
	public String toString()
	{
		return "Matrix4f:\n" +
				this.m00 + " " + this.m01 + " " + this.m02 + " " + this.m03 + "\n" +
				this.m10 + " " + this.m11 + " " + this.m12 + " " + this.m13 + "\n" +
				this.m20 + " " + this.m21 + " " + this.m22 + " " + this.m23 + "\n" +
				this.m30 + " " + this.m31 + " " + this.m32 + " " + this.m33 + "\n";
	}
	
	@Override
	public DhApiMat4f copy() { return new DhApiMat4f(this); }
	
}
