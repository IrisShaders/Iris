package net.coderbot.iris.compat.dh;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;

public class DHCompat {
	private GlFramebuffer fb;

	public int getFramebuffer() {
		return fb.getId();
	}

	public void setFramebuffer(GlFramebuffer fb) {
		this.fb = fb;
	}
}
