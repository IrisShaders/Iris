package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableList;

public class PackShadowDirectives {
	// Bump this up if you want more shadow color buffers!
	// This is currently set at 2 for ShadersMod / OptiFine parity but can theoretically be bumped up to 8.
	// TODO: Make this configurable?
	public static final int MAX_SHADOW_COLOR_BUFFERS = 2;

	private int resolution;
	// Use a boxed form so we can use null to indicate that there is not an FOV specified.
	private Float fov;
	private float distance;
	private float distanceRenderMul;
	private float entityShadowDistanceMul;
	private boolean explicitRenderDistance;
	private float intervalSize;

	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderBlockEntities;
	private final OptionalBoolean cullingState;

	private final ImmutableList<DepthSamplingSettings> depthSamplingSettings;
	private final ImmutableList<SamplingSettings> colorSamplingSettings;

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
		this.shouldRenderBlockEntities = properties.getShadowBlockEntities().orElse(true);
		this.cullingState = properties.getShadowCulling();

		this.depthSamplingSettings = ImmutableList.of(new DepthSamplingSettings(), new DepthSamplingSettings());

		ImmutableList.Builder<SamplingSettings> colorSamplingSettings = ImmutableList.builder();

		for (int i = 0; i < MAX_SHADOW_COLOR_BUFFERS; i++) {
			colorSamplingSettings.add(new SamplingSettings());
		}

		this.colorSamplingSettings = colorSamplingSettings.build();
	}

	public PackShadowDirectives(PackShadowDirectives shadowDirectives) {
		this.resolution = shadowDirectives.resolution;
		this.fov = shadowDirectives.fov;
		this.distance = shadowDirectives.distance;
		this.distanceRenderMul = shadowDirectives.distanceRenderMul;
		this.entityShadowDistanceMul = shadowDirectives.entityShadowDistanceMul;
		this.explicitRenderDistance = shadowDirectives.explicitRenderDistance;
		this.intervalSize = shadowDirectives.intervalSize;
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain;
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent;
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities;
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities;
		this.cullingState = shadowDirectives.cullingState;
		this.depthSamplingSettings = shadowDirectives.depthSamplingSettings;
		this.colorSamplingSettings = shadowDirectives.colorSamplingSettings;
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

	public boolean shouldRenderBlockEntities() {
		return shouldRenderBlockEntities;
	}

	public OptionalBoolean getCullingState() {
		return cullingState;
	}

	public ImmutableList<DepthSamplingSettings> getDepthSamplingSettings() {
		return depthSamplingSettings;
	}

	public ImmutableList<SamplingSettings> getColorSamplingSettings() {
		return colorSamplingSettings;
	}

	public void acceptDirectives(DirectiveHolder directives) {
		directives.acceptCommentIntDirective("SHADOWRES", resolution -> this.resolution = resolution);
		directives.acceptConstIntDirective("shadowMapResolution", resolution -> this.resolution = resolution);

		directives.acceptCommentFloatDirective("SHADOWFOV", fov -> this.fov = fov);
		directives.acceptConstFloatDirective("shadowMapFov", fov -> this.fov = fov);

		directives.acceptCommentFloatDirective("SHADOWHPL", distance -> this.distance = distance);
		directives.acceptConstFloatDirective("shadowDistance", distance -> this.distance = distance);

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
		if (samplers.size() >= 1) {
			directives.acceptConstBooleanDirective("shadowtexMipmap", samplers.get(0)::setMipmap);
		}

		// Standard override option: shadowtex0Mipmap and shadowtex1Mipmap
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowtex" + i + "Mipmap";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setMipmap);
		}
	}

	private static void acceptColorMipmapSettings(DirectiveHolder directives, ImmutableList<SamplingSettings> samplers) {
		// Get the default base value for the shadow depth mipmap setting
		directives.acceptConstBooleanDirective("generateShadowColorMipmap", mipmap -> {
			for (SamplingSettings samplerSettings : samplers) {
				samplerSettings.setMipmap(mipmap);
			}
		});

		// Find any per-sampler overrides for the shadow depth mipmap setting
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowcolor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers.get(i)::setMipmap);

			name = "shadowColor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers.get(i)::setMipmap);
		}
	}

	private static void acceptDepthFilteringSettings(DirectiveHolder directives, ImmutableList<DepthSamplingSettings> samplers) {
		if (samplers.size() >= 1) {
			directives.acceptConstBooleanDirective("shadowtexNearest", samplers.get(0)::setNearest);
		}

		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowtex" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);

			name = "shadow" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);
		}
	}

	private static void acceptColorFilteringSettings(DirectiveHolder directives, ImmutableList<SamplingSettings> samplers) {
		for (int i = 0; i < samplers.size(); i++) {
			String name = "shadowcolor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);

			name = "shadowColor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);

			name = "shadowColor" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers.get(i)::setNearest);
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

		public SamplingSettings() {
			mipmap = false;
			nearest = false;
		}

		protected void setMipmap(boolean mipmap) {
			this.mipmap = mipmap;
		}

		protected void setNearest(boolean nearest) {
			this.nearest = nearest;
		}

		public boolean getMipmap() {
			return this.mipmap;
		}

		public boolean getNearest() {
			return this.nearest;
		}

		@Override
		public String toString() {
			return "SamplingSettings{" +
					"mipmap=" + mipmap +
					", nearest=" + nearest +
					'}';
		}
	}

	public static class DepthSamplingSettings extends SamplingSettings {
		private boolean hardwareFiltering;

		public DepthSamplingSettings() {
			hardwareFiltering = false;
		}

		private void setHardwareFiltering(boolean hardwareFiltering) {
			this.hardwareFiltering = hardwareFiltering;
		}

		public boolean getHardwareFiltering() {
			return hardwareFiltering;
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
