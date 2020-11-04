package net.coderbot.iris.uniforms;

import java.nio.FloatBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class Uniforms {
	private Uniform texture;
	private Uniform lightmap;

	private Uniform gbufferModelView;
	private Uniform gbufferModelViewInverse;
	private Uniform gbufferProjection;
	private Uniform gbufferProjectionInverse;
	private FrameTimeCounterUniform frameTimeCounter;
	private Uniform shadowLightPosition;
	private Uniform cameraPosition;

	public Uniforms(GlProgram program) {
		program.getProgramRef();
		texture = new Uniform(program, "texture");
		lightmap = new Uniform(program, "lightmap");
		gbufferModelView = new Uniform(program, "gbufferModelView");
		gbufferModelViewInverse = new Uniform(program, "gbufferModelViewInverse");
		gbufferProjection = new Uniform(program, "gbufferProjection");
		gbufferProjectionInverse = new Uniform(program, "gbufferProjectionInverse");
		cameraPosition = new Uniform(program, "cameraPosition");
		frameTimeCounter = new FrameTimeCounterUniform(program, "frameTimeCounter");
		shadowLightPosition = new Uniform(program, "shadowLightPosition");
	}

	public void update() {
		// PERF: Only update uniforms if they have changed
		GL21.glUniform1i(texture.getUniform(), 0);
		// TODO: Apparently old shaders expect the lightmap to be in texture unit #1, not #2.
		// Not sure why Mojang changed the texture unit of the lightmap - we'll need to change it back.
		GL21.glUniform1i(lightmap.getUniform(), 2);
		try {
			gbufferModelView.update(CapturedRenderingState.INSTANCE.getGbufferModelView());
			gbufferModelViewInverse.update(invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView()));
			gbufferProjection.update(CapturedRenderingState.INSTANCE.getGbufferProjection());
			gbufferProjectionInverse.update(invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection()));
			Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
			//doesnt matter what we pass as the instance
			frameTimeCounter.update(null);
			cameraPosition.updateVector(cameraPos);
			assert MinecraftClient.getInstance().world != null;
			Vector4f shadowLightPositionVector = MinecraftClient.getInstance().world.isDay() ? new Vector4f(0.0F, 100.0F, 0.0F, 0.0F) : new Vector4f(0.0F, -100.0F, 0.0F, 0.0F);
			shadowLightPositionVector.transform(CapturedRenderingState.INSTANCE.getCelestialModelView());
			shadowLightPosition.updateVector(new Vector3f(0.0F, 100.0F, 0.0F));
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	private Matrix4f invertedCopy(Matrix4f matrix) {
		// PERF: Don't copy this matrix every time
		Matrix4f copy = matrix.copy();
		copy.invert();
		return copy;
	}
}
