package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.irisshaders.iris.Iris;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = Window.class, priority = 1010)
public class MixinWindow {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
	private void iris$enableDebugContext(WindowEventHandler arg, ScreenManager arg2, DisplayData arg3, String string, String string2, CallbackInfo ci) {
		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_NO_ERROR, GLFW.GLFW_FALSE);
			Iris.logger.info("OpenGL debug context activated.");
			if (SodiumClientMod.options().performance.useNoErrorGLContext) {
				TinyFileDialogs.tinyfd_messageBox("Iris", "Due to a configuration issue, Iris may crash on this launch. This has been fixed automatically for the next launch.", "ok", "warning", false);
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
