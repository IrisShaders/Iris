package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.api.RenderPassBackend;
import net.irisshaders.iris.mixinterface.CustomPass;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderPassBackend.class)
public interface MixinRenderPass_Stub extends RenderPassInterface {

	@Override
	default void iris$setCustomPass(CustomPass pass) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CustomPass iris$getCustomPass() {
		throw new UnsupportedOperationException();
	}
}
