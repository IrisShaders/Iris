package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Apply after Sodium's mixins so that we can mix in to the added method. We do this so that we have the option to
 * use the non-extended vertex format in some cases even if shaders are enabled, without assumptions in the sodium
 * compatibility code getting in the way.
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 1010)
@Pseudo
public class MixinBufferBuilder_ExtendedVertexFormatCompat {
	@Unique
	private static final int SHADOW_COLOR = ColorABGR.pack(1.0f, 1.0f, 1.0f);

	@Unique
	private static final Vector3fc SHADOW_NORMAL = new Vector3f(0.0f, 1.0f, 0.0f);

	@Overwrite(remap = false)
	private static void drawOptimizedShadowVertex(PoseStack.Pose entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
		var writer = VertexBufferWriter.of(vertices);

		var matNormal = entry.normal();
		var matPosition = entry.pose();

		boolean extend = shouldBeExtended();

		float nx = SHADOW_NORMAL.x();
		float ny = SHADOW_NORMAL.y();
		float nz = SHADOW_NORMAL.z();

		// The transformed normal vector
		float nxt = (matNormal.m00() * nx) + (matNormal.m10() * ny) + (matNormal.m20() * nz);
		float nyt = (matNormal.m01() * nx) + (matNormal.m11() * ny) + (matNormal.m21() * nz);
		float nzt = (matNormal.m02() * nx) + (matNormal.m12() * ny) + (matNormal.m22() * nz);

		int norm = Norm3b.pack(nxt, nyt, nzt);

		try (MemoryStack stack = VertexBufferWriter.STACK.push()) {
			long buffer = writer.buffer(stack, 1, extend ? EntityVertex.STRIDE : ModelVertex.STRIDE, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);

			// The transformed position vector
			float xt = (matPosition.m00() * x) + (matPosition.m10() * y) + (matPosition.m20() * z) + matPosition.m30();
			float yt = (matPosition.m01() * x) + (matPosition.m11() * y) + (matPosition.m21() * z) + matPosition.m31();
			float zt = (matPosition.m02() * x) + (matPosition.m12() * y) + (matPosition.m22() * z) + matPosition.m32();

			ModelVertex.write(buffer, xt, yt, zt, ColorABGR.withAlpha(SHADOW_COLOR, alpha), u, v, OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, norm);

			writer.push(buffer, 1, extend ? EntityVertex.STRIDE : ModelVertex.STRIDE, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);
		}
	}
	private static boolean shouldBeExtended() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
}
