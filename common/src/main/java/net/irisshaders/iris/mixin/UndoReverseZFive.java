package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

@Mixin(GlCommandEncoder.class)
public class UndoReverseZFive {
	@WrapMethod(method = "clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V")
	private void iris$change(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
		original.call(colorTexture, clearColor, depthTexture, saturate(Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel ? 1.0 - clearDepth : clearDepth));
	}
	@WrapMethod(method = "clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;DIIII)V")
	private void iris$change3(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, int regionX, int regionY, int regionWidth, int regionHeight, Operation<Void> original) {
		original.call(colorTexture, clearColor, depthTexture, saturate(Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel ? 1.0 - clearDepth : clearDepth), regionX, regionY, regionWidth, regionHeight);
	}
	@WrapMethod(method = "clearDepthTexture")
	private void iris$change2(GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
		original.call(depthTexture, saturate(Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel ? 1.0 - clearDepth : clearDepth));
	}
	@WrapMethod(method = "createRenderPass")
	private RenderPassBackend iris$change4(RenderPassDescriptor descriptor, Operation<RenderPassBackend> original) {
        if (!(Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel)) {
            return original.call(descriptor);
        }
		return original.call(descriptor.depthAttachment() != null && descriptor.depthAttachment().clearValue().isPresent() ? descriptor.withDepthAttachment(descriptor.depthAttachment().textureView(), OptionalDouble.of(saturate(1.0 - descriptor.depthAttachment.clearValue().getAsDouble()))) : descriptor);
	}

	@WrapOperation(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_polygonOffset(FF)V"))
	private void iris$revertPolygonOffset(float factor, float units, Operation<Void> original) {
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
			original.call(-factor, -units);
		} else {
			original.call(factor, units);
		}
	}

	@Unique
	private double saturate(double v) {
		return Math.clamp(v, 0.0, 1.0);
	}
}
