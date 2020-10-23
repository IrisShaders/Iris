package net.coderbot.iris.uniforms;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class TexturedUniforms {
	private Uniform texture;
	private Uniform lightmap;

	private Uniform gbufferModelView;
	private Uniform gbufferModelViewInverse;
	private Uniform gbufferProjection;
	private Uniform gbufferProjectionInverse;

	private Uniform cameraPosition;

	public TexturedUniforms(GlProgram program) {
		program.getProgramRef();
		texture = new Uniform(program, "texture");
		lightmap = new Uniform(program, "lightmap");
		gbufferModelView = new Uniform(program, "gbufferModelView");
		gbufferModelViewInverse = new Uniform(program, "gbufferModelViewInverse");
		gbufferProjection = new Uniform(program, "gbufferProjection");
		gbufferProjectionInverse = new Uniform(program, "gbufferProjectionInverse");
		cameraPosition = new Uniform(program, "cameraPosition");

	}

	public void update() {
		// PERF: Only update uniforms if they have changed
		GL21.glUniform1i(texture.getUniform(), 0);
		// TODO: Apparently old shaders expect the lightmap to be in texture unit #1, not #2.
		// Not sure why Mojang changed the texture unit of the lightmap - we'll need to change it back.
		GL21.glUniform1i(lightmap.getUniform(), 2);

		gbufferModelView.updateMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());
		gbufferModelViewInverse.updateMatrix(invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView()));
		gbufferProjection.updateMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
		gbufferProjectionInverse.updateMatrix(invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection()));
		Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		GL21.glUniform3f(cameraPosition.getUniform(), (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
	}
	private Matrix4f invertedCopy(Matrix4f matrix) {
		// PERF: Don't copy this matrix every time
		Matrix4f copy = matrix.copy();

		copy.invert();

		return copy;
	}
}
