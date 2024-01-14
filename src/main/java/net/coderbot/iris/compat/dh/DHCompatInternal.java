package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CapturedRenderingState;

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
			solidProgram = null;

			shouldOverride = false;
		}

		if (translucentProgram != null) {
			translucentProgram.free();

			translucentProgram = null;
		}

		if (pipeline.getDHTerrainShader().isEmpty() && pipeline.getDHWaterShader().isEmpty()) {
			Iris.logger.warn("No DH shader found in this pack.");
			return;
		}

		ProgramSource terrain = pipeline.getDHTerrainShader().get();
		solidProgram = IrisLodRenderProgram.createProgram(terrain.getName(), terrain, pipeline.getCustomUniforms(), pipeline);

		if (pipeline.getDHWaterShader().isPresent()) {
			ProgramSource water = pipeline.getDHWaterShader().get();
			translucentProgram = IrisLodRenderProgram.createProgram(water.getName(), water, pipeline.getCustomUniforms(), pipeline);
			dhWaterFramebuffer = pipeline.createDHFramebuffer(water, true);
		}

		dhTerrainFramebuffer = pipeline.createDHFramebuffer(terrain, false);

		if (translucentProgram == null) {
			translucentProgram = solidProgram;
		}

		shouldOverride = true;
	}

	public void reconnectDHTextures(int depthTex) {
		if (storedDepthTex != depthTex && dhTerrainFramebuffer != null) {
			storedDepthTex = depthTex;
			dhTerrainFramebuffer.addDepthAttachment(depthTex);
			if (dhWaterFramebuffer != null) {
				dhWaterFramebuffer.addDepthAttachment(depthTex);
			}
		}
	}

	public void clear() {
		if (solidProgram != null) {
			solidProgram.free();
			solidProgram = null;
		}
		if (translucentProgram != null) {
			translucentProgram.free();
			translucentProgram = null;
		}
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
		if (translucentProgram == null) {
			return solidProgram;
		}
		return translucentProgram;
	}

	public int getStoredDepthTex() {
		return storedDepthTex;
	}

	public int getRenderDistance() {
		return RenderUtil.getFarClipPlaneDistanceInBlocks();
	}

	public float getFarPlane() {
		return (float)((double)(RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));
	}

	public float getNearPlane() {
		return RenderUtil.getNearClipPlaneDistanceInBlocks(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	public GlFramebuffer getTranslucentFB() {
		return dhWaterFramebuffer;
	}
}
