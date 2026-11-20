package net.irisshaders.iris.mixin.vertices.immediate;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick optimization to disable the extended vertex format outside of level rendering if we're using a BufferSource.
 * This is a heuristic that should hopefully work almost always because of how people use BufferSource.
 */
@Mixin(StagedVertexBuffer.class)
public class MixinBufferSource {
	// The draw has to use the same format as the builder, which MixinBufferBuilder extends for particles.
	@ModifyVariable(method = "appendDraw(Lcom/mojang/renderpearl/api/vertex/VertexFormat;Lcom/mojang/renderpearl/api/pipeline/PrimitiveTopology;Lcom/mojang/blaze3d/vertex/VertexSorting;)Lnet/minecraft/client/renderer/StagedVertexBuffer$Draw;", at = @At("HEAD"), argsOnly = true)
	private VertexFormat iris$extendParticleFormat(VertexFormat format) {
		if (format.equals(DefaultVertexFormat.PARTICLE) && Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ImmediateState.skipExtension.get()) {
			return IrisVertexFormats.PARTICLE;
		}

		return format;
	}

	@WrapMethod(method = "getVertexBuilder")
	private VertexConsumer iris$redirectBegin(StagedVertexBuffer.Draw draw, Operation<VertexConsumer> original) {
		ImmediateState.skipExtension.set(iris$notRenderingLevel());
		VertexConsumer builder = original.call(draw);
		ImmediateState.skipExtension.set(false);

		return builder;
	}

	@Inject(method = "upload", at = @At(value = "HEAD"))
	private void iris$beforeFlushBuffer(CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(method = "upload", at = @At(value = "RETURN"))
	private void iris$afterFlushBuffer(CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}

	@Unique
	private boolean iris$notRenderingLevel() {
		return !ImmediateState.isRenderingLevel;
	}
}
