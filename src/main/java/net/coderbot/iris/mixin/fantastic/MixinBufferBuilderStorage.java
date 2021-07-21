package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.coderbot.iris.fantastic.FantasticVertexConsumerProvider;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage {
	@Unique
	private final VertexConsumerProvider.Immediate buffered = new FantasticVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private final OutlineVertexConsumerProvider outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);

	@Inject(method = "getEntityVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceEntityVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(buffered);
	}

	@Inject(method = "getEffectVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceEffectVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		// NB: We can return the same VertexConsumerProvider here as long as the block entity and its breaking animation
		// use different render layers. This seems like a sound assumption to make. This only works with our fully
		// buffered vertex consumer provider - vanilla's Immediate cannot be used here since it would try to return the
		// same buffer for the block entity and its breaking animation in many cases.
		//
		// If anything goes wrong here, Vanilla *will* catch the "duplicate delegates" error, so
		// this shouldn't cause silent bugs.
		provider.setReturnValue(buffered);
	}

	@Inject(method = "getOutlineVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineVertexConsumerProvider> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(outlineVertexConsumers);
	}

	@Override
	public void beginWorldRendering() {
		begins += 1;
	}

	@Override
	public void endWorldRendering() {
		begins -= 1;
	}
}
