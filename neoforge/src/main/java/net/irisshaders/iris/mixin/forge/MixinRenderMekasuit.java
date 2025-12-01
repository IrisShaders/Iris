package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pathways.LightningHandler;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mekanism/client/render/armor/MekaSuitArmor", remap = false)
public class MixinRenderMekasuit {
	private static Object MEKASUIT;

	static {
		try {
			MEKASUIT = Class.forName("mekanism.client.render.MekanismRenderType").getField("MEKASUIT").get(null);
		} catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
			Iris.logger.fatal("Failed to get Mekanism flame!");
		}
	}

	@Redirect(method = {
		"renderArm",
		"Lmekanism/client/render/armor/MekaSuitArmor;render(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IILmekanism/common/lib/Color;ZLnet/minecraft/world/entity/LivingEntity;Ljava/util/Map;Z)V"
	}, at = @At(value = "FIELD", target = "Lmekanism/client/render/MekanismRenderType;MEKASUIT:Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType doNotSwitchShaders() {
		//if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
		//	return LightningHandler.MEKASUIT;
		//} else {
			return (RenderType) MEKASUIT;
		//}
	}
}
