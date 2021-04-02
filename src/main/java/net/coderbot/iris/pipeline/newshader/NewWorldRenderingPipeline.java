package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;

import java.io.IOException;
import java.util.Optional;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private final Shader skyBasic;
	private final Shader skyBasicColor;
	private final Shader skyTextured;
	private final Shader terrainSolid;
	private final Shader terrainCutout;
	private final Shader terrainCutoutMipped;
	private final Shader terrainTranslucent;
	private WorldRenderingPhase phase = WorldRenderingPhase.NOT_RENDERING_WORLD;

	public NewWorldRenderingPipeline(ProgramSet programSet) throws IOException {
		Optional<ProgramSource> skyTexturedSource = first(programSet.getGbuffersSkyTextured(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> skyBasicSource = first(programSet.getGbuffersSkyBasic(), programSet.getGbuffersBasic());

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		// TODO: Resolve hasColorAttrib based on the vertex format
		this.skyBasic = NewShaderTests.create("gbuffers_sky_basic", skyBasicSource.orElseThrow(RuntimeException::new), 0.0F, VertexFormats.POSITION, false);
		this.skyBasicColor = NewShaderTests.create("gbuffers_sky_basic_color", skyBasicSource.orElseThrow(RuntimeException::new), 0.0F, VertexFormats.POSITION_COLOR, true);
		this.skyTextured = NewShaderTests.create("gbuffers_sky_textured", skyTexturedSource.orElseThrow(RuntimeException::new), 0.0F, VertexFormats.POSITION_TEXTURE, false);
		this.terrainSolid = NewShaderTests.create("gbuffers_terrain_solid", terrainSource.orElseThrow(RuntimeException::new), 0.0F, IrisVertexFormats.TERRAIN, true);
		this.terrainCutout = NewShaderTests.create("gbuffers_terrain_cutout", terrainSource.orElseThrow(RuntimeException::new), 0.1F, IrisVertexFormats.TERRAIN, true);
		this.terrainCutoutMipped = NewShaderTests.create("gbuffers_terrain_cutout_mipped", terrainSource.orElseThrow(RuntimeException::new), 0.5F, IrisVertexFormats.TERRAIN, true);

		if (translucentSource != terrainSource) {
			this.terrainTranslucent = NewShaderTests.create("gbuffers_translucent", translucentSource.orElseThrow(RuntimeException::new), 0.0F, IrisVertexFormats.TERRAIN, true);
		} else {
			this.terrainTranslucent = this.terrainSolid;
		}
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
		skyBasic.close();
		skyBasicColor.close();
		skyTextured.close();

		terrainSolid.close();
		terrainCutout.close();
		terrainCutoutMipped.close();

		if (terrainTranslucent != terrainSolid) {
			terrainTranslucent.close();
		}
	}
}
