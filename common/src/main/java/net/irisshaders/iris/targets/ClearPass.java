package net.irisshaders.iris.targets;

import org.joml.Vector4f;

import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;

public class ClearPass {
	private final Vector4f color;
	private final IntSupplier viewportX;
	private final IntSupplier viewportY;
	private final RenderFramebuffer framebuffer;
	private final int clearFlags;

	public ClearPass(Vector4f color, IntSupplier viewportX, IntSupplier viewportY, RenderFramebuffer framebuffer, int clearFlags) {
		this.color = color;
		this.viewportX = viewportX;
		this.viewportY = viewportY;
		this.framebuffer = framebuffer;
		this.clearFlags = clearFlags;
	}

	public static void executeAll(List<ClearPass> passes, Vector4f defaultClearColor) {
		if (passes.isEmpty()) {
			return;
		}

		for (var pass : passes) {
			pass.execute(defaultClearColor);
		}
	}

	public void execute(Vector4f defaultClearColor) {
		this.framebuffer.clear(this.color != null ? this.color : Objects.requireNonNull(defaultClearColor), this.clearFlags);
	}

	public RenderFramebuffer getFramebuffer() {
		return framebuffer;
	}
}
