package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.irisshaders.iris.mixinterface.RenderTypeInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderType.class)
public class MixinRenderType implements RenderTypeInterface {
	@Shadow
	@Final
	private RenderSetup state;

	@Override
	public RenderTarget iris$getRenderTarget() {
		return Minecraft.getInstance().gameRenderer.mainRenderTarget();
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return this.state.pipeline;
	}
}
