package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.minecraft.client.render.Shader;

import java.io.IOException;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private final Shader textured;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		this.textured = NewShaderTests.test(programSet);
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
		return textured;
	}

	@Override
	public Shader getTerrainCutout() {
		return textured;
	}

	@Override
	public Shader getTerrainCutoutMipped() {
		return textured;
	}

	@Override
	public Shader getTranslucent() {
		return textured;
	}

	@Override
	public void destroy() {
		// NB: If you forget this, shader reloads won't work!
		textured.close();
	}
}
