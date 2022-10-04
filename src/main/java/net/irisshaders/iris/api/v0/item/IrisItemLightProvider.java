package net.irisshaders.iris.api.v0.item;

import com.mojang.math.Vector3f;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public interface IrisItemLightProvider {

	Vector3f DEFAULT_LIGHT_COLOR = new Vector3f(1, 1, 1);

	default int getLightEmission(Player player, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem)stack.getItem();

			return item.getBlock().defaultBlockState().getLightEmission();
		}

		return 0;
	}

	default Vector3f getLightColor(Player player, ItemStack stack) {
		return DEFAULT_LIGHT_COLOR;
	}
}
