package net.coderbot.iris.mixin;

import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.EntityColorWrappedRenderLayer;
import net.coderbot.iris.layer.WrappableRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vanilla depends on being able to write to some buffers at the same time as other ones.
 * This includes enchantment glints.
 *
 * We need to make sure that wrapped variants of buffered render layers are buffered too,
 * or else we'll get crashes with this approach.
 *
 * This mixin dynamically creates buffers for the wrapped variants of buffered layers.
 * This strategy is needed for mods that dynamically add buffered layers, like Charm
 * which adds colored enchantment glints.
 */
@Mixin(VertexConsumerProvider.Immediate.class)
public class MixinImmediateVertexConsumerProvider_WrapperManager {
	@Shadow
	@Final
	protected Map<RenderLayer, BufferBuilder> layerBuffers;

	@Shadow
	public void draw(RenderLayer layer) {
		throw new AssertionError();
	}

	// A set of wrapped layers - layers that have corresponding wrappers
	@Unique
	private final Set<RenderLayer> wrapped = new HashSet<>();

	// A set of wrapper layers - layers that wrap an existing layer to provide additional information
	@Unique
	private final Set<RenderLayer> wrappers = new HashSet<>();

	// Maps a wrapped layer to its list of wrapper layers
	@Unique
	private final Map<RenderLayer, List<RenderLayer>> wrappedToWrapper = new HashMap<>();

	@Inject(method = "getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;",
			at = @At(value = "FIELD", target = "net/minecraft/client/render/VertexConsumerProvider$Immediate.layerBuffers : Ljava/util/Map;"))
	private void iris$fillInWrappers(RenderLayer layer, CallbackInfoReturnable<VertexConsumer> cir) {
		ensureBuffersPresent(layer);
	}

	@Inject(method = "getBufferInternal(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/BufferBuilder;", at = @At("HEAD"))
	private void iris$onGetBuffer(RenderLayer layer, CallbackInfoReturnable<BufferBuilder> cir) {
		ensureBuffersPresent(layer);
	}

	// Ensure that when Minecraft explicitly flushes a buffer, its corresponding wrapped buffers are flushed too.
	// This might avoid rendering issues.
	@Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("RETURN"))
	private void iris$onExplicitDraw(RenderLayer layer, CallbackInfo callback) {
		ensureBuffersPresent(layer);

		List<RenderLayer> correspondingWrappers = wrappedToWrapper.get(layer);

		if (correspondingWrappers == null) {
			return;
		}

		for (RenderLayer wrapper : correspondingWrappers) {
			draw(wrapper);
		}
	}

	@Unique
	private void ensureBuffersPresent(RenderLayer layer) {
		if (layerBuffers.containsKey(layer) && !wrappers.contains(layer)) {
			// If this is a buffered layer that isn't a wrapper itself, add the corresponding wrapped buffers.
			ensureWrapped(layer);
		} else if (layer instanceof WrappableRenderLayer) {
			// If this is a wrapper, try to unwrap it to find the base layer.
			RenderLayer unwrapped = ((WrappableRenderLayer) layer).unwrap();

			if (unwrapped != layer) {
				ensureBuffersPresent(unwrapped);
			}
		}
	}

	@Unique
	private void ensureWrapped(RenderLayer base) {
		// add() returns true if wrapped didn't contain the layer already
		// If this layer is already wrapped, we don't try to add wrapped buffer variants for it.
		if (!wrapped.add(base)) {
			return;
		}

		iris$addWrappedBuffer(base, iris$wrapWithEntityColor(base, true, false));
		iris$addWrappedBuffer(base, iris$wrapWithEntityColor(base, false, true));
	}

	@Unique
	private void iris$addWrappedBuffer(RenderLayer base, RenderLayer wrapper) {
		// add() returns true if wrappers didn't contain the layer already
		if (wrappers.add(wrapper)) {
			layerBuffers.put(wrapper, new BufferBuilder(wrapper.getExpectedBufferSize()));
			wrappedToWrapper.computeIfAbsent(base, layer -> new ArrayList<>()).add(wrapper);
		}
	}

	@Unique
	private RenderLayer iris$wrapWithEntityColor(RenderLayer base, boolean hurt, boolean whiteFlash) {
		EntityColorRenderPhase phase = new EntityColorRenderPhase(hurt, whiteFlash ? 1.0F : 0.0F);
		return new EntityColorWrappedRenderLayer("iris_entity_color", base, phase);
	}
}
