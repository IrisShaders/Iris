package net.coderbot.iris.uniforms;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private Matrix4f gbufferModelView;
	private Matrix4f gbufferProjection;
	private Vec3d fogColor;
	private float tickDelta;
	private BlockEntity currentRenderedBlockEntity;
	private Entity currentRenderedEntity;

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

	public Vec3d getFogColor() {
		if (MinecraftClient.getInstance().world == null || fogColor == null) {
			return Vec3d.ZERO;
		}

		return fogColor;
	}

	public void setFogColor(float red, float green, float blue) {
		fogColor = new Vec3d(red, green, blue);
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public float getTickDelta() {
		return tickDelta;
	}

	public void setCurrentBlockEntity(BlockEntity entity) {
		this.currentRenderedBlockEntity = entity;
	}

	public BlockEntity getCurrentRenderedBlockEntity() {
		return currentRenderedBlockEntity;
	}

	public void setCurrentEntity(Entity entity) {
		this.currentRenderedEntity = entity;
	}

	public Entity getCurrentRenderedEntity() {
		return currentRenderedEntity;
	}
}
