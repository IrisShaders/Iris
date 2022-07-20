package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.gfx.api.shader.ShaderDescription;
import net.caffeinemc.gfx.opengl.shader.GlProgram;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(GlProgram.class)
public class MixinGlProgram {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20C;glLinkProgram(I)V"))
	private void link(int handle) {
		GlStateManager._glBindAttribLocation(handle, 4, "mc_Entity");
		GlStateManager._glBindAttribLocation(handle, 5, "mc_midTexCoord");
		GlStateManager._glBindAttribLocation(handle, 6, "at_tangent");
		GlStateManager._glBindAttribLocation(handle, 8, "at_midBlock");

		GL20C.glLinkProgram(handle);
	}
}
