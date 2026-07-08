package net.irisshaders.iris.vertices;

import net.minecraft.client.renderer.RenderType;

/**
 * Some annoying global state needed for rendering.
 */
public class ImmediateState {
	public static final ThreadLocal<Boolean> skipExtension = ThreadLocal.withInitial(() -> false);
	public static boolean isRenderingLevel = false;
	public static boolean usingTessellation = false;
	public static boolean renderWithExtendedVertexFormat = true;
	public static boolean bypass;
	public static boolean mergeRendering;
	public static RenderType mergedRenderType;
}
