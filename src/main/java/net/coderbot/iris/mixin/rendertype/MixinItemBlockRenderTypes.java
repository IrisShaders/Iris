package net.coderbot.iris.mixin.rendertype;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.layer.UseProgramRenderStateShard;
import net.coderbot.iris.pipeline.HandRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
	private static RenderType wrap(String name, RenderType wrapped, GbufferProgram program) {
		return new IrisRenderTypeWrapper(name, wrapped, new UseProgramRenderStateShard(program));
	}

	private static RenderType wrap(RenderType wrapped, GbufferProgram program) {
		String name = ((RenderStateShardAccessor) wrapped).getName();

		return wrap("iris:" + name, wrapped, program);
	}

	@Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
	private static void getRenderType(CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		if(HandRenderer.ACTIVE) {
			cir.setReturnValue(wrap(base, GbufferProgram.HAND));
		}
    }
}
