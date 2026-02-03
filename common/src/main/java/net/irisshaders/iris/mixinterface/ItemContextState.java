package net.irisshaders.iris.mixinterface;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemContextState {
	void setDisplayItem(Item itemStack, Identifier location);

	Item getDisplayItem();
	Identifier getDisplayItemModel();
}
