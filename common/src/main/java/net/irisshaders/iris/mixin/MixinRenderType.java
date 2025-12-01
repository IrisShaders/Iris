package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.NeoLambdas;
import net.irisshaders.iris.mixinterface.RenderTypeInterface;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.CompositeRenderType.class)
public class MixinRenderType implements RenderTypeInterface {
	@Shadow
	@Final
	private RenderType.CompositeState state;

	@Shadow
	@Final
	private RenderPipeline renderPipeline;

	@Override
	public RenderTarget iris$getRenderTarget() {
		return this.state.outputState.getRenderTarget();
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return this.renderPipeline;
	}
}
