package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.DhApi;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DHCompat {
	private static boolean dhPresent = true;
	private static Object compatInternalInstance;
	private static MethodHandle createNewPipeline;
	private static MethodHandle deletePipeline;
	private static MethodHandle getDepthTex;
	private static MethodHandle getFarPlane;
	private static MethodHandle getNearPlane;
	private static MethodHandle getDepthTexNoTranslucent;
	private static MethodHandle checkFrame;
	private static MethodHandle getRenderDistance;
	private static MethodHandle renderShadowSolid;
	private static MethodHandle renderShadowTranslucent;

	public static Matrix4f getProjection() {
		if (compatInternalInstance == null) {
			return new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
		}

		Matrix4f projection = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
		return new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), DHCompat.getNearPlane(), DHCompat.getFarPlane());
	}

	public static void run() {
		try {
			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				LodRendererEvents.setupEventHandlers();

				compatInternalInstance = Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal").getField("INSTANCE").get(null);
				createNewPipeline = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "prepareNewPipeline", MethodType.methodType(void.class, IrisRenderingPipeline.class, boolean.class));
				deletePipeline = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "clear", MethodType.methodType(void.class));
				getDepthTex = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getStoredDepthTex", MethodType.methodType(int.class));
				getRenderDistance = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getRenderDistance", MethodType.methodType(int.class));
				getFarPlane = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getFarPlane", MethodType.methodType(float.class));
				getNearPlane = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getNearPlane", MethodType.methodType(float.class));
				getDepthTexNoTranslucent = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "getDepthTexNoTranslucent", MethodType.methodType(int.class));
				checkFrame = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "checkFrame", MethodType.methodType(void.class));
				renderShadowSolid = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "renderShadowSolid", MethodType.methodType(void.class));
				renderShadowTranslucent = MethodHandles.lookup().findVirtual(Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal"), "renderShadowTranslucent", MethodType.methodType(void.class));
			}
		} catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
			dhPresent = false;

			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				throw new RuntimeException("DH 2.0 not found, yet Fabric claims it's there. Curious.", e);
			} else {
				Iris.logger.info("DH not found, and classes not found.");
			}
		}
	}

	public static void connectNewPipeline(IrisRenderingPipeline pipeline, boolean renderDhShadow) {
		if (compatInternalInstance == null) return;
		try {
			createNewPipeline.invoke(compatInternalInstance, pipeline, renderDhShadow);
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
		if (compatInternalInstance == null) return Minecraft.getInstance().options.getEffectiveRenderDistance();

		try {
			return (int) getRenderDistance.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void renderShadowSolid() {
		if (compatInternalInstance == null) return;

		try {
			renderShadowSolid.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void renderShadowTranslucent() {
		if (compatInternalInstance == null) return;

		try {
			renderShadowTranslucent.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void checkFrame() {
		if (compatInternalInstance == null) return;

		try {
			checkFrame.invoke(compatInternalInstance);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	public static boolean hasRenderingEnabled() {
		if (!dhPresent) {
			return false;
		}


		try {
			if (DhApi.Delayed.configs == null) {
				// DH hasn't finished loading yet
				return false;
			}

			return DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
		} catch (NoClassDefFoundError e) {
			// if Distant Horizons isn't present the dhPresent
			// variable should already be set to false,
			// but this try-catch is present just in case

			dhPresent = false;
			return false;
		}
	}

}
