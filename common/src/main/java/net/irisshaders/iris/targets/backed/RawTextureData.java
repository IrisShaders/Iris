package net.irisshaders.iris.targets.backed;

import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RawTextureData {
	private RawTextureData() {
	}

	public static ByteBuffer convert(byte[] pixels, int texels, InternalTextureFormat internal, PixelFormat sourceFormat, PixelType type) {
		var target = internal.toGpuFormat();
		boolean integer = sourceFormat.isInteger();

		if (integer != internal.getPixelFormat().isInteger()) {
			throw new IllegalArgumentException("Integer texture upload format mismatch");
		}

		switch (type) {
			case BYTE, UNSIGNED_BYTE, SHORT, UNSIGNED_SHORT, INT, UNSIGNED_INT, HALF_FLOAT, FLOAT -> {
			}
			default -> throw new UnsupportedOperationException("Packed raw pixel conversion is not implemented: " + type);
		}

		int inputChannels = sourceFormat.getComponentCount();
		int stride = Math.multiplyExact(inputChannels, type.getByteSize());

		if (pixels.length < Math.multiplyExact(texels, stride)) {
			throw new IllegalArgumentException("Raw texture data is too short");
		}

		var input = ByteBuffer.wrap(pixels).order(ByteOrder.nativeOrder());
		var output = ByteBuffer.allocateDirect(Math.multiplyExact(texels, target.blockSize())).order(ByteOrder.nativeOrder());
		boolean bgr = sourceFormat == PixelFormat.BGR || sourceFormat == PixelFormat.BGRA || sourceFormat == PixelFormat.BGR_INTEGER ||
			sourceFormat == PixelFormat.BGRA_INTEGER;

		for (int i = 0; i < texels; i++) {
			for (int channel = 0; channel < target.componentCount(); channel++) {
				int sourceChannel = bgr && channel < 3 ? 2 - channel : channel;
				double value = channel == 3 ? 1 : 0;

				if (channel < internal.getPixelFormat().getComponentCount() && sourceChannel < inputChannels) {
					value = read(input, i * stride + sourceChannel * type.getByteSize(), type, integer);
				}

				switch (target.componentType()) {
					case UNORM_8 -> output.put((byte) Math.round(Math.clamp(value, 0, 1) * 255));
					case SNORM_8 -> output.put((byte) Math.round(Math.clamp(value, -1, 1) * 127));
					case UNORM_16 -> output.putShort((short) Math.round(Math.clamp(value, 0, 1) * 65535));
					case SNORM_16 -> output.putShort((short) Math.round(Math.clamp(value, -1, 1) * 32767));
					case UINT_8 -> output.put((byte) (int) Math.clamp(value, 0, 255));
					case SINT_8 -> output.put((byte) (int) Math.clamp(value, -128, 127));
					case UINT_16 -> output.putShort((short) (int) Math.clamp(value, 0, 65535));
					case SINT_16 -> output.putShort((short) (int) Math.clamp(value, -32768, 32767));
					case UINT_32 -> output.putInt((int) (long) Math.clamp(value, 0, 4294967295.0));
					case SINT_32 -> output.putInt((int) Math.clamp(value, -2147483648.0, 2147483647.0));
					case FLOAT_16 -> output.putShort(Float.floatToFloat16((float) value));
					case FLOAT_32 -> output.putFloat((float) value);
					default -> throw new UnsupportedOperationException("Packed raw image format is not implemented: " + internal);
				}
			}
		}

		return output.flip();
	}

	private static double read(ByteBuffer input, int offset, PixelType type, boolean integer) {
		return switch (type) {
			case BYTE -> integer ? input.get(offset) : Math.max(-1, input.get(offset) / 127.0);
			case UNSIGNED_BYTE -> integer ? Byte.toUnsignedInt(input.get(offset)) : Byte.toUnsignedInt(input.get(offset)) / 255.0;
			case SHORT -> integer ? input.getShort(offset) : Math.max(-1, input.getShort(offset) / 32767.0);
			case UNSIGNED_SHORT ->
				integer ? Short.toUnsignedInt(input.getShort(offset)) : Short.toUnsignedInt(input.getShort(offset)) / 65535.0;
			case INT -> integer ? input.getInt(offset) : Math.max(-1, input.getInt(offset) / 2147483647.0);
			case UNSIGNED_INT ->
				integer ? Integer.toUnsignedLong(input.getInt(offset)) : Integer.toUnsignedLong(input.getInt(offset)) / 4294967295.0;
			case HALF_FLOAT -> Float.float16ToFloat(input.getShort(offset));
			case FLOAT -> input.getFloat(offset);
			default -> throw new UnsupportedOperationException("Raw pixel type: " + type);
		};
	}
}
