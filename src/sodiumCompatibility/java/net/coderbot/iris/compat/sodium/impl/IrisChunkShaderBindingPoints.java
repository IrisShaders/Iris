package net.coderbot.iris.compat.sodium.impl;

/**
 * Defines Iris-specific chunk shader binding points.
 *
 * NB: Make sure this doesn't collide with anything in {@link me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints}
 */
public class IrisChunkShaderBindingPoints {
	public static final int NORMAL = 10;
	public static final int BLOCK_ID = 11;
    public static final int MID_TEX_COORD = 12;
    public static final int TANGENT = 13;
    public static final int MID_BLOCK = 14;
}
