package net.irisshaders.iris.api.v0.item;

import net.coderbot.iris.vendored.joml.Vector3f;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IrisItemColorProvider
{
	Vector3f DEFAULT_LIGHT_COLOR = new Vector3f(1, 1, 1);

	default Vector3f getLightColor(Player player, ItemStack stack) {
		return DEFAULT_LIGHT_COLOR;
	}
}
