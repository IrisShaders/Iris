package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class MixinCamera {
	@WrapOperation(method = "prepareCullFrustum", at = @At(value = "NEW", target = "Lnet/minecraft/client/renderer/culling/Frustum;"))
	private Frustum iris$disableFrustum(Matrix4fc modelViewMatrix, Matrix4f projectionMatrixForCulling, Operation<Frustum> original) {
		Frustum frustum;

		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldDisableFrustumCulling).orElse(false)) {
			frustum = new NonCullingFrustum(modelViewMatrix, projectionMatrixForCulling);
		} else {
			frustum = original.call(modelViewMatrix, projectionMatrixForCulling);
		}

		return frustum;
	}

}
