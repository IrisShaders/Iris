package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private boolean destroyed = false;

	private final RenderTargets renderTargets;

	private final Shader basic;
	private final Shader basicColor;
	private final Shader skyBasic;
	private final Shader skyBasicColor;
	private final Shader skyTextured;
	private final Shader skyTexturedColor;
	private final Shader clouds;
	private final Shader shadowTerrainCutout;

	private final Shader terrainSolid;
	private final Shader terrainCutout;
	private final Shader terrainCutoutMipped;

	private final Shader entitiesSolid;
	private final Shader entitiesCutout;
	private final Shader entitiesEyes;
	private final Shader shadowEntitiesCutout;
	private final Shader lightning;
	private final Shader leash;
	private final Shader particles;
	private final Shader weather;
	private final Shader crumbling;
	private final Shader text;
	private final Shader block;
	private final Shader beacon;
	private final Shader glint;
	private final Shader lines;
	private final Shader shadowLines;

	private final Shader terrainTranslucent;
	private WorldRenderingPhase phase = WorldRenderingPhase.NOT_RENDERING_WORLD;

	private final Set<Shader> loadedShaders;

	private final GlFramebuffer clearAltBuffers;
	private final GlFramebuffer clearMainBuffers;
	private final GlFramebuffer baseline;

	private Runnable createShadowMapRenderer;
	private ShadowMapRenderer shadowMapRenderer;
	private final CompositeRenderer deferredRenderer;
	private final CompositeRenderer compositeRenderer;
	private final FinalPassRenderer finalPassRenderer;
	private final NativeImageBackedSingleColorTexture normals;
	private final NativeImageBackedSingleColorTexture specular;
	private final AbstractTexture noise;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	private final SodiumTerrainPipeline sodiumTerrainPipeline;

	private final ImmutableSet<Integer> flippedBeforeTranslucent;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	boolean isBeforeTranslucent;

	private final int waterId;
	private final float sunPathRotation;
	private final boolean shouldRenderClouds;

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
		this.updateNotifier = new FrameUpdateNotifier();

		this.renderTargets = new RenderTargets(MinecraftClient.getInstance().getFramebuffer(), programSet.getPackDirectives().getRenderTargetDirectives());
		this.waterId = programSet.getPack().getIdMap().getBlockProperties().getOrDefault(new Identifier("minecraft", "water"), -1);
		this.sunPathRotation = programSet.getPackDirectives().getSunPathRotation();

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE2);

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);

		noise = programSet.getPack().getCustomNoiseTexture().flatMap(texture -> {
			try {
				AbstractTexture customNoiseTexture = new NativeImageBackedCustomTexture(texture);

				return Optional.of(customNoiseTexture);
			} catch (IOException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);
				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = programSet.getPackDirectives().getNoiseTextureResolution();

			return new NativeImageBackedNoiseTexture(noiseTextureResolution);
		});

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		// TODO: Change this once earlier passes are implemented.
		ImmutableSet<Integer> flippedBeforeTerrain = ImmutableSet.of();

		createShadowMapRenderer = () -> {
			shadowMapRenderer = new ShadowRenderer(this, programSet.getShadow().orElse(null),
					programSet.getPackDirectives(), () -> flippedBeforeTerrain, renderTargets, normals, specular, noise);
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
				noise, updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier);

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(programSet.getPackDirectives(), programSet.getComposite(), renderTargets,
				noise, updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier);
		this.finalPassRenderer = new FinalPassRenderer(programSet, renderTargets, noise, updateNotifier, flipper.snapshot(),
				centerDepthSampler, shadowMapRendererSupplier);

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedBeforeTranslucent : flippedAfterTranslucent;

		IntFunction<ProgramSamplers> createTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			IrisSamplers.addRenderTargetSamplers(builder, flipped, renderTargets, false);
			IrisSamplers.addWorldSamplers(builder, normals, specular);
			IrisSamplers.addWorldDepthSamplers(builder, renderTargets);
			IrisSamplers.addNoiseSampler(builder, noise);

			if (IrisSamplers.hasShadowSamplers(builder)) {
				createShadowMapRenderer.run();
				IrisSamplers.addShadowSamplers(builder, shadowMapRenderer);
			}

			return builder.build();
		};

		IntFunction<ProgramSamplers> createShadowTerrainSamplers = (programId) -> {
			ProgramSamplers.Builder builder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

			IrisSamplers.addRenderTargetSamplers(builder, () -> flippedBeforeTerrain, renderTargets, false);
			IrisSamplers.addWorldSamplers(builder, normals, specular);
			IrisSamplers.addNoiseSampler(builder, noise);

			// Only initialize these samplers if the shadow map renderer exists.
			// Otherwise, this program shouldn't be used at all?
			if (IrisSamplers.hasShadowSamplers(builder) && shadowMapRenderer != null) {
				IrisSamplers.addShadowSamplers(builder, shadowMapRenderer);
			}

			return builder.build();
		};

		Optional<ProgramSource> basicSource = programSet.getGbuffersBasic();

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
		Optional<ProgramSource> glintSource = first(programSet.getGbuffersGlint(), programSet.getGbuffersTextured());

		Optional<ProgramSource> damagedBlockSource = first(programSet.getGbuffersDamagedBlock(), terrainSource);

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// Matches OptiFine's default for CUTOUT and CUTOUT_MIPPED.
		AlphaTest terrainCutoutAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);
		AlphaTest nonZeroAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.0001F);

		this.loadedShaders = new HashSet<>();

		// TODO: Resolve hasColorAttrib based on the vertex format
		try {
			this.basic = createShader("gbuffers_basic", basicSource, AlphaTest.ALWAYS, VertexFormats.POSITION, FogMode.LINEAR);
			this.basicColor = createShader("gbuffers_basic_color", basicSource, nonZeroAlpha, VertexFormats.POSITION_COLOR, FogMode.OFF);
			this.skyBasic = createShader("gbuffers_sky_basic", skyBasicSource, AlphaTest.ALWAYS, VertexFormats.POSITION, FogMode.LINEAR);
			this.skyBasicColor = createShader("gbuffers_sky_basic_color", skyBasicSource, nonZeroAlpha, VertexFormats.POSITION_COLOR, FogMode.OFF);
			this.skyTextured = createShader("gbuffers_sky_textured", skyTexturedSource, AlphaTest.ALWAYS, VertexFormats.POSITION_TEXTURE, FogMode.OFF);
			this.skyTexturedColor = createShader("gbuffers_sky_textured_tex_color", skyTexturedSource, AlphaTest.ALWAYS, VertexFormats.POSITION_TEXTURE_COLOR, FogMode.OFF);
			this.clouds = createShader("gbuffers_clouds", cloudsSource, terrainCutoutAlpha, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL, FogMode.LINEAR);
			this.terrainSolid = createShader("gbuffers_terrain_solid", terrainSource, AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.terrainCutout = createShader("gbuffers_terrain_cutout", terrainSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.terrainCutoutMipped = createShader("gbuffers_terrain_cutout_mipped", terrainSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			this.entitiesSolid = createShader("gbuffers_entities_solid", entitiesSource, AlphaTest.ALWAYS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, FogMode.LINEAR);
			this.entitiesCutout = createShader("gbuffers_entities_cutout", entitiesSource, terrainCutoutAlpha, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, FogMode.LINEAR);
			this.entitiesEyes = createShader("gbuffers_spidereyes", entityEyesSource, nonZeroAlpha, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, FogMode.LINEAR);
			this.lightning = createShader("gbuffers_lightning", entitiesSource, AlphaTest.ALWAYS, VertexFormats.POSITION_COLOR, FogMode.LINEAR);
			this.leash = createShader("gbuffers_leash", basicSource, AlphaTest.ALWAYS, VertexFormats.POSITION_COLOR_LIGHT, FogMode.LINEAR);
			this.particles = createShader("gbuffers_particles", particleSource, terrainCutoutAlpha, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, FogMode.LINEAR);
			this.weather = createShader("gbuffers_weather", weatherSource, terrainCutoutAlpha, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, FogMode.LINEAR);
			this.crumbling = createShader("gbuffers_damagedblock", damagedBlockSource, terrainCutoutAlpha, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, FogMode.OFF);
			this.text = createShader("gbuffers_entities_text", entitiesSource, nonZeroAlpha, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, FogMode.LINEAR);
			this.block = createShader("gbuffers_block", blockSource, terrainCutoutAlpha, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, FogMode.LINEAR);
			this.beacon = createShader("gbuffers_beaconbeam", beaconSource, AlphaTest.ALWAYS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, FogMode.LINEAR);
			this.glint = createShader("gbuffers_glint", glintSource, nonZeroAlpha, VertexFormats.POSITION_TEXTURE, FogMode.LINEAR);
			this.lines = createShader("gbuffers_lines", programSet.getGbuffersBasic(), AlphaTest.ALWAYS, VertexFormats.LINES, FogMode.LINEAR);

			if (translucentSource != terrainSource) {
				this.terrainTranslucent = createShader("gbuffers_translucent", translucentSource, AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, FogMode.LINEAR);
			} else {
				this.terrainTranslucent = this.terrainSolid;
			}
		} catch (RuntimeException e) {
			destroyShaders();

			throw e;
		}

		BlockRenderingSettings.INSTANCE.setIdMap(programSet.getPack().getIdMap());
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(programSet.getPackDirectives().shouldUseSeparateAo());

		int[] buffersToBeCleared = programSet.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared().toIntArray();

		this.clearAltBuffers = renderTargets.createFramebufferWritingToAlt(buffersToBeCleared);
		this.clearMainBuffers = renderTargets.createFramebufferWritingToMain(buffersToBeCleared);

		if (shadowMapRenderer == null) {
			// Fallback just in case.
			// TODO: Can we remove this?
			this.shadowMapRenderer = new EmptyShadowMapRenderer(programSet.getPackDirectives().getShadowDirectives().getResolution());
			this.shadowTerrainCutout = null;
			this.shadowEntitiesCutout = null;
			this.shadowLines = null;
		} else {
			try {
				// TODO: Shadow programs should have access to different samplers.
				this.shadowTerrainCutout = createShadowShader("shadow_terrain_cutout", shadowSource, terrainCutoutAlpha, IrisVertexFormats.TERRAIN);
				this.shadowEntitiesCutout = createShadowShader("shadow_entities_cutout", shadowSource, terrainCutoutAlpha, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
				this.shadowLines = createShadowShader("shadow_lines", shadowSource, AlphaTest.ALWAYS, VertexFormats.LINES);
			}  catch (RuntimeException e) {
				destroyShaders();

				throw e;
			}
		}

		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(programSet, createTerrainSamplers,
				createShadowTerrainSamplers, renderTargets, flippedBeforeTranslucent, flippedAfterTranslucent,
				shadowMapRenderer instanceof ShadowRenderer ? ((ShadowRenderer) shadowMapRenderer).getFramebuffer() :
				null);
	}

	@Nullable
	private Shader createShader(String name, Optional<ProgramSource> source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode) throws IOException {
		if (!source.isPresent()) {
			return null;
		}

		return createShader(name, source.get(), fallbackAlpha, vertexFormat, fogMode);
	}

	private Shader createShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode) throws IOException {
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

		// TODO: All samplers added here need to be mirrored in NewShaderTests. Possible way to bypass this?
		IrisSamplers.addRenderTargetSamplers(extendedShader, flipped, renderTargets, false);

		// TODO: IrisSamplers.addWorldSamplers(builder, normals, specular);
		extendedShader.addDynamicSampler(normals::getGlId, "normals");
		extendedShader.addDynamicSampler(specular::getGlId, "specular");

		IrisSamplers.addWorldDepthSamplers(extendedShader, renderTargets);
		IrisSamplers.addNoiseSampler(extendedShader, noise);

		if (IrisSamplers.hasShadowSamplers(extendedShader)) {
			createShadowMapRenderer.run();
			IrisSamplers.addShadowSamplers(extendedShader, shadowMapRenderer);
		}

		return extendedShader;
	}

	private Shader createShadowShader(String name, Optional<ProgramSource> source, AlphaTest fallbackAlpha, VertexFormat vertexFormat) throws IOException {
		if (!source.isPresent()) {
			return null;
		}

		return createShadowShader(name, source.get(), fallbackAlpha, vertexFormat);
	}

	private Shader createShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat) throws IOException {
		GlFramebuffer framebuffer = ((ShadowRenderer) this.shadowMapRenderer).getFramebuffer();

		ExtendedShader extendedShader = NewShaderTests.create(name, source, framebuffer, framebuffer, baseline, fallbackAlpha, vertexFormat, updateNotifier, this, FogMode.LINEAR);

		loadedShaders.add(extendedShader);

		// TODO: waterShadowEnabled?
		// TODO: Audit these render targets...

		extendedShader.addIrisSampler("normals", this.normals.getGlId());
		extendedShader.addIrisSampler("specular", this.specular.getGlId());
		extendedShader.addIrisSampler("shadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("watershadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex0", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex1", this.shadowMapRenderer.getDepthTextureNoTranslucentsId());
		extendedShader.addIrisSampler("depthtex0", this.renderTargets.getDepthTexture().getTextureId());
		extendedShader.addIrisSampler("depthtex1", this.renderTargets.getDepthTextureNoTranslucents().getTextureId());
		extendedShader.addIrisSampler("noisetex", this.noise.getGlId());
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
	public void beginWorldRendering() {
		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		// NB: execute this before resizing / clearing so that the center depth sample is retrieved properly.
		updateNotifier.onNewFrame();

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		clearMainBuffers.bind();
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		clearAltBuffers.bind();
		// Not clearing the depth buffer since there's only one of those and it was already cleared
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		// Make sure to switch back to the main framebuffer. If we forget to do this then our alt buffers might be
		// cleared to the fog color, which absolutely is not what we want!
		//
		// If we forget to do this, then weird lines appear at the top of the screen and the right of the screen
		// on Sildur's Vibrant Shaders.
		main.beginWrite(true);

		isBeforeTranslucent = true;
	}

	@Override
	public void renderShadows(WorldRendererAccessor worldRenderer, Camera playerCamera) {
		this.shadowMapRenderer.renderShadows(worldRenderer, playerCamera);
	}

	@Override
	public void addDebugText(List<String> messages) {
		ShadowMapRenderer shadowMapRenderer = this.shadowMapRenderer;

		if (shadowMapRenderer instanceof ShadowRenderer) {
			messages.add("");
			messages.add("[Iris] Shadow Maps: " + ShadowRenderer.OVERALL_DEBUG_STRING);
			messages.add("[Iris] Shadow Terrain: " + ShadowRenderer.SHADOW_DEBUG_STRING);
			messages.add("[Iris] Shadow Entities: " + ShadowRenderer.getEntitiesDebugString());
			messages.add("[Iris] Shadow Block Entities: " + ShadowRenderer.getBlockEntitiesDebugString());
		} else if (shadowMapRenderer instanceof EmptyShadowMapRenderer) {
			messages.add("");
			messages.add("[Iris] Shadow Maps: not used by shader pack");
		} else {
			throw new IllegalStateException("Unknown shadow map renderer type!");
		}
	}

	@Override
	public void beginShadowRender() {

	}

	@Override
	public void endShadowRender() {

	}

	@Override
	public void beginTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use a destroyed world rendering pipeline");
		}

		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 and depthtex2 can contain the depth values for
		// all non-translucent content, as required.
		baseline.bindAsReadBuffer();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);

		deferredRenderer.renderAll();

		RenderSystem.enableBlend();

		MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
		MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();

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
	public void finalizeWorldRendering() {
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
	public boolean shouldDisableDirectionalShading() {
		return true;
	}

	@Override
	public Shader getBasic() {
		return basic;
	}

	@Override
	public Shader getBasicColor() {
		return basicColor;
	}

	@Override
	public Shader getSkyBasic() {
		return skyBasic;
	}

	@Override
	public Shader getSkyBasicColor() {
		return skyBasicColor;
	}

	@Override
	public Shader getSkyTextured() {
		return skyTextured;
	}

	@Override
	public Shader getSkyTexturedColor() {
		return skyTexturedColor;
	}

	@Override
	public Shader getClouds() {
		return clouds;
	}

	@Override
	public Shader getTerrain() {
		return terrainSolid;
	}

	@Override
	public Shader getTerrainCutout() {
		return terrainCutout;
	}

	@Override
	public Shader getTerrainCutoutMipped() {
		return terrainCutoutMipped;
	}

	@Override
	public Shader getEntitiesCutout() {
		return entitiesCutout;
	}

	@Override
	public Shader getEntitiesEyes() {
		return entitiesEyes;
	}

	@Override
	public Shader getLeash() {
		return leash;
	}

	@Override
	public Shader getLightning() {
		return lightning;
	}

	@Override
	public Shader getParticles() {
		return particles;
	}

	@Override
	public Shader getWeather() {
		return weather;
	}

	@Override
	public Shader getCrumbling() {
		return crumbling;
	}

	@Override
	public Shader getText() {
		return text;
	}

	@Override
	public Shader getBlock() {
		return block;
	}

	@Override
	public Shader getBeacon() {
		return beacon;
	}

	@Override
	public Shader getEntitiesSolid() {
		return entitiesSolid;
	}

	@Override
	public Shader getShadowTerrainCutout() {
		return shadowTerrainCutout;
	}

	@Override
	public Shader getShadowEntitiesCutout() {
		return shadowEntitiesCutout;
	}

	@Override
	public Shader getTranslucent() {
		return terrainTranslucent;
	}

	@Override
	public Shader getGlint() {
		return glint;
	}

	@Override
	public Shader getLines() {
		return lines;
	}

	@Override
	public Shader getShadowLines() {
		return shadowLines;
	}

	private void destroyShaders() {
		// NB: If you forget this, shader reloads won't work!
		loadedShaders.forEach(shader -> {
			shader.unbind();
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
		normals.close();
		specular.close();
		noise.close();

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);

		shadowMapRenderer.destroy();
		renderTargets.destroy();
	}

	@Override
	public SodiumTerrainPipeline getSodiumTerrainPipeline() {
		return sodiumTerrainPipeline;
	}

	@Override
	public float getSunPathRotation() {
		return sunPathRotation;
	}

	@Override
	public FrameUpdateNotifier getUpdateNotifier() {
		return updateNotifier;
	}
}
