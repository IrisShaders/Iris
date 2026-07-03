package net.irisshaders.iris.api.v0;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

/**
 * The entry point to the Iris API, major version 0. This is currently the latest
 * version of the API.
 * <p>
 * To access the API, use {@link #getInstance()}.
 */
public interface IrisApi {
	/**
	 * @since API v0.0
	 */
	static IrisApi getInstance() {
		return IrisApiInternal.INSTANCE;
	}

	/**
	 * Gets the minor revision of this API. This is incremented when
	 * new methods are added without breaking API. Mods can check this
	 * if they wish to check whether given API calls are available on
	 * the currently installed Iris version.
	 *
	 * @return The current minor revision. Currently, revision 2.
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
	 * a	 better approach to fixing bugs than calling this function.
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
	 * <p>
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
	 * / {@code TranslatableComponent}
	 * @since API v0.0
	 */
	String getMainScreenLanguageKey();

	/**
	 * Gets a config object that can edit the Iris configuration.
	 *
	 * @since API v0.0
	 */
	IrisApiConfig getConfig();

	/**
	 * Gets a text vertex sink to render into.
	 *
	 * @param maxQuadCount   Maximum amount of quads that will be rendered with this sink
	 * @param bufferProvider An IntFunction that can provide a {@code ByteBuffer} with at minimum the bytes provided by the input parameter
	 * @since API 0.1
	 */
	IrisTextVertexSink createTextVertexSink(int maxQuadCount, IntFunction<ByteBuffer> bufferProvider);

	/**
	 * Gets the sun path rotation used by the current shader pack.
	 *
	 * @return The sun path rotation as specified by the shader pack, or 0 if no shader pack is in use.
	 * @since API v0.2
	 */
	float getSunPathRotation();

	/**
	 * Assigns a render pipeline to an Iris shader key.
	 *
	 * @since API v0.3
	 */
	void assignPipeline(RenderPipeline pipeline, IrisProgram program);

	/**
	 * Registers a custom std140 uniform block (UBO) that a mod binds to its own
	 * {@code RenderPass} via {@code RenderPass.setUniform(blockName, buffer)}, so that
	 * Iris forwards it to the program it substitutes for the given pipeline when a
	 * shader pack is active.
	 *
	 * <p>Without this, when Iris substitutes its own program for a mod's terrain
	 * pipeline (see {@link #assignPipeline}), the mod's custom uniform block is
	 * silently dropped, because Iris's substituted program only declares Iris's own
	 * uniform blocks. This is the mechanism requested by IrisShaders/Iris#2974 for
	 * MaLiLib/Litematica's {@code ChunkFix} block.
	 *
	 * <p>The mod must:
	 * <ol>
	 *     <li>have already assigned the pipeline via {@link #assignPipeline};</li>
	 *     <li>bind the block to its render pass each draw via
	 *     {@code RenderPass.setUniform(blockName, gpuBuffer)}; and</li>
	 *     <li>pass the exact std140 GLSL declaration of the block, so Iris can inject
	 *     it into the substituted program's source (the mod cannot, since Iris
	 *     generates that program from the active shader pack).</li>
	 * </ol>
	 *
	 * <p>The binding is <em>optional at draw time</em>: Iris only binds the buffer for
	 * draws that actually supply it on the pass, so unrelated draws that share the same
	 * substituted program (e.g. vanilla terrain) are unaffected.
	 *
	 * @param pipeline        The mod render pipeline previously passed to {@link #assignPipeline}.
	 * @param blockName       The std140 uniform block name (e.g. {@code "ChunkFix"}), matching the
	 *                        name used in {@code RenderPass.setUniform} and in {@code glslDeclaration}.
	 * @param glslDeclaration The full GLSL declaration of the block, e.g.
	 *                        {@code "layout(std140) uniform ChunkFix { ivec2 TextureSize; float ChunkVisibility; int UseRgss; int hasShadersOn; };"}.
	 * @since API v0.4
	 */
	void registerCustomUniformBlock(RenderPipeline pipeline, String blockName, String glslDeclaration);
}
