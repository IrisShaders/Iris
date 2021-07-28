package net.coderbot.iris.mixin;

import java.util.Set;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.WrappingVertexConsumerProvider;
import net.coderbot.iris.layer.InnerWrappedRenderLayer;
import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.coderbot.iris.mixin.renderlayer.RenderPhaseAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(VertexConsumerProvider.Immediate.class)
public class MixinImmediateVertexConsumerProvider implements WrappingVertexConsumerProvider {
	@Unique
	private final Set<String> unwrapped = new ObjectOpenHashSet<>();

	@Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("HEAD"))
	private void iris$beginDraw(RenderLayer layer, CallbackInfo callback) {
		if (!(layer instanceof IrisRenderLayerWrapper) && !(layer instanceof InnerWrappedRenderLayer)) {
			String name = ((RenderPhaseAccessor) layer).getName();

			if (unwrapped.contains(name)) {
				return;
			}

			unwrapped.add(name);

			Iris.logger.warn("Iris has detected a non-wrapped render layer, it will not be rendered with the correct shader program: " + name);
		}
	}

	@Unique
	private Function<RenderLayer, RenderLayer> wrappingFunction;

	@ModifyVariable(method = "getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;",
			at = @At("HEAD"), ordinal = 0)
	private RenderLayer iris$applyWrappingFunction(RenderLayer layer) {
		if (wrappingFunction == null) {
			return layer;
		}

		return wrappingFunction.apply(layer);
	}

	@Override
	public void setWrappingFunction(Function<RenderLayer, RenderLayer> wrappingFunction) {
		this.wrappingFunction = wrappingFunction;
	}
}
