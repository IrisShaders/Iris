package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.render.RenderGlobal;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.common.util.MatrixHelper;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityRenderDispatcher.class, priority = 1010)
public class MixinEntityRenderDispatcher {
	@Unique
	private static final int SHADOW_COLOR = ColorABGR.pack(1.0f, 1.0f, 1.0f);
	
	/**
	 * @author IMS
	 * @reason Overwrite shadow rendering with our own
	 */
	@Overwrite(remap = false)
	private static void renderShadowPart(PoseStack.Pose matrices, VertexConsumer vertices, float radius, float alpha, float minX, float maxX, float minY, float minZ, float maxZ) {
		float size = 0.5F * (1.0F / radius);

		float u1 = (-minX * size) + 0.5F;
		float u2 = (-maxX * size) + 0.5F;

		float v1 = (-minZ * size) + 0.5F;
		float v2 = (-maxZ * size) + 0.5F;

		var matNormal = matrices.normal();
		var matPosition = matrices.pose();

		var color = ColorABGR.withAlpha(SHADOW_COLOR, alpha);
		var normal = MatrixHelper.transformNormal(matNormal, 0.0f, 1.0f, 0.0f);

		boolean extended = shouldBeExtended();

		int stride = extended ? EntityVertex.STRIDE : ModelVertex.STRIDE;
		try (MemoryStack stack = RenderGlobal.VERTEX_DATA.push()) {
			long buffer = stack.nmalloc(4 * stride);
			long ptr = buffer;

			if (extended) {
				writeShadowVertexIris(ptr, matPosition, minX, minY, minZ, u1, v1, color, normal);
				ptr += stride;

				writeShadowVertexIris(ptr, matPosition, minX, minY, maxZ, u1, v2, color, normal);
				ptr += stride;

				writeShadowVertexIris(ptr, matPosition, maxX, minY, maxZ, u2, v2, color, normal);
				ptr += stride;

				writeShadowVertexIris(ptr, matPosition, maxX, minY, minZ, u2, v1, color, normal);
				ptr += stride;
			} else {
				writeShadowVertex(ptr, matPosition, minX, minY, minZ, u1, v1, color, normal);
				ptr += stride;

				writeShadowVertex(ptr, matPosition, minX, minY, maxZ, u1, v2, color, normal);
				ptr += stride;

				writeShadowVertex(ptr, matPosition, maxX, minY, maxZ, u2, v2, color, normal);
				ptr += stride;

				writeShadowVertex(ptr, matPosition, maxX, minY, minZ, u2, v1, color, normal);
				ptr += stride;
			}

			VertexBufferWriter.of(vertices)
				.push(stack, buffer, 4, extended ? EntityVertex.FORMAT : ModelVertex.FORMAT);
		}
	}

	private static void writeShadowVertexIris(long ptr, Matrix4f matPosition, float x, float y, float z, float u, float v, int color, int normal) {
		// The transformed position vector
		float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
		float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
		float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

		EntityVertex.write(ptr, xt, yt, zt, color, u, v, 0.5f, 0.5f, OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, normal);
	}

	private static void writeShadowVertex(long ptr, Matrix4f matPosition, float x, float y, float z, float u, float v, int color, int normal) {
		// The transformed position vector
		float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
		float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
		float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

		ModelVertex.write(ptr, xt, yt, zt, color, u, v, OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, normal);
	}

	private static boolean shouldBeExtended() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
}
