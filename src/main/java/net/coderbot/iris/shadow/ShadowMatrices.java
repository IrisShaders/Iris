package net.coderbot.iris.shadow;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import java.nio.FloatBuffer;

public class ShadowMatrices {
	private static final float NEAR = 0.05f;
	private static final float FAR = 256.0f;

	// NB: These matrices are in column-major order, not row-major order like what you'd expect!

	public static float[] createOrthoMatrix(float halfPlaneLength) {
		return new float[] {
				// column 1
				1.0f / halfPlaneLength, 0f, 0f, 0f,
				// column 2
				0f, 1.0f / halfPlaneLength, 0f, 0f,
				// column 3
				0f, 0f, 2.0f / (NEAR - FAR), 0f,
				// column 4
				0f, 0f, -(FAR + NEAR) / (FAR - NEAR), 1f
		};
	}

	public static float[] createPerspectiveMatrix(float fov) {
		// This converts from degrees to radians.
		float yScale = (float) (1.0f / Math.tan(Math.toRadians(fov) * 0.5f));
		return new float[] {
				// column 1
				yScale, 0f, 0f, 0f,
				// column 2
				0f, yScale, 0f, 0f,
				// column 3
				0f, 0f, (FAR + NEAR) / (NEAR - FAR), -1.0F,
				// column 4
				0f, 0f, 2.0F * FAR * NEAR / (NEAR - FAR), 1f
		};
	}

	public static void createBaselineModelViewMatrix(Matrix4f target, float shadowAngle, float sunPathRotation) {
		float skyAngle;

		if (shadowAngle < 0.25f) {
			skyAngle = shadowAngle + 0.75f;
		} else {
			skyAngle = shadowAngle - 0.25f;
		}

		target.setIdentity();
		target.multiply(Matrix4f.createTranslateMatrix(0.0f, 0.0f, -100.0f));
		target.multiply(Vector3f.XP.rotationDegrees(90.0F));
		target.multiply(Vector3f.ZP.rotationDegrees(skyAngle * -360.0f));
		target.multiply(Vector3f.XP.rotationDegrees(sunPathRotation));
	}

	public static void snapModelViewToGrid(Matrix4f target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ) {
		// Calculate where we are within each grid "cell"
		// These values will be in the range of (-shadowIntervalSize, shadowIntervalSize)
		//
		// It looks like it's intended for these to be within the range [0, shadowIntervalSize), however since the
		// expression (-2.0f % 32.0f) returns -2.0f, negative inputs will result in negative outputs.
		float offsetX = (float) cameraX % shadowIntervalSize;
		float offsetY = (float) cameraY % shadowIntervalSize;
		float offsetZ = (float) cameraZ % shadowIntervalSize;

		// Halve the size of each grid cell in order to move to the center of it.
		float halfIntervalSize = shadowIntervalSize / 2.0f;

		// Shift by -halfIntervalSize
		//
		// It's clear that the intent of the algorithm was to place the values into the range:
		// [-shadowIntervalSize/2, shadowIntervalSize), however due to the previously-mentioned behavior with negatives,
		// it's possible that values will lie in the range (-3shadowIntervalSize/2, shadowIntervalSize/2).
		offsetX -= halfIntervalSize;
		offsetY -= halfIntervalSize;
		offsetZ -= halfIntervalSize;

		target.multiply(Matrix4f.createTranslateMatrix(offsetX, offsetY, offsetZ));
	}

	public static void createModelViewMatrix(Matrix4f target, float shadowAngle, float shadowIntervalSize,
											 float sunPathRotation, double cameraX, double cameraY, double cameraZ) {
		createBaselineModelViewMatrix(target, shadowAngle, sunPathRotation);
		snapModelViewToGrid(target, shadowIntervalSize, cameraX, cameraY, cameraZ);
	}

	private static final class Tests {
		public static void main(String[] args) {
			// const float shadowDistance = 32.0;
			// /* SHADOWHPL:32.0 */
			float[] expected = new float[] {
					0.03125f, 0f, 0f, 0f,
					0f, 0.03125f, 0f, 0f,
					0f, 0f, -0.007814026437699795f, 0f,
					0f, 0f, -1.000390648841858f, 1f
			};

			test("ortho projection hpl=32", expected, createOrthoMatrix(32.0f));

			// const float shadowDistance = 110.0;
			// /* SHADOWHPL:110.0 */
			float[] expected110 = new float[] {
					0.00909090880304575f, 0, 0, 0,
					0, 0.00909090880304575f, 0, 0,
					0, 0, -0.007814026437699795f, 0,
					0, 0, -1.000390648841858f, 1
			};

			test("ortho projection hpl=110", expected110, createOrthoMatrix(110.0f));

			float[] expected90Proj = new float[] {
					1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					0.0f, 0.0f, -1.0003906f, -1.0f,
					0.0f, 0.0f, -0.10001954f, 0.0f
			};

			test("perspective projection fov=90", expected90Proj, createPerspectiveMatrix(90.0f));

			float[] expectedModelViewAtDawn = new float[] {
					// column 1
					0.21545040607452393f,
					5.820481518981069E-8f,
					0.9765146970748901f,
					0,
					// column 2
					-0.9765147466795349f,
					1.2841844920785661E-8f,
					0.21545039117336273f,
					0,
					// column 3
					0,
					-0.9999999403953552f,
					5.960464477539063E-8f,
					0,
					// column 4
					0.38002151250839233f,
					1.0264281034469604f,
					-100.4463119506836f,
					1
			};

			Matrix4f modelView = new Matrix4f();

			// NB: At dawn, the shadow angle is NOT zero.
			// When DayTime=0, skyAngle = 282 degrees.
			// Thus, sunAngle = shadowAngle = 0.03451777f
			createModelViewMatrix(modelView, 0.03451777f, 2.0f,
					0.0f, 0.646045982837677f, 82.53274536132812f, -514.0264282226562f);

			test("model view at dawn", expectedModelViewAtDawn, toFloatArray(modelView));
		}

		private static float[] toFloatArray(Matrix4f matrix4f) {
			FloatBuffer buffer = FloatBuffer.allocate(16);
			matrix4f.store(buffer);

			return buffer.array();
		}

		private static void test(String name, float[] expected, float[] created) {
			if (!areMatricesEqualWithinEpsilon(expected, created)) {
				System.err.println("test " + name + " failed: ");
				System.err.println("    expected: ");
				System.err.print(printMatrix(expected, 8));
				System.err.println("    created: ");
				System.err.print(printMatrix(created, 8));
			} else {
				System.out.println("test " + name + " passed");
			}
		}

		private static boolean areMatricesEqualWithinEpsilon(float[] expected, float[] created) {
			for (int i = 0; i < 16; i++) {
				if (Math.abs(expected[i] - created[i]) > 0.0005f) {
					return false;
				}
			}

			return true;
		}

		private static String printMatrix(float[] matrix, int spaces) {
			StringBuilder lines = new StringBuilder();

			for (int row = 0; row < 4; row++) {
				for (int i = 0; i < spaces; i++) {
					lines.append(' ');
				}

				lines.append('[');

				for (int column = 0; column < 4; column++) {
					lines.append(' ');
					lines.append(matrix[column * 4 + row]);
				}

				lines.append(" ]");

				lines.append('\n');
			}

			return lines.toString();
		}
	}
}
