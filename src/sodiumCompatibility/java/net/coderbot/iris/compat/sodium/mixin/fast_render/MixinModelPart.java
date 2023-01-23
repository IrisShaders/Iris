package net.coderbot.iris.compat.sodium.mixin.fast_render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.ModelCuboid;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
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


			try (MemoryStack stack = VertexBufferWriter.STACK.push()) {
				long buffer = writer.buffer(stack, 4 * 6, extend ? EntityVertex.STRIDE : ModelVertex.STRIDE, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);
				long ptr = buffer;

				for (ModelCuboid.Quad quad : cuboid.quads) {
					var normal = quad.getNormal(matrices.normal());

					float midU = 0, midV = 0;

					if (extend) {
						for (int i = 0; i < 4; i++) {
							midU += quad.textures[i].x;
							midV += quad.textures[i].y;
						}

						midU *= 0.25;
						midV *= 0.25;
					}

					for (int i = 0; i < 4; i++) {
						var pos = quad.positions[i];
						var tex = quad.textures[i];

						if (extend) {
							EntityVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, midU, midV, light, overlay, normal);
						} else {
							ModelVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, light, overlay, normal);
						}

						ptr += extend ? EntityVertex.STRIDE : ModelVertex.STRIDE;
					}
				}

				writer.push(buffer, 4 * 6, extend ? EntityVertex.STRIDE : ModelVertex.STRIDE, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);
			}
		}
	}

	private static boolean shouldExtend() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
}
