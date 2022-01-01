package net.coderbot.iris;

import net.coderbot.iris.pipeline.FixedFunctionWorldRenderingPipeline;
import net.irisshaders.iris.api.v0.IrisApi;

public class IrisApiV0Impl implements IrisApi {
	public static final IrisApiV0Impl INSTANCE = new IrisApiV0Impl();

	@Override
	public boolean isShaderPackInUse() {
		return !(Iris.getPipelineManager().getPipelineNullable() instanceof FixedFunctionWorldRenderingPipeline);
	}
}
