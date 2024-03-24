package net.irisshaders.iris.api.v0;

/**
 * @since API v0.0
 */
public interface IrisApiConfig {
	/**
	 * Checks whether there is a shader pack loaded. Note that it is possible for a
	 * shader pack to be loaded, but not in use, if the shader pack failed to compile.
	 * <b>You probably meant to call {@link IrisApi#isShaderPackInUse()}!</b>
	 *
	 * @return Whether a shader pack was loaded from disk
	 * @since API v0.0
	 */
	boolean areShadersEnabled();

	/**
	 * Sets whether shaders are enabled or not, and then applies the change.
	 *
	 * @since API v0.0
	 */
	void setShadersEnabledAndApply(boolean enabled);
}
