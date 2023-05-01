package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.newshader.AlphaTests;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.coderbot.iris.uniforms.custom.CustomUniforms;

public class SodiumTerrainPipeline {
	Optional<String> terrainSolidVertex;
	Optional<String> terrainSolidGeometry;
	Optional<String> terrainSolidFragment;
	GlFramebuffer terrainSolidFramebuffer;
	BlendModeOverride terrainSolidBlendOverride;
	List<BufferBlendOverride> terrainSolidBufferOverrides;

	Optional<String> terrainCutoutVertex;
	Optional<String> terrainCutoutGeometry;
	Optional<String> terrainCutoutFragment;
	GlFramebuffer terrainCutoutFramebuffer;
	BlendModeOverride terrainCutoutBlendOverride;
	List<BufferBlendOverride> terrainCutoutBufferOverrides;
	Optional<AlphaTest> terrainCutoutAlpha;

	Optional<String> translucentVertex;
	Optional<String> translucentGeometry;
	Optional<String> translucentFragment;
	GlFramebuffer translucentFramebuffer;
	BlendModeOverride translucentBlendOverride;
	List<BufferBlendOverride> translucentBufferOverrides;
	Optional<AlphaTest> translucentAlpha;

	Optional<String> shadowVertex;
	Optional<String> shadowGeometry;
	Optional<String> shadowFragment;
	Optional<String> shadowCutoutFragment;
	GlFramebuffer shadowFramebuffer;
	BlendModeOverride shadowBlendOverride = BlendModeOverride.OFF;
	List<BufferBlendOverride> shadowBufferOverrides;
	Optional<AlphaTest> shadowAlpha;

	ProgramSet programSet;

	private final WorldRenderingPipeline parent;
	private final CustomUniforms customUniforms;

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	private final IntFunction<ProgramImages> createTerrainImages;
	private final IntFunction<ProgramImages> createShadowImages;

	public SodiumTerrainPipeline(WorldRenderingPipeline parent, ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers, IntFunction<ProgramImages> createTerrainImages, IntFunction<ProgramImages> createShadowImages,
								 RenderTargets targets,
								 ImmutableSet<Integer> flippedAfterPrepare,
								 ImmutableSet<Integer> flippedAfterTranslucent, GlFramebuffer shadowFramebuffer, CustomUniforms customUniforms) {
		this.parent = Objects.requireNonNull(parent);
		this.customUniforms = customUniforms;

		Optional<ProgramSource> terrainSolidSource = first(programSet.getGbuffersTerrainSolid(), programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> terrainCutoutSource = first(programSet.getGbuffersTerrainCutout(), programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainCutoutSource);

		this.programSet = programSet;
		this.shadowFramebuffer = shadowFramebuffer;

		terrainSolidSource.ifPresent(sources -> terrainSolidFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare,
				sources.getDirectives().getDrawBuffers()));

		terrainCutoutSource.ifPresent(sources -> terrainCutoutFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare,
				sources.getDirectives().getDrawBuffers()));

		translucentSource.ifPresent(sources -> translucentFramebuffer = targets.createGbufferFramebuffer(flippedAfterTranslucent,
				sources.getDirectives().getDrawBuffers()));

		if (terrainSolidFramebuffer == null) {
			terrainSolidFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		}

		if (terrainCutoutFramebuffer == null) {
			terrainCutoutFramebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		}

