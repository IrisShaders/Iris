package net.irisshaders.iris.compat;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;

public class SkipList {
	public static final MethodHandle NONE = MethodHandles.constant(Integer.class, 2);
	public static final MethodHandle ALWAYS = MethodHandles.constant(Integer.class, 1);
	public static Map<Class<?>, MethodHandle> shouldSkipList = new Object2ObjectOpenHashMap<>();
}
