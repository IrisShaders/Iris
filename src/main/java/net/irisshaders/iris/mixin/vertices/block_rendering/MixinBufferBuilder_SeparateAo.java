package net.irisshaders.iris.mixin.vertices.block_rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;

/**
 * Allows directional shading and ambient occlusion data to be stored separately in the vertex format.
 * <p>
 * By default, directional shading and ambient occlusion lighting coefficients are pre-multiplied into the vertex color
 * RGB. However, this causes issues with shader packs which would like to operate on this data separately from the
 * actual vertex color, which is generally Minecraft's built-in block tinting such as water color and foliage color.
 * <p>
 * Since the alpha field of the vertex color is unused for blocks (always set to 1.0), it is possible to use the alpha
 * field to store the directional shading / ambient occlusion coefficient for each vertex. This mixin implements that
 * behavior, though conditionally controlled by the current shader pack of course.
 */
@Mixin(value = BufferBuilder.class, priority = 999)
public abstract class MixinBufferBuilder_SeparateAo extends DefaultedVertexConsumer {
	private float[] brightnesses;

	private int brightnessIndex;

	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green,
							float blue, int[] lights, int overlay, boolean useQuadColorData) {
		if (WorldRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			this.brightnesses = brightnesses;
			this.brightnessIndex = 0;

			brightnesses = new float[brightnesses.length];
			Arrays.fill(brightnesses, 1.0f);
		}

		super.putBulkData(matrixEntry, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
	}

	@ModifyVariable(method = "vertex", at = @At("HEAD"), index = 7, argsOnly = true)
	public float vertex(float alpha) {
		if (brightnesses != null && WorldRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			if (brightnessIndex < brightnesses.length) {
				alpha = brightnesses[brightnessIndex++];
			} else {
				brightnesses = null;
			}
		}

		return alpha;
	}
}
