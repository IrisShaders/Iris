package net.coderbot.iris.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin makes the effects of view bobbing and nausea apply to the model view matrix, not the projection matrix.
 *
 * Applying these effects to the projection matrix causes severe issues with most shaderpacks. As it turns out, OptiFine
 * applies these effects to the modelview matrix. As such, we must do the same to properly run shaderpacks.
 *
 * This mixin make use of the matrix stack in order to make these changes without more invasive changes.
 */
@Mixin(GameRenderer.class)
public class MixinModelViewBobbing {
	@Unique
	private Matrix4f bobbingEffectsModel;

	@Unique
	private Matrix3f bobbingEffectsNormal;

	@ModifyArg(method = "renderWorld", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
	private MatrixStack iris$separateViewBobbing(MatrixStack stack) {
		stack.push();
		stack.peek().getModel().loadIdentity();
		stack.peek().getNormal().loadIdentity();

		return stack;
	}

	@Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;peek()Lnet/minecraft/client/util/math/MatrixStack$Entry;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V")))
	private MatrixStack.Entry iris$saveBobbing(MatrixStack stack) {
		bobbingEffectsModel = stack.peek().getModel().copy();
		bobbingEffectsNormal = stack.peek().getNormal().copy();

		stack.pop();

		return stack.peek();
	}

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;loadProjectionMatrix(Lnet/minecraft/util/math/Matrix4f;)V"))
	private void iris$applyBobbingToModelView(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		matrix.peek().getModel().multiply(bobbingEffectsModel);
		matrix.peek().getNormal().multiply(bobbingEffectsNormal);

		bobbingEffectsModel = null;
		bobbingEffectsNormal = null;
	}
}
