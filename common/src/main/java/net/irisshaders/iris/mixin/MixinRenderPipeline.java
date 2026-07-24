package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

@Mixin(RenderPipeline.class)
public class MixinRenderPipeline {
	@Inject(method = "getVertexFormatBinding", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
			VertexFormat vf = cir.getReturnValue();
			RenderPipeline thiss = (RenderPipeline) (Object) this;
			if (Objects.equals(vf, DefaultVertexFormat.BLOCK)) {
				cir.setReturnValue(IrisVertexFormats.TERRAIN);
			} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)) {
				cir.setReturnValue(IrisVertexFormats.GLYPH);
			} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_COLOR)) {
				cir.setReturnValue(IrisVertexFormats.GLYPH);
			} else if (Objects.equals(vf, DefaultVertexFormat.ENTITY)) {
				cir.setReturnValue(IrisVertexFormats.ENTITY);
			} else if (Objects.equals(vf, ChunkMeshFormats.COMPACT.getVertexFormat())) {
				cir.setReturnValue(WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat());
			}
		}
	}
	@Inject(method = "getVertexFormatBindings", at = @At("RETURN"), cancellable = true)
	private void iris$change2(CallbackInfoReturnable<VertexFormat[]> cir) {
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
			VertexFormat vf = cir.getReturnValue()[0];
			RenderPipeline thiss = (RenderPipeline) (Object) this;
			if (Objects.equals(vf, DefaultVertexFormat.BLOCK)) {
				cir.setReturnValue(new VertexFormat[] { IrisVertexFormats.TERRAIN });
			} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)) {
				cir.setReturnValue(new VertexFormat[] { IrisVertexFormats.GLYPH });
			} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_COLOR)) {
				cir.setReturnValue(new VertexFormat[] { IrisVertexFormats.GLYPH });
			} else if (Objects.equals(vf, DefaultVertexFormat.ENTITY)) {
				cir.setReturnValue(new VertexFormat[] { IrisVertexFormats.ENTITY });
			} else if (Objects.equals(vf, ChunkMeshFormats.COMPACT.getVertexFormat())) {
                cir.setReturnValue(new VertexFormat[] { WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat() });
            }
		}
	}
}
