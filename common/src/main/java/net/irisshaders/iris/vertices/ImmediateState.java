package net.irisshaders.iris.vertices;

/**
 * Some annoying global state needed for rendering.
 */
public class ImmediateState {
	public static boolean isRenderingLevel = false;
	public static boolean usingTessellation = false;
	public static boolean renderWithExtendedVertexFormat = true;
    public static ThreadLocal<Boolean> skipExtension = ThreadLocal.withInitial(() -> false);
}
