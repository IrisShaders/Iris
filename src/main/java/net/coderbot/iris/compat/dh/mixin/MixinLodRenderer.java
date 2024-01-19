package net.coderbot.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.pos.DhBlockPos;
import com.seibel.distanthorizons.core.render.RenderBufferHandler;
import com.seibel.distanthorizons.core.render.glObject.GLProxy;
import com.seibel.distanthorizons.core.render.glObject.texture.DHDepthTexture;
import com.seibel.distanthorizons.core.render.glObject.texture.DhFramebuffer;
import com.seibel.distanthorizons.core.render.renderer.LodRenderProgram;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftClientWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import com.seibel.distanthorizons.coreapi.util.math.Mat4f;
import com.seibel.distanthorizons.coreapi.util.math.Vec3d;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.dh.DHCompatInternal;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(value = LodRenderer.class, remap = false)
public class MixinLodRenderer {
	@Shadow
	@Final
	private static IMinecraftRenderWrapper MC_RENDER;

	@Shadow
	@Final
	private static IMinecraftClientWrapper MC;

	@Shadow
	private boolean deferWaterRendering;
	@Shadow
	private DHDepthTexture depthTexture;
	@Shadow
	private int cachedWidth;
	@Shadow
	private int cachedHeight;
	@Unique
	private boolean atTranslucent;

	@Unique
	private int frame;

	@Inject(method = "drawLODs", at = @At("TAIL"))
	private void setDeferred(IClientLevelWrapper clientLevelWrapper, Mat4f baseModelViewMatrix, Mat4f baseProjectionMatrix, float partialTicks, IProfilerWrapper profiler, CallbackInfo ci) {
		this.deferWaterRendering = IrisApi.getInstance().isShaderPackInUse();
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;buildRenderListAndUpdateSections(Lcom/seibel/distanthorizons/coreapi/util/math/Vec3f;)V"))
	private void dontBuildTwice(RenderBufferHandler instance, Vec3f e) {
		if (frame != SystemTimeUniforms.COUNTER.getAsInt()) {
			frame = SystemTimeUniforms.COUNTER.getAsInt();
			instance.buildRenderListAndUpdateSections(e);
		}
	}

	@Inject(method = "setActiveDepthTextureId", at = @At("TAIL"))
	private void reloadDepth(int depthTextureId, CallbackInfo ci) {
		DHCompatInternal.INSTANCE.reconnectDHTextures(depthTextureId);
	}

	@Inject(method = "createColorAndDepthTextures", at = @At("TAIL"))
	private void createDepthTex(int width, int height, CallbackInfo ci) {
		DHCompatInternal.INSTANCE.createDepthTex(width, height);
	}

	@Inject(method = {
		"drawLODs",
		"drawTranslucentLODs"
	}, at = @At("HEAD"), cancellable = true)
	private void cancelIfShadowDoesNotExist(IClientLevelWrapper clientLevelWrapper, Mat4f baseModelViewMatrix, Mat4f baseProjectionMatrix, float partialTicks, IProfilerWrapper profiler, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && !DHCompatInternal.INSTANCE.shouldOverrideShadow) {
			ci.cancel();
		}
	}

