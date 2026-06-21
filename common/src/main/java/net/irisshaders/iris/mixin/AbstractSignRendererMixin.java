package net.irisshaders.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.irisshaders.iris.apiimpl.IrisApiV0Impl;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {
    // maDU59_ was here =D
    // Sign text does not need to be rendered during the shadow pass as it's not visible in the shadow anyway
    @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
    public void fism$cancelSignTextRendering(CallbackInfo ci){
        if(IrisApiV0Impl.INSTANCE.isRenderingShadowPass()) ci.cancel();
    }
}
