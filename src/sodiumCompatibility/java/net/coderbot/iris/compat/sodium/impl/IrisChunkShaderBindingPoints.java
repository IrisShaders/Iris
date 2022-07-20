package net.coderbot.iris.compat.sodium.impl;

/**
 * Defines Iris-specific chunk shader binding points.
 *
 * NB: Make sure this doesn't collide with anything in {@link net.caffeinemc.sodium.render.chunk.shader.ChunkShaderBindingPoints}
 */
public class IrisChunkShaderBindingPoints {
    public static final int BLOCK_ID = 4;
    public static final int MID_TEX_COORD = 5;
    public static final int TANGENT = 6;
    public static final int NORMAL = 7;
	public static final int MID_BLOCK = 8;
}
