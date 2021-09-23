package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Objects;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;

/**
 * Implements uniforms relating the current viewport
 *
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#viewport">Uniforms: Viewport</a>
 */
public final class ViewportUniforms {
	/**
	 * The currently open Minecraft window. The window object is final in MinecraftClient, so it's safe to cache it here.
	 */
	private static final RenderTarget FRAMEBUFFER = Objects.requireNonNull(Minecraft.getInstance().getMainRenderTarget());

	// cannot be constructed
	private ViewportUniforms() {
	}

	/**
	 * Makes the viewport uniforms available to the given program
	 *
	 * @param uniforms the program to make the uniforms available to
	 */
	public static void addViewportUniforms(UniformHolder uniforms) {
		// TODO: What about the custom scale.composite3 property?
		uniforms
			.uniform1f(PER_FRAME, "viewHeight", () -> FRAMEBUFFER.height)
			.uniform1f(PER_FRAME, "viewWidth", () -> FRAMEBUFFER.width)
			.uniform1f(PER_FRAME, "aspectRatio", ViewportUniforms::getAspectRatio);
	}

	/**
	 * @return the current viewport aspect ratio, calculated from the current Minecraft window size
	 */
	private static float getAspectRatio() {
		return ((float) FRAMEBUFFER.width) / ((float) FRAMEBUFFER.height);
	}
}
