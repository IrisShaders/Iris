package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.ScissorState;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(GlCommandEncoder.class)
public class MixinGlCommandEncoder {
	@Shadow
	@Nullable
	private RenderPipeline lastPipeline;

	@Shadow
	@Nullable
	private GlProgram lastProgram;

	@Unique
	private int tempFBO;

	@Unique
	private List<IrisProgram> programsToClear = new ArrayList<>();

	// Do not change the viewport in the shadow pass.
	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"))
	private void changeViewport(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		} else {
			GlStateManager._viewport(i, j, k, l);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_scissorBox(IIII)V"))
	private void changeViewport2(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            GlStateManager._scissorBox(0, 0, ShadowRenderer.RESOLUTION, ShadowRenderer.RESOLUTION);
            return;
		} else {
			GlStateManager._scissorBox(i, j, k, l);
		}
	}


	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/ScissorState;enable(IIII)V"))
	private void changeViewport3(ScissorState instance, int x, int y, int width, int height) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            instance.enable(0, 0, ShadowRenderer.RESOLUTION, ShadowRenderer.RESOLUTION);
            return;
		} else {
            instance.enable(x, y, width, height);
		}
	}

	// Do not change the viewport in the shadow pass.
	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void changeFramebuffer(int i, int j) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() || ImmediateState.safeToMultiply) {
			this.tempFBO = j;
			return;
		} else {
			GlStateManager._glBindFramebuffer(i, j);
		}
	}


	@WrapOperation(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline;isCull()Z"))
	private boolean iris$redirectCull(RenderPipeline instance, Operation<Boolean> original) {
		return !ShadowRenderingState.areShadowsCurrentlyBeingRendered() && original.call(instance);
	}

	@Unique
	private static GlRenderPass lastPass;

	@Inject(method = "trySetup", at = @At("HEAD"), cancellable = true)
	private void iris$bypassSetup(GlRenderPass glRenderPass, Collection<String> collection, CallbackInfoReturnable<Boolean> cir) {
		DepthColorStorage.unlockDepthColor();

		if (ImmediateState.safeToMultiply && !(glRenderPass.pipeline.program() instanceof ExtendedShader)) {
			GlStateManager._glBindFramebuffer(GL46C.GL_FRAMEBUFFER, tempFBO);
		}

		lastPass = glRenderPass;

		if (glRenderPass.iris$getCustomPass() != null) {
			this.lastProgram = null;

			cir.setReturnValue(true);

			glRenderPass.iris$getCustomPass().setupState();

			RenderPipeline pipeline = glRenderPass.pipeline.info();

			if (glRenderPass.isScissorEnabled()) {
				GlStateManager._enableScissorTest();
				GlStateManager._scissorBox(glRenderPass.getScissorX(), glRenderPass.getScissorY(), glRenderPass.getScissorWidth(), glRenderPass.getScissorHeight());
			} else {
				GlStateManager._disableScissorTest();
			}

			if (this.lastPipeline != pipeline) {
				this.lastPipeline = pipeline;

				DepthStencilState depthStencilState = pipeline.getDepthStencilState();
				if (depthStencilState != null) {
					GlStateManager._enableDepthTest();
					GlStateManager._depthFunc(GlConst.toGl(depthStencilState.depthTest()));
					GlStateManager._depthMask(depthStencilState.writeDepth());
					if (depthStencilState.depthBiasConstant() == 0.0F && depthStencilState.depthBiasScaleFactor() == 0.0F) {
						GlStateManager._disablePolygonOffset();
					} else {
						GlStateManager._polygonOffset(depthStencilState.depthBiasScaleFactor(), depthStencilState.depthBiasConstant());
						GlStateManager._enablePolygonOffset();
					}
				} else {
					GlStateManager._disableDepthTest();
					GlStateManager._depthMask(false);
					GlStateManager._disablePolygonOffset();
				}

				if (pipeline.isCull() && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
					GlStateManager._enableCull();
				} else {
					GlStateManager._disableCull();
				}

				if (pipeline.getColorTargetState().blendFunction().isPresent()) {
					GlStateManager._enableBlend(0);
					BlendFunction blendFunction = (BlendFunction)pipeline.getColorTargetState().blendFunction().get();
					GlStateManager._blendFuncSeparate(
						GlConst.toGl(blendFunction.color().sourceFactor()),
						GlConst.toGl(blendFunction.color().destFactor()),
						GlConst.toGl(blendFunction.alpha().sourceFactor()),
						GlConst.toGl(blendFunction.alpha().destFactor())
					);
				} else {
					GlStateManager._disableBlend(0);
				}

				GlStateManager._polygonMode(1032, GlConst.toGl(pipeline.getPolygonMode()));
				GlStateManager._colorMask(pipeline.getColorTargetState().writeMask());
			}
		}
	}

	@Redirect(method = "trySetup", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawBuffers([I)V"))
	private void iris$skipShadowDrawBuffers(int[] buffers, @Local GlRenderPass glRenderPass) {
		if (glRenderPass.pipeline.program() instanceof IrisProgram is) {
			return;
		} else {
            GL33C.glDrawBuffers(buffers);
        }
	}

	@Inject(method = "trySetup", at = @At("RETURN"))
	private void iris$setupState(GlRenderPass glRenderPass, Collection<String> collection, CallbackInfoReturnable<Boolean> cir) {
		if (glRenderPass.pipeline.program() instanceof IrisProgram is && !is.iris$isSetUp()) {
			GlRenderPass.TextureViewAndSampler sam = glRenderPass.samplers.get("Sampler0");

            if (sam == null) {
                sam = glRenderPass.samplers.get("u_BlockTex");
            }
			if (sam != null && Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline irp) {
				irp.onSetAlbedoTex(sam.view());
			}
			is.iris$setupState(glRenderPass.samplers, sam == null ? null : sam.view());
			programsToClear.add(is);
		}
	}

	@Redirect(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_colorMask(II)V"))
	private void iris$changeColorMask(int index, int writeMask, @Local ColorTargetState[] states) {
		if (states.length == 1) {
			GlStateManager._colorMask(writeMask);
		} else {
			GlStateManager._colorMask(index, writeMask);
		}
	}

	@Inject(method = "submitRenderPass", at = @At("HEAD"))
	private void iris$clearState(CallbackInfo ci) {
		programsToClear.forEach(IrisProgram::iris$clearState);
		programsToClear.clear();
	}

	@ModifyArg(method = "executeDraws", index = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;nglMultiDrawElementsBaseVertex(IJIJIJ)V"))
	private int iris$terrainTessShaderCompat(int mode) {
		if (mode == GL43C.GL_TRIANGLES && ImmediateState.usingTessellation) {
			mode = GL43C.GL_PATCHES;
		}
		return mode;
	}

	@ModifyArg(method = "drawFromBuffers", index = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawElementsInstancedBaseVertex(IIIJII)V"))
	private int iris$entityTessShaderCompat(int mode) {
		if (mode == GL43C.GL_TRIANGLES && ImmediateState.usingTessellation) {
			mode = GL43C.GL_PATCHES;
		}
		return mode;
	}
}
