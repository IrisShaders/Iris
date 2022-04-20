package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.RenderBuffersExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
	// TODO: Figure out the cause of this being null in the first place.
	@Inject(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
	private void iris$fixToastItems(ItemStack arg, int i, int j, BakedModel arg2, CallbackInfo ci) {
		((RenderBuffersExt) Minecraft.getInstance().renderBuffers()).beginLevelRendering();
	}

	@Inject(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("TAIL"))
	private void iris$resetToastItemFix(ItemStack arg, int i, int j, BakedModel arg2, CallbackInfo ci) {
		((RenderBuffersExt) Minecraft.getInstance().renderBuffers()).endLevelRendering();
	}
}
