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
 * Often used to store block positions or any other
 * position in 3D space.
 *
 * @author James Seibel
 * @version 2024-6-3
 * @since API 2.2.0
 */
public class DhApiVec3f implements IDhApiCopyable
{
	public float x;
	public float y;
	public float z;
	
	
	
	/** creates a Vec3 at (0,0,0) */
	public DhApiVec3f()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public DhApiVec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj != null && this.getClass() == obj.getClass())
		{
			DhApiVec3f Vec3f = (DhApiVec3f) obj;
			if (Float.compare(Vec3f.x, this.x) != 0)
			{
				return false;
			}
			else if (Float.compare(Vec3f.y, this.y) != 0)
			{
				return false;
			}
			else
			{
				return Float.compare(Vec3f.z, this.z) == 0;
			}
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		int i = Float.floatToIntBits(this.x);
		i = 31 * i + Float.floatToIntBits(this.y);
		return 31 * i + Float.floatToIntBits(this.z);
	}
	
	@Override
	public String toString() { return "[" + this.x + ", " + this.y + ", " + this.z + "]"; }
	
	@Override 
	public DhApiVec3f copy() { return new DhApiVec3f(this.x, this.y, this.z); }
	
}
