package net.irisshaders.iris.api.v0;

import org.joml.Matrix4f;

/**
 * A callback invoked during the shadow pass, after opaque terrain has been
 * drawn.
 *
 * <p>Implementations should draw using a {@code RenderPipeline} that has been
 * assigned with {@link IrisApi#assignPipelineShadow}, otherwise no shader will
 * be applied to the geometry.
 *
 * <p>The shadow pass runs with backface culling disabled and the terrain-cutout
 * phase active. Geometry is drawn into the shadow framebuffer regardless of the
 * render target the implementation binds, so any target may be passed when
 * creating a render pass.
 *
 * @since API v0.4
 */

@FunctionalInterface
public interface IrisShadowRenderCallback {
	/**
	 * @param modelView  the shadow pass model-view matrix
	 * @param projection the shadow pass projection matrix
	 * @param cameraX    the player camera X position the shadow pass is centered on
	 * @param cameraY    the player camera Y position the shadow pass is centered on
	 * @param cameraZ    the player camera Z position the shadow pass is centered on
	 * @param tickDelta  the partial tick the frame is being rendered at
	 */
	void renderShadow(Matrix4f modelView, Matrix4f projection, double cameraX, double cameraY, double cameraZ, float tickDelta);
}
