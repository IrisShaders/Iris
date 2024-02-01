package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20C;

public class DHCompatInternal
{
	public static DHCompatInternal INSTANCE = new DHCompatInternal();
	public boolean shouldOverrideShadow;

    private IrisLodRenderProgram solidProgram;
	private IrisLodRenderProgram translucentProgram;
	private IrisLodRenderProgram shadowProgram;
	private GlFramebuffer dhTerrainFramebuffer;
	private DhFrameBufferWrapper dhTerrainFramebufferWrapper;
	private GlFramebuffer dhWaterFramebuffer;
	private GlFramebuffer dhShadowFramebuffer;
	private DhFrameBufferWrapper dhShadowFramebufferWrapper;
	private DepthTexture depthTexNoTranslucent;
	private boolean translucentDepthDirty;

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
		solidProgram = IrisLodRenderProgram.createProgram(terrain.getName(), false, false, terrain, pipeline.getCustomUniforms(), pipeline);

		if (pipeline.getDHWaterShader().isPresent()) {
			ProgramSource water = pipeline.getDHWaterShader().get();
			translucentProgram = IrisLodRenderProgram.createProgram(water.getName(), false, true, water, pipeline.getCustomUniforms(), pipeline);
			dhWaterFramebuffer = pipeline.createDHFramebuffer(water, true);
		}

		if (pipeline.getDHShadowShader().isPresent() && dhShadowEnabled) {
			ProgramSource shadow = pipeline.getDHShadowShader().get();
			shadowProgram = IrisLodRenderProgram.createProgram(shadow.getName(), true, false, shadow, pipeline.getCustomUniforms(), pipeline);
			if (pipeline.hasShadowRenderTargets()) {
				dhShadowFramebuffer = pipeline.createDHFramebufferShadow(shadow);
				dhShadowFramebufferWrapper = new DhFrameBufferWrapper(dhShadowFramebuffer);
			}
			shouldOverrideShadow = true;
		} else {
			shouldOverrideShadow = false;
		}

		dhTerrainFramebuffer = pipeline.createDHFramebuffer(terrain, false);
		dhTerrainFramebufferWrapper = new DhFrameBufferWrapper(dhTerrainFramebuffer);

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

		translucentDepthDirty = true;

		depthTexNoTranslucent = new DepthTexture(width, height, DepthBufferFormat.DEPTH32F);
	}

	public void renderShadowSolid() {
		// FIXME doesn't appear to do anything
		//ClientApi.INSTANCE.renderLods(ClientLevelWrapper.getWrapper(Minecraft.getInstance().level),
		//	McObjectConverter.Convert(ShadowRenderer.MODELVIEW),
		//	McObjectConverter.Convert(ShadowRenderer.PROJECTION),
		//	CapturedRenderingState.INSTANCE.getTickDelta());
	}

	public void renderShadowTranslucent() {
		// FIXME doesn't appear to do anything
		//ClientApi.INSTANCE.renderDeferredLods(ClientLevelWrapper.getWrapper(Minecraft.getInstance().level),
		//	McObjectConverter.Convert(ShadowRenderer.MODELVIEW),
		//	McObjectConverter.Convert(ShadowRenderer.PROJECTION),
		//	CapturedRenderingState.INSTANCE.getTickDelta());
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
		translucentDepthDirty = true;

		OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, dhTerrainFramebufferWrapper);
		OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, dhShadowFramebufferWrapper);
		dhTerrainFramebufferWrapper = null;
		dhShadowFramebufferWrapper = null;
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
	public DhFrameBufferWrapper getSolidFBWrapper() {
		return dhTerrainFramebufferWrapper;
	}

	public IrisLodRenderProgram getShadowShader() {
		return shadowProgram;
	}

	public GlFramebuffer getShadowFB() {
		return dhShadowFramebuffer;
	}
	public DhFrameBufferWrapper getShadowFBWrapper() {
		return dhShadowFramebufferWrapper;
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
		return getDhBlockRenderDistance();
	}
	public static int getDhBlockRenderDistance() {
		if (DhApi.Delayed.configs == null)
		{
			// Called before DH has finished setup
			return 0;
		}

		return DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16;
	}

	public void copyTranslucents(int width, int height) {
		if (translucentDepthDirty) {
			translucentDepthDirty = false;
			RenderSystem.bindTexture(depthTexNoTranslucent.getTextureId());
			dhTerrainFramebuffer.bindAsReadBuffer();
			IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, DepthBufferFormat.DEPTH32F.getGlInternalFormat(), 0, 0, width, height, 0);
		} else {
			DepthCopyStrategy.fastest(false).copy(dhTerrainFramebuffer, storedDepthTex, null, depthTexNoTranslucent.getTextureId(), width, height);
		}
	}

	public float getFarPlane() {
		return (float)((double)(getDhBlockRenderDistance() + 512) * Math.sqrt(2.0));
	}

	public float getNearPlane() {
		return DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	public GlFramebuffer getTranslucentFB() {
		return dhWaterFramebuffer;
	}

	public int getDepthTexNoTranslucent() {
		if (depthTexNoTranslucent == null) return 0;

		return depthTexNoTranslucent.getTextureId();
	}

}
