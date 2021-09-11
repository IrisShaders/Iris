package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.function.Supplier;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#camera">Uniforms: Camera</a>
 */
public class CameraUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CameraUniforms() {
	}

	public static void addCameraUniforms(UniformHolder uniforms, FrameUpdateNotifier notifier) {
		uniforms
			.uniform1f(ONCE, "near", () -> 0.05)
			.uniform1f(PER_FRAME, "far", CameraUniforms::getRenderDistanceInBlocks)
			.uniform3d(PER_FRAME, "cameraPosition", CameraUniforms::getCameraPosition)
			.uniform3d(PER_FRAME, "previousCameraPosition", new PreviousCameraPosition(notifier));
	}

	private static int getRenderDistanceInBlocks() {
		return client.options.viewDistance * 16;
	}

	public static Vec3d getCameraPosition() {
		return client.gameRenderer.getCamera().getPos();
	}

	private static class PreviousCameraPosition implements Supplier<Vec3d> {
		private Vec3d previousCameraPosition = new Vec3d(0.0, 0.0, 0.0);
		private Vec3d currentCameraPosition = new Vec3d(0.0, 0.0, 0.0);

		private PreviousCameraPosition(FrameUpdateNotifier notifier) {
			notifier.addListener(this::update);
		}

		private void update() {
			previousCameraPosition = currentCameraPosition;
			currentCameraPosition = getCameraPosition();
		}

		@Override
		public Vec3d get() {
			return previousCameraPosition;
		}
	}
}
