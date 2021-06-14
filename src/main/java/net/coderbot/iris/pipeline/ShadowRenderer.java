package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.coderbot.iris.fantastic.FlushableVertexConsumerProvider;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.shadows.CullingDataCache;
import net.coderbot.iris.shadows.Matrix4fAccess;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.shadows.frustum.ShadowFrustum;
import net.coderbot.iris.uniforms.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShadowRenderer implements ShadowMapRenderer {
	private final float halfPlaneLength;
	private final float renderDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	public static Matrix4f MODELVIEW;
	public static Matrix4f ORTHO;

	private final WorldRenderingPipeline pipeline;
	private final ShadowRenderTargets targets;

	private final Program shadowProgram;
	private final float sunPathRotation;

	private final BufferBuilderStorage buffers;
	private final ExtendedBufferStorage extendedBufferStorage;

	public static boolean ACTIVE = false;
	public static String OVERALL_DEBUG_STRING = "(unavailable)";
	public static String SHADOW_DEBUG_STRING = "(unavailable)";
	private static int renderedShadowEntities = 0;
	private static int renderedShadowBlockEntities = 0;

	public ShadowRenderer(WorldRenderingPipeline pipeline, ProgramSource shadow, PackDirectives directives) {
		this.pipeline = pipeline;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
		this.resolution = shadowDirectives.getResolution();
		this.intervalSize = shadowDirectives.getIntervalSize();

		OVERALL_DEBUG_STRING = "render distance = " + (renderDistanceMultiplier > 0 ? (halfPlaneLength * renderDistanceMultiplier) + " blocks" : "unlimited") + " @ " + resolution + "x" + resolution;

		if (shadowDirectives.getFov() != null) {
			// TODO: Support FOV in the shadow map for legacy shaders
			Iris.logger.warn("The shaderpack specifies a shadow FOV of " + shadowDirectives.getFov() + ", but Iris does not currently support perspective projections in the shadow pass.");
		}

		this.targets = new ShadowRenderTargets(resolution, new InternalTextureFormat[]{
			InternalTextureFormat.RGBA,
			InternalTextureFormat.RGBA
		});

		if (shadow != null) {
			this.shadowProgram = createProgram(shadow, directives).getLeft();
		} else {
			this.shadowProgram = null;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		this.buffers = new BufferBuilderStorage();

		if (this.buffers instanceof ExtendedBufferStorage) {
			this.extendedBufferStorage = (ExtendedBufferStorage) buffers;
		} else {
			this.extendedBufferStorage = null;
		}

		configureSamplingSettings(shadowDirectives);
	}

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
				shadowDirectives.getDepthSamplingSettings();

		GlStateManager.activeTexture(GL20C.GL_TEXTURE4);

		GlStateManager.bindTexture(getDepthTextureId());
		configureDepthSampler(depthSamplingSettings.get(0));

		GlStateManager.bindTexture(getDepthTextureNoTranslucentsId());
		configureDepthSampler(depthSamplingSettings.get(1));

		// TODO: Configure color samplers

		GlStateManager.bindTexture(0);
		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);
	}

	private void configureDepthSampler(PackShadowDirectives.DepthSamplingSettings settings) {
		// TODO: Mipmap support.

		if (settings.getHardwareFiltering()) {
			// We have to do this or else shadow hardware filtering breaks entirely!
			GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		}

		if (!settings.getNearest()) {
			// Make sure that things are smoothed
			GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
			GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);
		} else {
			GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_NEAREST);
			GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_NEAREST);
		}
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

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), directives, ((DeferredWorldRenderingPipeline) pipeline).getUpdateNotifier());
		SamplerUniforms.addWorldSamplerUniforms(builder);

		return new Pair<>(builder.build(), source.getDirectives());
	}

	private static void setupAttributes(Program program) {
		// Add default attribute values to avoid undefined behavior on content rendered without an extended vertex format
		// TODO: Avoid duplication with DeferredWorldRenderingPipeline
		setupAttribute(program, "mc_Entity", 10, -1.0F, -1.0F, -1.0F, -1.0F);
		setupAttribute(program, "mc_midTexCoord", 11, 0.0F, 0.0F, 0.0F, 0.0F);
		setupAttribute(program, "at_tangent", 12, 1.0F, 0.0F, 0.0F, 1.0F);
	}

	private static void setupAttribute(Program program, String name, int expectedLocation, float v0, float v1, float v2, float v3) {
		int location = GL20.glGetAttribLocation(program.getProgramId(), name);

		if (location != -1) {
			if (location != expectedLocation) {
				throw new IllegalStateException();
			}

			GL20.glVertexAttrib4f(location, v0, v1, v2, v3);
		}
	}

	public static MatrixStack createShadowModelView(float sunPathRotation, float intervalSize) {
		// Determine the camera position
		Vec3d cameraPos = CameraUniforms.getCameraPosition();

		double cameraX = cameraPos.getX();
		double cameraY = cameraPos.getY();
		double cameraZ = cameraPos.getZ();

		// Set up our modelview matrix stack
		MatrixStack modelView = new MatrixStack();
		ShadowMatrices.createModelViewMatrix(modelView.peek().getModel(), getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ);

		return modelView;
	}

	private Frustum createShadowFrustum(MatrixStack modelview, float[] ortho) {
		Matrix4f orthoMatrix = new Matrix4f();

		((Matrix4fAccess) (Object) orthoMatrix).copyFromArray(ortho);

		// TODO: Don't use the box culling thing if the render distance is less than the shadow distance, saves a few operations
		if (renderDistanceMultiplier <= 0) {
			return new Frustum(modelview.peek().getModel(), orthoMatrix);
		}

		return new ShadowFrustum(modelview.peek().getModel(), orthoMatrix, halfPlaneLength * renderDistanceMultiplier);
	}

	private Frustum createEntityShadowFrustum(MatrixStack modelview) {
		return createShadowFrustum(modelview, ShadowMatrices.createOrthoMatrix(16.0f));
	}

	@Override
	public void renderShadows(WorldRendererAccessor worldRenderer, Camera playerCamera) {
		MinecraftClient client = MinecraftClient.getInstance();

		worldRenderer.getWorld().getProfiler().swap("shadows");
		ACTIVE = true;

		// Create our camera
		MatrixStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize);
		MODELVIEW = modelView.peek().getModel().copy();
		float[] orthoMatrix = ShadowMatrices.createOrthoMatrix(halfPlaneLength);

		ORTHO = new Matrix4f();
		((Matrix4fAccess) (Object) ORTHO).copyFromArray(orthoMatrix);

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

		// Always schedule a terrain update
		// TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
		((WorldRenderer) worldRenderer).scheduleTerrainUpdate();

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
		setupShadowProgram();

		// Set up and clear our framebuffer
		targets.getFramebuffer().bind();

		// TODO: Support shadow clear color directives & disable buffer clearing
		// Ensure that the color and depth values are cleared appropriately
		RenderSystem.clearColor(255, 255, 255, 1);
		RenderSystem.clearDepth(1.0f);
		RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_COLOR_BUFFER_BIT, false);

		// Set up the viewport
		RenderSystem.viewport(0, 0, resolution, resolution);

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

		// Reset our shader program in case Sodium overrode it.
		//
		// If we forget to do this entities will be very small on most shaderpacks since they're being rendered
		// without shaders, which doesn't integrate with their shadow distortion code.
		setupShadowProgram();

		worldRenderer.getWorld().getProfiler().swap("entities");

		// Get the current tick delta. Normally this is the same as client.getTickDelta(), but when the game is paused,
		// it is set to a fixed value.
		final float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

		// Create a constrained shadow frustum for entities to avoid rendering faraway entities in the shadow pass
		// TODO: Make this configurable and disable-able
		final Frustum entityShadowFrustum = frustum; // createEntityShadowFrustum(modelView);
		// entityShadowFrustum.setPosition(cameraX, cameraY, cameraZ);

		// Render nearby entities
		//
		// Note: We must use a separate BuilderBufferStorage object here, or else very weird things will happen during
		// rendering.
		if (extendedBufferStorage != null) {
			extendedBufferStorage.beginWorldRendering();
		}

		VertexConsumerProvider.Immediate provider = buffers.getEntityVertexConsumers();
		EntityRenderDispatcher dispatcher = worldRenderer.getEntityRenderDispatcher();

		int shadowEntities = 0;

		worldRenderer.getWorld().getProfiler().push("cull");

		List<Entity> renderedEntities = new ArrayList<>(32);

		// TODO: I'm sure that this can be improved / optimized.
		for (Entity entity : getWorld().getEntities()) {
			if (!dispatcher.shouldRender(entity, entityShadowFrustum, cameraX, cameraY, cameraZ) || entity.isSpectator()) {
				continue;
			}

			renderedEntities.add(entity);
		}

		worldRenderer.getWorld().getProfiler().swap("build geometry");

		for (Entity entity : renderedEntities) {
			worldRenderer.invokeRenderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, modelView, provider);
			shadowEntities++;
		}

		worldRenderer.getWorld().getProfiler().pop();

		worldRenderer.getWorld().getProfiler().swap("build blockentities");

		int shadowBlockEntities = 0;

		// TODO: Use visibleChunks to cull block entities
		for (BlockEntity entity : getWorld().blockEntities) {
			modelView.push();
			BlockPos pos = entity.getPos();
			modelView.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
			BlockEntityRenderDispatcher.INSTANCE.render(entity, tickDelta, modelView, provider);
			modelView.pop();

			shadowBlockEntities++;
		}

		renderedShadowEntities = shadowEntities;
		renderedShadowBlockEntities = shadowBlockEntities;

		worldRenderer.getWorld().getProfiler().swap("draw entities");

		// NB: Don't try to draw the translucent parts of entities afterwards. It'll cause problems since some
		// shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
		// shadow...
		provider.draw();

		worldRenderer.getWorld().getProfiler().swap("translucent depth copy");

		// Copy the content of the depth texture before rendering translucent content.
		// This is needed for the shadowtex0 / shadowtex1 split.
		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
		RenderSystem.bindTexture(targets.getDepthTextureNoTranslucents().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, resolution, resolution, 0);

		worldRenderer.getWorld().getProfiler().swap("translucent terrain");

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		worldRenderer.invokeRenderLayer(RenderLayer.getTranslucent(), modelView, cameraX, cameraY, cameraZ);
		// Note: Apparently tripwire isn't rendered in the shadow pass.
		// worldRenderer.invokeRenderLayer(RenderLayer.getTripwire(), modelView, cameraX, cameraY, cameraZ);

		// NB: If we want to render anything after translucent terrain, we need to uncomment this line!
		// setupShadowProgram();

		if (extendedBufferStorage != null) {
			extendedBufferStorage.endWorldRendering();
		}

		SHADOW_DEBUG_STRING = ((WorldRenderer) worldRenderer).getChunksDebugString();

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

	private void setupShadowProgram() {
		if (shadowProgram != null) {
			shadowProgram.use();
			setupAttributes(shadowProgram);
		} else {
			GlProgramManager.useProgram(0);
		}
	}

	public static String getEntitiesDebugString() {
		return renderedShadowEntities + "/" + MinecraftClient.getInstance().world.getRegularEntityCount();
	}

	public static String getBlockEntitiesDebugString() {
		return renderedShadowBlockEntities + "/" + MinecraftClient.getInstance().world.blockEntities.size();
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

	@Override
	public int getDepthTextureId() {
		return targets.getDepthTexture().getTextureId();
	}

	@Override
	public int getDepthTextureNoTranslucentsId() {
		return targets.getDepthTextureNoTranslucents().getTextureId();
	}

	// TODO: Support more shadow color textures as well as support there being no shadow color textures.
	@Override
	public int getColorTexture0Id() {
		return targets.getColorTextureId(0);
	}

	@Override
	public int getColorTexture1Id() {
		return targets.getColorTextureId(1);
	}

	@Override
	public void destroy() {
		this.targets.destroy();

		if (this.shadowProgram != null) {
			this.shadowProgram.destroy();
		}
	}
}
