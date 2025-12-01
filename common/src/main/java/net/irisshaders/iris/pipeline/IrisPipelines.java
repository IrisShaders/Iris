package net.irisshaders.iris.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.datafixers.types.Func;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.irisshaders.iris.pipeline.programs.ShaderOverrides.isBlockEntities;

public class IrisPipelines {
	private static final Map<RenderPipeline, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMap = new Object2ObjectArrayMap<>();
	private static final Map<RenderPipeline, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMapShadow = new Object2ObjectArrayMap<>();
	private static final Function<IrisRenderingPipeline, ShaderKey> FAKE_FUNCTION = p -> null;

	static {
		assignToMain(RenderPipelines.SOLID, p -> ShaderKey.TERRAIN_SOLID);
		assignToMain(RenderPipelines.CUTOUT, p -> ShaderKey.TERRAIN_CUTOUT);
		assignToMain(RenderPipelines.CUTOUT_MIPPED, p -> ShaderKey.TERRAIN_CUTOUT);
		assignToMain(RenderPipelines.TRANSLUCENT, p -> ShaderKey.TERRAIN_TRANSLUCENT);
		assignToMain(RenderPipelines.TRANSLUCENT_MOVING_BLOCK, p -> ShaderKey.MOVING_BLOCK);
		assignToMain(RenderPipelines.TRIPWIRE, p -> ShaderKey.TERRAIN_TRANSLUCENT);
		assignToMain(RenderPipelines.WORLD_BORDER, p -> ShaderKey.TEXTURED);
		assignToMain(RenderPipelines.ENTITY_CUTOUT, p -> getCutout(p));
		assignToMain(RenderPipelines.ENTITY_CUTOUT_NO_CULL, p -> getCutout(p));
		assignToMain(RenderPipelines.ENTITY_CUTOUT_NO_CULL_Z_OFFSET, p -> getCutout(p));
		assignToMain(RenderPipelines.ENTITY_SMOOTH_CUTOUT, p -> getCutout(p));
		assignToMain(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL, p -> getTranslucent(p));
		assignToMain(RenderPipelines.ENTITY_TRANSLUCENT, p -> getTranslucent(p));
		assignToMain(RenderPipelines.ENTITY_SHADOW, p -> getTranslucent(p));
		assignToMain(RenderPipelines.ENTITY_NO_OUTLINE, p -> getTranslucent(p));
		assignToMain(RenderPipelines.ENTITY_DECAL, p -> getCutout(p));
		assignToMain(RenderPipelines.LINES, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.LINE_STRIP, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.SECONDARY_BLOCK_OUTLINE, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.STARS, p -> ShaderKey.SKY_BASIC);
		assignToMain(RenderPipelines.SUNRISE_SUNSET, p -> ShaderKey.SKY_BASIC_COLOR);
		assignToMain(RenderPipelines.SKY, p -> ShaderKey.SKY_BASIC);
		assignToMain(RenderPipelines.CELESTIAL, p -> ShaderKey.SKY_TEXTURED);
		assignToMain(RenderPipelines.OPAQUE_PARTICLE, p -> ShaderKey.PARTICLES);
		assignToMain(RenderPipelines.TRANSLUCENT_PARTICLE, p -> ShaderKey.PARTICLES_TRANS);
		assignToMain(RenderPipelines.WATER_MASK, p -> ShaderKey.BASIC);
		assignToMain(RenderPipelines.GLINT, p -> ShaderKey.GLINT);
		assignToMain(RenderPipelines.ARMOR_CUTOUT_NO_CULL, p -> getCutout(p));
		assignToMain(RenderPipelines.EYES, p -> ShaderKey.ENTITIES_EYES);
		assignToMain(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.ENTITIES_EYES_TRANS);
		assignToMain(RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, p -> getCutout(p));
		assignToMain(RenderPipelines.ARMOR_TRANSLUCENT, p -> getTranslucent(p));
		assignToMain(RenderPipelines.BREEZE_WIND, p -> getTranslucent(p));
		assignToMain(RenderPipelines.ENTITY_SOLID, p -> getSolid(p));
		assignToMain(RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, p -> getSolid(p));
		assignToMain(RenderPipelines.END_GATEWAY, p -> ShaderKey.BLOCK_ENTITY);
		assignToMain(RenderPipelines.ENERGY_SWIRL, p -> ShaderKey.ENTITIES_CUTOUT);
		assignToMain(RenderPipelines.LIGHTNING, p -> ShaderKey.LIGHTNING);
		assignToMain(RenderPipelines.DRAGON_RAYS, p -> ShaderKey.LIGHTNING);
		assignToMain(RenderPipelines.DRAGON_RAYS_DEPTH, p -> ShaderKey.LIGHTNING);
		assignToMain(RenderPipelines.BEACON_BEAM_OPAQUE, p -> ShaderKey.BEACON);
		assignToMain(RenderPipelines.BEACON_BEAM_TRANSLUCENT, p -> ShaderKey.BEACON);
		assignToMain(RenderPipelines.END_PORTAL, p -> ShaderKey.BLOCK_ENTITY);
		assignToMain(RenderPipelines.END_SKY, p -> ShaderKey.SKY_TEXTURED);
		assignToMain(RenderPipelines.WEATHER_DEPTH_WRITE, p -> ShaderKey.WEATHER);
		assignToMain(RenderPipelines.WEATHER_NO_DEPTH_WRITE, p -> ShaderKey.WEATHER);
		assignToMain(RenderPipelines.TEXT, p -> getText(p));
		assignToMain(RenderPipelines.TEXT_POLYGON_OFFSET, p -> getText(p));
		assignToMain(RenderPipelines.TEXT_SEE_THROUGH, p -> getText(p));
		assignToMain(RenderPipelines.TEXT_INTENSITY_SEE_THROUGH, p -> getTextIntensity(p));
		assignToMain(RenderPipelines.TEXT_BACKGROUND, p -> ShaderKey.TEXT_BG);
		assignToMain(RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH, p -> ShaderKey.TEXT_BG);
		assignToMain(RenderPipelines.TEXT_INTENSITY, p -> getTextIntensity(p));
		assignToMain(RenderPipelines.DRAGON_EXPLOSION_ALPHA, p -> ShaderKey.ENTITIES_ALPHA);
		assignToMain(RenderPipelines.CRUMBLING, p -> ShaderKey.CRUMBLING);
		assignToMain(RenderPipelines.LEASH, p -> ShaderKey.LEASH);
		assignToMain(RenderPipelines.CLOUDS, p -> ShaderKey.CLOUDS);
		assignToMain(RenderPipelines.FLAT_CLOUDS, p -> ShaderKey.CLOUDS);
		assignToMain(RenderPipelines.DEBUG_LINE_STRIP, p -> ShaderKey.BASIC_COLOR);

		assignToShadow(RenderPipelines.SOLID, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.CUTOUT, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.CUTOUT_MIPPED, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.TRANSLUCENT, p -> ShaderKey.SHADOW_TRANSLUCENT);
		assignToShadow(RenderPipelines.TRANSLUCENT_MOVING_BLOCK, p -> ShaderKey.SHADOW_TRANSLUCENT);
		assignToShadow(RenderPipelines.TRIPWIRE, p -> ShaderKey.SHADOW_TRANSLUCENT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ARMOR_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_SOLID, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.CRUMBLING, p -> ShaderKey.SHADOW_TEX);
		assignToShadow(RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT_NO_CULL_Z_OFFSET, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_SMOOTH_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.BREEZE_WIND, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.EYES, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.DRAGON_EXPLOSION_ALPHA, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);

		assignToShadow(RenderPipelines.ENTITY_NO_OUTLINE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENERGY_SWIRL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_DECAL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.WEATHER_DEPTH_WRITE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.WEATHER_NO_DEPTH_WRITE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.OPAQUE_PARTICLE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.TRANSLUCENT_PARTICLE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.LINES, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.LINE_STRIP, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.LEASH, p -> ShaderKey.SHADOW_LEASH);
		assignToShadow(RenderPipelines.SECONDARY_BLOCK_OUTLINE, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.TEXT, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_POLYGON_OFFSET, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_INTENSITY_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
		assignToShadow(RenderPipelines.TEXT_BACKGROUND, p -> ShaderKey.SHADOW_TEXT_BG);
		assignToShadow(RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT_BG);
		assignToShadow(RenderPipelines.TEXT_INTENSITY, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
		assignToShadow(RenderPipelines.WATER_MASK, p -> ShaderKey.SHADOW_BASIC);
		assignToShadow(RenderPipelines.BEACON_BEAM_OPAQUE, p -> ShaderKey.SHADOW_BEACON_BEAM);
		assignToShadow(RenderPipelines.BEACON_BEAM_TRANSLUCENT, p -> ShaderKey.SHADOW_BEACON_BEAM);
		assignToShadow(RenderPipelines.END_PORTAL, p -> ShaderKey.SHADOW_BLOCK);
		assignToShadow(RenderPipelines.END_GATEWAY, p -> ShaderKey.SHADOW_BLOCK);
		assignToShadow(RenderPipelines.ARMOR_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.LIGHTNING, p -> ShaderKey.SHADOW_LIGHTNING);
		// Check that all shaders are accounted for
		//for (RenderPipeline pipeline : RenderPipelines.getStaticPipelines()) {
		//	if (coreShaderMap.containsKey(pipeline) && !coreShaderMapShadow.containsKey(pipeline)) {
		//		Iris.logger.error("Shader program " + pipeline.getLocation() + " is not accounted for in the shadow list");
		//	}
		//}
	}

	private static ShaderKey getText(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (isBlockEntities(pipeline)) {
			return (ShaderKey.TEXT_BE);
		} else {
			return (ShaderKey.TEXT);
		}
	}

	private static ShaderKey getTextIntensity(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (isBlockEntities(pipeline)) {
			return (ShaderKey.TEXT_INTENSITY_BE);
		} else {
			return (ShaderKey.TEXT_INTENSITY);
		}
	}

	private static void assignToMain(RenderPipeline pipeline, Function<IrisRenderingPipeline, ShaderKey> o) {
		if (coreShaderMap.containsKey(pipeline)) {
			Function<IrisRenderingPipeline, ShaderKey> current = coreShaderMap.get(pipeline);
			ShaderKey currentKey = current.apply(null);
			ShaderKey newKey = o.apply(null);
			if (currentKey != newKey) {
				Iris.logger.warn("Pair already assigned: " + pipeline + " to " + currentKey + " -> " + newKey);
			}
		}

		coreShaderMap.put(pipeline, o);
	}

	private static void assignToShadow(RenderPipeline pipeline, Function<IrisRenderingPipeline, ShaderKey> o) {
		if (coreShaderMapShadow.containsKey(pipeline)) {
			Iris.logger.warn("Pair already assigned: " + pipeline);
		}

		coreShaderMapShadow.put(pipeline, o);
	}

	private static ShaderKey getCutout(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.INSTANCE.isActive()) {
			return (HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE);
		} else if (isBlockEntities(pipeline)) {
			return (ShaderKey.BLOCK_ENTITY_DIFFUSE);
		} else {
			return (ShaderKey.ENTITIES_CUTOUT_DIFFUSE);
		}
	}

	private static ShaderKey getSolid(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.INSTANCE.isActive()) {
			return (HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT : ShaderKey.HAND_TRANSLUCENT);
		} else if (isBlockEntities(pipeline)) {
			return (ShaderKey.BLOCK_ENTITY);
		} else {
			return (ShaderKey.ENTITIES_SOLID);
		}
	}

