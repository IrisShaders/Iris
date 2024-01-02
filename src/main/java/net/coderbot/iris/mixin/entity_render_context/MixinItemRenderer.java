package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ItemRenderer.class, priority = 1010)
public abstract class MixinItemRenderer {
	@Unique
	private int previousBeValue;

	@Inject(method = "render", at = @At(value = "HEAD"))
	private void changeId(ItemStack pItemRenderer0, ItemDisplayContext pItemTransforms$TransformType1, boolean pBoolean2, PoseStack pPoseStack3, MultiBufferSource pMultiBufferSource4, int pInt5, int pInt6, BakedModel pBakedModel7, CallbackInfo ci) {
		iris$setupId(pItemRenderer0);
	}

	@Unique
	private void iris$setupId(ItemStack pItemRenderer0) {
		if (BlockRenderingSettings.INSTANCE.getItemIds() == null) return;

		if (pItemRenderer0.getItem() instanceof BlockItem blockItem && !(pItemRenderer0.getItem() instanceof SolidBucketItem)) {
			if (BlockRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

			previousBeValue = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(1);

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(BlockRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(blockItem.getBlock().defaultBlockState(), 0));
		} else {
			ResourceLocation location = BuiltInRegistries.ITEM.getKey(pItemRenderer0.getItem());

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(BlockRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId(location.getNamespace(), location.getPath())));
		}
	}

	@Inject(method = "render", at = @At(value = "RETURN"))
	private void changeId3(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(previousBeValue);
		previousBeValue = 0;
	}
}
