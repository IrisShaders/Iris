package net.coderbot.iris.mixin.rendertype;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.layer.UseProgramRenderStateShard;
import net.coderbot.iris.pipeline.HandRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(ItemBlockRenderTypes.class)
public abstract class MixinItemBlockRenderTypes {
	@Shadow
	private static RenderType getChunkRenderType(BlockState blockState) {
		throw new UnsupportedOperationException("not shadowed");
	}

	private static RenderType wrap(String name, RenderType wrapped, GbufferProgram program) {
		return new IrisRenderTypeWrapper(name, wrapped, new UseProgramRenderStateShard(program));
	}

	private static RenderType wrap(RenderType wrapped, GbufferProgram program) {
		String name = ((RenderStateShardAccessor) wrapped).getName();

		return wrap("iris:" + name, wrapped, program);
	}

	@Inject(method = "getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("RETURN"), cancellable = true)
	private static void getRenderType(ItemStack itemStack, boolean bl, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		if(HandRenderer.isActive()) {
			//TODO: Is there a better way to do this?
			if(base == Sheets.translucentItemSheet()) {
				cir.setReturnValue(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
			} else if(base == Sheets.translucentCullBlockSheet()) {
				cir.setReturnValue(RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
			}
		}
	 }

	@Inject(method = "getRenderType(Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("RETURN"), cancellable = true)
	private static void getRenderType(BlockState blockState, boolean bl, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		if(HandRenderer.isActive()) {
			//TODO: Is there a better way to do this?
			cir.setReturnValue(wrap(base, getChunkRenderType(blockState) == RenderType.translucent() ? GbufferProgram.HAND_TRANSLUCENT : GbufferProgram.HAND));
		}
    }
}
