package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.helpers.FakeChainedJsonException;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pathways.HorizonRenderer;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.irisshaders.iris.pathways.colorspace.ColorSpaceConverter;
import net.irisshaders.iris.pathways.colorspace.ColorSpaceFragmentConverter;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.format.TextureFormat;
import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.texture.PBRTextureHolder;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.irisshaders.iris.pipeline.programs.ComputeProgram;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.ShaderCreator;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderLoadingMap;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import net.irisshaders.iris.pipeline.programs.ShaderSupplier;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.CustomTextureSamplerInterceptor;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
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
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.targets.backed.NativeImageBackedSingleColorTexture;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FogUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.VanillaUniforms;
import net.irisshaders.iris.uniforms.buffer.FrameUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.sodium.terrain.FormatAnalyzer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

public class IrisRenderingPipeline implements WorldRenderingPipeline, ShaderRenderingPipeline {
	private final RenderTargets renderTargets;
	private final PackSamplers packSamplers;
	private final ShaderMap shaderMap;
	private final WorldRenderTargets worldTargets;
	private final CustomUniforms customUniforms;
	private final FrameUniforms frameUniforms;
	private final ShadowCompositeRenderer shadowCompositeRenderer;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> customTextureMap;
	private final ComputeProgram[] setup;
	private final boolean separateHardwareSamplers;
	private final ProgramFallbackResolver resolver;
	private final Supplier<ShadowRenderTargets> shadowTargetsSupplier;
	private final Set<ExtendedShader> loadedShaders;
	private final CompositeRenderer beginRenderer;
	private final CompositeRenderer prepareRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final CustomTextureManager customTextureManager;
	private final DynamicTexture whitePixel;
	private final DynamicTexture biggerWhitePixel;
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
	private final Set<CustomImage> customImages;
	private final ImmutableList<CustomImage> clearImages;
	private final ShaderPack pack;
	private final PackShadowDirectives shadowDirectives;
	private final DHCompat dhCompat;
	private final int stackSize = 0;
	private final boolean skipAllRendering;
	private final CloudSetting dhCloudSetting;
	private final boolean supportsEndFlash;
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
	private AbstractTexture currentNormalTexture;
	private GpuSampler normalNativeSampler, specularNativeSampler;
	private AbstractTexture currentSpecularTexture;
	private ColorSpace currentColorSpace;
	private GlSampler normalSampler = GlSampler.MIPPED_NEAREST, specularSampler = GlSampler.MIPPED_NEAREST;
	private int albedoTex;
	private boolean shouldRemovePhase = false;

