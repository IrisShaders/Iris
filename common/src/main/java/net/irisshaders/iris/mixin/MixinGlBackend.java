package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlBackend.class)
public class MixinGlBackend {
    @Overwrite
    public void loadLibrary() {}

    @Overwrite
    public void unloadLibrary() {}
}
