package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.ItemInHandInterface;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer implements ItemInHandInterface {
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void iris$skipTranslucentHands(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			if (HandRenderer.INSTANCE.isRenderingSolid() == HandRenderer.INSTANCE.isHandTranslucent(itemStack)) {
				ci.cancel();
			}
		}
	}

	@Shadow
	private ItemStack mainHandItem;
	@Shadow
	private ItemStack offHandItem;

	@Override
	public boolean iris$isAnyHandTranslucent () {
		return HandRenderer.INSTANCE.isHandTranslucent(mainHandItem) || HandRenderer.INSTANCE.isHandTranslucent(offHandItem);
	}

	@Override
	public boolean iris$isAnyHandSolid () {
		return !(HandRenderer.INSTANCE.isHandTranslucent(mainHandItem) && HandRenderer.INSTANCE.isHandTranslucent(offHandItem));
	}
}
