package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.vertices.IrisVertexFormats;

import java.util.Locale;

public enum ShaderKey {
	// if you auto-format this and destroy all the manual indentation, I'll steal your kneecaps

	BASIC                  (ProgramId.Basic,       AlphaTests.OFF,             DefaultVertexFormat.POSITION,                    FogMode.ENABLED),
	BASIC_COLOR            (ProgramId.Basic,       AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.POSITION_COLOR,              FogMode.OFF    ),
	TEXTURED               (ProgramId.Textured,    AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.POSITION_TEX,                FogMode.OFF    ),
	TEXTURED_COLOR         (ProgramId.Textured,    AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR,          FogMode.OFF    ),
	SKY_BASIC              (ProgramId.SkyBasic,    AlphaTests.OFF,             DefaultVertexFormat.POSITION,                    FogMode.ENABLED),
	SKY_BASIC_COLOR        (ProgramId.SkyBasic,    AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR,              FogMode.OFF    ),
	SKY_TEXTURED           (ProgramId.SkyTextured, AlphaTests.OFF,             DefaultVertexFormat.POSITION_TEX,                FogMode.OFF    ),
	SKY_TEXTURED_COLOR     (ProgramId.SkyTextured, AlphaTests.OFF,             DefaultVertexFormat.POSITION_TEX_COLOR,          FogMode.OFF    ),
	CLOUDS                 (ProgramId.Clouds,      AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,   FogMode.ENABLED),
	TERRAIN_SOLID          (ProgramId.Terrain,     AlphaTests.OFF,             IrisVertexFormats.TERRAIN,                       FogMode.ENABLED),
	TERRAIN_CUTOUT         (ProgramId.Terrain,     AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN,                       FogMode.ENABLED),
	TERRAIN_CUTOUT_MIPPED  (ProgramId.Terrain,     AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN,                       FogMode.ENABLED),
	TERRAIN_TRANSLUCENT    (ProgramId.Water,       AlphaTests.OFF,             IrisVertexFormats.TERRAIN,                       FogMode.ENABLED),
	ENTITIES_SOLID         (ProgramId.Entities,    AlphaTests.OFF,             DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	ENTITIES_SOLID_DIFFUSE (ProgramId.Entities,    AlphaTests.OFF,             DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	ENTITIES_CUTOUT        (ProgramId.Entities,    AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	ENTITIES_CUTOUT_DIFFUSE(ProgramId.Entities,    AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	ENTITIES_EYES          (ProgramId.SpiderEyes,  AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	HAND_CUTOUT            (ProgramId.Hand,        AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	HAND_CUTOUT_DIFFUSE    (ProgramId.Hand,        AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	HAND_TRANSLUCENT       (ProgramId.HandWater,   AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	HAND_WATER_DIFFUSE     (ProgramId.HandWater,   AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	LIGHTNING              (ProgramId.Entities,    AlphaTests.OFF,             DefaultVertexFormat.POSITION_COLOR,              FogMode.ENABLED),
	LEASH                  (ProgramId.Basic,       AlphaTests.OFF,             DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,     FogMode.ENABLED),
	PARTICLES              (ProgramId.TexturedLit, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE,                    FogMode.ENABLED),
	WEATHER                (ProgramId.Weather,     AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE,                    FogMode.ENABLED),
	CRUMBLING              (ProgramId.DamagedBlock,AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.BLOCK,                       FogMode.OFF    ),
	TEXT                   (ProgramId.Entities,    AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, FogMode.ENABLED),
	TEXT_INTENSITY         (ProgramId.Entities,    AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, FogMode.ENABLED),
	BLOCK_ENTITY           (ProgramId.Block,       AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	BLOCK_ENTITY_DIFFUSE   (ProgramId.Block,       AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.ENABLED),
	BEACON                 (ProgramId.BeaconBeam,  AlphaTests.OFF,             DefaultVertexFormat.BLOCK,                       FogMode.ENABLED),
	GLINT                  (ProgramId.ArmorGlint,  AlphaTests.NON_ZERO_ALPHA,  DefaultVertexFormat.POSITION_TEX,                FogMode.ENABLED),
	LINES                  (ProgramId.Basic,       AlphaTests.OFF,             DefaultVertexFormat.POSITION_COLOR_NORMAL,       FogMode.ENABLED),

	// Note: These must be at the very end.
	// TODO: SHADOW_BASIC
	// TODO: SHADOW_BASIC_COLOR
	// TODO: SHADOW_TEXTURED
	// TODO: SHADOW_TEXTURED_COLOR
	SHADOW_TERRAIN_CUTOUT  (ProgramId.Shadow,      AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN,                       FogMode.OFF    ),
	SHADOW_ENTITIES_CUTOUT (ProgramId.Shadow,      AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY,                  FogMode.OFF    ),
	SHADOW_BEACON_BEAM     (ProgramId.Shadow,      AlphaTests.OFF,             DefaultVertexFormat.BLOCK,                       FogMode.OFF    ),
	SHADOW_LINES           (ProgramId.Shadow,      AlphaTests.OFF,             DefaultVertexFormat.POSITION_COLOR_NORMAL,       FogMode.OFF    );

	private final ProgramId program;
	private final AlphaTest alphaTest;
	private final VertexFormat vertexFormat;
	private final FogMode fogMode;

	ShaderKey(ProgramId program, AlphaTest alphaTest, VertexFormat vertexFormat, FogMode fogMode) {
		this.program = program;
		this.alphaTest = alphaTest;
		this.vertexFormat = vertexFormat;
		this.fogMode = fogMode;
	}

	public ProgramId getProgram() {
		return program;
	}

	public AlphaTest getAlphaTest() {
		return alphaTest;
	}

	public VertexFormat getVertexFormat() {
		return vertexFormat;
	}

	public FogMode getFogMode() {
		return fogMode;
	}

	public boolean isIntensity() {
		return this == TEXT_INTENSITY;
	}

	public String getName() {
		return toString().toLowerCase(Locale.ROOT);
	}

	public boolean isShadow() {
		return this.getProgram() == ProgramId.Shadow;
	}

	public boolean hasDiffuseLighting() {
		return this == ENTITIES_CUTOUT_DIFFUSE || this == ENTITIES_SOLID_DIFFUSE || this == BLOCK_ENTITY_DIFFUSE
				|| this == HAND_CUTOUT_DIFFUSE || this == HAND_WATER_DIFFUSE;
	}

	public boolean isBeaconBeam() {
		return this == BEACON || this == SHADOW_BEACON_BEAM;
	}

	public boolean isFullbright() {
		return isBeaconBeam() || this == ENTITIES_EYES;
	}
}
