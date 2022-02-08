package net.coderbot.iris;

import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.pipeline.FixedFunctionWorldRenderingPipeline;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisApiConfig;
import net.minecraft.client.gui.screens.Screen;

public class IrisApiV0Impl implements IrisApi {
	public static final IrisApiV0Impl INSTANCE = new IrisApiV0Impl();
	private static final IrisApiV0ConfigImpl CONFIG = new IrisApiV0ConfigImpl();

	@Override
	public int getMinorApiRevision() {
		return 1;
	}

	@Override
	public boolean isShaderPackInUse() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline == null) {
			return false;
		}

		return !(pipeline instanceof FixedFunctionWorldRenderingPipeline);
	}

	@Override
	public boolean isRenderingShadowPass() {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered();
	}

	@Override
	public Object getShadowFrustum() {
		return ShadowRenderer.FRUSTUM;
	}

	@Override
	public Object openMainIrisScreenObj(Object parent) {
		return new ShaderPackScreen((Screen) parent);
	}

	@Override
	public String getMainScreenLanguageKey() {
		return "options.iris.shaderPackSelection";
	}

	@Override
	public IrisApiConfig getConfig() {
		return CONFIG;
	}
}
