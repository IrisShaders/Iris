package net.coderbot.iris.compat.sodium.impl.shader_overrides;

public enum IrisTerrainPass {
    SHADOW("shadow"),
	SHADOW_CUTOUT("shadow"),
    GBUFFER_SOLID("gbuffers_terrain"),
	GBUFFER_CUTOUT("gbuffers_terrain_cutout"),
    GBUFFER_TRANSLUCENT("gbuffers_water");

    private final String name;

    IrisTerrainPass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

	public boolean isShadow() {
		return this == SHADOW || this == SHADOW_CUTOUT;
	}
}
