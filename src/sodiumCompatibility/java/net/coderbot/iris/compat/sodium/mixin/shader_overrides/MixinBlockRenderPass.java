package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderPass.class)
public class MixinBlockRenderPass {
	@Shadow(remap = false)
	@Final
	@Mutable
	private float alphaCutoff;

    @Inject(method = "<init>", at = @At("RETURN"))
	public void changeAlphaCutoff(String layer, int ordinal, RenderType renderType, boolean translucent, float alphaCutoff, CallbackInfo ci) {
		if (renderType == RenderType.cutoutMipped()) {
			this.alphaCutoff = 0.1F;
		}
	}
}
