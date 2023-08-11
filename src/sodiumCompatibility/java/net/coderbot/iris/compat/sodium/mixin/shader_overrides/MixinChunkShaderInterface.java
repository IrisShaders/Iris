package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShaderInterface.class)
public class MixinChunkShaderInterface {
	@Inject(method = "setupState", at = @At("TAIL"))
	private void iris$reloadMultibind(CallbackInfo ci) {
		GlStateManager._logicOp(91384);
	}
}
