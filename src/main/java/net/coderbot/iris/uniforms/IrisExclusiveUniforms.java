package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class IrisExclusiveUniforms {

	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1i(PER_TICK, "gameMode", IrisExclusiveUniforms::getGameType);
	}

	private static int getGameType() {

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return -1;
		}

		PlayerInfo playerInfo = connection.getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());

		if (playerInfo == null) {
			return -1;
		}

		GameType gameType = playerInfo.getGameMode();

		if (gameType != null) {
			return gameType.getId();
		} else {
			return -1;
		}
	}

}
