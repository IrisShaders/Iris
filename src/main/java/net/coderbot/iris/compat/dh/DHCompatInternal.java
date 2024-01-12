package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSource;

public class DHCompatInternal {
	public static DHCompatInternal INSTANCE = new DHCompatInternal();

	private IrisLodRenderProgram solidProgram;
	private IrisLodRenderProgram translucentProgram;
	private GlFramebuffer dhTerrainFramebuffer;
	private GlFramebuffer dhWaterFramebuffer;

	private int storedDepthTex;
	public boolean shouldOverride;

	public void prepareNewPipeline(NewWorldRenderingPipeline pipeline) {
		if (solidProgram != null) {
			solidProgram.free();
			translucentProgram.free();
			solidProgram = null;
			translucentProgram = null;
			shouldOverride = false;
		}


		if (pipeline.getDHTerrainShader().isEmpty() && pipeline.getDHWaterShader().isEmpty()) {
			Iris.logger.warn("No DH shader found in this pack.");
			return;
		}

		ProgramSource terrain = pipeline.getDHTerrainShader().get();
		solidProgram = IrisLodRenderProgram.createProgram(terrain.getName(), terrain, pipeline.getCustomUniforms(), pipeline);

		ProgramSource water = pipeline.getDHWaterShader().get();
		translucentProgram = IrisLodRenderProgram.createProgram(water.getName(), water, pipeline.getCustomUniforms(), pipeline);

		dhTerrainFramebuffer = pipeline.createDHFramebuffer(terrain);
		dhWaterFramebuffer = pipeline.createDHFramebuffer(water);

		shouldOverride = true;
	}

	public void reconnectDHTextures(int depthTex) {
		if (dhTerrainFramebuffer != null) {
			dhTerrainFramebuffer.addDepthAttachment(depthTex);
			dhWaterFramebuffer.addDepthAttachment(depthTex);
		}
	}

	public void clear() {
		solidProgram.free();
		solidProgram = null;
		translucentProgram.free();
		translucentProgram = null;
		shouldOverride = false;
		dhTerrainFramebuffer = null;
		dhWaterFramebuffer = null;
		storedDepthTex = -1;
	}

	public void setModelPos(Vec3f modelPos) {
		solidProgram.bind();
		solidProgram.setModelPos(modelPos);
		translucentProgram.bind();
		translucentProgram.setModelPos(modelPos);
		solidProgram.bind();
	}

	public IrisLodRenderProgram getSolidShader() {
		return solidProgram;
	}

	public GlFramebuffer getSolidFB() {
		return dhTerrainFramebuffer;
	}

	public IrisLodRenderProgram getTranslucentShader() {
		return translucentProgram;
	}

	public GlFramebuffer getTranslucentFB() {
		return dhWaterFramebuffer;
	}
}
