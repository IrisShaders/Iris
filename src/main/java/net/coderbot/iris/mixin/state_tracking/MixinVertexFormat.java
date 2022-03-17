package net.coderbot.iris.mixin.state_tracking;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gbuffer_overrides.state.StateTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexFormat.class)
public class MixinVertexFormat {
	@Shadow
	@Final
	private ImmutableList<VertexFormatElement> elements;

	private boolean iris$hasUv0;
	private boolean iris$hasUv1;
	private boolean iris$hasUv2;

	@Inject(method = "<init>(Lcom/google/common/collect/ImmutableList;)V", at = @At("RETURN"))
	private void iris$onInit(ImmutableList<VertexFormatElement> constructorElements, CallbackInfo ci) {
		iris$hasUv0 = elements.contains(DefaultVertexFormat.ELEMENT_UV0);
		iris$hasUv1 = elements.contains(DefaultVertexFormat.ELEMENT_UV1);
		iris$hasUv2 = elements.contains(DefaultVertexFormat.ELEMENT_UV2);
	}

	@Inject(method = "setupBufferState", at = @At("RETURN"), cancellable = true)
	private void iris$onSetupBufferState(long pointer, CallbackInfo ci) {
		StateTracker.INSTANCE.texAttribute = iris$hasUv0;
		StateTracker.INSTANCE.lightmapAttribute = iris$hasUv2;
		StateTracker.INSTANCE.overlayAttribute = iris$hasUv1;

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "clearBufferState", at = @At("RETURN"), cancellable = true)
	private void iris$onClearBufferState(CallbackInfo ci) {
		StateTracker.INSTANCE.texAttribute = false;
		StateTracker.INSTANCE.lightmapAttribute = false;
		StateTracker.INSTANCE.overlayAttribute = false;

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}
}
