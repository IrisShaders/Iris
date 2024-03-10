package net.irisshaders.iris.compat.sodium.mixin.vertex_format.entity;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.NormI8;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityRenderDispatcher.class, priority = 1010)
public class MixinEntityRenderDispatcher {
	@Unique
	private static final int SHADOW_COLOR = ColorABGR.pack(1.0f, 1.0f, 1.0f);

	/**
	 * @author IMS
	 * @reason Overwrite shadow rendering with our own
	 */
	private static void writeShadowVertexIris(long ptr, Matrix4f matPosition, float x, float y, float z, float u, float v, int color, float midU, float midV, int normal, int tangent) {
		// The transformed position vector
		float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
		float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
		float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

		EntityVertex.write(ptr, xt, yt, zt, color, u, v, midU, midV, OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, normal, tangent);
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

	private static int getTangent(int normal, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2) {
		// Capture all of the relevant vertex positions

		float normalX = NormI8.unpackX(normal);
		float normalY = NormI8.unpackY(normal);
		float normalZ = NormI8.unpackZ(normal);

		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		// Be very careful when writing out complex multi-step calculations
		// such as vector cross products! The calculation for pbitangentz
		// used to be broken because it multiplied values in the wrong order.

		float pbitangentx = tangenty * normalZ - tangentz * normalY;
		float pbitangenty = tangentz * normalX - tangentx * normalZ;
		float pbitangentz = tangentx * normalY - tangenty * normalX;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		return NormI8.pack(tangentx, tangenty, tangentz, tangentW);
	}

	private static float rsqrt(float value) {
		if (value == 0.0f) {
			// You heard it here first, folks: 1 divided by 0 equals 1
			// In actuality, this is a workaround for normalizing a zero length vector (leaving it as zero length)
			return 1.0f;
		} else {
			return (float) (1.0 / Math.sqrt(value));
		}
	}
}
