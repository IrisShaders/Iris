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

	public boolean texAttribute;
	public boolean lightmapAttribute;
	public boolean overlayAttribute;

	public InputAvailability getInputs() {
		return new InputAvailability(texAttribute && albedoSampler,
				lightmapAttribute && lightmapSampler,
				overlaySampler && overlayAttribute);
	}
}
