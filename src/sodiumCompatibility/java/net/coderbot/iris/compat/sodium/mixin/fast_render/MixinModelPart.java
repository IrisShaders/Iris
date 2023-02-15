package net.coderbot.iris.compat.sodium.mixin.fast_render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.api.render.immediate.RenderImmediate;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.NormalHelper;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class MixinModelPart {
	private ModelCuboid[] sodium$cuboids;

	private static float rsqrt(float value) {
		if (value == 0.0f) {
			// You heard it here first, folks: 1 divided by 0 equals 1
			// In actuality, this is a workaround for normalizing a zero length vector (leaving it as zero length)
			return 1.0f;
		} else {
			return (float) (1.0 / Math.sqrt(value));
		}
	}

	private static boolean shouldExtend() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(List<ModelPart.Cube> cuboids, Map<String, ModelPart> children, CallbackInfo ci) {
		var copies = new ModelCuboid[cuboids.size()];

		for (int i = 0; i < cuboids.size(); i++) {
			var accessor = (ModelCuboidAccessor) cuboids.get(i);
			copies[i] = accessor.copy();
		}

		this.sodium$cuboids = copies;
	}

	/**
	 * @author JellySquid
	 * @reason Use optimized vertex writer, avoid allocations, use quick matrix transformations
	 */
	@Overwrite
	private void compile(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		var writer = VertexBufferWriter.of(vertexConsumer);
		int color = ColorABGR.pack(red, green, blue, alpha);

		boolean extend = shouldExtend();
		for (ModelCuboid cuboid : this.sodium$cuboids) {
			cuboid.updateVertices(matrices.pose());


			try (MemoryStack stack = RenderImmediate.VERTEX_DATA.push()) {
				long buffer = stack.nmalloc(4 * 6 * (extend ? EntityVertex.STRIDE : ModelVertex.STRIDE));
				long ptr = buffer;

				for (ModelCuboid.Quad quad : cuboid.quads) {
					if (quad == null) continue;
					var normal = quad.getNormal(matrices.normal());

					float midU = 0, midV = 0;
					int tangent = 0;

					if (extend) {
						for (int i = 0; i < 4; i++) {
							midU += quad.textures[i].x;
							midV += quad.textures[i].y;
						}

						midU *= 0.25;
						midV *= 0.25;

						tangent = getTangent(normal, quad.positions[0].x, quad.positions[0].y, quad.positions[0].z, quad.textures[0].x, quad.textures[0].y,
							quad.positions[1].x, quad.positions[1].y, quad.positions[1].z, quad.textures[1].x, quad.textures[1].y,
							quad.positions[2].x, quad.positions[2].y, quad.positions[2].z, quad.textures[2].x, quad.textures[2].y
						);
					}

					for (int i = 0; i < 4; i++) {
						var pos = quad.positions[i];
						var tex = quad.textures[i];

						if (extend) {
							EntityVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, midU, midV, overlay, light, normal, tangent);
						} else {
							ModelVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, overlay, light, normal);
						}

						ptr += extend ? EntityVertex.STRIDE : ModelVertex.STRIDE;
					}
				}

				writer.push(stack, buffer, 4 * 6, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);
			}
		}
	}

	private int getTangent(int normal, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2) {
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

		return NormalHelper.packNormal(tangentx, tangenty, tangentz, tangentW);
	}
}
