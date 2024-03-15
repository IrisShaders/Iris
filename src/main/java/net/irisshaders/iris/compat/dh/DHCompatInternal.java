package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.gl.texture.DepthCopyStrategy;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.targets.DepthTexture;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20C;

import java.io.IOException;

public class DHCompatInternal {
	public static final DHCompatInternal SHADERLESS = new DHCompatInternal(null, false);
	static boolean dhEnabled;
	private final IrisRenderingPipeline pipeline;
	public boolean shouldOverrideShadow;
	public boolean shouldOverride;
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
	private boolean incompatible = false;

	public DHCompatInternal(IrisRenderingPipeline pipeline, boolean dhShadowEnabled) {
		this.pipeline = pipeline;

		if (pipeline == null || !DhApi.Delayed.configs.graphics().renderingEnabled().getValue()) {
			return;
		}

		if (pipeline.getDHTerrainShader().isEmpty() && pipeline.getDHWaterShader().isEmpty()) {
			Iris.logger.warn("No DH shader found in this pack.");
			incompatible = true;
			return;
		}

		createDepthTex(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height);
		translucentDepthDirty = true;

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

	public static int getDhBlockRenderDistance() {
		if (DhApi.Delayed.configs == null) {
			// Called before DH has finished setup
			return 0;
		}

		return DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16;
	}

	public static int getRenderDistance() {
		return getDhBlockRenderDistance();
	}

	public static float getFarPlane() {
		if (DhApi.Delayed.configs == null) {
			// Called before DH has finished setup
			return 0;
		}

		int lodChunkDist = DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue();
		int lodBlockDist = lodChunkDist * 16;
		// sqrt 2 to prevent the corners from being cut off
		return (float) ((lodBlockDist + 512) * Math.sqrt(2));
	}

	public static float getNearPlane() {
		if (DhApi.Delayed.renderProxy == null) {
			// Called before DH has finished setup
			return 0;
		}

		return DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(CapturedRenderingState.INSTANCE.getRealTickDelta());
	}

	public static boolean checkFrame() {
		if (dhEnabled != DhApi.Delayed.configs.graphics().renderingEnabled().getValue() && IrisApi.getInstance().isShaderPackInUse()) {
			dhEnabled = DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
			try {
				Iris.reload();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return dhEnabled;
	}

	public boolean incompatiblePack() {
		return incompatible;
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

		depthTexNoTranslucent = new DepthTexture("DH depth tex", width, height, DepthBufferFormat.DEPTH32F);
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

	public GlFramebuffer getTranslucentFB() {
		return dhWaterFramebuffer;
	}

	public int getDepthTexNoTranslucent() {
		if (depthTexNoTranslucent == null) return 0;

		return depthTexNoTranslucent.getTextureId();
	}
}
