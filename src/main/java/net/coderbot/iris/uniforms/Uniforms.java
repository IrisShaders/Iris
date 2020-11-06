package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import net.coderbot.iris.gl.uniform.ProgramUniforms;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public class Uniforms extends ProgramUniforms {
	private int gbufferModelView;
	private int gbufferModelViewInverse;
	private int gbufferProjection;
	private int gbufferProjectionInverse;

	private int viewHeight;
	private int viewWidth;

	private int cameraPosition;
	
	private int eyeAltitude;
	private int isEyeInWater;

	private int shadowLightPosition;
	private static int moonPhase;

	private MinecraftClient client;

	public Uniforms(GlProgram program) {
		super(program.getProgramRef());

		client = MinecraftClient.getInstance();

		int programId = program.getProgramRef();

		uniform1i(ONCE, "texture", TextureUnit.TERRAIN::getSamplerId);
		uniform1i(ONCE, "lightmap", TextureUnit.LIGHTMAP::getSamplerId);
		uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden);

		gbufferModelView = GL21.glGetUniformLocation(programId, "gbufferModelView");
		gbufferModelViewInverse = GL21.glGetUniformLocation(programId, "gbufferModelViewInverse");
		gbufferProjection = GL21.glGetUniformLocation(programId, "gbufferProjection");
		gbufferProjectionInverse = GL21.glGetUniformLocation(programId, "gbufferProjectionInverse");

		viewHeight = GL21.glGetUniformLocation(programId, "viewHeight");
		viewWidth = GL21.glGetUniformLocation(programId, "viewWidth");

		cameraPosition = GL21.glGetUniformLocation(programId, "cameraPosition");
		
		eyeAltitude = GL21.glGetUniformLocation(programId, "eyeAltitude");
		isEyeInWater = GL21.glGetUniformLocation(programId, "isEyeInWater");		

		shadowLightPosition = GL21.glGetUniformLocation(programId, "shadowLightPosition");
		moonPhase = GL21.glGetUniformLocation(programId, "moonPhase");
	}

	public void update() {
		updateMatrix(gbufferModelView, CapturedRenderingState.INSTANCE.getGbufferModelView());
		updateMatrix(gbufferModelViewInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView()));
		updateMatrix(gbufferProjection, CapturedRenderingState.INSTANCE.getGbufferProjection());
		updateMatrix(gbufferProjectionInverse, invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection()));

		GL21.glUniform1f(viewHeight, MinecraftClient.getInstance().getWindow().getHeight());
		GL21.glUniform1f(viewWidth, MinecraftClient.getInstance().getWindow().getWidth());


		updateVector(cameraPosition, MinecraftClient.getInstance().gameRenderer.getCamera().getPos());

		Entity cameraEntity = Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity());

		GL21.glUniform1f(eyeAltitude, (float) cameraEntity.getPos().getY());
		
		// TODO: Simplify
		int eyeInWater;
		GL21.glUniform1f(eyeAltitude, (float) cameraEntity.getPos().getY());

		if (cameraEntity.isSubmergedInWater()) {
			eyeInWater = 1;
		} else if (cameraEntity.isInLava()) {
			eyeInWater = 2;
		} else {
			eyeInWater = 0;
		}

		GL21.glUniform1i(isEyeInWater, eyeInWater);

		// TODO: Simplify this
		Vector4f shadowLightPositionVector;

		if (MinecraftClient.getInstance().world.isDay()) {
			// Sun position
			shadowLightPositionVector = new Vector4f(0.0F, 100.0F, 0.0F, 0.0F);
		} else {
			// Moon position
			shadowLightPositionVector = new Vector4f(0.0F, -100.0F, 0.0F, 0.0F);
		}

		shadowLightPositionVector.transform(CapturedRenderingState.INSTANCE.getCelestialModelView());

		updateVector(shadowLightPosition, new Vector3f(0.0F, 100.0F, 0.0F));
		
		GL21.glUniform1i(moonPhase, MinecraftClient.getInstance().world.getMoonPhase());
	}

	private void updateMatrix(int location, Matrix4f instance) {
		// PERF: Don't reallocate this buffer every time
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		instance.writeToBuffer(buffer);
		buffer.rewind();

		GL21.glUniformMatrix4fv(location, false, buffer);
	}

	private void updateVector(int location, Vec3d instance) {
		GL21.glUniform3f(location, (float) instance.x, (float) instance.y, (float) instance.z);
	}

	private void updateVector(int location, Vector3f instance) {
		GL21.glUniform3f(location, instance.getX(), instance.getY(), instance.getZ());
	}

	private Matrix4f invertedCopy(Matrix4f matrix) {
		// PERF: Don't copy this matrix every time
		Matrix4f copy = matrix.copy();

		copy.invert();

		return copy;
	}
}
