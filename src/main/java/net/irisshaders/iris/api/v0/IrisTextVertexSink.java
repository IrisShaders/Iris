package net.irisshaders.iris.api.v0;

import com.mojang.blaze3d.vertex.VertexFormat;

import java.nio.ByteBuffer;

public interface IrisTextVertexSink {
	VertexFormat getUnderlyingVertexFormat();
	ByteBuffer getUnderlyingByteBuffer();
	void quad(float x1, float y1, float x2, float y2, float z, int rgba, float u1, float v1, float u2, float v2, int light);
}
