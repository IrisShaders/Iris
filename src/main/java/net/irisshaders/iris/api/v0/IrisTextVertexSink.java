package net.irisshaders.iris.api.v0;

import com.mojang.blaze3d.vertex.VertexFormat;

import java.nio.ByteBuffer;

public interface IrisTextVertexSink {
	/**
	 * Gets the underlying vertex format used for rendering text.
	 *
	 * @return a valid {@code VertexFormat} instance
	 */
	VertexFormat getUnderlyingVertexFormat();

	/**
	 * Gets the underlying buffer used for rendering text in the current sink.
	 *
	 * @return a valid {@code ByteBuffer}
	 */
	ByteBuffer getUnderlyingByteBuffer();

	/**
	 * Writes a singular quad with all vertex attributes needed by the current format into the current {@code ByteBuffer}.
	 *
	 * @param x1    Left-most x coordinate of the quad
	 * @param y1    Top Y coordinate of the quad
	 * @param x2    Right-most x coordinate of the quad
	 * @param y2    Bottom Y coordinate of the quad
	 * @param z     Z coordinate of the quad
	 * @param color Integer-packed ABGR value, with the equation {@code int color = ((int) (a * 255.0F) & 0xFF) << 24 | ((int) (b * 255.0F) & 0xFF) << 16 | ((int) (g * 255.0F) & 0xFF) << 8 | ((int) (r * 255.0F) & 0xFF)}
	 * @param u1    Top-left U coordinate of the quad texture
	 * @param v1    Top-left V coordinate of the quad texture
	 * @param u2    Bottom-right U coordinate of the quad texture
	 * @param v2    Bottom right V coordinate of the quad texture
	 * @param light Integer packed light coordinate
	 */
	void quad(float x1, float y1, float x2, float y2, float z, int color, float u1, float v1, float u2, float v2, int light);
}
