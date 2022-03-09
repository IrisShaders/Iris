package net.coderbot.iris.compat.sodium.mixin.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This is a modified version of a mixin in Sodium, with a check for if a shader pack is active.
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * <p>Prevents the sky layer from rendering when the fog distance is reduced
	 * from the default. This helps prevent situations where the sky can be seen
	 * through chunks culled by fog occlusion. This also fixes the vanilla issue
	 * <a href="https://bugs.mojang.com/browse/MC-152504">MC-152504</a> since it
	 * is also caused by being able to see the sky through invisible chunks.</p>
	 *
	 * <p>However, this fix comes with some caveats. When underwater, it becomes 
	 * impossible to see the sun, stars, and moon since the sky is not rendered.
	 * While this does not exactly match the vanilla game, it is consistent with
	 * what Bedrock Edition does, so it can be considered vanilla-style. This is
	 * also more "correct" in the sense that underwater fog is applied to chunks
	 * outside of water, so the fog should also be covering the sun and sky.</p>
	 *
	 * <p>When updating Sodium to new releases of the game, please check for new
	 * ways the fog can be reduced in {@link FogRenderer#setupFog}.</p>
	 */
	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	private void preRenderSky(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		if (!Iris.getCurrentPack().isPresent()) {
			Vec3 cameraPosition = camera.getPosition();
			Entity cameraEntity = camera.getEntity();

			boolean isSubmersed = camera.getFluidInCamera() != FogType.NONE;
			boolean hasBlindness = cameraEntity instanceof LivingEntity && ((LivingEntity) cameraEntity).hasEffect(MobEffects.BLINDNESS);
			boolean useThickFog = this.minecraft.level.effects().isFoggyAt(Mth.floor(cameraPosition.x()),
					Mth.floor(cameraPosition.y())) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();

			if (isSubmersed || hasBlindness || useThickFog) {
				ci.cancel();
			}
		}
	}
}
