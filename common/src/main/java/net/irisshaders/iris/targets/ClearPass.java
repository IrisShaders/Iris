package net.irisshaders.iris.targets;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.function.IntSupplier;

public class ClearPass {
	private final Vector4f color;
	private final IntSupplier viewportX;
	private final IntSupplier viewportY;
	private final GlFramebuffer framebuffer;
	private final int clearFlags;

	public ClearPass(Vector4f color, IntSupplier viewportX, IntSupplier viewportY, GlFramebuffer framebuffer, int clearFlags) {
		this.color = color;
		this.viewportX = viewportX;
		this.viewportY = viewportY;
		this.framebuffer = framebuffer;
		this.clearFlags = clearFlags;
	}

	public void execute(Vector4f defaultClearColor) {
		framebuffer.clear(this.color != null ? this.color : Objects.requireNonNull(defaultClearColor), clearFlags);
	}

	public GlFramebuffer getFramebuffer() {
		return framebuffer;
	}
}
