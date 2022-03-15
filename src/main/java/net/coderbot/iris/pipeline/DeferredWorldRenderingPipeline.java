package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockMaterialMapping;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTestOverride;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisImages;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Encapsulates the compiled shader program objects for the currently loaded shaderpack.
 */
public class DeferredWorldRenderingPipeline implements WorldRenderingPipeline {
	private final RenderTargets renderTargets;

	private final List<Pass> allPasses;

	@Nullable
	private final Pass basic;
	@Nullable
	private final Pass textured;
	@Nullable
	private final Pass texturedLit;
	@Nullable
	private final Pass basicOverlay;
	@Nullable
	private final Pass texturedOverlay;
	@Nullable
	private final Pass texturedLitOverlay;
	@Nullable
	private final Pass skyBasic;
	@Nullable
	private final Pass skyTextured;
	@Nullable
	private final Pass clouds;
	@Nullable
	private final Pass terrain;
	@Nullable
	private final Pass translucent;
	@Nullable
	private final Pass damagedBlock;
	@Nullable
	private final Pass weather;
	@Nullable
	private final Pass beaconBeam;
	@Nullable
	private final Pass entities;
	@Nullable
	private final Pass entityNoOverlay;
	@Nullable
	private final Pass blockEntities;
	@Nullable
	private final Pass hand;
	@Nullable
	private final Pass handNoOverlay;
	@Nullable
	private final Pass handTranslucent;
	@Nullable
	private final Pass glowingEntities;
	@Nullable
	private final Pass glint;
	@Nullable
	private final Pass eyes;
	@Nullable
	private final Pass eyesNoOverlay;

	private final ImmutableList<ClearPass> clearPassesFull;
	private final ImmutableList<ClearPass> clearPasses;

	private final GlFramebuffer baseline;

	private Runnable createShadowMapRenderer;
	private final CompositeRenderer prepareRenderer;
	private ShadowMapRenderer shadowMapRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final CustomTextureManager customTextureManager;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	private final ImmutableSet<Integer> flippedAfterPrepare;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	private final SodiumTerrainPipeline sodiumTerrainPipeline;

	private boolean isBeforeTranslucent;

	private final float sunPathRotation;
	private final boolean shouldRenderClouds;
	private final boolean shouldRenderUnderwaterOverlay;
	private final boolean shouldRenderVignette;
	private final boolean shouldWriteRainAndSnowToDepthBuffer;
	private final boolean shouldRenderParticlesBeforeDeferred;
	private final boolean oldLighting;
	private final OptionalInt forcedShadowRenderDistanceChunks;

	private final List<GbufferProgram> programStack = new ArrayList<>();
	private final List<String> programStackLog = new ArrayList<>();

	private WorldRenderingPhase phase;

	public DeferredWorldRenderingPipeline(ProgramSet programs) {
		Objects.requireNonNull(programs);

		this.shouldRenderClouds = programs.getPackDirectives().areCloudsEnabled();
		this.shouldRenderUnderwaterOverlay = programs.getPackDirectives().underwaterOverlay();
		this.shouldRenderVignette = programs.getPackDirectives().vignette();
		this.shouldWriteRainAndSnowToDepthBuffer = programs.getPackDirectives().rainDepth();
		this.shouldRenderParticlesBeforeDeferred = programs.getPackDirectives().areParticlesBeforeDeferred();
		this.oldLighting = programs.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();

		this.allPasses = new ArrayList<>();

		this.renderTargets = new RenderTargets(Minecraft.getInstance().getMainRenderTarget(), programs.getPackDirectives().getRenderTargetDirectives());
		this.sunPathRotation = programs.getPackDirectives().getSunPathRotation();

		PackShadowDirectives shadowDirectives = programs.getPackDirectives().getShadowDirectives();

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

		BlockRenderingSettings.INSTANCE.setBlockStateIds(
				BlockMaterialMapping.createBlockStateIdMap(programs.getPack().getIdMap().getBlockProperties()));

		BlockRenderingSettings.INSTANCE.setEntityIds(programs.getPack().getIdMap().getEntityIdMap());
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(programs.getPackDirectives().getAmbientOcclusionLevel());
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(programs.getPackDirectives().shouldUseSeparateAo());

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE2);

