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

	@Inject(method = "setupBufferState", at = @At("RETURN"), cancellable = true)
	private void iris$onSetupBufferState(long pointer, CallbackInfo ci) {
		// TODO: Not efficient.
		StateTracker.INSTANCE.texAttribute = elements.contains(DefaultVertexFormat.ELEMENT_UV0);
		StateTracker.INSTANCE.lightmapAttribute = elements.contains(DefaultVertexFormat.ELEMENT_UV2);
		StateTracker.INSTANCE.overlayAttribute = elements.contains(DefaultVertexFormat.ELEMENT_UV1);

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "clearBufferState", at = @At("RETURN"), cancellable = true)
	private void iris$onClearBufferState(CallbackInfo ci) {
		// TODO: Not efficient.
		StateTracker.INSTANCE.texAttribute = false;
		StateTracker.INSTANCE.lightmapAttribute = false;
		StateTracker.INSTANCE.overlayAttribute = false;

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}
}
