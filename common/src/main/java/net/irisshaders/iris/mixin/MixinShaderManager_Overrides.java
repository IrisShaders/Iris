package net.irisshaders.iris.mixin;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderOverrides;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.ShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.irisshaders.iris.pipeline.programs.ShaderOverrides.isBlockEntities;

@Mixin(ShaderManager.class)
public abstract class MixinShaderManager_Overrides {
	@Shadow
	public abstract @Nullable CompiledShaderProgram getProgram(ShaderProgram shaderProgram);

	private static final Function<IrisRenderingPipeline, ShaderKey> FAKE_FUNCTION = p -> null;

	@Unique
	private static final Map<ShaderProgram, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMap = new Object2ObjectArrayMap<>();
	private static final Map<ShaderProgram, Function<IrisRenderingPipeline, ShaderKey>> coreShaderMapShadow = new Object2ObjectArrayMap<>();

	static {
			coreShaderMap.put(CoreShaders.POSITION, p -> ShaderOverrides.getSkyShader((IrisRenderingPipeline) p));
			coreShaderMap.put(CoreShaders.POSITION_TEX, p -> ShaderOverrides.getSkyTexShader((IrisRenderingPipeline) p));
			coreShaderMap.put(CoreShaders.POSITION_TEX_COLOR, p -> ShaderOverrides.getSkyTexColorShader((IrisRenderingPipeline) p));
			coreShaderMap.put(CoreShaders.POSITION_COLOR, p -> ShaderOverrides.getSkyColorShader((IrisRenderingPipeline) p));
			coreShaderMap.put(CoreShaders.PARTICLE, p -> ShaderOverrides.isPhase((IrisRenderingPipeline) p, WorldRenderingPhase.RAIN_SNOW) ? ShaderKey.WEATHER : ShaderKey.PARTICLES);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT, p -> getCutout(p));
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_SOLID, p -> isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.BLOCK_ENTITY : ShaderKey.ENTITIES_SOLID);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ARMOR_CUTOUT_NO_CULL, p -> getCutout(p));
			coreShaderMap.put(CoreShaders.RENDERTYPE_GLINT, p -> ShaderKey.GLINT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_GLINT, p -> ShaderKey.GLINT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_GLINT_TRANSLUCENT, p -> ShaderKey.GLINT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ARMOR_ENTITY_GLINT, p -> ShaderKey.GLINT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL, p -> getCutout(p));
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET, p -> getCutout(p));
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_SMOOTH_CUTOUT, p -> getCutout(p));
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT, MixinShaderManager_Overrides::getTranslucent);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.ENTITIES_EYES_TRANS);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_ALPHA, p -> ShaderKey.ENTITIES_ALPHA);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL, MixinShaderManager_Overrides::getTranslucent);
			coreShaderMap.put(CoreShaders.RENDERTYPE_SOLID, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.MOVING_BLOCK : 		ShaderKey.TERRAIN_SOLID);
			coreShaderMap.put(CoreShaders.RENDERTYPE_CUTOUT, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.MOVING_BLOCK : ShaderKey.TERRAIN_CUTOUT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_CUTOUT_MIPPED, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.MOVING_BLOCK : ShaderKey.TERRAIN_CUTOUT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TRANSLUCENT, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.MOVING_BLOCK : ShaderKey.TERRAIN_TRANSLUCENT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TRIPWIRE, p -> ShaderKey.TERRAIN_TRANSLUCENT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_LINES, p -> ShaderKey.LINES);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TEXT_BACKGROUND, p -> ShaderKey.TEXT_BG);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH, p -> ShaderKey.TEXT_BG);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TEXT, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.TEXT_BE : ShaderKey.TEXT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TEXT_INTENSITY, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.TEXT_INTENSITY_BE : ShaderKey.TEXT_INTENSITY);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.TEXT_INTENSITY_BE : ShaderKey.TEXT_INTENSITY);
			coreShaderMap.put(CoreShaders.RENDERTYPE_EYES, p -> ShaderKey.ENTITIES_EYES);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENTITY_NO_OUTLINE, MixinShaderManager_Overrides::getTranslucent);
			coreShaderMap.put(CoreShaders.RENDERTYPE_BREEZE_WIND, MixinShaderManager_Overrides::getTranslucent);
			coreShaderMap.put(CoreShaders.RENDERTYPE_ENERGY_SWIRL, p -> ShaderKey.ENTITIES_CUTOUT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_BEACON_BEAM, p -> ShaderKey.BEACON);
			coreShaderMap.put(CoreShaders.RENDERTYPE_LIGHTNING, p -> ShaderKey.LIGHTNING);
			coreShaderMap.put(CoreShaders.RENDERTYPE_END_PORTAL, MixinShaderManager_Overrides::getCutout);
			coreShaderMap.put(CoreShaders.RENDERTYPE_END_GATEWAY, MixinShaderManager_Overrides::getCutout);
			coreShaderMap.put(CoreShaders.RENDERTYPE_LEASH, p -> ShaderKey.LEASH);
			coreShaderMap.put(CoreShaders.RENDERTYPE_WATER_MASK, p -> ShaderKey.ENTITIES_CUTOUT);
			coreShaderMap.put(CoreShaders.RENDERTYPE_CLOUDS, p -> ShaderKey.CLOUDS);
			coreShaderMap.put(CloudRenderer.CLOUDS, p -> ShaderKey.CLOUDS_SODIUM);
			coreShaderMap.put(CoreShaders.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK, p -> ShaderKey.MOVING_BLOCK);

			coreShaderMapShadow.put(CoreShaders.POSITION, p -> ShaderKey.SHADOW_BASIC);
			coreShaderMapShadow.put(CoreShaders.POSITION_TEX, p -> ShaderKey.SHADOW_TEX);
			coreShaderMapShadow.put(CoreShaders.POSITION_TEX_COLOR, p -> ShaderKey.SHADOW_TEX_COLOR);
			coreShaderMapShadow.put(CoreShaders.POSITION_COLOR, p -> ShaderKey.SHADOW_BASIC_COLOR);
			coreShaderMapShadow.put(CoreShaders.PARTICLE, p -> ShaderKey.SHADOW_PARTICLES);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_SOLID, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ARMOR_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_GLINT_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ARMOR_ENTITY_GLINT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_SMOOTH_CUTOUT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_ALPHA, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_SOLID, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_CUTOUT, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_CUTOUT_MIPPED, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TRANSLUCENT, p -> ShaderOverrides.isBlockEntities((IrisRenderingPipeline) p) ? ShaderKey.SHADOW_ENTITIES_CUTOUT : ShaderKey.SHADOW_TERRAIN_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TRIPWIRE, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_LINES, p -> ShaderKey.SHADOW_LINES);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TEXT_BACKGROUND, p -> ShaderKey.SHADOW_TEXT_BG);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT_BG);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TEXT, p -> ShaderKey.SHADOW_TEXT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TEXT_INTENSITY, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH, p -> ShaderKey.SHADOW_TEXT_INTENSITY);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_EYES, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENTITY_NO_OUTLINE, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_BREEZE_WIND, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_ENERGY_SWIRL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_BEACON_BEAM, p -> ShaderKey.SHADOW_BEACON_BEAM);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_LIGHTNING, p -> ShaderKey.SHADOW_LIGHTNING);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_END_PORTAL, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_END_GATEWAY, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_LEASH, p -> ShaderKey.SHADOW_LEASH);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_WATER_MASK, p -> ShaderKey.SHADOW_ENTITIES_CUTOUT);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_CLOUDS, p -> ShaderKey.SHADOW_CLOUDS);
			coreShaderMapShadow.put(CloudRenderer.CLOUDS, p -> ShaderKey.SHADOW_CLOUDS);
			coreShaderMapShadow.put(CoreShaders.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK, p -> ShaderKey.SHADOW_TERRAIN_CUTOUT);

		// Check that all shaders are accounted for
		for (ShaderProgram program : CoreShaders.getProgramsToPreload()) {
			if (coreShaderMap.containsKey(program) && !coreShaderMapShadow.containsKey(program)) {
				throw new IllegalStateException("Shader program " + program + " is not accounted for in the shadow list");
			}
		}
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

	@Inject(method = "getProgram", at = @At(value = "HEAD"), cancellable = true)
	private void redirectIrisProgram(ShaderProgram shaderProgram, CallbackInfoReturnable<CompiledShaderProgram> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof IrisRenderingPipeline irisPipeline && irisPipeline.shouldOverrideShaders()) {
			ShaderProgram newProgram = shaderProgram;

			if (newProgram == ShaderAccess.MEKANISM_FLAME) {
				CompiledShaderProgram result = irisPipeline.getShaderMap().getShader(ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? ShaderKey.MEKANISM_FLAME_SHADOW : ShaderKey.MEKANISM_FLAME);

				if (result != null) cir.setReturnValue(result);
			} else if (shaderProgram == ShaderAccess.MEKASUIT) {
				newProgram = CoreShaders.RENDERTYPE_ENTITY_CUTOUT;
			}

			CompiledShaderProgram program = override(irisPipeline, newProgram);

			if (program != null) {
				cir.setReturnValue(program);
			}
		} else {
			if (shaderProgram == ShaderAccess.MEKANISM_FLAME) {
				cir.setReturnValue(getProgram(CoreShaders.POSITION_TEX_COLOR));
			} else if (shaderProgram == ShaderAccess.MEKASUIT) {
				cir.setReturnValue(getProgram(CoreShaders.RENDERTYPE_ENTITY_CUTOUT));
			} else if (shaderProgram == ShaderAccess.IE_COMPAT) {
				// TODO when IE updates
			}
		}
	}

	private static CompiledShaderProgram override(IrisRenderingPipeline pipeline, ShaderProgram shaderProgram) {
		ShaderKey shaderKey = convertToShaderKey(pipeline, shaderProgram);

		return shaderKey == null ? null : pipeline.getShaderMap().getShader(shaderKey);
	}

	private static ShaderKey convertToShaderKey(IrisRenderingPipeline pipeline, ShaderProgram shaderProgram) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered()? coreShaderMapShadow.getOrDefault(shaderProgram, FAKE_FUNCTION).apply(pipeline) : coreShaderMap.getOrDefault(shaderProgram, FAKE_FUNCTION).apply(pipeline);
	}
}
