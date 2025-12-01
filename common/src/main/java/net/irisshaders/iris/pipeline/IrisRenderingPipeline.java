package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.image.ImageClearPass;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.helpers.FakeChainedJsonException;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import net.irisshaders.iris.pathways.HorizonRenderer;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.irisshaders.iris.pathways.colorspace.ColorSpaceConverter;
import net.irisshaders.iris.pathways.colorspace.ColorSpaceFragmentConverter;
import net.irisshaders.iris.pbr.format.TextureFormat;
import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.texture.PBRTextureHolder;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.ShaderCreator;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderLoadingMap;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import net.irisshaders.iris.pipeline.programs.ShaderSupplier;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.shaderpack.ImageInformation;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.loading.ProgramArrayId;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.CloudSetting;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shaderpack.properties.ParticleRenderingSettings;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowCompositeRenderer;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.ClearPass;
import net.irisshaders.iris.targets.ClearPassCreator;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.targets.backed.NativeImageBackedSingleColorTexture;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.ARBClearTexture;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class IrisRenderingPipeline implements WorldRenderingPipeline, ShaderRenderingPipeline {
	private final RenderTargets renderTargets;
	private final ShaderMap shaderMap;
	private final CustomUniforms customUniforms;
	private final ShadowCompositeRenderer shadowCompositeRenderer;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> customTextureMap;
	private final ComputeProgram[] setup;
	private final boolean separateHardwareSamplers;
	private final ProgramFallbackResolver resolver;
	private final Supplier<ShadowRenderTargets> shadowTargetsSupplier;
	private final Set<GlProgram> loadedShaders;
	private final CompositeRenderer beginRenderer;
	private final CompositeRenderer prepareRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final CustomTextureManager customTextureManager;
	private final DynamicTexture whitePixel;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;
	private final ColorSpaceConverter colorSpaceConverter;
	private final ImmutableSet<Integer> flippedBeforeShadow;
	private final ImmutableSet<Integer> flippedAfterPrepare;
	private final ImmutableSet<Integer> flippedAfterTranslucent;
	private final HorizonRenderer horizonRenderer = new HorizonRenderer();
	@Nullable
	private final ComputeProgram[] shadowComputes;
	private final float sunPathRotation;
	private final boolean shouldRenderUnderwaterOverlay;
	private final boolean shouldRenderVignette;
	private final boolean shouldWriteRainAndSnowToDepthBuffer;
	private final boolean oldLighting;
	private final OptionalInt forcedShadowRenderDistanceChunks;
	private final boolean frustumCulling;
	private final boolean occlusionCulling;
	private final CloudSetting cloudSetting;
	private final boolean shouldRenderSun;
	private final boolean shouldRenderWeather;
	private final boolean shouldRenderWeatherParticles;
	private final boolean shouldRenderMoon;
	private final boolean shouldRenderStars;
	private final boolean shouldRenderSkyDisc;
	private final boolean allowConcurrentCompute;
	@Nullable
	private final ShadowRenderer shadowRenderer;
	private final int shadowMapResolution;
	private final ParticleRenderingSettings particleRenderingSettings;
	private final PackDirectives packDirectives;
	private final Set<GlImage> customImages;
	private final ImmutableList<ImageClearPass> clearImages;
	private final ShaderPack pack;
	private final PackShadowDirectives shadowDirectives;
	private final DHCompat dhCompat;
	private final int stackSize = 0;
	private final boolean skipAllRendering;
	private final CloudSetting dhCloudSetting;
	private final SodiumPrograms sodiumPrograms;
	public boolean isBeforeTranslucent;
	private boolean initializedBlockIds;
	private ShaderStorageBufferHolder shaderStorageBufferHolder;
	private ShadowRenderTargets shadowRenderTargets;
	private WorldRenderingPhase overridePhase = null;
	private WorldRenderingPhase phase = WorldRenderingPhase.NONE;
	private ImmutableList<ClearPass> clearPassesFull;
	private ImmutableList<ClearPass> clearPasses;
	private ImmutableList<ClearPass> shadowClearPasses;
	private ImmutableList<ClearPass> shadowClearPassesFull;
	private boolean destroyed = false;
	private boolean isRenderingWorld;
	private boolean isMainBound;
	private boolean shouldBindPBR;
	private int currentNormalTexture;
	private int currentSpecularTexture;
	private ColorSpace currentColorSpace;
	private GlFramebuffer defaultFB;
	private GlFramebuffer defaultFBAlt;
	private GlFramebuffer defaultFBShadow;
	private final boolean supportsEndFlash;

	public IrisRenderingPipeline(ProgramSet programSet) {
		ShaderPrinter.resetPrintState();

		this.shouldRenderUnderwaterOverlay = programSet.getPackDirectives().underwaterOverlay();
		this.supportsEndFlash = programSet.getPackDirectives().supportsEndFlash();
		this.shouldRenderVignette = programSet.getPackDirectives().vignette();
		this.shouldWriteRainAndSnowToDepthBuffer = programSet.getPackDirectives().rainDepth();
		this.oldLighting = programSet.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();
		this.packDirectives = programSet.getPackDirectives();
		this.customTextureMap = programSet.getPackDirectives().getTextureMap();
		this.separateHardwareSamplers = programSet.getPack().hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS);
		this.shadowDirectives = packDirectives.getShadowDirectives();
		this.cloudSetting = programSet.getPackDirectives().getCloudSetting();
		this.dhCloudSetting = programSet.getPackDirectives().getDHCloudSetting();
		this.shouldRenderSun = programSet.getPackDirectives().shouldRenderSun();
		this.shouldRenderWeather = programSet.getPackDirectives().shouldRenderWeather();
		this.shouldRenderWeatherParticles = programSet.getPackDirectives().shouldRenderWeatherParticles();
		this.shouldRenderMoon = programSet.getPackDirectives().shouldRenderMoon();
		this.shouldRenderStars = programSet.getPackDirectives().shouldRenderStars();
		this.shouldRenderSkyDisc = programSet.getPackDirectives().shouldRenderSkyDisc();
		this.allowConcurrentCompute = programSet.getPackDirectives().getConcurrentCompute();
		this.skipAllRendering = programSet.getPackDirectives().skipAllRendering();
		this.frustumCulling = programSet.getPackDirectives().shouldUseFrustumCulling();
		this.occlusionCulling = programSet.getPackDirectives().shouldUseOcclusionCulling();
		this.resolver = new ProgramFallbackResolver(programSet);
		this.pack = programSet.getPack();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		GpuTexture depthTexture  = main.getDepthTexture();
		int internalFormat = GlConst.toGlInternalId(depthTexture.getFormat());
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(internalFormat);

		if (!pack.getBufferObjects().isEmpty()) {
			if (IrisRenderSystem.supportsSSBO()) {
				this.shaderStorageBufferHolder = new ShaderStorageBufferHolder(pack.getBufferObjects(), main.width, main.height);

				this.shaderStorageBufferHolder.setupBuffers();
			} else {
				throw new IllegalStateException("Shader storage buffers/immutable buffer storage is not supported on this graphics card, however the shaderpack requested them? This shouldn't be possible.");
			}
		} else {
			for (int i = 0; i < Math.min(16, SamplerLimits.get().getMaxShaderStorageUnits()); i++) {
				IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, i, 0);
			}
		}

		this.customImages = new HashSet<>();
		for (ImageInformation information : programSet.getPack().getIrisCustomImages()) {
			if (information.isRelative()) {
				customImages.add(new GlImage.Relative(information.name(), information.samplerName(), information.format(), information.internalTextureFormat(), information.type(), information.clear(), information.relativeWidth(), information.relativeHeight(), main.width, main.height));
			} else {
				customImages.add(new GlImage(information.name(), information.samplerName(), information.target(), information.format(), information.internalTextureFormat(), information.type(), information.clear(), information.width(), information.height(), information.depth()));
			}
		}

		this.clearImages = customImages.stream()
			.filter(GlImage::shouldClear)
			.map(ImageClearPass::create)
			.collect(ImmutableList.toImmutableList());

		if (programSet.getPackDirectives().getParticleRenderingSettings() != ParticleRenderingSettings.UNSET) {
			this.particleRenderingSettings = programSet.getPackDirectives().getParticleRenderingSettings();
		} else if (programSet.getComposite(ProgramArrayId.Deferred).length > 0 && !programSet.getPackDirectives().shouldUseSeparateEntityDraws()) {
			this.particleRenderingSettings = ParticleRenderingSettings.AFTER;
		} else {
			this.particleRenderingSettings = ParticleRenderingSettings.MIXED;
		}


		this.renderTargets = new RenderTargets(main.width, main.height, depthTexture, ((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthBufferFormat, programSet.getPackDirectives().getRenderTargetDirectives().getRenderTargetSettings(), programSet.getPackDirectives());
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

		this.centerDepthSampler = new CenterDepthSampler(() -> renderTargets.getDepthTexture().iris$getGlId(), programSet.getPackDirectives().getCenterDepthHalfLife());

		this.shadowMapResolution = programSet.getPackDirectives().getShadowDirectives().getResolution();

		this.shadowTargetsSupplier = () -> {
			if (shadowRenderTargets == null) {
				// TODO: Support more than two shadowcolor render targets
				this.shadowRenderTargets = new ShadowRenderTargets(this, shadowMapResolution, shadowDirectives);
			}

			return shadowRenderTargets;
		};

		if (shadowDirectives.isShadowEnabled() == OptionalBoolean.TRUE) {
			shadowTargetsSupplier.get();
		}

		this.shadowComputes = createShadowComputes(programSet.getShadowCompute(), programSet);

		if (FullScreenQuadRenderer.init() != -1) throw new IllegalStateException("WHY");

		this.beginRenderer = new CompositeRenderer(this, CompositePass.BEGIN, programSet.getPackDirectives(), programSet.getComposite(ProgramArrayId.Begin), programSet.getCompute(ProgramArrayId.Begin), renderTargets, shaderStorageBufferHolder,
			customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.BEGIN,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.BEGIN, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			programSet.getPackDirectives().getExplicitFlips("begin_pre"), customUniforms);

		flippedBeforeShadow = flipper.snapshot();

		this.prepareRenderer = new CompositeRenderer(this, CompositePass.PREPARE, programSet.getPackDirectives(), programSet.getComposite(ProgramArrayId.Prepare), programSet.getCompute(ProgramArrayId.Prepare), renderTargets, shaderStorageBufferHolder,
			customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.PREPARE,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			programSet.getPackDirectives().getExplicitFlips("prepare_pre"), customUniforms);

		flippedAfterPrepare = flipper.snapshot();

		this.deferredRenderer = new CompositeRenderer(this, CompositePass.DEFERRED, programSet.getPackDirectives(), programSet.getComposite(ProgramArrayId.Deferred), programSet.getCompute(ProgramArrayId.Deferred), renderTargets, shaderStorageBufferHolder,
			customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.DEFERRED,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			programSet.getPackDirectives().getExplicitFlips("deferred_pre"), customUniforms);

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(this, CompositePass.COMPOSITE, programSet.getPackDirectives(), programSet.getComposite(ProgramArrayId.Composite), programSet.getCompute(ProgramArrayId.Composite), renderTargets, shaderStorageBufferHolder,
			customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowTargetsSupplier, TextureStage.COMPOSITE_AND_FINAL,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			programSet.getPackDirectives().getExplicitFlips("composite_pre"), customUniforms);
		this.finalPassRenderer = new FinalPassRenderer(this, programSet, renderTargets, customTextureManager.getNoiseTexture(), shaderStorageBufferHolder, updateNotifier, flipper.snapshot(),
			centerDepthSampler, shadowTargetsSupplier,
			customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()), customTextureManager.getIrisCustomTextures(), customImages,
			this.compositeRenderer.getFlippedAtLeastOnceFinal(), customUniforms);

		Supplier<ImmutableSet<Integer>> flipped =
			() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		IntFunction<ProgramSamplers> createTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.GBUFFERS_AND_SHADOW, Object2ObjectMaps.emptyMap()));

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false, this);
			IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());

			if (!shouldBindPBR) {
				shouldBindPBR = IrisSamplers.hasPBRSamplers(customTextureSamplerInterceptor);
			}

			IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, this, whitePixel, true, true, false);
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

		this.dhCompat = new DHCompat(this, shadowDirectives.isDhShadowEnabled().orElse(true));

		this.loadedShaders = new HashSet<>();


		ShaderLoadingMap loadingMap = new ShaderLoadingMap(key -> {
			try {
				if (key.isShadow()) {
					return createShadowShader(key.getName(), resolver.resolve(key.getProgram()), key);
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

		this.shaderMap = new ShaderMap(loadingMap, (shader) -> {
			if (shader.key().isShadow()) {
				return shadowRenderTargets == null;
			} else {
				return false;
			}
		}, loadedShaders::add);

		initializedBlockIds = false;

		WorldRenderingSettings.INSTANCE.setEntityIds(programSet.getPack().getIdMap().getEntityIdMap());
		WorldRenderingSettings.INSTANCE.setItemIds(programSet.getPack().getIdMap().getItemIdMap());
		WorldRenderingSettings.INSTANCE.setAmbientOcclusionLevel(programSet.getPackDirectives().getAmbientOcclusionLevel());
		WorldRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		WorldRenderingSettings.INSTANCE.setUseSeparateAo(programSet.getPackDirectives().shouldUseSeparateAo());
		WorldRenderingSettings.INSTANCE.setVoxelizeLightBlocks(programSet.getPackDirectives().shouldVoxelizeLightBlocks());
		WorldRenderingSettings.INSTANCE.setSeparateEntityDraws(programSet.getPackDirectives().shouldUseSeparateEntityDraws());

		if (shadowRenderTargets != null) {
			GlProgram shader = shaderMap.getShader(ShaderKey.SHADOW_TERRAIN_CUTOUT);
			boolean shadowUsesImages = false;

			if (shader instanceof ExtendedShader shader2) {
				shadowUsesImages = shader2.hasActiveImages();
			}

			this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
			this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
			this.shadowCompositeRenderer = new ShadowCompositeRenderer(this, programSet.getPackDirectives(), programSet.getComposite(ProgramArrayId.ShadowComposite), programSet.getCompute(ProgramArrayId.ShadowComposite), this.shadowRenderTargets, this.shaderStorageBufferHolder, customTextureManager.getNoiseTexture(), updateNotifier,
				customTextureManager.getCustomTextureIdMap(TextureStage.SHADOWCOMP), customImages, programSet.getPackDirectives().getExplicitFlips("shadowcomp_pre"), customTextureManager.getIrisCustomTextures(), customUniforms);

			if (programSet.getPackDirectives().getShadowDirectives().isShadowEnabled().orElse(true)) {
				this.shadowRenderer = new ShadowRenderer(this, resolver.resolveNullable(ProgramId.ShadowSolid),
					programSet.getPackDirectives(), shadowRenderTargets, shadowCompositeRenderer, customUniforms, programSet.getPack().hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
			} else {
				shadowRenderer = null;
			}

			defaultFBShadow = shadowRenderTargets.createFramebufferWritingToMain(new int[] {0});
		} else {
			this.shadowClearPasses = ImmutableList.of();
			this.shadowClearPassesFull = ImmutableList.of();
			this.shadowCompositeRenderer = null;
			this.shadowRenderer = null;
		}

		// TODO: Create fallback Sodium shaders if the pack doesn't provide terrain shaders
		//       Currently we use Sodium's shaders but they don't support EXP2 fog underwater.
		this.sodiumPrograms = new SodiumPrograms(this, programSet, resolver, renderTargets, shadowTargetsSupplier, customUniforms);

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
				public void process(GlTexture target) {

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
		int defaultTex = packDirectives.getFallbackTex();

		defaultFB = flippedAfterPrepare.contains(defaultTex) ? renderTargets.createFramebufferWritingToAlt(new int[] { defaultTex }) : renderTargets.createFramebufferWritingToMain(new int[] { defaultTex });
		defaultFBAlt = flippedAfterTranslucent.contains(defaultTex) ? renderTargets.createFramebufferWritingToAlt(new int[] { defaultTex }) : renderTargets.createFramebufferWritingToMain(new int[] { defaultTex });
	}

	private ComputeProgram[] createShadowComputes(ComputeSource[] compute, ProgramSet programSet) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || source.getSource().isEmpty()) {
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

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, false, this);
				IrisSamplers.addCustomTextures(builder, customTextureManager.getIrisCustomTextures());
				IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
				IrisImages.addRenderTargetImages(builder, flipped, renderTargets);
				IrisImages.addCustomImages(builder, customImages);

				IrisSamplers.addLevelSamplers(customTextureSamplerInterceptor, this, whitePixel, true, true, false);

				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, customTextureManager.getNoiseTexture());

				if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
					if (shadowRenderTargets != null) {
						IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets, null, separateHardwareSamplers);
						IrisImages.addShadowColorImages(builder, shadowRenderTargets, null);
					}
				}

				programs[i] = builder.buildCompute();

				this.customUniforms.mapholderToPass(builder, programs[i]);

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(), FilledIndirectPointer.basedOff(shaderStorageBufferHolder, source.getIndirectPointer()));
			}
		}


		return programs;
	}

	private ComputeProgram[] createSetupComputes(ComputeSource[] compute, ProgramSet programSet, TextureStage stage) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || source.getSource().isEmpty()) {
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

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, flipped, renderTargets, true, this);
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

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(), FilledIndirectPointer.basedOff(shaderStorageBufferHolder, source.getIndirectPointer()));
			}
		}


		return programs;
	}

	private ShaderSupplier createShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (source.isEmpty()) {
			return createFallbackShader(name, key);
		}

		return createShader(name, key, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(), key.getFogMode(),
			key.isIntensity(), key.shouldIgnoreLightmap(), key.isGlint(), key.isText(), key == ShaderKey.IE_COMPAT);
	}

	@Override
	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return customTextureMap;
	}

	private ShaderSupplier createShader(String name, ShaderKey key, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, FogMode fogMode,
										boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText, boolean isIE) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, source.getDirectives().getDrawBuffers());
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, source.getDirectives().getDrawBuffers());
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);


		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, isGlint, isText, isIE);

		Supplier<ImmutableSet<Integer>> flipped =
			() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;


		ShaderSupplier extendedShader = ShaderCreator.create(this, name, key, source, programId, beforeTranslucent, afterTranslucent,
			fallbackAlpha, vertexFormat, inputs, updateNotifier, this, flipped, fogMode, isIntensity, isFullbright, false, isLines, customUniforms);

		return extendedShader;
	}

	private ShaderSupplier createFallbackShader(String name, ShaderKey key) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, new int[]{0});
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, new int[]{0});

		ShaderSupplier shader = ShaderCreator.createFallback(name, key, beforeTranslucent, afterTranslucent,
			key.getAlphaTest(), key.getVertexFormat(), null, this, key.getFogMode(),
			key == ShaderKey.GLINT, key.isText(), key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		return shader;
	}

	private ShaderSupplier createShadowShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (source.isEmpty()) {
			return createFallbackShadowShader(name, key);
		}

		return createShadowShader(name, key, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(),
			key.isIntensity(), key.shouldIgnoreLightmap(), key.isText(), key == ShaderKey.IE_COMPAT_SHADOW);
	}

	private ShaderSupplier createFallbackShadowShader(String name, ShaderKey key) throws IOException {
		ShaderSupplier shader = ShaderCreator.createFallbackShadow(name, key, shadowTargetsSupplier,
			key.getAlphaTest(), key.getVertexFormat(), BlendModeOverride.OFF, this, key.getFogMode(),
			key == ShaderKey.GLINT, key.isText(), key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		return shader;
	}

	private ShaderSupplier createShadowShader(String name, ShaderKey key, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
											  VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText, boolean isIE) throws IOException {
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, false, isText, isIE);

		Supplier<ImmutableSet<Integer>> flipped = () -> flippedBeforeShadow;

		ShaderSupplier extendedShader = ShaderCreator.createShadow(this, name, key, source, programId, shadowTargetsSupplier,
			fallbackAlpha, vertexFormat, inputs, updateNotifier, this, flipped, FogMode.PER_VERTEX, isIntensity, isFullbright, true, isLines, customUniforms);

		return extendedShader;
	}

	public void addGbufferOrShadowSamplers(SamplerHolder samplers, ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
										   boolean isShadowPass, boolean hasTexture, boolean hasLightmap, boolean hasOverlay) {
		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor samplerHolder =
			ProgramSamplers.customTextureSamplerInterceptor(samplers,
				customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(samplerHolder, flipped, renderTargets, false, this);
		IrisSamplers.addCustomTextures(samplerHolder, customTextureManager.getIrisCustomTextures());
		IrisImages.addRenderTargetImages(images, flipped, renderTargets);
		IrisImages.addCustomImages(images, customImages);

		if (!shouldBindPBR) {
			shouldBindPBR = IrisSamplers.hasPBRSamplers(samplerHolder);
		}

		IrisSamplers.addLevelSamplers(samplers, this, whitePixel, hasTexture, hasLightmap, hasOverlay);
		IrisSamplers.addWorldDepthSamplers(samplerHolder, this.renderTargets);
		IrisSamplers.addNoiseSampler(samplerHolder, this.customTextureManager.getNoiseTexture());
		IrisSamplers.addCustomImages(samplerHolder, customImages);

		if (IrisSamplers.hasShadowSamplers(samplerHolder)) {
			IrisSamplers.addShadowSamplers(samplerHolder, shadowTargetsSupplier.get(), null, separateHardwareSamplers);
		}

		if (isShadowPass || IrisImages.hasShadowImages(images)) {
			IrisImages.addShadowColorImages(images, shadowTargetsSupplier.get(), null);
		}
	}

	private boolean shouldRemovePhase = false;

	@Override
	public WorldRenderingPhase getPhase() {
		if (shouldRemovePhase) {
			phase = WorldRenderingPhase.NONE;
			shouldRemovePhase = false;
			GLDebug.popGroup();
		}

		if (overridePhase != null) {
			return overridePhase;
		}

		return phase;
	}

	public void removePhaseIfNeeded() {
		if (shouldRemovePhase) {
			phase = WorldRenderingPhase.NONE;
			shouldRemovePhase = false;
			GLDebug.popGroup();
		}
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		if (phase == WorldRenderingPhase.NONE) {
			if (shouldRemovePhase) GLDebug.popGroup();
			shouldRemovePhase = true;
			return;
		} else {
			shouldRemovePhase = false;
			if (phase == this.phase) {
				return;
			}
		}

		GLDebug.popGroup();
		if (phase != WorldRenderingPhase.NONE && phase != WorldRenderingPhase.TERRAIN_CUTOUT && phase != WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED && phase != WorldRenderingPhase.TRIPWIRE) {
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				GLDebug.pushGroup(phase.ordinal(), "Shadow " + StringUtils.capitalize(phase.name().toLowerCase(Locale.ROOT).replace("_", " ")));
			} else {
				GLDebug.pushGroup(phase.ordinal(), StringUtils.capitalize(phase.name().toLowerCase(Locale.ROOT).replace("_", " ")));
			}
		}
		this.phase = phase;
	}

	@Override
	public void setOverridePhase(WorldRenderingPhase phase) {
		this.overridePhase = phase;
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
	public void onSetShaderTexture(GpuTextureView id) {
		if (shouldBindPBR && isRenderingWorld && id != null) {
			PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(id.texture().iris$getGlId());
			id.texture().iris$copyStateTo(pbrHolder.normalTexture().getTexture());
			id.texture().iris$copyStateTo(pbrHolder.specularTexture().getTexture());
			currentNormalTexture = pbrHolder.normalTexture().getTexture().iris$getGlId();
			currentSpecularTexture = pbrHolder.specularTexture().getTexture().iris$getGlId();

			TextureFormat textureFormat = TextureFormatLoader.getFormat();
			if (textureFormat != null) {
				int previousBinding = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
				textureFormat.setupTextureParameters(PBRType.NORMAL, pbrHolder.normalTexture());
				textureFormat.setupTextureParameters(PBRType.SPECULAR, pbrHolder.specularTexture());
				GlStateManager._bindTexture(previousBinding);
			}

			PBRTextureManager.notifyPBRTexturesChanged();
		}
	}

	@Override
	public void beginLevelRendering() {
		isRenderingWorld = true;

		if (!initializedBlockIds) {
			WorldRenderingSettings.INSTANCE.setBlockStateIds(
				BlockMaterialMapping.createBlockStateIdMap(pack.getIdMap().getBlockProperties(), pack.getIdMap().getTagEntries()));
			WorldRenderingSettings.INSTANCE.setBlockTypeIds(BlockMaterialMapping.createBlockTypeMap(pack.getIdMap().getBlockRenderTypeMap()));
			Minecraft.getInstance().levelRenderer.allChanged();
			initializedBlockIds = true;
		}

		// Make sure we're using texture unit 0 for this.
		GlStateManager._activeTexture(GL15C.GL_TEXTURE0);
		Vector4f emptyClearColor = new Vector4f(1.0F);

		GLDebug.pushGroup(100, "Clear textures");

		clearImages.forEach(ImageClearPass::execute);

		if (shadowRenderTargets != null) {
			if (packDirectives.getShadowDirectives().isShadowEnabled() == OptionalBoolean.FALSE) {
				if (shadowRenderTargets.isFullClearRequired()) {
					this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
					this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
					shadowRenderTargets.onFullClear();
					for (ClearPass clearPass : shadowClearPassesFull) {
						clearPass.execute(emptyClearColor);
					}
				}
			} else {
				// Clear depth first, regardless of any color clearing.
				shadowRenderTargets.getDepthSourceFb().bind();
				GlStateManager._depthMask(true);
				GlStateManager._clear(GL21C.GL_DEPTH_BUFFER_BIT);

				ImmutableList<ClearPass> passes;

				for (ComputeProgram computeProgram : shadowComputes) {
					if (computeProgram != null) {
						computeProgram.use();
						customUniforms.push(computeProgram);
						computeProgram.dispatch(shadowMapResolution, shadowMapResolution);
					}
				}

				if (shadowRenderTargets.isFullClearRequired()) {
					this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
					this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
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

		GpuTexture depthTexture = main.getDepthTexture();
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(GlConst.toGlInternalId(main.getDepthTexture().getFormat()));

		boolean changed = renderTargets.resizeIfNeeded(((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthTexture, main.width,
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

		GLDebug.popGroup();

		// Make sure to switch back to the main framebuffer. If we forget to do this then our alt buffers might be
		// cleared to the fog color, which absolutely is not what we want!
		//
		// If we forget to do this, then weird lines appear at the top of the screen and the right of the screen
		// on Sildur's Vibrant Shaders.
		Minecraft.getInstance().getMainRenderTarget().iris$bindFramebuffer();
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

		beginRenderer.renderAll();

		isBeforeTranslucent = true;
	}

	@Override
	public void renderShadows(LevelRendererAccessor worldRenderer, Camera playerCamera, CameraRenderState renderState) {
		if (shadowRenderer != null) {
			this.shadowRenderer.renderShadows(worldRenderer, playerCamera, renderState);
		}

		prepareRenderer.renderAll();
	}

	@Override
	public void addDebugText(DebugScreenDisplayer messages) {
		if (this.shadowRenderer != null) {
			shadowRenderer.addDebugText(messages);
		} else {
			messages.addLine("[Iris] Shadow Maps: not used by shader pack");
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

		removePhaseIfNeeded();

		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 can contain the depth values for
		// all non-translucent content, as required.
		renderTargets.copyPreTranslucentDepth();

		deferredRenderer.renderAll();

		// note: we are careful not to touch the lightmap texture unit or overlay color texture unit here,
		// so we don't need to do anything to restore them if needed.
		//
		// Previous versions of the code tried to "restore" things by enabling the lightmap & overlay color
		// but that actually broke rendering of clouds and rain by making them appear red in the case of
		// a pack not overriding those shader programs.
		//
		// Not good!

		// Reset shader or whatever...
	}

	@Override
	public void finalizeLevelRendering() {
		isRenderingWorld = false;
		removePhaseIfNeeded();
		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
	}

	@Override
	public void finalizeGameRendering() {
		colorSpaceConverter.process((GlTexture) Minecraft.getInstance().getMainRenderTarget().getColorTexture());
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
	public boolean shouldRenderWeather() {
		return shouldRenderWeather;
	}

	@Override
	public boolean shouldRenderWeatherParticles() {
		return shouldRenderWeatherParticles;
	}

	@Override
	public boolean shouldRenderMoon() {
		return shouldRenderMoon;
	}

	@Override
	public boolean shouldRenderStars() {
		return shouldRenderStars;
	}

	@Override
	public boolean shouldRenderSkyDisc() {
		return shouldRenderSkyDisc;
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
	public boolean shouldDisableOcclusionCulling() {
		return !occlusionCulling;
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
			GlStateManager._activeTexture(GL20C.GL_TEXTURE0 + i);
			IrisRenderSystem.unbindAllSamplers();
			GlStateManager._bindTexture(0);
		}

		// Set the active texture unit to unit 0
		//
		// This seems to be what most code expects. It's a sane default in any case.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		for (int i = 0; i < 12; i++) {
			// Clear all shader textures
			RenderSystem.setShaderTexture(i, null);
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

		renderTargets.destroy();
		dhCompat.clearPipeline();

		clearImages.forEach(ImageClearPass::destroy);
		customImages.forEach(GlImage::destroy);

		if (shadowRenderTargets != null) {
			shadowRenderTargets.destroy();
		}

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
	public SodiumPrograms getSodiumPrograms() {
		return sodiumPrograms;
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

	public AbstractTexture getWhitePixel() {
		return whitePixel;
	}

	@Override
	public void setIsMainBound(boolean bound) {
		isMainBound = bound;
	}

	@Override
	public void onBeginClear() {
		setPhase(WorldRenderingPhase.SKY);

		// Render our horizon box before actual sky rendering to avoid being broken by mods that do weird things
		// while rendering the sky.
		//
		// A lot of dimension mods touch sky rendering, FabricSkyboxes injects at HEAD and cancels, etc.
		DimensionSpecialEffects.SkyType skyType = Minecraft.getInstance().level.effects().skyType();

		if (shouldRenderSkyDisc && (skyType == DimensionSpecialEffects.SkyType.OVERWORLD || Minecraft.getInstance().level.dimensionType().hasSkyLight())) {
			Vector3d fogColor3 = CapturedRenderingState.INSTANCE.getFogColor();

			// NB: The alpha value must be 1.0 here, or else you will get a bunch of bugs. Sildur's Vibrant Shaders
			//     will give you pink reflections and other weirdness if this is zero.
			Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);

			horizonRenderer.renderHorizon(CapturedRenderingState.INSTANCE.getGbufferModelView(), CapturedRenderingState.INSTANCE.getGbufferProjection(), fogColor);
		}
	}

	@Override
	public boolean supportsEndFlash() {
		return supportsEndFlash;
	}

	public Optional<ProgramSource> getDHTerrainShader() {
		return resolver.resolve(ProgramId.DhTerrain);
	}

	public Optional<ProgramSource> getDHGenericShader() {
		return resolver.resolve(ProgramId.DhGeneric);
	}

	public Optional<ProgramSource> getDHWaterShader() {
		return resolver.resolve(ProgramId.DhWater);
	}

	public Optional<ProgramSource> getDHShadowShader() {
		return resolver.resolve(ProgramId.DhShadow);
	}

	public CustomUniforms getCustomUniforms() {
		return customUniforms;
	}

	public GlFramebuffer createDHFramebuffer(ProgramSource sources, boolean trans) {
		return renderTargets.createDHFramebuffer(trans ? flippedAfterTranslucent : flippedAfterPrepare,
			sources.getDirectives().getDrawBuffers());
	}

	public ImmutableSet<Integer> getFlippedBeforeShadow() {
		return flippedBeforeShadow;
	}

	public ImmutableSet<Integer> getFlippedAfterPrepare() {
		return flippedAfterPrepare;
	}

	public ImmutableSet<Integer> getFlippedAfterTranslucent() {
		return flippedAfterTranslucent;
	}

	public GlFramebuffer createDHFramebufferShadow(ProgramSource sources) {

		return shadowRenderTargets.createDHFramebuffer(ImmutableSet.of(), new int[]{0, 1});
	}

	public boolean hasShadowRenderTargets() {
		return shadowRenderTargets != null;
	}

	public boolean skipAllRendering() {
		return skipAllRendering;
	}

	public CloudSetting getDHCloudSetting() {
		return dhCloudSetting;
	}

	public void bindDefault() {
		if (isBeforeTranslucent) {
			defaultFB.bind();
		} else {
			defaultFBAlt.bind();
		}
	}

	public void bindDefaultShadow() {
		defaultFBShadow.bind();
	}
}
