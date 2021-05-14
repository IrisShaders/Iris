package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.shadows.CullingDataCache;
import net.coderbot.iris.shadows.Matrix4fAccess;
import net.coderbot.iris.uniforms.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
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
	private static final int RESOLUTION = 3072;
	public static Matrix4f MODELVIEW;

	private final WorldRenderingPipeline pipeline;
	private final RenderTargets targets;
	private final DepthTexture noTranslucents;

	private final GlFramebuffer shadowFb;
	private final Program shadowProgram;
	private final float sunPathRotation;

	public static boolean ACTIVE = false;

	public ShadowRenderer(WorldRenderingPipeline pipeline, ProgramSource shadow, PackDirectives directives) {
		this.pipeline = pipeline;

		this.targets = new RenderTargets(RESOLUTION, RESOLUTION, new InternalTextureFormat[]{
			InternalTextureFormat.RGBA,
			InternalTextureFormat.RGBA
		});

		this.noTranslucents = new DepthTexture(RESOLUTION, RESOLUTION);

		this.shadowFb = targets.createBaselineShadowFramebuffer();

		if (shadow != null) {
			this.shadowProgram = createProgram(shadow, directives).getLeft();
		} else {
			this.shadowProgram = null;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		GlStateManager.activeTexture(GL20C.GL_TEXTURE4);
		GlStateManager.bindTexture(getDepthTextureId());

		// TODO: Don't duplicate / hardcode these things, this should be controlled by shadowHardwareFiltering

		// We have to do this or else shadow hardware filtering breaks entirely!
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		// Make sure that things are smoothed
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		GlStateManager.bindTexture(getDepthTextureNoTranslucentsId());

		// We have to do this or else shadow hardware filtering breaks entirely!
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		// Make sure that things are smoothed
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		GlStateManager.bindTexture(0);
		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);
	}

	// TODO: Don't just copy this from ShaderPipeline
	private Pair<Program, ProgramDirectives> createProgram(ProgramSource source, PackDirectives directives) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
					source.getFragmentSource().orElse(null));
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), directives);
		SamplerUniforms.addWorldSamplerUniforms(builder);

		return new Pair<>(builder.build(), source.getDirectives());
	}

	public static MatrixStack createShadowModelView(float sunPathRotation) {
		// Determine the camera position
		Vec3d cameraPos = CameraUniforms.getCameraPosition();

		double cameraX = cameraPos.getX();
		double cameraY = cameraPos.getY();
		double cameraZ = cameraPos.getZ();

		// Set up our modelview matrix stack
		MatrixStack modelView = new MatrixStack();
		ShadowMatrices.createModelViewMatrix(modelView.peek().getModel(), getShadowAngle(), 2.0f, sunPathRotation, cameraX, cameraY, cameraZ);

		return modelView;
	}

	private static Frustum createShadowFrustum(MatrixStack modelview, float[] ortho) {
		Matrix4f orthoMatrix = new Matrix4f();

		((Matrix4fAccess) (Object) orthoMatrix).copyFromArray(ortho);

		return new Frustum(modelview.peek().getModel(), orthoMatrix);
	}

	public void renderShadows(WorldRendererAccessor worldRenderer, Camera playerCamera) {
		MinecraftClient client = MinecraftClient.getInstance();

		worldRenderer.getWorld().getProfiler().swap("shadows");
		ACTIVE = true;

		// Create our camera
		MatrixStack modelView = createShadowModelView(this.sunPathRotation);
		MODELVIEW = modelView.peek().getModel().copy();
		float[] orthoMatrix = ShadowMatrices.createOrthoMatrix(HALF_PLANE_LENGTH);

		worldRenderer.getWorld().getProfiler().push("terrain_setup");

		if (worldRenderer instanceof CullingDataCache) {
			((CullingDataCache) worldRenderer).saveState();
		}

		Frustum frustum;

		// NB: This frustum assumes that the shader pack uses standard shadow mapping techniques
		// TODO: If a shader pack tries to use voxelization, we must use a different culling method!
		frustum = createShadowFrustum(modelView, orthoMatrix);

		// Determine the player camera position
		Vec3d cameraPos = CameraUniforms.getCameraPosition();

		double cameraX = cameraPos.getX();
		double cameraY = cameraPos.getY();
		double cameraZ = cameraPos.getZ();

		// Center the frustum on the player camera position
		frustum.setPosition(cameraX, cameraY, cameraZ);

		// Disable chunk occlusion culling - it's a bit complex to get this properly working with shadow rendering
		// as-is, however in the future it will be good to work on restoring it for a nice performance boost.
		//
		// TODO: Get chunk occlusion working with shadows
		boolean wasChunkCullingEnabled = client.chunkCullingEnabled;
		client.chunkCullingEnabled = false;

		// Execute the vanilla terrain setup / culling routines using our shadow frustum.
		worldRenderer.invokeSetupTerrain(playerCamera, frustum, false, worldRenderer.getFrame(), false);

		// Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
		// and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
		// chunks during traversal, and break rendering in concerning ways.
		worldRenderer.setFrame(worldRenderer.getFrame() + 1);

		client.chunkCullingEnabled = wasChunkCullingEnabled;

		worldRenderer.getWorld().getProfiler().swap("terrain");

		pipeline.pushProgram(GbufferProgram.NONE);
		pipeline.beginShadowRender();

		// Set up the shadow program
		if (shadowProgram != null) {
			shadowProgram.use();
		} else {
			GlProgramManager.useProgram(0);
		}

		// Set up and clear our framebuffer
		shadowFb.bind();

		// Ensure that the color and depth values are cleared appropriately
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clearDepth(1.0f);
		RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_COLOR_BUFFER_BIT, false);

		// Set up the viewport
		RenderSystem.viewport(0, 0, RESOLUTION, RESOLUTION);

		// Set up our orthographic projection matrix and load it into the legacy matrix stack
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		GL11.glLoadMatrixf(orthoMatrix);
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		//
		// TODO: Better way of preventing light from leaking into places where it shouldn't
		RenderSystem.disableCull();

		// Render all opaque terrain
		worldRenderer.invokeRenderLayer(RenderLayer.getSolid(), modelView, cameraX, cameraY, cameraZ);
		worldRenderer.invokeRenderLayer(RenderLayer.getCutout(), modelView, cameraX, cameraY, cameraZ);
		worldRenderer.invokeRenderLayer(RenderLayer.getCutoutMipped(), modelView, cameraX, cameraY, cameraZ);

		// Copy the content of the depth texture before rendering translucent content.
		// This is needed for the shadowtex0 / shadowtex1 split.
		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
		RenderSystem.bindTexture(noTranslucents.getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, RESOLUTION, RESOLUTION, 0);

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		worldRenderer.invokeRenderLayer(RenderLayer.getTranslucent(), modelView, cameraX, cameraY, cameraZ);
		worldRenderer.invokeRenderLayer(RenderLayer.getTripwire(), modelView, cameraX, cameraY, cameraZ);

		worldRenderer.getWorld().getProfiler().pop();

		// Restore backface culling
		RenderSystem.enableCull();

		// Make sure to unload the projection matrix
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);

		pipeline.endShadowRender();
		// Note: This unbinds the shadow framebuffer
		pipeline.popProgram(GbufferProgram.NONE);

		// Restore the old viewport
		RenderSystem.viewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());

		if (worldRenderer instanceof CullingDataCache) {
			((CullingDataCache) worldRenderer).restoreState();
		}

		ACTIVE = false;
		worldRenderer.getWorld().getProfiler().swap("updatechunks");
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

		if (!CelestialUniforms.isDay()) {
			shadowAngle -= 0.5F;
		}

		return shadowAngle;
	}

	public int getDepthTextureId() {
		return targets.getDepthTexture().getTextureId();
	}

	public int getDepthTextureNoTranslucentsId() {
		return noTranslucents.getTextureId();
	}

	public int getColorTexture0Id() {
		return targets.get(0).getMainTexture();
	}

	public int getColorTexture1Id() {
		return targets.get(1).getMainTexture();
	}

	public void destroy() {
		this.targets.destroy();

		if (this.shadowProgram != null) {
			this.shadowProgram.destroy();
		}
	}
}
