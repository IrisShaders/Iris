package net.coderbot.iris.compat.sodium.impl;

/**
 * Defines Iris-specific chunk shader binding points.
 *
 * NB: Make sure this doesn't collide with anything in {@link me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints}
 */
public class IrisChunkShaderBindingPoints {
	public static final int NORMAL = 2;
	public static final int BLOCK_ID = 3;
    public static final int MID_TEX_COORD = 4;
    public static final int TANGENT = 5;
    public static final int MID_BLOCK = 6;
}
