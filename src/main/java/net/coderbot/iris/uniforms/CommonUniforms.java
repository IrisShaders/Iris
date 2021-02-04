package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import java.util.Objects;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.texunits.TextureUnit;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.LightType;

public final class CommonUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CommonUniforms() {
		// no construction allowed
	}

	public static void addCommonUniforms(UniformHolder uniforms, IdMap idMap) {
		CameraUniforms.addCameraUniforms(uniforms);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		CelestialUniforms.addCelestialUniforms(uniforms);
		IdMapUniforms.addIdMapUniforms(uniforms, idMap);
		MatrixUniforms.addMatrixUniforms(uniforms);

		uniforms
			.uniform1i(ONCE, "texture", TextureUnit.TERRAIN::getSamplerId)
			.uniform1i(ONCE, "lightmap", TextureUnit.LIGHTMAP::getSamplerId)
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getEyeY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1i(ONCE, "noisetex", () -> 15);
	}

	private static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance blindness = ((LivingEntity) cameraEntity).getStatusEffect(StatusEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in BackgroundRenderer
				// TODO: Add this to ShaderDoc
				return Math.min(1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	private static float getRainStrength() {
		if (client.world == null) {
			return 0f;
		}

		return client.world.getRainGradient(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static Vec2f getEyeBrightness() {
		if (client.cameraEntity == null || client.world == null) {
			return Vec2f.ZERO;
		}
		int blockLight = client.world.getLightLevel(LightType.BLOCK, client.cameraEntity.getBlockPos());
		int skyLight = client.world.getLightLevel(LightType.SKY, client.cameraEntity.getBlockPos());
		return new Vec2f(blockLight, skyLight);
	}

	private static int isEyeInWater() {
		FluidState submergedFluid = client.gameRenderer.getCamera().getSubmergedFluidState();

		if (submergedFluid.isIn(FluidTags.WATER)) {
			return 1;
		} else if (submergedFluid.isIn(FluidTags.LAVA)) {
			return 2;
		} else {
			return 0;
		}
	}
}
