package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.util.SodiumChunkSection;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shaderpack.properties.ShadowCullState;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.irisshaders.iris.shadows.frustum.CullEverythingFrustum;
import net.irisshaders.iris.shadows.frustum.FrustumHolder;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.irisshaders.iris.shadows.frustum.advanced.SafeZoneCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.irisshaders.iris.uniforms.CameraUniforms;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CelestialUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ShadowRenderer {
	public static boolean ACTIVE = false;
	public static List<BlockEntity> visibleBlockEntities;
	public static int renderDistance;
	public static Matrix4f MODELVIEW;
	public static Matrix4f PROJECTION;
	public static Frustum FRUSTUM;
	private final float halfPlaneLength;
	private final float nearPlane, farPlane;
	private final float voxelDistance;
	private final float renderDistanceMultiplier;
	private final float entityShadowDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	private final Float fov;
	private final ShadowRenderTargets targets;
	private final ShadowCullState packCullingState;
	private final ShadowCompositeRenderer compositeRenderer;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final boolean shouldRenderDH;
	private final float sunPathRotation;
	private final RenderBuffers buffers;
	private final List<MipmapPass> mipmapPasses = new ArrayList<>();
	private final String debugStringOverall;
	private final boolean separateHardwareSamplers;
	private final boolean shouldRenderLightBlockEntities;
	private final IrisRenderingPipeline pipeline;
	private final OutlineBufferSource outlineBuffers;
	private boolean packHasVoxelization;
	private FrustumHolder terrainFrustumHolder;
	private FrustumHolder entityFrustumHolder;
	private String debugStringTerrain = "(unavailable)";
	private int renderedShadowEntities = 0;
	private int renderedShadowBlockEntities = 0;

	private final LevelRenderState levelRenderState;
	private final SubmitNodeStorage submitNodeStorage;
	private final FeatureRenderDispatcher featureRenderDispatcher;

	public ShadowRenderer(IrisRenderingPipeline pipeline, ProgramSource shadow, PackDirectives directives,
						  ShadowRenderTargets shadowRenderTargets, ShadowCompositeRenderer compositeRenderer, CustomUniforms customUniforms, boolean separateHardwareSamplers) {

		this.pipeline = pipeline;
		this.separateHardwareSamplers = separateHardwareSamplers;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.nearPlane = shadowDirectives.getNearPlane();
		this.farPlane = shadowDirectives.getFarPlane();

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
		this.shouldRenderLightBlockEntities = shadowDirectives.shouldRenderLightBlockEntities();
		this.shouldRenderDH = shadowDirectives.isDhShadowEnabled().orElse(false);

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

		int processors = Runtime.getRuntime().availableProcessors();
		this.buffers = new RenderBuffers(processors);
		this.outlineBuffers = new OutlineBufferSource();

		configureSamplingSettings(shadowDirectives);

		levelRenderState = new LevelRenderState();
		submitNodeStorage = new SubmitNodeStorage();
		featureRenderDispatcher = new FeatureRenderDispatcher(submitNodeStorage, Minecraft.getInstance().getBlockRenderer(), buffers.bufferSource(), Minecraft.getInstance().getAtlasManager(), outlineBuffers, buffers.crumblingBufferSource(), Minecraft.getInstance().font);
	}

	public static PoseStack createShadowModelView(float sunPathRotation, float intervalSize, float nearPlane, float farPlane) {
		// Determine the camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		// Set up our modelview matrix stack
		PoseStack modelView = new PoseStack();
		ShadowMatrices.createModelViewMatrix(modelView, getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ, nearPlane, farPlane);

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

	public void setUsesImages(boolean usesImages) {
		this.packHasVoxelization = packHasVoxelization || usesImages;
	}

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
			shadowDirectives.getDepthSamplingSettings();

		final Int2ObjectMap<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
			shadowDirectives.getColorSamplingSettings();

		GlStateManager._activeTexture(GL20C.GL_TEXTURE4);

		configureDepthSampler(targets.getDepthTexture().iris$getGlId(), depthSamplingSettings.get(0));

		configureDepthSampler(targets.getDepthTextureNoTranslucents().iris$getGlId(), depthSamplingSettings.get(1));

		for (int i = 0; i < targets.getNumColorTextures(); i++) {
			if (targets.get(i) != null) {
				int glTextureId = targets.get(i).getMainTexture();

				configureSampler(glTextureId, colorSamplingSettings.computeIfAbsent(i, a -> new PackShadowDirectives.SamplingSettings()));
			}
		}

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);
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
			new int[]{GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_ONE});

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
		GlStateManager._activeTexture(GL20C.GL_TEXTURE4);

		for (MipmapPass mipmapPass : mipmapPasses) {
			setupMipmappingForTexture(mipmapPass.texture(), mipmapPass.targetFilteringMode());
		}

		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);
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

			boolean hasSafeZone = packCullingState == ShadowCullState.SAFE_ZONE;

			// Assume render multiplier is meant to be 1 if safe zone culling is on
			if (hasSafeZone && renderMultiplier < 0) renderMultiplier = 1.0f;

			double distance = (hasSafeZone ? voxelDistance : halfPlaneLength) * renderMultiplier;
			String setter = "(set by shader pack)";

			if (renderMultiplier < 0) {
				distance = IrisVideoSettings.shadowDistance * 16;
				setter = "(set by user)";
			}

			if (distance >= Minecraft.getInstance().options.getEffectiveRenderDistance() * 16 && !hasSafeZone) {
				distanceInfo = "render distance = " + Minecraft.getInstance().options.getEffectiveRenderDistance() * 16
					+ " blocks ";
				distanceInfo += Minecraft.getInstance().isLocalServer() ? "(capped by normal render distance)" : "(capped by normal/server render distance)";
				boxCuller = null;
			} else {
				distanceInfo = distance + " blocks " + setter;

				if (distance == 0.0 && !hasSafeZone) {
					cullingInfo = "no shadows rendered";
					holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
				}

				boxCuller = new BoxCuller(distance);
			}

			cullingInfo = (hasSafeZone ? "Safe Zone" : "Advanced") + " Frustum Culling enabled";

			Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

			Vector3f shadowLightVectorFromOrigin =
				new Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

			shadowLightVectorFromOrigin.normalize();

			Matrix4f projView = ((shouldRenderDH && DHCompat.hasRenderingEnabled()) ? DHCompat.getProjection() : CapturedRenderingState.INSTANCE.getGbufferProjection())
					.mul(CapturedRenderingState.INSTANCE.getGbufferModelView(), new Matrix4f());

			if (hasSafeZone) {
				return holder.setInfo(new SafeZoneCullingFrustum(projView, PROJECTION, shadowLightVectorFromOrigin, boxCuller, new BoxCuller(halfPlaneLength * renderMultiplier)), distanceInfo, cullingInfo);
			} else {
				return holder.setInfo(new AdvancedShadowCullingFrustum(projView, PROJECTION, shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);
			}
		}

		return holder;
	}

	public void setupShadowViewport() {
		// Set up the viewport
		GlStateManager._viewport(0, 0, resolution, resolution);
	}

	public void renderShadows(LevelRendererAccessor levelRenderer, Camera playerCamera, CameraRenderState renderState) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			return;
		}

		levelRenderState.cameraRenderState.blockPos = renderState.blockPos;
		levelRenderState.cameraRenderState.pos = renderState.pos;
		levelRenderState.cameraRenderState.orientation = renderState.orientation;
		levelRenderState.cameraRenderState.entityPos = renderState.entityPos;
		levelRenderState.cameraRenderState.initialized = renderState.initialized;

		Minecraft client = Minecraft.getInstance();

		ProfilerFiller profiler = Profiler.get();

		profiler.popPush("shadows");
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
		setupShadowViewport();

		// Create our camera
		PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize, nearPlane, farPlane);
		MODELVIEW = new Matrix4f(modelView.last().pose());

		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().set(MODELVIEW);

		// Set up our orthographic projection matrix and load it into RenderSystem
		Matrix4f shadowProjection;
		if (this.fov != null) {
			// If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
			shadowProjection = ShadowMatrices.createPerspectiveMatrix(this.fov);
		} else {
			shadowProjection = ShadowMatrices.createOrthoMatrix(halfPlaneLength, Mth.equal(nearPlane, -1.0f) ? -DHCompat.getRenderDistance() * 16 : nearPlane, Mth.equal(farPlane, -1.0f) ? DHCompat.getRenderDistance() * 16 : farPlane);
		}

		IrisRenderSystem.setShadowProjection(shadowProjection);

		PROJECTION = shadowProjection;

		profiler.push("terrain_setup");

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).saveState();
		}

		profiler.push("initialize frustum");

		terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

		FRUSTUM = terrainFrustumHolder.getFrustum();

		// Determine the player camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x();
		double cameraY = cameraPos.y();
		double cameraZ = cameraPos.z();

		// Center the frustum on the player camera position
		terrainFrustumHolder.getFrustum().prepare(cameraX, cameraY, cameraZ);

		profiler.pop();

		// Disable chunk occlusion culling - it's a bit complex to get this properly working with shadow rendering
		// as-is, however in the future it will be good to work on restoring it for a nice performance boost.
		//
		// TODO: Get chunk occlusion working with shadows
		boolean wasChunkCullingEnabled = client.smartCull;
		client.smartCull = false;

		ChunkRenderMatrices playerMatrices = ((LevelRendererExtension) levelRenderer).sodium$getMatrices();
		((LevelRendererExtension) levelRenderer).sodium$setMatrices(new ChunkRenderMatrices(shadowProjection, MODELVIEW));

		// Always schedule a terrain update
		// TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
		// We have to ensure that we don't regenerate clouds every frame, since that's what needsUpdate ends up doing.
		// This took up to 10% of the frame time before we applied this fix! That's really bad!

		// TODO IMS 24w35a determine clouds
		((LevelRenderer) levelRenderer).needsUpdate();

		// Execute the vanilla terrain setup / culling routines using our shadow frustum.
		levelRenderer.invokeCullTerrain(playerCamera, terrainFrustumHolder.getFrustum(),  false);

		// Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
		// and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
		// chunks during traversal, and break rendering in concerning ways.
		//worldRenderer.setFrameId(worldRenderer.getFrameId() + 1);

		client.smartCull = wasChunkCullingEnabled;

		profiler.popPush("terrain");

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		//
		// TODO: Better way of preventing light from leaking into places where it shouldn't
		GlStateManager._disableCull();

		ChunkSectionsToRender sections = new ChunkSectionsToRender(null, 0, null);
		((SodiumChunkSection) (Object) sections).sodium$setRendering(((LevelRendererExtension) levelRenderer).sodium$getWorldRenderer(),
			((LevelRendererExtension) levelRenderer).sodium$getMatrices(), cameraX, cameraY, cameraZ);

		// Render all opaque terrain unless pack requests not to
		if (shouldRenderTerrain) {
			pipeline.setPhase(WorldRenderingPhase.TERRAIN_SOLID);
			sections.renderGroup(ChunkSectionLayerGroup.OPAQUE);
			pipeline.setPhase(WorldRenderingPhase.NONE);
		}
		pipeline.setPhase(WorldRenderingPhase.ENTITIES);

		// Reset our viewport in case Sodium overrode it
		GlStateManager._viewport(0, 0, resolution, resolution);

		profiler.popPush("entities");

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
		this.levelRenderState.reset();


		if (shouldRenderEntities) {
			extractVisibleEntities(playerCamera, entityFrustumHolder.getFrustum(), Minecraft.getInstance().getDeltaTracker(), levelRenderState);
		} else if (shouldRenderPlayer) {
			Player player = Minecraft.getInstance().player;

			float g = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
			levelRenderState.entityRenderStates.add(Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(player, g));

			if (player.getVehicle() != null) {
				levelRenderState.entityRenderStates.add(Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(player.getVehicle(), g));
			}
		}

		// Render nearby entities
		//
		// Note: We must use a separate BuilderBufferStorage object here, or else very weird things will happen during
		// rendering.

		MultiBufferSource.BufferSource bufferSource = buffers.bufferSource();
		EntityRenderDispatcher dispatcher = levelRenderer.getEntityRenderDispatcher();
		RenderSystem.getModelViewStack().identity();

		renderedShadowEntities = renderEntities(levelRenderer, dispatcher, bufferSource, modelView, tickDelta, entityShadowFrustum, cameraX, cameraY, cameraZ);

		profiler.popPush("build blockentities");

		if (shouldRenderBlockEntities || shouldRenderLightBlockEntities) {
			extractVisibleBlockEntities(levelRenderer, bufferSource, modelView, tickDelta, playerCamera, levelRenderState, !shouldRenderBlockEntities && shouldRenderLightBlockEntities);
		}

		renderedShadowBlockEntities = renderBlockEntities(levelRenderer, modelView, submitNodeStorage, levelRenderState, playerCamera);

		profiler.popPush("draw entities");

		featureRenderDispatcher.renderAllFeatures();

		bufferSource.endBatch();

		copyPreTranslucentDepth(levelRenderer);

		RenderSystem.getModelViewStack().set(MODELVIEW);

		profiler.popPush("translucent terrain");
		pipeline.setPhase(WorldRenderingPhase.NONE);

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		if (shouldRenderTranslucent) {
			pipeline.setPhase(WorldRenderingPhase.TERRAIN_TRANSLUCENT);
			sections.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT);
			pipeline.setPhase(WorldRenderingPhase.NONE);
		}

		IrisRenderSystem.restorePlayerProjection();

		debugStringTerrain = ((LevelRenderer) levelRenderer).getSectionStatistics();

		profiler.popPush("generate mipmaps");

		generateMipmaps();

		profiler.popPush("restore gl state");

		// Restore backface culling
		GlStateManager._enableCull();
		((LevelRendererExtension) levelRenderer).sodium$setMatrices(playerMatrices);

		// Restore the old viewport
		GlStateManager._viewport(0, 0, client.getMainRenderTarget().width, client.getMainRenderTarget().height);

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).restoreState();
		}

		pipeline.removePhaseIfNeeded();

		GLDebug.pushGroup(901, "shadowcomp");
		compositeRenderer.renderAll();
		GLDebug.popGroup();

		levelRenderer.setRenderBuffers(playerBuffers);

		visibleBlockEntities = null;
		ACTIVE = false;

		RenderSystem.getModelViewStack().popMatrix();

		profiler.pop();
		profiler.popPush("updatechunks");
	}

	private int renderBlockEntities(LevelRendererAccessor levelRenderer, PoseStack modelView, SubmitNodeStorage submitNodeStorage, LevelRenderState levelRenderState, Camera camera) {
		Vec3 vec3 = camera.getPosition();
		PoseStack poseStack = modelView;
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();

		int i = 0;

		for (BlockEntityRenderState blockEntityRenderState : levelRenderState.blockEntityRenderStates) {
			BlockPos blockPos = blockEntityRenderState.blockPos;
			poseStack.pushPose();
			poseStack.translate(blockPos.getX() - d, blockPos.getY() - e, blockPos.getZ() - f);
			Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(blockEntityRenderState, poseStack, submitNodeStorage, levelRenderState.cameraRenderState);
			poseStack.popPose();
			i++;
		}

		return i;
	}

	private void extractVisibleBlockEntities(LevelRendererAccessor accessor, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Camera camera, LevelRenderState levelRenderState, boolean lightsOnly) {
		accessor.invokeExtractBlockEntities(camera, tickDelta, levelRenderState);

		if (lightsOnly) {
			Iterator<BlockEntityRenderState> state = levelRenderState.blockEntityRenderStates.iterator();

			while (state.hasNext()) {
				BlockEntityRenderState blockEntityRenderState = (BlockEntityRenderState) state.next();
				if (blockEntityRenderState.blockState.getLightEmission() == 0) state.remove();
			}
		}
	}

	private int renderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		Profiler.get().push("cull");

		for (EntityRenderState entityRenderState : levelRenderState.entityRenderStates) {
			Minecraft.getInstance().getEntityRenderDispatcher().submit(entityRenderState, levelRenderState.cameraRenderState, entityRenderState.x - cameraX, entityRenderState.y - cameraY, entityRenderState.z - cameraZ, modelView, submitNodeStorage);
		}

		Profiler.get().pop();

		return levelRenderState.entityRenderStates.size();
	}

	private void extractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		TickRateManager tickRateManager = Minecraft.getInstance().level.tickRateManager();
		Entity.setViewScale(Mth.clamp((double)Minecraft.getInstance().options.getEffectiveRenderDistance() / (double)8.0F, (double)1.0F, (double)2.5F) * (Double)Minecraft.getInstance().options.entityDistanceScaling().get());

		for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
			if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(Minecraft.getInstance().player)) {
				BlockPos blockPos = entity.blockPosition();
				if ((Minecraft.getInstance().level.isOutsideBuildHeight(blockPos.getY()) || Minecraft.getInstance().levelRenderer.isSectionCompiled(blockPos))) {
					if (entity.tickCount == 0) {
						entity.xOld = entity.getX();
						entity.yOld = entity.getY();
						entity.zOld = entity.getZ();
					}

					float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
					EntityRenderState entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, g);
					levelRenderState.entityRenderStates.add(entityRenderState);
				}
			}
		}

	}
	private int renderPlayerEntity(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		Profiler.get().push("cull");

		Entity player = Minecraft.getInstance().player;

		int shadowEntities = 0;

		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
			Profiler.get().pop();
			return 0;
		}

		Profiler.get().popPush("build geometry");

		if (!player.getPassengers().isEmpty()) {
			for (int i = 0; i < player.getPassengers().size(); i++) {
				float realTickDelta = Minecraft.getInstance().level.tickRateManager().isEntityFrozen(player.getPassengers().get(i)) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
				//levelRenderer.invokeRenderEntity(player.getPassengers().get(i), cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);
				shadowEntities++;
			}
		}

		if (player.getVehicle() != null) {
			float realTickDelta = Minecraft.getInstance().level.tickRateManager().isEntityFrozen(player.getVehicle()) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
			//levelRenderer.invokeRenderEntity(player.getVehicle(), cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);
			shadowEntities++;
		}

		float realTickDelta = Minecraft.getInstance().level.tickRateManager().isEntityFrozen(player) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
		//levelRenderer.invokeRenderEntity(player, cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);

		shadowEntities++;

		Profiler.get().pop();

		return shadowEntities;
	}

	private void copyPreTranslucentDepth(LevelRendererAccessor levelRenderer) {
		Profiler.get().popPush("translucent depth copy");

		targets.copyPreTranslucentDepth();
	}

	public void addDebugText(DebugScreenDisplayer messages) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			messages.addLine("[" + Iris.MODNAME + "] Shadow Maps: off, shadow distance 0");
			return;
		}

		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			messages.addLine("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
			messages.addLine("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
			messages.addLine("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
			messages.addLine("[" + Iris.MODNAME + "] Shadow Projection: " + getProjectionInfo());
			messages.addLine("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
				+ (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
			messages.addLine("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
			messages.addLine("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());
		} else {
			messages.addLine("[" + Iris.MODNAME + "] Shadow info: " + debugStringTerrain);
			messages.addLine("[" + Iris.MODNAME + "] E: " + renderedShadowEntities);
			messages.addLine("[" + Iris.MODNAME + "] BE: " + renderedShadowBlockEntities);
		}
	}

	private String getProjectionInfo() {
		return "Near: " + nearPlane + " Far: " + farPlane + " distance " + halfPlaneLength;
	}

	private String getEntitiesDebugString() {
		return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + (Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getEntityCount())) : "disabled by pack";
	}

	private String getBlockEntitiesDebugString() {
		return (shouldRenderBlockEntities || shouldRenderLightBlockEntities) ? renderedShadowBlockEntities + "" : "disabled by pack"; // TODO: + "/" + MinecraftClient.getInstance().world.blockEntities.size();
	}

	public void destroy() {

	}

	private record MipmapPass(int texture, int targetFilteringMode) {


	}
}
