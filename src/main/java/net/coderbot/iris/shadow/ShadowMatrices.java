package net.coderbot.iris.shadow;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix4f;

import java.nio.FloatBuffer;
import java.util.Arrays;

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

	public static void createModelViewMatrix(Matrix4f target, float shadowAngle) {
		target.loadIdentity();
		target.multiply(Matrix4f.translate(0.0f, 0.0f, -100.0f));
		target.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F));
		target.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90.0f + shadowAngle * -360.0f));
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
				0,
				0,
				-100,
				1,
			};

			Matrix4f modelView = new Matrix4f();
			// shadow angle = 0 implies dawn
			// This is odd, why is it not zero to get close? Need to go back and check the source numbers...
			// 0.03455 ~= 12 degrees
			createModelViewMatrix(modelView, 0.03455f);

			test("model view at dawn", expectedModelViewAtDawn, toFloatArray(modelView));
		}

		private static float[] toFloatArray(Matrix4f matrix4f) {
			FloatBuffer buffer = FloatBuffer.allocate(16);
			matrix4f.writeToBuffer(buffer);

			return buffer.array();
		}

		private static void test(String name, float[] expected, float[] created) {
			if (!Arrays.equals(expected, created)) {
				System.err.println("test " + name + " failed: ");
				System.err.println("    expected: ");
				System.err.print(printMatrix(expected, 8));
				System.err.println("    created: ");
				System.err.print(printMatrix(created, 8));
			} else {
				System.out.println("test " + name + " passed");
			}
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
