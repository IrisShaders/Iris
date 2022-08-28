package net.irisshaders.iris.api.v0.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public interface IrisItemLightProvider {
	default int getLightEmission(Player player, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem)stack.getItem();

			return item.getBlock().defaultBlockState().getLightEmission();
		}

		return 0;
	}
}
