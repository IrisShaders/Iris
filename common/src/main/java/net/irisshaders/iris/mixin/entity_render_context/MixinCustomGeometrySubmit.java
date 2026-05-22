package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeStorage.CustomGeometrySubmit.class)
public class MixinCustomGeometrySubmit implements ModelStorage {
	@Unique
	private int entityId, beId, itemId;

	@Unique
	private boolean isRenderingBEs;

	@Override
	public void iris$capture() {
		entityId = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		beId = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
		itemId = CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
		isRenderingBEs = ImmediateState.isRenderingBEs;
	}

	@Override
	public void iris$set() {
		CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(beId);
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(itemId);
	}

	@Override
	public boolean iris$wasBE() {
		return isRenderingBEs;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$capture2(PoseStack.Pose pose, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, CallbackInfo ci) {
		iris$capture();
	}
}
