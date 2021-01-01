package net.coderbot.iris.layer;

import net.coderbot.iris.mixin.renderlayer.RenderLayerAccessor;

import net.minecraft.client.render.RenderLayer;

public class IrisRenderLayerWrapper extends RenderLayer {
	private final UseProgramRenderPhase useProgram;

	public IrisRenderLayerWrapper(String name, RenderLayer wrapped, UseProgramRenderPhase useProgram) {
		super(name, wrapped.getVertexFormat(), wrapped.getDrawMode(), wrapped.getExpectedBufferSize(),
			wrapped.hasCrumbling(), isTranslucent(wrapped), wrapped::startDrawing, wrapped::endDrawing);

		this.useProgram = useProgram;
	}

	@Override
	public void startDrawing() {
		super.startDrawing();

		useProgram.startDrawing();
	}

	@Override
	public void endDrawing() {
		useProgram.endDrawing();

		super.endDrawing();
	}

	private static final boolean isTranslucent(RenderLayer layer) {
		return ((RenderLayerAccessor) layer).isTranslucent();
	}
}
