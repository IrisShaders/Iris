package net.irisshaders.iris.compat.dh;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DHCompat {
	private static boolean dhPresent = true;
	private static boolean lastIncompatible;
	private static MethodHandle setupEventHandlers;
	private static MethodHandle deletePipeline;
	private static MethodHandle incompatible;
	private static MethodHandle getDepthTex;
	private static MethodHandle getFarPlane;
	private static MethodHandle getNearPlane;
	private static MethodHandle getDepthTexNoTranslucent;
	private static MethodHandle checkFrame;
	private static MethodHandle getRenderDistance;
	private static MethodHandle renderShadowSolid;
	private static MethodHandle renderShadowTranslucent;
	private Object compatInternalInstance;

	public DHCompat(IrisRenderingPipeline pipeline, boolean renderDHShadow) {
		try {
			if (dhPresent) {
				compatInternalInstance = Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal").getDeclaredConstructor(pipeline.getClass(), boolean.class).newInstance(pipeline, renderDHShadow);
				lastIncompatible = (boolean) incompatible.invoke(compatInternalInstance);
			}
		} catch (Throwable e) {
			lastIncompatible = false;
			if (e.getCause() instanceof ShaderCompileException sce) {
				throw sce;
			} else {
				throw new RuntimeException(e);
			}
		}

	}

	public static Matrix4f getProjection() {
		if (!dhPresent) {
			return new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
		}

		Matrix4f projection = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
		return new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), DHCompat.getNearPlane(), DHCompat.getFarPlane());
	}

	public static void run() {
		try {
			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				deletePipeline = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "clear", MethodType.methodType(void.class));
				setupEventHandlers = MethodHandles.lookup().findStatic(Class.forName("net.irisshaders.iris.compat.dh.LodRendererEvents"), "setupEventHandlers", MethodType.methodType(void.class));
				getDepthTex = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getStoredDepthTex", MethodType.methodType(int.class));
				getRenderDistance = MethodHandles.lookup().findStatic(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getRenderDistance", MethodType.methodType(int.class));
				incompatible = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "incompatiblePack", MethodType.methodType(boolean.class));
				getFarPlane = MethodHandles.lookup().findStatic(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getFarPlane", MethodType.methodType(float.class));
				getNearPlane = MethodHandles.lookup().findStatic(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getNearPlane", MethodType.methodType(float.class));
				getDepthTexNoTranslucent = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getDepthTexNoTranslucent", MethodType.methodType(int.class));
				checkFrame = MethodHandles.lookup().findStatic(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "checkFrame", MethodType.methodType(boolean.class));
				renderShadowSolid = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "renderShadowSolid", MethodType.methodType(void.class));
				renderShadowTranslucent = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "renderShadowTranslucent", MethodType.methodType(void.class));

				setupEventHandlers.invoke();
			} else {
				dhPresent = false;
			}
		} catch (Throwable e) {
			dhPresent = false;

			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				throw new RuntimeException("DH 2.0 not found, yet Fabric claims it's there. Curious.", e);
			} else {
				Iris.logger.info("DH not found, and classes not found.");
			}
		}
	}

	public static boolean lastPackIncompatible() {
		return dhPresent && hasRenderingEnabled() && lastIncompatible;
	}

	public static float getFarPlane() {
		if (!dhPresent) return 0.01f;

		try {
			return (float) getFarPlane.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static float getNearPlane() {
		if (!dhPresent) return 0.01f;

		try {
			return (float) getNearPlane.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static int getRenderDistance() {
		if (!dhPresent) return Minecraft.getInstance().options.getEffectiveRenderDistance();

		try {
			return (int) getRenderDistance.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean checkFrame() {
		try {
			return (boolean) checkFrame.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean hasRenderingEnabled() {
		if (!dhPresent) {
			return false;
		}

		return checkFrame();
	}

	public void clearPipeline() {
		if (compatInternalInstance == null) return;

		try {
			deletePipeline.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public int getDepthTex() {
		if (compatInternalInstance == null) throw new IllegalStateException("Couldn't find DH depth texture");

		try {
			return (int) getDepthTex.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public int getDepthTexNoTranslucent() {
		if (compatInternalInstance == null) throw new IllegalStateException("Couldn't find DH depth texture");

		try {
			return (int) getDepthTexNoTranslucent.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void renderShadowSolid() {
		if (compatInternalInstance == null) return;

		try {
			renderShadowSolid.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void renderShadowTranslucent() {
		if (compatInternalInstance == null) return;

		try {
			renderShadowTranslucent.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public Object getInstance() {
		return compatInternalInstance;
	}
}