		if (translucentFramebuffer == null) {
			translucentFramebuffer = targets.createGbufferFramebuffer(flippedAfterTranslucent, new int[] {0});
		}

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
		this.createTerrainImages = createTerrainImages;
		this.createShadowImages = createShadowImages;
	}

	private static final Supplier<Optional<AlphaTest>> terrainCutoutDefault = () -> Optional.of(AlphaTests.ONE_TENTH_ALPHA);
	private static final Supplier<Optional<AlphaTest>> translucentDefault = () -> Optional.of(AlphaTest.ALWAYS);
	private static final Supplier<Optional<AlphaTest>> shadowDefault = () -> Optional.of(AlphaTests.NON_ZERO_ALPHA);

	public void patchShaders(ChunkVertexType vertexType) {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(true, true, false, true, true);

		Optional<ProgramSource> terrainSolidSource = first(programSet.getGbuffersTerrainSolid(), programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> terrainCutoutSource = first(programSet.getGbuffersTerrainCutout(), programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainCutoutSource);


		terrainSolidSource.ifPresentOrElse(sources -> {
			terrainSolidBlendOverride = sources.getDirectives().getBlendModeOverride().orElse(ProgramId.Terrain.getBlendModeOverride());
			terrainSolidBufferOverrides = new ArrayList<>();
			sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
				int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
				if (index > -1) {
					terrainSolidBufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
				}
			});

			Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null),
				AlphaTest.ALWAYS, inputs,
				vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale(), parent.getTextureMap(), sources.getGeometrySource().isPresent());
			terrainSolidVertex = Optional.ofNullable(transformed.get(PatchShaderType.VERTEX));
			terrainSolidGeometry = Optional.ofNullable(transformed.get(PatchShaderType.GEOMETRY));
			terrainSolidFragment = Optional.ofNullable(transformed.get(PatchShaderType.FRAGMENT));

			ShaderPrinter.printProgram(sources.getName() + "_sodium_solid").addSources(transformed).print();
		}, () -> {
			terrainSolidBlendOverride = null;
			terrainSolidBufferOverrides = Collections.emptyList();
			terrainSolidVertex = Optional.empty();
			terrainSolidGeometry = Optional.empty();
			terrainSolidFragment = Optional.empty();
		});

		terrainCutoutSource.ifPresentOrElse(sources -> {
			terrainCutoutBlendOverride = sources.getDirectives().getBlendModeOverride().orElse(ProgramId.Terrain.getBlendModeOverride());
			terrainCutoutBufferOverrides = new ArrayList<>();
			sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
				int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
				if (index > -1) {
					terrainCutoutBufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
				}
			});
			terrainCutoutAlpha = sources.getDirectives().getAlphaTestOverride().or(terrainCutoutDefault);

			Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null),
				terrainCutoutAlpha.orElse(AlphaTests.ONE_TENTH_ALPHA), inputs,
				vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale(), parent.getTextureMap(), sources.getGeometrySource().isPresent());
			terrainCutoutVertex = Optional.ofNullable(transformed.get(PatchShaderType.VERTEX));
			terrainCutoutGeometry = Optional.ofNullable(transformed.get(PatchShaderType.GEOMETRY));
			terrainCutoutFragment = Optional.ofNullable(transformed.get(PatchShaderType.FRAGMENT));

			ShaderPrinter.printProgram(sources.getName() + "_sodium_cutout").addSources(transformed).print();
		}, () -> {
			terrainCutoutBlendOverride = null;
			terrainCutoutBufferOverrides = Collections.emptyList();
			terrainCutoutAlpha = terrainCutoutDefault.get();
			terrainCutoutVertex = Optional.empty();
			terrainCutoutGeometry = Optional.empty();
			terrainCutoutFragment = Optional.empty();
		});


		translucentSource.ifPresentOrElse(sources -> {
			translucentBlendOverride = sources.getDirectives().getBlendModeOverride().orElse(ProgramId.Water.getBlendModeOverride());
			translucentBufferOverrides = new ArrayList<>();
			sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
				int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
				if (index > -1) {
					translucentBufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
				}
			});
			translucentAlpha = sources.getDirectives().getAlphaTestOverride().or(translucentDefault);

			Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null),
				translucentAlpha.orElse(AlphaTest.ALWAYS), inputs,
				vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale(), parent.getTextureMap(), sources.getGeometrySource().isPresent());
			translucentVertex = Optional.ofNullable(transformed.get(PatchShaderType.VERTEX));
			translucentGeometry = Optional.ofNullable(transformed.get(PatchShaderType.GEOMETRY));
			translucentFragment = Optional.ofNullable(transformed.get(PatchShaderType.FRAGMENT));

			ShaderPrinter.printProgram(sources.getName() + "_sodium").addSources(transformed).print();
		}, () -> {
			translucentBlendOverride = null;
			translucentBufferOverrides = Collections.emptyList();
			translucentAlpha = translucentDefault.get();
			translucentVertex = Optional.empty();
			translucentGeometry = Optional.empty();
			translucentFragment = Optional.empty();
		});

		programSet.getShadow().ifPresentOrElse(sources -> {
			shadowBlendOverride = sources.getDirectives().getBlendModeOverride().orElse(ProgramId.Shadow.getBlendModeOverride());
			shadowBufferOverrides = new ArrayList<>();
			sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
				int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
				if (index > -1) {
					shadowBufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
				}
			});
			shadowAlpha = sources.getDirectives().getAlphaTestOverride().or(shadowDefault);

			Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null),
				AlphaTest.ALWAYS, inputs,
				vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale(), parent.getTextureMap(), sources.getGeometrySource().isPresent());
			Map<PatchShaderType, String> transformedCutout = TransformPatcher.patchSodium(
				sources.getVertexSource().orElse(null),
				sources.getGeometrySource().orElse(null),
				sources.getFragmentSource().orElse(null),
				shadowAlpha.get(), inputs,
				vertexType.getPositionScale(), vertexType.getPositionOffset(), vertexType.getTextureScale(), parent.getTextureMap(), sources.getGeometrySource().isPresent());
			shadowVertex = Optional.ofNullable(transformed.get(PatchShaderType.VERTEX));
			shadowGeometry = Optional.ofNullable(transformed.get(PatchShaderType.GEOMETRY));
			shadowCutoutFragment = Optional.ofNullable(transformedCutout.get(PatchShaderType.FRAGMENT));
			shadowFragment = Optional.ofNullable(transformed.get(PatchShaderType.FRAGMENT));

			ShaderPrinter.printProgram(sources.getName() + "_sodium")
				.addSources(transformed)
				.setName(sources.getName() + "_sodium_cutout")
				.addSource(PatchShaderType.FRAGMENT, shadowCutoutFragment.orElse(null))
				.print();
		}, () -> {
			shadowBlendOverride = null;
			shadowBufferOverrides = Collections.emptyList();
			shadowAlpha = shadowDefault.get();
			shadowVertex = Optional.empty();
			shadowGeometry = Optional.empty();
			shadowCutoutFragment = Optional.empty();
			shadowFragment = Optional.empty();
		});
	}

	public Optional<String> getTerrainSolidVertexShaderSource() {
		return terrainSolidVertex;
	}

	public Optional<String> getTerrainSolidGeometryShaderSource() {
		return terrainSolidGeometry;
	}

	public Optional<String> getTerrainSolidFragmentShaderSource() {
		return terrainSolidFragment;
	}

	public Optional<String> getTerrainCutoutVertexShaderSource() {
		return terrainCutoutVertex;
	}

	public Optional<String> getTerrainCutoutGeometryShaderSource() {
		return terrainCutoutGeometry;
	}

	public Optional<String> getTerrainCutoutFragmentShaderSource() {
		return terrainCutoutFragment;
	}

	public GlFramebuffer getTerrainSolidFramebuffer() {
		return terrainSolidFramebuffer;
	}
	public GlFramebuffer getTerrainCutoutFramebuffer() {
		return terrainCutoutFramebuffer;
	}

	public BlendModeOverride getTerrainSolidBlendOverride() {
		return terrainSolidBlendOverride;
	}

	public List<BufferBlendOverride> getTerrainSolidBufferOverrides() {
		return terrainSolidBufferOverrides;
	}

	public BlendModeOverride getTerrainCutoutBlendOverride() {
		return terrainCutoutBlendOverride;
	}

	public List<BufferBlendOverride> getTerrainCutoutBufferOverrides() {
		return terrainCutoutBufferOverrides;
	}

	public Optional<AlphaTest> getTerrainCutoutAlpha() {
		return terrainCutoutAlpha;
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

	public GlFramebuffer getTranslucentFramebuffer() {
		return translucentFramebuffer;
	}

	public BlendModeOverride getTranslucentBlendOverride() {
		return translucentBlendOverride;
	}

	public List<BufferBlendOverride> getTranslucentBufferOverrides() {
		return translucentBufferOverrides;
	}

	public Optional<AlphaTest> getTranslucentAlpha() {
		return translucentAlpha;
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

	public Optional<String> getShadowCutoutFragmentShaderSource() {
		return shadowCutoutFragment;
	}

	public GlFramebuffer getShadowFramebuffer() {
		return shadowFramebuffer;
	}

	public BlendModeOverride getShadowBlendOverride() {
		return shadowBlendOverride;
	}

	public List<BufferBlendOverride> getShadowBufferOverrides() {
		return shadowBufferOverrides;
	}

	public Optional<AlphaTest> getShadowAlpha() {
		return shadowAlpha;
	}

	public ProgramUniforms.Builder initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
		customUniforms.assignTo(uniforms);

		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms;
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

	public CustomUniforms getCustomUniforms() {
		return customUniforms;
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
		return "";
	}
}
