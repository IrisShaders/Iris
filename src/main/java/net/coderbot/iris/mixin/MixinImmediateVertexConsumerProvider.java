package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(VertexConsumerProvider.Immediate.class)
public class MixinImmediateVertexConsumerProvider {
	@Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("HEAD"))
	private void iris$beginDraw(RenderLayer layer, CallbackInfo callback) {
		Iris.getPipeline().beginImmediateDrawing(layer);
	}

	@Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("RETURN"))
	private void iris$endDraw(RenderLayer layer, CallbackInfo callback) {
		Iris.getPipeline().endImmediateDrawing();
	}
}
