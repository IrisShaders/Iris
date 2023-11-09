package net.coderbot.iris.uniforms;

import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.mixin.DimensionTypeAccessor;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		WorldInfoUniforms.addWorldInfoUniforms(uniforms);

		uniforms.uniform1i(UniformUpdateFrequency.PER_TICK, "currentColorSpace", () -> IrisVideoSettings.colorSpace.ordinal());

		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHealth", IrisExclusiveUniforms::getCurrentHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHealth", IrisExclusiveUniforms::getMaxHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHunger", IrisExclusiveUniforms::getCurrentHunger);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHunger", () -> 20);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerAir", IrisExclusiveUniforms::getCurrentAir);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerAir", IrisExclusiveUniforms::getMaxAir);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", IrisExclusiveUniforms::isFirstPersonCamera);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "cloudTime", CapturedRenderingState.INSTANCE::getCloudTime);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "relativeEyePosition", () -> {
			return CameraUniforms.getUnshiftedCameraPosition().sub(getEyePosition());
		});
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerLookVector", () -> {
			return JomlConversions.fromVec3(Minecraft.getInstance().getCameraEntity().getLookAngle());
		});
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerBodyVector", () -> {
			return JomlConversions.fromVec3(Minecraft.getInstance().getCameraEntity().getForward());
		});
		Vector4f zero = new Vector4f(0, 0, 0, 0);
		uniforms.uniform4f(UniformUpdateFrequency.PER_TICK, "lightningBoltPosition", () -> {
			if (Minecraft.getInstance().level != null) {
				return StreamSupport.stream(Minecraft.getInstance().level.entitiesForRendering().spliterator(), false).filter(bolt -> bolt instanceof LightningBolt).findAny().map(bolt -> {
					Vector3d unshiftedCameraPosition = CameraUniforms.getUnshiftedCameraPosition();
					Vec3 vec3 = bolt.getPosition(Minecraft.getInstance().getDeltaFrameTime());
					return new Vector4f((float) (vec3.x - unshiftedCameraPosition.x), (float) (vec3.y - unshiftedCameraPosition.y), (float) (vec3.z - unshiftedCameraPosition.z), 1);
				}).orElse(zero);
			} else {
				return zero;
			}
		});
	}

	private static float getThunderStrength() {
		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			Minecraft.getInstance().level.getThunderLevel(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static float getCurrentHealth() {
		if (Minecraft.getInstance().player == null || !Minecraft.getInstance().gameMode.getPlayerMode().isSurvival()) {
			return -1;
		}

		return Minecraft.getInstance().player.getHealth() / Minecraft.getInstance().player.getMaxHealth();
	}

	private static float getCurrentHunger() {
		if (Minecraft.getInstance().player == null || !Minecraft.getInstance().gameMode.getPlayerMode().isSurvival()) {
			return -1;
		}

		return Minecraft.getInstance().player.getFoodData().getFoodLevel() / 20f;
	}

	private static float getCurrentAir() {
		if (Minecraft.getInstance().player == null || !Minecraft.getInstance().gameMode.getPlayerMode().isSurvival()) {
			return -1;
		}

		return (float) Minecraft.getInstance().player.getAirSupply() / (float) Minecraft.getInstance().player.getMaxAirSupply();
	}

	private static float getMaxAir() {
		if (Minecraft.getInstance().player == null || !Minecraft.getInstance().gameMode.getPlayerMode().isSurvival()) {
			return -1;
		}

		return Minecraft.getInstance().player.getMaxAirSupply();
	}

	private static float getMaxHealth() {
		if (Minecraft.getInstance().player == null || !Minecraft.getInstance().gameMode.getPlayerMode().isSurvival()) {
			return -1;
		}

		return Minecraft.getInstance().player.getMaxHealth();
	}

	private static boolean isFirstPersonCamera() {
		// If camera type is not explicitly third-person, assume it's first-person.
		switch (Minecraft.getInstance().options.getCameraType()) {
			case THIRD_PERSON_BACK:
			case THIRD_PERSON_FRONT:
				return false;
			default: return true;
		}
	}

	private static boolean isSpectator() {
		return Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR;
	}

	private static Vector3d getEyePosition() {
		Objects.requireNonNull(Minecraft.getInstance().getCameraEntity());
		Vec3 pos = Minecraft.getInstance().getCameraEntity().getEyePosition(CapturedRenderingState.INSTANCE.getTickDelta());
		return new Vector3d(pos.x, pos.y, pos.z);
	}

	public static class WorldInfoUniforms {
		public static void addWorldInfoUniforms(UniformHolder uniforms) {
			ClientLevel level = Minecraft.getInstance().level;
			// TODO: Use level.dimensionType() coordinates for 1.18!
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "bedrockLevel", () -> {
				if (level != null) {
					return level.dimensionType().minY();
				} else {
					return 0;
				}
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "cloudHeight", () -> {
				if (level != null) {
					return level.effects().getCloudHeight();
				} else {
					return 192.0;
				}
			});

			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "heightLimit", () -> {
				if (level != null) {
					return level.dimensionType().height();
				} else {
					return 256;
				}
			});
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "logicalHeightLimit", () -> {
				if (level != null) {
					return level.dimensionType().logicalHeight();
				} else {
					return 256;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasCeiling", () -> {
				if (level != null) {
					return level.dimensionType().hasCeiling();
				} else {
					return false;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasSkylight", () -> {
				if (level != null) {
					return level.dimensionType().hasSkyLight();
				} else {
					return true;
				}
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "ambientLight", () -> {
				if (level != null) {
					return level.dimensionType().ambientLight();
				} else {
					return 0f;
				}
			});

		}
	}
}
