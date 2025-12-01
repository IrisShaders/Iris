package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class MixinModelStorageTrigger {
	@WrapOperation(method = "submitModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V"))
	private <E> void iris$capture(ModelFeatureRenderer.Storage instance, RenderType renderType, SubmitNodeStorage.ModelSubmit<?> e, Operation<Void> original) {
		((ModelStorage) (Object) e).iris$capture();

		original.call(instance, renderType, e);
	}

	@WrapMethod(method = "submitModel")
	private <S> void iris$changeRenderType(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, @Nullable TextureAtlasSprite textureAtlasSprite, int l, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, Operation<Void> original) {
		if (ImmediateState.isRenderingBEs) {
			renderType = OuterWrappedRenderType.wrapExactlyOnce("iris:block_entity", renderType, BlockEntityRenderStateShard.INSTANCE);
		}

		original.call(model, object, poseStack, renderType, i, j, k, textureAtlasSprite, l, crumblingOverlay);
	}

	@WrapMethod(method = "submitCustomGeometry")
	private <S> void iris$changeRenderType2(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, Operation<Void> original) {
		if (ImmediateState.isRenderingBEs) {
			renderType = OuterWrappedRenderType.wrapExactlyOnce("iris:block_entity", renderType, BlockEntityRenderStateShard.INSTANCE);
		}

		original.call(poseStack, renderType, customGeometryRenderer);
	}

	@WrapOperation(method = "submitModelPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelPartFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;)V"))
	private <E> void iris$capture3(ModelPartFeatureRenderer.Storage instance, RenderType renderType, SubmitNodeStorage.ModelPartSubmit e, Operation<Void> original) {
		((ModelStorage) (Object) e).iris$capture();

		original.call(instance, renderType, e);
	}

	@WrapOperation(method = "submitItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private <E> boolean iris$capture4(List instance, E e, Operation<Boolean> original) {
		((ModelStorage) e).iris$capture();

		return original.call(instance, e);
	}

	@WrapOperation(method = "submitText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private <E> boolean iris$capture5(List instance, E e, Operation<Boolean> original) {
		((ModelStorage) e).iris$capture();

		return original.call(instance, e);
	}
}
