package net.coderbot.iris.pipeline;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;

import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;

public class SodiumTerrainPipeline {
	Optional<String> terrainVertex = Optional.empty();
	Optional<String> terrainGeometry = Optional.empty();
	Optional<String> terrainFragment = Optional.empty();
	Optional<String> translucentVertex = Optional.empty();
	Optional<String> translucentGeometry = Optional.empty();
	Optional<String> translucentFragment = Optional.empty();
	Optional<String> shadowVertex = Optional.empty();
	Optional<String> shadowGeometry = Optional.empty();
	Optional<String> shadowFragment = Optional.empty();
	//GlFramebuffer framebuffer;
	ProgramSet programSet;

	private final WorldRenderingPipeline parent;

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	private final IntFunction<ProgramImages> createTerrainImages;
	private final IntFunction<ProgramImages> createShadowImages;

	public SodiumTerrainPipeline(WorldRenderingPipeline parent,
								 ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers,
								 IntFunction<ProgramImages> createTerrainImages,
								 IntFunction<ProgramImages> createShadowImages) {
		this.parent = Objects.requireNonNull(parent);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);
		Optional<ProgramSource> shadowSource = programSet.getShadow();

		this.programSet = programSet;

		terrainSource.ifPresent(sources -> {
			Map<PatchShaderType, String> result = TransformPatcher.patchSodiumTerrain(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null));
			terrainVertex = Optional.ofNullable(result.get(PatchShaderType.VERTEX));
			terrainGeometry = Optional.ofNullable(result.get(PatchShaderType.GEOMETRY));
			terrainFragment = Optional.ofNullable(result.get(PatchShaderType.FRAGMENT));

			PatchedShaderPrinter.debugPatchedShaders(sources.getName() + "_sodium",
				terrainVertex.orElse(null), terrainGeometry.orElse(null), terrainFragment.orElse(null));
		});

		translucentSource.ifPresent(sources -> {
			Map<PatchShaderType, String> result = TransformPatcher.patchSodiumTerrain(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null));
			translucentVertex = Optional.ofNullable(result.get(PatchShaderType.VERTEX));
			translucentGeometry = Optional.ofNullable(result.get(PatchShaderType.GEOMETRY));
			translucentFragment = Optional.ofNullable(result.get(PatchShaderType.FRAGMENT));

			PatchedShaderPrinter.debugPatchedShaders(sources.getName() + "_sodium",
				translucentVertex.orElse(null), translucentGeometry.orElse(null), translucentFragment.orElse(null));
		});

		shadowSource.ifPresent(sources -> {
			Map<PatchShaderType, String> result = TransformPatcher.patchSodiumTerrain(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null));
			shadowVertex = Optional.ofNullable(result.get(PatchShaderType.VERTEX));
			shadowGeometry = Optional.ofNullable(result.get(PatchShaderType.GEOMETRY));
			shadowFragment = Optional.ofNullable(result.get(PatchShaderType.FRAGMENT));

			PatchedShaderPrinter.debugPatchedShaders(sources.getName() + "_sodium",
				shadowVertex.orElse(null), shadowGeometry.orElse(null), shadowFragment.orElse(null));
		});

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
		this.createTerrainImages = createTerrainImages;
		this.createShadowImages = createShadowImages;
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

	public Optional<String> getTranslucentVertexShaderSource() {
		return translucentVertex;
	}

	public Optional<String> getTranslucentGeometryShaderSource() {
		return translucentGeometry;
	}

	public Optional<String> getTranslucentFragmentShaderSource() {
		return translucentFragment;
	}

	public Optional<String> getShadowVertexShaderSource() {
		return shadowVertex;
	}

	public Optional<String> getShadowGeometryShaderSource() {
		return shadowGeometry;
	}

	public Optional<String> getShadowFragmentShaderSource() {
		return shadowFragment;
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), parent.getFrameUpdateNotifier());
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
}
