package net.irisshaders.iris.compat.sodium.mixin.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.GlyphVertex;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.GlyphVertexExt;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Math;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedGlyph.class)
public class MixinGlyphRenderer {
	@Shadow
	@Final
	private float left;

	@Shadow
	@Final
	private float right;

	@Shadow
	@Final
	private float up;

	@Shadow
	@Final
	private float down;

	@Shadow
	@Final
	private float u0;

	@Shadow
	@Final
	private float v0;

	@Shadow
	@Final
	private float v1;

	@Shadow
	@Final
	private float u1;

	private static void write(boolean ext, long buffer,
							  Matrix4f matrix, float x, float y, float z, int color, float u, float v, int light) {
		float x2 = Math.fma(matrix.m00(), x, Math.fma(matrix.m10(), y, Math.fma(matrix.m20(), z, matrix.m30())));
		float y2 = Math.fma(matrix.m01(), x, Math.fma(matrix.m11(), y, Math.fma(matrix.m21(), z, matrix.m31())));
		float z2 = Math.fma(matrix.m02(), x, Math.fma(matrix.m12(), y, Math.fma(matrix.m22(), z, matrix.m32())));

		if (ext) {
			GlyphVertexExt.write(buffer, x2, y2, z2, color, u, v, light);
		} else {
			GlyphVertex.put(buffer, x2, y2, z2, color, u, v, light);
		}
	}

	/**
	 * @reason Use intrinsics
	 * @author JellySquid
	 */
	@Overwrite
	public void render(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
		float x1 = x + this.left;
		float x2 = x + this.right;
		float y1 = this.up - 3.0F;
		float y2 = this.down - 3.0F;
		float h1 = y + y1;
		float h2 = y + y2;
		float w1 = italic ? 1.0F - 0.25F * y1 : 0.0F;
		float w2 = italic ? 1.0F - 0.25F * y2 : 0.0F;

		int color = ColorABGR.pack(red, green, blue, alpha);

		var writer = VertexBufferWriter.of(vertexConsumer);

		boolean ext = extend();
		int stride = ext ? GlyphVertexExt.STRIDE : GlyphVertex.STRIDE;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(4 * stride);
			long ptr = buffer;

			write(ext, ptr, matrix, x1 + w1, h1, 0.0F, color, this.u0, this.v0, light);
			ptr += stride;

			write(ext, ptr, matrix, x1 + w2, h2, 0.0F, color, this.u0, this.v1, light);
			ptr += stride;

			write(ext, ptr, matrix, x2 + w2, h2, 0.0F, color, this.u1, this.v1, light);
			ptr += stride;

			write(ext, ptr, matrix, x2 + w1, h1, 0.0F, color, this.u1, this.v0, light);
			ptr += stride;

			writer.push(stack, buffer, 4, ext ? GlyphVertexExt.FORMAT : GlyphVertex.FORMAT);
		}
	}

	private boolean extend() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}

}
