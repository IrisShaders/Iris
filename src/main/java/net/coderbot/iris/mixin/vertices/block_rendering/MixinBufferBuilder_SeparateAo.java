package net.coderbot.iris.mixin.vertices.block_rendering;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;

/**
 * Allows directional shading and ambient occlusion data to be stored separately in the vertex format.
 *
 * By default, directional shading and ambient occlusion lighting coefficients are pre-multiplied into the vertex color
 * RGB. However, this causes issues with shader packs which would like to operate on this data separately from the
 * actual vertex color, which is generally Minecraft's built-in block tinting such as water color and foliage color.
 *
 * Since the alpha field of the vertex color is unused for blocks (always set to 1.0), it is possible to use the alpha
 * field to store the directional shading / ambient occlusion coefficient for each vertex. This mixin implements that
 * behavior, though conditionally controlled by the current shader pack of course.
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder_SeparateAo extends FixedColorVertexConsumer {
	@Unique
	private float[] brightnesses;

	@Unique
	private int brightnessIndex;

	@Override
	public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green,
					 float blue, int[] lights, int overlay, boolean useQuadColorData) {
		if (BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			this.brightnesses = brightnesses;
			this.brightnessIndex = 0;

			brightnesses = new float[brightnesses.length];
			Arrays.fill(brightnesses, 1.0f);
		}

		super.quad(matrixEntry, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v,
					   int overlay, int light, float normalX, float normalY, float normalZ) {
		float ao = 1.0f;

		if (brightnesses != null && BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			if (brightnessIndex < brightnesses.length) {
				ao = brightnesses[brightnessIndex++];
			} else {
				brightnesses = null;
			}
		}

		super.vertex(x, y, z, red, green, blue, ao, u, v, overlay, light, normalX, normalY, normalZ);
	}
}
