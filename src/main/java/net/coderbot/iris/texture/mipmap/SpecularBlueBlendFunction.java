package net.coderbot.iris.texture.mipmap;

public class SpecularBlueBlendFunction implements ChannelMipmapGenerator.BlendFunction {
	public static final SpecularBlueBlendFunction INSTANCE = new SpecularBlueBlendFunction();

	@Override
	public int blend(int v0, int v1, int v2, int v3) {
		int t0 = v0 < 65 ? 0 : 1;
		int t1 = v1 < 65 ? 0 : 1;
		int t2 = v2 < 65 ? 0 : 1;
		int t3 = v3 < 65 ? 0 : 1;

		int targetType = selectTargetType(t0, t1, t2, t3);

		int sum = 0;
		int amount = 0;
		if (t0 == targetType) {
			sum += v0;
			amount++;
		}
		if (t1 == targetType) {
			sum += v1;
			amount++;
		}
		if (t2 == targetType) {
			sum += v2;
			amount++;
		}
		if (t3 == targetType) {
			sum += v3;
			amount++;
		}

		return sum / amount;
	}

	/**
	 * Selects the type that appears most often among the arguments.
	 * In the case of a tie, types that appear first in the argument list are given priority.
	 * A type is represented by an integer.
	 *
	 * <p><b>Assumes that only two distinct types are present.</b>
	 * */
	public static int selectTargetType(int t0, int t1, int t2, int t3) {
		if (t0 != t1 && t0 != t2) {
			if (t2 == t3) {
				return t2;
			} else {
				return t1;
			}
		}
		return t0;
	}
}
