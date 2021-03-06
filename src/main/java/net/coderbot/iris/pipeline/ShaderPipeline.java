package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.*;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTestOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.rendertarget.BuiltinNoiseTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
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

/**
 * Encapsulates the compiled shader program objects for the currently loaded shaderpack.
 */
public class ShaderPipeline {
	private final RenderTargets renderTargets;
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

	private final CompositeRenderer compositeRenderer;

	private final int waterId;

	private static final List<GbufferProgram> programStack = new ArrayList<>();
	private static final List<String> programStackLog = new ArrayList<>();

	public ShaderPipeline(ProgramSet programs) {
		Objects.requireNonNull(programs);

		this.renderTargets = new RenderTargets(MinecraftClient.getInstance().getFramebuffer(), programs.getPackDirectives());
		this.waterId = programs.getPack().getIdMap().getBlockProperties().getOrDefault(new Identifier("minecraft", "water"), -1);

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

		int[] buffersToBeCleared = programs.getPackDirectives().getBuffersToBeCleared().toIntArray();

		this.clearAltBuffers = renderTargets.createFramebufferWritingToAlt(buffersToBeCleared);
		this.clearMainBuffers = renderTargets.createFramebufferWritingToMain(buffersToBeCleared);
		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		this.compositeRenderer = new CompositeRenderer(programs, renderTargets);
	}

	public void pushProgram(GbufferProgram program) {
		if (!isRenderingWorld) {
			// don't mess with non-world rendering
			return;
		}

		programStack.add(program);
		useProgram(program);
		programStackLog.add("push:" + program);
	}

	public void popProgram(GbufferProgram expected) {
		if (!isRenderingWorld) {
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
			GlProgramManager.useProgram(0);
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
				setupAttribute(translucent, "mc_Entity", waterId, -1.0F, -1.0F, -1.0F);
			}
		}

		if (program != GbufferProgram.TRANSLUCENT_TERRAIN && pass != null && pass == translucent) {
			// Make sure that other stuff sharing the same program isn't rendered like water
			setupAttribute(translucent, "mc_Entity", -1.0F, -1.0F, -1.0F, -1.0F);
		}
	}

	private void teardownProgram() {
		GlProgramManager.useProgram(0);
		this.baseline.bind();
	}

	public boolean shouldDisableVanillaEntityShadows() {
		// TODO: Don't hardcode this for Sildur's
		// OptiFine seems to disable vanilla shadows when the shaderpack uses shadow mapping?
		return true;
	}

	private void beginPass(Pass pass) {
		if (pass != null) {
			pass.use();
		} else {
			GlProgramManager.useProgram(0);
			this.baseline.bind();
		}
	}

	private Pass createPass(ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null),
				source.getFragmentSource().orElse(null));
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap());
		GlFramebuffer framebuffer = renderTargets.createFramebufferWritingToMain(source.getDirectives().getDrawBuffers());

		builder.bindAttributeLocation(10, "mc_Entity");
		builder.bindAttributeLocation(11, "mc_midTexCoord");
		builder.bindAttributeLocation(12, "at_tangent");

		AlphaTestOverride alphaTestOverride = source.getDirectives().getAlphaTestOverride().orElse(null);

		if (alphaTestOverride != null) {
			Iris.logger.info("Configured alpha test override for " + source.getName() + ": " + alphaTestOverride);
		}

		return new Pass(builder.build(), framebuffer, alphaTestOverride, source.getDirectives().shouldDisableBlend());
	}

	private final class Pass {
		private final Program program;
		private final GlFramebuffer framebuffer;
		private final AlphaTestOverride alphaTestOverride;
		private final boolean disableBlend;

		private Pass(Program program, GlFramebuffer framebuffer, AlphaTestOverride alphaTestOverride, boolean disableBlend) {
			this.program = program;
			this.framebuffer = framebuffer;
			this.alphaTestOverride = alphaTestOverride;
			this.disableBlend = disableBlend;
		}

		public void use() {
			// TODO: Binding the texture here is ugly and hacky. It would be better to have a utility function to set up
			// a given program and bind the required textures instead.
			GlStateManager.activeTexture(GL15C.GL_TEXTURE15);
			BuiltinNoiseTexture.bind();
			GlStateManager.activeTexture(GL15C.GL_TEXTURE0);
			framebuffer.bind();
			program.use();

			// TODO: Render layers will likely override alpha testing and blend state, perhaps we need a way to override
			// that.
			if (alphaTestOverride != null) {
				alphaTestOverride.setup();
			}

			if (disableBlend) {
				GlStateManager.disableBlend();
			}
		}

		public void stopUsing() {
			if (alphaTestOverride != null) {
				AlphaTestOverride.teardown();
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
		destroyPasses(basic, textured, texturedLit, skyBasic, skyTextured, clouds, terrain, translucent, weather);

		// Destroy the composite rendering pipeline
		//
		// This destroys all of the loaded composite programs as well.
		compositeRenderer.destroy();

		// Destroy our render targets
		//
		// While it's possible to just clear them instead and reuse them, we'd need to investigate whether or not this
		// would help performance.
		renderTargets.destroy();
	}

	private static void destroyPasses(Pass... passes) {
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

		setupAttribute(pass, "mc_Entity", blockId, -1.0F, -1.0F, -1.0F);
		setupAttribute(pass, "mc_midTexCoord", 0.0F, 0.0F, 0.0F, 0.0F);
		setupAttribute(pass, "at_tangent", 1.0F, 0.0F, 0.0F, 1.0F);
	}

	private static void setupAttribute(Pass pass, String name, float v0, float v1, float v2, float v3) {
		int location = GL20.glGetAttribLocation(pass.getProgram().getProgramId(), name);

		if (location != -1) {
			GL20.glVertexAttrib4f(location, v0, v1, v2, v3);
		}
	}

	public void prepareRenderTargets() {
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

		// We only want the vanilla clear color to be applied to colortex0
		baseline.bind();
	}

	public void copyCurrentDepthTexture() {
		baseline.bind();
		GlStateManager.bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
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

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;

	public void beginWorldRender() {
		isRenderingWorld = true;

		if (!programStack.isEmpty()) {
			throw new IllegalStateException("Program stack before the start of rendering, something has gone very wrong!");
		}

		// Default to rendering with BASIC for all unknown content.
		// This probably isn't the best approach, but it works for now.
		pushProgram(GbufferProgram.BASIC);
	}

	public void finalizeWorldRendering() {
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
	}
}
