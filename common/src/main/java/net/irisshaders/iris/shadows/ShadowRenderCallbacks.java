package net.irisshaders.iris.shadows;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisShadowRenderCallback;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class ShadowRenderCallbacks {
	private static final List<IrisShadowRenderCallback> CALLBACKS = new ArrayList<>();

	private ShadowRenderCallbacks() {}

	public static void register(IrisShadowRenderCallback callback) {
		CALLBACKS.add(callback);
	}

	public static boolean isEmpty() {
		return CALLBACKS.isEmpty();
	}

	public static void invoke(Matrix4f modelView, Matrix4f projection, double cameraX, double cameraY, double cameraZ, float tickDelta) {
		for (IrisShadowRenderCallback callback : CALLBACKS) {
			try {
				callback.renderShadow(modelView, projection, cameraX, cameraY, cameraZ, tickDelta);
			} catch (Throwable t) {
				Iris.logger.error("Error while running shadow render callback", t);
			}
		}
	}
}
