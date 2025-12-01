package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTestFunction;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.vertices.IrisVertexFormats;

import java.util.Locale;
import java.util.Set;

public enum ShaderKey {
	// if you auto-format this and destroy all the manual indentation, I'll steal your kneecaps
	// update: sorry - ims

	BASIC(ProgramId.Basic, AlphaTests.OFF, DefaultVertexFormat.POSITION, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	BASIC_COLOR(ProgramId.Basic, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	TEXTURED(ProgramId.Textured, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, FogMode.OFF, LightingModel.LIGHTMAP),
	TEXTURED_COLOR(ProgramId.Textured, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	SPS(ProgramId.SpiderEyes, AlphaTests.OFF, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.PER_FRAGMENT, LightingModel.FULLBRIGHT),
	SKY_BASIC(ProgramId.SkyBasic, AlphaTests.OFF, DefaultVertexFormat.POSITION, FogMode.OFF, LightingModel.FULLBRIGHT),
	SKY_BASIC_COLOR(ProgramId.SkyBasic, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	SKY_TEXTURED(ProgramId.SkyTextured, AlphaTests.OFF, DefaultVertexFormat.POSITION_TEX, FogMode.OFF, LightingModel.LIGHTMAP),
	SKY_TEXTURED_COLOR(ProgramId.SkyTextured, AlphaTests.OFF, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	CLOUDS(ProgramId.Clouds, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	CLOUDS_SODIUM(ProgramId.Clouds, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.CLOUDS, FogMode.PER_FRAGMENT, LightingModel.LIGHTMAP),
	TERRAIN_SOLID(ProgramId.TerrainSolid, AlphaTests.OFF, IrisVertexFormats.TERRAIN, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TERRAIN_CUTOUT(ProgramId.TerrainCutout, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TERRAIN_TRANSLUCENT(ProgramId.Water, AlphaTests.OFF, IrisVertexFormats.TERRAIN, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	MOVING_BLOCK(ProgramId.Block, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	ENTITIES_ALPHA(ProgramId.Entities, AlphaTests.VERTEX_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	ENTITIES_SOLID(ProgramId.Entities, AlphaTests.OFF, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	ENTITIES_SOLID_DIFFUSE(ProgramId.Entities, AlphaTests.OFF, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	ENTITIES_SOLID_BRIGHT(ProgramId.Entities, AlphaTests.OFF, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	ENTITIES_CUTOUT(ProgramId.Entities, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	ENTITIES_CUTOUT_DIFFUSE(ProgramId.Entities, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	ENTITIES_TRANSLUCENT(ProgramId.EntitiesTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	ENTITIES_EYES(ProgramId.SpiderEyes, AlphaTests.NON_ZERO_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	ENTITIES_EYES_TRANS(ProgramId.SpiderEyes, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	HAND_CUTOUT(ProgramId.Hand, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	HAND_CUTOUT_BRIGHT(ProgramId.Hand, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	HAND_CUTOUT_DIFFUSE(ProgramId.Hand, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	HAND_TEXT(ProgramId.Hand, AlphaTests.NON_ZERO_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	HAND_TEXT_INTENSITY(ProgramId.Hand, AlphaTests.NON_ZERO_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	HAND_TRANSLUCENT(ProgramId.HandWater, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	HAND_WATER_BRIGHT(ProgramId.HandWater, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	HAND_WATER_DIFFUSE(ProgramId.HandWater, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	LIGHTNING(ProgramId.Lightning, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	LEASH(ProgramId.Basic, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TEXT_BG(ProgramId.EntitiesTrans, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	PARTICLES(ProgramId.Particles, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	PARTICLES_TRANS(ProgramId.ParticlesTrans, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	WEATHER(ProgramId.Weather, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	CRUMBLING(ProgramId.DamagedBlock, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN, FogMode.OFF, LightingModel.FULLBRIGHT),
	TEXT(ProgramId.EntitiesTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TEXT_INTENSITY(ProgramId.EntitiesTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TEXT_BE(ProgramId.BlockTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	TEXT_INTENSITY_BE(ProgramId.BlockTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.GLYPH, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	BLOCK_ENTITY(ProgramId.Block, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	BLOCK_ENTITY_BRIGHT(ProgramId.Block, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.FULLBRIGHT),
	BLOCK_ENTITY_DIFFUSE(ProgramId.Block, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	BE_TRANSLUCENT(ProgramId.BlockTrans, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.PER_VERTEX, LightingModel.DIFFUSE_LM),
	BEACON(ProgramId.BeaconBeam, AlphaTests.OFF, DefaultVertexFormat.BLOCK, FogMode.PER_FRAGMENT, LightingModel.FULLBRIGHT),
	GLINT(ProgramId.ArmorGlint, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	LINES(ProgramId.Line, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR_NORMAL, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	IE_COMPAT(ProgramId.Block, AlphaTests.ONE_TENTH_ALPHA, ShaderAccess.IE_FORMAT, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),
	MEKANISM_FLAME(ProgramId.SpiderEyes, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.PER_VERTEX, LightingModel.LIGHTMAP),

	// Note: These must be at the very end (NewWorldRenderingPipeline implementation details)
	SHADOW_TERRAIN_CUTOUT(ProgramId.ShadowCutout, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TRANSLUCENT(ProgramId.ShadowWater, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.TERRAIN, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_ENTITIES_CUTOUT(ProgramId.ShadowEntities, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_BLOCK(ProgramId.ShadowBlock, AlphaTests.ONE_TENTH_ALPHA, IrisVertexFormats.ENTITY, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_BEACON_BEAM(ProgramId.ShadowEntities, AlphaTests.OFF, DefaultVertexFormat.BLOCK, FogMode.OFF, LightingModel.FULLBRIGHT),
	SHADOW_BASIC(ProgramId.Shadow, AlphaTests.OFF, DefaultVertexFormat.POSITION, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_BASIC_COLOR(ProgramId.Shadow, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TEX(ProgramId.Shadow, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TEX_COLOR(ProgramId.Shadow, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_CLOUDS(ProgramId.Shadow, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_LINES(ProgramId.Shadow, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR_NORMAL, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_LEASH(ProgramId.Shadow, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_LIGHTNING(ProgramId.ShadowLightning, AlphaTests.OFF, DefaultVertexFormat.POSITION_COLOR, FogMode.OFF, LightingModel.FULLBRIGHT),
	SHADOW_PARTICLES(ProgramId.Shadow, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.PARTICLE, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TEXT(ProgramId.ShadowEntities, AlphaTests.NON_ZERO_ALPHA, IrisVertexFormats.GLYPH, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TEXT_BG(ProgramId.ShadowEntities, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, FogMode.OFF, LightingModel.LIGHTMAP),
	SHADOW_TEXT_INTENSITY(ProgramId.ShadowEntities, AlphaTests.NON_ZERO_ALPHA, IrisVertexFormats.GLYPH, FogMode.OFF, LightingModel.LIGHTMAP),
	IE_COMPAT_SHADOW(ProgramId.ShadowEntities, AlphaTests.ONE_TENTH_ALPHA, ShaderAccess.IE_FORMAT, FogMode.OFF, LightingModel.LIGHTMAP),
	MEKANISM_FLAME_SHADOW(ProgramId.ShadowEntities, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR, FogMode.OFF, LightingModel.LIGHTMAP);

	private final ProgramId program;
	private final AlphaTest alphaTest;
	private final VertexFormat vertexFormat;
	private final FogMode fogMode;
	private final LightingModel lightingModel;

	ShaderKey(ProgramId program, AlphaTest alphaTest, VertexFormat vertexFormat, FogMode fogMode, LightingModel lightingModel) {
		this.program = program;
		this.alphaTest = alphaTest;
		this.vertexFormat = vertexFormat;
		this.fogMode = fogMode;
		this.lightingModel = lightingModel;
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
		return this == TEXT_INTENSITY || this == TEXT_INTENSITY_BE || this == SHADOW_TEXT_INTENSITY;
	}

	public String getName() {
		return toString().toLowerCase(Locale.ROOT);
	}

	public boolean isShadow() {
		return this.getProgram() == ProgramId.Shadow
			|| this.getProgram() == ProgramId.ShadowCutout
			|| this.getProgram() == ProgramId.ShadowWater
			|| this.getProgram() == ProgramId.ShadowSolid
			|| this.getProgram() == ProgramId.ShadowEntities
			|| this.getProgram() == ProgramId.ShadowLightning
			|| this.getProgram() == ProgramId.ShadowBlock;
	}

	public boolean hasDiffuseLighting() {
		return lightingModel == LightingModel.DIFFUSE || lightingModel == LightingModel.DIFFUSE_LM;
	}

	public boolean shouldIgnoreLightmap() {
		return lightingModel == LightingModel.FULLBRIGHT || lightingModel == LightingModel.DIFFUSE;
	}

	public boolean isGlint() {
		return this == GLINT;
	}

	public boolean isText() {
		return this.name().contains("TEXT");
	}

	enum LightingModel {
		FULLBRIGHT,
		LIGHTMAP,
		DIFFUSE,
		DIFFUSE_LM
	}

	public static ShaderKey findBestMatch(RenderPipeline pipeline, ProgramId programId) {
		boolean hasAlphaTest = false;
		if (pipeline.getShaderDefines().values().containsKey("ALPHA_CUTOUT")) {
			hasAlphaTest = true;
		}

		if (hasAlphaTest) {
			for (ShaderKey key : ShaderKey.values()) {
				if (programId == key.getProgram() && pipeline.getVertexFormat() == key.vertexFormat && key.alphaTest.reference() > 0.01f && key.alphaTest.function() != AlphaTestFunction.NEVER) {
					Iris.logger.warn("Found perfect program match for " + pipeline.getLocation() + ": " + key);
					return key;
				}
			}
		}

		for (ShaderKey key : ShaderKey.values()) {
			if (programId == key.getProgram() && pipeline.getVertexFormat() == key.vertexFormat) {
				Iris.logger.warn("Found okay program match for " + pipeline.getLocation() + ": " + key);
				return key;
			}
		}

		if (hasAlphaTest) {
			for (ShaderKey key : ShaderKey.values()) {
				if (programId == key.getProgram() && key.alphaTest.reference() > 0.01f && key.alphaTest.function() != AlphaTestFunction.NEVER) {
					Iris.logger.warn("Found fine program match for " + pipeline.getLocation() + ": " + key);
					return key;
				}
			}
		}

		for (ShaderKey key : ShaderKey.values()) {
			if (programId == key.getProgram()) {
				Iris.logger.warn("Found *decent* program match for " + pipeline.getLocation() + ": " + key);
				return key;
			}
		}

		Iris.logger.warn("Somehow couldn't find any match for " + pipeline.getLocation());
		return null;
	}
}
