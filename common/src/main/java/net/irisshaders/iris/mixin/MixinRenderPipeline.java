package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mixin(RenderPipeline.class)
public class MixinRenderPipeline {
	@Unique
	private VertexFormat iris$cachedExtendedFormat;

	@Unique
	private List<VertexFormat> iris$cachedExtendedBindings;

	@Inject(method = "getVertexFormatBinding", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (iris$shouldExtend()) {
			VertexFormat extended = iris$getExtendedFormat(cir.getReturnValue());
			if (extended != null) {
				cir.setReturnValue(extended);
			}
		}
	}

	@Inject(method = "getVertexFormatBindings", at = @At("RETURN"), cancellable = true)
	private void iris$change2(CallbackInfoReturnable<List<VertexFormat>> cir) {
		if (iris$shouldExtend()) {
			List<VertexFormat> bindings = cir.getReturnValue();
			VertexFormat extended = iris$getExtendedFormat(bindings.get(0));
			if (extended != null) {
				if (iris$cachedExtendedFormat != extended) {
					List<VertexFormat> copy = new ArrayList<>(bindings);
					copy.set(0, extended);
					iris$cachedExtendedBindings = Collections.unmodifiableList(copy);
					iris$cachedExtendedFormat = extended;
				}

				cir.setReturnValue(iris$cachedExtendedBindings);
			}
		}
	}

	@Unique
	private static boolean iris$shouldExtend() {
		return Iris.isPackInUseQuick() && Thread.currentThread().getName().contains("Render") && ImmediateState.isRenderingLevel && !ImmediateState.skipExtension.get();
	}

	@Unique
	private VertexFormat iris$getExtendedFormat(VertexFormat vf) {
		if (Objects.equals(vf, DefaultVertexFormat.BLOCK)) {
			return IrisVertexFormats.TERRAIN;
		} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)) {
			return IrisVertexFormats.GLYPH;
		} else if (Objects.equals(vf, DefaultVertexFormat.POSITION_TEX_COLOR) && ((Object) this).equals(RenderPipelines.TEXT_SEE_THROUGH)) {
			return IrisVertexFormats.GLYPH;
		} else if (Objects.equals(vf, DefaultVertexFormat.ENTITY)) {
			return IrisVertexFormats.ENTITY;
		} else if (Objects.equals(vf, DefaultVertexFormat.ENTITY_GLINT_SPECIAL)) {
			return IrisVertexFormats.ENTITY_GLINT_SPECIAL;
		} else if (Objects.equals(vf, ChunkMeshFormats.COMPACT.getVertexFormat())) {
			return WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		return null;
	}
}
