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
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.newshader.TriforcePatcher;
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
	String terrainVertex;
	String terrainGeometry;
	String terrainFragment;
	String terrainCutoutFragment;
	GlFramebuffer terrainFramebuffer;
	BlendModeOverride terrainBlendOverride;

	String translucentVertex;
	String translucentGeometry;
	String translucentFragment;
	GlFramebuffer translucentFramebuffer;
	BlendModeOverride translucentBlendOverride;

	String shadowVertex;
	String shadowGeometry;
	String shadowFragment;
	String shadowCutoutFragment;
	GlFramebuffer shadowFramebuffer;
	BlendModeOverride shadowBlendOverride = BlendModeOverride.OFF;

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

		AlphaTest cutoutAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainGeometry = sources.getGeometrySource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);
			terrainBlendOverride = sources.getDirectives().getBlendModeOverride();
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().orElse(null);
			translucentGeometry = sources.getGeometrySource().orElse(null);
			translucentFragment = sources.getFragmentSource().orElse(null);
			translucentBlendOverride = sources.getDirectives().getBlendModeOverride();
		});

		programSet.getShadow().ifPresent(sources -> {
			shadowVertex = sources.getVertexSource().orElse(null);
			shadowGeometry = sources.getGeometrySource().orElse(null);
			shadowFragment = sources.getFragmentSource().orElse(null);
			shadowBlendOverride = sources.getDirectives().getBlendModeOverride();
		});

		if (terrainVertex != null) {
			terrainVertex = TriforcePatcher.patchSodium(terrainVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentVertex != null) {
			translucentVertex = TriforcePatcher.patchSodium(translucentVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowVertex != null) {
			shadowVertex = TriforcePatcher.patchSodium(shadowVertex, ShaderType.VERTEX, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (terrainGeometry != null) {
			terrainGeometry = TriforcePatcher.patchSodium(terrainGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentGeometry != null) {
			translucentGeometry = TriforcePatcher.patchSodium(translucentGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowGeometry != null) {
			shadowGeometry = TriforcePatcher.patchSodium(shadowGeometry, ShaderType.GEOMETRY, null, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (terrainFragment != null) {
			String fragment = terrainFragment;

			terrainFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			terrainCutoutFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, cutoutAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (translucentFragment != null) {
			translucentFragment = TriforcePatcher.patchSodium(translucentFragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
		}

		if (shadowFragment != null) {
			String fragment = shadowFragment;

			shadowFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
			shadowCutoutFragment = TriforcePatcher.patchSodium(fragment, ShaderType.FRAGMENT, cutoutAlpha, inputs, vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale());
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
