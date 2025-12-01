package net.irisshaders.iris.mixin.texture;

import com.mojang.blaze3d.textures.GpuTexture;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.AbstractTextureExtended;
import net.irisshaders.iris.pbr.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTexture.class)
public abstract class MixinAbstractTexture implements AbstractTextureExtended {
	@Shadow
	@Nullable
	protected GpuTexture texture;
	@Unique
	private GpuTexture lastChecked;

	@Inject(method = "getTexture", at = @At(value = "RETURN"))
	private void iris$afterGenerateId(CallbackInfoReturnable<GpuTexture> cir) {
		if (lastChecked != cir.getReturnValue()) {
			lastChecked = cir.getReturnValue();
			TextureTracker.INSTANCE.trackTexture(lastChecked.iris$getGlId(), (AbstractTexture) (Object) this);

		}
	}

	//@Inject(method = "setFilter(ZZ)V", at = @At("HEAD"))
	private void iris$setFilter(boolean bl, boolean bl2, CallbackInfo ci) {
		this.onSet(bl, bl2);
	}

	private void onSet(boolean bl, boolean bl2) {
		if (!bl) {
			if (((Object) this) instanceof ReloadableTexture rt) {
				Iris.logger.warn(rt.resourceId() + " was set to nearest");
			} else 	if (((Object) this) instanceof TextureAtlas rt) {
				Iris.logger.warn(rt.location() + " was set to nearest");
			}
		} else {
			if (((Object) this) instanceof ReloadableTexture rt) {
				Iris.logger.warn(rt.resourceId() + " was set to linear");
			} else 	if (((Object) this) instanceof TextureAtlas rt) {
				Iris.logger.warn(rt.location() + " was set to linear");
			}
		}
	}

}