		customTextureManager = new CustomTextureManager(programs.getPackDirectives(), programs.getPack().getCustomTextureDataMap(), programs.getPack().getCustomNoiseTexture());

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		ImmutableSet<Integer> flippedBeforeShadow = ImmutableSet.of();

		createShadowMapRenderer = () -> {
			shadowMapRenderer = new ShadowRenderer(this, programs.getShadow().orElse(null),
					programs.getPackDirectives(), () -> flippedBeforeShadow, renderTargets,
					customTextureManager.getNormals(), customTextureManager.getSpecular(), customTextureManager.getNoiseTexture(),
					programs, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));
			createShadowMapRenderer = () -> {};
		};

		BufferFlipper flipper = new BufferFlipper();

		this.centerDepthSampler = new CenterDepthSampler(renderTargets, updateNotifier);

		Supplier<ShadowMapRenderer> shadowMapRendererSupplier = () -> {
			createShadowMapRenderer.run();
			return shadowMapRenderer;
		};

		this.prepareRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getPrepare(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("prepare_pre"));

		flippedAfterPrepare = flipper.snapshot();

		this.deferredRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getDeferred(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("deferred_pre"));

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getComposite(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("composite_pre"));
		this.finalPassRenderer = new FinalPassRenderer(programs, renderTargets, customTextureManager.getNoiseTexture(), updateNotifier, flipper.snapshot(),
				centerDepthSampler, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				this.compositeRenderer.getFlippedAtLeastOnceFinal());

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

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

		IntFunction<ProgramImages> createTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, flipped, renderTargets);

			if (IrisImages.hasShadowImages(builder)) {
				createShadowMapRenderer.run();
				IrisImages.addShadowColorImages(builder, shadowMapRenderer);
			}

			return builder.build();
		};

		IntFunction<ProgramSamplers> createShadowTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flippedAfterPrepare, renderTargets, false);
			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(), customTextureManager.getSpecular());
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

			// Only initialize these samplers if the shadow map renderer exists.
			// Otherwise, this program shouldn't be used at all?
			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor) && shadowMapRenderer != null) {
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowMapRenderer);
			}

			return builder.build();
		};

		IntFunction<ProgramImages> createShadowTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, () -> flippedAfterPrepare, renderTargets);

			if (IrisImages.hasShadowImages(builder) && shadowMapRenderer != null) {
				IrisImages.addShadowColorImages(builder, shadowMapRenderer);
			}

			return builder.build();
		};

		this.basic = programs.getGbuffersBasic().map(this::createPass).orElse(null);
		this.textured = programs.getGbuffersTextured().map(this::createPass).orElse(basic);
		this.texturedLit = programs.getGbuffersTexturedLit().map(this::createPass).orElse(textured);
		this.basicOverlay = programs.getGbuffersBasic().map(this::createEntityPass).orElse(null);
		this.texturedOverlay = programs.getGbuffersTextured().map(this::createEntityPass).orElse(basicOverlay);
		this.texturedLitOverlay = programs.getGbuffersTexturedLit().map(this::createEntityPass).orElse(texturedOverlay);
		this.skyBasic = programs.getGbuffersSkyBasic().map(this::createPass).orElse(basic);
		this.skyTextured = programs.getGbuffersSkyTextured().map(this::createPass).orElse(textured);
		this.clouds = programs.getGbuffersClouds().map(this::createPass).orElse(textured);
		this.terrain = programs.getGbuffersTerrain().map(this::createPass).orElse(texturedLit);
		this.translucent = programs.getGbuffersWater().map(this::createPass).orElse(terrain);
		this.damagedBlock = programs.getGbuffersDamagedBlock().map(this::createPass).orElse(terrain);
		this.weather = programs.getGbuffersWeather().map(this::createPass).orElse(texturedLit);
		this.beaconBeam = programs.getGbuffersBeaconBeam().map(this::createPass).orElse(textured);
		this.entities = programs.getGbuffersEntities().map(this::createEntityPass).orElse(texturedLitOverlay);
		this.entityNoOverlay = programs.getGbuffersEntities().map(this::createPass).orElse(texturedLit);
		this.blockEntities = programs.getGbuffersBlock().map(this::createPass).orElse(terrain);
		this.hand = programs.getGbuffersHand().map(this::createEntityPass).orElse(texturedLitOverlay);
		this.handNoOverlay = programs.getGbuffersHand().map(this::createPass).orElse(texturedLit);
		this.handTranslucent = programs.getGbuffersHandWater().map(this::createEntityPass).orElse(hand);
		this.glowingEntities = programs.getGbuffersEntitiesGlowing().map(this::createEntityPass).orElse(entities);
		this.glint = programs.getGbuffersGlint().map(this::createPass).orElse(textured);
		this.eyes = programs.getGbuffersEntityEyes().map(this::createEntityPass).orElse(texturedOverlay);
		this.eyesNoOverlay = programs.getGbuffersEntityEyes().map(this::createPass).orElse(textured);

		this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true,
				programs.getPackDirectives().getRenderTargetDirectives());
		this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false,
				programs.getPackDirectives().getRenderTargetDirectives());

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		if (shadowMapRenderer == null) {
			// Fallback just in case.
			// TODO: Can we remove this?
			this.shadowMapRenderer = new EmptyShadowMapRenderer(programs.getPackDirectives().getShadowDirectives().getResolution());
		}

		this.phase = WorldRenderingPhase.NONE;

		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(this, programs, createTerrainSamplers,
			shadowMapRenderer instanceof EmptyShadowMapRenderer || shadowMapRenderer == null ? null : createShadowTerrainSamplers,
			createTerrainImages, createShadowTerrainImages);
	}

	private void checkWorld() {
		// If we're not in a world, then obviously we cannot possibly be rendering a world.
		if (Minecraft.getInstance().level == null) {
			isRenderingWorld = false;
			programStackLog.clear();
			programStack.clear();
		}
	}

	@Override
	public void pushProgram(GbufferProgram program) {
		checkWorld();

		if (!isRenderingWorld || isRenderingShadow) {
			// don't mess with non-world rendering
			return;
		}

		programStack.add(program);
		useProgram(program);
		programStackLog.add("push:" + program);
	}

	@Override
	public void popProgram(GbufferProgram expected) {
		checkWorld();

		if (!isRenderingWorld || isRenderingShadow) {
			// don't mess with non-world rendering
			return;
		}

		if (programStack.isEmpty()) {
			Iris.logger.fatal("Tried to pop from an empty program stack!");
			Iris.logger.fatal("Program stack log: " + programStackLog);
			throw new IllegalStateException("Tried to pop from an empty program stack!");
		}

		// Equivalent to pop(), but a bit more verbose.
		// This shouldn't have the same performance issues that remove() normally has since we're removing from the end
		// every time.
		GbufferProgram popped = programStack.remove(programStack.size() - 1);

		if (popped != expected) {
			Iris.logger.fatal("Program stack in invalid state, popped " + popped + " but expected to pop " + expected);
			Iris.logger.fatal("Program stack content after pop: " + programStack);
			throw new IllegalStateException("Program stack in invalid state, popped " + popped + " but expected to pop " + expected);
		}

		if (popped != GbufferProgram.NONE) {
			Pass poppedPass = getPass(popped);

			if (poppedPass != null) {
				poppedPass.stopUsing();
			}
		}

		programStackLog.add("pop:" + popped);

		if (programStack.isEmpty()) {
			// No remaining program, use fixed-function rendering
			teardownProgram();
			return;
		}

		// Use the previous program
		GbufferProgram toUse = programStack.get(programStack.size() - 1);

		useProgram(toUse);
	}

	private Pass getPass(GbufferProgram program) {
		switch (program) {
			case TERRAIN:
				return terrain;
			case TRANSLUCENT_TERRAIN:
				return translucent;
			case DAMAGED_BLOCKS:
				return damagedBlock;
			case BASIC:
				return basic;
			case LINES:
				return basic;
			case BEACON_BEAM:
				return beaconBeam;
			case ENTITIES:
				return entities;
			case ENTITY_NO_OVERLAY:
				return entityNoOverlay;
			case BLOCK_ENTITIES:
				return blockEntities;
			case ENTITIES_GLOWING:
				return glowingEntities;
			case EYES:
				return eyes;
			case EYES_NO_OVERLAY:
				return eyesNoOverlay;
			case ARMOR_GLINT:
				return glint;
			case CLOUDS:
				return clouds;
			case SKY_BASIC:
				return skyBasic;
			case SKY_TEXTURED:
				return skyTextured;
			case TEXTURED_LIT:
				return texturedLit;
			case TEXTURED:
				return textured;
			case WEATHER:
				return weather;
			case HAND:
				return hand;
			case HAND_NO_OVERLAY:
				return handNoOverlay;
			case HAND_TRANSLUCENT:
				return handTranslucent;
			default:
				// TODO
				throw new UnsupportedOperationException("TODO: Unsupported gbuffer program: " + program);
		}
	}

	private void useProgram(GbufferProgram program) {
		if (program == GbufferProgram.NONE) {
			// Note that we don't unbind the framebuffer here. Uses of GbufferProgram.NONE
			// are responsible for ensuring that the framebuffer is switched properly.
			Program.unbind();
			return;
		}

		Pass pass = getPass(program);
		beginPass(pass);

		if (program == GbufferProgram.TERRAIN) {
			if (terrain != null) {
				setupAttributeDefaults(terrain);
			}
		} else if (program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			if (translucent != null) {
				setupAttributeDefaults(translucent);
			}
		}
	}

	private void teardownProgram() {
		Program.unbind();
		this.baseline.bind();
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return shadowMapRenderer instanceof ShadowRenderer;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return !oldLighting;
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
	public boolean shouldWriteRainAndSnowToDepthBuffer() {
		return shouldWriteRainAndSnowToDepthBuffer;
	}

	@Override
	public boolean shouldRenderParticlesBeforeDeferred() {
		return shouldRenderParticlesBeforeDeferred;
	}

	@Override
	public float getSunPathRotation() {
		return sunPathRotation;
	}

	private void beginPass(Pass pass) {
		if (pass != null) {
			pass.use();
		} else {
			Program.unbind();
			this.baseline.bind();
		}
	}

	private Pass createPass(ProgramSource source) {
		// TODO: Properly handle empty shaders
		ProgramBuilder builder;
		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElseThrow(NullPointerException::new), source.getGeometrySource().orElse(null),
					source.getFragmentSource().orElseThrow(NullPointerException::new), IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		return createPassInner(builder, source.getParent().getPack().getIdMap(), source.getDirectives(), source.getParent().getPackDirectives());
	}

	private Pass createEntityPass(ProgramSource source) {
		// TODO: Properly handle empty shaders
		String geometry = source.getGeometrySource().orElse(null);
		String vertex = AttributeShaderTransformer.patch(source.getVertexSource().orElseThrow(NullPointerException::new),
				ShaderType.VERTEX, geometry != null);
		String fragment = AttributeShaderTransformer.patch(source.getFragmentSource().orElseThrow(NullPointerException::new),
				ShaderType.FRAGMENT, geometry != null);

		ProgramBuilder builder;
		try {
			builder = ProgramBuilder.begin(source.getName(), vertex, geometry,
					fragment, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		return createPassInner(builder, source.getParent().getPack().getIdMap(), source.getDirectives(), source.getParent().getPackDirectives());
	}

	private Pass createPassInner(ProgramBuilder builder, IdMap map, ProgramDirectives programDirectives, PackDirectives packDirectives) {

		CommonUniforms.addCommonUniforms(builder, map, packDirectives, updateNotifier);

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
		IrisImages.addRenderTargetImages(builder, flipped, renderTargets);

		IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(), customTextureManager.getSpecular());
		IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			createShadowMapRenderer.run();
			IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowMapRenderer);
			IrisImages.addShadowColorImages(builder, shadowMapRenderer);
		}

		GlFramebuffer framebufferBeforeTranslucents =
				renderTargets.createGbufferFramebuffer(flippedAfterPrepare, programDirectives.getDrawBuffers());
		GlFramebuffer framebufferAfterTranslucents =
				renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, programDirectives.getDrawBuffers());

		builder.bindAttributeLocation(11, "mc_Entity");
		builder.bindAttributeLocation(12, "mc_midTexCoord");
		builder.bindAttributeLocation(13, "at_tangent");

		AlphaTestOverride alphaTestOverride = programDirectives.getAlphaTestOverride().orElse(null);

		Pass pass = new Pass(builder.build(), framebufferBeforeTranslucents, framebufferAfterTranslucents, alphaTestOverride,
				programDirectives.getBlendModeOverride());

		allPasses.add(pass);

		return pass;
	}

	private final class Pass {
		private final Program program;
		private final GlFramebuffer framebufferBeforeTranslucents;
		private final GlFramebuffer framebufferAfterTranslucents;
		private final AlphaTestOverride alphaTestOverride;
		private final BlendModeOverride blendModeOverride;

		private Pass(Program program, GlFramebuffer framebufferBeforeTranslucents, GlFramebuffer framebufferAfterTranslucents,
					 AlphaTestOverride alphaTestOverride, BlendModeOverride blendModeOverride) {
			this.program = program;
			this.framebufferBeforeTranslucents = framebufferBeforeTranslucents;
			this.framebufferAfterTranslucents = framebufferAfterTranslucents;
			this.alphaTestOverride = alphaTestOverride;
			this.blendModeOverride = blendModeOverride;
		}

		public void use() {
			if (isBeforeTranslucent) {
				framebufferBeforeTranslucents.bind();
			} else {
				framebufferAfterTranslucents.bind();
			}

			program.use();

			if (alphaTestOverride != null) {
				alphaTestOverride.apply();
			} else {
				// Previous program on the stack might have applied an override
				AlphaTestOverride.restore();
			}

			if (blendModeOverride != null) {
				blendModeOverride.apply();
			} else {
				// Previous program on the stack might have applied an override
				BlendModeOverride.restore();
			}
		}

		public void stopUsing() {
			if (alphaTestOverride != null) {
				AlphaTestOverride.restore();
			}

			if (blendModeOverride != null) {
				BlendModeOverride.restore();
			}
		}

		public Program getProgram() {
			return program;
		}

		public void destroy() {
			this.program.destroy();
		}
	}

	public void destroy() {
		destroyPasses(allPasses);

		// Destroy the composite rendering pipeline
		//
		// This destroys all of the loaded composite programs as well.
		compositeRenderer.destroy();
		deferredRenderer.destroy();
		finalPassRenderer.destroy();

		// Make sure that any custom framebuffers are not bound before destroying render targets
		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		// Destroy our render targets
		//
		// While it's possible to just clear them instead and reuse them, we'd need to investigate whether or not this
		// would help performance.
		renderTargets.destroy();

		// Destroy the shadow map renderer and its render targets
		shadowMapRenderer.destroy();

		// Destroy custom textures and the static samplers (normals, specular, and noise)
		customTextureManager.destroy();
	}

	private static void destroyPasses(List<Pass> passes) {
		Set<Pass> destroyed = new HashSet<>();

		for (Pass pass : passes) {
			if (pass == null) {
				continue;
			}

			if (destroyed.contains(pass)) {
				continue;
			}

			pass.destroy();
			destroyed.add(pass);
		}
	}

	private static void setupAttributeDefaults(Pass pass) {
		setupAttribute(pass, "mc_Entity", 11, -1.0F, -1.0F, -1.0F, -1.0F);
		setupAttribute(pass, "mc_midTexCoord", 12, 0.0F, 0.0F, 0.0F, 0.0F);
		setupAttribute(pass, "at_tangent", 13, 1.0F, 0.0F, 0.0F, 1.0F);
	}

	private static void setupAttribute(Pass pass, String name, int expectedLocation, float v0, float v1, float v2, float v3) {
		int location = IrisRenderSystem.getAttribLocation(pass.getProgram().getProgramId(), name);

		if (location != -1) {
			if (location != expectedLocation) {
				throw new IllegalStateException();
			}

			IrisRenderSystem.vertexAttrib4f(location, v0, v1, v2, v3);
		}
	}

	private void prepareRenderTargets() {
		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

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
	}

	@Override
	public void beginHand() {
		// We need to copy the current depth texture so that depthtex2 can contain the depth values for
		// all non-translucent content without the hand, as required.
		baseline.bind();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoHand().getTextureId());
		IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);
	}

	@Override
	public void beginTranslucents() {
		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 can contain the depth values for
		// all non-translucent content, as required.
		baseline.bind();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);

		deferredRenderer.renderAll();
		Program.unbind();

		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();

		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();

		if (!programStack.isEmpty()) {
			GbufferProgram toUse = programStack.get(programStack.size() - 1);

			useProgram(toUse);
		} else {
			useProgram(GbufferProgram.NONE);
			baseline.bind();
		}
	}

	@Override
	public void renderShadows(LevelRendererAccessor levelRenderer, Camera playerCamera) {
		this.shadowMapRenderer.renderShadows(levelRenderer, playerCamera);

		prepareRenderer.renderAll();
	}

	@Override
	public void addDebugText(List<String> messages) {
		if (shadowMapRenderer != null) {
			messages.add("");
			shadowMapRenderer.addDebugText(messages);
		}
	}

	@Override
	public OptionalInt getForcedShadowRenderDistanceChunksForDisplay() {
		return forcedShadowRenderDistanceChunks;
	}

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;

	@Override
	public void beginLevelRendering() {
		isRenderingWorld = true;
		isBeforeTranslucent = true;
		phase = WorldRenderingPhase.NONE;
		HandRenderer.INSTANCE.getBufferSource().resetDrawCalls();

		checkWorld();

		if (!isRenderingWorld) {
			Iris.logger.warn("beginWorldRender was called but we are not currently rendering a world?");
			return;
		}

		if (!programStack.isEmpty()) {
			throw new IllegalStateException("Program stack before the start of rendering, something has gone very wrong!");
		}

		updateNotifier.onNewFrame();

		// Get ready for world rendering
		prepareRenderTargets();

		// Default to rendering with BASIC for all unknown content.
		// This probably isn't the best approach, but it works for now.
		pushProgram(GbufferProgram.BASIC);
	}

	@Override
	public void finalizeLevelRendering() {
		checkWorld();

		if (!isRenderingWorld) {
			Iris.logger.warn("finalizeWorldRendering was called but we are not currently rendering a world?");
			return;
		}

		popProgram(GbufferProgram.BASIC);

		if (!programStack.isEmpty()) {
			Iris.logger.fatal("Program stack not empty at end of rendering, something has gone very wrong!");
			Iris.logger.fatal("Program stack log: " + programStackLog);
			Iris.logger.fatal("Program stack content: " + programStack);
			throw new IllegalStateException("Program stack not empty at end of rendering, something has gone very wrong!");
		}

		isRenderingWorld = false;
		phase = WorldRenderingPhase.NONE;
		programStackLog.clear();

		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
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
	public WorldRenderingPhase getPhase() {
		return phase;
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		this.phase = phase;
		GbufferPrograms.runPhaseChangeNotifier();
	}

	private boolean isRenderingShadow = false;

	public void beginShadowRender() {
		isRenderingShadow = true;
	}

	public void endShadowRender() {
		isRenderingShadow = false;
	}
}
