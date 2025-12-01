package net.irisshaders.iris.shaderpack.loading;

import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeFunction;
import net.irisshaders.iris.gl.blending.BlendModeOverride;

import java.util.Objects;
import java.util.Optional;

public enum ProgramId {
	Shadow(ProgramGroup.Shadow, "", BlendModeOverride.OFF),
	ShadowSolid(ProgramGroup.Shadow, "solid", Shadow, BlendModeOverride.OFF),
	ShadowCutout(ProgramGroup.Shadow, "cutout", Shadow, BlendModeOverride.OFF),
	ShadowWater(ProgramGroup.Shadow, "water", Shadow, BlendModeOverride.OFF),
	ShadowEntities(ProgramGroup.Shadow, "entities", Shadow, BlendModeOverride.OFF),
	ShadowLightning(ProgramGroup.Shadow, "lightning", ShadowEntities, BlendModeOverride.OFF),
	ShadowBlock(ProgramGroup.Shadow, "block", Shadow, BlendModeOverride.OFF),

	Basic(ProgramGroup.Gbuffers, "basic"),
	Line(ProgramGroup.Gbuffers, "line", Basic),

	Textured(ProgramGroup.Gbuffers, "textured", Basic),
	TexturedLit(ProgramGroup.Gbuffers, "textured_lit", Textured),
	SkyBasic(ProgramGroup.Gbuffers, "skybasic", Basic),
	SkyTextured(ProgramGroup.Gbuffers, "skytextured", Textured),
	Clouds(ProgramGroup.Gbuffers, "clouds", Textured),

	Terrain(ProgramGroup.Gbuffers, "terrain", TexturedLit),
	TerrainSolid(ProgramGroup.Gbuffers, "terrain_solid", Terrain),
	TerrainCutout(ProgramGroup.Gbuffers, "terrain_cutout", Terrain),
	DamagedBlock(ProgramGroup.Gbuffers, "damagedblock", Terrain),

	Block(ProgramGroup.Gbuffers, "block", Terrain),
	BlockTrans(ProgramGroup.Gbuffers, "block_translucent", Block),
	BeaconBeam(ProgramGroup.Gbuffers, "beaconbeam", Textured),
	Item(ProgramGroup.Gbuffers, "item", TexturedLit),

	Entities(ProgramGroup.Gbuffers, "entities", TexturedLit),
	EntitiesTrans(ProgramGroup.Gbuffers, "entities_translucent", Entities),
	Lightning(ProgramGroup.Gbuffers, "lightning", Entities),
	Particles(ProgramGroup.Gbuffers, "particles", TexturedLit),
	ParticlesTrans(ProgramGroup.Gbuffers, "particles_translucent", Particles),
	EntitiesGlowing(ProgramGroup.Gbuffers, "entities_glowing", Entities),
	ArmorGlint(ProgramGroup.Gbuffers, "armor_glint", Textured),
	SpiderEyes(ProgramGroup.Gbuffers, "spidereyes", Textured,
		new BlendModeOverride(new BlendMode(BlendModeFunction.SRC_ALPHA.getGlId(), BlendModeFunction.ONE.getGlId(), BlendModeFunction.ZERO.getGlId(), BlendModeFunction.ONE.getGlId()))),

	Hand(ProgramGroup.Gbuffers, "hand", TexturedLit),
	Weather(ProgramGroup.Gbuffers, "weather", TexturedLit),
	Water(ProgramGroup.Gbuffers, "water", Terrain),
	HandWater(ProgramGroup.Gbuffers, "hand_water", Hand),
	DhTerrain(ProgramGroup.Dh, "terrain"),
	DhWater(ProgramGroup.Dh, "water", DhTerrain),
	DhGeneric(ProgramGroup.Dh, "generic", DhTerrain),
	DhShadow(ProgramGroup.Dh, "shadow"),

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

	ProgramId(ProgramGroup group, String name, BlendModeOverride defaultBlendOverride) {
		this.group = group;
		this.sourceName = name.isEmpty() ? group.getBaseName() : group.getBaseName() + "_" + name;
		this.fallback = null;
		this.defaultBlendOverride = defaultBlendOverride;
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

	public static ProgramId fromAPI(IrisProgram program) {
		return switch (program) {
			case BASIC -> Basic;
			case TEXTURED -> Textured;
			case TERRAIN -> Terrain;
			case TERRAIN_SOLID -> TerrainSolid;
			case TERRAIN_CUTOUT -> TerrainCutout;
			case TRANSLUCENT -> Water;
			case SKY_BASIC -> SkyBasic;
			case SKY_TEXTURED -> SkyTextured;
			case ARMOR_GLINT -> ArmorGlint;
			case ENTITIES -> Entities;
			case ENTITIES_TRANSLUCENT -> EntitiesTrans;
			case CLOUDS -> Clouds;
			case BLOCK -> Block;
			case BLOCK_TRANSLUCENT -> BlockTrans;
			case HAND -> Hand;
			case HAND_TRANSLUCENT -> HandWater;
			case PARTICLES -> Particles;
			case PARTICLES_TRANSLUCENT -> ParticlesTrans;
			case EMISSIVE_ENTITIES -> SpiderEyes;
			case BEACON_BEAM -> BeaconBeam;
			case LINES -> Line;
		};
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
