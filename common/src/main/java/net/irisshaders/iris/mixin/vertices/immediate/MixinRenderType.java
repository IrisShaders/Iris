package net.irisshaders.iris.mixin.vertices.immediate;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class MixinRenderType {
	@Inject(method = "format", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (Iris.isPackInUseQuick() && ImmediateState.renderWithExtendedVertexFormat && ImmediateState.isRenderingLevel) {
			VertexFormat vf = cir.getReturnValue();
			RenderType thiss = (RenderType) (Object) this;
			if (vf.equals(DefaultVertexFormat.BLOCK)) {
				cir.setReturnValue(IrisVertexFormats.TERRAIN);
			} else if (vf.equals(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)) {
				cir.setReturnValue(IrisVertexFormats.GLYPH);
			} else if (vf.equals(DefaultVertexFormat.ENTITY)) {
				cir.setReturnValue(IrisVertexFormats.ENTITY);
			}
		}
	}
}
