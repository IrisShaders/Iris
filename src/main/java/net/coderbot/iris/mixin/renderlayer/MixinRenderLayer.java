package net.coderbot.iris.mixin.renderlayer;

import java.util.Optional;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.coderbot.iris.layer.ProgramRenderLayer;
import net.coderbot.iris.layer.UseProgramRenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinRenderLayer implements ProgramRenderLayer {
	@Shadow
	@Final
	@Mutable
	private static RenderLayer SOLID;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT_MIPPED;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRANSLUCENT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRIPWIRE;

	@Unique
	private static RenderLayer iris$LINES;

	@Shadow @Final @Mutable private static RenderLayer LEASH;
	@Shadow @Final @Mutable private static RenderLayer ARMOR_GLINT;
	@Shadow @Final @Mutable private static RenderLayer ARMOR_ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderLayer GLINT_TRANSLUCENT;
	@Shadow @Final @Mutable private static RenderLayer GLINT;
	@Shadow @Final @Mutable private static RenderLayer DIRECT_GLINT;
	@Shadow @Final @Mutable private static RenderLayer ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderLayer DIRECT_ENTITY_GLINT;

	@Shadow @Final @Mutable private static RenderLayer TRANSLUCENT_MOVING_BLOCK;

	@Override
	public Optional<GbufferProgram> getProgram() {
		// By default, don't use shaders to render content
		return Optional.empty();
	}

	static {
		SOLID = wrap("iris:terrain_solid", SOLID, GbufferProgram.TERRAIN);
		CUTOUT_MIPPED = wrap("iris:terrain_cutout_mipped", CUTOUT_MIPPED, GbufferProgram.TERRAIN);
		CUTOUT = wrap("iris:terrain_cutout", CUTOUT, GbufferProgram.TERRAIN);
		TRANSLUCENT = wrap("iris:translucent", TRANSLUCENT, GbufferProgram.TRANSLUCENT_TERRAIN);
		TRIPWIRE = wrap("iris:tripwire", TRIPWIRE, GbufferProgram.TRANSLUCENT_TERRAIN);
		// TODO: figure out how to assign to RenderLayer.LINES
		// We cannot use @Shadow easily because the type of the field is a package-private class
		iris$LINES = wrap("iris:lines", RenderLayer.LINES, GbufferProgram.BASIC);

		// TODO: SOLID / CUTOUT_MIPPED / CUTOUT are used for falling blocks and blocks being pushed by pistons
		// Should they still be rendered in terrain?
		TRANSLUCENT_MOVING_BLOCK = wrap("iris:translucent_moving_block", TRANSLUCENT_MOVING_BLOCK, GbufferProgram.BLOCK_ENTITIES);
		// TODO: TRANSLUCENT_NO_CRUMBLING, doesn't appear to be used

		LEASH = wrap("iris:leash", LEASH, GbufferProgram.BASIC);
		// TODO: Should WATER_MASK be wrapped?
		ARMOR_GLINT = wrapGlint("armor", ARMOR_GLINT);
		ARMOR_ENTITY_GLINT = wrapGlint("armor_entity", ARMOR_ENTITY_GLINT);
		GLINT_TRANSLUCENT = wrapGlint("translucent", GLINT_TRANSLUCENT);
		GLINT = wrapGlint(null, GLINT);
		DIRECT_GLINT = wrapGlint("direct", DIRECT_GLINT);
		ENTITY_GLINT = wrapGlint("entity", ENTITY_GLINT);
		DIRECT_ENTITY_GLINT = wrapGlint("direct_entity_glint", DIRECT_ENTITY_GLINT);

		// TODO: crumbling, text, text_see_through, lightning, end_portal
	}

	/**
	 * @author coderbot
	 * @reason Use the wrapped render layer instead.
	 */
	@Overwrite
	public static RenderLayer getLines() {
		return iris$LINES;
	}

	private static RenderLayer wrap(String name, RenderLayer wrapped, GbufferProgram program) {
		return new IrisRenderLayerWrapper(name, wrapped, new UseProgramRenderPhase(program));
	}

	private static RenderLayer wrapGlint(String glintType, RenderLayer wrapped) {
		String name = ((RenderPhaseAccessor) wrapped).getName();

		String wrappedName = "iris:" + glintType + "_glint";

		if (glintType == null) {
			wrappedName = "iris:glint";
		}

		return wrap(wrappedName, wrapped, GbufferProgram.ARMOR_GLINT);
	}

	private static RenderLayer wrap(RenderLayer wrapped, GbufferProgram program) {
		String name = ((RenderPhaseAccessor) wrapped).getName();

		return wrap("iris:" + name, wrapped, program);
	}

	@Inject(at = @At("RETURN"), method = {
		"getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntitySolid(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getItemEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntitySmoothCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityDecal(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityNoOutline(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityShadow(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityRenderLayers(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityCutoutNoCullZOffset(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityTranslucent(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityRenderLayersZ(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEntityAlpha(Lnet/minecraft/util/Identifier;F)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityAlpha(Identifier texture, float alpha, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getBeaconBeam(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapBeaconBeam(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.BEACON_BEAM));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEyes(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapEyes(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.EYES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEnergySwirl(Lnet/minecraft/util/Identifier;FF)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapEnergySwirl(Identifier texture, float x, float y, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		// TODO: What render layer to use for this? It's used by charged creepers and withers.
		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getOutline(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/RenderPhase$Cull;)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapGlowingOutline(Identifier texture, RenderPhase.Cull cull, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		// TODO: This only appears to be used for part of glowing entities. Verify that it's used for all
		// glowing stuff.
		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES_GLOWING));
	}

	@Inject(at = @At("RETURN"), method = {
		"getBlockBreaking(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapBlockBreakingRenderLayer(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.DAMAGED_BLOCKS));
	}
}
