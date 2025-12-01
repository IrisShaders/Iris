package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
	private boolean inRenderPass;

	@Shadow
	@Nullable
	private GlProgram lastProgram;

	@Unique
	private int tempFBO;

	@Unique
	private List<IrisProgram> programsToClear = new ArrayList<>();

	// Do not change the viewport in the shadow pass.
	@Redirect(method = "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"))
	private void changeViewport(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		} else {
			GlStateManager._viewport(i, j, k, l);
		}
	}

	// Do not change the viewport in the shadow pass.
	@Redirect(method = "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void changeFramebuffer(int i, int j) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() || ImmediateState.safeToMultiply) {
			this.tempFBO = j;
			return;
		} else {
			GlStateManager._glBindFramebuffer(i, j);
		}
	}

	@Redirect(method = "finishRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void finishFramebuffer(int i, int j) {
		if (!ImmediateState.safeToMultiply) {
			GlStateManager._glBindFramebuffer(i, j);
		}
	}

	@Redirect(method = "writeToBuffer", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/opengl/GlCommandEncoder;inRenderPass:Z"))
	private boolean ignore(GlCommandEncoder instance) {
		if (ImmediateState.temporarilyIgnorePass) {
			return false;
		} else {
			return this.inRenderPass;
		}
	}

	@Redirect(method = {
		"writeToTexture(Lcom/mojang/blaze3d/textures/GpuTexture;Ljava/nio/ByteBuffer;Lcom/mojang/blaze3d/platform/NativeImage$Format;IIIIII)V",
		"writeToTexture(Lcom/mojang/blaze3d/textures/GpuTexture;Lcom/mojang/blaze3d/platform/NativeImage;IIIIIIII)V",
		"writeToTexture(Lcom/mojang/blaze3d/textures/GpuTexture;Lcom/mojang/blaze3d/platform/NativeImage;)V"
	}, at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/opengl/GlCommandEncoder;inRenderPass:Z"))
	private boolean ignore2(GlCommandEncoder instance) {
		if (ImmediateState.temporarilyIgnorePass) {
			return false;
		} else {
			return this.inRenderPass;
		}
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

			RenderPipeline renderPipeline = glRenderPass.pipeline.info();

			if (glRenderPass.isScissorEnabled()) {
				GlStateManager._enableScissorTest();
				GlStateManager._scissorBox(glRenderPass.getScissorX(), glRenderPass.getScissorY(), glRenderPass.getScissorWidth(), glRenderPass.getScissorHeight());
			} else {
				GlStateManager._disableScissorTest();
			}

			if (this.lastPipeline != renderPipeline) {
				this.lastPipeline = renderPipeline;

				if (renderPipeline.getDepthTestFunction() != DepthTestFunction.NO_DEPTH_TEST) {
					GlStateManager._enableDepthTest();
					GlStateManager._depthFunc(GlConst.toGl(renderPipeline.getDepthTestFunction()));
				} else {
					GlStateManager._disableDepthTest();
				}

				if (renderPipeline.isCull()) {
					GlStateManager._enableCull();
				} else {
					GlStateManager._disableCull();
				}

				GlStateManager._polygonMode(1032, GlConst.toGl(renderPipeline.getPolygonMode()));
				GlStateManager._depthMask(renderPipeline.isWriteDepth());
				GlStateManager._colorMask(renderPipeline.isWriteColor(), renderPipeline.isWriteColor(), renderPipeline.isWriteColor(), renderPipeline.isWriteAlpha());

				if (renderPipeline.getDepthBiasConstant() == 0.0F && renderPipeline.getDepthBiasScaleFactor() == 0.0F) {
					GlStateManager._disablePolygonOffset();
				} else {
					GlStateManager._polygonOffset(renderPipeline.getDepthBiasScaleFactor(), renderPipeline.getDepthBiasConstant());
					GlStateManager._enablePolygonOffset();
				}

				switch (renderPipeline.getColorLogic()) {
					case NONE:
						GlStateManager._disableColorLogicOp();
						break;
					case OR_REVERSE:
						GlStateManager._enableColorLogicOp();
						GlStateManager._logicOp(5387);
				}
			}
		}
	}

	@Inject(method = "trySetup", at = @At("RETURN"))
	private void iris$setupState(GlRenderPass glRenderPass, Collection<String> collection, CallbackInfoReturnable<Boolean> cir) {
		if (glRenderPass.pipeline.program() instanceof IrisProgram is && !is.iris$isSetUp()) {
			GpuTextureView sam = glRenderPass.samplers.get("Sampler0");
			if (sam != null) {
				RenderSystem.setShaderTexture(0, sam);
			}
			is.iris$setupState();
			programsToClear.add(is);
		}
	}

	@Inject(method = "finishRenderPass", at = @At("HEAD"))
	private void iris$clearState(CallbackInfo ci) {
		programsToClear.forEach(IrisProgram::iris$clearState);
		programsToClear.clear();
	}
}
