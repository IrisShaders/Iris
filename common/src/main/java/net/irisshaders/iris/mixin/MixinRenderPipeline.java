package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RenderPipeline.class)
public class MixinRenderPipeline {
	@Inject(method = "getVertexFormat", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (Iris.isPackInUseQuick() && ImmediateState.renderWithExtendedVertexFormat && ImmediateState.isRenderingLevel) {
			VertexFormat vf = cir.getReturnValue();
			RenderPipeline thiss = (RenderPipeline) (Object) this;
			if (vf == DefaultVertexFormat.BLOCK) {
				cir.setReturnValue(IrisVertexFormats.TERRAIN);
			} else if (vf == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
				cir.setReturnValue(IrisVertexFormats.GLYPH);
			} else if (vf == DefaultVertexFormat.NEW_ENTITY) {
				cir.setReturnValue(IrisVertexFormats.ENTITY);
			}
		}
	}
}
