package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.VertexArray;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.GlProgramBindings;
import net.irisshaders.iris.mixinterface.GlRenderPipelineAccess;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlRenderPipeline.class)
public class MixinGlRenderPipeline implements GlRenderPipelineAccess {
	@Shadow
	@Final
	private GlProgram program;
	@Unique
	private BackendRenderPipeline.CreateInfo createInfo;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$setCreateInfo(GlDevice device, BackendRenderPipeline.CreateInfo createInfo, GlProgram program, VertexArray vertexArray, CallbackInfo ci) {
		this.createInfo = createInfo;
	}
	@Override
	public BackendRenderPipeline.CreateInfo getCreateInfo() {
		return this.createInfo;
	}

	@WrapOperation(method = "bind", at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/opengl/GlRenderPipeline;cull:Z"))
	private boolean iris$redirectCull(GlRenderPipeline instance, Operation<Boolean> original) {
		return !ShadowRenderingState.areShadowsCurrentlyBeingRendered() && original.call(instance);
	}

	@WrapOperation(method = "bind", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_polygonOffset(FF)V"))
	private void iris$revertPolygonOffset(float factor, float units, Operation<Void> original) {
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
			original.call(-factor, -units);
		} else {
			original.call(factor, units);
		}
	}


	@Inject(method = "bind", at = @At("RETURN"))
	private void iris$bind(CallbackInfo ci) {
		if (this.program instanceof IrisProgram irisProgram) {
			((GlProgramBindings) this.program).iris$setupBindings(this.createInfo.uniforms(), this.createInfo.pushConstantsSize());
			irisProgram.iris$setupState(this.createInfo.uniforms());
		}
	}
}
