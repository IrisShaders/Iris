package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pathways.LightningHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Pseudo
@Mixin(targets = "mekanism/client/render/entity/RenderFlame", remap = false)
public class MixinRenderFlame {
	private static Object MEKANISM_FLAME;

	static {
		try {
			MEKANISM_FLAME = Class.forName("mekanism.client.render.MekanismRenderType").getField("FLAME").get(null);
		} catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
			Iris.logger.fatal("Failed to get Mekanism flame!");
		}
	}

	@Redirect(method = {
		"render(Lmekanism/common/entity/EntityFlame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
	}, at = @At(value = "FIELD", target = "Lmekanism/client/render/MekanismRenderType;FLAME:Ljava/util/function/Function;"))
	private Function<ResourceLocation, RenderType> doNotSwitchShaders() {
		//if (Iris.isPackInUseQuick()) {
		//	return LightningHandler.MEKANISM_FLAME;
		//} else {
			return (Function<ResourceLocation, RenderType>) MEKANISM_FLAME;
		//}
	}
}
