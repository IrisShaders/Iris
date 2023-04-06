package net.coderbot.iris.uniforms;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.client.Minecraft;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private Matrix4f gbufferModelView;
	private Matrix4f gbufferProjection;
	private Vector3d fogColor;
	private float fogDensity;
	private float darknessLightFactor;
	private float tickDelta;
	private int currentRenderedBlockEntity;

	private int currentRenderedEntity = -1;
	private int currentRenderedItem = -1;

	private float currentAlphaTest;

	private CapturedRenderingState() {
	}

	public Matrix4f getGbufferModelView() {
		return gbufferModelView;
	}

	public void setGbufferModelView(Matrix4f gbufferModelView) {
		this.gbufferModelView = gbufferModelView.copy();
	}

	public Matrix4f getGbufferProjection() {
		return gbufferProjection;
	}

	public void setGbufferProjection(Matrix4f gbufferProjection) {
		this.gbufferProjection = gbufferProjection.copy();
	}

	public Vector3d getFogColor() {
		if (Minecraft.getInstance().level == null || fogColor == null) {
			return ZERO_VECTOR_3d;
		}

		return fogColor;
	}

	public void setFogColor(float red, float green, float blue) {
		fogColor = new Vector3d(red, green, blue);
	}

	public float getFogDensity() {
		return fogDensity;
	}

	public void setFogDensity(float fogDensity) {
		this.fogDensity = fogDensity;
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public float getTickDelta() {
		return tickDelta;
	}

	public void setCurrentBlockEntity(int entity) {
		this.currentRenderedBlockEntity = entity;
	}

	public int getCurrentRenderedBlockEntity() {
		return currentRenderedBlockEntity;
	}

	public void setCurrentEntity(int entity) {
		this.currentRenderedEntity = entity;
	}

	public int getCurrentRenderedEntity() {
		return currentRenderedEntity;
	}

	public void setCurrentRenderedItem(int item) {
		this.currentRenderedItem = item;
	}

	public int getCurrentRenderedItem() {
		return currentRenderedItem;
	}

    public float getCurrentAlphaTest() {
		return currentAlphaTest;
    }

	public void setCurrentAlphaTest(float alphaTest) {
		this.currentAlphaTest = alphaTest;
	}

	public float getDarknessLightFactor() {
		return darknessLightFactor;
	}

    public void setDarknessLightFactor(float factor) {
		darknessLightFactor = factor;
	}
}
