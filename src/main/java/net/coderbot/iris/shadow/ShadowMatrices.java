package net.coderbot.iris.shadow;

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

	private static final class Tests {
		public static void main(String[] args) {
			float[] expected = new float[] {
				0.03125f, 0f, 0f, 0f,
				0f, 0.03125f, 0f, 0f,
				0f, 0f, -0.007814026437699795f, 0f,
				0f, 0f, -1.000390648841858f, 1f
			};

			test("ortho projection hpl=32", expected, createOrthoMatrix(32.0f));
		}

		private static void test(String name, float[] expected, float[] created) {
			if (!Arrays.equals(expected, created)) {
				System.err.println("test " + name + " failed: ");
				System.err.println("    expected: ");
				System.err.print(printMatrix(expected, 8));
				System.err.println("    created: " + Arrays.toString(created));
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
