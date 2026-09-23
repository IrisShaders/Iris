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
			throw new IllegalArgumentException("integer texture upload format mismatch");
		}

		switch (type) {
			case BYTE, UNSIGNED_BYTE, SHORT, UNSIGNED_SHORT, INT, UNSIGNED_INT, HALF_FLOAT, FLOAT, UNSIGNED_SHORT_5_6_5,
			     UNSIGNED_SHORT_5_6_5_REV, UNSIGNED_SHORT_5_5_5_1, UNSIGNED_SHORT_1_5_5_5_REV -> {
			}

			default -> throw new UnsupportedOperationException("packed raw pixel conversion is not implemented: " + type);
		}

		int inputChannels = sourceFormat.getComponentCount();
		int packedChannels = switch (type) {
			case UNSIGNED_SHORT_5_6_5, UNSIGNED_SHORT_5_6_5_REV -> 3;
			case UNSIGNED_SHORT_5_5_5_1, UNSIGNED_SHORT_1_5_5_5_REV -> 4;
			default -> 0;
		};

		if (packedChannels != 0 && (integer || inputChannels != packedChannels)) {
			throw new IllegalArgumentException("packed raw pixel format mismatch: " + sourceFormat + " " + type);
		}

		int stride = packedChannels != 0 ? type.getByteSize() : Math.multiplyExact(inputChannels, type.getByteSize());

		if (pixels.length < Math.multiplyExact(texels, stride)) {
			throw new IllegalArgumentException("raw texture data is too short");
		}

		var input = ByteBuffer.wrap(pixels).order(ByteOrder.nativeOrder());
		var output = ByteBuffer.allocateDirect(Math.multiplyExact(texels, target.blockSize())).order(ByteOrder.nativeOrder());
		boolean bgr = sourceFormat == PixelFormat.BGR || sourceFormat == PixelFormat.BGRA || sourceFormat == PixelFormat.BGR_INTEGER || sourceFormat == PixelFormat.BGRA_INTEGER;

		boolean packedTarget = internal == InternalTextureFormat.RGB565 || internal == InternalTextureFormat.RGB5_A1;
		int channels = packedTarget ? internal.getPixelFormat().getComponentCount() : target.componentCount();

		for (int i = 0; i < texels; i++) {
			int packed = 0;

			for (int channel = 0; channel < channels; channel++) {
				int sourceChannel = bgr && channel < 3 ? 2 - channel : channel;
				double value = channel == 3 ? 1 : 0;

				if (channel < internal.getPixelFormat().getComponentCount() && sourceChannel < inputChannels) {
					value = packedChannels != 0 ? readPacked(input, i * stride, sourceChannel, type) : read(input,
						i * stride + sourceChannel * type.getByteSize(), type, integer);
				}

				if (packedTarget) {
					int bits = internal == InternalTextureFormat.RGB565 && channel == 1 ? 6 : channel == 3 ? 1 : 5;
					packed = packed << bits | (int) Math.round(Math.clamp(value, 0, 1) * ((1 << bits) - 1));
					continue;
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
					default -> throw new UnsupportedOperationException("packed raw image format is not implemented: " + internal);
				}
			}

			if (packedTarget) {
				if (internal == InternalTextureFormat.RGB5_A1) {
					packed = packed >>> 1 | (packed & 1) << 15;
				}
				output.putShort((short) packed);
			}
		}

		return output.flip();
	}

	private static double readPacked(ByteBuffer input, int offset, int channel, PixelType type) {
		boolean rgb = type == PixelType.UNSIGNED_SHORT_5_6_5 || type == PixelType.UNSIGNED_SHORT_5_6_5_REV;
		int bits = rgb && channel == 1 ? 6 : channel == 3 ? 1 : 5;
		int shift = switch (type) {
			case UNSIGNED_SHORT_5_6_5 -> channel == 0 ? 11 : channel == 1 ? 5 : 0;
			case UNSIGNED_SHORT_5_6_5_REV -> channel == 0 ? 0 : channel == 1 ? 5 : 11;
			case UNSIGNED_SHORT_5_5_5_1 -> channel == 3 ? 0 : 11 - channel * 5;
			case UNSIGNED_SHORT_1_5_5_5_REV -> channel == 3 ? 15 : channel * 5;
			default -> throw new UnsupportedOperationException("packed raw pixel type: " + type);
		};
		int mask = (1 << bits) - 1;
		return (input.getShort(offset) >> shift & mask) / (double) mask;
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
			default -> throw new UnsupportedOperationException("raw pixel type: " + type);
		};
	}
}
