package net.coderbot.iris.mixin;

import net.coderbot.iris.vertices.ExtendedBufferBuilder;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockBufferBuilderStorage.class)
public class MixinBlockBufferBuilderStorage {
	@Shadow
	@Final
	private Map<RenderLayer, BufferBuilder> builders;

	@Inject(method = "get(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/BufferBuilder;", at = @At("RETURN"), cancellable = true)
	public void iris$extendBufferBuilder(RenderLayer layer, CallbackInfoReturnable<BufferBuilder> cir) {
		if (!(cir.getReturnValue() instanceof ExtendedBufferBuilder)) {
			ExtendedBufferBuilder extended = new ExtendedBufferBuilder(layer.getExpectedBufferSize());

			builders.put(layer, extended);

			cir.setReturnValue(extended);
		}
	}
}
