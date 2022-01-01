package net.irisshaders.iris.api.v0;

import net.coderbot.iris.IrisApiV0Impl;

public interface IrisApi {
	static IrisApi getInstance() {
		return IrisApiV0Impl.INSTANCE;
	}

	/**
	 * Gets the minor revision of this API. This is incremented when
	 * new methods are added without breaking API. Mods can check this
	 * if they wish to check whether given API calls are available on
	 * the currently installed Iris version.
	 *
	 * @return The current minor revision. Currently, revision 0.
	 */
	int getMinorApiRevision();

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
	 * @since {@link #getMinorApiRevision() API v0.0}
	 */
	boolean isShaderPackInUse();

	/**
	 * Checks whether the shadow pass is currently being rendered.
	 *
	 * <p>Generally, mods won't need to call this function for much.
	 * Mods should be fine with things being rendered multiple times
	 * each frame from different camera perspectives. Often, there's
	 * a better approach to fixing bugs than calling this function.
	 *
	 * <p>Pretty much the main legitimate use for this function that
	 * I've seen is in a mod like Immersive Portals, where it has
	 * very custom culling that doesn't work when the Iris shadow
	 * pass is active.
	 *
	 * <p>Naturally, this function can only return true if
	 * {@link #isShaderPackInUse()} returns true.
	 *
	 * @return Whether Iris is currently rendering the shadow pass.
	 * @since API v0.0
	 */
	boolean isRenderingShadowPass();
}
