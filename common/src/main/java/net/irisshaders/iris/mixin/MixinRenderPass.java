package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlRenderPass;
import net.irisshaders.iris.mixinterface.CustomPass;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GlRenderPass.class)
public class MixinRenderPass implements RenderPassInterface {
	@Unique
	private CustomPass iris$customPass;

	@Override
	public void iris$setCustomPass(CustomPass pass) {
		this.iris$customPass = pass;
	}

	@Override
	public CustomPass iris$getCustomPass() {
		return this.iris$customPass;
	}
}
