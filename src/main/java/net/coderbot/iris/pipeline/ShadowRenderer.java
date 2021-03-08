package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.uniforms.CameraUniforms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.Objects;

public class ShadowRenderer {
	public static final float HALF_PLANE_LENGTH = 80F;
	private static final int RESOLUTION = 1024;
	public static Matrix4f MODELVIEW;

	private final WorldRenderingPipeline pipeline;
	private final RenderTargets targets;
	private final GlFramebuffer shadowFb;
	private final Program shadowProgram;

	public ShadowRenderer(WorldRenderingPipeline pipeline, ProgramSource shadow) {
		this.pipeline = pipeline;

		this.targets = new RenderTargets(RESOLUTION, RESOLUTION, new InternalTextureFormat[]{
			InternalTextureFormat.RGBA
		});

		this.shadowFb = targets.createBaselineFramebuffer();

		if (shadow != null) {
			this.shadowProgram = createProgram(shadow).getLeft();
		} else {
			this.shadowProgram = null;
		}

		GlStateManager.activeTexture(GL20C.GL_TEXTURE4);
		GlStateManager.bindTexture(getDepthTextureId());

		// We have to do this or else shadow hardware filtering breaks entirely!
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		// Make sure that things are smoothed
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		GlStateManager.bindTexture(0);
		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);
	}

	// TODO: Don't just copy this from ShaderPipeline
	private Pair<Program, ProgramDirectives> createProgram(ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null),
					source.getFragmentSource().orElse(null));
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap());

		return new Pair<>(builder.build(), source.getDirectives());
	}

	public void renderShadows(WorldRendererAccessor worldRenderer) {
		pipeline.pushProgram(GbufferProgram.NONE);
		pipeline.beginShadowRender();

		// Create our camera
		MinecraftClient client = MinecraftClient.getInstance();
		Vec3d cameraPos = CameraUniforms.getCameraPosition();

		double cameraX = cameraPos.getX();
		double cameraY = cameraPos.getY();
		double cameraZ = cameraPos.getZ();

		// Set up our modelview matrix stack
		MatrixStack modelView = new MatrixStack();
		ShadowMatrices.createModelViewMatrix(modelView.peek().getModel(), getShadowAngle(), 2.0f, cameraX, cameraY, cameraZ);

		MODELVIEW = modelView.peek().getModel().copy();

		// Set up the shadow program
		if (shadowProgram != null) {
			shadowProgram.use();
		} else {
			GlProgramManager.useProgram(0);
		}

		// Set up and clear our framebuffer
		shadowFb.bind();
		RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_COLOR_BUFFER_BIT, false);

		// Set up the viewport
		RenderSystem.viewport(0, 0, RESOLUTION, RESOLUTION);

		// Set up our orthographic projection matrix and load it into the legacy matrix stack
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		GL11.glLoadMatrixf(ShadowMatrices.createOrthoMatrix(HALF_PLANE_LENGTH));
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);

		// Render all opaque terrain
		worldRenderer.invokeRenderLayer(RenderLayer.getSolid(), modelView, cameraX, cameraY, cameraZ);
		worldRenderer.invokeRenderLayer(RenderLayer.getCutout(), modelView, cameraX, cameraY, cameraZ);
		worldRenderer.invokeRenderLayer(RenderLayer.getCutoutMipped(), modelView, cameraX, cameraY, cameraZ);

		// Make sure to unload the projection matrix
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);

		pipeline.endShadowRender();
		// Note: This unbinds the shadow framebuffer
		pipeline.popProgram(GbufferProgram.NONE);

		// Restore the old viewport
		RenderSystem.viewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
	}

	private static ClientWorld getWorld() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world);
	}

	private static float getSkyAngle() {
		return getWorld().getSkyAngle(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static float getSunAngle() {
		float skyAngle = getSkyAngle();

		if (skyAngle < 0.75F) {
			return skyAngle + 0.25F;
		} else {
			return skyAngle - 0.75F;
		}
	}

	private static float getShadowAngle() {
		float shadowAngle = getSunAngle();

		if (!isDay()) {
			shadowAngle -= 0.5F;
		}

		return shadowAngle;
	}

	private static boolean isDay() {
		return getWorld().isDay();
	}

	public int getDepthTextureId() {
		return targets.getDepthTexture().getTextureId();
	}

	public void destroy() {
		this.shadowFb.destroy();
		this.targets.destroy();
		this.shadowProgram.destroy();
	}
}