	@Inject(method = "renderWaterOnly", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;renderTransparent(Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;)V"))
	private void onTransparent(IProfilerWrapper profiler, float partialTicks, CallbackInfo ci) {
		if (DHCompatInternal.INSTANCE.shouldOverrideShadow && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			DHCompatInternal.INSTANCE.getShadowShader().bind();
			DHCompatInternal.INSTANCE.getShadowFB().bind();
			atTranslucent = true;

			return;
		}
		if (DHCompatInternal.INSTANCE.shouldOverride && DHCompatInternal.INSTANCE.getTranslucentFB() != null) {
			DepthCopyStrategy.fastest(false).copy(DHCompatInternal.INSTANCE.getSolidFB(), depthTexture.getTextureId(), null, DHCompatInternal.INSTANCE.getDepthTexNoTranslucent(), cachedWidth, cachedHeight);
			DHCompatInternal.INSTANCE.getTranslucentShader().bind();
			Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
			float nearClip = RenderUtil.getNearClipPlaneDistanceInBlocks(partialTicks);
			float farClip = (float) ((double) (RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));


			DHCompatInternal.INSTANCE.getTranslucentShader().fillUniformData(new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), nearClip, farClip), CapturedRenderingState.INSTANCE.getGbufferModelView(), MC.getWrappedClientLevel().getMinHeight(), partialTicks);

			DHCompatInternal.INSTANCE.getTranslucentFB().bind();
		}
		atTranslucent = true;
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/GLProxy;runRenderThreadTasks()V"))
	private void runOnlyOnMain(GLProxy instance) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			instance.runRenderThreadTasks();
		}
	}

	@Redirect(method = {
		"setupGLState",
	}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL32;glClear(I)V"))
	private void properClear(int i) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return;

		if (DHCompatInternal.INSTANCE.shouldOverride) {
			GL43C.glClear(GL43C.GL_DEPTH_BUFFER_BIT);
		} else {
			GL43C.glClear(i);
		}
 	}

	@Redirect(method = "setupGLState", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;bind()V"))
	private void bindSolid(LodRenderProgram instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			instance.bind();
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				DHCompatInternal.INSTANCE.getShadowShader().bind();
			} else {
				DHCompatInternal.INSTANCE.getSolidShader().bind();
			}
			atTranslucent = false;
		} else {
			instance.bind();
		}
	}

	@Redirect(method = {
		"drawLODs",
		"drawTranslucentLODs"
	}, at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;unbind()V"))
	private void unbindSolid(LodRenderProgram instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				DHCompatInternal.INSTANCE.getShadowShader().unbind();
			} else {
				DHCompatInternal.INSTANCE.getSolidShader().unbind();
			}
			instance.unbind();
		} else {
			instance.unbind();
		}
	}

	@Redirect(method = "setupGLState", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/texture/DhFramebuffer;bind()V"))
	private void changeFramebuffer(DhFramebuffer instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				DHCompatInternal.INSTANCE.getShadowFB().bind();
			} else {
				DHCompatInternal.INSTANCE.getSolidFB().bind();
			}
		} else {
			instance.bind();
		}
	}

	@Redirect(method = "setupGLState", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/texture/DhFramebuffer;getId()I"))
	private int changeFramebuffer2(DhFramebuffer instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				return DHCompatInternal.INSTANCE.getShadowFB().getId();
			} else {
				return DHCompatInternal.INSTANCE.getSolidFB().getId();
			}
		} else {
			return instance.getId();
		}
	}

	@Inject(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/misc/ILightMapWrapper;bind()V", remap = true))
	private void fillUniformDataSolid(IClientLevelWrapper clientLevelWrapper, Mat4f baseModelViewMatrix, Mat4f baseProjectionMatrix, float partialTicks, IProfilerWrapper profiler, CallbackInfo ci) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				DHCompatInternal.INSTANCE.getShadowShader().fillUniformData(ShadowRenderer.PROJECTION, ShadowRenderer.MODELVIEW, MC.getWrappedClientLevel().getMinHeight(), partialTicks);
			} else {
				Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
				float nearClip = RenderUtil.getNearClipPlaneDistanceInBlocks(partialTicks);
				float farClip = (float)((double)(RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));

				DHCompatInternal.INSTANCE.getSolidShader().fillUniformData(new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), nearClip, farClip), CapturedRenderingState.INSTANCE.getGbufferModelView(), MC.getWrappedClientLevel().getMinHeight(), partialTicks);
			}
		}
	}

	@Inject(method = "setupOffset", at = @At("HEAD"), cancellable = true)
	private void override1(DhBlockPos pos, CallbackInfo ci) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			ci.cancel();
			Vec3d cam = MC_RENDER.getCameraExactPosition();
			Vec3f modelPos = new Vec3f((float)((double)pos.x - cam.x), (float)((double)pos.y - cam.y), (float)((double)pos.z - cam.z));
			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				DHCompatInternal.INSTANCE.getShadowShader().bind();
				DHCompatInternal.INSTANCE.getShadowShader().setModelPos(modelPos);
			} else if (atTranslucent) {
				DHCompatInternal.INSTANCE.getTranslucentShader().bind();
				DHCompatInternal.INSTANCE.getTranslucentShader().setModelPos(modelPos);
			} else {
				DHCompatInternal.INSTANCE.getSolidShader().bind();
				DHCompatInternal.INSTANCE.getSolidShader().setModelPos(modelPos);
			}
		}
	}
}
