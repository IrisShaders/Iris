package net.irisshaders.iris.mixin.texture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.pbr.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture {
	@Shadow
	protected int id;

	@WrapOperation(method = "getId()I", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", remap = false))
	private int iris$afterGenerateId(Operation<Integer> original) {
		int id = original.call();
		TextureTracker.INSTANCE.trackTexture(id, (AbstractTexture) (Object) this);
		return id;
	}
}
