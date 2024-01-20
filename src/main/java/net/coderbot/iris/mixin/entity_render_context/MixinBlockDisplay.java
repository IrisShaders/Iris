package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.BlockDisplayRenderer.class)
public class MixinBlockDisplay {
	@Unique
	private int previousBeValue;
	@Inject(method = "renderInner(Lnet/minecraft/world/entity/Display$BlockDisplay;Lnet/minecraft/world/entity/Display$BlockDisplay$BlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At("HEAD"))
	private void iris$setId(Display.BlockDisplay pDisplayRenderer$BlockDisplayRenderer0, Display.BlockDisplay.BlockRenderState pDisplay$BlockDisplay$BlockRenderState1, PoseStack pPoseStack2, MultiBufferSource pMultiBufferSource3, int pInt4, float pFloat5, CallbackInfo ci) {
		if (pDisplay$BlockDisplay$BlockRenderState1.blockState() != null) {

			previousBeValue = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(1);

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(BlockRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(pDisplay$BlockDisplay$BlockRenderState1.blockState(), 0));
		}
	}

	@Inject(method = "renderInner(Lnet/minecraft/world/entity/Display$BlockDisplay;Lnet/minecraft/world/entity/Display$BlockDisplay$BlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At("RETURN"))
	private void iris$resetId(Display.BlockDisplay pDisplayRenderer$BlockDisplayRenderer0, Display.BlockDisplay.BlockRenderState pDisplay$BlockDisplay$BlockRenderState1, PoseStack pPoseStack2, MultiBufferSource pMultiBufferSource3, int pInt4, float pFloat5, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(previousBeValue);
	}

}
