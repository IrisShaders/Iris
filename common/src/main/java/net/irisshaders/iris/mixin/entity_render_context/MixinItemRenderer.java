package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.ItemContextState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemModelResolver.class, priority = 1010)
public abstract class MixinItemRenderer {
	@Unique
	private int previousBeValue;

	@Inject(method = "appendItemLayers", at = @At(value = "HEAD"))
	private void changeId(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
		if (itemStack != null) {
			((ItemContextState) itemStackRenderState).setDisplayItem(itemStack.getItem(), itemStack.get(DataComponents.ITEM_MODEL));
		} else {
			((ItemContextState) itemStackRenderState).setDisplayItem(null, null);
		}
	}
}
