package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableSet;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.AlphaTests;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SodiumTerrainPipeline {
	Optional<String> terrainVertex = Optional.empty();
	Optional<String> terrainGeometry = Optional.empty();
	Optional<String> terrainFragment = Optional.empty();
	String terrainCutoutFragment;
	GlFramebuffer terrainFramebuffer;
	BlendModeOverride terrainBlendOverride;
	AlphaTest terrainCutoutAlpha;

	Optional<String> translucentVertex = Optional.empty();
	Optional<String> translucentGeometry = Optional.empty();
	Optional<String> translucentFragment = Optional.empty();
	GlFramebuffer translucentFramebuffer;
	BlendModeOverride translucentBlendOverride;
	AlphaTest translucentAlpha;

	Optional<String> shadowVertex = Optional.empty();
	Optional<String> shadowGeometry = Optional.empty();
	Optional<String> shadowFragment = Optional.empty();
	String shadowCutoutFragment;
	GlFramebuffer shadowFramebuffer;
	BlendModeOverride shadowBlendOverride = BlendModeOverride.OFF;
	AlphaTest shadowAlpha;

	ProgramSet programSet;

	private final WorldRenderingPipeline parent;

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	private final IntFunction<ProgramImages> createTerrainImages;
	private final IntFunction<ProgramImages> createShadowImages;

	public SodiumTerrainPipeline(WorldRenderingPipeline parent, ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers, IntFunction<ProgramImages> createTerrainImages, IntFunction<ProgramImages> createShadowImages,
								 RenderTargets targets,
								 ImmutableSet<Integer> flippedAfterPrepare,
								 ImmutableSet<Integer> flippedAfterTranslucent, GlFramebuffer shadowFramebuffer) {
		this.parent = Objects.requireNonNull(parent);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		this.programSet = programSet;
		this.shadowFramebuffer = shadowFramebuffer;

		terrainSource.ifPresent(sources -> terrainFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare,
				sources.getDirectives().getDrawBuffers()));

		translucentSource.ifPresent(sources -> translucentFramebuffer = targets.createGbufferFramebuffer(flippedAfterTranslucent,
				sources.getDirectives().getDrawBuffers()));

		if (terrainFramebuffer == null) {
			terrainFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		}

		if (translucentFramebuffer == null) {
			translucentFramebuffer = targets.createGbufferFramebuffer(flippedAfterTranslucent, new int[] {0});
		}

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
		this.createTerrainImages = createTerrainImages;
		this.createShadowImages = createShadowImages;
	}

	public void patchShaders(ChunkVertexType vertexType) {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(true, true, false, true, true);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainGeometry = sources.getGeometrySource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);
			terrainBlendOverride = sources.getDirectives().getBlendModeOverride();
			terrainCutoutAlpha = sources.getDirectives().getAlphaTestOverride().orElse(AlphaTests.ONE_TENTH_ALPHA);
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().map(TransformPatcher::patchSodiumTerrainVertex);

		if (terrainGeometry != null) {
			terrainGeometry = TransformPatcher.patchSodium(terrainGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentGeometry != null) {
			translucentGeometry = TransformPatcher.patchSodium(translucentGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowGeometry != null) {
			shadowGeometry = TransformPatcher.patchSodium(shadowGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (terrainFragment != null) {
			String fragment = terrainFragment;

			terrainFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			terrainCutoutFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, terrainCutoutAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentFragment != null) {
			translucentFragment = TransformPatcher.patchSodium(translucentFragment, ShaderType.FRAGMENT, translucentAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowFragment != null) {
			String fragment = shadowFragment;

			shadowFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			shadowCutoutFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, shadowAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}
	}

	public void patchShaders(ChunkVertexType vertexType) {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(true, true, false, true, true);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainGeometry = sources.getGeometrySource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);
			terrainBlendOverride = sources.getDirectives().getBlendModeOverride();
			terrainCutoutAlpha = sources.getDirectives().getAlphaTestOverride().orElse(AlphaTests.ONE_TENTH_ALPHA);
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().orElse(null);
			translucentGeometry = sources.getGeometrySource().orElse(null);
			translucentFragment = sources.getFragmentSource().orElse(null);
			translucentBlendOverride = sources.getDirectives().getBlendModeOverride();
			translucentAlpha = sources.getDirectives().getAlphaTestOverride().orElse(AlphaTest.ALWAYS);
		});

		programSet.getShadow().ifPresent(sources -> {
			shadowVertex = sources.getVertexSource().orElse(null);
			shadowGeometry = sources.getGeometrySource().orElse(null);
			shadowFragment = sources.getFragmentSource().orElse(null);
			shadowBlendOverride = sources.getDirectives().getBlendModeOverride();
			shadowAlpha = sources.getDirectives().getAlphaTestOverride().orElse(AlphaTests.NON_ZERO_ALPHA);
		});

		if (terrainVertex != null) {
			terrainVertex = TransformPatcher.patchSodium(terrainVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentVertex != null) {
			translucentVertex = TransformPatcher.patchSodium(translucentVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowVertex != null) {
			shadowVertex = TransformPatcher.patchSodium(shadowVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (terrainGeometry != null) {
			terrainGeometry = TransformPatcher.patchSodium(terrainGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentGeometry != null) {
			translucentGeometry = TransformPatcher.patchSodium(translucentGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowGeometry != null) {
			shadowGeometry = TransformPatcher.patchSodium(shadowGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (terrainFragment != null) {
			String fragment = terrainFragment;

			terrainFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			terrainCutoutFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, terrainCutoutAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentFragment != null) {
			translucentFragment = TransformPatcher.patchSodium(translucentFragment, ShaderType.FRAGMENT, translucentAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowFragment != null) {
			String fragment = shadowFragment;

			shadowFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			shadowCutoutFragment = TransformPatcher.patchSodium(fragment, ShaderType.FRAGMENT, shadowAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}
	}

	public Optional<String> getTerrainVertexShaderSource() {
		return terrainVertex;
	}

	public Optional<String> getTerrainGeometryShaderSource() {
		return terrainGeometry;
	}

	public Optional<String> getTerrainFragmentShaderSource() {
		return terrainFragment;
	}

	public Optional<String> getTerrainCutoutFragmentShaderSource() {
		return Optional.ofNullable(terrainCutoutFragment);
	}

	public GlFramebuffer getTerrainFramebuffer() {
		return terrainFramebuffer;
	}

	public BlendModeOverride getTerrainBlendOverride() {
		return terrainBlendOverride;
	}

	public Optional<AlphaTest> getTerrainCutoutAlpha() {
		return Optional.ofNullable(terrainCutoutAlpha);
	}

	public Optional<String> getTranslucentVertexShaderSource() {
		return translucentVertex;
	}

	public Optional<String> getTranslucentGeometryShaderSource() {
		return translucentGeometry;
	}

	public Optional<String> getTranslucentFragmentShaderSource() {
		return Optional.ofNullable(translucentFragment);
	}

	public GlFramebuffer getTranslucentFramebuffer() {
		return translucentFramebuffer;
	}

	public BlendModeOverride getTranslucentBlendOverride() {
		return translucentBlendOverride;
	}

	public Optional<AlphaTest> getTranslucentAlpha() {
		return Optional.ofNullable(translucentAlpha);
	}

	public Optional<String> getShadowVertexShaderSource() {
		return shadowVertex;
	}

	public Optional<String> getShadowGeometryShaderSource() {
		return shadowGeometry;
	}

	public Optional<String> getShadowFragmentShaderSource() {
		return Optional.ofNullable(shadowFragment);
	}

	public Optional<String> getShadowCutoutFragmentShaderSource() {
		return Optional.ofNullable(shadowCutoutFragment);
	}

	public GlFramebuffer getShadowFramebuffer() {
		return shadowFramebuffer;
	}

	public BlendModeOverride getShadowBlendOverride() {
		return shadowBlendOverride;
	}

	public Optional<AlphaTest> getShadowAlpha() {
		return Optional.ofNullable(shadowAlpha);
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), parent.getFrameUpdateNotifier(), FogMode.PER_VERTEX);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms.buildUniforms();
	}

	public boolean hasShadowPass() {
		return createShadowSamplers != null;
	}

	public ProgramSamplers initTerrainSamplers(int programId) {
		return createTerrainSamplers.apply(programId);
	}

	public ProgramSamplers initShadowSamplers(int programId) {
		return createShadowSamplers.apply(programId);
	}

	public ProgramImages initTerrainImages(int programId) {
		return createTerrainImages.apply(programId);
	}

	public ProgramImages initShadowImages(int programId) {
		return createShadowImages.apply(programId);
	}

	/*public void bindFramebuffer() {
		this.framebuffer.bind();
	}

	public void unbindFramebuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
	}*/

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}

	public static String parseSodiumImport(String shader) {
		Pattern IMPORT_PATTERN = Pattern.compile("#import <(?<namespace>.*):(?<path>.*)>");
		Matcher matcher = IMPORT_PATTERN.matcher(shader);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Malformed import statement (expected format: " + IMPORT_PATTERN + ")");
		}

		String namespace = matcher.group("namespace");
		String path = matcher.group("path");

		ResourceLocation identifier = new ResourceLocation(namespace, path);
		return ShaderLoader.getShaderSource(identifier);
	}
}
