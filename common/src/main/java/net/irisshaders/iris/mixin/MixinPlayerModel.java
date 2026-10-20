package net.irisshaders.iris.mixin;

import net.irisshaders.iris.pathways.SkinRenderType;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(PlayerModel.class)
public class MixinPlayerModel {
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/model/HumanoidModel;<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V"), index = 1)
	private static Function<Identifier, RenderType> iris$skinRenderType(Function<Identifier, RenderType> original) {
		return SkinRenderType::get;
	}
}
