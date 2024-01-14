package net.coderbot.iris.compat.dh;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class DHCompat {
	public static Matrix4f getProjection() {
			Matrix4f projection = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
			return new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), DHCompat.getNearPlane(), DHCompat.getFarPlane());
	}

	private static Field renderingEnabled;
	private static MethodHandle renderingEnabledGet;
	private static Object compatInternalInstance;
	private static MethodHandle createNewPipeline;
	private static MethodHandle deletePipeline;
	private static MethodHandle getDepthTex;
	private static MethodHandle getFarPlane;
	private static MethodHandle getNearPlane;
	private static MethodHandle getDepthTexNoTranslucent;
	private static MethodHandle getRenderDistance;

	static {
        try {
			renderingEnabled = Class.forName("com.seibel.distanthorizons.core.config.Config$Client").getField("quickEnableRendering");
			renderingEnabledGet = MethodHandles.lookup().findVirtual(Class.forName("com.seibel.distanthorizons.core.config.types.ConfigEntry"), "get", MethodType.methodType(Object.class));
			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				compatInternalInstance = Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal").getField("INSTANCE").get(null);
				createNewPipeline = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "prepareNewPipeline", MethodType.methodType(void.class, NewWorldRenderingPipeline.class));
				deletePipeline = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "clear", MethodType.methodType(void.class));
				getDepthTex = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "getStoredDepthTex", MethodType.methodType(int.class));
				getRenderDistance = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "getRenderDistance", MethodType.methodType(int.class));
				getFarPlane = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "getFarPlane", MethodType.methodType(float.class));
				getNearPlane = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "getNearPlane", MethodType.methodType(float.class));
				getDepthTexNoTranslucent = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "getDepthTexNoTranslucent", MethodType.methodType(int.class));
			}
		} catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				throw new RuntimeException("DH 2.0 not found, yet Fabric claims it's there. Curious.", e);
			} else {
				Iris.logger.info("DH not found, and classes not found.");
			}
        }
    }

	public static void connectNewPipeline(NewWorldRenderingPipeline pipeline) {
		if (compatInternalInstance == null) return;
        try {
            createNewPipeline.invoke(compatInternalInstance, pipeline);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static void clearPipeline() {
		if (compatInternalInstance == null) return;

		try {
            deletePipeline.invoke(compatInternalInstance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static int getDepthTex() {
		if (compatInternalInstance == null) throw new IllegalStateException("Couldn't find DH depth texture");

		try {
			return (int) getDepthTex.invoke(compatInternalInstance);
		} catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static int getDepthTexNoTranslucent() {
		if (compatInternalInstance == null) throw new IllegalStateException("Couldn't find DH depth texture");

		try {
			return (int) getDepthTexNoTranslucent.invoke(compatInternalInstance);
		} catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static float getFarPlane() {
		if (compatInternalInstance == null) return 0.01f;

		try {
			return (float) getFarPlane.invoke(compatInternalInstance);
		} catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static float getNearPlane() {
		if (compatInternalInstance == null) return 0.01f;

		try {
			return (float) getNearPlane.invoke(compatInternalInstance);
		} catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static int getRenderDistance() {
		if (compatInternalInstance == null) return 0;

		try {
			return (int) getRenderDistance.invoke(compatInternalInstance);
		} catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	public static boolean hasRenderingEnabled() {
		if (renderingEnabledGet == null) return false;

		try {
            return (boolean) renderingEnabledGet.invoke(renderingEnabled.get(null));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
