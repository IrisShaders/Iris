package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import loaderCommon.fabric.com.seibel.distanthorizons.common.wrappers.McObjectConverter;
import loaderCommon.fabric.com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;

public class DHCompatInternal {
	public static DHCompatInternal INSTANCE = new DHCompatInternal();
	public boolean shouldOverrideShadow;

    private IrisLodRenderProgram solidProgram;
	private IrisLodRenderProgram translucentProgram;
	private IrisLodRenderProgram shadowProgram;
	private GlFramebuffer dhTerrainFramebuffer;
	private GlFramebuffer dhWaterFramebuffer;
	private GlFramebuffer dhShadowFramebuffer;
	private DepthTexture depthTexNoTranslucent;

	private int storedDepthTex;
	public boolean shouldOverride;

	public void prepareNewPipeline(NewWorldRenderingPipeline pipeline, boolean dhShadowEnabled) {
		if (solidProgram != null) {
			solidProgram.free();
			solidProgram = null;

			shouldOverride = false;
		}

		if (translucentProgram != null) {
			translucentProgram.free();

			translucentProgram = null;
		}

		if (shadowProgram != null) {
			shadowProgram.free();

			shadowProgram = null;
		}

		if (pipeline.getDHTerrainShader().isEmpty() && pipeline.getDHWaterShader().isEmpty()) {
			Iris.logger.warn("No DH shader found in this pack.");
			return;
		}

		ProgramSource terrain = pipeline.getDHTerrainShader().get();
		solidProgram = IrisLodRenderProgram.createProgram(terrain.getName(), false, terrain, pipeline.getCustomUniforms(), pipeline);

		if (pipeline.getDHWaterShader().isPresent()) {
			ProgramSource water = pipeline.getDHWaterShader().get();
			translucentProgram = IrisLodRenderProgram.createProgram(water.getName(), false, water, pipeline.getCustomUniforms(), pipeline);
			dhWaterFramebuffer = pipeline.createDHFramebuffer(water, true);
		}

		if (pipeline.getDHShadowShader().isPresent() && dhShadowEnabled) {
			ProgramSource shadow = pipeline.getDHShadowShader().get();
			shadowProgram = IrisLodRenderProgram.createProgram(shadow.getName(), true, shadow, pipeline.getCustomUniforms(), pipeline);
			dhShadowFramebuffer = pipeline.createDHFramebufferShadow(shadow);
			shouldOverrideShadow = true;
		} else {
			shouldOverrideShadow = false;
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

	public void createDepthTex(int width, int height) {
		if (depthTexNoTranslucent != null) {
			depthTexNoTranslucent.destroy();
			depthTexNoTranslucent = null;
		}

		depthTexNoTranslucent = new DepthTexture(width, height, DepthBufferFormat.DEPTH32F);
	}

	public void renderShadowSolid() {
		ClientApi.INSTANCE.renderLods(ClientLevelWrapper.getWrapper(Minecraft.getInstance().level),
			McObjectConverter.Convert(ShadowRenderer.MODELVIEW),
			McObjectConverter.Convert(ShadowRenderer.PROJECTION),
			CapturedRenderingState.INSTANCE.getTickDelta());
	}

	public void renderShadowTranslucent() {
		ClientApi.INSTANCE.renderTranslucentLods(ClientLevelWrapper.getWrapper(Minecraft.getInstance().level),
			McObjectConverter.Convert(ShadowRenderer.MODELVIEW),
			McObjectConverter.Convert(ShadowRenderer.PROJECTION),
			CapturedRenderingState.INSTANCE.getTickDelta());
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
		if (shadowProgram != null) {
			shadowProgram.free();
			shadowProgram = null;
		}
		shouldOverrideShadow = false;
		shouldOverride = false;
		dhTerrainFramebuffer = null;
		dhWaterFramebuffer = null;
		dhShadowFramebuffer = null;
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

	public IrisLodRenderProgram getShadowShader() {
		return shadowProgram;
	}

	public GlFramebuffer getShadowFB() {
		return dhShadowFramebuffer;
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

	public int getDepthTexNoTranslucent() {
		if (depthTexNoTranslucent == null) return 0;

		return depthTexNoTranslucent.getTextureId();
	}
}