	public IrisRenderingPipeline(ProgramSet programSet) {
		var cleanup = new ArrayList<Runnable>();
		cleanup.add(this.horizonRenderer::destroy);

		try {
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
			this.shadowDirectives = this.packDirectives.getShadowDirectives();
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
			WorldRenderingSettings.INSTANCE.setVertexFormat(FormatAnalyzer.createFormat(true, true, true, true));

			RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
			GpuTexture depthTexture = main.getDepthTexture();
			DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromMojang(depthTexture.getFormat());

			if (!this.pack.getBufferObjects().isEmpty()) {
				if (IrisRenderSystem.supportsSSBO()) {
					this.shaderStorageBufferHolder = new ShaderStorageBufferHolder(this.pack.getBufferObjects(), main.width, main.height);

					cleanup.add(this.shaderStorageBufferHolder::close);
				} else {
					throw new IllegalStateException("shader storage buffers are not supported");
				}
			}

			this.customImages = new LinkedHashSet<>();
			cleanup.add(() -> this.customImages.forEach(CustomImage::destroy));

			for (ImageInformation information : programSet.getPack().getIrisCustomImages()) {
				if (information.isRelative()) {
					this.customImages.add(new CustomImage.Relative(information.name(), information.samplerName(), information.format(),
						information.internalTextureFormat(), information.type(), information.clear(), information.relativeWidth(),
						information.relativeHeight(), main.width, main.height));
				} else {
					this.customImages.add(
						new CustomImage(information.name(), information.samplerName(), information.target(), information.format(),
							information.internalTextureFormat(), information.type(), information.clear(), information.width(),
							information.height(), information.depth()));
				}
			}

			this.clearImages = this.customImages.stream().filter(CustomImage::shouldClear).collect(ImmutableList.toImmutableList());

			if (programSet.getPackDirectives().getParticleRenderingSettings() != ParticleRenderingSettings.UNSET) {
				this.particleRenderingSettings = programSet.getPackDirectives().getParticleRenderingSettings();
			} else if (programSet.getComposite(ProgramArrayId.Deferred).length > 0 && !programSet.getPackDirectives()
				.shouldUseSeparateEntityDraws()) {
				this.particleRenderingSettings = ParticleRenderingSettings.AFTER;
			} else {
				this.particleRenderingSettings = ParticleRenderingSettings.MIXED;
			}

			this.renderTargets = new RenderTargets(main.width, main.height, depthTexture,
				((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthBufferFormat,
				programSet.getPackDirectives().getRenderTargetDirectives().getRenderTargetSettings(), programSet.getPackDirectives());
			cleanup.add(this.renderTargets::destroy);
			this.packSamplers = new PackSamplers();
			cleanup.add(this.packSamplers::close);
			this.sunPathRotation = programSet.getPackDirectives().getSunPathRotation();

			PackShadowDirectives shadowDirectives = programSet.getPackDirectives().getShadowDirectives();

			if (shadowDirectives.isDistanceRenderMulExplicit()) {
				if (shadowDirectives.getDistanceRenderMul() >= 0.0) {

					this.forcedShadowRenderDistanceChunks = OptionalInt.of(
						((int) (shadowDirectives.getDistance() * shadowDirectives.getDistanceRenderMul()) + 15) / 16);
				} else {
					this.forcedShadowRenderDistanceChunks = OptionalInt.of(-1);
				}
			} else {
				this.forcedShadowRenderDistanceChunks = OptionalInt.empty();
			}

			this.customUniforms = programSet.getPack().customUniforms.build(holder -> {
				CommonUniforms.addNonDynamicUniforms(holder, programSet.getPack().getIdMap(), programSet.getPackDirectives(),
					this.updateNotifier);
				BuiltinReplacementUniforms.addBuiltinReplacementUniforms(holder);
				FogUniforms.addFogColor(holder);
				VanillaUniforms.addVanillaUniforms(holder);
			});

			this.frameUniforms = new FrameUniforms(this.customUniforms);
			cleanup.add(this.frameUniforms::close);

			this.customTextureManager = new CustomTextureManager(programSet.getPackDirectives(),
				programSet.getPack().getCustomTextureDataMap(), programSet.getPack().getIrisCustomTextureDataMap(),
				programSet.getPack().getCustomNoiseTexture());
			cleanup.add(this.customTextureManager::destroy);
			this.whitePixel = new NativeImageBackedSingleColorTexture(255, 255, 255, 255);
			cleanup.add(this.whitePixel::close);
			this.biggerWhitePixel = new NativeImageBackedSingleColorTexture(32, 32, 255, 255, 255, 255);
			cleanup.add(this.biggerWhitePixel::close);

			BufferFlipper flipper = new BufferFlipper();
			this.centerDepthSampler = new CenterDepthSampler(this.renderTargets::getDepthTexture,
				programSet.getPackDirectives().getCenterDepthHalfLife());
			cleanup.add(this.centerDepthSampler::destroy);

			this.shadowMapResolution = programSet.getPackDirectives().getShadowDirectives().getResolution();

			this.shadowTargetsSupplier = () -> {
				if (this.shadowRenderTargets == null) {

					this.shadowRenderTargets = new ShadowRenderTargets(this, this.shadowMapResolution, shadowDirectives);
					cleanup.add(this.shadowRenderTargets::destroy);
				}

				return this.shadowRenderTargets;
			};

			if (shadowDirectives.isShadowEnabled() == OptionalBoolean.TRUE) {
				this.shadowTargetsSupplier.get();
			}

			this.shadowComputes = this.createShadowComputes(programSet.getShadowCompute(), programSet);
			cleanup.add(() -> destroyComputes(this.shadowComputes));

			this.beginRenderer = new CompositeRenderer(this, CompositePass.BEGIN, programSet.getPackDirectives(),
				programSet.getComposite(ProgramArrayId.Begin), programSet.getCompute(ProgramArrayId.Begin), this.renderTargets,
				this.shaderStorageBufferHolder, this.customTextureManager.getNoiseTexture(), this.updateNotifier, this.centerDepthSampler,
				flipper, this.shadowTargetsSupplier, TextureStage.BEGIN,
				this.customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.BEGIN, Object2ObjectMaps.emptyMap()),
				this.customTextureManager.getIrisCustomTextures(), this.customImages,
				programSet.getPackDirectives().getExplicitFlips("begin_pre"), this.customUniforms);
			cleanup.add(this.beginRenderer::destroy);

			this.flippedBeforeShadow = flipper.snapshot();

			this.prepareRenderer = new CompositeRenderer(this, CompositePass.PREPARE, programSet.getPackDirectives(),
				programSet.getComposite(ProgramArrayId.Prepare), programSet.getCompute(ProgramArrayId.Prepare), this.renderTargets,
				this.shaderStorageBufferHolder, this.customTextureManager.getNoiseTexture(), this.updateNotifier, this.centerDepthSampler,
				flipper, this.shadowTargetsSupplier, TextureStage.PREPARE,
				this.customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()),
				this.customTextureManager.getIrisCustomTextures(), this.customImages,
				programSet.getPackDirectives().getExplicitFlips("prepare_pre"), this.customUniforms);
			cleanup.add(this.prepareRenderer::destroy);

			this.flippedAfterPrepare = flipper.snapshot();

			this.deferredRenderer = new CompositeRenderer(this, CompositePass.DEFERRED, programSet.getPackDirectives(),
				programSet.getComposite(ProgramArrayId.Deferred), programSet.getCompute(ProgramArrayId.Deferred), this.renderTargets,
				this.shaderStorageBufferHolder, this.customTextureManager.getNoiseTexture(), this.updateNotifier, this.centerDepthSampler,
				flipper, this.shadowTargetsSupplier, TextureStage.DEFERRED,
				this.customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
				this.customTextureManager.getIrisCustomTextures(), this.customImages,
				programSet.getPackDirectives().getExplicitFlips("deferred_pre"), this.customUniforms);
			cleanup.add(this.deferredRenderer::destroy);

			this.flippedAfterTranslucent = flipper.snapshot();

			this.compositeRenderer = new CompositeRenderer(this, CompositePass.COMPOSITE, programSet.getPackDirectives(),
				programSet.getComposite(ProgramArrayId.Composite), programSet.getCompute(ProgramArrayId.Composite), this.renderTargets,
				this.shaderStorageBufferHolder, this.customTextureManager.getNoiseTexture(), this.updateNotifier, this.centerDepthSampler,
				flipper, this.shadowTargetsSupplier, TextureStage.COMPOSITE_AND_FINAL, this.customTextureManager.getCustomTextureIdMap()
				.getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				this.customTextureManager.getIrisCustomTextures(), this.customImages,
				programSet.getPackDirectives().getExplicitFlips("composite_pre"), this.customUniforms);
			cleanup.add(this.compositeRenderer::destroy);
			this.finalPassRenderer = new FinalPassRenderer(this, programSet, this.renderTargets,
				this.customTextureManager.getNoiseTexture(), this.shaderStorageBufferHolder, this.updateNotifier, flipper.snapshot(),
				this.centerDepthSampler, this.shadowTargetsSupplier, this.customTextureManager.getCustomTextureIdMap()
				.getOrDefault(TextureStage.COMPOSITE_AND_FINAL, Object2ObjectMaps.emptyMap()),
				this.customTextureManager.getIrisCustomTextures(), this.customImages, this.compositeRenderer.getFlippedAtLeastOnceFinal(),
				this.customUniforms);
			cleanup.add(this.finalPassRenderer::destroy);

			this.dhCompat = new DHCompat(this, shadowDirectives.isDhShadowEnabled().orElse(true));
			cleanup.add(this.dhCompat::clearPipeline);

			this.loadedShaders = new HashSet<>();
			cleanup.add(this::destroyShaders);
			this.worldTargets = new WorldRenderTargets(this.renderTargets, this.shadowTargetsSupplier, this.flippedAfterPrepare,
				this.flippedAfterTranslucent);

			ShaderLoadingMap loadingMap = new ShaderLoadingMap((key, patchType) -> {
				try {
					if (key.isShadow()) {
						return this.createShadowShader(key.getName(), this.resolver.resolve(key.getProgram()), key, patchType);
					} else {
						return this.createShader(key.getName(), this.resolver.resolve(key.getProgram()), key, patchType);
					}
				} catch (FakeChainedJsonException e) {
					this.destroyShaders();

					throw e.getTrueException();
				} catch (IOException e) {
					this.destroyShaders();

					throw new RuntimeException(e);
				} catch (RuntimeException e) {
					this.destroyShaders();

					throw e;
				}
			});
			this.shaderMap = new ShaderMap(loadingMap, (shader) -> {
				if (shader.key().isShadow()) {
					return this.shadowRenderTargets == null;
				} else {
					return false;
				}
			}, this.loadedShaders::add);

			this.initializedBlockIds = false;

			WorldRenderingSettings.INSTANCE.setEntityIds(programSet.getPack().getIdMap().getEntityIdMap());
			WorldRenderingSettings.INSTANCE.setItemIds(programSet.getPack().getIdMap().getItemIdMap());
			WorldRenderingSettings.INSTANCE.setAmbientOcclusionLevel(programSet.getPackDirectives().getAmbientOcclusionLevel());
			WorldRenderingSettings.INSTANCE.setDisableDirectionalShading(this.shouldDisableDirectionalShading());
			WorldRenderingSettings.INSTANCE.setUseSeparateAo(programSet.getPackDirectives().shouldUseSeparateAo());
			WorldRenderingSettings.INSTANCE.setBreaksAnisotropy(programSet.getPackDirectives().breaksAnisotropy());
			WorldRenderingSettings.INSTANCE.setVoxelizeLightBlocks(programSet.getPackDirectives().shouldVoxelizeLightBlocks());
			WorldRenderingSettings.INSTANCE.setSeparateEntityDraws(programSet.getPackDirectives().shouldUseSeparateEntityDraws());

			if (this.shadowRenderTargets != null) {
				boolean shadowUsesImages = false;
				this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(this.shadowRenderTargets, false, shadowDirectives);
				this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(this.shadowRenderTargets, true, shadowDirectives);
				this.shadowCompositeRenderer = new ShadowCompositeRenderer(this, programSet.getPackDirectives(),
					programSet.getComposite(ProgramArrayId.ShadowComposite), programSet.getCompute(ProgramArrayId.ShadowComposite),
					this.shadowRenderTargets, this.shaderStorageBufferHolder, this.customTextureManager.getNoiseTexture(),
					this.updateNotifier, this.customTextureManager.getCustomTextureIdMap(TextureStage.SHADOWCOMP), this.customImages,
					programSet.getPackDirectives().getExplicitFlips("shadowcomp_pre"), this.customTextureManager.getIrisCustomTextures(),
					this.customUniforms);

				if (programSet.getPackDirectives().getShadowDirectives().isShadowEnabled().orElse(true)) {
					this.shadowRenderer = new ShadowRenderer(this, this.resolver.resolveNullable(ProgramId.ShadowSolid),
						programSet.getPackDirectives(), this.shadowRenderTargets, this.shadowCompositeRenderer, this.customUniforms,
						programSet.getPack().hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
				} else {
					this.shadowRenderer = null;
				}

			} else {
				this.shadowClearPasses = ImmutableList.of();
				this.shadowClearPassesFull = ImmutableList.of();
				this.shadowCompositeRenderer = null;
				this.shadowRenderer = null;
			}

			this.setup = this.createSetupComputes(programSet.getSetup(), programSet, TextureStage.SETUP);
			cleanup.add(() -> destroyComputes(this.setup));

			this.customUniforms.optimise();

			boolean hasRun = false;
			this.clearPassesFull = ClearPassCreator.createClearPasses(this.renderTargets, true,
				programSet.getPackDirectives().getRenderTargetDirectives());
			this.clearPasses = ClearPassCreator.createClearPasses(this.renderTargets, false,
				programSet.getPackDirectives().getRenderTargetDirectives());

			for (ComputeProgram program : this.setup) {
				if (program != null) {
					program.prepare();
				}
			}

			for (ComputeProgram program : this.setup) {
				if (program != null) {
					if (!hasRun) {
						hasRun = true;
						this.renderTargets.onFullClear();

						Vector3d fogColor3 = CapturedRenderingState.INSTANCE.getFogColor();

						Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);
						ClearPass.executeAll(this.clearPassesFull, fogColor);
					}

					program.dispatch(1, 1);
				}
			}

			if (hasRun) {
			}

			if (programSet.getPackDirectives().supportsColorCorrection()) {
				this.colorSpaceConverter = new ColorSpaceConverter() {
					@Override
					public void rebuildProgram(int width, int height, ColorSpace colorSpace) {
					}

					@Override
					public void process(GpuTextureView target) {
					}
				};
			} else {

				this.colorSpaceConverter = new ColorSpaceFragmentConverter(main.width, main.height, IrisVideoSettings.colorSpace);
			}

			cleanup.add(this.colorSpaceConverter::close);
			this.currentColorSpace = IrisVideoSettings.colorSpace;

			{
				for (ComputeProgram program : this.shadowComputes) {
					if (program != null) {
						program.prepare();
					}
				}
				this.loadedShaders.forEach(ExtendedShader::prepare);
				IrisPipelines.preparePrograms(this, this.shaderMap);
				this.beginRenderer.prepare();
				this.prepareRenderer.prepare();
				this.deferredRenderer.prepare();
				this.compositeRenderer.prepare();
				if (this.shadowCompositeRenderer != null) {
					this.shadowCompositeRenderer.prepare();
				}
				this.finalPassRenderer.prepare();
				this.colorSpaceConverter.prepare(main.getColorTextureView());
				this.dhCompat.prepare();
				Iris.getRenderBackend().finishCompilation();
			}

			cleanup.clear();
		} catch (RuntimeException | Error failure) {
			try {
				Iris.getRenderBackend().finishCompilation();
			} catch (RuntimeException | Error suppressed) {
				if (suppressed != failure) {
					failure.addSuppressed(suppressed);
				}
			}

			for (var close : cleanup.reversed()) {
				try {
					close.run();
				} catch (RuntimeException | Error suppressed) {
					failure.addSuppressed(suppressed);
				}
			}

			throw failure;
		}
	}

