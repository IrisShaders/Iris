package net.coderbot.iris.uniforms;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.fantastic.ParticleIdHolder;
import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;
import net.coderbot.iris.mixin.TerrainParticleAccessor;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private Matrix4f gbufferModelView;
	private Matrix4f gbufferProjection;
	private Vector3d fogColor;
	private float tickDelta;
	private int currentRenderedBlockEntity;
	private Runnable blockEntityIdListener = null;

	private int currentRenderedEntity = -1;

	private Vector2i currentParticle = new Vector2i();

	private Runnable entityIdListener = null;

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
			return new Vector3d();
		}

		return fogColor;
	}

	public void setFogColor(float red, float green, float blue) {
		fogColor = new Vector3d(red, green, blue);
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public float getTickDelta() {
		return tickDelta;
	}

	public void setCurrentBlockEntity(int entity) {
		this.currentRenderedBlockEntity = entity;

		if (this.blockEntityIdListener != null) {
			this.blockEntityIdListener.run();
		}
	}

	public int getCurrentRenderedBlockEntity() {
		return currentRenderedBlockEntity;
	}

	public void setCurrentEntity(int entity) {
		this.currentRenderedEntity = entity;

		if (this.entityIdListener != null) {
			this.entityIdListener.run();
		}
	}

	public ValueUpdateNotifier getEntityIdNotifier() {
		return listener -> this.entityIdListener = listener;
	}

	public ValueUpdateNotifier getBlockEntityIdNotifier() {
		return listener -> this.blockEntityIdListener = listener;
	}

	public void setCurrentParticle(Particle particle) {
		if (particle == null) {
			this.currentParticle.set(0);
			return;
		}

		int particleId = BlockRenderingSettings.INSTANCE.getParticleIds().applyAsInt(((ParticleIdHolder) particle).getParticleId());
		int blockParticleId = 0;
		if (particle instanceof TerrainParticle && BlockRenderingSettings.INSTANCE.getBlockStateIds() != null) {
			blockParticleId = BlockRenderingSettings.INSTANCE.getBlockStateIds().applyAsInt(((TerrainParticleAccessor) particle).getBlockState());
		}
		this.currentParticle.set(particleId, blockParticleId);
	}

	public Vector2i getCurrentParticle() {
		return currentParticle;
	}

	public int getCurrentRenderedEntity() {
		return currentRenderedEntity;
	}
}
