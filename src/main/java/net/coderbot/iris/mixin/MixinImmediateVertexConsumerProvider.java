package net.coderbot.iris.mixin;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.coderbot.iris.mixin.renderlayer.RenderPhaseAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(VertexConsumerProvider.Immediate.class)
public class MixinImmediateVertexConsumerProvider {
	@Unique
	private Set<String> unwrapped = new ObjectOpenHashSet<>();

	@Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("HEAD"))
	private void iris$beginDraw(RenderLayer layer, CallbackInfo callback) {
		if (!(layer instanceof IrisRenderLayerWrapper)) {
			String name = ((RenderPhaseAccessor) layer).getName();

			if (unwrapped.contains(name)) {
				return;
			}

			unwrapped.add(name);

			Iris.logger.warn("Iris has detected a non-wrapped render layer, it will not be rendered with the correct shader program: " + name);
		}
	}
}
