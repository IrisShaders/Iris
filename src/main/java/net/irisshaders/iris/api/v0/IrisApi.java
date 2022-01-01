package net.irisshaders.iris.api.v0;

import net.coderbot.iris.IrisApiV0Impl;

public interface IrisApi {
	static IrisApi getInstance() {
		return IrisApiV0Impl.INSTANCE;
	}

	/**
	 * Checks whether a shader pack is currently in use and being used
	 * for rendering. If there is no shader pack enabled or a shader
	 * pack failed to compile and is therefore not in use, this will
	 * return false.
	 *
	 * <p>Mods that need to enable custom workarounds for shaders
	 * should use this method.
	 *
	 * @return Whether shaders are being used for rendering.
	 */
	boolean isShaderPackInUse();
}