	private static ShaderKey getTranslucent(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.INSTANCE.isActive()) {
			return (HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE);
		} else if (isBlockEntities(pipeline)) {
			return (ShaderKey.BLOCK_ENTITY);
		} else {
			return (ShaderKey.ENTITIES_TRANSLUCENT);
		}
	}

	@Nullable
	public static ShaderKey getPipeline(IrisRenderingPipeline pipeline, RenderPipeline shader) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return coreShaderMapShadow.getOrDefault(shader, FAKE_FUNCTION).apply(pipeline);
		} else {
			return coreShaderMap.getOrDefault(shader, FAKE_FUNCTION).apply(pipeline);
		}
	}

	public static void assignPipeline(RenderPipeline pipeline, ShaderKey programId) {
		if (coreShaderMap.containsKey(pipeline)) {
			throw new IllegalStateException("Shader already assigned: " + pipeline.getLocation() + ": " + programId);
		} else {
			coreShaderMap.put(pipeline, p -> programId);
		}
	}

	public static void copyPipeline(RenderPipeline pipelineToCopy, RenderPipeline returnValue) {
		if (coreShaderMap.containsKey(pipelineToCopy)) {
			coreShaderMap.put(returnValue, coreShaderMap.get(pipelineToCopy));
		}

		if (coreShaderMapShadow.containsKey(pipelineToCopy)) {
			coreShaderMapShadow.put(returnValue, coreShaderMapShadow.get(pipelineToCopy));
		}
	}
}
