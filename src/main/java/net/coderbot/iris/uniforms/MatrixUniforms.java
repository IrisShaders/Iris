package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.minecraft.util.math.Matrix4f;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	public static void addMatrixUniforms(UniformHolder uniforms) {
		addMatrix(uniforms, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		addMatrix(uniforms, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
	}

	private static void addMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformJomlMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
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
			copy.writeToBuffer(buffer);
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
