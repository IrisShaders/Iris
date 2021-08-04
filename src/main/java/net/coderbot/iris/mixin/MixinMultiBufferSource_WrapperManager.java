package net.coderbot.iris.mixin;

import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.IsBlockEntityRenderPhase;
import net.coderbot.iris.layer.IsEntityRenderPhase;
import net.coderbot.iris.layer.InnerWrappedRenderLayer;
import net.coderbot.iris.layer.OuterWrappedRenderLayer;
import net.coderbot.iris.layer.WrappableRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@Mixin(MultiBufferSource.BufferSource.class)
public class MixinMultiBufferSource_WrapperManager {
	@Shadow
	@Final
	protected Map<RenderType, BufferBuilder> fixedBuffers;

	@Shadow
	public void endBatch(RenderType layer) {
		throw new AssertionError();
	}

	// A set of wrapped layers - layers that have corresponding wrappers
	@Unique
	private final Set<RenderType> wrapped = new HashSet<>();

	// A set of wrapper layers - layers that wrap an existing layer to provide additional information
	@Unique
	private final Set<RenderType> wrappers = new HashSet<>();

	// Maps a wrapped layer to its list of wrapper layers
	@Unique
	private final Map<RenderType, List<RenderType>> wrappedToWrapper = new HashMap<>();

	@Inject(method = "getBuffer",
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;fixedBuffers:Ljava/util/Map;"))
	private void iris$fillInWrappers(RenderType layer, CallbackInfoReturnable<VertexConsumer> cir) {
		ensureBuffersPresent(layer);
	}

	@Inject(method = "getBuilderRaw", at = @At("HEAD"))
	private void iris$onGetBuffer(RenderType layer, CallbackInfoReturnable<BufferBuilder> cir) {
		ensureBuffersPresent(layer);
	}

	// Ensure that when Minecraft explicitly flushes a buffer, its corresponding wrapped buffers are flushed too.
	// This might avoid rendering issues.
	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("RETURN"))
	private void iris$onExplicitDraw(RenderType layer, CallbackInfo callback) {
		ensureBuffersPresent(layer);

		List<RenderType> correspondingWrappers = wrappedToWrapper.get(layer);

		if (correspondingWrappers == null) {
			return;
		}

		for (RenderType wrapper : correspondingWrappers) {
			endBatch(Objects.requireNonNull(wrapper));
		}
	}

	@Inject(method = "endBatch()V", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;fixedBuffers:Ljava/util/Map;"))
	private void iris$onFullFlush(CallbackInfo callback) {
		// Ensure that all corresponding buffers are filled in now.
		// This avoids us mutating layerBuffers while iterating through it.
		Set<RenderType> bufferedLayers = new HashSet<>(fixedBuffers.keySet());

		for (RenderType buffered : bufferedLayers) {
			ensureBuffersPresent(buffered);
		}
	}

	@Unique
	private void ensureBuffersPresent(RenderType layer) {
		if (fixedBuffers.containsKey(layer) && !wrappers.contains(layer)) {
			// If this is a buffered layer that isn't a wrapper itself, add the corresponding wrapped buffers.
			ensureWrapped(layer);
		} else if (layer instanceof WrappableRenderLayer) {
			// If this is a wrapper, try to unwrap it to find the base layer.
			RenderType unwrapped = ((WrappableRenderLayer) layer).unwrap();

			if (unwrapped != layer && unwrapped != null) {
				ensureBuffersPresent(unwrapped);
			}
		}
	}

	@Unique
	private void ensureWrapped(RenderType base) {
		// add() returns true if wrapped didn't contain the layer already
		// If this layer is already wrapped, we don't try to add wrapped buffer variants for it.
		if (!wrapped.add(base)) {
			return;
		}

		Objects.requireNonNull(base);

		iris$addWrappedBuffer(base, iris$wrapWithIsEntity(base));
		iris$addWrappedBuffer(base, iris$wrapWithIsBlockEntity(base));
		iris$addWrappedBuffer(base, iris$wrapWithEntityColor(base, true, false));
		iris$addWrappedBuffer(base, iris$wrapWithEntityColor(base, false, true));
		iris$addWrappedBuffer(base, iris$wrapWithIsEntity(iris$wrapWithEntityColor(base, true, false)));
		iris$addWrappedBuffer(base, iris$wrapWithIsEntity(iris$wrapWithEntityColor(base, false, true)));
	}

	@Unique
	private void iris$addWrappedBuffer(RenderType base, RenderType wrapper) {
		Objects.requireNonNull(wrapper);

		// add() returns true if wrappers didn't contain the layer already
		if (wrappers.add(wrapper)) {
			fixedBuffers.put(wrapper, new BufferBuilder(wrapper.bufferSize()));
			wrappedToWrapper.computeIfAbsent(base, layer -> new ArrayList<>()).add(wrapper);
		}
	}

	@Unique
	private RenderType iris$wrapWithEntityColor(RenderType base, boolean hurt, boolean whiteFlash) {
		EntityColorRenderPhase phase = new EntityColorRenderPhase(hurt, whiteFlash ? 1.0F : 0.0F);
		return new InnerWrappedRenderLayer("iris_entity_color", base, phase);
	}

	@Unique
	private RenderType iris$wrapWithIsEntity(RenderType base) {
		return new OuterWrappedRenderLayer("iris:is_entity", base, IsEntityRenderPhase.INSTANCE);
	}

	@Unique
	private RenderType iris$wrapWithIsBlockEntity(RenderType base) {
		return new OuterWrappedRenderLayer("iris:is_block_entity", base, IsBlockEntityRenderPhase.INSTANCE);
	}
}
