package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockMaterialMapping;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gbuffer_overrides.matching.ProgramTable;
import net.coderbot.iris.gbuffer_overrides.matching.RenderCondition;
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTestOverride;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisImages;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramFallbackResolver;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Encapsulates the compiled shader program objects for the currently loaded shaderpack.
 */
public class DeferredWorldRenderingPipeline implements WorldRenderingPipeline, RenderTargetStateListener  {
	private final RenderTargets renderTargets;

	@Nullable
	private ShadowRenderTargets shadowRenderTargets;
	private final Supplier<ShadowRenderTargets> shadowTargetsSupplier;

	private final ProgramTable<Pass> table;

	private final ImmutableList<ClearPass> clearPassesFull;
	private final ImmutableList<ClearPass> clearPasses;

	private final GlFramebuffer baseline;

	private final CompositeRenderer prepareRenderer;

	@Nullable
	private final ShadowRenderer shadowRenderer;

	private final int shadowMapResolution;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final CustomTextureManager customTextureManager;
	private final AbstractTexture whitePixel;
	private final FrameUpdateNotifier updateNotifier;

	private final ImmutableSet<Integer> flippedBeforeShadow;
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

	private Pass current = null;

	private WorldRenderingPhase phase = WorldRenderingPhase.NONE;
	private InputAvailability inputs = new InputAvailability(false, false, false);

