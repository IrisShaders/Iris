package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.NullCascade;
import net.irisshaders.iris.shadows.ShadowMatrices;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.function.Supplier;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	public static void addMatrixUniforms(UniformHolder uniforms, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		addMatrix(uniforms, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		addMatrix(uniforms, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
		addDHMatrix(uniforms, "Projection", DHCompat::getProjection);
		addShadowMatrix(uniforms, "ModelView", () ->
			new Matrix4f(ShadowRenderer.createShadowModelView(directives.getSunPathRotation(), directives.getShadowDirectives().getIntervalSize()).last().pose()));
		addShadowMatrixProj(uniforms, updateNotifier, "Projection", () -> ShadowMatrices.createOrthoMatrix(directives.getShadowDirectives().getDistance(),
			directives.getShadowDirectives().getNearPlane() < 0 ? -DHCompat.getRenderDistance() : directives.getShadowDirectives().getNearPlane(),
			directives.getShadowDirectives().getFarPlane() < 0 ? DHCompat.getRenderDistance() : directives.getShadowDirectives().getFarPlane()));
	}

	private static void addMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
	}

	private static void addDHMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "dh" + name, supplier)
			.uniformMatrix(PER_FRAME, "dh" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "dhPrevious" + name, new Previous(supplier));
	}

	private static void addShadowMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "shadow" + name, supplier)
			.uniformMatrix(PER_FRAME, "shadow" + name + "Inverse", new Inverted(supplier));
	}

	private static NullCascade.CascadeOutput out;

	private static void addShadowMatrixProj(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, String name, Supplier<Matrix4fc> supplier) {
		updateNotifier.addListener(() -> {
			out = NullCascade.getCascades(ShadowRenderer.MODELVIEW, ShadowRenderer.nearPlane, ShadowRenderer.farPlane, ShadowRenderer.halfPlaneLength);
		});

		uniforms
			.uniformMatrixArray(PER_FRAME, "shadow" + name, ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.cascadeProjection;
			})
			.uniform1fArray(PER_FRAME, "cascadeSize", ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.cascadeSize;
			})
			.uniform2fArray(PER_FRAME, "shadowProjectionSize", ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.shadowProjectionSize;
			})
			.uniform2fArray(PER_FRAME, "shadowProjectionPos", ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.shadowProjectionPos;
			})
			.uniform2fArray(PER_FRAME, "cascadeViewMin", ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.cascadeViewMin;
			})
			.uniform2fArray(PER_FRAME, "cascadeViewMax", ShadowRenderTargets.NUM_CASCADES, () -> {
				return out.cascadeViewMax;
			})
			.uniformMatrix(PER_FRAME, "shadow" + name + "Inverse", new Inverted(supplier));
	}

	private record Inverted(Supplier<Matrix4fc> parent) implements Supplier<Matrix4fc> {
		@Override
		public Matrix4fc get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = new Matrix4f(parent.get());

			copy.invert();

			return copy;
		}
	}

	private static class Previous implements Supplier<Matrix4fc> {
		private final Supplier<Matrix4fc> parent;
		private Matrix4f previous;

		Previous(Supplier<Matrix4fc> parent) {
			this.parent = parent;
			this.previous = new Matrix4f();
		}

		@Override
		public Matrix4fc get() {
			// PERF: Don't copy + allocate these matrices every time?
			Matrix4f copy = new Matrix4f(parent.get());
			Matrix4f previous = new Matrix4f(this.previous);

			this.previous = copy;

			return previous;
		}
	}
}
