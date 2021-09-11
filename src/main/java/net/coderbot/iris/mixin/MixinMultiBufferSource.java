package net.coderbot.iris.mixin;

import java.util.Set;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.WrappingVertexConsumerProvider;
import net.coderbot.iris.layer.InnerWrappedRenderType;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.mixin.rendertype.RenderStateShardAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinMultiBufferSource implements WrappingVertexConsumerProvider {
	@Unique
	private final Set<String> unwrapped = new ObjectOpenHashSet<>();

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"))
	private void iris$beginDraw(RenderType type, CallbackInfo callback) {
		if (!(type instanceof IrisRenderTypeWrapper) && !(type instanceof InnerWrappedRenderType)) {
			String name = ((RenderStateShardAccessor) type).getName();

			if (unwrapped.contains(name)) {
				return;
			}

			unwrapped.add(name);

			Iris.logger.warn("Iris has detected a non-wrapped render type, it will not be rendered with the correct shader program: " + name);
		}
	}

	@Unique
	private Function<RenderType, RenderType> wrappingFunction;

	@ModifyVariable(method = "getBuffer",
			at = @At("HEAD"), ordinal = 0)
	private RenderType iris$applyWrappingFunction(RenderType type) {
		if (wrappingFunction == null) {
			return type;
		}

		return wrappingFunction.apply(type);
	}

	@Override
	public void pushWrappingFunction(Function<RenderType, RenderType> wrappingFunction) {
		this.wrappingFunction = wrappingFunction;
	}

	@Override
	public void popWrappingFunction() {
		this.wrappingFunction = null;
	}

	@Override
	public void assertWrapStackEmpty() {

	}
}
