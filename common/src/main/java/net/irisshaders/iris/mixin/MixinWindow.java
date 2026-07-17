package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlBackend;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.device.GpuBackend;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.irisshaders.iris.Iris;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(value = GlBackend.class, priority = 1010)
public class MixinWindow {
	@Inject(method = "setWindowHints", at = @At(value = "RETURN"))
	private void iris$enableDebugContext(CallbackInfo ci) {
		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_DEBUG_FLAG, 1);
			SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_NO_ERROR, 0);
			Iris.logger.info("OpenGL debug context activated.");
			if (SodiumClientMod.options().performance.useNoErrorGLContext) {
				TinyFileDialogs.tinyfd_messageBox("Iris", "Due to a configuration issue, Iris may crash on this launch. This has been fixed automatically for the next launch.", "ok", "warning", 0);
				SodiumClientMod.options().performance.useNoErrorGLContext = false;
				try {
					SodiumOptions.writeToDisk(SodiumClientMod.options());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
