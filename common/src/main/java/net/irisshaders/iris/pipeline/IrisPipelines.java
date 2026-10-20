package net.irisshaders.iris.pipeline;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import net.irisshaders.iris.pipeline.programs.ShaderOverrides;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class IrisPipelines {
	private static final Map<RenderPipeline, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMap = new ConcurrentHashMap<>();
	private static final Map<RenderPipeline, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMapShadow = new ConcurrentHashMap<>();
	private static final Map<RenderPipeline, List<ShaderKey>> coreShaderVariants = new ConcurrentHashMap<>();
	private static final Object2BooleanMap<RenderPipeline> sodiumPipelines = Object2BooleanMaps.synchronize(new Object2BooleanOpenHashMap<>());
	private static final AtomicInteger version = new AtomicInteger();
	private static final Function<IrisRenderingPipeline, ShaderKey> FAKE_FUNCTION = p -> null;

	static {
		assignToMain(RenderPipelines.SOLID_BLOCK, p -> ShaderKey.TERRAIN_SOLID);
		assignToMain(RenderPipelines.CUTOUT_BLOCK, p -> ShaderKey.TERRAIN_CUTOUT);
		assignToMain(RenderPipelines.SOLID_TERRAIN, p -> ShaderKey.TERRAIN_SOLID);
		assignToMain(RenderPipelines.CUTOUT_TERRAIN, p -> ShaderKey.TERRAIN_CUTOUT);
		assignToMain(RenderPipelines.TRANSLUCENT_TERRAIN, p -> ShaderKey.TERRAIN_TRANSLUCENT);
		assignToMain(RenderPipelines.TRANSLUCENT_BLOCK, p -> ShaderKey.MOVING_BLOCK);
		assignToMain(RenderPipelines.WORLD_BORDER, p -> ShaderKey.TEXTURED);
		assignToMain(RenderPipelines.ENTITY_CUTOUT, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ENTITY_CUTOUT_CULL, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ENTITY_CUTOUT_DISSOLVE, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ENTITY_TRANSLUCENT_CULL, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ITEM_TRANSLUCENT, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ITEM_CUTOUT, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignGlint(RenderPipelines.ITEM_CUTOUT_GLINT, ShaderKey.ENTITIES_CUTOUT_GLINT);
		assignGlint(RenderPipelines.ENTITY_SOLID_GLINT, ShaderKey.ENTITIES_SOLID_GLINT);
		assignGlint(RenderPipelines.ITEM_TRANSLUCENT_GLINT, ShaderKey.ENTITIES_TRANSLUCENT_GLINT);
		assignGlint(RenderPipelines.ITEM_TRANSLUCENT_GLINT_SPECIAL, ShaderKey.ENTITIES_TRANSLUCENT_GLINT_SPECIAL);
		assignGlint(RenderPipelines.ITEM_CUTOUT_GLINT_SPECIAL, ShaderKey.ENTITIES_CUTOUT_GLINT_SPECIAL);
		assignToMain(RenderPipelines.ENTITY_TRANSLUCENT, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ENTITY_SHADOW, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.LINES, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.LINES_TRANSLUCENT, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.LINES_TRANSLUCENT_NO_DEPTH_WRITE, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.LINES_DEPTH_BIAS, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.SECONDARY_BLOCK_OUTLINE, p -> ShaderKey.LINES);
		assignToMain(RenderPipelines.DEBUG_FILLED_BOX, p -> ShaderKey.BASIC_COLOR);
		assignToMain(RenderPipelines.DEBUG_QUADS, p -> ShaderKey.BASIC_COLOR);
		assignToMain(RenderPipelines.DEBUG_TRIANGLE_FAN, p -> ShaderKey.BASIC_COLOR);
		assignToMain(RenderPipelines.STARS, p -> ShaderKey.SKY_BASIC);
		assignToMain(RenderPipelines.SUNRISE_SUNSET, p -> ShaderKey.SKY_BASIC_COLOR);
		assignToMain(RenderPipelines.SKY, p -> ShaderKey.SKY_BASIC);
		assignToMain(RenderPipelines.CELESTIAL, p -> ShaderKey.SKY_TEXTURED);
		assignToMain(RenderPipelines.OPAQUE_PARTICLE, p -> ShaderKey.PARTICLES);
		assignToMain(RenderPipelines.TRANSLUCENT_PARTICLE, p -> ShaderKey.PARTICLES_TRANS);
		assignToMain(RenderPipelines.WATER_MASK, p -> ShaderKey.BASIC);
		assignToMain(RenderPipelines.GLINT, p -> ShaderKey.GLINT);
		assignToMain(RenderPipelines.ARMOR_CUTOUT_NO_CULL, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignGlint(RenderPipelines.ARMOR_CUTOUT_NO_CULL_GLINT, ShaderKey.ENTITIES_CUTOUT_GLINT_ARMOR);
		assignToMain(RenderPipelines.WOLF_ARMOR_CRACKS, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.EYES, p -> ShaderKey.ENTITIES_EYES);
		assignToMain(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.ENTITIES_EYES_TRANS);
		assignToMain(RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, p -> getCutout(p), ShaderKey.BLOCK_ENTITY_DIFFUSE, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.BREEZE_WIND, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.ENTITY_SOLID, p -> getSolid(p), ShaderKey.BLOCK_ENTITY, ShaderKey.HAND_CUTOUT, ShaderKey.HAND_TRANSLUCENT);
		assignToMain(RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, p -> getSolid(p), ShaderKey.BLOCK_ENTITY, ShaderKey.HAND_CUTOUT, ShaderKey.HAND_TRANSLUCENT);
		assignToMain(RenderPipelines.END_GATEWAY, p -> ShaderKey.BLOCK_ENTITY);
		assignToMain(RenderPipelines.ENERGY_SWIRL, p -> ShaderKey.ENTITIES_CUTOUT);
		assignToMain(RenderPipelines.END_CRYSTAL_BEAM, p -> ShaderKey.ENTITIES_CUTOUT);
		assignToMain(RenderPipelines.ENTITY_CUTOUT_Z_OFFSET, p -> ShaderKey.ENTITIES_CUTOUT);
		assignToMain(RenderPipelines.LIGHTNING, p -> ShaderKey.LIGHTNING);
		assignToMain(RenderPipelines.DRAGON_RAYS, p -> ShaderKey.LIGHTNING);
		assignToMain(RenderPipelines.BEACON_BEAM_OPAQUE, p -> ShaderKey.BEACON);
		assignToMain(RenderPipelines.BEACON_BEAM_TRANSLUCENT, p -> ShaderKey.BEACON);
		assignToMain(RenderPipelines.END_PORTAL, p -> ShaderKey.BLOCK_ENTITY);
		assignToMain(RenderPipelines.END_SKY, p -> ShaderKey.SKY_TEXTURED);
		assignToMain(RenderPipelines.TEXT, p -> getText(p), ShaderKey.TEXT_BE, ShaderKey.HAND_TEXT, ShaderKey.HAND_TEXT_TRANSLUCENT);
		assignToMain(RenderPipelines.TEXT_POLYGON_OFFSET, p -> getText(p), ShaderKey.TEXT_BE, ShaderKey.HAND_TEXT, ShaderKey.HAND_TEXT_TRANSLUCENT);
		assignToMain(RenderPipelines.TEXT_SEE_THROUGH, p -> getText(p), ShaderKey.TEXT_BE, ShaderKey.HAND_TEXT, ShaderKey.HAND_TEXT_TRANSLUCENT);
		assignToMain(RenderPipelines.TEXT_GRAYSCALE_SEE_THROUGH, p -> getTextIntensity(p), ShaderKey.TEXT_INTENSITY_BE);
		assignToMain(RenderPipelines.TEXT_GRAYSCALE, p -> getTextIntensity(p), ShaderKey.TEXT_INTENSITY_BE);
		assignToMain(RenderPipelines.TEXT_GRAYSCALE_POLYGON_OFFSET, p -> getTextIntensity(p), ShaderKey.TEXT_INTENSITY_BE);
		assignToMain(RenderPipelines.CRUMBLING, p -> ShaderKey.CRUMBLING);
		assignToMain(RenderPipelines.LEASH, p -> ShaderKey.LEASH);
		assignToMain(RenderPipelines.CLOUDS, p -> ShaderKey.CLOUDS);
		assignToMain(RenderPipelines.FLAT_CLOUDS, p -> ShaderKey.CLOUDS);
		assignToMain(RenderPipelines.BANNER_PATTERN, p -> getTranslucent(p), ShaderKey.BE_TRANSLUCENT, ShaderKey.HAND_CUTOUT_DIFFUSE, ShaderKey.HAND_WATER_DIFFUSE);
		assignToMain(RenderPipelines.WEATHER, p -> ShaderKey.WEATHER);

		assignToShadow(RenderPipelines.SOLID_BLOCK, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.SOLID_TERRAIN, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.CUTOUT_TERRAIN, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.TRANSLUCENT_TERRAIN, p -> ShaderKey.SHADOW_TRANSLUCENT);
		assignToShadow(RenderPipelines.CUTOUT_BLOCK, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
		assignToShadow(RenderPipelines.TRANSLUCENT_BLOCK, p -> ShaderKey.SHADOW_TRANSLUCENT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ARMOR_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ARMOR_CUTOUT_NO_CULL_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.WOLF_ARMOR_CRACKS, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_SOLID, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_SOLID_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.CRUMBLING, p -> ShaderKey.SHADOW_TEX);
		assignToShadow(RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_CUTOUT_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_CUTOUT_GLINT_SPECIAL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_TRANSLUCENT_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ITEM_TRANSLUCENT_GLINT_SPECIAL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT_DISSOLVE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_TRANSLUCENT_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.END_CRYSTAL_BEAM, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_CUTOUT_Z_OFFSET, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.BREEZE_WIND, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.EYES, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.BANNER_PATTERN, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.WOLF_ARMOR_CRACKS, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);

		assignToShadow(RenderPipelines.ENERGY_SWIRL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
		assignToShadow(RenderPipelines.OPAQUE_PARTICLE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.TRANSLUCENT_PARTICLE, p -> ShaderKey.SHADOW_PARTICLES);
		assignToShadow(RenderPipelines.LINES, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.LINES_TRANSLUCENT, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.LINES_TRANSLUCENT_NO_DEPTH_WRITE, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.LINES_DEPTH_BIAS, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.DEBUG_FILLED_BOX, p -> ShaderKey.SHADOW_BASIC_COLOR);
		assignToShadow(RenderPipelines.DEBUG_QUADS, p -> ShaderKey.SHADOW_BASIC_COLOR);
		assignToShadow(RenderPipelines.DEBUG_TRIANGLE_FAN, p -> ShaderKey.SHADOW_BASIC_COLOR);
		assignToShadow(RenderPipelines.LEASH, p -> ShaderKey.SHADOW_LEASH);
		assignToShadow(RenderPipelines.SECONDARY_BLOCK_OUTLINE, p -> ShaderKey.SHADOW_LINES);
		assignToShadow(RenderPipelines.TEXT, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_POLYGON_OFFSET, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT);
		assignToShadow(RenderPipelines.TEXT_GRAYSCALE_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
		assignToShadow(RenderPipelines.TEXT_GRAYSCALE, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
		assignToShadow(RenderPipelines.TEXT_GRAYSCALE_POLYGON_OFFSET, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
		assignToShadow(RenderPipelines.WATER_MASK, p -> ShaderKey.SHADOW_BASIC);
		assignToShadow(RenderPipelines.BEACON_BEAM_OPAQUE, p -> ShaderKey.SHADOW_BEACON_BEAM);
		assignToShadow(RenderPipelines.BEACON_BEAM_TRANSLUCENT, p -> ShaderKey.SHADOW_BEACON_BEAM);
		assignToShadow(RenderPipelines.END_PORTAL, p -> ShaderKey.SHADOW_BLOCK);
		assignToShadow(RenderPipelines.END_GATEWAY, p -> ShaderKey.SHADOW_BLOCK);
		assignToShadow(RenderPipelines.LIGHTNING, p -> ShaderKey.SHADOW_LIGHTNING);
		assignToShadow(RenderPipelines.DRAGON_RAYS, p -> ShaderKey.SHADOW_LIGHTNING);
	}

	private static ShaderKey getText(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.getInstance().isActive()) {

			return (HandRenderer.getInstance().isRenderingSolid() ? ShaderKey.HAND_TEXT : ShaderKey.HAND_TEXT_TRANSLUCENT);
		} else if (ShaderOverrides.isBlockEntities(pipeline)) {
			return (ShaderKey.TEXT_BE);
		} else {
			return (ShaderKey.TEXT);
		}
	}

	private static ShaderKey getTextIntensity(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (ShaderOverrides.isBlockEntities(pipeline)) {
			return (ShaderKey.TEXT_INTENSITY_BE);
		} else {
			return (ShaderKey.TEXT_INTENSITY);
		}
	}

	private static void assignToMain(RenderPipeline pipeline, Function<IrisRenderingPipeline, ShaderKey> o, ShaderKey... alternatives) {
		if (coreShaderMap.containsKey(pipeline)) {
			Function<IrisRenderingPipeline, ShaderKey> current = coreShaderMap.get(pipeline);
			ShaderKey currentKey = current.apply(null);
			ShaderKey newKey = o.apply(null);
			if (currentKey != newKey) {
				Iris.logger.warn("Pair already assigned: " + pipeline + " to " + currentKey + " -> " + newKey);
			}
		}

		coreShaderMap.put(pipeline, o);
		coreShaderVariants.put(pipeline, List.of(alternatives));
		version.incrementAndGet();
	}

	private static void assignToShadow(RenderPipeline pipeline, Function<IrisRenderingPipeline, ShaderKey> o) {
		if (coreShaderMapShadow.containsKey(pipeline)) {
			Iris.logger.warn("Pair already assigned: " + pipeline);
		}

		coreShaderMapShadow.put(pipeline, o);
		version.incrementAndGet();
	}

	private static void assignGlint(RenderPipeline pipeline, ShaderKey key) {
		assignToMain(pipeline, p -> getGlint(key), getGlint(key, true), getGlint(key, false));
	}

	private static ShaderKey getGlint(ShaderKey key) {
		if (!HandRenderer.getInstance().isActive()) {
			return key;
		}

		return getGlint(key, HandRenderer.getInstance().isRenderingSolid());
	}

	private static ShaderKey getGlint(ShaderKey key, boolean solid) {
		return switch (key) {
			case ENTITIES_CUTOUT_GLINT_SPECIAL, ENTITIES_TRANSLUCENT_GLINT_SPECIAL ->
				solid ? ShaderKey.HAND_CUTOUT_GLINT_SPECIAL : ShaderKey.HAND_TRANSLUCENT_GLINT_SPECIAL;
			case ENTITIES_CUTOUT_GLINT_ARMOR -> solid ? ShaderKey.HAND_CUTOUT_GLINT_ARMOR : ShaderKey.HAND_TRANSLUCENT_GLINT_ARMOR;
			case ENTITIES_SOLID_GLINT -> solid ? ShaderKey.HAND_CUTOUT_GLINT : ShaderKey.HAND_TRANSLUCENT_GLINT;
			default -> solid ? ShaderKey.HAND_CUTOUT_GLINT_DIFFUSE : ShaderKey.HAND_TRANSLUCENT_GLINT_DIFFUSE;
		};
	}

	private static ShaderKey getCutout(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.getInstance().isActive()) {
			return (HandRenderer.getInstance().isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE);
		} else if (ShaderOverrides.isBlockEntities(pipeline)) {
			return (ShaderKey.BLOCK_ENTITY_DIFFUSE);
		} else {
			return (ShaderKey.ENTITIES_CUTOUT_DIFFUSE);
		}
	}

	private static ShaderKey getSolid(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.getInstance().isActive()) {
			return (HandRenderer.getInstance().isRenderingSolid() ? ShaderKey.HAND_CUTOUT : ShaderKey.HAND_TRANSLUCENT);
		} else if (ShaderOverrides.isBlockEntities(pipeline)) {
			return (ShaderKey.BLOCK_ENTITY);
		} else {
			return (ShaderKey.ENTITIES_SOLID);
		}
	}

	private static ShaderKey getTranslucent(Object p) {
		IrisRenderingPipeline pipeline = (IrisRenderingPipeline) p;

		if (HandRenderer.getInstance().isActive()) {
			return (HandRenderer.getInstance().isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE);
		} else if (ShaderOverrides.isBlockEntities(pipeline)) {
			return (ShaderKey.BE_TRANSLUCENT);
		} else {
			return (ShaderKey.ENTITIES_TRANSLUCENT);
		}
	}

	@Nullable
	public static ShaderKey getPipeline(IrisRenderingPipeline pipeline, RenderPipeline shader) {
		return getPipeline(pipeline, shader, ShadowRenderingState.areShadowsCurrentlyBeingRendered()
			? WorldRenderTargets.Phase.SHADOW : WorldRenderTargets.Phase.OPAQUE);
	}

	@Nullable
	public static ShaderKey getPipeline(IrisRenderingPipeline pipeline, RenderPipeline shader, WorldRenderTargets.Phase phase) {
		boolean shadow = phase == WorldRenderTargets.Phase.SHADOW;
		if (shader.getLocation().getNamespace().contains("sodium")) {
			return sodiumPipeline(shader, shadow);
		}
		if (shadow) {
			return coreShaderMapShadow.getOrDefault(shader, FAKE_FUNCTION).apply(pipeline);
		} else {
			return coreShaderMap.getOrDefault(shader, FAKE_FUNCTION).apply(pipeline);
		}
	}

	private static ShaderKey sodiumPipeline(RenderPipeline shader, boolean shadow) {
		if (shader.getColorTargetStates().getFirst().blendFunction().isPresent()) {
			return shadow ? ShaderKey.SHADOW_SODIUM_TERRAIN_TRANSLUCENT : ShaderKey.SODIUM_TERRAIN_TRANSLUCENT;
		} else if (shader.getShaderDefines().asSourceDirectives().contains("CUTOUT")) {
			return shadow ? ShaderKey.SHADOW_SODIUM_TERRAIN_CUTOUT : ShaderKey.SODIUM_TERRAIN_CUTOUT;
		} else {
			return shadow ? ShaderKey.SHADOW_SODIUM_TERRAIN_SOLID : ShaderKey.SODIUM_TERRAIN_SOLID;
		}
	}

	private static void forEachMapping(IrisRenderingPipeline pipeline, BiConsumer<RenderPipeline, ShaderKey> consumer) {
		for (var assignments : List.of(coreShaderMap, coreShaderMapShadow)) {
			assignments.forEach((draw, keySupplier) -> {
				var key = keySupplier.apply(pipeline);
				if (key != null) {
					consumer.accept(draw, key);
				}
			});
		}
		coreShaderVariants.forEach((draw, keys) -> keys.forEach(key -> consumer.accept(draw, key)));
		for (var draw : sodiumPipelines.keySet().toArray(RenderPipeline[]::new)) {
			consumer.accept(draw, sodiumPipeline(draw, false));
			consumer.accept(draw, sodiumPipeline(draw, true));
		}
	}

	public static void preparePrograms(IrisRenderingPipeline pipeline, ShaderMap shaders) {
		forEachMapping(pipeline, (draw, key) -> {
			var program = shaders.getShader(key);
			if (program != null) {
				program.prepare(IrisVertexFormats.getExtendedFormats(draw));
			}
		});
	}

	public static void preparePipelines(IrisRenderingPipeline pipeline, ShaderMap shaders) {
		forEachMapping(pipeline, (draw, key) -> {
			var program = shaders.getShader(key);
			if (program != null) {
				try {
					program.preparePipelines(draw);
				} catch (RuntimeException failure) {
					Iris.logger.warn("Failed to prepare render pipeline {} for {}", draw.getLocation(), key, failure);
				}
			}
		});
	}

	public static int getVersion() {
		return version.get();
	}

	public static void registerPipeline(RenderPipeline pipeline) {
		if (pipeline.getLocation().getNamespace().contains("sodium") && !sodiumPipelines.putIfAbsent(pipeline, true)) {
			version.incrementAndGet();
		}
	}

	public static void assignPipeline(RenderPipeline pipeline, ShaderKey programId) {
		if (coreShaderMap.containsKey(pipeline)) {
			throw new IllegalStateException("Shader already assigned: " + pipeline.getLocation() + ": " + programId);
		} else {
			assignToMain(pipeline, p -> programId);
		}
	}

	public static void assignPipelineShadow(RenderPipeline pipeline, ShaderKey programId) {
		if (coreShaderMapShadow.containsKey(pipeline)) {
			throw new IllegalStateException("Shadow shader already assigned: " + pipeline.getLocation() + ": " + programId);
		} else {
			assignToShadow(pipeline, p -> programId);
		}
	}

	public static void copyPipeline(RenderPipeline pipelineToCopy, RenderPipeline returnValue) {
		if (coreShaderMap.containsKey(pipelineToCopy)) {
			coreShaderMap.put(returnValue, coreShaderMap.get(pipelineToCopy));
			coreShaderVariants.put(returnValue, coreShaderVariants.getOrDefault(pipelineToCopy, List.of()));
			version.incrementAndGet();
		}

		if (coreShaderMapShadow.containsKey(pipelineToCopy)) {
			coreShaderMapShadow.put(returnValue, coreShaderMapShadow.get(pipelineToCopy));
			version.incrementAndGet();
		}
	}
}
