package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.NeoLambdas;
import net.irisshaders.iris.mixinterface.RenderTypeInterface;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinRenderType implements RenderTypeInterface {
	@Shadow
	@Final
	private RenderSetup state;

	@Override
	public RenderTarget iris$getRenderTarget() {
		return this.state.outputTarget.getRenderTarget();
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return this.state.pipeline;
	}
}
