package net.irisshaders.iris.gl.blending;

import net.irisshaders.iris.Iris;
import org.lwjgl.vulkan.VK10;

import java.util.Optional;

public enum BlendModeFunction {
	ZERO(VK10.VK_BLEND_FACTOR_ZERO),
	ONE(VK10.VK_BLEND_FACTOR_ONE),
	SRC_COLOR(VK10.VK_BLEND_FACTOR_SRC_COLOR),
	ONE_MINUS_SRC_COLOR(VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR),
	DST_COLOR(VK10.VK_BLEND_FACTOR_DST_COLOR),
	ONE_MINUS_DST_COLOR(VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR),
	SRC_ALPHA(VK10.VK_BLEND_FACTOR_SRC_ALPHA),
	ONE_MINUS_SRC_ALPHA(VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA),
	DST_ALPHA(VK10.VK_BLEND_FACTOR_DST_ALPHA),
	ONE_MINUS_DST_ALPHA(VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA),
	SRC_ALPHA_SATURATE(VK10.VK_BLEND_FACTOR_SRC_ALPHA_SATURATE);

	private final int vkId;

	BlendModeFunction(int vkFactor) {
		this.vkId = vkFactor;
	}

	public static Optional<BlendModeFunction> fromString(String name) {
		try {
			return Optional.of(BlendModeFunction.valueOf(name));
		} catch (IllegalArgumentException e) {
			Iris.logger.warn("Invalid blend mode! " + name);

			return Optional.empty();
		}
	}

	public int getVkId() {
		return vkId;
	}
}
