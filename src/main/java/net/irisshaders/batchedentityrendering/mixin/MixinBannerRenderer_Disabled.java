package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.irisshaders.batchedentityrendering.impl.Groupable;
import net.irisshaders.batchedentityrendering.impl.wrappers.TaggingRenderTypeWrapper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * This Mixin groups banner patterns separately, to not batch the wrong patterns.
 * It has been disabled for now, as the behavior seems to not be required. (IMS, September 2, 2022)
 */
@Mixin(BannerRenderer.class)
public class MixinBannerRenderer_Disabled {
	private static final String RENDER_PATTERNS =
		"Lnet/minecraft/client/renderer/blockentity/BannerRenderer;renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLjava/util/List;Z)V";

	/**
	 * Holds a Groupable instance, if we successfully started a group.
	 * This is because we need to make sure to end the group that we started.
	 */
	@Unique
	private static Groupable groupableToEnd;
	private static int index;

	@ModifyVariable(method = RENDER_PATTERNS, at = @At("HEAD"), argsOnly = true)
	private static MultiBufferSource iris$wrapBufferSource(MultiBufferSource multiBufferSource) {
		if (multiBufferSource instanceof Groupable groupable) {
			boolean started = groupable.maybeStartGroup();

			if (started) {
				groupableToEnd = groupable;
			}

			index = 0;
			// NB: Groupable not needed for this implementation of MultiBufferSource.
			return type -> multiBufferSource.getBuffer(new TaggingRenderTypeWrapper(type.toString(), type, index++));
		}

		return multiBufferSource;
	}

	@Inject(method = RENDER_PATTERNS, at = @At("RETURN"))
	private static void iris$endRenderingCanvas(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ModelPart modelPart, Material material, boolean bl, List<Pair<BannerPattern, DyeColor>> list, boolean bl2, CallbackInfo ci) {
		if (groupableToEnd != null) {
			groupableToEnd.endGroup();
			groupableToEnd = null;
			index = 0;
		}
	}
}
