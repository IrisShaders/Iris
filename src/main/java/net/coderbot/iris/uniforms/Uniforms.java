package net.coderbot.iris.uniforms;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class Uniforms {
	private int texture;
	private int lightmap;

	private int gbufferModelView;
	private int gbufferModelViewInverse;
	private int gbufferProjection;
	private int gbufferProjectionInverse;

	private int cameraPosition;

	public Uniforms(GlProgram program) {
		int programId = program.getProgramRef();

		texture = GL21.glGetUniformLocation(programId, "texture");
		lightmap = GL21.glGetUniformLocation(programId, "lightmap");

		gbufferModelView = GL21.glGetUniformLocation(programId, "gbufferModelView");
		gbufferModelViewInverse = GL21.glGetUniformLocation(programId, "gbufferModelViewInverse");
		gbufferProjection = GL21.glGetUniformLocation(programId, "gbufferProjection");
		gbufferProjectionInverse = GL21.glGetUniformLocation(programId, "gbufferProjectionInverse");

		cameraPosition = GL21.glGetUniformLocation(programId, "cameraPosition");
	}

	public void update() {
		// PERF: Only update uniforms if they have changed
		GL21.glUniform1i(texture, 0);
		// TODO: Apparently old shaders expect the lightmap to be in texture unit #1, not #2.
		// Not sure why Mojang changed the texture unit of the lightmap - we'll need to change it back.
		GL21.glUniform1i(lightmap, 2);

		updateMatrix(gbufferModelView, CapturedRenderingState.INSTANCE.getGbufferModelView());
		updateMatrix(gbufferModelViewInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView()));
		updateMatrix(gbufferProjection, CapturedRenderingState.INSTANCE.getGbufferProjection());
		updateMatrix(gbufferProjectionInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection()));

		Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		GL21.glUniform3f(cameraPosition, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
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
