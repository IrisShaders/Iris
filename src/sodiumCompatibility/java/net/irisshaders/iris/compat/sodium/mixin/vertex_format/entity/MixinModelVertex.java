package net.irisshaders.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelVertex.class)
public class MixinModelVertex {
	@Inject(method = "writeQuadVertices", at = @At("HEAD"), cancellable = true)
	private static void redirect2(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int light, int overlay, int color, CallbackInfo ci) {
		if (shouldBeExtended()) {
			ci.cancel();
			EntityVertex.writeQuadVertices(writer, matrices, quad, light, overlay, color);
		}
	}

	private static boolean shouldBeExtended() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
}
