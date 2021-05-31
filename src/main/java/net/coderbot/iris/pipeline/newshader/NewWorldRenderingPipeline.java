package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NoiseTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.rendertarget.SingleColorTexture;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class NewWorldRenderingPipeline implements WorldRenderingPipeline, CoreWorldRenderingPipeline {
	private final RenderTargets renderTargets;

	private final Shader skyBasic;
	private final Shader skyBasicColor;
	private final Shader skyTextured;
	private final Shader terrainSolid;
	private final Shader terrainCutout;
	private final Shader terrainCutoutMipped;
	private final Shader terrainTranslucent;
	private WorldRenderingPhase phase = WorldRenderingPhase.NOT_RENDERING_WORLD;

	private final GlFramebuffer clearAltBuffers;
	private final GlFramebuffer clearMainBuffers;
	private final GlFramebuffer baseline;

	private final EmptyShadowMapRenderer shadowMapRenderer;
	private final CompositeRenderer compositeRenderer;
	private final SingleColorTexture normals;
	private final SingleColorTexture specular;
	private final AbstractTexture noise;

	private final int waterId;
	private final float sunPathRotation;

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

		this.renderTargets = new RenderTargets(MinecraftClient.getInstance().getFramebuffer(), programSet.getPackDirectives().getRenderTargetDirectives());
		this.waterId = programSet.getPack().getIdMap().getBlockProperties().getOrDefault(new Identifier("minecraft", "water"), -1);
		this.sunPathRotation = programSet.getPackDirectives().getSunPathRotation();

		// Don't clobber anything in texture unit 0. It probably won't cause issues, but we're just being cautious here.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE2);

		// Create some placeholder PBR textures for now
		normals = new SingleColorTexture(127, 127, 255, 255);
		specular = new SingleColorTexture(0, 0, 0, 0);

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

		this.shadowMapRenderer = new EmptyShadowMapRenderer(2048);

		Optional<ProgramSource> skyTexturedSource = first(programSet.getGbuffersSkyTextured(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> skyBasicSource = first(programSet.getGbuffersSkyBasic(), programSet.getGbuffersBasic());

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// Matches OptiFine's default for CUTOUT and CUTOUT_MIPPED.
		AlphaTest terrainCutoutAlpha = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);

		// TODO: Resolve hasColorAttrib based on the vertex format
		this.skyBasic = createShader("gbuffers_sky_basic", skyBasicSource.orElseThrow(RuntimeException::new), AlphaTest.ALWAYS, VertexFormats.POSITION, false);
		this.skyBasicColor = createShader("gbuffers_sky_basic_color", skyBasicSource.orElseThrow(RuntimeException::new), AlphaTest.ALWAYS, VertexFormats.POSITION_COLOR, true);
		this.skyTextured = createShader("gbuffers_sky_textured", skyTexturedSource.orElseThrow(RuntimeException::new), AlphaTest.ALWAYS, VertexFormats.POSITION_TEXTURE, false);
		this.terrainSolid = createShader("gbuffers_terrain_solid", terrainSource.orElseThrow(RuntimeException::new), AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, true);
		this.terrainCutout = createShader("gbuffers_terrain_cutout", terrainSource.orElseThrow(RuntimeException::new), terrainCutoutAlpha, IrisVertexFormats.TERRAIN, true);
		this.terrainCutoutMipped = createShader("gbuffers_terrain_cutout_mipped", terrainSource.orElseThrow(RuntimeException::new), terrainCutoutAlpha, IrisVertexFormats.TERRAIN, true);

		if (translucentSource != terrainSource) {
			this.terrainTranslucent = createShader("gbuffers_translucent", translucentSource.orElseThrow(RuntimeException::new), AlphaTest.ALWAYS, IrisVertexFormats.TERRAIN, true);
		} else {
			this.terrainTranslucent = this.terrainSolid;
		}

		int[] buffersToBeCleared = programSet.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared().toIntArray();

		this.clearAltBuffers = renderTargets.createFramebufferWritingToAlt(buffersToBeCleared);
		this.clearMainBuffers = renderTargets.createFramebufferWritingToMain(buffersToBeCleared);

		this.compositeRenderer = new CompositeRenderer(programSet, renderTargets, shadowMapRenderer, noise);
	}

	private Shader createShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, boolean hasColorAttrib) throws IOException {
		ExtendedShader extendedShader = NewShaderTests.create(name, source, renderTargets, baseline, fallbackAlpha, vertexFormat, hasColorAttrib);

		// TODO: waterShadowEnabled?

		extendedShader.addIrisSampler("normals", this.normals.getTextureId());
		extendedShader.addIrisSampler("specular", this.specular.getTextureId());
		extendedShader.addIrisSampler("shadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("watershadow", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex0", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("shadowtex1", this.shadowMapRenderer.getDepthTextureId());
		extendedShader.addIrisSampler("depthtex0", this.renderTargets.getDepthTexture().getTextureId());
		extendedShader.addIrisSampler("depthtex1", this.renderTargets.getDepthTextureNoTranslucents().getTextureId());
		extendedShader.addIrisSampler("noisetex", this.noise.getGlId());

		// TODO: Shadowcolor

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
	}

	@Override
	public void beginTranslucents() {
		// We need to copy the current depth texture so that depthtex1 and depthtex2 can contain the depth values for
		// all non-translucent content, as required.
		baseline.bind();
		GlStateManager._bindTexture(renderTargets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight(), 0);
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

	@Override
	public float getSunPathRotation() {
		return sunPathRotation;
	}
}
