package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		if (WorldRenderingSettings.INSTANCE.getItemIds() == null) return;

		if (pItemRenderer0.getItem() instanceof BlockItem blockItem && !(pItemRenderer0.getItem() instanceof SolidBucketItem)) {
			if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

			previousBeValue = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(1);

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(blockItem.getBlock().defaultBlockState(), 0));
		} else {
			ResourceLocation location = BuiltInRegistries.ITEM.getKey(pItemRenderer0.getItem());

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId(location.getNamespace(), location.getPath())));
		}
	}

	@Inject(method = "render", at = @At(value = "RETURN"))
	private void changeId3(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(previousBeValue);
		previousBeValue = 0;
	}
}
