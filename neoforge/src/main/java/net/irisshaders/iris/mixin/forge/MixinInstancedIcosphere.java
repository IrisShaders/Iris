package net.irisshaders.iris.mixin.forge;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "tv.soaryn.xycraft.machines.client.render.instanced.InstancedIcosphere")
public class MixinInstancedIcosphere {
	@Inject(method = "draw", at = @At("RETURN"), require = 0)
	private static void onDraw(CallbackInfo ci, @Local ShaderInstance shader) {
		shader.clear();
	}
}
