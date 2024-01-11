package net.coderbot.iris.compat.dh;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.core.config.Config;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DHCompat {
	private GlFramebuffer fb;

	public int getFramebuffer() {
		return fb.getId();
	}

	public void setFramebuffer(GlFramebuffer fb) {
		this.fb = fb;
	}

	private static Field renderingEnabled;
	private static MethodHandle renderingEnabledGet;
	private static Object compatInternalInstance;
	private static MethodHandle createNewPipeline;
	private static MethodHandle deletePipeline;

	static {
        try {
			renderingEnabled = Class.forName("com.seibel.distanthorizons.core.config.Config$Client").getField("quickEnableRendering");
			renderingEnabledGet = MethodHandles.lookup().findVirtual(Class.forName("com.seibel.distanthorizons.core.config.types.ConfigEntry"), "get", MethodType.methodType(Object.class));
			if (FabricLoader.getInstance().isModLoaded("distanthorizons")) {
				compatInternalInstance = Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal").getField("INSTANCE").get(null);
				createNewPipeline = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "prepareNewPipeline", MethodType.methodType(void.class, NewWorldRenderingPipeline.class));
				deletePipeline = MethodHandles.lookup().findVirtual(Class.forName("net.coderbot.iris.compat.dh.DHCompatInternal"), "clear", MethodType.methodType(void.class));
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
		if (compatInternalInstance == null) throw new IllegalStateException("missingno");
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

	public static boolean hasRenderingEnabled() {
		if (renderingEnabledGet == null) return false;

		try {
            return (boolean) renderingEnabledGet.invoke(renderingEnabled.get(null));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
