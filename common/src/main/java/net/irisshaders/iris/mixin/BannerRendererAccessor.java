package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BannerRenderer.class)
public interface BannerRendererAccessor {
	// maDU59_ was here =D
	@Invoker("submitPatternLayer")
	static <S> void iris$invokeSubmitPatternLayer(final SpriteGetter sprites, final PoseStack poseStack,
		final OrderedSubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final Model<S> model,
		final S state, final SpriteId sprite, final DyeColor color) {

	}
}
