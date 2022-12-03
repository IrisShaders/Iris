package net.coderbot.iris.shaderpack.loading;

import net.coderbot.iris.gl.blending.BlendMode;
import net.coderbot.iris.gl.blending.BlendModeFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;

import java.util.Objects;
import java.util.Optional;

public enum ProgramId {
	Shadow(ProgramGroup.Shadow, ""),
	ShadowSolid(ProgramGroup.Shadow, "solid", Shadow),
	ShadowCutout(ProgramGroup.Shadow, "cutout", Shadow),

	Basic(ProgramGroup.Gbuffers, "basic"),
	Line(ProgramGroup.Gbuffers, "line", Basic),

	Textured(ProgramGroup.Gbuffers, "textured", Basic),
	TexturedLit(ProgramGroup.Gbuffers, "textured_lit", Textured),
	SkyBasic(ProgramGroup.Gbuffers, "skybasic", Basic),
	SkyTextured(ProgramGroup.Gbuffers, "skytextured", Textured),
	Clouds(ProgramGroup.Gbuffers, "clouds", Textured),

	Terrain(ProgramGroup.Gbuffers, "terrain", TexturedLit),
	TerrainSolid(ProgramGroup.Gbuffers, "terrain_solid", Terrain),
	TerrainCutoutMip(ProgramGroup.Gbuffers, "terrain_cutout_mip", Terrain),
	TerrainCutout(ProgramGroup.Gbuffers, "terrain_cutout", Terrain),
	DamagedBlock(ProgramGroup.Gbuffers, "damagedblock", Terrain),

	Block(ProgramGroup.Gbuffers, "block", Terrain),
	BeaconBeam(ProgramGroup.Gbuffers, "beaconbeam", Textured),
	Item(ProgramGroup.Gbuffers, "item", TexturedLit),

	Entities(ProgramGroup.Gbuffers, "entities", TexturedLit),
	EntitiesTrans(ProgramGroup.Gbuffers, "entities_translucent", Entities),
	EntitiesGlowing(ProgramGroup.Gbuffers, "entities_glowing", Entities),
	ArmorGlint(ProgramGroup.Gbuffers, "armor_glint", Textured),
	SpiderEyes(ProgramGroup.Gbuffers, "spidereyes", Textured,
			new BlendModeOverride(new BlendMode(BlendModeFunction.SRC_ALPHA.getGlId(), BlendModeFunction.ONE.getGlId(), BlendModeFunction.ZERO.getGlId(), BlendModeFunction.ONE.getGlId()))),

	Hand(ProgramGroup.Gbuffers, "hand", TexturedLit),
	Weather(ProgramGroup.Gbuffers, "weather", TexturedLit),
	Water(ProgramGroup.Gbuffers, "water", Terrain),
	HandWater(ProgramGroup.Gbuffers, "hand_water", Hand),

	Final(ProgramGroup.Final, ""),
	;

	private final ProgramGroup group;
	private final String sourceName;
	private final ProgramId fallback;
	private final BlendModeOverride defaultBlendOverride;

	ProgramId(ProgramGroup group, String name) {
		this.group = group;
		this.sourceName = name.isEmpty() ? group.getBaseName() : group.getBaseName() + "_" + name;
		this.fallback = null;
		this.defaultBlendOverride = null;
	}

	ProgramId(ProgramGroup group, String name, ProgramId fallback) {
		this.group = group;
		this.sourceName = name.isEmpty() ? group.getBaseName() : group.getBaseName() + "_" + name;
		this.fallback = Objects.requireNonNull(fallback);
		this.defaultBlendOverride = null;
	}

	ProgramId(ProgramGroup group, String name, ProgramId fallback, BlendModeOverride defaultBlendOverride) {
		this.group = group;
		this.sourceName = name.isEmpty() ? group.getBaseName() : group.getBaseName() + "_" + name;
		this.fallback = Objects.requireNonNull(fallback);
		this.defaultBlendOverride = defaultBlendOverride;
	}

	public ProgramGroup getGroup() {
		return group;
	}

	public String getSourceName() {
		return sourceName;
	}

	public Optional<ProgramId> getFallback() {
		return Optional.ofNullable(fallback);
	}

	public BlendModeOverride getBlendModeOverride() {
		return defaultBlendOverride;
	}
}
