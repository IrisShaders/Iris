package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.platform.CompareOp;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import org.lwjgl.opengl.GL43;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlConst.class)
public class UndoReverseZThree {
	@Inject(method = "toGl(Lcom/mojang/blaze3d/platform/CompareOp;)I", at = @At("HEAD"), cancellable = true)
	private static void iris$to(CompareOp compareOp, CallbackInfoReturnable<Integer> cir) {
        if (!(Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel)) return;

		cir.setReturnValue(switch (compareOp) {
			case ALWAYS_PASS -> 519;
			case LESS_THAN -> GL43.GL_GREATER;
			case LESS_THAN_OR_EQUAL -> GL43.GL_GEQUAL;
			case EQUAL -> 514;
			case NOT_EQUAL -> 517;
			case GREATER_THAN_OR_EQUAL -> GL43.GL_LEQUAL;
			case GREATER_THAN -> GL43.GL_LESS;
			case NEVER_PASS -> 512;
		});
	}
}
