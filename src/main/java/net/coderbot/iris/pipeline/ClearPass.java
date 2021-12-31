package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.Framebuffer;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL21C;

import java.util.Objects;

public class ClearPass {
	private final Vector4f color;
	private final Framebuffer framebuffer;
	private final int clearFlags;

	public ClearPass(Vector4f color, Framebuffer framebuffer, boolean clearDepth) {
		this.color = color;
		this.framebuffer = framebuffer;
		this.clearFlags = GL21C.GL_COLOR_BUFFER_BIT | (clearDepth ? GL21C.GL_DEPTH_BUFFER_BIT : 0);
	}

	public void execute(Vector4f defaultClearColor) {
		// TODO: viewport if needed for custom buffer sizes
		framebuffer.bind();

		Vector4f color = Objects.requireNonNull(defaultClearColor);

		if (this.color != null) {
			color = this.color;
		}

		RenderSystem.clearColor(color.x, color.y, color.z, color.w);
		RenderSystem.clear(clearFlags, Minecraft.ON_OSX);
	}
}