	public DeferredWorldRenderingPipeline(ProgramSet programs) {
		Objects.requireNonNull(programs);

		this.shouldRenderClouds = programs.getPackDirectives().areCloudsEnabled();
		this.shouldRenderUnderwaterOverlay = programs.getPackDirectives().underwaterOverlay();
		this.shouldRenderVignette = programs.getPackDirectives().vignette();
		this.shouldWriteRainAndSnowToDepthBuffer = programs.getPackDirectives().rainDepth();
		this.shouldRenderParticlesBeforeDeferred = programs.getPackDirectives().areParticlesBeforeDeferred();
		this.oldLighting = programs.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();

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

		whitePixel = new NativeImageBackedSingleColorTexture(255, 255, 255, 255);

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		this.flippedBeforeShadow = ImmutableSet.of();

		BufferFlipper flipper = new BufferFlipper();

		CenterDepthSampler centerDepthSampler = new CenterDepthSampler(renderTargets, updateNotifier);

		this.shadowMapResolution = programs.getPackDirectives().getShadowDirectives().getResolution();

		this.shadowTargetsSupplier = () -> {
			if (shadowRenderTargets == null) {
				// TODO: Support more than two shadowcolor render targets
				this.shadowRenderTargets = new ShadowRenderTargets(shadowMapResolution, new InternalTextureFormat[]{
					// TODO: Custom shadowcolor format support
					InternalTextureFormat.RGBA,
					InternalTextureFormat.RGBA
				});
			}

			return shadowRenderTargets;
		};

		this.prepareRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getPrepare(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("prepare_pre"));

		flippedAfterPrepare = flipper.snapshot();

		this.deferredRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getDeferred(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("deferred_pre"));

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getComposite(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				programs.getPackDirectives().getExplicitFlips("composite_pre"));
		this.finalPassRenderer = new FinalPassRenderer(programs, renderTargets, customTextureManager.getNoiseTexture(), updateNotifier, flipper.snapshot(),
				centerDepthSampler, shadowTargetsSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				this.compositeRenderer.getFlippedAtLeastOnceFinal());

		// [(textured=false,lightmap=false), (textured=true,lightmap=false), (textured=true,lightmap=true)]
		ProgramId[] ids = new ProgramId[] {
				ProgramId.Basic, ProgramId.Textured, ProgramId.TexturedLit,
				ProgramId.SkyBasic, ProgramId.SkyTextured, ProgramId.SkyTextured,
				null, null, ProgramId.Terrain,
				null, null, ProgramId.Water,
				null, ProgramId.Clouds, ProgramId.Clouds,
				null, ProgramId.DamagedBlock, ProgramId.DamagedBlock,
				ProgramId.Block, ProgramId.Block, ProgramId.Block,
				ProgramId.BeaconBeam, ProgramId.BeaconBeam, ProgramId.BeaconBeam,
				ProgramId.Entities, ProgramId.Entities, ProgramId.Entities,
				null, ProgramId.ArmorGlint, ProgramId.ArmorGlint,
				null, ProgramId.SpiderEyes, ProgramId.SpiderEyes,
				ProgramId.Hand, ProgramId.Hand, ProgramId.Hand,
				ProgramId.HandWater, ProgramId.HandWater, ProgramId.HandWater,
				null, null, ProgramId.Weather,
				// world border uses textured_lit even though it has no lightmap :/
				null, ProgramId.TexturedLit, ProgramId.TexturedLit,
				ProgramId.Shadow, ProgramId.Shadow, ProgramId.Shadow
		};

		if (ids.length != RenderCondition.values().length * 3) {
			throw new IllegalStateException("Program ID table length mismatch");
		}

		ProgramFallbackResolver resolver = new ProgramFallbackResolver(programs);

		Map<Pair<ProgramId, InputAvailability>, Pass> cachedPasses = new HashMap<>();

		this.table = new ProgramTable<>((condition, availability) -> {
			int idx;

			if (availability.texture && availability.lightmap) {
				idx = 2;
			} else if (availability.texture) {
				idx = 1;
			} else {
				idx = 0;
			}

			ProgramId id = ids[condition.ordinal() * 3 + idx];

			if (id == null) {
				id = ids[idx];
			}

			return cachedPasses.computeIfAbsent(new Pair<>(id, availability), p -> {
				ProgramSource source = resolver.resolveNullable(p.getFirst());

				if (condition == RenderCondition.SHADOW) {
					if (shadowRenderTargets == null) {
						// shadow is not used
						return null;
					} else if (source == null) {
						// still need the custom framebuffer, viewport, and blend mode behavior
						GlFramebuffer shadowFb = shadowRenderTargets.getFramebuffer();
						return new Pass(null, shadowFb, shadowFb, null,
							BlendModeOverride.OFF, true);
					}
				}

				if (source == null) {
					return createDefaultPass();
				}

				return createPass(source, availability, condition == RenderCondition.SHADOW);
			});
		});

		this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true,
				programs.getPackDirectives().getRenderTargetDirectives());
		this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false,
				programs.getPackDirectives().getRenderTargetDirectives());

		this.baseline = renderTargets.createGbufferFramebuffer(ImmutableSet.of(), new int[] {0});

		if (shadowRenderTargets != null) {
			Program shadowProgram = table.match(RenderCondition.SHADOW, new InputAvailability(true, true, true)).getProgram();
			boolean shadowUsesImages = shadowProgram != null && shadowProgram.getActiveImages() > 0;

			this.shadowRenderer = new ShadowRenderer(programs.getShadow().orElse(null),
				programs.getPackDirectives(), programs, shadowRenderTargets, shadowUsesImages);
		} else {
			this.shadowRenderer = null;
		}

		// SodiumTerrainPipeline setup follows.

		Supplier<ImmutableSet<Integer>> flipped =
			() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		IntFunction<ProgramSamplers> createTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(),
				customTextureManager.getSpecular(), whitePixel, new InputAvailability(true, true, false));
			IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, Objects.requireNonNull(shadowRenderTargets));
			}

			return builder.build();
		};

		IntFunction<ProgramImages> createTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, flipped, renderTargets);

			if (IrisImages.hasShadowImages(builder)) {
				IrisImages.addShadowColorImages(builder, Objects.requireNonNull(shadowRenderTargets));
			}

			return builder.build();
		};

		IntFunction<ProgramSamplers> createShadowTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flippedAfterPrepare, renderTargets, false);
			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(),
				customTextureManager.getSpecular(), whitePixel, new InputAvailability(true, true, false));
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

			// Only initialize these samplers if the shadow map renderer exists.
			// Otherwise, this program shouldn't be used at all?
			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, Objects.requireNonNull(shadowRenderTargets));
			}

			return builder.build();
		};

		IntFunction<ProgramImages> createShadowTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, () -> flippedAfterPrepare, renderTargets);

			if (IrisImages.hasShadowImages(builder)) {
				IrisImages.addShadowColorImages(builder, Objects.requireNonNull(shadowRenderTargets));
			}

			return builder.build();
		};

		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(this, programs, createTerrainSamplers,
			shadowRenderer == null ? null : createShadowTerrainSamplers, createTerrainImages,
			shadowRenderer == null ? null : createShadowTerrainImages);
	}

	private void checkWorld() {
		// If we're not in a world, then obviously we cannot possibly be rendering a world.
		if (Minecraft.getInstance().level == null) {
			isRenderingWorld = false;
			current = null;
		}
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return shadowRenderer != null;
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

	private RenderCondition getCondition(WorldRenderingPhase phase) {
		if (isRenderingShadow) {
			return RenderCondition.SHADOW;
		}

		// TODO: Special stages (entity_eyes, destroy, glint, beacon_beam)

		switch (phase) {
			case NONE:
			case OUTLINE:
			case DEBUG:
			case PARTICLES:
				return RenderCondition.DEFAULT;
			case SKY:
			case SUNSET:
			case CUSTOM_SKY:
			case SUN:
			case MOON:
			case STARS:
			case VOID:
				return RenderCondition.SKY;
			case TERRAIN_SOLID:
			case TERRAIN_CUTOUT:
			case TERRAIN_CUTOUT_MIPPED:
				return RenderCondition.TERRAIN_OPAQUE;
			case ENTITIES:
				return RenderCondition.ENTITIES;
			case BLOCK_ENTITIES:
				return RenderCondition.BLOCK_ENTITIES;
			case DESTROY:
				return RenderCondition.DESTROY;
			case HAND_SOLID:
				return RenderCondition.HAND_OPAQUE;
			case TERRAIN_TRANSLUCENT:
			case TRIPWIRE:
				return RenderCondition.TERRAIN_TRANSLUCENT;
			case CLOUDS:
				return RenderCondition.CLOUDS;
			case RAIN_SNOW:
				return RenderCondition.RAIN_SNOW;
			case HAND_TRANSLUCENT:
				return RenderCondition.HAND_TRANSLUCENT;
			case WORLD_BORDER:
				return RenderCondition.WORLD_BORDER;
			default:
				throw new IllegalStateException("Unknown render phase " + phase);
		}
	}

	private void matchPass() {
		if (!isRenderingWorld || isRenderingFullScreenPass || isPostChain || !isMainBound) {
			return;
		}

		if (sodiumTerrainRendering) {
			beginPass(table.match(getCondition(phase), new InputAvailability(true, true, false)));
			return;
		}

		beginPass(table.match(getCondition(phase), inputs));
	}

	public void beginPass(Pass pass) {
		if (current == pass) {
			return;
		}

		if (current != null) {
			current.stopUsing();
		}

		current = pass;

		if (pass != null) {
			pass.use();
		} else {
			Program.unbind();
		}
	}

	private Pass createDefaultPass() {
		GlFramebuffer framebufferBeforeTranslucents;
		GlFramebuffer framebufferAfterTranslucents;

		framebufferBeforeTranslucents =
			renderTargets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		framebufferAfterTranslucents =
			renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, new int[] {0});

		return new Pass(null, framebufferBeforeTranslucents, framebufferAfterTranslucents, null,
			null, false);
	}

	private Pass createPass(ProgramSource source, InputAvailability availability, boolean shadow) {
		// TODO: Properly handle empty shaders
		String geometry = source.getGeometrySource().orElse(null);
		String vertex = AttributeShaderTransformer.patch(source.getVertexSource().orElseThrow(NullPointerException::new),
				ShaderType.VERTEX, geometry != null, availability);
		String fragment = AttributeShaderTransformer.patch(source.getFragmentSource().orElseThrow(NullPointerException::new),
				ShaderType.FRAGMENT, geometry != null, availability);

		ProgramBuilder builder;
		try {
			builder = ProgramBuilder.begin(source.getName(), vertex, geometry,
					fragment, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		return createPassInner(builder, source.getParent().getPack().getIdMap(), source.getDirectives(), source.getParent().getPackDirectives(), availability, shadow);
	}

	private Pass createPassInner(ProgramBuilder builder, IdMap map, ProgramDirectives programDirectives,
								 PackDirectives packDirectives, InputAvailability availability, boolean shadow) {

		CommonUniforms.addCommonUniforms(builder, map, packDirectives, updateNotifier);

		Supplier<ImmutableSet<Integer>> flipped;

		if (shadow) {
			flipped = () -> flippedBeforeShadow;
		} else {
			flipped = () -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;
		}

		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor =
			ProgramSamplers.customTextureSamplerInterceptor(builder,
				customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
		IrisImages.addRenderTargetImages(builder, flipped, renderTargets);

		IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, customTextureManager.getNormals(),
			customTextureManager.getSpecular(), whitePixel, availability);

		if (!shadow) {
			IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
		}

		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			if (!shadow) {
				shadowTargetsSupplier.get();
			}

			if (shadowRenderTargets != null) {
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets);
				IrisImages.addShadowColorImages(builder, shadowRenderTargets);
			}
		}

		GlFramebuffer framebufferBeforeTranslucents;
		GlFramebuffer framebufferAfterTranslucents;

		if (shadow) {
			framebufferBeforeTranslucents = Objects.requireNonNull(shadowRenderTargets).getFramebuffer();
			framebufferAfterTranslucents = framebufferBeforeTranslucents;
		} else {
			framebufferBeforeTranslucents =
				renderTargets.createGbufferFramebuffer(flippedAfterPrepare, programDirectives.getDrawBuffers());
			framebufferAfterTranslucents =
				renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, programDirectives.getDrawBuffers());
		}

		builder.bindAttributeLocation(11, "mc_Entity");
		builder.bindAttributeLocation(12, "mc_midTexCoord");
		builder.bindAttributeLocation(13, "at_tangent");

		AlphaTestOverride alphaTestOverride = programDirectives.getAlphaTestOverride().orElse(null);

		return new Pass(builder.build(), framebufferBeforeTranslucents, framebufferAfterTranslucents, alphaTestOverride,
				programDirectives.getBlendModeOverride(), shadow);
	}

	private boolean isPostChain;
	private boolean isMainBound = true;

	@Override
	public void beginPostChain() {
		isPostChain = true;

		beginPass(null);
	}

	@Override
	public void endPostChain() {
		isPostChain = false;
	}

	@Override
	public void setIsMainBound(boolean bound) {
		isMainBound = bound;

		if (!isRenderingWorld || isRenderingFullScreenPass || isPostChain) {
			return;
		}

		if (bound) {
			// force refresh
			current = null;
		} else {
			beginPass(null);
		}
	}

	private final class Pass {
		@Nullable
		private final Program program;
		private final GlFramebuffer framebufferBeforeTranslucents;
		private final GlFramebuffer framebufferAfterTranslucents;
		@Nullable
		private final AlphaTestOverride alphaTestOverride;
		@Nullable
		private final BlendModeOverride blendModeOverride;
		private final boolean shadowViewport;

		private Pass(@Nullable Program program, GlFramebuffer framebufferBeforeTranslucents, GlFramebuffer framebufferAfterTranslucents,
					 @Nullable AlphaTestOverride alphaTestOverride, @Nullable BlendModeOverride blendModeOverride, boolean shadowViewport) {
			this.program = program;
			this.framebufferBeforeTranslucents = framebufferBeforeTranslucents;
			this.framebufferAfterTranslucents = framebufferAfterTranslucents;
			this.alphaTestOverride = alphaTestOverride;
			this.blendModeOverride = blendModeOverride;
			this.shadowViewport = shadowViewport;
		}

		public void use() {
			if (isBeforeTranslucent) {
				framebufferBeforeTranslucents.bind();
			} else {
				framebufferAfterTranslucents.bind();
			}

			if (shadowViewport) {
				RenderSystem.viewport(0, 0, shadowMapResolution, shadowMapResolution);
			} else {
				RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
				RenderSystem.viewport(0, 0, main.width, main.height);
			}

			if (program != null) {
				program.use();
			}

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

		@Nullable
		public Program getProgram() {
			return program;
		}

		public void destroy() {
			if (this.program != null) {
				this.program.destroy();
			}
		}
	}

	public void destroy() {
		BlendModeOverride.restore();
		AlphaTestOverride.restore();

		destroyPasses(table);

		// Destroy the composite rendering pipeline
		//
		// This destroys all the loaded composite programs as well.
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

		// destroy the shadow render targets
		if (shadowRenderTargets != null) {
			shadowRenderTargets.destroy();
		}

		// Destroy custom textures and the static samplers (normals, specular, and noise)
		customTextureManager.destroy();
		whitePixel.releaseId();
	}

	private static void destroyPasses(ProgramTable<Pass> table) {
		Set<Pass> destroyed = new HashSet<>();

		table.forEach(pass -> {
			if (pass == null) {
				return;
			}

			if (destroyed.contains(pass)) {
				return;
			}

			pass.destroy();
			destroyed.add(pass);
		});
	}

	private void prepareRenderTargets() {
		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		if (shadowRenderTargets != null) {
			// NB: This will be re-bound to the correct framebuffer in beginLevelRendering when matchPass is called.
			shadowRenderTargets.getFramebuffer().bind();

			// TODO: Support shadow clear color directives & disable buffer clearing
			// Ensure that the color and depth values are cleared appropriately
			RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.clearDepth(1.0f);
			RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_COLOR_BUFFER_BIT, false);
		}

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		Blaze3dRenderTargetExt mainExt = (Blaze3dRenderTargetExt) main;

		renderTargets.resizeIfNeeded(mainExt.iris$isDepthBufferDirty(), main.width, main.height);

		mainExt.iris$clearDepthBufferDirtyFlag();

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

		// needed to remove blend mode overrides and similar
		beginPass(null);

		isRenderingFullScreenPass = true;

		deferredRenderer.renderAll();

		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();

		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();

		isRenderingFullScreenPass = false;
	}

	@Override
	public void renderShadows(LevelRendererAccessor levelRenderer, Camera playerCamera) {
		if (shadowRenderer != null) {
			isRenderingShadow = true;

			shadowRenderer.renderShadows(levelRenderer, playerCamera);

			// needed to remove blend mode overrides and similar
			beginPass(null);
			isRenderingShadow = false;
		}

		isRenderingFullScreenPass = true;

		prepareRenderer.renderAll();

		isRenderingFullScreenPass = false;
	}

	@Override
	public void addDebugText(List<String> messages) {
		messages.add("");

		if (shadowRenderer != null) {
			shadowRenderer.addDebugText(messages);
		} else {
			messages.add("[Iris] Shadow Maps: not used by shader pack");
		}
	}

	@Override
	public OptionalInt getForcedShadowRenderDistanceChunksForDisplay() {
		return forcedShadowRenderDistanceChunks;
	}

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;
	private boolean isRenderingFullScreenPass = false;

	@Override
	public void beginLevelRendering() {
		isRenderingFullScreenPass = false;
		isRenderingWorld = true;
		isBeforeTranslucent = true;
		isMainBound = true;
		isPostChain = false;
		phase = WorldRenderingPhase.NONE;
		HandRenderer.INSTANCE.getBufferSource().resetDrawCalls();

		checkWorld();

		if (!isRenderingWorld) {
			Iris.logger.warn("beginWorldRender was called but we are not currently rendering a world?");
			return;
		}

		if (current != null) {
			throw new IllegalStateException("Called beginLevelRendering but level rendering appears to still be in progress?");
		}

		updateNotifier.onNewFrame();

		// Get ready for world rendering
		prepareRenderTargets();
	}

	@Override
	public void finalizeLevelRendering() {
		checkWorld();

		if (!isRenderingWorld) {
			Iris.logger.warn("finalizeWorldRendering was called but we are not currently rendering a world?");
			return;
		}

		beginPass(null);

		isRenderingWorld = false;
		phase = WorldRenderingPhase.NONE;

		isRenderingFullScreenPass = true;

		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();

		isRenderingFullScreenPass = false;
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

	boolean sodiumTerrainRendering = false;

	@Override
	public void syncProgram() {
		matchPass();
	}

	@Override
	public void beginSodiumTerrainRendering() {
		sodiumTerrainRendering = true;
		syncProgram();

	}

	@Override
	public void endSodiumTerrainRendering() {
		sodiumTerrainRendering = false;
		current = null;
		syncProgram();
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		this.phase = phase;

		GbufferPrograms.runPhaseChangeNotifier();
	}

	@Override
	public void setInputs(InputAvailability availability) {
		this.inputs = availability;
	}

	@Override
	public RenderTargetStateListener getRenderTargetStateListener() {
		return this;
	}

	private boolean isRenderingShadow = false;

	public void beginShadowRender() {
	}

	public void endShadowRender() {
	}
}
