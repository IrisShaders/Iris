package net.coderbot.iris.compat.sodium.mixin.copyEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.vertex.VertexConsumerUtils;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin {
	@Shadow public float x;
	@Shadow public float y;
	@Shadow public float z;

	@Shadow public float yRot;
	@Shadow public float xRot;
	@Shadow public float zRot;

	@Shadow public float xScale;
	@Shadow public float yScale;
	@Shadow public float zScale;

	@Unique
	private ModelCuboid[] sodium$cuboids;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(List<ModelPart.Cube> cuboids, Map<String, ModelPart> children, CallbackInfo ci) {
		var copies = new ModelCuboid[cuboids.size()];

		for (int i = 0; i < cuboids.size(); i++) {
			var accessor = (ModelCuboidAccessor) cuboids.get(i);
			copies[i] = accessor.sodium$copy();
		}

		this.sodium$cuboids = copies;
	}

	/**
	 * @author JellySquid
	 * @reason Use optimized vertex writer, avoid allocations, use quick matrix transformations
	 */
	@Inject(method = "compile", at = @At("HEAD"), cancellable = true)
	private void renderCuboidsFast(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		var writer = VertexConsumerUtils.convertOrLog(vertexConsumer);
		if(writer == null) {
			return;
		}

		ci.cancel();

		int color = ColorABGR.pack(red, green, blue, alpha);

		for (ModelCuboid cuboid : this.sodium$cuboids) {
			cuboid.updateVertices(matrices.pose());

			try (MemoryStack stack = MemoryStack.stackPush()) {
				long buffer = stack.nmalloc(4 * 6 * ModelVertex.STRIDE);
				long ptr = buffer;

				int count = 0;

				for (ModelCuboid.Quad quad : cuboid.quads) {
					if (quad == null) continue;

					var normal = quad.getNormal(matrices.normal());

					for (int i = 0; i < 4; i++) {
						var pos = quad.positions[i];
						var tex = quad.textures[i];

						ModelVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, overlay, light, normal);

						ptr += ModelVertex.STRIDE;
					}

					count += 4;
				}

				writer.push(stack, buffer, count, ModelVertex.FORMAT);
			}
		}
	}

	/**
	 * @author JellySquid
	 * @reason Apply transform more quickly
	 */
	@Overwrite
	public void translateAndRotate(PoseStack matrices) {
		matrices.translate(this.x * (1.0F / 16.0F), this.y * (1.0F / 16.0F), this.z * (1.0F / 16.0F));

		if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
			MatrixHelper.rotateZYX(matrices.last(), this.zRot, this.yRot, this.xRot);
		}

		if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
			matrices.scale(this.xScale, this.yScale, this.zScale);
		}
	}
}
