package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import net.caffeinemc.gfx.opengl.GlObject;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.GlObjectExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlObject.class)
public class MixinGlObject implements GlObjectExt {
	@Shadow
	private int handle;

	@Override
	public boolean isHandleValid() {
		return this.handle != -2147483648;
	}
}
