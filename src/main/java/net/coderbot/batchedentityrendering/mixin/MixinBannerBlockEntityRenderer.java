package net.coderbot.batchedentityrendering.mixin;

import com.mojang.datafixers.util.Pair;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.batchedentityrendering.impl.wrappers.TaggingRenderLayerWrapper;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BannerBlockEntityRenderer.class)
public class MixinBannerBlockEntityRenderer {
    private static final String RENDER_CANVAS =
            "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLjava/util/List;Z)V";

    /**
     * Holds a Groupable instance, if we successfully started a group.
     * This is because we need to make sure to end the group that we started.
     */
    @Unique
    private static Groupable groupableToEnd;
    private static int index;

    @ModifyVariable(method = RENDER_CANVAS, at = @At("HEAD"))
    private static VertexConsumerProvider iris$wrapVertexConsumerProvider(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof Groupable) {
            Groupable groupable = (Groupable) vertexConsumers;
            boolean started = groupable.maybeStartGroup();

            if (started) {
                groupableToEnd = groupable;
            }

			index = 0;
			// NB: Groupable not needed for this implementation of VertexConsumerProvider.
			return layer -> vertexConsumers.getBuffer(new TaggingRenderLayerWrapper(layer.toString(), layer, index++));
        }

        return vertexConsumers;
    }

    @Inject(method = RENDER_CANVAS, at = @At("RETURN"))
    private static void iris$endRenderingCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                                int overlay, ModelPart canvas, SpriteIdentifier baseSprite,
                                                boolean isBanner, List<Pair<BannerPattern, DyeColor>> patterns,
                                                boolean glint, CallbackInfo ci) {
        if (groupableToEnd != null) {
            groupableToEnd.endGroup();
            groupableToEnd = null;
			index = 0;
        }
    }
}
