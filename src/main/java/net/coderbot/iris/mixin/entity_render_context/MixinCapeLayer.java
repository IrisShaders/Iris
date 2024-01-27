package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CapeLayer.class)
public class MixinCapeLayer {
    private static final NamespacedId CAPE_LOCATION = new NamespacedId("minecraft", "player_cape");

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
    private void changeId(PoseStack pCapeLayer0, MultiBufferSource pMultiBufferSource1, int pInt2, AbstractClientPlayer pAbstractClientPlayer3, float pFloat4, float pFloat5, float pFloat6, float pFloat7, float pFloat8, float pFloat9, CallbackInfo ci) {
        if (BlockRenderingSettings.INSTANCE.getItemIds() == null) return;

        CapturedRenderingState.INSTANCE.setCurrentRenderedItem(BlockRenderingSettings.INSTANCE.getItemIds().applyAsInt(CAPE_LOCATION));
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "RETURN"))
    private void changeId2(CallbackInfo ci) {
        CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
    }
}
