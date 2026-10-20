package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import net.irisshaders.iris.mixinterface.FrontendRenderPipelineAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FrontendRenderPipeline.class)
public class MixinFrontendRenderPipeline implements FrontendRenderPipelineAccess {
	@Unique
	private @Nullable RenderPipeline iris$source;

	@Override
	public @Nullable RenderPipeline iris$getSource() {
		return this.iris$source;
	}

	@Override
	public void iris$setSource(RenderPipeline source) {
		this.iris$source = source;
	}
}
