package net.irisshaders.iris.mixin;

import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.helpers.FakeChainedJsonException;
import net.minecraft.server.ChainedJsonException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChainedJsonException.class)
public class MixinChainedJsonException {
	@Inject(method = "forException", at = @At("HEAD"), cancellable = true)
	private static void iris$changeShaderParseException(Exception exception, CallbackInfoReturnable<ChainedJsonException> cir) {
		if (exception instanceof ShaderCompileException e) {
			cir.setReturnValue(new FakeChainedJsonException(e));
		}
	}
}
