package net.coderbot.iris.uniforms;

import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.client.Minecraft;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#camera">Uniforms: Camera</a>
 */
public class CameraUniforms {
	private static final Minecraft client = Minecraft.getInstance();

	private CameraUniforms() {
	}

	public static void addCameraUniforms(UniformHolder uniforms, FrameUpdateNotifier notifier) {
		CameraPositionTracker tracker = new CameraPositionTracker(notifier);

		uniforms
			.uniform1f(ONCE, "near", () -> 0.05)
			.uniform1f(PER_FRAME, "far", CameraUniforms::getRenderDistanceInBlocks)
			.uniform3d(PER_FRAME, "cameraPosition", tracker::getCurrentCameraPosition)
			.uniform3d(PER_FRAME, "previousCameraPosition", tracker::getPreviousCameraPosition);
	}

	private static int getRenderDistanceInBlocks() {
		// TODO: Should we ask the game renderer for this?
		return client.options.renderDistance * 16;
	}

	public static Vector3d getUnshiftedCameraPosition() {
		return JomlConversions.fromVec3(client.gameRenderer.getMainCamera().getPosition());
	}

	static class CameraPositionTracker {
		/**
		 * Value range of cameraPosition. We want this to be small enough that precision is maintained when we convert
		 * from a double to a float, but big enough that shifts happen infrequently, since each shift corresponds with
		 * a noticeable change in shader animations and similar. 1024 is a reasonable compromise, since this means that
		 * a player has to walk 1024 blocks on any axis in order to see a shift while playing.
		 */
		private static final double RANGE = 1024.0;

		private Vector3d previousCameraPosition = new Vector3d();
		private Vector3d currentCameraPosition = new Vector3d();
		private final Vector3d shift = new Vector3d();

		CameraPositionTracker(FrameUpdateNotifier notifier) {
			notifier.addListener(this::update);
		}

		private void update() {
			previousCameraPosition = currentCameraPosition;
			currentCameraPosition = getUnshiftedCameraPosition().add(shift);

			updateShift();
		}

		/**
		 * Updates our shift values to try to keep |x| < 1024 and |z| < 1024, to maintain precision with cameraPosition.
		 * Since our actual range is 2048x2048, this means that we won't excessively move back and forth when moving
		 * around a chunk border.
		 */
		private void updateShift() {
			double dX = getShift(currentCameraPosition.x, RANGE);
			double dZ = getShift(currentCameraPosition.z, RANGE);

			if (dX != 0.0 || dZ != 0.0) {
				applyShift(dX, dZ);
			}
		}

		private static double getShift(double value, double range) {
			if (Math.abs(value) > range) {
				// Only shift by increments of RANGE - this is required for some packs (like SEUS PTGI) to work properly
				return -(value - (value % RANGE));
			} else {
				return 0.0;
			}
		}

		/**
		 * Shifts all current and future positions by the given amount. This is done in such a way that the difference
		 * between cameraPosition and previousCameraPosition remains the same, since they are completely arbitrary
		 * to the shader for the most part.
		 */
		private void applyShift(double dX, double dZ) {
			shift.x += dX;
			currentCameraPosition.x += dX;
			previousCameraPosition.x += dX;

			shift.z += dZ;
			currentCameraPosition.z += dZ;
			previousCameraPosition.z += dZ;
		}

		public Vector3d getCurrentCameraPosition() {
			return currentCameraPosition;
		}

		public Vector3d getPreviousCameraPosition() {
			return previousCameraPosition;
		}
	}
}
