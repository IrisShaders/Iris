package net.irisshaders.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.irisshaders.iris.apiimpl.IrisApiV0Impl;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // maDU59_ was here =D
    // Enchantment glint does not need to be rendered during the shadow pass as it's not visible in the shadow anyway
    @Inject(method = "hasFoil", at = @At("HEAD"), cancellable = true)
    private void fism$cancelGlintRendering(CallbackInfoReturnable<Boolean> cir){
        if(IrisApiV0Impl.INSTANCE.isRenderingShadowPass()) cir.setReturnValue(false);
    }
}
