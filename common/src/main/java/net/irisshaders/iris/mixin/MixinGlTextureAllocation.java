package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GlDevice.class)
public class MixinGlTextureAllocation {
	@ModifyArgs(method = "createTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_texImage2D(IIIIIIIILjava/nio/ByteBuffer;)V"))
	private void iris$clampMipDimensions(Args args) {
		args.set(3, Math.max(1, args.get(3)));
		args.set(4, Math.max(1, args.get(4)));
	}
}
