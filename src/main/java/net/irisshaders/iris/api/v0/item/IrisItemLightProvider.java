package net.irisshaders.iris.api.v0.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IrisItemLightProvider {
	int getLightEmission(Player player, ItemStack stack);
}
