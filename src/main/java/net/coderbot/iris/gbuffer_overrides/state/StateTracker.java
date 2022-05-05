package net.coderbot.iris.gbuffer_overrides.state;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;

public class StateTracker {
	public static final StateTracker INSTANCE = new StateTracker();

	// All textures are disabled by default

	// TextureStateShard / TextureUnit.TERRAIN
	public boolean albedoSampler;
	// LightmapStateShard / TextureUnit.LIGHTMAP
	public boolean lightmapSampler;
	// OverlayStateShard / TextureUnit.OVERLAY
	public boolean overlaySampler;

	public InputAvailability getInputs() {
		return new InputAvailability(albedoSampler,
				lightmapSampler,
				overlaySampler);
	}
}
