package net.irisshaders.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.irisshaders.iris.apiimpl.IrisApiV0Impl;
import net.minecraft.client.renderer.MapRenderer;

@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {
    // maDU59_ was here =D
    // Maps in item frames do not need to be rendered during the shadow pass as they are not visible in the shadow anyway
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public <S> void fism$cancelMapRendering(CallbackInfo ci) {
        if(IrisApiV0Impl.INSTANCE.isRenderingShadowPass()) {
            ci.cancel();
        }
    }
}
