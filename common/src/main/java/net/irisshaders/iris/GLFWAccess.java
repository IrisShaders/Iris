package net.irisshaders.iris;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;
import org.lwjgl.system.SharedLibrary;

import java.lang.reflect.Field;

public class GLFWAccess {
	private static SharedLibrary lib;

	static {
		Class<?> clas = GLFW.class;

		try {
			Field field = clas.getDeclaredField("GLFW");
			field.setAccessible(true);
			lib = (SharedLibrary) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static GLFWHDRConfig conf;

	private static boolean done;

	public static void tryHDRConf() {
		if (done) return;
		done = true;
		long addr = JNI.invokePP(Minecraft.getInstance().getWindow().getWindow(), lib.getFunctionAddress("glfwGetHDRConfig"));
		conf = GLFWHDRConfig.create(addr);
		System.out.println("Testing red primary X: " + conf.getPrimaryRedX());
	}
}
