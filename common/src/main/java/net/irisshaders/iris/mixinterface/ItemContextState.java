package net.irisshaders.iris.mixinterface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemContextState {
	void setDisplayItem(Item itemStack, ResourceLocation location);

	Item getDisplayItem();
	ResourceLocation getDisplayItemModel();
}
