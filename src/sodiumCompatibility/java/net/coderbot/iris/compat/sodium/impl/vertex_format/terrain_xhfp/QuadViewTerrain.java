package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import net.coderbot.iris.vertices.QuadView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class QuadViewTerrain implements QuadView {
	long writePointer;
	int stride;

	@Override
	public float x(int index) {
		return XHFPModelVertexType.decodePosition(getShort(writePointer - stride * (3 - index)));
	}

	@Override
	public float y(int index) {
		return XHFPModelVertexType.decodePosition(getShort(writePointer + 2 - stride * (3 - index)));
	}

	@Override
	public float z(int index) {
		return XHFPModelVertexType.decodePosition(getShort(writePointer + 4 - stride * (3 - index)));
	}

	@Override
	public float u(int index) {
		return XHFPModelVertexType.decodeBlockTexture(getShort(writePointer + 12 - stride * (3 - index)));
	}

	@Override
	public float v(int index) {
		return XHFPModelVertexType.decodeBlockTexture(getShort(writePointer + 14 - stride * (3 - index)));
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
		private ByteBuffer buffer;

		public void setup(ByteBuffer buffer, int writePointer, int stride) {
			this.buffer = buffer;
			this.writePointer = writePointer;
			this.stride = stride;
		}

		@Override
		short getShort(long writePointer) {
			return buffer.getShort((int) writePointer);
		}
	}
}
