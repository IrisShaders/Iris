package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.ItemInHandInterface;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer implements ItemInHandInterface {
	@Shadow
	public abstract void renderHandsWithItems(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int i);

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

	@Unique
	private HandRenderer customRenderer;

	@Override
	public void iris$renderHandsWithCustomRenderer(HandRenderer handRenderer, float tickDelta, PoseStack poseStack, SubmitNodeStorage submitNodeCollector, @Nullable LocalPlayer player, int packedLightCoords) {
		customRenderer = handRenderer;
		this.renderHandsWithItems(tickDelta, poseStack, submitNodeCollector, player, packedLightCoords);
		customRenderer = null;
	}

	@WrapWithCondition(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
	private boolean iris$wrapHand(FeatureRenderDispatcher instance) {
		return customRenderer == null;
	}

	@WrapOperation(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private void iris$wrapHand2(MultiBufferSource.BufferSource instance, Operation<Void> original) {
		if (customRenderer == null) {
			original.call(instance);
		} else {
			customRenderer.endRender();
		}
	}
}
