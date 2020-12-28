package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.RenderLayer;

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
	private final Pass weather;

	private final GlFramebuffer clearAltBuffers;
	private final GlFramebuffer clearMainBuffers;
	private final GlFramebuffer baseline;

	public ShaderPipeline(ShaderPack pack, RenderTargets renderTargets) {
		this.renderTargets = renderTargets;

		this.basic = pack.getGbuffersBasic().map(this::createPass).orElse(null);
		this.textured = pack.getGbuffersTextured().map(this::createPass).orElse(basic);
		// TODO: Load textured_lit program
		this.texturedLit = textured;
		this.skyBasic = pack.getGbuffersSkyBasic().map(this::createPass).orElse(basic);
		this.skyTextured = pack.getGbuffersSkyTextured().map(this::createPass).orElse(textured);
		this.clouds = pack.getGbuffersClouds().map(this::createPass).orElse(textured);
		this.terrain = pack.getGbuffersTerrain().map(this::createPass).orElse(texturedLit);
		// TODO: Load water, weather shaders
		this.translucent = terrain;
		this.weather = texturedLit;

		int[] buffersToBeCleared = pack.getPackDirectives().getBuffersToBeCleared().toIntArray();

		this.clearAltBuffers = renderTargets.createFramebufferWritingToAlt(buffersToBeCleared);
		this.clearMainBuffers = renderTargets.createFramebufferWritingToMain(buffersToBeCleared);
		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});
	}

	private Pass createPass(ShaderPack.ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null),
				source.getFragmentSource().orElse(null));
		} catch (IOException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getIdMap());
		GlFramebuffer framebuffer = renderTargets.createFramebufferWritingToMain(source.getDirectives().getDrawBuffers());

		return new Pass(builder.build(), framebuffer);
	}
	
	private static final class Pass {
		private final Program program;
		private final GlFramebuffer framebuffer;

		private Pass(Program program, GlFramebuffer framebuffer) {
			this.program = program;
			this.framebuffer = framebuffer;
		}
		
		public void use() {
			framebuffer.bind();
			program.use();
		}

		public Program getProgram() {
			return program;
		}
	}

	private void end() {
		if (this.basic == null) {
			GlProgramManager.useProgram(0);
			this.baseline.bind();

			return;
		}

		this.basic.use();
	}

	private static void setupAttributes(Pass pass) {
		// TODO: Properly add these attributes into the vertex format

		int mcEntity = GL20.glGetAttribLocation(pass.getProgram().getProgramId(), "mc_Entity");

		if (mcEntity != -1) {
			float blockId = -1.0F;

			GL20.glVertexAttrib4f(mcEntity, blockId, -1.0F, -1.0F, -1.0F);
		}
	}

	public void prepareRenderTargets() {
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		clearMainBuffers.bind();
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		clearAltBuffers.bind();
		// Not clearing the depth buffer since there's only one of those and it was already cleared
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		clearMainBuffers.bind();
	}

	public void beginClouds() {
		if (clouds == null) {
			return;
		}

		clouds.use();
	}

	public void endClouds() {
		end();
	}

	public void beginTerrainLayer(RenderLayer terrainLayer) {
		if (terrainLayer == RenderLayer.getTranslucent() || terrainLayer == RenderLayer.getTripwire()) {
			if (translucent == null) {
				return;
			}

			translucent.use();
			setupAttributes(translucent);
		} else if (terrainLayer == RenderLayer.getSolid() || terrainLayer == RenderLayer.getCutout() || terrainLayer == RenderLayer.getCutoutMipped()) {
			if (terrain == null) {
				return;
			}

			terrain.use();
			setupAttributes(terrain);
		}
	}

	public void endTerrainLayer(RenderLayer terrainLayer) {
		end();
	}

	public void beginSky() {
		if (skyBasic == null) {
			return;
		}

		skyBasic.use();
	}

	public void beginTexturedSky() {
		if (skyTextured == null) {
			return;
		}

		skyTextured.use();
	}

	public void endTexturedSky() {
		if (skyBasic == null) {
			endSky();
		} else {
			skyBasic.use();
		}
	}

	public void endSky() {
		end();
	}

	public void beginWeather() {
		if (weather == null) {
			return;
		}

		weather.use();
	}

	public void endWeather() {
		end();
	}

	public void beginWorldBorder() {
		if (texturedLit == null) {
			return;
		}

		texturedLit.use();
	}

	public void endWorldBorder() {
		end();
	}

	public void beginImmediateDrawing(RenderLayer layer) {
		if (!isRenderingWorld) {
			// don't mess with non-world rendering
			return;
		}

		if (texturedLit == null) {
			return;
		}

		texturedLit.use();
		if ((layer.isOutline() || layer == RenderLayer.getLines()) && basic != null) {
			basic.use();
		}
	}

	public void endImmediateDrawing() {
		if (!isRenderingWorld) {
			// don't mess with non-world rendering
			return;
		}

		end();
	}

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;

	public void beginWorldRender() {
		isRenderingWorld = true;
	}

	public void endWorldRender() {
		GlProgramManager.useProgram(0);
		isRenderingWorld = false;
	}
}
