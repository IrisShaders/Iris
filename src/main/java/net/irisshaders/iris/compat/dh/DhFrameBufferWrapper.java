package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import org.lwjgl.opengl.GL32;

public class DhFrameBufferWrapper implements IDhApiFramebuffer {
	private final GlFramebuffer framebuffer;


	public DhFrameBufferWrapper(GlFramebuffer framebuffer) {
		this.framebuffer = framebuffer;
	}


	@Override
	public boolean overrideThisFrame() {
		return true;
	}

	@Override
	public void bind() {
		this.framebuffer.bind();
	}

	@Override
	public void addDepthAttachment(int i, boolean b) {
		// ignore
	}

	@Override
	public int getId() {
		return this.framebuffer.getId();
	}

	@Override
	public int getStatus() {
		this.bind();
		return GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER);
	}

	@Override
	public void addColorAttachment(int i, int i1) {
		// ignore
	}

	@Override
	public void destroy() {
		// ignore
		//this.framebuffer.destroy();
	}

}
