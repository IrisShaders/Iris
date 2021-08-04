package net.coderbot.iris.pipeline;

import java.util.Optional;
import java.util.function.IntFunction;

import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.newshader.TriforceSodiumPatcher;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;

public class SodiumTerrainPipeline {
	String terrainVertex;
	String terrainGeometry;
	String terrainFragment;
	String terrainCutoutFragment;
	GlFramebuffer terrainFramebuffer;

	String translucentVertex;
	String translucentGeometry;
	String translucentFragment;
	GlFramebuffer translucentFramebuffer;

	String shadowVertex;
	String shadowGeometry;
	String shadowFragment;
	String shadowCutoutFragment;
	GlFramebuffer shadowFramebuffer;

	ProgramSet programSet;

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	public SodiumTerrainPipeline(ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers, RenderTargets targets,
								 ImmutableSet<Integer> flippedBeforeTranslucent,
								 ImmutableSet<Integer> flippedAfterTranslucent, GlFramebuffer shadowFramebuffer) {
		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);
		Optional<ProgramSource> shadowSource = programSet.getShadow();

		this.programSet = programSet;
		this.shadowFramebuffer = shadowFramebuffer;

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainGeometry = sources.getGeometrySource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);
			terrainFramebuffer = targets.createGbufferFramebuffer(flippedBeforeTranslucent,
					sources.getDirectives().getDrawBuffers());
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().orElse(null);
			translucentGeometry = sources.getGeometrySource().orElse(null);
			translucentFragment = sources.getFragmentSource().orElse(null);
			translucentFramebuffer = targets.createGbufferFramebuffer(flippedAfterTranslucent,
					sources.getDirectives().getDrawBuffers());
		});

		shadowSource.ifPresent(sources -> {
			shadowVertex = sources.getVertexSource().orElse(null);
			shadowGeometry = sources.getGeometrySource().orElse(null);
			shadowFragment = sources.getFragmentSource().orElse(null);
		});

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(true, true, false, true, true);

		AlphaTest cutoutAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);

		if (terrainVertex != null) {
			terrainVertex = TriforceSodiumPatcher.patch(terrainVertex, ShaderType.VERTEX, null, inputs);
		}

		if (translucentVertex != null) {
			translucentVertex = TriforceSodiumPatcher.patch(translucentVertex, ShaderType.VERTEX, null, inputs);
		}

		if (shadowVertex != null) {
			shadowVertex = TriforceSodiumPatcher.patch(shadowVertex, ShaderType.VERTEX, null, inputs);
		}

		if (terrainGeometry != null) {
			terrainGeometry = TriforceSodiumPatcher.patch(terrainGeometry, ShaderType.GEOMETRY, null, inputs);
		}

		if (translucentGeometry != null) {
			translucentGeometry = TriforceSodiumPatcher.patch(translucentGeometry, ShaderType.GEOMETRY, null, inputs);
		}

		if (shadowGeometry != null) {
			shadowGeometry = TriforceSodiumPatcher.patch(shadowGeometry, ShaderType.GEOMETRY, null, inputs);
		}

		if (terrainFragment != null) {
			String fragment = terrainFragment;

			terrainFragment = TriforceSodiumPatcher.patch(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs);
			terrainCutoutFragment = TriforceSodiumPatcher.patch(fragment, ShaderType.FRAGMENT, cutoutAlpha, inputs);
		}

		if (translucentFragment != null) {
			translucentFragment = TriforceSodiumPatcher.patch(translucentFragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs);
		}

		if (shadowFragment != null) {
			String fragment = shadowFragment;

			shadowFragment = TriforceSodiumPatcher.patch(fragment, ShaderType.FRAGMENT, AlphaTest.ALWAYS, inputs);
			shadowCutoutFragment = TriforceSodiumPatcher.patch(fragment, ShaderType.FRAGMENT, cutoutAlpha, inputs);
		}

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
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

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		FrameUpdateNotifier updateNotifier;

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof DeferredWorldRenderingPipeline) {
			updateNotifier = ((DeferredWorldRenderingPipeline) pipeline).getUpdateNotifier();
		} else if (pipeline instanceof CoreWorldRenderingPipeline) {
			updateNotifier = ((CoreWorldRenderingPipeline) pipeline).getUpdateNotifier();
		} else if (pipeline instanceof FixedFunctionWorldRenderingPipeline) {
			// TODO: This isn't what we should do.
			updateNotifier = new FrameUpdateNotifier();
		} else {
			// TODO: Proper interface
			throw new IllegalStateException("Unsupported pipeline: " + pipeline);
		}

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), updateNotifier, FogMode.LINEAR);
		SamplerUniforms.addCommonSamplerUniforms(uniforms);
		SamplerUniforms.addWorldSamplerUniforms(uniforms);
		SamplerUniforms.addDepthSamplerUniforms(uniforms);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms.buildUniforms();
	}

	public ProgramSamplers initTerrainSamplers(int programId) {
		return createTerrainSamplers.apply(programId);
	}

	public ProgramSamplers initShadowSamplers(int programId) {
		return createShadowSamplers.apply(programId);
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
