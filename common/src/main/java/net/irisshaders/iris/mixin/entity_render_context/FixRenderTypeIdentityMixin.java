package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public class FixRenderTypeIdentityMixin {
	@Shadow
	private @Nullable RenderType lastRenderType;

	@Definition(id = "lastRenderType", field = "Lnet/minecraft/client/renderer/feature/RenderTypeFeatureRenderer$Group;lastRenderType:Lnet/minecraft/client/renderer/rendertype/RenderType;")
	@Expression("this.lastRenderType != ?")
	@ModifyExpressionValue(method = "getVertexBuilder", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean iris$replaceEqualityCheck(boolean original, @Local(argsOnly = true) RenderType renderType) {
		if (lastRenderType == null)
			return true;

		return !lastRenderType.getClass().equals(renderType.getClass()) || !Objects.equals(this.lastRenderType, renderType);
	}
}
