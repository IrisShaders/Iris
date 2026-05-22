package net.irisshaders.iris.vertices;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressWarnings("all")
public final class MemoryAccess {
	private static final Unsafe U = getUnsafe();

	private static Unsafe getUnsafe() {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			return (Unsafe) f.get(null);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static int getInt(long ptr) {
		return U.getInt(ptr);
	}

	public static float getFloat(long ptr) {
		return U.getFloat(ptr);
	}

	public static long getLong(long ptr) {
		return U.getLong(ptr);
	}

	public static short getShort(long ptr) {
		return U.getShort(ptr);
	}

	public static byte getByte(long ptr) {
		return U.getByte(ptr);
	}

	public static void setInt(long ptr, int value) {
		U.putInt(ptr, value);
	}

	public static void setFloat(long ptr, float value) {
		U.putFloat(ptr, value);
	}

	public static void setLong(long ptr, long value) {
		U.putLong(ptr, value);
	}

	public static void setShort(long ptr, short value) {
		U.putShort(ptr, value);
	}

	public static void setByte(long ptr, byte value) {
		U.putByte(ptr, value);
	}

	public static void copy(long src, long dst, long size) {
		U.copyMemory(src, dst, size);
	}
}
