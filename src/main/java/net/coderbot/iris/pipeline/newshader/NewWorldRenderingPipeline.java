package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.minecraft.client.render.Shader;

import java.io.IOException;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private final Shader terrainSolid;
	private final Shader terrainCutout;
	private final Shader terrainCutoutMipped;
	private final Shader terrainTranslucent;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		ProgramSource source = programSet.getGbuffersTextured().flatMap(ProgramSource::requireValid).orElseGet(() -> programSet.getGbuffersBasic().flatMap(ProgramSource::requireValid).orElseThrow(RuntimeException::new));

		this.terrainSolid = NewShaderTests.create("gbuffers_textured_solid", source, 0.0F);
		this.terrainCutout = NewShaderTests.create("gbuffers_textured_cutout", source, 0.1F);
		this.terrainCutoutMipped = NewShaderTests.create("gbuffers_textured_cutout_mipped", source, 0.5F);
		// TODO: Once this isn't the same as terrain, don't forget about destroying it.
		this.terrainTranslucent = terrainSolid;
	}

	@Override
	public void beginWorldRendering() {

	}

	@Override
	public void beginTranslucents() {

	}

	@Override
	public void pushProgram(GbufferProgram program) {

	}

	@Override
	public void popProgram(GbufferProgram program) {

	}

	@Override
	public void finalizeWorldRendering() {

	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		return true;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return true;
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
	public Shader getTranslucent() {
		return terrainTranslucent;
	}

	@Override
	public void destroy() {
		// NB: If you forget this, shader reloads won't work!
		terrainSolid.close();
		terrainCutout.close();
		terrainCutoutMipped.close();
		// TODO: Don't forget about translucent when it actually differs from terrain...
	}
}
