package net.irisshaders.iris.compat.sodium.mixin.separate_ao;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BufferBuilder.class, priority = 1010)
public abstract class MixinBufferBuilder_IntrinsicSeparateAo extends DefaultedVertexConsumer {
	@Shadow
	private boolean fastFormat;
	private float[] brightnessTable;
	private int brightnessIndex;

	/*@Overwrite
	public void putBulkData(PoseStack.Pose matrices, BakedQuad quad, float[] brightnessTable, float red, float green,
							float blue, int[] lights, int overlay, boolean useQuadColorData) {
		if (!this.fastFormat) {
			if (BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
				this.brightnessTable = brightnessTable;
				this.brightnessIndex = 0;

				brightnessTable = new float[brightnessTable.length];
				Arrays.fill(brightnessTable, 1.0f);
			}

			super.putBulkData(matrices, quad, brightnessTable, red, green, blue, lights, overlay, useQuadColorData);

			return;
		}

		if (this.defaultColorSet) {
			throw new IllegalStateException();
		}

		ModelQuadView quadView = (ModelQuadView) quad;

		Matrix4f positionMatrix = matrices.pose();
		Matrix3f normalMatrix = matrices.normal();

		int norm = MatrixUtil.computeNormal(normalMatrix, quad.getDirection());

		QuadVertexSink drain = VertexDrain.of(this)
			.createSink(VanillaVertexTypes.QUADS);
		drain.ensureCapacity(4);

		for (int i = 0; i < 4; i++) {
			float x = quadView.getX(i);
			float y = quadView.getY(i);
			float z = quadView.getZ(i);

			float fR;
			float fG;
			float fB;

			float brightness = brightnessTable[i];
			float alpha = 1.0F;

			if (BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
				alpha = brightness;
				if (useQuadColorData) {
					int color = quadView.getColor(i);

					float oR = ColorU8.normalize(ColorABGR.unpackRed(color));
					float oG = ColorU8.normalize(ColorABGR.unpackGreen(color));
					float oB = ColorU8.normalize(ColorABGR.unpackBlue(color));

					fR = oR * red;
					fG = oG * green;
					fB = oB * blue;
				} else {
					fR = red;
					fG = green;
					fB = blue;
				}
			} else {
				if (useQuadColorData) {
					int color = quadView.getColor(i);

					float oR = ColorU8.normalize(ColorABGR.unpackRed(color));
					float oG = ColorU8.normalize(ColorABGR.unpackGreen(color));
					float oB = ColorU8.normalize(ColorABGR.unpackBlue(color));

					fR = oR * brightness * red;
					fG = oG * brightness * green;
					fB = oB * brightness * blue;
				} else {
					fR = brightness * red;
					fG = brightness * green;
					fB = brightness * blue;
				}
			}

			float u = quadView.getTexU(i);
			float v = quadView.getTexV(i);

			int color = ColorABGR.pack(fR, fG, fB, alpha);

			Vector4f pos = new Vector4f(x, y, z, 1.0F);
			pos.transform(positionMatrix);

			drain.writeQuad(pos.x(), pos.y(), pos.z(), color, u, v, lights[i], overlay, norm);
		}

		drain.flush();
	}*/
}
