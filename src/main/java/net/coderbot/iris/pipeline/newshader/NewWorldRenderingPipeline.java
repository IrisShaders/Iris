package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.coderbot.iris.block_rendering.BlockMaterialMapping;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ClearPass;
import net.coderbot.iris.pipeline.ClearPassCreator;
import net.coderbot.iris.pipeline.CustomTextureManager;
import net.coderbot.iris.pipeline.HorizonRenderer;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.DepthBufferTracker;
import net.coderbot.iris.samplers.IrisImages;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramFallbackResolver;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
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
	private final RenderTargets renderTargets;
	private final ShaderMap shaderMap;

	private WorldRenderingPhase phase = WorldRenderingPhase.NONE;

	private final Set<ShaderInstance> loadedShaders;
	private final ImmutableList<ClearPass> clearPassesFull;
	private final ImmutableList<ClearPass> clearPasses;
	private final GlFramebuffer baseline;

	private final CompositeRenderer prepareRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;

	private final CustomTextureManager customTextureManager;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;
	private final SodiumTerrainPipeline sodiumTerrainPipeline;

	private final ImmutableSet<Integer> flippedAfterPrepare;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	public boolean isBeforeTranslucent;

	private final HorizonRenderer horizonRenderer = new HorizonRenderer();

	private final float sunPathRotation;
	private final boolean shouldRenderClouds;
	private final boolean shouldRenderUnderwaterOverlay;
	private final boolean shouldRenderVignette;
	private final boolean shouldWriteRainAndSnowToDepthBuffer;
	private final boolean shouldRenderParticlesBeforeDeferred;
	private final boolean oldLighting;
	private final OptionalInt forcedShadowRenderDistanceChunks;
	private boolean destroyed = false;
	private boolean isRenderingWorld;

	private Runnable createShadowMapRenderer;
	private ShadowMapRenderer shadowMapRenderer;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
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
		}

		this.shouldRenderClouds = programSet.getPackDirectives().areCloudsEnabled();
		this.shouldRenderUnderwaterOverlay = programSet.getPackDirectives().underwaterOverlay();
		this.shouldRenderVignette = programSet.getPackDirectives().vignette();
		this.shouldWriteRainAndSnowToDepthBuffer = programSet.getPackDirectives().rainDepth();
		this.shouldRenderParticlesBeforeDeferred = programSet.getPackDirectives().areParticlesBeforeDeferred();
		this.oldLighting = programSet.getPackDirectives().isOldLighting();
		this.updateNotifier = new FrameUpdateNotifier();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		int depthTextureId = main.getDepthTextureId();
		DepthBufferFormat depthBufferFormat = DepthBufferTracker.INSTANCE.getFormat(depthTextureId);

		this.renderTargets = new RenderTargets(main.width, main.height, depthTextureId, depthBufferFormat, programSet.getPackDirectives().getRenderTargetDirectives().getRenderTargetSettings());
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

		ImmutableSet<Integer> flippedBeforeShadow = ImmutableSet.of();

		createShadowMapRenderer = () -> {
			shadowMapRenderer = new ShadowRenderer(this, programSet,
					programSet.getPackDirectives(), renderTargets);
			createShadowMapRenderer = () -> {};
		};


		BufferFlipper flipper = new BufferFlipper();

		this.centerDepthSampler = new CenterDepthSampler(renderTargets);

		Supplier<ShadowMapRenderer> shadowMapRendererSupplier = () -> {
			createShadowMapRenderer.run();
			return shadowMapRenderer;
		};

		this.prepareRenderer = new CompositeRenderer(programSet.getPackDirectives(), programSet.getPrepare(), renderTargets,
				customTextureManager.getNoiseTexture(), updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier,
				customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.PREPARE, Object2ObjectMaps.emptyMap()),
				programSet.getPackDirectives().getExplicitFlips("prepare_pre"));

		flippedAfterPrepare = flipper.snapshot();

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

			IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flippedBeforeShadow, renderTargets, false);
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

			IrisImages.addRenderTargetImages(builder, () -> flippedBeforeShadow, renderTargets);

			if (IrisImages.hasShadowImages(builder) && shadowMapRenderer != null) {
				IrisImages.addShadowColorImages(builder, shadowMapRenderer);
			}

			return builder.build();
		};

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});
		this.loadedShaders = new HashSet<>();

		ProgramFallbackResolver resolver = new ProgramFallbackResolver(programSet);

		this.shaderMap = new ShaderMap(key -> {
			try {
				if (key.isShadow()) {
					if (shadowMapRenderer != null) {
						return createShadowShader(key.getName(), resolver.resolve(key.getProgram()), key);
					} else {
						return null;
					}
				} else {
					return createShader(key.getName(), resolver.resolve(key.getProgram()), key);
				}
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
		} else {
			ShaderInstance shader = shaderMap.getShader(ShaderKey.SHADOW_TERRAIN_CUTOUT);

			if (shader instanceof ExtendedShader && shadowMapRenderer instanceof ShadowRenderer) {
				// Assume voxelization if image load / store is detected in the shadow program.
				// We have to do this here to avoid a circular reference in setting up shaders.
				// This code should probably be cleaned up later.
				ImageHolder holder = ((ExtendedShader) getShaderMap().getShader(ShaderKey.SHADOW_TERRAIN_CUTOUT));

				((ShadowRenderer) shadowMapRenderer).packHasVoxelization
						|= IrisImages.hasShadowImages(holder)
						|| IrisImages.hasRenderTargetImages(holder, renderTargets);
			}
		}

		// TODO: Create fallback Sodium shaders if the pack doesn't provide terrain shaders
		//       Currently we use Sodium's shaders but they don't support EXP2 fog underwater.
		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(this, programSet, createTerrainSamplers,
			shadowMapRenderer instanceof EmptyShadowMapRenderer || shadowMapRenderer == null ? null : createShadowTerrainSamplers, createTerrainImages, createShadowTerrainImages, renderTargets, flippedAfterPrepare, flippedAfterTranslucent,
				shadowMapRenderer instanceof ShadowRenderer ? ((ShadowRenderer) shadowMapRenderer).getFramebuffer() :
						null);
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

	private ShaderInstance createShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (!source.isPresent()) {
			return createFallbackShader(name, key);
		}

		return createShader(name, source.get(), key.getAlphaTest(), key.getVertexFormat(), key.getFogMode(),
				key.shouldIgnoreLightmap());
	}

	private ShaderInstance createShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, FogMode fogMode,
										boolean isFullbright) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, source.getDirectives().getDrawBuffers());
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, source.getDirectives().getDrawBuffers());

		ExtendedShader extendedShader = NewShaderTests.create(name, source, beforeTranslucent, afterTranslucent,
				baseline, fallbackAlpha, vertexFormat, updateNotifier, this, fogMode, isFullbright);

		loadedShaders.add(extendedShader);

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedAfterPrepare : flippedAfterTranslucent;

		addGbufferOrShadowSamplers(extendedShader, flipped, false);

		return extendedShader;
	}

	private ShaderInstance createFallbackShader(String name, ShaderKey key) throws IOException {
		GlFramebuffer beforeTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterPrepare, new int[] {0});
		GlFramebuffer afterTranslucent = renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, new int[] {0});

		FallbackShader shader = NewShaderTests.createFallback(name, beforeTranslucent, afterTranslucent,
				key.getAlphaTest(), key.getVertexFormat(), null, this, key.getFogMode(),
				key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		loadedShaders.add(shader);

		return shader;
	}

	private ShaderInstance createShadowShader(String name, Optional<ProgramSource> source, ShaderKey key) throws IOException {
		if (!source.isPresent()) {
			return createFallbackShadowShader(name, key);
		}

		return createShadowShader(name, source.get(), key.getAlphaTest(), key.getVertexFormat(),
				key.shouldIgnoreLightmap());
	}

	private ShaderInstance createFallbackShadowShader(String name, ShaderKey key) throws IOException {
		GlFramebuffer framebuffer = this.shadowMapRenderer.getRenderTargets().getFramebuffer();

		FallbackShader shader = NewShaderTests.createFallback(name, framebuffer, framebuffer,
				key.getAlphaTest(), key.getVertexFormat(), BlendModeOverride.OFF, this, key.getFogMode(),
				key.hasDiffuseLighting(), key.isIntensity(), key.shouldIgnoreLightmap());

		loadedShaders.add(shader);

		return shader;
	}

	private ShaderInstance createShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
											  VertexFormat vertexFormat, boolean isFullbright) throws IOException {
		GlFramebuffer framebuffer = this.shadowMapRenderer.getRenderTargets().getFramebuffer();

		ExtendedShader extendedShader = NewShaderTests.create(name, source, framebuffer, framebuffer, baseline,
				fallbackAlpha, vertexFormat, updateNotifier, this, FogMode.PER_VERTEX, isFullbright);

		loadedShaders.add(extendedShader);

		ImmutableSet<Integer> flippedSet = ImmutableSet.of();
		Supplier<ImmutableSet<Integer>> flipped = () -> flippedSet;

		addGbufferOrShadowSamplers(extendedShader, flipped, true);

		return extendedShader;
	}

	private void addGbufferOrShadowSamplers(ExtendedShader extendedShader, Supplier<ImmutableSet<Integer>> flipped,
											boolean isShadowPass) {
		TextureStage textureStage = TextureStage.GBUFFERS_AND_SHADOW;

		ProgramSamplers.CustomTextureSamplerInterceptor samplerHolder =
				ProgramSamplers.customTextureSamplerInterceptor(extendedShader,
						customTextureManager.getCustomTextureIdMap().getOrDefault(textureStage, Object2ObjectMaps.emptyMap()));

		IrisSamplers.addRenderTargetSamplers(samplerHolder, flipped, renderTargets, false);
		IrisImages.addRenderTargetImages(extendedShader, flipped, renderTargets);

		// TODO: IrisSamplers.addLevelSamplers(builder, normals, specular);
		samplerHolder.addDynamicSampler(customTextureManager.getNormals()::getId, "normals");
		samplerHolder.addDynamicSampler(customTextureManager.getSpecular()::getId, "specular");

		IrisSamplers.addWorldDepthSamplers(samplerHolder, this.renderTargets);
		IrisSamplers.addNoiseSampler(samplerHolder, this.customTextureManager.getNoiseTexture());

		if (isShadowPass || IrisSamplers.hasShadowSamplers(samplerHolder)) {
			if (!isShadowPass) {
				createShadowMapRenderer.run();
			}

			IrisSamplers.addShadowSamplers(samplerHolder, shadowMapRenderer);
		}

		if (isShadowPass || IrisImages.hasShadowImages(extendedShader)) {
			IrisImages.addShadowColorImages(extendedShader, shadowMapRenderer);
		}
	}

	@Override
	public WorldRenderingPhase getPhase() {
		return phase;
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {
		this.phase = phase;
	}

	@Override
	public void beginLevelRendering() {
		isRenderingWorld = true;

		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		// NB: execute this before resizing / clearing so that the center depth sample is retrieved properly.
		updateNotifier.onNewFrame();

		RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		renderTargets.resizeIfNeeded(((Blaze3dRenderTargetExt) main).iris$isDepthBufferDirty(), main.getDepthTextureId(), main.width, main.height, DepthBufferTracker.INSTANCE.getFormat(main.getDepthTextureId()));
		((Blaze3dRenderTargetExt) main).iris$clearDepthBufferDirtyFlag();

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

		setPhase(WorldRenderingPhase.SKY);

		// Render our horizon box before actual sky rendering to avoid being broken by mods that do weird things
		// while rendering the sky.
		//
		// A lot of dimension mods touch sky rendering, FabricSkyboxes injects at HEAD and cancels, etc.
		DimensionSpecialEffects.SkyType skyType = Minecraft.getInstance().level.effects().skyType();

		if (skyType != DimensionSpecialEffects.SkyType.NONE) {
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);

			RenderSystem.setShaderColor(fogColor.x, fogColor.y, fogColor.z, fogColor.w);

			horizonRenderer.renderHorizon(CapturedRenderingState.INSTANCE.getGbufferModelView(), CapturedRenderingState.INSTANCE.getGbufferProjection(), GameRenderer.getPositionShader());

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
		}
	}

	@Override
	public void renderShadows(LevelRendererAccessor worldRenderer, Camera playerCamera) {
		this.shadowMapRenderer.renderShadows(worldRenderer, playerCamera);

		prepareRenderer.renderAll();
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

	private void copyDepthTexture() {
		// Note: Use copyTexSubImage2D, since that will not re-allocate the target texture's storage,
		// even though we copy the whole depth texture. This might make the copy be a bit faster.
		GlStateManager._glCopyTexSubImage2D(
			// target
			GL20C.GL_TEXTURE_2D,
			// level
			0,
			// xoffset, yoffset
			0, 0,
			// x, y
			0, 0,
			// width
			renderTargets.getCurrentWidth(),
			// height
			renderTargets.getCurrentHeight());
	}

	@Override
	public void beginHand() {
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

		centerDepthSampler.updateSample();

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
		isRenderingWorld = false;
		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return shadowMapRenderer instanceof ShadowRenderer;
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
	public boolean shouldDisableDirectionalShading() {
		return !oldLighting;
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
	public boolean isRenderingWorld() {
		return isRenderingWorld;
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
