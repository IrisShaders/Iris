package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class MixinItemInHandRenderer {

	@Shadow
	public abstract void submitHandsWithItems(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
		PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state);

	@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
	private void iris$skipTranslucentHands(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks,
		float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			if (HandRenderer.getInstance().isRenderingSolid() == HandRenderer.getInstance().isHandTranslucent(itemStack)) {
				ci.cancel();
			}
		}
	}

}
