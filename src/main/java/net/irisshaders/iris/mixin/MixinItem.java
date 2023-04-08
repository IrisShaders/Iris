package net.irisshaders.iris.mixin;

import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class MixinItem implements IrisItemLightProvider {
}
