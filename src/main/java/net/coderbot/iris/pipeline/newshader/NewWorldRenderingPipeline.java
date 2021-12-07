package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.coderbot.iris.block_rendering.BlockMaterialMapping;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.*;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private boolean destroyed = false;

	private final RenderTargets renderTargets;

	private final ShaderInstance basic;
	private final ShaderInstance basicColor;
	private final ShaderInstance textured;
	private final ShaderInstance texturedColor;
	private final ShaderInstance skyBasic;
	private final ShaderInstance skyBasicColor;
	private final ShaderInstance skyTextured;
	private final ShaderInstance skyTexturedColor;
	private final ShaderInstance clouds;
	private final ShaderInstance shadowTerrainCutout;

	private final ShaderInstance terrainSolid;
	private final ShaderInstance terrainCutout;
	private final ShaderInstance terrainCutoutMipped;

	private final ShaderInstance entitiesSolid;
	private final ShaderInstance entitiesCutout;
	private final ShaderInstance entitiesEyes;
	private final ShaderInstance handCutout;
	private final ShaderInstance handTranslucent;
	private final ShaderInstance shadowEntitiesCutout;
	private final ShaderInstance shadowBeaconBeam;
	private final ShaderInstance lightning;
	private final ShaderInstance leash;
	private final ShaderInstance particles;
	private final ShaderInstance weather;
	private final ShaderInstance crumbling;
	private final ShaderInstance text;
	private final ShaderInstance textIntensity;
	private final ShaderInstance block;
	private final ShaderInstance beacon;
	private final ShaderInstance glint;
	private final ShaderInstance lines;
	private final ShaderInstance shadowLines;

	private final ShaderInstance terrainTranslucent;
	private WorldRenderingPhase phase = WorldRenderingPhase.NOT_RENDERING_WORLD;

	private final Set<ShaderInstance> loadedShaders;

	private final ImmutableList<ClearPass> clearPassesFull;
	private final ImmutableList<ClearPass> clearPasses;
	private final GlFramebuffer baseline;

	private Runnable createShadowMapRenderer;
	private ShadowMapRenderer shadowMapRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final CustomTextureManager customTextureManager;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	private final SodiumTerrainPipeline sodiumTerrainPipeline;

	private final ImmutableSet<Integer> flippedBeforeTranslucent;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	boolean isBeforeTranslucent;

	private final float sunPathRotation;
	private final boolean shouldRenderClouds;
	private final boolean shouldRenderUnderwaterOverlay;
	private final boolean shouldRenderVignette;
	private final boolean oldLighting;
	private final OptionalInt forcedShadowRenderDistanceChunks;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");

		if (Files.exists(debugOutDir)) {
			Files.list(debugOutDir).forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}

		Files.createDirectories(debugOutDir);

		this.shouldRenderClouds = programSet.getPackDirectives().areCloudsEnabled();
		this.shouldRenderUnderwaterOverlay = programSet.getPackDirectives().underwaterOverlay();
		this.shouldRenderVignette = programSet.getPackDirectives().vignette();
		this.oldLighting = programSet.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();

		this.renderTargets = new RenderTargets(Minecraft.getInstance().getMainRenderTarget(), programSet.getPackDirectives().getRenderTargetDirectives());
		this.sunPathRotation = programSet.getPackDirectives().getSunPathRotation();

		PackShadowDirectives shadowDirectives = programSet.getPackDirectives().getShadowDirectives();

		if (shadowDirectives.isDistanceRenderMulExplicit()) {
			if (shadowDirectives.getDistanceRenderMul() >= 0.0) {
				// add 15 and then divide by 16 to ensure we're rounding up
				forcedShadowRenderDistanceChunks =
						OptionalInt.of(((int) (shadowDirectives.getDistance() * shadowDirectives.getDistanceRenderMul()) + 15) / 16);
			} else {
				forcedShadowRenderDistanceChunks = OptionalInt.of(-1);
			}
		} else {
			forcedShadowRenderDistanceChunks = OptionalInt.empty();
		}

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE2);

		customTextureManager = new CustomTextureManager(programSet.getPackDirectives(), programSet.getPack().getCustomTextureDataMap(), programSet.getPack().getCustomNoiseTexture());

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		// TODO: Change this once earlier passes are implemented.
		ImmutableSet<Integer> flippedBeforeTerrain = ImmutableSet.of();

		createShadowMapRenderer = () -> {
			shadowMapRenderer = new ShadowRenderer(this, programSet.getShadow().orElse(null),
					programSet.getPackDirectives(), () -> flippedBeforeTerrain, renderTargets,
					customTextureManager.getNormals(), customTextureManager.getSpecular(), customTextureManager.getNoiseTexture(),
					programSet, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));
			createShadowMapRenderer = () -> {};
		};

		BufferFlipper flipper = new BufferFlipper();

		this.centerDepthSampler = new CenterDepthSampler(renderTargets, updateNotifier);

		flippedBeforeTranslucent = flipper.snapshot();

		Supplier<ShadowMapRenderer> shadowMapRendererSupplier = () -> {
			createShadowMapRenderer.run();
			return shadowMapRenderer;
		};

		this.deferredRenderer = new CompositeRenderer(programSet.getPackDirectives(), programSet.getDeferred(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
				programSet.getPackDirectives().getExplicitFlips("deferred_pre"));

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(programSet.getPackDirectives(), programSet.getComposite(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				programSet.getPackDirectives().getExplicitFlips("composite_pre"));
		this.finalPassRenderer = new FinalPassRenderer(programSet, renderTargets, customTextureManager.getNoiseTexture(), updateNotifier, flipper.snapshot(),
				centerDepthSampler, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				this.compositeRenderer.getFlippedAtLeastOnceFinal());

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedBeforeTranslucent : flippedAfterTranslucent;

		IntFunction<ProgramSamplers> createTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(), customTextureManager.getSpecular());
			IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
				createShadowMapRenderer.run();
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowMapRenderer);
			}

			return builder.build();
		};

		IntFunction<ProgramSamplers> createShadowTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flippedBeforeTerrain, renderTargets, false);
			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(), customTextureManager.getSpecular());
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

			// Only initialize these samplers if the shadow map renderer exists.
			// Otherwise, this program shouldn't be used at all?
			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor) && shadowMapRenderer != null) {
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowMapRenderer);
			}

			return builder.build();
		};

		Optional<ProgramSource> basicSource = programSet.getGbuffersBasic();
		Optional<ProgramSource> texturedSource = first(programSet.getGbuffersTextured(), basicSource);

		Optional<ProgramSource> skyTexturedSource = first(programSet.getGbuffersSkyTextured(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> skyBasicSource = first(programSet.getGbuffersSkyBasic(), programSet.getGbuffersBasic());
		Optional<ProgramSource> cloudsSource = first(programSet.getGbuffersClouds(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());

		Optional<ProgramSource> particleSource = first(programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> weatherSource = first(programSet.getGbuffersWeather(), particleSource);
		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);
		Optional<ProgramSource> shadowSource = programSet.getShadow();
		Optional<ProgramSource> blockSource = first(programSet.getGbuffersBlock(), terrainSource);
		Optional<ProgramSource> beaconSource = first(programSet.getGbuffersBeaconBeam(), programSet.getGbuffersTextured());

		Optional<ProgramSource> entitiesSource = first(programSet.getGbuffersEntities(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> entityEyesSource = first(programSet.getGbuffersEntityEyes(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> handSource = first(programSet.getGbuffersHand(), particleSource);
		Optional<ProgramSource> handTranslucentSource = first(programSet.getGbuffersHandWater(), handSource);
		Optional<ProgramSource> glintSource = first(programSet.getGbuffersGlint(), programSet.getGbuffersTextured());

		Optional<ProgramSource> damagedBlockSource = first(programSet.getGbuffersDamagedBlock(), terrainSource);

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// Matches OptiFine's default for CUTOUT and CUTOUT_MIPPED.
		AlphaTest terrainCutoutAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);
		AlphaTest nonZeroAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.0001F);

		this.loadedShaders = new HashSet<>();

		// TODO: Resolve hasColorAttrib based on the vertex format
		try {
			this.basic = createShader("gbuffers_basic", basicSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION, FogMode.LINEAR);
			this.basicColor = createShader("gbuffers_basic_color", basicSource, nonZeroAlpha, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF);
			this.textured = createShader("gbuffers_textured", texturedSource, nonZeroAlpha, DefaultVertexFormat.POSITION_TEX, FogMode.OFF);
			this.texturedColor = createShader("gbuffers_textured_color", texturedSource, terrainCutoutAlpha, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF);
			this.skyBasic = createShader("gbuffers_sky_basic", skyBasicSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION, FogMode.LINEAR);
			this.skyBasicColor = createShader("gbuffers_sky_basic_color", skyBasicSource, nonZeroAlpha, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF);
			this.skyTextured = createShader("gbuffers_sky_textured", skyTexturedSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_TEX, FogMode.OFF);
			this.skyTexturedColor = createShader("gbuffers_sky_textured_tex_color", skyTexturedSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF);
			this.clouds = createShader("gbuffers_clouds", cloudsSource, terrainCutoutAlpha, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, FogMode.LINEAR);
			this.terrainSolid = createShader("gbuffers_terrain_solid", terrainSource, AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.terrainCutout = createShader("gbuffers_terrain_cutout", terrainSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.terrainCutoutMipped = createShader("gbuffers_terrain_cutout_mipped", terrainSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.entitiesSolid = createShader("gbuffers_entities_solid", entitiesSource, AlphaTest.ALWAYS, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.entitiesCutout = createShader("gbuffers_entities_cutout", entitiesSource, terrainCutoutAlpha, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.entitiesEyes = createShader("gbuffers_spidereyes", entityEyesSource, nonZeroAlpha, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.handCutout = createShader("gbuffers_hand_cutout", handSource, terrainCutoutAlpha, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.handTranslucent = createShader("gbuffers_hand_translucent", handTranslucentSource, terrainCutoutAlpha, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.lightning = createShader("gbuffers_lightning", entitiesSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_COLOR, FogMode.LINEAR);
			this.leash = createShader("gbuffers_leash", basicSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, FogMode.LINEAR);
			this.particles = createShader("gbuffers_particles", particleSource, terrainCutoutAlpha, DefaultVertexFormat.PARTICLE, FogMode.LINEAR);
			this.weather = createShader("gbuffers_weather", weatherSource, terrainCutoutAlpha, DefaultVertexFormat.PARTICLE, FogMode.LINEAR);
			this.crumbling = createShader("gbuffers_damagedblock", damagedBlockSource, terrainCutoutAlpha, DefaultVertexFormat.BLOCK, FogMode.OFF);
			// TODO: block entities text?
			this.text = createShader("gbuffers_entities_text", entitiesSource, nonZeroAlpha, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, FogMode.LINEAR);
			this.textIntensity = createShader("gbuffers_entities_text_intensity", entitiesSource, nonZeroAlpha, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, FogMode.LINEAR);
			this.block = createShader("gbuffers_block", blockSource, terrainCutoutAlpha, DefaultVertexFormat.NEW_ENTITY, FogMode.LINEAR);
			this.beacon = createShader("gbuffers_beaconbeam", beaconSource, AlphaTest.ALWAYS, DefaultVertexFormat.BLOCK, FogMode.LINEAR);
			this.glint = createShader("gbuffers_glint", glintSource, nonZeroAlpha, DefaultVertexFormat.POSITION_TEX, FogMode.LINEAR);
			this.lines = createShader("gbuffers_lines", programSet.getGbuffersBasic(), AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_COLOR_NORMAL, FogMode.LINEAR);

			if (translucentSource != terrainSource) {
				this.terrainTranslucent = createShader("gbuffers_translucent", translucentSource, AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			} else {
				this.terrainTranslucent = this.terrainSolid;
			}
		} catch (RuntimeException e) {
			destroyShaders();

			throw e;
		}

		BlockRenderingSettings.INSTANCE.setBlockStateIds(
				BlockMaterialMapping.createBlockStateIdMap(programSet.getPack().getIdMap().getBlockProperties()));

		BlockRenderingSettings.INSTANCE.setEntityIds(programSet.getPack().getIdMap().getEntityIdMap());
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(programSet.getPackDirectives().getAmbientOcclusionLevel());
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(programSet.getPackDirectives().shouldUseSeparateAo());

		this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true,
				programSet.getPackDirectives().getRenderTargetDirectives());
		this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false,
				programSet.getPackDirectives().getRenderTargetDirectives());

		if (shadowMapRenderer == null) {
			// Fallback just in case.
			// TODO: Can we remove this?
			this.shadowMapRenderer = new EmptyShadowMapRenderer(programSet.getPackDirectives().getShadowDirectives().getResolution());
			this.shadowTerrainCutout = null;
			this.shadowEntitiesCutout = null;
			this.shadowBeaconBeam = null;
			this.shadowLines = null;
		} else {
			try {
				// TODO: Shadow programs should have access to different samplers.
				this.shadowTerrainCutout = createShadowShader("shadow_terrain_cutout", shadowSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN);
				this.shadowEntitiesCutout = createShadowShader("shadow_entities_cutout", shadowSource, terrainCutoutAlpha, DefaultVertexFormat.NEW_ENTITY);
				this.shadowBeaconBeam = createShadowShader("shadow_beacon_beam", shadowSource, AlphaTest.ALWAYS, DefaultVertexFormat.BLOCK);
				this.shadowLines = createShadowShader("shadow_lines", shadowSource, AlphaTest.ALWAYS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			}  catch (RuntimeException e) {
				destroyShaders();

				throw e;
			}
		}

		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(this, programSet, createTerrainSamplers,
				createShadowTerrainSamplers, renderTargets, flippedBeforeTranslucent, flippedAfterTranslucent,
				shadowMapRenderer instanceof ShadowRenderer ? ((ShadowRenderer) shadowMapRenderer).getFramebuffer() :
				null);
	}

	@Nullable
	private ShaderInstance createShader(String name, Optional<ProgramSource> source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode) throws IOException {
		if (!source.isPresent()) {
			return null;
		}

		return createShader(name, source.get(), fallbackAlpha, vertexFormat, fogMode);
	}

	private ShaderInstance createShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedBeforeTranslucent, source.getDirectives().getDrawBuffers());
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, source.getDirectives().getDrawBuffers());

		ExtendedShader extendedShader = NewShaderTests.create(name, source, beforeTranslucent, afterTranslucent, baseline, fallbackAlpha, vertexFormat, updateNotifier, this, fogMode);

		loadedShaders.add(extendedShader);

		// TODO: waterShadowEnabled?

		// TODO: initialize shadowMapRenderer as needed, finish refactor!!!!
		// TODO: Don't render shadows if they aren't used by the pack.
		// TODO: Pay close attention, should try to unify stuff too.

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedBeforeTranslucent : flippedAfterTranslucent;

		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(extendedShader, customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		// TODO: All samplers added here need to be mirrored in NewShaderTests. Possible way to bypass this?
		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);

		// TODO: IrisSamplers.addWorldSamplers(builder, normals, specular);
		customTextureSamplerInterceptor.addDynamicSampler(customTextureManager.getNormals()::getId, "normals");
		customTextureSamplerInterceptor.addDynamicSampler(customTextureManager.getSpecular()::getId, "specular");

		IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			createShadowMapRenderer.run();
			IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowMapRenderer);
		}

		return extendedShader;
	}

	private ShaderInstance createShadowShader(String name, Optional<ProgramSource> source, AlphaTest fallbackAlpha, VertexFormat vertexFormat) throws IOException {
		if (!source.isPresent()) {
			return null;
		}

		return createShadowShader(name, source.get(), fallbackAlpha, vertexFormat);
	}

	private ShaderInstance createShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat) throws IOException {
		GlFramebuffer framebuffer = ((ShadowRenderer) this.shadowMapRenderer).getFramebuffer();

		ExtendedShader extendedShader = NewShaderTests.create(name, source, framebuffer, framebuffer, baseline, fallbackAlpha, vertexFormat, updateNotifier, this, FogMode.LINEAR);

		loadedShaders.add(extendedShader);

		// TODO: waterShadowEnabled?
		// TODO: Audit these render targets...

		extendedShader.addIrisSampler("normals", this.customTextureManager.getNormals().getId());
		extendedShader.addIrisSampler("specular", this.customTextureManager.getSpecular().getId());
		extendedShader.addIrisSampler("shadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("watershadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex0", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex1", this.shadowMapRenderer.getDepthTextureNoTranslucentsId());
		extendedShader.addIrisSampler("depthtex0", this.renderTargets.getDepthTexture().getTextureId());
		extendedShader.addIrisSampler("depthtex1", this.renderTargets.getDepthTextureNoTranslucents().getTextureId());
		extendedShader.addIrisSampler("noisetex", this.customTextureManager.getNoiseTexture().getAsInt());
		extendedShader.addIrisSampler("shadowcolor", this.shadowMapRenderer.getColorTexture0Id());
		extendedShader.addIrisSampler("shadowcolor0", this.shadowMapRenderer.getColorTexture0Id());
		extendedShader.addIrisSampler("shadowcolor1", this.shadowMapRenderer.getColorTexture1Id());

		// TODO: colortex8 to 15
		for (int i = 0; i < 8; i++) {
			// TODO: This should be "alt" for programs executing after deferred.
			extendedShader.addIrisSampler("colortex" + i, this.renderTargets.get(i).getMainTexture());
		}

		for (int i = 1; i <= 4; i++) {
			// TODO: This should be "alt" for programs executing after deferred.

			// gaux1 -> colortex4, gaux2 -> colortex5, gaux3 -> colortex6, gaux4 -> colortex7
			extendedShader.addIrisSampler("gaux" + i, this.renderTargets.get(i + 3).getMainTexture());
		}

		return extendedShader;
	}

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		this.phase = phase;
	}

	@Override
	public WorldRenderingPhase getPhase() {
		return phase;
	}

	@Override
	public void beginLevelRendering() {
		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		// NB: execute this before resizing / clearing so that the center depth sample is retrieved properly.
		updateNotifier.onNewFrame();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		renderTargets.resizeIfNeeded(main.width, main.height);

		final ImmutableList<ClearPass> passes;

		if (renderTargets.isFullClearRequired()) {
			renderTargets.onFullClear();
			passes = clearPassesFull;
		} else {
			passes = clearPasses;
		}

		Vector3d fogColor3 = CapturedRenderingState.INSTANCE.getFogColor();

		// NB: The alpha value must be 1.0 here, or else you will get a bunch of bugs. Sildur's Vibrant Shaders
		//     will give you pink reflections and other weirdness if this is zero.
		Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);

		for (ClearPass clearPass : passes) {
			clearPass.execute(fogColor);
		}

		// Make sure to switch back to the main framebuffer. If we forget to do this then our alt buffers might be
		// cleared to the fog color, which absolutely is not what we want!
		//
		// If we forget to do this, then weird lines appear at the top of the screen and the right of the screen
		// on Sildur's Vibrant Shaders.
		main.bindWrite(true);

		isBeforeTranslucent = true;
	}

	@Override
	public void renderShadows(LevelRendererAccessor worldRenderer, Camera playerCamera) {
		this.shadowMapRenderer.renderShadows(worldRenderer, playerCamera);
	}

	@Override
	public void addDebugText(List<String> messages) {
		ShadowMapRenderer shadowMapRenderer = this.shadowMapRenderer;

		if (shadowMapRenderer instanceof ShadowRenderer) {
			messages.add("");
			shadowMapRenderer.addDebugText(messages);
		} else if (shadowMapRenderer instanceof EmptyShadowMapRenderer) {
			messages.add("");
			messages.add("[Iris] Shadow Maps: not used by shader pack");
		} else {
			throw new IllegalStateException("Unknown shadow map renderer type!");
		}
	}

	@Override
	public OptionalInt getForcedShadowRenderDistanceChunksForDisplay() {
		return forcedShadowRenderDistanceChunks;
	}

	@Override
	public void beginShadowRender() {

	}

	@Override
	public void endShadowRender() {

	}

	@Override
	public void beginHand() {
		// We need to copy the current depth texture so that depthtex2 can contain the depth values for
		// all non-translucent content excluding the hand, as required.
		baseline.bindAsReadBuffer();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoHand().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);
	}

	@Override
	public void beginTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use a destroyed world rendering pipeline");
		}

		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 can contain the depth values for
		// all non-translucent content, as required.
		baseline.bindAsReadBuffer();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);

		deferredRenderer.renderAll();

		RenderSystem.enableBlend();

		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();

		// Reset shader or whatever...
		RenderSystem.setShader(GameRenderer::getPositionShader);
	}

	@Override
	public void pushProgram(GbufferProgram program) {

	}

	@Override
	public void popProgram(GbufferProgram program) {

	}

	@Override
	public void finalizeLevelRendering() {
		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		return true;
	}

	@Override
	public boolean shouldRenderClouds() {
		return shouldRenderClouds;
	}

	@Override
	public boolean shouldRenderUnderwaterOverlay() {
		return shouldRenderUnderwaterOverlay;
	}

	@Override
	public boolean shouldRenderVignette() {
		return shouldRenderVignette;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return !oldLighting;
	}

	@Override
	public ShaderInstance getBasic() {
		return basic;
	}

	@Override
	public ShaderInstance getBasicColor() {
		return basicColor;
	}

	@Override
	public ShaderInstance getTextured() {
		return textured;
	}

	@Override
	public ShaderInstance getTexturedColor() {
		return texturedColor;
	}

	@Override
	public ShaderInstance getSkyBasic() {
		return skyBasic;
	}

	@Override
	public ShaderInstance getSkyBasicColor() {
		return skyBasicColor;
	}

	@Override
	public ShaderInstance getSkyTextured() {
		return skyTextured;
	}

	@Override
	public ShaderInstance getSkyTexturedColor() {
		return skyTexturedColor;
	}

	@Override
	public ShaderInstance getClouds() {
		return clouds;
	}

	@Override
	public ShaderInstance getTerrain() {
		return terrainSolid;
	}

	@Override
	public ShaderInstance getTerrainCutout() {
		return terrainCutout;
	}

	@Override
	public ShaderInstance getTerrainCutoutMipped() {
		return terrainCutoutMipped;
	}

	@Override
	public ShaderInstance getEntitiesCutout() {
		return entitiesCutout;
	}

	@Override
	public ShaderInstance getEntitiesEyes() {
		return entitiesEyes;
	}

	@Override
	public ShaderInstance getHandCutout() {
		return handCutout;
	}

	@Override
	public ShaderInstance getHandTranslucent() {
		return handTranslucent;
	}

	@Override
	public ShaderInstance getLeash() {
		return leash;
	}

	@Override
	public ShaderInstance getLightning() {
		return lightning;
	}

	@Override
	public ShaderInstance getParticles() {
		return particles;
	}

	@Override
	public ShaderInstance getWeather() {
		return weather;
	}

	@Override
	public ShaderInstance getCrumbling() {
		return crumbling;
	}

	@Override
	public ShaderInstance getText() {
		return text;
	}

	@Override
	public ShaderInstance getTextIntensity() {
		return textIntensity;
	}

	@Override
	public ShaderInstance getBlock() {
		return block;
	}

	@Override
	public ShaderInstance getBeacon() {
		return beacon;
	}

	@Override
	public ShaderInstance getEntitiesSolid() {
		return entitiesSolid;
	}

	@Override
	public ShaderInstance getShadowTerrainCutout() {
		return shadowTerrainCutout;
	}

	@Override
	public ShaderInstance getShadowEntitiesCutout() {
		return shadowEntitiesCutout;
	}

	@Override
	public ShaderInstance getShadowBeaconBeam() {
		return shadowEntitiesCutout;
	}

	@Override
	public ShaderInstance getTranslucent() {
		return terrainTranslucent;
	}

	@Override
	public ShaderInstance getGlint() {
		return glint;
	}

	@Override
	public ShaderInstance getLines() {
		return lines;
	}

	@Override
	public ShaderInstance getShadowLines() {
		return shadowLines;
	}

	private void destroyShaders() {
		// NB: If you forget this, shader reloads won't work!
		loadedShaders.forEach(shader -> {
			shader.clear();
			shader.close();
		});
	}

	@Override
	public void destroy() {
		destroyed = true;

		destroyShaders();

		// Unbind all textures
		//
		// This is necessary because we don't want destroyed render target textures to remain bound to certain texture
		// units. Vanilla appears to properly rebind all textures as needed, and we do so too, so this does not cause
		// issues elsewhere.
		//
		// Without this code, there will be weird issues when reloading certain shaderpacks.
		for (int i = 0; i < 16; i++) {
			GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0 + i);
			GlStateManager._bindTexture(0);
		}

		// Set the active texture unit to unit 0
		//
		// This seems to be what most code expects. It's a sane default in any case.
		GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0);

		for (int i = 0; i < 12; i++) {
			// Clear all shader textures
			RenderSystem.setShaderTexture(i, 0);
		}

		compositeRenderer.destroy();
		customTextureManager.destroy();

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		shadowMapRenderer.destroy();
		renderTargets.destroy();
	}

	@Override
	public SodiumTerrainPipeline getSodiumTerrainPipeline() {
		return sodiumTerrainPipeline;
	}

	@Override
	public FrameUpdateNotifier getFrameUpdateNotifier() {
		return updateNotifier;
	}

	@Override
	public float getSunPathRotation() {
		return sunPathRotation;
	}
}
