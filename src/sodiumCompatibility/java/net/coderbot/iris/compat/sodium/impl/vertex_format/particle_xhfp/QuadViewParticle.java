package net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp;

import net.coderbot.iris.compat.sodium.impl.vertex_format.QuadView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class QuadViewParticle extends QuadView {
	long writePointer;
	int stride = 48;

	public float x(int index) {
		return getFloat(writePointer - stride * (4L - index));
	}

	public float y(int index) {
		return getFloat(writePointer + 4 - stride * (4L - index));
	}

	public float z(int index) {
		return getFloat(writePointer + 8 - stride * (4L - index));
	}

	public float u(int index) {
		return getFloat(writePointer + 16 - stride * (4L - index));
	}

	public float v(int index) {
		return getFloat(writePointer + 20 - stride * (4L - index));
	}

	abstract float getFloat(long writePointer);

	public static class QuadViewParticleUnsafe extends QuadViewParticle {
		public void setup(long writePointer, int stride) {
			this.writePointer = writePointer;
			this.stride = stride;
		}

		@Override
		float getFloat(long writePointer) {
			return MemoryUtil.memGetFloat(writePointer);
		}
	}

	public static class QuadViewParticleNio extends QuadViewParticle {
		ByteBuffer buffer;

		public void setup(ByteBuffer buffer, int writeOffset, int stride) {
			this.buffer = buffer;
			this.writePointer = writeOffset;
			this.stride = stride;
		}

		@Override
		float getFloat(long writePointer) {
			return buffer.getFloat((int) writePointer);
		}
	}
}
