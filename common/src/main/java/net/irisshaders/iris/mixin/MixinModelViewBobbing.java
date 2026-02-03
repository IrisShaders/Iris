package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
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
 * <p>
 * Applying these effects to the projection matrix causes severe issues with most shaderpacks. As it turns out, OptiFine
 * applies these effects to the modelview matrix. As such, we must do the same to properly run shaderpacks.
 * <p>
 * This mixin makes use of the matrix stack in order to make these changes without more invasive changes.
 */
@Mixin(GameRenderer.class)
public abstract class MixinModelViewBobbing {
	@Shadow
	@Final
	Minecraft minecraft;

	@Shadow
	@Final
	private Camera mainCamera;
	@Unique
	private Matrix4fc bobbingEffectsModel;
	@Unique
	private boolean areShadersOn;

	@Shadow
	protected abstract void bobView(PoseStack pGameRenderer0, float pFloat1);

	@Shadow
	protected abstract void bobHurt(PoseStack pGameRenderer0, float pFloat1);

	@Shadow
	private float spinningEffectTime;

	@Shadow
	private float spinningEffectSpeed;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$saveShadersOn(DeltaTracker deltaTracker, CallbackInfo ci) {
		areShadersOn = Iris.isPackInUseQuick();
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
			target = "Ljava/lang/Double;floatValue()F"))
	private float iris$disableConfusionWithShaders(Double instance) {
		return areShadersOn ? 0.0f : instance.floatValue();
	}

	@Redirect(method = "renderLevel",
		at = @At(value = "INVOKE",
			target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;", remap = false))
	private Matrix4f iris$applyBobbingToModelView(Matrix4f instance, Quaternionfc quat, DeltaTracker deltaTracker) {
		if (!areShadersOn) {
			instance.rotation(quat);

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

		LocalPlayer localPlayer = minecraft.player;
		float f = deltaTracker.getGameTimeDeltaPartialTick(false);
		float i = ((Double)this.minecraft.options.screenEffectScale().get()).floatValue();
		float j = Mth.lerp(f, localPlayer.oPortalEffectIntensity, localPlayer.portalEffectIntensity);
		float k = localPlayer.getEffectBlendFactor(MobEffects.NAUSEA, f);
		float l = Math.max(j, k) * i * i;
		if (l > 0.0F) {
			float m = 5.0F / (l * l + 5.0F) - l * 0.04F;
			m *= m;
			Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
			float n = (this.spinningEffectTime + f * this.spinningEffectSpeed) * ((float)Math.PI / 180F);
			instance.rotate(n, vector3f);
			instance.scale(1.0F / m, 1.0F, 1.0F);
			instance.rotate(-n, vector3f);
		}

		instance.rotate(quat);

		return instance;
	}
}
