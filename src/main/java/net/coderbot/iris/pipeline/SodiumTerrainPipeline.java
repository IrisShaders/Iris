package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableSet;
import net.caffeinemc.sodium.render.shader.ShaderLoader;
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
import net.coderbot.iris.pipeline.newshader.TriforcePatcher;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SodiumTerrainPipeline {
	String terrainVertex;
	String terrainGeometry;
	String terrainFragment;
	String terrainCutoutFragment;
	GlFramebuffer terrainFramebuffer;
	BlendModeOverride terrainBlendOverride;
	AlphaTest terrainCutoutAlpha;

	String translucentVertex;
	String translucentGeometry;
	String translucentFragment;
	GlFramebuffer translucentFramebuffer;
	BlendModeOverride translucentBlendOverride;
	AlphaTest translucentAlpha;

	String shadowVertex;
	String shadowGeometry;
	String shadowFragment;
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

	public void patchShaders(int maxBatchSize, float vertexRange, boolean baseInstanced) {
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
			terrainVertex = TriforcePatcher.patchSodium(terrainVertex, ShaderType.VERTEX, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (translucentVertex != null) {
			translucentVertex = TriforcePatcher.patchSodium(translucentVertex, ShaderType.VERTEX, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (shadowVertex != null) {
			shadowVertex = TriforcePatcher.patchSodium(shadowVertex, ShaderType.VERTEX, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (terrainGeometry != null) {
			terrainGeometry = TriforcePatcher.patchSodium(terrainGeometry, ShaderType.GEOMETRY, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (translucentGeometry != null) {
			translucentGeometry = TriforcePatcher.patchSodium(translucentGeometry, ShaderType.GEOMETRY, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (shadowGeometry != null) {
			shadowGeometry = TriforcePatcher.patchSodium(shadowGeometry, ShaderType.GEOMETRY, null, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (terrainFragment != null) {
			String fragment = terrainFragment;

			terrainFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexRange, maxBatchSize, baseInstanced);
			terrainCutoutFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTests.ONE_TENTH_ALPHA, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (translucentFragment != null) {
			translucentFragment = TriforcePatcher.patchSodium(translucentFragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (shadowFragment != null) {
			String fragment = shadowFragment;

			shadowFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexRange, maxBatchSize, baseInstanced);
			shadowCutoutFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTests.ONE_TENTH_ALPHA, inputs, vertexRange, maxBatchSize, baseInstanced);
		}

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");
			try {
				if (hasShadowPass()) {
					Files.write(debugOutDir.resolve("sodium_shadow.vsh"), shadowVertex.getBytes(StandardCharsets.UTF_8));
					Files.write(debugOutDir.resolve("sodium_shadowCutout.fsh"), shadowCutoutFragment.getBytes(StandardCharsets.UTF_8));
					Files.write(debugOutDir.resolve("sodium_shadow.fsh"), shadowFragment.getBytes(StandardCharsets.UTF_8));
				}
				Files.write(debugOutDir.resolve("sodium_terrain.vsh"), terrainVertex.getBytes(StandardCharsets.UTF_8));
				Files.write(debugOutDir.resolve("sodium_terrainCutout.fsh"), terrainCutoutFragment.getBytes(StandardCharsets.UTF_8));
				Files.write(debugOutDir.resolve("sodium_terrain.fsh"), terrainFragment.getBytes(StandardCharsets.UTF_8));
				Files.write(debugOutDir.resolve("sodium_translucent.vsh"), translucentVertex.getBytes(StandardCharsets.UTF_8));
				Files.write(debugOutDir.resolve("sodium_translucent.fsh"), translucentFragment.getBytes(StandardCharsets.UTF_8));

				if (shadowGeometry != null) {
					Files.write(debugOutDir.resolve("sodium_shadow.gsh"), shadowGeometry.getBytes(StandardCharsets.UTF_8));
				}

				if (terrainGeometry != null) {
					Files.write(debugOutDir.resolve("sodium_terrain.gsh"), terrainGeometry.getBytes(StandardCharsets.UTF_8));
				}

				if (translucentGeometry != null) {
					Files.write(debugOutDir.resolve("sodium_translucent.gsh"), translucentGeometry.getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Optional<String> getTerrainVertexShaderSource() {
		return Optional.ofNullable(terrainVertex);
	}

	public Optional<String> getTerrainGeometryShaderSource() {
		return Optional.ofNullable(terrainGeometry);
	}

	public Optional<String> getTerrainFragmentShaderSource() {
		return Optional.ofNullable(terrainFragment);
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
		return Optional.ofNullable(translucentVertex);
	}

	public Optional<String> getTranslucentGeometryShaderSource() {
		return Optional.ofNullable(translucentGeometry);
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
		return Optional.ofNullable(shadowVertex);
	}

	public Optional<String> getShadowGeometryShaderSource() {
		return Optional.ofNullable(shadowGeometry);
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
		return ShaderLoader.MINECRAFT_ASSETS.getShaderSource(identifier);
	}
}
