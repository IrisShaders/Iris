package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlDevice;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlHeuristics")
public class UndoReverseZTwo {
	@Redirect(method = "createDeviceInfo", at = @At(value = "FIELD", target = "Lorg/lwjgl/opengl/GLCapabilities;GL_ARB_clip_control:Z"))
	private boolean iris$fakeClipControl(GLCapabilities instance) {
		return false;
	}
}
