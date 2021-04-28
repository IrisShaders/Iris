package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tweaks the far plane of the projection matrix to match OptiFine.
 *
 * As it turns out, OptiFine significantly reduces the far plane distance compared to vanilla. This is likely because
 * vanilla chooses a far plane that is four times the render distance in blocks, which is a bit overkill. Instead,
 * OptiFine makes the far plane only two times the render distance in blocks, and to compensate, adds a minimum distance
 * for the far plane to make it not appear too closely to the player.
 *
 * OptiFine also modifies the distance of the far plane based on the fog setting. We mimic the "fast" fog setting
 * because that is the setting that reduces the far plane distance the most.
 *
 * So why is this needed? As it turns out, shaderpacks actually rely on this behavior to work properly. Most notably,
 * the water reflection code in Sildur's Vibrant Shaders will often create impossible reflections with the default far
 * plane, where things that are close to the player will be reflected in water that is very far away.
 *
 * A possible reason for this is that the value of the {@code far} uniform does not actually match the far plane
 * distance, so shaderpacks one way or another have come to just bodge things to work around the issue, and in the
 * process become subtly reliant on OptiFine implementation details.
 *
 * Fun.
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class MixinTweakFarPlane {
	@Shadow
	private float viewDistance;

	@Redirect(method = "getBasicProjectionMatrix(Lnet/minecraft/client/render/Camera;FZ)Lnet/minecraft/util/math/Matrix4f;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;viewDistance:F"))
	private float iris$tweakViewDistanceToMatchOptiFine(GameRenderer renderer) {
		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			return this.viewDistance;
		}

		float tweakedViewDistance = this.viewDistance;

		// Halve the distance of the far plane in the projection matrix from vanilla. Normally, the far plane is 4 times
		// the view distance, but this makes it so that it is only two times the view distance.
		tweakedViewDistance *= 0.5;

		// Use a minimum distance for the far plane
		// The real far plane will be 4 times this, so this will result in a far plane of 173 meters.
		//
		// Math.max returns the maximum of thw two values, so whenever tweakedViewDistance falls below 43.25F, this code
		// forces it to take on a value of 43.25F.
		tweakedViewDistance = Math.max(43.25F, tweakedViewDistance);

		return tweakedViewDistance;
	}

	@Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;viewDistance:F", shift = At.Shift.AFTER))
	private void iris$tweakViewDistanceBasedOnFog(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			return;
		}

		// Tweak the view distance based on the fog setting
		//
		// Coefficient values: 0.83 for fast fog, 0.95 for fancy fog, 1.0 for no fog
		//
		// We mimic "fast" fog here

		viewDistance *= 0.83F;
	}
}
