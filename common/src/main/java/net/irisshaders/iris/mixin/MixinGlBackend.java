package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlBackend;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlBackend.class)
public class MixinGlBackend {
    @Redirect(method = "loadLibrary()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLVideo;SDL_GL_LoadLibrary(Ljava/lang/CharSequence;)Z"))
    private static boolean iris$loadDefaultGL(CharSequence path) {
	    return SDLVideo.SDL_GL_LoadLibrary((CharSequence)null);
    }
}
