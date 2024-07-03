package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin makes the effects of view bobbing and nausea apply to the model view matrix, not the projection matrix.
 *
 * Applying these effects to the projection matrix causes severe issues with most shaderpacks. As it turns out, OptiFine
 * applies these effects to the modelview matrix. As such, we must do the same to properly run shaderpacks.
 *
 * This mixin makes use of the matrix stack in order to make these changes without more invasive changes.
 */
@Mixin(GameRenderer.class)
public abstract class MixinModelViewBobbing {
	@Shadow
	protected abstract void bobView(PoseStack pGameRenderer0, float pFloat1);

	@Shadow
	protected abstract void bobHurt(PoseStack pGameRenderer0, float pFloat1);

	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private Camera mainCamera;
	@Shadow
	private int confusionAnimationTick;
	@Unique
	private Matrix4fc bobbingEffectsModel;

	@Unique
	private boolean areShadersOn;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$saveShadersOn(float f, long l, CallbackInfo ci) {
		areShadersOn = IrisApi.getInstance().isShaderPackInUse();
	}

	@ModifyArg(method = "renderLevel", index = 0,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private PoseStack iris$separateViewBobbing(PoseStack stack) {
		if (!areShadersOn) return stack;

		stack.pushPose();
		stack.last().pose().identity();

		return stack;
	}

	@Redirect(method = "renderLevel",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void iris$stopBobbing(GameRenderer instance, PoseStack pGameRenderer0, float pFloat1) {
		if (!areShadersOn) this.bobView(pGameRenderer0, pFloat1);
	}


	@Redirect(method = "renderLevel",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void iris$saveBobbing(GameRenderer instance, PoseStack pGameRenderer0, float pFloat1) {
		if (!areShadersOn) this.bobHurt(pGameRenderer0, pFloat1);
	}


	@Redirect(method = "renderLevel",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
	private<T> T iris$disableConfusionWithShaders(OptionInstance<T> instance) {
		return areShadersOn ? (T) (Object) 0.0 : instance.get();
	}

	@Redirect(method = "renderLevel",
		at = @At(value = "INVOKE",
			target = "Lorg/joml/Matrix4f;rotationXYZ(FFF)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$applyBobbingToModelView(Matrix4f instance, float angleX, float angleY, float angleZ, float f) {
		if (!areShadersOn) {
			instance.rotateXYZ(angleX, angleY, angleZ);

			return instance;
		}

		PoseStack stack = new PoseStack();
		stack.last().pose().set(instance);

		float tickDelta = this.mainCamera.getPartialTickTime();

		this.bobHurt(stack, tickDelta);
		if (this.minecraft.options.bobView().get()) {
			this.bobView(stack, tickDelta);
		}

		instance.set(stack.last().pose());

		float h = this.minecraft.options.screenEffectScale().get().floatValue();
		float i = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * h * h;
		if (i > 0.0F) {
			int j = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
			float k = 5.0F / (i * i + 5.0F) - i * 0.04F;
			k *= k;
			Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
			float l = ((float)this.confusionAnimationTick + f) * (float)j * (float) (Math.PI / 180.0);
			instance.rotate(l, vector3f);
			instance.scale(1.0F / k, 1.0F, 1.0F);
			instance.rotate(-l, vector3f);
		}

		instance.rotateXYZ(angleX, angleY, angleZ);

		return instance;
	}
}
