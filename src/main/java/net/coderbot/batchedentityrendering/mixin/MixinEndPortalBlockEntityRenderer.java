package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.batchedentityrendering.impl.wrappers.TaggingRenderLayerWrapper;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla end portal rendering in 1.16 and below is layered, so this is needed. It's not needed in 1.17 though.
 */
@Mixin(EndPortalBlockEntityRenderer.class)
public class MixinEndPortalBlockEntityRenderer {
    private static final String RENDER =
            "render(Lnet/minecraft/block/entity/EndPortalBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V";

    private static final String MATRIXSTACK_GET_MODEL
			= "net/minecraft/client/util/math/MatrixStack$Entry.getModel ()Lnet/minecraft/util/math/Matrix4f;";

    /**
     * Holds a Groupable instance, if we successfully started a group.
     * This is because we need to make sure to end the group that we started.
     */
    @Unique
    private static Groupable groupableToEnd;
    private static int index;

    // Inject a little bit after HEAD to avoid cancellations.
    @ModifyVariable(method = RENDER, at = @At(value = "INVOKE", target = MATRIXSTACK_GET_MODEL))
    private VertexConsumerProvider iris$wrapVertexConsumerProvider(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof Groupable) {
            Groupable groupable = (Groupable) vertexConsumers;
            boolean started = groupable.maybeStartGroup();

            if (started) {
                groupableToEnd = groupable;
            }
        }

        index = 0;
        // NB: Groupable not needed for this implementation of VertexConsumerProvider.
        return layer -> vertexConsumers.getBuffer(new TaggingRenderLayerWrapper(layer.toString(), layer, index++));
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void iris$endRenderingPortal(EndPortalBlockEntity entity, float tickDelta, MatrixStack matrices,
										 VertexConsumerProvider vertexConsumers, int light, int overlay,
										 CallbackInfo ci) {
        if (groupableToEnd != null) {
            groupableToEnd.endGroup();
            groupableToEnd = null;
        }

        index = 0;
    }
}
