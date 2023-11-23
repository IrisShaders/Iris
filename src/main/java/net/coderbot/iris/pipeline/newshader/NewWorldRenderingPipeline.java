package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.coderbot.iris.block_rendering.BlockMaterialMapping;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.colorspace.ColorSpace;
import net.coderbot.iris.colorspace.ColorSpaceConverter;
import net.coderbot.iris.colorspace.ColorSpaceFragmentConverter;
import net.coderbot.iris.compat.dh.DHCompat;
import net.coderbot.iris.features.FeatureFlags;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.GlImage;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.ComputeProgram;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.shader.ShaderCompileException;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ClearPass;
import net.coderbot.iris.pipeline.ClearPassCreator;
import net.coderbot.iris.pipeline.CustomTextureManager;
import net.coderbot.iris.pipeline.HorizonRenderer;
import net.coderbot.iris.pipeline.ShaderPrinter;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisImages;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.CloudSetting;
import net.coderbot.iris.shaderpack.ComputeSource;
import net.coderbot.iris.shaderpack.ImageInformation;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ParticleRenderingSettings;
import net.coderbot.iris.shaderpack.ProgramFallbackResolver;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shadows.ShadowCompositeRenderer;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRTextureHolder;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.coderbot.iris.texture.pbr.PBRType;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBClearTexture;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline, RenderTargetStateListener {
	private final RenderTargets renderTargets;
	private final ShaderMap shaderMap;
	private final CustomUniforms customUniforms;
	private final ShadowCompositeRenderer shadowCompositeRenderer;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> customTextureMap;
	private final ComputeProgram[] setup;
	private final boolean separateHardwareSamplers;
	private ShaderStorageBufferHolder shaderStorageBufferHolder;
	private final ProgramFallbackResolver resolver;

	private ShadowRenderTargets shadowRenderTargets;
	private final Supplier<ShadowRenderTargets> shadowTargetsSupplier;

	private WorldRenderingPhase overridePhase = null;
	private WorldRenderingPhase phase = WorldRenderingPhase.NONE;

	private final Set<ShaderInstance> loadedShaders;
	private ImmutableList<ClearPass> clearPassesFull;
	private ImmutableList<ClearPass> clearPasses;
	private ImmutableList<ClearPass> shadowClearPasses;
	private ImmutableList<ClearPass> shadowClearPassesFull;
	private final GlFramebuffer baseline;

	private final CompositeRenderer beginRenderer;
	private final CompositeRenderer prepareRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;

	private final CustomTextureManager customTextureManager;
	private final DynamicTexture whitePixel;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;
	private final SodiumTerrainPipeline sodiumTerrainPipeline;
	private final ColorSpaceConverter colorSpaceConverter;

	private final ImmutableSet<Integer> flippedBeforeShadow;
	private final ImmutableSet<Integer> flippedAfterPrepare;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	public boolean isBeforeTranslucent;

	private final HorizonRenderer horizonRenderer = new HorizonRenderer();
	@Nullable
	private ComputeProgram[] shadowComputes;

	private final float sunPathRotation;
	private final boolean shouldRenderUnderwaterOverlay;
	private final boolean shouldRenderVignette;
	private final boolean shouldWriteRainAndSnowToDepthBuffer;
	private final boolean oldLighting;
	private final OptionalInt forcedShadowRenderDistanceChunks;
	private boolean destroyed = false;
	private boolean isRenderingWorld;
	private boolean isMainBound;
	private boolean frustumCulling;
	private final CloudSetting cloudSetting;
	private final boolean shouldRenderSun;
	private final boolean shouldRenderMoon;
	private final boolean allowConcurrentCompute;

	@Nullable
	private final ShadowRenderer shadowRenderer;
	private final int shadowMapResolution;
	private boolean shouldBindPBR;
	private int currentNormalTexture;
	private int currentSpecularTexture;
	private ParticleRenderingSettings particleRenderingSettings;
	private PackDirectives packDirectives;
	private Set<GlImage> customImages;
	private GlImage[] clearImages;
	private final ShaderPack pack;
	private PackShadowDirectives shadowDirectives;
	private ColorSpace currentColorSpace;
	private DHCompat dhCompat;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		ShaderPrinter.resetPrintState();

		this.shouldRenderUnderwaterOverlay = programSet.getPackDirectives().underwaterOverlay();
		this.shouldRenderVignette = programSet.getPackDirectives().vignette();
		this.shouldWriteRainAndSnowToDepthBuffer = programSet.getPackDirectives().rainDepth();
		this.oldLighting = programSet.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();
		this.packDirectives = programSet.getPackDirectives();
		this.customTextureMap = programSet.getPackDirectives().getTextureMap();
		this.separateHardwareSamplers = programSet.getPack().hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS);
		this.shadowDirectives = packDirectives.getShadowDirectives();
		this.cloudSetting = programSet.getPackDirectives().getCloudSetting();
		this.shouldRenderSun = programSet.getPackDirectives().shouldRenderSun();
		this.shouldRenderMoon = programSet.getPackDirectives().shouldRenderMoon();
		this.allowConcurrentCompute = programSet.getPackDirectives().getConcurrentCompute();
		this.frustumCulling = programSet.getPackDirectives().shouldUseFrustumCulling();
		this.dhCompat = new DHCompat();

		this.resolver = new ProgramFallbackResolver(programSet);
		this.pack = programSet.getPack();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		int depthTextureId = main.getDepthTextureId();
		int internalFormat = TextureInfoCache.INSTANCE.getInfo(depthTextureId).getInternalFormat();
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(internalFormat);

		this.customImages = new HashSet<>();
		for (ImageInformation information : programSet.getPack().getIrisCustomImages()) {
			if (information.isRelative()) {
				customImages.add(new GlImage.Relative(information.name(), information.samplerName(), information.format(), information.internalTextureFormat(), information.type(), information.clear(), information.relativeWidth(), information.relativeHeight(), main.width, main.height));
			} else {
				customImages.add(new GlImage(information.name(), information.samplerName(), information.target(), information.format(), information.internalTextureFormat(), information.type(), information.clear(), information.width(), information.height(), information.depth()));
			}
		}

		this.clearImages = customImages.stream().filter(GlImage::shouldClear).toArray(GlImage[]::new);

		this.particleRenderingSettings = programSet.getPackDirectives().getParticleRenderingSettings().orElseGet(() -> {
			if (programSet.getDeferred().length > 0 && !programSet.getPackDirectives().shouldUseSeparateEntityDraws()) {
				return ParticleRenderingSettings.AFTER;
			} else {
				return ParticleRenderingSettings.MIXED;
			}
		});

		this.renderTargets = new RenderTargets(main.width, main.height, depthTextureId, ((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthBufferFormat, programSet.getPackDirectives().getRenderTargetDirectives().getRenderTargetSettings(), programSet.getPackDirectives());
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

		this.customUniforms = programSet.getPack().customUniforms.build(
			holder -> CommonUniforms.addNonDynamicUniforms(holder, programSet.getPack().getIdMap(), programSet.getPackDirectives(), this.updateNotifier)
		);

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE2);

		customTextureManager = new CustomTextureManager(programSet.getPackDirectives(), programSet.getPack().getCustomTextureDataMap(), programSet.getPack().getIrisCustomTextureDataMap(), programSet.getPack().getCustomNoiseTexture());
		whitePixel = new NativeImageBackedSingleColorTexture(255, 255, 255, 255);

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		BufferFlipper flipper = new BufferFlipper();


		this.centerDepthSampler = new CenterDepthSampler(() -> renderTargets.getDepthTexture(), programSet.getPackDirectives().getCenterDepthHalfLife());

		this.shadowMapResolution = programSet.getPackDirectives().getShadowDirectives().getResolution();

		this.shadowTargetsSupplier = () -> {
			if (shadowRenderTargets == null) {
				// TODO: Support more than two shadowcolor render targets
				this.shadowRenderTargets = new ShadowRenderTargets(this, shadowMapResolution, shadowDirectives);
			}

			return shadowRenderTargets;
		};

		if (!programSet.getPackDirectives().getBufferObjects().isEmpty()) {
			if (IrisRenderSystem.supportsSSBO()) {
				this.shaderStorageBufferHolder = new ShaderStorageBufferHolder(programSet.getPackDirectives().getBufferObjects(), main.width, main.height);

				this.shaderStorageBufferHolder.setupBuffers();
			} else {
				throw new IllegalStateException("Shader storage buffers/immutable buffer storage is not supported on this graphics card, however the shaderpack requested them? This shouldn't be possible.");
			}
		} else {
			for (int i = 0; i < Math.min(16, SamplerLimits.get().getMaxShaderStorageUnits()); i++) {
				IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, i, 0);
			}
		}

		this.shadowComputes = createShadowComputes(programSet.getShadowCompute(), programSet);

		this.beginRenderer = new CompositeRenderer(this, programSet.getPackDirectives(), programSet.getBegin(), programSet.getBeginCompute(), renderTargets,
			customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.BEGIN,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.BEGIN, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			programSet.getPackDirectives().getExplicitFlips("begin_pre"), customUniforms);

		flippedBeforeShadow = flipper.snapshot();

		this.prepareRenderer = new CompositeRenderer(this, programSet.getPackDirectives(), programSet.getPrepare(), programSet.getPrepareCompute(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.PREPARE,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
				programSet.getPackDirectives().getExplicitFlips("prepare_pre"), customUniforms);

		flippedAfterPrepare = flipper.snapshot();

		this.deferredRenderer = new CompositeRenderer(this, programSet.getPackDirectives(), programSet.getDeferred(), programSet.getDeferredCompute(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.DEFERRED,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
				programSet.getPackDirectives().getExplicitFlips("deferred_pre"), customUniforms);

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(this, programSet.getPackDirectives(), programSet.getComposite(), programSet.getCompositeCompute(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.COMPOSITE_AND_FINAL,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
				programSet.getPackDirectives().getExplicitFlips("composite_pre"), customUniforms);
		this.finalPassRenderer = new FinalPassRenderer(this, programSet, renderTargets, customTextureManager.getNoiseTexture(), updateNotifier, flipper.snapshot(),
				centerDepthSampler, shadowTargetsSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
				this.compositeRenderer.getFlippedAtLeastOnceFinal(), customUniforms);

		Supplier<ImmutableSet<Integer>> flipped =
			() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		IntFunction<ProgramSamplers> createTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
			IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());

			if (!shouldBindPBR) {
				shouldBindPBR = IrisSamplers.hasPBRSamplers(customTextureSamplerInterceptor);
			}

			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, this, whitePixel, new InputAvailability(true, true, false));
			IrisSamplers.addWorldDepthSamplers(customTextureSamplerInterceptor, renderTargets);
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());
			IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);

			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
				// we compiled the non-Sodium version of this program first... so if this is somehow null, something
				// very odd is going on.
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, Objects.requireNonNull(shadowRenderTargets), null, separateHardwareSamplers);
			}

			return builder.build();
		};

		IntFunction<ProgramImages> createTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, flipped, renderTargets);
			IrisImages.addCustomImages(builder, customImages);

			if (IrisImages.hasShadowImages(builder)) {
				// we compiled the non-Sodium version of this program first... so if this is somehow null, something
				// very odd is going on.
				IrisImages.addShadowColorImages(builder, Objects.requireNonNull(shadowRenderTargets), null);
			}

			return builder.build();
		};

		IntFunction<ProgramSamplers> createShadowTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flippedBeforeShadow, renderTargets, false);
			IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());

			if (!shouldBindPBR) {
				shouldBindPBR = IrisSamplers.hasPBRSamplers(customTextureSamplerInterceptor);
			}

			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, this, whitePixel, new InputAvailability(true, true, false));
			IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());
			IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);

			// Only initialize these samplers if the shadow map renderer exists.
			// Otherwise, this program shouldn't be used at all?
			if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
				// We don't compile Sodium shadow programs unless there's a shadow pass... And a shadow pass
				// can only exist if the shadow render targets have been created by detecting their
				// usage in a different program. So this null-check makes sense here.
				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, Objects.requireNonNull(shadowRenderTargets), null, separateHardwareSamplers);
			}

			return builder.build();
		};

		IntFunction<ProgramImages> createShadowTerrainImages = (programId) -> {
			ProgramImages.Builder builder = ProgramImages.builder(programId);

			IrisImages.addRenderTargetImages(builder, () -> flippedBeforeShadow, renderTargets);
			IrisImages.addCustomImages(builder, customImages);

			if (IrisImages.hasShadowImages(builder)) {
				// We don't compile Sodium shadow programs unless there's a shadow pass... And a shadow pass
				// can only exist if the shadow render targets have been created by detecting their
				// usage in a different program. So this null-check makes sense here.
				IrisImages.addShadowColorImages(builder, Objects.requireNonNull(shadowRenderTargets), null);
			}

			return builder.build();
		};

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});
		this.loadedShaders = new HashSet<>();


		this.shaderMap = new ShaderMap(key -> {
			try {
				if (key.isShadow()) {
					if (shadowRenderTargets != null) {
						return createShadowShader(key.getName(), resolver.resolve(key.getProgram()), key);
					} else {
						return null;
					}
				} else {
					return createShader(key.getName(), resolver.resolve(key.getProgram()), key);
				}
			} catch (FakeChainedJsonException e) {
				destroyShaders();
				throw e.getTrueException();
			} catch (IOException e) {
				destroyShaders();
				throw new RuntimeException(e);
			} catch (RuntimeException e) {
				destroyShaders();
				throw e;
			}
		});

		BlockRenderingSettings.INSTANCE.setBlockStateIds(
				BlockMaterialMapping.createBlockStateIdMap(programSet.getPack().getIdMap().getBlockProperties()));
		BlockRenderingSettings.INSTANCE.setBlockTypeIds(BlockMaterialMapping.createBlockTypeMap(programSet.getPack().getIdMap().getBlockRenderTypeMap()));

		BlockRenderingSettings.INSTANCE.setEntityIds(programSet.getPack().getIdMap().getEntityIdMap());
		BlockRenderingSettings.INSTANCE.setItemIds(programSet.getPack().getIdMap().getItemIdMap());
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(programSet.getPackDirectives().getAmbientOcclusionLevel());
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(programSet.getPackDirectives().shouldUseSeparateAo());
		BlockRenderingSettings.INSTANCE.setVoxelizeLightBlocks(programSet.getPackDirectives().shouldVoxelizeLightBlocks());
		BlockRenderingSettings.INSTANCE.setSeparateEntityDraws(programSet.getPackDirectives().shouldUseSeparateEntityDraws());
		BlockRenderingSettings.INSTANCE.setUseExtendedVertexFormat(true);

		if (shadowRenderTargets == null && shadowDirectives.isShadowEnabled() == OptionalBoolean.TRUE) {
			shadowRenderTargets = new ShadowRenderTargets(this, shadowMapResolution, shadowDirectives);
		}

		if (shadowRenderTargets != null) {
			ShaderInstance shader = shaderMap.getShader(ShaderKey.SHADOW_TERRAIN_CUTOUT);
			boolean shadowUsesImages = false;

			if (shader instanceof ExtendedShader) {
				ExtendedShader shader2 = (ExtendedShader) shader;
				shadowUsesImages = shader2.hasActiveImages();
			}

			this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
			this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
			this.shadowCompositeRenderer = new ShadowCompositeRenderer(this, programSet.getPackDirectives(), programSet.getShadowComposite(), programSet.getShadowCompCompute(), this.shadowRenderTargets, customTextureManager.getNoiseTexture(), updateNotifier,
				customTextureManager.getCustomTextureIdMap(TextureStage.SHADOWCOMP), customImages, programSet.getPackDirectives().getExplicitFlips("shadowcomp_pre"), customTextureManager.getIrisCustomTextures(), customUniforms);

			if (programSet.getPackDirectives().getShadowDirectives().isShadowEnabled().orElse(true)) {
				this.shadowRenderer = new ShadowRenderer(programSet.getShadow().orElse(null),
					programSet.getPackDirectives(), shadowRenderTargets, shadowCompositeRenderer, customUniforms, programSet.getPack().hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
			} else {
				shadowRenderer = null;
			}

		} else {
			this.shadowClearPasses = ImmutableList.of();
			this.shadowClearPassesFull = ImmutableList.of();
			this.shadowCompositeRenderer = null;
			this.shadowRenderer = null;
		}

		dhCompat.setFramebuffer(renderTargets.createGbufferFramebuffer(ImmutableSet.of(), new int[] { 0 }));
		// TODO: Create fallback Sodium shaders if the pack doesn't provide terrain shaders
		//       Currently we use Sodium's shaders but they don't support EXP2 fog underwater.
		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(this, programSet, createTerrainSamplers,
			shadowRenderTargets == null ? null : createShadowTerrainSamplers, createTerrainImages, createShadowTerrainImages, renderTargets, flippedAfterPrepare, flippedAfterTranslucent,
			shadowRenderTargets != null ? shadowRenderTargets.createShadowFramebuffer(ImmutableSet.of(), programSet.getShadow().filter(source -> !source.getDirectives().hasUnknownDrawBuffers()).map(source -> source.getDirectives().getDrawBuffers()).orElse(new int[]{0, 1})) : null, customUniforms);


		this.setup = createSetupComputes(programSet.getSetup(), programSet, TextureStage.SETUP);

		// first optimization pass
		this.customUniforms.optimise();
		boolean hasRun = false;

		this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true,
			programSet.getPackDirectives().getRenderTargetDirectives());
		this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false,
			programSet.getPackDirectives().getRenderTargetDirectives());

		for (ComputeProgram program : setup) {
			if (program != null) {
				if (!hasRun) {
					hasRun = true;
					renderTargets.onFullClear();
					Vector3d fogColor3 = CapturedRenderingState.INSTANCE.getFogColor();

					// NB: The alpha value must be 1.0 here, or else you will get a bunch of bugs. Sildur's Vibrant Shaders
					//     will give you pink reflections and other weirdness if this is zero.
					Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);

					clearPassesFull.forEach(clearPass -> clearPass.execute(fogColor));
				}
				program.use();
				program.dispatch(1, 1);
			}
		}

		if (hasRun) {
			ComputeProgram.unbind();
		}

		if (programSet.getPackDirectives().supportsColorCorrection()) {
			colorSpaceConverter = new ColorSpaceConverter() {
				@Override
				public void rebuildProgram(int width, int height, ColorSpace colorSpace) {

				}

				@Override
				public void process(int target) {

				}
			};
		} else {
			// TODO: Fix grid appearing on some devices with compute converter
			//if (IrisRenderSystem.supportsCompute()) {
			//	colorSpaceConverter = new ColorSpaceComputeConverter(main.width, main.height, IrisVideoSettings.colorSpace);
			//} else {
				colorSpaceConverter = new ColorSpaceFragmentConverter(main.width, main.height, IrisVideoSettings.colorSpace);
			//}
		}

		currentColorSpace = IrisVideoSettings.colorSpace;
	}

	private ComputeProgram[] createShadowComputes(ComputeSource[] compute, ProgramSet programSet) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || !source.getSource().isPresent()) {
				continue;
			} else {
				ProgramBuilder builder;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), TextureStage.GBUFFERS_AND_SHADOW, customTextureMap);

					ShaderPrinter.printProgram(source.getName()).addSource(PatchShaderType.COMPUTE, transformed).print();

					builder = ProgramBuilder.beginCompute(source.getName(), transformed, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
				} catch (ShaderCompileException e) {
					throw e;
				} catch (RuntimeException e) {
					// TODO: Better error handling
					throw new RuntimeException("Shader compilation failed for compute " + source.getName() + "!", e);
				}

				CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
				customUniforms.assignTo(builder);

				Supplier<ImmutableSet<Integer>> flipped;

				flipped = () -> flippedBeforeShadow;

				TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

				ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor =
					ProgramSamplers.customTextureSamplerInterceptor(builder,
						customTextureManager.getCustomTextureIdMap(textureStage));

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false);
				IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());
				IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
				IrisImages.addRenderTargetImages(builder, flipped, renderTargets);
				IrisImages.addCustomImages(builder, customImages);

				IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, this, whitePixel, new InputAvailability(true, true, false));

				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

				if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
					if (shadowRenderTargets != null) {
						IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets, null, separateHardwareSamplers);
						IrisImages.addShadowColorImages(builder, shadowRenderTargets, null);
					}
				}

				programs[i] = builder.buildCompute();

				this.customUniforms.mapholderToPass(builder, programs[i]);

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups());
			}
		}


		return programs;
	}

	private ComputeProgram[] createSetupComputes(ComputeSource[] compute, ProgramSet programSet, TextureStage stage) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || !source.getSource().isPresent()) {
				continue;
			} else {
				ProgramBuilder builder;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), stage, customTextureMap);

					ShaderPrinter.printProgram(source.getName()).addSource(PatchShaderType.COMPUTE, transformed).print();

					builder = ProgramBuilder.beginCompute(source.getName(), transformed, IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
				} catch (RuntimeException e) {
					// TODO: Better error handling
					throw new RuntimeException("Shader compilation failed for setup compute " + source.getName() + "!", e);
				}

				CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
				customUniforms.assignTo(builder);

				ImmutableSet<Integer> empty = ImmutableSet.of();
				Supplier<ImmutableSet<Integer>> flipped;

				flipped = () -> empty;

				TextureStage textureStage = TextureStage.SETUP;

				ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor =
					ProgramSamplers.customTextureSamplerInterceptor(builder,
						customTextureManager.getCustomTextureIdMap(textureStage));

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, true);
				IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());
				IrisSamplers.addCompositeSamplers(builder, renderTargets);
				IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
				IrisImages.addRenderTargetImages(builder, flipped, renderTargets);
				IrisImages.addCustomImages(builder, customImages);

				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

				if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
					if (shadowRenderTargets != null) {
						IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets, null, separateHardwareSamplers);
						IrisImages.addShadowColorImages(builder, shadowRenderTargets, null);
					}
				}


				programs[i] = builder.buildCompute();

				this.customUniforms.mapholderToPass(builder, programs[i]);

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups());
			}
		}


		return programs;
	}

	private ShaderInstance createShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (!source.isPresent()) {
			return createFallbackShader(name, key);
		}

		return createShader(name, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(), key.getFogMode(),
				key.isIntensity(), key.shouldIgnoreLightmap(), key.isGlint(), key.isText());
	}

	@Override
	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return customTextureMap;
	}

	private ShaderInstance createShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, FogMode fogMode,
										boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, source.getDirectives().getDrawBuffers());
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, source.getDirectives().getDrawBuffers());
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);


		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, isGlint, isText);

		Supplier<ImmutableSet<Integer>> flipped =
			() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;


		ExtendedShader extendedShader = NewShaderTests.create(this, name, source, programId, beforeTranslucent, afterTranslucent,
				baseline, fallbackAlpha, vertexFormat, inputs, updateNotifier, this, flipped, fogMode, isIntensity, isFullbright, false, isLines, customUniforms);

		loadedShaders.add(extendedShader);

		return extendedShader;
	}

	private ShaderInstance createFallbackShader(String name, ShaderKey key) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, new int[] {0});

		FallbackShader shader = NewShaderTests.createFallback(name, beforeTranslucent, afterTranslucent,
				key.getAlphaTest(), key.getVertexFormat(), null, this, key.getFogMode(),
				key == ShaderKey.GLINT, key.isText(), key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		loadedShaders.add(shader);

		return shader;
	}

	private ShaderInstance createShadowShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (!source.isPresent()) {
			return createFallbackShadowShader(name, key);
		}

		return createShadowShader(name, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(),
				key.isIntensity(), key.shouldIgnoreLightmap(), key.isText());
	}

	private ShaderInstance createFallbackShadowShader(String name, ShaderKey key) throws IOException {
		GlFramebuffer framebuffer = shadowRenderTargets.createShadowFramebuffer(ImmutableSet.of(), new int[] { 0 });

		FallbackShader shader = NewShaderTests.createFallback(name, framebuffer, framebuffer,
				key.getAlphaTest(), key.getVertexFormat(), BlendModeOverride.OFF, this, key.getFogMode(),
				key == ShaderKey.GLINT, key.isText(), key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		loadedShaders.add(shader);

		return shader;
	}

	private ShaderInstance createShadowShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
											  VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText) throws IOException {
		GlFramebuffer framebuffer = shadowRenderTargets.createShadowFramebuffer(ImmutableSet.of(), source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers());
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, false, isText);

		Supplier<ImmutableSet<Integer>> flipped = () -> flippedBeforeShadow;

		ExtendedShader extendedShader = NewShaderTests.create(this, name, source, programId, framebuffer, framebuffer, baseline,
				fallbackAlpha, vertexFormat, inputs, updateNotifier, this, flipped, FogMode.PER_VERTEX, isIntensity, isFullbright, true, isLines, customUniforms);

		loadedShaders.add(extendedShader);

		return extendedShader;
	}

	public void addGbufferOrShadowSamplers(SamplerHolder samplers, ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
										   boolean isShadowPass, InputAvailability availability) {
		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor samplerHolder =
				ProgramSamplers.customTextureSamplerInterceptor(samplers,
						customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(samplerHolder, flipped, renderTargets, false);
		IrisSamplers.addCustomTextures(samplerHolder, customTextureManager.getIrisCustomTextures());
		IrisImages.addRenderTargetImages(images, flipped, renderTargets);
		IrisImages.addCustomImages(images, customImages);

		if (!shouldBindPBR) {
			shouldBindPBR = IrisSamplers.hasPBRSamplers(samplerHolder);
		}

		IrisSamplers.addLevelSamplers(samplers, this, whitePixel, availability);
		IrisSamplers.addWorldDepthSamplers(samplerHolder, this.renderTargets);
		IrisSamplers.addNoiseSampler(samplerHolder, this.customTextureManager.getNoiseTexture());
		IrisSamplers.addCustomImages(samplerHolder, customImages);

		if (isShadowPass || IrisSamplers.hasShadowSamplers(samplerHolder)) {
			if (!isShadowPass) {
				shadowTargetsSupplier.get();
			}

			IrisSamplers.addShadowSamplers(samplerHolder, Objects.requireNonNull(shadowRenderTargets), null, separateHardwareSamplers);
		}

		if (isShadowPass || IrisImages.hasShadowImages(images)) {
			// Note: hasShadowSamplers currently queries for shadow images too, so the shadow render targets will be
			// created by this point... that's sorta ugly, though.
			IrisImages.addShadowColorImages(images, Objects.requireNonNull(shadowRenderTargets), null);
		}
	}

	@Override
	public WorldRenderingPhase getPhase() {
		if (overridePhase != null) {
			return overridePhase;
		}

		return phase;
	}

	@Override
	public void beginSodiumTerrainRendering() {
		// no-op
	}

	@Override
	public void endSodiumTerrainRendering() {
		// no-op
	}

	@Override
	public void setOverridePhase(WorldRenderingPhase phase) {
		this.overridePhase = phase;
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		this.phase = phase;
	}

	@Override
	public void setSpecialCondition(SpecialCondition special) {
		// no-op
	}

	@Override
	public RenderTargetStateListener getRenderTargetStateListener() {
		return this;
	}

	@Override
	public int getCurrentNormalTexture() {
		return currentNormalTexture;
	}

	@Override
	public int getCurrentSpecularTexture() {
		return currentSpecularTexture;
	}

	@Override
	public void onSetShaderTexture(int id) {
		if (shouldBindPBR && isRenderingWorld) {
			PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(id);
			currentNormalTexture = pbrHolder.getNormalTexture().getId();
			currentSpecularTexture = pbrHolder.getSpecularTexture().getId();

			TextureFormat textureFormat = TextureFormatLoader.getFormat();
			if (textureFormat != null) {
				int previousBinding = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
				textureFormat.setupTextureParameters(PBRType.NORMAL, pbrHolder.getNormalTexture());
				textureFormat.setupTextureParameters(PBRType.SPECULAR, pbrHolder.getSpecularTexture());
				GlStateManager._bindTexture(previousBinding);
			}

			PBRTextureManager.notifyPBRTexturesChanged();
		}
	}

	@Override
	public void onShadowBufferChange() {
		this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
		this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
	}

	@Override
	public void beginLevelRendering() {
		isRenderingWorld = true;

		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
		Vector4f emptyClearColor = new Vector4f(1.0F);

		for (GlImage image : clearImages) {
			ARBClearTexture.glClearTexImage(image.getId(), 0, image.getFormat().getGlFormat(), image.getPixelType().getGlFormat(), (int[]) null);
		}

		if (shadowRenderTargets != null) {
			if (packDirectives.getShadowDirectives().isShadowEnabled() == OptionalBoolean.FALSE) {
				if (shadowRenderTargets.isFullClearRequired()) {
					shadowRenderTargets.onFullClear();
					for (ClearPass clearPass : shadowClearPassesFull) {
						clearPass.execute(emptyClearColor);
					}
				}
			} else {
				// Clear depth first, regardless of any color clearing.
				shadowRenderTargets.getDepthSourceFb().bind();
				RenderSystem.clear(GL21C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

				ImmutableList<ClearPass> passes;

			for (ComputeProgram computeProgram : shadowComputes) {
				if (computeProgram != null) {
					computeProgram.use();
					customUniforms.push(computeProgram);
					computeProgram.dispatch(shadowMapResolution, shadowMapResolution);
				}
			}

				if (shadowRenderTargets.isFullClearRequired()) {
					passes = shadowClearPassesFull;
					shadowRenderTargets.onFullClear();
				} else {
					passes = shadowClearPasses;
				}

				for (ClearPass clearPass : passes) {
					clearPass.execute(emptyClearColor);
				}
			}
		}

		// NB: execute this before resizing / clearing so that the center depth sample is retrieved properly.
		updateNotifier.onNewFrame();

		// Update custom uniforms
		this.customUniforms.update();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();

		int depthTextureId = main.getDepthTextureId();
		int internalFormat = TextureInfoCache.INSTANCE.getInfo(depthTextureId).getInternalFormat();
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(internalFormat);

		boolean changed = renderTargets.resizeIfNeeded(((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthTextureId, main.width,
			main.height, depthBufferFormat, packDirectives);

		if (changed) {
			beginRenderer.recalculateSizes();
			prepareRenderer.recalculateSizes();
			deferredRenderer.recalculateSizes();
			compositeRenderer.recalculateSizes();
			finalPassRenderer.recalculateSwapPassSize();
			if (shaderStorageBufferHolder != null) {
				shaderStorageBufferHolder.hasResizedScreen(main.width, main.height);
			}

			customImages.forEach(image -> image.updateNewSize(main.width, main.height));

			this.clearPassesFull.forEach(clearPass -> renderTargets.destroyFramebuffer(clearPass.getFramebuffer()));
			this.clearPasses.forEach(clearPass -> renderTargets.destroyFramebuffer(clearPass.getFramebuffer()));

			this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true,
				packDirectives.getRenderTargetDirectives());
			this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false,
				packDirectives.getRenderTargetDirectives());
		}

		if (changed || IrisVideoSettings.colorSpace != currentColorSpace) {
			currentColorSpace = IrisVideoSettings.colorSpace;
			colorSpaceConverter.rebuildProgram(main.width, main.height, currentColorSpace);
		}

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
		isMainBound = true;

		if (changed) {
			boolean hasRun = false;

			for (ComputeProgram program : setup) {
				if (program != null) {
					hasRun = true;
					program.use();
					program.dispatch(1, 1);
				}
			}

			if (hasRun) {
				ComputeProgram.unbind();
			}
		}

		isBeforeTranslucent = true;

		beginRenderer.renderAll();

		setPhase(WorldRenderingPhase.SKY);

		// Render our horizon box before actual sky rendering to avoid being broken by mods that do weird things
		// while rendering the sky.
		//
		// A lot of dimension mods touch sky rendering, FabricSkyboxes injects at HEAD and cancels, etc.
		DimensionSpecialEffects.SkyType skyType = Minecraft.getInstance().level.effects().skyType();

		if (skyType == DimensionSpecialEffects.SkyType.NORMAL) {
			RenderSystem.depthMask(false);

			RenderSystem.setShaderColor(fogColor.x, fogColor.y, fogColor.z, fogColor.w);

			horizonRenderer.renderHorizon(CapturedRenderingState.INSTANCE.getGbufferModelView(), CapturedRenderingState.INSTANCE.getGbufferProjection(), GameRenderer.getPositionShader());

			RenderSystem.depthMask(true);

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	@Override
	public void renderShadows(LevelRendererAccessor worldRenderer, Camera playerCamera) {
		if (shadowRenderer != null) {
			this.shadowRenderer.renderShadows(worldRenderer, playerCamera);
		}

		prepareRenderer.renderAll();
	}

	@Override
	public void addDebugText(List<String> messages) {
		if (this.shadowRenderer != null) {
			messages.add("");
			shadowRenderer.addDebugText(messages);
		} else {
			messages.add("");
			messages.add("[Iris] Shadow Maps: not used by shader pack");
		}
	}

	@Override
	public OptionalInt getForcedShadowRenderDistanceChunksForDisplay() {
		return forcedShadowRenderDistanceChunks;
	}

	@Override
	public void beginHand() {
		centerDepthSampler.sampleCenterDepth();

		// We need to copy the current depth texture so that depthtex2 can contain the depth values for
		// all non-translucent content excluding the hand, as required.
		renderTargets.copyPreHandDepth();
	}

	@Override
	public void beginTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use a destroyed world rendering pipeline");
		}

		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 can contain the depth values for
		// all non-translucent content, as required.
		renderTargets.copyPreTranslucentDepth();

		deferredRenderer.renderAll();

		RenderSystem.enableBlend();

		// note: we are careful not to touch the lightmap texture unit or overlay color texture unit here,
		// so we don't need to do anything to restore them if needed.
		//
		// Previous versions of the code tried to "restore" things by enabling the lightmap & overlay color
		// but that actually broke rendering of clouds and rain by making them appear red in the case of
		// a pack not overriding those shader programs.
		//
		// Not good!

		// Reset shader or whatever...
		RenderSystem.setShader(GameRenderer::getPositionShader);
	}

	@Override
	public void finalizeLevelRendering() {
		isRenderingWorld = false;
		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
		colorSpaceConverter.process(Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return shadowRenderer != null;
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
	public boolean shouldRenderSun() {
		return shouldRenderSun;
	}

	@Override
	public boolean shouldRenderMoon() {
		return shouldRenderMoon;
	}

	@Override
	public boolean shouldWriteRainAndSnowToDepthBuffer() {
		return shouldWriteRainAndSnowToDepthBuffer;
	}

	@Override
	public ParticleRenderingSettings getParticleRenderingSettings() {
		return particleRenderingSettings;
	}

	@Override
	public boolean allowConcurrentCompute() {
		return allowConcurrentCompute;
	}

	@Override
	public boolean hasFeature(FeatureFlags flag) {
		return pack.hasFeature(flag);
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return !oldLighting;
	}

	@Override
	public boolean shouldDisableFrustumCulling() {
		return !frustumCulling;
	}

	@Override
	public CloudSetting getCloudSetting() {
		return cloudSetting;
	}

	@Override
	public ShaderMap getShaderMap() {
		return shaderMap;
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
			IrisRenderSystem.unbindAllSamplers();
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

		if (shadowCompositeRenderer != null) {
			shadowCompositeRenderer.destroy();
		}

		prepareRenderer.destroy();
		compositeRenderer.destroy();
		deferredRenderer.destroy();
		finalPassRenderer.destroy();
		centerDepthSampler.destroy();
		customTextureManager.destroy();
		whitePixel.close();

		horizonRenderer.destroy();

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		renderTargets.destroy();

		customImages.forEach(GlImage::destroy);

		if (shadowRenderer != null) {
			shadowRenderer.destroy();
		}

		if (shaderStorageBufferHolder != null) {
			shaderStorageBufferHolder.destroyBuffers();
		}
	}

	@Override
	public boolean shouldOverrideShaders() {
		return isRenderingWorld && isMainBound;
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

	@Override
	public DHCompat getDHCompat() {
		return dhCompat;
	}

	protected AbstractTexture getWhitePixel() {
		return whitePixel;
	}

	@Override
	public void beginPostChain() {

	}

	@Override
	public void endPostChain() {

	}

	@Override
	public void setIsMainBound(boolean bound) {
		isMainBound = bound;
	}
}
