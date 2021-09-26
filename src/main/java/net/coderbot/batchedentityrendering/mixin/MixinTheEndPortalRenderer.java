package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.batchedentityrendering.impl.wrappers.TaggingRenderTypeWrapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla end portal rendering in 1.16 and below is layered, so this is needed. It's not needed in 1.17 though.
 */
@Mixin(TheEndPortalRenderer.class)
public class MixinTheEndPortalRenderer {
    private static final String RENDER =
            "Lnet/minecraft/client/renderer/blockentity/TheEndPortalRenderer;render(Lnet/minecraft/world/level/block/entity/TheEndPortalBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    private static final String POSESTACK_POSE
			= "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lcom/mojang/math/Matrix4f;";

    /**
     * Holds a Groupable instance, if we successfully started a group.
     * This is because we need to make sure to end the group that we started.
     */
    @Unique
    private static Groupable groupableToEnd;
    private static int index;

    // Inject a little bit after HEAD to avoid cancellations.
    @ModifyVariable(method = RENDER, at = @At(value = "INVOKE", target = POSESTACK_POSE))
    private MultiBufferSource iris$wrapBufferSource(MultiBufferSource bufferSource) {
        if (bufferSource instanceof Groupable) {
            Groupable groupable = (Groupable) bufferSource;
            boolean started = groupable.maybeStartGroup();

            if (started) {
                groupableToEnd = groupable;
			}

			index = 0;
			// NB: Groupable not needed for this implementation of VertexConsumerProvider.
			return type -> bufferSource.getBuffer(new TaggingRenderTypeWrapper(type.toString(), type, index++));
		}

		return bufferSource;
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void iris$endRenderingPortal(TheEndPortalBlockEntity entity, float tickDelta, PoseStack poseStack,
										 MultiBufferSource bufferSource, int light, int overlay,
										 CallbackInfo ci) {
		if (groupableToEnd != null) {
			groupableToEnd.endGroup();
			groupableToEnd = null;
			index = 0;
		}
    }
}
