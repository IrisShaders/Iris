package net.irisshaders.iris.shaderpack.properties;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.parsing.DirectiveHolder;
import org.joml.Vector4f;

import java.util.Optional;

public class PackShadowDirectives {
	// Bump this up if you want more shadow color buffers!
	// This is currently set at 2 for ShadersMod / OptiFine parity but can theoretically be bumped up to 8.
	// TODO: Make this configurable?
	public static final int MAX_SHADOW_COLOR_BUFFERS_IRIS = 8;
	public static final int MAX_SHADOW_COLOR_BUFFERS_OF = 2;

	private final OptionalBoolean shadowEnabled;
	private final OptionalBoolean dhShadowEnabled;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final boolean shouldRenderLightBlockEntities;
	private final ShadowCullState cullingState;
	private final ImmutableList<DepthSamplingSettings> depthSamplingSettings;
	private final Int2ObjectMap<SamplingSettings> colorSamplingSettings;
	private int resolution;
	// Use a boxed form so we can use null to indicate that there is not an FOV specified.
	private Float fov;
	private float distance;
	private float nearPlane;
	private float farPlane;
	private float voxelDistance;
	private float distanceRenderMul;
	private float entityShadowDistanceMul;
	private boolean explicitRenderDistance;
	private float intervalSize;

	public PackShadowDirectives(ShaderProperties properties) {
		// By default, the shadow map has a resolution of 1024x1024. It's recommended to increase this for better
		// quality.
		this.resolution = 1024;

		// By default, shadows do not use FOV, and instead use an orthographic projection controlled by shadowDistance
		//
		// If FOV is defined, shadows will use a perspective projection controlled by the FOV, and shadowDistance will
		// be disregarded for the purposes of creating the projection matrix. However, it will still be used to figure
		// out the render distance for shadows if shadowRenderDistanceMul is greater than zero.
		this.fov = null;

		// By default, an orthographic projection with a half plane of 160 meters is used, corresponding to a render
		// distance of 10 chunks.
		//
		// It's recommended for shader pack authors to lower this setting to meet their needs in addition to setting
		// shadowRenderDistanceMul to a nonzero value, since having a high shadow render distance will impact
		// performance quite heavily on most systems.
		this.distance = 160.0f;
		this.nearPlane = 0.05f;
		this.farPlane = 256.0f;
		this.voxelDistance = 0.0f;

		// By default, shadows are not culled based on distance from the player. However, pack authors may
		// enable this by setting shadowRenderDistanceMul to a nonzero value.
		//
		// Culling shadows based on the shadow matrices is often infeasible because shader packs frequently
		// employ non-linear transformations that end up fitting more far away chunks into the shadow map,
		// as well as giving higher detail to close up chunks.
		//
		// However, Iris does still does cull shadows whenever it can - but, it does so by analyzing
		// whether or not shadows can possibly be cast into the player's view, instead of just checking
		// the shadow matrices.
		this.distanceRenderMul = -1.0f;
		this.entityShadowDistanceMul = 1.0f;
		this.explicitRenderDistance = false;

		// By default, a shadow interval size of 2 meters is used. This means that the shadow camera will be snapped to
		// a grid where each grid cell is 2 meters by 2 meters by 2 meters, and it will only move either when the sun /
		// moon move, or when the player camera moves into a different grid cell.
		this.intervalSize = 2.0f;

		this.shouldRenderTerrain = properties.getShadowTerrain().orElse(true);
		this.shouldRenderTranslucent = properties.getShadowTranslucent().orElse(true);
		this.shouldRenderEntities = properties.getShadowEntities().orElse(true);
		this.shouldRenderPlayer = properties.getShadowPlayer().orElse(false);
		this.shouldRenderBlockEntities = properties.getShadowBlockEntities().orElse(true);
		this.shouldRenderLightBlockEntities = properties.getShadowLightBlockEntities().orElse(false);
		this.cullingState = properties.getShadowCulling();
		this.shadowEnabled = properties.getShadowEnabled();
		this.dhShadowEnabled = properties.getDhShadowEnabled();

		this.depthSamplingSettings = ImmutableList.of(new DepthSamplingSettings(), new DepthSamplingSettings());

		ImmutableList.Builder<SamplingSettings> colorSamplingSettings = ImmutableList.builder();

		this.colorSamplingSettings = new Int2ObjectArrayMap<>();
	}

