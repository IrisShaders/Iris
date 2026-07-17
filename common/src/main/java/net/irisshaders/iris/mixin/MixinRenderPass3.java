package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import net.irisshaders.iris.mixinterface.CustomPass;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FrontendRenderPass.class)
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
