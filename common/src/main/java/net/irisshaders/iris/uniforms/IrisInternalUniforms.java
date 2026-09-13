package net.irisshaders.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.FogStorage;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.ShadowMatrices;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * Internal Iris uniforms that are not directly accessible by shaders.
 */
public class IrisInternalUniforms {
	private static final Vector4f ONE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

	private IrisInternalUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		uniforms
			.uniform4f("iris_FogColor", () -> {
				FogParameters fog = ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters();

				if (fog == FogParameters.NONE) return ONE;
				return new Vector4f(fog.red(), fog.green(), fog.blue(), fog.alpha());
			}, t -> {});

		uniforms.uniform1f("iris_FogStart", () -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalStart(), t -> {})
			.uniform1f("iris_FogEnd", () -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalEnd(), t -> {});

		uniforms.uniform1f("iris_FogDensity", () -> {
			// ensure that the minimum value is 0.0
			return Math.max(0.0F, CapturedRenderingState.INSTANCE.getFogDensity());
		}, notifier -> {
		});

		uniforms.uniform1f("iris_currentAlphaTest", CapturedRenderingState.INSTANCE::getCurrentAlphaTest, notifier -> {
		});

		// Optifine compatibility
		uniforms.uniform1f("alphaTestRef", CapturedRenderingState.INSTANCE::getCurrentAlphaTest, notifier -> {
		});
	}

	public static void addOtherUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives) {
		uniforms.uniformMatrix3(PER_FRAME, "iris_DefaultNormalMat", () -> {
			return CapturedRenderingState.INSTANCE.getGbufferModelView().invert(new Matrix4f()).transpose3x3(new Matrix3f());
		});

		uniforms.uniformMatrix(PER_FRAME, "iris_DefaultProjectionMatrixInverse", () -> {
			return CapturedRenderingState.INSTANCE.getGbufferProjection().invert(new Matrix4f());
		});

		uniforms.uniformMatrix(PER_FRAME, "iris_DefaultModelViewMatrixInverse", () -> {
			return CapturedRenderingState.INSTANCE.getGbufferModelView().invert(new Matrix4f());
		});

		uniforms.uniformMatrix(PER_FRAME, "iris_ShadowModelViewMatrixInverse", () -> {
			return new Matrix4f(ShadowRenderer.createShadowModelView(directives.getSunPathRotation(), directives.getShadowDirectives().getIntervalSize(),
				Mth.equal(directives.getShadowDirectives().getNearPlane(), -1.0f) ? -DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getNearPlane(),
				Mth.equal(directives.getShadowDirectives().getFarPlane(), -1.0f) ? DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getFarPlane()
			).last().pose()).invert();
		});

		uniforms.uniformMatrix(PER_FRAME, "iris_ShadowProjectionMatrixInverse", () -> {
			return ShadowMatrices.createOrthoMatrix(directives.getShadowDirectives().getDistance(),
				Mth.equal(directives.getShadowDirectives().getNearPlane(), -1.0f) ? -DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getNearPlane(),
				Mth.equal(directives.getShadowDirectives().getFarPlane(), -1.0f) ? DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getFarPlane()).invert();
		});
	}
}
