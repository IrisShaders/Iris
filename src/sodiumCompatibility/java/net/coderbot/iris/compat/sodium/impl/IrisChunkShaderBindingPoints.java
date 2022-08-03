package net.coderbot.iris.compat.sodium.impl;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderBindingPoint;

/**
 * Defines Iris-specific chunk shader binding points.
 *
 * NB: Make sure this doesn't collide with anything in {@link me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints}
 */
public class IrisChunkShaderBindingPoints {
	public static final ShaderBindingPoint NORMAL = new ShaderBindingPoint(5);
	public static final ShaderBindingPoint TANGENT = new ShaderBindingPoint(6);
	public static final ShaderBindingPoint MID_TEX_COORD = new ShaderBindingPoint(7);
	public static final ShaderBindingPoint BLOCK_ID = new ShaderBindingPoint(8);
	public static final ShaderBindingPoint MID_BLOCK = new ShaderBindingPoint(9);
}
