package net.coderbot.iris.uniforms;

import net.minecraft.util.math.Matrix4f;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private Matrix4f gbufferModelView;
	private Matrix4f gbufferProjection;

	/**
	 * The state of the modelview matrix right after the sky angle rotation has been applied
	 */
	private Matrix4f celestialModelView;

	private CapturedRenderingState() {
	}

	public Matrix4f getGbufferModelView() {
		return gbufferModelView;
	}

	public void setGbufferModelView(Matrix4f gbufferModelView) {
		this.gbufferModelView = gbufferModelView;
	}

	public Matrix4f getGbufferProjection() {
		return gbufferProjection;
	}

	public void setGbufferProjection(Matrix4f gbufferProjection) {
		this.gbufferProjection = gbufferProjection;
	}

	public Matrix4f getCelestialModelView() {
		return celestialModelView;
	}

	public void setCelestialModelView(Matrix4f celestialModelView) {
		this.celestialModelView = celestialModelView;
	}
}