	public PackShadowDirectives(PackShadowDirectives shadowDirectives) {
		this.resolution = shadowDirectives.resolution;
		this.fov = shadowDirectives.fov;
		this.distance = shadowDirectives.distance;
		this.nearPlane = shadowDirectives.nearPlane;
		this.farPlane = shadowDirectives.farPlane;
		this.voxelDistance = shadowDirectives.voxelDistance;
		this.distanceRenderMul = shadowDirectives.distanceRenderMul;
		this.entityShadowDistanceMul = shadowDirectives.entityShadowDistanceMul;
		this.explicitRenderDistance = shadowDirectives.explicitRenderDistance;
		this.intervalSize = shadowDirectives.intervalSize;
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain;
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent;
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities;
		this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer;
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities;
		this.shouldRenderLightBlockEntities = shadowDirectives.shouldRenderLightBlockEntities;
		this.cullingState = shadowDirectives.cullingState;
		this.depthSamplingSettings = shadowDirectives.depthSamplingSettings;
		this.colorSamplingSettings = shadowDirectives.colorSamplingSettings;
		this.shadowEnabled = shadowDirectives.shadowEnabled;
		this.dhShadowEnabled = shadowDirectives.dhShadowEnabled;
	}

	/**
	 * Handles shadowHardwareFiltering* directives
	 */
	private static void acceptHardwareFilteringSettings(DirectiveHolder directives, ImmutableList<DepthSamplingSettings> samplers) {
		// Get the default base value for the hardware filtering setting
		directives.acceptConstBooleanDirective("shadowHardwareFiltering", hardwareFiltering -> {
			for (DepthSamplingSettings samplerSettings : samplers) {
				samplerSettings.setHardwareFiltering(hardwareFiltering);
			}
		});

		// Find any per-sampler overrides for the hardware filtering setting
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowHardwareFiltering" + i;

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setHardwareFiltering);
		}
	}

	private static void acceptDepthMipmapSettings(DirectiveHolder directives, ImmutableList<DepthSamplingSettings> samplers) {
		// Get the default base value for the shadow depth mipmap setting
		directives.acceptConstBooleanDirective("generateShadowMipmap", mipmap -> {
			for (SamplingSettings samplerSettings : samplers) {
				samplerSettings.setMipmap(mipmap);
			}
		});

		// Find any per-sampler overrides for the shadow depth mipmap setting

		// Legacy override option: shadowtexMipmap, an alias for shadowtex0Mipmap
		if (!samplers.isEmpty()) {
			directives.acceptConstBooleanDirective("shadowtexMipmap", samplers.get(0)::setMipmap);
		}

		// Standard override option: shadowtex0Mipmap and shadowtex1Mipmap
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowtex" + i + "Mipmap";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setMipmap);
		}
	}

	private static void acceptColorMipmapSettings(DirectiveHolder directives, Int2ObjectMap<SamplingSettings> samplers) {
		// Get the default base value for the shadow depth mipmap setting
		directives.acceptConstBooleanDirective("generateShadowColorMipmap", mipmap -> {
			samplers.forEach((i, sampler) -> sampler.setMipmap(mipmap));
		});

		// Find any per-sampler overrides for the shadow depth mipmap setting
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowcolor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers.computeIfAbsent(i, sa -> new SamplingSettings())::setMipmap);

			name = "shadowColor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers.computeIfAbsent(i, sa -> new SamplingSettings())::setMipmap);
		}
	}

	private static void acceptDepthFilteringSettings(DirectiveHolder directives, ImmutableList<DepthSamplingSettings> samplers) {
		if (!samplers.isEmpty()) {
			directives.acceptConstBooleanDirective("shadowtexNearest", samplers.get(0)::setNearest);
		}

		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowtex" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);

			name = "shadow" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);
		}
	}

	private static void acceptColorFilteringSettings(DirectiveHolder directives, Int2ObjectMap<SamplingSettings> samplers) {
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowcolor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.computeIfAbsent(i, sa -> new SamplingSettings())::setNearest);

			name = "shadowColor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.computeIfAbsent(i, sa -> new SamplingSettings())::setNearest);

			name = "shadowColor" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers.computeIfAbsent(i, sa -> new SamplingSettings())::setNearest);
		}
	}

	public int getResolution() {
		return resolution;
	}

	public Float getFov() {
		return fov;
	}

	public float getDistance() {
		return distance;
	}

	public float getNearPlane() {
		return nearPlane;
	}

	public float getFarPlane() {
		return farPlane;
	}

	public float getVoxelDistance() {
		return voxelDistance;
	}

	public float getDistanceRenderMul() {
		return distanceRenderMul;
	}

	public float getEntityShadowDistanceMul() {
		return entityShadowDistanceMul;
	}

	public boolean isDistanceRenderMulExplicit() {
		return explicitRenderDistance;
	}

	public float getIntervalSize() {
		return intervalSize;
	}

	public boolean shouldRenderTerrain() {
		return shouldRenderTerrain;
	}

	public boolean shouldRenderTranslucent() {
		return shouldRenderTranslucent;
	}

	public boolean shouldRenderEntities() {
		return shouldRenderEntities;
	}

	public boolean shouldRenderPlayer() {
		return shouldRenderPlayer;
	}

	public boolean shouldRenderBlockEntities() {
		return shouldRenderBlockEntities;
	}

	public boolean shouldRenderLightBlockEntities() {
		return shouldRenderLightBlockEntities;
	}

	public ShadowCullState getCullingState() {
		return cullingState;
	}

	public OptionalBoolean isShadowEnabled() {
		return shadowEnabled;
	}

	public OptionalBoolean isDhShadowEnabled() {
		return dhShadowEnabled;
	}

	public ImmutableList<DepthSamplingSettings> getDepthSamplingSettings() {
		return depthSamplingSettings;
	}

	public Int2ObjectMap<SamplingSettings> getColorSamplingSettings() {
		return colorSamplingSettings;
	}

	public void acceptDirectives(DirectiveHolder directives) {
		directives.acceptCommentIntDirective("SHADOWRES", resolution -> this.resolution = resolution);
		directives.acceptConstIntDirective("shadowMapResolution", resolution -> this.resolution = resolution);

		directives.acceptCommentFloatDirective("SHADOWFOV", fov -> this.fov = fov);
		directives.acceptConstFloatDirective("shadowMapFov", fov -> this.fov = fov);

		directives.acceptCommentFloatDirective("SHADOWHPL", distance -> this.distance = distance);
		directives.acceptConstFloatDirective("shadowDistance", distance -> this.distance = distance);
		directives.acceptConstFloatDirective("shadowNearPlane", nearPlane -> this.nearPlane = nearPlane);
		directives.acceptConstFloatDirective("shadowFarPlane", farPlane -> this.farPlane = farPlane);
		directives.acceptConstFloatDirective("voxelDistance", distance -> this.voxelDistance = distance);

		directives.acceptConstFloatDirective("entityShadowDistanceMul", distance -> this.entityShadowDistanceMul = distance);

		directives.acceptConstFloatDirective("shadowDistanceRenderMul", distanceRenderMul -> {
			this.distanceRenderMul = distanceRenderMul;
			this.explicitRenderDistance = true;
		});

		directives.acceptConstFloatDirective("shadowIntervalSize",
			intervalSize -> this.intervalSize = intervalSize);

		acceptHardwareFilteringSettings(directives, depthSamplingSettings);
		acceptDepthMipmapSettings(directives, depthSamplingSettings);
		acceptColorMipmapSettings(directives, colorSamplingSettings);
		acceptDepthFilteringSettings(directives, depthSamplingSettings);
		acceptColorFilteringSettings(directives, colorSamplingSettings);
		acceptBufferDirectives(directives, colorSamplingSettings);
	}

	private void acceptBufferDirectives(DirectiveHolder directives, Int2ObjectMap<SamplingSettings> settings) {
		for (int i = 0; i < PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS; i++) {
			String bufferName = "shadowcolor" + i;
			int finalI = i;
			directives.acceptConstStringDirective(bufferName + "Format", format -> {
				Optional<InternalTextureFormat> internalFormat = InternalTextureFormat.fromString(format);

				if (internalFormat.isPresent()) {
					settings.computeIfAbsent(finalI, sa -> new SamplingSettings()).setFormat(internalFormat.get());
				} else {
					Iris.logger.warn("Unrecognized internal texture format " + format + " specified for " + bufferName + "Format, ignoring.");
				}
			});

			// TODO: Only for composite and deferred
			directives.acceptConstBooleanDirective(bufferName + "Clear",
				shouldClear -> settings.computeIfAbsent(finalI, sa -> new SamplingSettings()).setClear(shouldClear));

			// TODO: Only for composite, deferred, and final

			// Note: This is still relevant even if shouldClear is false,
			// since this will be the initial color of the buffer.
			directives.acceptConstVec4Directive(bufferName + "ClearColor",
				clearColor -> settings.computeIfAbsent(finalI, sa -> new SamplingSettings()).setClearColor(clearColor));
		}
	}

	@Override
	public String toString() {
		return "PackShadowDirectives{" +
			"resolution=" + resolution +
			", fov=" + fov +
			", distance=" + distance +
			", distanceRenderMul=" + distanceRenderMul +
			", entityDistanceRenderMul=" + entityShadowDistanceMul +
			", intervalSize=" + intervalSize +
			", depthSamplingSettings=" + depthSamplingSettings +
			", colorSamplingSettings=" + colorSamplingSettings +
			'}';
	}

	public static class SamplingSettings {
		/**
		 * Whether mipmaps should be generated before sampling. Disabled by default.
		 */
		private boolean mipmap;

		/**
		 * Whether nearest texture filtering should be used in place of linear filtering. By default, linear filtering
		 * is used, which applies some blur, but if this is not desired behavior, nearest filtering can be used.
		 */
		private boolean nearest;

		/**
		 * Whether to clear the buffer every frame.
		 */
		private boolean clear;

		/**
		 * The color to clear the buffer to. If {@code clear} is false, this has no effect.
		 */
		private Vector4f clearColor;

		/**
		 * The internal format to use for the color buffer.
		 */
		private InternalTextureFormat format;

		public SamplingSettings() {
			mipmap = false;
			nearest = false;
			clear = true;
			clearColor = new Vector4f(1.0F);
			format = InternalTextureFormat.RGBA;
		}

		public boolean getMipmap() {
			return this.mipmap;
		}

		protected void setMipmap(boolean mipmap) {
			this.mipmap = mipmap;
		}

		public boolean getNearest() {
			return this.nearest || this.format.getPixelFormat().isInteger();
		}

		protected void setNearest(boolean nearest) {
			this.nearest = nearest;
		}

		public boolean getClear() {
			return clear;
		}

		protected void setClear(boolean clear) {
			this.clear = clear;
		}

		public Vector4f getClearColor() {
			return clearColor;
		}

		protected void setClearColor(Vector4f clearColor) {
			this.clearColor = clearColor;
		}

		public InternalTextureFormat getFormat() {
			return this.format;
		}

		protected void setFormat(InternalTextureFormat format) {
			this.format = format;
		}

		@Override
		public String toString() {
			return "SamplingSettings{" +
				"mipmap=" + mipmap +
				", nearest=" + nearest +
				", clear=" + clear +
				", clearColor=" + clearColor +
				", format=" + format.name() +
				'}';
		}
	}

	public static class DepthSamplingSettings extends SamplingSettings {
		private boolean hardwareFiltering;

		public DepthSamplingSettings() {
			hardwareFiltering = false;
		}

		public boolean getHardwareFiltering() {
			return hardwareFiltering;
		}

		private void setHardwareFiltering(boolean hardwareFiltering) {
			this.hardwareFiltering = hardwareFiltering;
		}

		@Override
		public String toString() {
			return "DepthSamplingSettings{" +
				"mipmap=" + getMipmap() +
				", nearest=" + getNearest() +
				", hardwareFiltering=" + hardwareFiltering +
				'}';
		}
	}
}
