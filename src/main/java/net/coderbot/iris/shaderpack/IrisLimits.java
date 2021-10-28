package net.coderbot.iris.shaderpack;

public class IrisLimits {
	/**
	 * The maximum number of color textures that a shader pack can write to and read from in gbuffer and composite
	 * programs.
	 *
	 * It's not recommended to raise this higher than 16 until code for avoiding allocation of unused color textures
	 * is implemented.
	 */
	public static final int MAX_COLOR_BUFFERS = 16;
}
