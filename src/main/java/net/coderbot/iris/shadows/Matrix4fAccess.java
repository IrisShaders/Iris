package net.coderbot.iris.shadows;

public interface Matrix4fAccess {
	/**
	 * Sets the values of this matrix from an array. The values in the array must be specified in column-major order,
	 * just like with OpenGL. Keep this in mind, since the natural way of laying out a matrix in array form is row-major
	 * order!
	 */
	void copyFromArray(float[] m);

	/**
	 * Gets the values of this matrix into an array. The values in the array will be laid out in column-major order,
	 * just like with OpenGL. Keep this in mind, since the natural way of laying out a matrix in array form is row-major
	 * order!
	 */
	float[] copyIntoArray();

	/**
	 * Converts the matrix into a JOML matrix. This matrix is inherently column-major, and compatible with OpenGL.
	 * @return JOML matrix
	 */
	net.coderbot.iris.vendored.joml.Matrix4f convertToJOML();
}
