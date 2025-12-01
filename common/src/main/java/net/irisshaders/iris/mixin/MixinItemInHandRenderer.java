package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void iris$skipTranslucentHands(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int j, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			if (HandRenderer.INSTANCE.isRenderingSolid() && HandRenderer.INSTANCE.isHandTranslucent(interactionHand)) {
				ci.cancel();
			} else if (!HandRenderer.INSTANCE.isRenderingSolid() && !HandRenderer.INSTANCE.isHandTranslucent(interactionHand)) {
				ci.cancel();
			}
		}
	}
}
