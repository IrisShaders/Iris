package net.coderbot.iris.vertices;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.BufferVertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.GlyphVertexBufferWriterUnsafe;
import net.irisshaders.iris.api.v0.IrisTextVertexSink;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public class IrisTextVertexSinkImpl implements IrisTextVertexSink, VertexBufferView {
	static VertexFormat format = IrisVertexFormats.TERRAIN;
	private final ByteBuffer buffer;
	private int vertexCount;
	private int elementOffset;
	private GlyphVertexBufferWriterUnsafe drain;

	public IrisTextVertexSinkImpl(int maxQuadSize, IntFunction<ByteBuffer> buffer) {
		this.buffer = buffer.apply(format.getVertexSize() * 4 * maxQuadSize);
		this.drain = new GlyphVertexBufferWriterUnsafe(this);
	}

	@Override
	public VertexFormat getUnderlyingVertexFormat() {
		return format;
	}

	@Override
	public void quad(float minX, float minY, float maxX, float maxY, float z, int color, float minU, float minV, float maxU, float maxV, int light) {
		drain.writeGlyph(minX, minY, 0.0F, color, minU, minV, light);
		drain.writeGlyph(minX, maxY, 0.0F, color, minU, maxV, light);
		drain.writeGlyph(maxX, maxY, 0.0F, color, maxU, maxV, light);
		drain.writeGlyph(maxX, minY, 0.0F, color, maxU, minV, light);
	}

	@Override
	public void flush() {
		drain.flush();
	}

	@Override
	public boolean ensureBufferCapacity(int i) {
		return false;
	}

	@Override
	public ByteBuffer getDirectBuffer() {
		return buffer;
	}

	@Override
	public int getWriterPosition() {
		return this.elementOffset;
	}

	@Override
	public void flush(int i, BufferVertexFormat bufferVertexFormat) {
		this.vertexCount += vertexCount;
		this.elementOffset += vertexCount * format.getVertexSize();
	}

	@Override
	public BufferVertexFormat getVertexFormat() {
		return BufferVertexFormat.from(format);
	}
}
