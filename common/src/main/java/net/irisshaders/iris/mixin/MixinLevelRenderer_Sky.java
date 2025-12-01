package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.NeoLambdas;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This is a modified version of a mixin in Sodium, with a check for if a shader pack is active.
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_Sky {
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
	@Inject(method = { "method_62215", NeoLambdas.NEO_RENDER_SKY }, require = 1, at = @At("HEAD"), cancellable = true)
	private void preRenderSky(CallbackInfo ci) {
		if (Iris.getCurrentPack().isEmpty()) {
			Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
			Vec3 cameraPosition = camera.getPosition();
			Entity cameraEntity = camera.getEntity();

			boolean isSubmersed = camera.getFluidInCamera() != FogType.NONE;
			boolean blockSky = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).invokeDoesMobEffectBlockSky(camera);
			boolean useThickFog = this.minecraft.level.effects().isFoggyAt(Mth.floor(cameraPosition.x()),
				Mth.floor(cameraPosition.y())) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();

			if (isSubmersed || blockSky || useThickFog) {
				ci.cancel();
			}
		}
	}
}
