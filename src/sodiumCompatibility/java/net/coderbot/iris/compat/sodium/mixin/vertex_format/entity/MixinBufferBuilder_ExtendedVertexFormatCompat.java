package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Apply after Sodium's mixins so that we can mix in to the added method. We do this so that we have the option to
 * use the non-extended vertex format in some cases even if shaders are enabled, without assumptions in the sodium
 * compatibility code getting in the way.
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 1010)
public class MixinBufferBuilder_ExtendedVertexFormatCompat {
	@SuppressWarnings("target")
	@Redirect(method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;drawOptimizedShadowVertex(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/vertex/formats/ModelVertex;write(JFFFIFFIII)V"), remap = false)
	private static void redirectWrite(long ptr,
							   float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
		if (shouldBeExtended()) {
			EntityVertex.write(ptr, x, y, z, color, u, v, 0.5f, 0.5f, light, overlay, normal);
		} else {
			ModelVertex.write(ptr, x, y, z, color, u, v, light, overlay, normal);
		}
	}

	@SuppressWarnings("target")
	@ModifyArg(method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;drawOptimizedShadowVertex(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/vertex/VertexBufferWriter;buffer(Lorg/lwjgl/system/MemoryStack;IILme/jellysquid/mods/sodium/client/render/vertex/VertexFormatDescription;)J"), index = 2, remap = false)
	private static int redirectWrite2(int before) {
		if (shouldBeExtended()) {
			return EntityVertex.STRIDE;
		} else {
			return ModelVertex.STRIDE;
		}
	}

	@SuppressWarnings("target")
	@ModifyArg(method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;drawOptimizedShadowVertex(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/vertex/VertexBufferWriter;buffer(Lorg/lwjgl/system/MemoryStack;IILme/jellysquid/mods/sodium/client/render/vertex/VertexFormatDescription;)J"), index = 3, remap = false)
	private static VertexFormatDescription redirectWrite3(VertexFormatDescription before) {
		if (shouldBeExtended()) {
			return EntityVertex.FORMAT;
		} else {
			return ModelVertex.FORMAT;
		}
	}

	@SuppressWarnings("target")
	@ModifyArg(method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;drawOptimizedShadowVertex(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/vertex/VertexBufferWriter;push(JIILme/jellysquid/mods/sodium/client/render/vertex/VertexFormatDescription;)V"), index = 2, remap = false)
	private static int redirectWrite4(int before) {
		if (shouldBeExtended()) {
			return EntityVertex.STRIDE;
		} else {
			return ModelVertex.STRIDE;
		}
	}

	@SuppressWarnings("target")
	@ModifyArg(method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;drawOptimizedShadowVertex(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/vertex/VertexBufferWriter;push(JIILme/jellysquid/mods/sodium/client/render/vertex/VertexFormatDescription;)V"), index = 3, remap = false)
	private static VertexFormatDescription redirectWrite5(VertexFormatDescription before) {
		if (shouldBeExtended()) {
			return EntityVertex.FORMAT;
		} else {
			return ModelVertex.FORMAT;
		}
	}

	private static boolean shouldBeExtended() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
}
