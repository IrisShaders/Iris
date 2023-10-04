package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL;
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
 * OptiFine makes the far plane only 1024 blocks above the render distance in blocks (two times the render distance in 1.16 and lower), and (in 1.16) to compensate, adds a minimum distance
 * for the far plane to make it not appear too closely to the player.
 *
 * On 1.16 and lower, OptiFine also modifies the distance of the far plane based on the fog setting.
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

/**
 * Due to shader devs moving forward with removing legacy code, I have decided to disable this Mixin. It will be stored here for reference. -IMS
 */
@Mixin(GameRenderer.class)
public class MixinTweakFarPlane {
	@Shadow
	private float renderDistance;

	@Shadow
	public float getDepthFar() {
		throw new AssertionError();
	}

	@Redirect(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getDepthFar()F"))
	private float iris$tweakViewDistanceToMatchOptiFine(GameRenderer renderer) {
		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			return this.getDepthFar();
		}

		float tweakedViewDistance = this.renderDistance;

		// Halve the distance of the far plane in the projection matrix from vanilla. Normally, the far plane is 4 times
		// the view distance, but this makes it so that it is only the view distance + 1024 blocks. (OptiFine for 1.16 and lower uses view distance * 2, and OptiFine for 1.17+ uses view distance + 1024)
		tweakedViewDistance += 1024.0F;

		// Use a minimum distance for the far plane
		// The real far plane will be 4 times this, so this will result in a far plane of 173 meters.
		//
		// Math.max returns the maximum of the two values, so whenever tweakedViewDistance falls below 43.25F, this code
		// forces it to take on a value of 43.25F.
		// NB: This should not be activated on 1.17+.
		//tweakedViewDistance = Math.max(43.25F, tweakedViewDistance);

		return tweakedViewDistance;
	}

	// Seemingly, this should not be activated on 1.17.
	//@Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderDistance:F", shift = At.Shift.AFTER))
	private void iris$tweakViewDistanceBasedOnFog(float f, long l, PoseStack poseStack, CallbackInfo ci) {
		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			return;
		}

		// Tweak the view distance based on the fog setting
		//
		// Coefficient values: 0.83 for fast fog, 0.95 for fancy fog, 1.0 for no fog
		//
		// On 1.16, we select the value based on if GL_NV_fog_distance is supported, and on 1.17+ only fancy fog is supported.

		renderDistance *= 0.95F;
	}
}
