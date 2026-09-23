package net.irisshaders.iris.compat.dh;

import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class DHCompat {
	private static boolean dhPresent;
	private final DHCompatInternal compat;

	public DHCompat(WorldRenderingPipeline pipeline, boolean renderDHShadow) {
		this.compat = dhPresent && pipeline instanceof IrisRenderingPipeline iris ? new DHCompatInternal(iris, renderDHShadow) : null;
	}

	public static void run() {
		dhPresent = IrisPlatformHelpers.getInstance().isModLoaded("distanthorizons");
		if (dhPresent) {
			LodRendererEvents.setupEventHandlers();
		}
	}

	public static Matrix4f getProjection() {
		return dhPresent ? getProjection(getNearPlane(), getFarPlane()) : new Matrix4f(
			CapturedRenderingState.INSTANCE.getGbufferProjection());
	}

	public static Matrix4f getProjection(float nearPlane, float farPlane) {
		var projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
		return new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), nearPlane, farPlane);
	}

	public static boolean lastPackIncompatible() {
		var compat = dhPresent ? DHCompatInternal.current() : null;
		return hasRenderingEnabled() && compat != null && compat.incompatiblePack();
	}

	public static float getFarPlane() {
		return dhPresent ? DHCompatInternal.getFarPlane() : 0.01f;
	}

	public static float getNearPlane() {
		return dhPresent ? DHCompatInternal.getNearPlane() : 0.01f;
	}

	public static int getRenderDistance() {
		return dhPresent ? DHCompatInternal.getRenderDistance() : Minecraft.getInstance().options.getEffectiveRenderDistance();
	}

	public static boolean checkFrame() {
		if (dhPresent) {
			LodRendererEvents.updateRenderer();
		}
		return hasRenderingEnabled();
	}

	public static boolean hasRenderingEnabled() {
		return dhPresent && DHCompatInternal.hasRenderingEnabled();
	}

	public static void addDepthSamplers(SamplerHolder samplers, Supplier<DHCompat> compat) {
		if (!dhPresent) {
			return;
		}

		samplers.addTextureSampler(depthBinding("dhDepthTex", compat, false), false, "dhDepthTex", "dhDepthTex0");
		samplers.addTextureSampler(depthBinding("dhDepthTex1", compat, true), false, "dhDepthTex1");
	}

	private static SamplerHolder.TextureBinding depthBinding(String name, Supplier<DHCompat> compat, boolean opaque) {
		return new SamplerHolder.TextureBinding(name, () -> {
			var current = compat.get();
			return current == null || current.compat == null || current.compat.incompatiblePack() ? Minecraft.getInstance().gameRenderer.mainRenderTarget()
				.getDepthTextureView() : current.compat.getDepthTexture(opaque);
		}, () -> GlSampler.NEAREST);
	}

	public static void renderLods(boolean translucent) {
		if (dhPresent) {
			LodRendererEvents.renderLods(translucent);
		}
	}

	public void prepare() {
		if (this.compat != null) {
			this.compat.prepare();
		}
	}

	public void clearPipeline() {
		if (this.compat != null) {
			this.compat.close();
		}
	}

	public DHCompatInternal getInstance() {
		return this.compat;
	}
}
