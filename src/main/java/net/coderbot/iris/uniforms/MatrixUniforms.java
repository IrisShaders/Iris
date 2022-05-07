package net.coderbot.iris.uniforms;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shadow.ShadowMatrices;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	public static void addMatrixUniforms(UniformHolder uniforms, PackDirectives directives) {
		addMatrix(uniforms, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		// TODO: In some cases, gbufferProjectionInverse takes on a value much different than OptiFine...
		// We need to audit Mojang's linear algebra.
		addMatrix(uniforms, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
		addShadowMatrix(uniforms, "ModelView", () ->
				ShadowRenderer.createShadowModelView(directives.getSunPathRotation(), directives.getShadowDirectives().getIntervalSize()).last().pose().copy());
		addShadowArrayMatrix(uniforms, "Projection", () -> ShadowMatrices.createOrthoMatrix(directives.getShadowDirectives().getDistance()));
	}

	private static void addMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformJomlMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
	}

	private static void addShadowMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
				.uniformMatrix(PER_FRAME, "shadow" + name, supplier)
				.uniformJomlMatrix(PER_FRAME, "shadow" + name + "Inverse", new Inverted(supplier));
	}

	private static void addShadowArrayMatrix(UniformHolder uniforms, String name, Supplier<float[]> supplier) {
		uniforms
				.uniformMatrixFromArray(PER_FRAME, "shadow" + name, supplier)
				.uniformJomlMatrix(PER_FRAME, "shadow" + name + "Inverse", new InvertedArrayMatrix(supplier));
	}

	private static class Inverted implements Supplier<net.coderbot.iris.vendored.joml.Matrix4f> {
		private final Supplier<Matrix4f> parent;

		Inverted(Supplier<Matrix4f> parent) {
			this.parent = parent;
		}

		@Override
		public net.coderbot.iris.vendored.joml.Matrix4f get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = parent.get().copy();

			FloatBuffer buffer = FloatBuffer.allocate(16);

			copy.store(buffer);
			buffer.rewind();

			net.coderbot.iris.vendored.joml.Matrix4f matrix4f = new net.coderbot.iris.vendored.joml.Matrix4f(buffer);
			matrix4f.invert();

			return matrix4f;
		}
	}

	private static class InvertedArrayMatrix implements Supplier<net.coderbot.iris.vendored.joml.Matrix4f> {
		private final Supplier<float[]> parent;

		InvertedArrayMatrix(Supplier<float[]> parent) {
			this.parent = parent;
		}

		@Override
		public net.coderbot.iris.vendored.joml.Matrix4f get() {
			FloatBuffer buffer = FloatBuffer.allocate(16);
			buffer.put(parent.get());
			buffer.rewind();

			net.coderbot.iris.vendored.joml.Matrix4f matrix4f = new net.coderbot.iris.vendored.joml.Matrix4f(buffer);
			matrix4f.invert();

			return matrix4f;
		}
	}

	private static class Previous implements Supplier<Matrix4f> {
		private final Supplier<Matrix4f> parent;
		private Matrix4f previous;

		Previous(Supplier<Matrix4f> parent) {
			this.parent = parent;
			this.previous = new Matrix4f();
		}

		@Override
		public Matrix4f get() {
			// PERF: Don't copy + allocate these matrices every time?
			Matrix4f copy = parent.get().copy();
			Matrix4f previous = this.previous.copy();

			this.previous = copy;

			return previous;
		}
	}
}
