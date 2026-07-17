package net.irisshaders.iris.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.irisshaders.iris.apiimpl.IrisApiV0Impl;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {
    // maDU59_ was here =D
    // Banner patterns do not need to be rendered during the shadow pass as they are not visible anyway

}
