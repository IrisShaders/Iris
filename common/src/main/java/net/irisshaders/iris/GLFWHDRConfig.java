package net.irisshaders.iris;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memGetFloat;

public class GLFWHDRConfig extends Struct<GLFWHDRConfig> {
	public static final int SIZEOF, ALIGNOF;

	private static final int OFFSET_TRANSFER_FUNCTION;
	private static final int OFFSET_PRIMARY_RED_X;
	private static final int OFFSET_PRIMARY_RED_Y;
	private static final int OFFSET_PRIMARY_GREEN_X;
	private static final int OFFSET_PRIMARY_GREEN_Y;
	private static final int OFFSET_PRIMARY_BLUE_X;
	private static final int OFFSET_PRIMARY_BLUE_Y;
	private static final int OFFSET_WHITE_POINT_X;
	private static final int OFFSET_WHITE_POINT_Y;
	private static final int OFFSET_MAX_LUMINANCE;
	private static final int OFFSET_MIN_LUMINANCE;
	private static final int OFFSET_MAX_FRAME_LUMINANCE;

	static {
		var layout = __struct(
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES),
			__member(Float.BYTES)
		);

		SIZEOF = layout.getSize();
		ALIGNOF = layout.getAlignment();

		OFFSET_TRANSFER_FUNCTION = layout.offsetof(0);
		OFFSET_PRIMARY_RED_X = layout.offsetof(1);
		OFFSET_PRIMARY_RED_Y = layout.offsetof(2);
		OFFSET_PRIMARY_GREEN_X = layout.offsetof(3);
		OFFSET_PRIMARY_GREEN_Y = layout.offsetof(4);
		OFFSET_PRIMARY_BLUE_X = layout.offsetof(5);
		OFFSET_PRIMARY_BLUE_Y = layout.offsetof(6);
		OFFSET_WHITE_POINT_X = layout.offsetof(7);
		OFFSET_WHITE_POINT_Y = layout.offsetof(8);
		OFFSET_MAX_LUMINANCE = layout.offsetof(9);
		OFFSET_MIN_LUMINANCE = layout.offsetof(10);
		OFFSET_MAX_FRAME_LUMINANCE = layout.offsetof(11);
	}

	GLFWHDRConfig(long address, ByteBuffer container) {
		super(address, container);
	}

	@Override
	protected GLFWHDRConfig create(long address, ByteBuffer container) {
		return new GLFWHDRConfig(address, container);
	}

	public static GLFWHDRConfig create(long address) {
		return new GLFWHDRConfig(address, null);
	}

	public static Buffer calloc(int count) {
		return new Buffer(MemoryUtil.nmemCalloc(count, SIZEOF), count);
	}

	public float getTransferFunction() {
		return memGetFloat(this.address + OFFSET_TRANSFER_FUNCTION);
	}

	public float getPrimaryRedX() {
		return memGetFloat(this.address + OFFSET_PRIMARY_RED_X);
	}

	public float getPrimaryRedY() {
		return memGetFloat(this.address + OFFSET_PRIMARY_RED_Y);
	}

	public float getPrimaryGreenX() {
		return memGetFloat(this.address + OFFSET_PRIMARY_GREEN_X);
	}

	public float getPrimaryGreenY() {
		return memGetFloat(this.address + OFFSET_PRIMARY_GREEN_Y);
	}

	public float getPrimaryBlueX() {
		return memGetFloat(this.address + OFFSET_PRIMARY_BLUE_X);
	}

	public float getPrimaryBlueY() {
		return memGetFloat(this.address + OFFSET_PRIMARY_BLUE_Y);
	}

	public float getWhitePointX() {
		return memGetFloat(this.address + OFFSET_WHITE_POINT_X);
	}

	public float getWhitePointY() {
		return memGetFloat(this.address + OFFSET_WHITE_POINT_Y);
	}

	public float getMaxLuminance() {
		return memGetFloat(this.address + OFFSET_MAX_LUMINANCE);
	}

	public float getMaxFrameLuminance() {
		return memGetFloat(this.address + OFFSET_MAX_FRAME_LUMINANCE);
	}

	public float getMinLuminance() {
		return memGetFloat(this.address + OFFSET_MIN_LUMINANCE);
	}

	@Override
	public int sizeof() {
		return SIZEOF;
	}

	static class Buffer extends StructBuffer<GLFWHDRConfig, Buffer> {
		private static final GLFWHDRConfig ELEMENT_FACTORY = GLFWHDRConfig.create(-1L);

		protected Buffer(long address, int capacity) {
			super(address, null, -1, 0, capacity, capacity);
		}

		@Override
		protected @NotNull GLFWHDRConfig getElementFactory() {
			return ELEMENT_FACTORY;
		}

		@Override
		protected @NotNull Buffer self() {
			return this;
		}
	}
}
