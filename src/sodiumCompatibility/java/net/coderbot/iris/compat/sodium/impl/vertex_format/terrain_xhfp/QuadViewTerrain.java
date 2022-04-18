package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import net.coderbot.iris.compat.sodium.impl.vertex_format.QuadView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class QuadViewTerrain extends QuadView {
	public long writePointer;
	int stride;

	public float x(int index) {
		return normalizeVertexPositionShortAsFloat(getShort(writePointer - stride * (3L - index)));
	}

	public float y(int index) {
		return normalizeVertexPositionShortAsFloat(getShort(writePointer + 2 - stride * (3L - index)));
	}

	public float z(int index) {
		return normalizeVertexPositionShortAsFloat(getShort(writePointer + 4 - stride * (3L - index)));
	}

	@Override
	public float u(int index) {
		return normalizeVertexTextureShortAsFloat(getShort(writePointer + 12 - stride * (3L - index)));
	}

	@Override
	public float v(int index) {
		return normalizeVertexTextureShortAsFloat(getShort(writePointer + 14 - stride * (3L - index)));
	}

	private static float normalizeVertexTextureShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 32768.0f);
	}

	private static float normalizeVertexPositionShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 65535.0f);
	}

	abstract short getShort(long writePointer);

	public static class QuadViewTerrainUnsafe extends QuadViewTerrain {
		public void setup(long writePointer, int stride) {
			this.writePointer = writePointer;
			this.stride = stride;
		}

		@Override
		short getShort(long writePointer) {
			return MemoryUtil.memGetShort(writePointer);
		}
	}

	public static class QuadViewTerrainNio extends QuadViewTerrain {
		ByteBuffer buffer;

		public void setup(ByteBuffer buffer, int writeOffset, int stride) {
			this.buffer = buffer;
			this.writePointer = writeOffset;
			this.stride = stride;
		}

		@Override
		short getShort(long writePointer) {
			return buffer.getShort((int) writePointer);
		}
	}
}
