package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.Objects;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

/**
 * Implements uniforms relating the current viewport
 *
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#viewport">Uniforms: Viewport</a>
 */
public final class ViewportUniforms {
	/**
	 * The currently open Minecraft window. The window object is final in MinecraftClient, so it's safe to cache it here.
	 */
	private static final Window WINDOW = Objects.requireNonNull(MinecraftClient.getInstance().getWindow());

	// cannot be constructed
	private ViewportUniforms() {
	}

	/**
	 * Makes the viewport uniforms available to the given program
	 *
	 * @param uniforms the program to make the uniforms available to
	 */
	public static void addViewportUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_FRAME, "viewHeight", WINDOW::getHeight)
			.uniform1f(PER_FRAME, "viewWidth", WINDOW::getWidth)
			.uniform1f(PER_FRAME, "aspectRatio", ViewportUniforms::getAspectRatio);
	}

	/**
	 * @return the current viewport aspect ratio, calculated from the current Minecraft window size
	 */
	private static float getAspectRatio() {
		return ((float) WINDOW.getWidth()) / ((float) WINDOW.getHeight());
	}
}
