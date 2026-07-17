package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class MixinModelStorageTrigger {
	@Inject(method = "submitModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;waterMask()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
	private <S> void iris$capture(Model<? super S> model, Object state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, UvMapping uvMapping, int outlineColor, CallbackInfo ci, @Local ModelFeatureRenderer.Submit<S> submit) {
		((ModelStorage) (Object) submit).iris$capture();
	}

	@WrapOperation(method = "submitBlockModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderType;hasBlending()Z"))
	private <S> boolean iris$capture6(RenderType instance, Operation<Boolean> original, @Local BlockModelFeatureRenderer.Submit submit) {
		((ModelStorage) (Object) submit).iris$capture();
		return instance.hasBlending();
	}


	@WrapOperation(method = "submitCustomGeometry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderType;hasBlending()Z"))
	private <S> boolean iris$capture7(RenderType instance, Operation<Boolean> original, @Local CustomFeatureRenderer.Submit submit) {
		((ModelStorage) (Object) submit).iris$capture();
		return instance.hasBlending();
	}

	@WrapMethod(method = "submitModel")
	private <S> void iris$changeRenderType(Model<? super S> model, Object state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, UvMapping uvMapping, int outlineColor, Operation<Void> original) {
		if (ImmediateState.isRenderingBEs) {
			renderType = OuterWrappedRenderType.wrapExactlyOnce("iris:block_entity", renderType, BlockEntityRenderStateShard.INSTANCE);
		}

		original.call(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, uvMapping, outlineColor);
	}

	@WrapMethod(method = "submitCustomGeometry")
	private <S> void iris$changeRenderType2(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, Operation<Void> original) {
		if (ImmediateState.isRenderingBEs) {
			renderType = OuterWrappedRenderType.wrapExactlyOnce("iris:block_entity", renderType, BlockEntityRenderStateShard.INSTANCE);
		}

		original.call(poseStack, renderType, customGeometryRenderer);
	}

	@WrapOperation(method = "submitItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"))
	private <E> void iris$capture4(SimpleFeatureRenderPhase instance, SubmitNode submit, Operation<Void> original) {
		((ModelStorage) (Object) submit).iris$capture();
		original.call(instance, submit);
	}

	@WrapOperation(method = "submitText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"))
	private <E> void iris$capture5(SimpleFeatureRenderPhase instance, SubmitNode submit, Operation<Void> original) {
		((ModelStorage) (Object) submit).iris$capture();

		original.call(instance, submit);
	}
}
