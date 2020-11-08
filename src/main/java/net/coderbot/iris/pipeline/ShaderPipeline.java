package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.Objects;

import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.RenderLayer;

/**
 * Encapsulates the compiled shader program objects for the currently loaded shaderpack.
 */
public class ShaderPipeline {
	private final Program clouds;
	private final Program terrain;
	private final Program translucent;

	public ShaderPipeline(ShaderPack pack) {
		Program textured = createProgram(pack.getGbuffersTextured());

		this.clouds = pack.getGbuffersClouds().map(ShaderPipeline::createProgram).orElse(textured);
		this.terrain = textured;
		this.translucent = textured;
	}

	private static Program createProgram(ShaderPack.ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin("gbuffers_textured", source.getVertexSource().orElse(null),
					source.getFragmentSource().orElse(null));
		} catch (IOException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder);

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
		clouds.use();
	}

	public void endClouds() {
		GlProgramManager.useProgram(0);
	}

	public void beginTerrainLayer(RenderLayer terrainLayer) {
		if (terrainLayer == RenderLayer.getTranslucent() || terrainLayer == RenderLayer.getTripwire()) {
			translucent.use();
			setupAttributes(translucent);
		} else if (terrainLayer == RenderLayer.getSolid() || terrainLayer == RenderLayer.getCutout() || terrainLayer == RenderLayer.getCutoutMipped()) {
			terrain.use();
			setupAttributes(terrain);
		}
	}

	public void endTerrainLayer(RenderLayer terrainLayer) {
		GlProgramManager.useProgram(0);
	}
}
