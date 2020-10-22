package net.coderbot.iris.uniforms;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;

public class Uniforms {
	private int gbufferModelView;
	private int gbufferModelViewInverse;
	private int gbufferProjection;
	private int gbufferProjectionInverse;

	public Uniforms(GlProgram program) {
		int programId = program.getProgramRef();

		gbufferModelView = GL21.glGetUniformLocation(programId, "gbufferModelView");
		gbufferModelViewInverse = GL21.glGetUniformLocation(programId, "gbufferModelViewInverse");
		gbufferProjection = GL21.glGetUniformLocation(programId, "gbufferProjection");
		gbufferProjectionInverse = GL21.glGetUniformLocation(programId, "gbufferProjectionInverse");
	}

	public void update() {
		// PERF: Only update uniforms if they have changed
		updateMatrix(gbufferModelView, CapturedRenderingState.INSTANCE.getGbufferModelView());
		updateMatrix(gbufferModelViewInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView()));
		updateMatrix(gbufferProjection, CapturedRenderingState.INSTANCE.getGbufferProjection());
		updateMatrix(gbufferProjectionInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection()));
	}

	private void updateMatrix(int location, Matrix4f instance) {
		// PERF: Don't reallocate this buffer every time
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		instance.writeToBuffer(buffer);
		buffer.rewind();

		GL21.glUniformMatrix4fv(location, false, buffer);
	}

	private Matrix4f invertedCopy(Matrix4f matrix) {
		// PERF: Don't copy this matrix every time
		Matrix4f copy = matrix.copy();

		copy.invert();

		return copy;
	}
}
