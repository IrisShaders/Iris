package net.coderbot.iris.shaderpack;

import java.util.Arrays;

public class PackShadowDirectives {
	private int resolution;
	// Use a boxed form so we can use null to indicate that there is not an FOV specified.
	private Float fov;
	private float distance;
	private float distanceRenderMul;
	private float intervalSize;

	private final DepthSamplingSettings[] depthSamplingSettings;
	private final SamplingSettings[] colorSamplingSettings;

	public PackShadowDirectives() {
		// By default, the shadow map has a resolution of 1024x1024. It's recommended to increase this for better
		// quality.
		this.resolution = 1024;

		// By default, shadows do not use FOV, and instead use an orthographic projection controlled by shadowDistance
		//
		// If FOV is defined, shadows will use a perspective projection controlled by the FOV, and shadowDistance will
		// be disregarded for the purposes of creating the projection matrix. However, it will still be used to figure
		// out the render distance for shadows.
		this.fov = null;

		// By default, an orthographic projection with a half plane of 160 meters is used, corresponding to a render
		// distance of 10 chunks.
		//
		// It's recommended for shader pack authors to lower this setting to meet their needs, since having a render
		// distance that is this high will impact performance quite heavily on most systems.
		this.distance = 160.0f;

		// By default, Iris uses the shadow distance / half plane length to determine the render distance within the
		// shadow pass.
		//
		// ShadersMod and OptiFine set this to -1.0 by default, which prevents them from using the shadow distance to
		// restrict the render distance within the shadow pass unless the shader pack explicitly enables it.
		//
		// On OptiFine, this means that every chunk within the player's render distance will be rendered in the shadow
		// pass, destroying framerates by default. ShadersMod implements frustum culling, but OptiFine disables it!
		//
		// This seems like undesirable behavior, so I've made it so that Iris sets this variable to 1.0 by default.
		// If a shaderpack author truly relies on the unspecified shadow render distance behavior, they can set the
		// variable to -1.0 themselves.
		this.distanceRenderMul = 1.0f;

		// By default, a shadow interval size of 2 meters is used. This means that the shadow camera will be snapped to
		// a grid where each grid cell is 2 meters by 2 meters by 2 meters, and it will only move either when the sun /
		// moon move, or when the player camera moves into a different grid cell.
		this.intervalSize = 2.0f;

		this.depthSamplingSettings = new DepthSamplingSettings[] {
			new DepthSamplingSettings(),
			new DepthSamplingSettings()
		};

		this.colorSamplingSettings = new SamplingSettings[] {
			new SamplingSettings(),
			new SamplingSettings()
		};
	}

	public void acceptDirectives(DirectiveHolder directives) {
		directives.acceptCommentIntDirective("SHADOWRES", resolution -> this.resolution = resolution);
		directives.acceptConstIntDirective("shadowMapResolution", resolution -> this.resolution = resolution);

		directives.acceptCommentFloatDirective("SHADOWFOV", fov -> this.fov = fov);
		directives.acceptConstFloatDirective("shadowMapFov", fov -> this.fov = fov);

		directives.acceptCommentFloatDirective("SHADOWHPL", distance -> this.distance = distance);
		directives.acceptConstFloatDirective("shadowDistance", distance -> this.distance = distance);

		directives.acceptConstFloatDirective("shadowDistanceRenderMul",
				distanceRenderMul -> this.distanceRenderMul = distanceRenderMul);

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
	private static void acceptHardwareFilteringSettings(DirectiveHolder directives, DepthSamplingSettings[] samplers) {
		// Get the default base value for the hardware filtering setting
		directives.acceptConstBooleanDirective("shadowHardwareFiltering", hardwareFiltering -> {
			for (DepthSamplingSettings samplerSettings : samplers) {
				samplerSettings.setHardwareFiltering(hardwareFiltering);
			}
		});

		// Find any per-sampler overrides for the hardware filtering setting
		for (int i = 0; i < samplers.length; i++) {
			String name = "shadowHardwareFiltering" + i;

			directives.acceptConstBooleanDirective(name, samplers[i]::setHardwareFiltering);
		}
	}

	private static void acceptDepthMipmapSettings(DirectiveHolder directives, SamplingSettings[] samplers) {
		// Get the default base value for the shadow depth mipmap setting
		directives.acceptConstBooleanDirective("generateShadowMipmap", mipmap -> {
			for (SamplingSettings samplerSettings : samplers) {
				samplerSettings.setMipmap(mipmap);
			}
		});

		// Find any per-sampler overrides for the shadow depth mipmap setting

		// Legacy override option: shadowtexMipmap, an alias for shadowtex0Mipmap
		if (samplers.length >= 1) {
			directives.acceptConstBooleanDirective("shadowtexMipmap", samplers[0]::setMipmap);
		}

		// Standard override option: shadowtex0Mipmap and shadowtex1Mipmap
		for (int i = 0; i < samplers.length; i++) {
			String name = "shadowtex" + i + "Mipmap";

			directives.acceptConstBooleanDirective(name, samplers[i]::setMipmap);
		}
	}

	private static void acceptColorMipmapSettings(DirectiveHolder directives, SamplingSettings[] samplers) {
		// Get the default base value for the shadow depth mipmap setting
		directives.acceptConstBooleanDirective("generateShadowColorMipmap", mipmap -> {
			for (SamplingSettings samplerSettings : samplers) {
				samplerSettings.setMipmap(mipmap);
			}
		});

		// Find any per-sampler overrides for the shadow depth mipmap setting
		for (int i = 0; i < samplers.length; i++) {
			String name = "shadowcolor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers[i]::setMipmap);

			name = "shadowColor" + i + "Mipmap";
			directives.acceptConstBooleanDirective(name, samplers[i]::setMipmap);
		}
	}

	private static void acceptDepthFilteringSettings(DirectiveHolder directives, SamplingSettings[] samplers) {
		if (samplers.length >= 1) {
			directives.acceptConstBooleanDirective("shadowtexNearest", samplers[0]::setNearest);
		}

		for (int i = 0; i < samplers.length; i++) {
			String name = "shadowtex" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers[i]::setNearest);

			name = "shadow" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers[i]::setNearest);
		}
	}

	private static void acceptColorFilteringSettings(DirectiveHolder directives, SamplingSettings[] samplers) {
		for (int i = 0; i < samplers.length; i++) {
			String name = "shadowcolor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers[i]::setNearest);

			name = "shadowColor" + i + "Nearest";

			directives.acceptConstBooleanDirective(name, samplers[i]::setNearest);

			name = "shadowColor" + i + "MinMagNearest";

			directives.acceptConstBooleanDirective(name, samplers[i]::setNearest);
		}
	}

	@Override
	public String toString() {
		return "PackShadowDirectives{" +
				"resolution=" + resolution +
				", fov=" + fov +
				", distance=" + distance +
				", distanceRenderMul=" + distanceRenderMul +
				", intervalSize=" + intervalSize +
				", depthSamplingSettings=" + Arrays.toString(depthSamplingSettings) +
				", colorSamplingSettings=" + Arrays.toString(colorSamplingSettings) +
				'}';
	}

	private static class SamplingSettings {
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

		private void setMipmap(boolean mipmap) {
			this.mipmap = mipmap;
		}

		private void setNearest(boolean nearest) {
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

	private static class DepthSamplingSettings extends SamplingSettings {
		private boolean hardwareFiltering;

		public DepthSamplingSettings() {
			hardwareFiltering = false;
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
