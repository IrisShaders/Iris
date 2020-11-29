package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.function.Supplier;

import net.coderbot.iris.gl.program.ProgramBuilder;

import net.minecraft.util.math.Matrix4f;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	public static void addMatrixUniforms(ProgramBuilder builder) {
		addMatrix(builder, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		addMatrix(builder, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
	}

	private static void addMatrix(ProgramBuilder builder, String name, Supplier<Matrix4f> supplier) {
		builder
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
	}

	private static class Inverted implements Supplier<Matrix4f> {
		private final Supplier<Matrix4f> parent;

		Inverted(Supplier<Matrix4f> parent) {
			this.parent = parent;
		}

		@Override
		public Matrix4f get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = parent.get().copy();

			copy.invert();

			return copy;
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
