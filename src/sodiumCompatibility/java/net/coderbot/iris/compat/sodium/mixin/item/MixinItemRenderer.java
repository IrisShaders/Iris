package net.coderbot.iris.compat.sodium.mixin.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.color.interop.ItemColorsExtended;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import me.jellysquid.mods.sodium.client.render.vertex.VertexConsumerUtils;
import me.jellysquid.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.coderbot.iris.compat.sodium.impl.entities.IrisBakedQuad;
import net.coderbot.iris.compat.sodium.impl.entities.VertexHistory;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.NormalHelper;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemRenderer.class, priority = 1010)
public class MixinItemRenderer {
    @Unique
    private static short encodeTexture(float value) {
        return (short) (Math.min(0.99999997F, value) * 65536);
    }

    @Unique
    private static void writeQuadVerticesIris(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay) {
        Matrix3f matNormal = matrices.normal();
        Matrix4f matPosition = matrices.pose();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(4 * EntityVertex.STRIDE);
            long ptr = buffer;

            // The packed transformed normal vector
            var normal = MatrixHelper.transformNormal(matNormal, quad.getNormal());

            float midUTemp = 0.0f, midVTemp = 0.0f;

            for (int i = 0; i < 4; i++) {
                midUTemp += quad.getTexU(i);
                midVTemp += quad.getTexV(i);
            }

            midUTemp *= 0.25f;
            midVTemp *= 0.25f;

            short midU = encodeTexture(midUTemp);
            short midV = encodeTexture(midVTemp);

			VertexHistory lastPos = ((IrisBakedQuad) quad).getPrevious();

			for (int i = 0; i < 4; i++) {
                // The position vector
                float x = quad.getX(i);
                float y = quad.getY(i);
                float z = quad.getZ(i);

                // The transformed position vector
                float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
                float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
                float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);


                EntityVertex.writeUnknownTangentWithVelocity(ptr, xt, yt, zt, lastPos.storedPositions[i].x, lastPos.storedPositions[i].y, lastPos.storedPositions[i].z, color, quad.getTexU(i), quad.getTexV(i), midU, midV, light, overlay, normal);
                ptr += EntityVertex.STRIDE;

				if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) lastPos.storedPositions[i].set(xt, yt, zt);
            }

            writer.push(stack, buffer, 4, EntityVertex.FORMAT);
        }
    }

    @Unique
    private final RandomSource random = new LegacyRandomSource(42L);

    @Shadow
    @Final
    private ItemColors itemColors;

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Inject(method = "renderModelLists", at = @At("HEAD"), cancellable = true)
    private void renderModelFast(BakedModel model, ItemStack itemStack, int light, int overlay, PoseStack matrixStack, VertexConsumer vertexConsumer, CallbackInfo ci) {
        var writer = VertexConsumerUtils.convertOrLog(vertexConsumer);

        if (writer == null) {
            return;
        }

        ci.cancel();

        RandomSource random = this.random;
        PoseStack.Pose matrices = matrixStack.last();

        ItemColor colorProvider = null;

        if (!itemStack.isEmpty()) {
            colorProvider = ((ItemColorsExtended) this.itemColors).sodium$getColorProvider(itemStack);
        }

        for (Direction direction : DirectionUtil.ALL_DIRECTIONS) {
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(null, direction, random);

            if (!quads.isEmpty()) {
                this.renderBakedItemQuads(matrices, writer, quads, itemStack, colorProvider, light, overlay);
            }
        }

        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(null, null, random);

        if (!quads.isEmpty()) {
            this.renderBakedItemQuads(matrices, writer, quads, itemStack, colorProvider, light, overlay);
        }
    }

    @Unique
    private boolean extend() {
        return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
    }

    @Unique
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void renderBakedItemQuads(PoseStack.Pose matrices, VertexBufferWriter writer, List<BakedQuad> quads, ItemStack itemStack, ItemColor colorProvider, int light, int overlay) {
        boolean extend = extend();
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad bakedQuad = quads.get(i);
            BakedQuadView quad = (BakedQuadView) bakedQuad;

            int color = 0xFFFFFFFF;

            if (colorProvider != null && quad.hasColor()) {
                color = ColorARGB.toABGR((colorProvider.getColor(itemStack, quad.getColorIndex())), 255);
            }

            if (extend) {
				writeQuadVerticesIris(writer, matrices, quad, color, light, overlay);
			} else {
				BakedModelEncoder.writeQuadVertices(writer, matrices, quad, color, light, overlay);
			}


            SpriteUtil.markSpriteActive(quad.getSprite());
        }
    }
}
