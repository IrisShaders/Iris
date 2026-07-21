package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.mixinterface.BlockModelRenderStateExtension;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelRenderState.class)
public class BlockModelRenderStateMixin implements BlockModelRenderStateExtension {
	@Unique
	private BlockState block;

	@Override
	public void setBlock(BlockState block) {
		this.block = block;
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void onRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int externalLightCoords, int overlayCoords, int outlineColor, CallbackInfo ci, @Share("lastBState") LocalIntRef ref) {
		ref.set(CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		iris$setupId(this.block);
	}

	@Inject(method = "submit", at = @At("TAIL"))
	private void onRenderEnd(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int externalLightCoords, int overlayCoords, int outlineColor, CallbackInfo ci, @Share("lastBState") LocalIntRef ref) {
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(ref.get());
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
	}

	@Unique
	private void iris$setupId(BlockState state) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(1);

		//System.out.println(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockItem.getBlock().defaultBlockState()));
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(state, 0));
	}
}
