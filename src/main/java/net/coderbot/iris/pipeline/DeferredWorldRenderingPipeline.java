package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.postprocess.FinalPassRenderer;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.opengl.GL30C;

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
	private final Pass blockEntities;
	@Nullable
	private final Pass glowingEntities;
	@Nullable
	private final Pass glint;
	@Nullable
	private final Pass eyes;

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

	private final ImmutableSet<Integer> flippedBeforeTranslucent;
	private final ImmutableSet<Integer> flippedAfterTranslucent;

	private final SodiumTerrainPipeline sodiumTerrainPipeline;

	private boolean isBeforeTranslucent;

	private final int waterId;
	private final float sunPathRotation;
	private final boolean shouldRenderClouds;

	private static final List<GbufferProgram> programStack = new ArrayList<>();
	private static final List<String> programStackLog = new ArrayList<>();

	private static final Identifier WATER_IDENTIFIER = new Identifier("minecraft", "water");

	public DeferredWorldRenderingPipeline(ProgramSet programs) {
		Objects.requireNonNull(programs);

		this.shouldRenderClouds = programs.getPackDirectives().areCloudsEnabled();
		this.updateNotifier = new FrameUpdateNotifier();

		this.allPasses = new ArrayList<>();

		this.renderTargets = new RenderTargets(MinecraftClient.getInstance().getFramebuffer(), programs.getPackDirectives().getRenderTargetDirectives());
		this.waterId = programs.getPack().getIdMap().getBlockProperties().getOrDefault(Registry.BLOCK.get(WATER_IDENTIFIER).getDefaultState(), -1);
		this.sunPathRotation = programs.getPackDirectives().getSunPathRotation();

		BlockRenderingSettings.INSTANCE.setIdMap(programs.getPack().getIdMap());
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(programs.getPackDirectives().shouldUseSeparateAo());

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager.glActiveTexture(GL20C.GL_TEXTURE2);

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);

		noise = programs.getPack().getCustomNoiseTexture().flatMap(texture -> {
			try {
				AbstractTexture customNoiseTexture = new NativeImageBackedCustomTexture(texture);

				return Optional.of(customNoiseTexture);
			} catch (IOException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);
				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = programs.getPackDirectives().getNoiseTextureResolution();

			return new NativeImageBackedNoiseTexture(noiseTextureResolution);
		});

		GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0);

		// TODO: Change this once earlier passes are implemented.
		ImmutableSet<Integer> flippedBeforeTerrain = ImmutableSet.of();

		createShadowMapRenderer = () -> {
			shadowMapRenderer = new ShadowRenderer(this, programs.getShadow().orElse(null),
					programs.getPackDirectives(), () -> flippedBeforeTerrain, renderTargets, normals, specular, noise);
			createShadowMapRenderer = () -> {};
		};

		BufferFlipper flipper = new BufferFlipper();

		this.centerDepthSampler = new CenterDepthSampler(renderTargets, updateNotifier);

		flippedBeforeTranslucent = flipper.snapshot();

		Supplier<ShadowMapRenderer> shadowMapRendererSupplier = () -> {
			createShadowMapRenderer.run();
			return shadowMapRenderer;
		};

		this.deferredRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getDeferred(), renderTargets,
				noise, updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier);

		flippedAfterTranslucent = flipper.snapshot();

		this.compositeRenderer = new CompositeRenderer(programs.getPackDirectives(), programs.getComposite(), renderTargets,
				noise, updateNotifier, centerDepthSampler, flipper, shadowMapRendererSupplier);
		this.finalPassRenderer = new FinalPassRenderer(programs, renderTargets, noise, updateNotifier, flipper.snapshot(),
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

		this.basic = programs.getGbuffersBasic().map(this::createPass).orElse(null);
		this.textured = programs.getGbuffersTextured().map(this::createPass).orElse(basic);
		this.texturedLit = programs.getGbuffersTexturedLit().map(this::createPass).orElse(textured);
		this.skyBasic = programs.getGbuffersSkyBasic().map(this::createPass).orElse(basic);
		this.skyTextured = programs.getGbuffersSkyTextured().map(this::createPass).orElse(textured);
		this.clouds = programs.getGbuffersClouds().map(this::createPass).orElse(textured);
		this.terrain = programs.getGbuffersTerrain().map(this::createPass).orElse(texturedLit);
		this.translucent = programs.getGbuffersWater().map(this::createPass).orElse(terrain);
		this.damagedBlock = programs.getGbuffersDamagedBlock().map(this::createPass).orElse(terrain);
		this.weather = programs.getGbuffersWeather().map(this::createPass).orElse(texturedLit);
		this.beaconBeam = programs.getGbuffersBeaconBeam().map(this::createPass).orElse(textured);
		this.entities = programs.getGbuffersEntities().map(this::createPass).orElse(texturedLit);
		this.blockEntities = programs.getGbuffersBlock().map(this::createPass).orElse(terrain);
		this.glowingEntities = programs.getGbuffersEntitiesGlowing().map(this::createPass).orElse(entities);
		this.glint = programs.getGbuffersGlint().map(this::createPass).orElse(textured);
		this.eyes = programs.getGbuffersEntityEyes().map(this::createPass).orElse(textured);

		int[] buffersToBeCleared = programs.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared().toIntArray();

		this.clearAltBuffers = renderTargets.createFramebufferWritingToAlt(buffersToBeCleared);
		this.clearMainBuffers = renderTargets.createFramebufferWritingToMain(buffersToBeCleared);
		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		if (shadowMapRenderer == null) {
			// Fallback just in case.
			// TODO: Can we remove this?
			this.shadowMapRenderer = new EmptyShadowMapRenderer(programs.getPackDirectives().getShadowDirectives().getResolution());
		}

		this.sodiumTerrainPipeline = new SodiumTerrainPipeline(programs, createTerrainSamplers,
				createShadowTerrainSamplers, renderTargets, flippedBeforeTranslucent, flippedAfterTranslucent,
				shadowMapRenderer instanceof ShadowRenderer ? ((ShadowRenderer) shadowMapRenderer).getFramebuffer() :
						null);
	}

	private void checkWorld() {
		// If we're not in a world, then obviously we cannot possibly be rendering a world.
		if (MinecraftClient.getInstance().world == null) {
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

		if (popped != GbufferProgram.NONE && popped != GbufferProgram.CLEAR) {
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
			case BEACON_BEAM:
				return beaconBeam;
			case ENTITIES:
				return entities;
			case BLOCK_ENTITIES:
				return blockEntities;
			case ENTITIES_GLOWING:
				return glowingEntities;
			case EYES:
				return eyes;
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
		} else if (program == GbufferProgram.CLEAR) {
			// Ensure that Minecraft's main framebuffer is cleared, or else very odd issues will happen with shaders
			// that have composites that don't write to all pixels.
			//
			// NB: colortex0 should not be cleared to the fog color! This causes a lot of issues on shaderpacks like
			// Sildur's Vibrant Shaders. Instead, it should be cleared to solid black like the other buffers. The
			// horizon rendered by HorizonRenderer ensures that shaderpacks that don't override the sky rendering don't
			// have issues, and this also gives shaderpacks more control over sky rendering in general.
			MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
			Program.unbind();

			return;
		}

		Pass pass = getPass(program);
		beginPass(pass);

		if (program == GbufferProgram.TERRAIN) {
			if (terrain != null) {
				setupAttributes(terrain);
			}
		} else if (program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			if (translucent != null) {
				setupAttributes(translucent);

				// TODO: This is just making it so that all translucent content renders like water. We need to
				// properly support mc_Entity!
				setupAttribute(translucent, "mc_Entity", 10, waterId, -1.0F, -1.0F, -1.0F);
			}
		}

		if (program != GbufferProgram.TRANSLUCENT_TERRAIN && pass != null && pass == translucent) {
			// Make sure that other stuff sharing the same program isn't rendered like water
			setupAttribute(translucent, "mc_Entity", 10, -1.0F, -1.0F, -1.0F, -1.0F);
		}
	}

	private void teardownProgram() {
		Program.unbind();
		this.baseline.bind();
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		// TODO: Don't hardcode this for Sildur's
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return true;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return true;
	}

	@Override
	public boolean shouldRenderClouds() {
		return shouldRenderClouds;
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
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
				source.getFragmentSource().orElse(null), IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier);

		Supplier<ImmutableSet<Integer>> flipped =
				() -> isBeforeTranslucent ? flippedBeforeTranslucent : flippedAfterTranslucent;

		IrisSamplers.addRenderTargetSamplers(builder, flipped, renderTargets, false);
		IrisSamplers.addWorldSamplers(builder, normals, specular);
		IrisSamplers.addWorldDepthSamplers(builder, renderTargets);
		IrisSamplers.addNoiseSampler(builder, noise);

		if (IrisSamplers.hasShadowSamplers(builder)) {
			createShadowMapRenderer.run();
			IrisSamplers.addShadowSamplers(builder, shadowMapRenderer);
		}

		GlFramebuffer framebufferBeforeTranslucents =
				renderTargets.createGbufferFramebuffer(flippedBeforeTranslucent, source.getDirectives().getDrawBuffers());
		GlFramebuffer framebufferAfterTranslucents =
				renderTargets.createGbufferFramebuffer(flippedAfterTranslucent, source.getDirectives().getDrawBuffers());

		builder.bindAttributeLocation(10, "mc_Entity");
		builder.bindAttributeLocation(11, "mc_midTexCoord");
		builder.bindAttributeLocation(12, "at_tangent");

		AlphaTest alphaTestOverride = source.getDirectives().getAlphaTestOverride().orElse(null);

		if (alphaTestOverride != null) {
			Iris.logger.info("Configured alpha test override for " + source.getName() + ": " + alphaTestOverride);
		}

		Pass pass = new Pass(builder.build(), framebufferBeforeTranslucents, framebufferAfterTranslucents, alphaTestOverride,
				source.getDirectives().shouldDisableBlend());

		allPasses.add(pass);

		return pass;
	}

	private final class Pass {
		private final Program program;
		private final GlFramebuffer framebufferBeforeTranslucents;
		private final GlFramebuffer framebufferAfterTranslucents;
		private final AlphaTest alphaTestOverride;
		private final boolean disableBlend;

		private Pass(Program program, GlFramebuffer framebufferBeforeTranslucents, GlFramebuffer framebufferAfterTranslucents, AlphaTest alphaTestOverride, boolean disableBlend) {
			this.program = program;
			this.framebufferBeforeTranslucents = framebufferBeforeTranslucents;
			this.framebufferAfterTranslucents = framebufferAfterTranslucents;
			this.alphaTestOverride = alphaTestOverride;
			this.disableBlend = disableBlend;
		}

		public void use() {
			if (isBeforeTranslucent) {
				framebufferBeforeTranslucents.bind();
			} else {
				framebufferAfterTranslucents.bind();
			}

			program.use();

			// TODO: Render layers will likely override alpha testing and blend state, perhaps we need a way to override
			// that.
			if (alphaTestOverride != null) {
				alphaTestOverride.setup();
			}

			if (disableBlend) {
				GlStateManager._disableBlend();
			}
		}

		public void stopUsing() {
			if (alphaTestOverride != null) {
				AlphaTest.teardown();
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

		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);

		// Destroy our render targets
		//
		// While it's possible to just clear them instead and reuse them, we'd need to investigate whether or not this
		// would help performance.
		renderTargets.destroy();

		// Destroy the shadow map renderer and its render targets
		shadowMapRenderer.destroy();

		// Destroy the static samplers (specular, normals, and noise)
		specular.close();
		normals.close();
		noise.close();
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

	private static void setupAttributes(Pass pass) {
		// TODO: Properly add these attributes into the vertex format

		float blockId = -1.0F;

		setupAttribute(pass, "mc_Entity", 10, blockId, -1.0F, -1.0F, -1.0F);
		setupAttribute(pass, "mc_midTexCoord", 11, 0.0F, 0.0F, 0.0F, 0.0F);
		setupAttribute(pass, "at_tangent", 12, 1.0F, 0.0F, 0.0F, 1.0F);
	}

	private static void setupAttribute(Pass pass, String name, int expectedLocation, float v0, float v1, float v2, float v3) {
		int location = GL20.glGetAttribLocation(pass.getProgram().getProgramId(), name);

		if (location != -1) {
			if (location != expectedLocation) {
				throw new IllegalStateException();
			}

			GL20.glVertexAttrib4f(location, v0, v1, v2, v3);
		}
	}

	private void prepareRenderTargets() {
		// Make sure we're using texture unit 0 for this.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		clearMainBuffers.bind();
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		clearAltBuffers.bind();
		// Not clearing the depth buffer since there's only one of those and it was already cleared
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
	}

	@Override
	public void beginTranslucents() {
		isBeforeTranslucent = false;

		// We need to copy the current depth texture so that depthtex1 and depthtex2 can contain the depth values for
		// all non-translucent content, as required.
		baseline.bindAsReadBuffer();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
		GlStateManager._bindTexture(0);

		deferredRenderer.renderAll();
		Program.unbind();

		RenderSystem.enableBlend();

		MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
		MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();

		if (!programStack.isEmpty()) {
			GbufferProgram toUse = programStack.get(programStack.size() - 1);

			useProgram(toUse);
		} else {
			useProgram(GbufferProgram.NONE);
			baseline.bind();
		}
	}

	public static GbufferProgram getProgramForSheet(ParticleTextureSheet sheet) {
		if (sheet == ParticleTextureSheet.PARTICLE_SHEET_OPAQUE || sheet == ParticleTextureSheet.TERRAIN_SHEET || sheet == ParticleTextureSheet.CUSTOM) {
			return GbufferProgram.TEXTURED_LIT;
		} else if (sheet == ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT) {
			// TODO: Should we be using some other pass? (gbuffers_water?)
			return GbufferProgram.TEXTURED_LIT;
		} else {
			// sheet == ParticleTextureSheet.PARTICLE_SHEET_LIT
			//
			// Yes, this seems backwards. However, in this case, these particles are always bright regardless of the
			// lighting condition, and therefore don't use the textured_lit program.
			return GbufferProgram.TEXTURED;
		}
	}

	@Override
	public void renderShadows(WorldRendererAccessor worldRenderer, Camera playerCamera) {
		this.shadowMapRenderer.renderShadows(worldRenderer, playerCamera);
	}

	@Override
	public void addDebugText(List<String> messages) {
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

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;

	@Override
	public void beginWorldRendering() {
		isRenderingWorld = true;
		isBeforeTranslucent = true;

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
	public void finalizeWorldRendering() {
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
		programStackLog.clear();

		compositeRenderer.renderAll();
		finalPassRenderer.renderFinalPass();
	}

	@Override
	public SodiumTerrainPipeline getSodiumTerrainPipeline() {
		return sodiumTerrainPipeline;
	}

	private boolean isRenderingShadow = false;

	public void beginShadowRender() {
		isRenderingShadow = true;
	}

	public void endShadowRender() {
		isRenderingShadow = false;
	}

	public FrameUpdateNotifier getUpdateNotifier() {
		return updateNotifier;
	}
}
