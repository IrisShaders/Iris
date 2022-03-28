package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import net.caffeinemc.gfx.opengl.shader.GlProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlProgram.class)
public class MixinGlProgram {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glBindFragDataLocation(IILjava/lang/CharSequence;)V"))
	private void no(int nameEncoded, int program, CharSequence colorNumber) {}
}
