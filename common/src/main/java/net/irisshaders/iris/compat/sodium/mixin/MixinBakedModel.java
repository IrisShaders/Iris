package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.QuadPositionAccess;
import net.irisshaders.iris.pipeline.QuadPositions;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.vertices.sodium.IrisEntityVertex;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedModelEncoder.class)
public abstract class MixinBakedModel {
	@Shadow
	private static int mergeLighting(int stored, int calculated) {
		return 0;
	}

	@Inject(method = "writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;IIIZ)V", at = @At("HEAD"), cancellable = true)
	private static void redirectToIris(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay, boolean colorize, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			ci.cancel();
			writeIris(writer, matrices, quad, color, light, overlay, colorize);
		}
	}

	private static void writeIris(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay, boolean colorize) {
		Matrix3f matNormal = matrices.normal();
		Matrix4f matPosition = matrices.pose();

		QuadPositions quadPositions = ((QuadPositionAccess) quad).getQuadPosition(CapturedRenderingState.INSTANCE.getEntityRollingId());

		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(4 * IrisEntityVertex.STRIDE);
			long ptr = buffer;

			float midU = (quad.getTexU(0) + quad.getTexU(1) + quad.getTexU(2) + quad.getTexU(3)) * 0.25f;
			float midV = (quad.getTexV(0) + quad.getTexV(1) + quad.getTexV(2) + quad.getTexV(3)) * 0.25f;

			for (int i = 0; i < 4; i++) {
				// The position vector
				float x = quad.getX(i);
				float y = quad.getY(i);
				float z = quad.getZ(i);

				int newLight = mergeLighting(quad.getLight(i), light);

				int newColor = color;

				if (colorize) {
					newColor = ColorHelper.multiplyColor(newColor, quad.getColor(i));
				}

				// The packed transformed normal vector
				int normal = MatrixHelper.transformNormal(matNormal, matrices.trustedNormals, quad.getAccurateNormal(i));

				// The transformed position vector
				float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
				float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
				float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

				quadPositions.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), i, xt, yt, zt);

				// TODO TANGENT
				IrisEntityVertex.write(ptr, xt, yt, zt, quadPositions.velocityX[i], quadPositions.velocityY[i], quadPositions.velocityZ[i], newColor, quad.getTexU(i), quad.getTexV(i), overlay, newLight, normal, 0, midU, midV);
				ptr += IrisEntityVertex.STRIDE;
			}

			writer.push(stack, buffer, 4, IrisEntityVertex.FORMAT);
		}
	}
}
