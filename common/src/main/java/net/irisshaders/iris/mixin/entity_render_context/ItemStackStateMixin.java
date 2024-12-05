package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.ItemContextState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public class ItemStackStateMixin implements ItemContextState {
	@Unique
	private Item iris_displayStack;
	@Unique
	private ResourceLocation iris_displayModelId;

	@Override
	public void setDisplayItem(Item itemStack, ResourceLocation modelId) {
		this.iris_displayStack = itemStack;
		this.iris_displayModelId = modelId;
	}

	@Override
	public Item getDisplayItem() {
		return iris_displayStack;
	}
	public ResourceLocation getDisplayItemModel() {
		return iris_displayModelId;
	}

	@Inject(method = "clear", at = @At("HEAD"))
	private void clearDisplayStack(CallbackInfo ci) {
		this.iris_displayStack = null;
		this.iris_displayModelId = null;
	}
}
