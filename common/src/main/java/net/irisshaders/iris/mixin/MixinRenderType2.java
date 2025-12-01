package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.mixinterface.RenderTypeInterface;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(RenderType.class)
public class MixinRenderType2 implements RenderTypeInterface {
	@Override
	public RenderTarget iris$getRenderTarget() {
		return null;
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return null;
	}
}
