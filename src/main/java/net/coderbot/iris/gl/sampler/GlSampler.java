package net.coderbot.iris.gl.sampler;

import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;

public class GlSampler extends GlResource {
	protected GlSampler() {
		super(IrisRenderSystem.genSampler());
	}

	@Override
	protected void destroyInternal() {
		IrisRenderSystem.destroySampler(getGlId());
	}

	public int getId() {
		return getGlId();
	}
}
