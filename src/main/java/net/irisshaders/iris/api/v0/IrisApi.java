package net.irisshaders.iris.api.v0;

import net.coderbot.iris.apiimpl.IrisApiV0Impl;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

/**
 * The entry point to the Iris API, major version 0. This is currently the latest
 * version of the API.
 *
 * To access the API, use {@link #getInstance()}.
 */
public interface IrisApi {
	/**
	 * @since API v0.0
	 */
	static IrisApi getInstance() {
		return IrisApiV0Impl.INSTANCE;
	}

	/**
	 * Gets the minor revision of this API. This is incremented when
	 * new methods are added without breaking API. Mods can check this
	 * if they wish to check whether given API calls are available on
	 * the currently installed Iris version.
	 *
	 * @return The current minor revision. Currently, revision 1.
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

	/**
	 * Opens the main Iris GUI screen. It's up to Iris to decide
	 * what this screen is, but generally this is the shader selection
	 * screen.
	 *
	 * This method takes and returns Objects instead of any concrete
	 * Minecraft screen class to avoid referencing Minecraft classes.
	 * Nevertheless, the passed parent must either be null, or an
	 * object that is a subclass of the appropriate {@code Screen}
	 * class for the given Minecraft version.
	 *
	 * @param parent The parent screen, an instance of the appropriate
	 *               {@code Screen} class.
	 * @return A {@code Screen} class for the main Iris GUI screen.
	 * @since API v0.0
	 */
	Object openMainIrisScreenObj(Object parent);

	/**
	 * Gets the language key of the main screen. Currently, this
	 * is "options.iris.shaderPackSelection".
	 *
	 * @return the language key, for use with {@code TranslatableText}
	 *        / {@code TranslatableComponent}
	 * @since API v0.0
	 */
	String getMainScreenLanguageKey();

	/**
	 * Gets a config object that can edit the Iris configuration.
	 * @since API v0.0
	 */
	IrisApiConfig getConfig();

	/**
	 * Gets a text vertex sink to render into.
	 * @param maxQuadCount Maximum amount of quads that will be rendered with this sink
	 * @param bufferProvider An IntFunction that can provide a {@code ByteBuffer} with at minimum the bytes provided by the input parameter
	 * @since API 0.1
	 */
	IrisTextVertexSink createTextVertexSink(int maxQuadCount, IntFunction<ByteBuffer> bufferProvider);
}
