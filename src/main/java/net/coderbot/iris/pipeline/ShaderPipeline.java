package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.Objects;

import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.RenderLayer;

/**
 * Encapsulates the compiled shader program objects for the currently loaded shaderpack.
 */
public class ShaderPipeline {
	@Nullable
	private final Program basic;
	@Nullable
	private final Program textured;
	@Nullable
	private final Program texturedLit;
	@Nullable
	private final Program skyBasic;
	@Nullable
	private final Program skyTextured;
	@Nullable
	private final Program clouds;
	@Nullable
	private final Program terrain;
	@Nullable
	private final Program translucent;
	@Nullable
	private final Program weather;

	public ShaderPipeline(ShaderPack pack) {
		this.basic = pack.getGbuffersBasic().map(ShaderPipeline::createProgram).orElse(null);
		this.textured = pack.getGbuffersTextured().map(ShaderPipeline::createProgram).orElse(basic);
		// TODO: Load textured_lit program
		this.texturedLit = textured;
		this.skyBasic = pack.getGbuffersSkyBasic().map(ShaderPipeline::createProgram).orElse(basic);
		this.skyTextured = pack.getGbuffersSkyTextured().map(ShaderPipeline::createProgram).orElse(textured);
		this.clouds = pack.getGbuffersClouds().map(ShaderPipeline::createProgram).orElse(textured);
		// TODO: Load terrain, water, weather shaders
		this.terrain = texturedLit;
		this.translucent = terrain;
		this.weather = texturedLit;
	}

	private static Program createProgram(ShaderPack.ProgramSource source) {
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

		CommonUniforms.addCommonUniforms(builder, source.getParent().getIdMapParser());

		return builder.build();
	}

	private static void setupAttributes(Program program) {
		// TODO: Properly add these attributes into the vertex format

		int mcEntity = GL20.glGetAttribLocation(program.getProgramId(), "mc_Entity");

		if (mcEntity != -1) {
			float blockId = -1.0F;

			GL20.glVertexAttrib4f(mcEntity, blockId, -1.0F, -1.0F, -1.0F);
		}
	}

	public void beginClouds() {
		if (clouds == null) {
			return;
		}

		clouds.use();
	}

	public void endClouds() {
		GlProgramManager.useProgram(0);
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
		GlProgramManager.useProgram(0);
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
		GlProgramManager.useProgram(0);
	}

	public void beginWeather() {
		if (weather == null) {
			return;
		}

		weather.use();
	}

	public void endWeather() {
		GlProgramManager.useProgram(0);
	}

	public void beginWorldBorder() {
		if (texturedLit == null) {
			return;
		}

		texturedLit.use();
	}

	public void endWorldBorder() {
		GlProgramManager.useProgram(0);
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
		if ((layer.isOutline() || layer == RenderLayer.getLines()) && basic != null){
			basic.use();
		}
	}

	public void endImmediateDrawing() {
		if (!isRenderingWorld) {
			// don't mess with non-world rendering
			return;
		}

		GlProgramManager.useProgram(0);
	}

	// TODO: better way to avoid this global state?
	private boolean isRenderingWorld = false;

	public void beginWorldRender() {
		isRenderingWorld = true;
	}

	public void endWorldRender() {
		isRenderingWorld = false;
	}
}
