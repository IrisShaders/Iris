package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL21C;

import java.util.Objects;
import java.util.function.Supplier;

public class ClearPass {
	private final Vector4f color;
	private final Supplier<Integer> viewportX;
	private final Supplier<Integer> viewportY;
	private final GlFramebuffer framebuffer;
	private final int clearFlags;

	public ClearPass(Vector4f color, Supplier<Integer> viewportX, Supplier<Integer> viewportY, GlFramebuffer framebuffer, int clearFlags) {
		this.color = color;
		this.viewportX = viewportX;
		this.viewportY = viewportY;
		this.framebuffer = framebuffer;
		this.clearFlags = clearFlags;
	}

	public void execute(Vector4f defaultClearColor) {
		RenderSystem.viewport(0, 0, viewportX.get(), viewportY.get());
		framebuffer.bind();

		Vector4f color = Objects.requireNonNull(defaultClearColor);

		if (this.color != null) {
			color = this.color;
		}

		RenderSystem.clearColor(color.x, color.y, color.z, color.w);
		RenderSystem.clear(clearFlags, Minecraft.ON_OSX);
	}
}
