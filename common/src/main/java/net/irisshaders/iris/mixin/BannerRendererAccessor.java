package net.irisshaders.iris.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;

@Mixin(BannerRenderer.class)
public interface BannerRendererAccessor {
    // maDU59_ was here =D
    @Invoker("submitPatternLayer")
    static <S> void fism$submitPatternLayerInvoke(final SpriteGetter sprites, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final Model<S> model, final S state, final SpriteId sprite, final DyeColor color, final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress){};
}
