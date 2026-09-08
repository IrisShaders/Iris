package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
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
import org.objectweb.asm.Opcodes;
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
	private GlRenderPipeline lastPipeline;

	@Shadow
	@Nullable
	private GlProgram lastProgram;

	@Unique
	private int tempFBO;

	@Unique
	private List<IrisProgram> programsToClear = new ArrayList<>();

	// Do not change the viewport in the shadow pass.
	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_viewport(IIII)V"))
	private void changeViewport(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		} else {
			GlStateManager._viewport(i, j, k, l);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_scissorBox(IIII)V"))
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
	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void changeFramebuffer(int i, int j) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() || ImmediateState.safeToMultiply) {
			this.tempFBO = j;
			return;
		} else {
			GlStateManager._glBindFramebuffer(i, j);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawBuffers([I)V"))
	private void iris$changeDrawBuffers(int[] buffers) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered() && !ImmediateState.safeToMultiply) {
			GL33C.glDrawBuffers(buffers);
		}
	}


	@Unique
	private static GlRenderPass lastPass;

	@Inject(method = "setupDraw", at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/opengl/GlCommandEncoder;lastPipeline:Lcom/mojang/renderpearl/backend/opengl/GlRenderPipeline;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void iris$bypassSetup(GlRenderPass glRenderPass, CallbackInfo cir) {
		DepthColorStorage.unlockDepthColor();

		if (ImmediateState.safeToMultiply && !(glRenderPass.pipeline.program() instanceof ExtendedShader)) {
			GlStateManager._glBindFramebuffer(GL46C.GL_FRAMEBUFFER, tempFBO);
		}

		lastPass = glRenderPass;

		if (glRenderPass.iris$getCustomPass() != null) {
			this.lastProgram = null;

			cir.cancel();

			glRenderPass.iris$getCustomPass().setupState();


			if (glRenderPass.isScissorEnabled()) {
				GlStateManager._enableScissorTest();
				GlStateManager._scissorBox(glRenderPass.getScissorX(), glRenderPass.getScissorY(), glRenderPass.getScissorWidth(), glRenderPass.getScissorHeight());
			} else {
				GlStateManager._disableScissorTest();
			}

			GlStateManager._disableDepthTest();
			GlStateManager._depthMask(false);
			GlStateManager._disablePolygonOffset();
			GlStateManager._disableCull();
			GlStateManager._disableBlend(0);
			GlStateManager._colorMask(15);
		}
	}

	@Inject(method = "setupDraw", at = @At("RETURN"))
	private void iris$trackProgram(GlRenderPass glRenderPass, CallbackInfo ci) {
		if (glRenderPass.pipeline.program() instanceof IrisProgram irisProgram && !this.programsToClear.contains(irisProgram)) {
			this.programsToClear.add(irisProgram);
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

	@ModifyArg(method = "executeDraw", index = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawElementsInstancedBaseVertex(IIIJII)V"))
	private int iris$entityTessShaderCompat(int mode) {
		if (mode == GL43C.GL_TRIANGLES && ImmediateState.usingTessellation) {
			mode = GL43C.GL_PATCHES;
		}
		return mode;
	}
}
