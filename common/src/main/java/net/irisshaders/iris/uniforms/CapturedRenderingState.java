package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.gl.blending.BlendMode;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector4i;
import org.lwjgl.vulkan.VK10;

public class CapturedRenderingState {

	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private Matrix4fc gbufferModelView;
	private Matrix4fc gbufferProjection;
	private Vector3d fogColor;
	private float fogDensity;
	private float darknessLightFactor;
	private float tickDelta;
	private float realTickDelta;
	private int currentRenderedBlockEntity;

	private int currentRenderedEntity = -1;
	private int currentRenderedItem = -1;
	private int textureReloadCount = 0;

	private float currentAlphaTest;
	private float cloudTime;

	private CapturedRenderingState() {
	}

	public Matrix4fc getGbufferModelView() {
		return gbufferModelView;
	}

	public void setGbufferModelView(Matrix4fc gbufferModelView) {
		this.gbufferModelView = gbufferModelView;
	}

	public Matrix4fc getGbufferProjection() {
		return gbufferProjection;
	}

	public void setGbufferProjection(Matrix4f gbufferProjection) {
		this.gbufferProjection = new Matrix4f(gbufferProjection);
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

	public float getTickDelta() {
		return tickDelta;
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public float getRealTickDelta() {
		return realTickDelta;
	}

	public void setRealTickDelta(float tickDelta) {
		this.realTickDelta = tickDelta;
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

	public int getCurrentRenderedItem() {
		return currentRenderedItem;
	}

	public void setCurrentRenderedItem(int item) {
		this.currentRenderedItem = item;
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

	public float getCloudTime() {
		return this.cloudTime;
	}

	public void setCloudTime(float cloudTime) {
		this.cloudTime = cloudTime;
	}

	public int getTextureReloadCount() {
		return textureReloadCount;
	}

	public void incrementTextureReloadCount() {
		this.textureReloadCount++;
	}

	public void resetTextureReloadCount() {
		this.textureReloadCount = 0;
	}

	private BlendMode blend;

	public void setBlendMode(BlendMode value) {
		blend = value;
	}

	public Vector4i getBlendFunction() {
		return blend == null ? new Vector4i()
			: new Vector4i(legacyFactor(blend.srcRgb()), legacyFactor(blend.dstRgb()), legacyFactor(blend.srcAlpha()),
			legacyFactor(blend.dstAlpha()));
	}

	private static int legacyFactor(int factor) {
		return switch (factor) {
			case VK10.VK_BLEND_FACTOR_ZERO -> 0;
			case VK10.VK_BLEND_FACTOR_ONE -> 1;
			case VK10.VK_BLEND_FACTOR_SRC_COLOR -> 0x300;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR -> 0x301;
			case VK10.VK_BLEND_FACTOR_SRC_ALPHA -> 0x302;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA -> 0x303;
			case VK10.VK_BLEND_FACTOR_DST_ALPHA -> 0x304;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA -> 0x305;
			case VK10.VK_BLEND_FACTOR_DST_COLOR -> 0x306;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR -> 0x307;
			case VK10.VK_BLEND_FACTOR_SRC_ALPHA_SATURATE -> 0x308;
			case VK10.VK_BLEND_FACTOR_CONSTANT_COLOR -> 0x8001;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR -> 0x8002;
			case VK10.VK_BLEND_FACTOR_CONSTANT_ALPHA -> 0x8003;
			case VK10.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA -> 0x8004;
			default -> throw new IllegalArgumentException("Unsupported Iris blend factor: " + factor);
		};
	}
}
