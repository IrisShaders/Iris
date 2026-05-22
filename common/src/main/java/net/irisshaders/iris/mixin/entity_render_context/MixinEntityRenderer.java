package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;


@Mixin(NameTagFeatureRenderer.class)
public class MixinEntityRenderer {
    @Unique
    private static final NamespacedId NAME_TAG_ID = new NamespacedId("minecraft", "name_tag");
    
    @Unique
    private int lastId = -100;

    @Inject(method = "renderTranslucent", at = @At("HEAD"))
    private void setNameTagId(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, Font font, CallbackInfo ci) { 
        Object2IntFunction<NamespacedId> entityIds = WorldRenderingSettings.INSTANCE.getEntityIds();

        if (entityIds == null) return;

        this.lastId = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
        CapturedRenderingState.INSTANCE.setCurrentEntity(entityIds.applyAsInt(NAME_TAG_ID));
    }

    @Inject(method = "renderTranslucent", at = @At("RETURN"))
    private void resetId(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, Font font, CallbackInfo ci) {
        if (lastId != -100) {
            CapturedRenderingState.INSTANCE.setCurrentEntity(lastId);
            lastId = -100;
        }
    }
}
