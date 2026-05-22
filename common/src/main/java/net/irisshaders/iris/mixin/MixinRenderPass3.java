package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import net.irisshaders.iris.mixinterface.CustomPass;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderPass.class)
public class MixinRenderPass3 implements RenderPassInterface {
	@Shadow
	@Final
	private RenderPassBackend backend;

	@Override
	public CustomPass iris$getCustomPass() {
		return this.backend.iris$getCustomPass();
	}

	@Override
	public void iris$setCustomPass(CustomPass pass) {
		this.backend.iris$setCustomPass(pass);
	}
}
