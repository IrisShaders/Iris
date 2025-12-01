package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.TextureUtil;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureUtil.class)
public class MixinTextureUtil {

}