	private static void destroyComputes(ComputeProgram[] programs) {
		for (var program : programs) {
			if (program != null) {
				program.close();
			}
		}
	}

	public GpuSampler normalNativeSampler() {
		return this.normalNativeSampler;
	}

	public GpuSampler specularNativeSampler() {
		return this.specularNativeSampler;
	}

	private ComputeProgram[] createShadowComputes(ComputeSource[] compute, ProgramSet programSet) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || source.getSource().isEmpty()) {
			} else {
				ProgramResources builder;
				ComputeProgram program;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null),
						TextureStage.GBUFFERS_AND_SHADOW, customTextureMap, getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));

					program = new ComputeProgram(this.packSamplers, source.getName(), transformed);
					builder = program.getSamplers();
					builder.addStorageBuffers(this.shaderStorageBufferHolder);
				} catch (ShaderCompileException e) {
					throw e;
				} catch (RuntimeException e) {

					throw new RuntimeException("Shader compilation failed for compute " + source.getName() + "!", e);
				}

				program.getUniforms().useFrameUniforms(this.frameUniforms);
				CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

				Supplier<ImmutableSet<Integer>> flipped;

				flipped = () -> flippedBeforeShadow;

				TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

				CustomTextureSamplerInterceptor customTextureSamplerInterceptor = new CustomTextureSamplerInterceptor(builder,
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
						IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets, null,
							separateHardwareSamplers);
						IrisImages.addShadowColorImages(builder, shadowRenderTargets, null);
					}
				}

				builder.finish();
				programs[i] = program;

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(),
					FilledIndirectPointer.basedOff(shaderStorageBufferHolder, source.getIndirectPointer()));
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
				ProgramResources builder;
				ComputeProgram program;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), stage,
						customTextureMap, getTextureOverrides(stage));

					program = new ComputeProgram(this.packSamplers, source.getName(), transformed);
					builder = program.getSamplers();
					builder.addStorageBuffers(this.shaderStorageBufferHolder);
				} catch (RuntimeException e) {

					throw new RuntimeException("Shader compilation failed for setup compute " + source.getName() + "!", e);
				}

				program.getUniforms().useFrameUniforms(this.frameUniforms);
				CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

				ImmutableSet<Integer> empty = ImmutableSet.of();
				Supplier<ImmutableSet<Integer>> flipped;

				flipped = () -> empty;

				TextureStage textureStage = TextureStage.SETUP;

				CustomTextureSamplerInterceptor customTextureSamplerInterceptor = new CustomTextureSamplerInterceptor(builder,
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
						IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowRenderTargets, null,
							separateHardwareSamplers);
						IrisImages.addShadowColorImages(builder, shadowRenderTargets, null);
					}
				}

				builder.finish();
				programs[i] = program;

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(),
					FilledIndirectPointer.basedOff(shaderStorageBufferHolder, source.getIndirectPointer()));
			}
		}

		return programs;
	}

	private ShaderSupplier createShader(String name, Optional<ProgramSource> source, ShaderKey key, Patch patch) throws IOException {
		if (source.isEmpty()) {
			return createFallbackShader(name, key);
		}

		return createShader(name, key, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(), key.getFogMode(),
			key.isIntensity(), key.shouldIgnoreLightmap(), key.isGlint(), key.isText(), false, patch);
	}

	@Override
	public Set<String> getTextureOverrides(TextureStage stage) {
		return customTextureManager.getCustomTextureIdMap(stage).keySet();
	}

	@Override
	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return customTextureMap;
	}

	private ShaderSupplier createShader(String name, ShaderKey key, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
		VertexFormat vertexFormat, FogMode fogMode, boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText,
		boolean isIE, Patch patch) throws IOException {
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, isGlint, isText, isIE);

		Supplier<ImmutableSet<Integer>> flipped = () -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		ShaderSupplier extendedShader = ShaderCreator.create(this, name, key, source, programId, fallbackAlpha, vertexFormat, inputs,
			this.updateNotifier, this, flipped, fogMode, isIntensity, isFullbright, false, isLines, this.customUniforms, patch);

		return extendedShader;
	}

	private ShaderSupplier createFallbackShader(String name, ShaderKey key) throws IOException {

		ShaderSupplier shader = ShaderCreator.createFallback(name, key, key.getAlphaTest(), key.getVertexFormat(), null, this,
			key.getFogMode(), key.hasDiffuseLighting(), key == ShaderKey.GLINT, key.isText(), key.isIntensity(),
			key.shouldIgnoreLightmap());

		return shader;
	}

	private ShaderSupplier createShadowShader(String name, Optional<ProgramSource> source, ShaderKey key,
		Patch patchType) throws IOException {
		if (source.isEmpty()) {
			return createFallbackShadowShader(name, key);
		}

		return createShadowShader(name, key, source.get(), key.getProgram(), key.getAlphaTest(), key.getVertexFormat(), key.isIntensity(),
			key.shouldIgnoreLightmap(), key.isText(), false, patchType);
	}

	private ShaderSupplier createFallbackShadowShader(String name, ShaderKey key) throws IOException {
		ShaderSupplier shader = ShaderCreator.createFallbackShadow(name, key, shadowTargetsSupplier, key.getAlphaTest(),
			key.getVertexFormat(), BlendModeOverride.OFF, this, key.getFogMode(), key.hasDiffuseLighting(), key == ShaderKey.GLINT,
			key.isText(), key.isIntensity(), key.shouldIgnoreLightmap());

		return shader;
	}

	private ShaderSupplier createShadowShader(String name, ShaderKey key, ProgramSource source, ProgramId programId,
		AlphaTest fallbackAlpha, VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText, boolean isIE,
		Patch patchType) throws IOException {
		boolean isLines = programId == ProgramId.Line && resolver.has(ProgramId.Line);

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, isLines, false, isText, isIE);

		Supplier<ImmutableSet<Integer>> flipped = () -> flippedBeforeShadow;

		ShaderSupplier extendedShader = ShaderCreator.createShadow(this, name, key, source, programId, shadowTargetsSupplier, fallbackAlpha,
			vertexFormat, inputs, updateNotifier, this, flipped, FogMode.PER_VERTEX, isIntensity, isFullbright, true, isLines,
			customUniforms, patchType);

		return extendedShader;
	}

	public void addGbufferOrShadowSamplers(SamplerHolder samplers, ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
		boolean isShadowPass, boolean hasTexture, boolean hasLightmap, boolean hasOverlay) {
		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		CustomTextureSamplerInterceptor samplerHolder = new CustomTextureSamplerInterceptor(samplers,
			customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(samplerHolder, flipped, renderTargets, false, this);
		IrisSamplers.addCustomTextures(samplerHolder, customTextureManager.getIrisCustomTextures());
		IrisImages.addRenderTargetImages(images, flipped, renderTargets);
		IrisImages.addCustomImages(images, customImages);

		if (!shouldBindPBR) {
			shouldBindPBR = IrisSamplers.hasPBRSamplers(samplerHolder);
		}

		IrisSamplers.addLevelSamplers(samplerHolder, this, this.whitePixel, hasTexture, hasLightmap, hasOverlay);
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

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		if (phase == WorldRenderingPhase.NONE) {
			if (this.shouldRemovePhase) {
				GLDebug.popGroup();
			}

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
				GLDebug.pushGroup(phase.ordinal(),
					"Shadow " + StringUtils.capitalize(phase.name().toLowerCase(Locale.ROOT).replace("_", " ")));
			} else {
				GLDebug.pushGroup(phase.ordinal(), StringUtils.capitalize(phase.name().toLowerCase(Locale.ROOT).replace("_", " ")));
			}
		}
		this.phase = phase;
		GbufferPrograms.runPhaseChangeNotifier();
	}

	public void removePhaseIfNeeded() {
		if (shouldRemovePhase) {
			phase = WorldRenderingPhase.NONE;
			shouldRemovePhase = false;
			GLDebug.popGroup();
		}
	}

	@Override
	public void setOverridePhase(WorldRenderingPhase phase) {
		this.overridePhase = phase;
		GbufferPrograms.runPhaseChangeNotifier();
	}

	@Override
	public int getCurrentNormalTexture() {
		return this.currentNormalTexture == null ? 0 : TextureTracker.INSTANCE.identity(this.currentNormalTexture.getTexture());
	}

	public GlSampler getNormalSampler() {
		return normalSampler;
	}

	public GlSampler getSpecularSampler() {
		return specularSampler;
	}

	@Override
	public int getCurrentSpecularTexture() {
		return this.currentSpecularTexture == null ? 0 : TextureTracker.INSTANCE.identity(this.currentSpecularTexture.getTexture());
	}

	@Override
	public void onSetAlbedoTex(GpuTextureView id) {
		if (id != null) {
			TextureTracker.INSTANCE.bindTexture(id.texture());
			this.albedoTex = TextureTracker.INSTANCE.identity(id.texture());
			int maxAnisotropy = Minecraft.getInstance().options.textureFiltering()
				.get() == TextureFilteringMethod.ANISOTROPIC ? Minecraft.getInstance().options.maxAnisotropyValue() : 1;
			if (shouldBindPBR && isRenderingWorld) {
				PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(id.texture());
				currentNormalTexture = pbrHolder.normalTexture();
				currentSpecularTexture = pbrHolder.specularTexture();

				TextureFormat textureFormat = TextureFormatLoader.getFormat();
				this.normalSampler = this.specularSampler = GlSampler.MIPPED_NEAREST_NEAREST;
				this.normalNativeSampler = textureFormat == null || textureFormat.canInterpolateValues(
					PBRType.NORMAL) ? IrisSamplers.getTerrainCache(maxAnisotropy) : null;
				this.specularNativeSampler = textureFormat == null || textureFormat.canInterpolateValues(
					PBRType.SPECULAR) ? IrisSamplers.getTerrainCache(maxAnisotropy) : null;

				PBRTextureManager.notifyPBRTexturesChanged();
			}
		}
	}

	@Override
	public void beginLevelRendering() {

		isRenderingWorld = true;

		if (!initializedBlockIds) {
			WorldRenderingSettings.INSTANCE.setBlockStateIds(
				BlockMaterialMapping.createBlockStateIdMap(pack.getIdMap().getBlockProperties(), pack.getIdMap().getTagEntries()));
			WorldRenderingSettings.INSTANCE.setBlockTypeIds(
				BlockMaterialMapping.createBlockTypeMap(pack.getIdMap().getBlockRenderTypeMap()));
			Minecraft.getInstance().levelExtractor.allChanged();
			initializedBlockIds = true;
		}

		PBRTextureManager.INSTANCE.onNewFrame();

		this.updateNotifier.onNewFrame();

		this.customUniforms.update();
		this.frameUniforms.beginFrame();
		this.packSamplers.beginFrame();
		Vector4f emptyClearColor = new Vector4f(1.0F);

		GLDebug.pushGroup(100, "Clear textures");

		this.clearImages.forEach(CustomImage::clear);

		if (shadowRenderTargets != null) {
			if (packDirectives.getShadowDirectives().isShadowEnabled() == OptionalBoolean.FALSE) {
				if (shadowRenderTargets.isFullClearRequired()) {
					this.shadowClearPasses = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, false, shadowDirectives);
					this.shadowClearPassesFull = ClearPassCreator.createShadowClearPasses(shadowRenderTargets, true, shadowDirectives);
					shadowRenderTargets.onFullClear();
					ClearPass.executeAll(shadowClearPassesFull, emptyClearColor);
				}
			} else {

				RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.shadowRenderTargets.getDepthTexture(), 1.0f);

				ImmutableList<ClearPass> passes;

				for (ComputeProgram computeProgram : shadowComputes) {
					if (computeProgram != null) {
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

				ClearPass.executeAll(passes, emptyClearColor);
			}
		}

		RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();

		GpuTexture depthTexture = main.getDepthTexture();
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromMojang(depthTexture.getFormat());

		boolean changed = renderTargets.resizeIfNeeded(((Blaze3dRenderTargetExt) main).iris$getDepthBufferVersion(), depthTexture,
			main.width, main.height, depthBufferFormat, packDirectives);

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

			this.clearPassesFull = ClearPassCreator.createClearPasses(renderTargets, true, packDirectives.getRenderTargetDirectives());
			this.clearPasses = ClearPassCreator.createClearPasses(renderTargets, false, packDirectives.getRenderTargetDirectives());
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

		Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);

		ClearPass.executeAll(passes, fogColor);

		GLDebug.popGroup();

		isMainBound = true;

		if (changed) {
			boolean hasRun = false;

			for (ComputeProgram program : setup) {
				if (program != null) {
					hasRun = true;
					program.dispatch(1, 1);
				}
			}

			if (hasRun) {
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

		renderTargets.copyPreHandDepth();
	}

	@Override
	public void beginTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use a destroyed world rendering pipeline");
		}

		removePhaseIfNeeded();

		isBeforeTranslucent = false;

		renderTargets.copyPreTranslucentDepth();

		deferredRenderer.renderAll();
		DHCompat.renderLods(true);
	}

	@Override
	public void finalizeLevelRendering() {
		isRenderingWorld = false;
		removePhaseIfNeeded();

		this.compositeRenderer.renderAll();
		this.finalPassRenderer.renderFinalPass();
	}

	@Override
	public void finalizeGameRendering() {
		this.colorSpaceConverter.process(Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView());
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {

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

	public WorldRenderTargets getWorldTargets() {
		return this.worldTargets;
	}

	public ShaderStorageBufferHolder getStorageBuffers() {
		return this.shaderStorageBufferHolder;
	}

	public PackSamplers getPackSamplers() {
		return this.packSamplers;
	}

	public FrameUniforms getFrameUniforms() {
		return this.frameUniforms;
	}

	public WorldRenderTargets.Phase getWorldTargetPhase() {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? WorldRenderTargets.Phase.SHADOW : this.isBeforeTranslucent ? WorldRenderTargets.Phase.OPAQUE : WorldRenderTargets.Phase.TRANSLUCENT;
	}

	public GpuTextureView normalTextureView() {
		return (this.currentNormalTexture == null ? PBRTextureManager.INSTANCE.getDefaultHolder()
			.normalTexture() : this.currentNormalTexture).getTextureView();
	}

	public GpuTextureView specularTextureView() {
		return (this.currentSpecularTexture == null ? PBRTextureManager.INSTANCE.getDefaultHolder()
			.specularTexture() : this.currentSpecularTexture).getTextureView();
	}

	public ShaderMap getShaderMap() {
		return shaderMap;
	}

	private void destroyShaders() {

		loadedShaders.forEach(shader -> {
			shader.close();
		});
	}

	@Override
	public void destroy() {
		destroyed = true;
		destroyComputes(this.setup);
		destroyComputes(this.shadowComputes);

		destroyShaders();
		this.colorSpaceConverter.close();

		if (shadowCompositeRenderer != null) {
			shadowCompositeRenderer.destroy();
		}

		this.beginRenderer.destroy();
		prepareRenderer.destroy();
		compositeRenderer.destroy();
		deferredRenderer.destroy();
		finalPassRenderer.destroy();
		centerDepthSampler.destroy();
		customTextureManager.destroy();
		whitePixel.close();
		biggerWhitePixel.close();

		horizonRenderer.destroy();

		this.frameUniforms.close();
		this.packSamplers.close();

		renderTargets.destroy();
		dhCompat.clearPipeline();

		this.customImages.forEach(CustomImage::destroy);

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

	public AbstractTexture getBiggerWhitePixel() {
		return biggerWhitePixel;
	}

	@Override
	public void setIsMainBound(boolean bound) {
		isMainBound = bound;
	}

	@Override
	public void onBeginClear() {
		setPhase(WorldRenderingPhase.SKY);

		DimensionType.Skybox skyType = Minecraft.getInstance().level.dimensionType().skybox();

		if (shouldRenderSkyDisc && (skyType == DimensionType.Skybox.OVERWORLD || Minecraft.getInstance().level.dimensionType()
			.hasSkyLight())) {
			Vector3d fogColor3 = CapturedRenderingState.INSTANCE.getFogColor();

			Vector4f fogColor = new Vector4f((float) fogColor3.x, (float) fogColor3.y, (float) fogColor3.z, 1.0F);

			horizonRenderer.renderHorizon(CapturedRenderingState.INSTANCE.getGbufferModelView(),
				CapturedRenderingState.INSTANCE.getGbufferProjection(), fogColor);
		}
	}

	@Override
	public boolean supportsEndFlash() {
		return supportsEndFlash;
	}

	@Override
	public int getAlbedoTex() {
		return albedoTex;
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

	public RenderFramebuffer createDHFramebuffer(ProgramSource sources, boolean trans) {
		if (sources.getDirectives().getDrawBuffers().length == 0) {
			var framebuffer = new RenderFramebuffer();
			framebuffer.disableDrawBuffers();
			return framebuffer;
		}
		return renderTargets.createDHFramebuffer(trans ? flippedAfterTranslucent : flippedAfterPrepare,
			java.util.Arrays.stream(sources.getDirectives().getDrawBuffers()).sorted().toArray());
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

	public RenderFramebuffer createDHFramebufferShadow(ProgramSource sources) {

		var targets = this.shadowTargetsSupplier.get();
		RenderFramebuffer framebuffer;
		if (sources.getDirectives().getDrawBuffers().length == 0) {
			framebuffer = new RenderFramebuffer();
			framebuffer.disableDrawBuffers();
		} else {
			framebuffer = targets.createDHFramebuffer(ImmutableSet.of(),
				java.util.Arrays.stream(sources.getDirectives().getDrawBuffers()).sorted().toArray());
		}
		if (!framebuffer.hasDepthAttachment()) {
			framebuffer.addDepthAttachment(this.shadowRenderTargets.getDepthTexture());
		}
		return framebuffer;
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
}
