package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.coderbot.batchedentityrendering.impl.BatchingDebugMessageHelper;
import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.coderbot.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingRenderBuffers;
import net.coderbot.batchedentityrendering.impl.RenderBuffersExt;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.program.ComputeProgram;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.shaderpack.ComputeSource;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShadowCullState;
import net.coderbot.iris.shadows.ShadowMatrices;
import net.coderbot.iris.shadows.CullingDataCache;
import net.coderbot.iris.shadows.Matrix4fAccess;
import net.coderbot.iris.shadows.ShadowCompositeRenderer;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import net.coderbot.iris.shadows.frustum.FrustumHolder;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.coderbot.iris.shadows.frustum.advanced.ReversedAdvancedShadowCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.coderbot.iris.uniforms.CameraUniforms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ShadowRenderer {
	public static boolean ACTIVE = false;
	public static List<BlockEntity> visibleBlockEntities;
	public static int renderDistance;

	private final float halfPlaneLength;
	private final float voxelDistance;
	private final float renderDistanceMultiplier;
	private final float entityShadowDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	private final Float fov;
	public static Matrix4f MODELVIEW;

	private final ShadowRenderTargets targets;
	private final ShadowCullState packCullingState;
	private final ShadowCompositeRenderer compositeRenderer;
	private boolean packHasVoxelization;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final float sunPathRotation;
	private final RenderBuffers buffers;
	private final RenderBuffersExt renderBuffersExt;
	private final List<MipmapPass> mipmapPasses = new ArrayList<>();
	private final String debugStringOverall;
	private FrustumHolder terrainFrustumHolder;
	private FrustumHolder entityFrustumHolder;
	private String debugStringTerrain = "(unavailable)";
	private int renderedShadowEntities = 0;
	private int renderedShadowBlockEntities = 0;

	private final CustomUniforms customUniforms;
	private final boolean separateHardwareSamplers;

	public ShadowRenderer(ProgramSource shadow, PackDirectives directives,
						  ShadowRenderTargets shadowRenderTargets, ShadowCompositeRenderer compositeRenderer, CustomUniforms customUniforms, boolean separateHardwareSamplers) {

		this.customUniforms = customUniforms;

		this.separateHardwareSamplers = separateHardwareSamplers;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.voxelDistance = shadowDirectives.getVoxelDistance();
		this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
		this.entityShadowDistanceMultiplier = shadowDirectives.getEntityShadowDistanceMul();
		this.resolution = shadowDirectives.getResolution();
		this.intervalSize = shadowDirectives.getIntervalSize();
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain();
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent();
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities();
		this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer();
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities();

		this.compositeRenderer = compositeRenderer;

		debugStringOverall = "half plane = " + halfPlaneLength + " meters @ " + resolution + "x" + resolution;

		this.terrainFrustumHolder = new FrustumHolder();
		this.entityFrustumHolder = new FrustumHolder();

		this.fov = shadowDirectives.getFov();
		this.targets = shadowRenderTargets;

		if (shadow != null) {
			// Assume that the shader pack is doing voxelization if a geometry shader is detected.
			// Also assume voxelization if image load / store is detected.
			this.packHasVoxelization = shadow.getGeometrySource().isPresent();
			this.packCullingState = shadowDirectives.getCullingState();
		} else {
			this.packHasVoxelization = false;
			this.packCullingState = ShadowCullState.DEFAULT;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		this.buffers = new RenderBuffers();

		if (this.buffers instanceof RenderBuffersExt) {
			this.renderBuffersExt = (RenderBuffersExt) buffers;
		} else {
			this.renderBuffersExt = null;
		}

		configureSamplingSettings(shadowDirectives);
	}

	public void setUsesImages(boolean usesImages) {
		this.packHasVoxelization = packHasVoxelization || usesImages;
	}

	public static PoseStack createShadowModelView(float sunPathRotation, float intervalSize) {
		// Determine the camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		// Set up our modelview matrix stack
		PoseStack modelView = new PoseStack();
		ShadowMatrices.createModelViewMatrix(modelView, getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ);

		return modelView;
	}

	private static ClientLevel getLevel() {
		return Objects.requireNonNull(Minecraft.getInstance().level);
	}

	private static float getSkyAngle() {
		return getLevel().getTimeOfDay(CapturedRenderingState.INSTANCE.getTickDelta());
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

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
			shadowDirectives.getDepthSamplingSettings();

		final Int2ObjectMap<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
			shadowDirectives.getColorSamplingSettings();

		RenderSystem.activeTexture(GL20C.GL_TEXTURE4);

		configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

		configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

		for (int i = 0; i < targets.getNumColorTextures(); i++) {
			if (targets.get(i) != null) {
				int glTextureId = targets.get(i).getMainTexture();

				configureSampler(glTextureId, colorSamplingSettings.computeIfAbsent(i, a -> new PackShadowDirectives.SamplingSettings()));
			}
		}

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
	}

	private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
		if (settings.getHardwareFiltering() && !separateHardwareSamplers) {
			// We have to do this or else shadow hardware filtering breaks entirely!
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		}

		// Workaround for issues with old shader packs like Chocapic v4.
		// They expected the driver to put the depth value in z, but it's supposed to only
		// be available in r. So we set up the swizzle to fix that.
		IrisRenderSystem.texParameteriv(glTextureId, GL20C.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
			new int[] { GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_ONE });

		configureSampler(glTextureId, settings);
	}

	private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
		if (settings.getMipmap()) {
			int filteringMode = settings.getNearest() ? GL20C.GL_NEAREST_MIPMAP_NEAREST : GL20C.GL_LINEAR_MIPMAP_LINEAR;
			mipmapPasses.add(new MipmapPass(glTextureId, filteringMode));
		}

		if (!settings.getNearest()) {
			// Make sure that things are smoothed
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);
		} else {
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_NEAREST);
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_NEAREST);
		}
	}

	private void generateMipmaps() {
		RenderSystem.activeTexture(GL20C.GL_TEXTURE4);

		for (MipmapPass mipmapPass : mipmapPasses) {
			setupMipmappingForTexture(mipmapPass.getTexture(), mipmapPass.getTargetFilteringMode());
		}

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
	}

	private void setupMipmappingForTexture(int texture, int filteringMode) {
		IrisRenderSystem.generateMipmaps(texture, GL20C.GL_TEXTURE_2D);
		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filteringMode);
	}

	private FrustumHolder createShadowFrustum(float renderMultiplier, FrustumHolder holder) {
		// TODO: Cull entities / block entities with Advanced Frustum Culling even if voxelization is detected.
		String distanceInfo;
		String cullingInfo;
		if ((packCullingState == ShadowCullState.DEFAULT && packHasVoxelization) || packCullingState == ShadowCullState.DISTANCE) {
			double distance = halfPlaneLength * renderMultiplier;

			String reason;

			if (packCullingState == ShadowCullState.DISTANCE) {
				reason = "(set by shader pack)";
			} else /*if (packHasVoxelization)*/ {
				reason = "(voxelization detected)";
			}

			if (distance <= 0 || distance > Minecraft.getInstance().options.getEffectiveRenderDistance() * 16) {
				distanceInfo = "render distance = " + Minecraft.getInstance().options.getEffectiveRenderDistance() * 16
					+ " blocks ";
				distanceInfo += Minecraft.getInstance().isLocalServer() ? "(capped by normal render distance)" : "(capped by normal/server render distance)";
				cullingInfo = "disabled " + reason;
				return holder.setInfo(new NonCullingFrustum(), distanceInfo, cullingInfo);
			} else {
				distanceInfo = distance + " blocks (set by shader pack)";
				cullingInfo = "distance only " + reason;
				BoxCuller boxCuller = new BoxCuller(distance);
				holder.setInfo(new BoxCullingFrustum(boxCuller), distanceInfo, cullingInfo);
			}
		} else {
			BoxCuller boxCuller;

			boolean isReversed = packCullingState == ShadowCullState.REVERSED;

			// Assume render multiplier is meant to be 1 if reversed culling is on
			if (isReversed && renderMultiplier < 0) renderMultiplier = 1.0f;

			double distance = (isReversed ? voxelDistance : halfPlaneLength) * renderMultiplier;
			String setter = "(set by shader pack)";

			if (renderMultiplier < 0) {
				distance = IrisVideoSettings.shadowDistance * 16;
				setter = "(set by user)";
			}

			if (distance >= Minecraft.getInstance().options.getEffectiveRenderDistance() * 16 && !isReversed) {
				distanceInfo = "render distance = " + Minecraft.getInstance().options.getEffectiveRenderDistance() * 16
					+ " blocks ";
				distanceInfo += Minecraft.getInstance().isLocalServer() ? "(capped by normal render distance)" : "(capped by normal/server render distance)";
				boxCuller = null;
			} else {
				distanceInfo = distance + " blocks " + setter;

				if (distance == 0.0 && !isReversed) {
					cullingInfo = "no shadows rendered";
					holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
				}

				boxCuller = new BoxCuller(distance);
			}

			cullingInfo = (isReversed ? "Reversed" : "Advanced") + " Frustum Culling enabled";

			Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

			Vector3f shadowLightVectorFromOrigin =
				new Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

			shadowLightVectorFromOrigin.normalize();

			if (isReversed) {
				return holder.setInfo(new ReversedAdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(),
					CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller, new BoxCuller(halfPlaneLength * renderMultiplier)), distanceInfo, cullingInfo);
			} else {
				return holder.setInfo(new AdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(),
					CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);
			}
		}

		return holder;
	}

	public void setupShadowViewport() {
		// Set up the viewport
		RenderSystem.viewport(0, 0, resolution, resolution);
	}

	public void renderShadows(LevelRendererAccessor levelRenderer, Camera playerCamera) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		levelRenderer.getLevel().getProfiler().popPush("shadows");
		ACTIVE = true;

		renderDistance = (int) ((halfPlaneLength * renderDistanceMultiplier) / 16);

		if (renderDistanceMultiplier < 0) {
			renderDistance = IrisVideoSettings.shadowDistance;
		}



		visibleBlockEntities = new ArrayList<>();

		// NB: We store the previous player buffers in order to be able to allow mods rendering entities in the shadow pass (Flywheel) to use the shadow buffers instead.
		RenderBuffers playerBuffers = levelRenderer.getRenderBuffers();
		levelRenderer.setRenderBuffers(buffers);

		visibleBlockEntities = new ArrayList<>();

		// Create our camera
		PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize);
		MODELVIEW = new Matrix4f(modelView.last().pose());

		levelRenderer.getLevel().getProfiler().push("terrain_setup");

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).saveState();
		}

		levelRenderer.getLevel().getProfiler().push("initialize frustum");

		terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

		// Determine the player camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x();
		double cameraY = cameraPos.y();
		double cameraZ = cameraPos.z();

		// Center the frustum on the player camera position
		terrainFrustumHolder.getFrustum().prepare(cameraX, cameraY, cameraZ);

		levelRenderer.getLevel().getProfiler().pop();

		// Disable chunk occlusion culling - it's a bit complex to get this properly working with shadow rendering
		// as-is, however in the future it will be good to work on restoring it for a nice performance boost.
		//
		// TODO: Get chunk occlusion working with shadows
		boolean wasChunkCullingEnabled = client.smartCull;
		client.smartCull = false;

		// Always schedule a terrain update
		// TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
		// We have to ensure that we don't regenerate clouds every frame, since that's what needsUpdate ends up doing.
		// This took up to 10% of the frame time before we applied this fix! That's really bad!
		boolean regenerateClouds = levelRenderer.shouldRegenerateClouds();
		((LevelRenderer) levelRenderer).needsUpdate();
		levelRenderer.setShouldRegenerateClouds(regenerateClouds);

		// Execute the vanilla terrain setup / culling routines using our shadow frustum.
		levelRenderer.invokeSetupRender(playerCamera, terrainFrustumHolder.getFrustum(), false, false);

		// Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
		// and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
		// chunks during traversal, and break rendering in concerning ways.
		//worldRenderer.setFrameId(worldRenderer.getFrameId() + 1);

		client.smartCull = wasChunkCullingEnabled;

		levelRenderer.getLevel().getProfiler().popPush("terrain");

		setupShadowViewport();

		// Set up our orthographic projection matrix and load it into RenderSystem
		Matrix4f shadowProjection;
		if (this.fov != null) {
			// If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
			shadowProjection = ShadowMatrices.createPerspectiveMatrix(this.fov);
		} else {
			shadowProjection = ShadowMatrices.createOrthoMatrix(halfPlaneLength);
		}

		IrisRenderSystem.setShadowProjection(shadowProjection);

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		//
		// TODO: Better way of preventing light from leaking into places where it shouldn't
		RenderSystem.disableCull();

		// Render all opaque terrain unless pack requests not to
		if (shouldRenderTerrain) {
			levelRenderer.invokeRenderChunkLayer(RenderType.solid(), modelView, cameraX, cameraY, cameraZ, shadowProjection);
			levelRenderer.invokeRenderChunkLayer(RenderType.cutout(), modelView, cameraX, cameraY, cameraZ, shadowProjection);
			levelRenderer.invokeRenderChunkLayer(RenderType.cutoutMipped(), modelView, cameraX, cameraY, cameraZ, shadowProjection);
		}

		// Reset our viewport in case Sodium overrode it
		RenderSystem.viewport(0, 0, resolution, resolution);

		levelRenderer.getLevel().getProfiler().popPush("entities");

		// Get the current tick delta. Normally this is the same as client.getTickDelta(), but when the game is paused,
		// it is set to a fixed value.
		final float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

		// Create a constrained shadow frustum for entities to avoid rendering faraway entities in the shadow pass,
		// if the shader pack has requested it. Otherwise, use the same frustum as for terrain.
		boolean hasEntityFrustum = false;

		if (entityShadowDistanceMultiplier == 1.0F || entityShadowDistanceMultiplier < 0.0F) {
			entityFrustumHolder.setInfo(terrainFrustumHolder.getFrustum(), terrainFrustumHolder.getDistanceInfo(), terrainFrustumHolder.getCullingInfo());
		} else {
			hasEntityFrustum = true;
			entityFrustumHolder = createShadowFrustum(renderDistanceMultiplier * entityShadowDistanceMultiplier, entityFrustumHolder);
		}

		Frustum entityShadowFrustum = entityFrustumHolder.getFrustum();
		entityShadowFrustum.prepare(cameraX, cameraY, cameraZ);

		// Render nearby entities
		//
		// Note: We must use a separate BuilderBufferStorage object here, or else very weird things will happen during
		// rendering.
		if (renderBuffersExt != null) {
			renderBuffersExt.beginLevelRendering();
		}

		if (buffers instanceof DrawCallTrackingRenderBuffers) {
			((DrawCallTrackingRenderBuffers) buffers).resetDrawCounts();
		}

		MultiBufferSource.BufferSource bufferSource = buffers.bufferSource();
		EntityRenderDispatcher dispatcher = levelRenderer.getEntityRenderDispatcher();

		if (shouldRenderEntities) {
			renderedShadowEntities = renderEntities(levelRenderer, dispatcher, bufferSource, modelView, tickDelta, entityShadowFrustum, cameraX, cameraY, cameraZ);
		} else if (shouldRenderPlayer) {
			renderedShadowEntities = renderPlayerEntity(levelRenderer, dispatcher, bufferSource, modelView, tickDelta, entityShadowFrustum, cameraX, cameraY, cameraZ);
		}

		levelRenderer.getLevel().getProfiler().popPush("build blockentities");

		if (shouldRenderBlockEntities) {
			renderedShadowBlockEntities = ShadowRenderingState.renderBlockEntities(this, bufferSource, modelView, playerCamera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum);
		}

		levelRenderer.getLevel().getProfiler().popPush("draw entities");

		// NB: Don't try to draw the translucent parts of entities afterwards in the shadow pass. It'll cause problems since some
		// shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
		// shadow...
		if (bufferSource instanceof FullyBufferedMultiBufferSource fullyBufferedMultiBufferSource) fullyBufferedMultiBufferSource.readyUp();

		bufferSource.endBatch();

		copyPreTranslucentDepth(levelRenderer);

		levelRenderer.getLevel().getProfiler().popPush("translucent terrain");

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		if (shouldRenderTranslucent) {
			levelRenderer.invokeRenderChunkLayer(RenderType.translucent(), modelView, cameraX, cameraY, cameraZ, shadowProjection);
		}

		// Note: Apparently tripwire isn't rendered in the shadow pass.
		// levelRenderer.invokeRenderChunkLayer(RenderType.tripwire(), modelView, cameraX, cameraY, cameraZ, shadowProjection);

		if (renderBuffersExt != null) {
			renderBuffersExt.endLevelRendering();
		}

		IrisRenderSystem.restorePlayerProjection();

		debugStringTerrain = ((LevelRenderer) levelRenderer).getChunkStatistics();

		levelRenderer.getLevel().getProfiler().popPush("generate mipmaps");

		generateMipmaps();

		levelRenderer.getLevel().getProfiler().popPush("restore gl state");

		// Restore backface culling
		RenderSystem.enableCull();

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		// Restore the old viewport
		RenderSystem.viewport(0, 0, client.getMainRenderTarget().width, client.getMainRenderTarget().height);

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).restoreState();
		}

		compositeRenderer.renderAll();

		levelRenderer.setRenderBuffers(playerBuffers);

		visibleBlockEntities = null;
		ACTIVE = false;

		levelRenderer.getLevel().getProfiler().pop();
		levelRenderer.getLevel().getProfiler().popPush("updatechunks");
	}

	public int renderBlockEntities(MultiBufferSource.BufferSource bufferSource, PoseStack modelView, Camera camera, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum) {
		getLevel().getProfiler().push("build blockentities");

		int shadowBlockEntities = 0;
		BoxCuller culler = null;
		if (hasEntityFrustum) {
			culler = new BoxCuller(halfPlaneLength * (renderDistanceMultiplier * entityShadowDistanceMultiplier));
			culler.setPosition(cameraX, cameraY, cameraZ);
		}

		for (BlockEntity entity : visibleBlockEntities) {
			BlockPos pos = entity.getBlockPos();
			if (hasEntityFrustum) {
				if (culler.isCulled(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					continue;
				}
			}
			modelView.pushPose();
			modelView.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
			Minecraft.getInstance().getBlockEntityRenderDispatcher().render(entity, tickDelta, modelView, bufferSource);
			modelView.popPose();

			shadowBlockEntities++;
		}

		getLevel().getProfiler().pop();

		return shadowBlockEntities;
	}

	private int renderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		levelRenderer.getLevel().getProfiler().push("cull");

		List<Entity> renderedEntities = new ArrayList<>(32);

		// TODO: I'm sure that this can be improved / optimized.
		for (Entity entity : getLevel().entitiesForRendering()) {
			if (!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ) || entity.isSpectator()) {
				continue;
			}

			renderedEntities.add(entity);
		}

		levelRenderer.getLevel().getProfiler().popPush("sort");

		// Sort the entities by type first in order to allow vanilla's entity batching system to work better.
		renderedEntities.sort(Comparator.comparingInt(entity -> entity.getType().hashCode()));

		levelRenderer.getLevel().getProfiler().popPush("build entity geometry");

		for (Entity entity : renderedEntities) {
			levelRenderer.invokeRenderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, modelView, bufferSource);
		}

		levelRenderer.getLevel().getProfiler().pop();

		return renderedEntities.size();
	}

	private int renderPlayerEntity(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		levelRenderer.getLevel().getProfiler().push("cull");

		Entity player = Minecraft.getInstance().player;

		int shadowEntities = 0;

		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
			levelRenderer.getLevel().getProfiler().pop();
			return 0;
		}

		levelRenderer.getLevel().getProfiler().popPush("build geometry");

		if (!player.getPassengers().isEmpty()) {
			for (int i = 0; i < player.getPassengers().size(); i++) {
				levelRenderer.invokeRenderEntity(player.getPassengers().get(i), cameraX, cameraY, cameraZ, tickDelta, modelView, bufferSource);
				shadowEntities++;
			}
		}

		if (player.getVehicle() != null) {
			levelRenderer.invokeRenderEntity(player.getVehicle(), cameraX, cameraY, cameraZ, tickDelta, modelView, bufferSource);
			shadowEntities++;
		}

		levelRenderer.invokeRenderEntity(player, cameraX, cameraY, cameraZ, tickDelta, modelView, bufferSource);

		shadowEntities++;

		levelRenderer.getLevel().getProfiler().pop();

		return shadowEntities;
	}

	private void copyPreTranslucentDepth(LevelRendererAccessor levelRenderer) {
		levelRenderer.getLevel().getProfiler().popPush("translucent depth copy");

		targets.copyPreTranslucentDepth();
	}

	public void addDebugText(List<String> messages) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			messages.add("[" + Iris.MODNAME + "] Shadow Maps: off, shadow distance 0");
			return;
		}

		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			messages.add("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
			messages.add("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
			messages.add("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
			messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
				+ (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
			messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
			messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());

			if (buffers instanceof DrawCallTrackingRenderBuffers && (shouldRenderEntities || shouldRenderPlayer)) {
				DrawCallTrackingRenderBuffers drawCallTracker = (DrawCallTrackingRenderBuffers) buffers;
				messages.add("[" + Iris.MODNAME + "] Shadow Entity Batching: " + BatchingDebugMessageHelper.getDebugMessage(drawCallTracker));
			}
		} else {
			messages.add("[" + Iris.MODNAME + "] Shadow info: " + debugStringTerrain);
			messages.add("[" + Iris.MODNAME + "] E: " + renderedShadowEntities);
			messages.add("[" + Iris.MODNAME + "] BE: " + renderedShadowBlockEntities);
		}
	}

	private String getEntitiesDebugString() {
		return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + Minecraft.getInstance().level.getEntityCount()) : "disabled by pack";
	}

	private String getBlockEntitiesDebugString() {
		return shouldRenderBlockEntities ? renderedShadowBlockEntities + "" : "disabled by pack"; // TODO: + "/" + MinecraftClient.getInstance().world.blockEntities.size();
	}

	public void destroy() {
		targets.destroy();
		((MemoryTrackingRenderBuffers) buffers).freeAndDeleteBuffers();
	}

	private static class MipmapPass {
		private final int texture;
		private final int targetFilteringMode;

		public MipmapPass(int texture, int targetFilteringMode) {
			this.texture = texture;
			this.targetFilteringMode = targetFilteringMode;
		}

		public int getTexture() {
			return texture;
		}

		public int getTargetFilteringMode() {
			return targetFilteringMode;
		}
	}
}
