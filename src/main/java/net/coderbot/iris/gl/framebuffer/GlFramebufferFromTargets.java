package net.coderbot.iris.gl.framebuffer;

import com.google.common.collect.ImmutableSet;

public class GlFramebufferFromTargets extends GlFramebuffer {
	private final int[] renderTargetBuffers;
	private final ImmutableSet<Integer> mainBuffers;

	public GlFramebufferFromTargets(int[] drawBuffers, ImmutableSet<Integer> mainBuffers) {
		super();

		this.renderTargetBuffers = drawBuffers;
		this.mainBuffers = mainBuffers;
	}

	public int[] getRenderTargetBuffers() {
		return renderTargetBuffers;
	}

	public ImmutableSet<Integer> getMainBuffers() {
		return mainBuffers;
	}
}
