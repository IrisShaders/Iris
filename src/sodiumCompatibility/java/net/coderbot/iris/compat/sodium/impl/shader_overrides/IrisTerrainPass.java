package net.coderbot.iris.compat.sodium.impl.shader_overrides;

public enum IrisTerrainPass {
    SHADOW("shadow"),
    GBUFFER_SOLID("gbuffers_terrain"),
    GBUFFER_TRANSLUCENT("gbuffers_water");

    private final String name;

    IrisTerrainPass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
